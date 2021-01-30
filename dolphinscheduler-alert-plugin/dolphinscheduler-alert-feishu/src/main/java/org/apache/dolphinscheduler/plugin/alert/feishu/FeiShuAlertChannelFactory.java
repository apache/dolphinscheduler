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

package org.apache.dolphinscheduler.plugin.alert.feishu;

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

public class FeiShuAlertChannelFactory implements AlertChannelFactory {
    @Override
    public String getName() {
        return "Feishu";
    }

    @Override
    public List<PluginParams> getParams() {
        InputParam webHookParam = InputParam.newBuilder(FeiShuParamsConstants.NAME_WEB_HOOK, FeiShuParamsConstants.WEB_HOOK)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();
        RadioParam isEnableProxy =
                RadioParam.newBuilder(FeiShuParamsConstants.NAME_FEI_SHU_PROXY_ENABLE, FeiShuParamsConstants.NAME_FEI_SHU_PROXY_ENABLE)
                        .addParamsOptions(new ParamsOptions("YES", true, false))
                        .addParamsOptions(new ParamsOptions("NO", false, false))
                        .setValue(true)
                        .addValidate(Validate.newBuilder()
                                .setRequired(false)
                                .build())
                        .build();
        InputParam proxyParam =
                InputParam.newBuilder(FeiShuParamsConstants.NAME_FEI_SHU_PROXY, FeiShuParamsConstants.FEI_SHU_PROXY)
                        .addValidate(Validate.newBuilder()
                                .setRequired(false).build())
                        .build();

        InputParam portParam = InputParam.newBuilder(FeiShuParamsConstants.NAME_FEI_SHU_PORT, FeiShuParamsConstants.FEI_SHU_PORT)
                .addValidate(Validate.newBuilder()
                        .setRequired(false).build())
                .build();

        InputParam userParam =
                InputParam.newBuilder(FeiShuParamsConstants.NAME_FEI_SHU_USER, FeiShuParamsConstants.FEI_SHU_USER)
                        .addValidate(Validate.newBuilder()
                                .setRequired(false).build())
                        .build();
        PasswordParam passwordParam = PasswordParam.newBuilder(FeiShuParamsConstants.NAME_FEI_SHU_PASSWORD, FeiShuParamsConstants.FEI_SHU_PASSWORD)
                .setPlaceholder("if enable use authentication, you need input password")
                .build();

        return Arrays.asList(webHookParam, isEnableProxy, proxyParam, portParam, userParam, passwordParam);

    }

    @Override
    public AlertChannel create() {
        return new FeiShuAlertChannel();
    }
}
