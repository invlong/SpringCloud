package com.springboot.auth.authorization.provider;

import com.springboot.auth.authorization.entity.K12User;
import com.springboot.cloud.common.core.entity.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "k12-java-coreserver", fallback = K12CoreProviderFallback.class)
public interface K12CoreProvider {

    @GetMapping(value = "/userInfo/get_user_by_unique_id")
    Result<K12User> getUserByUniqueId(@RequestParam("unique_id") String uniqueId, @RequestParam("school_id") Integer schoolId);

}
