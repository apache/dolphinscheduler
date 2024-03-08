package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlertGroupOperatorImpl extends BaseOperator {

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    @Override
    public String getObjectNameFromReturnIdentity(Object identity) {
        AlertGroup obj = alertGroupMapper.selectById(Long.parseLong(identity.toString()));
        return obj == null ? "" : obj.getGroupName();
    }
}
