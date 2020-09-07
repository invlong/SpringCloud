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

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import com.springboot.cloud.auth.client.service.impl.K12AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Timeout {@link NacosConfigListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosConfiguration
 * @since 0.1.0
 */
@Configuration
public class PojoNacosConfigListener {

	private static final Logger logger = LoggerFactory
			.getLogger(PojoNacosConfigListener.class);

	@NacosInjected
	private ConfigService configService;

	@Autowired
	private K12AuthService k12AuthService;

	/*@PostConstruct
	public void init() throws Exception {
		Pojo pojo = new Pojo();
		// Initialize
		// Serialization
		ObjectMapper objectMapper = new ObjectMapper();
		String content = objectMapper.writeValueAsString(pojo);
		// Publish
		configService.publishConfig(POJO_DATA_ID, "common", content);
	}*/

	@NacosConfigListener(dataId = "${spring.cloud.nacos.config.ext-config[0].data-id}", groupId = "${spring.cloud.nacos.config.ext-config[0].group}", type = ConfigType.YAML, converter = PojoNacosConfigConverter.class)
	public void onReceived(JSONObject value) throws InterruptedException {
		logger.debug("网关配置变更，重新加载白名单:{}", value);
		k12AuthService.init();
	}

	@NacosConfigListener(dataId = "${spring.cloud.nacos.config.ext-config[1].data-id}", groupId = "${spring.cloud.nacos.config.ext-config[1].group}", type = ConfigType.YAML, converter = PojoNacosConfigConverter.class)
	public void onReceived2(JSONObject value) throws InterruptedException {
		logger.debug("node白名单变更，重新加载白名单:{}", value);
		k12AuthService.init();
	}
}
