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

package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.registry.MasterRegistryClient;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FailoverExecuteThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(FailoverExecuteThread.class);

    @Autowired
    private MasterRegistryClient masterRegistryClient;

    @Autowired
    private RegistryClient registryClient;

    @Autowired
    private MasterConfig masterConfig;

    /**
     * process service
     */
    @Autowired
    private ProcessService processService;

    @Override
    public synchronized void start() {
        super.setName("FailoverExecuteThread");
        super.start();
    }

    @Override
    public void run() {
        while (Stopper.isRunning()) {
            logger.info("failover execute started");
            try {
                List<String> hosts = getNeedFailoverMasterServers();
                if (CollectionUtils.isEmpty(hosts)) {
                    continue;
                }
                logger.info("need failover hosts:{}", hosts);

                for (String host : hosts) {
                    String failoverPath = masterRegistryClient.getFailoverLockPath(NodeType.MASTER, host);
                    try {
                        registryClient.getLock(failoverPath);
                        masterRegistryClient.failoverMaster(host);
                    } catch (Exception e) {
                        logger.error("{} server failover failed, host:{}", NodeType.MASTER, host, e);
                    } finally {
                        registryClient.releaseLock(failoverPath);
                    }
                }
            } catch (Exception e) {
                logger.error("failover execute error", e);
            } finally {
                ThreadUtils.sleep((long) Constants.SLEEP_TIME_MILLIS * masterConfig.getFailoverInterval() * 60);
            }
        }
    }

    private List<String> getNeedFailoverMasterServers() {
        // failover myself && failover dead masters
        List<String> hosts = processService.queryNeedFailoverProcessInstanceHost();

        Iterator<String> iterator = hosts.iterator();
        while (iterator.hasNext()) {
            String host = iterator.next();
            if (registryClient.checkNodeExists(host, NodeType.MASTER)) {
                if (!host.equals(masterRegistryClient.getLocalAddress())) {
                    iterator.remove();
                }
            }
        }
        return hosts;
    }
}
