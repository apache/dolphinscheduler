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

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class WorkflowInstanceDaoImplTest extends BaseDaoTest {

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Test
    void queryByWorkflowCodeVersionStatus_EMPTY_INSTANCE() {
        long workflowDefinitionCode = 1L;
        int workflowDefinitionVersion = 1;
        int[] status = WorkflowExecutionStatus.getNeedFailoverWorkflowInstanceState();

        assertTrue(isEmpty(workflowInstanceDao.queryByWorkflowCodeVersionStatus(workflowDefinitionCode,
                workflowDefinitionVersion, status)));
    }

    @Test
    void queryByWorkflowCodeVersionStatus_EXIST_NOT_FINISH_INSTANCE() {
        long workflowDefinitionCode = 1L;
        int workflowDefinitionVersion = 1;
        int[] status = WorkflowExecutionStatus.getNotTerminalStatus();

        assertTrue(isEmpty(workflowInstanceDao.queryByWorkflowCodeVersionStatus(workflowDefinitionCode,
                workflowDefinitionVersion, status)));

        workflowInstanceDao.insert(createWorkflowInstance(workflowDefinitionCode, workflowDefinitionVersion,
                WorkflowExecutionStatus.RUNNING_EXECUTION));
        workflowInstanceDao.insert(createWorkflowInstance(workflowDefinitionCode, workflowDefinitionVersion,
                WorkflowExecutionStatus.READY_PAUSE));
        workflowInstanceDao.insert(createWorkflowInstance(workflowDefinitionCode, workflowDefinitionVersion,
                WorkflowExecutionStatus.READY_STOP));
        workflowInstanceDao.insert(createWorkflowInstance(workflowDefinitionCode, workflowDefinitionVersion,
                WorkflowExecutionStatus.SERIAL_WAIT));
        workflowInstanceDao.insert(createWorkflowInstance(workflowDefinitionCode, workflowDefinitionVersion,
                WorkflowExecutionStatus.WAIT_TO_RUN));
        assertEquals(5, workflowInstanceDao
                .queryByWorkflowCodeVersionStatus(workflowDefinitionCode, workflowDefinitionVersion, status).size());
    }

    @Test
    void updateWorkflowInstanceState_success() {
        WorkflowInstance workflowInstance = createWorkflowInstance(
                1L, 1, WorkflowExecutionStatus.RUNNING_EXECUTION);
        workflowInstanceDao.insert(workflowInstance);

        assertDoesNotThrow(() -> workflowInstanceDao.updateWorkflowInstanceState(
                workflowInstance.getId(),
                WorkflowExecutionStatus.RUNNING_EXECUTION,
                WorkflowExecutionStatus.SUCCESS));
    }

    @Test
    void updateWorkflowInstanceState_failed() {
        WorkflowInstance workflowInstance = createWorkflowInstance(
                1L, 1, WorkflowExecutionStatus.RUNNING_EXECUTION);
        workflowInstanceDao.insert(workflowInstance);

        UnsupportedOperationException unsupportedOperationException = assertThrows(UnsupportedOperationException.class,
                () -> workflowInstanceDao.updateWorkflowInstanceState(
                        workflowInstance.getId(),
                        WorkflowExecutionStatus.READY_STOP,
                        WorkflowExecutionStatus.STOP));
        Assertions.assertEquals("updateWorkflowInstance " + workflowInstance.getId()
                + " state failed, expect original state is " + WorkflowExecutionStatus.READY_STOP.name()
                + " actual state is : {} " + workflowInstance.getState().name(),
                unsupportedOperationException.getMessage());
    }

    @Test
    void queryByWorkflowCodeVersionStatus_EXIST_FINISH_INSTANCE() {
        long workflowDefinitionCode = 1L;
        int workflowDefinitionVersion = 1;
        int[] status = WorkflowExecutionStatus.getNotTerminalStatus();

        workflowInstanceDao.insert(createWorkflowInstance(workflowDefinitionCode, workflowDefinitionVersion,
                WorkflowExecutionStatus.PAUSE));
        workflowInstanceDao.insert(createWorkflowInstance(workflowDefinitionCode, workflowDefinitionVersion,
                WorkflowExecutionStatus.STOP));
        workflowInstanceDao.insert(createWorkflowInstance(workflowDefinitionCode, workflowDefinitionVersion,
                WorkflowExecutionStatus.FAILURE));
        workflowInstanceDao.insert(createWorkflowInstance(workflowDefinitionCode, workflowDefinitionVersion,
                WorkflowExecutionStatus.SUCCESS));
        assertTrue(isEmpty(workflowInstanceDao.queryByWorkflowCodeVersionStatus(workflowDefinitionCode,
                workflowDefinitionVersion, status)));
    }

    private WorkflowInstance createWorkflowInstance(Long workflowDefinitionCode, int workflowDefinitionVersion,
                                                    WorkflowExecutionStatus status) {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setName("WorkflowInstance" + System.currentTimeMillis());
        workflowInstance.setWorkflowDefinitionCode(workflowDefinitionCode);
        workflowInstance.setWorkflowDefinitionVersion(workflowDefinitionVersion);
        workflowInstance.setState(status);
        return workflowInstance;
    }

}
