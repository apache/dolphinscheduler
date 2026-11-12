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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.k8s.impl.K8sTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.K8sUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.NodeSelectorRequirement;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobStatus;

public class K8sTaskExecutorTest {

    private static final Logger logger = LoggerFactory.getLogger(K8sTaskExecutorTest.class);

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
    @BeforeEach
    public void before() {
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

    @Test
    public void testRunFailureDoesNotInterruptWorkerThread() throws Exception {
        TaskExecutionContext taskRequest = new TaskExecutionContext();
        taskRequest.setTaskInstanceId(taskInstanceId);
        taskRequest.setTaskAppId("task-app-id");
        taskRequest.setK8sTaskExecutionContext(new K8sTaskExecutionContext("config", namespace));

        K8sTaskExecutor executor = new K8sTaskExecutor(taskRequest);
        K8sUtils k8sUtils = Mockito.mock(K8sUtils.class);
        Mockito.doThrow(new RuntimeException("k8s unavailable")).when(k8sUtils).buildClient(Mockito.anyString());
        executor.k8sUtils = k8sUtils;

        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean interrupted = new AtomicBoolean();
        Thread workerThread = new Thread(() -> {
            try {
                executor.run("params");
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                interrupted.set(Thread.currentThread().isInterrupted());
            }
        }, "k8s-task-executor-test-worker");
        workerThread.start();
        workerThread.join(10000);

        Assertions.assertTrue(failure.get() instanceof RuntimeException);
        Assertions.assertFalse(interrupted.get(),
                "a failed k8s task must not leave the worker thread interrupted");
    }

    @Test
    public void testRunRestoresInterruptWhenCancellationFails() throws Exception {
        TaskExecutionContext taskRequest = new TaskExecutionContext();
        taskRequest.setTaskInstanceId(taskInstanceId);
        taskRequest.setTaskName(taskName);
        taskRequest.setTaskAppId("task-app-id");
        taskRequest.setK8sTaskExecutionContext(new K8sTaskExecutionContext("config", namespace));

        K8sTaskExecutor executor = new K8sTaskExecutor(taskRequest);
        // cancelApplication only stops the job when it was already submitted
        executor.buildK8sJob(k8sTaskMainParameters);

        K8sUtils k8sUtils = Mockito.mock(K8sUtils.class);
        Mockito.doAnswer(invocation -> {
            throw new InterruptedException("worker is shutting down");
        }).when(k8sUtils).buildClient(Mockito.anyString());
        // make the cancellation itself fail while stopping the job
        Mockito.when(k8sUtils.jobExist(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.doThrow(new RuntimeException("k8s delete failed"))
                .when(k8sUtils).deleteJob(Mockito.anyString(), Mockito.anyString());
        executor.k8sUtils = k8sUtils;

        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean interrupted = new AtomicBoolean();
        Thread workerThread = new Thread(() -> {
            try {
                executor.run(JSONUtils.toJsonString(k8sTaskMainParameters));
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                interrupted.set(Thread.currentThread().isInterrupted());
            }
        }, "k8s-task-executor-cancel-failure-test");
        workerThread.start();
        workerThread.join(10000);

        Assertions.assertTrue(failure.get() instanceof InterruptedException,
                "the original InterruptedException must stay the primary exception");
        Assertions.assertTrue(interrupted.get(),
                "the interrupt flag must be restored even when cancellation fails");
        Assertions.assertEquals(1, failure.get().getSuppressed().length,
                "the cancellation failure must be attached as a suppressed exception");
    }

    @Test
    public void testRunKeepsOriginalFailureWhenCancellationFails() throws Exception {
        TaskExecutionContext taskRequest = new TaskExecutionContext();
        taskRequest.setTaskInstanceId(taskInstanceId);
        taskRequest.setTaskName(taskName);
        taskRequest.setTaskAppId("task-app-id");
        taskRequest.setK8sTaskExecutionContext(new K8sTaskExecutionContext("config", namespace));

        K8sTaskExecutor executor = new K8sTaskExecutor(taskRequest);
        // cancelApplication only stops the job when it was already submitted
        executor.buildK8sJob(k8sTaskMainParameters);

        K8sUtils k8sUtils = Mockito.mock(K8sUtils.class);
        Mockito.doThrow(new RuntimeException("k8s unavailable"))
                .when(k8sUtils).buildClient(Mockito.anyString());
        // make the cancellation itself fail while stopping the job
        Mockito.when(k8sUtils.jobExist(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.doThrow(new RuntimeException("k8s delete failed"))
                .when(k8sUtils).deleteJob(Mockito.anyString(), Mockito.anyString());
        executor.k8sUtils = k8sUtils;

        RuntimeException failure = Assertions.assertThrows(RuntimeException.class,
                () -> executor.run(JSONUtils.toJsonString(k8sTaskMainParameters)));

        Assertions.assertEquals("k8s unavailable", failure.getMessage(),
                "the original failure must stay the primary exception");
        Assertions.assertEquals(1, failure.getSuppressed().length,
                "the cancellation failure must be attached as a suppressed exception");
    }

}
