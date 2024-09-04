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

package org.apache.dolphinscheduler.service.command;

import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_START_DATE;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_RECOVER_WORKFLOW_ID_STRING;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstanceRelation;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.fasterxml.jackson.databind.JsonNode;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MessageServiceImplTest {

    @InjectMocks
    private CommandServiceImpl commandService;

    @Mock
    private CommandMapper commandMapper;

    @Mock
    private WorkflowDefinitionMapper processDefineMapper;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Test
    public void testCreateSubCommand() {
        WorkflowInstance parentInstance = new WorkflowInstance();
        parentInstance.setWarningType(WarningType.SUCCESS);
        parentInstance.setWarningGroupId(0);

        TaskInstance task = new TaskInstance();
        task.setTaskParams("{\"processDefinitionCode\":10}}");
        task.setId(10);
        task.setTaskCode(1L);
        task.setTaskDefinitionVersion(1);

        WorkflowInstance childInstance = null;
        WorkflowInstanceRelation instanceMap = new WorkflowInstanceRelation();
        instanceMap.setParentWorkflowInstanceId(1);
        instanceMap.setParentTaskInstanceId(10);
        Command command;

        // father history: start; child null == command type: start
        parentInstance.setHistoryCmd("START_PROCESS");
        parentInstance.setCommandType(CommandType.START_PROCESS);
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(10L);
        Mockito.when(processDefineMapper.queryByDefineId(100)).thenReturn(workflowDefinition);
        Mockito.when(processDefineMapper.queryByCode(10L)).thenReturn(workflowDefinition);
        command = commandService.createSubProcessCommand(parentInstance, childInstance, instanceMap, task);
        Assertions.assertEquals(CommandType.START_PROCESS, command.getCommandType());

        // father history: start,start failure; child null == command type: start
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("START_PROCESS,START_FAILURE_TASK_PROCESS");
        command = commandService.createSubProcessCommand(parentInstance, childInstance, instanceMap, task);
        Assertions.assertEquals(CommandType.START_PROCESS, command.getCommandType());

        // father history: scheduler,start failure; child null == command type: scheduler
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("SCHEDULER,START_FAILURE_TASK_PROCESS");
        command = commandService.createSubProcessCommand(parentInstance, childInstance, instanceMap, task);
        Assertions.assertEquals(CommandType.SCHEDULER, command.getCommandType());

        // father history: complement,start failure; child null == command type: complement

        String startString = "2020-01-01 00:00:00";
        String endString = "2020-01-10 00:00:00";
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("COMPLEMENT_DATA,START_FAILURE_TASK_PROCESS");
        Map<String, String> complementMap = new HashMap<>();
        complementMap.put(CMD_PARAM_COMPLEMENT_DATA_START_DATE, startString);
        complementMap.put(CMD_PARAM_COMPLEMENT_DATA_END_DATE, endString);
        parentInstance.setCommandParam(JSONUtils.toJsonString(complementMap));
        command = commandService.createSubProcessCommand(parentInstance, childInstance, instanceMap, task);
        Assertions.assertEquals(CommandType.COMPLEMENT_DATA, command.getCommandType());

        JsonNode complementDate = JSONUtils.parseObject(command.getCommandParam());
        Date start = DateUtils.stringToDate(complementDate.get(CMD_PARAM_COMPLEMENT_DATA_START_DATE).asText());
        Date end = DateUtils.stringToDate(complementDate.get(CMD_PARAM_COMPLEMENT_DATA_END_DATE).asText());
        Assertions.assertEquals(startString, DateUtils.dateToString(start));
        Assertions.assertEquals(endString, DateUtils.dateToString(end));

        // father history: start,failure,start failure; child not null == command type: start failure
        childInstance = new WorkflowInstance();
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("START_PROCESS,START_FAILURE_TASK_PROCESS");
        command = commandService.createSubProcessCommand(parentInstance, childInstance, instanceMap, task);
        Assertions.assertEquals(CommandType.START_FAILURE_TASK_PROCESS, command.getCommandType());
    }

    @Test
    public void testVerifyIsNeedCreateCommand() {

        List<Command> commands = new ArrayList<>();

        Command command = new Command();
        command.setCommandType(CommandType.REPEAT_RUNNING);
        command.setCommandParam("{\"" + CMD_PARAM_RECOVER_WORKFLOW_ID_STRING + "\":\"111\"}");
        commands.add(command);
        Mockito.when(commandMapper.selectList(null)).thenReturn(commands);
        Assertions.assertFalse(commandService.verifyIsNeedCreateCommand(command));

        Command command1 = new Command();
        command1.setCommandType(CommandType.REPEAT_RUNNING);
        command1.setCommandParam("{\"" + CMD_PARAM_RECOVER_WORKFLOW_ID_STRING + "\":\"222\"}");
        Assertions.assertTrue(commandService.verifyIsNeedCreateCommand(command1));

        Command command2 = new Command();
        command2.setCommandType(CommandType.PAUSE);
        Assertions.assertTrue(commandService.verifyIsNeedCreateCommand(command2));
    }

    @Test
    public void testCreateCommand() {
        Command command = new Command();
        command.setWorkflowDefinitionCode(123);
        command.setCommandParam("{\"ProcessInstanceId\":222}");
        command.setCommandType(CommandType.START_PROCESS);
        int mockResult = 1;
        Mockito.when(commandMapper.insert(command)).thenReturn(mockResult);
        int exeMethodResult = commandService.createCommand(command);
        Assertions.assertEquals(mockResult, exeMethodResult);
        Mockito.verify(commandMapper, Mockito.times(1)).insert(command);
    }

}
