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

package org.apache.dolphinscheduler.plugin.task.http;

import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * http parameter
 */
public class HttpParametersTest {

    @Test
    public void testGenerator() {
        String paramData = "{\"localParams\":[],\"httpParams\":[],\"url\":\"https://www.baidu.com/\","
                + "\"httpMethod\":\"GET\",\"httpCheckCondition\":\"STATUS_CODE_DEFAULT\",\"condition\":\"\",\"connectTimeout\":\"10000\",\"socketTimeout\":\"10000\"}";
        HttpParameters httpParameters = JSONUtils.parseObject(paramData, HttpParameters.class);

        Assertions.assertEquals(10000, httpParameters.getConnectTimeout());
        Assertions.assertEquals(10000, httpParameters.getSocketTimeout());
        Assertions.assertEquals("https://www.baidu.com/", httpParameters.getUrl());
        Assertions.assertEquals(HttpMethod.GET, httpParameters.getHttpMethod());
        Assertions.assertEquals(HttpCheckCondition.STATUS_CODE_DEFAULT, httpParameters.getHttpCheckCondition());
        Assertions.assertEquals("", httpParameters.getCondition());

    }

    @Test
    public void testCheckParameters() {
        String paramData = "{\"localParams\":[],\"httpParams\":[],\"url\":\"https://www.baidu.com/\","
                + "\"httpMethod\":\"GET\",\"httpCheckCondition\":\"STATUS_CODE_DEFAULT\",\"condition\":\"\",\"connectTimeout\":\"10000\",\"socketTimeout\":\"10000\"}";
        HttpParameters httpParameters = JSONUtils.parseObject(paramData, HttpParameters.class);

        Assertions.assertTrue(httpParameters.checkParameters());
        Assertions.assertEquals(10000, httpParameters.getConnectTimeout());
        Assertions.assertEquals(10000, httpParameters.getSocketTimeout());
        Assertions.assertEquals("https://www.baidu.com/", httpParameters.getUrl());
        Assertions.assertEquals(HttpMethod.GET, httpParameters.getHttpMethod());
        Assertions.assertEquals(HttpCheckCondition.STATUS_CODE_DEFAULT, httpParameters.getHttpCheckCondition());
        Assertions.assertEquals("", httpParameters.getCondition());

    }

    @Test
    public void testCheckValues() {
        String paramData = "{\"localParams\":[],\"httpParams\":[],\"url\":\"https://www.baidu.com/\","
                + "\"httpMethod\":\"GET\",\"httpCheckCondition\":\"STATUS_CODE_DEFAULT\",\"condition\":\"\",\"connectTimeout\":\"10000\",\"socketTimeout\":\"10000\"}";
        HttpParameters httpParameters = JSONUtils.parseObject(paramData, HttpParameters.class);

        Assertions.assertTrue(httpParameters.checkParameters());
        Assertions.assertEquals(10000, httpParameters.getConnectTimeout());
        Assertions.assertEquals(10000, httpParameters.getSocketTimeout());
        Assertions.assertEquals("https://www.baidu.com/", httpParameters.getUrl());
        Assertions.assertEquals(HttpMethod.GET, httpParameters.getHttpMethod());
        Assertions.assertEquals(HttpCheckCondition.STATUS_CODE_DEFAULT, httpParameters.getHttpCheckCondition());
        Assertions.assertEquals("", httpParameters.getCondition());
        Assertions.assertEquals(0, httpParameters.getLocalParametersMap().size());
        Assertions.assertEquals(0, httpParameters.getResourceFilesList().size());
    }

    @Test
    public void testHttpParams() {
        String paramData = "{\"localParams\":[],\"httpParams\":[],\"url\":\"https://www.baidu.com/\","
                + "\"httpMethod\":\"GET\",\"httpCheckCondition\":\"STATUS_CODE_DEFAULT\",\"condition\":\"\",\"connectTimeout\":\"10000\",\"socketTimeout\":\"10000\"}";
        HttpParameters httpParameters = JSONUtils.parseObject(paramData, HttpParameters.class);

        String body = "{\"localParams\":[],\"httpParams\":[],\"url\":\"https://www.baidu.com/\","
                + "\"httpMethod\":\"GET\",\"httpCheckCondition\":\"STATUS_CODE_DEFAULT\",\"condition\":\"\",\"connectTimeout\":\"10000\",\"socketTimeout\":\"10000\"}";
        List<Property> localParams = new ArrayList<>();
        Property property = new Property();
        property.setProp("body");
        property.setDirect(Direct.OUT);
        property.setType(DataType.VARCHAR);
        property.setValue("1");
        localParams.add(property);
        httpParameters.setLocalParams(localParams);
        String result = httpParameters.setBodyReturn(body, httpParameters.getLocalParams());

        Assert.assertTrue(httpParameters.checkParameters());
        Assert.assertEquals(10000,httpParameters.getConnectTimeout());
        Assert.assertEquals(10000,httpParameters.getSocketTimeout());
        Assert.assertEquals("https://www.baidu.com/",httpParameters.getUrl());
        Assert.assertEquals(HttpMethod.GET,httpParameters.getHttpMethod());
        Assert.assertEquals(HttpCheckCondition.STATUS_CODE_DEFAULT,httpParameters.getHttpCheckCondition());
        Assert.assertEquals("",httpParameters.getCondition());
        Assert.assertEquals(1,httpParameters.getLocalParametersMap().size());
        Assert.assertEquals(0,httpParameters.getResourceFilesList().size());
    }
}
