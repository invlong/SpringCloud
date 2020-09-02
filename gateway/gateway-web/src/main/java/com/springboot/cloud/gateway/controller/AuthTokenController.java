package com.springboot.cloud.gateway.controller;


import com.alibaba.fastjson.JSONObject;
import com.springboot.cloud.gateway.feign.OauthTokenFeign;
import com.springboot.cloud.gateway.feign.model.AcquireTokenModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@Slf4j
public class AuthTokenController {

    @Autowired
    private OauthTokenFeign oauthTokenFeign;

    @PostMapping("/acquireToken")
    public Map<String, Object> acquireToken(@RequestBody Map<String, Object> map) {
        log.info("acquireToken param is {}", JSONObject.toJSONString(map));
        AcquireTokenModel acquireTokenModel = new AcquireTokenModel("client_credentials", String.valueOf(map.get("app_id")), String.valueOf(map.get("app_secret")));
        Map<String, Object> outhResult = oauthTokenFeign.oauthToken(acquireTokenModel.getGrant_type(), acquireTokenModel.getClient_id(), acquireTokenModel.getClient_secret());
        if (!outhResult.containsKey("code")) {
            outhResult.put("code",600);
        }
        log.info("acquireToken result is {}", JSONObject.toJSONString(outhResult));
        return outhResult;
    }
}
