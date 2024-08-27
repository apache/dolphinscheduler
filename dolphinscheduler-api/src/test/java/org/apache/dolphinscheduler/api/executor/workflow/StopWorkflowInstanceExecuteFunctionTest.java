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

package org.apache.dolphinscheduler.api.executor.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StopWorkflowInstanceExecuteFunctionTest {

    @Mock
    private WorkflowInstanceDao workflowInstanceDao;

    @InjectMocks
    private StopWorkflowInstanceExecutorDelegate stopWorkflowInstanceExecutorDelegate;

    @ParameterizedTest
    @EnumSource(value = WorkflowExecutionStatus.class, names = {
            "RUNNING_EXECUTION",
            "READY_PAUSE",
            "READY_STOP",
            "SERIAL_WAIT",
            "WAIT_TO_RUN"})
    void exceptionIfWorkflowInstanceCannotStop_canStop(WorkflowExecutionStatus workflowExecutionStatus) {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setName("Workflow-1");
        workflowInstance.setState(workflowExecutionStatus);
        assertDoesNotThrow(
                () -> stopWorkflowInstanceExecutorDelegate.exceptionIfWorkflowInstanceCannotStop(workflowInstance));
    }

    @ParameterizedTest
    @EnumSource(value = WorkflowExecutionStatus.class, names = {
            "RUNNING_EXECUTION",
            "READY_PAUSE",
            "READY_STOP",
            "SERIAL_WAIT",
            "WAIT_TO_RUN"}, mode = EnumSource.Mode.EXCLUDE)
    void exceptionIfWorkflowInstanceCannotStop_canNotStop(WorkflowExecutionStatus workflowExecutionStatus) {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setName("Workflow-1");
        workflowInstance.setState(workflowExecutionStatus);
        ServiceException serviceException = assertThrows(ServiceException.class,
                () -> stopWorkflowInstanceExecutorDelegate.exceptionIfWorkflowInstanceCannotStop(workflowInstance));
        Assertions.assertEquals(
                "Internal Server Error: The workflow instance: Workflow-1 status is " + workflowExecutionStatus
                        + ", can not stop",
                serviceException.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = WorkflowExecutionStatus.class, names = {
            "SERIAL_WAIT",
            "WAIT_TO_RUN"})
    void ifWorkflowInstanceCanDirectStopInDB_canDirectStopInDB(WorkflowExecutionStatus workflowExecutionStatus) {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setName("Workflow-1");
        workflowInstance.setState(workflowExecutionStatus);
        Assertions
                .assertTrue(stopWorkflowInstanceExecutorDelegate.ifWorkflowInstanceCanDirectStopInDB(workflowInstance));
    }

    @ParameterizedTest
    @EnumSource(value = WorkflowExecutionStatus.class, names = {
            "SERIAL_WAIT",
            "WAIT_TO_RUN"}, mode = EnumSource.Mode.EXCLUDE)
    void ifWorkflowInstanceCanDirectStopInDB_canNotDirectStopInDB(WorkflowExecutionStatus workflowExecutionStatus) {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setName("Workflow-1");
        workflowInstance.setState(workflowExecutionStatus);
        Assertions.assertFalse(
                stopWorkflowInstanceExecutorDelegate.ifWorkflowInstanceCanDirectStopInDB(workflowInstance));
    }
}
