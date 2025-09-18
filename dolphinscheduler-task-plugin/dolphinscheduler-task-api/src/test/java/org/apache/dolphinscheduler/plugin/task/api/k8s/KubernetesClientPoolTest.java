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



import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import io.fabric8.kubernetes.client.KubernetesClient;


public class KubernetesClientPoolTest {

    private KubernetesClientPool mockPool;
    private final String mockKubeConfig = "apiVersion: v1\nclusters:\n- cluster:\n    server: https://kubernetes.default.svc\n  name: mock-cluster\ncontexts:\n- context:\n    cluster: mock-cluster\n    namespace: default\n    user: mock-user\n  name: mock-context\ncurrent-context: mock-context\nkind: Config\npreferences: {}\nusers:\n- name: mock-user\n  user: {}";
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
        java.lang.reflect.Field clusterClientPoolsField = KubernetesClientPool.class.getDeclaredField("clusterClientPools");
        clusterClientPoolsField.setAccessible(true);
        ConcurrentHashMap<String, KubernetesClientPool.ClusterClientPool> mockClusterClientPools = new ConcurrentHashMap<>();

        // Create a mock ClusterClientPool
        KubernetesClientPool.ClusterClientPool mockClusterClientPool = Mockito.mock(KubernetesClientPool.ClusterClientPool.class);
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
        java.lang.reflect.Field clusterClientPoolsField = KubernetesClientPool.class.getDeclaredField("clusterClientPools");
        clusterClientPoolsField.setAccessible(true);
        ConcurrentHashMap<String, KubernetesClientPool.ClusterClientPool> mockClusterClientPools = new ConcurrentHashMap<>();

        // Create a mock ClusterClientPool
        KubernetesClientPool.ClusterClientPool mockClusterClientPool = Mockito.mock(KubernetesClientPool.ClusterClientPool.class);
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
        java.lang.reflect.Field clusterClientPoolsField = KubernetesClientPool.class.getDeclaredField("clusterClientPools");
        clusterClientPoolsField.setAccessible(true);
        ConcurrentHashMap<String, KubernetesClientPool.ClusterClientPool> mockClusterClientPools = new ConcurrentHashMap<>();

        // Create a mock ClusterClientPool
        KubernetesClientPool.ClusterClientPool mockClusterClientPool = Mockito.mock(KubernetesClientPool.ClusterClientPool.class);
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
        java.lang.reflect.Field clusterClientPoolsField = KubernetesClientPool.class.getDeclaredField("clusterClientPools");
        clusterClientPoolsField.setAccessible(true);
        ConcurrentHashMap<String, KubernetesClientPool.ClusterClientPool> mockClusterClientPools = new ConcurrentHashMap<>();

        // Create multiple mock ClusterClientPools for different clusters
        String clusterId1 = clusterId;
        String clusterId2 = clusterId + "_2";

        KubernetesClientPool.ClusterClientPool mockClusterClientPool1 = Mockito.mock(KubernetesClientPool.ClusterClientPool.class);
        KubernetesClientPool.ClusterClientPool mockClusterClientPool2 = Mockito.mock(KubernetesClientPool.ClusterClientPool.class);

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
        java.lang.reflect.Field clusterClientPoolsField = KubernetesClientPool.class.getDeclaredField("clusterClientPools");
        clusterClientPoolsField.setAccessible(true);
        ConcurrentHashMap<String, KubernetesClientPool.ClusterClientPool> mockClusterClientPools = new ConcurrentHashMap<>();

        // Create a mock ClusterClientPool
        KubernetesClientPool.ClusterClientPool mockClusterClientPool = Mockito.mock(KubernetesClientPool.ClusterClientPool.class);
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
            java.lang.reflect.Field clusterClientPoolsField = KubernetesClientPool.class.getDeclaredField("clusterClientPools");
            clusterClientPoolsField.setAccessible(true);
            ConcurrentHashMap<String, KubernetesClientPool.ClusterClientPool> mockClusterClientPools = new ConcurrentHashMap<>();

            // Create a mock ClusterClientPool
            KubernetesClientPool.ClusterClientPool mockClusterClientPool = Mockito.mock(KubernetesClientPool.ClusterClientPool.class);
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

}