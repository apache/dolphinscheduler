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
