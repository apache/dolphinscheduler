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
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;

import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ds_error_command")
public class ErrorCommand {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

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
    private Date scheduleTime;

    /**
     * start time
     */
    private Date startTime;

    /**
     * process instance priority
     */
    private Priority processInstancePriority;

    /**
     * update time
     */
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

    /**
     * dry run flag
     */
    private int dryRun;

    /**
     * test flag
     */
    @TableField("test_flag")
    private int testFlag;

    public ErrorCommand() {
    }
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
        this.dryRun = command.getDryRun();
        this.testFlag = command.getTestFlag();
    }
}
