package com.springboot.cloud.gateway.controller;


import com.alibaba.fastjson.JSONObject;
import com.springboot.cloud.gateway.model.GatewayRouteDefinition;
import com.springboot.cloud.gateway.service.impl.RouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/route")
@Slf4j
public class RouteController {

    @Autowired
    private RouteService routeService;

    /**
     * 增加路由
     * @param gwdefinition
     * @return
     */
    @PostMapping("/add")
    public String add(@RequestBody GatewayRouteDefinition gwdefinition) {
        try {
            log.info("GatewayRouteDefinition is {}",JSONObject.toJSONString(gwdefinition));
            RouteDefinition definition = routeService.assembleRouteDefinition(gwdefinition);
            return routeService.addDefinition(definition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "succss";
    }


}
