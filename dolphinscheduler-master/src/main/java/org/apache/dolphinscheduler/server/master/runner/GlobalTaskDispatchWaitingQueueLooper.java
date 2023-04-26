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
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.server.master.runner.dispatcher.TaskDispatchFactory;
import org.apache.dolphinscheduler.server.master.runner.dispatcher.TaskDispatcher;
import org.apache.dolphinscheduler.server.master.runner.execute.DefaultTaskExecuteRunnable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GlobalTaskDispatchWaitingQueueLooper extends BaseDaemonThread implements AutoCloseable {

    @Autowired
    private GlobalTaskDispatchWaitingQueue globalTaskDispatchWaitingQueue;

    @Autowired
    private TaskDispatchFactory taskDispatchFactory;

    private final AtomicBoolean RUNNING_FLAG = new AtomicBoolean(false);

    private final AtomicInteger DISPATCHED_TIMES = new AtomicInteger();

    private static final Integer MAX_DISPATCHED_FAILED_TIMES = 100;

    public GlobalTaskDispatchWaitingQueueLooper() {
        super("GlobalTaskDispatchWaitingQueueLooper");
    }

    @Override
    public synchronized void start() {
        if (!RUNNING_FLAG.compareAndSet(false, true)) {
            log.error("The GlobalTaskDispatchWaitingQueueLooper already started, will not start again");
            return;
        }
        log.info("GlobalTaskDispatchWaitingQueueLooper starting...");
        super.start();
        log.info("GlobalTaskDispatchWaitingQueueLooper started...");
    }

    @Override
    public void run() {
        DefaultTaskExecuteRunnable defaultTaskExecuteRunnable;
        while (RUNNING_FLAG.get()) {
            try {
                defaultTaskExecuteRunnable = globalTaskDispatchWaitingQueue.takeNeedToDispatchTaskExecuteRunnable();
            } catch (InterruptedException e) {
                log.warn("Get waiting dispatch task failed, the current thread has been interrupted, will stop loop");
                Thread.currentThread().interrupt();
                break;
            }
            try {
                final TaskDispatcher taskDispatcher = taskDispatchFactory
                        .getTaskDispatcher(defaultTaskExecuteRunnable.getTaskInstance().getTaskType());
                taskDispatcher.dispatchTask(defaultTaskExecuteRunnable);
                DISPATCHED_TIMES.set(0);
            } catch (Exception e) {
                globalTaskDispatchWaitingQueue.submitNeedToDispatchTaskExecuteRunnable(defaultTaskExecuteRunnable);
                if (DISPATCHED_TIMES.incrementAndGet() > MAX_DISPATCHED_FAILED_TIMES) {
                    ThreadUtils.sleep(10 * 1000L);
                }
                log.error("Dispatch task failed", e);
            }
        }
        log.info("GlobalTaskDispatchWaitingQueueLooper started...");
    }

    @Override
    public void close() throws Exception {
        if (RUNNING_FLAG.compareAndSet(true, false)) {
            log.info("GlobalTaskDispatchWaitingQueueLooper stopping...");
            log.info("GlobalTaskDispatchWaitingQueueLooper stopped...");
        }
    }
}
