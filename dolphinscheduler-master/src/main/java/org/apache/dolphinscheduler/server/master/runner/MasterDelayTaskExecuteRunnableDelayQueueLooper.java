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

import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterDelayTaskExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterTaskExecuteRunnableHolder;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterTaskExecuteRunnableThreadPool;

import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MasterDelayTaskExecuteRunnableDelayQueueLooper extends BaseDaemonThread implements AutoCloseable {

    @Autowired
    private MasterDelayTaskExecuteRunnableDelayQueue masterDelayTaskExecuteRunnableDelayQueue;

    @Autowired
    private MasterTaskExecuteRunnableThreadPool masterTaskExecuteRunnableThreadPool;

    private final AtomicBoolean RUNNING_FLAG = new AtomicBoolean(false);

    public MasterDelayTaskExecuteRunnableDelayQueueLooper() {
        super("MasterDelayTaskExecuteRunnableDelayQueueLooper");
    }

    @Override
    public synchronized void start() {
        if (!RUNNING_FLAG.compareAndSet(false, true)) {
            log.error("The MasterDelayTaskExecuteRunnableDelayQueueLooper already started, will not start again");
            return;
        }
        log.info("MasterDelayTaskExecuteRunnableDelayQueueLooper starting...");
        super.start();
        masterTaskExecuteRunnableThreadPool.start();
        log.info("MasterDelayTaskExecuteRunnableDelayQueueLooper started...");
    }

    @Override
    public void run() {
        while (RUNNING_FLAG.get()) {
            try {
                final MasterDelayTaskExecuteRunnable masterDelayTaskExecuteRunnable =
                        masterDelayTaskExecuteRunnableDelayQueue.takeMasterDelayTaskExecuteRunnable();
                masterTaskExecuteRunnableThreadPool.submitMasterTaskExecuteRunnable(masterDelayTaskExecuteRunnable);
                MasterTaskExecuteRunnableHolder.putMasterTaskExecuteRunnable(masterDelayTaskExecuteRunnable);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.warn("MasterDelayTaskExecuteRunnableDelayQueueLooper has been interrupted, will stop loop");
                break;
            }
        }
        log.info("MasterDelayTaskExecuteRunnableDelayQueueLooper stop loop...");
    }

    @Override
    public void close() throws Exception {
        if (RUNNING_FLAG.compareAndSet(true, false)) {
            log.info("MasterDelayTaskExecuteRunnableDelayQueueLooper stopping...");
            log.info("MasterDelayTaskExecuteRunnableDelayQueueLooper stopped...");
        }
    }
}
