package com.springboot.cloud.auth.client.provider;

import com.alibaba.fastjson.JSONObject;
import com.springboot.cloud.common.core.entity.vo.Result;
import com.springboot.cloud.common.core.exception.K12AuthErrorType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name = "authorization-server", fallback = K12AuthorizationProvider.AuthProviderFallback.class)
public interface K12AuthorizationProvider {
    /**
     * 调用授权服务，获取access token
     * @param authentication
     * @param username
     * @param password
     * @param grant_type
     * @param scope
     * @return
     * 成功：
     * {
     *     "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJhZG1pbiIsInNjb3BlIjpbInJlYWQiXSwib3JnYW5pemF0aW9uIjoiYWRtaW4iLCJleHAiOjE1OTM0MTk1MzEsImF1dGhvcml0aWVzIjpbIkFETUlOIl0sImp0aSI6ImN0a0d6VHp3dVFVR2lIakpBY3dHazNqem1uOCIsImNsaWVudF9pZCI6InRlc3RfY2xpZW50In0.uJUCKPZKQ0xCsF3fOp57Pe6ijg-5XjY4Bx2b19qe9HQ",
     *     "token_type": "bearer",
     *     "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJhZG1pbiIsInNjb3BlIjpbInJlYWQiXSwib3JnYW5pemF0aW9uIjoiYWRtaW4iLCJhdGkiOiJjdGtHelR6d3VRVUdpSGpKQWN3R2szanptbjgiLCJleHAiOjE1OTM1MjAzMzEsImF1dGhvcml0aWVzIjpbIkFETUlOIl0sImp0aSI6ImxpN0VxQklfd0E1cm5RVnh5OWZ6TmJvWUc5RSIsImNsaWVudF9pZCI6InRlc3RfY2xpZW50In0.Plovq95cv7zMdUlsU9RcV4oYK30do8s81Kn6LXtacWY",
     *     "expires_in": 7199,
     *     "scope": "read",
     *     "organization": "admin",
     *     "jti": "ctkGzTzwuQUGiHjJAcwGk3jzmn8"
     * }
     * 失败：
     * {
     *     "code": "040004",
     *     "mesg": "无效scope",
     *     "time": "2020-06-29T06:44:56.279Z",
     *     "data": {
     *         "error": "invalid_scope",
     *         "error_description": "Invalid scope: read1",
     *         "scope": "read"
     *     }
     * }
     */
    @PostMapping(value = "/oauth/token")
    JSONObject token(@RequestHeader(HttpHeaders.AUTHORIZATION) String authentication, @RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("grant_type") String grant_type, @RequestParam("scope") String scope);

    @Component
    class AuthProviderFallback implements K12AuthorizationProvider {

        /**
         * 降级统一返回无权限
         * @param authentication
         * @param username
         * @param password
         * @param grant_type
         * @param scope
         * @return
         */
        @Override
        public JSONObject token(@RequestHeader(HttpHeaders.AUTHORIZATION) String authentication, @RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("grant_type") String grant_type, @RequestParam("scope") String scope) {
            return null;
        }
    }
}
