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

package org.apache.dolphinscheduler.server.worker.executor;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.task.executor.ITaskExecutor;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck;
import org.apache.dolphinscheduler.task.executor.events.IReportableTaskExecutorLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorLifecycleEventType;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorSuccessLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorReassignMasterRequest;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PhysicalTaskEngineDelegatorTest {

    private final PhysicalTaskExecutorRepository repository = new PhysicalTaskExecutorRepository();
    private final PhysicalTaskExecutorEventRemoteReporterClient client =
            mock(PhysicalTaskExecutorEventRemoteReporterClient.class);
    private final PhysicalTaskExecutorLifecycleEventReporter reporter =
            new PhysicalTaskExecutorLifecycleEventReporter(client, repository, new WorkerConfig());
    private final PhysicalTaskEngineDelegator delegator = new PhysicalTaskEngineDelegator(
            mock(PhysicalTaskEngineFactory.class), mock(PhysicalTaskExecutorFactory.class), repository, reporter);

    @AfterEach
    void tearDown() throws InterruptedException {
        reporter.close();
        reporter.join(5000);
        assertFalse(reporter.isAlive());
    }

    @Test
    void shouldTakeOverPendingResultAfterExecutorIsFinalized() {
        addExecutor();
        reporter.reportTaskExecutorLifecycleEvent(TaskExecutorSuccessLifecycleEvent.builder()
                .taskInstanceId(7).workflowInstanceId(101).type(TaskExecutorLifecycleEventType.SUCCESS).build());
        repository.remove(7);
        final AtomicReference<String> destination = new AtomicReference<>();
        doAnswer(invocation -> {
            final IReportableTaskExecutorLifecycleEvent event = invocation.getArgument(1);
            event.setLatestReportTime(System.currentTimeMillis());
            destination.set(invocation.getArgument(0));
            reporter.receiveTaskExecutorLifecycleEventACK(new TaskExecutorLifecycleEventAck(7, event.getType()));
            return null;
        }).when(client).reportTaskExecutionEventToMaster(anyString(), any());

        assertTrue(delegator.reassignWorkflowInstanceHost(request()));
        reporter.start();

        await().atMost(Duration.ofSeconds(5)).until(() -> reporter.getEventChannels().isEmpty());
        assertEquals("new-master:5678", destination.get());
        assertFalse(repository.contains(7));
    }

    @Test
    void shouldStillTakeOverExecutingTaskWithoutPendingEvents() {
        final TaskExecutionContext context = addExecutor();

        assertTrue(delegator.reassignWorkflowInstanceHost(request()));

        assertEquals("new-master:5678", context.getWorkflowInstanceHost());
    }

    @Test
    void shouldRejectTakeoverWhenNeitherExecutorNorResultExists() {
        assertFalse(delegator.reassignWorkflowInstanceHost(request()));
    }

    private TaskExecutionContext addExecutor() {
        final TaskExecutionContext context = new TaskExecutionContext();
        context.setWorkflowInstanceHost("old-master:5678");
        final ITaskExecutor executor = mock(ITaskExecutor.class);
        when(executor.getId()).thenReturn(7);
        when(executor.getTaskExecutionContext()).thenReturn(context);
        repository.put(executor);
        return context;
    }

    private TaskExecutorReassignMasterRequest request() {
        return TaskExecutorReassignMasterRequest.builder().taskInstanceId(7).workflowHost("new-master:5678").build();
    }
}
