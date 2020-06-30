package com.springboot.cloud.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.springboot.cloud.auth.client.entity.dto.LoginParam;
import com.springboot.cloud.auth.client.service.IK12AuthService;
import com.springboot.cloud.common.core.entity.vo.Result;
import com.springboot.cloud.common.core.exception.K12AuthErrorType;
import com.springboot.cloud.gateway.service.IPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * 请求url权限校验
 */
@Configuration
@ComponentScan(basePackages = "com.springboot.cloud.auth.client")
@Slf4j
public class K12AccessGatewayFilter implements GlobalFilter {

    private static final String X_CLIENT_TOKEN_USER = "x-client-token-user";
    private static final String X_CLIENT_TOKEN = "x-client-token";

    private static final String LOGIN_URL = "k12auth/login";
    private static final String LOGIN_NAME = "username";
    private static final String LOGIN_PASSWORD = "password";
    private static final String LOGIN_SCHOOL_ID = "school_id";
    private static final String LOGIN_ROLE_NO = "role_no";
    private static final String LOGIN_CLIENT = "client";
    private static final String LOGIN_GRANT_TYPE = "grant_type";
    private static final String LOGIN_SCOPE = "scope";
    private static final String USER_INFO_ROLE_LIST = "role_list";
    private static final String USER_INFO_TOKEN = "token";

    /**
     * 由authentication-client模块提供签权的feign客户端
     */
    @Autowired
    private IK12AuthService authService;

    @Autowired
    private IPermissionService permissionService;

    /**
     * 1.首先网关检查token是否有效，无效直接返回401，不调用签权服务
     * 2.调用签权服务器看是否对该请求有权限，有权限进入下一个filter，没有权限返回401
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String authentication = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String method = request.getMethodValue();
        String url = request.getPath().value();
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        log.debug("url:{},method:{},headers:{}", url, method, request.getHeaders());
        /*
        特殊处理登录接口，先走登录逻辑，如果登陆者是单角色，增加access_token，
        否则需要用户选择角色后，调用签权接口
        */
        if (url.endsWith(LOGIN_URL)) {
            String username;
            String password;
            String client;
            String grantType;
            String scope;
            Integer schoolId = null;
            String roleNo = null;
            if (queryParams.containsKey(LOGIN_NAME)) {
                username = queryParams.getFirst(LOGIN_NAME);
            } else {
                return customResp(exchange, HttpStatus.UNAUTHORIZED, K12AuthErrorType.AUTH_WRONG_ACCOUNT.getMsg());
            }
            if (queryParams.containsKey(LOGIN_PASSWORD)) {
                password = queryParams.getFirst(LOGIN_PASSWORD);
            } else {
                return customResp(exchange, HttpStatus.UNAUTHORIZED, K12AuthErrorType.AUTH_WRONG_ACCOUNT.getMsg());
            }
            if (queryParams.containsKey(LOGIN_CLIENT)) {
                client = queryParams.getFirst(LOGIN_CLIENT);
            } else {
                return customResp(exchange, HttpStatus.UNAUTHORIZED, K12AuthErrorType.AUTH_WRONG_CLIENT.getMsg());
            }
            if (queryParams.containsKey(LOGIN_GRANT_TYPE)) {
                grantType = queryParams.getFirst(LOGIN_GRANT_TYPE);
            } else {
                return customResp(exchange, HttpStatus.UNAUTHORIZED, K12AuthErrorType.AUTH_WRONG_TOKEN.getMsg());
            }
            if (queryParams.containsKey(LOGIN_SCOPE)) {
                scope = queryParams.getFirst(LOGIN_SCOPE);
            } else {
                return customResp(exchange, HttpStatus.UNAUTHORIZED, K12AuthErrorType.AUTH_WRONG_TOKEN.getMsg());
            }
            if (queryParams.containsKey(LOGIN_SCHOOL_ID)) {
                schoolId = Integer.valueOf(Objects.requireNonNull(queryParams.getFirst(LOGIN_SCHOOL_ID)));
            }
            if (queryParams.containsKey(LOGIN_ROLE_NO)) {
                roleNo = queryParams.getFirst(LOGIN_ROLE_NO);
            }
            LoginParam loginParam = new LoginParam();
            loginParam.setClient(client);
            loginParam.setPassword(password);
            loginParam.setRoleNo(roleNo);
            loginParam.setSchoolId(schoolId);
            loginParam.setUsername(username);
            Result loginResult = authService.login(loginParam);
            if (loginResult.isSuccess()) {
                JSONObject loginData = JSONObject.parseObject(JSONObject.toJSONString(loginResult.getData()));
                if (loginData.getJSONArray(USER_INFO_ROLE_LIST).size() == 1) {
                    // 用户只有一个角色，那么调用授权服务
                    Result tokenResult = authService.token(authentication, username, password, grantType, scope);
                    if (tokenResult.isFail()) {
                        // 失败直接返回授权失败
                        return customResp(exchange, HttpStatus.UNAUTHORIZED, tokenResult.getMsg());
                    }
                    loginData.put(USER_INFO_TOKEN, JSONObject.toJSONString(tokenResult.getData()));
                }
                return customResp(exchange, HttpStatus.OK, JSONObject.toJSONString(loginData));
            } else {
                return customResp(exchange, HttpStatus.OK, JSONObject.toJSONString(loginResult.getData()));
            }
        }
        //不需要网关签权的url
        if (authService.ignoreAuthentication(url)) {
            return chain.filter(exchange);
        }
        //调用签权服务看用户是否有权限，若有权限进入下一个filter
        Result permission = permissionService.permission(authentication, url, method);
        if (permission.isSuccess()) {
            ServerHttpRequest.Builder builder = request.mutate();
            //TODO 转发的请求都加上服务间认证token
            builder.header(X_CLIENT_TOKEN, "TODO zhoutaoo添加服务间简单认证");
            //将jwt token中的用户信息传给服务
            builder.header(X_CLIENT_TOKEN_USER, getUserToken(authentication));
            return chain.filter(exchange.mutate().request(builder.build()).build());
        }
        // 增加鉴权失败错误提示
        HttpStatus code;
        switch (K12AuthErrorType.valueOf(permission.getCode())) {
            case AUTH_EXPIRE:
                code = HttpStatus.NOT_ACCEPTABLE;
                break;
            case AUTH_RE_LOGIN:
                code = HttpStatus.UNAUTHORIZED;
                break;
            case AUTH_ROLE_CHANGE:
                code = HttpStatus.UNAUTHORIZED;
                break;
            case AUTH_WRONG_TOKEN:
                code = HttpStatus.UNAUTHORIZED;
                break;
            default:
                code = HttpStatus.UNAUTHORIZED;
                break;
        }
        return customResp(exchange, code, permission.getMsg());
    }

    /**
     * 提取jwt token中的数据，转为json
     *
     * @param authentication
     * @return
     */
    private String getUserToken(String authentication) {
        String token = "{}";
        try {
            token = new ObjectMapper().writeValueAsString(authService.getJwt(authentication).getBody());
            return token;
        } catch (JsonProcessingException e) {
            log.error("token json error:{}", e.getMessage());
        }
        return token;
    }

    /**
     * 自定义返回
     * @param serverWebExchange
     * @param code
     * @param tip
     * @return
     */
    private Mono<Void> customResp(ServerWebExchange serverWebExchange, HttpStatus code, String tip) {
        serverWebExchange.getResponse().setStatusCode(null == code ? HttpStatus.UNAUTHORIZED : code);
        DataBuffer buffer = serverWebExchange.getResponse()
                .bufferFactory().wrap(Strings.isNullOrEmpty(tip) ? HttpStatus.UNAUTHORIZED.getReasonPhrase().getBytes() : tip.getBytes());
        return serverWebExchange.getResponse().writeWith(Flux.just(buffer));
    }
}
