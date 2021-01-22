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

package org.apache.dolphinscheduler.plugin.alert.wechat;

import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.alert.AlertChannelFactory;
import org.apache.dolphinscheduler.spi.alert.AlertConstants;
import org.apache.dolphinscheduler.spi.alert.ShowType;
import org.apache.dolphinscheduler.spi.params.InputParam;
import org.apache.dolphinscheduler.spi.params.RadioParam;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;

import java.util.Arrays;
import java.util.List;

/**
 * WeChatAlertChannelFactory
 */
public class WeChatAlertChannelFactory implements AlertChannelFactory {

    @Override
    public String getName() {
        return "WeChat";
    }

    @Override
    public List<PluginParams> getParams() {
        InputParam corpIdParam = InputParam.newBuilder(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_CORP_ID, WeChatAlertParamsConstants.ENTERPRISE_WE_CHAT_CORP_ID)
                .setPlaceholder("please input corp id ")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam secretParam = InputParam.newBuilder(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_SECRET, WeChatAlertParamsConstants.ENTERPRISE_WE_CHAT_SECRET)
                .setPlaceholder("please input secret ")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam usersParam = InputParam.newBuilder(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_USERS, WeChatAlertParamsConstants.ENTERPRISE_WE_CHAT_USERS)
                .setPlaceholder("please input users ")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam userSendMsgParam = InputParam.newBuilder(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_USER_SEND_MSG, WeChatAlertParamsConstants.ENTERPRISE_WE_CHAT_USER_SEND_MSG)
                .setPlaceholder("please input corp id ")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam agentIdParam = InputParam.newBuilder(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_AGENT_ID, WeChatAlertParamsConstants.ENTERPRISE_WE_CHAT_AGENT_ID)
                .setPlaceholder("please input agent id ")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        RadioParam showType = RadioParam.newBuilder(AlertConstants.SHOW_TYPE, AlertConstants.SHOW_TYPE)
                .addParamsOptions(new ParamsOptions(ShowType.TABLE.getDescp(), ShowType.TABLE.getDescp(), false))
                .addParamsOptions(new ParamsOptions(ShowType.TEXT.getDescp(), ShowType.TEXT.getDescp(), false))
                .setValue(ShowType.TABLE.getDescp())
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        return Arrays.asList(corpIdParam, secretParam, usersParam, userSendMsgParam, agentIdParam, showType);
    }

    @Override
    public AlertChannel create() {
        return new WeChatAlertChannel();
    }
}
