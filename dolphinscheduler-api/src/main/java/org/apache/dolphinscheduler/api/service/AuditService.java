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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.AuditModuleType;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.dao.entity.User;

/**
 * audit information service
 */
public interface AuditService {

    /**
     * add new audit record
     *
     * @param user          login user
     * @param module        module type
     * @param operation     operation type
     * @param projectName   project name
     * @param processName   process name
     */
    void addAudit(User user, AuditModuleType module, AuditOperationType operation,
                  String projectName, String processName);

    /**
     * query audit log list
     *
     * @param loginUser         login user
     * @param moduleType        module type
     * @param operationType     operation type
     * @param startTime         start time
     * @param endTime           end time
     * @param userName          query user name
     * @param projectName       project name
     * @param processName       process name
     * @param pageNo            page number
     * @param pageSize          page size
     * @return                  audit log string
     */
    Result queryLogListPaging(User loginUser, AuditModuleType moduleType,
                              AuditOperationType operationType, String startTime,
                              String endTime, String userName,
                              String projectName, String processName,
                              Integer pageNo, Integer pageSize);
}
