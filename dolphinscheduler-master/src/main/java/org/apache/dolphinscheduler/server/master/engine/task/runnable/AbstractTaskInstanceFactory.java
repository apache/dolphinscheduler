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

package org.apache.dolphinscheduler.server.master.engine.task.runnable;

import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentMapper;
import org.apache.dolphinscheduler.dao.utils.EnvironmentUtils;
import org.apache.dolphinscheduler.dao.utils.WorkerGroupUtils;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractTaskInstanceFactory<BUILDER extends ITaskInstanceFactory.ITaskInstanceBuilder>
        implements
            ITaskInstanceFactory<BUILDER> {

    @Autowired
    protected EnvironmentMapper environmentMapper;

    protected TaskInstance cloneTaskInstance(TaskInstance originTaskInstance) {
        final TaskInstance result = new TaskInstance();
        result.setId(originTaskInstance.getId());
        result.setName(originTaskInstance.getName());
        result.setTaskType(originTaskInstance.getTaskType());
        result.setWorkflowInstanceId(originTaskInstance.getWorkflowInstanceId());
        result.setWorkflowInstanceName(originTaskInstance.getWorkflowInstanceName());
        result.setProjectCode(originTaskInstance.getProjectCode());
        result.setTaskCode(originTaskInstance.getTaskCode());
        result.setTaskDefinitionVersion(originTaskInstance.getTaskDefinitionVersion());
        result.setState(originTaskInstance.getState());
        result.setFirstSubmitTime(originTaskInstance.getFirstSubmitTime());
        result.setSubmitTime(originTaskInstance.getSubmitTime());
        result.setStartTime(originTaskInstance.getStartTime());
        result.setEndTime(originTaskInstance.getEndTime());
        result.setHost(originTaskInstance.getHost());
        result.setExecutePath(originTaskInstance.getExecutePath());
        result.setLogPath(originTaskInstance.getLogPath());
        result.setRetryTimes(originTaskInstance.getRetryTimes());
        result.setAlertFlag(originTaskInstance.getAlertFlag());
        result.setPid(originTaskInstance.getPid());
        result.setAppLink(originTaskInstance.getAppLink());
        result.setFlag(originTaskInstance.getFlag());
        result.setIsCache(originTaskInstance.getIsCache());
        result.setMaxRetryTimes(originTaskInstance.getMaxRetryTimes());
        result.setRetryInterval(originTaskInstance.getRetryInterval());
        result.setTaskInstancePriority(originTaskInstance.getTaskInstancePriority());
        result.setWorkerGroup(originTaskInstance.getWorkerGroup());
        result.setEnvironmentCode(originTaskInstance.getEnvironmentCode());
        result.setEnvironmentConfig(originTaskInstance.getEnvironmentConfig());
        result.setExecutorId(originTaskInstance.getExecutorId());
        result.setVarPool(originTaskInstance.getVarPool());
        result.setExecutorName(originTaskInstance.getExecutorName());
        result.setDelayTime(originTaskInstance.getDelayTime());
        result.setTaskParams(originTaskInstance.getTaskParams());
        result.setDryRun(originTaskInstance.getDryRun());
        result.setTaskGroupId(originTaskInstance.getTaskGroupId());
        result.setCpuQuota(originTaskInstance.getCpuQuota());
        result.setMemoryMax(originTaskInstance.getMemoryMax());
        result.setTaskExecuteType(originTaskInstance.getTaskExecuteType());
        result.setTestFlag(originTaskInstance.getTestFlag());
        return result;
    }

    protected void injectMetadataFromTaskDefinition(TaskInstance taskInstance, TaskDefinition taskDefinition) {
        taskInstance.setName(taskDefinition.getName());
        taskInstance.setTaskType(taskDefinition.getTaskType());
        taskInstance.setTaskCode(taskDefinition.getCode());
        taskInstance.setTaskDefinitionVersion(taskDefinition.getVersion());
        taskInstance.setIsCache(taskDefinition.getIsCache());
        taskInstance.setMaxRetryTimes(taskDefinition.getFailRetryTimes());
        taskInstance.setRetryInterval(taskDefinition.getFailRetryInterval());
        taskInstance.setTaskInstancePriority(taskDefinition.getTaskPriority());
        taskInstance.setWorkerGroup(
                WorkerGroupUtils.getWorkerGroupOrDefault(
                        taskInstance.getWorkerGroup(), taskDefinition.getWorkerGroup()));
        taskInstance.setEnvironmentCode(
                EnvironmentUtils.getEnvironmentCodeOrDefault(
                        taskInstance.getEnvironmentCode(), taskDefinition.getEnvironmentCode()));
        taskInstance.setDelayTime(taskDefinition.getDelayTime());
        taskInstance.setTaskParams(taskDefinition.getTaskParams());
        taskInstance.setTaskGroupId(taskDefinition.getTaskGroupId());
        taskInstance.setCpuQuota(taskDefinition.getCpuQuota());
        taskInstance.setMemoryMax(taskDefinition.getMemoryMax());
        taskInstance.setTaskExecuteType(taskDefinition.getTaskExecuteType());
    }

    protected void injectMetadataFromWorkflowInstance(TaskInstance taskInstance, WorkflowInstance workflowInstance) {
        taskInstance.setWorkflowInstanceId(workflowInstance.getId());
        taskInstance.setWorkflowInstanceName(workflowInstance.getName());
        taskInstance.setProjectCode(workflowInstance.getProjectCode());
        taskInstance.setWorkerGroup(
                WorkerGroupUtils.getWorkerGroupOrDefault(
                        taskInstance.getWorkerGroup(), workflowInstance.getWorkerGroup()));
        taskInstance.setEnvironmentCode(
                EnvironmentUtils.getEnvironmentCodeOrDefault(
                        taskInstance.getEnvironmentCode(), workflowInstance.getEnvironmentCode()));
        taskInstance.setExecutorId(workflowInstance.getExecutorId());
        taskInstance.setVarPool(workflowInstance.getVarPool());
        taskInstance.setExecutorName(workflowInstance.getExecutorName());
        taskInstance.setDryRun(workflowInstance.getDryRun());
        taskInstance.setTestFlag(workflowInstance.getTestFlag());
    }

    protected void injectEnvironmentConfigFromDB(TaskInstance taskInstance) {
        if (EnvironmentUtils.isEnvironmentCodeEmpty(taskInstance.getEnvironmentCode())) {
            return;
        }
        Environment environment = environmentMapper.queryByEnvironmentCode(taskInstance.getEnvironmentCode());
        if (environment == null) {
            throw new IllegalArgumentException("Cannot find the environment: " + taskInstance.getEnvironmentCode());
        }
        taskInstance.setEnvironmentConfig(environment.getConfig());
    }
}
