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
import org.apache.dolphinscheduler.remote.command.log.GetLogBytesRequest;
import org.apache.dolphinscheduler.remote.command.log.GetLogBytesResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

@Component
@Slf4j
public class GetLogBytesProcessor extends BaseLogProcessor implements NettyRequestProcessor {

    @Override
    public void process(Channel channel, Message message) {
        GetLogBytesRequest getLogRequest = JSONUtils.parseObject(message.getBody(), GetLogBytesRequest.class);
        GetLogBytesResponse getLogResponse = getFileContentBytes(getLogRequest);
        channel.writeAndFlush(getLogResponse.convert2Command(message.getOpaque()));
    }

    private GetLogBytesResponse getFileContentBytes(GetLogBytesRequest logBytesRequestCommand) {
        if (logBytesRequestCommand == null) {
            return GetLogBytesResponse.error(GetLogBytesResponse.Status.COMMAND_IS_NULL);
        }
        String path = logBytesRequestCommand.getPath();
        try (
                InputStream in = Files.newInputStream(Paths.get(path));
                ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            return GetLogBytesResponse.success(bos.toByteArray());
        } catch (IOException e) {
            log.error("Get file bytes error, meet an unknown exception", e);
            return GetLogBytesResponse.error(GetLogBytesResponse.Status.UNKNOWN_ERROR);
        }
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.GET_LOG_BYTES_REQUEST;
    }
}
