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

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRunningCommand;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerRpcClient;

import lombok.NonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskExecuteRunningMessageSender implements MessageSender<TaskExecuteRunningCommand> {

    @Autowired
    private WorkerRpcClient workerRpcClient;

    @Autowired
    private WorkerConfig workerConfig;

    @Override
    public void sendMessage(TaskExecuteRunningCommand message) throws RemotingException {
        workerRpcClient.send(Host.of(message.getMessageReceiverAddress()), message.convert2Command());
    }

    public TaskExecuteRunningCommand buildMessage(@NonNull TaskExecutionContext taskExecutionContext,
                                                  @NonNull String messageReceiverAddress) {
        TaskExecuteRunningCommand taskExecuteRunningMessage =
                new TaskExecuteRunningCommand(workerConfig.getWorkerAddress(),
                        messageReceiverAddress,
                        System.currentTimeMillis());
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

    @Override
    public CommandType getMessageType() {
        return CommandType.TASK_EXECUTE_RUNNING;
    }
}
