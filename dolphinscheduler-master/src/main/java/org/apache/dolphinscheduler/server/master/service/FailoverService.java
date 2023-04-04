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

package org.apache.dolphinscheduler.server.master.service;

import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FailoverService {

    private final MasterFailoverService masterFailoverService;
    private final WorkerFailoverService workerFailoverService;

    public FailoverService(@NonNull MasterFailoverService masterFailoverService,
                           @NonNull WorkerFailoverService workerFailoverService) {
        this.masterFailoverService = masterFailoverService;
        this.workerFailoverService = workerFailoverService;
    }

    /**
     * failover server when server down
     *
     * @param serverHost server host
     * @param nodeType   node type
     */
    public void failoverServerWhenDown(String serverHost, RegistryNodeType nodeType) {
        switch (nodeType) {
            case MASTER:
                log.info("Master failover starting, masterServer: {}", serverHost);
                masterFailoverService.failoverMaster(serverHost);
                log.info("Master failover finished, masterServer: {}", serverHost);
                break;
            case WORKER:
                log.info("Worker failover starting, workerServer: {}", serverHost);
                workerFailoverService.failoverWorker(serverHost);
                log.info("Worker failover finished, workerServer: {}", serverHost);
                break;
            default:
                break;
        }
    }

}
