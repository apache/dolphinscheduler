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
import org.apache.dolphinscheduler.api.audit.enums.AuditType;
import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
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
    public void modifyAuditOperationType(AuditType auditType, Map<String, Object> paramsMap,
                                         List<AuditLog> auditLogList) {
        AuditOperationType auditOperationType = OperatorUtils.modifyReleaseOperationType(auditType, paramsMap);
        auditLogList.forEach(auditLog -> auditLog.setOperationType(auditOperationType.getName()));
    }

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
        int objId = checkNumInt(identity.toString());
        if (objId == -1) {
            return "";
        }

        ProcessInstance obj = processInstanceMapper.queryDetailById(objId);
        return obj == null ? "" : obj.getName();
    }
}
