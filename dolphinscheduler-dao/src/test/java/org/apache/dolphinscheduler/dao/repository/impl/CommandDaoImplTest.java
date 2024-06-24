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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CommandDaoImplTest extends BaseDaoTest {

    @Autowired
    private CommandDao commandDao;

    @Test
    void fetchCommandByIdSlot() {
        int commandSize = RandomUtils.nextInt(1, 1000);
        for (int i = 0; i < commandSize; i++) {
            createCommand(CommandType.START_PROCESS, 0);
        }
        int totalSlot = RandomUtils.nextInt(1, 10);
        int currentSlotIndex = RandomUtils.nextInt(0, totalSlot);
        int fetchSize = RandomUtils.nextInt(10, 100);
        for (int i = 1; i < 5; i++) {
            int idStep = i;
            List<Command> commands = commandDao.queryCommandByIdSlot(currentSlotIndex, totalSlot, idStep, fetchSize);
            assertThat(commands.size()).isGreaterThan(0);
            assertThat(commands.size())
                    .isEqualTo(commandDao.queryAll()
                            .stream()
                            .filter(command -> (command.getId() / idStep) % totalSlot == currentSlotIndex)
                            .limit(fetchSize)
                            .count());

        }

    }

    private void createCommand(CommandType commandType, int processDefinitionCode) {
        Command command = new Command();
        command.setCommandType(commandType);
        command.setProcessDefinitionCode(processDefinitionCode);
        command.setExecutorId(4);
        command.setCommandParam("test command param");
        command.setTaskDependType(TaskDependType.TASK_ONLY);
        command.setFailureStrategy(FailureStrategy.CONTINUE);
        command.setWarningType(WarningType.ALL);
        command.setWarningGroupId(1);
        command.setScheduleTime(DateUtils.stringToDate("2019-12-29 12:10:00"));
        command.setProcessInstancePriority(Priority.MEDIUM);
        command.setStartTime(DateUtils.stringToDate("2019-12-29 10:10:00"));
        command.setUpdateTime(DateUtils.stringToDate("2019-12-29 10:10:00"));
        command.setWorkerGroup(WorkerGroupUtils.getDefaultWorkerGroup());
        command.setProcessInstanceId(0);
        command.setProcessDefinitionVersion(0);
        commandDao.insert(command);
    }
}
