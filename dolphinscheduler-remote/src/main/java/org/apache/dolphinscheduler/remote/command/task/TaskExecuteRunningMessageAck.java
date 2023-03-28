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
import org.apache.dolphinscheduler.remote.command.CommandType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * task execute running ack command
 * from master to worker
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TaskExecuteRunningMessageAck extends BaseMessage {

    private boolean success;
    private int taskInstanceId;

    public TaskExecuteRunningMessageAck(boolean success,
                                        int taskInstanceId,
                                        String messageSenderAddress,
                                        String messageReceiverAddress,
                                        long messageSendTime) {
        super(messageSenderAddress, messageReceiverAddress, messageSendTime);
        this.success = success;
        this.taskInstanceId = taskInstanceId;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.TASK_EXECUTE_RUNNING_MESSAGE_ACK;
    }

}
