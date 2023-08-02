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

package org.apache.dolphinscheduler.listener.rpc;

import org.apache.dolphinscheduler.listener.service.ListenerPluginService;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.listener.ListenerResponse;
import org.apache.dolphinscheduler.remote.command.listener.RemoveListenerPluginRequest;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.JsonSerializer;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

@Component
@Slf4j
public final class RemoveListenerPluginProcessor implements NettyRequestProcessor {

    private final ListenerPluginService listenerPluginService;

    public RemoveListenerPluginProcessor(ListenerPluginService listenerPluginService) {
        this.listenerPluginService = listenerPluginService;
    }

    @Override
    public void process(Channel channel, Message message) {
        RemoveListenerPluginRequest request =
                JsonSerializer.deserialize(message.getBody(), RemoveListenerPluginRequest.class);

        log.info("Received command : {}", request);

        ListenerResponse listenerResponse = listenerPluginService.removeListenerPlugin(
                request.getPluginId());
        channel.writeAndFlush(listenerResponse.convert2Command(message.getOpaque()));
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.REMOVE_LISTENER_PLUGIN;
    }
}
