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

import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_RECOVER_WORKFLOW_ID_STRING;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
