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

import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRequestCommand;
import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 *  master/worker task transport
 */
public class TaskExecutionContext implements Serializable{

    /**
     *  task id
     */
    private int taskInstanceId;


    /**
     *  taks name
     */
    private String taskName;

    /**
     *  task start time
     */
    private Date startTime;

    /**
     *  task type
     */
    private String taskType;

    /**
     * host
     */
    private String host;
    
    /**
     *  task execute path
     */
    private String executePath;

    /**
     * log path
     */
    private String logPath;

    /**
     *  task json
     */
    private String taskJson;

    /**
     * processId
     */
    private int processId;

    /**
     * appIds
     */
    private String appIds;

    /**
     *  process instance id
     */
    private int processInstanceId;


    /**
     *  process instance schedule time
     */
    private Date scheduleTime;

    /**
     *  process instance global parameters
     */
    private String globalParams;


    /**
     *  execute user id
     */
    private int executorId;


    /**
     *  command type if complement
     */
    private int cmdTypeIfComplement;


    /**
     *  tenant code
     */
    private String tenantCode;

    /**
     *  task queue
     */
    private String queue;


    /**
     *  process define id
     */
    private int processDefineId;

    /**
     *  project id
     */
    private int projectId;

    /**
     * taskParams
     */
    private String taskParams;

    /**
     *  envFile
     */
    private String envFile;

    /**
     *  definedParams
     */
    private Map<String, String> definedParams;

    /**
     * task AppId
     */
    private String taskAppId;

    /**
     *  task timeout strategy
     */
    private int taskTimeoutStrategy;

    /**
     * task timeout
     */
    private int taskTimeout;

    /**
     * worker group
     */
    private String workerGroup;

    /**
     * resources full name and tenant code
     */
    private Map<String,String> resources;

    /**
     *  sql TaskExecutionContext
     */
    private SQLTaskExecutionContext sqlTaskExecutionContext;

    /**
     *  datax TaskExecutionContext
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
     *  procedure TaskExecutionContext
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

    public int getTaskTimeoutStrategy() {
        return taskTimeoutStrategy;
    }

    public void setTaskTimeoutStrategy(int taskTimeoutStrategy) {
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

    public Command toCommand(){
        TaskExecuteRequestCommand requestCommand = new TaskExecuteRequestCommand();
        requestCommand.setTaskExecutionContext(FastJsonSerializer.serializeToString(this));
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
        return "TaskExecutionContext{" +
                "taskInstanceId=" + taskInstanceId +
                ", taskName='" + taskName + '\'' +
                ", startTime=" + startTime +
                ", taskType='" + taskType + '\'' +
                ", host='" + host + '\'' +
                ", executePath='" + executePath + '\'' +
                ", logPath='" + logPath + '\'' +
                ", taskJson='" + taskJson + '\'' +
                ", processId=" + processId +
                ", appIds='" + appIds + '\'' +
                ", processInstanceId=" + processInstanceId +
                ", scheduleTime=" + scheduleTime +
                ", globalParams='" + globalParams + '\'' +
                ", executorId=" + executorId +
                ", cmdTypeIfComplement=" + cmdTypeIfComplement +
                ", tenantCode='" + tenantCode + '\'' +
                ", queue='" + queue + '\'' +
                ", processDefineId=" + processDefineId +
                ", projectId=" + projectId +
                ", taskParams='" + taskParams + '\'' +
                ", envFile='" + envFile + '\'' +
                ", definedParams=" + definedParams +
                ", taskAppId='" + taskAppId + '\'' +
                ", taskTimeoutStrategy=" + taskTimeoutStrategy +
                ", taskTimeout=" + taskTimeout +
                ", workerGroup='" + workerGroup + '\'' +
                ", resources=" + resources +
                ", sqlTaskExecutionContext=" + sqlTaskExecutionContext +
                ", dataxTaskExecutionContext=" + dataxTaskExecutionContext +
                ", dependenceTaskExecutionContext=" + dependenceTaskExecutionContext +
                ", sqoopTaskExecutionContext=" + sqoopTaskExecutionContext +
                ", procedureTaskExecutionContext=" + procedureTaskExecutionContext +
                '}';
    }
}
