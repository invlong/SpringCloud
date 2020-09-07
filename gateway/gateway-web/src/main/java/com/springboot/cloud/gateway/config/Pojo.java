/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springboot.cloud.gateway.config;

import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.alibaba.nacos.api.config.annotation.NacosProperty;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * 这是一个动态刷新的例子，目前实际上用不到
 */
@NacosConfigurationProperties(dataId = "${spring.cloud.nacos.config.ext-config[0].data-id}", groupId = "${spring.cloud.nacos.config.ext-config[0].group}", prefix = "database", type = ConfigType.YAML, autoRefreshed = true)
@Data
@Component
public class Pojo {

	public static final String DATA_ID = "config-dev.yml";

	@NacosProperty("host")
	private String host;

	@NacosProperty("port")
	private String port;

	@NacosProperty("username")
	private String username;

	@NacosProperty("password")
	private String password;

	@NacosProperty("driver-class-name")
	private String driverClassName;
}
