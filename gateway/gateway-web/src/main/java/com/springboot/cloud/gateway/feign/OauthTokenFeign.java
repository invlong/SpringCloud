package com.springboot.cloud.gateway.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "k12-java-oauth", fallbackFactory = OauthTokenFeignFallBackFactory.class)
public interface OauthTokenFeign {

    @RequestMapping(value = "/oauthTwo/checkToken", method = RequestMethod.POST, headers = {"Content-Type=application/json"})
    Map<String, Object> checkToken(@RequestParam("accessToken") String accessToken,
                                   @RequestParam("url") String url);

    @RequestMapping(value = "/oauth/token", method = RequestMethod.POST)
    Map<String, Object> oauthToken(@RequestParam("grant_type") String grant_type, @RequestParam("client_id") String client_id, @RequestParam("client_secret") String client_secret);

}
