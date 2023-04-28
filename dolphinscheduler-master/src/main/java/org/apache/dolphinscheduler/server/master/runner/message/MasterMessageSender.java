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

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.remote.command.BaseMessage;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;

// todo: unify with WorkerMessageSender
public interface MasterMessageSender<T extends BaseMessage> {

    /**
     * Send the message
     *
     * @throws RemotingException Cannot connect to the target host.
     */
    void sendMessage(T message) throws RemotingException;

    default void sendMessage(TaskExecutionContext taskExecutionContext) throws RemotingException {
        T message = buildMessage(taskExecutionContext);
        sendMessage(message);
    }

    /**
     * Build the message from task context and message received address.
     */
    T buildMessage(TaskExecutionContext taskExecutionContext);

    /**
     * The message type can be sent by this sender.
     */
    MessageType getMessageType();

}
