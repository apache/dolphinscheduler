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

/**
 * DingTalkParamsConstants
 */
public class DingTalkParamsConstants {

    private DingTalkParamsConstants() {
        throw new IllegalStateException("Utility class");
    }

    static final String DING_TALK_WEB_HOOK = "dingtalk.webhook";

    static final String NAME_DING_TALK_WEB_HOOK = "dingTalkWebHook";

    static final String DING_TALK_KEYWORD = "dingtalk.keyword";

    static final String NAME_DING_TALK_KEYWORD = "dingTalkKeyword";

    public static final String DING_TALK_PROXY_ENABLE = "dingtalk.isEnableProxy";

    static final String NAME_DING_TALK_PROXY_ENABLE = "dingTalkIsEnableProxy";

    static final String DING_TALK_PROXY = "dingtalk.proxy";

    static final String NAME_DING_TALK_PROXY = "dingTalkProxy";

    static final String DING_TALK_PORT = "dingtalk.port";

    static final String NAME_DING_TALK_PORT = "dingTalkPort";

    static final String DING_TALK_USER = "dingtalk.user";

    static final String NAME_DING_TALK_USER = "dingTalkUser";

    static final String DING_TALK_PASSWORD = "dingtalk.password";

    static final String NAME_DING_TALK_PASSWORD = "dingTalkPassword";

}
