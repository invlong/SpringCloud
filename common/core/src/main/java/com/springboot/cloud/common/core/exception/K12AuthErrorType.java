package com.springboot.cloud.common.core.exception;

import lombok.Getter;

@Getter
public enum K12AuthErrorType implements ErrorType {

    AUTH_WRONG_TOKEN("060000", "非法请求"),
    AUTH_EXPIRE("060001", "登录时效已过期，请重新登录"),
    AUTH_ROLE_CHANGE("060002", "账号权限有变动，请重新登录"),
    AUTH_RE_LOGIN("060003", "账号在别处登录，请重新登录"),
    AUTH_WRONG_ACCOUNT("060004", "账号或密码错误"),
    AUTH_WRONG_CLIENT("060005","客户端错误，请确认");

    /**
     * 错误类型码
     */
    private String code;
    /**
     * 错误类型描述信息
     */
    private String msg;

    K12AuthErrorType(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
