package com.springboot.cloud.auth.authentication.entity;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 自定义的人员信息，增加人员id和schoolId和client放入到token中
 */
public class K12AuthUser extends org.springframework.security.core.userdetails.User {

    private Integer userId;

    private Integer schoolId;

    private String client;

    public K12AuthUser(Integer userId, Integer schoolId, String client, String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.setUserId(userId);
        this.setSchoolId(schoolId);
        this.setClient(client);
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Integer schoolId) {
        this.schoolId = schoolId;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }
}
