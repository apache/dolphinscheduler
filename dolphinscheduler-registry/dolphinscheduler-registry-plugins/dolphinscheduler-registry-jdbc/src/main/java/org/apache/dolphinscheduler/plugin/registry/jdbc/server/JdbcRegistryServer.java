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

package org.apache.dolphinscheduler.plugin.registry.jdbc.server;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.plugin.registry.jdbc.JdbcRegistryProperties;
import org.apache.dolphinscheduler.plugin.registry.jdbc.JdbcRegistryThreadFactory;
import org.apache.dolphinscheduler.plugin.registry.jdbc.client.IJdbcRegistryClient;
import org.apache.dolphinscheduler.plugin.registry.jdbc.client.JdbcRegistryClientIdentify;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.DataType;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.JdbcRegistryClientHeartbeatDTO;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.JdbcRegistryDataDTO;
import org.apache.dolphinscheduler.plugin.registry.jdbc.repository.JdbcRegistryClientRepository;
import org.apache.dolphinscheduler.plugin.registry.jdbc.repository.JdbcRegistryDataChanceEventRepository;
import org.apache.dolphinscheduler.plugin.registry.jdbc.repository.JdbcRegistryDataRepository;
import org.apache.dolphinscheduler.plugin.registry.jdbc.repository.JdbcRegistryLockRepository;
import org.apache.dolphinscheduler.registry.api.RegistryException;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Lists;

/**
 * The JdbcRegistryServer will manage the client, once a client is disconnected, the server will remove the client from the registry, and remove it's related data and lock.
 */
@Slf4j
public class JdbcRegistryServer implements IJdbcRegistryServer {

    private final JdbcRegistryProperties jdbcRegistryProperties;

    private final JdbcRegistryDataRepository jdbcRegistryDataRepository;

    private final JdbcRegistryLockRepository jdbcRegistryLockRepository;

    private final JdbcRegistryClientRepository jdbcRegistryClientRepository;

    private final JdbcRegistryDataManager jdbcRegistryDataManager;

    private final JdbcRegistryLockManager jdbcRegistryLockManager;

    private JdbcRegistryServerState jdbcRegistryServerState;

    private final List<IJdbcRegistryClient> jdbcRegistryClients = new CopyOnWriteArrayList<>();

    private final List<ConnectionStateListener> connectionStateListeners = new CopyOnWriteArrayList<>();

    private final Map<JdbcRegistryClientIdentify, JdbcRegistryClientHeartbeatDTO> jdbcRegistryClientDTOMap =
            new ConcurrentHashMap<>();

    private Long lastSuccessHeartbeat;

    public JdbcRegistryServer(JdbcRegistryDataRepository jdbcRegistryDataRepository,
                              JdbcRegistryLockRepository jdbcRegistryLockRepository,
                              JdbcRegistryClientRepository jdbcRegistryClientRepository,
                              JdbcRegistryDataChanceEventRepository jdbcRegistryDataChanceEventRepository,
                              JdbcRegistryProperties jdbcRegistryProperties) {
        this.jdbcRegistryDataRepository = checkNotNull(jdbcRegistryDataRepository);
        this.jdbcRegistryLockRepository = checkNotNull(jdbcRegistryLockRepository);
        this.jdbcRegistryClientRepository = checkNotNull(jdbcRegistryClientRepository);
        this.jdbcRegistryProperties = checkNotNull(jdbcRegistryProperties);
        this.jdbcRegistryDataManager = new JdbcRegistryDataManager(
                jdbcRegistryProperties, jdbcRegistryDataRepository, jdbcRegistryDataChanceEventRepository);
        this.jdbcRegistryLockManager = new JdbcRegistryLockManager(
                jdbcRegistryProperties, jdbcRegistryLockRepository);
        this.jdbcRegistryServerState = JdbcRegistryServerState.INIT;
        lastSuccessHeartbeat = System.currentTimeMillis();
    }

    @Override
    public void start() {
        if (jdbcRegistryServerState != JdbcRegistryServerState.INIT) {
            // The server is already started or stopped, will not start again.
            return;
        }
        // Purge the previous client to avoid the client is still in the registry.
        purgePreviousJdbcRegistryClient();
        // Start the Purge thread
        // The Purge thread will remove the client from the registry, and remove it's related data and lock.
        // Connect to the database, load the data and lock.
        purgeDeadJdbcRegistryClient();
        JdbcRegistryThreadFactory.getDefaultSchedulerThreadExecutor()
                .scheduleWithFixedDelay(this::purgeDeadJdbcRegistryClient,
                        jdbcRegistryProperties.getHeartbeatRefreshInterval().toMillis(),
                        jdbcRegistryProperties.getHeartbeatRefreshInterval().toMillis(),
                        TimeUnit.MILLISECONDS);
        jdbcRegistryDataManager.start();
        jdbcRegistryServerState = JdbcRegistryServerState.STARTED;
        doTriggerOnConnectedListener();
        JdbcRegistryThreadFactory.getDefaultSchedulerThreadExecutor()
                .scheduleWithFixedDelay(this::refreshClientsHeartbeat,
                        0,
                        jdbcRegistryProperties.getHeartbeatRefreshInterval().toMillis(),
                        TimeUnit.MILLISECONDS);
    }

    @SneakyThrows
    @Override
    public void registerClient(IJdbcRegistryClient jdbcRegistryClient) {
        checkNotNull(jdbcRegistryClient);

        JdbcRegistryClientIdentify jdbcRegistryClientIdentify = jdbcRegistryClient.getJdbcRegistryClientIdentify();
        checkNotNull(jdbcRegistryClientIdentify);

        JdbcRegistryClientHeartbeatDTO registryClientDTO = JdbcRegistryClientHeartbeatDTO.builder()
                .id(jdbcRegistryClientIdentify.getClientId())
                .clientName(jdbcRegistryClientIdentify.getClientName())
                .clientConfig(
                        new JdbcRegistryClientHeartbeatDTO.ClientConfig(
                                jdbcRegistryProperties.getSessionTimeout().toMillis()))
                .createTime(new Date())
                .lastHeartbeatTime(System.currentTimeMillis())
                .build();

        while (jdbcRegistryClientDTOMap.containsKey(jdbcRegistryClientIdentify)) {
            log.warn("The client {} is already exist the registry.", jdbcRegistryClientIdentify.getClientId());
            Thread.sleep(jdbcRegistryProperties.getHeartbeatRefreshInterval().toMillis());
        }
        jdbcRegistryClientRepository.insert(registryClientDTO);
        jdbcRegistryClients.add(jdbcRegistryClient);
        jdbcRegistryClientDTOMap.put(jdbcRegistryClientIdentify, registryClientDTO);
    }

    @Override
    public void deregisterClient(IJdbcRegistryClient jdbcRegistryClient) {
        checkNotNull(jdbcRegistryClient);
        jdbcRegistryClients.remove(jdbcRegistryClient);
        jdbcRegistryClientDTOMap.remove(jdbcRegistryClient.getJdbcRegistryClientIdentify());

        JdbcRegistryClientIdentify jdbcRegistryClientIdentify = jdbcRegistryClient.getJdbcRegistryClientIdentify();
        checkNotNull(jdbcRegistryClientIdentify);

        doPurgeJdbcRegistryClientInDB(Lists.newArrayList(jdbcRegistryClientIdentify.getClientId()));
    }

    @Override
    public JdbcRegistryServerState getServerState() {
        return jdbcRegistryServerState;
    }

    @Override
    public void subscribeConnectionStateChange(ConnectionStateListener connectionStateListener) {
        checkNotNull(connectionStateListener);
        connectionStateListeners.add(connectionStateListener);
    }

    @Override
    public void subscribeJdbcRegistryDataChange(JdbcRegistryDataChangeListener jdbcRegistryDataChangeListener) {
        checkNotNull(jdbcRegistryDataChangeListener);
        jdbcRegistryDataManager.subscribeRegistryRowChange(
                new IRegistryRowChangeNotifier.RegistryRowChangeListener<JdbcRegistryDataDTO>() {

                    @Override
                    public void onRegistryRowUpdated(JdbcRegistryDataDTO data) {
                        jdbcRegistryDataChangeListener.onJdbcRegistryDataChanged(data.getDataKey(),
                                data.getDataValue());
                    }

                    @Override
                    public void onRegistryRowAdded(JdbcRegistryDataDTO data) {
                        jdbcRegistryDataChangeListener.onJdbcRegistryDataAdded(data.getDataKey(), data.getDataValue());
                    }

                    @Override
                    public void onRegistryRowDeleted(JdbcRegistryDataDTO data) {
                        jdbcRegistryDataChangeListener.onJdbcRegistryDataDeleted(data.getDataKey());
                    }
                });
    }

    @Override
    public boolean existJdbcRegistryDataKey(String key) {
        return jdbcRegistryDataManager.existKey(key);
    }

    @Override
    public Optional<JdbcRegistryDataDTO> getJdbcRegistryDataByKey(String key) {
        return jdbcRegistryDataManager.getRegistryDataByKey(key);
    }

    @Override
    public List<JdbcRegistryDataDTO> listJdbcRegistryDataChildren(String key) {
        return jdbcRegistryDataManager.listJdbcRegistryDataChildren(key);
    }

    @Override
    public void putJdbcRegistryData(Long clientId, String key, String value, DataType dataType) {
        jdbcRegistryDataManager.putJdbcRegistryData(clientId, key, value, dataType);
    }

    @Override
    public void deleteJdbcRegistryDataByKey(String key) {
        jdbcRegistryDataManager.deleteJdbcRegistryDataByKey(key);
    }

    @Override
    public void acquireJdbcRegistryLock(Long clientId, String lockKey) {
        try {
            jdbcRegistryLockManager.acquireJdbcRegistryLock(clientId, lockKey);
        } catch (Exception ex) {
            throw new RegistryException("Acquire the lock: " + lockKey + " error", ex);
        }
    }

    @Override
    public boolean acquireJdbcRegistryLock(Long clientId, String lockKey, long timeout) {
        try {
            return jdbcRegistryLockManager.acquireJdbcRegistryLock(clientId, lockKey, timeout);
        } catch (Exception ex) {
            throw new RegistryException("Acquire the lock: " + lockKey + " error", ex);
        }
    }

    @Override
    public void releaseJdbcRegistryLock(Long clientId, String lockKey) {
        try {
            jdbcRegistryLockManager.releaseJdbcRegistryLock(clientId, lockKey);
        } catch (Exception ex) {
            throw new RegistryException("Release the lock: " + lockKey + " error", ex);
        }
    }

    @Override
    public void close() {
        jdbcRegistryServerState = JdbcRegistryServerState.STOPPED;
        JdbcRegistryThreadFactory.getDefaultSchedulerThreadExecutor().shutdown();
        List<Long> clientIds = jdbcRegistryClients.stream()
                .map(IJdbcRegistryClient::getJdbcRegistryClientIdentify)
                .map(JdbcRegistryClientIdentify::getClientId)
                .collect(Collectors.toList());
        doPurgeJdbcRegistryClientInDB(clientIds);
        jdbcRegistryClients.clear();
        jdbcRegistryClientDTOMap.clear();
    }

    private void purgePreviousJdbcRegistryClient() {
        if (jdbcRegistryServerState == JdbcRegistryServerState.STOPPED) {
            return;
        }
        List<Long> previousJdbcRegistryClientIds = jdbcRegistryClientRepository.queryAll()
                .stream()
                .filter(jdbcRegistryClientHeartbeat -> jdbcRegistryClientHeartbeat.getClientName()
                        .equals(jdbcRegistryProperties.getJdbcRegistryClientName()))
                .map(JdbcRegistryClientHeartbeatDTO::getId)
                .collect(Collectors.toList());
        doPurgeJdbcRegistryClientInDB(previousJdbcRegistryClientIds);

    }

    private void purgeDeadJdbcRegistryClient() {
        if (jdbcRegistryServerState == JdbcRegistryServerState.STOPPED) {
            return;
        }
        List<Long> deadJdbcRegistryClientIds = jdbcRegistryClientRepository.queryAll()
                .stream()
                .filter(JdbcRegistryClientHeartbeatDTO::isDead)
                .map(JdbcRegistryClientHeartbeatDTO::getId)
                .collect(Collectors.toList());
        doPurgeJdbcRegistryClientInDB(deadJdbcRegistryClientIds);

    }

    private void doPurgeJdbcRegistryClientInDB(List<Long> jdbcRegistryClientIds) {
        if (CollectionUtils.isEmpty(jdbcRegistryClientIds)) {
            return;
        }
        log.info("Begin to delete dead jdbcRegistryClient: {}", jdbcRegistryClientIds);
        jdbcRegistryDataRepository.deleteEphemeralDateByClientIds(jdbcRegistryClientIds);
        jdbcRegistryLockRepository.deleteByClientIds(jdbcRegistryClientIds);
        jdbcRegistryClientRepository.deleteByIds(jdbcRegistryClientIds);
        log.info("Success delete dead jdbcRegistryClient: {}", jdbcRegistryClientIds);
    }

    private void refreshClientsHeartbeat() {
        if (CollectionUtils.isEmpty(jdbcRegistryClients)) {
            return;
        }
        if (jdbcRegistryServerState == JdbcRegistryServerState.STOPPED) {
            log.warn("The JdbcRegistryServer is STOPPED, will not refresh clients: {} heartbeat.",
                    CollectionUtils.collect(jdbcRegistryClients, IJdbcRegistryClient::getJdbcRegistryClientIdentify));
            return;
        }
        // Refresh the client's term
        try {
            long now = System.currentTimeMillis();
            for (IJdbcRegistryClient jdbcRegistryClient : jdbcRegistryClients) {
                JdbcRegistryClientHeartbeatDTO jdbcRegistryClientHeartbeatDTO =
                        jdbcRegistryClientDTOMap.get(jdbcRegistryClient.getJdbcRegistryClientIdentify());
                if (jdbcRegistryClientHeartbeatDTO == null) {
                    // This may occur when the data in db has been deleted, but the client is still alive.
                    log.error(
                            "The client {} is not found in the registry, will not refresh it's term. (This may happen when the client is removed from the db)",
                            jdbcRegistryClient.getJdbcRegistryClientIdentify().getClientId());
                    continue;
                }
                JdbcRegistryClientHeartbeatDTO clone = jdbcRegistryClientHeartbeatDTO.clone();
                clone.setLastHeartbeatTime(now);
                jdbcRegistryClientRepository.updateById(jdbcRegistryClientHeartbeatDTO);
                jdbcRegistryClientHeartbeatDTO.setLastHeartbeatTime(clone.getLastHeartbeatTime());
            }
            if (jdbcRegistryServerState == JdbcRegistryServerState.SUSPENDED) {
                jdbcRegistryServerState = JdbcRegistryServerState.STARTED;
                doTriggerReconnectedListener();
            }
            lastSuccessHeartbeat = now;
            log.debug("Success refresh clients: {} heartbeat.",
                    CollectionUtils.collect(jdbcRegistryClients, IJdbcRegistryClient::getJdbcRegistryClientIdentify));
        } catch (Exception ex) {
            log.error("Failed to refresh the client's term", ex);
            switch (jdbcRegistryServerState) {
                case STARTED:
                    jdbcRegistryServerState = JdbcRegistryServerState.SUSPENDED;
                    break;
                case SUSPENDED:
                    if (System.currentTimeMillis() - lastSuccessHeartbeat > jdbcRegistryProperties.getSessionTimeout()
                            .toMillis()) {
                        jdbcRegistryServerState = JdbcRegistryServerState.DISCONNECTED;
                        doTriggerOnDisConnectedListener();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void doTriggerReconnectedListener() {
        log.info("Trigger:onReconnected listener.");
        connectionStateListeners.forEach(listener -> {
            try {
                listener.onReconnected();
            } catch (Exception ex) {
                log.error("Trigger:onReconnected failed", ex);
            }
        });
    }

    private void doTriggerOnConnectedListener() {
        log.info("Trigger:onConnected listener.");
        connectionStateListeners.forEach(listener -> {
            try {
                listener.onConnected();
            } catch (Exception ex) {
                log.error("Trigger:onConnected failed", ex);
            }
        });
    }

    private void doTriggerOnDisConnectedListener() {
        log.info("Trigger:onDisConnected listener.");
        connectionStateListeners.forEach(listener -> {
            try {
                listener.onDisConnected();
            } catch (Exception ex) {
                log.error("Trigger:onDisConnected failed", ex);
            }
        });
    }

}
