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

import org.apache.dolphinscheduler.extract.master.transportor.ITaskExecutionEvent;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.config.TaskExecuteThreadsFullPolicy;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.worker.metrics.WorkerServerMetrics;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerMessageSender;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Component
@Slf4j
public class TaskCoordinator {

    private final Map<Integer, Integer> dispatchedAckMap = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduledExecutorService;

    private final WorkerConfig workerConfig;

    @Autowired
    private WorkerTaskExecutorThreadPool workerTaskExecutorThreadPool;

    @Autowired
    private WorkerTaskExecutorFactoryBuilder workerTaskExecutorFactoryBuilder;

    @Autowired
    private WorkerMessageSender workerMessageSender;

    public TaskCoordinator(
                           WorkerConfig workerConfig,
                           WorkerTaskExecutorThreadPool workerTaskExecutorThreadPool,
                           WorkerTaskExecutorFactoryBuilder workerTaskExecutorFactoryBuilder,
                           WorkerMessageSender workerMessageSender) {
        this.workerConfig = workerConfig;
        this.scheduledExecutorService = Executors.newScheduledThreadPool(
                this.workerConfig.getExecThreads(),
                new ThreadFactoryBuilder().setNameFormat("TaskCoordinator").setDaemon(true).build());
        this.workerTaskExecutorThreadPool = workerTaskExecutorThreadPool;
        this.workerTaskExecutorFactoryBuilder = workerTaskExecutorFactoryBuilder;
        this.workerMessageSender = workerMessageSender;
    }

    public boolean publishDispatchedEvent(WorkerTaskExecutor workerTaskExecutor) {
        if (TaskExecuteThreadsFullPolicy.CONTINUE.equals(workerConfig.getTaskExecuteThreadsFullPolicy())) {
            WorkerTaskExecutorHolder.put(workerTaskExecutor);
            sendDispatchedEvent(workerTaskExecutor);
            return true;
        }
        if (workerTaskExecutorThreadPool.isOverload()) {
            log.warn("WorkerTaskExecutorThreadPool is overload, cannot submit new WorkerTaskExecutor");
            WorkerServerMetrics.incWorkerSubmitQueueIsFullCount();
            return false;
        }
        WorkerTaskExecutorHolder.put(workerTaskExecutor);
        sendDispatchedEvent(workerTaskExecutor);
        return true;
    }

    protected void sendDispatchedEvent(WorkerTaskExecutor workerTaskExecutor) {
        workerMessageSender.sendMessageWithRetry(
                workerTaskExecutor.getTaskExecutionContext(),
                ITaskExecutionEvent.TaskInstanceExecutionEventType.DISPATCH);
    }

    public void onDispatchedEventAck(int taskInstanceId) {
        dispatchedAckMap.put(taskInstanceId, taskInstanceId);
    }

    public boolean register(TaskExecutionContext taskExecutionContext) {
        synchronized (TaskCoordinator.class) {
            WorkerTaskExecutor workerTaskExecutor = workerTaskExecutorFactoryBuilder
                    .createWorkerTaskExecutorFactory(taskExecutionContext)
                    .createWorkerTaskExecutor();

            if (!publishDispatchedEvent(workerTaskExecutor)) {
                log.info("Abort task: {} publishDispatchedEvent failed", taskExecutionContext.getTaskName());
                return false;
            }

            this.scheduledExecutorService.scheduleWithFixedDelay(() -> {
                if (!this.dispatchedAckMap.containsKey(taskExecutionContext.getTaskInstanceId())) {
                    log.info("Abort task: {}", taskExecutionContext.getTaskName());
                }

                TaskMetrics.incrTaskTypeExecuteCount(taskExecutionContext.getTaskType());

                if (!workerTaskExecutorThreadPool.submitWorkerTaskExecutor(workerTaskExecutor)) {
                    log.info("Submit task: {} to wait queue failed", taskExecutionContext.getTaskName());
                } else {

                    log.info("Submit task: {} to wait queue success", taskExecutionContext.getTaskName());
                }
            },
                    this.workerConfig.getRegisterInitialDelay(),
                    this.workerConfig.getRegisterDelay(),
                    TimeUnit.SECONDS);
            return true;
        }

    }
}
