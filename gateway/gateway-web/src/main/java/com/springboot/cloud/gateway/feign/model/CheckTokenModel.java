package com.springboot.cloud.gateway.feign.model;

import lombok.Data;

@Data
public class CheckTokenModel {

    private String accessToken;

    private String url;

    public CheckTokenModel(String accessToken, String url) {
        this.accessToken = accessToken;
        this.url = url;
    }
}
