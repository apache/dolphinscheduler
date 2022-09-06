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

import lombok.NonNull;
import org.apache.dolphinscheduler.common.storage.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.async.AsyncTaskCallbackFunction;
import org.apache.dolphinscheduler.plugin.task.api.async.AsyncTaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerMessageSender;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.task.TaskPluginManager;

import javax.annotation.Nullable;

public class AsyncWorkerDelayTaskExecuteRunnable extends WorkerDelayTaskExecuteRunnable {

    private AsyncTaskExecutionContext asyncTaskExecutionContext;

    public AsyncWorkerDelayTaskExecuteRunnable(@NonNull TaskExecutionContext taskExecutionContext,
                                               @NonNull WorkerConfig workerConfig,
                                               @NonNull String workflowMasterAddress,
                                               @NonNull WorkerMessageSender workerMessageSender,
                                               @NonNull AlertClientService alertClientService,
                                               @NonNull TaskPluginManager taskPluginManager,
                                               @Nullable StorageOperate storageOperate) {
        super(taskExecutionContext, workerConfig, workflowMasterAddress, workerMessageSender, alertClientService,
                taskPluginManager, storageOperate);
    }

    @Override
    public void executeTask() throws TaskException {
        if (task == null) {
            throw new TaskException("The task plugin instance is null");
        }
        // we execute the handle method here, but for async task, this method will not block
        task.handle();
        // submit the task to async task queue
        asyncTaskExecutionContext = new AsyncTaskExecutionContext(
                taskExecutionContext,
                task.getAsyncTaskExecuteFunction(),
                new AsyncTaskCallbackFunctionImpl(this));
        AsyncTaskDelayQueue.addAsyncTask(asyncTaskExecutionContext);
    }

    public AsyncTaskExecutionContext getAsyncTaskExecutionContext() {
        return asyncTaskExecutionContext;
    }

    @Override
    protected void afterExecute() throws TaskException {
        // do nothing, since this task doesn't really finished
    }

    @Override
    public void afterThrowing(Throwable throwable) throws TaskException {
        // need to clear from the async queue
        super.afterThrowing(throwable);
    }

    public static class AsyncTaskCallbackFunctionImpl implements AsyncTaskCallbackFunction {

        private final AsyncWorkerDelayTaskExecuteRunnable asyncWorkerDelayTaskExecuteRunnable;

        public AsyncTaskCallbackFunctionImpl(@NonNull AsyncWorkerDelayTaskExecuteRunnable asyncWorkerDelayTaskExecuteRunnable) {
            this.asyncWorkerDelayTaskExecuteRunnable = asyncWorkerDelayTaskExecuteRunnable;
        }

        @Override
        public void executeRunning() {
            AsyncTaskDelayQueue.addAsyncTask(asyncWorkerDelayTaskExecuteRunnable.getAsyncTaskExecutionContext());
        }

        @Override
        public void executeSuccess() {
            executeFinished();
        }

        @Override
        public void executeFailed() {
            executeFinished();
        }

        @Override
        public void executeThrowing(Throwable throwable) {
            asyncWorkerDelayTaskExecuteRunnable.afterThrowing(throwable);
        }

        private void executeFinished() {
            if (asyncWorkerDelayTaskExecuteRunnable.task == null) {
                throw new TaskException("The current task instance is null");
            }
            asyncWorkerDelayTaskExecuteRunnable.sendAlertIfNeeded();

            asyncWorkerDelayTaskExecuteRunnable.sendTaskResult();

            TaskExecutionContextCacheManager.removeByTaskInstanceId(
                    asyncWorkerDelayTaskExecuteRunnable.getTaskExecutionContext().getTaskInstanceId());
            asyncWorkerDelayTaskExecuteRunnable.clearTaskExecPathIfNeeded();

        }
    }

}
