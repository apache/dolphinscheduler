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

package org.apache.dolphinscheduler.remote.command.task;

import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.remote.command.BaseMessage;
import org.apache.dolphinscheduler.remote.command.MessageType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Task running message, means the task is running in worker.
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TaskExecuteRunningMessage extends BaseMessage {

    /**
     * taskInstanceId
     */
    private int taskInstanceId;

    /**
     * process instance id
     */
    private int processInstanceId;

    /**
     * startTime
     */
    private long startTime;

    /**
     * host
     */
    private String host;

    /**
     * status
     */
    private TaskExecutionStatus status;

    /**
     * logPath
     */
    private String logPath;

    /**
     * executePath
     */
    private String executePath;

    /**
     * processId
     */
    private int processId;

    /**
     * appIds
     */
    private String appIds;

    public TaskExecuteRunningMessage(String messageSenderAddress, String messageReceiverAddress, long messageSendTime) {
        super(messageSenderAddress, messageReceiverAddress, messageSendTime);
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.TASK_EXECUTE_RUNNING_MESSAGE;
    }

}
