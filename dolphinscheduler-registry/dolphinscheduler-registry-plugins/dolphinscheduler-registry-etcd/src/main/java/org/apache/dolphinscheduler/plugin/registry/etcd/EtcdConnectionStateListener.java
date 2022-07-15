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
package org.apache.dolphinscheduler.plugin.registry.etcd;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.etcd.jetcd.Client;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.registry.api.RegistryException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Get the connection status by listening to the Client's Channel
 */
public class EtcdConnectionStateListener implements AutoCloseable{
    private final List<ConnectionListener> connectionListeners = Collections.synchronizedList(new ArrayList<>());
    // A thread pool that periodically obtains connection status
    private final ScheduledExecutorService scheduledExecutorService;
    // Client's Channel
    private AtomicReference<ManagedChannel> channel;
    // monitored client
    private Client client;
    // The state of the last monitor
    private ConnectionState connectionState;
    private long initialDelay = 500L;
    private long delay = 500L;
    public EtcdConnectionStateListener(Client client) {
        this.client = client;
        channel = new AtomicReference<>();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(
                1,
                new ThreadFactoryBuilder().setNameFormat("EtcdConnectionStateListenerThread").setDaemon(true).build());
    }

    public void addConnectionListener(ConnectionListener connectionListener) {
        connectionListeners.add(connectionListener);
    }

    @Override
    public void close() throws Exception {
        connectionListeners.clear();
        scheduledExecutorService.shutdownNow();
    }

    /**
     * try to get jetcd client ManagedChannel
     * @param client the etcd client
     * @return current connection channel
     */
    private ManagedChannel newChannel(Client client) {
        try {
            Field connectField =client.getClass().getDeclaredField("connectManager");
            if(!connectField.isAccessible()){
                connectField.setAccessible(true);
            }
            Object connection = connectField.get(client);
            Method channel = connection.getClass().getDeclaredMethod("getChannel");
            if (!channel.isAccessible()) {
                channel.setAccessible(true);
            }
            return (ManagedChannel) channel.invoke(connection);
        } catch (Exception e) {
            throw new RegistryException("Failed to get the etcd client channel", e);
        }
    }

    /**
     * try to get the current channel
     * @return connected channel
     */
    private ManagedChannel getChannel(){
        if(channel.get() == null||(channel.get().isShutdown() || channel.get().isTerminated())){
            channel.set(newChannel(client));
        }
        return channel.get();
    }

    /**
     * if channel state is in [READY,IDLE],channel is connected
     * @return the current connection state
     */
    private ConnectionState isConnected() {
        if(ConnectivityState.READY == (getChannel().getState(false))
                || ConnectivityState.IDLE == (getChannel().getState(false))){
            return ConnectionState.CONNECTED;
        }
        return ConnectionState.DISCONNECTED;
    }

    /**
     * Periodically execute thread to get connection status
     */
    public void start(){
        this.scheduledExecutorService.scheduleWithFixedDelay(()->{
            ConnectionState currentConnectionState = isConnected();
            if(currentConnectionState == connectionState){
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
