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

package org.apache.dolphinscheduler.server.worker.registry;

import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.task.WorkerHeartBeatTask;
import org.apache.dolphinscheduler.service.registry.RegistryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static org.apache.dolphinscheduler.common.Constants.SLEEP_TIME_MILLIS;

@Slf4j
@Service
public class WorkerRegistryClient implements AutoCloseable {

    @Autowired
    private WorkerConfig workerConfig;

    @Autowired
    private RegistryClient registryClient;

    @Autowired
    private WorkerConnectStrategy workerConnectStrategy;

    @Autowired
    private WorkerHeartBeatTask workerHeartBeatTask;

    public synchronized void start() {
        try {
            registry();
            registryClient.addConnectionStateListener(
                    new WorkerConnectionStateListener(workerConfig, registryClient, workerConnectStrategy));
        } catch (Exception ex) {
            throw new RegistryException("WorkerRegistryClient start up error", ex);
        }
    }

    private void registry() {
        String workerAddress = workerConfig.getWorkerAddress();
        String heartBeatJsonString = workerHeartBeatTask.getHeartBeatJsonString();
        log.info("Worker node: {} registering to registry", workerAddress);

        for (String workerGroupRegistryPath : workerConfig.getWorkerGroupRegistryPaths()) {
            // remove before persist
            registryClient.remove(workerGroupRegistryPath);
            registryClient.persistEphemeral(workerGroupRegistryPath, heartBeatJsonString);
            log.info("Worker node: {} registered to registry successfully, workerGroupPath: {} heartBeat: {}",
                    workerAddress, workerGroupRegistryPath, heartBeatJsonString);
        }

        while (!registryClient.checkNodeExists(NetUtils.getHost(), NodeType.WORKER)) {
            ThreadUtils.sleep(SLEEP_TIME_MILLIS);
        }

        // sleep 1s, waiting master failover remove
        ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);

        workerHeartBeatTask.start();

        log.info("Worker node: {} registered to registry", workerAddress);
    }

    public void setRegistryStoppable(IStoppable stoppable) {
        registryClient.setStoppable(stoppable);
    }

    @Override
    public void close() throws IOException {
        workerHeartBeatTask.shutdown();

        registryClient.close();
        log.info("registry client closed");
    }

}
