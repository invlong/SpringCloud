package com.springboot.cloud.auth.client.entity.jwt;

import lombok.Data;

@Data
public class JWTCreateResponse {

    // 0为成功，1为用户已登录，-1为其他错误
    private String code;

    // 返回描述信息
    private String message;

    // 存放jwt
    private String data;

    @Override
    public String toString() {
        return "JWTCreateResponse{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
