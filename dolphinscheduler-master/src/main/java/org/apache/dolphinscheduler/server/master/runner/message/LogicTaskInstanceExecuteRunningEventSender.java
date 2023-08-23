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

import org.apache.dolphinscheduler.extract.base.client.SingletonJdkDynamicRpcClientProxyFactory;
import org.apache.dolphinscheduler.extract.master.ITaskInstanceExecutionEventListener;
import org.apache.dolphinscheduler.extract.master.transportor.TaskInstanceExecutionRunningEvent;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import lombok.NonNull;

import org.springframework.stereotype.Component;

@Component
public class LogicTaskInstanceExecuteRunningEventSender
        implements
            LogicTaskInstanceExecutionEventSender<TaskInstanceExecutionRunningEvent> {

    @Override
    public void sendMessage(TaskInstanceExecutionRunningEvent taskInstanceExecutionRunningEvent) {
        ITaskInstanceExecutionEventListener iTaskInstanceExecutionEventListener =
                SingletonJdkDynamicRpcClientProxyFactory.getInstance()
                        .getProxyClient(taskInstanceExecutionRunningEvent.getHost(),
                                ITaskInstanceExecutionEventListener.class);
        iTaskInstanceExecutionEventListener.onTaskInstanceExecutionRunning(taskInstanceExecutionRunningEvent);
    }

    @Override
    public TaskInstanceExecutionRunningEvent buildMessage(@NonNull TaskExecutionContext taskExecutionContext) {
        TaskInstanceExecutionRunningEvent taskExecuteRunningMessage = new TaskInstanceExecutionRunningEvent();
        taskExecuteRunningMessage.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        taskExecuteRunningMessage.setProcessInstanceId(taskExecutionContext.getProcessInstanceId());
        taskExecuteRunningMessage.setStatus(taskExecutionContext.getCurrentExecutionStatus());
        taskExecuteRunningMessage.setLogPath(taskExecutionContext.getLogPath());
        taskExecuteRunningMessage.setHost(taskExecutionContext.getHost());
        taskExecuteRunningMessage.setStartTime(taskExecutionContext.getStartTime());
        taskExecuteRunningMessage.setExecutePath(taskExecutionContext.getExecutePath());
        taskExecuteRunningMessage.setAppIds(taskExecutionContext.getAppIds());
        return taskExecuteRunningMessage;
    }

}
