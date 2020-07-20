package com.springboot.cloud.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.cloud.auth.client.service.IAuthService;
import com.springboot.cloud.gateway.service.IPermissionService;
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

import java.util.function.Consumer;

/**
 * 请求url权限校验
 */
@Configuration
@ComponentScan(basePackages = "com.springboot.cloud.auth.client")
@Slf4j
public class AccessGatewayFilter implements GlobalFilter {

    private static final String X_CLIENT_TOKEN_USER = "x-client-token-user";
    private static final String X_CLIENT_TOKEN = "x-client-token";

    /**
     * 由authentication-client模块提供签权的feign客户端
     */
    @Autowired
    private IAuthService authService;

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
        try {
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

            String authentication = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String method = request.getMethodValue();
            String url = request.getPath().value();
            log.debug("finalReqContextId is {}:url:{},method:{},headers:{}", finalReqContextId, url, method, request.getHeaders());
            //不需要网关签权的url
            if (authService.ignoreAuthentication(url)) {
                Consumer<HttpHeaders> httpHeaders = httpHeader -> {
                    httpHeader.set(GlobalTraceIdContext.REQUESTID_HEADER_KEY, finalReqContextId);
                };
                ServerHttpRequest serverHttpRequest = exchange.getRequest().mutate().headers(httpHeaders).build();
                exchange.mutate().request(serverHttpRequest).build();
                return chain.filter(exchange);
            }

            //调用签权服务看用户是否有权限，若有权限进入下一个filter
            if (permissionService.permission(authentication, url, method)) {
                Consumer<HttpHeaders> httpHeaders = httpHeader -> {
                    httpHeader.set(GlobalTraceIdContext.REQUESTID_HEADER_KEY, finalReqContextId);
                    httpHeader.set(X_CLIENT_TOKEN, "TODO zhoutaoo添加服务间简单认证");
                    httpHeader.set(X_CLIENT_TOKEN_USER, getUserToken(authentication));
                };
                ServerHttpRequest serverHttpRequest = exchange.getRequest().mutate().headers(httpHeaders).build();
                exchange.mutate().request(serverHttpRequest).build();
                return chain.filter(exchange);
            }
        } catch (Exception e) {
            log.error("AccessGatewayFilter error", e);
        } finally {
            MDC.clear();
            //避免tomcat回收线程造成过期数据
            GlobalTraceIdContext.setRequestId(null);
        }
        return unauthorized(exchange);
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
     * 网关拒绝，返回401
     *
     * @param
     */
    private Mono<Void> unauthorized(ServerWebExchange serverWebExchange) {
        serverWebExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        DataBuffer buffer = serverWebExchange.getResponse()
                .bufferFactory().wrap(HttpStatus.UNAUTHORIZED.getReasonPhrase().getBytes());
        return serverWebExchange.getResponse().writeWith(Flux.just(buffer));
    }
}
