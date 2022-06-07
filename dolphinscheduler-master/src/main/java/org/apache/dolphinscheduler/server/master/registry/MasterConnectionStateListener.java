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

package org.apache.dolphinscheduler.server.master.registry;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterConnectionStateListener implements ConnectionListener {

    private static final Logger logger = LoggerFactory.getLogger(MasterConnectionStateListener.class);

    private final String masterNodePath;
    private final RegistryClient registryClient;

    public MasterConnectionStateListener(String masterNodePath, RegistryClient registryClient) {
        this.masterNodePath = checkNotNull(masterNodePath);
        this.registryClient = checkNotNull(registryClient);
    }

    @Override
    public void onUpdate(ConnectionState state) {
        switch (state) {
            case CONNECTED:
                logger.debug("registry connection state is {}", state);
                break;
            case SUSPENDED:
                logger.warn("registry connection state is {}, ready to retry connection", state);
                break;
            case RECONNECTED:
                logger.debug("registry connection state is {}, clean the node info", state);
                registryClient.remove(masterNodePath);
                registryClient.persistEphemeral(masterNodePath, "");
                break;
            case DISCONNECTED:
                logger.warn("registry connection state is {}, ready to stop myself", state);
                registryClient.getStoppable().stop("registry connection state is DISCONNECTED, stop myself");
                break;
            default:
        }
    }
}
