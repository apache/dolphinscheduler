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

package org.apache.dolphinscheduler.server.builder;

import static org.apache.dolphinscheduler.common.Constants.SEC_2_MINUTES_TIME_UNIT;

import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.DataQualityTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

/**
 *  TaskExecutionContext builder
 */
public class TaskExecutionContextBuilder {

    public static TaskExecutionContextBuilder get() {
        return new TaskExecutionContextBuilder();
    }

    private TaskExecutionContext taskExecutionContext =  new TaskExecutionContext();

    /**
     * build taskInstance related info
     *
     * @param taskInstance taskInstance
     * @return TaskExecutionContextBuilder
     */
    public TaskExecutionContextBuilder buildTaskInstanceRelatedInfo(TaskInstance taskInstance) {
        taskExecutionContext.setTaskInstanceId(taskInstance.getId());
        taskExecutionContext.setTaskName(taskInstance.getName());
        taskExecutionContext.setFirstSubmitTime(taskInstance.getFirstSubmitTime());
        taskExecutionContext.setStartTime(taskInstance.getStartTime());
        taskExecutionContext.setTaskType(taskInstance.getTaskType());
        taskExecutionContext.setLogPath(taskInstance.getLogPath());
        taskExecutionContext.setWorkerGroup(taskInstance.getWorkerGroup());
        taskExecutionContext.setEnvironmentConfig(taskInstance.getEnvironmentConfig());
        taskExecutionContext.setHost(taskInstance.getHost());
        taskExecutionContext.setResources(taskInstance.getResources());
        taskExecutionContext.setDelayTime(taskInstance.getDelayTime());
        taskExecutionContext.setVarPool(taskInstance.getVarPool());
        taskExecutionContext.setDryRun(taskInstance.getDryRun());
        taskExecutionContext.setCurrentExecutionStatus(ExecutionStatus.SUBMITTED_SUCCESS);
        return this;
    }

    public TaskExecutionContextBuilder buildTaskDefinitionRelatedInfo(TaskDefinition taskDefinition) {
        taskExecutionContext.setTaskTimeout(Integer.MAX_VALUE);
        if (taskDefinition.getTimeoutFlag() == TimeoutFlag.OPEN) {
            taskExecutionContext.setTaskTimeoutStrategy(taskDefinition.getTimeoutNotifyStrategy());
            if (taskDefinition.getTimeoutNotifyStrategy() == TaskTimeoutStrategy.FAILED
                || taskDefinition.getTimeoutNotifyStrategy() == TaskTimeoutStrategy.WARNFAILED) {
                taskExecutionContext.setTaskTimeout(Math.min(taskDefinition.getTimeout() * SEC_2_MINUTES_TIME_UNIT, Integer.MAX_VALUE));
            }
        }
        taskExecutionContext.setTaskParams(taskDefinition.getTaskParams());
        return this;
    }

    /**
     * build processInstance related info
     *
     * @param processInstance processInstance
     * @return TaskExecutionContextBuilder
     */
    public TaskExecutionContextBuilder buildProcessInstanceRelatedInfo(ProcessInstance processInstance) {
        taskExecutionContext.setProcessInstanceId(processInstance.getId());
        taskExecutionContext.setScheduleTime(processInstance.getScheduleTime());
        taskExecutionContext.setGlobalParams(processInstance.getGlobalParams());
        taskExecutionContext.setExecutorId(processInstance.getExecutorId());
        taskExecutionContext.setCmdTypeIfComplement(processInstance.getCmdTypeIfComplement().getCode());
        taskExecutionContext.setTenantCode(processInstance.getTenantCode());
        taskExecutionContext.setQueue(processInstance.getQueue());
        return this;
    }

    /**
     * build processDefinition related info
     *
     * @param processDefinition processDefinition
     * @return TaskExecutionContextBuilder
     */
    public TaskExecutionContextBuilder buildProcessDefinitionRelatedInfo(ProcessDefinition processDefinition) {
        taskExecutionContext.setProcessDefineCode(processDefinition.getCode());
        taskExecutionContext.setProcessDefineVersion(processDefinition.getVersion());
        taskExecutionContext.setProjectCode(processDefinition.getProjectCode());
        return this;
    }

    public TaskExecutionContextBuilder buildDataQualityTaskExecutionContext(DataQualityTaskExecutionContext dataQualityTaskExecutionContext) {
        taskExecutionContext.setDataQualityTaskExecutionContext(dataQualityTaskExecutionContext);
        return this;
    }

    public TaskExecutionContextBuilder buildResourceParametersInfo(ResourceParametersHelper parametersHelper) {
        taskExecutionContext.setResourceParametersHelper(parametersHelper);
        return this;
    }
    /**
     * build k8sTask related info
     *
     * @param k8sTaskExecutionContext sqoopTaskExecutionContext
     * @return TaskExecutionContextBuilder
     */

    public TaskExecutionContextBuilder buildK8sTaskRelatedInfo(K8sTaskExecutionContext k8sTaskExecutionContext) {
        taskExecutionContext.setK8sTaskExecutionContext(k8sTaskExecutionContext);
        return this;
    }
    /**
     * create
     *
     * @return taskExecutionContext
     */

    public TaskExecutionContext create() {
        return taskExecutionContext;
    }

}
