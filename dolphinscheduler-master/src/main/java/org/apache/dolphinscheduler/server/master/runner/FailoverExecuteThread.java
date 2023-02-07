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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.service.MasterFailoverService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FailoverExecuteThread extends BaseDaemonThread {

    @Autowired
    private MasterConfig masterConfig;

    /**
     * failover service
     */
    @Autowired
    private MasterFailoverService masterFailoverService;

    protected FailoverExecuteThread() {
        super("FailoverExecuteThread");
    }

    @Override
    public synchronized void start() {
        log.info("Master failover thread staring");
        super.start();
        log.info("Master failover thread stared");
    }

    @Override
    public void run() {
        // when startup, wait 10s for ready
        ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS * 10);

        while (!ServerLifeCycleManager.isStopped()) {
            try {
                if (!ServerLifeCycleManager.isRunning()) {
                    continue;
                }
                // todo: DO we need to schedule a task to do this kind of check
                // This kind of check may only need to be executed when a master server start
                masterFailoverService.checkMasterFailover();
            } catch (Exception e) {
                log.error("Master failover thread execute error", e);
            } finally {
                ThreadUtils.sleep(masterConfig.getFailoverInterval().toMillis());
            }
        }
    }
}
