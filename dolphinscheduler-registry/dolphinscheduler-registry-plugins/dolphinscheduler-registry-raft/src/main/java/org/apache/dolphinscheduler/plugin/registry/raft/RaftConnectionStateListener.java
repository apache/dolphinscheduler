/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
