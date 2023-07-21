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

package org.apache.dolphinscheduler.plugin.alert.dingtalk;

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

import java.util.Arrays;
import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(AlertChannelFactory.class)
public final class DingTalkAlertChannelFactory implements AlertChannelFactory {

    @Override
    public String name() {
        return "DingTalk";
    }

    @Override
    public List<PluginParams> params() {
        InputParam webHookParam = InputParam
                .newBuilder(DingTalkParamsConstants.NAME_DING_TALK_WEB_HOOK, DingTalkParamsConstants.DING_TALK_WEB_HOOK)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();
        InputParam keywordParam = InputParam
                .newBuilder(DingTalkParamsConstants.NAME_DING_TALK_KEYWORD, DingTalkParamsConstants.DING_TALK_KEYWORD)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        InputParam secretParam = InputParam
                .newBuilder(DingTalkParamsConstants.NAME_DING_TALK_SECRET, DingTalkParamsConstants.DING_TALK_SECRET)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        RadioParam msgTypeParam = RadioParam
                .newBuilder(DingTalkParamsConstants.NAME_DING_TALK_MSG_TYPE, DingTalkParamsConstants.DING_TALK_MSG_TYPE)
                .addParamsOptions(new ParamsOptions(DingTalkParamsConstants.DING_TALK_MSG_TYPE_TEXT,
                        DingTalkParamsConstants.DING_TALK_MSG_TYPE_TEXT, false))
                .addParamsOptions(new ParamsOptions(DingTalkParamsConstants.DING_TALK_MSG_TYPE_MARKDOWN,
                        DingTalkParamsConstants.DING_TALK_MSG_TYPE_MARKDOWN, false))
                .setValue(DingTalkParamsConstants.DING_TALK_MSG_TYPE_TEXT)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        InputParam atMobilesParam = InputParam
                .newBuilder(DingTalkParamsConstants.NAME_DING_TALK_AT_MOBILES,
                        DingTalkParamsConstants.DING_TALK_AT_MOBILES)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();
        InputParam atUserIdsParam = InputParam
                .newBuilder(DingTalkParamsConstants.NAME_DING_TALK_AT_USERIDS,
                        DingTalkParamsConstants.DING_TALK_AT_USERIDS)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();
        RadioParam isAtAll = RadioParam
                .newBuilder(DingTalkParamsConstants.NAME_DING_TALK_AT_ALL, DingTalkParamsConstants.DING_TALK_AT_ALL)
                .addParamsOptions(new ParamsOptions(STRING_YES, STRING_TRUE, false))
                .addParamsOptions(new ParamsOptions(STRING_NO, STRING_FALSE, false))
                .setValue(STRING_FALSE)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        RadioParam isEnableProxy = RadioParam
                .newBuilder(DingTalkParamsConstants.NAME_DING_TALK_PROXY_ENABLE,
                        DingTalkParamsConstants.DING_TALK_PROXY_ENABLE)
                .addParamsOptions(new ParamsOptions(STRING_YES, STRING_TRUE, false))
                .addParamsOptions(new ParamsOptions(STRING_NO, STRING_FALSE, false))
                .setValue(STRING_FALSE)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();
        InputParam proxyParam = InputParam
                .newBuilder(DingTalkParamsConstants.NAME_DING_TALK_PROXY, DingTalkParamsConstants.DING_TALK_PROXY)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        InputNumberParam portParam = InputNumberParam
                .newBuilder(DingTalkParamsConstants.NAME_DING_TALK_PORT, DingTalkParamsConstants.DING_TALK_PORT)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .setType(DataType.NUMBER.getDataType())
                        .build())
                .build();

        InputParam userParam = InputParam
                .newBuilder(DingTalkParamsConstants.NAME_DING_TALK_USER, DingTalkParamsConstants.DING_TALK_USER)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();
        InputParam passwordParam = InputParam
                .newBuilder(DingTalkParamsConstants.NAME_DING_TALK_PASSWORD, DingTalkParamsConstants.DING_TALK_PASSWORD)
                .setPlaceholder(AlertInputTips.PASSWORD.getMsg())
                .setType("password")
                .build();

        return Arrays.asList(webHookParam, keywordParam, secretParam, msgTypeParam, atMobilesParam, atUserIdsParam,
                isAtAll, isEnableProxy, proxyParam, portParam, userParam, passwordParam);
    }

    @Override
    public AlertChannel create() {
        return new DingTalkAlertChannel();
    }
}
