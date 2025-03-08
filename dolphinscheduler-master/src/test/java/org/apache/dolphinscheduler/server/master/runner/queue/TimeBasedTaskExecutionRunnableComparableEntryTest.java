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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.Priority;
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
import org.apache.dolphinscheduler.server.master.runner.TaskExecutionContextFactory;

import java.util.Date;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

public class TimeBasedTaskExecutionRunnableComparableEntryTest {

    private static final long DEFAULT_DELAY_TIME = 1000L;
    private ITaskExecutionRunnable mockTaskExecutionRunnable;

    private Date date = new Date();

    @BeforeEach
    public void setUp() {
        mockTaskExecutionRunnable = createTaskExecuteRunnable().getData();
    }

    private TimeBasedTaskExecutionRunnableComparableEntry createTaskExecuteRunnable() {
        return createTaskExecuteRunnable(Priority.MEDIUM);
    }
    private TimeBasedTaskExecutionRunnableComparableEntry createTaskExecuteRunnable(Priority workFlowInstancePriority) {

        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setWorkflowInstancePriority(workFlowInstancePriority);
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setWorkerGroup("default");
        taskInstance.setTaskInstancePriority(Priority.MEDIUM);
        taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
        taskInstance.setFirstSubmitTime(date);
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

    @Test
    public void testConstructor_NullData_ThrowsException() {
        assertThrows(NullPointerException.class,
                () -> new TimeBasedTaskExecutionRunnableComparableEntry(DEFAULT_DELAY_TIME, null));
    }

    @Test
    public void testCompareTo_DataDifferent_ReturnsNonZero() {
        TimeBasedTaskExecutionRunnableComparableEntry entry1 =
                new TimeBasedTaskExecutionRunnableComparableEntry(DEFAULT_DELAY_TIME, mockTaskExecutionRunnable);
        TimeBasedTaskExecutionRunnableComparableEntry entry2 =
                new TimeBasedTaskExecutionRunnableComparableEntry(DEFAULT_DELAY_TIME,
                        createTaskExecuteRunnable(Priority.HIGH).getData());
        int result = entry1.compareTo(entry2);
        assertNotEquals(0, result);
    }

    @Test
    public void testCompareTo_DataSameDelayDifferent_ReturnsNegative() {
        TimeBasedTaskExecutionRunnableComparableEntry entry1 =
                new TimeBasedTaskExecutionRunnableComparableEntry(DEFAULT_DELAY_TIME, mockTaskExecutionRunnable);
        TimeBasedTaskExecutionRunnableComparableEntry entry2 = new TimeBasedTaskExecutionRunnableComparableEntry(
                DEFAULT_DELAY_TIME + 1, createTaskExecuteRunnable().getData());
        int result = entry1.compareTo(entry2);
        // entry1 should be greater priority than entry2
        assertTrue(result < 0);
    }

    @Test
    public void testCompareTo_DataSameDelaySame_ReturnsZero() {
        TimeBasedTaskExecutionRunnableComparableEntry entry1 =
                new TimeBasedTaskExecutionRunnableComparableEntry(DEFAULT_DELAY_TIME, mockTaskExecutionRunnable);
        TimeBasedTaskExecutionRunnableComparableEntry entry2 = new TimeBasedTaskExecutionRunnableComparableEntry(
                DEFAULT_DELAY_TIME, createTaskExecuteRunnable().getData());
        int result = entry1.compareTo(entry2);
        assertEquals(0, result);
    }

    @Test
    public void testEquals_SameObjects_ReturnsTrue() {
        TimeBasedTaskExecutionRunnableComparableEntry entry1 =
                new TimeBasedTaskExecutionRunnableComparableEntry(DEFAULT_DELAY_TIME, mockTaskExecutionRunnable);
        TimeBasedTaskExecutionRunnableComparableEntry entry2 =
                new TimeBasedTaskExecutionRunnableComparableEntry(DEFAULT_DELAY_TIME, mockTaskExecutionRunnable);
        assertEquals(entry1, entry2);
    }

    @Test
    public void testEquals_DifferentObjects_ReturnsFalse() {
        TimeBasedTaskExecutionRunnableComparableEntry entry1 =
                new TimeBasedTaskExecutionRunnableComparableEntry(DEFAULT_DELAY_TIME, mockTaskExecutionRunnable);
        TimeBasedTaskExecutionRunnableComparableEntry entry2 = new TimeBasedTaskExecutionRunnableComparableEntry(
                DEFAULT_DELAY_TIME + 1, createTaskExecuteRunnable().getData());
        assertNotEquals(entry1, entry2);
    }

    @Test
    public void testHashCode_SameObjects_HashCodeEqual() {
        TimeBasedTaskExecutionRunnableComparableEntry entry1 =
                new TimeBasedTaskExecutionRunnableComparableEntry(DEFAULT_DELAY_TIME, mockTaskExecutionRunnable);
        TimeBasedTaskExecutionRunnableComparableEntry entry2 =
                new TimeBasedTaskExecutionRunnableComparableEntry(DEFAULT_DELAY_TIME, mockTaskExecutionRunnable);
        assertEquals(entry1.hashCode(), entry2.hashCode());
    }

    @Test
    public void testHashCode_DifferentObjects_HashCodeDifferent() {
        TimeBasedTaskExecutionRunnableComparableEntry entry1 =
                new TimeBasedTaskExecutionRunnableComparableEntry(DEFAULT_DELAY_TIME, mockTaskExecutionRunnable);
        TimeBasedTaskExecutionRunnableComparableEntry entry2 = new TimeBasedTaskExecutionRunnableComparableEntry(
                DEFAULT_DELAY_TIME + 1, createTaskExecuteRunnable().getData());
        assertNotEquals(entry1.hashCode(), entry2.hashCode());
    }
}
