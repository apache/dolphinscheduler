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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;

public class EnvironmentControllerTest extends AbstractControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentControllerTest.class);

    private String environmentCode;

    public static final String environmentName = "Env1";

    public static final String config = "this is config content";

    public static final String desc = "this is environment description";

    @BeforeEach
    public void before() throws Exception {
        testCreateEnvironment();
    }

    @Override
    @AfterEach
    public void after() throws Exception {
        testDeleteEnvironment();
    }

    public void testCreateEnvironment() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("name", environmentName);
        paramsMap.add("config", config);
        paramsMap.add("description", desc);

        MvcResult mvcResult = mockMvc.perform(post("/environment/create")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(),
                new TypeReference<Result<String>>() {
                });
        logger.info(result.toString());
        Assertions.assertTrue(result != null && result.isSuccess());
        Assertions.assertNotNull(result.getData());
        logger.info("create environment return result:{}", mvcResult.getResponse().getContentAsString());

        environmentCode = (String) result.getData();
    }

    @Test
    public void testUpdateEnvironment() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("code", environmentCode);
        paramsMap.add("name", "environment_test_update");
        paramsMap.add("config", "this is config content");
        paramsMap.add("desc", "the test environment update");

        MvcResult mvcResult = mockMvc.perform(post("/environment/update")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        logger.info(result.toString());
        Assertions.assertTrue(result != null && result.isSuccess());
        logger.info("update environment return result:{}", mvcResult.getResponse().getContentAsString());

    }

    @Test
    public void testQueryEnvironmentByCode() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("environmentCode", environmentCode);

        MvcResult mvcResult = mockMvc.perform(get("/environment/query-by-code")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        logger.info(result.toString());
        Assertions.assertTrue(result != null && result.isSuccess());
        logger.info(mvcResult.getResponse().getContentAsString());
        logger.info("query environment by id :{}, return result:{}", environmentCode,
                mvcResult.getResponse().getContentAsString());

    }

    @Test
    public void testQueryEnvironmentListPaging() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("searchVal", "test");
        paramsMap.add("pageSize", "2");
        paramsMap.add("pageNo", "2");

        MvcResult mvcResult = mockMvc.perform(get("/environment/list-paging")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        logger.info(result.toString());
        Assertions.assertTrue(result != null && result.isSuccess());
        logger.info("query list-paging environment return result:{}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryAllEnvironmentList() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();

        MvcResult mvcResult = mockMvc.perform(get("/environment/query-environment-list")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        logger.info(result.toString());
        Assertions.assertTrue(result != null && result.isSuccess());
        logger.info("query all environment return result:{}", mvcResult.getResponse().getContentAsString());

    }

    @Test
    public void testVerifyEnvironment() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("environmentName", environmentName);

        MvcResult mvcResult = mockMvc.perform(post("/environment/verify-environment")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        logger.info(result.toString());
        Assertions.assertTrue(result.isStatus(Status.ENVIRONMENT_NAME_EXISTS));
        logger.info("verify environment return result:{}", mvcResult.getResponse().getContentAsString());

    }

    private void testDeleteEnvironment() throws Exception {
        Preconditions.checkNotNull(environmentCode);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("environmentCode", environmentCode);

        MvcResult mvcResult = mockMvc.perform(post("/environment/delete")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        logger.info(result.toString());
        Assertions.assertTrue(result != null && result.isSuccess());
        logger.info("delete environment return result:{}", mvcResult.getResponse().getContentAsString());
    }
}
