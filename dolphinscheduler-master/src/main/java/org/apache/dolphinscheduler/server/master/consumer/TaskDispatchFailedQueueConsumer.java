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
import org.apache.dolphinscheduler.service.queue.TaskFailedRetryPriority;
import org.apache.dolphinscheduler.service.queue.TaskPriority;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueue;

import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TaskDispatchFailedQueueConsumer extends BaseDaemonThread {

    /**
     * taskPriorityQueue
     */
    @Autowired
    private TaskPriorityQueue<TaskPriority> taskPriorityQueueImpl;

    /**
     * taskDispatchFailedQueue
     */
    @Autowired
    private TaskPriorityQueue<TaskFailedRetryPriority> taskDispatchFailedQueueImpl;

    @Autowired
    private MasterConfig masterConfig;

    private ThreadPoolExecutor retryConsumerThreadPoolExecutor;

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
                dispatchFailedBackToTaskPriorityQueue(masterConfig.getDispatchTaskNumber());
            } catch (Exception e) {
                TaskMetrics.incTaskDispatchError();
                log.error("failed task retry error", e);
            }
        }
    }

    /**
     * put the failed dispatch task into the dispatch queue again
     */
    private void dispatchFailedBackToTaskPriorityQueue(int fetchTaskNum) {
        for (int i = 0; i < fetchTaskNum; i++) {
            try {
                TaskFailedRetryPriority delayTaskPriority = taskDispatchFailedQueueImpl.poll(Constants.SLEEP_TIME_MILLIS, TimeUnit.MILLISECONDS);
                if (Objects.isNull(delayTaskPriority)) {
                    continue;
                }
                retryConsumerThreadPoolExecutor.submit(() -> {
                    TaskPriority retryTaskPriority = delayTaskPriority.getTaskPriority();
                    retryTaskPriority.setDispatchFailedRetryTimes(retryTaskPriority.getDispatchFailedRetryTimes() + 1);
                    taskPriorityQueueImpl.put(retryTaskPriority);
                });
            } catch (InterruptedException exception) {
                log.error("dispatch failed queue poll error", exception);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("dispatch failed back to task priority queue error", e);
            } finally {
                ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);
            }
        }
    }
}
