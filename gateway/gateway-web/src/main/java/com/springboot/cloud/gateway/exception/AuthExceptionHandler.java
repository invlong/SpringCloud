package com.springboot.cloud.gateway.exception;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.springboot.cloud.common.core.exception.K12AuthErrorType;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class AuthExceptionHandler extends Throwable implements ErrorWebExceptionHandler {

	private static final String UTF8_HEADER = "text/plain;charset=UTF-8";
	private static final String UNAUTHORIZED_TIMESTAMP = "timestamp";
	private static final String UNAUTHORIZED_STATUS = "status";
	private static final String UNAUTHORIZED_ERROR = "error";
	private static final String UNAUTHORIZED_MESSAGE = "message";

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		Object num = exchange.getAttribute("code");
		Object tip = exchange.getAttribute("tip");
		HttpStatus code;
		if (null == num) {
			code = HttpStatus.UNAUTHORIZED;
		}else if (num.toString().equals(K12AuthErrorType.AUTH_EXPIRE.getCode())) {
			code = HttpStatus.NOT_ACCEPTABLE;
		} else if (num.toString().equals(K12AuthErrorType.AUTH_RE_LOGIN.getCode())) {
			code = HttpStatus.UNAUTHORIZED;
		} else if (num.toString().equals(K12AuthErrorType.AUTH_ROLE_CHANGE.getCode())) {
			code = HttpStatus.UNAUTHORIZED;
		} else if (num.toString().equals(K12AuthErrorType.AUTH_WRONG_TOKEN.getCode())) {
			code = HttpStatus.UNAUTHORIZED;
		} else {
			code = HttpStatus.UNAUTHORIZED;
		}
		JSONObject respJson = new JSONObject();
		respJson.put(UNAUTHORIZED_TIMESTAMP, System.currentTimeMillis());
		respJson.put(UNAUTHORIZED_STATUS, code.value());
		respJson.put(UNAUTHORIZED_ERROR, HttpStatus.UNAUTHORIZED.getReasonPhrase());
		respJson.put(UNAUTHORIZED_MESSAGE, null == tip ? HttpStatus.UNAUTHORIZED.getReasonPhrase() : tip);
		exchange.getResponse().setStatusCode(code);
		exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, UTF8_HEADER);
		byte[] bytes = respJson.toJSONString().getBytes(StandardCharsets.UTF_8);
		DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
		return exchange.getResponse().writeWith(Flux.just(buffer));
	}
}
