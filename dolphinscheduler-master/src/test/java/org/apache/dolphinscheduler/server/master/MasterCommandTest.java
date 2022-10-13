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

package org.apache.dolphinscheduler.server.master;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  master test
 */
@Disabled
public class MasterCommandTest {

    private final Logger logger = LoggerFactory.getLogger(MasterCommandTest.class);

    private CommandMapper commandMapper;

    private ProcessDefinitionMapper processDefinitionMapper;

    @Test
    public void startFromFailedCommand() {
        Command cmd = new Command();
        cmd.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        cmd.setCommandParam("{\"ProcessInstanceId\":325}");
        cmd.setProcessDefinitionCode(63);

        commandMapper.insert(cmd);

    }

    @Test
    public void recoverSuspendCommand() {

        Command cmd = new Command();
        cmd.setProcessDefinitionCode(44);
        cmd.setCommandParam("{\"ProcessInstanceId\":290}");
        cmd.setCommandType(CommandType.RECOVER_SUSPENDED_PROCESS);

        commandMapper.insert(cmd);
    }

    @Test
    public void startNewProcessCommand() {
        Command cmd = new Command();
        cmd.setCommandType(CommandType.START_PROCESS);
        cmd.setProcessDefinitionCode(167);
        cmd.setFailureStrategy(FailureStrategy.CONTINUE);
        cmd.setWarningType(WarningType.NONE);
        cmd.setWarningGroupId(4);
        cmd.setExecutorId(19);

        commandMapper.insert(cmd);
    }

    @Test
    public void toleranceCommand() {
        Command cmd = new Command();
        cmd.setCommandType(CommandType.RECOVER_TOLERANCE_FAULT_PROCESS);
        cmd.setCommandParam("{\"ProcessInstanceId\":816}");
        cmd.setProcessDefinitionCode(15);

        commandMapper.insert(cmd);
    }

    @Test
    public void insertCommand() {
        Command cmd = new Command();
        cmd.setCommandType(CommandType.START_PROCESS);
        cmd.setFailureStrategy(FailureStrategy.CONTINUE);
        cmd.setWarningType(WarningType.ALL);
        cmd.setProcessDefinitionCode(72);
        cmd.setExecutorId(10);
        commandMapper.insert(cmd);
    }

}
