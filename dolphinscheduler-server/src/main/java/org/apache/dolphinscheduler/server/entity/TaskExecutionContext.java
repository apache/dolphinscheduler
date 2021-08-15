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

package org.apache.dolphinscheduler.server.entity;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRequestCommand;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * master/worker task transport
 */
public class TaskExecutionContext implements Serializable {

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date firstSubmitTime;

    /**
     * task start time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
     * resources full name and tenant code
     */
    private Map<String, String> resources;

    /**
     * sql TaskExecutionContext
     */
    private SQLTaskExecutionContext sqlTaskExecutionContext;

    /**
     * datax TaskExecutionContext
     */
    private DataxTaskExecutionContext dataxTaskExecutionContext;

    /**
     * dependence TaskExecutionContext
     */
    private DependenceTaskExecutionContext dependenceTaskExecutionContext;

    /**
     * sqoop TaskExecutionContext
     */
    private SqoopTaskExecutionContext sqoopTaskExecutionContext;

    /**
     * taskInstance varPool
     */
    private String varPool;

    /**
     * procedure TaskExecutionContext
     */
    private ProcedureTaskExecutionContext procedureTaskExecutionContext;

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

    public long getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(long projectCode) {
        this.projectCode = projectCode;
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

    public ExecutionStatus getCurrentExecutionStatus() {
        return currentExecutionStatus;
    }

    public void setCurrentExecutionStatus(ExecutionStatus currentExecutionStatus) {
        this.currentExecutionStatus = currentExecutionStatus;
    }

    public SQLTaskExecutionContext getSqlTaskExecutionContext() {
        return sqlTaskExecutionContext;
    }

    public void setSqlTaskExecutionContext(SQLTaskExecutionContext sqlTaskExecutionContext) {
        this.sqlTaskExecutionContext = sqlTaskExecutionContext;
    }

    public DataxTaskExecutionContext getDataxTaskExecutionContext() {
        return dataxTaskExecutionContext;
    }

    public void setDataxTaskExecutionContext(DataxTaskExecutionContext dataxTaskExecutionContext) {
        this.dataxTaskExecutionContext = dataxTaskExecutionContext;
    }

    public ProcedureTaskExecutionContext getProcedureTaskExecutionContext() {
        return procedureTaskExecutionContext;
    }

    public void setProcedureTaskExecutionContext(ProcedureTaskExecutionContext procedureTaskExecutionContext) {
        this.procedureTaskExecutionContext = procedureTaskExecutionContext;
    }

    public Command toCommand() {
        TaskExecuteRequestCommand requestCommand = new TaskExecuteRequestCommand();
        requestCommand.setTaskExecutionContext(JSONUtils.toJsonString(this));
        return requestCommand.convert2Command();
    }

    public DependenceTaskExecutionContext getDependenceTaskExecutionContext() {
        return dependenceTaskExecutionContext;
    }

    public void setDependenceTaskExecutionContext(DependenceTaskExecutionContext dependenceTaskExecutionContext) {
        this.dependenceTaskExecutionContext = dependenceTaskExecutionContext;
    }

    public Map<String, String> getResources() {
        return resources;
    }

    public void setResources(Map<String, String> resources) {
        this.resources = resources;
    }

    public SqoopTaskExecutionContext getSqoopTaskExecutionContext() {
        return sqoopTaskExecutionContext;
    }

    public void setSqoopTaskExecutionContext(SqoopTaskExecutionContext sqoopTaskExecutionContext) {
        this.sqoopTaskExecutionContext = sqoopTaskExecutionContext;
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
                + ", definedParams=" + definedParams
                + ", taskAppId='" + taskAppId + '\''
                + ", taskTimeoutStrategy=" + taskTimeoutStrategy
                + ", taskTimeout=" + taskTimeout
                + ", workerGroup='" + workerGroup + '\''
                + ", delayTime=" + delayTime
                + ", resources=" + resources
                + ", sqlTaskExecutionContext=" + sqlTaskExecutionContext
                + ", dataxTaskExecutionContext=" + dataxTaskExecutionContext
                + ", dependenceTaskExecutionContext=" + dependenceTaskExecutionContext
                + ", sqoopTaskExecutionContext=" + sqoopTaskExecutionContext
                + ", procedureTaskExecutionContext=" + procedureTaskExecutionContext
                + '}';
    }

    public String getVarPool() {
        return varPool;
    }

    public void setVarPool(String varPool) {
        this.varPool = varPool;
    }
}
