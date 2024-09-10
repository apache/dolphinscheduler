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

package org.apache.dolphinscheduler.extract.master.transportor.workflow;

import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowManualTriggerRequest {

    private Integer userId;

    private Long workflowDefinitionCode;

    private Integer workflowDefinitionVersion;

    private List<Long> startNodes;

    @Builder.Default
    private FailureStrategy failureStrategy = FailureStrategy.CONTINUE;

    @Builder.Default
    private TaskDependType taskDependType = TaskDependType.TASK_POST;

    @Builder.Default
    private WarningType warningType = WarningType.NONE;

    private Integer warningGroupId;

    @Builder.Default
    private Priority workflowInstancePriority = Priority.MEDIUM;

    private String workerGroup;

    private String tenantCode;

    private Long environmentCode;

    @Builder.Default
    private List<Property> startParamList = new ArrayList<>();

    @Builder.Default
    private Flag dryRun = Flag.NO;

    @Builder.Default
    private Flag testFlag = Flag.NO;
}
