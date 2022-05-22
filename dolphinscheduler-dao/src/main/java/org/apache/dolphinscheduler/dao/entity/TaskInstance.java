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

import static org.apache.dolphinscheduler.common.Constants.SEC_2_MINUTES_TIME_UNIT;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_BLOCKING;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_CONDITIONS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_DEPENDENT;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SUB_PROCESS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SWITCH;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * task instance
 */
@TableName("t_ds_task_instance")
public class TaskInstance implements Serializable {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
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
     * process instance id
     */
    private int processInstanceId;

    /**
     * task code
     */
    private long taskCode;

    /**
     * task definition version
     */
    private int taskDefinitionVersion;

    /**
     * process instance name
     */
    @TableField(exist = false)
    private String processInstanceName;

    /**
     * process instance name
     */
    @TableField(exist = false)
    private int taskGroupPriority;

    /**
     * state
     */
    private ExecutionStatus state;

    /**
     * task first submit time.
     */
    private Date firstSubmitTime;

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
     * process instance
     */
    @TableField(exist = false)
    private ProcessInstance processInstance;

    /**
     * process definition
     */
    @TableField(exist = false)
    private ProcessDefinition processDefine;

    /**
     * task definition
     */
    @TableField(exist = false)
    private TaskDefinition taskDefine;

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
    @TableField(exist = false)
    private DependentParameters dependency;

    /**
     * switch dependency
     */
    @TableField(exist = false)
    private SwitchParameters switchDependency;

    /**
     * duration
     */
    @TableField(exist = false)
    private String duration;

    /**
     * max retry times
     */
    private int maxRetryTimes;

    /**
     * task retry interval, unit: minute
     */
    private int retryInterval;

    /**
     * task intance priority
     */
    private Priority taskInstancePriority;

    /**
     * process intance priority
     */
    @TableField(exist = false)
    private Priority processInstancePriority;

    /**
     * dependent state
     */
    @TableField(exist = false)
    private String dependentResult;


    /**
     * workerGroup
     */
    private String workerGroup;

    /**
     * environment code
     */
    private Long environmentCode;

    /**
     * environment config
     */
    private String environmentConfig;

    /**
     * executor id
     */
    private int executorId;

    /**
     * varPool string
     */
    private String varPool;

    /**
     * executor name
     */
    @TableField(exist = false)
    private String executorName;


    @TableField(exist = false)
    private Map<String, String> resources;

    /**
     * delay execution time.
     */
    private int delayTime;


    /**
     * task params
     */
    private String taskParams;

    /**
     * dry run flag
     */
    private int dryRun;
    /**
     * task group id
     */
    private int taskGroupId;

    public void init(String host, Date startTime, String executePath) {
        this.host = host;
        this.startTime = startTime;
        this.executePath = executePath;
    }

    public String getVarPool() {
        return varPool;
    }

    public void setVarPool(String varPool) {
        this.varPool = varPool;
    }

    public int getTaskGroupId() {
        return taskGroupId;
    }

    public void setTaskGroupId(int taskGroupId) {
        this.taskGroupId = taskGroupId;
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

    public TaskDefinition getTaskDefine() {
        return taskDefine;
    }

    public void setTaskDefine(TaskDefinition taskDefine) {
        this.taskDefine = taskDefine;
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

    public int getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(int processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public ExecutionStatus getState() {
        return state;
    }

    public void setState(ExecutionStatus state) {
        this.state = state;
    }

    public Date getFirstSubmitTime() {
        return firstSubmitTime;
    }

    public void setFirstSubmitTime(Date firstSubmitTime) {
        this.firstSubmitTime = firstSubmitTime;
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

    public Long getEnvironmentCode() {
        return this.environmentCode;
    }

    public void setEnvironmentCode(Long environmentCode) {
        this.environmentCode = environmentCode;
    }

    public String getEnvironmentConfig() {
        return this.environmentConfig;
    }

    public void setEnvironmentConfig(String environmentConfig) {
        this.environmentConfig = environmentConfig;
    }

    public DependentParameters getDependency() {
        if (this.dependency == null) {
            Map<String, Object> taskParamsMap = JSONUtils.parseObject(this.getTaskParams(), new TypeReference<Map<String, Object>>() {
            });
            this.dependency = JSONUtils.parseObject((String) taskParamsMap.get(Constants.DEPENDENCE), DependentParameters.class);
        }
        return this.dependency;
    }

    public void setDependency(DependentParameters dependency) {
        this.dependency = dependency;
    }

    public SwitchParameters getSwitchDependency() {
        if (this.switchDependency == null) {
            Map<String, Object> taskParamsMap = JSONUtils.parseObject(this.getTaskParams(), new TypeReference<Map<String, Object>>() {
            });
            this.switchDependency = JSONUtils.parseObject((String) taskParamsMap.get(Constants.SWITCH_RESULT), SwitchParameters.class);
        }
        return this.switchDependency;
    }

    public void setSwitchDependency(SwitchParameters switchDependency) {
        Map<String, Object> taskParamsMap = JSONUtils.parseObject(this.getTaskParams(), new TypeReference<Map<String, Object>>() {
        });
        taskParamsMap.put(Constants.SWITCH_RESULT, JSONUtils.toJsonString(switchDependency));
        this.setTaskParams(JSONUtils.toJsonString(taskParamsMap));
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
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

    public int getExecutorId() {
        return executorId;
    }

    public void setExecutorId(int executorId) {
        this.executorId = executorId;
    }

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    public int getDryRun() {
        return dryRun;
    }

    public void setDryRun(int dryRun) {
        this.dryRun = dryRun;
    }

    public boolean isTaskComplete() {

        return this.getState().typeIsPause()
                || this.getState().typeIsSuccess()
                || this.getState().typeIsCancel()
                || (this.getState().typeIsFailure() && !taskCanRetry());
    }

    public Map<String, String> getResources() {
        return resources;
    }

    public void setResources(Map<String, String> resources) {
        this.resources = resources;
    }

    public boolean isSubProcess() {
        return TASK_TYPE_SUB_PROCESS.equalsIgnoreCase(this.taskType);
    }

    public boolean isDependTask() {
        return TASK_TYPE_DEPENDENT.equalsIgnoreCase(this.taskType);
    }

    public boolean isConditionsTask() {
        return TASK_TYPE_CONDITIONS.equalsIgnoreCase(this.taskType);
    }

    public boolean isSwitchTask() {
        return TASK_TYPE_SWITCH.equalsIgnoreCase(this.taskType);
    }

    public boolean isBlockingTask() {
        return TASK_TYPE_BLOCKING.equalsIgnoreCase(this.taskType);
    }

    /**
     * determine if a task instance can retry
     * if subProcess,
     *
     * @return can try result
     */
    public boolean taskCanRetry() {
        if (this.isSubProcess()) {
            return false;
        }
        if (this.getState() == ExecutionStatus.NEED_FAULT_TOLERANCE) {
            return true;
        }
        return this.getState() == ExecutionStatus.FAILURE && (this.getRetryTimes() < this.getMaxRetryTimes());
    }

    /**
     * whether the retry interval is timed out
     *
     * @return Boolean
     */
    public boolean retryTaskIntervalOverTime() {
        if (getState() != ExecutionStatus.FAILURE) {
            return true;
        }
        if (getMaxRetryTimes() == 0 || getRetryInterval() == 0) {
            return true;
        }
        Date now = new Date();
        long failedTimeInterval = DateUtils.differSec(now, getEndTime());
        // task retry does not over time, return false
        return getRetryInterval() * SEC_2_MINUTES_TIME_UNIT < failedTimeInterval;
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

    public String getWorkerGroup() {
        return workerGroup;
    }

    public void setWorkerGroup(String workerGroup) {
        this.workerGroup = workerGroup;
    }

    public String getDependentResult() {
        return dependentResult;
    }

    public void setDependentResult(String dependentResult) {
        this.dependentResult = dependentResult;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    @Override
    public String toString() {
        return "TaskInstance{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", taskType='" + taskType + '\''
                + ", processInstanceId=" + processInstanceId
                + ", processInstanceName='" + processInstanceName + '\''
                + ", state=" + state
                + ", firstSubmitTime=" + firstSubmitTime
                + ", submitTime=" + submitTime
                + ", startTime=" + startTime
                + ", endTime=" + endTime
                + ", host='" + host + '\''
                + ", executePath='" + executePath + '\''
                + ", logPath='" + logPath + '\''
                + ", retryTimes=" + retryTimes
                + ", alertFlag=" + alertFlag
                + ", processInstance=" + processInstance
                + ", processDefine=" + processDefine
                + ", pid=" + pid
                + ", appLink='" + appLink + '\''
                + ", flag=" + flag
                + ", dependency='" + dependency + '\''
                + ", duration=" + duration
                + ", maxRetryTimes=" + maxRetryTimes
                + ", retryInterval=" + retryInterval
                + ", taskInstancePriority=" + taskInstancePriority
                + ", processInstancePriority=" + processInstancePriority
                + ", dependentResult='" + dependentResult + '\''
                + ", workerGroup='" + workerGroup + '\''
                + ", environmentCode=" + environmentCode
                + ", environmentConfig='" + environmentConfig + '\''
                + ", executorId=" + executorId
                + ", executorName='" + executorName + '\''
                + ", delayTime=" + delayTime
                + ", dryRun=" + dryRun
                + ", taskGroupId=" + taskGroupId
                + ", taskGroupPriority=" + taskGroupPriority
                + '}';
    }

    public long getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(long taskCode) {
        this.taskCode = taskCode;
    }

    public int getTaskDefinitionVersion() {
        return taskDefinitionVersion;
    }

    public void setTaskDefinitionVersion(int taskDefinitionVersion) {
        this.taskDefinitionVersion = taskDefinitionVersion;
    }

    public String getTaskParams() {
        return taskParams;
    }

    public void setTaskParams(String taskParams) {
        this.taskParams = taskParams;
    }

    public boolean isFirstRun() {
        return endTime == null;
    }

    public int getTaskGroupPriority() {
        return taskGroupPriority;
    }

    public void setTaskGroupPriority(int taskGroupPriority) {
        this.taskGroupPriority = taskGroupPriority;
    }
}
