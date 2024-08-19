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

package org.apache.dolphinscheduler.plugin.registry.raft.manage;

import static com.alipay.sofa.jraft.util.BytesUtil.readUtf8;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.model.BaseHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.registry.raft.RaftRegistryProperties;
import org.apache.dolphinscheduler.plugin.registry.raft.model.NodeItem;
import org.apache.dolphinscheduler.plugin.registry.raft.model.NodeType;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.storage.KVEntry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
@Slf4j
public class RaftSubscribeDataManager implements IRaftSubscribeDataManager {

    private final Map<String, List<SubscribeListener>> dataSubScribeMap = new ConcurrentHashMap<>();

    private final RaftRegistryProperties properties;

    private final RheaKVStore kvStore;

    private final ScheduledExecutorService scheduledExecutorService;

    public RaftSubscribeDataManager(RaftRegistryProperties properties, RheaKVStore kvStore) {
        this.properties = properties;
        this.kvStore = kvStore;
        this.scheduledExecutorService = Executors.newScheduledThreadPool(
                properties.getSubscribeListenerThreadPoolSize(),
                new ThreadFactoryBuilder().setNameFormat("SubscribeListenerCheckThread").setDaemon(true).build());
    }

    @Override
    public void start() {
        scheduledExecutorService.scheduleWithFixedDelay(new SubscribeCheckTask(),
                properties.getListenerCheckInterval().toMillis(),
                properties.getListenerCheckInterval().toMillis(),
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void addDataSubscribeListener(String path, SubscribeListener listener) {
        final List<SubscribeListener> subscribeListeners =
                dataSubScribeMap.computeIfAbsent(path, k -> new ArrayList<>());
        subscribeListeners.add(listener);
    }

    private class SubscribeCheckTask implements Runnable {

        private final Map<String, NodeItem> oldDataMap = new ConcurrentHashMap<>();

        @Override
        public void run() {
            try {
                final Map<String, NodeItem> newDataMap = getNodeDataMap();
                if (dataSubScribeMap.isEmpty() || newDataMap == null || newDataMap.isEmpty()) {
                    return;
                }
                // find the different
                final Map<String, String> addedData = new HashMap<>();
                final Map<String, String> deletedData = new HashMap<>();
                final Map<String, String> updatedData = new HashMap<>();

                Iterator<Map.Entry<String, NodeItem>> iterator = newDataMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, NodeItem> entry = iterator.next();
                    final NodeItem oldData = oldDataMap.get(entry.getKey());
                    if (oldData == null) {
                        addedData.put(entry.getKey(), entry.getValue().getNodeValue());
                    } else if (NodeType.EPHEMERAL.getName().equals(entry.getValue().getNodeType())
                            && isUnHealthy(entry.getValue().getNodeValue())) {
                        kvStore.bDelete(entry.getKey());
                        iterator.remove();
                    } else if (!oldData.getNodeValue().equals(entry.getValue().getNodeValue())) {
                        updatedData.put(entry.getKey(), entry.getValue().getNodeValue());
                    }
                }
                for (Map.Entry<String, NodeItem> entry : oldDataMap.entrySet()) {
                    if (!newDataMap.containsKey(entry.getKey())) {
                        deletedData.put(entry.getKey(), entry.getValue().getNodeValue());
                    }
                }
                oldDataMap.clear();
                oldDataMap.putAll(newDataMap);
                // trigger listener
                triggerListener(addedData, deletedData, updatedData);
            } catch (Exception ex) {
                log.error("Error in SubscribeCheckTask run method", ex);
            }
        }

        private boolean isUnHealthy(String heartBeat) {
            try {
                // consider this not a valid heartbeat instance, do not check
                if (heartBeat == null || !heartBeat.contains("reportTime")) {
                    return false;
                }
                BaseHeartBeat baseHeartBeat = JSONUtils.parseObject(heartBeat, BaseHeartBeat.class);
                if (baseHeartBeat != null) {
                    return System.currentTimeMillis() - baseHeartBeat.getReportTime() > properties.getHeartBeatTimeOut()
                            .toMillis();
                }
            } catch (Exception ex) {
                log.error("Fail to parse heartBeat : {}", heartBeat, ex);
            }
            return false;
        }

        private void triggerListener(Map<String, String> addedData, Map<String, String> deletedData,
                                     Map<String, String> updatedData) {
            for (Map.Entry<String, List<SubscribeListener>> entry : dataSubScribeMap.entrySet()) {
                String subscribeKey = entry.getKey();
                final List<SubscribeListener> subscribeListeners = entry.getValue();
                if (!addedData.isEmpty()) {
                    triggerListener(addedData, subscribeKey, subscribeListeners, Event.Type.ADD);
                }
                if (!updatedData.isEmpty()) {
                    triggerListener(updatedData, subscribeKey, subscribeListeners, Event.Type.UPDATE);
                }
                if (!deletedData.isEmpty()) {
                    triggerListener(deletedData, subscribeKey, subscribeListeners, Event.Type.REMOVE);
                }
            }
        }

        private Map<String, NodeItem> getNodeDataMap() {
            try {
                final Map<String, NodeItem> nodeItemMap = new HashMap<>();
                final List<KVEntry> entryList = kvStore.bScan(RegistryNodeType.ALL_SERVERS.getRegistryPath(),
                        RegistryNodeType.ALL_SERVERS.getRegistryPath() + Constants.SINGLE_SLASH
                                + Constants.RAFT_END_KEY);

                for (KVEntry kvEntry : entryList) {
                    final String entryKey = readUtf8(kvEntry.getKey());
                    final String compositeValue = readUtf8(kvEntry.getValue());

                    if (StringUtils.isEmpty(compositeValue)
                            || !entryKey.startsWith(RegistryNodeType.ALL_SERVERS.getRegistryPath())) {
                        continue;
                    }

                    String[] nodeTypeAndValue = compositeValue.split(Constants.AT_SIGN);
                    if (nodeTypeAndValue.length < 2) {
                        continue;
                    }
                    String nodeType = nodeTypeAndValue[0];
                    String nodeValue = nodeTypeAndValue[1];

                    nodeItemMap.put(entryKey, NodeItem.builder().nodeValue(nodeValue).nodeType(nodeType).build());
                }
                return nodeItemMap;
            } catch (Exception ex) {
                log.error("Fail to getNodeDataMap", ex);
                return null;
            }
        }

        private void triggerListener(Map<String, String> nodeDataMap, String subscribeKey,
                                     List<SubscribeListener> subscribeListeners, Event.Type type) {
            for (Map.Entry<String, String> entry : nodeDataMap.entrySet()) {
                final String key = entry.getKey();
                if (key.startsWith(subscribeKey)) {
                    subscribeListeners
                            .forEach(listener -> listener.notify(new Event(key, key, entry.getValue(), type)));
                }
            }
        }
    }

    @Override
    public void close() {
        log.info("closing raft subscribe data manager");
        dataSubScribeMap.clear();
        scheduledExecutorService.shutdown();
        log.info("raft subscribe data manager closed successfully");
    }
}
