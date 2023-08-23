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

package org.apache.dolphinscheduler.server.worker.message;

import org.apache.dolphinscheduler.extract.base.client.SingletonJdkDynamicRpcClientProxyFactory;
import org.apache.dolphinscheduler.extract.master.ITaskInstanceExecutionEventListener;
import org.apache.dolphinscheduler.extract.master.transportor.ITaskInstanceExecutionEvent;
import org.apache.dolphinscheduler.extract.master.transportor.TaskInstanceExecutionInfoEvent;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import lombok.NonNull;

import org.springframework.stereotype.Component;

@Component
public class TaskInstanceExecutionInfoUpdateEventSender
        implements
            TaskInstanceExecutionEventSender<TaskInstanceExecutionInfoEvent> {

    @Override
    public void sendEvent(TaskInstanceExecutionInfoEvent taskInstanceExecutionInfoEvent) {
        ITaskInstanceExecutionEventListener iTaskInstanceExecutionEventListener =
                SingletonJdkDynamicRpcClientProxyFactory.getInstance()
                        .getProxyClient(taskInstanceExecutionInfoEvent.getHost(),
                                ITaskInstanceExecutionEventListener.class);
        iTaskInstanceExecutionEventListener.onTaskInstanceExecutionInfoUpdate(taskInstanceExecutionInfoEvent);
    }

    @Override
    public TaskInstanceExecutionInfoEvent buildEvent(@NonNull TaskExecutionContext taskExecutionContext) {
        TaskInstanceExecutionInfoEvent taskUpdatePidRequest = new TaskInstanceExecutionInfoEvent();
        taskUpdatePidRequest.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        taskUpdatePidRequest.setProcessInstanceId(taskExecutionContext.getProcessInstanceId());
        taskUpdatePidRequest.setHost(taskExecutionContext.getHost());
        taskUpdatePidRequest.setStartTime(taskExecutionContext.getStartTime());
        return taskUpdatePidRequest;
    }

    @Override
    public ITaskInstanceExecutionEvent.TaskInstanceExecutionEventType getMessageType() {
        return ITaskInstanceExecutionEvent.TaskInstanceExecutionEventType.RUNNING_INFO;
    }
}
