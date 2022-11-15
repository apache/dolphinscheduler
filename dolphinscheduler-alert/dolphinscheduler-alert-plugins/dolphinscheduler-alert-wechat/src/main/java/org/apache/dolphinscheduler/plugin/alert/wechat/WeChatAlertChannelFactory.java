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

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.alert.api.AlertConstants;
import org.apache.dolphinscheduler.alert.api.AlertInputTips;
import org.apache.dolphinscheduler.alert.api.ShowType;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;
import org.apache.dolphinscheduler.spi.params.radio.RadioParam;

import java.util.Arrays;
import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(AlertChannelFactory.class)
public final class WeChatAlertChannelFactory implements AlertChannelFactory {

    @Override
    public String name() {
        return "WeChat";
    }

    @Override
    public List<PluginParams> params() {
        InputParam corpIdParam = InputParam
                .newBuilder(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_CORP_ID,
                        WeChatAlertParamsConstants.ENTERPRISE_WE_CHAT_CORP_ID)
                .setPlaceholder(AlertInputTips.CORP_ID.getMsg())
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam secretParam = InputParam
                .newBuilder(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_SECRET,
                        WeChatAlertParamsConstants.ENTERPRISE_WE_CHAT_SECRET)
                .setPlaceholder(AlertInputTips.SECRET.getMsg())
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam usersParam = InputParam
                .newBuilder(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_USERS,
                        WeChatAlertParamsConstants.ENTERPRISE_WE_CHAT_USERS)
                .setPlaceholder(AlertInputTips.WECHAT_MENTION_USERS.getMsg())
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        InputParam agentIdParam = InputParam
                .newBuilder(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_AGENT_ID,
                        WeChatAlertParamsConstants.ENTERPRISE_WE_CHAT_AGENT_ID)
                .setPlaceholder(AlertInputTips.WECHAT_AGENT_ID.getMsg())
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        RadioParam sendType = RadioParam
                .newBuilder(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_SEND_TYPE,
                        WeChatAlertParamsConstants.ENTERPRISE_WE_CHAT_SEND_TYPE)
                .addParamsOptions(new ParamsOptions(WeChatType.APP.getDescp(), WeChatType.APP.getDescp(), false))
                .addParamsOptions(
                        new ParamsOptions(WeChatType.APPCHAT.getDescp(), WeChatType.APPCHAT.getDescp(), false))
                .setValue(WeChatType.APP.getDescp())
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        RadioParam showType = RadioParam.newBuilder(AlertConstants.NAME_SHOW_TYPE, AlertConstants.SHOW_TYPE)
                .addParamsOptions(new ParamsOptions(ShowType.MARKDOWN.getDescp(), ShowType.MARKDOWN.getDescp(), false))
                .addParamsOptions(new ParamsOptions(ShowType.TEXT.getDescp(), ShowType.TEXT.getDescp(), false))
                .setValue(ShowType.MARKDOWN.getDescp())
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        return Arrays.asList(corpIdParam, secretParam, usersParam, agentIdParam, sendType, showType);
    }

    @Override
    public AlertChannel create() {
        return new WeChatAlertChannel();
    }
}
