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

package org.apache.dolphinscheduler.plugin.task.api.k8s;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.k8s.impl.K8sTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.K8sUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.fabric8.kubernetes.api.model.NodeSelectorRequirement;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobStatus;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;

public class K8sTaskExecutorTest {

    private K8sTaskExecutor k8sTaskExecutor = null;
    private K8sTaskMainParameters k8sTaskMainParameters = null;
    private final String image = "ds-dev";
    private final String imagePullPolicy = "IfNotPresent";
    private final String namespace = "namespace";
    private final double minCpuCores = 2;
    private final double minMemorySpace = 10;
    private final int taskInstanceId = 1000;
    private final String taskName = "k8s_task_test";
    private Job job;
    private K8sUtils k8sUtils;

    @BeforeEach
    public void before() throws Exception {
        TaskExecutionContext taskRequest = new TaskExecutionContext();
        taskRequest.setTaskInstanceId(taskInstanceId);
        taskRequest.setTaskName(taskName);
        Map<String, String> labelMap = new HashMap<>();
        labelMap.put("test", "1234");

        NodeSelectorRequirement requirement = new NodeSelectorRequirement();
        requirement.setKey("node-label");
        requirement.setOperator("In");
        requirement.setValues(Arrays.asList("1234", "123456"));
        k8sTaskExecutor = new K8sTaskExecutor(taskRequest);
        k8sUtils = mock(K8sUtils.class);
        injectK8sUtils(k8sTaskExecutor, k8sUtils);
        k8sTaskMainParameters = new K8sTaskMainParameters();
        k8sTaskMainParameters.setImage(image);
        k8sTaskMainParameters.setImagePullPolicy(imagePullPolicy);
        k8sTaskMainParameters.setNamespaceName(namespace);
        k8sTaskMainParameters.setMinCpuCores(minCpuCores);
        k8sTaskMainParameters.setMinMemorySpace(minMemorySpace);
        k8sTaskMainParameters.setCommand("[\"perl\" ,\"-Mbignum=bpi\", \"-wle\", \"print bpi(2000)\"]");
        k8sTaskMainParameters.setLabelMap(labelMap);
        k8sTaskMainParameters.setNodeSelectorRequirements(Arrays.asList(requirement));
        k8sTaskExecutor.buildK8sJob(k8sTaskMainParameters);
        job = k8sTaskExecutor.getJob();
    }

    private void injectK8sUtils(K8sTaskExecutor executor, K8sUtils mockK8sUtils) throws Exception {
        Field field = executor.getClass().getSuperclass().getDeclaredField("k8sUtils");
        field.setAccessible(true);
        field.set(executor, mockK8sUtils);
    }

    private TaskExecutionContext getTaskRequest() throws Exception {
        Field field = k8sTaskExecutor.getClass().getSuperclass().getDeclaredField("taskRequest");
        field.setAccessible(true);
        return (TaskExecutionContext) field.get(k8sTaskExecutor);
    }

    private WatcherHarness startBatchJobWatcher(TaskResponse taskResponse) throws InterruptedException {
        WatcherHarness harness = new WatcherHarness();
        harness.informer = mock(SharedIndexInformer.class);
        CountDownLatch handlerReady = new CountDownLatch(1);
        AtomicReference<ResourceEventHandler<Job>> handlerRef = new AtomicReference<>();
        when(k8sUtils.createBatchJobInformer(eq(job.getMetadata().getName()), eq(namespace), any()))
                .thenAnswer(invocation -> {
                    handlerRef.set(invocation.getArgument(2));
                    handlerReady.countDown();
                    return harness.informer;
                });
        harness.thread = new Thread(() -> k8sTaskExecutor.registerBatchJobWatcher(job,
                String.valueOf(taskInstanceId), taskResponse));
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
    public void testRegisterBatchJobInformerOnUpdateSuccess() throws Exception {
        TaskResponse taskResponse = new TaskResponse();
        WatcherHarness harness = startBatchJobWatcher(taskResponse);
        harness.handler.onUpdate(job, jobWithStatus(1, null));
        finishWatcher(harness);
        assertEquals(EXIT_CODE_SUCCESS, taskResponse.getExitStatusCode());
    }

    @Test
    public void testRegisterBatchJobInformerOnUpdateFailed() throws Exception {
        TaskResponse taskResponse = new TaskResponse();
        WatcherHarness harness = startBatchJobWatcher(taskResponse);
        harness.handler.onUpdate(job, jobWithStatus(null, 1));
        finishWatcher(harness);
        assertEquals(EXIT_CODE_FAILURE, taskResponse.getExitStatusCode());
    }

    @Test
    public void testRegisterBatchJobInformerOnDelete() throws Exception {
        TaskResponse taskResponse = new TaskResponse();
        WatcherHarness harness = startBatchJobWatcher(taskResponse);
        harness.handler.onDelete(job, false);
        finishWatcher(harness);
        assertEquals(EXIT_CODE_FAILURE, taskResponse.getExitStatusCode());
    }

    @Test
    public void testRegisterBatchJobInformerIgnoreOnAdd() throws Exception {
        TaskResponse taskResponse = new TaskResponse();
        WatcherHarness harness = startBatchJobWatcher(taskResponse);
        harness.handler.onAdd(jobWithStatus(1, null));
        Assertions.assertTrue(harness.thread.isAlive());
        harness.handler.onUpdate(job, jobWithStatus(1, null));
        finishWatcher(harness);
        assertEquals(EXIT_CODE_SUCCESS, taskResponse.getExitStatusCode());
    }

    @Test
    public void testRegisterBatchJobInformerIgnoreRunningUpdate() throws Exception {
        TaskResponse taskResponse = new TaskResponse();
        WatcherHarness harness = startBatchJobWatcher(taskResponse);
        harness.handler.onUpdate(job, jobWithStatus(null, null));
        Assertions.assertTrue(harness.thread.isAlive());
        harness.handler.onUpdate(job, jobWithStatus(1, null));
        finishWatcher(harness);
        assertEquals(EXIT_CODE_SUCCESS, taskResponse.getExitStatusCode());
    }

    @Test
    public void testRegisterBatchJobInformerTimeout() throws Exception {
        TaskExecutionContext taskRequest = getTaskRequest();
        taskRequest.setTaskTimeoutStrategy(TaskTimeoutStrategy.FAILED);
        taskRequest.setTaskTimeout(1);

        TaskResponse taskResponse = new TaskResponse();
        WatcherHarness harness = startBatchJobWatcher(taskResponse);
        finishWatcher(harness);
        assertEquals(EXIT_CODE_FAILURE, taskResponse.getExitStatusCode());
    }

    private static final class WatcherHarness {

        private SharedIndexInformer<Job> informer;
        private ResourceEventHandler<Job> handler;
        private Thread thread;
    }

    @Test
    public void testGetK8sJobStatusNormal() {
        JobStatus jobStatus = new JobStatus();
        jobStatus.setSucceeded(1);
        job.setStatus(jobStatus);
        Assertions.assertEquals(0, Integer.compare(0, k8sTaskExecutor.getK8sJobStatus(job)));
    }
    @Test
    public void testSetTaskStatusNormal() {
        int jobStatus = 0;
        TaskResponse taskResponse = new TaskResponse();
        k8sTaskExecutor.setJob(job);
        k8sTaskExecutor.setTaskStatus(jobStatus, String.valueOf(taskInstanceId), taskResponse);
        Assertions.assertEquals(0, taskResponse.getExitStatusCode());
    }
    @Test
    public void testWaitTimeoutNormal() {
        try {
            k8sTaskExecutor.waitTimeout(true);
        } catch (TaskException e) {
            Assertions.assertEquals(e.getMessage(), "K8sTask is timeout");
        }
    }

    @Test
    public void testLoadYamlCorrectly() {
        List<String> expectedCommands = Arrays.asList("perl", "-Mbignum=bpi", "-wle", "print bpi(2000)");
        List<String> actualCommands =
                k8sTaskExecutor.getJob().getSpec().getTemplate().getSpec().getContainers().get(0).getCommand();
        Assertions.assertEquals(expectedCommands, actualCommands);
    }

}
