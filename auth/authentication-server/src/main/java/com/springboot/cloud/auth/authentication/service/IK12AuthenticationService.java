package com.springboot.cloud.auth.authentication.service;

import com.springboot.cloud.common.core.entity.vo.Result;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public interface IK12AuthenticationService {
    /**
     * 校验权限
     *
     * @param authRequest
     * @param pdata 增加旧token存入的pdata内容
     * @return 是否有权限
     */
    Result decide(HttpServletRequest authRequest, String pdata);

}
