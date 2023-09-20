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

package org.apache.dolphinscheduler.plugin.alert.dingtalk.personalwork;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.alert.api.AlertInputTips;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;
import org.apache.dolphinscheduler.spi.params.radio.RadioParam;

import java.util.Arrays;
import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(AlertChannelFactory.class)
public final class DingTalkAlertPersonalWorkChannelFactory implements AlertChannelFactory {

    @Override
    public String name() {
        return "DingTalkPersonalWork";
    }

    @Override
    public List<PluginParams> params() {
        InputParam appKeyParam = InputParam
                .newBuilder(DingTalkPersonalWorkParamConstants.NAME_DING_TALK_PERSONAL_WORK_APP_KEY,
                        DingTalkPersonalWorkParamConstants.DING_TALK_PERSONAL_WORK_APP_KEY)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .setPlaceholder(AlertInputTips.APP_KEY.getMsg())
                .build();
        InputParam appSecretParam = InputParam
                .newBuilder(DingTalkPersonalWorkParamConstants.NAME_DING_TALK_PERSONAL_WORK_APP_SECRET,
                        DingTalkPersonalWorkParamConstants.DING_TALK_PERSONAL_WORK_APP_SECRET)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .setPlaceholder(AlertInputTips.APP_SECRET.getMsg())
                .build();
        InputParam robotCodeParam = InputParam
                .newBuilder(DingTalkPersonalWorkParamConstants.NAME_DING_TALK_PERSONAL_WORK_ROBOT_CODE,
                        DingTalkPersonalWorkParamConstants.DING_TALK_PERSONAL_WORK_ROBOT_CODE)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .setPlaceholder(AlertInputTips.ROBOT_CODE.getMsg())
                .build();
        InputParam userIdsParam = InputParam
                .newBuilder(DingTalkPersonalWorkParamConstants.NAME_DING_TALK_PERSONAL_WORK_USER_IDS,
                        DingTalkPersonalWorkParamConstants.DING_TALK_PERSONAL_WORK_USER_IDS)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .setPlaceholder(AlertInputTips.USER_IDS.getMsg())
                .build();
        RadioParam msgTypeParam = RadioParam
                .newBuilder(DingTalkPersonalWorkParamConstants.NAME_DING_TALK_PERSONAL_WORK_MSG_KEY,
                        DingTalkPersonalWorkParamConstants.DING_TALK_PERSONAL_WORK_MSG_KEY)
                .addParamsOptions(
                        new ParamsOptions(DingTalkPersonalWorkParamConstants.DING_TALK_PERSONAL_WORK_MSG_KEY_TEXT,
                                DingTalkPersonalWorkParamConstants.DING_TALK_PERSONAL_WORK_MSG_KEY_TEXT, false))
                .addParamsOptions(
                        new ParamsOptions(DingTalkPersonalWorkParamConstants.DING_TALK_PERSONAL_WORK_MSG_KEY_MARKDOWN,
                                DingTalkPersonalWorkParamConstants.DING_TALK_PERSONAL_WORK_MSG_KEY_MARKDOWN, false))
                .setValue(DingTalkPersonalWorkParamConstants.DING_TALK_PERSONAL_WORK_MSG_KEY_TEXT)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();
        return Arrays.asList(appKeyParam, appSecretParam, robotCodeParam, userIdsParam, msgTypeParam);
    }

    @Override
    public AlertChannel create() {
        return new DingTalkPersonalWorkAlertChannel();
    }
}
