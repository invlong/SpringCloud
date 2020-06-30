package com.springboot.cloud.auth.client.entity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginParam {
    private String username;
    private String password;
    private String client;
    private Integer schoolId;
    private String roleNo;
}
