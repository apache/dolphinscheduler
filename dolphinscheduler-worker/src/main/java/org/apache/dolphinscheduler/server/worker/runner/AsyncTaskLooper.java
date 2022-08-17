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

import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.async.AsyncTaskCallbackFunction;
import org.apache.dolphinscheduler.plugin.task.api.async.AsyncTaskExecuteFunction;
import org.apache.dolphinscheduler.plugin.task.api.async.AsyncTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.async.AsyncTaskExecutionStatus;
import org.apache.dolphinscheduler.remote.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class AsyncTaskLooper extends BaseDaemonThread {

    private final Logger logger = LoggerFactory.getLogger(AsyncTaskLooper.class);

    private final ExecutorService asyncCheckThreadPool = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 2, new NamedThreadFactory("AsyncTaskCheckThreadPool"));

    protected AsyncTaskLooper() {
        super("AsyncConditionTaskLooper");
    }

    @PostConstruct
    @Override
    public synchronized void start() {
        logger.info("Master Event execute service starting");
        super.start();
        logger.info("Master Event execute service started");
    }

    @Override
    public void run() {
        while (Stopper.isRunning()) {
            try {
                AsyncTaskExecutionContext asyncTaskExecutionContext = AsyncTaskDelayQueue.pollAsyncTask();
                if (asyncTaskExecutionContext == null) {
                    continue;
                }
                final TaskExecutionContext taskExecutionContext = asyncTaskExecutionContext.getTaskExecutionContext();
                if (TaskExecutionContextCacheManager.getByTaskInstanceId(taskExecutionContext.getTaskInstanceId()) == null) {
                    logger.warn("Cannot find the taskInstance from TaskExecutionContextCacheManager, the task may already been killed");
                    continue;
                }
                asyncCheckThreadPool.submit(() -> {
                    Thread.currentThread().setName(taskExecutionContext.getTaskLogName());
                    final AsyncTaskExecuteFunction asyncTaskExecuteFunction = asyncTaskExecutionContext.getAsyncTaskExecuteFunction();
                    final AsyncTaskCallbackFunction asyncTaskCallbackFunction = asyncTaskExecutionContext.getAsyncTaskCallbackFunction();
                    try {
                        AsyncTaskExecutionStatus asyncTaskExecutionStatus = asyncTaskExecuteFunction.getTaskExecuteStatus();
                        switch (asyncTaskExecutionStatus) {
                            case RUNNING:
                                asyncTaskCallbackFunction.executeRunning();
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
                    }
                });
            } catch (InterruptedException e) {
                logger.info("AsyncConditionTaskLooper has been interrupted, will break this loop", e);
                break;
            }
        }
    }
}
