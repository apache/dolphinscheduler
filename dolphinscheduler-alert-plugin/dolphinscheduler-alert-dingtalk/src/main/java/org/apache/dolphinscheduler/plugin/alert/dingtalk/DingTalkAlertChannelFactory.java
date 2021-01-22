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

import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.alert.AlertChannelFactory;
import org.apache.dolphinscheduler.spi.params.InputParam;
import org.apache.dolphinscheduler.spi.params.PasswordParam;
import org.apache.dolphinscheduler.spi.params.RadioParam;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;

import java.util.Arrays;
import java.util.List;

/**
 * DingTalkAlertChannelFactory
 */
public class DingTalkAlertChannelFactory implements AlertChannelFactory {
    @Override
    public String getName() {
        return "DingTalk";
    }

    @Override
    public List<PluginParams> getParams() {
        InputParam webHookParam = InputParam.newBuilder(DingTalkParamsConstants.NAME_DING_TALK_WEB_HOOK, DingTalkParamsConstants.DING_TALK_WEB_HOOK)
            .addValidate(Validate.newBuilder()
                .setRequired(true)
                .build())
            .build();
        InputParam keywordParam = InputParam.newBuilder(DingTalkParamsConstants.NAME_DING_TALK_KEYWORD, DingTalkParamsConstants.DING_TALK_KEYWORD)
            .addValidate(Validate.newBuilder()
                .setRequired(true)
                .build())
            .build();
        RadioParam isEnableProxy =
            RadioParam.newBuilder(DingTalkParamsConstants.NAME_DING_TALK_PROXY_ENABLE, DingTalkParamsConstants.NAME_DING_TALK_PROXY_ENABLE)
                    .addParamsOptions(new ParamsOptions("YES", true, false))
                    .addParamsOptions(new ParamsOptions("NO", false, false))
                .setValue(true)
                .addValidate(Validate.newBuilder()
                    .setRequired(false)
                    .build())
                .build();
        InputParam proxyParam =
            InputParam.newBuilder(DingTalkParamsConstants.NAME_DING_TALK_PROXY, DingTalkParamsConstants.DING_TALK_PROXY)
                .addValidate(Validate.newBuilder()
                    .setRequired(false).build())
                .build();

        InputParam portParam = InputParam.newBuilder(DingTalkParamsConstants.NAME_DING_TALK_PORT, DingTalkParamsConstants.DING_TALK_PORT)
            .addValidate(Validate.newBuilder()
                .setRequired(false).build())
            .build();

        InputParam userParam =
            InputParam.newBuilder(DingTalkParamsConstants.NAME_DING_TALK_USER, DingTalkParamsConstants.DING_TALK_USER)
                .addValidate(Validate.newBuilder()
                    .setRequired(false).build())
                .build();
        PasswordParam passwordParam = PasswordParam.newBuilder(DingTalkParamsConstants.NAME_DING_TALK_PASSWORD, DingTalkParamsConstants.DING_TALK_PASSWORD)
            .setPlaceholder("if enable use authentication, you need input password")
            .build();

        return Arrays.asList(webHookParam, keywordParam, isEnableProxy, proxyParam, portParam, userParam, passwordParam);
    }

    @Override
    public AlertChannel create() {
        return new DingTalkAlertChannel();
    }
}
