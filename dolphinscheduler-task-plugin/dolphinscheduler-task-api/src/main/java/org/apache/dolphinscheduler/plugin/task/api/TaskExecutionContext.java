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

package org.apache.dolphinscheduler.plugin.task.api;

import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

import java.util.Date;
import java.util.Map;

/**
 * to master/worker task transport
 */
public class TaskExecutionContext {

    /**
     * task id
     */
    private int taskInstanceId;

    /**
     * task name
     */
    private String taskName;

    /**
     * task first submit time.
     */
    private Date firstSubmitTime;

    /**
     * task start time
     */
    private Date startTime;

    /**
     * task type
     */
    private String taskType;

    /**
     * host
     */
    private String host;

    /**
     * task execute path
     */
    private String executePath;

    /**
     * log path
     */
    private String logPath;

    /**
     * task json
     */
    private String taskJson;

    /**
     * processId
     */
    private int processId;

    /**
     * processCode
     */
    private Long processDefineCode;

    /**
     * processVersion
     */
    private int processDefineVersion;

    /**
     * appIds
     */
    private String appIds;

    /**
     * process instance id
     */
    private int processInstanceId;


    /**
     * process instance schedule time
     */
    private Date scheduleTime;

    /**
     * process instance global parameters
     */
    private String globalParams;


    /**
     * execute user id
     */
    private int executorId;


    /**
     * command type if complement
     */
    private int cmdTypeIfComplement;


    /**
     * tenant code
     */
    private String tenantCode;

    /**
     * task queue
     */
    private String queue;


    /**
     * process define id
     */
    private int processDefineId;

    /**
     * project id
     */
    private int projectId;

    /**
     * project code
     */
    private long projectCode;

    /**
     * taskParams
     */
    private String taskParams;

    /**
     * envFile
     */
    private String envFile;

    /**
     * environmentConfig
     */
    private String environmentConfig;

    /**
     * definedParams
     */
    private Map<String, String> definedParams;

    /**
     * task AppId
     */
    private String taskAppId;

    /**
     * task timeout strategy
     */
    private TaskTimeoutStrategy taskTimeoutStrategy;

    /**
     * task timeout
     */
    private int taskTimeout;

    /**
     * worker group
     */
    private String workerGroup;

    /**
     * delay execution time.
     */
    private int delayTime;

    /**
     * current execution status
     */
    private ExecutionStatus currentExecutionStatus;

    /**
     *  Task Logger name should be like: Task-{processDefinitionId}-{processInstanceId}-{taskInstanceId}
     */
    private String taskLogName;

    private ResourceParametersHelper resourceParametersHelper;

    /**
     * endTime
     */
    private Date endTime;

    /**
     * sql TaskExecutionContext
     */
    private SQLTaskExecutionContext sqlTaskExecutionContext;
    /**
     * k8s TaskExecutionContext
     */
    private K8sTaskExecutionContext k8sTaskExecutionContext;
    /**
     * resources full name and tenant code
     */
    private Map<String, String> resources;

    /**
     * taskInstance varPool
     */
    private String varPool;

    /**
     * dry run flag
     */
    private int dryRun;

    private Map<String, Property> paramsMap;

    private DataQualityTaskExecutionContext dataQualityTaskExecutionContext;

    public String getTaskLogName() {
        return taskLogName;
    }

    public void setTaskLogName(String taskLogName) {
        this.taskLogName = taskLogName;
    }

    public Map<String, String> getResources() {
        return resources;
    }

    public void setResources(Map<String, String> resources) {
        this.resources = resources;
    }

    public Map<String, Property> getParamsMap() {
        return paramsMap;
    }

    public void setParamsMap(Map<String, Property> paramsMap) {
        this.paramsMap = paramsMap;
    }

    public int getTaskInstanceId() {
        return taskInstanceId;
    }

    public void setTaskInstanceId(int taskInstanceId) {
        this.taskInstanceId = taskInstanceId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Date getFirstSubmitTime() {
        return firstSubmitTime;
    }

    public void setFirstSubmitTime(Date firstSubmitTime) {
        this.firstSubmitTime = firstSubmitTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getExecutePath() {
        return executePath;
    }

    public void setExecutePath(String executePath) {
        this.executePath = executePath;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getTaskJson() {
        return taskJson;
    }

    public void setTaskJson(String taskJson) {
        this.taskJson = taskJson;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public String getAppIds() {
        return appIds;
    }

    public void setAppIds(String appIds) {
        this.appIds = appIds;
    }

    public int getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(int processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public Date getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(Date scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public String getGlobalParams() {
        return globalParams;
    }

    public void setGlobalParams(String globalParams) {
        this.globalParams = globalParams;
    }

    public int getExecutorId() {
        return executorId;
    }

    public void setExecutorId(int executorId) {
        this.executorId = executorId;
    }

    public int getCmdTypeIfComplement() {
        return cmdTypeIfComplement;
    }

    public void setCmdTypeIfComplement(int cmdTypeIfComplement) {
        this.cmdTypeIfComplement = cmdTypeIfComplement;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public int getProcessDefineId() {
        return processDefineId;
    }

    public void setProcessDefineId(int processDefineId) {
        this.processDefineId = processDefineId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getTaskParams() {
        return taskParams;
    }

    public void setTaskParams(String taskParams) {
        this.taskParams = taskParams;
    }

    public String getEnvFile() {
        return envFile;
    }

    public void setEnvFile(String envFile) {
        this.envFile = envFile;
    }

    public String getEnvironmentConfig() {
        return environmentConfig;
    }

    public void setEnvironmentConfig(String config) {
        this.environmentConfig = config;
    }

    public Map<String, String> getDefinedParams() {
        return definedParams;
    }

    public void setDefinedParams(Map<String, String> definedParams) {
        this.definedParams = definedParams;
    }

    public String getTaskAppId() {
        return taskAppId;
    }

    public void setTaskAppId(String taskAppId) {
        this.taskAppId = taskAppId;
    }

    public TaskTimeoutStrategy getTaskTimeoutStrategy() {
        return taskTimeoutStrategy;
    }

    public void setTaskTimeoutStrategy(TaskTimeoutStrategy taskTimeoutStrategy) {
        this.taskTimeoutStrategy = taskTimeoutStrategy;
    }

    public int getTaskTimeout() {
        return taskTimeout;
    }

    public void setTaskTimeout(int taskTimeout) {
        this.taskTimeout = taskTimeout;
    }

    public String getWorkerGroup() {
        return workerGroup;
    }

    public void setWorkerGroup(String workerGroup) {
        this.workerGroup = workerGroup;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    public ResourceParametersHelper getResourceParametersHelper() {
        return resourceParametersHelper;
    }

    public void setResourceParametersHelper(ResourceParametersHelper resourceParametersHelper) {
        this.resourceParametersHelper = resourceParametersHelper;
    }

    public String getVarPool() {
        return varPool;
    }

    public void setVarPool(String varPool) {
        this.varPool = varPool;
    }

    public int getDryRun() {
        return dryRun;
    }

    public void setDryRun(int dryRun) {
        this.dryRun = dryRun;
    }

    public Long getProcessDefineCode() {
        return processDefineCode;
    }

    public void setProcessDefineCode(Long processDefineCode) {
        this.processDefineCode = processDefineCode;
    }

    public int getProcessDefineVersion() {
        return processDefineVersion;
    }

    public void setProcessDefineVersion(int processDefineVersion) {
        this.processDefineVersion = processDefineVersion;
    }

    public long getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(long projectCode) {
        this.projectCode = projectCode;
    }

    public DataQualityTaskExecutionContext getDataQualityTaskExecutionContext() {
        return dataQualityTaskExecutionContext;
    }

    public void setDataQualityTaskExecutionContext(DataQualityTaskExecutionContext dataQualityTaskExecutionContext) {
        this.dataQualityTaskExecutionContext = dataQualityTaskExecutionContext;
    }

    public void setCurrentExecutionStatus(ExecutionStatus currentExecutionStatus) {
        this.currentExecutionStatus = currentExecutionStatus;
    }

    public ExecutionStatus getCurrentExecutionStatus() {
        return currentExecutionStatus;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public K8sTaskExecutionContext getK8sTaskExecutionContext() {
        return k8sTaskExecutionContext;
    }

    public void setK8sTaskExecutionContext(K8sTaskExecutionContext k8sTaskExecutionContext) {
        this.k8sTaskExecutionContext = k8sTaskExecutionContext;
    }

    @Override
    public String toString() {
        return "TaskExecutionContext{"
                + "taskInstanceId=" + taskInstanceId
                + ", taskName='" + taskName + '\''
                + ", currentExecutionStatus=" + currentExecutionStatus
                + ", firstSubmitTime=" + firstSubmitTime
                + ", startTime=" + startTime
                + ", taskType='" + taskType + '\''
                + ", host='" + host + '\''
                + ", executePath='" + executePath + '\''
                + ", logPath='" + logPath + '\''
                + ", taskJson='" + taskJson + '\''
                + ", processId=" + processId
                + ", processDefineCode=" + processDefineCode
                + ", processDefineVersion=" + processDefineVersion
                + ", appIds='" + appIds + '\''
                + ", processInstanceId=" + processInstanceId
                + ", scheduleTime=" + scheduleTime
                + ", globalParams='" + globalParams + '\''
                + ", executorId=" + executorId
                + ", cmdTypeIfComplement=" + cmdTypeIfComplement
                + ", tenantCode='" + tenantCode + '\''
                + ", queue='" + queue + '\''
                + ", projectCode=" + projectCode
                + ", taskParams='" + taskParams + '\''
                + ", envFile='" + envFile + '\''
                + ", dryRun='" + dryRun + '\''
                + ", definedParams=" + definedParams
                + ", taskAppId='" + taskAppId + '\''
                + ", taskTimeoutStrategy=" + taskTimeoutStrategy
                + ", taskTimeout=" + taskTimeout
                + ", workerGroup='" + workerGroup + '\''
                + ", environmentConfig='" + environmentConfig + '\''
                + ", delayTime=" + delayTime
                + ", resources=" + resources
                + ", sqlTaskExecutionContext=" + sqlTaskExecutionContext
                + ", k8sTaskExecutionContext=" + k8sTaskExecutionContext
                + ", dataQualityTaskExecutionContext=" + dataQualityTaskExecutionContext
                + '}';
    }

}
