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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.storage.StorageOperate;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.metrics.WorkerServerMetrics;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

/**
 * Manage tasks
 */
@Component
public class WorkerManagerThread implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(WorkerManagerThread.class);

    /**
     * task queue
     */
    private final BlockingQueue<TaskExecuteThread> waitSubmitQueue;

    @Autowired(required = false)
    private StorageOperate storageOperate;

    /**
     * thread executor service
     */
    private final WorkerExecService workerExecService;

    /**
     * task callback service
     */
    @Autowired
    private WorkerMessageSender workerMessageSender;

    private volatile int workerExecThreads;

    /**
     * running task
     */
    private final ConcurrentHashMap<Integer, TaskExecuteThread> taskExecuteThreadMap = new ConcurrentHashMap<>();

    public WorkerManagerThread(WorkerConfig workerConfig) {
        workerExecThreads = workerConfig.getExecThreads();
        this.waitSubmitQueue = new DelayQueue<>();
        workerExecService = new WorkerExecService(
                ThreadUtils.newDaemonFixedThreadExecutor("Worker-Execute-Thread", workerConfig.getExecThreads()),
                taskExecuteThreadMap);
    }

    public TaskExecuteThread getTaskExecuteThread(Integer taskInstanceId) {
        return this.taskExecuteThreadMap.get(taskInstanceId);
    }

    /**
     * get wait submit queue size
     *
     * @return queue size
     */
    public int getWaitSubmitQueueSize() {
        return waitSubmitQueue.size();
    }

    /**
     * get thread pool queue size
     *
     * @return queue size
     */
    public int getThreadPoolQueueSize() {
        return this.workerExecService.getThreadPoolQueueSize();
    }

    /**
     * Kill tasks that have not been executed, like delay task
     * then send Response to Master, update the execution status of task instance
     */
    public void killTaskBeforeExecuteByInstanceId(Integer taskInstanceId) {
        waitSubmitQueue.stream()
                .filter(taskExecuteThread -> taskExecuteThread.getTaskExecutionContext()
                        .getTaskInstanceId() == taskInstanceId)
                .forEach(waitSubmitQueue::remove);
    }

    /**
     * submit task
     *
     * @param taskExecuteThread taskExecuteThread
     * @return submit result
     */
    public boolean offer(TaskExecuteThread taskExecuteThread) {
        if (waitSubmitQueue.size() > workerExecThreads) {
            WorkerServerMetrics.incWorkerSubmitQueueIsFullCount();
            // if waitSubmitQueue is full, it will wait 1s, then try add
            ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);
            if (waitSubmitQueue.size() > workerExecThreads) {
                return false;
            }
        }
        return waitSubmitQueue.offer(taskExecuteThread);
    }

    public void start() {
        logger.info("Worker manager thread starting");
        Thread thread = new Thread(this, this.getClass().getName());
        thread.setDaemon(true);
        thread.start();
        logger.info("Worker manager thread started");
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Worker-Execute-Manager-Thread");
        TaskExecuteThread taskExecuteThread;
        while (!ServerLifeCycleManager.isStopped()) {
            try {
                if (!ServerLifeCycleManager.isRunning()) {
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                }
                if (this.getThreadPoolQueueSize() <= workerExecThreads) {
                    taskExecuteThread = waitSubmitQueue.take();
                    workerExecService.submit(taskExecuteThread);
                } else {
                    WorkerServerMetrics.incWorkerOverloadCount();
                    logger.info("Exec queue is full, waiting submit queue {}, waiting exec queue size {}",
                            this.getWaitSubmitQueueSize(), this.getThreadPoolQueueSize());
                    ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);
                }
            } catch (Exception e) {
                logger.error("An unexpected interrupt is happened, "
                        + "the exception will be ignored and this thread will continue to run", e);
            }
        }
    }

    public void clearTask() {
        waitSubmitQueue.clear();
        workerExecService.getTaskExecuteThreadMap().values().forEach(TaskExecuteThread::kill);
        workerExecService.getTaskExecuteThreadMap().clear();
    }
}
