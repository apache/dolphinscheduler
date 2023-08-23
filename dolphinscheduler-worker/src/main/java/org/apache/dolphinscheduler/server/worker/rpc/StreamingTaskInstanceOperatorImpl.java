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

package org.apache.dolphinscheduler.server.worker.rpc;

import org.apache.dolphinscheduler.extract.worker.IStreamingTaskInstanceOperator;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceTriggerSavepointRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceTriggerSavepointResponse;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.stream.StreamTask;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecuteRunnable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StreamingTaskInstanceOperatorImpl implements IStreamingTaskInstanceOperator {

    @Autowired
    private WorkerManagerThread workerManager;

    @Override
    public TaskInstanceTriggerSavepointResponse triggerSavepoint(TaskInstanceTriggerSavepointRequest taskInstanceTriggerSavepointRequest) {
        log.info("Receive triggerSavepoint request: {}", taskInstanceTriggerSavepointRequest);

        try {
            int taskInstanceId = taskInstanceTriggerSavepointRequest.getTaskInstanceId();
            LogUtils.setTaskInstanceIdMDC(taskInstanceId);
            TaskExecutionContext taskExecutionContext =
                    TaskExecutionContextCacheManager.getByTaskInstanceId(taskInstanceId);
            if (taskExecutionContext == null) {
                log.error("Cannot find TaskExecutionContext for taskInstance: {}", taskInstanceId);
                return TaskInstanceTriggerSavepointResponse.fail("Cannot find TaskExecutionContext");
            }
            WorkerTaskExecuteRunnable workerTaskExecuteRunnable = workerManager.getTaskExecuteThread(taskInstanceId);
            if (workerTaskExecuteRunnable == null) {
                log.error("Cannot find WorkerTaskExecuteRunnable for taskInstance: {}", taskInstanceId);
                return TaskInstanceTriggerSavepointResponse.fail("Cannot find WorkerTaskExecuteRunnable");
            }
            AbstractTask task = workerTaskExecuteRunnable.getTask();
            if (task == null) {
                log.error("Cannot find StreamTask for taskInstance:{}", taskInstanceId);
                return TaskInstanceTriggerSavepointResponse.fail("Cannot find StreamTask");
            }
            if (!(task instanceof StreamTask)) {
                log.warn("The taskInstance: {} is not StreamTask", taskInstanceId);
                return TaskInstanceTriggerSavepointResponse.fail("The taskInstance is not StreamTask");
            }
            try {
                ((StreamTask) task).savePoint();
            } catch (Exception e) {
                log.error("StreamTask: {} call savePoint error", taskInstanceId, e);
                return TaskInstanceTriggerSavepointResponse.fail("StreamTask call savePoint error: " + e.getMessage());
            }
            return TaskInstanceTriggerSavepointResponse.success();
        } finally {
            LogUtils.removeTaskInstanceIdMDC();
        }
    }
}
