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

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.registry.jdbc.JdbcRegistryProperties;
import org.apache.dolphinscheduler.plugin.registry.jdbc.client.IJdbcRegistryClient;
import org.apache.dolphinscheduler.plugin.registry.jdbc.client.JdbcRegistryClientIdentify;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.DataType;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.JdbcRegistryClientHeartbeatDTO;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.JdbcRegistryDataDTO;
import org.apache.dolphinscheduler.plugin.registry.jdbc.repository.JdbcRegistryClientRepository;
import org.apache.dolphinscheduler.plugin.registry.jdbc.repository.JdbcRegistryDataChangeEventRepository;
import org.apache.dolphinscheduler.plugin.registry.jdbc.repository.JdbcRegistryDataRepository;
import org.apache.dolphinscheduler.plugin.registry.jdbc.repository.JdbcRegistryLockRepository;
import org.apache.dolphinscheduler.registry.api.RegistryException;

import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Lists;

/**
 * The JdbcRegistryServer will manage the client, once a client is disconnected, the server will remove the client from the registry, and remove it's related data and lock.
 */
@Slf4j
public class JdbcRegistryServer implements IJdbcRegistryServer {

    private final JdbcRegistryProperties jdbcRegistryProperties;

    private final JdbcRegistryLockRepository jdbcRegistryLockRepository;

    private final JdbcRegistryClientRepository jdbcRegistryClientRepository;

    private final JdbcRegistryDataManager jdbcRegistryDataManager;

    private final JdbcRegistryLockManager jdbcRegistryLockManager;

    private final AtomicReference<JdbcRegistryServerState> serverState =
            new AtomicReference<>(JdbcRegistryServerState.INIT);

    private final List<ConnectionStateListener> connectionStateListeners = new CopyOnWriteArrayList<>();

    private final ReentrantLock lifecycleLock = new ReentrantLock();

    private final Map<JdbcRegistryClientIdentify, ClientRegistration> clientRegistrations = new LinkedHashMap<>();

    private final CompletableFuture<Void> closeCompletion = new CompletableFuture<>();

    private final ScheduledExecutorService schedulerThreadExecutor;

    private volatile long lastSuccessHeartbeat;

    private static final class ClientRegistration {

        private final IJdbcRegistryClient client;
        private final JdbcRegistryClientHeartbeatDTO heartbeat;
        private final ReentrantLock lock = new ReentrantLock();
        private volatile boolean active = true;

        private ClientRegistration(IJdbcRegistryClient client, JdbcRegistryClientHeartbeatDTO heartbeat) {
            this.client = client;
            this.heartbeat = heartbeat;
        }
    }

    private static final class HeartbeatUpdateException extends RuntimeException {

        private final ClientRegistration registration;

        private HeartbeatUpdateException(ClientRegistration registration) {
            super("The client heartbeat record no longer exists: " + registration.heartbeat.getId());
            this.registration = registration;
        }
    }

    public JdbcRegistryServer(JdbcRegistryDataRepository jdbcRegistryDataRepository,
                              JdbcRegistryLockRepository jdbcRegistryLockRepository,
                              JdbcRegistryClientRepository jdbcRegistryClientRepository,
                              JdbcRegistryDataChangeEventRepository jdbcRegistryDataChangeEventRepository,
                              JdbcRegistryProperties jdbcRegistryProperties,
                              TransactionTemplate transactionTemplate) {
        this.jdbcRegistryLockRepository = checkNotNull(jdbcRegistryLockRepository);
        this.jdbcRegistryClientRepository = checkNotNull(jdbcRegistryClientRepository);
        this.jdbcRegistryProperties = checkNotNull(jdbcRegistryProperties);
        this.schedulerThreadExecutor = ThreadUtils.newDaemonScheduledExecutorService(
                "ds-jdbc-registry-default-scheduler-thread-%d",
                Runtime.getRuntime().availableProcessors());
        this.jdbcRegistryDataManager = new JdbcRegistryDataManager(
                jdbcRegistryProperties, jdbcRegistryDataRepository, jdbcRegistryDataChangeEventRepository,
                transactionTemplate, schedulerThreadExecutor);
        this.jdbcRegistryLockManager = new JdbcRegistryLockManager(
                jdbcRegistryProperties, jdbcRegistryLockRepository);
        lastSuccessHeartbeat = System.currentTimeMillis();
    }

    @Override
    public void start() {
        lifecycleLock.lock();
        try {
            if (serverState.get() != JdbcRegistryServerState.INIT) {
                // The server is already started or stopped, will not start again.
                return;
            }
            // Start the Purge thread
            // The Purge thread will clear the invalidated data
            purgeInvalidJdbcRegistryMetadata();
            schedulerThreadExecutor.scheduleWithFixedDelay(
                    this::purgeInvalidJdbcRegistryMetadata,
                    jdbcRegistryProperties.getSessionTimeout().toMillis(),
                    jdbcRegistryProperties.getSessionTimeout().toMillis(),
                    TimeUnit.MILLISECONDS);
            jdbcRegistryDataManager.start();
            if (!serverState.compareAndSet(JdbcRegistryServerState.INIT, JdbcRegistryServerState.STARTED)) {
                log.warn("The JdbcRegistryServer state changed before startup completed: {}", serverState.get());
                return;
            }
        } finally {
            lifecycleLock.unlock();
        }

        lifecycleLock.lock();
        try {
            if (serverState.get() != JdbcRegistryServerState.STARTED) {
                return;
            }
        } finally {
            lifecycleLock.unlock();
        }
        doTriggerOnConnectedListener();

        lifecycleLock.lock();
        try {
            if (serverState.get() == JdbcRegistryServerState.STARTED && !schedulerThreadExecutor.isShutdown()) {
                schedulerThreadExecutor.scheduleWithFixedDelay(
                        this::refreshClientsHeartbeat,
                        0,
                        jdbcRegistryProperties.getHeartbeatRefreshInterval().toMillis(),
                        TimeUnit.MILLISECONDS);
            }
        } finally {
            lifecycleLock.unlock();
        }
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

        lifecycleLock.lock();
        try {
            JdbcRegistryServerState currentState = serverState.get();
            if (currentState == JdbcRegistryServerState.STOPPED
                    || currentState == JdbcRegistryServerState.DISCONNECTED) {
                throw new IllegalStateException("Cannot register a client when the JdbcRegistryServer is "
                        + currentState);
            }
            if (clientRegistrations.containsKey(jdbcRegistryClientIdentify)) {
                throw new IllegalArgumentException("The client is already registered: " + jdbcRegistryClientIdentify);
            }
            jdbcRegistryClientRepository.insert(registryClientDTO);
            clientRegistrations.put(
                    jdbcRegistryClientIdentify, new ClientRegistration(jdbcRegistryClient, registryClientDTO));
        } finally {
            lifecycleLock.unlock();
        }
    }

    @Override
    public void deregisterClient(IJdbcRegistryClient jdbcRegistryClient) {
        checkNotNull(jdbcRegistryClient);
        final JdbcRegistryClientIdentify clientIdentify = jdbcRegistryClient.getJdbcRegistryClientIdentify();
        checkNotNull(clientIdentify);

        lifecycleLock.lock();
        try {
            ClientRegistration registration = clientRegistrations.get(clientIdentify);
            if (registration == null || registration.client != jdbcRegistryClient) {
                return;
            }
            registration.active = false;
            clientRegistrations.remove(clientIdentify, registration);
            registration.lock.lock();
            try {
                purgeClientRegistration(registration);
            } finally {
                registration.lock.unlock();
            }
        } finally {
            lifecycleLock.unlock();
        }
    }

    @Override
    public JdbcRegistryServerState getServerState() {
        return serverState.get();
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
                        jdbcRegistryDataChangeListener.onJdbcRegistryDataDeleted(data.getDataKey(),
                                data.getDataValue());
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
        List<ClientRegistration> registrationsToClose;
        boolean waitForClose = false;
        lifecycleLock.lock();
        try {
            JdbcRegistryServerState currentState = serverState.get();
            if (currentState == JdbcRegistryServerState.STOPPED) {
                waitForClose = true;
                registrationsToClose = null;
            } else if (!serverState.compareAndSet(currentState, JdbcRegistryServerState.STOPPED)) {
                log.warn("Failed to stop JdbcRegistryServer from state {}, current state is {}",
                        currentState,
                        serverState.get());
                return;
            } else {
                registrationsToClose = new ArrayList<>(clientRegistrations.values());
                clientRegistrations.clear();
                registrationsToClose.forEach(registration -> registration.active = false);
            }
        } finally {
            lifecycleLock.unlock();
        }

        if (waitForClose) {
            closeCompletion.join();
            return;
        }

        try {
            schedulerThreadExecutor.shutdownNow();
            for (ClientRegistration registration : registrationsToClose) {
                registration.lock.lock();
                try {
                    purgeClientRegistration(registration);
                } finally {
                    registration.lock.unlock();
                }
            }
            try {
                if (!schedulerThreadExecutor.awaitTermination(
                        Math.max(1, jdbcRegistryProperties.getHeartbeatRefreshInterval().toMillis()),
                        TimeUnit.MILLISECONDS)) {
                    log.warn("The JdbcRegistryServer scheduler did not terminate within the close timeout.");
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while closing the JdbcRegistryServer", ex);
            }
            closeCompletion.complete(null);
        } catch (RuntimeException ex) {
            closeCompletion.completeExceptionally(ex);
            throw ex;
        }
    }

    private void purgeInvalidJdbcRegistryMetadata() {
        final StopWatch stopWatch = StopWatch.createStarted();
        JdbcRegistryServerState currentState = getServerState();
        if (currentState == JdbcRegistryServerState.STOPPED
                || currentState == JdbcRegistryServerState.DISCONNECTED) {
            return;
        }
        // remove the client which is already dead from the registry, and remove it's related data and lock.
        final List<JdbcRegistryClientHeartbeatDTO> jdbcRegistryClients = jdbcRegistryClientRepository.queryAll();
        jdbcRegistryClients.stream()
                .filter(JdbcRegistryClientHeartbeatDTO::isDead)
                .filter(jdbcRegistryClient -> jdbcRegistryClientRepository.deleteByIdAndLastHeartbeatTime(
                        jdbcRegistryClient.getId(), jdbcRegistryClient.getLastHeartbeatTime()))
                .forEach(jdbcRegistryClient -> log.info(
                        "Success delete dead jdbcRegistryClient: {}", jdbcRegistryClient.getId()));

        // Remove data and locks only while the database still confirms that the owner has no heartbeat.
        purgeInactiveMetadata(null);
        stopWatch.stop();
        log.debug("Success purge invalid jdbcRegistryMetadata, cost: {} ms", stopWatch.getTime());
    }

    private void purgeClientRegistration(ClientRegistration registration) {
        Long clientId = registration.heartbeat.getId();
        log.info("Begin to delete dead jdbcRegistryClient: {}", clientId);
        jdbcRegistryClientRepository.deleteByIdAndLastHeartbeatTime(
                clientId, registration.heartbeat.getLastHeartbeatTime());
        purgeInactiveMetadata(Lists.newArrayList(clientId));
        log.info("Success delete dead jdbcRegistryClient: {}", clientId);
    }

    private void purgeInactiveMetadata(Collection<Long> candidateClientIds) {
        Set<Long> candidates = candidateClientIds == null
                ? null
                : candidateClientIds.stream().collect(Collectors.toSet());
        jdbcRegistryDataManager.getAllJdbcRegistryData()
                .stream()
                .filter(jdbcRegistryDataDTO -> DataType.EPHEMERAL.name().equals(jdbcRegistryDataDTO.getDataType()))
                .filter(jdbcRegistryDataDTO -> candidates == null
                        || candidates.contains(jdbcRegistryDataDTO.getClientId()))
                .forEach(jdbcRegistryData -> {
                    log.info("Remove the JdbcRegistryData: {} which client is not exist in the registry",
                            jdbcRegistryData);
                    jdbcRegistryDataManager.deleteEphemeralDataIfClientInactive(jdbcRegistryData);
                });
        jdbcRegistryLockRepository.queryAll()
                .stream()
                .filter(jdbcRegistryLockDTO -> candidates == null
                        || candidates.contains(jdbcRegistryLockDTO.getClientId()))
                .forEach(jdbcRegistryLock -> {
                    log.info("Remove the JdbcRegistryLock: {} which client is not exist in the registry",
                            jdbcRegistryLock);
                    jdbcRegistryLockManager.deleteIfInactive(jdbcRegistryLock);
                });
    }

    private void refreshClientsHeartbeat() {
        List<ClientRegistration> registrations;
        lifecycleLock.lock();
        try {
            JdbcRegistryServerState currentState = serverState.get();
            if (currentState == JdbcRegistryServerState.STOPPED
                    || currentState == JdbcRegistryServerState.DISCONNECTED) {
                return;
            }
            registrations = new ArrayList<>(clientRegistrations.values());
        } finally {
            lifecycleLock.unlock();
        }
        if (registrations.isEmpty()) {
            return;
        }

        ClientRegistration failedRegistration = null;
        try {
            long heartbeatTime = System.currentTimeMillis();
            for (ClientRegistration registration : registrations) {
                failedRegistration = registration;
                registration.lock.lock();
                try {
                    if (!registration.active || !isHeartbeatRefreshAllowed()) {
                        continue;
                    }
                    JdbcRegistryClientHeartbeatDTO clone = registration.heartbeat.clone();
                    clone.setLastHeartbeatTime(heartbeatTime);
                    if (!jdbcRegistryClientRepository.updateById(clone)) {
                        log.error("The client heartbeat has expired: {}", registration.heartbeat.getId());
                        throw new HeartbeatUpdateException(registration);
                    }
                    if (registration.active) {
                        registration.heartbeat.setLastHeartbeatTime(clone.getLastHeartbeatTime());
                    }
                } finally {
                    registration.lock.unlock();
                }
            }

            boolean reconnect = false;
            lifecycleLock.lock();
            try {
                JdbcRegistryServerState currentState = serverState.get();
                if (currentState == JdbcRegistryServerState.STOPPED
                        || currentState == JdbcRegistryServerState.DISCONNECTED) {
                    return;
                }
                if (currentState == JdbcRegistryServerState.SUSPENDED) {
                    if (!serverState.compareAndSet(
                            JdbcRegistryServerState.SUSPENDED, JdbcRegistryServerState.STARTED)) {
                        log.debug("Failed to reconnect JdbcRegistryServer; current state is {}", serverState.get());
                        return;
                    }
                    reconnect = true;
                } else if (currentState != JdbcRegistryServerState.STARTED) {
                    return;
                }
                lastSuccessHeartbeat = System.currentTimeMillis();
            } finally {
                lifecycleLock.unlock();
            }
            if (reconnect) {
                doTriggerReconnectedListener();
            }
            log.debug("Success refresh clients: {} heartbeat.",
                    registrations.stream()
                            .map(registration -> registration.client.getJdbcRegistryClientIdentify())
                            .collect(Collectors.toList()));
        } catch (HeartbeatUpdateException ex) {
            log.error("Failed to refresh the client's term", ex);
            handleHeartbeatFailure(ex.registration);
        } catch (Exception ex) {
            log.error("Failed to refresh the client's term", ex);
            handleHeartbeatFailure(failedRegistration);
        }
    }

    private boolean isHeartbeatRefreshAllowed() {
        JdbcRegistryServerState currentState = serverState.get();
        return currentState != JdbcRegistryServerState.STOPPED
                && currentState != JdbcRegistryServerState.DISCONNECTED;
    }

    private void handleHeartbeatFailure(ClientRegistration failedRegistration) {
        if (failedRegistration != null && !failedRegistration.active) {
            return;
        }
        boolean disconnected = false;
        long sessionTimeoutMillis = jdbcRegistryProperties.getSessionTimeout().toMillis();
        lifecycleLock.lock();
        try {
            JdbcRegistryServerState currentState = serverState.get();
            if (currentState == JdbcRegistryServerState.STOPPED
                    || currentState == JdbcRegistryServerState.DISCONNECTED) {
                return;
            }
            boolean sessionTimedOut = System.currentTimeMillis() - lastSuccessHeartbeat > sessionTimeoutMillis;
            if (sessionTimedOut) {
                if ((currentState == JdbcRegistryServerState.STARTED
                        || currentState == JdbcRegistryServerState.SUSPENDED)
                        && serverState.compareAndSet(currentState, JdbcRegistryServerState.DISCONNECTED)) {
                    disconnected = true;
                } else {
                    log.debug("Failed to disconnect JdbcRegistryServer from state {}, current state is {}",
                            currentState,
                            serverState.get());
                }
            } else if (currentState == JdbcRegistryServerState.STARTED
                    && !serverState.compareAndSet(
                            JdbcRegistryServerState.STARTED, JdbcRegistryServerState.SUSPENDED)) {
                log.debug("Failed to suspend JdbcRegistryServer; current state is {}", serverState.get());
            }
        } finally {
            lifecycleLock.unlock();
        }
        if (disconnected) {
            doTriggerOnDisConnectedListener();
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
