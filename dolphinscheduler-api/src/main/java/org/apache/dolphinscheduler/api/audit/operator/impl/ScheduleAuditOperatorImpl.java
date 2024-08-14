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
public class ScheduleAuditOperatorImpl extends BaseAuditOperator {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Override
    public void modifyRequestParams(String[] paramNameArr, Map<String, Object> paramsMap, List<AuditLog> auditLogList) {
        if (!paramNameArr[0].equals(AuditLogConstants.ID)) {
            return;
        }
        int id = (int) paramsMap.get(paramNameArr[0]);
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule != null) {
            paramsMap.put(AuditLogConstants.CODE, schedule.getProcessDefinitionCode());
            paramNameArr[0] = AuditLogConstants.CODE;
            auditLogList.forEach(auditLog -> auditLog.setDetail(String.valueOf(id)));
        }
    }

    @Override
    protected void setObjectIdentityFromReturnObject(AuditType auditType, Result<?> result,
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
    protected String getObjectNameFromIdentity(Object identity) {
        Long objId = toLong(identity);
        if (objId == -1) {
            return "";
        }

        ProcessDefinition obj = processDefinitionMapper.queryByCode(objId);
        return obj == null ? "" : obj.getName();
    }
}
