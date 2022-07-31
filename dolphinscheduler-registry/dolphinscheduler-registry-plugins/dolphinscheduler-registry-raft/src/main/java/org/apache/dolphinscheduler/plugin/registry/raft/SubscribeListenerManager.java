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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.HeartBeat;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.storage.KVEntry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class SubscribeListenerManager implements AutoCloseable {

    private final Map<String, List<SubscribeListener>> dataSubScribeMap = new ConcurrentHashMap<>();

    private final RaftRegistryProperties properties;

    private final RheaKVStore kvStore;

    private static final String REGEX = "\\d{1,3}(\\.\\d{1,3}){3,5}:\\d+";

    public SubscribeListenerManager(RaftRegistryProperties properties, RheaKVStore kvStore) {
        this.properties = properties;
        this.kvStore = kvStore;
    }

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(
            1,
            new ThreadFactoryBuilder().setNameFormat("SubscribeListenerCheckThread").setDaemon(true).build());

    public void start() {
        scheduledExecutorService.scheduleWithFixedDelay(new SubscribeCheckTask(),
                properties.getListenerCheckInterval().toMillis(),
                properties.getListenerCheckInterval().toMillis(),
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        dataSubScribeMap.clear();
        scheduledExecutorService.shutdown();
    }

    public boolean addSubscribeListener(String path, SubscribeListener listener) {
        return dataSubScribeMap.computeIfAbsent(path, k -> new ArrayList<>()).add(listener);
    }

    public void removeSubscribeListener(String path) {
        dataSubScribeMap.remove(path);
    }

    private class SubscribeCheckTask implements Runnable {

        private final Map<String, String> nodeDataMap = new ConcurrentHashMap<>();

        @Override
        public void run() {
            if (dataSubScribeMap.isEmpty()) {
                return;
            }
            final Map<String, String> currentNodeDataMap = getNodeDataMap();
            if (currentNodeDataMap.isEmpty()) {
                return;
            }
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

        /**
         * get node path and heartbeat map
         * @return
         */
        private Map<String, String> getNodeDataMap() {
            Pattern pattern = Pattern.compile(REGEX);
            HashMap<String, String> nodeDataMap = new HashMap<>();
            final List<KVEntry> entryList = kvStore.bScan(Constants.REGISTRY_DOLPHINSCHEDULER_NODE + Constants.SINGLE_SLASH, null);
            for (KVEntry kvEntry : entryList) {
                final String value = readUtf8(kvEntry.getValue());
                final String entryKey = readUtf8(kvEntry.getKey());
                if (StringUtils.isEmpty(value) || pattern.matcher(value).find() || !entryKey.startsWith(Constants.REGISTRY_DOLPHINSCHEDULER_NODE)) {
                    continue;
                }
                nodeDataMap.put(entryKey, value);
            }
            return nodeDataMap;
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
}
