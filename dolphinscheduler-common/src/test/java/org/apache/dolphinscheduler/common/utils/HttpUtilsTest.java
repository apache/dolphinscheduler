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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpClient utils test
 */
public class HttpUtilsTest {

    public static final Logger logger = LoggerFactory.getLogger(HttpUtilsTest.class);
    private HadoopUtils hadoopUtils = HadoopUtils.getInstance();

    @Test
    public void testGetTest() {
	// success
	String result = HttpUtils.get("https://www.baidu.com/sugrec?prod=pc_his&from=pc_web&json=1&sid=36427_36454_31253_36422_36165_36487_36055_36376_36234_26350_36469_36316&hisdata=&_t=1653904731156&req=2&csor=0");
	Assert.assertNotNull(result);
	ObjectNode jsonObject = JSONUtils.parseObject(result);
	Assert.assertNotNull(jsonObject.path("queryid").asText());
	result = HttpUtils.get("https://123.333.111.33/ccc");
	Assert.assertNull(result);
    }

    @Test
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

    @Test
    public void testGetResponseContentString() {
	CloseableHttpClient httpclient = HttpClients.createDefault();
	HttpGet httpget = new HttpGet("https://www.baidu.com/sugrec?prod=pc_his&from=pc_web&json=1&sid=36427_36454_31253_36422_36165_36487_36055_36376_36234_26350_36469_36316&hisdata=&_t=1653904731156&req=2&csor=0");
	/** set timeout、request time、socket timeout */
	RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT)
		.setConnectionRequestTimeout(Constants.HTTP_CONNECTION_REQUEST_TIMEOUT)
		.setSocketTimeout(Constants.SOCKET_TIMEOUT).setRedirectsEnabled(true).build();
	httpget.setConfig(requestConfig);
	String responseContent = HttpUtils.getResponseContentString(httpget, httpclient);
	Assert.assertNotNull(responseContent);

	responseContent = HttpUtils.getResponseContentString(null, httpclient);
	Assert.assertNull(responseContent);

	responseContent = HttpUtils.getResponseContentString(httpget, null);
	Assert.assertNull(responseContent);
    }


	@Test
	public void testGetHttpClient() {
		CloseableHttpClient httpClient1 = HttpUtils.getInstance();
		CloseableHttpClient httpClient2 = HttpUtils.getInstance();
		Assert.assertEquals(httpClient1, httpClient2);
	}

}
