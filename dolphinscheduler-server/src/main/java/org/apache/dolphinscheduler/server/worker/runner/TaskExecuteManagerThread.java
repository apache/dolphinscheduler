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

package org.apache.dolphinscheduler.server.worker.runner;

import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Manage tasks
 */
@Component
public class TaskExecuteManagerThread implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(TaskExecuteManagerThread.class);

    /**
     * task queue
     */
    private final DelayQueue<TaskExecuteThread> workerExecuteQueue = new DelayQueue<>();

    /**
     * worker config
     */
    private final WorkerConfig workerConfig;

    /**
     * thread executor service
     */
    private final ExecutorService workerExecService;

    public TaskExecuteManagerThread() {
        this.workerConfig = SpringApplicationContext.getBean(WorkerConfig.class);
        this.workerExecService = ThreadUtils.newDaemonFixedThreadExecutor("Worker-Execute-Thread", this.workerConfig.getWorkerExecThreads());
    }

    /**
     * get queue size
     *
     * @return queue size
     */
    public int getQueueSize() {
        return workerExecuteQueue.size();
    }

    /**
     * submit task
     *
     * @param taskExecuteThread taskExecuteThread
     * @return submit result
     */
    public boolean offer(TaskExecuteThread taskExecuteThread) {
        return workerExecuteQueue.offer(taskExecuteThread);
    }

    public void start() {
        Thread thread = new Thread(this, this.getClass().getName());
        thread.start();
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Worker-Execute-Manager-Thread");
        TaskExecuteThread taskExecuteThread;
        while (Stopper.isRunning()) {
            try {
                taskExecuteThread = workerExecuteQueue.take();
                workerExecService.submit(taskExecuteThread);
            } catch (Exception e) {
                logger.error("An unexpected interrupt is happened, "
                        + "the exception will be ignored and this thread will continue to run", e);
            }
        }
    }
}
