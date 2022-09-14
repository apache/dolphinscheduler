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

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ds_command")
public class Command {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * command type
     */
    @TableField("command_type")
    private CommandType commandType;

    /**
     * process definition code
     */
    @TableField("process_definition_code")
    private long processDefinitionCode;

    /**
     * executor id
     */
    @TableField("executor_id")
    private int executorId;

    /**
     * command parameter, format json
     */
    @TableField("command_param")
    private String commandParam;

    /**
     * task depend type
     */
    @TableField("task_depend_type")
    private TaskDependType taskDependType;

    /**
     * failure strategy
     */
    @TableField("failure_strategy")
    private FailureStrategy failureStrategy;

    /**
     * warning type
     */
    @TableField("warning_type")
    private WarningType warningType;

    /**
     * warning group id
     */
    @TableField("warning_group_id")
    private Integer warningGroupId;

    /**
     * schedule time
     */
    @TableField("schedule_time")
    private Date scheduleTime;

    /**
     * start time
     */
    @TableField("start_time")
    private Date startTime;

    /**
     * process instance priority
     */
    @TableField("process_instance_priority")
    private Priority processInstancePriority;

    /**
     * update time
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * worker group
     */
    @TableField("worker_group")
    private String workerGroup;

    /**
     * environment code
     */
    @TableField("environment_code")
    private Long environmentCode;

    /**
     * dry run flag
     */
    @TableField("dry_run")
    private int dryRun;

    @TableField("process_instance_id")
    private int processInstanceId;

    @TableField("process_definition_version")
    private int processDefinitionVersion;

    public Command() {
        this.taskDependType = TaskDependType.TASK_POST;
        this.failureStrategy = FailureStrategy.CONTINUE;
        this.startTime = new Date();
        this.updateTime = new Date();
    }

    public Command(
                   CommandType commandType,
                   TaskDependType taskDependType,
                   FailureStrategy failureStrategy,
                   int executorId,
                   long processDefinitionCode,
                   String commandParam,
                   WarningType warningType,
                   int warningGroupId,
                   Date scheduleTime,
                   String workerGroup,
                   Long environmentCode,
                   Priority processInstancePriority,
                   int dryRun,
                   int processInstanceId,
                   int processDefinitionVersion) {
        this.commandType = commandType;
        this.executorId = executorId;
        this.processDefinitionCode = processDefinitionCode;
        this.commandParam = commandParam;
        this.warningType = warningType;
        this.warningGroupId = warningGroupId;
        this.scheduleTime = scheduleTime;
        this.taskDependType = taskDependType;
        this.failureStrategy = failureStrategy;
        this.startTime = new Date();
        this.updateTime = new Date();
        this.workerGroup = workerGroup;
        this.environmentCode = environmentCode;
        this.processInstancePriority = processInstancePriority;
        this.dryRun = dryRun;
        this.processInstanceId = processInstanceId;
        this.processDefinitionVersion = processDefinitionVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Command command = (Command) o;

        if (id != command.id) {
            return false;
        }
        if (processDefinitionCode != command.processDefinitionCode) {
            return false;
        }
        if (executorId != command.executorId) {
            return false;
        }
        if (workerGroup != null ? workerGroup.equals(command.workerGroup) : command.workerGroup == null) {
            return false;
        }

        if (environmentCode != null ? environmentCode.equals(command.environmentCode)
                : command.environmentCode == null) {
            return false;
        }

        if (commandType != command.commandType) {
            return false;
        }
        if (commandParam != null ? !commandParam.equals(command.commandParam) : command.commandParam != null) {
            return false;
        }
        if (taskDependType != command.taskDependType) {
            return false;
        }
        if (failureStrategy != command.failureStrategy) {
            return false;
        }
        if (warningType != command.warningType) {
            return false;
        }
        if (warningGroupId != null ? !warningGroupId.equals(command.warningGroupId) : command.warningGroupId != null) {
            return false;
        }
        if (scheduleTime != null ? !scheduleTime.equals(command.scheduleTime) : command.scheduleTime != null) {
            return false;
        }
        if (startTime != null ? !startTime.equals(command.startTime) : command.startTime != null) {
            return false;
        }
        if (processInstancePriority != command.processInstancePriority) {
            return false;
        }
        if (processInstanceId != command.processInstanceId) {
            return false;
        }
        if (processDefinitionVersion != command.getProcessDefinitionVersion()) {
            return false;
        }
        return !(updateTime != null ? !updateTime.equals(command.updateTime) : command.updateTime != null);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (commandType != null ? commandType.hashCode() : 0);
        result = 31 * result + Long.hashCode(processDefinitionCode);
        result = 31 * result + executorId;
        result = 31 * result + (commandParam != null ? commandParam.hashCode() : 0);
        result = 31 * result + (taskDependType != null ? taskDependType.hashCode() : 0);
        result = 31 * result + (failureStrategy != null ? failureStrategy.hashCode() : 0);
        result = 31 * result + (warningType != null ? warningType.hashCode() : 0);
        result = 31 * result + (warningGroupId != null ? warningGroupId.hashCode() : 0);
        result = 31 * result + (scheduleTime != null ? scheduleTime.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (processInstancePriority != null ? processInstancePriority.hashCode() : 0);
        result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
        result = 31 * result + (workerGroup != null ? workerGroup.hashCode() : 0);
        result = 31 * result + (environmentCode != null ? environmentCode.hashCode() : 0);
        result = 31 * result + dryRun;
        result = 31 * result + processInstanceId;
        result = 31 * result + processDefinitionVersion;
        return result;
    }
}
