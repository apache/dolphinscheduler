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

import org.apache.dolphinscheduler.spi.alert.AlertData;
import org.apache.dolphinscheduler.spi.alert.AlertInfo;
import org.apache.dolphinscheduler.spi.alert.AlertResult;
import org.apache.dolphinscheduler.spi.params.InputParam;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * HttpAlertChannel UT
 */
public class HttpAlertChannelTest {

    @Test
    public void processTest() {

        HttpAlertChannel alertChannel = new HttpAlertChannel();
        AlertInfo alertInfo = new AlertInfo();
        AlertData alertData = new AlertData();
        alertData.setContent("Fault tolerance warning");
        alertInfo.setAlertData(alertData);
        AlertResult alertResult = alertChannel.process(alertInfo);
        Assert.assertEquals("http params is null", alertResult.getMessage());
    }

    @Test
    public void processTest2() {

        HttpAlertChannel alertChannel = new HttpAlertChannel();
        AlertInfo alertInfo = new AlertInfo();
        AlertData alertData = new AlertData();
        alertData.setContent("Fault tolerance warning");
        alertInfo.setAlertData(alertData);
        Map<String, String> paramsMap = PluginParamsTransfer.getPluginParamsMap(getParams());
        alertInfo.setAlertParams(paramsMap);
        AlertResult alertResult = alertChannel.process(alertInfo);
        Assert.assertEquals("true", alertResult.getStatus());
    }

    /**
     * create params
     */
    private String getParams() {

        List<PluginParams> paramsList = new ArrayList<>();
        InputParam urlParam = InputParam.newBuilder("url", "url")
                .setValue("http://www.baidu.com")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        InputParam headerParams = InputParam.newBuilder("headerParams", "headerParams")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .setValue("{\"Content-Type\":\"application/json\"}")
                .build();

        InputParam bodyParams = InputParam.newBuilder("bodyParams", "bodyParams")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .setValue("{\"number\":\"13457654323\"}")
                .build();

        InputParam content = InputParam.newBuilder("contentField", "contentField")
                .setValue("content")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        InputParam requestType = InputParam.newBuilder("requestType", "requestType")
                .setValue("POST")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        paramsList.add(urlParam);
        paramsList.add(headerParams);
        paramsList.add(bodyParams);
        paramsList.add(content);
        paramsList.add(requestType);

        return JSONUtils.toJsonString(paramsList);
    }

}
