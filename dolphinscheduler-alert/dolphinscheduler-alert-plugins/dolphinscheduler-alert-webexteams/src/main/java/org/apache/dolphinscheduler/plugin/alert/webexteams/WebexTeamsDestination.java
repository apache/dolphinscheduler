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

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;

import com.google.common.base.Functions;

public enum WebexTeamsDestination {

    /**
     * 0 ROOM_ID;
     * 1 PERSON_EMAIL;
     * 2 PERSON_ID;
     */
    ROOM_ID(0, "roomId"),
    PERSON_EMAIL(1, "personEmail"),
    PERSON_ID(2, "personId");

    private final int code;
    private final String descp;

    WebexTeamsDestination(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }

    private static final Map<String, WebexTeamsDestination> WEBEX_TEAMS_DESTINATION_MAP =
            Arrays.stream(WebexTeamsDestination.values())
                    .collect(toMap(WebexTeamsDestination::getDescp, Functions.identity()));

    public static WebexTeamsDestination of(String descp) {
        if (WEBEX_TEAMS_DESTINATION_MAP.containsKey(descp)) {
            return WEBEX_TEAMS_DESTINATION_MAP.get(descp);
        }

        return null;
    }
}
