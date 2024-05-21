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

package org.apache.dolphinscheduler.plugin.alert.http;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.apache.dolphinscheduler.alert.api.AlertResult;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HttpSenderTest {

    @Test
    public void sendTest() throws Exception {
        Map<String, String> paramsMap = new HashMap<>();
        String url = "https://www.dolphinscheduler-not-exists-web.com:12345";
        String contentField = "content";
        paramsMap.put(HttpAlertConstants.NAME_URL, url);
        paramsMap.put(HttpAlertConstants.NAME_REQUEST_TYPE, "GET");
        paramsMap.put(HttpAlertConstants.NAME_HEADER_PARAMS, "{\"Content-Type\":\"application/json\"}");
        paramsMap.put(HttpAlertConstants.NAME_BODY_PARAMS, "{\"number\":\"123456\"}");
        paramsMap.put(HttpAlertConstants.NAME_CONTENT_FIELD, contentField);
        paramsMap.put(HttpAlertConstants.NAME_TIMEOUT, String.valueOf(HttpAlertConstants.DEFAULT_TIMEOUT));

        HttpSender httpSender = spy(new HttpSender(paramsMap));
        doReturn("success").when(httpSender).getResponseString(any());
        AlertResult alertResult = httpSender.send("Fault tolerance warning");
        Assertions.assertTrue(alertResult.isSuccess());
        Assertions.assertTrue(httpSender.getRequestUrl().contains(url));
        Assertions.assertTrue(httpSender.getRequestUrl().contains(contentField));
    }
}
