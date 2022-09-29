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
import io.swagger.annotations.ApiModelProperty;

/**
 * task update request
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TaskUpdateRequest {

    @ApiModelProperty(example = "workflow-code", required = true)
    private long workflowCode;
    @ApiModelProperty(example = "task-name")
    private String name;
    @ApiModelProperty(example = "describe what this task actual do")
    private String description;

    @ApiModelProperty(example = "SHELL")
    private String taskType;

    // todo
    @ApiModelProperty(example = "{\"localParams\": [], \"rawScript\": \"echo 1\", \"resourceList\": []}", notes = "task definition params")
    private String taskParams;

    @ApiModelProperty(example = "YES", allowableValues = "YES,NO", notes = "default YES is not provided")
    private String flag;

    @ApiModelProperty(example = "MEDIUM", allowableValues = "HIGHEST,HIGH,MEDIUM,LOW,LOWEST", notes = "default MEDIUM is not provided")
    private String taskPriority;

    @ApiModelProperty(example = "default", notes = "default 'default' if not provided")
    private String workerGroup;

    @ApiModelProperty(example = "6563415109312", dataType = "Long")
    private long environmentCode;

    @ApiModelProperty(example = "0", dataType = "Integer", notes = "default 0 not provided")
    private int failRetryTimes;

    @ApiModelProperty(example = "1")
    private int failRetryInterval;

    @ApiModelProperty(example = "SHELL")
    private int timeout;

    @ApiModelProperty(example = "CLOSE", allowableValues = "CLOSE,OPEN", notes = "default CLOSE is not provided")
    private String timeoutFlag;

    @ApiModelProperty(example = "MEDIUM", allowableValues = "WARN,FAILED,WARNFAILED", notes = "default MEDIUM is not provided")
    private String timeoutNotifyStrategy;

    @ApiModelProperty(example = "1,2,3")
    private String resourceIds;

    @ApiModelProperty(example = "2")
    private int taskGroupId;

    @ApiModelProperty(example = "1", dataType = "Integer", notes = "A priority number for execute task, the bigger the high priority, default null if not provided")
    private int taskGroupPriority;

    @ApiModelProperty(example = "0.1", dataType = "Float", notes = "default unlimited if not provided")
    private Integer cpuQuota;

    @ApiModelProperty(example = "0.1", dataType = "Float", notes = "default unlimited if not provided")
    private Integer memoryMax;

    @ApiModelProperty(example = "upstream-task-codes1,upstream-task-codes2", notes = "use , to split multiple upstream task codes")
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
