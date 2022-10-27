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

package org.apache.dolphinscheduler.plugin.alert.telegram;

public final class TelegramParamsConstants {

    static final String TELEGRAM_WEB_HOOK = "$t('webHook')";
    static final String NAME_TELEGRAM_WEB_HOOK = "webHook";

    static final String TELEGRAM_BOT_TOKEN = "botToken";
    static final String NAME_TELEGRAM_BOT_TOKEN = "botToken";

    static final String TELEGRAM_CHAT_ID = "chatId";
    static final String NAME_TELEGRAM_CHAT_ID = "chatId";

    static final String TELEGRAM_PARSE_MODE = "parseMode";
    static final String NAME_TELEGRAM_PARSE_MODE = "parseMode";

    static final String TELEGRAM_PROXY_ENABLE = "$t('isEnableProxy')";
    static final String NAME_TELEGRAM_PROXY_ENABLE = "IsEnableProxy";

    static final String TELEGRAM_PROXY = "$t('proxy')";
    static final String NAME_TELEGRAM_PROXY = "Proxy";

    static final String TELEGRAM_PORT = "$t('port')";
    static final String NAME_TELEGRAM_PORT = "Port";

    static final String TELEGRAM_USER = "$t('user')";
    static final String NAME_TELEGRAM_USER = "User";

    static final String TELEGRAM_PASSWORD = "$t('password')";
    static final String NAME_TELEGRAM_PASSWORD = "Password";

    private TelegramParamsConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}
