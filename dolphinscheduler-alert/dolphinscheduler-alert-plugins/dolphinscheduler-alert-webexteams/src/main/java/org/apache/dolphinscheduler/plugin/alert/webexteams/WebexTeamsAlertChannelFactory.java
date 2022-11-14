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

package org.apache.dolphinscheduler.plugin.alert.webexteams;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.alert.api.AlertInputTips;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;
import org.apache.dolphinscheduler.spi.params.radio.RadioParam;

import java.util.ArrayList;
import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(AlertChannelFactory.class)
public final class WebexTeamsAlertChannelFactory implements AlertChannelFactory {

    @Override
    public String name() {
        return "WebexTeams";
    }

    @Override
    public List<PluginParams> params() {
        List<PluginParams> paramsList = new ArrayList<>();
        InputParam botAccessToken = InputParam
                .newBuilder(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_BOT_ACCESS_TOKEN,
                        WebexTeamsParamsConstants.WEBEX_TEAMS_BOT_ACCESS_TOKEN)
                .setPlaceholder(AlertInputTips.BOT_TOKEN.getMsg())
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam roomId = InputParam
                .newBuilder(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_ROOM_ID,
                        WebexTeamsParamsConstants.WEBEX_TEAMS_ROOM_ID)
                .setPlaceholder(AlertInputTips.ROOM_ID.getMsg())
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        InputParam toPersonId = InputParam
                .newBuilder(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_TO_PERSON_ID,
                        WebexTeamsParamsConstants.WEBEX_TEAMS_TO_PERSON_ID)
                .setPlaceholder(AlertInputTips.RECIPIENT_USER_ID.getMsg())
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        InputParam toPersonEmail = InputParam
                .newBuilder(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_TO_PERSON_EMAIL,
                        WebexTeamsParamsConstants.WEBEX_TEAMS_TO_PERSON_EMAIL)
                .setPlaceholder(AlertInputTips.RECIPIENT_EMAIL.getMsg())
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        InputParam atSomeoneInRoom = InputParam
                .newBuilder(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_AT_SOMEONE_IN_ROOM,
                        WebexTeamsParamsConstants.WEBEX_TEAMS_AT_SOMEONE_IN_ROOM)
                .setPlaceholder(AlertInputTips.WEBEX_MENTION_USERS.getMsg())
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        RadioParam destination = RadioParam
                .newBuilder(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_DESTINATION,
                        WebexTeamsParamsConstants.WEBEX_TEAMS_DESTINATION)
                .addParamsOptions(new ParamsOptions(WebexTeamsDestination.ROOM_ID.getDescp(),
                        WebexTeamsDestination.ROOM_ID.getDescp(), false))
                .addParamsOptions(new ParamsOptions(WebexTeamsDestination.PERSON_EMAIL.getDescp(),
                        WebexTeamsDestination.PERSON_EMAIL.getDescp(), false))
                .addParamsOptions(new ParamsOptions(WebexTeamsDestination.PERSON_ID.getDescp(),
                        WebexTeamsDestination.PERSON_ID.getDescp(), false))
                .setValue(WebexTeamsDestination.ROOM_ID.getDescp())
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        paramsList.add(botAccessToken);
        paramsList.add(roomId);
        paramsList.add(toPersonId);
        paramsList.add(toPersonEmail);
        paramsList.add(atSomeoneInRoom);
        paramsList.add(destination);

        return paramsList;
    }

    @Override
    public AlertChannel create() {
        return new WebexTeamsAlertChannel();
    }
}
