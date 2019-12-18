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
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.dolphinscheduler.common.enums.*;

import java.util.Date;

/**
 * command
 */
@Data
@NoArgsConstructor
@ToString
@TableName("t_ds_error_command")
public class ErrorCommand {
    @TableId(value = "id", type = IdType.INPUT)
    private int id;
    private CommandType commandType;
    private int processDefinitionId;
    private int executorId;
    private String commandParam;
    private TaskDependType taskDependType;
    private FailureStrategy failureStrategy;
    private WarningType warningType;
    private Integer warningGroupId;
    private Date scheduleTime;
    private Date startTime;
    private Priority processInstancePriority;
    private Date updateTime;
    private String message;
    private int workerGroupId;

    public ErrorCommand(Command command, String message) {
        this.id = command.getId();
        this.commandType = command.getCommandType();
        this.executorId = command.getExecutorId();
        this.processDefinitionId = command.getProcessDefinitionId();
        this.commandParam = command.getCommandParam();
        this.warningType = command.getWarningType();
        this.warningGroupId = command.getWarningGroupId();
        this.scheduleTime = command.getScheduleTime();
        this.taskDependType = command.getTaskDependType();
        this.failureStrategy = command.getFailureStrategy();
        this.startTime = command.getStartTime();
        this.updateTime = command.getUpdateTime();
        this.processInstancePriority = command.getProcessInstancePriority();
        this.message = message;
    }
}
