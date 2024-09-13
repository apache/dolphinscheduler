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

package org.apache.dolphinscheduler.plugin.alert.telegram;

import static org.apache.dolphinscheduler.common.constants.Constants.STRING_FALSE;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_NO;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_TRUE;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_YES;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.alert.api.AlertInputTips;
import org.apache.dolphinscheduler.spi.params.base.DataType;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;
import org.apache.dolphinscheduler.spi.params.input.number.InputNumberParam;
import org.apache.dolphinscheduler.spi.params.radio.RadioParam;
import org.apache.dolphinscheduler.spi.params.select.SelectParam;

import java.util.Arrays;
import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(AlertChannelFactory.class)
public final class TelegramAlertChannelFactory implements AlertChannelFactory {

    @Override
    public String name() {
        return "Telegram";
    }

    @Override
    public AlertChannel create() {
        return new TelegramAlertChannel();
    }

    @Override
    public List<PluginParams> params() {

        InputParam webHookParam = InputParam
                .newBuilder(TelegramParamsConstants.NAME_TELEGRAM_WEB_HOOK, TelegramParamsConstants.TELEGRAM_WEB_HOOK)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .setPlaceholder(AlertInputTips.WEBHOOK.getMsg())
                .build();

        InputParam botTokenParam = InputParam
                .newBuilder(TelegramParamsConstants.NAME_TELEGRAM_BOT_TOKEN, TelegramParamsConstants.TELEGRAM_BOT_TOKEN)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .setPlaceholder(AlertInputTips.BOT_TOKEN.getMsg())
                .build();

        InputParam chatIdParam = InputParam
                .newBuilder(TelegramParamsConstants.NAME_TELEGRAM_CHAT_ID, TelegramParamsConstants.TELEGRAM_CHAT_ID)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .setPlaceholder(AlertInputTips.CHANNEL_ID.getMsg())
                .build();

        SelectParam parseMode = SelectParam
                .newBuilder(TelegramParamsConstants.NAME_TELEGRAM_PARSE_MODE,
                        TelegramParamsConstants.TELEGRAM_PARSE_MODE)
                .addOptions(new ParamsOptions(TelegramAlertConstants.PARSE_MODE_TXT,
                        TelegramAlertConstants.PARSE_MODE_TXT, false))
                .addOptions(new ParamsOptions(TelegramAlertConstants.PARSE_MODE_MARKDOWN,
                        TelegramAlertConstants.PARSE_MODE_MARKDOWN, false))
                .addOptions(new ParamsOptions(TelegramAlertConstants.PARSE_MODE_MARKDOWN_V2,
                        TelegramAlertConstants.PARSE_MODE_MARKDOWN_V2, false))
                .addOptions(new ParamsOptions(TelegramAlertConstants.PARSE_MODE_HTML,
                        TelegramAlertConstants.PARSE_MODE_HTML, false))
                .setValue(TelegramAlertConstants.PARSE_MODE_TXT)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        RadioParam isEnableProxy = RadioParam
                .newBuilder(TelegramParamsConstants.NAME_TELEGRAM_PROXY_ENABLE,
                        TelegramParamsConstants.TELEGRAM_PROXY_ENABLE)
                .addParamsOptions(new ParamsOptions(STRING_YES, STRING_TRUE, false))
                .addParamsOptions(new ParamsOptions(STRING_NO, STRING_FALSE, false))
                .setValue(STRING_FALSE)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        InputParam proxyParam = InputParam
                .newBuilder(TelegramParamsConstants.NAME_TELEGRAM_PROXY, TelegramParamsConstants.TELEGRAM_PROXY)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        InputNumberParam portParam =
                InputNumberParam
                        .newBuilder(TelegramParamsConstants.NAME_TELEGRAM_PORT, TelegramParamsConstants.TELEGRAM_PORT)
                        .addValidate(Validate.newBuilder()
                                .setRequired(false)
                                .setType(DataType.NUMBER.getDataType())
                                .build())
                        .build();

        InputParam userParam =
                InputParam.newBuilder(TelegramParamsConstants.NAME_TELEGRAM_USER, TelegramParamsConstants.TELEGRAM_USER)
                        .addValidate(Validate.newBuilder()
                                .setRequired(false)
                                .build())
                        .build();

        InputParam passwordParam = InputParam
                .newBuilder(TelegramParamsConstants.NAME_TELEGRAM_PASSWORD, TelegramParamsConstants.TELEGRAM_PASSWORD)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .setPlaceholder("if enable use authentication, you need input password")
                .setType("password")
                .build();

        return Arrays.asList(webHookParam, botTokenParam, chatIdParam, parseMode, isEnableProxy, proxyParam, portParam,
                userParam, passwordParam);
    }

}
