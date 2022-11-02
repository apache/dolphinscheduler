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

package org.apache.dolphinscheduler.server.master.builder;

import static org.apache.dolphinscheduler.common.constants.Constants.SEC_2_MINUTES_TIME_UNIT;

import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.DataQualityTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  TaskExecutionContext builder
 */

public class TaskExecutionContextBuilder {

    protected final Logger logger =
            LoggerFactory.getLogger(String.format(TaskConstants.TASK_LOG_LOGGER_NAME_FORMAT, getClass()));

    public static TaskExecutionContextBuilder get() {
        return new TaskExecutionContextBuilder();
    }

    private TaskExecutionContext taskExecutionContext = new TaskExecutionContext();

    /**
     * build taskInstance related info
     *
     * @param taskInstance taskInstance
     * @return TaskExecutionContextBuilder
     */
    public TaskExecutionContextBuilder buildTaskInstanceRelatedInfo(TaskInstance taskInstance) {
        taskExecutionContext.setTaskInstanceId(taskInstance.getId());
        taskExecutionContext.setTaskName(taskInstance.getName());
        taskExecutionContext.setFirstSubmitTime(DateUtils.dateToTimeStamp(taskInstance.getFirstSubmitTime()));
        taskExecutionContext.setStartTime(DateUtils.dateToTimeStamp(taskInstance.getStartTime()));
        taskExecutionContext.setTaskType(taskInstance.getTaskType());
        taskExecutionContext.setLogPath(taskInstance.getLogPath());
        taskExecutionContext.setWorkerGroup(taskInstance.getWorkerGroup());
        taskExecutionContext.setEnvironmentConfig(taskInstance.getEnvironmentConfig());
        taskExecutionContext.setHost(taskInstance.getHost());
        taskExecutionContext.setResources(taskInstance.getResources());
        taskExecutionContext.setDelayTime(taskInstance.getDelayTime());
        taskExecutionContext.setVarPool(taskInstance.getVarPool());
        taskExecutionContext.setDryRun(taskInstance.getDryRun());
        taskExecutionContext.setTestFlag(taskInstance.getTestFlag());
        taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.SUBMITTED_SUCCESS);
        taskExecutionContext.setCpuQuota(taskInstance.getCpuQuota());
        taskExecutionContext.setMemoryMax(taskInstance.getMemoryMax());
        taskExecutionContext.setAppIds(taskInstance.getAppLink());
        return this;
    }

    public TaskExecutionContextBuilder buildTaskDefinitionRelatedInfo(TaskDefinition taskDefinition) {
        taskExecutionContext.setTaskTimeout(Integer.MAX_VALUE);
        if (taskDefinition.getTimeoutFlag() == TimeoutFlag.OPEN) {
            taskExecutionContext.setTaskTimeoutStrategy(taskDefinition.getTimeoutNotifyStrategy());
            if (taskDefinition.getTimeoutNotifyStrategy() == TaskTimeoutStrategy.FAILED
                    || taskDefinition.getTimeoutNotifyStrategy() == TaskTimeoutStrategy.WARNFAILED) {
                taskExecutionContext.setTaskTimeout(
                        Math.min(taskDefinition.getTimeout() * SEC_2_MINUTES_TIME_UNIT, Integer.MAX_VALUE));
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
        taskExecutionContext.setScheduleTime(DateUtils.dateToTimeStamp(processInstance.getScheduleTime()));
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
     * build global and local params
     * @param propertyMap
     * @return
     */
    public TaskExecutionContextBuilder buildParamInfo(Map<String, Property> propertyMap) {
        taskExecutionContext.setPrepareParamsMap(propertyMap);
        return this;
    }

    /**
     * build business params
     * @param businessParamsMap
     * @return
     */
    public TaskExecutionContextBuilder buildBusinessParamsMap(Map<String, Property> businessParamsMap) {
        taskExecutionContext.setParamsMap(businessParamsMap);
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
