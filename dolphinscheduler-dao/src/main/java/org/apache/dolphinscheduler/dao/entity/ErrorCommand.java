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

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * command
 */
@TableName("t_ds_error_command")
public class ErrorCommand {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.INPUT)
    private int id;

    /**
     * command type
     */
    private CommandType commandType;

    /**
     * process definition code
     */
    private long processDefinitionCode;

    /**
     * executor id
     */
    private int executorId;

    /**
     * command parameter, format json
     */
    private String commandParam;

    /**
     * task depend type
     */
    private TaskDependType taskDependType;

    /**
     * failure strategy
     */
    private FailureStrategy failureStrategy;

    /**
     *  warning type
     */
    private WarningType warningType;

    /**
     * warning group id
     */
    private Integer warningGroupId;

    /**
     * schedule time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date scheduleTime;

    /**
     * start time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date startTime;

    /**
     * process instance priority
     */
    private Priority processInstancePriority;

    /**
     * update time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;

    /**
     * 执行信息
     */
    private String message;

    /**
     * worker group
     */
    private String workerGroup;

    /**
     * environment code
     */
    private Long environmentCode;

    public ErrorCommand(){}

    public ErrorCommand(Command command, String message) {
        this.id = command.getId();
        this.commandType = command.getCommandType();
        this.executorId = command.getExecutorId();
        this.processDefinitionCode = command.getProcessDefinitionCode();
        this.commandParam = command.getCommandParam();
        this.warningType = command.getWarningType();
        this.warningGroupId = command.getWarningGroupId();
        this.scheduleTime = command.getScheduleTime();
        this.taskDependType = command.getTaskDependType();
        this.failureStrategy = command.getFailureStrategy();
        this.startTime = command.getStartTime();
        this.updateTime = command.getUpdateTime();
        this.environmentCode = command.getEnvironmentCode();
        this.processInstancePriority = command.getProcessInstancePriority();
        this.message = message;
    }

    public TaskDependType getTaskDependType() {
        return taskDependType;
    }

    public void setTaskDependType(TaskDependType taskDependType) {
        this.taskDependType = taskDependType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public long getProcessDefinitionCode() {
        return processDefinitionCode;
    }

    public void setProcessDefinitionCode(long processDefinitionCode) {
        this.processDefinitionCode = processDefinitionCode;
    }

    public FailureStrategy getFailureStrategy() {
        return failureStrategy;
    }

    public void setFailureStrategy(FailureStrategy failureStrategy) {
        this.failureStrategy = failureStrategy;
    }

    public void setCommandParam(String commandParam) {
        this.commandParam = commandParam;
    }

    public String getCommandParam() {
        return commandParam;
    }

    public WarningType getWarningType() {
        return warningType;
    }

    public void setWarningType(WarningType warningType) {
        this.warningType = warningType;
    }

    public Integer getWarningGroupId() {
        return warningGroupId;
    }

    public void setWarningGroupId(Integer warningGroupId) {
        this.warningGroupId = warningGroupId;
    }

    public Date getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(Date scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public int getExecutorId() {
        return executorId;
    }

    public void setExecutorId(int executorId) {
        this.executorId = executorId;
    }

    public Priority getProcessInstancePriority() {
        return processInstancePriority;
    }

    public void setProcessInstancePriority(Priority processInstancePriority) {
        this.processInstancePriority = processInstancePriority;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getWorkerGroup() {
        return workerGroup;
    }

    public void setWorkerGroup(String workerGroup) {
        this.workerGroup = workerGroup;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getEnvironmentCode() {
        return this.environmentCode;
    }

    public void setEnvironmentCode(Long environmentCode) {
        this.environmentCode = environmentCode;
    }

    @Override
    public String toString() {
        return "ErrorCommand{"
                + "id=" + id
                + ", commandType=" + commandType
                + ", processDefinitionCode=" + processDefinitionCode
                + ", executorId=" + executorId
                + ", commandParam='" + commandParam + '\''
                + ", taskDependType=" + taskDependType
                + ", failureStrategy=" + failureStrategy
                + ", warningType=" + warningType
                + ", warningGroupId=" + warningGroupId
                + ", scheduleTime=" + scheduleTime
                + ", startTime=" + startTime
                + ", processInstancePriority=" + processInstancePriority
                + ", updateTime=" + updateTime
                + ", message='" + message + '\''
                + ", workerGroup='" + workerGroup + '\''
                + ", environmentCode='" + environmentCode + '\''
                + '}';
    }
}
