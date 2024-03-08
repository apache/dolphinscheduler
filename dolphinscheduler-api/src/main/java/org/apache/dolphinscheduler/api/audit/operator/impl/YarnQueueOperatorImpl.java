package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.mapper.QueueMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class YarnQueueOperatorImpl extends BaseOperator {

    @Autowired
    private QueueMapper queueMapper;

    @Override
    public String getObjectNameFromReturnIdentity(Object identity) {
        Queue obj = queueMapper.selectById(Long.parseLong(identity.toString()));
        return obj == null ? "" : obj.getQueueName();
    }
}
