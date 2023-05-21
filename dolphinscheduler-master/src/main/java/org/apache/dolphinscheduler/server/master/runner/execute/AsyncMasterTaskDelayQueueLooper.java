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

package org.apache.dolphinscheduler.server.master.runner.execute;

import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AsyncMasterTaskDelayQueueLooper extends BaseDaemonThread implements AutoCloseable {

    @Autowired
    private AsyncMasterTaskDelayQueue asyncMasterTaskDelayQueue;

    @Autowired
    private MasterConfig masterConfig;

    private static final AtomicBoolean RUNNING_FLAG = new AtomicBoolean(false);

    private ExecutorService asyncTaskStateCheckThreadPool;

    public AsyncMasterTaskDelayQueueLooper() {
        super("AsyncMasterTaskDelayQueueLooper");
    }

    @Override
    public synchronized void start() {
        if (!RUNNING_FLAG.compareAndSet(false, true)) {
            log.info("The AsyncMasterTaskDelayQueueLooper has already been started, will not start again");
            return;
        }

        log.info("AsyncMasterTaskDelayQueueLooper starting...");
        super.start();
        log.info("AsyncMasterTaskDelayQueueLooper started...");
    }

    @Override
    public void run() {
        asyncTaskStateCheckThreadPool = ThreadUtils.newDaemonFixedThreadExecutor("AsyncTaskStateCheckThreadPool",
                masterConfig.getMasterAsyncTaskStateCheckThreadPoolSize());
        while (RUNNING_FLAG.get()) {
            AsyncTaskExecutionContext asyncTaskExecutionContext;
            try {
                asyncTaskExecutionContext = asyncMasterTaskDelayQueue.pollAsyncTask();
            } catch (InterruptedException e) {
                log.error("AsyncConditionTaskLooper has been interrupted, will break this loop", e);
                Thread.currentThread().interrupt();
                break;
            }
            final TaskExecutionContext taskExecutionContext = asyncTaskExecutionContext.getTaskExecutionContext();
            try (
                    LogUtils.MDCAutoClosableContext mdcAutoClosableContext = LogUtils.setWorkflowAndTaskInstanceIDMDC(
                            taskExecutionContext.getProcessInstanceId(), taskExecutionContext.getTaskInstanceId());
                    LogUtils.MDCAutoClosableContext mdcAutoClosableContext1 =
                            LogUtils.setTaskInstanceLogFullPathMDC(taskExecutionContext.getLogPath())) {

                if (MasterTaskExecutionContextHolder
                        .getTaskExecutionContext(taskExecutionContext.getTaskInstanceId()) == null) {
                    log.warn(
                            "Cannot find the taskInstance from TaskExecutionContextCacheManager, the task may already been killed, will stop the async master task");
                    continue;
                }
                asyncTaskStateCheckThreadPool.submit(() -> {
                    final AsyncTaskExecuteFunction asyncTaskExecuteFunction =
                            asyncTaskExecutionContext.getAsyncTaskExecuteFunction();
                    final AsyncTaskCallbackFunction asyncTaskCallbackFunction =
                            asyncTaskExecutionContext.getAsyncTaskCallbackFunction();
                    try {
                        LogUtils.setTaskInstanceLogFullPathMDC(taskExecutionContext.getLogPath());
                        LogUtils.setTaskInstanceIdMDC(taskExecutionContext.getTaskInstanceId());
                        AsyncTaskExecuteFunction.AsyncTaskExecutionStatus asyncTaskExecutionStatus =
                                asyncTaskExecuteFunction.getAsyncTaskExecutionStatus();
                        switch (asyncTaskExecutionStatus) {
                            case RUNNING:
                                // If the task status is running, means the task real status is not finished. We
                                // will
                                // put it back to the queue to get the status again.
                                asyncMasterTaskDelayQueue.addAsyncTask(asyncTaskExecutionContext);
                                break;
                            case SUCCESS:
                                asyncTaskCallbackFunction.executeSuccess();
                                break;
                            case FAILED:
                                asyncTaskCallbackFunction.executeFailed();
                                break;
                        }
                    } catch (Exception ex) {
                        asyncTaskCallbackFunction.executeThrowing(ex);
                    } finally {
                        LogUtils.removeTaskInstanceLogFullPathMDC();
                        LogUtils.removeTaskInstanceIdMDC();
                    }
                });
            }
        }
        log.info("AsyncMasterTaskDelayQueueLooper closed...");
    }

    @Override
    public void close() throws Exception {
        if (!RUNNING_FLAG.compareAndSet(true, false)) {
            log.warn("The AsyncMasterTaskDelayQueueLooper is not started, will not close");
            return;
        }
        log.info("AsyncMasterTaskDelayQueueLooper closing...");
        asyncTaskStateCheckThreadPool.shutdown();
        log.info("AsyncMasterTaskDelayQueueLooper closed...");
    }
}
