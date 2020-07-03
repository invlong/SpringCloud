package com.springboot.cloud.gateway.service;

import com.springboot.cloud.common.core.entity.vo.Result;

public interface IPermissionService {
    /**
     * @param authentication
     * @param method
     * @param url
     * @return
     */
    Result permission(String authentication, String url, String method);
}
