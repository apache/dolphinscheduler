/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.registry.raft;

import static com.alipay.sofa.jraft.util.BytesUtil.readUtf8;
import static com.alipay.sofa.jraft.util.BytesUtil.writeUtf8;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.HeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 * 1. EphemeralNodeRefreshThread check current master node connection and check ephemeral node expire time
 * 2. maintain Map<String, Long> activeMasterServers
 * 3. maintain Map<String, Long> activeWorkerServers
 * 4. maintain Map<Constants.REGISTRY_DOLPHINSCHEDULER_MASTERS, List<String>> master servers
 * 5. maintain Map<Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS, List<String>> worker servers
 * 6. maintain Map<Constants.REGISTRY_DOLPHINSCHEDULER_DEAD_SERVERS, List<String>> dead servers
 */
@Slf4j
public class EphemeralNodeManager implements AutoCloseable {
    private final List<ConnectionListener> connectionListeners = Collections.synchronizedList(new ArrayList<>());

    private final Map<String, List<SubscribeListener>> dataSubScribeMap = new ConcurrentHashMap<>();

    private RaftRegistryProperties properties;


    private RheaKVStore kvStore;

    public EphemeralNodeManager(RaftRegistryProperties properties, RheaKVStore kvStore) {
        this.properties = properties;
        this.kvStore = kvStore;
    }

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(
            2,
            new ThreadFactoryBuilder().setNameFormat("EphemeralNodeRefreshThread").setDaemon(true).build());

    public void start() {
        scheduledExecutorService.scheduleWithFixedDelay(new ConnectionCheckTask(),
                properties.getListenerCheckInterval().toMillis(),
                properties.getListenerCheckInterval().toMillis(),
                TimeUnit.MILLISECONDS);
        scheduledExecutorService.scheduleWithFixedDelay(new SubscribeCheckTask(),
                properties.getListenerCheckInterval().toMillis(),
                properties.getListenerCheckInterval().toMillis(),
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        connectionListeners.clear();
        dataSubScribeMap.clear();
        scheduledExecutorService.shutdown();
    }

    public void addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    public boolean addSubscribeListener(String path, SubscribeListener listener) {
        return dataSubScribeMap.computeIfAbsent(path, k -> new ArrayList<>()).add(listener);
    }

    public void removeSubscribeListener(String path) {
        dataSubScribeMap.remove(path);
    }

    private class ConnectionCheckTask implements Runnable {
        private ConnectionState connectionState = null;

        @Override
        public void run() {
            checkConnection();
            checkActiveNode();
        }

        private void checkConnection() {
            final String host = NetUtils.getHost();
            if (getActiveMasterServers().keySet().stream().anyMatch(address -> address.split(Constants.COLON)[0].equals(host))) {
                if (connectionState == null && !connectionListeners.isEmpty()) {
                    triggerListener(ConnectionState.CONNECTED);
                } else if (connectionState == ConnectionState.DISCONNECTED) {
                    triggerListener(ConnectionState.RECONNECTED);
                } else {
                    triggerListener(ConnectionState.CONNECTED);
                }
                connectionState = ConnectionState.CONNECTED;
            }
        }

        private void checkActiveNode() {
            long expireTime = properties.getConnectionExpireFactor() * properties.getListenerCheckInterval().toMillis();
            Map<String, Long> activeMasterServers = getActiveMasterServers();
            for (Map.Entry<String, Long> entry : activeMasterServers.entrySet()) {
                if ((System.currentTimeMillis() - entry.getValue()) > expireTime) {
                    final String nodeAddress = entry.getKey();
                    activeMasterServers.remove(nodeAddress);
                    updateActiveMaster(activeMasterServers);
                    addDeadServer(Constants.MASTER_TYPE, nodeAddress);
                    if (nodeAddress.split(Constants.COLON)[0].equals(NetUtils.getHost())) {
                        connectionState = ConnectionState.DISCONNECTED;
                        triggerListener(ConnectionState.DISCONNECTED);
                        removeNodeData(nodeAddress);
                    }
                    log.warn("Master server {} connect to raft cluster timeout, last heartbeat {}, timeout config {} ms",
                            nodeAddress, convertTimeToString(entry.getValue()), expireTime);
                }
            }
            Map<String, Long> activeWorkerServers = getActiveWorkerServers();
            for (Map.Entry<String, Long> entry : activeWorkerServers.entrySet()) {
                if ((System.currentTimeMillis() - entry.getValue()) > expireTime) {
                    final String nodeAddress = entry.getKey();
                    activeWorkerServers.remove(nodeAddress);
                    updateActiveWorker(nodeAddress, activeWorkerServers);
                    removeWorkerGroup(nodeAddress);
                    addDeadServer(Constants.WORKER_TYPE, nodeAddress);
                    removeNodeData(nodeAddress);
                    log.warn("Worker server {} connect to raft cluster timeout, last heartbeat {}, timeout config {} ms",
                            nodeAddress, convertTimeToString(entry.getValue()), expireTime);
                }
            }
        }

        private void triggerListener(ConnectionState connectionState) {
            connectionListeners.forEach(listener -> listener.onUpdate(connectionState));
        }
    }

    private class SubscribeCheckTask implements Runnable {

        private final Map<String, String> nodeDataMap = new ConcurrentHashMap<>();

        @Override
        public void run() {
            subscribeCheck();
        }

        private void subscribeCheck() {
            if (dataSubScribeMap.isEmpty()) {
                return;
            }
            final Map<String, String> currentNodeDataMap = getNodeDataMap();
            // find the different
            Map<String, String> addedData = new HashMap<>();
            Map<String, String> deletedData = new HashMap<>();
            Map<String, String> updatedData = new HashMap<>();
            for (Map.Entry<String, String> entry : currentNodeDataMap.entrySet()) {
                final String oldData = nodeDataMap.get(entry.getKey());
                if (oldData == null) {
                    addedData.put(entry.getKey(), entry.getValue());
                } else {
                    HeartBeat newHeartBeat = HeartBeat.decodeHeartBeat(entry.getValue());
                    HeartBeat oldHeartBeat = HeartBeat.decodeHeartBeat(oldData);
                    if (newHeartBeat != null && newHeartBeat.getReportTime() != oldHeartBeat.getReportTime()) {
                        updatedData.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            for (Map.Entry<String, String> entry : nodeDataMap.entrySet()) {
                if (!currentNodeDataMap.containsKey(entry.getKey())) {
                    deletedData.put(entry.getKey(), entry.getValue());
                }
            }
            nodeDataMap.clear();
            nodeDataMap.putAll(currentNodeDataMap);
            // trigger listener
            for (Map.Entry<String, List<SubscribeListener>> entry : dataSubScribeMap.entrySet()) {
                String subscribeKey = entry.getKey();
                List<SubscribeListener> subscribeListeners = entry.getValue();
                triggerListener(addedData, subscribeKey, subscribeListeners, Event.Type.ADD);
                triggerListener(deletedData, subscribeKey, subscribeListeners, Event.Type.REMOVE);
                triggerListener(updatedData, subscribeKey, subscribeListeners, Event.Type.UPDATE);
            }

        }

        private void triggerListener(Map<String, String> nodeDataMap, String subscribeKey, List<SubscribeListener> subscribeListeners, Event.Type type) {
            for (Map.Entry<String, String> entry : nodeDataMap.entrySet()) {
                final String key = entry.getKey();
                if (key.startsWith(subscribeKey)) {
                    subscribeListeners.forEach(listener -> listener.notify(new Event(key, key, entry.getValue(), type)));
                }
            }
        }
    }

    public static String convertTimeToString(Long time) {
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
    }

    public void putHandler(String key, String value) {
        final String nodeAddress = key.substring(key.lastIndexOf(Constants.SINGLE_SLASH) + 1);
        //update heart beat time and node set
        if (key.startsWith(Constants.REGISTRY_DOLPHINSCHEDULER_MASTERS)) {
            Map<String, Long> activeMasterServers = getActiveMasterServers();
            activeMasterServers.put(nodeAddress, System.currentTimeMillis());
            updateActiveMaster(activeMasterServers);
            removeDeadServer(Constants.MASTER_TYPE, nodeAddress);
            addNodeData(key, value);
        } else if (key.startsWith(Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS)) {
            Map<String, Long> activeWorkerServers = getActiveWorkerServers();
            activeWorkerServers.put(nodeAddress, System.currentTimeMillis());
            updateActiveWorker(key, activeWorkerServers);
            addWorkerGroup(key);
            removeDeadServer(Constants.WORKER_TYPE, nodeAddress);
            addNodeData(key, value);
        } else if (key.startsWith(Constants.REGISTRY_DOLPHINSCHEDULER_DEAD_SERVERS)) {
            final List<String> deadServers = getDeadServers();
            if (!deadServers.contains(nodeAddress)) {
                deadServers.add(nodeAddress);
                kvStore.bPut(Constants.REGISTRY_DOLPHINSCHEDULER_DEAD_SERVERS, writeUtf8(JSONUtils.toJsonString(deadServers)));
            }
        }

    }

    public void deleteHandler(String key) {
        final String nodeAddress = key.substring(key.lastIndexOf(Constants.SINGLE_SLASH) + 1);
        if (key.contains(Constants.REGISTRY_DOLPHINSCHEDULER_MASTERS)) {
            Map<String, Long> activeMasterServers = getActiveMasterServers();
            activeMasterServers.remove(nodeAddress);
            updateActiveMaster(activeMasterServers);
            removeNodeData(nodeAddress);
            log.info("Raft registry remove master server {}", nodeAddress);
        } else if (key.contains(Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS)) {
            Map<String, Long> activeWorkerServers = getActiveWorkerServers();
            activeWorkerServers.remove(nodeAddress);
            updateActiveWorker(key, activeWorkerServers);
            removeWorkerGroup(nodeAddress);
            removeNodeData(nodeAddress);
            log.info("Raft registry remove worker server {}", nodeAddress);
        }
    }

    private void updateActiveMaster(Map<String, Long> activeNodes) {
        kvStore.bPut(Constants.MASTER_TYPE, writeUtf8(JSONUtils.toJsonString(activeNodes)));
        //Update the mapping of the master group and master node list
        kvStore.bPut(Constants.REGISTRY_DOLPHINSCHEDULER_MASTERS, writeUtf8(JSONUtils.toJsonString(activeNodes.keySet())));
    }

    private void updateActiveWorker(String key, Map<String, Long> activeNodes) {
        kvStore.bPut(Constants.WORKER_TYPE, writeUtf8(JSONUtils.toJsonString(activeNodes)));
        //Update the mapping of the worker group and worker node list
        kvStore.bPut(key.substring(0, key.lastIndexOf(Constants.SINGLE_SLASH)), writeUtf8(JSONUtils.toJsonString(activeNodes.keySet())));
    }

    private void addDeadServer(String nodeType, String nodeAddress) {
        final String deadServerAddress = getDeadServerSuffix(nodeType, nodeAddress);
        List<String> deadServers = getDeadServers();
        if (!deadServers.contains(deadServerAddress)) {
            deadServers.add(deadServerAddress);
            kvStore.bPut(Constants.REGISTRY_DOLPHINSCHEDULER_DEAD_SERVERS, writeUtf8(JSONUtils.toJsonString(deadServers)));
        }
    }

    private void removeDeadServer(String nodeType, String nodeAddress) {
        final String deadServerAddress = getDeadServerSuffix(nodeType, nodeAddress);
        List<String> deadServers = getDeadServers();
        if (deadServers.contains(deadServerAddress)) {
            deadServers.remove(deadServerAddress);
            kvStore.bPut(Constants.REGISTRY_DOLPHINSCHEDULER_DEAD_SERVERS, writeUtf8(JSONUtils.toJsonString(deadServers)));
        }
    }

    /**
     * @return IP:Port->TimeMillis
     */
    private Map<String, Long> getActiveWorkerServers() {
        final String servers = readUtf8(kvStore.bGet(Constants.WORKER_TYPE));
        if (StringUtils.isEmpty(servers)) {
            return new HashMap<>();
        }
        return JSONUtils.toMap(servers, String.class, Long.class);
    }

    private List<String> getDeadServers() {
        final String storedDeadServers = readUtf8(kvStore.bGet(Constants.REGISTRY_DOLPHINSCHEDULER_DEAD_SERVERS));
        if (StringUtils.isEmpty(storedDeadServers)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(JSONUtils.toList(storedDeadServers, String.class));
    }

    /**
     * @return IP:Port->TimeMillis
     */
    private Map<String, Long> getActiveMasterServers() {
        final String storedMasterServers = readUtf8(kvStore.bGet(Constants.MASTER_TYPE));
        if (StringUtils.isEmpty(storedMasterServers)) {
            return new HashMap<>();
        }
        return JSONUtils.toMap(storedMasterServers, String.class, Long.class);
    }

    private String getDeadServerSuffix(String nodeType, String serverAddress) {
        return nodeType + Constants.UNDERLINE + serverAddress;
    }

    private void addWorkerGroup(String key) {
        List<String> workerGroupList = getWorkerGroups();
        String workerGroup = key.substring(key.indexOf(Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS)
                + Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS.length() + 1, key.lastIndexOf(Constants.SINGLE_SLASH));
        if (!workerGroupList.contains(workerGroup)) {
            workerGroupList.add(workerGroup);
            kvStore.bPut(Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS, writeUtf8(JSONUtils.toJsonString(workerGroupList)));
        }
    }

    private void removeWorkerGroup(String nodeAddress) {
        List<String> workerGroupList = getWorkerGroups();
        final int originSize = workerGroupList.size();
        final Iterator<String> iterator = workerGroupList.iterator();
        while (iterator.hasNext()) {
            String group = iterator.next();
            final String groupKey = Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS + Constants.SINGLE_SLASH + group;
            final String storedWorkerList = readUtf8(kvStore.bGet(groupKey));
            if (storedWorkerList != null) {
                final List<String> workers = JSONUtils.toList(storedWorkerList, String.class);
                workers.remove(nodeAddress);
                if (workers.isEmpty()) {
                    kvStore.bDelete(groupKey);
                    iterator.remove();
                } else {
                    kvStore.bPut(groupKey, writeUtf8(JSONUtils.toJsonString(workers)));
                }
            }
        }
        if (originSize != workerGroupList.size()) {
            kvStore.bPut(Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS, writeUtf8(JSONUtils.toJsonString(workerGroupList)));
        }

    }

    private List<String> getWorkerGroups() {
        final String storedWorkerGroup = readUtf8(kvStore.bGet(Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS));
        if (StringUtils.isEmpty(storedWorkerGroup)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(JSONUtils.toList(storedWorkerGroup, String.class));
    }

    private void addNodeData(String key, String value) {
        Map<String, String> nodeDataMap = getNodeDataMap();
        nodeDataMap.put(key, value);
        kvStore.bPut(Constants.REGISTRY_DOLPHINSCHEDULER_NODE, writeUtf8(JSONUtils.toJsonString(nodeDataMap)));
    }

    private void removeNodeData(String key) {
        Map<String, String> nodeDataMap = getNodeDataMap();
        nodeDataMap.remove(key);
        kvStore.bPut(Constants.REGISTRY_DOLPHINSCHEDULER_NODE, writeUtf8(JSONUtils.toJsonString(nodeDataMap)));
    }

    private Map<String, String> getNodeDataMap() {
        final String storedMasterServers = readUtf8(kvStore.bGet(Constants.REGISTRY_DOLPHINSCHEDULER_NODE));
        if (StringUtils.isEmpty(storedMasterServers)) {
            return new HashMap<>();
        }
        return JSONUtils.toMap(storedMasterServers, String.class, String.class);
    }
}
