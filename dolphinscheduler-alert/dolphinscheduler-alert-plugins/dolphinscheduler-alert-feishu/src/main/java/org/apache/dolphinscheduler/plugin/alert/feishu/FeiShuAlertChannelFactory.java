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
public final class FeiShuAlertChannelFactory implements AlertChannelFactory {

    @Override
    public String name() {
        return "Feishu";
    }

    @Override
    public List<PluginParams> params() {
        InputParam webHookOrAppIdParam =
                InputParam
                        .newBuilder(FeiShuParamsConstants.NAME_WEB_HOOK_OR_APP_ID,
                                FeiShuParamsConstants.WEB_HOOK_OR_APP_ID)
                        .addValidate(Validate.newBuilder()
                                .setRequired(true)
                                .build())
                        .setPlaceholder(AlertInputTips.WEBHOOK_OR_APP_ID.getMsg())
                        .build();

        RadioParam sendType = RadioParam
                .newBuilder(FeiShuParamsConstants.NAME_FEI_SHU_SEND_TYPE,
                        FeiShuParamsConstants.FEI_SHU_SEND_TYPE)
                .addParamsOptions(new ParamsOptions(FeiShuType.CUSTOM_ROBOT.getDescp(),
                        FeiShuType.CUSTOM_ROBOT.getDescp(), false))
                .addParamsOptions(
                        new ParamsOptions(FeiShuType.APPLIANCE_ROBOT.getDescp(), FeiShuType.APPLIANCE_ROBOT.getDescp(),
                                false))
                .setValue(FeiShuType.CUSTOM_ROBOT.getDescp())
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();
        InputParam appSecretParam =
                InputParam.newBuilder(FeiShuParamsConstants.NAME_PERSONAL_WORK_APP_SECRET,
                        FeiShuParamsConstants.PERSONAL_WORK_APP_SECRET)
                        .addValidate(Validate.newBuilder()
                                .setRequired(false)
                                .build())
                        .setPlaceholder(AlertInputTips.APP_SECRET.getMsg())
                        .build();
        SelectParam receiveIdTypeParam = SelectParam
                .newBuilder(FeiShuParamsConstants.NAME_RECEIVE_ID_TYPE,
                        FeiShuParamsConstants.RECEIVE_ID_TYPE)
                .addOptions(new ParamsOptions(FeiShuParamsConstants.OPEN_ID,
                        FeiShuParamsConstants.OPEN_ID, false))
                .addOptions(new ParamsOptions(FeiShuParamsConstants.USER_ID,
                        FeiShuParamsConstants.USER_ID, false))
                .addOptions(new ParamsOptions(FeiShuParamsConstants.UNION_ID,
                        FeiShuParamsConstants.UNION_ID, false))
                .addOptions(new ParamsOptions(FeiShuParamsConstants.EMAIL,
                        FeiShuParamsConstants.EMAIL, false))
                .addOptions(new ParamsOptions(FeiShuParamsConstants.CHAT_ID,
                        FeiShuParamsConstants.CHAT_ID, false))
                .setValue(FeiShuParamsConstants.OPEN_ID)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();
        InputParam receiveIdParam =
                InputParam.newBuilder(FeiShuParamsConstants.NAME_RECEIVE_ID,
                        FeiShuParamsConstants.RECEIVE_ID)
                        .addValidate(Validate.newBuilder()
                                .setRequired(false)
                                .build())
                        .setPlaceholder(AlertInputTips.RECEIVE_ID.getMsg())
                        .build();

        RadioParam isEnableProxy =
                RadioParam
                        .newBuilder(FeiShuParamsConstants.NAME_FEI_SHU_PROXY_ENABLE,
                                FeiShuParamsConstants.FEI_SHU_PROXY_ENABLE)
                        .addParamsOptions(new ParamsOptions(STRING_YES, STRING_TRUE, false))
                        .addParamsOptions(new ParamsOptions(STRING_NO, STRING_FALSE, false))
                        .setValue(STRING_TRUE)
                        .addValidate(Validate.newBuilder()
                                .setRequired(false)
                                .build())
                        .build();
        InputParam proxyParam =
                InputParam.newBuilder(FeiShuParamsConstants.NAME_FEI_SHU_PROXY, FeiShuParamsConstants.FEI_SHU_PROXY)
                        .addValidate(Validate.newBuilder()
                                .setRequired(false).build())
                        .build();

        InputNumberParam portParam =
                InputNumberParam.newBuilder(FeiShuParamsConstants.NAME_FEI_SHU_PORT, FeiShuParamsConstants.FEI_SHU_PORT)
                        .addValidate(Validate.newBuilder()
                                .setRequired(false)
                                .setType(DataType.NUMBER.getDataType())
                                .build())
                        .build();

        InputParam userParam =
                InputParam.newBuilder(FeiShuParamsConstants.NAME_FEI_SHU_USER, FeiShuParamsConstants.FEI_SHU_USER)
                        .addValidate(Validate.newBuilder()
                                .setRequired(false).build())
                        .build();
        InputParam passwordParam = InputParam
                .newBuilder(FeiShuParamsConstants.NAME_FEI_SHU_PASSWORD, FeiShuParamsConstants.FEI_SHU_PASSWORD)
                .setPlaceholder(AlertInputTips.PASSWORD.getMsg())
                .setType("password")
                .build();

        return Arrays.asList(sendType, webHookOrAppIdParam, appSecretParam, receiveIdTypeParam, receiveIdParam,
                isEnableProxy, proxyParam, portParam, userParam, passwordParam);

    }

    @Override
    public AlertChannel create() {
        return new FeiShuAlertChannel();
    }
}
