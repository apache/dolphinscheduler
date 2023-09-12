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

import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.extract.master.transportor.TaskInstanceWakeupRequest;
import org.apache.dolphinscheduler.extract.master.transportor.TaskInstanceWakeupResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.event.TaskStateEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.StateEventResponseService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskInstanceWakeupOperationFunction
        implements
            ITaskInstanceOperationFunction<TaskInstanceWakeupRequest, TaskInstanceWakeupResponse> {

    @Autowired
    private StateEventResponseService stateEventResponseService;

    @Override
    public TaskInstanceWakeupResponse operate(TaskInstanceWakeupRequest taskInstanceWakeupRequest) {
        TaskStateEvent stateEvent = TaskStateEvent.builder()
                .processInstanceId(taskInstanceWakeupRequest.getProcessInstanceId())
                .taskInstanceId(taskInstanceWakeupRequest.getTaskInstanceId())
                .key(taskInstanceWakeupRequest.getKey())
                .type(StateEventType.WAKE_UP_TASK_GROUP)
                .build();
        try {
            LogUtils.setWorkflowAndTaskInstanceIDMDC(stateEvent.getProcessInstanceId(), stateEvent.getTaskInstanceId());
            log.info("Received wakeupTaskInstance request, event: {}", stateEvent);
            stateEventResponseService.addEvent2WorkflowExecute(stateEvent);
            return TaskInstanceWakeupResponse.success();
        } finally {
            LogUtils.removeWorkflowAndTaskInstanceIdMDC();
        }
    }
}
