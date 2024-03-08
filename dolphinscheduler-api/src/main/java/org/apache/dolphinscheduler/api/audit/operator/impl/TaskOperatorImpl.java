package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.OperatorUtils;
import org.apache.dolphinscheduler.api.audit.enums.AuditType;
import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskOperatorImpl extends BaseOperator {

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Override
    public void modifyAuditOperationType(AuditType auditType, Map<String, Object> paramsMap,
                                         List<AuditLog> auditLogList) {
        AuditOperationType auditOperationType = OperatorUtils.modifyReleaseOperationType(auditType, paramsMap);
        auditLogList.forEach(auditLog -> auditLog.setOperationType(auditOperationType.getName()));
    }

    @Override
    protected void setObjectByParma(String[] paramNameArr, Map<String, Object> paramsMap,
                                    List<AuditLog> auditLogList) {

        super.setObjectByParma(paramNameArr, paramsMap, auditLogList);
        if (paramsMap.containsKey("version")) {
            auditLogList.get(0).setDetail(paramsMap.get("version").toString());
        } ;
    }

    @Override
    protected String getObjectNameFromReturnIdentity(Object identity) {
        TaskDefinition obj = taskDefinitionMapper.queryByCode((long) identity);
        return obj == null ? "" : obj.getName();
    }
}
