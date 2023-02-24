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

package org.apache.dolphinscheduler.api.executor;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;

import lombok.Data;

// todo: to be interface
@Data
public class ExecuteContext {

    private final ProcessInstance workflowInstance;

    private final ProcessDefinition workflowDefinition;

    private final User executeUser;

    private final ExecuteType executeType;

    public ExecuteContext(ProcessInstance workflowInstance,
                          ProcessDefinition workflowDefinition,
                          User executeUser,
                          ExecuteType executeType) {
        this.workflowInstance = checkNotNull(workflowInstance, "workflowInstance cannot be null");
        this.workflowDefinition = checkNotNull(workflowDefinition, "workflowDefinition cannot be null");
        this.executeUser = checkNotNull(executeUser, "executeUser cannot be null");
        this.executeType = checkNotNull(executeType, "executeType cannot be null");
    }

}
