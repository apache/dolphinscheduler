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
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.log.ViewLogRequest;
import org.apache.dolphinscheduler.remote.command.log.ViewLogResponseResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

@Component
@Slf4j
public class ViewWholeLogProcessor extends BaseLogProcessor implements NettyRequestProcessor {

    @Override
    public void process(Channel channel, Command command) {
        ViewLogRequest viewLogRequest = JSONUtils.parseObject(
                command.getBody(), ViewLogRequest.class);
        String viewLogPath = viewLogRequest.getPath();
        String msg = readWholeFileContent(viewLogPath);
        ViewLogResponseResponse viewLogResponse = new ViewLogResponseResponse(msg);
        channel.writeAndFlush(viewLogResponse.convert2Command(command.getOpaque()));
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.VIEW_WHOLE_LOG_REQUEST;
    }
}
