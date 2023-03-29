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

package org.apache.dolphinscheduler.remote.command;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This is the base class for rpc message.
 * <p>
 * Since we use async mode, the client send a message and will wait the target server
 * send ack for the message, the client will retry during a while if he doesn't receive an ack.
 * <p>
 * When there is a network error, the server cannot send ack to the client by the origin channel,
 * since the client has closed the channel, so the server need to know the command source.
 */
@Data
@NoArgsConstructor
public abstract class BaseMessage implements Serializable, RequestMessageBuilder {

    private static final long serialVersionUID = -1L;

    /**
     * If the message receiver want to send ack to the sender, need to use this address.
     */
    protected String messageSenderAddress;

    /**
     * The message receiver address.
     */
    protected String messageReceiverAddress;

    protected long messageSendTime;

    protected BaseMessage(String messageSenderAddress, String messageReceiverAddress, long messageSendTime) {
        this.messageSenderAddress = messageSenderAddress;
        this.messageReceiverAddress = messageReceiverAddress;
        this.messageSendTime = messageSendTime;
    }
}
