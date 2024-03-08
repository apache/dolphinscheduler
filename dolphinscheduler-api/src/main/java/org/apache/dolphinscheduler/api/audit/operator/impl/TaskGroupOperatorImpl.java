package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskGroupOperatorImpl extends BaseOperator {

    @Autowired
    private TaskGroupMapper taskGroupMapper;

    @Override
    public String getObjectNameFromReturnIdentity(Object identity) {
        TaskGroup obj = taskGroupMapper.selectById((Long) identity);
        return obj == null ? "" : obj.getName();
    }
}
