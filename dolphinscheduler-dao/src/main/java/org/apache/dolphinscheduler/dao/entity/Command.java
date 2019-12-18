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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import org.apache.dolphinscheduler.common.enums.*;

import java.util.Date;

/**
 * command
 */
@Data
@ToString
@TableName("t_ds_command")
public class Command {
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    @TableField("command_type")
    private CommandType commandType;
    @TableField("process_definition_id")
    private int processDefinitionId;
    @TableField("executor_id")
    private int executorId;
    @TableField("command_param")
    private String commandParam;
    @TableField("task_depend_type")
    private TaskDependType taskDependType;
    @TableField("failure_strategy")
    private FailureStrategy failureStrategy;
    @TableField("warning_type")
    private WarningType warningType;
    @TableField("warning_group_id")
    private Integer warningGroupId;
    @TableField("schedule_time")
    private Date scheduleTime;
    @TableField("start_time")
    private Date startTime;
    @TableField("process_instance_priority")
    private Priority processInstancePriority;
    @TableField("update_time")
    private Date updateTime;
    @TableField("worker_group_id")
    private int workerGroupId;


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
            int processDefinitionId,
            String commandParam,
            WarningType warningType,
            int warningGroupId,
            Date scheduleTime,
            Priority processInstancePriority) {
        this.commandType = commandType;
        this.executorId = executorId;
        this.processDefinitionId = processDefinitionId;
        this.commandParam = commandParam;
        this.warningType = warningType;
        this.warningGroupId = warningGroupId;
        this.scheduleTime = scheduleTime;
        this.taskDependType = taskDependType;
        this.failureStrategy = failureStrategy;
        this.startTime = new Date();
        this.updateTime = new Date();
        this.processInstancePriority = processInstancePriority;
    }
}

