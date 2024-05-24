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

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.server.worker.config.TaskExecuteThreadsFullPolicy;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.metrics.WorkerServerMetrics;

import java.util.concurrent.ThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkerTaskExecutorThreadPool {

    private final ThreadPoolExecutor threadPoolExecutor;

    private final WorkerConfig workerConfig;

    public WorkerTaskExecutorThreadPool(WorkerConfig workerConfig) {
        this.threadPoolExecutor =
                ThreadUtils.newDaemonFixedThreadExecutor("WorkerTaskExecutorThreadPool", workerConfig.getExecThreads());
        threadPoolExecutor.prestartAllCoreThreads();
        this.workerConfig = workerConfig;

        WorkerServerMetrics.registerWorkerExecuteQueueSizeGauge(this::getWaitingTaskExecutorSize);
        WorkerServerMetrics.registerWorkerActiveExecuteThreadGauge(this::getRunningTaskExecutorSize);
    }

    public boolean submitWorkerTaskExecutor(WorkerTaskExecutor workerTaskExecutor) {
        synchronized (WorkerTaskExecutorThreadPool.class) {
            if (TaskExecuteThreadsFullPolicy.CONTINUE.equals(workerConfig.getTaskExecuteThreadsFullPolicy())) {
                WorkerTaskExecutorHolder.put(workerTaskExecutor);
                threadPoolExecutor.execute(workerTaskExecutor);
                return true;
            }
            if (isOverload()) {
                log.warn("WorkerTaskExecutorThreadPool is overload, cannot submit new WorkerTaskExecutor");
                WorkerServerMetrics.incWorkerSubmitQueueIsFullCount();
                return false;
            }
            WorkerTaskExecutorHolder.put(workerTaskExecutor);
            threadPoolExecutor.execute(workerTaskExecutor);
            return true;
        }
    }

    public boolean isOverload() {
        return WorkerTaskExecutorHolder.size() >= workerConfig.getExecThreads();
    }

    public int getWaitingTaskExecutorSize() {
        if (WorkerTaskExecutorHolder.size() <= workerConfig.getExecThreads()) {
            return 0;
        } else {
            return WorkerTaskExecutorHolder.size() - workerConfig.getExecThreads();
        }
    }

    public int getRunningTaskExecutorSize() {
        return Math.min(WorkerTaskExecutorHolder.size(), workerConfig.getExecThreads());
    }

    /**
     * Kill tasks that have not been executed, e.g. waiting in the queue
     */
    public void killTaskBeforeExecuteByInstanceId(Integer taskInstanceId) {
        synchronized (WorkerTaskExecutorThreadPool.class) {
            WorkerTaskExecutor workerTaskExecutor = WorkerTaskExecutorHolder.get(taskInstanceId);
            threadPoolExecutor.remove(workerTaskExecutor);
        }
    }

    public void clearTask() {
        threadPoolExecutor.getQueue().clear();
    }
}
