package com.springboot.cloud.auth.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.ConcurrentHashMap;

@Data
@ConfigurationProperties(prefix = "jwt-custom")
public class JWTProperties {

    // 签名用的key，多个用","分割
    private String secretKey;

    // 加密用的key，多个用","分割，key和iv用"&"分割
    private String secretPkey;

    private boolean active;

    private String apiToken;

    private String filter;

    private ConcurrentHashMap<String, String> userRoles;

    public ConcurrentHashMap<String, String> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(ConcurrentHashMap<String, String> userRoles) {
        this.userRoles = userRoles;
    }
}
