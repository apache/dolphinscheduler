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

package org.apache.dolphinscheduler.common.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalServerHttpUtilsTest extends TestCase{

    private HadoopUtils hadoopUtils = HadoopUtils.getInstance();
    public static final Logger logger = LoggerFactory.getLogger(LocalServerHttpUtilsTest.class);
    private static LocalJettyHttpServer server = null;
    public static Test suite(){
        TestSuite suite=new TestSuite();
        suite.addTestSuite(LocalServerHttpUtilsTest.class);
        server = new LocalJettyHttpServer(suite);
        return server;
    }

    public void testGetTest() throws Exception {
        // success
        String result = null;
        result = HttpUtils.get("http://localhost:" + server.getServerPort()+ "/test.json");
        Assert.assertNotNull(result);
		ObjectNode jsonObject = JSONUtils.parseObject(result);
		Assert.assertEquals("Github",jsonObject.path("name").asText());
		result = HttpUtils.get("http://123.333.111.33/ccc");
		Assert.assertNull(result);
    }

    public void testGetByKerberos() {
        try {
            String applicationUrl = hadoopUtils.getApplicationUrl("application_1542010131334_0029");
            String responseContent;
            responseContent = HttpUtils.get(applicationUrl);
            Assert.assertNull(responseContent);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    public void testGetResponseContentString() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet("http://localhost:" +server.getServerPort()+"/test.json");
        /** set timeout、request time、socket timeout */
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(Constants.HTTP_CONNECTION_REQUEST_TIMEOUT)
                .setSocketTimeout(Constants.SOCKET_TIMEOUT).setRedirectsEnabled(true).build();
        httpget.setConfig(requestConfig);

        String responseContent = null;
        responseContent = HttpUtils.getResponseContentString(httpget, httpclient);
        Assert.assertNotNull(responseContent);

        responseContent = HttpUtils.getResponseContentString(null, httpclient);
        Assert.assertNull(responseContent);

        responseContent = HttpUtils.getResponseContentString(httpget, null);
        Assert.assertNull(responseContent);
    }

    public void testGetHttpClient() {
        CloseableHttpClient httpClient1 = HttpUtils.getInstance();
        CloseableHttpClient httpClient2 = HttpUtils.getInstance();
        Assert.assertEquals(httpClient1, httpClient2);
    }
}
