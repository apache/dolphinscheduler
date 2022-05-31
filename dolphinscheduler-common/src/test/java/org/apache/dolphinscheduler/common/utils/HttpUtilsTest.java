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
    public void testGetTest() throws InterruptedException {
		// success
		String result = null;
		for (int i = 0; i < 10; i++) {
			result = HttpUtils.get("https://github.com/manifest.json");
			if (result != null) {
				break;
			}
		}
		Assert.assertNotNull(result);
		ObjectNode jsonObject = JSONUtils.parseObject(result);
		Assert.assertEquals("GitHub",jsonObject.path("name").asText());
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
		HttpGet httpget = new HttpGet("https://github.com/manifest.json");
		/** set timeout、request time、socket timeout */
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT)
				.setConnectionRequestTimeout(Constants.HTTP_CONNECTION_REQUEST_TIMEOUT)
				.setSocketTimeout(Constants.SOCKET_TIMEOUT).setRedirectsEnabled(true).build();
		httpget.setConfig(requestConfig);

		String responseContent = null;
		for (int i = 0; i < 10; i++) {
			responseContent = HttpUtils.getResponseContentString(httpget, httpclient);
			if (responseContent != null) {
				break;
			}
		}
		Assert.assertNotNull(responseContent);
		responseContent = null;

		for (int i = 0; i < 10; i++) {
			responseContent = HttpUtils.getResponseContentString(null, httpclient);
			if (responseContent != null) {
				break;
			}
		}
		Assert.assertNull(responseContent);
		responseContent = null;

		for (int i = 0; i < 10; i++) {
			responseContent = HttpUtils.getResponseContentString(httpget, null);
			if (responseContent != null) {
				break;
			}
		}
		Assert.assertNull(responseContent);
    }


	@Test
	public void testGetHttpClient() {
		CloseableHttpClient httpClient1 = HttpUtils.getInstance();
		CloseableHttpClient httpClient2 = HttpUtils.getInstance();
		Assert.assertEquals(httpClient1, httpClient2);
	}

}
