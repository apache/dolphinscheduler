package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentOperatorImpl extends BaseOperator {

    @Autowired
    private EnvironmentMapper environmentMapper;

    @Override
    public String getObjectNameFromReturnIdentity(Object identity) {
        Environment obj = environmentMapper.queryByEnvironmentCode((long) identity);
        return obj == null ? "" : obj.getName();
    }
}
