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

import static java.time.Duration.ofSeconds;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.graph.WorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionRunnableBuilder;
import org.apache.dolphinscheduler.server.master.runner.dispatcher.TaskDispatchFactory;
import org.apache.dolphinscheduler.server.master.runner.dispatcher.TaskDispatcher;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GlobalTaskDispatchWaitingQueueLooperTest {

    @InjectMocks
    private GlobalTaskDispatchWaitingQueueLooper globalTaskDispatchWaitingQueueLooper;

    @Mock
    private GlobalTaskDispatchWaitingQueue globalTaskDispatchWaitingQueue;

    @Mock
    private TaskDispatchFactory taskDispatchFactory;

    @Test
    void testTaskExecutionRunnableStatusIsNotSubmitted() throws Exception {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setState(TaskExecutionStatus.KILL);
        taskInstance.setTaskParams(JSONUtils.toJsonString(new HashMap<>()));
        final ITaskExecutionRunnable defaultTaskExecuteRunnable =
                createTaskExecuteRunnable(taskInstance, workflowInstance);

        TaskDispatcher taskDispatcher = mock(TaskDispatcher.class);
        when(taskDispatchFactory.getTaskDispatcher(taskInstance)).thenReturn(taskDispatcher);
        doNothing().when(taskDispatcher).dispatchTask(any());

        when(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable()).thenReturn(defaultTaskExecuteRunnable);
        globalTaskDispatchWaitingQueueLooper.doDispatch();
        await().during(ofSeconds(1))
                .untilAsserted(() -> verify(taskDispatchFactory, never()).getTaskDispatcher(taskInstance));
    }

    @Test
    void testTaskExecutionRunnableStatusIsSubmitted() throws Exception {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
        taskInstance.setTaskParams(JSONUtils.toJsonString(new HashMap<>()));
        final ITaskExecutionRunnable defaultTaskExecuteRunnable =
                createTaskExecuteRunnable(taskInstance, workflowInstance);

        TaskDispatcher taskDispatcher = mock(TaskDispatcher.class);
        when(taskDispatchFactory.getTaskDispatcher(taskInstance)).thenReturn(taskDispatcher);
        doNothing().when(taskDispatcher).dispatchTask(any());

        when(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable()).thenReturn(defaultTaskExecuteRunnable);
        globalTaskDispatchWaitingQueueLooper.doDispatch();
        await().atMost(ofSeconds(1)).untilAsserted(() -> {
            verify(taskDispatchFactory, atLeastOnce()).getTaskDispatcher(any(TaskInstance.class));
            verify(taskDispatcher, atLeastOnce()).dispatchTask(any(ITaskExecutionRunnable.class));
        });

    }

    private ITaskExecutionRunnable createTaskExecuteRunnable(final TaskInstance taskInstance,
                                                             final WorkflowInstance workflowInstance) {

        final ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(TaskExecutionContextFactory.class))
                .thenReturn(mock(TaskExecutionContextFactory.class));
        final TaskExecutionRunnableBuilder taskExecutionRunnableBuilder = TaskExecutionRunnableBuilder.builder()
                .applicationContext(applicationContext)
                .workflowInstance(workflowInstance)
                .taskInstance(taskInstance)
                .workflowExecutionGraph(new WorkflowExecutionGraph())
                .workflowDefinition(new WorkflowDefinition())
                .taskDefinition(new TaskDefinition())
                .workflowEventBus(new WorkflowEventBus())
                .build();
        return new TaskExecutionRunnable(taskExecutionRunnableBuilder);
    }
}
