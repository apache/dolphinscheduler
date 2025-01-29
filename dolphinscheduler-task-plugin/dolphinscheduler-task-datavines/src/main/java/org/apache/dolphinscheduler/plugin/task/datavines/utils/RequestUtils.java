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

package org.apache.dolphinscheduler.plugin.task.datavines.utils;

import org.apache.dolphinscheduler.plugin.task.datavines.DatavinesTaskConstants;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.net.URI;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
public class RequestUtils {

    public static JsonNode executeJob(String address, String jobId, String token) {
        return parse(doPost(address + DatavinesTaskConstants.EXECUTE_JOB + jobId, token));
    }

    public static JsonNode getJobExecutionStatus(String address, String jobExecutionId, String token) {
        return parse(doGet(address + DatavinesTaskConstants.GET_JOB_EXECUTION_STATUS + jobExecutionId, token));
    }

    public static JsonNode getJobExecutionResult(String address, String jobExecutionId, String token) {
        return parse(doGet(address + DatavinesTaskConstants.GET_JOB_EXECUTION_RESULT + jobExecutionId, token));
    }

    public static void killJobExecution(String address, String jobExecutionId, String token) {
        parse(doPost(address + DatavinesTaskConstants.JOB_EXECUTION_KILL + jobExecutionId, token));
    }

    public static JsonNode parse(String res) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode result = null;
        try {
            result = mapper.readTree(res);
        } catch (JsonProcessingException e) {
            log.error("datavines task submit failed with error", e);
        }
        return result;
    }

    public static String doGet(String url, String token) {
        String result = "";
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            URI uri = uriBuilder.build();
            httpGet = new HttpGet(uri);
            httpGet.setHeader("Authorization", "Bearer " + token);
            log.info("access url: {}", uri);
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(response.getEntity());
                log.info("datavines task succeed with results: {}", result);
            } else {
                log.error("datavines task terminated,response: {}", response);
            }
        } catch (IllegalArgumentException ie) {
            log.error("datavines task terminated: {}", ie.getMessage());
        } catch (Exception e) {
            log.error("datavines task terminated: ", e);
        } finally {
            if (null != httpGet) {
                httpGet.releaseConnection();
            }
        }
        return result;
    }

    public static String doPost(String url, String token) {
        String result = "";
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost.setHeader("Authorization", "Bearer " + token);
            HttpResponse response = httpClient.execute(httpPost);
            log.info("access url: {}", url);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(response.getEntity());
                log.info("datavines task succeed with results: {}", result);
            } else {
                log.error("datavines task terminated, response: {}", response);
            }
        } catch (IllegalArgumentException ie) {
            log.error("datavines task terminated: {}", ie.getMessage());
        } catch (Exception he) {
            log.error("datavines task terminated: ", he);
        }
        return result;
    }
}
