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
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerMessageSender;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.storage.StorageOperate;
import org.apache.dolphinscheduler.service.task.TaskPluginManager;

import javax.annotation.Nullable;

public class DefaultWorkerDelayTaskExecuteRunnable extends WorkerDelayTaskExecuteRunnable {

    public DefaultWorkerDelayTaskExecuteRunnable(@NonNull TaskExecutionContext taskExecutionContext,
                                                 @NonNull WorkerConfig workerConfig,
                                                 @NonNull String workflowMaster,
                                                 @NonNull WorkerMessageSender workerMessageSender,
                                                 @NonNull AlertClientService alertClientService,
                                                 @NonNull TaskPluginManager taskPluginManager,
                                                 @Nullable StorageOperate storageOperate) {
        super(taskExecutionContext, workerConfig, workflowMaster, workerMessageSender, alertClientService, taskPluginManager, storageOperate);
    }

    @Override
    public void executeTask(TaskCallBack taskCallBack) throws TaskException {
        if (task == null) {
            throw new TaskException("The task plugin instance is not initialized");
        }
        task.handle(taskCallBack);
    }

    @Override
    protected void afterExecute() {
        super.afterExecute();
    }

    @Override
    protected void afterThrowing(Throwable throwable) throws TaskException {
        super.afterThrowing(throwable);
    }
}
