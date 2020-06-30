package com.springboot.cloud.auth.client.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.springboot.cloud.auth.client.provider.K12AuthProvider;
import com.springboot.cloud.auth.client.service.IK12AuthService;
import com.springboot.cloud.auth.client.utils.AESUtil;
import com.springboot.cloud.common.core.entity.vo.Result;
import com.springboot.cloud.common.core.exception.K12AuthErrorType;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;
import java.util.stream.Stream;

@Service
@Slf4j
public class K12AuthService implements IK12AuthService {

    /**
     * Authorization认证开头是"bearer "
     */
    private static final String BEARER = "Bearer ";

    private static final String AUD = "aud";

    private static final String JTI = "jti";

    private static final String CLIENT = "client";

    private static final String PDATA = "pdata";

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
        Object pdata = null;
        //token是否有效，在网关进行校验，无效/过期等
        Result invalidTokenResp = invalidJwtAccessToken(authentication);
        if (invalidTokenResp.isFail()) {
            // 二次校验之前的签发格式的token
            Result invalidK12TokenResp = this.invalidK12JwtAccessToken(authentication);
            if (invalidK12TokenResp.isFail()) {
                return invalidTokenResp;
            } else {
                pdata = invalidTokenResp.getData();
            }
        }
        // 调用鉴权服务，判断redis黑名单
        return authenticate(authentication, null == pdata ? null : String.valueOf(pdata), url, method);
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

    @Override
    public Result invalidK12JwtAccessToken(String authentication) {
        if (authentication.length() > 15) {
            StringTokenizer st = new StringTokenizer(authentication);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();
                log.debug(basic);
                String credentials = new String(java.util.Base64.getDecoder().decode(st.nextToken()), StandardCharsets.UTF_8);
                try {
                    Claims claims = Jwts.parser()
                            .setSigningKey(signingKey.getBytes())
                            .parseClaimsJws(credentials).getBody();
                    String pdata = claims.get(PDATA).toString();
                    String aud = claims.get(AUD).toString();
                    // 解密pdata
                    String pdataStr = AESUtil.decrypt(pdata, aesKey, aesValue);
                    JSONObject pdataJson = JSONObject.parseObject(pdataStr);
                    pdataJson.put(CLIENT, aud);
                    pdataJson.put(JTI, authentication);
                    return Result.success(pdataJson.toJSONString());
                } catch (SignatureException | ExpiredJwtException | MalformedJwtException ex) {
                    ex.printStackTrace();
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
