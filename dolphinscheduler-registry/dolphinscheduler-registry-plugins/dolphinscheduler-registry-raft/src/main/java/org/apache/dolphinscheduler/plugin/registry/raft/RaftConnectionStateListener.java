package org.apache.dolphinscheduler.plugin.registry.raft;

import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;

import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.core.Replicator;
import com.alipay.sofa.jraft.entity.PeerId;

public class RaftConnectionStateListener implements Replicator.ReplicatorStateListener {
    private final ConnectionListener connectionListener;
    private ConnectionState connectionState;

    public RaftConnectionStateListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    @Override
    public void onCreated(PeerId peerId) {

    }

    @Override
    public void onError(PeerId peerId, Status status) {

    }

    @Override
    public void onDestroyed(PeerId peerId) {

    }

    @Override
    public void stateChanged(PeerId peer, ReplicatorState newState) {
        switch (newState) {
            case CREATED:
                connectionState = ConnectionState.CONNECTED;
                break;
            case ONLINE:
                if (connectionState == ConnectionState.DISCONNECTED || connectionState == ConnectionState.SUSPENDED) {
                    connectionState = ConnectionState.RECONNECTED;
                }
                break;
            case OFFLINE:
                connectionState = ConnectionState.SUSPENDED;
                break;
            case DESTROYED:
                connectionState = ConnectionState.DISCONNECTED;
                break;
            default:
        }
        connectionListener.onUpdate(connectionState);
    }
}
