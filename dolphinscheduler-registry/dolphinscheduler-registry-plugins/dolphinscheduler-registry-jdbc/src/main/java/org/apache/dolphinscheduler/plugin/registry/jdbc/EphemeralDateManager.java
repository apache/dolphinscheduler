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

import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * This thread is used to check the connect state to jdbc.
 */
@Slf4j
class EphemeralDateManager implements AutoCloseable {

    private ConnectionState connectionState;
    private final JdbcOperator jdbcOperator;
    private final JdbcRegistryProperties registryProperties;
    private final List<ConnectionListener> connectionListeners = Collections.synchronizedList(new ArrayList<>());
    private final Set<Long> ephemeralDateIds = Collections.synchronizedSet(new HashSet<>());
    private final ScheduledExecutorService scheduledExecutorService;

    EphemeralDateManager(JdbcRegistryProperties registryProperties, JdbcOperator jdbcOperator) {
        this.registryProperties = registryProperties;
        this.jdbcOperator = checkNotNull(jdbcOperator);
        this.scheduledExecutorService = Executors.newScheduledThreadPool(
                1,
                new ThreadFactoryBuilder().setNameFormat("EphemeralDateTermRefreshThread").setDaemon(true).build());
    }

    public void start() {
        this.scheduledExecutorService.scheduleWithFixedDelay(
                new EphemeralDateTermRefreshTask(jdbcOperator, connectionListeners, ephemeralDateIds),
                registryProperties.getTermRefreshInterval().toMillis(),
                registryProperties.getTermRefreshInterval().toMillis(),
                TimeUnit.MILLISECONDS);
    }

    public void addConnectionListener(ConnectionListener connectionListener) {
        connectionListeners.add(connectionListener);
    }

    public long insertOrUpdateEphemeralData(String key, String value) throws SQLException {
        long ephemeralId = jdbcOperator.insertOrUpdateEphemeralData(key, value);
        ephemeralDateIds.add(ephemeralId);
        return ephemeralId;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    @Override
    public void close() throws SQLException {
        for (Long ephemeralDateId : ephemeralDateIds) {
            jdbcOperator.deleteDataById(ephemeralDateId);
        }
        ephemeralDateIds.clear();
        connectionListeners.clear();
        scheduledExecutorService.shutdownNow();
    }

    // Use this task to refresh ephemeral term and check the connect state.
    class EphemeralDateTermRefreshTask implements Runnable {

        private final List<ConnectionListener> connectionListeners;
        private final Set<Long> ephemeralDateIds;
        private final JdbcOperator jdbcOperator;

        private EphemeralDateTermRefreshTask(JdbcOperator jdbcOperator,
                                             List<ConnectionListener> connectionListeners,
                                             Set<Long> ephemeralDateIds) {
            this.jdbcOperator = checkNotNull(jdbcOperator);
            this.connectionListeners = checkNotNull(connectionListeners);
            this.ephemeralDateIds = checkNotNull(ephemeralDateIds);
        }

        @Override
        public void run() {
            try {
                ConnectionState currentConnectionState = getConnectionState();
                if (currentConnectionState == connectionState) {
                    // no state change
                    return;
                }

                if (connectionState == ConnectionState.CONNECTED) {
                    if (currentConnectionState == ConnectionState.DISCONNECTED) {
                        connectionState = ConnectionState.DISCONNECTED;
                        triggerListener(ConnectionState.DISCONNECTED);
                    }
                } else if (connectionState == ConnectionState.DISCONNECTED) {
                    if (currentConnectionState == ConnectionState.CONNECTED) {
                        connectionState = ConnectionState.CONNECTED;
                        triggerListener(ConnectionState.RECONNECTED);
                    }
                } else if (connectionState == null) {
                    connectionState = currentConnectionState;
                    triggerListener(connectionState);
                }
            } catch (Exception e) {
                log.error("Jdbc Registry connect state check task execute failed", e);
                connectionState = ConnectionState.DISCONNECTED;
                triggerListener(ConnectionState.DISCONNECTED);
            }
        }

        private ConnectionState getConnectionState() {
            try {
                if (ephemeralDateIds.isEmpty()) {
                    jdbcOperator.healthCheck();
                } else {
                    updateEphemeralDateTerm();
                }
                jdbcOperator.clearExpireEphemeralDate();
                return ConnectionState.CONNECTED;
            } catch (Exception ex) {
                log.error("Get connection state error, meet an unknown exception", ex);
                return ConnectionState.DISCONNECTED;
            }
        }

        private void updateEphemeralDateTerm() {
            if (!jdbcOperator.updateEphemeralDataTerm(ephemeralDateIds)) {
                log.warn("Update jdbc registry ephemeral data: {} term error", ephemeralDateIds);
            }
        }

        private void triggerListener(ConnectionState connectionState) {
            for (ConnectionListener connectionListener : connectionListeners) {
                connectionListener.onUpdate(connectionState);
            }
        }
    }
}
