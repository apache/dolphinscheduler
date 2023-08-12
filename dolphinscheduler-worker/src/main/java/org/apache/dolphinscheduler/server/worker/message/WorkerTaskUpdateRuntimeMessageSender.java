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
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.task.TaskUpdateRuntimeMessage;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerRpcClient;

import lombok.NonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkerTaskUpdateRuntimeMessageSender implements MessageSender<TaskUpdateRuntimeMessage> {

    @Autowired
    private WorkerRpcClient workerRpcClient;

    @Autowired
    private WorkerConfig workerConfig;

    @Override
    public void sendMessage(TaskUpdateRuntimeMessage message) throws RemotingException {
        workerRpcClient.send(Host.of(message.getMessageReceiverAddress()), message.convert2Command());
    }

    @Override
    public TaskUpdateRuntimeMessage buildMessage(@NonNull TaskExecutionContext taskExecutionContext) {
        TaskUpdateRuntimeMessage taskUpdatePidRequest =
                new TaskUpdateRuntimeMessage(workerConfig.getWorkerAddress(),
                        taskExecutionContext.getWorkflowInstanceHost(),
                        System.currentTimeMillis());
        taskUpdatePidRequest.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        taskUpdatePidRequest.setProcessInstanceId(taskExecutionContext.getProcessInstanceId());
        taskUpdatePidRequest.setHost(taskExecutionContext.getHost());
        taskUpdatePidRequest.setStartTime(taskExecutionContext.getStartTime());
        return taskUpdatePidRequest;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.TASK_UPDATE_RUNTIME_MESSAGE;
    }
}
