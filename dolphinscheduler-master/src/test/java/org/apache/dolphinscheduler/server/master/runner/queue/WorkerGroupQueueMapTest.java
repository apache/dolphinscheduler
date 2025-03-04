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

package org.apache.dolphinscheduler.server.master.runner.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.graph.WorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionRunnableBuilder;
import org.apache.dolphinscheduler.server.master.runner.TaskExecutionContextFactory;

import org.apache.commons.lang3.RandomUtils;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
class WorkerGroupQueueMapTest {

    @InjectMocks
    private WorkerGroupQueueMap workerGroupQueueMap;

    @Mock
    private ITaskExecutionRunnable taskExecutionRunnableMock;

    @BeforeEach
    public void setUp() {
        workerGroupQueueMap = new WorkerGroupQueueMap();
        taskExecutionRunnableMock = createTaskExecuteRunnable();
    }

    private ITaskExecutionRunnable createTaskExecuteRunnable() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setWorkflowInstancePriority(Priority.MEDIUM);

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(RandomUtils.nextInt());
        taskInstance.setTaskInstancePriority(Priority.MEDIUM);
        taskInstance.setFirstSubmitTime(new Date());

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
    void testAddTaskWithDelaySuccess() {
        String workerGroup = "testGroup";
        long delayTimeMills = 1000L;

        workerGroupQueueMap.add(workerGroup, taskExecutionRunnableMock, delayTimeMills);

        Map<String, ITaskExecutionRunnable> map = workerGroupQueueMap.poll();

        // 使用 ScheduledExecutorService 模拟延迟调用
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            ITaskExecutionRunnable iTaskExecutionRunnable = map.get(workerGroup);
            assertNotNull(iTaskExecutionRunnable);
        }, 1000, TimeUnit.SECONDS);

    }

    @Test
    void testAddTaskWithoutDelaySuccess() {
        String workerGroup = "testGroup";

        workerGroupQueueMap.add(workerGroup, taskExecutionRunnableMock, 0);

        Map<String, ITaskExecutionRunnable> map = workerGroupQueueMap.poll();
        ITaskExecutionRunnable iTaskExecutionRunnable = map.get(workerGroup);
        assertNotNull(iTaskExecutionRunnable);
    }

    @Test
    void testPollWithTasksSuccess() {
        String workerGroup1 = "group1";
        String workerGroup2 = "group2";
        ITaskExecutionRunnable task1 = createTaskExecuteRunnable();
        ITaskExecutionRunnable task2 = createTaskExecuteRunnable();

        workerGroupQueueMap.add(workerGroup1, task1, 0L);
        workerGroupQueueMap.add(workerGroup2, task2, 0L);

        Map<String, ITaskExecutionRunnable> result = workerGroupQueueMap.poll();

        assertEquals(2, result.size());
        assertTrue(result.containsKey(workerGroup1));
        assertTrue(result.containsKey(workerGroup2));
        assertEquals(task1, result.get(workerGroup1));
        assertEquals(task2, result.get(workerGroup2));
    }

    @Test
    void testPollWithNoTasksSuccess() {
        Map<String, ITaskExecutionRunnable> result = workerGroupQueueMap.poll();

        assertTrue(result.isEmpty());
    }

    @Test
    void testPollWithPartialTasksSuccess() {
        String workerGroup1 = "group1";
        String workerGroup2 = "group2";
        ITaskExecutionRunnable task1 = createTaskExecuteRunnable();
        ITaskExecutionRunnable task2 = createTaskExecuteRunnable();

        workerGroupQueueMap.add(workerGroup1, task1, 0L);
        workerGroupQueueMap.add(workerGroup2, task2, 1000L);

        Map<String, ITaskExecutionRunnable> result = workerGroupQueueMap.poll();

        assertEquals(1, result.size());
        assertTrue(result.containsKey(workerGroup1));
        assertEquals(task1, result.get(workerGroup1));
    }
}
