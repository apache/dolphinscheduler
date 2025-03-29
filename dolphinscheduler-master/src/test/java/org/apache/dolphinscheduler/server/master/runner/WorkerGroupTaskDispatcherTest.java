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

import static org.apache.dolphinscheduler.common.thread.ThreadUtils.sleep;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;

import java.time.Duration;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WorkerGroupTaskDispatcherTest {

    @Mock
    private ITaskExecutorClient taskExecutorClient;

    @Mock
    private ITaskExecutionRunnable taskExecutionRunnable;

    @InjectMocks
    private WorkerGroupTaskDispatcher workerGroupTaskDispatcher;

    @Test
    public void testInitAddTaskSuccess() {
        boolean result = workerGroupTaskDispatcher.addTaskToWorkerGroupQueue(taskExecutionRunnable, 0L);
        assertTrue(result);
    }

    @Test
    public void testAddTaskFail() {
        workerGroupTaskDispatcher.markDispatcherClosing();
        boolean result = workerGroupTaskDispatcher.addTaskToWorkerGroupQueue(taskExecutionRunnable, 0L);
        assertFalse(result);
    }

    @Test
    public void testStartDispatcher() {
        assertFalse(workerGroupTaskDispatcher.isAlive());
        workerGroupTaskDispatcher.start();
        assertTrue(workerGroupTaskDispatcher.isAlive());
    }

    @Test
    public void testCloseToStartDispatcher() {
        workerGroupTaskDispatcher.start();
        workerGroupTaskDispatcher.markDispatcherClosing();

        boolean result = workerGroupTaskDispatcher.addTaskToWorkerGroupQueue(taskExecutionRunnable, 0L);
        assertFalse(result);
        sleep(1000);
        Awaitility.await()
                .atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
                    assertTrue(workerGroupTaskDispatcher.checkCloseDispatchWorkerComplete());
                    // closed can not to start, cannot add task
                    workerGroupTaskDispatcher.start();
                    assertFalse(workerGroupTaskDispatcher.addTaskToWorkerGroupQueue(taskExecutionRunnable, 0L));
                });
    }
}
