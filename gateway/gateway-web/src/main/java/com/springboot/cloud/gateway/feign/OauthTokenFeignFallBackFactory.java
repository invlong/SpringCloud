package com.springboot.cloud.gateway.feign;

import com.alibaba.fastjson.JSONObject;
import com.springboot.cloud.gateway.feign.model.CheckTokenModel;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class OauthTokenFeignFallBackFactory implements FallbackFactory<OauthTokenFeign> {

    @Override
    public OauthTokenFeign create(Throwable e) {
        log.error(e.getMessage(), e);
        return new OauthTokenFeign() {
            @Override
            public Map<String, Object> checkToken(CheckTokenModel checkTokenModel) {
                log.info("OauthTokenFeignFallBack checkToken param is {}", JSONObject.toJSONString(checkTokenModel));
                Map<String, Object> checkResultMap = new HashMap<>();
                checkResultMap.put("code", "-1");
                checkResultMap.put("expired", true);
                checkResultMap.put("tokenType", "bearer");
                checkResultMap.put("value", checkTokenModel.getAccessToken());
                checkResultMap.put("urlPermission", false);
                return checkResultMap;
            }

            @Override
            public Map<String, Object> oauthToken(String grant_type, String client_id, String client_secret) {
                Map<String, Object> checkResultMap = new HashMap<>();
                checkResultMap.put("code", "601");
                checkResultMap.put("accessToken", "");
                checkResultMap.put("tokenType", "bearer");
                checkResultMap.put("expiresIn", "");
                checkResultMap.put("scope", "");
                return checkResultMap;
            }
        };
    }
}
