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

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.config.TaskExecuteThreadsFullPolicy;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.metrics.WorkerServerMetrics;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GlobalTaskInstanceDispatchQueue {

    private final WorkerConfig workerConfig;

    private final BlockingQueue<TaskExecutionContext> blockingQueue;

    public GlobalTaskInstanceDispatchQueue(WorkerConfig workerConfig) {
        this.workerConfig = workerConfig;
        this.blockingQueue = new ArrayBlockingQueue<>(workerConfig.getExecThreads());
    }

    public boolean addDispatchTask(TaskExecutionContext taskExecutionContext) {
        if (workerConfig.getTaskExecuteThreadsFullPolicy() == TaskExecuteThreadsFullPolicy.CONTINUE) {
            return blockingQueue.offer(taskExecutionContext);
        }

        if (blockingQueue.size() > getQueueSize()) {
            log.warn("Wait submit queue is full, will retry submit task later");
            WorkerServerMetrics.incWorkerSubmitQueueIsFullCount();
            return false;
        }
        return blockingQueue.offer(taskExecutionContext);
    }

    public TaskExecutionContext take() throws InterruptedException {
        return blockingQueue.take();
    }

    public void clearTask() {
        blockingQueue.clear();
    }

    public int getQueueSize() {
        return workerConfig.getExecThreads();
    }

}
