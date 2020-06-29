package com.springboot.auth.authorization.oauth2;

import com.springboot.auth.authorization.entity.K12AuthUser;
import com.springboot.auth.authorization.entity.K12User;
import com.springboot.auth.authorization.service.IK12UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service("userDetailsService")
public class K12UserDetailsService implements UserDetailsService {

    private static final String SPLIT = "%::%";

    private static final String DEFAULT_CLIENT = "web-api";

    @Autowired
    private IK12UserService k12UserService;

    @Override
    public UserDetails loadUserByUsername(String uniqueId) {
        String[] params = uniqueId.split(SPLIT);
        String loginName;
        String client;
        Integer schoolId = null;
        switch (params.length) {
            case 1:
                loginName = params[0];
                client = DEFAULT_CLIENT;
                break;
            case 2:
                loginName = params[0];
                client = params[1];
                break;
            case 3:
                loginName = params[0];
                client = params[1];
                schoolId = Integer.valueOf(params[2]);
                break;
            default:
                loginName = params[0];
                client = params[1];
                break;
        }
        K12User user = k12UserService.getByUniqueId(loginName, schoolId);
        log.info("load user by username :{}", user.toString());
        return new K12AuthUser(
                user.getUserId(),
                user.getSchoolId(),
                user.getCurrentRoleNo(),
                client,
                user.getUsername(),
                user.getPassword(),
                user.getEnabled(),
                user.getAccountNonExpired(),
                user.getCredentialsNonExpired(),
                user.getAccountNonLocked(),
                this.obtainGrantedAuthorities(user));
    }
    /**
     * 获得登录者所有角色的权限集合.
     *
     * @param user
     * @return
     */
    protected Set<GrantedAuthority> obtainGrantedAuthorities(K12User user) {
        Set<String> roles = new HashSet<>(Arrays.asList(user.getRoleNos().split(",")));
        log.info("user:{},roles:{}", user.getUsername(), roles);
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }
}
