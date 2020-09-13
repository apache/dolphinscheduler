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

package org.apache.dolphinscheduler.server.worker.processor;

import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.server.worker.runner.TaskExecuteThread;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage tasks that need to be delayed
 */
public class TaskDelayExecManagerThread implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(TaskDelayExecManagerThread.class);

    private final DelayQueue<TaskExecuteThread> delayQueue = new DelayQueue<>();

    private final ExecutorService workerExecService;

    public TaskDelayExecManagerThread(ExecutorService executorService) {
        this.workerExecService = executorService;
    }

    public boolean offer(TaskExecuteThread taskExecuteThread) {
        return delayQueue.offer(taskExecuteThread);
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Worker-Delay-Exec-Manager-Thread");
        TaskExecuteThread taskExecuteThread;
        while (Stopper.isRunning()) {
            try {
                taskExecuteThread = delayQueue.take();
                workerExecService.submit(taskExecuteThread);
            } catch (Exception e) {
                logger.error("An unexpected interrupt is happened, "
                        + "the exception will be ignored and this thread will continue to run", e);
            }
        }
    }
}
