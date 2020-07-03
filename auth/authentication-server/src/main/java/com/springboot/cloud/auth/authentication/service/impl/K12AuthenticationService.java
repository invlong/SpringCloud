package com.springboot.cloud.auth.authentication.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.springboot.cloud.auth.authentication.entity.K12AuthUser;
import com.springboot.cloud.auth.authentication.service.IK12AuthenticationService;
import com.springboot.cloud.common.core.entity.vo.Result;
import com.springboot.cloud.common.core.exception.K12AuthErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class K12AuthenticationService implements IK12AuthenticationService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String USER_ID = "userID";
    private static final String SCHOOL_ID = "schoolId";
    private static final String CLIENT = "client";
    private static final String JTI = "jti";
    private static final String GLOBAL_INVALID_FLAG = "INVALID_TOKEN";

    /**
     * @param authRequest 访问的url,method
     * @return 有权限true, 无权限或全局资源中未找到请求url返回否
     */
    @Override
    public Result decide(HttpServletRequest authRequest, String pdata) {
        Integer userId;
        Integer schoolId;
        String client;
        String jti;
        log.debug("正在访问的url是:{}，method:{}", authRequest.getServletPath(), authRequest.getMethod());
        //获取用户认证信息，2020年06月25日增加旧token的兼容
        if (Strings.isNullOrEmpty(pdata)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            K12AuthUser k12AuthUser = (K12AuthUser) authentication.getPrincipal();
            userId = k12AuthUser.getUserId();
            schoolId = k12AuthUser.getSchoolId();
            client = k12AuthUser.getClient();
            Map<String, String> authenticationDetails = (LinkedHashMap<String, String>) authentication.getDetails();
            jti = authenticationDetails.get("jti");
        } else {
            // 解密pdata
            JSONObject pdataJson = JSONObject.parseObject(pdata);
            userId = pdataJson.getInteger(USER_ID);
            schoolId = pdataJson.getInteger(SCHOOL_ID);
            client = pdataJson.getString(CLIENT);
            jti = pdataJson.getString(JTI);
        }
        if (null == userId || null == schoolId) {
            log.error("解析token失败，未获取到有效的信息");
            return Result.fail(K12AuthErrorType.AUTH_WRONG_TOKEN);
        }
        log.debug("请求人id:{}，请求人学校id:{}", userId, schoolId);
        if (userId > 0) {
            // 判断token是否失效
            List<String> invalidTokens = redisTemplate.opsForList().range(userId + "_" + client + "_" + "invalid", 0, -1);
            String invalidTokenG = redisTemplate.opsForValue().get(client + "_" + "invalid");
            if (null != invalidTokens) {
                if (invalidTokens.contains(jti)) {
                    log.warn("账号在别处登录，请重新登录");
                    return Result.fail(K12AuthErrorType.AUTH_RE_LOGIN);
                } else if (invalidTokens.contains(GLOBAL_INVALID_FLAG)) {
                    log.warn("账号权限有变动，请重新登录");
                    return Result.fail(K12AuthErrorType.AUTH_ROLE_CHANGE);
                }
            }
            if (null != invalidTokenG) {
                log.warn("账号权限有变动，请重新登录");
                return Result.fail(K12AuthErrorType.AUTH_ROLE_CHANGE);
            }
        }
        return Result.success();
    }
}
