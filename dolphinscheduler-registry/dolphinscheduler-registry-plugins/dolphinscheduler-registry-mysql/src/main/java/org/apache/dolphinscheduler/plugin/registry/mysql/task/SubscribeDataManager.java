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

package org.apache.dolphinscheduler.plugin.registry.mysql.task;

import org.apache.dolphinscheduler.plugin.registry.mysql.MysqlOperator;
import org.apache.dolphinscheduler.plugin.registry.mysql.MysqlRegistryProperties;
import org.apache.dolphinscheduler.plugin.registry.mysql.model.MysqlRegistryData;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.RequiredArgsConstructor;

/**
 * Used to refresh if the subscribe path has been changed.
 */
public class SubscribeDataManager implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscribeDataManager.class);

    private final MysqlOperator mysqlOperator;
    private final MysqlRegistryProperties registryProperties;
    private final Map<String, List<SubscribeListener>> dataSubScribeMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService dataSubscribeCheckThreadPool;
    private final Map<String, MysqlRegistryData> mysqlRegistryDataMap = new ConcurrentHashMap<>();

    public SubscribeDataManager(MysqlRegistryProperties registryProperties, MysqlOperator mysqlOperator) {
        this.registryProperties = registryProperties;
        this.mysqlOperator = mysqlOperator;
        this.dataSubscribeCheckThreadPool = Executors.newScheduledThreadPool(
                1,
                new ThreadFactoryBuilder().setNameFormat("MysqlRegistrySubscribeDataCheckThread").setDaemon(true).build());
    }

    public void start() {
        dataSubscribeCheckThreadPool.scheduleWithFixedDelay(
                new RegistrySubscribeDataCheckTask(dataSubScribeMap, mysqlOperator, mysqlRegistryDataMap),
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

    public String getData(String path) {
        MysqlRegistryData mysqlRegistryData = mysqlRegistryDataMap.get(path);
        if (mysqlRegistryData == null) {
            return null;
        }
        return mysqlRegistryData.getData();
    }

    @Override
    public void close() {
        dataSubscribeCheckThreadPool.shutdownNow();
        dataSubScribeMap.clear();
    }

    @RequiredArgsConstructor
    static class RegistrySubscribeDataCheckTask implements Runnable {

        private final Map<String, List<SubscribeListener>> dataSubScribeMap;
        private final MysqlOperator mysqlOperator;
        private final Map<String, MysqlRegistryData> mysqlRegistryDataMap;

        @Override
        public void run() {
            // query the full data from database, and update the mysqlRegistryDataMap
            try {
                Map<String, MysqlRegistryData> currentMysqlDataMap = mysqlOperator.queryAllMysqlRegistryData()
                        .stream()
                        .collect(Collectors.toMap(MysqlRegistryData::getKey, Function.identity()));
                // find the different
                List<MysqlRegistryData> addedData = new ArrayList<>();
                List<MysqlRegistryData> deletedData = new ArrayList<>();
                List<MysqlRegistryData> updatedData = new ArrayList<>();
                for (Map.Entry<String, MysqlRegistryData> entry : currentMysqlDataMap.entrySet()) {
                    MysqlRegistryData newData = entry.getValue();
                    MysqlRegistryData oldData = mysqlRegistryDataMap.get(entry.getKey());
                    if (oldData == null) {
                        addedData.add(newData);
                    } else {
                        if (!entry.getValue().getLastUpdateTime().equals(oldData.getLastUpdateTime())) {
                            updatedData.add(newData);
                        }
                    }
                }
                for (Map.Entry<String, MysqlRegistryData> entry : mysqlRegistryDataMap.entrySet()) {
                    if (!currentMysqlDataMap.containsKey(entry.getKey())) {
                        deletedData.add(entry.getValue());
                    }
                }
                mysqlRegistryDataMap.clear();
                mysqlRegistryDataMap.putAll(currentMysqlDataMap);
                // trigger listener
                for (Map.Entry<String, List<SubscribeListener>> entry : dataSubScribeMap.entrySet()) {
                    String subscribeKey = entry.getKey();
                    List<SubscribeListener> subscribeListeners = entry.getValue();
                    triggerListener(addedData, subscribeKey, subscribeListeners, Event.Type.ADD);
                    triggerListener(deletedData, subscribeKey, subscribeListeners, Event.Type.REMOVE);
                    triggerListener(updatedData, subscribeKey, subscribeListeners, Event.Type.UPDATE);
                }
            } catch (Exception e) {
                LOGGER.error("Query data from mysql registry error");
            }
        }

        private void triggerListener(List<MysqlRegistryData> dataList,
                                     String subscribeKey,
                                     List<SubscribeListener> subscribeListeners,
                                     Event.Type type) {
            for (MysqlRegistryData data : dataList) {
                if (data.getKey().startsWith(subscribeKey)) {
                    subscribeListeners.forEach(subscribeListener ->
                            subscribeListener.notify(new Event(data.getKey(), data.getKey(), data.getData(), type)));
                }
            }
        }

    }
}
