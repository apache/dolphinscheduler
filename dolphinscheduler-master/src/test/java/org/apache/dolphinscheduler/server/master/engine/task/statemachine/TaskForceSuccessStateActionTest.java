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

package org.apache.dolphinscheduler.server.master.engine.task.statemachine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.AbstractLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.ITaskGroupCoordinator;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskStartLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskForceSuccessStateActionTest {

    private TaskForceSuccessStateAction taskForceSuccessStateAction;

    @Mock
    private ITaskGroupCoordinator taskGroupCoordinator;

    @Mock
    private TaskInstanceDao taskInstanceDao;

    @Mock
    private ITaskExecution taskExecution;

    @Mock
    private IWorkflowExecution workflowExecution;

    @Mock
    private WorkflowEventBus workflowEventBus;

    private TaskInstance taskInstance;

    @BeforeEach
    void setUp() {
        taskForceSuccessStateAction = new TaskForceSuccessStateAction();
        taskForceSuccessStateAction.taskGroupCoordinator = taskGroupCoordinator;
        taskForceSuccessStateAction.taskInstanceDao = taskInstanceDao;

        taskInstance = new TaskInstance();
        taskInstance.setState(TaskExecutionStatus.FORCED_SUCCESS);

        when(taskExecution.getTaskInstance()).thenReturn(taskInstance);
        when(taskExecution.getWorkflowEventBus()).thenReturn(workflowEventBus);
        when(workflowExecution.getWorkflowInstance()).thenReturn(new WorkflowInstance());
    }

    @Test
    void shouldPreserveForcedSuccessWhileContinuingWorkflow() {
        taskForceSuccessStateAction.onStartEvent(
                workflowExecution,
                taskExecution,
                TaskStartLifecycleEvent.of(taskExecution));

        assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.FORCED_SUCCESS);
        verify(workflowEventBus).publish(any(AbstractLifecycleEvent.class));
    }
}
