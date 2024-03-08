package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskInstanceOperatorImpl extends BaseOperator {

    @Autowired
    private TaskInstanceMapper taskInstanceMapper;

    @Override
    protected String getObjectNameFromReturnIdentity(Object identity) {
        TaskInstance obj = taskInstanceMapper.selectById(Integer.parseInt(identity.toString()));
        return obj == null ? "" : obj.getName();
    }
}
