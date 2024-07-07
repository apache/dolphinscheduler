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

package org.apache.dolphinscheduler.plugin.registry.consul;

import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.orbitz.consul.Consul;

public class ConsulConnectionStateListener implements AutoCloseable {

    private final List<ConnectionListener> connectionListeners = Collections.synchronizedList(new ArrayList<>());
    // A thread pool that periodically obtains connection status
    private final ScheduledExecutorService scheduledExecutorService;
    // monitored client
    private final Consul consulClient;
    // The state of the last monitor
    private volatile ConnectionState connectionState;

    public ConsulConnectionStateListener(Consul consulClient) {
        this.consulClient = consulClient;
        this.scheduledExecutorService = Executors.newScheduledThreadPool(
                1,
                new ThreadFactoryBuilder().setNameFormat("ConsulConnectionStateListenerThread")
                        .setDaemon(true).build());
    }

    public void addConnectionListener(ConnectionListener connectionListener) {
        connectionListeners.add(connectionListener);
    }

    @Override
    public void close() {
        connectionListeners.clear();
        scheduledExecutorService.shutdownNow();
    }

    /**
     * Apply for a lease through the client, if there is no exception, the connection is normal
     * @return the current connection state
     * @throws if there is an exception, return is DISCONNECTED
     */
    private ConnectionState currentConnectivityState() {
        try {
            consulClient.agentClient().ping();
            return ConnectionState.CONNECTED;
        } catch (Exception e) {
            return ConnectionState.DISCONNECTED;
        }
    }

    /**
     * Periodically execute thread to get connection status
     */
    public void start() {
        long initialDelay = 500L;
        long delay = 500L;
        this.scheduledExecutorService.scheduleWithFixedDelay(() -> {
            ConnectionState currentConnectionState = currentConnectivityState();
            if (currentConnectionState == connectionState) {
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
        },
                initialDelay,
                delay,
                TimeUnit.MILLISECONDS);
    }

    // notify all listeners
    private void triggerListener(ConnectionState connectionState) {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onUpdate(connectionState);
        }
    }
}
