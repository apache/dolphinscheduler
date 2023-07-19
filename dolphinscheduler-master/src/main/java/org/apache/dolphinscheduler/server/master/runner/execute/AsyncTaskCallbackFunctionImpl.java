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

import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.log.TaskInstanceLogHeader;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncTaskCallbackFunctionImpl implements AsyncTaskCallbackFunction {

    private final AsyncMasterDelayTaskExecuteRunnable asyncMasterDelayTaskExecuteRunnable;

    public AsyncTaskCallbackFunctionImpl(@NonNull AsyncMasterDelayTaskExecuteRunnable asyncMasterDelayTaskExecuteRunnable) {
        this.asyncMasterDelayTaskExecuteRunnable = asyncMasterDelayTaskExecuteRunnable;
    }

    @Override
    public void executeSuccess() {
        asyncMasterDelayTaskExecuteRunnable.getTaskExecutionContext()
                .setCurrentExecutionStatus(TaskExecutionStatus.SUCCESS);
        executeFinished();
    }

    @Override
    public void executeFailed() {
        asyncMasterDelayTaskExecuteRunnable.getTaskExecutionContext()
                .setCurrentExecutionStatus(TaskExecutionStatus.FAILURE);
        executeFinished();
    }

    @Override
    public void executeThrowing(Throwable throwable) {
        asyncMasterDelayTaskExecuteRunnable.afterThrowing(throwable);
    }

    private void executeFinished() {
        TaskInstanceLogHeader.printFinalizeTaskHeader();
        int taskInstanceId = asyncMasterDelayTaskExecuteRunnable.getTaskExecutionContext().getTaskInstanceId();
        MasterTaskExecutionContextHolder.removeTaskExecutionContext(taskInstanceId);
        MasterTaskExecuteRunnableHolder.removeMasterTaskExecuteRunnable(taskInstanceId);
        log.info("Task execute finished, removed the TaskExecutionContext");
        asyncMasterDelayTaskExecuteRunnable.sendTaskResult();
        log.info(
                "Execute task finished, will send the task execute result to master, the current task execute result is {}",
                asyncMasterDelayTaskExecuteRunnable.getTaskExecutionContext().getCurrentExecutionStatus().name());
        asyncMasterDelayTaskExecuteRunnable.closeLogAppender();
    }
}
