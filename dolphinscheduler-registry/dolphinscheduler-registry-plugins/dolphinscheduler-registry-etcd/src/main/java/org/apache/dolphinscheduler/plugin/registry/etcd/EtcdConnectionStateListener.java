package org.apache.dolphinscheduler.plugin.registry.etcd;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.etcd.jetcd.Client;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class EtcdConnectionStateListener implements AutoCloseable{

    private final List<ConnectionListener> connectionListeners = Collections.synchronizedList(new ArrayList<>());
    private final ScheduledExecutorService scheduledExecutorService;
    private AtomicReference<ManagedChannel> channel;
    private Client client;
    private ConnectionState connectionState;
    public EtcdConnectionStateListener(Client client) {
        this.client = client;
        this.scheduledExecutorService = Executors.newScheduledThreadPool(
                1,
                new ThreadFactoryBuilder().setNameFormat("EphemeralDateTermRefreshThread").setDaemon(true).build());
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
                500,
                500,
                TimeUnit.MILLISECONDS);
    }
    private void triggerListener(ConnectionState connectionState) {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onUpdate(connectionState);
        }
    }
}
