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
package cn.escheduler.dao.model;

import cn.escheduler.common.enums.ExecutionStatus;
import cn.escheduler.common.enums.Flag;
import cn.escheduler.common.enums.Priority;
import cn.escheduler.common.enums.TaskType;
import cn.escheduler.common.model.TaskNode;
import cn.escheduler.common.utils.JSONUtils;

import java.util.Date;

/**
 * task instance
 */
public class TaskInstance {

    /**
     * id
     */
    private int id;

    /**
     * task name
     */
    private String name;

    /**
     * task type
     */
    private String taskType;

    /**
     * process definition id
     */
    private int processDefinitionId;

    /**
     * process instance id
     */
    private int processInstanceId;

    /**
     * process instance name
     */
    private String processInstanceName;

    /**
     * task json
     */
    private String taskJson;

    /**
     * state
     */
    private ExecutionStatus state;

    /**
     * task submit time
     */
    private Date submitTime;

    /**
     * task start time
     */
    private Date startTime;

    /**
     * task end time
     */
    private Date endTime;

    /**
     * task host
     */
    private String host;

    /**
     * task shell execute path and the resource down from hdfs
     * default path: $base_run_dir/processInstanceId/taskInstanceId/retryTimes
     */
    private String executePath;

    /**
     * task log path
     * default path: $base_run_dir/processInstanceId/taskInstanceId/retryTimes
     */
    private String logPath;

    /**
     * retry times
     */
    private int retryTimes;

    /**
     * alert flag
     */
    private Flag alertFlag;

    /**
     * run flag
     */
    private Flag runFlag;

    /**
     * process instance
     */
    private ProcessInstance processInstance;

    /**
     * process definition
     */
    private ProcessDefinition processDefine;

    /**
     * process id
     */
    private int pid;

    /**
     * appLink
     */
    private String appLink;

    /**
     * flag
     */
    private Flag flag;

    /**
     * dependency
     */
    private String dependency;

    /**
     * duration
     * @return
     */
    private Long duration;

    /**
     * max retry times
     * @return
     */
    private int maxRetryTimes;

    /**
     * task retry interval, unit: minute
     * @return
     */
    private int retryInterval;

    /**
     * task intance priority
     */
    private Priority taskInstancePriority;

    /**
     * process intance priority
     */
    private Priority processInstancePriority;

    /**
     * dependent state
     * @return
     */
    private String dependentResult;


    /**
     * worker group id
     * @return
     */
    private int workerGroupId;



    public void  init(String host,Date startTime,String executePath){
        this.host = host;
        this.startTime = startTime;
        this.executePath = executePath;
    }


    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    public ProcessDefinition getProcessDefine() {
        return processDefine;
    }

    public void setProcessDefine(ProcessDefinition processDefine) {
        this.processDefine = processDefine;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public int getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(int processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public int getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(int processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getTaskJson() {
        return taskJson;
    }

    public void setTaskJson(String taskJson) {
        this.taskJson = taskJson;
    }

    public ExecutionStatus getState() {
        return state;
    }

    public void setState(ExecutionStatus state) {
        this.state = state;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
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

    public Flag getAlertFlag() {
        return alertFlag;
    }

    public void setAlertFlag(Flag alertFlag) {
        this.alertFlag = alertFlag;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public Boolean isTaskSuccess(){
        return this.state == ExecutionStatus.SUCCESS;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getAppLink() {
        return appLink;
    }

    public void setAppLink(String appLink) {
        this.appLink = appLink;
    }


    public Boolean isSubProcess(){
        return TaskType.SUB_PROCESS.toString().equals(this.taskType.toUpperCase());
    }

    public String getDependency(){

        if(this.dependency != null){
            return this.dependency;
        }
        TaskNode taskNode = JSONUtils.parseObject(taskJson, TaskNode.class);

        return taskNode.getDependence();
    }

    public Flag getFlag() {
        return flag;
    }

    public void setFlag(Flag flag) {
        this.flag = flag;
    }
    public String getProcessInstanceName() {
        return processInstanceName;
    }

    public void setProcessInstanceName(String processInstanceName) {
        this.processInstanceName = processInstanceName;
    }

    public Flag getRunFlag() {
        return runFlag;
    }

    public void setRunFlag(Flag runFlag) {
        this.runFlag = runFlag;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public Boolean isTaskComplete() {

        return this.getState().typeIsPause()
                || this.getState().typeIsSuccess()
                || this.getState().typeIsCancel()
                || (this.getState().typeIsFailure() && !taskCanRetry());
    }
    /**
     * 判断是否可以重试
     * @return
     */
    public boolean taskCanRetry() {
        if(this.isSubProcess()){
            return false;
        }
        if(this.getState() == ExecutionStatus.NEED_FAULT_TOLERANCE){
            return true;
        }else {
            return (this.getState().typeIsFailure()
                && this.getRetryTimes() < this.getMaxRetryTimes());
        }
    }

    public void setDependency(String dependency) {
        this.dependency = dependency;
    }

    public Priority getTaskInstancePriority() {
        return taskInstancePriority;
    }

    public void setTaskInstancePriority(Priority taskInstancePriority) {
        this.taskInstancePriority = taskInstancePriority;
    }

    public Priority getProcessInstancePriority() {
        return processInstancePriority;
    }

    public void setProcessInstancePriority(Priority processInstancePriority) {
        this.processInstancePriority = processInstancePriority;
    }

    public int getWorkerGroupId() {
        return workerGroupId;
    }

    public void setWorkerGroupId(int workerGroupId) {
        this.workerGroupId = workerGroupId;
    }

    @Override
    public String toString() {
        return "TaskInstance{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", taskType='" + taskType + '\'' +
                ", processDefinitionId=" + processDefinitionId +
                ", processInstanceId=" + processInstanceId +
                ", processInstanceName='" + processInstanceName + '\'' +
                ", taskJson='" + taskJson + '\'' +
                ", state=" + state +
                ", submitTime=" + submitTime +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", host='" + host + '\'' +
                ", executePath='" + executePath + '\'' +
                ", logPath='" + logPath + '\'' +
                ", retryTimes=" + retryTimes +
                ", alertFlag=" + alertFlag +
                ", runFlag=" + runFlag +
                ", processInstance=" + processInstance +
                ", processDefine=" + processDefine +
                ", pid=" + pid +
                ", appLink='" + appLink + '\'' +
                ", flag=" + flag +
                ", dependency=" + dependency +
                ", duration=" + duration +
                ", maxRetryTimes=" + maxRetryTimes +
                ", retryInterval=" + retryInterval +
                ", taskInstancePriority=" + taskInstancePriority +
                ", processInstancePriority=" + processInstancePriority +
                ", workGroupId=" + workerGroupId +
                '}';
    }

    public String getDependentResult() {
        return dependentResult;
    }

    public void setDependentResult(String dependentResult) {
        this.dependentResult = dependentResult;
    }
}
