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
import org.apache.dolphinscheduler.remote.command.log.RollViewLogRequest;
import org.apache.dolphinscheduler.remote.command.log.RollViewLogResponse;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

@Component
@Slf4j
public class RollViewLogRequestProcessor extends BaseLogProcessor implements NettyRequestProcessor {

    @Override
    public void process(Channel channel, Message message) {
        RollViewLogRequest rollViewLogRequest = JSONUtils.parseObject(message.getBody(), RollViewLogRequest.class);
        RollViewLogResponse rollViewLogRequestResponse = readPartFileContent(rollViewLogRequest);
        channel.writeAndFlush(rollViewLogRequestResponse.convert2Command(message.getOpaque()));
    }

    protected RollViewLogResponse readPartFileContent(RollViewLogRequest rollViewLogRequest) {

        String rollViewLogPath = rollViewLogRequest.getPath();
        File file = new File(rollViewLogPath);
        if (!file.exists() || !file.isFile()) {
            log.error("Log file path: {} doesn't exists", rollViewLogPath);
            return RollViewLogResponse.error(RollViewLogResponse.Status.LOG_FILE_NOT_FOUND);
        }

        int skipLine = rollViewLogRequest.getSkipLineNum();
        int limit = rollViewLogRequest.getLimit();
        try (
                Stream<String> stream = Files.lines(Paths.get(rollViewLogPath));
                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(rollViewLogPath))) {

            List<String> lines = stream.skip(skipLine).limit(limit).collect(Collectors.toList());
            lineNumberReader.skip(Long.MAX_VALUE);

            return RollViewLogResponse.builder()
                    .currentLineNumber(skipLine + (long) lines.size())
                    .currentTotalLineNumber(lineNumberReader.getLineNumber())
                    .log(String.join("\r\n", lines))
                    .build();
        } catch (IOException e) {
            log.error("Rolling view log error, meet an unknown exception, request: {}", rollViewLogRequest, e);
            return RollViewLogResponse.error(RollViewLogResponse.Status.UNKNOWN_ERROR);
        }
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.ROLL_VIEW_LOG_REQUEST;
    }
}
