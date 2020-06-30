package com.springboot.cloud.auth.client.service;

import com.springboot.cloud.auth.client.entity.dto.LoginParam;
import com.springboot.cloud.common.core.entity.vo.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

public interface IK12AuthService {
    /**
     * 调用签权服务，判断用户是否有权限
     *
     * @param authentication
     * @param pdata 老的token中放入的加密的信息，兼容处理的参数
     * @param url
     * @param method
     * @return Result
     */
    Result authenticate(String authentication, String pdata, String url, String method);

    /**
     * 判断url是否在忽略的范围内
     * 只要是配置中的开头，即返回true
     *
     * @param url
     * @return
     */
    boolean ignoreAuthentication(String url);

    /**
     * 查看签权服务器返回结果，有权限返回true
     *
     * @param authResult
     * @return
     */
    boolean hasPermission(Result authResult);

    /**
     * 调用签权服务，判断用户是否有权限
     *
     * @param authentication
     * @param url
     * @param method
     * @return true/false
     */
    Result hasPermission(String authentication, String url, String method);

    /**
     * 是否无效authentication
     *
     * @param authentication
     * @return
     */
    Result invalidJwtAccessToken(String authentication);

    /**
     * 旧token的解析方法
     * @param authentication
     * @return
     */
    Result invalidK12JwtAccessToken(String authentication);

    /**
     * 从认证信息中提取jwt token 对象
     *
     * @param jwtToken toke信息 header.payload.signature
     * @return Jws对象
     */
    Jws<Claims> getJwt(String jwtToken);

    /**
     * 登录接口，如果只有一个角色，获取token返回给前台，否则前台调用oauth2接口获取
     * @param param
     * @return
     */
    Result login(LoginParam param);

    /**
     * oauth2的授权接口,获取access token
     * @param authentication
     * @param username
     * @param password
     * @param grant_type
     * @param scope
     * @return
     */
    Result token(String authentication, String username, String password, String grant_type, String scope);
}
