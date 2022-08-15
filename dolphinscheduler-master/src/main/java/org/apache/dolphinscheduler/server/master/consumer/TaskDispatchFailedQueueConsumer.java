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

package org.apache.dolphinscheduler.server.master.consumer;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.metrics.TaskMetrics;
import org.apache.dolphinscheduler.service.exceptions.TaskPriorityQueueException;
import org.apache.dolphinscheduler.service.queue.TaskPriority;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueue;

import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TaskDispatchFailedQueueConsumer extends BaseDaemonThread {

    private static final Logger logger = LoggerFactory.getLogger(TaskDispatchFailedQueueConsumer.class);

    /**
     * taskPriorityQueue
     */
    @Autowired
    @Qualifier(Constants.TASK_PRIORITY_QUEUE)
    private TaskPriorityQueue<TaskPriority> taskPriorityQueueImpl;

    /**
     * taskDispatchFailedQueue
     */
    @Autowired
    @Qualifier(Constants.TASK_DISPATCH_FAILED_QUEUE)
    private TaskPriorityQueue<TaskPriority> taskDispatchFailedQueueImpl;

    @Autowired
    private MasterConfig masterConfig;

    private ThreadPoolExecutor retryConsumerThreadPoolExecutor;

    /**
     * delay time for retries
     */
    private static final Long[] TIME_DELAY;

    /**
     * initialization failure retry delay rule
     */
    static {
        TIME_DELAY = new Long[Constants.DEFAULT_MAX_RETRY_COUNT];
        for (int i = 0; i < Constants.DEFAULT_MAX_RETRY_COUNT; i++) {
            long delayTime = (i + 1) * Constants.SLEEP_TIME_MILLIS;
            TIME_DELAY[i] = delayTime;
        }
    }

    protected TaskDispatchFailedQueueConsumer() {
        super("TaskDispatchFailedQueueConsumerThread");
    }

    @PostConstruct
    public void init() {
        this.retryConsumerThreadPoolExecutor = (ThreadPoolExecutor) ThreadUtils
                .newDaemonFixedThreadExecutor("TaskDispatchFailedQueueConsumerThread", masterConfig.getDispatchTaskNumber());
        super.start();
    }

    @Override
    public void run() {
        while (!ServerLifeCycleManager.isStopped()) {
            try {
                failedRetry();
            } catch (Exception e) {
                TaskMetrics.incTaskDispatchError();
                logger.error("failed task retry error", e);
            } finally {
                ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);
            }
        }
    }

    public void failedRetry() throws TaskPriorityQueueException {
        if (taskDispatchFailedQueueImpl.size() > 0) {
            retryConsumerThreadPoolExecutor.submit(() -> dispatchFailedBackToTaskPriorityQueue(masterConfig.getDispatchTaskNumber()));
        }
    }

    /**
     * put the failed dispatch task into the dispatch queue again
     */
    private void dispatchFailedBackToTaskPriorityQueue(int fetchTaskNum) {
        for (int i = 0; i < fetchTaskNum; i++) {
            try {
                TaskPriority dispatchFailedTaskPriority = taskDispatchFailedQueueImpl.poll(Constants.SLEEP_TIME_MILLIS, TimeUnit.MILLISECONDS);
                if (Objects.isNull(dispatchFailedTaskPriority)) {
                    continue;
                }
                if (canRetry(dispatchFailedTaskPriority)) {
                    dispatchFailedTaskPriority.setDispatchFailedRetryTimes(dispatchFailedTaskPriority.getDispatchFailedRetryTimes() + 1);
                    taskPriorityQueueImpl.put(dispatchFailedTaskPriority);
                } else {
                    taskDispatchFailedQueueImpl.put(dispatchFailedTaskPriority);
                }
            } catch (InterruptedException exception) {
                logger.error("dispatch failed queue poll error", exception);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error("dispatch failed back to task priority queue error", e);
            }
        }
    }

    /**
     * the time interval is adjusted according to the number of retries
     */
    private boolean canRetry (TaskPriority taskPriority){
        int dispatchFailedRetryTimes = taskPriority.getDispatchFailedRetryTimes();
        long now = System.currentTimeMillis();
        // retry more than 100 times with 100 seconds delay each time
        if (dispatchFailedRetryTimes >= Constants.DEFAULT_MAX_RETRY_COUNT){
            return now - taskPriority.getLastDispatchTime() >= TIME_DELAY[Constants.DEFAULT_MAX_RETRY_COUNT - 1];
        }
        return now - taskPriority.getLastDispatchTime() >= TIME_DELAY[dispatchFailedRetryTimes];
    }
}
