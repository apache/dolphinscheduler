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

package org.apache.dolphinscheduler.server.master.runner.task.dynamic;

import org.apache.dolphinscheduler.common.constants.CommandKeyConstants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DynamicCommandUtilsTest {

    private WorkflowInstance workflowInstance;
    private Long subProcessDefinitionCode;
    private Integer subProcessDefinitionVersion;
    private Map<String, String> parameters;

    @BeforeEach
    void setUp() {
        workflowInstance = new WorkflowInstance();
        subProcessDefinitionCode = 1L;
        subProcessDefinitionVersion = 1;
        parameters = new HashMap<>();

        // Populate processInstance with some dummy data
        workflowInstance.setCommandType(CommandType.START_PROCESS);
        workflowInstance.setFailureStrategy(null); // update this
        workflowInstance.setWarningType(null); // update this
        workflowInstance.setGlobalParams("{\"prop\":\"value\"}");
        workflowInstance.setExecutorId(1);
        workflowInstance.setWarningGroupId(1);
        workflowInstance.setWorkflowInstancePriority(null); // update this
        workflowInstance.setWorkerGroup("worker");
        workflowInstance.setTenantCode("unit-root");
        workflowInstance.setDryRun(0);
    }

    @Test
    void testCreateCommand() {
        Command command = DynamicCommandUtils.createCommand(workflowInstance, subProcessDefinitionCode,
                subProcessDefinitionVersion, parameters);

        Assertions.assertEquals(CommandType.DYNAMIC_GENERATION, command.getCommandType());
        Assertions.assertEquals(subProcessDefinitionCode, command.getWorkflowDefinitionCode());
        Assertions.assertEquals(subProcessDefinitionVersion, command.getWorkflowDefinitionVersion());
        Assertions.assertEquals(TaskDependType.TASK_POST, command.getTaskDependType());
        Assertions.assertEquals(workflowInstance.getFailureStrategy(), command.getFailureStrategy());
        Assertions.assertEquals(workflowInstance.getWarningType(), command.getWarningType());
        Assertions.assertEquals(workflowInstance.getExecutorId(), command.getExecutorId());
        Assertions.assertEquals(workflowInstance.getWarningGroupId(), command.getWarningGroupId());
        Assertions.assertEquals(workflowInstance.getWorkflowInstancePriority(), command.getWorkflowInstancePriority());
        Assertions.assertEquals(workflowInstance.getWorkerGroup(), command.getWorkerGroup());
        Assertions.assertEquals(workflowInstance.getDryRun(), command.getDryRun());
        Assertions.assertEquals(workflowInstance.getTenantCode(), command.getTenantCode());
    }

    @Test
    void testGetDataFromCommandParam() {
        Command command = new Command();
        DynamicCommandUtils.addDataToCommandParam(command, "testKey", "testData");
        String data = DynamicCommandUtils.getDataFromCommandParam(command.getCommandParam(), "testKey");

        Assertions.assertEquals("testData", data);
    }

    @Test
    void testCreateCommandCommandType() {
        // Scenario 1: CommandType is START_PROCESS
        workflowInstance.setCommandType(CommandType.START_PROCESS);
        Command command1 = DynamicCommandUtils.createCommand(workflowInstance, subProcessDefinitionCode,
                subProcessDefinitionVersion, parameters);
        Assertions.assertEquals(CommandType.DYNAMIC_GENERATION, command1.getCommandType());

        // Scenario 2: CommandType is not START_PROCESS
        workflowInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        Command command2 = DynamicCommandUtils.createCommand(workflowInstance, subProcessDefinitionCode,
                subProcessDefinitionVersion, parameters);
        Assertions.assertEquals(CommandType.START_FAILURE_TASK_PROCESS, command2.getCommandType());
    }

    @Test
    void testCreateCommandStartParams() {
        // Scenario: Add some data to parameters
        parameters.put("testKey", "testValue");
        Command command = DynamicCommandUtils.createCommand(workflowInstance, subProcessDefinitionCode,
                subProcessDefinitionVersion, parameters);

        String startParamsJson = DynamicCommandUtils.getDataFromCommandParam(command.getCommandParam(),
                CommandKeyConstants.CMD_PARAM_START_PARAMS);
        Map<String, String> startParams = JSONUtils.toMap(startParamsJson);

        Assertions.assertEquals("testValue", startParams.get("testKey"));
    }

    @Test
    void testCreateCommandGlobalParams() {
        // Scenario: processInstance has globalParams
        parameters.put("testKey", "testValue");
        workflowInstance.setGlobalParams("[{\"prop\":\"globalKey\",\"value\":\"globalValue\"}]");

        Command command = DynamicCommandUtils.createCommand(workflowInstance, subProcessDefinitionCode,
                subProcessDefinitionVersion, parameters);

        String startParamsJson = DynamicCommandUtils.getDataFromCommandParam(command.getCommandParam(),
                CommandKeyConstants.CMD_PARAM_START_PARAMS);
        Map<String, String> startParams = JSONUtils.toMap(startParamsJson);

        Assertions.assertEquals("testValue", startParams.get("testKey"));
        Assertions.assertEquals("globalValue", startParams.get("globalKey"));
    }

}
