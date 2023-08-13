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
import org.apache.dolphinscheduler.extract.master.transportor.TaskInstanceExecutionFinishEvent;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import org.springframework.stereotype.Component;

@Component
public class LogicTaskInstanceExecutionFinishEventSender
        implements
            LogicTaskInstanceExecutionEventSender<TaskInstanceExecutionFinishEvent> {

    @Override
    public void sendMessage(TaskInstanceExecutionFinishEvent message) {
        ITaskInstanceExecutionEventListener iTaskInstanceExecutionEventListener =
                SingletonJdkDynamicRpcClientProxyFactory.getInstance()
                        .getProxyClient(message.getHost(), ITaskInstanceExecutionEventListener.class);
        iTaskInstanceExecutionEventListener.onTaskInstanceExecutionFinish(new TaskInstanceExecutionFinishEvent());
    }

    @Override
    public TaskInstanceExecutionFinishEvent buildMessage(TaskExecutionContext taskExecutionContext) {
        TaskInstanceExecutionFinishEvent taskExecuteResultMessage = new TaskInstanceExecutionFinishEvent();
        taskExecuteResultMessage.setProcessInstanceId(taskExecutionContext.getProcessInstanceId());
        taskExecuteResultMessage.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        taskExecuteResultMessage.setStatus(taskExecutionContext.getCurrentExecutionStatus().getCode());
        taskExecuteResultMessage.setLogPath(taskExecutionContext.getLogPath());
        taskExecuteResultMessage.setExecutePath(taskExecutionContext.getExecutePath());
        taskExecuteResultMessage.setAppIds(taskExecutionContext.getAppIds());
        taskExecuteResultMessage.setProcessId(taskExecutionContext.getProcessId());
        taskExecuteResultMessage.setHost(taskExecutionContext.getHost());
        taskExecuteResultMessage.setStartTime(taskExecutionContext.getStartTime());
        taskExecuteResultMessage.setEndTime(taskExecutionContext.getEndTime());
        taskExecuteResultMessage.setVarPool(taskExecutionContext.getVarPool());
        taskExecuteResultMessage.setExecutePath(taskExecutionContext.getExecutePath());
        return taskExecuteResultMessage;
    }

}
