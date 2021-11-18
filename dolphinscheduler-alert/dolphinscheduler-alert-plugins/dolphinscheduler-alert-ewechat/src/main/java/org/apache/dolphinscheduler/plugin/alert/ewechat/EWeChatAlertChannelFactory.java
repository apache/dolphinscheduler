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

package org.apache.dolphinscheduler.plugin.alert.ewechat;

import static org.apache.dolphinscheduler.spi.utils.Constants.STRING_FALSE;
import static org.apache.dolphinscheduler.spi.utils.Constants.STRING_NO;
import static org.apache.dolphinscheduler.spi.utils.Constants.STRING_TRUE;
import static org.apache.dolphinscheduler.spi.utils.Constants.STRING_YES;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.spi.params.PasswordParam;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;
import org.apache.dolphinscheduler.spi.params.radio.RadioParam;

import java.util.Arrays;
import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(EWeChatAlertChannelFactory.class)
public final class EWeChatAlertChannelFactory implements AlertChannelFactory {
    @Override
    public AlertChannel create() {
        return new EWeChatAlertChannel();
    }

    @Override
    public String name() {
        return "EWeChat";
    }

    @Override
    public List<PluginParams> params() {
        InputParam webHookParam = InputParam.newBuilder(EWeChatParamsConstants.NAME_EWE_CHAT_WEB_HOOK, EWeChatParamsConstants.EWE_CHAT_WEB_HOOK)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();
        RadioParam isEnableProxy =
                RadioParam.newBuilder(EWeChatParamsConstants.NAME_EWE_CHAT_PROXY_ENABLE, EWeChatParamsConstants.EWE_CHAT_PROXY_ENABLE)
                        .addParamsOptions(new ParamsOptions(STRING_YES, STRING_TRUE, false))
                        .addParamsOptions(new ParamsOptions(STRING_NO, STRING_FALSE, false))
                        .setValue(STRING_TRUE)
                        .addValidate(Validate.newBuilder()
                                .setRequired(false)
                                .build())
                        .build();
        InputParam proxyParam =
                InputParam.newBuilder(EWeChatParamsConstants.NAME_EWE_CHAT_PROXY, EWeChatParamsConstants.EWE_CHAT_PROXY)
                        .addValidate(Validate.newBuilder()
                                .setRequired(false).build())
                        .build();

        InputParam portParam = InputParam.newBuilder(EWeChatParamsConstants.NAME_EWE_CHAT_PORT, EWeChatParamsConstants.EWE_CHAT_PORT)
                .addValidate(Validate.newBuilder()
                        .setRequired(false).build())
                .build();

        InputParam userParam =
                InputParam.newBuilder(EWeChatParamsConstants.NAME_EWE_CHAT_USER, EWeChatParamsConstants.EWE_CHAT_USER)
                        .addValidate(Validate.newBuilder()
                                .setRequired(false).build())
                        .build();

        PasswordParam proxyPassword = PasswordParam.newBuilder(EWeChatParamsConstants.NAME_EWE_CHAT_PASSWD, EWeChatParamsConstants.EWE_CHAT_PASSWD)
                .setPlaceholder("if enable use authentication, you need input password")
                .build();

        return Arrays.asList(webHookParam, isEnableProxy, proxyParam, portParam, userParam, proxyPassword);
    }
}
