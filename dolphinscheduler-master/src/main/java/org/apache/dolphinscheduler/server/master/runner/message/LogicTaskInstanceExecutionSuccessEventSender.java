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

package org.apache.dolphinscheduler.server.master.runner.message;

import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.ITaskExecutionEventListener;
import org.apache.dolphinscheduler.extract.master.transportor.TaskExecutionSuccessEvent;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import org.springframework.stereotype.Component;

@Component
public class LogicTaskInstanceExecutionSuccessEventSender
        implements
            LogicTaskInstanceExecutionEventSender<TaskExecutionSuccessEvent> {

    @Override
    public void sendMessage(final TaskExecutionSuccessEvent message) {
        Clients
                .withService(ITaskExecutionEventListener.class)
                .withHost(message.getWorkflowInstanceHost())
                .onTaskInstanceExecutionSuccess(message);
    }

    @Override
    public TaskExecutionSuccessEvent buildMessage(final TaskExecutionContext taskExecutionContext) {
        return TaskExecutionSuccessEvent.builder()
                .workflowInstanceId(taskExecutionContext.getWorkflowInstanceId())
                .taskInstanceId(taskExecutionContext.getTaskInstanceId())
                .appIds(taskExecutionContext.getAppIds())
                .processId(taskExecutionContext.getProcessId())
                .workflowInstanceHost(taskExecutionContext.getWorkflowInstanceHost())
                .taskInstanceHost(taskExecutionContext.getHost())
                .endTime(taskExecutionContext.getEndTime())
                .varPool(taskExecutionContext.getVarPool())
                .build();
    }

}
