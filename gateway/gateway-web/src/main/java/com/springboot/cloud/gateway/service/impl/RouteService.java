package com.springboot.cloud.gateway.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.springboot.cloud.gateway.model.GatewayFilterDefinition;
import com.springboot.cloud.gateway.model.GatewayPredicateDefinition;
import com.springboot.cloud.gateway.model.GatewayRouteDefinition;
import com.springboot.cloud.gateway.service.IRouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;

@Service
@Slf4j
public class RouteService implements IRouteService, ApplicationEventPublisherAware {


    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    private ApplicationEventPublisher publisher;

    private Map<String, RouteDefinition> routeDefinitionMaps = new HashMap<>();

    @Override
    public Collection<RouteDefinition> getRouteDefinitions() {
        return routeDefinitionMaps.values();
    }

    @Override
    public boolean save(RouteDefinition routeDefinition) {
        routeDefinitionMaps.put(routeDefinition.getId(), routeDefinition);
        log.info("新增路由1条：{},目前路由共{}条", routeDefinition, routeDefinitionMaps.size());
        return true;
    }

    @Override
    public boolean delete(String routeId) {
        routeDefinitionMaps.remove(routeId);
        log.info("删除路由1条：{},目前路由共{}条", routeId, routeDefinitionMaps.size());
        return true;
    }

    public String addDefinition(RouteDefinition definition) {
        routeDefinitionWriter.save(Mono.just(definition)).subscribe();
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
        return "success";
    }

    public RouteDefinition assembleRouteDefinition(GatewayRouteDefinition gwdefinition) {

        RouteDefinition definition = new RouteDefinition();
        // ID
        definition.setId(gwdefinition.getId());
        // Predicates
        List<PredicateDefinition> pdList = new ArrayList<>();
        for (GatewayPredicateDefinition gpDefinition : gwdefinition.getPredicates()) {
            PredicateDefinition predicate = new PredicateDefinition();
            predicate.setArgs(gpDefinition.getArgs());
            predicate.setName(gpDefinition.getName());
            pdList.add(predicate);
        }
        definition.setPredicates(pdList);
        // Filters
        List<FilterDefinition> fdList = new ArrayList<>();
        for (GatewayFilterDefinition gfDefinition : gwdefinition.getFilters()) {
            FilterDefinition filter = new FilterDefinition();
            filter.setArgs(gfDefinition.getArgs());
            filter.setName(gfDefinition.getName());
            fdList.add(filter);
        }
        definition.setFilters(fdList);
        // URI
        URI uri = UriComponentsBuilder.fromUriString(gwdefinition.getUri()).build().toUri();
        definition.setUri(uri);

        return definition;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    public void update(GatewayRouteDefinition definition) {
        RouteDefinition routeDefinition = assembleRouteDefinition(definition);
        log.info("GatewayRouteDefinition definition is {},RouteDefinition routeDefinition is {}", JSONObject.toJSONString(definition),JSONObject.toJSONString(routeDefinition));
        if (routeDefinition != null) {
            try {
                routeDefinitionWriter.delete(Mono.just(definition.getId()));
            } catch (Exception e) {
                log.error("GatewayRouteDefinition update error", e);
                return;
            }
            try {
                routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
                this.publisher.publishEvent(new RefreshRoutesEvent(this));
            } catch (Exception e) {
                log.error("routeDefinitionWriter save error",e);
            }
        }

    }
}
