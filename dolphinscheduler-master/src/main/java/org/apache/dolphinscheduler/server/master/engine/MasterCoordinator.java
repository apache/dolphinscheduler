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

package org.apache.dolphinscheduler.server.master.engine;

import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.registry.api.ha.AbstractHAServer;
import org.apache.dolphinscheduler.registry.api.ha.AbstractServerStatusChangeListener;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The MasterCoordinator is singleton at the clusters, which is used to do some control work, e.g manage the {@link TaskGroupCoordinator}
 */
@Slf4j
@Component
public class MasterCoordinator extends AbstractHAServer {

    @Autowired
    private TaskGroupCoordinator taskGroupCoordinator;

    public MasterCoordinator(final Registry registry, final MasterConfig masterConfig) {
        super(
                registry,
                RegistryNodeType.MASTER_COORDINATOR.getRegistryPath(),
                masterConfig.getMasterAddress());

        addServerStatusChangeListener(new AbstractServerStatusChangeListener() {

            @Override
            public void changeToActive() {
                onActive();
            }

            @Override
            public void changeToStandBy() {
                onStandBy();
            }
        });
    }

    @Override
    public void start() {
        super.start();
        log.info("MasterCoordinator started...");
    }

    @Override
    public void close() {
        taskGroupCoordinator.close();
        log.info("MasterCoordinator shutdown...");
    }

    private void onActive() {
        taskGroupCoordinator.start();
    }

    private void onStandBy() {
        taskGroupCoordinator.close();
    }

}
