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

import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.service.alert.ListenerEventAlertManager;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClusterStateMonitors {

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ListenerEventAlertManager listenerEventAlertManager;

    @Autowired
    private AlertDao alertDao;

    public void start() {
        this.clusterManager.getMasterClusters()
                .registerListener((IClusters.ServerRemovedListener<MasterServer>) this::masterRemoved);
        this.clusterManager.getWorkerClusters()
                .registerListener((IClusters.ServerRemovedListener<WorkerServer>) this::workerRemoved);
        log.info("ClusterStateMonitors started...");
    }

    void masterRemoved(MasterServer masterServer) {
        // todo: unify the alert message
        alertDao.sendServerStoppedAlert(masterServer.getAddress(), "MASTER");
        listenerEventAlertManager.publishServerDownListenerEvent(masterServer.getAddress(), "MASTER");
    }

    void workerRemoved(WorkerServer workerServer) {
        alertDao.sendServerStoppedAlert(workerServer.getAddress(), "WORKER");
        listenerEventAlertManager.publishServerDownListenerEvent(workerServer.getAddress(), "WORKER");
    }

}
