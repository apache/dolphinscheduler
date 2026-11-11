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

package org.apache.dolphinscheduler.server.master.cluster;

import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.model.ServerIdentity;

import java.util.List;
import java.util.Optional;

public interface IClusters<S extends IClusters.IServerMetadata> {

    // Runtime metadata exists only after a valid heartbeat has been received.
    List<S> getServers();

    // Registration is independent of heartbeat availability.
    List<String> getServerAddresses();

    Optional<ServerIdentity> getServerIdentity(String address);

    Optional<S> getServer(final String address);

    void registerListener(IClustersChangeListener<S> listener);

    interface IServerMetadata {

        ServerIdentity getServerIdentity();

        String getAddress();

        ServerStatus getServerStatus();

    }

    interface IClustersChangeListener<S extends IServerMetadata> {

        default void onServerAdded(ServerIdentity server) {
        }

        default void onServerRemove(ServerIdentity server) {
        }

        default void onServerUpdate(S server) {
        }

    }

    interface ServerAddedListener<S extends IServerMetadata> extends IClustersChangeListener<S> {

        @Override
        void onServerAdded(ServerIdentity server);

    }

    interface ServerRemovedListener<S extends IServerMetadata> extends IClustersChangeListener<S> {

        @Override
        void onServerRemove(ServerIdentity server);

    }

}
