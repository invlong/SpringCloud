package com.springboot.auth.authorization.service;

import com.springboot.auth.authorization.entity.K12User;
import com.springboot.auth.authorization.entity.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public interface IK12UserService {

    /**
     * 根据用户唯一标识获取用户信息
     *
     * @param uniqueId
     * @return
     */
    @Cacheable(value = "#id")
    K12User getByUniqueId(String uniqueId, Integer schoolId);
}
