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

package org.apache.dolphinscheduler.api.dto.workflow;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.ExecutionOrder;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowBackFillRequest {

    private User loginUser;

    private long workflowDefinitionCode;

    private String startNodes;

    private FailureStrategy failureStrategy;

    private TaskDependType taskDependType;

    private CommandType execType;

    private WarningType warningType;

    private Integer warningGroupId;

    private Priority workflowInstancePriority;

    private String workerGroup;

    private String tenantCode;

    private Long environmentCode;

    private String startParamList;

    private Flag dryRun;

    private Flag testFlag;

    private RunMode backfillRunMode;

    private BackfillTime backfillTime;

    private Integer expectedParallelismNumber;

    private ComplementDependentMode backfillDependentMode;

    private boolean allLevelDependent;

    private ExecutionOrder executionOrder;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BackfillTime {

        private String complementStartDate;
        private String complementEndDate;
        private String complementScheduleDateList;

    }

}
