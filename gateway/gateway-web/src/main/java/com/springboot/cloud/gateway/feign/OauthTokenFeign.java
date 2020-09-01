package com.springboot.cloud.gateway.feign;


import com.springboot.cloud.gateway.feign.model.AcquireTokenModel;
import com.springboot.cloud.gateway.feign.model.CheckTokenModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "k12-java-oauth", fallbackFactory = OauthTokenFeignFallBackFactory.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public interface OauthTokenFeign {

    @RequestMapping(value = "/oauthTwo/checkToken", method = RequestMethod.POST)
    Map<String, Object> checkToken(@RequestBody CheckTokenModel checkTokenModel);

    @RequestMapping(value = "/oauth/token", method = RequestMethod.POST)
    Map<String, Object> oauthToken(@RequestParam("grant_type") String grant_type, @RequestParam("client_id") String client_id, @RequestParam("client_secret") String client_secret);

}
