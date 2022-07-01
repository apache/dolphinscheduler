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

package org.apache.dolphinscheduler.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;

public class ClusterControllerTest extends AbstractControllerTest {
    public static final String clusterName = "Cluster1";
    public static final String config = "this is config content";
    public static final String desc = "this is cluster description";
    private static final Logger logger = LoggerFactory.getLogger(ClusterControllerTest.class);
    private String clusterCode;

    @Before
    public void before() throws Exception {
        testCreateCluster();
    }

    @Override
    @After
    public void after() throws Exception {
        testDeleteCluster();
    }

    public void testCreateCluster() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("name", clusterName);
        paramsMap.add("config", config);
        paramsMap.add("description", desc);

        MvcResult mvcResult = mockMvc.perform(post("/cluster/create")
            .header(SESSION_ID, sessionId)
            .params(paramsMap))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), new TypeReference<Result<String>>() {
        });
        logger.info(result.toString());
        Assert.assertTrue(result != null && result.isSuccess());
        Assert.assertNotNull(result.getData());
        logger.info("create cluster return result:{}", mvcResult.getResponse().getContentAsString());

        clusterCode = (String) result.getData();
    }

    @Test
    public void testUpdateCluster() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("code", clusterCode);
        paramsMap.add("name", "cluster_test_update");
        paramsMap.add("config", "{\"k8s\":\"apiVersion: v1\"}");
        paramsMap.add("desc", "the test cluster update");

        MvcResult mvcResult = mockMvc.perform(post("/cluster/update")
            .header(SESSION_ID, sessionId)
            .params(paramsMap))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        logger.info(result.toString());
        Assert.assertTrue(result != null && result.isSuccess());
        logger.info("update cluster return result:{}", mvcResult.getResponse().getContentAsString());

    }

    @Test
    public void testQueryClusterByCode() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("clusterCode", clusterCode);

        MvcResult mvcResult = mockMvc.perform(get("/cluster/query-by-code")
            .header(SESSION_ID, sessionId)
            .params(paramsMap))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        logger.info(result.toString());
        Assert.assertTrue(result != null && result.isSuccess());
        logger.info(mvcResult.getResponse().getContentAsString());
        logger.info("query cluster by id :{}, return result:{}", clusterCode, mvcResult.getResponse().getContentAsString());

    }

    @Test
    public void testQueryClusterListPaging() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("searchVal", "test");
        paramsMap.add("pageSize", "2");
        paramsMap.add("pageNo", "2");

        MvcResult mvcResult = mockMvc.perform(get("/cluster/list-paging")
            .header(SESSION_ID, sessionId)
            .params(paramsMap))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        logger.info(result.toString());
        Assert.assertTrue(result != null && result.isSuccess());
        logger.info("query list-paging cluster return result:{}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryAllClusterList() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();

        MvcResult mvcResult = mockMvc.perform(get("/cluster/query-cluster-list")
            .header(SESSION_ID, sessionId)
            .params(paramsMap))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        logger.info(result.toString());
        Assert.assertTrue(result != null && result.isSuccess());
        logger.info("query all cluster return result:{}", mvcResult.getResponse().getContentAsString());

    }

    @Test
    public void testVerifyCluster() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("clusterName", clusterName);

        MvcResult mvcResult = mockMvc.perform(post("/cluster/verify-cluster")
            .header(SESSION_ID, sessionId)
            .params(paramsMap))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        logger.info(result.toString());
        Assert.assertTrue(result.isStatus(Status.CLUSTER_NAME_EXISTS));
        logger.info("verify cluster return result:{}", mvcResult.getResponse().getContentAsString());

    }

    private void testDeleteCluster() throws Exception {
        Preconditions.checkNotNull(clusterCode);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("clusterCode", clusterCode);

        MvcResult mvcResult = mockMvc.perform(post("/cluster/delete")
            .header(SESSION_ID, sessionId)
            .params(paramsMap))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        logger.info(result.toString());
        Assert.assertTrue(result != null && result.isSuccess());
        logger.info("delete cluster return result:{}", mvcResult.getResponse().getContentAsString());
    }
}
