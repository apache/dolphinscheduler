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

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.message.MasterMessageSenderManager;
import org.apache.dolphinscheduler.server.master.runner.task.IAsyncLogicTask;
import org.apache.dolphinscheduler.server.master.runner.task.LogicTaskPluginFactoryBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncMasterDelayTaskExecuteRunnable extends MasterDelayTaskExecuteRunnable {

    private final AsyncMasterTaskDelayQueue asyncMasterTaskDelayQueue;

    public AsyncMasterDelayTaskExecuteRunnable(TaskExecutionContext taskExecutionContext,
                                               LogicTaskPluginFactoryBuilder logicTaskPluginFactoryBuilder,
                                               MasterMessageSenderManager masterMessageSenderManager,
                                               AsyncMasterTaskDelayQueue asyncTaskDelayQueue) {
        super(taskExecutionContext, logicTaskPluginFactoryBuilder, masterMessageSenderManager);
        this.asyncMasterTaskDelayQueue = asyncTaskDelayQueue;
    }

    @Override
    protected void executeTask() throws MasterTaskExecuteException {
        if (logicTask == null) {
            throw new MasterTaskExecuteException("The task plugin instance is null");
        }
        final IAsyncLogicTask iAsyncLogicTask = (IAsyncLogicTask) logicTask;
        // we execute the handle method here, but for async task, this method will not block
        // submit the task to async task queue
        final AsyncTaskExecutionContext asyncTaskExecutionContext = new AsyncTaskExecutionContext(
                taskExecutionContext,
                iAsyncLogicTask.getAsyncTaskExecuteFunction(),
                new AsyncTaskCallbackFunctionImpl(this));
        asyncMasterTaskDelayQueue.addAsyncTask(asyncTaskExecutionContext);
    }

    @Override
    protected void afterExecute() {
        // Do nothing, async task is not finished yet
    }
}
