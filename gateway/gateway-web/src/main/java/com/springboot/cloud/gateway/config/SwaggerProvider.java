package com.springboot.cloud.gateway.config;

import com.springboot.cloud.gateway.service.impl.RouteService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.List;

@Component
@Primary
@AllArgsConstructor
@Slf4j
public class SwaggerProvider implements SwaggerResourcesProvider {
    private static final String API_URI = "/v2/api-docs";
    private static final String EXCLUDE = "auth";

    @Autowired
    private final RouteService routeService;

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        routeService.getRouteDefinitions().stream()
                .forEach(routeDefinition -> routeDefinition.getPredicates().stream()
                        .filter(predicateDefinition -> "Path".equalsIgnoreCase(predicateDefinition.getName()))
                        .peek(predicateDefinition -> log.debug("路由配置参数：{}", predicateDefinition.getArgs()))
                        .forEach(predicateDefinition -> {
                            SwaggerResource pattern = swaggerResource(routeDefinition.getId(),
                                    predicateDefinition.getArgs().get("pattern").replace("/**", API_URI));
                            if (null != pattern) {
                                resources.add(pattern);
                            }
                        }));
        log.debug("resources:{}", resources);
        return resources;
    }

    private SwaggerResource swaggerResource(String name, String location) {
        // 排除掉不需要的swagger
        if (name.startsWith(EXCLUDE)) {
            return null;
        }
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;
    }
}