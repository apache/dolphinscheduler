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

public final class TelegramAlertConstants {

    static final String PARSE_MODE_TXT = "Txt";

    static final String PARSE_MODE_MARKDOWN = "Markdown";

    static final String PARSE_MODE_MARKDOWN_V2 = "MarkdownV2";

    static final String PARSE_MODE_HTML = "Html";

    /**
     * TELEGRAM_PUSH_URL
     *
     * <pre>
     *     https://api.telegram.org/bot{botToken}/sendMessage
     * </pre>
     */
    static final String TELEGRAM_PUSH_URL = "https://api.telegram.org/bot{botToken}/sendMessage";

    private TelegramAlertConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
