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

public final class WeChatAlertParamsConstants {

    static final String ENTERPRISE_WE_CHAT_CORP_ID = "$t('corpId')";
    static final String NAME_ENTERPRISE_WE_CHAT_CORP_ID = "corpId";
    static final String ENTERPRISE_WE_CHAT_SECRET = "$t('secret')";
    static final String NAME_ENTERPRISE_WE_CHAT_SECRET = "secret";
    static final String ENTERPRISE_WE_CHAT_TEAM_SEND_MSG = "$t('teamSendMsg')";
    static final String NAME_ENTERPRISE_WE_CHAT_TEAM_SEND_MSG = "teamSendMsg";
    static final String ENTERPRISE_WE_CHAT_AGENT_ID = "$t('agentId/chatId')";
    static final String NAME_ENTERPRISE_WE_CHAT_AGENT_ID = "agentId/chatId";
    static final String ENTERPRISE_WE_CHAT_USERS = "$t('users')";
    static final String NAME_ENTERPRISE_WE_CHAT_USERS = "users";

    static final String NAME_ENTERPRISE_WE_CHAT_SEND_TYPE = "sendType";

    static final String ENTERPRISE_WE_CHAT_SEND_TYPE = "send.type";

    private WeChatAlertParamsConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
