package com.springboot.cloud.auth.client.provider;

import com.springboot.cloud.auth.client.entity.dto.LoginParam;
import com.springboot.cloud.common.core.entity.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(name = "k12-node-server", fallback = K12NodeServerProvider.K12CoreProviderFallback.class)
public interface K12NodeServerProvider {

    @PostMapping(value = "/auth/cloud_auth/login_check")
    Result login(@RequestBody LoginParam param);

    @Component
    class K12CoreProviderFallback implements K12NodeServerProvider {

        @Override
        public Result login(@RequestBody LoginParam param) {
            return Result.fail();
        }
    }
}
