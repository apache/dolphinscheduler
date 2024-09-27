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

package org.apache.dolphinscheduler.plugin.registry.raft.manage;

import org.apache.dolphinscheduler.plugin.registry.raft.RaftRegistryProperties;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.rpc.CliClientService;
import com.alipay.sofa.jraft.rpc.impl.cli.CliClientServiceImpl;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Slf4j
public class RaftConnectionStateManager implements IRaftConnectionStateManager {

    private static final String DEFAULT_REGION_ID = "--1";
    private static final Duration CONNECT_STATE_CHECK_INTERVAL = Duration.ofSeconds(2);
    private static final int CONNECT_STATE_REFRESH_THREAD_POOL_SIZE = 1;
    private static final Duration REFRESH_LEADER_TIME_OUT = Duration.ofSeconds(2);
    private static final int MAX_RANDOM_DELAY_MS = 500;
    private ConnectionState currentConnectionState;
    private final RaftRegistryProperties properties;
    private final List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduledExecutorService;
    private final CliOptions cliOptions;
    private final CliClientService cliClientService;

    public RaftConnectionStateManager(RaftRegistryProperties properties) {
        this.properties = properties;
        this.cliOptions = new CliOptions();
        this.cliClientService = new CliClientServiceImpl();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(
                CONNECT_STATE_REFRESH_THREAD_POOL_SIZE,
                new ThreadFactoryBuilder().setNameFormat("ConnectionStateRefreshThread").setDaemon(true).build());
    }
    @Override
    public void start() {
        cliClientService.init(cliOptions);
        scheduledExecutorService.scheduleWithFixedDelay(
                new ConnectionStateRefreshTask(connectionListeners),
                getRandomizedDelay(CONNECT_STATE_CHECK_INTERVAL.toMillis()),
                getRandomizedDelay(CONNECT_STATE_CHECK_INTERVAL.toMillis()),
                TimeUnit.MILLISECONDS);
    }

    private long getRandomizedDelay(long baseDelay) {
        // Add a random value in the range [0, RANDOM_DELAY_RANGE_MS]
        Random random = new Random();
        long randomOffset = random.nextInt(MAX_RANDOM_DELAY_MS + 1);
        return baseDelay + randomOffset;
    }

    @Override
    public void addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    @Override
    public ConnectionState getConnectionState() {
        return currentConnectionState;
    }

    class ConnectionStateRefreshTask implements Runnable {

        private final List<ConnectionListener> connectionListeners;
        ConnectionStateRefreshTask(List<ConnectionListener> connectionListeners) {
            this.connectionListeners = connectionListeners;
        }

        @Override
        public void run() {
            try {
                ConnectionState newConnectionState = getCurrentConnectionState();
                if (newConnectionState == currentConnectionState) {
                    // no state change
                    return;
                }
                if (newConnectionState == ConnectionState.DISCONNECTED
                        && currentConnectionState == ConnectionState.CONNECTED) {
                    currentConnectionState = ConnectionState.DISCONNECTED;
                    triggerListeners(ConnectionState.DISCONNECTED);
                } else if (newConnectionState == ConnectionState.CONNECTED
                        && currentConnectionState == ConnectionState.DISCONNECTED) {
                    currentConnectionState = ConnectionState.CONNECTED;
                    triggerListeners(ConnectionState.RECONNECTED);
                } else if (currentConnectionState == null) {
                    currentConnectionState = newConnectionState;
                    triggerListeners(currentConnectionState);
                }
            } catch (Exception ex) {
                log.error("raft registry connection state check failed", ex);
                currentConnectionState = ConnectionState.DISCONNECTED;
                triggerListeners(ConnectionState.DISCONNECTED);
            }
        }

        private ConnectionState getCurrentConnectionState() {
            try {
                String groupId = properties.getClusterName() + DEFAULT_REGION_ID;
                if (RouteTable.getInstance()
                        .refreshLeader(cliClientService, groupId, (int) REFRESH_LEADER_TIME_OUT.toMillis()).isOk()) {
                    return ConnectionState.CONNECTED;
                } else {
                    return ConnectionState.DISCONNECTED;
                }
            } catch (Exception ex) {
                log.error("cannot connect to raft leader", ex);
                return ConnectionState.DISCONNECTED;
            }
        }

        private void triggerListeners(ConnectionState connectionState) {
            for (ConnectionListener connectionListener : connectionListeners) {
                connectionListener.onUpdate(connectionState);
            }
        }
    }

    @Override
    public void close() throws Exception {
        connectionListeners.clear();
        scheduledExecutorService.shutdownNow();
        cliClientService.shutdown();
    }

}
