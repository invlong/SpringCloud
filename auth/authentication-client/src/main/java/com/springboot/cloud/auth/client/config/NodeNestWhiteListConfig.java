package com.springboot.cloud.auth.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Data
@ConfigurationProperties(prefix = "k12-nestjs-server")
public class NodeNestWhiteListConfig {

    private List<String> GET = new ArrayList<>();
    private List<String> POST = new ArrayList<>();
    private List<String> PUT = new ArrayList<>();
    private List<String> DELETE = new ArrayList<>();

}
