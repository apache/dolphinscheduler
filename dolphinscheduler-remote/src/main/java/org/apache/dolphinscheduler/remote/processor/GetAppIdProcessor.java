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

import static org.apache.dolphinscheduler.common.constants.Constants.APPID_COLLECT;
import static org.apache.dolphinscheduler.common.constants.Constants.DEFAULT_COLLECT_WAY;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.log.GetAppIdRequest;
import org.apache.dolphinscheduler.remote.command.log.GetAppIdResponse;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

@Component
@Slf4j
public class GetAppIdProcessor extends BaseLogProcessor implements NettyRequestProcessor {

    @Override
    public void process(Channel channel, Message message) {
        GetAppIdRequest getAppIdRequest =
                JSONUtils.parseObject(message.getBody(), GetAppIdRequest.class);
        String appInfoPath = getAppIdRequest.getAppInfoPath();
        String logPath = getAppIdRequest.getLogPath();
        List<String> appIds = LogUtils.getAppIds(logPath, appInfoPath,
                PropertyUtils.getString(APPID_COLLECT, DEFAULT_COLLECT_WAY));
        channel.writeAndFlush(
                new GetAppIdResponse(appIds).convert2Command(message.getOpaque()));
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.GET_APP_ID_REQUEST;
    }
}
