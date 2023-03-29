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

import org.apache.dolphinscheduler.remote.command.BaseMessage;
import org.apache.dolphinscheduler.remote.command.MessageType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * execute task response command
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TaskExecuteResultMessage extends BaseMessage {

    public TaskExecuteResultMessage(String messageSenderAddress, String messageReceiverAddress, long messageSendTime) {
        super(messageSenderAddress, messageReceiverAddress, messageSendTime);
    }

    /**
     * task instance id
     */
    private int taskInstanceId;

    /**
     * process instance id
     */
    private int processInstanceId;

    /**
     * status
     */
    private int status;

    /**
     * startTime
     */
    private long startTime;

    /**
     * host
     */
    private String host;

    /**
     * logPath
     */
    private String logPath;

    /**
     * executePath
     */
    private String executePath;

    /**
     * end time
     */
    private long endTime;

    /**
     * processId
     */
    private int processId;

    /**
     * appIds
     */
    private String appIds;

    /**
     * varPool string
     */
    private String varPool;

    @Override
    public MessageType getCommandType() {
        return MessageType.TASK_EXECUTE_RESULT_MESSAGE;
    }
}
