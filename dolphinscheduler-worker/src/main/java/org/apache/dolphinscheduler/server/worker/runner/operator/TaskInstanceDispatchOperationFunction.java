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

import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceDispatchRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceDispatchResponse;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecutor;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecutorFactoryBuilder;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecutorThreadPool;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskInstanceDispatchOperationFunction
        implements
            ITaskInstanceOperationFunction<TaskInstanceDispatchRequest, TaskInstanceDispatchResponse> {

    @Autowired
    private WorkerConfig workerConfig;

    @Autowired
    private WorkerTaskExecutorFactoryBuilder workerTaskExecutorFactoryBuilder;

    @Autowired
    private WorkerTaskExecutorThreadPool workerTaskExecutorThreadPool;

    public TaskInstanceDispatchOperationFunction(
                                                 WorkerConfig workerConfig,
                                                 WorkerTaskExecutorFactoryBuilder workerTaskExecutorFactoryBuilder,
                                                 WorkerTaskExecutorThreadPool workerTaskExecutorThreadPool) {
        this.workerConfig = workerConfig;
        this.workerTaskExecutorFactoryBuilder = workerTaskExecutorFactoryBuilder;
        this.workerTaskExecutorThreadPool = workerTaskExecutorThreadPool;
    }

    @Override
    public TaskInstanceDispatchResponse operate(TaskInstanceDispatchRequest taskInstanceDispatchRequest) {
        log.info("Receive TaskInstanceDispatchRequest: {}", taskInstanceDispatchRequest);
        TaskExecutionContext taskExecutionContext = taskInstanceDispatchRequest.getTaskExecutionContext();
        try {
            taskExecutionContext.setHost(workerConfig.getWorkerAddress());
            taskExecutionContext.setLogPath(LogUtils.getTaskInstanceLogFullPath(taskExecutionContext));

            LogUtils.setWorkflowAndTaskInstanceIDMDC(taskExecutionContext.getProcessInstanceId(),
                    taskExecutionContext.getTaskInstanceId());

            // check server status, if server is not running, return failed to reject this task
            if (!ServerLifeCycleManager.isRunning()) {
                log.error("server is not running. reject task: {}", taskExecutionContext.getProcessInstanceId());
                return TaskInstanceDispatchResponse.failed(taskExecutionContext.getTaskInstanceId(),
                        "server is not running");
            }

            TaskMetrics.incrTaskTypeExecuteCount(taskExecutionContext.getTaskType());

            WorkerTaskExecutor workerTaskExecutor = workerTaskExecutorFactoryBuilder
                    .createWorkerTaskExecutorFactory(taskExecutionContext).createWorkerTaskExecutor();
            if (!workerTaskExecutorThreadPool.submitWorkerTaskExecutor(workerTaskExecutor)) {
                log.info("Submit task: {} to wait queue failed", taskExecutionContext.getTaskName());
                return TaskInstanceDispatchResponse.failed(taskExecutionContext.getTaskInstanceId(),
                        "WorkerManagerThread is full");
            } else {
                log.info("Submit task: {} to wait queue success", taskExecutionContext.getTaskName());
                return TaskInstanceDispatchResponse.success(taskExecutionContext.getTaskInstanceId());
            }
        } finally {
            LogUtils.removeWorkflowAndTaskInstanceIdMDC();
        }
    }
}
