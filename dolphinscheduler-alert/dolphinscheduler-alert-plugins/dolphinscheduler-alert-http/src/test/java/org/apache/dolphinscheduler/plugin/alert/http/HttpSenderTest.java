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

import org.apache.dolphinscheduler.alert.api.AlertResult;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class HttpSenderTest {

    @Test
    public void sendTest() {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put(HttpAlertConstants.NAME_URL, "http://www.baidu.com");
        paramsMap.put(HttpAlertConstants.NAME_REQUEST_TYPE, "POST");
        paramsMap.put(HttpAlertConstants.NAME_HEADER_PARAMS, "{\"Content-Type\":\"application/json\"}");
        paramsMap.put(HttpAlertConstants.NAME_BODY_PARAMS, "{\"number\":\"13457654323\"}");
        paramsMap.put(HttpAlertConstants.NAME_CONTENT_FIELD, "content");
        HttpSender httpSender = new HttpSender(paramsMap);
        AlertResult alertResult = httpSender.send("Fault tolerance warning");
        Assert.assertEquals("true", alertResult.getStatus());
    }

    @Test
    public void sendActualWeChatRobotTest() throws Exception {
        Map<String, String> paramsMap = new HashMap<>();
        String url = "http://wechatapi.gi.compal.com:3002/api/Robot/Send/R000047";
        String contentField = "text.content";

        paramsMap.put(HttpAlertConstants.NAME_URL, url);
        paramsMap.put(HttpAlertConstants.NAME_REQUEST_TYPE, "POST");
        paramsMap.put(HttpAlertConstants.NAME_HEADER_PARAMS,
                "{\"Authorization\":\"Basic R0lcUFVCX0NJVElJX1NWTjphaWRzaWRAMjAyNQ==\",\"Content-Type\":\"application/json\"}");
        paramsMap.put(HttpAlertConstants.NAME_BODY_PARAMS,
                "{\"msgtype\":\"text\",\"text\":{\"content\":\"\"}}");
        paramsMap.put(HttpAlertConstants.NAME_CONTENT_FIELD, contentField);

        HttpSender httpSender = new HttpSender(paramsMap);
        AlertResult alertResult = httpSender.send("[{\"projectCode\":15379329985376,\"projectName\":\"test\",\"owner\":\"zachary_zhang\",\"processId\":1021216,\"processDefinitionCode\":15464960863968,\"processName\":\"补数-6-20251230104409314\",\"taskCode\":15464943982432,\"taskName\":\"时间\",\"taskType\":\"SHELL\",\"taskState\":\"FAILURE\",\"taskStartTime\":\"2025-12-30 10:44:09\",\"taskEndTime\":\"2025-12-30 10:44:09\",\"taskHost\":\"10.129.137.136:1234\",\"logPath\":\"/opt/apps/dolphinscheduler/worker-server/logs/20251230/15464960863968_6-1021216-2219793.log\"}]");
        Assert.assertEquals("true", alertResult.getStatus());
        // 验证发送结果
        System.out.println("发送结果: " + alertResult);
        System.out.println("请求URL: " + httpSender.getRequestUrl());
    }
}
