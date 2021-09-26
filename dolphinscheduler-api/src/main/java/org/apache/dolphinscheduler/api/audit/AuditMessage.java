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

import org.apache.dolphinscheduler.common.enums.AuditModuleType;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Date;

public class AuditMessage {
    private User user;

    private Date auditDate;

    private AuditModuleType module;

    private AuditOperationType operation;

    private String projectName;

    private String processName;

    public AuditMessage(User user, Date auditDate, AuditModuleType module, AuditOperationType operation, String projectName, String processName) {
        this.user = user;
        this.auditDate = auditDate;
        this.module = module;
        this.operation = operation;
        this.processName = processName;
        this.projectName = projectName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(Date auditDate) {
        this.auditDate = auditDate;
    }

    public AuditModuleType getModule() {
        return module;
    }

    public void setModule(AuditModuleType module) {
        this.module = module;
    }

    public AuditOperationType getOperation() {
        return operation;
    }

    public void setOperation(AuditOperationType operation) {
        this.operation = operation;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    @Override
    public String toString() {
        return "AuditMessage{"
                + "user=" + user
                + ", Date=" + auditDate
                + ", module=" + module
                + ", operation=" + operation
                + ", projectName='" + projectName + '\''
                + ", processName='" + processName + '\'';
    }
}
