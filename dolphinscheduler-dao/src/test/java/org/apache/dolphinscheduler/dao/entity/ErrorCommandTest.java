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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;

import java.util.Date;

import org.junit.jupiter.api.Test;

class ErrorCommandTest {

    @Test
    void testConstructor() {
        Command command = new Command();
        command.setId(1);
        command.setCommandType(CommandType.PAUSE);
        command.setExecutorId(1);
        command.setProcessDefinitionCode(123);
        command.setProcessDefinitionVersion(1);
        command.setProcessInstanceId(1);
        command.setCommandParam("param");
        command.setTaskDependType(TaskDependType.TASK_POST);
        command.setFailureStrategy(FailureStrategy.CONTINUE);
        command.setWarningType(WarningType.ALL);
        command.setWarningGroupId(1);
        command.setScheduleTime(new Date());
        command.setStartTime(new Date());
        command.setUpdateTime(new Date());
        command.setProcessInstancePriority(Priority.HIGHEST);
        command.setWorkerGroup("default");
        command.setTenantCode("root");
        command.setEnvironmentCode(1L);
        command.setDryRun(1);
        command.setTestFlag(Flag.NO.getCode());

        ErrorCommand errorCommand = new ErrorCommand(command, "test");
        assertEquals(command.getCommandType(), errorCommand.getCommandType());
        assertEquals(command.getExecutorId(), errorCommand.getExecutorId());
        assertEquals(command.getProcessDefinitionCode(), errorCommand.getProcessDefinitionCode());
        assertEquals(command.getProcessDefinitionVersion(), errorCommand.getProcessDefinitionVersion());
        assertEquals(command.getProcessInstanceId(), errorCommand.getProcessInstanceId());
        assertEquals(command.getCommandParam(), errorCommand.getCommandParam());
        assertEquals(command.getTaskDependType(), errorCommand.getTaskDependType());
        assertEquals(command.getFailureStrategy(), errorCommand.getFailureStrategy());
        assertEquals(command.getWarningType(), errorCommand.getWarningType());
        assertEquals(command.getWarningGroupId(), errorCommand.getWarningGroupId());
        assertEquals(command.getScheduleTime(), errorCommand.getScheduleTime());
        assertEquals(command.getStartTime(), errorCommand.getStartTime());
        assertEquals(command.getUpdateTime(), errorCommand.getUpdateTime());
        assertEquals(command.getProcessInstancePriority(), errorCommand.getProcessInstancePriority());
        assertEquals(command.getWorkerGroup(), errorCommand.getWorkerGroup());
        assertEquals(command.getTenantCode(), errorCommand.getTenantCode());
        assertEquals(command.getEnvironmentCode(), errorCommand.getEnvironmentCode());
        assertEquals(command.getDryRun(), errorCommand.getDryRun());
        assertEquals(command.getTestFlag(), errorCommand.getTestFlag());
        assertEquals("test", errorCommand.getMessage());
    }

}
