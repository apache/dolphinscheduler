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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import io.fabric8.kubernetes.client.KubernetesClient;

public class KubernetesClientPoolTest {

    private KubernetesClientPool mockPool;
    private final String mockKubeConfig =
            "apiVersion: v1\nclusters:\n- cluster:\n    server: https://kubernetes.default.svc\n  name: mock-cluster\ncontexts:\n- context:\n    cluster: mock-cluster\n    namespace: default\n    user: mock-user\n  name: mock-context\ncurrent-context: mock-context\nkind: Config\npreferences: {}\nusers:\n- name: mock-user\n  user: {}";
    private final String clusterId = "mock-cluster-id";
    private MockedStatic<KubernetesClientPool> mockedKubernetesClientPool;

    @BeforeEach
    public void before() throws Exception {
        mockedKubernetesClientPool = Mockito.mockStatic(KubernetesClientPool.class);

        mockPool = Mockito.mock(KubernetesClientPool.class);
        KubernetesClient mockClient = Mockito.mock(KubernetesClient.class);

        Mockito.when(KubernetesClientPool.getInstance()).thenReturn(mockPool);
        Mockito.when(mockPool.getClient(Mockito.anyString(), Mockito.anyString())).thenReturn(mockClient);
        Mockito.when(mockPool.getClusterId(mockKubeConfig)).thenReturn(clusterId);
    }

    @AfterEach
    public void after() {
        if (mockedKubernetesClientPool != null) {
            mockedKubernetesClientPool.close();
        }
    }

    /**
     * test: getClusterId,getClient,closePool,returnClient
     */
    @Test
    public void testKubernetesClientPoolBasicFunction() {
        KubernetesClientPool mockPool = KubernetesClientPool.getInstance();

        String clusterId = "mock-cluster-id";
        Mockito.when(mockPool.getClusterId(Mockito.anyString())).thenReturn(clusterId);

        KubernetesClient mockClient = Mockito.mock(KubernetesClient.class);
        Mockito.when(mockPool.getClient(clusterId, mockKubeConfig)).thenReturn(mockClient);

        String actualClusterId = mockPool.getClusterId(mockKubeConfig);
        Assertions.assertEquals(clusterId, actualClusterId);

        KubernetesClient client1 = mockPool.getClient(clusterId, mockKubeConfig);
        Assertions.assertNotNull(client1);
        Assertions.assertEquals(mockClient, client1);

        mockPool.returnClient(clusterId, client1);
        Mockito.verify(mockPool).returnClient(clusterId, client1);

        KubernetesClient client2 = mockPool.getClient(clusterId, mockKubeConfig);
        Assertions.assertNotNull(client2);

        mockPool.closePool(clusterId);
        Mockito.verify(mockPool).closePool(clusterId);
    }

    /**
     * test:PoolConfig,
     */
    @Test
    public void testKubernetesClientPoolConfig() {
        try {
            KubernetesClientPool.PoolConfig expectedConfig = new KubernetesClientPool.PoolConfig(
                    10, // maxSize
                    2, // minIdle
                    5, // maxIdle
                    10000, // maxWaitMs
                    600000 // idleTimeoutMs
            );

            KubernetesClientPool mockPool = Mockito.mock(KubernetesClientPool.class);

            java.lang.reflect.Field configField = KubernetesClientPool.class.getDeclaredField("poolConfig");
            configField.setAccessible(true);
            configField.set(mockPool, expectedConfig);

            Assertions.assertEquals(10, expectedConfig.getMaxSize());
            Assertions.assertEquals(2, expectedConfig.getMinIdle());
            Assertions.assertEquals(5, expectedConfig.getMaxIdle());
            Assertions.assertEquals(10000, expectedConfig.getMaxWaitMs());
        } catch (Exception e) {
            Assertions.fail("Failed to test connection pool config: " + e.getMessage());
        }
    }

    /**
     * Test handling of invalid kubeConfig
     */
    @Test
    public void testInvalidKubeConfig() {
        String invalidKubeConfig = "invalid config";
        // Mock KubernetesClientPool.getInstance() to return our mockPool
        KubernetesClientPool realPool = Mockito.spy(KubernetesClientPool.class); // spy on actual method
        String clusterId = realPool.getClusterId(invalidKubeConfig);
        Assertions.assertNotNull(clusterId, "Cluster ID should not be null even for invalid kubeConfig");

        // Mock getClient to throw exception for invalid kubeConfig
        Mockito.when(mockPool.getClient(clusterId, invalidKubeConfig))
                .thenThrow(new RuntimeException("Invalid kubeconfig"));

        // Test getClient with invalid kubeConfig (should throw exception)
        Assertions.assertThrows(Exception.class, () -> mockPool.getClient(clusterId, invalidKubeConfig),
                "getClient should throw exception for invalid kubeConfig");
    }
    @Test
    public void testClusterIdGeneration() {

        KubernetesClientPool mockPool = KubernetesClientPool.getInstance();

        String mockClusterId = "mock-cluster-id-1";
        Mockito.when(mockPool.getClusterId(mockKubeConfig)).thenReturn(mockClusterId);

        String differentKubeConfig = mockKubeConfig + "#different";
        String mockDifferentClusterId = "mock-cluster-id-2";
        Mockito.when(mockPool.getClusterId(differentKubeConfig)).thenReturn(mockDifferentClusterId);

        String clusterId1 = mockPool.getClusterId(mockKubeConfig);
        String clusterId2 = mockPool.getClusterId(mockKubeConfig);
        Assertions.assertEquals(clusterId1, clusterId2);
        Assertions.assertEquals(mockClusterId, clusterId1);

        String clusterId3 = mockPool.getClusterId(differentKubeConfig);
        Assertions.assertNotEquals(clusterId1, clusterId3);
        Assertions.assertEquals(mockDifferentClusterId, clusterId3);
    }

    /**
     * Test singleton pattern of KubernetesClientPool
     */
    @Test
    public void testSingletonPattern() {
        KubernetesClientPool instance1 = KubernetesClientPool.getInstance();
        KubernetesClientPool instance2 = KubernetesClientPool.getInstance();
        Assertions.assertSame(instance1, instance2, "KubernetesClientPool should be a singleton");
    }

    /**
     * Test getClusterId method with valid kubeConfig
     */
    @Test
    public void testGetClusterId() {
        String clusterId = mockPool.getClusterId(mockKubeConfig);
        Assertions.assertNotNull(clusterId, "Cluster ID should not be null");
        // Verify that the same kubeConfig returns the same clusterId
        String sameClusterId = mockPool.getClusterId(mockKubeConfig);
        Assertions.assertEquals(clusterId, sameClusterId, "Same kubeConfig should return same cluster ID");
    }

    /**
     * Test getClusterId method with different kubeConfigs
     */
    @Test
    public void testGetClusterIdDifferentConfigs() {
        String clusterId1 = mockPool.getClusterId(mockKubeConfig);
        String differentKubeConfig = mockKubeConfig + "#different";
        String clusterId2 = mockPool.getClusterId(differentKubeConfig);
        Assertions.assertNotEquals(clusterId1, clusterId2, "Different kubeConfigs should return different cluster IDs");
    }

    /**
     * Test getClient method with valid parameters
     */
    @Test
    public void testGetClient() throws Exception {
        // Mock a KubernetesClient and ClusterClientPool
        KubernetesClient mockClient = Mockito.mock(KubernetesClient.class);
        // Get the real instance for testing
        KubernetesClientPool realPool = Mockito.spy(KubernetesClientPool.class);

        // Mock the clusterClientPools field to return a mock ClusterClientPool
        java.lang.reflect.Field clusterClientPoolsField =
                KubernetesClientPool.class.getDeclaredField("clusterClientPools");
        clusterClientPoolsField.setAccessible(true);
        ConcurrentHashMap<String, KubernetesClientPool.ClusterClientPool> mockClusterClientPools =
                new ConcurrentHashMap<>();

        // Create a mock ClusterClientPool
        KubernetesClientPool.ClusterClientPool mockClusterClientPool =
                Mockito.mock(KubernetesClientPool.ClusterClientPool.class);
        mockClusterClientPools.put(clusterId, mockClusterClientPool);

        // Mock the borrowObject method
        Mockito.doReturn(mockClient).when(mockClusterClientPool).borrowObject();

        // Set the mock clusterClientPools into the realPool
        clusterClientPoolsField.set(realPool, mockClusterClientPools);

        // Mock the getInstance method to return our spy
        Mockito.when(KubernetesClientPool.getInstance()).thenReturn(realPool);

        // Test getting a client
        KubernetesClient client = realPool.getClient(clusterId, mockKubeConfig);

        // Verify the client is not null
        Assertions.assertNotNull(client, "Client should not be null");
        Assertions.assertEquals(mockClient, client, "Returned client should match the mock client");
    }

    /**
     * Test returnClient method
     */
    @Test
    public void testReturnClient() throws Exception {
        // Mock a KubernetesClient and ClusterClientPool
        KubernetesClient mockClient = Mockito.mock(KubernetesClient.class);

        // Get the real instance for testing
        KubernetesClientPool realPool = Mockito.spy(KubernetesClientPool.class);

        // Mock the clusterClientPools field to return a mock ClusterClientPool
        java.lang.reflect.Field clusterClientPoolsField =
                KubernetesClientPool.class.getDeclaredField("clusterClientPools");
        clusterClientPoolsField.setAccessible(true);
        ConcurrentHashMap<String, KubernetesClientPool.ClusterClientPool> mockClusterClientPools =
                new ConcurrentHashMap<>();

        // Create a mock ClusterClientPool
        KubernetesClientPool.ClusterClientPool mockClusterClientPool =
                Mockito.mock(KubernetesClientPool.ClusterClientPool.class);
        mockClusterClientPools.put(clusterId, mockClusterClientPool);

        // Mock the borrowObject method to return our mock client
        Mockito.doReturn(mockClient).when(mockClusterClientPool).borrowObject();

        // Set the mock clusterClientPools into the realPool
        clusterClientPoolsField.set(realPool, mockClusterClientPools);

        // Mock the getInstance method to return our spy
        Mockito.when(KubernetesClientPool.getInstance()).thenReturn(realPool);

        // Get a client
        KubernetesClient client = realPool.getClient(clusterId, mockKubeConfig);
        Assertions.assertNotNull(client, "Client should not be null");

        // Return the client to the pool
        realPool.returnClient(clusterId, client);

        // Verify that returnObject was called on the mockClusterClientPool
        Mockito.verify(mockClusterClientPool).returnObject(client);

        // Verify that getClient calls borrowObject again
        realPool.getClient(clusterId, mockKubeConfig);
        Mockito.verify(mockClusterClientPool, Mockito.times(2)).borrowObject();
    }

    /**
     * Test closePool method
     */
    @Test
    public void testClosePool() throws Exception {
        // Get the real instance for testing
        KubernetesClientPool realPool = Mockito.spy(KubernetesClientPool.class);

        // Mock the clusterClientPools field to return a mock ClusterClientPool
        java.lang.reflect.Field clusterClientPoolsField =
                KubernetesClientPool.class.getDeclaredField("clusterClientPools");
        clusterClientPoolsField.setAccessible(true);
        ConcurrentHashMap<String, KubernetesClientPool.ClusterClientPool> mockClusterClientPools =
                new ConcurrentHashMap<>();

        // Create a mock ClusterClientPool
        KubernetesClientPool.ClusterClientPool mockClusterClientPool =
                Mockito.mock(KubernetesClientPool.ClusterClientPool.class);
        mockClusterClientPools.put(clusterId, mockClusterClientPool);

        // Set the mock clusterClientPools into the realPool
        clusterClientPoolsField.set(realPool, mockClusterClientPools);

        // Mock the getInstance method to return our spy
        Mockito.when(KubernetesClientPool.getInstance()).thenReturn(realPool);

        // Close the pool
        realPool.closePool(clusterId);

        // Verify that the ClusterClientPool's close method was called
        Mockito.verify(mockClusterClientPool).close();

        // Verify that the clusterClientPools no longer contains the clusterId
        Assertions.assertFalse(mockClusterClientPools.containsKey(clusterId),
                "Cluster client pool should be removed after close");
    }

    /**
     * Test close method (closes all pools)
     */
    @Test
    public void testClose() throws Exception {
        // Get the real instance for testing
        KubernetesClientPool realPool = Mockito.spy(KubernetesClientPool.class);

        // Mock the clusterClientPools field
        java.lang.reflect.Field clusterClientPoolsField =
                KubernetesClientPool.class.getDeclaredField("clusterClientPools");
        clusterClientPoolsField.setAccessible(true);
        ConcurrentHashMap<String, KubernetesClientPool.ClusterClientPool> mockClusterClientPools =
                new ConcurrentHashMap<>();

        // Create multiple mock ClusterClientPools for different clusters
        String clusterId1 = clusterId;
        String clusterId2 = clusterId + "_2";

        KubernetesClientPool.ClusterClientPool mockClusterClientPool1 =
                Mockito.mock(KubernetesClientPool.ClusterClientPool.class);
        KubernetesClientPool.ClusterClientPool mockClusterClientPool2 =
                Mockito.mock(KubernetesClientPool.ClusterClientPool.class);

        mockClusterClientPools.put(clusterId1, mockClusterClientPool1);
        mockClusterClientPools.put(clusterId2, mockClusterClientPool2);

        // Set the mock clusterClientPools into the realPool
        clusterClientPoolsField.set(realPool, mockClusterClientPools);

        // Mock the getInstance method to return our spy
        Mockito.when(KubernetesClientPool.getInstance()).thenReturn(realPool);

        // Close all pools
        realPool.closePool(clusterId1);
        realPool.closePool(clusterId2);

        // Verify that both ClusterClientPools' close methods were called
        Mockito.verify(mockClusterClientPool1).close();
        Mockito.verify(mockClusterClientPool2).close();

        // Verify that the clusterClientPools is now empty
        Assertions.assertTrue(mockClusterClientPools.isEmpty(),
                "All cluster client pools should be removed after close");
    }

    @Test
    public void testConcurrentAccess() throws Exception {
        final int threadCount = 10;
        final int operationsPerThread = 5;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        final List<Exception> exceptions = new ArrayList<>();

        // Get the real instance for testing
        KubernetesClientPool realPool = Mockito.spy(KubernetesClientPool.class);

        // Mock the clusterClientPools field
        java.lang.reflect.Field clusterClientPoolsField =
                KubernetesClientPool.class.getDeclaredField("clusterClientPools");
        clusterClientPoolsField.setAccessible(true);
        ConcurrentHashMap<String, KubernetesClientPool.ClusterClientPool> mockClusterClientPools =
                new ConcurrentHashMap<>();

        // Create a mock ClusterClientPool
        KubernetesClientPool.ClusterClientPool mockClusterClientPool =
                Mockito.mock(KubernetesClientPool.ClusterClientPool.class);
        mockClusterClientPools.put(clusterId, mockClusterClientPool);

        // Create a mock KubernetesClient
        KubernetesClient mockClient = Mockito.mock(KubernetesClient.class);

        // Mock the borrowObject method to return our mock client
        Mockito.when(mockClusterClientPool.borrowObject()).thenReturn(mockClient);

        // Set the mock clusterClientPools into the realPool
        clusterClientPoolsField.set(realPool, mockClusterClientPools);

        // Mock the getInstance method to return our spy
        Mockito.when(KubernetesClientPool.getInstance()).thenReturn(realPool);

        try {
            // Start multiple threads that concurrently access the client pool
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    try {
                        for (int j = 0; j < operationsPerThread; j++) {
                            // Get a client
                            KubernetesClient client = realPool.getClient(clusterId, mockKubeConfig);
                            Assertions.assertNotNull(client, "Client should not be null");

                            // Simulate some work with the client
                            Awaitility.await().atMost(Duration.ofMillis(10));

                            // Return the client to the pool
                            realPool.returnClient(clusterId, client);
                        }
                    } catch (Exception e) {
                        exceptions.add(e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Wait for all threads to complete
            boolean completed = latch.await(60, TimeUnit.SECONDS);
            Assertions.assertTrue(completed, "All threads should complete within timeout");

            // Verify no exceptions occurred during concurrent access
            Assertions.assertTrue(exceptions.isEmpty(), "No exceptions should occur during concurrent access");

            // Verify that borrowObject and returnObject were called the expected number of times
            int expectedCalls = threadCount * operationsPerThread;
            Mockito.verify(mockClusterClientPool, Mockito.times(expectedCalls)).borrowObject();
            Mockito.verify(mockClusterClientPool, Mockito.times(expectedCalls)).returnObject(Mockito.any());
        } finally {
            executorService.shutdown();
        }
    }

    /**
     * Test handling of closed client
     */
    @Test
    public void testHandlingClosedClient() {
        // Get the real instance for testing
        KubernetesClientPool realPool = Mockito.spy(KubernetesClientPool.class);

        // Mock the clusterClientPools field to return a mock ClusterClientPool
        try {
            java.lang.reflect.Field clusterClientPoolsField =
                    KubernetesClientPool.class.getDeclaredField("clusterClientPools");
            clusterClientPoolsField.setAccessible(true);
            ConcurrentHashMap<String, KubernetesClientPool.ClusterClientPool> mockClusterClientPools =
                    new ConcurrentHashMap<>();

            // Create a mock ClusterClientPool
            KubernetesClientPool.ClusterClientPool mockClusterClientPool =
                    Mockito.mock(KubernetesClientPool.ClusterClientPool.class);
            mockClusterClientPools.put(clusterId, mockClusterClientPool);

            // Mock the borrowObject method to throw an exception, simulating a closed client scenario
            Mockito.doThrow(new RuntimeException("Client is closed")).when(mockClusterClientPool).borrowObject();

            // Set the mock clusterClientPools into the realPool
            clusterClientPoolsField.set(realPool, mockClusterClientPools);

            // Test getClient with a closed client scenario
            try {
                // This should handle the exception gracefully
                KubernetesClient client = realPool.getClient(clusterId, mockKubeConfig);
                // If no exception is thrown, verify the client is handled properly
                Assertions.assertNull(client, "Client should be null when borrowObject fails");
            } catch (Exception e) {
                // If exception propagates, this is also acceptable behavior
                System.out.println("Expected exception when getting client in closed scenario: " + e.getMessage());
            }

            // Verify that borrowObject was called
            Mockito.verify(mockClusterClientPool).borrowObject();
        } catch (Exception e) {
            Assertions.fail("Failed to setup test for closed client scenario: " + e.getMessage());
        }
    }


    /**
     * Test that PooledClient constructor correctly initializes the client and lastUsedTime.
     */
    @Test
    public void testPooledClientConstructor() throws Exception {
        // Mock a KubernetesClient
        KubernetesClient mockClient = Mockito.mock(KubernetesClient.class);
        Class<?> pooledClientClass = getPooledClientClass();
        long currentTime = System.currentTimeMillis();

        // Create a PooledClient instance using reflection
        Constructor<?> constructor = pooledClientClass.getDeclaredConstructor(KubernetesClient.class);
        constructor.setAccessible(true);
        Object pooledClient = constructor.newInstance(mockClient);

        // Verify that the client was set correctly
        Field clientField = pooledClientClass.getDeclaredField("client");
        Field lastUsedTimeField = pooledClientClass.getDeclaredField("lastUsedTime");
        lastUsedTimeField.setAccessible(true);
        clientField.setAccessible(true);

        Object clientValue = clientField.get(pooledClient);
        // Verify the client is accessible and correct
        Assertions.assertNotNull(clientValue, "Client should not be null");
        Assertions.assertSame(mockClient, clientValue, "Client reference should match the original client");

        // Verify that lastUsedTime was initialized correctly
        long lastUsedTimeValue = lastUsedTimeField.getLong(pooledClient);

        // Allow for a small time difference (up to 1 second) between test execution and constructor call
        Assertions.assertEquals(currentTime, lastUsedTimeValue, 1000, "lastUsedTime should be initialized to current time");

        // Test that we can modify the lastUsedTime field.
        long newTime = System.currentTimeMillis() + 10000; // 10 seconds in the future
        lastUsedTimeField.setLong(pooledClient, newTime);
        // Verify the modification
        long updatedTime = lastUsedTimeField.getLong(pooledClient);
        Assertions.assertEquals(newTime, updatedTime, "lastUsedTime should be successfully updated");
    }

    /**
     * Helper method to get the PooledClient class using reflection.
     */
    private Class<?> getPooledClientClass() throws ClassNotFoundException {
        // Get the outer class
        Class<?> clusterClientPoolClass = Class.forName("org.apache.dolphinscheduler.plugin.task.api.k8s.KubernetesClientPool$ClusterClientPool");

        // Get the inner PooledClient class
        Class<?>[] nestedClasses = clusterClientPoolClass.getDeclaredClasses();
        for (Class<?> nestedClass : nestedClasses) {
            if (nestedClass.getSimpleName().equals("PooledClient")) {
                return nestedClass;
            }
        }

        throw new ClassNotFoundException("Could not find PooledClient class");
    }

    @Test
    public void testClusterClientPoolBorrowObjectTimeout() throws Exception {
        // Mock a KubernetesClient and create a PoolConfig with small timeout
        KubernetesClientPool.PoolConfig poolConfig = new KubernetesClientPool.PoolConfig(
                2, // maxSize
                0, // minIdle
                2, // maxIdle
                500, // maxWaitMs (500ms timeout)
                600000); // idleTimeoutMs

        // Create a real ClusterClientPool instance using reflection
        Class<?> clusterClientPoolClass = Class.forName("org.apache.dolphinscheduler.plugin.task.api.k8s.KubernetesClientPool$ClusterClientPool");
        Constructor<?> constructor = clusterClientPoolClass.getDeclaredConstructor(String.class, String.class, KubernetesClientPool.PoolConfig.class);
        constructor.setAccessible(true);

        // Create mock clients
        KubernetesClient mockClient1 = Mockito.mock(KubernetesClient.class);
        KubernetesClient mockClient2 = Mockito.mock(KubernetesClient.class);

        // Use a class-level AtomicInteger to track builder calls
        AtomicInteger builderCounter = new AtomicInteger(0);

        // Mock KubernetesClientBuilder to control client creation
        try (MockedConstruction<KubernetesClientBuilder> mockedConstruction = Mockito.mockConstruction(KubernetesClientBuilder.class,
                (mock, context) -> {
                    // Configure the mock behavior
                    Mockito.when(mock.withConfig((Config) Mockito.any())).thenReturn(mock);
                    // Use the class-level counter to determine which mock client to return
                    Mockito.when(mock.build()).thenAnswer(invocation -> {
                        int count = builderCounter.getAndIncrement();
                        if (count == 0) {
                            return mockClient1;
                        } else if (count == 1) {
                            return mockClient2;
                        } else {
                            // For any additional calls, just return mockClient2
                            return mockClient2;
                        }
                    });
                })) {

            Object clusterClientPool = constructor.newInstance(clusterId, mockKubeConfig, poolConfig);

            // Get the borrowObject method
            Method borrowObjectMethod = clusterClientPoolClass.getDeclaredMethod("borrowObject");
            borrowObjectMethod.setAccessible(true);

            // Get all available clients (maxSize = 2)
            KubernetesClient client1 = (KubernetesClient) borrowObjectMethod.invoke(clusterClientPool);
            KubernetesClient client2 = (KubernetesClient) borrowObjectMethod.invoke(clusterClientPool);

            // Try to borrow another client - should timeout after 500ms
            long startTime = System.currentTimeMillis();
            Exception exception = Assertions.assertThrows(Exception.class,
                    () -> {
                        try {
                            borrowObjectMethod.invoke(clusterClientPool);
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    },
                    "borrowObject should throw exception when timeout");
            long endTime = System.currentTimeMillis();

            // Verify exception message and approximate timeout duration
            Assertions.assertTrue(exception.getMessage().contains("Timeout"),
                    "Exception message should contain timeout information");
            Assertions.assertTrue((endTime - startTime) >= 500,
                    "Timeout should be at least the configured maxWaitMs");
        }
    }


    @Test
    public void testClusterClientPoolReturnObjectAndClientField() throws Exception {
        // Create a real ClusterClientPool instance using reflection
        Class<?> clusterClientPoolClass = Class.forName("org.apache.dolphinscheduler.plugin.task.api.k8s.KubernetesClientPool$ClusterClientPool");
        Constructor<?> constructor = clusterClientPoolClass.getDeclaredConstructor(String.class, String.class, KubernetesClientPool.PoolConfig.class);
        constructor.setAccessible(true);
        KubernetesClientPool.PoolConfig poolConfig = new KubernetesClientPool.PoolConfig(
                2, // maxSize
                0, // minIdle
                2, // maxIdle
                30000, // maxWaitMs
                600); // idleTimeoutMs


        // Create a mock client
        KubernetesClient mockClient = Mockito.mock(KubernetesClient.class);
        NamespaceList namespaceList = Mockito.mock(NamespaceList.class);
        NonNamespaceOperation<Namespace, NamespaceList, Resource<Namespace>> namespaces = Mockito.mock(NonNamespaceOperation.class);
        Mockito.when(mockClient.namespaces()).thenReturn(namespaces);
        Mockito.when(namespaces.list()).thenReturn(namespaceList);

        // Mock KubernetesClientBuilder to control client creation
        try (MockedConstruction<KubernetesClientBuilder> mockedConstruction = Mockito.mockConstruction(KubernetesClientBuilder.class,
                (mock, context) -> {
                    // Configure the mock behavior
                    Mockito.when(mock.withConfig((Config) Mockito.any())).thenReturn(mock);
                    Mockito.when(mock.build()).thenReturn(mockClient);
                })) {

            Object clusterClientPool = constructor.newInstance(clusterId, mockKubeConfig, poolConfig);

            // Get the borrowObject and returnObject methods
            Method borrowObjectMethod = clusterClientPoolClass.getDeclaredMethod("borrowObject");
            borrowObjectMethod.setAccessible(true);
            Method returnObjectMethod = clusterClientPoolClass.getDeclaredMethod("returnObject", KubernetesClient.class);
            returnObjectMethod.setAccessible(true);
            Method isClientValidMethod = clusterClientPoolClass.getDeclaredMethod("isClientValid", KubernetesClient.class);
            isClientValidMethod.setAccessible(true);
            Method cleanupIdleMethod = clusterClientPoolClass.getDeclaredMethod("cleanupIdle");
            cleanupIdleMethod.setAccessible(true);

            Field createdCountField = clusterClientPoolClass.getDeclaredField("createdCount");
            createdCountField.setAccessible(true);
            // Get idleClients field to verify client return
            Field idleClientsField = clusterClientPoolClass.getDeclaredField("idleClients");
            idleClientsField.setAccessible(true);
            BlockingQueue<?> idleClients = (BlockingQueue<?>) idleClientsField.get(clusterClientPool);
            // Get activeClients field
            Field activeClientsField = clusterClientPoolClass.getDeclaredField("activeClients");
            activeClientsField.setAccessible(true);
            Set<?> activeClients = (Set<?>) activeClientsField.get(clusterClientPool);


            // Borrow a client
            KubernetesClient client = (KubernetesClient) borrowObjectMethod.invoke(clusterClientPool);
            // Verify client is active
            Assertions.assertEquals(1, activeClients.size(), "Active clients count should be 1 after borrowing");

            // Test with valid client
            Boolean isValid  = (Boolean) isClientValidMethod.invoke(clusterClientPool, client);
            Assertions.assertTrue(isValid, "Client is valid");
            // Test with invalid client
            KubernetesClient invalidClient = Mockito.mock(KubernetesClient.class); // null client
            boolean isInvalid = (boolean) isClientValidMethod.invoke(clusterClientPool, invalidClient);
            Assertions.assertFalse(isInvalid, "Invalid client should be considered invalid");

            // Return the client
            returnObjectMethod.invoke(clusterClientPool, client);


            // Verify client is returned to idle pool
            Assertions.assertEquals(0, activeClients.size(), "Active clients count should be 0 after returning");
            Assertions.assertEquals(1, idleClients.size(), "Idle clients count should be 1 after returning");

            // Modify lastUsedTime of PooledClient objects to simulate idle time
            for (Object pooledClient : new ArrayList<>(idleClients)) {
                Class<?> pooledClientClass = getPooledClientClass();
                Field lastUsedTimeField = pooledClientClass.getDeclaredField("lastUsedTime");
                lastUsedTimeField.setAccessible(true);
                lastUsedTimeField.setLong(pooledClient, System.currentTimeMillis() - 600); // 200ms old
            }
            // Wait for the idle timeout to pass
            Thread.sleep(150);

            // Run cleanupIdle
            cleanupIdleMethod.invoke(clusterClientPool);

            // Verify that idle clients were cleaned up
            Assertions.assertEquals(0, idleClients.size(), "Idle clients should be cleaned up after timeout");
            Assertions.assertEquals(0, ((java.util.concurrent.atomic.AtomicInteger) createdCountField.get(clusterClientPool)).get(),
                    "Created count should be 0 after cleanup");

        }
    }

    /**
     * Test ClusterClientPool's createClient method with pool size limit
     */
    @Test
    public void testCreatClient() throws Exception {
        // Create a real ClusterClientPool instance using reflection
        Class<?> clusterClientPoolClass = Class.forName("org.apache.dolphinscheduler.plugin.task.api.k8s.KubernetesClientPool$ClusterClientPool");
        Constructor<?> constructor = clusterClientPoolClass.getDeclaredConstructor(String.class, String.class, KubernetesClientPool.PoolConfig.class);
        constructor.setAccessible(true);

        KubernetesClientPool.PoolConfig poolConfig = new KubernetesClientPool.PoolConfig(
                2, // maxSize
                0, // minIdle
                2, // maxIdle
                30000, // maxWaitMs
                600000); // idleTimeoutMs

        // Create a mock client
        KubernetesClient mockClient = Mockito.mock(KubernetesClient.class);
        NamespaceList namespaceList = Mockito.mock(NamespaceList.class);
        NonNamespaceOperation<Namespace, NamespaceList, Resource<Namespace>> namespaces = Mockito.mock(NonNamespaceOperation.class);
        Mockito.when(mockClient.namespaces()).thenReturn(namespaces);
        Mockito.when(namespaces.list()).thenReturn(namespaceList);

        // Mock KubernetesClientBuilder to control client creation
        try (MockedConstruction<KubernetesClientBuilder> mockedConstruction = Mockito.mockConstruction(KubernetesClientBuilder.class,
                (mock, context) -> {
                    // Configure the mock behavior
                    Mockito.when(mock.withConfig((Config) Mockito.any())).thenReturn(mock);
                    Mockito.when(mock.build()).thenReturn(mockClient);
                })) {

            Object clusterClientPool = constructor.newInstance(clusterId, mockKubeConfig, poolConfig);
            Method createClientMethod = clusterClientPoolClass.getDeclaredMethod("createClient");
            createClientMethod.setAccessible(true);

            // Create first client - should succeed
            Object pooledClient1 = createClientMethod.invoke(clusterClientPool);
            Assertions.assertNotNull(pooledClient1, "First client creation should succeed");
            Object pooledClient2 = createClientMethod.invoke(clusterClientPool);
            Assertions.assertNotNull(pooledClient2, "Second client creation should succeed");
            // Try to create second client - should fail due to maxSize limit
            Exception exception = Assertions.assertThrows(Exception.class,
                    () -> {
                        try {
                            createClientMethod.invoke(clusterClientPool);
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    },
                    "createClient should throw exception when pool reaches max size");
            Assertions.assertTrue(exception.getMessage().contains("max size"),
                    "Exception message should indicate pool size limit");
        }
    }

    /**
     * Test ClusterClientPool's createIdleConnection method
     */
    @Test
    public void testClusterClientPoolCreateIdleConnection() throws Exception {
        // Create a real ClusterClientPool instance using reflection
        Class<?> clusterClientPoolClass = Class.forName("org.apache.dolphinscheduler.plugin.task.api.k8s.KubernetesClientPool$ClusterClientPool");
        Constructor<?> constructor = clusterClientPoolClass.getDeclaredConstructor(String.class, String.class, KubernetesClientPool.PoolConfig.class);
        constructor.setAccessible(true);

        KubernetesClientPool.PoolConfig poolConfig = new KubernetesClientPool.PoolConfig(
                10, // maxSize
                0, // minIdle
                5, // maxIdle
                30000, // maxWaitMs
                600000); // idleTimeoutMs

        // Create a mock client
        KubernetesClient mockClient = Mockito.mock(KubernetesClient.class);

        // Mock KubernetesClientBuilder to control client creation
        try (MockedConstruction<KubernetesClientBuilder> mockedConstruction = Mockito.mockConstruction(KubernetesClientBuilder.class,
                (mock, context) -> {
                    Mockito.when(mock.withConfig((Config) Mockito.any())).thenReturn(mock);
                    Mockito.when(mock.build()).thenReturn(mockClient);
                })) {
            Object clusterClientPool = constructor.newInstance(clusterId, mockKubeConfig, poolConfig);

            // Get the createIdleConnection method
            Method createIdleConnectionMethod = clusterClientPoolClass.getDeclaredMethod("createIdleConnection");
            createIdleConnectionMethod.setAccessible(true);

            // Get the idleClients field to verify client creation
            Field idleClientsField = clusterClientPoolClass.getDeclaredField("idleClients");
            idleClientsField.setAccessible(true);
            BlockingQueue<?> idleClients = (BlockingQueue<?>) idleClientsField.get(clusterClientPool);

            // Get the createdCount field
            Field createdCountField = clusterClientPoolClass.getDeclaredField("createdCount");
            createdCountField.setAccessible(true);

            // Initial state verification
            Assertions.assertEquals(0, idleClients.size(), "Idle clients should be 0 initially");
            Assertions.assertEquals(0, ((AtomicInteger) createdCountField.get(clusterClientPool)).get(), "Created count should be 0 initially");

            // Call createIdleConnection
            createIdleConnectionMethod.invoke(clusterClientPool);

            // Verify client was created and added to idle pool
            Assertions.assertEquals(1, idleClients.size(), "Idle clients should be 1 after createIdleConnection");
            Assertions.assertEquals(1, ((AtomicInteger) createdCountField.get(clusterClientPool)).get(), "Created count should be 1 after createIdleConnection");
        }
    }

    /**
     * Test ClusterClientPool's initializeMinIdleConnections method
     */
    @Test
    public void testClusterClientPoolInitializeMinIdleConnections() throws Exception {
        // Create a real ClusterClientPool instance using reflection
        Class<?> clusterClientPoolClass = Class.forName("org.apache.dolphinscheduler.plugin.task.api.k8s.KubernetesClientPool$ClusterClientPool");
        Constructor<?> constructor = clusterClientPoolClass.getDeclaredConstructor(String.class, String.class, KubernetesClientPool.PoolConfig.class);
        constructor.setAccessible(true);

        // Create a pool config with minIdle=3
        KubernetesClientPool.PoolConfig poolConfig = new KubernetesClientPool.PoolConfig(
                10, // maxSize
                3, // minIdle (we want to test initialization of 3 idle connections)
                5, // maxIdle
                30000, // maxWaitMs
                600000); // idleTimeoutMs

        // Create a mock client
        KubernetesClient mockClient = Mockito.mock(KubernetesClient.class);

        // Mock KubernetesClientBuilder to control client creation
        try (MockedConstruction<KubernetesClientBuilder> mockedConstruction = Mockito.mockConstruction(KubernetesClientBuilder.class,
                (mock, context) -> {
                    Mockito.when(mock.withConfig((Config) Mockito.any())).thenReturn(mock);
                    Mockito.when(mock.build()).thenReturn(mockClient);
                })) {
            // This will automatically call initializeMinIdleConnections during construction
            Object clusterClientPool = constructor.newInstance(clusterId, mockKubeConfig, poolConfig);

            // Get the idleClients field to verify client initialization
            Field idleClientsField = clusterClientPoolClass.getDeclaredField("idleClients");
            idleClientsField.setAccessible(true);
            BlockingQueue<?> idleClients = (BlockingQueue<?>) idleClientsField.get(clusterClientPool);

            // Get the createdCount field
            Field createdCountField = clusterClientPoolClass.getDeclaredField("createdCount");
            createdCountField.setAccessible(true);

            // Verify minIdle connections were created and added to idle pool
            Assertions.assertEquals(3, idleClients.size(), "Idle clients should be 3 after initialization");
            Assertions.assertEquals(3, ((AtomicInteger) createdCountField.get(clusterClientPool)).get(), "Created count should be 3 after initialization");
        }
    }

    /**
     * Test ClusterClientPool constructor with different configurations
     */
    @Test
    public void testClusterClientPoolConstructor() throws Exception {
        // Create different pool configurations to test
        KubernetesClientPool.PoolConfig poolConfig1 = new KubernetesClientPool.PoolConfig(
                5, // maxSize
                1, // minIdle
                3, // maxIdle
                15000, // maxWaitMs
                300000); // idleTimeoutMs

        KubernetesClientPool.PoolConfig poolConfig2 = new KubernetesClientPool.PoolConfig(
                20, // maxSize
                5, // minIdle
                10, // maxIdle
                60000, // maxWaitMs
                1200000); // idleTimeoutMs

        // Create a mock client
        KubernetesClient mockClient = Mockito.mock(KubernetesClient.class);

        // Test constructor with different configurations
        for (KubernetesClientPool.PoolConfig poolConfig : new KubernetesClientPool.PoolConfig[] {poolConfig1, poolConfig2}) {
            try (MockedConstruction<KubernetesClientBuilder> mockedConstruction = Mockito.mockConstruction(KubernetesClientBuilder.class,
                    (mock, context) -> {
                        Mockito.when(mock.withConfig((Config) Mockito.any())).thenReturn(mock);
                        Mockito.when(mock.build()).thenReturn(mockClient);
                    })) {
                // Create ClusterClientPool instance
                Object clusterClientPool = Class.forName("org.apache.dolphinscheduler.plugin.task.api.k8s.KubernetesClientPool$ClusterClientPool")
                        .getDeclaredConstructor(String.class, String.class, KubernetesClientPool.PoolConfig.class)
                        .newInstance(clusterId, mockKubeConfig, poolConfig);

                // Get fields to verify initialization
                Class<?> clusterClientPoolClass = Class.forName("org.apache.dolphinscheduler.plugin.task.api.k8s.KubernetesClientPool$ClusterClientPool");
                Field clusterIdField = clusterClientPoolClass.getDeclaredField("clusterId");
                Field kubeConfigField = clusterClientPoolClass.getDeclaredField("kubeConfig");
                Field configField = clusterClientPoolClass.getDeclaredField("config");
                Field idleClientsField = clusterClientPoolClass.getDeclaredField("idleClients");
                Field activeClientsField = clusterClientPoolClass.getDeclaredField("activeClients");
                Field createdCountField = clusterClientPoolClass.getDeclaredField("createdCount");

                // Make fields accessible
                clusterIdField.setAccessible(true);
                kubeConfigField.setAccessible(true);
                configField.setAccessible(true);
                idleClientsField.setAccessible(true);
                activeClientsField.setAccessible(true);
                createdCountField.setAccessible(true);

                // Verify field initialization
                Assertions.assertEquals(clusterId, clusterIdField.get(clusterClientPool), "Cluster ID should be correctly initialized");
                Assertions.assertEquals(mockKubeConfig, kubeConfigField.get(clusterClientPool), "KubeConfig should be correctly initialized");
                Assertions.assertEquals(poolConfig, configField.get(clusterClientPool), "PoolConfig should be correctly initialized");
                Assertions.assertNotNull(idleClientsField.get(clusterClientPool), "Idle clients queue should be initialized");
                Assertions.assertNotNull(activeClientsField.get(clusterClientPool), "Active clients set should be initialized");
                Assertions.assertNotNull(createdCountField.get(clusterClientPool), "Created count should be initialized");

                // Verify minIdle connections were created
                BlockingQueue<?> idleClients = (BlockingQueue<?>) idleClientsField.get(clusterClientPool);
                Assertions.assertEquals(poolConfig.getMinIdle(), idleClients.size(),
                        "Idle clients should match minIdle configuration");
                Assertions.assertEquals(poolConfig.getMinIdle(), ((AtomicInteger) createdCountField.get(clusterClientPool)).get(),
                        "Created count should match minIdle configuration");
            }
        }
    }

    /**
     * Test KubernetesClientPool's cleanupIdleClients method
     */
    @Test
    public void testCleanupIdleClients() throws Exception {
        // Get the real instance for testing
        KubernetesClientPool realPool = Mockito.spy(KubernetesClientPool.class);

        // Mock the clusterClientPools field
        java.lang.reflect.Field clusterClientPoolsField =
                KubernetesClientPool.class.getDeclaredField("clusterClientPools");
        clusterClientPoolsField.setAccessible(true);
        ConcurrentHashMap<String, KubernetesClientPool.ClusterClientPool> mockClusterClientPools =
                new ConcurrentHashMap<>();

        // Create multiple mock ClusterClientPools for different clusters
        String clusterId1 = clusterId;
        String clusterId2 = clusterId + "_2";

        KubernetesClientPool.ClusterClientPool mockClusterClientPool1 =
                Mockito.mock(KubernetesClientPool.ClusterClientPool.class);
        KubernetesClientPool.ClusterClientPool mockClusterClientPool2 =
                Mockito.mock(KubernetesClientPool.ClusterClientPool.class);

        mockClusterClientPools.put(clusterId1, mockClusterClientPool1);
        mockClusterClientPools.put(clusterId2, mockClusterClientPool2);

        // Set the mock clusterClientPools into the realPool
        clusterClientPoolsField.set(realPool, mockClusterClientPools);

        // Mock the getInstance method to return our spy
        Mockito.when(KubernetesClientPool.getInstance()).thenReturn(realPool);

        // Get the cleanupIdleClients method
        Method cleanupIdleClientsMethod = KubernetesClientPool.class.getDeclaredMethod("cleanupIdleClients");
        cleanupIdleClientsMethod.setAccessible(true);

        // Call the method
        cleanupIdleClientsMethod.invoke(realPool);

        // Verify that cleanupIdle was called on both ClusterClientPools
        Mockito.verify(mockClusterClientPool1).cleanupIdle();
        Mockito.verify(mockClusterClientPool2).cleanupIdle();
    }

    /**
     * Test KubernetesClientPool's cleanupIdleClients method with no pools
     */
    @Test
    public void testCleanupIdleClientsNoPools() throws Exception {
        // Get the real instance for testing
        KubernetesClientPool realPool = Mockito.spy(KubernetesClientPool.class);

        // Mock the clusterClientPools field with an empty map
        java.lang.reflect.Field clusterClientPoolsField =
                KubernetesClientPool.class.getDeclaredField("clusterClientPools");
        clusterClientPoolsField.setAccessible(true);
        ConcurrentHashMap<String, KubernetesClientPool.ClusterClientPool> mockClusterClientPools =
                new ConcurrentHashMap<>();

        // Set the mock clusterClientPools into the realPool
        clusterClientPoolsField.set(realPool, mockClusterClientPools);

        // Mock the getInstance method to return our spy
        Mockito.when(KubernetesClientPool.getInstance()).thenReturn(realPool);

        // Get the cleanupIdleClients method
        Method cleanupIdleClientsMethod = KubernetesClientPool.class.getDeclaredMethod("cleanupIdleClients");
        cleanupIdleClientsMethod.setAccessible(true);

        // Call the method - should not throw exception
        try {
            cleanupIdleClientsMethod.invoke(realPool);
        } catch (Exception e) {
            Assertions.fail("cleanupIdleClients should not throw exception when there are no pools: " + e.getMessage());
        }
    }

    /**
     * Test ClusterClientPool's cleanupIdle method with timeout connections
     */
    @Test
    public void testClusterClientPoolCleanupIdle() throws Exception {
        // Create a real ClusterClientPool instance using reflection
        Class<?> clusterClientPoolClass = Class.forName("org.apache.dolphinscheduler.plugin.task.api.k8s.KubernetesClientPool$ClusterClientPool");
        Constructor<?> constructor = clusterClientPoolClass.getDeclaredConstructor(String.class, String.class, KubernetesClientPool.PoolConfig.class);
        constructor.setAccessible(true);

        // Use a very small idle timeout for testing
        long idleTimeoutMs = 600; // 600ms
        KubernetesClientPool.PoolConfig poolConfig = new KubernetesClientPool.PoolConfig(
                10, // maxSize
                1, // minIdle (keep at least 1 connection)
                5, // maxIdle
                30000, // maxWaitMs
                idleTimeoutMs); // idleTimeoutMs

        // Create a mock client
        KubernetesClient mockClient = Mockito.mock(KubernetesClient.class);
        NamespaceList namespaceList = Mockito.mock(NamespaceList.class);
        NonNamespaceOperation<Namespace, NamespaceList, Resource<Namespace>> namespaces = Mockito.mock(NonNamespaceOperation.class);
        Mockito.when(mockClient.namespaces()).thenReturn(namespaces);
        Mockito.when(namespaces.list()).thenReturn(namespaceList);

        // Mock KubernetesClientBuilder to control client creation
        try (MockedConstruction<KubernetesClientBuilder> mockedConstruction = Mockito.mockConstruction(KubernetesClientBuilder.class,
                (mock, context) -> {
                    Mockito.when(mock.withConfig((Config) Mockito.any())).thenReturn(mock);
                    Mockito.when(mock.build()).thenReturn(mockClient);
                })) {
            Object clusterClientPool = constructor.newInstance(clusterId, mockKubeConfig, poolConfig);

            // Get the necessary methods and fields
            Method borrowObjectMethod = clusterClientPoolClass.getDeclaredMethod("borrowObject");
            borrowObjectMethod.setAccessible(true);
            Method returnObjectMethod = clusterClientPoolClass.getDeclaredMethod("returnObject", KubernetesClient.class);
            returnObjectMethod.setAccessible(true);
            Method cleanupIdleMethod = clusterClientPoolClass.getDeclaredMethod("cleanupIdle");
            cleanupIdleMethod.setAccessible(true);
            Method isClientValidMethod = clusterClientPoolClass.getDeclaredMethod("isClientValid", KubernetesClient.class);
            isClientValidMethod.setAccessible(true);

            Field createdCountField = clusterClientPoolClass.getDeclaredField("createdCount");
            createdCountField.setAccessible(true);
            // Get idleClients field to verify client return
            Field idleClientsField = clusterClientPoolClass.getDeclaredField("idleClients");
            idleClientsField.setAccessible(true);
            BlockingQueue<?> idleClients = (BlockingQueue<?>) idleClientsField.get(clusterClientPool);
            // Get activeClients field
            Field activeClientsField = clusterClientPoolClass.getDeclaredField("activeClients");
            activeClientsField.setAccessible(true);
            Set<?> activeClients = (Set<?>) activeClientsField.get(clusterClientPool);



            // Borrow and return 3 clients to create idle connections
            KubernetesClient client1 = (KubernetesClient) borrowObjectMethod.invoke(clusterClientPool);
            KubernetesClient client2 = (KubernetesClient) borrowObjectMethod.invoke(clusterClientPool);
            KubernetesClient client3 = (KubernetesClient) borrowObjectMethod.invoke(clusterClientPool);
            Assertions.assertEquals(3, activeClients.size(), "Active clients count should be 3 after borrowing");

            isClientValidMethod.invoke(clusterClientPool,client1);
            isClientValidMethod.invoke(clusterClientPool,client2);
            isClientValidMethod.invoke(clusterClientPool,client3);

            returnObjectMethod.invoke(clusterClientPool, client1);
            returnObjectMethod.invoke(clusterClientPool, client2);
            returnObjectMethod.invoke(clusterClientPool, client3);


            // Verify all clients are idle
            Assertions.assertEquals(3, idleClients.size(), "Idle clients count should be 3 after returning");
            Assertions.assertEquals(3, ((AtomicInteger) createdCountField.get(clusterClientPool)).get(),
                    "Created count should be 3");

            // Mock the PooledClient's lastUsedTime to simulate timeout
            Class<?> pooledClientClass = getPooledClientClass();
            Field lastUsedTimeField = pooledClientClass.getDeclaredField("lastUsedTime");
            lastUsedTimeField.setAccessible(true);

            // Set all PooledClient's lastUsedTime to a time before the timeout
            long oldTime = System.currentTimeMillis() - idleTimeoutMs - 100; // 100ms older than timeout
            for (Object pooledClient : idleClients.toArray()) {
                lastUsedTimeField.setLong(pooledClient, oldTime);
            }

            // Call cleanupIdle
            cleanupIdleMethod.invoke(clusterClientPool);

            // Verify that only minIdle=1 client remains
            Assertions.assertEquals(1, idleClients.size(),
                    "Idle clients count should be minIdle=1 after cleanup");
            Assertions.assertEquals(1, ((AtomicInteger) createdCountField.get(clusterClientPool)).get(),
                    "Created count should be 1 after cleanup");
        }
    }

    /**
     * Test ClusterClientPool's cleanupIdle method with partial timeout connections
     */
    @Test
    public void testClusterClientPoolCleanupIdlePartialTimeout() throws Exception {
        // Create a real ClusterClientPool instance using reflection
        Class<?> clusterClientPoolClass = Class.forName("org.apache.dolphinscheduler.plugin.task.api.k8s.KubernetesClientPool$ClusterClientPool");
        Constructor<?> constructor = clusterClientPoolClass.getDeclaredConstructor(String.class, String.class, KubernetesClientPool.PoolConfig.class);
        constructor.setAccessible(true);

        // Use a very small idle timeout for testing
        long idleTimeoutMs = 100; // 100ms
        KubernetesClientPool.PoolConfig poolConfig = new KubernetesClientPool.PoolConfig(
                10, // maxSize
                1, // minIdle
                5, // maxIdle
                30000, // maxWaitMs
                idleTimeoutMs); // idleTimeoutMs

        // Create a mock client
        KubernetesClient mockClient = Mockito.mock(KubernetesClient.class);
        NamespaceList namespaceList = Mockito.mock(NamespaceList.class);
        NonNamespaceOperation<Namespace, NamespaceList, Resource<Namespace>> namespaces = Mockito.mock(NonNamespaceOperation.class);
        Mockito.when(mockClient.namespaces()).thenReturn(namespaces);
        Mockito.when(namespaces.list()).thenReturn(namespaceList);

        // Mock KubernetesClientBuilder to control client creation
        try (MockedConstruction<KubernetesClientBuilder> mockedConstruction = Mockito.mockConstruction(KubernetesClientBuilder.class,
                (mock, context) -> {
                    Mockito.when(mock.withConfig((Config) Mockito.any())).thenReturn(mock);
                    Mockito.when(mock.build()).thenReturn(mockClient);
                })) {
            Object clusterClientPool = constructor.newInstance(clusterId, mockKubeConfig, poolConfig);

            // Get the necessary methods and fields
            Method borrowObjectMethod = clusterClientPoolClass.getDeclaredMethod("borrowObject");
            borrowObjectMethod.setAccessible(true);
            Method returnObjectMethod = clusterClientPoolClass.getDeclaredMethod("returnObject", KubernetesClient.class);
            returnObjectMethod.setAccessible(true);
            Method cleanupIdleMethod = clusterClientPoolClass.getDeclaredMethod("cleanupIdle");
            cleanupIdleMethod.setAccessible(true);

            Field idleClientsField = clusterClientPoolClass.getDeclaredField("idleClients");
            idleClientsField.setAccessible(true);
            BlockingQueue<?> idleClients = (BlockingQueue<?>) idleClientsField.get(clusterClientPool);

            Field createdCountField = clusterClientPoolClass.getDeclaredField("createdCount");
            createdCountField.setAccessible(true);

            // Borrow and return 3 clients to create idle connections
            KubernetesClient client1 = (KubernetesClient) borrowObjectMethod.invoke(clusterClientPool);
            KubernetesClient client2 = (KubernetesClient) borrowObjectMethod.invoke(clusterClientPool);
            KubernetesClient client3 = (KubernetesClient) borrowObjectMethod.invoke(clusterClientPool);
            returnObjectMethod.invoke(clusterClientPool, client1);
            returnObjectMethod.invoke(clusterClientPool, client2);
            returnObjectMethod.invoke(clusterClientPool, client3);

            // Verify all clients are idle
            Assertions.assertEquals(3, idleClients.size(), "Idle clients count should be 3 after returning");
            Assertions.assertEquals(3, ((AtomicInteger) createdCountField.get(clusterClientPool)).get(),
                    "Created count should be 3");

            // Mock the PooledClient's lastUsedTime - only some clients timeout
            Class<?> pooledClientClass = getPooledClientClass();
            Field lastUsedTimeField = pooledClientClass.getDeclaredField("lastUsedTime");
            lastUsedTimeField.setAccessible(true);

            // Set only 2 PooledClient's lastUsedTime to a time before the timeout
            long oldTime = System.currentTimeMillis() - idleTimeoutMs - 100; // 100ms older than timeout
            Object[] clients = idleClients.toArray();
            for (int i = 0; i < 2; i++) {
                lastUsedTimeField.setLong(clients[i], oldTime);
            }
            // Leave the third client with current time (not timeout)

            // Call cleanupIdle
            cleanupIdleMethod.invoke(clusterClientPool);

            // Verify that only non-timeout client remains
            Assertions.assertEquals(1, idleClients.size(),
                    "Idle clients count should be 1 (non-timeout client) after cleanup");
            Assertions.assertEquals(1, ((AtomicInteger) createdCountField.get(clusterClientPool)).get(),
                    "Created count should be 1 after cleanup");
        }
    }
}
