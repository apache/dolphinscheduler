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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_ds_command")
public class Command {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("command_type")
    private CommandType commandType;

    @TableField("workflow_definition_code")
    private long workflowDefinitionCode;

    @TableField("workflow_definition_version")
    private int workflowDefinitionVersion;

    @TableField("workflow_instance_id")
    private int workflowInstanceId;

    /**
     * command parameter, format json
     */
    @TableField("command_param")
    private String commandParam;

    @TableField("workflow_instance_priority")
    private Priority workflowInstancePriority;

    @Deprecated
    @TableField("executor_id")
    private int executorId;

    @Deprecated
    @TableField("task_depend_type")
    @Builder.Default
    private TaskDependType taskDependType = TaskDependType.TASK_POST;

    @Deprecated
    @TableField("failure_strategy")
    @Builder.Default
    private FailureStrategy failureStrategy = FailureStrategy.CONTINUE;

    @Deprecated
    @TableField("warning_type")
    private WarningType warningType;

    @Deprecated
    @TableField("warning_group_id")
    private Integer warningGroupId;

    @Deprecated
    @TableField("schedule_time")
    private Date scheduleTime;

    @Deprecated
    @TableField("start_time")
    private Date startTime = new Date();

    @Deprecated
    @TableField("update_time")
    @Builder.Default
    private Date updateTime = new Date();

    @Deprecated
    @TableField("worker_group")
    private String workerGroup;

    /**
     * tenant code
     */
    @Deprecated
    private String tenantCode;

    @Deprecated
    @TableField("environment_code")
    private Long environmentCode;

    @Deprecated
    @TableField("dry_run")
    private int dryRun;

    /**
     * test flag
     */
    @Deprecated
    @TableField("test_flag")
    private int testFlag;

    public Command(
                   CommandType commandType,
                   TaskDependType taskDependType,
                   FailureStrategy failureStrategy,
                   int executorId,
                   long workflowDefinitionCode,
                   String commandParam,
                   WarningType warningType,
                   int warningGroupId,
                   Date scheduleTime,
                   String workerGroup,
                   Long environmentCode,
                   Priority workflowInstancePriority,
                   int dryRun,
                   int workflowInstanceId,
                   int workflowDefinitionVersion,
                   int testFlag) {
        this.commandType = commandType;
        this.executorId = executorId;
        this.workflowDefinitionCode = workflowDefinitionCode;
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
        this.workflowInstancePriority = workflowInstancePriority;
        this.dryRun = dryRun;
        this.workflowInstanceId = workflowInstanceId;
        this.workflowDefinitionVersion = workflowDefinitionVersion;
        this.testFlag = testFlag;
    }
}
