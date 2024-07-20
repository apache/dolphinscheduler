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

package org.apache.dolphinscheduler.plugin.registry.raft;

import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;

/**
 * Interface for managing the connection state in a raft registry client.
 */
public interface IRaftConnectionStateManager extends AutoCloseable {

    /**
     * Starts the connection state manager.
     * This method initializes and starts monitoring the connection state.
     */
    void start();

    /**
     * Adds a connection listener to listen for connection state changes.
     *
     * @param listener the listener to be added for connection state changes
     */
    void addConnectionListener(ConnectionListener listener);

    /**
     * Retrieves the current connection state.
     *
     * @return the current connection state
     */
    ConnectionState getConnectionState();
}
