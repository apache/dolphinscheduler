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

package org.apache.dolphinscheduler.api.dto.task;

import static org.apache.dolphinscheduler.common.constants.Constants.VERSION_FIRST;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;

import java.util.Date;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * task create request
 */
@Data
public class TaskCreateRequest {

    @Schema(example = "workflow-code", required = true)
    private long workflowCode;
    @Schema(example = "task-name", required = true)
    private String name;
    @Schema(example = "describe what this task actual do", required = true)
    private String description;

    @Schema(example = "6816095515584", implementation = Long.class)
    private long projectCode;

    @Schema(example = "SHELL", required = true)
    private String taskType;

    // todo
    @Schema(example = "{\"localParams\": [], \"rawScript\": \"echo 1\", \"resourceList\": []}", required = true, description = "task definition params")
    private String taskParams;

    @Schema(example = "YES", allowableValues = "YES,NO", description = "default YES is not provided")
    private String flag;

    @Schema(example = "MEDIUM", allowableValues = "HIGHEST,HIGH,MEDIUM,LOW,LOWEST", description = "default MEDIUM is not provided")
    private String taskPriority;

    @Schema(example = "default", description = "default 'default' if not provided")
    private String workerGroup;

    @Schema(example = "6563415109312", implementation = Long.class)
    private long environmentCode;

    @Schema(example = "0", implementation = int.class, description = "default 0 not provided")
    private int failRetryTimes;

    @Schema(example = "1")
    private int failRetryInterval;

    @Schema(example = "SHELL")
    private int timeout;

    @Schema(example = "CLOSE", allowableValues = "CLOSE,OPEN", description = "default CLOSE is not provided")
    private String timeoutFlag;

    @Schema(example = "MEDIUM", allowableValues = "WARN,FAILED,WARNFAILED", description = "default MEDIUM is not provided")
    private String timeoutNotifyStrategy;

    @Schema(example = "1,2,3")
    private String resourceIds;

    @Schema(example = "2")
    private int taskGroupId;

    @Schema(example = "1", implementation = int.class, description = "A priority number for execute task, the bigger the high priority, default null if not provided")
    private int taskGroupPriority;

    @Schema(example = "1", implementation = Integer.class, description = "default unlimited if not provided")
    private Integer cpuQuota;

    @Schema(example = "0.1", implementation = Integer.class, description = "default unlimited if not provided")
    private Integer memoryMax;

    @Schema(example = "upstream-task-codes1,upstream-task-codes2", description = "use , to split multiple upstream task codes")
    private String upstreamTasksCodes;

    public TaskDefinition convert2TaskDefinition() {
        TaskDefinition taskDefinition = new TaskDefinition();

        taskDefinition.setName(this.name);
        taskDefinition.setDescription(this.description);
        taskDefinition.setProjectCode(this.projectCode);
        taskDefinition.setTaskType(this.taskType);
        taskDefinition.setTaskParams(this.taskParams);
        taskDefinition.setWorkerGroup(this.workerGroup == null ? Constants.DEFAULT_WORKER_GROUP : this.workerGroup);
        taskDefinition.setEnvironmentCode(this.environmentCode);
        taskDefinition.setFailRetryTimes(this.failRetryTimes);
        taskDefinition.setFailRetryInterval(this.failRetryInterval);
        taskDefinition.setTimeout(this.timeout);
        taskDefinition.setResourceIds(this.resourceIds);
        taskDefinition.setTaskGroupId(this.taskGroupId);
        taskDefinition.setTaskGroupPriority(this.taskGroupPriority);
        taskDefinition.setCpuQuota(this.cpuQuota);
        taskDefinition.setMemoryMax(this.memoryMax);

        Flag flagCreate = this.flag == null ? Flag.YES : Flag.valueOf(this.flag);
        taskDefinition.setFlag(flagCreate);

        TimeoutFlag timeoutFlagCreate =
                this.timeoutFlag == null ? TimeoutFlag.CLOSE : TimeoutFlag.valueOf(this.timeoutFlag);
        taskDefinition.setTimeoutFlag(timeoutFlagCreate);

        Priority taskPriorityCreate = this.taskPriority == null ? Priority.MEDIUM : Priority.valueOf(this.taskPriority);
        taskDefinition.setTaskPriority(taskPriorityCreate);

        TaskTimeoutStrategy taskTimeoutStrategyCreate = this.timeoutNotifyStrategy == null ? TaskTimeoutStrategy.WARN
                : TaskTimeoutStrategy.valueOf(this.timeoutNotifyStrategy);
        taskDefinition.setTimeoutNotifyStrategy(taskTimeoutStrategyCreate);

        taskDefinition.setVersion(VERSION_FIRST);
        Date date = new Date();
        taskDefinition.setCreateTime(date);
        taskDefinition.setUpdateTime(date);
        return taskDefinition;
    }
}
