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

package org.apache.dolphinscheduler.plugin.task.api.k8s.impl;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.K8sUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobStatus;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;

public class K8sJobMonitorTest {

    private final String namespace = "namespace";
    private final int taskInstanceId = 1000;
    private final String taskName = "k8s_task_test";
    private Job job;
    private K8sUtils k8sUtils;
    private TaskExecutionContext taskRequest;

    @BeforeEach
    public void before() throws Exception {
        taskRequest = new TaskExecutionContext();
        taskRequest.setTaskInstanceId(taskInstanceId);
        taskRequest.setTaskName(taskName);
        k8sUtils = mock(K8sUtils.class);
        job = new JobBuilder()
                .withNewMetadata()
                .withName(String.format("%s-%s", taskName, taskInstanceId))
                .withNamespace(namespace)
                .endMetadata()
                .build();
    }

    private K8sJobMonitor createMonitor(TaskExecutionContext taskExecutionContext) {
        return createMonitor(taskExecutionContext, K8sJobMonitor.DEFAULT_POLL_INTERVAL_SECONDS,
                K8sJobMonitor.DEFAULT_MAX_CONSECUTIVE_POLL_FAILURES);
    }

    private K8sJobMonitor createMonitor(TaskExecutionContext taskExecutionContext,
                                        long pollIntervalSeconds,
                                        int maxConsecutivePollFailures) {
        return new K8sJobMonitor(k8sUtils, taskExecutionContext, pollIntervalSeconds, maxConsecutivePollFailures);
    }

    private WatcherHarness startMonitor(K8sJobMonitor monitor, TaskResponse taskResponse) throws InterruptedException {
        WatcherHarness harness = new WatcherHarness();
        harness.informer = mock(SharedIndexInformer.class);
        when(harness.informer.start()).thenReturn(CompletableFuture.completedFuture(null));
        CountDownLatch handlerReady = new CountDownLatch(1);
        AtomicReference<ResourceEventHandler<Job>> handlerRef = new AtomicReference<>();
        when(k8sUtils.createBatchJobInformer(eq(job.getMetadata().getName()), eq(namespace), any()))
                .thenAnswer(invocation -> {
                    handlerRef.set(invocation.getArgument(2));
                    handlerReady.countDown();
                    return harness.informer;
                });
        harness.thread = new Thread(() -> monitor.monitorUntilTerminal(job, taskResponse));
        harness.thread.start();
        Assertions.assertTrue(handlerReady.await(5, TimeUnit.SECONDS));
        harness.handler = handlerRef.get();
        return harness;
    }

    private void finishWatcher(WatcherHarness harness) throws InterruptedException {
        harness.thread.join(5000);
        verify(harness.informer).stop();
    }

    private Job jobWithStatus(Integer succeeded, Integer failed) {
        JobStatus status = new JobStatus();
        status.setSucceeded(succeeded);
        status.setFailed(failed);
        Job watchedJob = new Job();
        watchedJob.setMetadata(job.getMetadata());
        watchedJob.setStatus(status);
        return watchedJob;
    }

    @Test
    public void testMonitorOnUpdateSuccess() throws Exception {
        TaskResponse taskResponse = new TaskResponse();
        WatcherHarness harness = startMonitor(createMonitor(taskRequest), taskResponse);
        harness.handler.onUpdate(job, jobWithStatus(1, null));
        finishWatcher(harness);
        assertEquals(EXIT_CODE_SUCCESS, taskResponse.getExitStatusCode());
    }

    @Test
    public void testMonitorOnUpdateFailed() throws Exception {
        TaskResponse taskResponse = new TaskResponse();
        WatcherHarness harness = startMonitor(createMonitor(taskRequest), taskResponse);
        harness.handler.onUpdate(job, jobWithStatus(null, 1));
        finishWatcher(harness);
        assertEquals(EXIT_CODE_FAILURE, taskResponse.getExitStatusCode());
    }

    @Test
    public void testMonitorOnDelete() throws Exception {
        TaskResponse taskResponse = new TaskResponse();
        WatcherHarness harness = startMonitor(createMonitor(taskRequest), taskResponse);
        harness.handler.onDelete(job, false);
        finishWatcher(harness);
        assertEquals(EXIT_CODE_FAILURE, taskResponse.getExitStatusCode());
    }

    @Test
    public void testMonitorOnAddSuccess() throws Exception {
        TaskResponse taskResponse = new TaskResponse();
        WatcherHarness harness = startMonitor(createMonitor(taskRequest), taskResponse);
        harness.handler.onAdd(jobWithStatus(1, null));
        finishWatcher(harness);
        assertEquals(EXIT_CODE_SUCCESS, taskResponse.getExitStatusCode());
    }

    @Test
    public void testMonitorOnAddFailed() throws Exception {
        TaskResponse taskResponse = new TaskResponse();
        WatcherHarness harness = startMonitor(createMonitor(taskRequest), taskResponse);
        harness.handler.onAdd(jobWithStatus(null, 1));
        finishWatcher(harness);
        assertEquals(EXIT_CODE_FAILURE, taskResponse.getExitStatusCode());
    }

    @Test
    public void testMonitorOnAddRunning() throws Exception {
        TaskResponse taskResponse = new TaskResponse();
        WatcherHarness harness = startMonitor(createMonitor(taskRequest), taskResponse);
        harness.handler.onAdd(jobWithStatus(null, null));
        Assertions.assertTrue(harness.thread.isAlive());
        harness.handler.onUpdate(job, jobWithStatus(1, null));
        finishWatcher(harness);
        assertEquals(EXIT_CODE_SUCCESS, taskResponse.getExitStatusCode());
    }

    @Test
    public void testMonitorInformerStartFailed() throws Exception {
        TaskResponse taskResponse = new TaskResponse();
        doThrow(new RuntimeException("informer start failed")).when(k8sUtils)
                .createBatchJobInformer(eq(job.getMetadata().getName()), eq(namespace), any());
        Thread thread = new Thread(() -> createMonitor(taskRequest).monitorUntilTerminal(job, taskResponse));
        thread.start();
        thread.join(5000);
        assertEquals(EXIT_CODE_FAILURE, taskResponse.getExitStatusCode());
    }

    @Test
    public void testMonitorIgnoreRunningUpdate() throws Exception {
        TaskResponse taskResponse = new TaskResponse();
        WatcherHarness harness = startMonitor(createMonitor(taskRequest), taskResponse);
        harness.handler.onUpdate(job, jobWithStatus(null, null));
        Assertions.assertTrue(harness.thread.isAlive());
        harness.handler.onUpdate(job, jobWithStatus(1, null));
        finishWatcher(harness);
        assertEquals(EXIT_CODE_SUCCESS, taskResponse.getExitStatusCode());
    }

    @Test
    public void testMonitorDoesNotOverwriteFirstTerminalStatus() throws Exception {
        TaskResponse taskResponse = new TaskResponse();
        WatcherHarness harness = startMonitor(createMonitor(taskRequest), taskResponse);
        harness.handler.onUpdate(job, jobWithStatus(1, null));
        harness.handler.onDelete(job, false);
        finishWatcher(harness);
        assertEquals(EXIT_CODE_SUCCESS, taskResponse.getExitStatusCode());
    }

    @Test
    public void testMonitorTimeout() throws Exception {
        taskRequest.setTaskTimeoutStrategy(TaskTimeoutStrategy.FAILED);
        taskRequest.setTaskTimeout(1);

        TaskResponse taskResponse = new TaskResponse();
        WatcherHarness harness = startMonitor(createMonitor(taskRequest), taskResponse);
        finishWatcher(harness);
        assertEquals(EXIT_CODE_FAILURE, taskResponse.getExitStatusCode());
    }

    @Test
    public void testMonitorJobStatusPollingSuccess() throws Exception {
        when(k8sUtils.getJob(eq(job.getMetadata().getName()), eq(namespace)))
                .thenReturn(jobWithStatus(1, null));

        TaskResponse taskResponse = new TaskResponse();
        WatcherHarness harness = startMonitor(createMonitor(taskRequest, 1L,
                K8sJobMonitor.DEFAULT_MAX_CONSECUTIVE_POLL_FAILURES), taskResponse);
        finishWatcher(harness);
        assertEquals(EXIT_CODE_SUCCESS, taskResponse.getExitStatusCode());
        verify(k8sUtils).getJob(eq(job.getMetadata().getName()), eq(namespace));
    }

    @Test
    public void testMonitorJobStatusPollingDeleted() throws Exception {
        when(k8sUtils.getJob(eq(job.getMetadata().getName()), eq(namespace))).thenReturn(null);

        TaskResponse taskResponse = new TaskResponse();
        WatcherHarness harness = startMonitor(createMonitor(taskRequest, 1L,
                K8sJobMonitor.DEFAULT_MAX_CONSECUTIVE_POLL_FAILURES), taskResponse);
        finishWatcher(harness);
        assertEquals(EXIT_CODE_FAILURE, taskResponse.getExitStatusCode());
        verify(k8sUtils).getJob(eq(job.getMetadata().getName()), eq(namespace));
    }

    @Test
    public void testMonitorConsecutivePollFailuresWithoutTimeoutStrategy() throws Exception {
        taskRequest.setTaskTimeoutStrategy(TaskTimeoutStrategy.WARN);
        when(k8sUtils.getJob(eq(job.getMetadata().getName()), eq(namespace)))
                .thenThrow(new RuntimeException("apiserver unreachable"));

        TaskResponse taskResponse = new TaskResponse();
        WatcherHarness harness = startMonitor(createMonitor(taskRequest, 1L, 2), taskResponse);
        finishWatcher(harness);
        assertEquals(EXIT_CODE_FAILURE, taskResponse.getExitStatusCode());
        verify(k8sUtils, atLeast(2)).getJob(eq(job.getMetadata().getName()), eq(namespace));
    }

    private static final class WatcherHarness {

        private SharedIndexInformer<Job> informer;
        private ResourceEventHandler<Job> handler;
        private Thread thread;
    }
}
