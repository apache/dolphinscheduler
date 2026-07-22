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

package org.apache.dolphinscheduler.server.master.engine.task.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.apache.dolphinscheduler.server.master.engine.task.dispatcher.event.TaskDispatchableEvent;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TaskDispatchableEventBusTest {

    private TaskDispatchableEventBus<TaskDispatchableEvent<ITaskExecution>, ITaskExecution> queue;
    private ITaskExecution taskExecution;

    @BeforeEach
    public void setUp() {
        queue = new TaskDispatchableEventBus<>();
        taskExecution = mock(ITaskExecution.class);
    }

    @Test
    public void testAdd() {
        queue.add(new TaskDispatchableEvent<>(1000, taskExecution));
        assertEquals(1, queue.size());

        queue.add(new TaskDispatchableEvent<>(2000, taskExecution));
        assertEquals(2, queue.size());
    }

    @Test
    public void testTake() throws InterruptedException {
        queue.add(new TaskDispatchableEvent<>(1000, taskExecution));
        TaskDispatchableEvent<ITaskExecution> entry = queue.take();
        assertNotNull(entry);
        assertEquals(0, queue.size());

    }

    @Test
    public void testSize() {
        assertEquals(0, queue.size());

        queue.add(new TaskDispatchableEvent<>(1000, taskExecution));
        assertEquals(1, queue.size());
    }

    @Test
    public void testClear() {
        queue.add(new TaskDispatchableEvent<>(1000, taskExecution));
        queue.add(new TaskDispatchableEvent<>(2000, taskExecution));
        assertEquals(2, queue.size());

        queue.clear();
        assertEquals(0, queue.size());
    }
}
