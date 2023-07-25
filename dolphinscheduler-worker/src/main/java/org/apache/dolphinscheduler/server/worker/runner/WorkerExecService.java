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

import org.apache.dolphinscheduler.server.worker.metrics.WorkerServerMetrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

@Slf4j
public class WorkerExecService {

    private final ListeningExecutorService listeningExecutorService;

    /**
     * thread executor service
     */
    private final ExecutorService execService;

    /**
     * running task
     */
    private final ConcurrentHashMap<Integer, WorkerTaskExecuteRunnable> taskExecuteThreadMap;

    public WorkerExecService(ExecutorService execService,
                             ConcurrentHashMap<Integer, WorkerTaskExecuteRunnable> taskExecuteThreadMap) {
        this.execService = execService;
        this.listeningExecutorService = MoreExecutors.listeningDecorator(this.execService);
        this.taskExecuteThreadMap = taskExecuteThreadMap;
        WorkerServerMetrics.registerWorkerTaskTotalGauge(taskExecuteThreadMap::size);
    }

    public void submit(final WorkerTaskExecuteRunnable taskExecuteThread) {
        taskExecuteThreadMap.put(taskExecuteThread.getTaskExecutionContext().getTaskInstanceId(), taskExecuteThread);
        ListenableFuture future = this.listeningExecutorService.submit(taskExecuteThread);
        FutureCallback futureCallback = new FutureCallback() {

            @Override
            public void onSuccess(Object o) {
                taskExecuteThreadMap.remove(taskExecuteThread.getTaskExecutionContext().getTaskInstanceId());
            }

            @Override
            public void onFailure(Throwable throwable) {
                log.error("task execute failed, processInstanceId:{}, taskInstanceId:{}",
                        taskExecuteThread.getTaskExecutionContext().getProcessInstanceId(),
                        taskExecuteThread.getTaskExecutionContext().getTaskInstanceId(),
                        throwable);
                taskExecuteThreadMap.remove(taskExecuteThread.getTaskExecutionContext().getTaskInstanceId());
            }
        };
        Futures.addCallback(future, futureCallback, this.listeningExecutorService);
    }

    /**
     * get thread pool queue size
     *
     * @return queue size
     */
    public int getThreadPoolQueueSize() {
        return ((ThreadPoolExecutor) this.execService).getQueue().size();
    }

    public int getActiveExecThreadCount() {
        return ((ThreadPoolExecutor) this.execService).getActiveCount();
    }

    public Map<Integer, WorkerTaskExecuteRunnable> getTaskExecuteThreadMap() {
        return taskExecuteThreadMap;
    }

}
