package com.springboot.cloud.auth.client.utils;

import com.alibaba.fastjson.JSONObject;
import com.springboot.cloud.auth.client.config.JWTProperties;
import com.springboot.cloud.auth.client.entity.jwt.JWTParseResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

@Slf4j
public class JWTService {

    static int kid = 0;
    static int pkeyid = 1;

    public static final String GLOBAL_INVALID_FLAG = "INVALID_TOKEN";

    /**
     * @param jwt
     * @return
     * @throws Exception
     */
    public static JWTParseResponse parseJWT(String jwt, JWTProperties jwtProperties, StringRedisTemplate stringRedisTemplate) throws Exception {
        // 返回的对象
        JWTParseResponse jwtParseResponse = new JWTParseResponse();
        byte[] encodedBytes = org.apache.commons.codec.binary.Base64.encodeBase64(jwt.getBytes());
        String creToken = new String(encodedBytes);
        try {
            List<String> keys = Arrays.asList(jwtProperties.getSecretKey().split(","));
            List<JSONObject> pkeys = new ArrayList<>();
            List<String> pJson = Arrays.asList(jwtProperties.getSecretPkey().split(","));
            for (String json : pJson) {
                String[] values = json.split("&");
                JSONObject pvalue = new JSONObject();
                pvalue.put("key", values[0]);
                pvalue.put("iv", values[1]);
                pkeys.add(pvalue);
            }
            //This line will throw an exception if it is not a signed JWS (as expected)
            Claims claims = Jwts.parser()
                    .setSigningKey(keys.get(kid).getBytes())
                    .parseClaimsJws(jwt).getBody();
            String pdata = claims.get("pdata").toString();
            // 解密pdata
            // 获取到解密用的key
            JSONObject pkeyData = pkeys.get(pkeyid);
            String pdataStr = AESUtil.decrypt(pdata, pkeyData.get("key").toString(), pkeyData.get("iv").toString());
            JSONObject pdataJson = JSONObject.parseObject(pdataStr);
            String userIDStr = pdataJson.getString("userID");
            Integer userID = 0;
            if (isNumeric(userIDStr)) {
                userID = pdataJson.getInteger("userID");
            }
            if (userID > 0) {
                // 判断token是否失效
                List<String> invalidTokens = stringRedisTemplate.opsForList().range(pdataJson.get("userID").toString() + "_" + claims.get("aud") + "_" + "invalid", 0, -1);
                String invalidTokenG = stringRedisTemplate.opsForValue().get(pdataJson.get("userID").toString() + "_" + "invalid");
                if (null != invalidTokens) {
                    if (invalidTokens.contains(creToken)) {
                        log.warn("账号在别处登录，请重新登录");
                        jwtParseResponse.setCode("-1");
                        jwtParseResponse.setMessage("3");
                        return jwtParseResponse;
                    } else if (invalidTokens.contains(GLOBAL_INVALID_FLAG)) {
                        log.warn("账号权限有变动，请重新登录");
                        jwtParseResponse.setCode("-1");
                        jwtParseResponse.setMessage("2");
                        return jwtParseResponse;
                    }
                }
                if (null != invalidTokenG) {
                    log.warn("账号权限有变动，请重新登录");
                    jwtParseResponse.setCode("-1");
                    jwtParseResponse.setMessage("2");
                    return jwtParseResponse;
                }
            }
            String pdataRen = AESUtil.decrypt(pdata, pkeyData.get("key").toString(), pkeyData.get("iv").toString());
            jwtParseResponse.setCode("0");
            jwtParseResponse.setMessage("success");
            jwtParseResponse.setPdata(pdataRen);
            return jwtParseResponse;
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("登录时效已过期 请重新登录");
            log.info(ExceptionUtils.getStackTrace(e));
            jwtParseResponse.setCode("-1");
            jwtParseResponse.setMessage("1");
            return jwtParseResponse;
        }
    }

    // Gets HTTP basic authentication's user name and password.
    public static String authorizationToken(String token, JWTProperties jwtProperties, StringRedisTemplate stringRedisTemplate) throws IOException {
        log.debug(token);
        if (token != null && token.length() > 15) {
            try {
                StringTokenizer st = new StringTokenizer(token);
                if (st.hasMoreTokens()) {
                    String basic = st.nextToken();
                    String credentials = new String(java.util.Base64.getDecoder().decode(st.nextToken()), "UTF-8");
                    JWTParseResponse jwtParseResponse = parseJWT(credentials, jwtProperties, stringRedisTemplate);
                    log.info(jwtParseResponse.toString());
                    if ("0".equals(jwtParseResponse.getCode())) {
                        return jwtParseResponse.getPdata();
                    } else {
                        return jwtParseResponse.getMessage();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        } else {
            log.error("Authorization header not found");
        }
        return "4";
    }

    private static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
