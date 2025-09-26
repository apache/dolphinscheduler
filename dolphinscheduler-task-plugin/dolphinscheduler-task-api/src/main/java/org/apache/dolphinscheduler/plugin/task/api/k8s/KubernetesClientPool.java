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
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

/**
 * KubernetesClientPool is used to manage the Kubernetes client connection pool
 * Maintain an independent connection pool for each K8s cluster to implement connection creation, acquisition, return, and closure
 */
@Slf4j
public class KubernetesClientPool {

    /**
     * Connection pool instance
     */
    private static final KubernetesClientPool INSTANCE = new KubernetesClientPool();

    /**
     * Cluster connection pool mapping, with the key being the cluster identifier
     */
    private final ConcurrentMap<String, ClusterClientPool> clusterClientPools = new ConcurrentHashMap<>();

    /**
     * Connection pool configuration
     */
    private final PoolConfig poolConfig;

    private KubernetesClientPool() {
        this.poolConfig = new PoolConfig(
                PropertyUtils.getInt(TaskConstants.K8S_CLIENT_POOL_MAX_SIZE, 10),
                PropertyUtils.getInt(TaskConstants.K8S_CLIENT_POOL_MIN_IDLE, 2),
                PropertyUtils.getInt(TaskConstants.K8S_CLIENT_POOL_MAX_IDLE, 5),
                PropertyUtils.getInt(TaskConstants.K8S_CLIENT_POOL_MAX_WAIT_MS, 30000),
                PropertyUtils.getInt(TaskConstants.K8S_CLIENT_POOL_IDLE_TIMEOUT_MS, 600000));
        log.info("KubernetesClientPool initialized with config: {}", poolConfig);

        // clean connection thread
        startCleanupThread();
    }

    public static KubernetesClientPool getInstance() {
        return INSTANCE;
    }

    /**
     * Generate cluster identifier based on kubeconfig
     * @param kubeConfig kubeconfig Configuration
     * @return Cluster identification
     */
    public String getClusterId(String kubeConfig) {
        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(kubeConfig.getBytes(StandardCharsets.UTF_8));

            String base64Hash = Base64.getUrlEncoder().encodeToString(hashBytes);
            return base64Hash.replace("=", "");
        } catch (Exception e) {
            log.error("Failed to generate cluster ID", e);
            return Integer.toString(kubeConfig.hashCode());
        }
    }

    /**
     * Obtain the Kubernetes client for the specified cluster
     * @param clusterId Cluster Identifier
     * @param kubeConfig kubeconfig Configuration
     * @return Kubernetes Client
     */
    public KubernetesClient getClient(String clusterId, String kubeConfig) {
        ClusterClientPool pool = clusterClientPools.computeIfAbsent(clusterId,
                k -> new ClusterClientPool(k, kubeConfig, poolConfig));
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            log.error("Failed to get Kubernetes client", e);
            return null;
        }
    }

    /**
     * Return the Kubernetes client to the connection pool
     * @param clusterId Cluster Identifier
     * @param client Kubernetes Client will be returned
     */
    public void returnClient(String clusterId, KubernetesClient client) {
        ClusterClientPool pool = clusterClientPools.get(clusterId);
        if (pool != null) {
            pool.returnObject(client);
        }
    }

    /**
     * Close the connection pool of the specified cluster
     * @param clusterId Cluster Identifier
     */
    public void closePool(String clusterId) {
        ClusterClientPool pool = clusterClientPools.remove(clusterId);
        if (pool != null) {
            pool.close();
        }
    }

    /**
     * Start the cleanup thread to regularly clean up idle connections
     */
    private void startCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    // every 30s
                    Thread.sleep(30000);
                    cleanupIdleClients();
                } catch (InterruptedException e) {
                    log.warn("Cleanup thread interrupted", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "k8s-client-cleanup-thread");
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    /**
     * clean free connections
     */
    private void cleanupIdleClients() {
        for (ClusterClientPool pool : clusterClientPools.values()) {
            pool.cleanupIdle();
        }
    }

    /**
     * Configuration Class
     */
    public static class PoolConfig {

        private final int maxSize; // max connection num
        private final int minIdle; // min free connection num
        private final int maxIdle; // max free connection num
        private final long maxWaitMs; // max waiting time
        private final long idleTimeoutMs; // free timeout

        public PoolConfig(int maxSize, int minIdle, int maxIdle, long maxWaitMs, long idleTimeoutMs) {
            this.maxSize = maxSize;
            this.minIdle = minIdle;
            this.maxIdle = maxIdle;
            this.maxWaitMs = maxWaitMs;
            this.idleTimeoutMs = idleTimeoutMs;
        }

        public int getMaxSize() {
            return maxSize;
        }
        public int getMinIdle() {
            return minIdle;
        }
        public int getMaxIdle() {
            return maxIdle;
        }
        public long getMaxWaitMs() {
            return maxWaitMs;
        }
        public long getIdleTimeoutMs() {
            return idleTimeoutMs;
        }

        @Override
        public String toString() {
            return "PoolConfig{" +
                    "maxSize=" + maxSize +
                    ", minIdle=" + minIdle +
                    ", maxIdle=" + maxIdle +
                    ", maxWaitMs=" + maxWaitMs +
                    ", idleTimeoutMs=" + idleTimeoutMs +
                    '}';
        }
    }

    /**
     * Cluster Connection Pool Class
     */
    public static class ClusterClientPool {

        private final String clusterId;
        private final String kubeConfig;
        private final PoolConfig config;

        private final BlockingQueue<PooledClient> idleClients;
        private final Set<PooledClient> activeClients;
        private final AtomicInteger createdCount = new AtomicInteger(0);

        public ClusterClientPool(String clusterId, String kubeConfig, PoolConfig config) {
            this.clusterId = clusterId;
            this.kubeConfig = kubeConfig;
            this.config = config;
            this.idleClients = new LinkedBlockingQueue<>();
            this.activeClients = new HashSet<>();

            // initial
            initializeMinIdleConnections();
        }

        private void initializeMinIdleConnections() {
            for (int i = 0; i < config.getMinIdle(); i++) {
                try {
                    createIdleConnection();
                } catch (Exception e) {
                    log.error("Failed to initialize idle connection for cluster {}", clusterId, e);
                }
            }
        }

        private void createIdleConnection() throws Exception {
            PooledClient client = createClient();
            boolean offer = idleClients.offer(client);
            log.debug("{} to initialize idle connection for cluster {}", offer, clusterId);
        }

        private PooledClient createClient() throws Exception {
            if (createdCount.get() >= config.getMaxSize()) {
                throw new Exception("Connection pool reached max size: " + config.getMaxSize());
            }

            try {
                KubernetesClient client = new KubernetesClientBuilder()
                        .withConfig(Config.fromKubeconfig(kubeConfig)).build();
                createdCount.incrementAndGet();
                log.debug("Created new Kubernetes client for cluster {}", clusterId);
                return new PooledClient(client);
            } catch (Exception e) {
                log.error("Failed to create Kubernetes client for cluster {}", clusterId, e);
                throw e;
            }
        }

        /**
         * Retrieve the client from the connection pool
         */
        public synchronized KubernetesClient borrowObject() throws Exception {
            PooledClient client = idleClients.poll();
            if (client != null) {
                activeClients.add(client);
                client.lastUsedTime = System.currentTimeMillis();
                return client.client;
            }

            if (createdCount.get() < config.getMaxSize()) {
                client = createClient();
                activeClients.add(client);
                return client.client;
            }

            client = idleClients.poll(config.getMaxWaitMs(), TimeUnit.MILLISECONDS);
            if (client != null) {
                activeClients.add(client);
                client.lastUsedTime = System.currentTimeMillis();
                return client.client;
            }
            throw new Exception("Timeout waiting for available Kubernetes client connection");
        }

        public synchronized void returnObject(KubernetesClient client) {
            PooledClient pooledClient = null;
            for (PooledClient pc : activeClients) {
                if (pc.client == client) {
                    pooledClient = pc;
                    break;
                }
            }

            if (pooledClient != null) {
                activeClients.remove(pooledClient);
                pooledClient.lastUsedTime = System.currentTimeMillis();
                if (idleClients.size() >= config.getMaxIdle() || !isClientValid(pooledClient.client)) {
                    closeClient(pooledClient);
                } else {
                    boolean offer = idleClients.offer(pooledClient);
                    log.debug("{} to return Object", offer);
                }
            }
        }

        public synchronized void cleanupIdle() {
            long now = System.currentTimeMillis();
            PooledClient[] clients = idleClients.toArray(new PooledClient[0]);
            int keepIdle = Math.max(config.getMinIdle(), 0);
            for (PooledClient client : clients) {
                if (idleClients.size() > keepIdle &&
                        now - client.lastUsedTime > config.getIdleTimeoutMs()) {
                    if (idleClients.remove(client)) {
                        closeClient(client);
                    }
                }
            }
        }

        private boolean isClientValid(KubernetesClient client) {
            try {
                client.namespaces().list();
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        private void closeClient(PooledClient client) {
            try {
                client.client.close();
                createdCount.decrementAndGet();
                log.debug("Closed Kubernetes client for cluster {}", clusterId);
            } catch (Exception e) {
                log.error("Error closing Kubernetes client for cluster {}", clusterId, e);
            }
        }

        public synchronized void close() {
            PooledClient client;
            while ((client = idleClients.poll()) != null) {
                closeClient(client);
            }

            for (PooledClient activeClient : activeClients) {
                closeClient(activeClient);
            }
            activeClients.clear();
        }

        public static class PooledClient {

            private final KubernetesClient client;
            private long lastUsedTime;

            public PooledClient(KubernetesClient client) {
                this.client = client;
                this.lastUsedTime = System.currentTimeMillis();
            }
        }
    }
}
