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
public interface HAServer {

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
     * Participate in the election of active server, this method will block until the server is active.
     */
    boolean participateElection();

    /**
     * Add a listener to listen to the status change of the server.
     *
     * @param listener listener to add.
     */
    void addServerStatusChangeListener(ServerStatusChangeListener listener);

    /**
     * Get the status of the server.
     *
     * @return the status of the server.
     */
    ServerStatus getServerStatus();

    /**
     * Shutdown the server, release resources.
     */
    void shutdown();

    enum ServerStatus {
        ACTIVE,
        STAND_BY,
        ;
    }

}
