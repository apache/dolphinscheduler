package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessInstanceOperatorImpl extends BaseOperator {

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Override
    protected void setObjectByParma(String[] paramNameArr, Map<String, Object> paramsMap,
                                    List<AuditLog> auditLogList) {
        if (paramNameArr[0].equals("processInstanceIds")) {
            super.setObjectByParmaArr(paramNameArr, paramsMap, auditLogList);
        } else {
            super.setObjectByParma(paramNameArr, paramsMap, auditLogList);
        }
    }

    @Override
    protected String getObjectNameFromReturnIdentity(Object identity) {
        ProcessInstance obj = processInstanceMapper.queryDetailById(Integer.parseInt(identity.toString()));
        return obj == null ? "" : obj.getName();
    }
}
