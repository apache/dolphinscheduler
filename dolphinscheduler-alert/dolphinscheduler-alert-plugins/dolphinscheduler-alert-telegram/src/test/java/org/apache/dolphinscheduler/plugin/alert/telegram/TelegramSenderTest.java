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

import org.apache.dolphinscheduler.alert.api.AlertData;
import org.apache.dolphinscheduler.alert.api.AlertResult;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TelegramSenderTest {

    private static Map<String, String> telegramConfig = new HashMap<>();

    @BeforeEach
    public void initConfig() {
        telegramConfig.put(TelegramParamsConstants.NAME_TELEGRAM_WEB_HOOK,
                "https://api.telegram.org/bot{botToken}/sendMessage");
        telegramConfig.put(
                TelegramParamsConstants.NAME_TELEGRAM_BOT_TOKEN, "BOT_TOKEN");
        telegramConfig.put(
                TelegramParamsConstants.NAME_TELEGRAM_CHAT_ID, "CHAT_ID");
        telegramConfig.put(
                TelegramParamsConstants.NAME_TELEGRAM_PARSE_MODE, TelegramAlertConstants.PARSE_MODE_TXT);
    }

    @Test
    public void testSendMessageFailByParamToken() {
        AlertData alertData = new AlertData();
        alertData.setTitle("[telegram alert] test title");
        alertData.setContent("telegram test content");
        telegramConfig.put(
                TelegramParamsConstants.NAME_TELEGRAM_BOT_TOKEN, "XXXXXXX");
        TelegramSender telegramSender = new TelegramSender(telegramConfig);
        AlertResult result = telegramSender.sendMessage(alertData);
        Assertions.assertFalse(result.isSuccess());

    }

    @Test
    public void testSendMessageFailByChatId() {
        AlertData alertData = new AlertData();
        alertData.setTitle("[telegram alert] test title");
        alertData.setContent("telegram test content");
        telegramConfig.put(
                TelegramParamsConstants.NAME_TELEGRAM_CHAT_ID, "-XXXXXXX");
        TelegramSender telegramSender = new TelegramSender(telegramConfig);
        AlertResult result = telegramSender.sendMessage(alertData);
        Assertions.assertFalse(result.isSuccess());
    }

    @Test
    public void testSendMessage() {
        AlertData alertData = new AlertData();
        alertData.setTitle("[telegram alert] test title");
        alertData.setContent("telegram test content");
        TelegramSender telegramSender = new TelegramSender(telegramConfig);
        AlertResult result = telegramSender.sendMessage(alertData);
        Assertions.assertFalse(result.isSuccess());

    }

    @Test
    public void testSendMessageByMarkdown() {
        AlertData alertData = new AlertData();
        alertData.setTitle("[telegram alert]test markdown");
        alertData.setContent(
                "```python \npre-formatted fixed-width code block written in the Python programming language```");
        telegramConfig.put(
                TelegramParamsConstants.NAME_TELEGRAM_PARSE_MODE, TelegramAlertConstants.PARSE_MODE_MARKDOWN);
        TelegramSender telegramSender = new TelegramSender(telegramConfig);
        AlertResult result = telegramSender.sendMessage(alertData);
        Assertions.assertFalse(result.isSuccess());

    }

    @Test
    public void testSendMessageByHtml() {
        AlertData alertData = new AlertData();
        alertData.setTitle("[telegram alert]test html");
        alertData.setContent("<b>bold</b>");
        telegramConfig.put(
                TelegramParamsConstants.NAME_TELEGRAM_PARSE_MODE, TelegramAlertConstants.PARSE_MODE_HTML);
        TelegramSender telegramSender = new TelegramSender(telegramConfig);
        AlertResult result = telegramSender.sendMessage(alertData);
        Assertions.assertFalse(result.isSuccess());

    }

}
