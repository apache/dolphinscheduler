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

package org.apache.dolphinscheduler.plugin.alert.slack;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.alert.api.AlertInputTips;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;

import java.util.LinkedList;
import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(AlertChannelFactory.class)
public final class SlackAlertChannelFactory implements AlertChannelFactory {

    @Override
    public String name() {
        return "Slack";
    }

    @Override
    public List<PluginParams> params() {
        List<PluginParams> paramsList = new LinkedList<>();

        InputParam webHookParam = InputParam
                .newBuilder(SlackParamsConstants.SLACK_WEB_HOOK_URL_NAME, SlackParamsConstants.SLACK_WEB_HOOK_URL)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .setPlaceholder(AlertInputTips.WEBHOOK.getMsg())
                .build();

        InputParam botName = InputParam.newBuilder(SlackParamsConstants.SLACK_BOT_NAME, SlackParamsConstants.SLACK_BOT)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .setPlaceholder(AlertInputTips.BOT_NAME.getMsg())
                .build();

        paramsList.add(webHookParam);
        paramsList.add(botName);
        return paramsList;
    }

    @Override
    public AlertChannel create() {
        return new SlackAlertChannel();
    }
}
