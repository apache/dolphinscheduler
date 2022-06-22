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
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventExecuteService extends BaseDaemonThread {

    private static final Logger logger = LoggerFactory.getLogger(EventExecuteService.class);

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    /**
     * workflow exec service
     */
    @Autowired
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    protected EventExecuteService() {
        super("EventServiceStarted");
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    @Override
    public void run() {
        logger.info("Event service started");
        while (Stopper.isRunning()) {
            try {
                eventHandler();
                TimeUnit.MILLISECONDS.sleep(Constants.SLEEP_TIME_MILLIS_SHORT);
            } catch (Exception e) {
                logger.error("Event service thread error", e);
            }
        }
    }

    private void eventHandler() {
        for (WorkflowExecuteRunnable workflowExecuteThread : this.processInstanceExecCacheManager.getAll()) {
            workflowExecuteThreadPool.executeEvent(workflowExecuteThread);
        }
    }
}
