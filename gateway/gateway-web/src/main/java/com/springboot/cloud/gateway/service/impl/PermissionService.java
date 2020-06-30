package com.springboot.cloud.gateway.service.impl;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.springboot.cloud.auth.client.service.IK12AuthService;
import com.springboot.cloud.common.core.entity.vo.Result;
import com.springboot.cloud.gateway.service.IPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class PermissionService implements IPermissionService {

    @Autowired
    private IK12AuthService k12AuthService;

    @Override
    @Cached(name = "gateway_auth::", key = "#authentication+#method+#url",
            cacheType = CacheType.LOCAL, expire = 10, timeUnit = TimeUnit.SECONDS, localLimit = 10000)
    public Result permission(String authentication, String url, String method) {
        // return authService.hasPermission(authentication, url, method);
        // 2020年06月24日 使用自己业务逻辑的鉴权
        return k12AuthService.hasPermission(authentication, url, method);
    }
}
