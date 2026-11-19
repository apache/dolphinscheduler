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

package org.apache.dolphinscheduler.plugin.alert.feishu;

import org.apache.dolphinscheduler.alert.api.AlertData;
import org.apache.dolphinscheduler.alert.api.AlertInfo;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpServer;

public class FeiShuSenderTest {

    private static Map<String, String> feiShuConfig = new HashMap<>();

    @BeforeEach
    public void initFeiShuConfig() {
        feiShuConfig.put(FeiShuParamsConstants.WEB_HOOK, "https://open.feishu.cn/open-apis/bot/v2/hook/xxxxx");
    }

    @Test
    public void testSend() {
        AlertData alertData = new AlertData();
        alertData.setTitle("feishu test title");
        alertData.setContent("feishu test content");
        FeiShuSender feiShuSender = new FeiShuSender(feiShuConfig);
        AlertResult alertResult = feiShuSender.sendFeiShuMsg(alertData);
        Assertions.assertFalse(alertResult.isSuccess());
    }

    @Test
    public void testSendQueryResultWithNullValue() throws Exception {
        assertQueryResultSent("[{\"null_column\":null,\"number_column\":0,\"flag_column\":false,\"empty_column\":\"\"},"
                + "{\"text_column\":\"中文内容\",\"null_column\":null}]",
                "null_column:null", "number_column:0", "flag_column:false", "empty_column:\n", "text_column:中文内容");
    }

    @Test
    public void testSendQueryResultWithoutNullValue() throws Exception {
        assertQueryResultSent("[{\"message\":\"ok\",\"number_column\":42}]", "message:ok", "number_column:42");
    }

    private void assertQueryResultSent(String content, String... expectedValues) throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/hook", exchange -> {
            try (
                    InputStream input = exchange.getRequestBody();
                    ByteArrayOutputStream body = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) != -1) {
                    body.write(buffer, 0, length);
                }
                requestBody.set(new String(body.toByteArray(), StandardCharsets.UTF_8));
            }
            byte[] response = "{\"code\":0,\"msg\":\"success\",\"data\":{}}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
        server.start();
        try {
            Map<String, String> config = new HashMap<>();
            config.put(FeiShuParamsConstants.NAME_WEB_HOOK,
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/hook");
            config.put(FeiShuParamsConstants.NAME_FEI_SHU_PROXY_ENABLE, "false");
            AlertData data = new AlertData();
            data.setTitle("SQL query result");
            data.setContent(content);
            AlertInfo info = new AlertInfo();
            info.setAlertData(data);
            info.setAlertParams(config);

            AlertResult result = new FeiShuAlertChannel().process(info);

            Assertions.assertTrue(result.isSuccess(), result.getMessage());
            JsonNode payload = JSONUtils.parseObject(requestBody.get(), JsonNode.class);
            Assertions.assertEquals("text", payload.get("msg_type").asText());
            String text = payload.get("content").get("text").asText();
            Assertions.assertTrue(text.contains("SQL query result"));
            for (String expected : expectedValues) {
                Assertions.assertTrue(text.contains(expected), text);
            }
        } finally {
            server.stop(0);
        }
    }
}
