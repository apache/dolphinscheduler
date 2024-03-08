package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.OperatorUtils;
import org.apache.dolphinscheduler.api.audit.enums.AuditType;
import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleOperatorImpl extends BaseOperator {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Override
    public void modifyRequestParams(String[] paramNameArr, Map<String, Object> paramsMap, List<AuditLog> auditLogList) {
        if (!paramNameArr[0].equals("id")) {
            return;
        }
        int id = (int) paramsMap.get(paramNameArr[0]);
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule != null) {
            paramsMap.put("code", schedule.getProcessDefinitionCode());
            paramNameArr[0] = "code";
            auditLogList.forEach(auditLog -> auditLog.setDetail(String.valueOf(id)));
        }
    }

    @Override
    protected void setObjectIdentityFromReturnObject(AuditType auditType, Result result,
                                                     List<AuditLog> auditLogList) {
        String[] returnObjectFieldNameArr = auditType.getReturnObjectFieldName();
        if (returnObjectFieldNameArr.length == 0) {
            return;
        }

        Map<String, Object> returnObjectMap =
                OperatorUtils.getObjectIfFromReturnObject(result.getData(), returnObjectFieldNameArr);
        auditLogList
                .forEach(auditLog -> auditLog.setDetail(returnObjectMap.get(returnObjectFieldNameArr[0]).toString()));
    }

    @Override
    protected String getObjectNameFromReturnIdentity(Object identity) {
        ProcessDefinition obj = processDefinitionMapper.queryByCode((long) identity);
        return obj == null ? "" : obj.getName();
    }
}
