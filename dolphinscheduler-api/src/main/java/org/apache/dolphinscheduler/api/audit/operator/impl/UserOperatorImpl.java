package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserOperatorImpl extends BaseOperator {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String getObjectNameFromReturnIdentity(Object identity) {
        User obj = userMapper.selectById(Long.parseLong(identity.toString()));
        return obj == null ? "" : obj.getUserName();
    }
}
