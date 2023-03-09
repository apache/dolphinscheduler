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

import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.common.enums.AuditResourceType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Date;

public class AuditMessage {

    private User user;

    private Date auditDate;

    private AuditResourceType resourceType;

    private AuditOperationType operation;

    private Integer resourceId;

    public AuditMessage(User user, Date auditDate, AuditResourceType resourceType, AuditOperationType operation,
                        Integer resourceId) {
        this.user = user;
        this.auditDate = auditDate;
        this.resourceType = resourceType;
        this.operation = operation;
        this.resourceId = resourceId;
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

    public AuditResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(AuditResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public AuditOperationType getOperation() {
        return operation;
    }

    public void setOperation(AuditOperationType operation) {
        this.operation = operation;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public String toString() {
        return "AuditMessage{"
                + "user=" + user
                + ", Date=" + auditDate
                + ", resourceType" + resourceType
                + ", operation=" + operation
                + ", resourceId='" + resourceId + '\'';
    }
}
