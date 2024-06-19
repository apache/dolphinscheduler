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
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Collection;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * This is one of the implementation of {@link Registry}, with this implementation, you need to rely on mysql database to
 * store the DolphinScheduler master/worker's metadata and do the server registry/unRegistry.
 */
@Slf4j
public final class JdbcRegistry implements Registry {

    private final JdbcRegistryProperties jdbcRegistryProperties;
    private final EphemeralDateManager ephemeralDateManager;
    private final SubscribeDataManager subscribeDataManager;
    private final RegistryLockManager registryLockManager;
    private final JdbcOperator jdbcOperator;

    JdbcRegistry(JdbcRegistryProperties jdbcRegistryProperties,
                 JdbcOperator jdbcOperator) {
        this.jdbcOperator = jdbcOperator;
        jdbcOperator.clearExpireLock();
        jdbcOperator.clearExpireEphemeralDate();
        this.jdbcRegistryProperties = jdbcRegistryProperties;
        this.ephemeralDateManager = new EphemeralDateManager(jdbcRegistryProperties, jdbcOperator);
        this.subscribeDataManager = new SubscribeDataManager(jdbcRegistryProperties, jdbcOperator);
        this.registryLockManager = new RegistryLockManager(jdbcRegistryProperties, jdbcOperator);
        log.info("Initialize Jdbc Registry...");
    }

    @Override
    public void start() {
        log.info("Starting Jdbc Registry...");
        // start a jdbc connect check
        ephemeralDateManager.start();
        subscribeDataManager.start();
        registryLockManager.start();
        log.info("Started Jdbc Registry...");
    }

    @Override
    public boolean isConnected() {
        jdbcOperator.healthCheck();
        return true;
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
            if (ephemeralDateManager.getConnectionState() == ConnectionState.CONNECTED) {
                return;
            }
            try {
                Thread.sleep(jdbcRegistryProperties.getTermRefreshInterval().toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RegistryException("Cannot connect to jdbc registry due to interrupted exception", e);
            }
        }
    }

    @Override
    public void subscribe(String path, SubscribeListener listener) {
        // new a schedule thread to query the path, if the path
        subscribeDataManager.addListener(path, listener);
    }

    @Override
    public void unsubscribe(String path) {
        subscribeDataManager.removeListener(path);
    }

    @Override
    public void addConnectionStateListener(ConnectionListener listener) {
        // check the current connection
        ephemeralDateManager.addConnectionListener(listener);
    }

    @Override
    public String get(String key) {
        try {
            // get the key value
            JdbcRegistryData data = jdbcOperator.getData(key);
            if (data == null) {
                throw new RegistryException("key: " + key + " not exist");
            }
            return data.getDataValue();
        } catch (RegistryException registryException) {
            throw registryException;
        } catch (Exception e) {
            throw new RegistryException(String.format("Get key: %s error", key), e);
        }
    }

    @Override
    public void put(String key, String value, boolean deleteOnDisconnect) {
        try {
            if (deleteOnDisconnect) {
                // when put a ephemeralData will new a scheduler thread to update it
                ephemeralDateManager.insertOrUpdateEphemeralData(key, value);
            } else {
                jdbcOperator.insertOrUpdatePersistentData(key, value);
            }
        } catch (Exception ex) {
            throw new RegistryException(String.format("put key:%s, value:%s error", key, value), ex);
        }
    }

    @Override
    public void delete(String key) {
        try {
            jdbcOperator.deleteDataByKey(key);
        } catch (Exception e) {
            throw new RegistryException(String.format("Delete key: %s error", key), e);
        }
    }

    @Override
    public Collection<String> children(String key) {
        try {
            return jdbcOperator.getChildren(key);
        } catch (SQLException e) {
            throw new RegistryException(String.format("Get key: %s children error", key), e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            return jdbcOperator.existKey(key);
        } catch (Exception e) {
            throw new RegistryException(String.format("Check key: %s exist error", key), e);
        }
    }

    @Override
    public boolean acquireLock(String key) {
        try {
            registryLockManager.acquireLock(key);
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
            return registryLockManager.acquireLock(key, timeout);
        } catch (RegistryException e) {
            throw e;
        } catch (Exception e) {
            throw new RegistryException(String.format("Acquire lock: %s error", key), e);
        }
    }

    @Override
    public boolean releaseLock(String key) {
        registryLockManager.releaseLock(key);
        return true;
    }

    @Override
    public void close() {
        log.info("Closing Jdbc Registry...");
        // remove the current Ephemeral node, if can connect to jdbc
        try (
                EphemeralDateManager closed1 = ephemeralDateManager;
                SubscribeDataManager close2 = subscribeDataManager;
                RegistryLockManager close3 = registryLockManager) {
        } catch (Exception e) {
            log.error("Close Jdbc Registry error", e);
        }
        log.info("Closed Jdbc Registry...");
    }
}
