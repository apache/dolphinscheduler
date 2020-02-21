package org.apache.dolphinscheduler.remote.command;

import java.io.Serializable;
import java.util.Date;

/**
 *  master/worker task transport
 */
public class TaskInfo implements Serializable{

    /**
     *  task instance id
     */
    private Integer taskId;


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
     *  task execute path
     */
    private String executePath;

    /**
     *  task json
     */
    private String taskJson;


    /**
     *  process instance id
     */
    private Integer processInstanceId;


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
    private Integer executorId;


    /**
     *  command type if complement
     */
    private Integer cmdTypeIfComplement;


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
    private Integer processDefineId;

    /**
     *  project id
     */
    private Integer projectId;

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
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

    public String getExecutePath() {
        return executePath;
    }

    public void setExecutePath(String executePath) {
        this.executePath = executePath;
    }

    public String getTaskJson() {
        return taskJson;
    }

    public void setTaskJson(String taskJson) {
        this.taskJson = taskJson;
    }

    public Integer getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Integer processInstanceId) {
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

    public Integer getProcessDefineId() {
        return processDefineId;
    }

    public void setProcessDefineId(Integer processDefineId) {
        this.processDefineId = processDefineId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getExecutorId() {
        return executorId;
    }

    public void setExecutorId(Integer executorId) {
        this.executorId = executorId;
    }

    public Integer getCmdTypeIfComplement() {
        return cmdTypeIfComplement;
    }

    public void setCmdTypeIfComplement(Integer cmdTypeIfComplement) {
        this.cmdTypeIfComplement = cmdTypeIfComplement;
    }

    @Override
    public String toString() {
        return "TaskInfo{" +
                "taskId=" + taskId +
                ", taskName='" + taskName + '\'' +
                ", startTime=" + startTime +
                ", taskType='" + taskType + '\'' +
                ", executePath='" + executePath + '\'' +
                ", taskJson='" + taskJson + '\'' +
                ", processInstanceId=" + processInstanceId +
                ", scheduleTime=" + scheduleTime +
                ", globalParams='" + globalParams + '\'' +
                ", executorId=" + executorId +
                ", cmdTypeIfComplement=" + cmdTypeIfComplement +
                ", tenantCode='" + tenantCode + '\'' +
                ", queue='" + queue + '\'' +
                ", processDefineId=" + processDefineId +
                ", projectId=" + projectId +
                '}';
    }
}
