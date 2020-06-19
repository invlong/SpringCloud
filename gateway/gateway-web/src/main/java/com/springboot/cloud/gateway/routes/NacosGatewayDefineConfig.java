package com.springboot.cloud.gateway.routes;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.springboot.cloud.gateway.model.GatewayRouteDefinition;
import com.springboot.cloud.gateway.service.impl.RouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

@Slf4j
@Component
@NacosPropertySource(dataId = "${nacos.config.data-id}", autoRefreshed = true, groupId = "${nacos.config.group}")
public class NacosGatewayDefineConfig implements CommandLineRunner {

    @Value("${nacos.config.data-id}")
    private String dataId;

    @Value("${spring.cloud.nacos.config.server-addr}")
    private String serverAddr;

    @Value("${spring.cloud.nacos.config.namespace}")
    private String nameSpace;

    @Value("${nacos.config.group}")
    private String group;

    @Autowired
    RouteService routeService;

    @Override
    public void run(String... args) throws Exception {
        addRouteNacosListen();
    }
    private void addRouteNacosListen() {
        try {
            Properties properties = new Properties();
            properties.put("serverAddr",serverAddr);
            properties.put("namespace",nameSpace);
            ConfigService configService = ConfigFactory.createConfigService(properties);
            String configInfo = configService.getConfig(dataId, group, 5000);
            log.info("addRouteNacosListen:从Nacos返回的配置：{}",configInfo);
            getNacosDataRoutes(configInfo);
            //注册Nacos配置更新监听器，用于监听触发
            configService.addListener(dataId, group, new Listener()  {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("addRouteNacosListen:Nacos更新了,接收到数据:{}",configInfo);
                    getNacosDataRoutes(configInfo);
                }
                @Override
                public Executor getExecutor() {
                    return null;
                }
            });
        } catch (NacosException e) {
            log.error("nacos-addListener-error", e);
        }
    }

    private void getNacosDataRoutes(String configInfo) {
        List<GatewayRouteDefinition> list = JSON.parseArray(configInfo, GatewayRouteDefinition.class);
        list.stream().forEach(definition -> {
            routeService.update(definition);
        });
    }
}
