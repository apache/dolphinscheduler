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

package org.apache.dolphinscheduler.plugin.alert.wechatrobot;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.alert.api.AlertConstants;
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
        return "WeChatRobot";
    }

    @Override
    public List<PluginParams> params() {
        InputParam robotUrl = InputParam
                .newBuilder(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_ROBOT_URL,
                        WeChatAlertParamsConstants.ENTERPRISE_WE_CHAT_ROBOT_URL)
                .setPlaceholder("please input robot url ")
                .setValue(WeChatAlertConstants.ROBOT_SEND_URL)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam robotKey = InputParam
                .newBuilder(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_ROBOT_KEY,
                        WeChatAlertParamsConstants.ENTERPRISE_WE_CHAT_ROBOT_KEY)
                .setPlaceholder("please input robot key ")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        RadioParam showType = RadioParam.newBuilder(AlertConstants.NAME_SHOW_TYPE, AlertConstants.SHOW_TYPE)
                .addParamsOptions(new ParamsOptions(ShowType.TABLE.getDescp(), ShowType.TABLE.getDescp(), false))
                .addParamsOptions(new ParamsOptions(ShowType.TEXT.getDescp(), ShowType.TEXT.getDescp(), false))
                .setValue(ShowType.TABLE.getDescp())
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        return Arrays.asList(robotUrl, robotKey, showType);
    }

    @Override
    public AlertChannel create() {
        return new WeChatRobotAlertChannel();
    }
}
