/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.OperatorUtils;
import org.apache.dolphinscheduler.api.audit.constants.AuditLogConstants;
import org.apache.dolphinscheduler.api.audit.enums.AuditType;
import org.apache.dolphinscheduler.api.audit.operator.BaseAuditOperator;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessAuditOperatorImpl extends BaseAuditOperator {

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Override
    public void modifyAuditOperationType(AuditType auditType, Map<String, Object> paramsMap,
                                         List<AuditLog> auditLogList) {
        AuditOperationType auditOperationType = OperatorUtils.modifyReleaseOperationType(auditType, paramsMap);
        auditLogList.forEach(auditLog -> auditLog.setOperationType(auditOperationType.getName()));
    }

    @Override
    protected void setObjectByParam(String[] paramNameArr, Map<String, Object> paramsMap,
                                    List<AuditLog> auditLogList) {
        if (paramNameArr[0].equals(AuditLogConstants.CODES)
                || paramNameArr[0].equals(AuditLogConstants.PROCESS_DEFINITION_CODES)
                || paramNameArr[0].equals(AuditLogConstants.PROCESS_INSTANCE_IDS)) {
            super.setObjectByParamArr(paramNameArr, paramsMap, auditLogList);
        } else {
            super.setObjectByParam(paramNameArr, paramsMap, auditLogList);
        }
        if (paramsMap.containsKey(AuditLogConstants.VERSION)) {
            if (paramsMap.get(AuditLogConstants.VERSION) != null) {
                auditLogList.get(0).setDetail(paramsMap.get(AuditLogConstants.VERSION).toString());
            } else {
                auditLogList.get(0).setDetail("latest");
            }
        }
    }

    @Override
    protected String getObjectNameFromIdentity(Object identity) {
        Long objId = toLong(identity);
        if (objId == -1) {
            return "";
        }

        ProcessDefinition obj = processDefinitionMapper.queryByCode(objId);
        return obj == null ? "" : obj.getName();
    }
}
