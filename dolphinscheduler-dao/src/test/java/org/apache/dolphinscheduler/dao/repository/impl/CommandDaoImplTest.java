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

package org.apache.dolphinscheduler.dao.repository.impl;

import static com.google.common.truth.Truth.assertThat;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.repository.CommandDao;
import org.apache.dolphinscheduler.dao.utils.WorkerGroupUtils;

import org.apache.commons.lang3.RandomUtils;

import java.util.List;

import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CommandDaoImplTest extends BaseDaoTest {

    @Autowired
    private CommandDao commandDao;

    @RepeatedTest(value = 10)
    void fetchCommandByIdSlot() {
        int totalSlot = RandomUtils.nextInt(1, 10);
        int currentSlotIndex = RandomUtils.nextInt(0, totalSlot);
        int fetchSize = RandomUtils.nextInt(10, 100);
        int idStep = RandomUtils.nextInt(1, 5);
        int commandSize = RandomUtils.nextInt(currentSlotIndex, 1000);
        // Generate commandSize commands
        int id = 1;
        for (int j = 0; j < commandSize; j++) {
            Command command = generateCommand(CommandType.START_PROCESS, 0);
            command.setId(id);
            commandDao.insert(command);
            id += idStep;
        }

        List<Command> commands = commandDao.queryCommandByIdSlot(currentSlotIndex, totalSlot, idStep, fetchSize);
        assertThat(commands.size())
                .isEqualTo(commandDao.queryAll()
                        .stream()
                        .filter(command -> (command.getId() / idStep) % totalSlot == currentSlotIndex)
                        .limit(fetchSize)
                        .count());

    }

    private Command generateCommand(CommandType commandType, int processDefinitionCode) {
        Command command = new Command();
        command.setCommandType(commandType);
        command.setWorkflowDefinitionCode(processDefinitionCode);
        command.setExecutorId(4);
        command.setCommandParam("test command param");
        command.setTaskDependType(TaskDependType.TASK_ONLY);
        command.setFailureStrategy(FailureStrategy.CONTINUE);
        command.setWarningType(WarningType.ALL);
        command.setWarningGroupId(1);
        command.setScheduleTime(DateUtils.stringToDate("2019-12-29 12:10:00"));
        command.setWorkflowInstancePriority(Priority.MEDIUM);
        command.setStartTime(DateUtils.stringToDate("2019-12-29 10:10:00"));
        command.setUpdateTime(DateUtils.stringToDate("2019-12-29 10:10:00"));
        command.setWorkerGroup(WorkerGroupUtils.getDefaultWorkerGroup());
        command.setWorkflowInstanceId(0);
        command.setWorkflowDefinitionVersion(0);
        return command;
    }
}
