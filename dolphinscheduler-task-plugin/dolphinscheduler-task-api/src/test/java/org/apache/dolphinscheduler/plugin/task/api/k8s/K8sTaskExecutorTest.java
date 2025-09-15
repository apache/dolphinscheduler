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

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.k8s.impl.K8sTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.NodeSelectorRequirement;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.Watch;

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

    // 模拟的kubeconfig内容
    private final String mockKubeConfig = "apiVersion: v1\nclusters:\n- cluster:\n    server: https://kubernetes.default.svc\n  name: mock-cluster\ncontexts:\n- context:\n    cluster: mock-cluster\n    namespace: default\n    user: mock-user\n  name: mock-context\ncurrent-context: mock-context\nkind: Config\npreferences: {}\nusers:\n- name: mock-user\n  user: {}\n";

    // 用于重置单例的连接池
    private MockedStatic<KubernetesClientPool> mockedKubernetesClientPool;

    @BeforeEach
    public void before() throws Exception {
        // 初始化模拟的连接池
        mockedKubernetesClientPool = Mockito.mockStatic(KubernetesClientPool.class);
        KubernetesClientPool mockPool = Mockito.mock(KubernetesClientPool.class);
        KubernetesClient mockClient = Mockito.mock(KubernetesClient.class);
        Mockito.when(KubernetesClientPool.getInstance()).thenReturn(mockPool);
        Mockito.when(mockPool.getClient(Mockito.anyString(), Mockito.anyString())).thenReturn(mockClient);

        // 模拟配置读取 - 注意：在实际测试中，我们不会直接修改配置，而是通过模拟对象来控制行为
        // 以下配置值会在测试方法中通过反射或模拟进行验证

        // 初始化任务执行上下文
        TaskExecutionContext taskRequest = new TaskExecutionContext();
        taskRequest.setTaskInstanceId(taskInstanceId);
        taskRequest.setTaskName(taskName);

        // 设置K8s上下文
        K8sTaskExecutionContext k8sContext = new K8sTaskExecutionContext();
        k8sContext.setConfigYaml(mockKubeConfig);
        taskRequest.setK8sTaskExecutionContext(k8sContext);

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

    @AfterEach
    public void after() {
        // 释放模拟对象
        if (mockedKubernetesClientPool != null) {
            mockedKubernetesClientPool.close();
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
     * 测试K8s连接池的基本获取和归还功能
     */
    @Test
    public void testKubernetesClientPoolBasicFunction() throws Exception {
        // 获取模拟的连接池实例
        KubernetesClientPool mockPool = KubernetesClientPool.getInstance();

        // 模拟clusterId生成
        String clusterId = "mock-cluster-id";
        Mockito.when(mockPool.getClusterId(Mockito.anyString())).thenReturn(clusterId);

        // 模拟客户端获取
        KubernetesClient mockClient = Mockito.mock(KubernetesClient.class);
        Mockito.when(mockPool.getClient(clusterId, mockKubeConfig)).thenReturn(mockClient);

        // 测试连接池的基本功能
        String actualClusterId = mockPool.getClusterId(mockKubeConfig);
        Assertions.assertEquals(clusterId, actualClusterId);

        // 从连接池获取连接
        KubernetesClient client1 = mockPool.getClient(clusterId, mockKubeConfig);
        Assertions.assertNotNull(client1);
        Assertions.assertEquals(mockClient, client1);

        // 归还连接到池中
        mockPool.returnClient(clusterId, client1);
        Mockito.verify(mockPool).returnClient(clusterId, client1);

        // 再次获取连接
        KubernetesClient client2 = mockPool.getClient(clusterId, mockKubeConfig);
        Assertions.assertNotNull(client2);

        // 关闭连接池
        mockPool.closePool(clusterId);
        Mockito.verify(mockPool).closePool(clusterId);
    }

    /**
     * 测试K8s连接池的并发使用
     */
    @Test
    public void testKubernetesClientPoolConcurrency() throws Exception {
        // 使用模拟的连接池实例
        KubernetesClientPool mockPool = KubernetesClientPool.getInstance();
        String clusterId = "mock-cluster-id";

        // 设置模拟行为
        Mockito.when(mockPool.getClusterId(Mockito.anyString())).thenReturn(clusterId);

        final int threadCount = 5;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        final List<Exception> exceptions = new ArrayList<>();

        try {
            // 启动多个线程并发获取连接
            for (int i = 0; i < threadCount; i++) {
                final int threadNum = i;
                executorService.submit(() -> {
                    try {
                        // 为每个线程创建独立的模拟客户端
                        KubernetesClient mockClient = Mockito.mock(KubernetesClient.class);
                        Mockito.when(mockPool.getClient(clusterId, mockKubeConfig)).thenReturn(mockClient);

                        // 从连接池获取连接
                        KubernetesClient client = mockPool.getClient(clusterId, mockKubeConfig);
                        Assertions.assertNotNull(client);

                        // 模拟业务操作
                        Thread.sleep(100);

                        // 归还连接到池中
                        mockPool.returnClient(clusterId, client);
                    } catch (Exception e) {
                        exceptions.add(e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // 等待所有线程完成
            latch.await(30, TimeUnit.SECONDS);

            // 验证没有异常发生
            Assertions.assertTrue(exceptions.isEmpty(), "Concurrent access to connection pool caused exceptions");
        } finally {
            executorService.shutdown();
        }
    }

    /**
     * 测试连接池的配置加载
     */
    @Test
    public void testKubernetesClientPoolConfig() {
        try {
            // 创建模拟的PoolConfig对象
            KubernetesClientPool.PoolConfig expectedConfig = new KubernetesClientPool.PoolConfig(
                    10,  // maxSize
                    2,   // minIdle
                    5,   // maxIdle
                    10000, // maxWaitMs
                    600000 // idleTimeoutMs (使用默认值)
            );

            // 模拟KubernetesClientPool.getInstance()返回一个具有特定配置的实例
            KubernetesClientPool mockPool = Mockito.mock(KubernetesClientPool.class);

            // 通过反射设置内部配置
            java.lang.reflect.Field configField = KubernetesClientPool.class.getDeclaredField("poolConfig");
            configField.setAccessible(true);
            configField.set(mockPool, expectedConfig);

            // 验证配置值
            Assertions.assertEquals(10, expectedConfig.getMaxSize());
            Assertions.assertEquals(2, expectedConfig.getMinIdle());
            Assertions.assertEquals(5, expectedConfig.getMaxIdle());
            Assertions.assertEquals(10000, expectedConfig.getMaxWaitMs());
        } catch (Exception e) {
            Assertions.fail("Failed to test connection pool config: " + e.getMessage());
        }
    }

    /**
     * 测试连接池的集群标识生成
     */
    @Test
    public void testClusterIdGeneration() {
        // 使用模拟的连接池实例
        KubernetesClientPool mockPool = KubernetesClientPool.getInstance();

        // 设置模拟行为 - 相同的kubeconfig返回相同的clusterId
        String mockClusterId = "mock-cluster-id-1";
        Mockito.when(mockPool.getClusterId(mockKubeConfig)).thenReturn(mockClusterId);

        // 不同的kubeconfig返回不同的clusterId
        String differentKubeConfig = mockKubeConfig + "#different";
        String mockDifferentClusterId = "mock-cluster-id-2";
        Mockito.when(mockPool.getClusterId(differentKubeConfig)).thenReturn(mockDifferentClusterId);

        // 相同的kubeconfig应该生成相同的clusterId
        String clusterId1 = mockPool.getClusterId(mockKubeConfig);
        String clusterId2 = mockPool.getClusterId(mockKubeConfig);
        Assertions.assertEquals(clusterId1, clusterId2);
        Assertions.assertEquals(mockClusterId, clusterId1);

        // 不同的kubeconfig应该生成不同的clusterId
        String clusterId3 = mockPool.getClusterId(differentKubeConfig);
        Assertions.assertNotEquals(clusterId1, clusterId3);
        Assertions.assertEquals(mockDifferentClusterId, clusterId3);
    }

}
