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
package org.apache.dolphinscheduler.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("t_ds_task_instance")
public class TaskInstance {
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    @TableField(value = "name")
    private String name;
    @TableField(value = "task_type")
    private String taskType;
    @TableField(value = "process_definition_id")
    private int processDefinitionId;
    @TableField(value = "process_instance_id")
    private int processInstanceId;
    @TableField(exist = false)
    private String processInstanceName;
    @TableField(value = "task_json")
    private String taskJson;
    @TableField(value = "state")
    private ExecutionStatus state;
    @TableField(value = "submit_time")
    private Date submitTime;
    @TableField(value = "start_time")
    private Date startTime;
    @TableField(value = "end_time")
    private Date endTime;
    @TableField(value = "host")
    private String host;
    @TableField(value = "execute_path")
    private String executePath;
    @TableField(value = "log_path")
    private String logPath;
    @TableField(value = "retry_times")
    private int retryTimes;
    @TableField(value = "alert_flag")
    private Flag alertFlag;
    @TableField(exist = false)
    private ProcessInstance processInstance;
    @TableField(exist = false)
    private ProcessDefinition processDefine;
    @TableField(value = "pid")
    private int pid;
    @TableField(value = "app_link")
    private String appLink;
    @TableField("flag")
    private Flag flag;
    @TableField(exist = false)
    private String dependency;
    @TableField(exist = false)
    private Long duration;
    @TableField(value = "max_retry_times")
    private int maxRetryTimes;
    @TableField(value = "retry_interval")
    private int retryInterval;
    @TableField(value = "task_instance_priority")
    private Priority taskInstancePriority;
    @TableField(exist = false)
    private Priority processInstancePriority;
    @TableField(exist = false)
    private String dependentResult;
    @TableField(value = "worker_group_id")
    private int workerGroupId;

    public void init(String host, Date startTime, String executePath) {
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

    public Boolean isTaskSuccess() {
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


    public Boolean isSubProcess() {
        return TaskType.SUB_PROCESS.toString().equals(this.taskType.toUpperCase());
    }

    public String getDependency() {

        if (this.dependency != null) {
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
     * determine if you can try again
     *
     * @return can try result
     */
    public boolean taskCanRetry() {
        if (this.isSubProcess()) {
            return false;
        }
        if (this.getState() == ExecutionStatus.NEED_FAULT_TOLERANCE) {
            return true;
        } else {
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

    public String getDependentResult() {
        return dependentResult;
    }

    public void setDependentResult(String dependentResult) {
        this.dependentResult = dependentResult;
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
                ", flag=" + flag +
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
}
