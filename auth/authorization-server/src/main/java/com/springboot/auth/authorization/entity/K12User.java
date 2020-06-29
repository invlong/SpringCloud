package com.springboot.auth.authorization.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class K12User {
    private Integer userId;
    private Integer schoolId;
    private String currentRoleNo;
    private String roleNos;
    private String mobile;
    private String username;
    private String password;
    private Boolean enabled;
    private Boolean accountNonExpired;
    private Boolean credentialsNonExpired;
    private Boolean accountNonLocked;
}
