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

package org.apache.dolphinscheduler.plugin.registry.mysql;

import lombok.NonNull;
import org.apache.dolphinscheduler.plugin.registry.mysql.task.EphemeralDateManager;
import org.apache.dolphinscheduler.plugin.registry.mysql.task.RegistryLockManager;
import org.apache.dolphinscheduler.plugin.registry.mysql.task.SubscribeDataManager;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collection;

/**
 * This is one of the implementation of {@link Registry}, with this implementation, you need to rely on mysql database to
 * store the DolphinScheduler master/worker's metadata and do the server registry/unRegistry.
 */
@Component
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "mysql")
public class MysqlRegistry implements Registry {

    private static Logger LOGGER = LoggerFactory.getLogger(MysqlRegistry.class);

    private final MysqlRegistryProperties mysqlRegistryProperties;
    private final EphemeralDateManager ephemeralDateManager;
    private final SubscribeDataManager subscribeDataManager;
    private final RegistryLockManager registryLockManager;
    private final MysqlOperator mysqlOperator;

    public MysqlRegistry(MysqlRegistryProperties mysqlRegistryProperties) {
        this.mysqlOperator = new MysqlOperator(mysqlRegistryProperties);
        mysqlOperator.clearExpireLock();
        mysqlOperator.clearExpireEphemeralDate();
        this.mysqlRegistryProperties = mysqlRegistryProperties;
        this.ephemeralDateManager = new EphemeralDateManager(mysqlRegistryProperties, mysqlOperator);
        this.subscribeDataManager = new SubscribeDataManager(mysqlRegistryProperties, mysqlOperator);
        this.registryLockManager = new RegistryLockManager(mysqlRegistryProperties, mysqlOperator);
        LOGGER.info("Initialize Mysql Registry...");
    }

    @PostConstruct
    public void start() {
        LOGGER.info("Starting Mysql Registry...");
        // start a mysql connect check
        ephemeralDateManager.start();
        subscribeDataManager.start();
        registryLockManager.start();
        LOGGER.info("Started Mysql Registry...");
    }

    @Override
    public void connectUntilTimeout(@NonNull Duration timeout) throws RegistryException {
        long beginTimeMillis = System.currentTimeMillis();
        long endTimeMills = timeout.getSeconds() <= 0 ? Long.MAX_VALUE : beginTimeMillis + timeout.toMillis();
        while (true) {
            if (System.currentTimeMillis() > endTimeMills) {
                throw new RegistryException(
                        String.format("Cannot connect to mysql registry in %s s", timeout.getSeconds()));
            }
            if (ephemeralDateManager.getConnectionState() == ConnectionState.CONNECTED) {
                return;
            }
            try {
                Thread.sleep(mysqlRegistryProperties.getTermRefreshInterval().toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RegistryException("Cannot connect to mysql registry due to interrupted exception", e);
            }
        }
    }

    @Override
    public boolean subscribe(String path, SubscribeListener listener) {
        // new a schedule thread to query the path, if the path
        subscribeDataManager.addListener(path, listener);
        return true;
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
        // get the key value
        return subscribeDataManager.getData(key);
    }

    @Override
    public void put(String key, String value, boolean deleteOnDisconnect) {
        try {
            if (deleteOnDisconnect) {
                // when put a ephemeralData will new a scheduler thread to update it
                ephemeralDateManager.insertOrUpdateEphemeralData(key, value);
            } else {
                mysqlOperator.insertOrUpdatePersistentData(key, value);
            }
        } catch (Exception ex) {
            throw new RegistryException(String.format("put key:%s, value:%s error", key, value), ex);
        }
    }

    @Override
    public void delete(String key) {
        try {
            mysqlOperator.deleteEphemeralData(key);
            mysqlOperator.deletePersistentData(key);
        } catch (Exception e) {
            throw new RegistryException(String.format("Delete key: %s error", key), e);
        }
    }

    @Override
    public Collection<String> children(String key) {
        try {
            return mysqlOperator.getChildren(key);
        } catch (SQLException e) {
            throw new RegistryException(String.format("Get key: %s children error", key), e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            return mysqlOperator.existKey(key);
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
    public boolean releaseLock(String key) {
        registryLockManager.releaseLock(key);
        return true;
    }

    @Override
    public void close() {
        LOGGER.info("Closing Mysql Registry...");
        // remove the current Ephemeral node, if can connect to mysql
        try (
                EphemeralDateManager closed1 = ephemeralDateManager;
                SubscribeDataManager close2 = subscribeDataManager;
                RegistryLockManager close3 = registryLockManager;
                MysqlOperator closed4 = mysqlOperator) {
        } catch (Exception e) {
            LOGGER.error("Close Mysql Registry error", e);
        }
        LOGGER.info("Closed Mysql Registry...");
    }
}
