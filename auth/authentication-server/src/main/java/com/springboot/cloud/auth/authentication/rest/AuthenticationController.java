package com.springboot.cloud.auth.authentication.rest;

import com.springboot.cloud.auth.authentication.service.IK12AuthenticationService;
import com.springboot.cloud.common.core.entity.vo.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@Api("auth")
@Slf4j
public class AuthenticationController {

    @Autowired
    IK12AuthenticationService k12AuthenticationService;

    @ApiOperation(value = "权限验证", notes = "根据用户token，访问的url和method判断用户是否有权限访问")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "pdata", value = "兼容旧的token需要的数据", required = false, dataType = "string"),
            @ApiImplicitParam(paramType = "query", name = "url", value = "访问的url", required = true, dataType = "string"),
            @ApiImplicitParam(paramType = "query", name = "method", value = "访问的method", required = true, dataType = "string")
    })
    @ApiResponses(@ApiResponse(code = 200, message = "处理成功", response = Result.class))
    @PostMapping(value = "/auth/permission")
    public Result decide(@RequestParam String pdata, @RequestParam String url, @RequestParam String method, HttpServletRequest request) {
        // 2020年06月25日使用自己业务的鉴权服务
        return k12AuthenticationService.decide(new HttpServletRequestAuthWrapper(request, url, method), pdata);
    }

}