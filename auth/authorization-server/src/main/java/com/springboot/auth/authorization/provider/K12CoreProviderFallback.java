package com.springboot.auth.authorization.provider;

import com.springboot.auth.authorization.entity.K12User;
import com.springboot.cloud.common.core.entity.vo.Result;
import org.springframework.stereotype.Component;

@Component
public class K12CoreProviderFallback implements K12CoreProvider {

    @Override
    public Result<K12User> getUserByUniqueId(String uniqueId, Integer schoolId) {
        return Result.success(new K12User());
    }

}
