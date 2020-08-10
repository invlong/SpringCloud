package com.springboot.cloud.auth.client.utils;

import com.weds.framework.auth.entity.jwt.JWTParseResponse;
import com.weds.framework.auth.service.JWTService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.StringTokenizer;

@Slf4j
public class AuthUtil {

    // Gets HTTP basic authentication's user name and password.
    public static String authorizationToken(String token,JWTService jwtService) throws IOException {
        log.debug(token);
        if (token != null && token.length() > 15) {
            try {
                StringTokenizer st = new StringTokenizer(token);
                if (st.hasMoreTokens()) {
                    String basic = st.nextToken();
                    String credentials = new String(java.util.Base64.getDecoder().decode(st.nextToken()), "UTF-8");
                    JWTParseResponse jwtParseResponse = jwtService.parseJWT(credentials);
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
}
