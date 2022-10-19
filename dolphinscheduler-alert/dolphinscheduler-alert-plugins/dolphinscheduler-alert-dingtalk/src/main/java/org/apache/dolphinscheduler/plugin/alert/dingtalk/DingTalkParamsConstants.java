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

package org.apache.dolphinscheduler.plugin.alert.dingtalk;

public final class DingTalkParamsConstants {

    static final String DING_TALK_PROXY_ENABLE = "$t('isEnableProxy')";
    static final String NAME_DING_TALK_PROXY_ENABLE = "IsEnableProxy";

    static final String DING_TALK_WEB_HOOK = "$t('webhook')";
    static final String NAME_DING_TALK_WEB_HOOK = "WebHook";

    static final String DING_TALK_KEYWORD = "$t('keyword')";
    static final String NAME_DING_TALK_KEYWORD = "Keyword";

    static final String DING_TALK_SECRET = "$t('secret')";
    static final String NAME_DING_TALK_SECRET = "Secret";

    static final String DING_TALK_MSG_TYPE = "$t('msgType')";
    static final String NAME_DING_TALK_MSG_TYPE = "MsgType";

    static final String DING_TALK_MSG_TYPE_TEXT = "text";
    static final String DING_TALK_MSG_TYPE_MARKDOWN = "markdown";

    static final String DING_TALK_AT_MOBILES = "$t('atMobiles')";
    static final String NAME_DING_TALK_AT_MOBILES = "AtMobiles";

    static final String DING_TALK_AT_USERIDS = "$t('atUserIds')";
    static final String NAME_DING_TALK_AT_USERIDS = "AtUserIds";

    static final String DING_TALK_AT_ALL = "$t('isAtAll')";
    static final String NAME_DING_TALK_AT_ALL = "IsAtAll";

    static final String DING_TALK_PROXY = "$t('proxy')";
    static final String NAME_DING_TALK_PROXY = "Proxy";

    static final String DING_TALK_PORT = "$t('port')";
    static final String NAME_DING_TALK_PORT = "Port";

    static final String DING_TALK_USER = "$t('user')";
    static final String NAME_DING_TALK_USER = "User";

    static final String DING_TALK_PASSWORD = "$t('password')";
    static final String NAME_DING_TALK_PASSWORD = "Password";

    private DingTalkParamsConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
