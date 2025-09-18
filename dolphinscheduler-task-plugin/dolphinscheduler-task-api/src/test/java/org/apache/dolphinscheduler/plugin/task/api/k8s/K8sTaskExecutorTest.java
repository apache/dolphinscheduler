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

import io.fabric8.kubernetes.api.model.ObjectMeta;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.k8s.impl.K8sTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.K8sUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ProcessUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.Affinity;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.NodeSelectorRequirement;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.LogWatch;


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
    private final String taskAppId = "k8s_task_test_app_1000";
    private Job job;

    private final String mockKubeConfig =
            "apiVersion: v1\nclusters:\n- cluster:\n    server: https://kubernetes.default.svc\n  name: mock-cluster\ncontexts:\n- context:\n    cluster: mock-cluster\n    namespace: default\n    user: mock-user\n  name: mock-context\ncurrent-context: mock-context\nkind: Config\npreferences: {}\nusers:\n- name: mock-user\n  user: {}\n";

    private MockedStatic<KubernetesClientPool> mockedKubernetesClientPool;
    private MockedStatic<K8sUtils> mockedK8sUtils;
    private MockedStatic<ProcessUtils> mockedProcessUtils;
    private MockedStatic<LogUtils> mockedLogUtils;
    private MockedStatic<JSONUtils> mockedJsonUtils;

    private KubernetesClient mockClient;
    private K8sUtils mockK8sUtilsImpl;
    private Watch mockWatch;
    private LogWatch mockLogWatch;

    @BeforeEach
    public void before() throws Exception {
        mockedKubernetesClientPool = Mockito.mockStatic(KubernetesClientPool.class);
        mockedK8sUtils = Mockito.mockStatic(K8sUtils.class);
        mockedProcessUtils = Mockito.mockStatic(ProcessUtils.class);
        mockedLogUtils = Mockito.mockStatic(LogUtils.class);
        mockedJsonUtils = Mockito.mockStatic(JSONUtils.class);

        KubernetesClientPool mockPool = Mockito.mock(KubernetesClientPool.class);
        mockClient = Mockito.mock(KubernetesClient.class);
        mockK8sUtilsImpl = Mockito.mock(K8sUtils.class);
        mockWatch = Mockito.mock(Watch.class);
        mockLogWatch = Mockito.mock(LogWatch.class);

        Mockito.when(KubernetesClientPool.getInstance()).thenReturn(mockPool);
        Mockito.when(mockPool.getClient(Mockito.anyString(), Mockito.anyString())).thenReturn(mockClient);

        TaskExecutionContext taskRequest = new TaskExecutionContext();
        taskRequest.setTaskInstanceId(taskInstanceId);
        taskRequest.setTaskName(taskName);
        taskRequest.setTaskAppId(taskAppId);
        taskRequest.setTaskTimeout(60);
        taskRequest.setTaskTimeoutStrategy(TaskTimeoutStrategy.WARN);

        K8sTaskExecutionContext k8sContext = new K8sTaskExecutionContext();
        k8sContext.setConfigYaml(mockKubeConfig);
        taskRequest.setK8sTaskExecutionContext(k8sContext);


        Mockito.when(JSONUtils.parseObject(Mockito.anyString(), Mockito.eq(K8sTaskMainParameters.class)))
                .thenReturn(k8sTaskMainParameters);

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
        k8sTaskMainParameters.setNodeSelectorRequirements(Collections.singletonList(requirement));
        k8sTaskExecutor.buildK8sJob(k8sTaskMainParameters);
        job = k8sTaskExecutor.getJob();

        // reflect
        Field field = AbstractK8sTaskExecutor.class.getDeclaredField("k8sUtils");
        field.setAccessible(true);
        field.set(k8sTaskExecutor, mockK8sUtilsImpl);

    }

    @AfterEach
    public void after() {
        if (mockedKubernetesClientPool != null) {
            mockedKubernetesClientPool.close();
        }
        if (mockedK8sUtils != null) {
            mockedK8sUtils.close();
        }
        if (mockedProcessUtils != null) {
            mockedProcessUtils.close();
        }
        if (mockedLogUtils != null) {
            mockedLogUtils.close();
        }
        if (mockedJsonUtils != null) {
            mockedJsonUtils.close();
        }
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

    /**
     * Test registerBatchJobWatcher method with successful job completion
     */
    @Test
    public void testRegisterBatchJobWatcherSuccess() throws Exception {
        // mock setTaskStatus
        TaskResponse taskResponse = new TaskResponse();
        String taskInstanceIdStr = String.valueOf(taskInstanceId);

        // Mock Job and its status
        Job mockJob = Mockito.mock(Job.class);
        JobStatus mockJobStatus = Mockito.mock(JobStatus.class);
        ObjectMeta mockMetadata = Mockito.mock(ObjectMeta.class);

        Mockito.when(mockJob.getMetadata()).thenReturn(mockMetadata);
        Mockito.when(mockMetadata.getName()).thenReturn("test-job");
        Mockito.when(mockJob.getStatus()).thenReturn(mockJobStatus);
        // mock getK8sJobStatus
        Mockito.when(mockJobStatus.getSucceeded()).thenReturn(1); // Job succeeded

        // Setup CountDownLatch behavior with a spy
        K8sTaskExecutor spyExecutor = Mockito.spy(k8sTaskExecutor);
        // Create a real Watcher instance that we can control
        final Watcher<Job>[] capturedWatcher = new Watcher[1];
        // Mock k8sUtils.createBatchJobWatcher to capture the watcher
        Mockito.doAnswer(invocation -> {
            capturedWatcher[0] = invocation.getArgument(2);
            return mockWatch;
        }).when(mockK8sUtilsImpl).createBatchJobWatcher(
                Mockito.anyString(), Mockito.anyString(), Mockito.any(Watcher.class));

        // Create a thread to run the method and capture the watcher
        CountDownLatch testLatch = new CountDownLatch(1);
        Thread watcherThread = new Thread(() -> {
            spyExecutor.registerBatchJobWatcher(mockJob, taskInstanceIdStr, taskResponse);
            testLatch.countDown();
        });

        watcherThread.start();
        Thread.sleep(100);
        if (capturedWatcher[0] != null) {
            capturedWatcher[0].eventReceived(Watcher.Action.MODIFIED, mockJob);
        }

        testLatch.await(2, TimeUnit.SECONDS);
        Assertions.assertEquals(EXIT_CODE_SUCCESS, taskResponse.getExitStatusCode());
    }
}
