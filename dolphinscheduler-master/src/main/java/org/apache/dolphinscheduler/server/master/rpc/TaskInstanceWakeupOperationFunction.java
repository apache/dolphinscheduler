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

package org.apache.dolphinscheduler.server.master.rpc;

import org.apache.dolphinscheduler.extract.master.transportor.TaskInstanceWakeupRequest;
import org.apache.dolphinscheduler.extract.master.transportor.TaskInstanceWakeupResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.runner.DefaultTaskExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskInstanceWakeupOperationFunction
        implements
            ITaskInstanceOperationFunction<TaskInstanceWakeupRequest, TaskInstanceWakeupResponse> {

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Override
    public TaskInstanceWakeupResponse operate(TaskInstanceWakeupRequest taskInstanceWakeupRequest) {
        try {
            log.info("Received TaskInstanceWakeupRequest request{}", taskInstanceWakeupRequest);

            int workflowInstanceId = taskInstanceWakeupRequest.getProcessInstanceId();
            int taskInstanceId = taskInstanceWakeupRequest.getTaskInstanceId();
            LogUtils.setWorkflowAndTaskInstanceIDMDC(workflowInstanceId, taskInstanceId);
            WorkflowExecuteRunnable workflowExecuteRunnable =
                    processInstanceExecCacheManager.getByProcessInstanceId(workflowInstanceId);
            if (workflowExecuteRunnable == null) {
                log.warn("cannot find WorkflowExecuteRunnable: {}, no need to Wakeup task", workflowInstanceId);
                return TaskInstanceWakeupResponse.failed("cannot find WorkflowExecuteRunnable: " + workflowInstanceId);
            }
            DefaultTaskExecuteRunnable defaultTaskExecuteRunnable =
                    workflowExecuteRunnable.getTaskExecuteRunnableById(taskInstanceId).orElse(null);
            if (defaultTaskExecuteRunnable == null) {
                log.warn("Cannot find DefaultTaskExecuteRunnable: {}, cannot Wakeup task", taskInstanceId);
                return TaskInstanceWakeupResponse.failed("Cannot find DefaultTaskExecuteRunnable: " + taskInstanceId);
            }
            defaultTaskExecuteRunnable.dispatch();
            log.info("Success Wakeup TaskInstance: {}", taskInstanceId);
            return TaskInstanceWakeupResponse.success();
        } finally {
            LogUtils.removeWorkflowAndTaskInstanceIdMDC();
        }
    }
}
