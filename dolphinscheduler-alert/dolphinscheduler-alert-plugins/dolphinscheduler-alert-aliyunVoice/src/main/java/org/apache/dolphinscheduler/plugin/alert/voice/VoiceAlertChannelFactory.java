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

package org.apache.dolphinscheduler.plugin.alert.voice;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.alert.api.AlertInputTips;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;

import java.util.Arrays;
import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(AlertChannelFactory.class)
public final class VoiceAlertChannelFactory implements AlertChannelFactory {

    @Override
    public String name() {
        return "AliyunVoice";
    }

    @Override
    public List<PluginParams> params() {

        InputParam calledNumber =
                InputParam.newBuilder(VoiceAlertConstants.NAME_CALLED_NUMBER, VoiceAlertConstants.CALLED_NUMBER)
                        .setPlaceholder(AlertInputTips.CALLED_NUMBER.getMsg())
                        .addValidate(Validate.newBuilder()
                                .setRequired(true)
                                .build())
                        .build();

        InputParam calledShowNumber = InputParam
                .newBuilder(VoiceAlertConstants.NAME_CALLED_SHOW_NUMBER, VoiceAlertConstants.CALLED_SHOW_NUMBER)
                .setPlaceholder(AlertInputTips.CALLED_SHOW_NUMBER.getMsg())
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        InputParam ttsCode = InputParam.newBuilder(VoiceAlertConstants.NAME_TTS_CODE, VoiceAlertConstants.TTS_CODE)
                .setPlaceholder(AlertInputTips.TTS_CODE.getMsg())
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        InputParam address = InputParam.newBuilder(VoiceAlertConstants.NAME_ADDRESS, VoiceAlertConstants.ADDRESS)
                .setPlaceholder(AlertInputTips.ALIYUN_VIICE_ADDRESS.getMsg())
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        InputParam accessKeyId =
                InputParam.newBuilder(VoiceAlertConstants.NAME_ACCESS_KEY_ID, VoiceAlertConstants.ACCESS_KEY_ID)
                        .setPlaceholder(AlertInputTips.ALIYUN_VIICE_ACCESSKEYID.getMsg())
                        .addValidate(Validate.newBuilder()
                                .setRequired(false)
                                .build())
                        .build();
        InputParam accessKeySecret =
                InputParam.newBuilder(VoiceAlertConstants.NAME_ACCESS_KEY_SECRET, VoiceAlertConstants.ACCESS_KEY_SECRET)
                        .setPlaceholder(AlertInputTips.ALIYUN_VIICE_ACCESSKEY_SECRET.getMsg())
                        .addValidate(Validate.newBuilder()
                                .setRequired(false)
                                .build())
                        .build();

        return Arrays.asList(calledNumber, calledShowNumber, ttsCode, address, accessKeyId, accessKeySecret);
    }

    @Override
    public AlertChannel create() {
        return new VoiceAlertChannel();
    }
}
