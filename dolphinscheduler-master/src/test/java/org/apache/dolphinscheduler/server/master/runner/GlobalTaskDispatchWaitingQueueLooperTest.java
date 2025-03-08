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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
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
import org.apache.dolphinscheduler.server.master.runner.queue.TimeBasedTaskExecutionRunnableComparableEntry;

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
    private WorkerGroupTaskDispatchManager workerGroupTaskDispatchManager;

    @Test
    void testTaskExecutionRunnableStatusIsSubmitted() {

        final TimeBasedTaskExecutionRunnableComparableEntry defaultEntryTaskExecuteRunnable =
                createTaskExecuteRunnable("workerGroup2", TaskExecutionStatus.SUBMITTED_SUCCESS);
        when(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable()).thenReturn(defaultEntryTaskExecuteRunnable);
        globalTaskDispatchWaitingQueueLooper.doDispatch();

        verify(workerGroupTaskDispatchManager, times(1)).add(anyString(), any(ITaskExecutionRunnable.class), anyLong());
    }

    private TimeBasedTaskExecutionRunnableComparableEntry createTaskExecuteRunnable(String groupName,
                                                                                    TaskExecutionStatus status) {

        WorkflowInstance workflowInstance = new WorkflowInstance();
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setWorkerGroup(groupName);
        taskInstance.setState(status);
        taskInstance.setTaskParams(JSONUtils.toJsonString(new HashMap<>()));

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
        return new TimeBasedTaskExecutionRunnableComparableEntry(0,
                new TaskExecutionRunnable(taskExecutionRunnableBuilder));
    }
}
