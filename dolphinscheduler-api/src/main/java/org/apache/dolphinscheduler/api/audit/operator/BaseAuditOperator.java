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

package org.apache.dolphinscheduler.api.audit.operator;

import org.apache.dolphinscheduler.api.audit.OperatorLog;
import org.apache.dolphinscheduler.api.audit.OperatorLogAspect;
import org.apache.dolphinscheduler.api.audit.OperatorUtils;
import org.apache.dolphinscheduler.api.audit.enums.AuditType;
import org.apache.dolphinscheduler.api.service.AuditService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.AuditLog;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

@Service
@Slf4j
public abstract class BaseAuditOperator implements AuditOperator {

    @Autowired
    private AuditService auditService;

    @Override
    public void recordAudit(OperatorLogAspect.AuditContext auditContext, Object returnValue) {
        Result<?> result = new Result<>();

        if (returnValue instanceof Result) {
            result = (Result<?>) returnValue;
            if (OperatorUtils.resultFail(result)) {
                log.error("request fail, code {}", result.getCode());
                return;
            }
        }

        long latency = System.currentTimeMillis() - auditContext.getBeginTime();
        List<AuditLog> auditLogList = auditContext.getAuditLogList();

        Map<String, Object> paramsMap = auditContext.getParamsMap();
        OperatorLog operatorLog = auditContext.getOperatorLog();
        AuditType auditType = operatorLog.auditType();

        setObjectIdentityFromReturnObject(auditType, result, auditLogList);
        modifyAuditOperationType(auditType, paramsMap, auditLogList);
        modifyAuditObjectType(auditType, paramsMap, auditLogList);

        auditLogList.forEach(auditLog -> auditLog.setLatency(latency));
        auditLogList.forEach(auditLog -> auditService.addAudit(auditLog));
    }

    @Override
    public void setRequestParam(AuditType auditType, List<AuditLog> auditLogList, Map<String, Object> paramsMap) {
        String[] paramNameArr = auditType.getRequestParamName();

        if (paramNameArr.length == 0) {
            return;
        }

        modifyRequestParams(paramNameArr, paramsMap, auditLogList);
        setObjectByParam(paramNameArr, paramsMap, auditLogList);

        if (auditLogList.get(0).getModelId() == null) {
            auditLogList.get(0).setModelId(OperatorUtils.getObjectIdentityByParam(paramNameArr, paramsMap));
        }
    }

    protected void setObjectByParam(String[] paramNameArr, Map<String, Object> paramsMap,
                                    List<AuditLog> auditLogList) {

        String name = paramNameArr[0];
        Object value = paramsMap.get(name);

        if (value == null) {
            return;
        }

        String objName = getObjectNameFromIdentity(value);

        if (Strings.isNullOrEmpty(objName)) {
            auditLogList.get(0).setModelName(value.toString());
            return;
        }

        try {
            long objectId = Long.parseLong(value.toString());
            auditLogList.get(0).setModelId(objectId);
        } catch (NumberFormatException e) {
            log.error("value is not long, value: {}", value);
        }

        auditLogList.get(0).setModelName(objName);
    }

    protected void setObjectByParamArr(String[] paramNameArr, Map<String, Object> paramsMap,
                                       List<AuditLog> auditLogList) {

        AuditLog auditLog = auditLogList.get(0);
        for (String param : paramNameArr) {
            if (!paramsMap.containsKey(param)) {
                continue;
            }

            String[] identityArr = ((String) paramsMap.get(param)).split(",");
            for (String identityString : identityArr) {
                long identity = toLong(identityString);

                String value = getObjectNameFromIdentity(identity);

                if (value == null) {
                    continue;
                }

                auditLog.setModelId(identity);
                auditLog.setModelName(value);
                auditLogList.add(auditLog);
                auditLog = AuditLog.copyNewOne(auditLog);
            }
        }
        auditLogList.remove(0);
    }

    protected void setObjectIdentityFromReturnObject(AuditType auditType, Result<?> result,
                                                     List<AuditLog> auditLogList) {
        String[] returnObjectFieldNameArr = auditType.getReturnObjectFieldName();
        if (returnObjectFieldNameArr.length == 0) {
            return;
        }
        Map<String, Object> returnObjectMap =
                OperatorUtils.getObjectIfFromReturnObject(result.getData(), returnObjectFieldNameArr);
        modifyObjectFromReturnObject(returnObjectFieldNameArr, returnObjectMap, auditLogList);
        setObjectNameFromReturnIdentity(auditLogList);
    }

    protected void setObjectNameFromReturnIdentity(List<AuditLog> auditLogList) {
        auditLogList
                .forEach(auditLog -> auditLog.setModelName(getObjectNameFromIdentity(auditLog.getModelId())));
    }

    protected void modifyObjectFromReturnObject(String[] params, Map<String, Object> returnObjectMap,
                                                List<AuditLog> auditLogList) {
        if (returnObjectMap.isEmpty() || returnObjectMap.get(params[0]) == null) {
            return;
        }

        Long objId = toLong(returnObjectMap.get(params[0]));

        if (objId != -1) {
            auditLogList.get(0).setModelId(objId);
        }
    }

    protected Long toLong(Object str) {
        if (str == null) {
            return -1L;
        }

        return NumberUtils.toLong(str.toString(), -1);
    }

    protected String getObjectNameFromIdentity(Object identity) {
        return identity.toString();
    }

    protected void modifyRequestParams(String[] paramNameArr, Map<String, Object> paramsMap,
                                       List<AuditLog> auditLogList) {

    }

    protected void modifyAuditObjectType(AuditType auditType, Map<String, Object> paramsMap,
                                         List<AuditLog> auditLogList) {

    }

    protected void modifyAuditOperationType(AuditType auditType, Map<String, Object> paramsMap,
                                            List<AuditLog> auditLogList) {

    }
}
