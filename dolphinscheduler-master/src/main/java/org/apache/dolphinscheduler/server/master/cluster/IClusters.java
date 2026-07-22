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

import java.util.List;
import java.util.Optional;

public interface IClusters<S extends IClusters.IServerMetadata> {

    List<S> getServers();

    Optional<S> getServer(final String address);

    void registerListener(IClustersChangeListener<S> listener);

    interface IServerMetadata {

        String getAddress();

        ServerStatus getServerStatus();

    }

    interface IClustersChangeListener<S extends IServerMetadata> {

        void onServerAdded(S server);

        void onServerRemove(S server);

        void onServerUpdate(S server);

    }

    interface ServerAddedListener<S extends IServerMetadata> extends IClustersChangeListener<S> {

        @Override
        default void onServerRemove(S server) {
            // only care about server added
        }

        @Override
        default void onServerUpdate(S server) {
            // only care about server added
        }

    }

    interface ServerRemovedListener<S extends IServerMetadata> extends IClustersChangeListener<S> {

        @Override
        default void onServerAdded(S server) {
            // only care about server removed
        }

        @Override
        void onServerRemove(S server);

        @Override
        default void onServerUpdate(S server) {
            // only care about server added
        }

    }

}
