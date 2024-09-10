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
@TableName("t_ds_error_command")
public class ErrorCommand {

    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    private CommandType commandType;

    private long workflowDefinitionCode;

    private int workflowDefinitionVersion;

    private int workflowInstanceId;

    private int executorId;

    private String commandParam;

    private TaskDependType taskDependType;

    private FailureStrategy failureStrategy;

    private WarningType warningType;

    private Integer warningGroupId;

    private Date scheduleTime;

    private Date startTime;

    private Priority workflowInstancePriority;

    private Date updateTime;

    private String message;

    private String workerGroup;

    private String tenantCode;

    private Long environmentCode;

    private int dryRun;

    @TableField("test_flag")
    private int testFlag;

    public ErrorCommand() {
    }

    public ErrorCommand(Command command, String message) {
        this.id = command.getId();
        this.commandType = command.getCommandType();
        this.executorId = command.getExecutorId();
        this.workflowDefinitionCode = command.getWorkflowDefinitionCode();
        this.workflowDefinitionVersion = command.getWorkflowDefinitionVersion();
        this.workflowInstanceId = command.getWorkflowInstanceId();
        this.commandParam = command.getCommandParam();
        this.taskDependType = command.getTaskDependType();
        this.failureStrategy = command.getFailureStrategy();
        this.warningType = command.getWarningType();
        this.warningGroupId = command.getWarningGroupId();
        this.scheduleTime = command.getScheduleTime();
        this.startTime = command.getStartTime();
        this.updateTime = command.getUpdateTime();
        this.workflowInstancePriority = command.getWorkflowInstancePriority();
        this.workerGroup = command.getWorkerGroup();
        this.tenantCode = command.getTenantCode();
        this.environmentCode = command.getEnvironmentCode();
        this.message = message;
        this.dryRun = command.getDryRun();
        this.testFlag = command.getTestFlag();
    }
}
