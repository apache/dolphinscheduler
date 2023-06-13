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

import org.apache.dolphinscheduler.server.master.runner.execute.AsyncMasterTaskDelayQueueLooper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MasterTaskExecutorBootstrap implements AutoCloseable {

    @Autowired
    private GlobalTaskDispatchWaitingQueueLooper globalTaskDispatchWaitingQueueLooper;

    @Autowired
    private MasterDelayTaskExecuteRunnableDelayQueueLooper masterDelayTaskExecuteRunnableDelayQueueLooper;

    @Autowired
    private AsyncMasterTaskDelayQueueLooper asyncMasterTaskDelayQueueLooper;

    public synchronized void start() {
        log.info("MasterTaskExecutorBootstrap starting...");
        globalTaskDispatchWaitingQueueLooper.start();
        masterDelayTaskExecuteRunnableDelayQueueLooper.start();
        asyncMasterTaskDelayQueueLooper.start();
        log.info("MasterTaskExecutorBootstrap started...");
    }

    @Override
    public void close() throws Exception {
        log.info("MasterTaskExecutorBootstrap closing...");
        try (
                final GlobalTaskDispatchWaitingQueueLooper globalTaskDispatchWaitingQueueLooper1 =
                        globalTaskDispatchWaitingQueueLooper;
                final MasterDelayTaskExecuteRunnableDelayQueueLooper masterDelayTaskExecuteRunnableDelayQueueLooper1 =
                        masterDelayTaskExecuteRunnableDelayQueueLooper;
                final AsyncMasterTaskDelayQueueLooper asyncMasterTaskDelayQueueLooper1 =
                        asyncMasterTaskDelayQueueLooper) {
            // closed the resource
        }
        log.info("MasterTaskExecutorBootstrap closed...");
    }
}
