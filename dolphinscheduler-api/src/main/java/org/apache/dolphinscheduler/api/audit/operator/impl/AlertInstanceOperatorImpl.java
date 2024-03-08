package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlertInstanceOperatorImpl extends BaseOperator {

    @Autowired
    private AlertPluginInstanceMapper alertPluginInstanceMapper;

    @Override
    public String getObjectNameFromReturnIdentity(Object identity) {
        AlertPluginInstance obj = alertPluginInstanceMapper.selectById(Long.parseLong(identity.toString()));
        return obj == null ? "" : obj.getInstanceName();
    }
}
