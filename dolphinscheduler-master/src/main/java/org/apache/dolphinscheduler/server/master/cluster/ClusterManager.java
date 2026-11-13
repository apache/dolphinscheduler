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

import org.apache.dolphinscheduler.common.model.ServerIdentity;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClusterManager {

    @Getter
    @Autowired
    private MasterClusters masterClusters;
    @Getter
    @Autowired
    private WorkerClusters workerClusters;
    @Autowired
    private MasterSlotManager masterSlotManager;
    @Autowired
    private WorkerGroupChangeNotifier workerGroupChangeNotifier;
    @Autowired
    private RegistryClient registryClient;

    public void start() {
        initializeMasterClusters();
        initializeWorkerClusters();
        log.info("ClusterManager started...");
    }

    private void initializeMasterClusters() {
        masterClusters.registerListener(new MasterSlotChangeListenerAdaptor(masterSlotManager, masterClusters));
        synchronized (masterClusters) {
            // Apply the initial snapshot before processing membership events.
            registryClient.subscribe(RegistryNodeType.MASTER.getRegistryPath(), masterClusters);
            registryClient.getServerList(RegistryNodeType.MASTER).forEach(server -> masterClusters.onServerAdded(
                    JSONUtils.parseObject(server.getHeartBeatInfo(), ServerIdentity.class)));
        }
        log.info("Initialized MasterClusters: {}", masterClusters.getServerAddresses());
    }

    private void initializeWorkerClusters() {
        synchronized (workerClusters) {
            // Apply the initial snapshot before processing membership events.
            registryClient.subscribe(RegistryNodeType.WORKER.getRegistryPath(), workerClusters);
            registryClient.getServerList(RegistryNodeType.WORKER).forEach(server -> workerClusters.onServerAdded(
                    JSONUtils.parseObject(server.getHeartBeatInfo(), ServerIdentity.class)));
        }
        log.info("Initialized WorkerClusters: {}", workerClusters.getServerAddresses());
        workerGroupChangeNotifier.subscribeWorkerGroupsChange(workerClusters);
        workerGroupChangeNotifier.start();
    }

}
