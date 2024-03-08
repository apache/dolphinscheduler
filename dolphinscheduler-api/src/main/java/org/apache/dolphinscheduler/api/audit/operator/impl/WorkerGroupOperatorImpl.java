package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.enums.AuditType;
import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkerGroupOperatorImpl extends BaseOperator {

    @Autowired
    private WorkerGroupMapper workerGroupMapper;

    @Override
    public void modifyAuditOperationType(AuditType auditType, Map<String, Object> paramsMap,
                                         List<AuditLog> auditLogList) {
        if (auditType.getAuditOperationType() == AuditOperationType.CREATE
                && paramsMap.get("id") != null &&
                !paramsMap.get("id").toString().equals("0")) {
            auditLogList.forEach(auditLog -> auditLog.setOperationType(AuditOperationType.UPDATE.getName()));
        }
    }

    @Override
    public String getObjectNameFromReturnIdentity(Object identity) {
        WorkerGroup obj = workerGroupMapper.selectById(Long.parseLong(identity.toString()));
        return obj == null ? "" : obj.getName();
    }
}
