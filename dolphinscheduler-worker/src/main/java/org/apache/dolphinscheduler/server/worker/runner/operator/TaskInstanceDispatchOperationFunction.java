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

import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceDispatchRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceDispatchResponse;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.worker.runner.GlobalTaskInstanceDispatchQueue;

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
    private GlobalTaskInstanceDispatchQueue globalTaskInstanceDispatchQueue;

    @Override
    public TaskInstanceDispatchResponse operate(TaskInstanceDispatchRequest taskInstanceDispatchRequest) {
        log.info("Receive TaskInstanceDispatchRequest: {}", taskInstanceDispatchRequest);
        TaskExecutionContext taskExecutionContext = taskInstanceDispatchRequest.getTaskExecutionContext();
        try {
            // set cache, it will be used when kill task
            TaskExecutionContextCacheManager.cacheTaskExecutionContext(taskExecutionContext);
            taskExecutionContext.setHost(workerConfig.getWorkerAddress());
            taskExecutionContext.setLogPath(LogUtils.getTaskInstanceLogFullPath(taskExecutionContext));

            LogUtils.setWorkflowAndTaskInstanceIDMDC(taskExecutionContext.getProcessInstanceId(),
                    taskExecutionContext.getTaskInstanceId());
            TaskMetrics.incrTaskTypeExecuteCount(taskExecutionContext.getTaskType());

            if (!globalTaskInstanceDispatchQueue.addDispatchTask(taskExecutionContext)) {
                log.error("Submit task: {} to wait queue error, current queue size: {} is full",
                        taskExecutionContext.getTaskName(), workerConfig.getExecThreads());
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
