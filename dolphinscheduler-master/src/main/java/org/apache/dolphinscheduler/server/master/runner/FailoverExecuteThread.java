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
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.service.FailoverService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FailoverExecuteThread extends BaseDaemonThread {

    private static final Logger logger = LoggerFactory.getLogger(FailoverExecuteThread.class);

    @Autowired
    private MasterConfig masterConfig;

    /**
     * failover service
     */
    @Autowired
    private FailoverService failoverService;

    protected FailoverExecuteThread() {
        super("FailoverExecuteThread");
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    @Override
    public void run() {
        // when startup, wait 10s for ready
        ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS * 10);

        logger.info("failover execute thread started");
        while (Stopper.isRunning()) {
            try {
                // todo: DO we need to schedule a task to do this kind of check
                // This kind of check may only need to be executed when a master server start
                failoverService.checkMasterFailover();
            } catch (Exception e) {
                logger.error("failover execute error", e);
            } finally {
                ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS * masterConfig.getFailoverInterval() * 60);
            }
        }
    }
}
