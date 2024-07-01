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

public final class WebexTeamsParamsConstants {

    public static final String NAME_WEBEX_TEAMS_BOT_ACCESS_TOKEN = "BotAccessToken";
    public static final String WEBEX_TEAMS_BOT_ACCESS_TOKEN = "botAccessToken";
    public static final String NAME_WEBEX_TEAMS_DESTINATION = "Destination";
    public static final String WEBEX_TEAMS_DESTINATION = "destination";
    public static final String NAME_WEBEX_TEAMS_TO_PERSON_ID = "ToPersonId";
    public static final String WEBEX_TEAMS_TO_PERSON_ID = "toPersonId";
    public static final String NAME_WEBEX_TEAMS_TO_PERSON_EMAIL = "ToPersonEmail";
    public static final String WEBEX_TEAMS_TO_PERSON_EMAIL = "toPersonEmail";
    public static final String NAME_WEBEX_TEAMS_ROOM_ID = "RoomId";
    public static final String WEBEX_TEAMS_ROOM_ID = "roomId";
    public static final String NAME_WEBEX_TEAMS_AT_SOMEONE_IN_ROOM = "AtSomeoneInRoom";
    public static final String WEBEX_TEAMS_AT_SOMEONE_IN_ROOM = "atSomeoneInRoom";
    public static final String WEBEX_TEAMS_API = "https://webexapis.com/v1/messages";

    private WebexTeamsParamsConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
