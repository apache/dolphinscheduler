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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.SerialCommand;
import org.apache.dolphinscheduler.dao.model.SerialCommandDto;
import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SerialCommandDaoImplTest extends BaseDaoTest {

    @Autowired
    private SerialCommandDao serialCommandDao;

    @Test
    void testWaitingCommandCanOnlyBeAcquiredOnce() {
        SerialCommand command = insertWaitingCommand();
        Date updateTime = new Date(System.currentTimeMillis() + 1000);
        assertTrue(serialCommandDao.launchSerialCommand(command.getId(), updateTime));
        assertFalse(serialCommandDao.launchSerialCommand(command.getId(), new Date()));
        SerialCommand acquired = serialCommandDao.queryById(command.getId());
        assertEquals(SerialCommandDto.State.LAUNCHED.getValue(), acquired.getState());
        assertEquals(updateTime, acquired.getUpdateTime());
    }

    @Test
    void testStaleWaitingSnapshotCannotDeleteLaunchedCommand() {
        SerialCommand command = insertWaitingCommand();
        assertTrue(serialCommandDao.launchSerialCommand(command.getId(), new Date()));
        assertFalse(serialCommandDao.deleteByIdAndState(command.getId(), SerialCommandDto.State.WAITING));
        assertNotNull(serialCommandDao.queryById(command.getId()));
    }

    @Test
    void testWaitingCommandCanOnlyBeDiscardedOnce() {
        SerialCommand command = insertWaitingCommand();
        assertTrue(serialCommandDao.deleteByIdAndState(command.getId(), SerialCommandDto.State.WAITING));
        assertFalse(serialCommandDao.deleteByIdAndState(command.getId(), SerialCommandDto.State.WAITING));
        assertNull(serialCommandDao.queryById(command.getId()));
    }

    @Test
    void testDiscardedCommandCannotBeAcquired() {
        SerialCommand command = insertWaitingCommand();
        assertTrue(serialCommandDao.deleteByIdAndState(command.getId(), SerialCommandDto.State.WAITING));
        assertFalse(serialCommandDao.launchSerialCommand(command.getId(), new Date()));
    }

    private SerialCommand insertWaitingCommand() {
        SerialCommand command = SerialCommand.builder()
                .workflowInstanceId(1)
                .workflowDefinitionCode(1L)
                .workflowDefinitionVersion(1)
                .command("{}")
                .state(SerialCommandDto.State.WAITING.getValue())
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        assertEquals(1, serialCommandDao.insert(command));
        return command;
    }
}
