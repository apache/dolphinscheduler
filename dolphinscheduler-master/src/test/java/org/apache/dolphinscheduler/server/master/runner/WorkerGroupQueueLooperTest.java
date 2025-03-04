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

package org.apache.dolphinscheduler.server.master.runner;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.graph.WorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionRunnableBuilder;
import org.apache.dolphinscheduler.server.master.exception.dispatch.TaskDispatchException;
import org.apache.dolphinscheduler.server.master.runner.queue.WorkerGroupQueueMap;

import org.apache.commons.lang3.RandomUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class WorkerGroupQueueLooperTest {

    @Mock
    private WorkerGroupQueueMap workerGroupQueueMap;

    @Mock
    private ITaskExecutorClient taskExecutorClient;

    @InjectMocks
    private WorkerGroupQueueLooper workerGroupQueueLooper;

    private ITaskExecutionRunnable createTaskExecuteRunnable(TaskExecutionStatus state) {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setWorkflowInstancePriority(Priority.MEDIUM);

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(RandomUtils.nextInt());
        taskInstance.setTaskInstancePriority(Priority.MEDIUM);
        taskInstance.setFirstSubmitTime(new Date());
        taskInstance.setState(state);

        final ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(TaskExecutionContextFactory.class))
                .thenReturn(mock(TaskExecutionContextFactory.class));
        final TaskExecutionRunnableBuilder taskExecutionRunnableBuilder = TaskExecutionRunnableBuilder.builder()
                .applicationContext(applicationContext)
                .workflowInstance(workflowInstance)
                .taskInstance(taskInstance)
                .workflowExecutionGraph(new WorkflowExecutionGraph())
                .workflowDefinition(new WorkflowDefinition())
                .project(new Project())
                .taskDefinition(new TaskDefinition())
                .workflowEventBus(new WorkflowEventBus())
                .build();
        return new TaskExecutionRunnable(taskExecutionRunnableBuilder);
    }
    @Test
    void testDoDispatchSuccess() throws TaskDispatchException {
        ITaskExecutionRunnable taskExecutionRunnable = createTaskExecuteRunnable(TaskExecutionStatus.SUBMITTED_SUCCESS);

        Map<String, ITaskExecutionRunnable> taskMap = new HashMap<>();
        taskMap.put("testWorkerGroup", taskExecutionRunnable);
        when(workerGroupQueueMap.poll()).thenReturn(taskMap);

        workerGroupQueueLooper.doDispatch();

        verify(taskExecutorClient, times(1)).dispatch(taskExecutionRunnable);
    }

    @Test
    void testDoDispatchFail() throws TaskDispatchException {
        ITaskExecutionRunnable taskExecutionRunnable = createTaskExecuteRunnable(TaskExecutionStatus.SUBMITTED_SUCCESS);
        doThrow(new RuntimeException("Dispatch failed")).when(taskExecutorClient).dispatch(taskExecutionRunnable);

        Map<String, ITaskExecutionRunnable> taskMap = new HashMap<>();
        taskMap.put("testWorkerGroup", taskExecutionRunnable);
        when(workerGroupQueueMap.poll()).thenReturn(taskMap);

        assertThrows(RuntimeException.class, () -> workerGroupQueueLooper.doDispatch());
    }

    @Test
    void testDoDispatchTaskStateNotEligible() throws TaskDispatchException {
        ITaskExecutionRunnable taskExecutionRunnable = createTaskExecuteRunnable(TaskExecutionStatus.SUCCESS);

        Map<String, ITaskExecutionRunnable> taskMap = new HashMap<>();
        taskMap.put("testWorkerGroup", taskExecutionRunnable);
        when(workerGroupQueueMap.poll()).thenReturn(taskMap);

        workerGroupQueueLooper.doDispatch();

        verify(taskExecutorClient, never()).dispatch(taskExecutionRunnable);
    }
}
