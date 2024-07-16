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

import org.apache.dolphinscheduler.extract.master.ITaskInstanceController;
import org.apache.dolphinscheduler.extract.master.transportor.TaskGroupSlotAcquireSuccessNotifyRequest;
import org.apache.dolphinscheduler.extract.master.transportor.TaskGroupSlotAcquireSuccessNotifyResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskDispatchLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskInstanceControllerImpl implements ITaskInstanceController {

    @Autowired
    private IWorkflowRepository workflowExecutionRunnableMemoryRepository;

    @Override
    public TaskGroupSlotAcquireSuccessNotifyResponse notifyTaskGroupSlotAcquireSuccess(
                                                                                       final TaskGroupSlotAcquireSuccessNotifyRequest taskGroupSlotAcquireSuccessNotifyRequest) {
        log.info("Received TaskGroupSlotAcquireSuccessRequest request{}", taskGroupSlotAcquireSuccessNotifyRequest);
        try {
            final int workflowInstanceId = taskGroupSlotAcquireSuccessNotifyRequest.getWorkflowInstanceId();
            final int taskInstanceId = taskGroupSlotAcquireSuccessNotifyRequest.getTaskInstanceId();
            LogUtils.setWorkflowAndTaskInstanceIDMDC(workflowInstanceId, taskInstanceId);
            final IWorkflowExecutionRunnable workflowExecutionRunnable =
                    workflowExecutionRunnableMemoryRepository.get(workflowInstanceId);
            if (workflowExecutionRunnable == null) {
                log.warn("cannot find WorkflowExecuteRunnable: {}, no need to Wakeup task", workflowInstanceId);
                return TaskGroupSlotAcquireSuccessNotifyResponse
                        .failed("cannot find WorkflowExecuteRunnable: " + workflowInstanceId);
            }
            final ITaskExecutionRunnable taskExecutionRunnable = workflowExecutionRunnable
                    .getWorkflowExecuteContext()
                    .getWorkflowExecutionGraph()
                    .getTaskExecutionRunnableById(taskInstanceId);
            if (taskExecutionRunnable == null) {
                log.warn("Cannot find TaskExecutionRunnable: {}, no need to Wakeup task", taskInstanceId);
                return TaskGroupSlotAcquireSuccessNotifyResponse
                        .failed("Cannot find TaskExecutionRunnable: " + taskInstanceId);
            }
            workflowExecutionRunnable.getWorkflowEventBus()
                    .publish(TaskDispatchLifecycleEvent.of(taskExecutionRunnable));
            log.info("Success Wakeup TaskInstance: {}", taskInstanceId);
            return TaskGroupSlotAcquireSuccessNotifyResponse.success();
        } finally {
            LogUtils.removeWorkflowAndTaskInstanceIdMDC();
        }
    }
}
