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
package org.apache.dolphinscheduler.common.task;

import org.apache.dolphinscheduler.common.enums.HttpCheckCondition;
import org.apache.dolphinscheduler.common.enums.HttpMethod;
import org.apache.dolphinscheduler.common.task.http.HttpParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * http parameter
 */
public class HttpParametersTest  {


    @Test
    public void testGenerator(){
        String paramData = "{\"localParams\":[],\"httpParams\":[],\"url\":\"https://www.baidu.com/\"," +
                "\"httpMethod\":\"GET\",\"httpCheckCondition\":\"STATUS_CODE_DEFAULT\",\"condition\":\"\",\"connectTimeout\":\"10000\",\"socketTimeout\":\"10000\"}";
        HttpParameters httpParameters = JSONUtils.parseObject(paramData, HttpParameters.class);


        Assert.assertEquals(10000,httpParameters.getConnectTimeout() );
        Assert.assertEquals(10000,httpParameters.getSocketTimeout());
        Assert.assertEquals("https://www.baidu.com/",httpParameters.getUrl());
        Assert.assertEquals(HttpMethod.GET,httpParameters.getHttpMethod());
        Assert.assertEquals(HttpCheckCondition.STATUS_CODE_DEFAULT,httpParameters.getHttpCheckCondition());
        Assert.assertEquals("",httpParameters.getCondition());

    }


    @Test
    public void testCheckParameters(){
        String paramData = "{\"localParams\":[],\"httpParams\":[],\"url\":\"https://www.baidu.com/\"," +
                "\"httpMethod\":\"GET\",\"httpCheckCondition\":\"STATUS_CODE_DEFAULT\",\"condition\":\"\",\"connectTimeout\":\"10000\",\"socketTimeout\":\"10000\"}";
        HttpParameters httpParameters = JSONUtils.parseObject(paramData, HttpParameters.class);

        Assert.assertTrue( httpParameters.checkParameters());
        Assert.assertEquals(10000,httpParameters.getConnectTimeout() );
        Assert.assertEquals(10000,httpParameters.getSocketTimeout());
        Assert.assertEquals("https://www.baidu.com/",httpParameters.getUrl());
        Assert.assertEquals(HttpMethod.GET,httpParameters.getHttpMethod());
        Assert.assertEquals(HttpCheckCondition.STATUS_CODE_DEFAULT,httpParameters.getHttpCheckCondition());
        Assert.assertEquals("",httpParameters.getCondition());

    }


    @Test
    public void testCheckValues() {
        String paramData = "{\"localParams\":[],\"httpParams\":[],\"url\":\"https://www.baidu.com/\"," +
                "\"httpMethod\":\"GET\",\"httpCheckCondition\":\"STATUS_CODE_DEFAULT\",\"condition\":\"\",\"connectTimeout\":\"10000\",\"socketTimeout\":\"10000\"}";
        HttpParameters httpParameters = JSONUtils.parseObject(paramData, HttpParameters.class);

        Assert.assertTrue( httpParameters.checkParameters());
        Assert.assertEquals(10000,httpParameters.getConnectTimeout() );
        Assert.assertEquals(10000,httpParameters.getSocketTimeout());
        Assert.assertEquals("https://www.baidu.com/",httpParameters.getUrl());
        Assert.assertEquals(HttpMethod.GET,httpParameters.getHttpMethod());
        Assert.assertEquals(HttpCheckCondition.STATUS_CODE_DEFAULT,httpParameters.getHttpCheckCondition());
        Assert.assertEquals("",httpParameters.getCondition());
        Assert.assertEquals(0,httpParameters.getLocalParametersMap().size());
        Assert.assertEquals(0,httpParameters.getResourceFilesList().size());
    }


}
