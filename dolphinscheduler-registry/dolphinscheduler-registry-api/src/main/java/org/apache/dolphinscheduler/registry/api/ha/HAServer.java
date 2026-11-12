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

package org.apache.dolphinscheduler.registry.api.ha;

/**
 * Interface for HA server, used to select a active server from multiple servers.
 * In HA mode, there are multiple servers, only one server is active, others are standby.
 */
public interface HAServer extends AutoCloseable {

    /**
     * Start the server.
     */
    void start();

    /**
     * Judge whether the server is active.
     *
     * @return true if the current server is active.
     */
    boolean isActive();

    /**
     * Synchronously check selector ownership, attempting election when the selector is absent.
     * This may block on Registry I/O and retries. It does not update the local role or notify listeners.
     *
     * @return true if this instance owns the selector, false otherwise.
     */
    boolean participateElection();

    /**
     * Register a status-change listener before starting the server.
     *
     * @param listener listener to add.
     */
    void addServerStatusChangeListener(ServerStatusChangeListener listener);

    /**
     * Get the last assigned HA role, which is retained after close.
     * Use {@link #isActive()} to determine whether the server is currently active.
     *
     * @return the last assigned role.
     */
    ServerStatus getServerStatus();

    /**
     * Shutdown the server, release resources.
     */
    @Override
    void close();

    enum ServerStatus {
        ACTIVE,
        STAND_BY,
        ;
    }

}
