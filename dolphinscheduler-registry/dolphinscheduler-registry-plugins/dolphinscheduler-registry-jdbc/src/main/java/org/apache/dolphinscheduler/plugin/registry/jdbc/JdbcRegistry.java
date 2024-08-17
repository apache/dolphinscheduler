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

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.plugin.registry.jdbc.client.JdbcRegistryClient;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.DataType;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.JdbcRegistryDataDTO;
import org.apache.dolphinscheduler.plugin.registry.jdbc.server.ConnectionStateListener;
import org.apache.dolphinscheduler.plugin.registry.jdbc.server.IJdbcRegistryServer;
import org.apache.dolphinscheduler.plugin.registry.jdbc.server.JdbcRegistryDataChangeListener;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * This is one of the implementation of {@link Registry}, with this implementation, you need to rely on mysql database to
 * store the DolphinScheduler master/worker's metadata and do the server registry/unRegistry.
 */
@Slf4j
public final class JdbcRegistry implements Registry {

    private final JdbcRegistryProperties jdbcRegistryProperties;
    private final JdbcRegistryClient jdbcRegistryClient;

    private final IJdbcRegistryServer jdbcRegistryServer;

    JdbcRegistry(JdbcRegistryProperties jdbcRegistryProperties, IJdbcRegistryServer jdbcRegistryServer) {
        this.jdbcRegistryProperties = jdbcRegistryProperties;
        this.jdbcRegistryServer = jdbcRegistryServer;
        this.jdbcRegistryClient = new JdbcRegistryClient(jdbcRegistryProperties, jdbcRegistryServer);
        log.info("Initialize Jdbc Registry...");
    }

    @Override
    public void start() {
        log.info("Starting Jdbc Registry...");
        jdbcRegistryServer.start();
        jdbcRegistryClient.start();
        log.info("Started Jdbc Registry...");
    }

    @Override
    public boolean isConnected() {
        return jdbcRegistryClient.isConnectivity();
    }

    @Override
    public void connectUntilTimeout(@NonNull Duration timeout) throws RegistryException {
        long beginTimeMillis = System.currentTimeMillis();
        long endTimeMills = timeout.getSeconds() <= 0 ? Long.MAX_VALUE : beginTimeMillis + timeout.toMillis();
        while (true) {
            if (System.currentTimeMillis() > endTimeMills) {
                throw new RegistryException(
                        String.format("Cannot connect to jdbc registry in %s s", timeout.getSeconds()));
            }
            if (jdbcRegistryClient.isConnectivity()) {
                return;
            }
            try {
                Thread.sleep(jdbcRegistryProperties.getHeartbeatRefreshInterval().toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RegistryException("Cannot connect to jdbc registry due to interrupted exception", e);
            }
        }
    }

    @Override
    public void subscribe(String path, SubscribeListener listener) {
        checkNotNull(path);
        checkNotNull(listener);
        jdbcRegistryClient.subscribeJdbcRegistryDataChange(new JdbcRegistryDataChangeListener() {

            @Override
            public void onJdbcRegistryDataChanged(String key, String value) {
                if (!key.startsWith(path)) {
                    return;
                }
                Event event = Event.builder()
                        .key(key)
                        .path(path)
                        .data(value)
                        .type(Event.Type.UPDATE)
                        .build();
                listener.notify(event);
            }

            @Override
            public void onJdbcRegistryDataDeleted(String key) {
                if (!key.startsWith(path)) {
                    return;
                }
                Event event = Event.builder()
                        .key(key)
                        .path(key)
                        .type(Event.Type.REMOVE)
                        .build();
                listener.notify(event);
            }

            @Override
            public void onJdbcRegistryDataAdded(String key, String value) {
                if (!key.startsWith(path)) {
                    return;
                }
                Event event = Event.builder()
                        .key(key)
                        .path(key)
                        .data(value)
                        .type(Event.Type.ADD)
                        .build();
                listener.notify(event);
            }
        });
    }

    @Override
    public void addConnectionStateListener(ConnectionListener listener) {
        checkNotNull(listener);
        jdbcRegistryClient.subscribeConnectionStateChange(new ConnectionStateListener() {

            @Override
            public void onConnected() {
                listener.onUpdate(ConnectionState.CONNECTED);
            }

            @Override
            public void onDisConnected() {
                listener.onUpdate(ConnectionState.DISCONNECTED);
            }

            @Override
            public void onReconnected() {
                listener.onUpdate(ConnectionState.RECONNECTED);
            }
        });
    }

    @Override
    public String get(String key) {
        try {
            // get the key value
            // Directly get from the db?
            Optional<JdbcRegistryDataDTO> jdbcRegistryDataOptional = jdbcRegistryClient.getJdbcRegistryDataByKey(key);
            if (!jdbcRegistryDataOptional.isPresent()) {
                throw new RegistryException("key: " + key + " not exist");
            }
            return jdbcRegistryDataOptional.get().getDataValue();
        } catch (RegistryException registryException) {
            throw registryException;
        } catch (Exception e) {
            throw new RegistryException(String.format("Get key: %s error", key), e);
        }
    }

    @Override
    public void put(String key, String value, boolean deleteOnDisconnect) {
        try {
            DataType dataType = deleteOnDisconnect ? DataType.EPHEMERAL : DataType.PERSISTENT;
            jdbcRegistryClient.putJdbcRegistryData(key, value, dataType);
        } catch (Exception ex) {
            throw new RegistryException(String.format("put key:%s, value:%s error", key, value), ex);
        }
    }

    @Override
    public void delete(String key) {
        try {
            jdbcRegistryClient.deleteJdbcRegistryDataByKey(key);
        } catch (Exception e) {
            throw new RegistryException(String.format("Delete key: %s error", key), e);
        }
    }

    @Override
    public Collection<String> children(String key) {
        try {
            List<JdbcRegistryDataDTO> children = jdbcRegistryClient.listJdbcRegistryDataChildren(key);
            return children
                    .stream()
                    .map(JdbcRegistryDataDTO::getDataKey)
                    .filter(fullPath -> fullPath.length() > key.length())
                    .map(fullPath -> StringUtils.substringBefore(fullPath.substring(key.length() + 1), "/"))
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RegistryException(String.format("Get key: %s children error", key), e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            return jdbcRegistryClient.existJdbcRegistryDataKey(key);
        } catch (Exception e) {
            throw new RegistryException(String.format("Check key: %s exist error", key), e);
        }
    }

    @Override
    public boolean acquireLock(String key) {
        try {
            jdbcRegistryClient.acquireJdbcRegistryLock(key);
            return true;
        } catch (RegistryException e) {
            throw e;
        } catch (Exception e) {
            throw new RegistryException(String.format("Acquire lock: %s error", key), e);
        }
    }

    @Override
    public boolean acquireLock(String key, long timeout) {
        try {
            return jdbcRegistryClient.acquireJdbcRegistryLock(key, timeout);
        } catch (RegistryException e) {
            throw e;
        } catch (Exception e) {
            throw new RegistryException(String.format("Acquire lock: %s error", key), e);
        }
    }

    @Override
    public boolean releaseLock(String key) {
        jdbcRegistryClient.releaseJdbcRegistryLock(key);
        return true;
    }

    @Override
    public void close() {
        log.info("Closing Jdbc Registry...");
        // remove the current Ephemeral node, if can connect to jdbc
        try (JdbcRegistryClient closed1 = jdbcRegistryClient) {
        } catch (Exception e) {
            log.error("Close Jdbc Registry error", e);
        }
        log.info("Closed Jdbc Registry...");
    }
}
