package com.springboot.auth.authorization.service.impl;

import com.springboot.auth.authorization.entity.K12User;
import com.springboot.auth.authorization.provider.K12CoreProvider;
import com.springboot.auth.authorization.service.IK12UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class K12UserService implements IK12UserService {

    @Autowired
    private K12CoreProvider k12CoreProvider;

    @Override
    public K12User getByUniqueId(String uniqueId, Integer schoolId) {
        return k12CoreProvider.getUserByUniqueId(uniqueId, schoolId).getData();
    }
}
