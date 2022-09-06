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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.NonNull;
import org.apache.dolphinscheduler.server.worker.metrics.WorkerServerMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class TaskExecuteThreadPool {

    /**
     * logger of WorkerExecService
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskExecuteThreadPool.class);

    private final ListeningExecutorService listeningExecutorService;

    private final ThreadPoolExecutor taskExecuteThreadPool;

    private final ConcurrentHashMap<Integer, WorkerTaskExecuteRunnable> taskExecuteThreadMap;

    public TaskExecuteThreadPool(ExecutorService taskExecuteThreadPool,
                                 ConcurrentHashMap<Integer, WorkerTaskExecuteRunnable> taskExecuteThreadMap) {
        this.taskExecuteThreadPool = (ThreadPoolExecutor) taskExecuteThreadPool;
        this.listeningExecutorService = MoreExecutors.listeningDecorator(this.taskExecuteThreadPool);
        this.taskExecuteThreadMap = taskExecuteThreadMap;
        WorkerServerMetrics.registerWorkerRunningTaskGauge(taskExecuteThreadMap::size);
    }

    public void submit(@NonNull final WorkerTaskExecuteRunnable workerTaskExecuteRunnable) {
        taskExecuteThreadMap.put(workerTaskExecuteRunnable.getTaskExecutionContext().getTaskInstanceId(),
                workerTaskExecuteRunnable);
        ListenableFuture future = this.listeningExecutorService.submit(workerTaskExecuteRunnable);
        FutureCallback futureCallback = new FutureCallback() {

            @Override
            public void onSuccess(Object o) {
                taskExecuteThreadMap.remove(workerTaskExecuteRunnable.getTaskExecutionContext().getTaskInstanceId());
            }

            @Override
            public void onFailure(Throwable throwable) {
                logger.error("task execute failed, processInstanceId:{}, taskInstanceId:{}",
                        workerTaskExecuteRunnable.getTaskExecutionContext().getProcessInstanceId(),
                        workerTaskExecuteRunnable.getTaskExecutionContext().getTaskInstanceId(),
                        throwable);
                taskExecuteThreadMap.remove(workerTaskExecuteRunnable.getTaskExecutionContext().getTaskInstanceId());
            }
        };
        Futures.addCallback(future, futureCallback, this.listeningExecutorService);
    }

    /**
     * get thread pool queue size
     *
     * @return queue size
     */
    public int getThreadPoolWaitingTaskNum() {
        return taskExecuteThreadPool.getQueue().size();
    }

    public int getThreadPoolRunningTaskNum() {
        // we don't calculate from execService, since we don't want to lock tge executeService
        return taskExecuteThreadMap.size();
    }

    public Map<Integer, WorkerTaskExecuteRunnable> getTaskExecuteThreadMap() {
        return taskExecuteThreadMap;
    }

    public List<WorkerTaskExecuteRunnable> getWaitingTask() {
        List<WorkerTaskExecuteRunnable> waitingTasks =
                Arrays.stream(taskExecuteThreadPool.getQueue().toArray(new WorkerTaskExecuteRunnable[0]))
                        .collect(Collectors.toList());
        return Collections.unmodifiableList(waitingTasks);
    }

}
