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

import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
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
    private MasterClusters masterClusters;

    @Getter
    private WorkerClusters workerClusters;

    @Autowired
    private MasterSlotManager masterSlotManager;

    @Autowired
    private WorkerGroupChangeNotifier workerGroupChangeNotifier;

    @Autowired
    private RegistryClient registryClient;

    public ClusterManager() {
        this.masterClusters = new MasterClusters();
        this.workerClusters = new WorkerClusters();
    }

    public void start() {
        initializeMasterClusters();
        initializeWorkerClusters();
        log.info("ClusterManager started...");
    }

    /**
     * Initialize the master clusters.
     * <p> 1. Register master slot listener once master clusters changed.
     * <p> 2. Fetch master nodes from registry.
     * <p> 3. Subscribe the master change event.
     */
    private void initializeMasterClusters() {
        this.masterClusters.registerListener(new MasterSlotChangeListenerAdaptor(masterSlotManager, masterClusters));

        registryClient.getServerList(RegistryNodeType.MASTER).forEach(server -> {
            final MasterHeartBeat masterHeartBeat =
                    JSONUtils.parseObject(server.getHeartBeatInfo(), MasterHeartBeat.class);
            masterClusters.onServerAdded(MasterServerMetadata.parseFromHeartBeat(masterHeartBeat));
        });
        log.info("Initialized MasterClusters: {}", JSONUtils.toPrettyJsonString(masterClusters.getServers()));

        this.registryClient.subscribe(RegistryNodeType.MASTER.getRegistryPath(), masterClusters);
    }

    /**
     * Initialize the worker clusters.
     * <p> 1. Fetch worker nodes from registry.
     * <p> 2. Register worker group change notifier once worker clusters changed.
     * <p> 3. Subscribe the worker change event.
     */
    private void initializeWorkerClusters() {
        registryClient.getServerList(RegistryNodeType.WORKER).forEach(server -> {
            final WorkerHeartBeat workerHeartBeat =
                    JSONUtils.parseObject(server.getHeartBeatInfo(), WorkerHeartBeat.class);
            workerClusters.onServerAdded(WorkerServerMetadata.parseFromHeartBeat(workerHeartBeat));
        });
        log.info("Initialized WorkerClusters: {}", JSONUtils.toPrettyJsonString(workerClusters.getServers()));

        this.registryClient.subscribe(RegistryNodeType.WORKER.getRegistryPath(), workerClusters);

        this.workerGroupChangeNotifier.subscribeWorkerGroupsChange(workerClusters);
        this.workerGroupChangeNotifier.start();
    }

}
