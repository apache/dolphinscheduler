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
import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.runner.queue.DelayEntry;
import org.apache.dolphinscheduler.server.master.runner.queue.PriorityDelayQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkerGroupTaskDispatchWaitingQueueLooper extends BaseDaemonThread implements AutoCloseable {
    private final AtomicBoolean RUNNING_FLAG = new AtomicBoolean(false);

    private final DispatchWorker dispatchWorker;

    public WorkerGroupTaskDispatchWaitingQueueLooper(String workerGroupName,
                                                     ITaskExecutorClient taskExecutorClient,
                                                     PriorityDelayQueue<DelayEntry<ITaskExecutionRunnable>> workerGroupQueue) {
        super("WorkerGroupQueueLooper-"+workerGroupName);
        this.dispatchWorker = new DispatchWorker(taskExecutorClient, workerGroupQueue);
    }

    @Override
    public synchronized void start() {
        if (!RUNNING_FLAG.compareAndSet(false, true)) {
            log.error("The {} already started, will not start again", this.getName());
            return;
        }
        log.info("{} starting...", this.getName());
        super.start();
        log.info("{} started...", this.getName());
    }

    @Override
    public void close() throws Exception {
        if (RUNNING_FLAG.compareAndSet(true, false)) {
            log.info("{} stopping...", this.getName());
            log.info("{} stopped...", this.getName());
        } else {
            log.error("{} is not started", this.getName());
        }
    }

    @Override
    public void run() {
        while (RUNNING_FLAG.get()) {
            doDispatch();
        }
    }


    private void doDispatch() {
        dispatchWorker.dispatch();
    }

}
