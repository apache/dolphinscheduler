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

package org.apache.dolphinscheduler.plugin.task.api;

import org.apache.dolphinscheduler.plugin.task.api.utils.ProcessUtils;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class AbstractCommandExecutorTest {

    @Test
    public void testAwaitCollectedReturnsWhenTheOutputIsCollected() {
        CompletableFuture<Void> collected = new CompletableFuture<>();
        collected.complete(null);

        Assertions.assertTrue(AbstractCommandExecutor.awaitCollected(collected, 1_000L));
    }

    @Test
    public void testAwaitCollectedWithoutOutputCollection() {
        // no process was started, there is nothing to wait for
        Assertions.assertTrue(AbstractCommandExecutor.awaitCollected(null, 1_000L));
    }

    @Test
    public void testAwaitCollectedWhenTheCollectionFailed() {
        // a failed collection means the output is incomplete, the caller must not treat it as
        // collected, otherwise a missing JobID is treated as if the submission had no job
        CompletableFuture<Void> failed = new CompletableFuture<>();
        failed.completeExceptionally(new IllegalStateException("collect failed"));

        Assertions.assertFalse(AbstractCommandExecutor.awaitCollected(failed, 1_000L));
    }

    @Test
    public void testAwaitCollectedTimesOut() {
        CompletableFuture<Void> pending = new CompletableFuture<>();

        long start = System.currentTimeMillis();
        boolean collected = AbstractCommandExecutor.awaitCollected(pending, 200L);
        long elapsed = System.currentTimeMillis() - start;

        Assertions.assertFalse(collected);
        Assertions.assertTrue(elapsed >= 200L, "the wait must last for the whole timeout, elapsed: " + elapsed + "ms");
    }

    @Test
    public void testCancelApplicationPropagatesTheProcessTreeKillFailure() throws Exception {
        TaskExecutionContext taskRequest = buildTaskRequest();
        AbstractCommandExecutor executor = new TestCommandExecutor(taskRequest);
        setStartedProcess(executor);

        try (MockedStatic<ProcessUtils> processUtils = Mockito.mockStatic(ProcessUtils.class)) {
            processUtils.when(() -> ProcessUtils.kill(taskRequest)).thenReturn(false);

            TaskException exception = Assertions.assertThrows(TaskException.class, executor::cancelApplication);

            Assertions.assertTrue(exception.getMessage().contains("process tree"),
                    "the failure must name the process tree, message: " + exception.getMessage());
            // the application cleanup is still attempted, only the failure is propagated
            processUtils.verify(() -> ProcessUtils.cancelApplication(taskRequest));
        }
    }

    @Test
    public void testCancelApplicationPropagatesTheApplicationCancellationFailure() throws Exception {
        TaskExecutionContext taskRequest = buildTaskRequest();
        AbstractCommandExecutor executor = new TestCommandExecutor(taskRequest);
        setStartedProcess(executor);

        try (MockedStatic<ProcessUtils> processUtils = Mockito.mockStatic(ProcessUtils.class)) {
            processUtils.when(() -> ProcessUtils.kill(taskRequest)).thenReturn(true);
            processUtils.when(() -> ProcessUtils.cancelApplication(taskRequest))
                    .thenThrow(new TaskException("Cancel the yarn application failed"));

            TaskException exception = Assertions.assertThrows(TaskException.class, executor::cancelApplication);

            Assertions.assertTrue(exception.getMessage().contains("yarn application"),
                    "the resource manager failure must be propagated, message: " + exception.getMessage());
        }
    }

    @Test
    public void testCancelApplicationKeepsBothFailures() throws Exception {
        TaskExecutionContext taskRequest = buildTaskRequest();
        AbstractCommandExecutor executor = new TestCommandExecutor(taskRequest);
        setStartedProcess(executor);

        try (MockedStatic<ProcessUtils> processUtils = Mockito.mockStatic(ProcessUtils.class)) {
            processUtils.when(() -> ProcessUtils.kill(taskRequest)).thenReturn(false);
            processUtils.when(() -> ProcessUtils.cancelApplication(taskRequest))
                    .thenThrow(new TaskException("Cancel the yarn application failed"));

            TaskException exception = Assertions.assertThrows(TaskException.class, executor::cancelApplication);

            Assertions.assertEquals(1, exception.getSuppressed().length,
                    "the application failure must be attached to the process tree failure");
        }
    }

    private static TaskExecutionContext buildTaskRequest() {
        TaskExecutionContext taskRequest = new TaskExecutionContext();
        taskRequest.setTaskInstanceId(16789);
        taskRequest.setTaskAppId("16789");
        taskRequest.setProcessId(4321);
        return taskRequest;
    }

    /**
     * Mark the executor as started, otherwise {@code cancelApplication()} returns before killing
     * anything.
     */
    private static void setStartedProcess(AbstractCommandExecutor executor) throws Exception {
        Field processField = AbstractCommandExecutor.class.getDeclaredField("process");
        processField.setAccessible(true);
        processField.set(executor, Mockito.mock(Process.class));
    }

    private static class TestCommandExecutor extends AbstractCommandExecutor {

        TestCommandExecutor(TaskExecutionContext taskRequest) {
            super(taskRequest);
        }
    }
}
