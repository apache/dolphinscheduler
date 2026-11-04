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

package org.apache.dolphinscheduler.api.audit;

import org.apache.dolphinscheduler.api.service.AuditService;
import org.apache.dolphinscheduler.common.enums.AuditModelType;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectAuthorizationAuditLogger {

    private final AuditService auditService;

    public void record(User operator, Project project, AuditOperationType operation, String action,
                       Map<String, Object> changes) {
        Map<String, Object> detail = new LinkedHashMap<>(changes);
        detail.put("action", action);
        detail.put("operatorId", operator.getId());
        detail.put("operatorName", operator.getUserName());
        detail.put("projectCode", project.getCode());
        AuditLog audit = new AuditLog();
        audit.setUserId(operator.getId());
        audit.setModelType(AuditModelType.PROJECT.getName());
        audit.setModelId(project.getCode());
        audit.setModelName(project.getName());
        audit.setOperationType(operation.getName());
        audit.setDescription(action);
        audit.setDetail(JSONUtils.toJsonString(detail));
        audit.setCreateTime(new Date());
        auditService.addAudit(audit);
    }
}
