package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.OperatorUtils;
import org.apache.dolphinscheduler.api.audit.enums.AuditType;
import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.common.enums.AuditObjectType;
import org.apache.dolphinscheduler.dao.entity.AuditLog;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class ResourceOperatorImpl extends BaseOperator {

    @Override
    public void modifyAuditObjectType(AuditType auditType, Map<String, Object> paramsMap, List<AuditLog> auditLogList) {
        if (OperatorUtils.isUdfResource(paramsMap)) {
            auditLogList.forEach(auditLog -> auditLog.setObjectType(AuditObjectType.UDF_FOLDER.getName()));
        }
    }

    @Override
    protected void setObjectByParma(String[] paramNameArr, Map<String, Object> paramsMap,
                                    List<AuditLog> auditLogList) {

        Object objName = getFileNameFromParam(paramNameArr, paramsMap);

        if (objName == null) {
            return;
        }

        auditLogList.get(0).setObjectName(objName.toString());
    }

    private String getFileNameFromParam(String[] paramNameArr, Map<String, Object> paramsMap) {
        for (String param : paramNameArr) {
            if (!param.equals("type")) {
                return paramsMap.get(param).toString();
            }
        }

        return null;
    }
}
