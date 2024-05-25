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

package org.apache.dolphinscheduler.plugin.registry.zookeeper;

import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionStateListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class ZookeeperConnectionStateListener implements ConnectionStateListener {

    private final ConnectionListener listener;

    ZookeeperConnectionStateListener(ConnectionListener listener) {
        this.listener = listener;
    }

    @Override
    public void stateChanged(CuratorFramework client,
                             org.apache.curator.framework.state.ConnectionState newState) {
        switch (newState) {
            case CONNECTED:
                log.info("Registry connected");
                listener.onUpdate(ConnectionState.CONNECTED);
                break;
            case LOST:
                log.warn("Registry disconnected");
                listener.onUpdate(ConnectionState.DISCONNECTED);
                break;
            case RECONNECTED:
                log.info("Registry reconnected");
                listener.onUpdate(ConnectionState.RECONNECTED);
                break;
            case SUSPENDED:
                log.warn("Registry suspended");
                listener.onUpdate(ConnectionState.SUSPENDED);
                break;
            default:
                break;
        }
    }
}
