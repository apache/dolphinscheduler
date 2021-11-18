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

package org.apache.dolphinscheduler.plugin.alert.ewechat;

public final class EWeChatParamsConstants {

    private EWeChatParamsConstants() {
        throw new IllegalStateException(EWeChatParamsConstants.class.getName());
    }

    static final String EWE_CHAT_PROXY_ENABLE = "$t('isEnableProxy')";

    static final String NAME_EWE_CHAT_PROXY_ENABLE = "IsEnableProxy";

    static final String EWE_CHAT_WEB_HOOK = "$t('webhook')";

    static final String NAME_EWE_CHAT_WEB_HOOK = "WebHook";

    static final String EWE_CHAT_PROXY = "$t('proxy')";

    static final String NAME_EWE_CHAT_PROXY = "Proxy";

    static final String EWE_CHAT_PORT = "$t('port')";

    static final String NAME_EWE_CHAT_PORT = "Port";

    static final String EWE_CHAT_USER = "$t('user')";

    static final String NAME_EWE_CHAT_USER = "User";

    static final String EWE_CHAT_PASSWD = "$t('password')";

    static final String NAME_EWE_CHAT_PASSWD = "Password";

}
