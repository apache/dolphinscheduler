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

package org.apache.dolphinscheduler.plugin.alert.feishu.personalwork;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.alert.api.AlertInputTips;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;
import org.apache.dolphinscheduler.spi.params.select.SelectParam;

import java.util.Arrays;
import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(AlertChannelFactory.class)
public final class FeiShuPersonalWorkAlertChannelFactory implements AlertChannelFactory {

    @Override
    public String name() {
        return "FeiShuPersonalWork";
    }

    @Override
    public List<PluginParams> params() {
        InputParam appIdParam =
                InputParam.newBuilder(FeiShuPersonalWorkParamConstants.NAME_PERSONAL_WORK_APP_ID,
                        FeiShuPersonalWorkParamConstants.PERSONAL_WORK_APP_ID)
                        .addValidate(Validate.newBuilder()
                                .setRequired(true)
                                .build())
                        .setPlaceholder(AlertInputTips.APP_ID.getMsg())
                        .build();
        InputParam appSecretParam =
                InputParam.newBuilder(FeiShuPersonalWorkParamConstants.NAME_PERSONAL_WORK_APP_SECRET,
                        FeiShuPersonalWorkParamConstants.PERSONAL_WORK_APP_SECRET)
                        .addValidate(Validate.newBuilder()
                                .setRequired(true)
                                .build())
                        .setPlaceholder(AlertInputTips.APP_SECRET.getMsg())
                        .build();
        SelectParam receiveIdTypeParam = SelectParam
                .newBuilder(FeiShuPersonalWorkParamConstants.NAME_RECEIVE_ID_TYPE,
                        FeiShuPersonalWorkParamConstants.RECEIVE_ID_TYPE)
                .addOptions(new ParamsOptions(FeiShuPersonalWorkParamConstants.OPEN_ID,
                        FeiShuPersonalWorkParamConstants.OPEN_ID, false))
                .addOptions(new ParamsOptions(FeiShuPersonalWorkParamConstants.USER_ID,
                        FeiShuPersonalWorkParamConstants.USER_ID, false))
                .addOptions(new ParamsOptions(FeiShuPersonalWorkParamConstants.UNION_ID,
                        FeiShuPersonalWorkParamConstants.UNION_ID, false))
                .addOptions(new ParamsOptions(FeiShuPersonalWorkParamConstants.EMAIL,
                        FeiShuPersonalWorkParamConstants.EMAIL, false))
                .addOptions(new ParamsOptions(FeiShuPersonalWorkParamConstants.CHAT_ID,
                        FeiShuPersonalWorkParamConstants.CHAT_ID, false))
                .setValue(FeiShuPersonalWorkParamConstants.OPEN_ID)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();
        InputParam receiveIdParam =
                InputParam.newBuilder(FeiShuPersonalWorkParamConstants.NAME_RECEIVE_ID,
                        FeiShuPersonalWorkParamConstants.RECEIVE_ID)
                        .addValidate(Validate.newBuilder()
                                .setRequired(true)
                                .build())
                        .setPlaceholder(AlertInputTips.RECEIVE_ID.getMsg())
                        .build();

        return Arrays.asList(appIdParam, appSecretParam, receiveIdTypeParam, receiveIdParam);

    }

    @Override
    public AlertChannel create() {
        return new FeiShuPersonalWorkAlertChannel();
    }
}
