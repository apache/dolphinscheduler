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

import org.apache.dolphinscheduler.spi.alert.AlertResult;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * HttpSender UT
 */
public class HttpSenderTest {

    @Test
    public void sendTest() {

        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put(HttpAlertConstants.URL, "http://www.baidu.com");
        paramsMap.put(HttpAlertConstants.REQUEST_TYPE, "POST");
        paramsMap.put(HttpAlertConstants.HEADER_PARAMS, "{\"Content-Type\":\"application/json\"}");
        paramsMap.put(HttpAlertConstants.BODY_PARAMS, "{\"number\":\"13457654323\"}");
        paramsMap.put(HttpAlertConstants.CONTENT_FIELD, "content");
        HttpSender httpSender = new HttpSender(paramsMap);
        AlertResult alertResult = httpSender.send("Fault tolerance warning");
        Assert.assertEquals("true", alertResult.getStatus());
    }
}
