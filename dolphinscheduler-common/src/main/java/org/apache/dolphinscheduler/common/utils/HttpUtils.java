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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * http utils
 */
public class HttpUtils {

    public static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * get http request content
     *
     * @param url url
     * @return http get request response content
     */
    public static String get(String url) {
	boolean kerberState = PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false);
	if (kerberState) {
	    String responseContent;
	    KerberosHttpClient kerberosHttpClient = new KerberosHttpClient(
		    PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME),
		    PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH),
		    PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH), true);
	    responseContent = kerberosHttpClient.get(url,
		    PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME));
	    return responseContent;

	} else {
	    CloseableHttpClient httpclient = HttpClients.createDefault();

	    HttpGet httpget = new HttpGet(url);
	    /** set timeout、request time、socket timeout */
	    RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT)
		    .setConnectionRequestTimeout(Constants.HTTP_CONNECTION_REQUEST_TIMEOUT)
		    .setSocketTimeout(Constants.SOCKET_TIMEOUT).setRedirectsEnabled(true).build();
	    httpget.setConfig(requestConfig);
	    return getResponseContentString(httpget, httpclient);
	}

    }

    /**
     * get http response content
     *
     * @param httpget    httpget
     * @param httpClient httpClient
     * @return http get request response content
     */
    public static String getResponseContentString(HttpGet httpget, CloseableHttpClient httpClient) {
	String responseContent = null;
	CloseableHttpResponse response = null;
	try {
	    response = httpClient.execute(httpget);
	    // check response status is 200
	    if (response.getStatusLine().getStatusCode() == 200) {
		HttpEntity entity = response.getEntity();
		if (entity != null) {
		    responseContent = EntityUtils.toString(entity, Constants.UTF_8);
		} else {
		    logger.warn("http entity is null");
		}
	    } else {
		logger.error("http get:{} response status code is not 200!", response.getStatusLine().getStatusCode());
	    }
	} catch (IOException ioe) {
	    logger.error(ioe.getMessage(), ioe);
	} finally {
	    try {
		if (response != null) {
		    EntityUtils.consume(response.getEntity());
		    response.close();
		}
	    } catch (IOException e) {
		logger.error(e.getMessage(), e);
	    }
	    if (!httpget.isAborted()) {
		httpget.releaseConnection();
		httpget.abort();
	    }
	    try {
		httpClient.close();
	    } catch (IOException e) {
		logger.error(e.getMessage(), e);
	    }
	}
	return responseContent;
    }

}
