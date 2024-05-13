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

package org.apache.dolphinscheduler.plugin.registry.jdbc;

import org.apache.dolphinscheduler.plugin.registry.jdbc.model.JdbcRegistryData;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Used to refresh if the subscribe path has been changed.
 */
@Slf4j
class SubscribeDataManager implements AutoCloseable {

    private final JdbcOperator jdbcOperator;
    private final JdbcRegistryProperties registryProperties;
    private final Map<String, List<SubscribeListener>> dataSubScribeMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService dataSubscribeCheckThreadPool;
    private final Map<String, JdbcRegistryData> jdbcRegistryDataMap = new ConcurrentHashMap<>();

    SubscribeDataManager(JdbcRegistryProperties registryProperties, JdbcOperator jdbcOperator) {
        this.registryProperties = registryProperties;
        this.jdbcOperator = jdbcOperator;
        this.dataSubscribeCheckThreadPool = Executors.newScheduledThreadPool(
                1,
                new ThreadFactoryBuilder().setNameFormat("JdbcRegistrySubscribeDataCheckThread").setDaemon(true)
                        .build());
    }

    public void start() {
        dataSubscribeCheckThreadPool.scheduleWithFixedDelay(
                new RegistrySubscribeDataCheckTask(dataSubScribeMap, jdbcOperator, jdbcRegistryDataMap),
                registryProperties.getTermRefreshInterval().toMillis(),
                registryProperties.getTermRefreshInterval().toMillis(),
                TimeUnit.MILLISECONDS);
    }

    public void addListener(String path, SubscribeListener subscribeListener) {
        dataSubScribeMap.computeIfAbsent(path, k -> new ArrayList<>()).add(subscribeListener);
    }

    public void removeListener(String path) {
        dataSubScribeMap.remove(path);
    }

    public JdbcRegistryData getData(String path) {
        return jdbcRegistryDataMap.get(path);
    }

    @Override
    public void close() {
        dataSubscribeCheckThreadPool.shutdownNow();
        dataSubScribeMap.clear();
    }

    @RequiredArgsConstructor
    static class RegistrySubscribeDataCheckTask implements Runnable {

        private final Map<String, List<SubscribeListener>> dataSubScribeMap;
        private final JdbcOperator jdbcOperator;
        private final Map<String, JdbcRegistryData> jdbcRegistryDataMap;

        @Override
        public void run() {
            // query the full data from database, and update the jdbcRegistryDataMap
            try {
                Map<String, JdbcRegistryData> currentJdbcDataMap = jdbcOperator.queryAllJdbcRegistryData()
                        .stream()
                        .collect(Collectors.toMap(JdbcRegistryData::getDataKey, Function.identity()));
                // find the different
                List<JdbcRegistryData> addedData = new ArrayList<>();
                List<JdbcRegistryData> deletedData = new ArrayList<>();
                List<JdbcRegistryData> updatedData = new ArrayList<>();

                for (Map.Entry<String, JdbcRegistryData> entry : currentJdbcDataMap.entrySet()) {
                    JdbcRegistryData newData = entry.getValue();
                    JdbcRegistryData oldData = jdbcRegistryDataMap.get(entry.getKey());
                    if (oldData == null) {
                        addedData.add(newData);
                    } else {
                        if (!entry.getValue().getLastUpdateTime().equals(oldData.getLastUpdateTime())) {
                            updatedData.add(newData);
                        }
                    }
                }

                for (Map.Entry<String, JdbcRegistryData> entry : jdbcRegistryDataMap.entrySet()) {
                    if (!currentJdbcDataMap.containsKey(entry.getKey())) {
                        deletedData.add(entry.getValue());
                    }
                }
                jdbcRegistryDataMap.clear();
                jdbcRegistryDataMap.putAll(currentJdbcDataMap);
                // trigger listener
                for (Map.Entry<String, List<SubscribeListener>> entry : dataSubScribeMap.entrySet()) {
                    String subscribeKey = entry.getKey();
                    List<SubscribeListener> subscribeListeners = entry.getValue();
                    triggerListener(addedData, subscribeKey, subscribeListeners, Event.Type.ADD);
                    triggerListener(deletedData, subscribeKey, subscribeListeners, Event.Type.REMOVE);
                    triggerListener(updatedData, subscribeKey, subscribeListeners, Event.Type.UPDATE);
                }
            } catch (Exception e) {
                log.error("Query data from jdbc registry error");
            }
        }

        private void triggerListener(List<JdbcRegistryData> dataList,
                                     String subscribeKey,
                                     List<SubscribeListener> subscribeListeners,
                                     Event.Type type) {
            for (JdbcRegistryData data : dataList) {
                if (data.getDataKey().startsWith(subscribeKey)) {
                    subscribeListeners.forEach(subscribeListener -> subscribeListener
                            .notify(new Event(data.getDataKey(), data.getDataKey(), data.getDataValue(), type)));
                }
            }
        }

    }
}
