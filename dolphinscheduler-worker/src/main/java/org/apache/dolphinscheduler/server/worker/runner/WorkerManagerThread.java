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

import com.facebook.presto.jdbc.internal.javax.annotation.Nullable;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

/**
 * Manage tasks
 */
@Component
public class WorkerManagerThread implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(WorkerManagerThread.class);

    private final DelayQueue<WorkerDelayTaskExecuteRunnable> waitSubmitQueue;

    private final TaskExecuteThreadPool taskExecuteThreadPool;

    /**
     * running task
     */
    private final ConcurrentHashMap<Integer, WorkerTaskExecuteRunnable> taskExecuteThreadMap =
            new ConcurrentHashMap<>();

    public WorkerManagerThread(WorkerConfig workerConfig) {
        this.waitSubmitQueue = new DelayQueue<>();
        taskExecuteThreadPool = new TaskExecuteThreadPool(
                ThreadUtils.newDaemonFixedThreadExecutor("Worker-Execute-Thread", workerConfig.getExecThreads()),
                taskExecuteThreadMap);
    }

    public @Nullable WorkerTaskExecuteRunnable getTaskExecuteThread(Integer taskInstanceId) {
        return taskExecuteThreadMap.get(taskInstanceId);
    }

    public int getWaitSubmitQueueSize() {
        return waitSubmitQueue.size();
    }

    public int getThreadPoolWaitingTaskNum() {
        return taskExecuteThreadPool.getThreadPoolWaitingTaskNum();
    }

    public int getThreadPoolRunningTaskNum() {
        return taskExecuteThreadPool.getThreadPoolRunningTaskNum();
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

    public boolean offer(WorkerDelayTaskExecuteRunnable workerDelayTaskExecuteRunnable) {
        return waitSubmitQueue.offer(workerDelayTaskExecuteRunnable);
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
        while (!ServerLifeCycleManager.isStopped()) {
            try {
                if (!ServerLifeCycleManager.isRunning()) {
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                }
                final WorkerDelayTaskExecuteRunnable workerDelayTaskExecuteRunnable = waitSubmitQueue.take();

                taskExecuteThreadPool.submit(workerDelayTaskExecuteRunnable);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                logger.warn("Worker execute manager thread has been interrupted, will stop", ex);
                break;
            } catch (Exception e) {
                logger.error("An unexpected interrupt is happened, "
                        + "the exception will be ignored and this thread will continue to run", e);
                try {
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    logger.warn("Worker execute manager thread has been interrupted, will stop", ex);
                    break;
                }
            }
        }
    }

    public List<WorkerTaskExecuteRunnable> getWaitingTask() {
        return taskExecuteThreadPool.getWaitingTask();
    }

    public void clearTask() {
        waitSubmitQueue.clear();
        taskExecuteThreadPool.getTaskExecuteThreadMap().values().forEach(WorkerTaskExecuteRunnable::cancelTask);
        taskExecuteThreadPool.getTaskExecuteThreadMap().clear();
    }
}
