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

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.registry.MasterRegistryClient;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClusterManager implements AutoCloseable {

    @Getter
    private final MasterClusters masterClusters = new MasterClusters();
    @Getter
    private final WorkerClusters workerClusters = new WorkerClusters();
    @Autowired
    private MasterSlotManager masterSlotManager;
    @Autowired
    private WorkerGroupChangeNotifier workerGroupChangeNotifier;
    @Autowired
    private RegistryClient registryClient;
    @Autowired
    private MasterConfig masterConfig;
    @Lazy
    @Autowired
    private MasterRegistryClient masterRegistryClient;

    private final ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1,
            ThreadUtils.newDaemonThreadFactory("MasterHeartbeatTimer-%d"));
    private final ExecutorService executor = Executors.newFixedThreadPool(8,
            ThreadUtils.newDaemonThreadFactory("MasterHeartbeatFetch-%d"));

    public void start() {
        Duration interval = masterConfig.getHeartbeatFetchInterval();
        masterClusters.registerListener(new MasterSlotChangeListenerAdaptor(masterSlotManager, masterClusters));
        masterClusters.setLocalHeartbeat(masterConfig.getMasterAddress(), masterRegistryClient::getServerMetadata);
        masterClusters.start(registryClient, interval, executor);
        workerClusters.start(registryClient, interval, executor);
        workerGroupChangeNotifier.subscribeWorkerGroupsChange(workerClusters);
        workerGroupChangeNotifier.start();
        timer.scheduleWithFixedDelay(() -> {
            masterClusters.refreshHeartbeats();
            workerClusters.refreshHeartbeats();
        }, 0, interval.toMillis(), TimeUnit.MILLISECONDS);
        log.info("ClusterManager started...");
    }

    @Override
    @PreDestroy
    public void close() {
        masterClusters.close();
        workerClusters.close();
        timer.shutdownNow();
        executor.shutdownNow();
    }
}
