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

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;

import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * task update request
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TaskUpdateRequest {

    @Schema(example = "workflow-code", required = true)
    private long workflowCode;
    @Schema(example = "task-name")
    private String name;
    @Schema(example = "describe what this task actual do")
    private String description;

    @Schema(example = "SHELL")
    private String taskType;

    // todo
    @Schema(example = "{\"localParams\": [], \"rawScript\": \"echo 1\", \"resourceList\": []}", description = "task definition params")
    private String taskParams;

    @Schema(example = "YES", allowableValues = "YES,NO", description = "default YES is not provided")
    private String flag;

    @Schema(example = "MEDIUM", allowableValues = "HIGHEST,HIGH,MEDIUM,LOW,LOWEST", description = "default MEDIUM is not provided")
    private String taskPriority;

    @Schema(example = "default", description = "default 'default' if not provided")
    private String workerGroup;

    @Schema(example = "6563415109312", implementation = long.class)
    private long environmentCode;

    @Schema(example = "0", implementation = Integer.class, description = "default 0 not provided")
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

    @Schema(example = "0.1", implementation = Integer.class, description = "default unlimited if not provided")
    private Integer cpuQuota;

    @Schema(example = "0.1", implementation = Integer.class, description = "default unlimited if not provided")
    private Integer memoryMax;

    @Schema(example = "upstream-task-codes1,upstream-task-codes2", description = "use , to split multiple upstream task codes")
    private String upstreamTasksCodes;

    /**
     * Merge taskUpdateRequest information into exists task definition object
     *
     * @param taskDefinition exists task definition object
     * @return task definition
     */
    public TaskDefinition mergeIntoTaskDefinition(TaskDefinition taskDefinition) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        TaskDefinition taskDefinitionDeepCopy = (TaskDefinition) BeanUtils.cloneBean(taskDefinition);
        assert taskDefinitionDeepCopy != null;
        if (this.name != null) {
            taskDefinitionDeepCopy.setName(this.name);
        }
        if (this.description != null) {
            taskDefinitionDeepCopy.setDescription(this.description);
        }
        if (this.taskType != null) {
            taskDefinitionDeepCopy.setTaskType(this.taskType);
        }
        if (this.taskParams != null) {
            taskDefinitionDeepCopy.setTaskParams(this.taskParams);
        }
        if (this.flag != null) {
            taskDefinitionDeepCopy.setFlag(Flag.valueOf(this.flag));
        }
        if (this.taskPriority != null) {
            taskDefinitionDeepCopy.setTaskPriority(Priority.valueOf(this.taskPriority));
        }
        if (this.workerGroup != null) {
            taskDefinitionDeepCopy.setWorkerGroup(this.workerGroup);
        }
        if (this.environmentCode != 0L) {
            taskDefinitionDeepCopy.setEnvironmentCode(this.environmentCode);
        }
        if (this.failRetryTimes != 0) {
            taskDefinitionDeepCopy.setFailRetryTimes(this.failRetryTimes);
        }
        if (this.failRetryInterval != 0) {
            taskDefinitionDeepCopy.setFailRetryInterval(this.failRetryInterval);
        }
        if (this.timeout != 0) {
            taskDefinitionDeepCopy.setTimeout(this.timeout);
        }
        if (this.timeoutFlag != null) {
            taskDefinitionDeepCopy.setTimeoutFlag(TimeoutFlag.valueOf(this.timeoutFlag));
        }
        if (this.timeoutNotifyStrategy != null) {
            taskDefinitionDeepCopy.setTimeoutNotifyStrategy(TaskTimeoutStrategy.valueOf(this.timeoutNotifyStrategy));
        }
        if (this.resourceIds != null) {
            taskDefinitionDeepCopy.setResourceIds(this.resourceIds);
        }
        if (this.taskGroupId != 0) {
            taskDefinitionDeepCopy.setTaskGroupId(this.taskGroupId);
        }
        if (this.taskGroupPriority != 0) {
            taskDefinitionDeepCopy.setTaskGroupPriority(this.taskGroupPriority);
        }
        if (this.cpuQuota != null) {
            taskDefinitionDeepCopy.setCpuQuota(this.cpuQuota);
        }
        if (this.memoryMax != null) {
            taskDefinitionDeepCopy.setMemoryMax(this.memoryMax);
        }

        if (!taskDefinition.equals(taskDefinitionDeepCopy)) {
            int version = taskDefinitionDeepCopy.getVersion() + 1;
            taskDefinitionDeepCopy.setVersion(version);
            taskDefinitionDeepCopy.setUpdateTime(new Date());
        }
        return taskDefinitionDeepCopy;
    }
}
