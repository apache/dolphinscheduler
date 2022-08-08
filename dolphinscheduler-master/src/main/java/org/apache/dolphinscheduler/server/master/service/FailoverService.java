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

import org.apache.dolphinscheduler.common.enums.NodeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.NonNull;

/**
 * failover service
 */
@Component
public class FailoverService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FailoverService.class);

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
    public void failoverServerWhenDown(String serverHost, NodeType nodeType) {
        switch (nodeType) {
            case MASTER:
                LOGGER.info("Master failover starting, masterServer: {}", serverHost);
                masterFailoverService.failoverMaster(serverHost);
                LOGGER.info("Master failover finished, masterServer: {}", serverHost);
                break;
            case WORKER:
                LOGGER.info("Worker failover staring, workerServer: {}", serverHost);
                workerFailoverService.failoverWorker(serverHost);
                LOGGER.info("Worker failover finished, workerServer: {}", serverHost);
                break;
            default:
                break;
        }
    }

}
