package org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity;

import org.apache.dolphinscheduler.api.test.base.AbstractBaseEntity;
import org.apache.dolphinscheduler.api.test.utils.enums.Flag;
import org.apache.dolphinscheduler.api.test.utils.enums.Priority;
import org.apache.dolphinscheduler.api.test.utils.enums.TimeoutFlag;

public class TaskDefinitionEntity extends AbstractBaseEntity {
    private String code;

    private String delayTime;

    private String description;

    private int environmentCode;

    private String failRetryInterval;

    private String failRetryTimes;

    private Flag flag;

    private String name;

    private TaskParamsEntity taskParams;

    private Priority taskPriority;

    private String taskType;

    private int timeout;

    private TimeoutFlag timeoutFlag;

    private String timeoutNotifyStrategy;

    private String workerGroup;

    private int cpuQuota;

    private int memoryMax;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(String delayTime) {
        this.delayTime = delayTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getEnvironmentCode() {
        return environmentCode;
    }

    public void setEnvironmentCode(int environmentCode) {
        this.environmentCode = environmentCode;
    }

    public String getFailRetryInterval() {
        return failRetryInterval;
    }

    public void setFailRetryInterval(String failRetryInterval) {
        this.failRetryInterval = failRetryInterval;
    }

    public String getFailRetryTimes() {
        return failRetryTimes;
    }

    public void setFailRetryTimes(String failRetryTimes) {
        this.failRetryTimes = failRetryTimes;
    }

    public Flag getFlag() {
        return flag;
    }

    public void setFlag(Flag flag) {
        this.flag = flag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskParamsEntity getTaskParams() {
        return taskParams;
    }

    public void setTaskParams(TaskParamsEntity taskParams) {
        this.taskParams = taskParams;
    }

    public Priority getTaskPriority() {
        return taskPriority;
    }

    public void setTaskPriority(Priority taskPriority) {
        this.taskPriority = taskPriority;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getTimeoutNotifyStrategy() {
        return timeoutNotifyStrategy;
    }

    public void setTimeoutNotifyStrategy(String timeoutNotifyStrategy) {
        this.timeoutNotifyStrategy = timeoutNotifyStrategy;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public TimeoutFlag getTimeoutFlag() {
        return timeoutFlag;
    }

    public void setTimeoutFlag(TimeoutFlag timeoutFlag) {
        this.timeoutFlag = timeoutFlag;
    }

    public String getWorkerGroup() {
        return workerGroup;
    }

    public void setWorkerGroup(String workerGroup) {
        this.workerGroup = workerGroup;
    }

    public int getCpuQuota() {
        return cpuQuota;
    }

    public void setCpuQuota(int cpuQuota) {
        this.cpuQuota = cpuQuota;
    }

    public int getMemoryMax() {
        return memoryMax;
    }

    public void setMemoryMax(int memoryMax) {
        this.memoryMax = memoryMax;
    }
}
