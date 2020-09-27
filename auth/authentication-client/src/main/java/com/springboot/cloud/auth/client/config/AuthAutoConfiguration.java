package com.springboot.cloud.auth.client.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.springboot.cloud.auth.client")
@EnableConfigurationProperties({JWTProperties.class, NodeNestWhiteListConfig.class, NodeServerWhiteListConfig.class, NodePaymentWhiteListConfig.class})
public class AuthAutoConfiguration {


}
