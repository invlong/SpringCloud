package com.springboot.cloud.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.springboot.cloud.auth.client.service.IK12AuthService;
import com.springboot.cloud.common.core.entity.vo.Result;
import com.springboot.cloud.common.core.exception.K12AuthErrorType;
import com.springboot.cloud.gateway.exception.AuthExceptionHandler;
import com.springboot.cloud.gateway.service.IPermissionService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 请求url权限校验
 */
@Configuration
@ComponentScan(basePackages = "com.springboot.cloud.auth.client")
@Slf4j
public class K12AccessGatewayFilter implements GlobalFilter {

    private static final String X_CLIENT_TOKEN_USER = "x-client-token-user";
    private static final String X_CLIENT_TOKEN = "x-client-token";
    private static final String JWT_SCHOOL_ID = "schoolId";
    private static final String UTF8_HEADER = "text/plain;charset=UTF-8";
    private static final String UNAUTHORIZED_TIMESTAMP = "timestamp";
    private static final String UNAUTHORIZED_STATUS = "status";
    private static final String UNAUTHORIZED_ERROR = "error";
    private static final String UNAUTHORIZED_MESSAGE = "message";

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
    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String reqContextId = request.getHeaders().getFirst(GlobalTraceIdContext.REQUESTID_HEADER_KEY);
        if (StringUtils.isNotBlank(reqContextId)) {
            MDC.put(GlobalTraceIdContext.REQUESTID_HEADER_KEY, reqContextId);
        } else {
            reqContextId = GlobalTraceIdContext.getUUID();
            MDC.put(GlobalTraceIdContext.REQUESTID_HEADER_KEY, reqContextId);
        }
        GlobalTraceIdContext.setRequestId(reqContextId);
        String finalReqContextId = reqContextId;
        log.info("finalReqContextId is {}",finalReqContextId);
        String authentication = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String method = request.getMethodValue();
        String url = request.getPath().value();
        log.debug("url:{},method:{},headers:{}", url, method, request.getHeaders());
        //不需要网关签权的url
        if (authService.ignoreAuthentication(url)) {
            log.debug("不需要鉴权的url:{}", url);
            if (!Strings.isNullOrEmpty(authentication)) {
                log.debug("兼容处理，白名单接口，但是有token信息，进行解析");
                Result permission = permissionService.permission(authentication, url, method);
                if (permission.isSuccess()) {
                    JSONObject userData = JSON.parseObject(String.valueOf(permission.getData()));
                    ServerHttpRequest.Builder builder = request.mutate();
                    //TODO 转发的请求都加上服务间认证token
                    builder.header(X_CLIENT_TOKEN, "TODO zhoutaoo添加服务间简单认证");
                    builder.header(GlobalTraceIdContext.REQUESTID_HEADER_KEY, finalReqContextId);
                    //将jwt token中的用户信息传给服务
                    builder.header(X_CLIENT_TOKEN_USER, userData.toJSONString());
                    log.debug("转发请求");
                    return chain.filter(exchange.mutate().request(builder.build()).build());
                } else {
                    log.debug("给的token解析失败，不存入用户信息");
                }
            }
            return chain.filter(exchange);
        }
        //调用签权服务看用户是否有权限，若有权限进入下一个filter
        Result permission = permissionService.permission(authentication, url, method);
        if (permission.isSuccess()) {
            JSONObject userData = JSON.parseObject(String.valueOf(permission.getData()));
            ServerHttpRequest.Builder builder = request.mutate();
            //TODO 转发的请求都加上服务间认证token
            builder.header(X_CLIENT_TOKEN, "TODO zhoutaoo添加服务间简单认证");
            builder.header(GlobalTraceIdContext.REQUESTID_HEADER_KEY, finalReqContextId);
            //将jwt token中的用户信息传给服务
            builder.header(X_CLIENT_TOKEN_USER, userData.toJSONString());
            log.debug("转发请求");
            return chain.filter(exchange.mutate().request(builder.build()).build());
        }
        // 增加鉴权失败错误提示
        Map<String, Object> attributes = exchange.getAttributes();
        attributes.put("code", permission.getCode());
        attributes.put("tip", permission.getMsg());
        throw new AuthExceptionHandler();
    }

    private static boolean isJSON(String str) {
        boolean result = false;
        try {
            Object obj = JSON.parse(str);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;
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
    private Mono<Void> unauthorizedResp(ServerWebExchange serverWebExchange, HttpStatus code, String tip) {
        log.debug(tip);
        /*{
            "timestamp": 1594282058018,
                "status": 401,
                "error": "Unauthorized",
                "message": "账号在别处登录，请重新登录",
                "path": "/bepf/user/get_student_list_by_dept"
        }*/
        // 兼容旧的格式
        JSONObject respJson = new JSONObject();
        respJson.put(UNAUTHORIZED_TIMESTAMP, System.currentTimeMillis());
        respJson.put(UNAUTHORIZED_STATUS, null == code ? HttpStatus.UNAUTHORIZED.value() : code.value());
        respJson.put(UNAUTHORIZED_ERROR, HttpStatus.UNAUTHORIZED.getReasonPhrase());
        respJson.put(UNAUTHORIZED_MESSAGE, Strings.isNullOrEmpty(tip) ? HttpStatus.UNAUTHORIZED.getReasonPhrase() : tip);
        serverWebExchange.getResponse().setStatusCode(null == code ? HttpStatus.UNAUTHORIZED : code);
        serverWebExchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, UTF8_HEADER);
        DataBuffer buffer;
        buffer = serverWebExchange.getResponse()
                .bufferFactory().wrap(respJson.toJSONString().getBytes(StandardCharsets.UTF_8));
        return serverWebExchange.getResponse().writeWith(Flux.just(buffer));
    }
}
