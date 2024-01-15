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

import lombok.Data;
import org.apache.dolphinscheduler.common.enums.Audit.AuditObjectType;
import org.apache.dolphinscheduler.common.enums.Audit.AuditOperationType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Date;

@Data
public class AuditMessage {

    private User user;

    private Date auditDate;

    private AuditObjectType objectType;

    private AuditOperationType operationType;

    private Integer resourceId;

    public AuditMessage(User user, Date auditDate, AuditObjectType objectType, AuditOperationType operationType,
                        Integer resourceId) {
        this.user = user;
        this.auditDate = auditDate;
        this.objectType = objectType;
        this.operationType = operationType;
        this.resourceId = resourceId;
    }

    @Override
    public String toString() {
        return "AuditMessage{"
                + "user=" + user
                + ", Date=" + auditDate
                + ", objectType" + objectType
                + ", operationType=" + operationType
                + ", resourceId='" + resourceId + '\'';
    }
}
