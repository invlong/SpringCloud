package com.springboot.cloud.gateway.filter;

import com.springboot.cloud.auth.client.service.IAuthService;
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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/**
 * 请求url权限校验
 */
@Configuration
@ComponentScan(basePackages = "com.springboot.cloud.auth.client")
@ComponentScan
@Slf4j
public class AccessGatewayFilter implements GlobalFilter {

    private static final String X_CLIENT_TOKEN_USER = "x-client-token-user";
    private static final String X_CLIENT_TOKEN = "Token";

    /**
     * 由authentication-client模块提供签权的feign客户端
     */
    @Autowired
    private IAuthService authService;

//    @Autowired
//    private IPermissionService permissionService;

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
        log.info("url:{},method:{},headers:{}", url, method, request.getHeaders());
        //不需要网关签权的url
        if (authService.ignoreAuthentication(url)) {
            return chain.filter(exchange);
        }
        //调用签权服务看用户是否有权限，若有权限进入下一个filter
//        if (permissionService.permission(authentication, url, method)) {
        //TODO 转发的请求都加上服务间认证token
        String token = "Token ZXlKcmFXUWlPaUl3SWl3aWRIbHdJam9pU2xkVUlpd2lZV3huSWpvaVNGTXlOVFlpZlEuZXlKcFlYUWlPakUxT1RJMU16Z3dPVGNzSW5OMVlpSTZJbXh2WjJsdUlpd2lhWE56SWpvaWQyVmtjeUlzSW1WNGNDSTZNVGMwT0RBMU9EQTVOeXdpY0dGc1p5STZJbUZsY3kweU5UWXRZMkpqSWl3aWNHdGxlV2xrSWpvaU1TSXNJbXAwYVNJNklqTXlNakl6TUhkbFlpMWhjR2xzYjJkcGJpSXNJbUYxWkNJNkluZGxZaTFoY0draUxDSndaR0YwWVNJNklqRmtPRGs0WXpneE1qWmpPRGhrTURFMk56VTJZMlkwTURZd1lqazFaamszWlRWaFlqaGhORE5qWkdSbU9HRTRNR1V4TVRnMU1EWTBOV0ZrWWpsak1tWXdOR1ZrWVdFelpUVm1aamhrWWpnMk5tSmpNMkk0TldGbFpUZGtNekpqT1RJNVpHTXpZamxoTldFd05XVmpOR0ZrTXpkbE9UZGlZMlkwTXpJeU9EZ3hJbjAuMkhNTmhwTnp0dWwtZWVUMXpjMXUyMy1ZREcweDlrYTZYYlFwckxnSjFpWQ==";
        //向headers中放文件，记得build
        Consumer<HttpHeaders> httpHeaders = httpHeader -> {
            httpHeader.set(HttpHeaders.AUTHORIZATION, token);
        };
        ServerHttpRequest serverHttpRequest = exchange.getRequest().mutate().headers(httpHeaders).build();
        return chain.filter(exchange.mutate().request(serverHttpRequest).build());
//        }
//        return unauthorized(exchange);
    }

    /**
     * 提取jwt token中的数据，转为json
     *
     * @param authentication
     * @return
     */
    /*private String getUserToken(String authentication) {
        String token = "{}";
        try {
            token = new ObjectMapper().writeValueAsString(authService.getJwt(authentication).getBody());
            return token;
        } catch (JsonProcessingException e) {
            log.error("token json error:{}", e.getMessage());
        }
        return token;
    }*/

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
