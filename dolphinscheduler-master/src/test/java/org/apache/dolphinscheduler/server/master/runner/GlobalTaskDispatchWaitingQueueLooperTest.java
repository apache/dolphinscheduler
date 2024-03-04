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
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.runner.dispatcher.TaskDispatchFactory;
import org.apache.dolphinscheduler.server.master.runner.dispatcher.TaskDispatcher;
import org.apache.dolphinscheduler.server.master.runner.operator.TaskExecuteRunnableOperatorManager;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
        ProcessInstance processInstance = new ProcessInstance();
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setState(TaskExecutionStatus.KILL);
        taskInstance.setTaskParams(JSONUtils.toJsonString(new HashMap<>()));
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        TaskExecuteRunnableOperatorManager taskExecuteRunnableOperatorManager =
                new TaskExecuteRunnableOperatorManager();
        DefaultTaskExecuteRunnable defaultTaskExecuteRunnable = new DefaultTaskExecuteRunnable(processInstance,
                taskInstance, taskExecutionContext, taskExecuteRunnableOperatorManager);

        TaskDispatcher taskDispatcher = mock(TaskDispatcher.class);
        when(taskDispatchFactory.getTaskDispatcher(taskInstance)).thenReturn(taskDispatcher);
        doNothing().when(taskDispatcher).dispatchTask(any());

        when(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable()).thenReturn(defaultTaskExecuteRunnable);
        globalTaskDispatchWaitingQueueLooper.start();
        await().during(ofSeconds(1))
                .untilAsserted(() -> verify(taskDispatchFactory, never()).getTaskDispatcher(taskInstance));
        globalTaskDispatchWaitingQueueLooper.close();
    }

    @Test
    void testTaskExecutionRunnableStatusIsSubmitted() throws Exception {
        ProcessInstance processInstance = new ProcessInstance();
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
        taskInstance.setTaskParams(JSONUtils.toJsonString(new HashMap<>()));
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        TaskExecuteRunnableOperatorManager taskExecuteRunnableOperatorManager =
                new TaskExecuteRunnableOperatorManager();
        DefaultTaskExecuteRunnable defaultTaskExecuteRunnable = new DefaultTaskExecuteRunnable(processInstance,
                taskInstance, taskExecutionContext, taskExecuteRunnableOperatorManager);

        TaskDispatcher taskDispatcher = mock(TaskDispatcher.class);
        when(taskDispatchFactory.getTaskDispatcher(taskInstance)).thenReturn(taskDispatcher);
        doNothing().when(taskDispatcher).dispatchTask(any());

        when(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable()).thenReturn(defaultTaskExecuteRunnable);
        globalTaskDispatchWaitingQueueLooper.start();
        await().atMost(ofSeconds(1)).untilAsserted(() -> {
            verify(taskDispatchFactory, atLeastOnce()).getTaskDispatcher(any(TaskInstance.class));
            verify(taskDispatcher, atLeastOnce()).dispatchTask(any(TaskExecuteRunnable.class));
        });
        globalTaskDispatchWaitingQueueLooper.close();

    }
}
