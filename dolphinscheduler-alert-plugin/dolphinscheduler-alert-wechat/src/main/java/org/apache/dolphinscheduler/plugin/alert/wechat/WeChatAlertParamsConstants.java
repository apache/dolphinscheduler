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

package org.apache.dolphinscheduler.plugin.alert.wechat;

/**
 * WeChatAlertParamsConstants
 */
public class WeChatAlertParamsConstants {

    private WeChatAlertParamsConstants() {
        throw new IllegalStateException(WeChatAlertParamsConstants.class.getName());
    }

    static final String ENTERPRISE_WE_CHAT_CORP_ID = "corp.id";

    static final String NAME_ENTERPRISE_WE_CHAT_CORP_ID = "corpId";


    static final String ENTERPRISE_WE_CHAT_SECRET = "secret";

    static final String NAME_ENTERPRISE_WE_CHAT_SECRET = "secret";

    static final String ENTERPRISE_WE_CHAT_TEAM_SEND_MSG = "team.send.msg";

    static final String NAME_ENTERPRISE_WE_CHAT_TEAM_SEND_MSG = "teamSendMsg";


    static final String ENTERPRISE_WE_CHAT_USER_SEND_MSG = "user.send.msg";

    static final String NAME_ENTERPRISE_WE_CHAT_USER_SEND_MSG = "userSendMsg";


    static final String ENTERPRISE_WE_CHAT_AGENT_ID = "agent.id";

    static final String NAME_ENTERPRISE_WE_CHAT_AGENT_ID = "agentId";


    static final String ENTERPRISE_WE_CHAT_USERS = "users";


    static final String NAME_ENTERPRISE_WE_CHAT_USERS = "users";


}
