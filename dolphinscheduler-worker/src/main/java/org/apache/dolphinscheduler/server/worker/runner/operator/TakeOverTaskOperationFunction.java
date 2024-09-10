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

package org.apache.dolphinscheduler.server.worker.runner.operator;

import org.apache.dolphinscheduler.extract.worker.transportor.TakeOverTaskRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TakeOverTaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecutor;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecutorHolder;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TakeOverTaskOperationFunction
        implements
            ITaskInstanceOperationFunction<TakeOverTaskRequest, TakeOverTaskResponse> {

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    public TakeOverTaskOperationFunction(MessageRetryRunner messageRetryRunner) {
        this.messageRetryRunner = messageRetryRunner;
    }

    @Override
    public TakeOverTaskResponse operate(TakeOverTaskRequest takeOverTaskRequest) {
        try {
            final int taskInstanceId = takeOverTaskRequest.getTaskInstanceId();
            final String workflowHost = takeOverTaskRequest.getWorkflowHost();

            LogUtils.setTaskInstanceIdMDC(taskInstanceId);
            log.info("Received TakeOverTaskRequest: {}", takeOverTaskRequest);

            boolean updateWorkerTaskExecutor = updateHostInWorkflowTaskExecutor(taskInstanceId, workflowHost);
            boolean updateMessage = updateHostInMessage(taskInstanceId, workflowHost);
            if (updateWorkerTaskExecutor || updateMessage) {
                return TakeOverTaskResponse.success();
            }
            return TakeOverTaskResponse.failed("The taskInstance is not in the worker");
        } finally {
            LogUtils.removeTaskInstanceIdMDC();
            LogUtils.removeTaskInstanceLogFullPathMDC();
        }
    }

    private boolean updateHostInWorkflowTaskExecutor(int taskInstanceId, String workflowHost) {
        WorkerTaskExecutor workerTaskExecutor = WorkerTaskExecutorHolder.get(taskInstanceId);
        if (workerTaskExecutor == null) {
            return false;
        }
        TaskExecutionContext taskExecutionContext = workerTaskExecutor.getTaskExecutionContext();
        taskExecutionContext.setWorkflowInstanceHost(workflowHost);
        return true;
    }

    private boolean updateHostInMessage(int taskInstanceId, String workflowHost) {
        return messageRetryRunner.updateMessageHost(taskInstanceId, workflowHost);
    }
}
