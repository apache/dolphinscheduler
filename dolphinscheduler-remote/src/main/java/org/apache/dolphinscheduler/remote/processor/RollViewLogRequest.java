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

package org.apache.dolphinscheduler.remote.processor;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.log.RollViewLogResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

@Component
@Slf4j
public class RollViewLogRequest extends BaseLogProcessor implements NettyRequestProcessor {

    @Override
    public void process(Channel channel, Message message) {
        org.apache.dolphinscheduler.remote.command.log.RollViewLogRequest rollViewLogRequest = JSONUtils.parseObject(
                message.getBody(), org.apache.dolphinscheduler.remote.command.log.RollViewLogRequest.class);

        String rollViewLogPath = rollViewLogRequest.getPath();

        List<String> lines = readPartFileContent(rollViewLogPath,
                rollViewLogRequest.getSkipLineNum(), rollViewLogRequest.getLimit());
        StringBuilder builder = new StringBuilder();
        final int MaxResponseLogSize = 65535;
        int totalLogByteSize = 0;
        for (String line : lines) {
            // If a single line of log is exceed max response size, cut off the line
            final int lineByteSize = line.getBytes(StandardCharsets.UTF_8).length;
            if (lineByteSize >= MaxResponseLogSize) {
                builder.append(line, 0, MaxResponseLogSize)
                        .append(" [this line's size ").append(lineByteSize).append(" bytes is exceed ")
                        .append(MaxResponseLogSize).append(" bytes, so only ")
                        .append(MaxResponseLogSize).append(" characters are reserved for performance reasons.]")
                        .append("\r\n");
            } else {
                builder.append(line).append("\r\n");
            }
            totalLogByteSize += lineByteSize;
            if (totalLogByteSize >= MaxResponseLogSize) {
                break;
            }
        }
        RollViewLogResponse rollViewLogRequestResponse =
                new RollViewLogResponse(builder.toString());
        channel.writeAndFlush(rollViewLogRequestResponse.convert2Command(message.getOpaque()));
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.ROLL_VIEW_LOG_REQUEST;
    }
}
