package com.springboot.cloud.auth.client.entity.jwt;

import lombok.Data;

@Data
public class JWTParseResponse {

    // 返回码 0成功，-1已失效
    private String code;

    // 返回描述
    private String message;

    // 解密后的payload数据
    private String pdata;

    @Override
    public String toString() {
        return "JWTParseResponse{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", pdata=" + pdata +
                '}';
    }
}
