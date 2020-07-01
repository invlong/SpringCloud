package com.springboot.cloud.auth.client.service.impl;

import com.springboot.cloud.auth.client.provider.K12AuthProvider;
import com.springboot.cloud.auth.client.service.IK12AuthService;
import com.springboot.cloud.common.core.entity.vo.Result;
import com.springboot.cloud.common.core.exception.K12AuthErrorType;
import com.weds.framework.auth.service.JWTService;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.stream.Stream;

@Service
@Slf4j
public class K12AuthService implements IK12AuthService {

    @Autowired
    private JWTService jwtService;

    /**
     * Authorization认证开头是"bearer "
     */
    private static final String BEARER = "Bearer ";

    /**
     * Authorization认证开头是"Token "
     * 兼容老的写法
     */
    private static final String TOKEN = "Token ";

    @Autowired
    private K12AuthProvider k12AuthProvider;

    /**
     * jwt token 密钥，主要用于token解析，签名验证
     */
    @Value("${spring.security.oauth2.jwt.signingKey}")
    private String signingKey;

    /**
     * 不需要网关签权的url配置(/oauth,/open)
     * 默认/oauth开头是不需要的
     */
    @Value("${gate.ignore.authentication.startWith}")
    private String ignoreUrls = "/oauth";

    /**
     * 加密jwt中附带信息的key
     */
    @Value("${spring.security.oauth2.jwt.aesKey}")
    private String aesKey;

    /**
     * 加密jwt中附带信息的value
     */
    @Value("${spring.security.oauth2.jwt.aesValue}")
    private String aesValue;

    @Override
    public Result authenticate(String authentication, String pdata, String url, String method) {
        return k12AuthProvider.auth(authentication, pdata, url, method);
    }

    @Override
    public boolean ignoreAuthentication(String url) {
        return Stream.of(this.ignoreUrls.split(",")).anyMatch(ignoreUrl -> url.startsWith(StringUtils.trim(ignoreUrl)));
    }

    @Override
    public boolean hasPermission(Result authResult) {
        log.debug("签权结果:{}", authResult.getData());
        return authResult.isSuccess() && (boolean) authResult.getData();
    }

    @Override
    public Result hasPermission(String authentication, String url, String method) {
        // 如果请求未携带token信息, 直接权限
        if (StringUtils.isBlank(authentication) || (!authentication.startsWith(BEARER) && !authentication.startsWith(TOKEN))) {
            log.error("非法请求，请求头不包含token");
            return Result.fail(K12AuthErrorType.AUTH_WRONG_TOKEN);
        }
        String jwtData;
        try {
            jwtData = jwtService.authorizationToken(authentication);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(ExceptionUtils.getStackTrace(e));
            log.error("解析token发生异常");
            return Result.fail(K12AuthErrorType.AUTH_WRONG_TOKEN);
        }
        if (jwtData.length() == 1) {
            switch (jwtData) {
                case "1":
                    log.debug("登录时效已过期，请重新登录");
                    return Result.fail(K12AuthErrorType.AUTH_EXPIRE);
                case "2":
                    log.debug("账号权限有变动，请重新登录");
                    return Result.fail(K12AuthErrorType.AUTH_ROLE_CHANGE);
                case "3":
                    log.debug("账号在别处登录，请重新登录");
                    return Result.fail(K12AuthErrorType.AUTH_RE_LOGIN);
                default:
                    log.debug("非法请求");
                    return Result.fail(K12AuthErrorType.AUTH_WRONG_TOKEN);
            }
        } else {
            return Result.success(jwtData);
        }
    }

    @Override
    public Jws<Claims> getJwt(String jwtToken) {
        if (jwtToken.startsWith(BEARER)) {
            jwtToken = StringUtils.substring(jwtToken, BEARER.length());
        }
        return Jwts.parser()  //得到DefaultJwtParser
                .setSigningKey(signingKey.getBytes()) //设置签名的秘钥
                .parseClaimsJws(jwtToken);
    }

    @Override
    public Result invalidJwtAccessToken(String authentication) {
        if (authentication.length() > 15) {
            StringTokenizer st = new StringTokenizer(authentication);
            if (st.hasMoreTokens()) {
                try {
                    getJwt(authentication);
                    return Result.success();
                } catch (SignatureException | ExpiredJwtException | MalformedJwtException ex) {
                    log.error("user token error :{}", ex.getMessage());
                    log.error("Token过期");
                    return Result.fail(K12AuthErrorType.AUTH_EXPIRE);
                }
            } else {
                log.error("非法请求，token格式错误");
                return Result.fail(K12AuthErrorType.AUTH_WRONG_TOKEN);
            }
        } else {
            log.error("非法请求，请求头不包含token或者token过短不超过15位");
            return Result.fail(K12AuthErrorType.AUTH_WRONG_TOKEN);

        }
    }
}
