package com.springboot.cloud.auth.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "k12-node-server")
public class NodeServerWhiteListConfig {

    private List<String> GET = new ArrayList<>();
    private List<String> POST = new ArrayList<>();
    private List<String> PUT = new ArrayList<>();
    private List<String> DELETE = new ArrayList<>();

}
