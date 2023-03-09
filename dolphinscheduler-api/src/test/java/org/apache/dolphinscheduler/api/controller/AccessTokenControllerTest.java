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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * access token controller test
 */
public class AccessTokenControllerTest extends AbstractControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(AccessTokenControllerTest.class);

    @Test
    public void testCreateToken() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId", "4");
        paramsMap.add("expireTime", "2019-12-18 00:00:00");
        paramsMap.add("token", "607f5aeaaa2093dbdff5d5522ce00510");
        MvcResult mvcResult = mockMvc.perform(post("/access-tokens")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testCreateTokenIfAbsent() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId", "4");
        paramsMap.add("expireTime", "2019-12-18 00:00:00");
        paramsMap.add("token", null);

        MvcResult mvcResult = this.mockMvc
                .perform(post("/access-tokens")
                        .header("sessionId", this.sessionId)
                        .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testExceptionHandler() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId", "-1");
        paramsMap.add("expireTime", "2019-12-18 00:00:00");
        paramsMap.add("token", "507f5aeaaa2093dbdff5d5522ce00510");
        MvcResult mvcResult = mockMvc.perform(post("/access-tokens")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testGenerateToken() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId", "4");
        paramsMap.add("expireTime", "2019-12-28 00:00:00");
        MvcResult mvcResult = mockMvc.perform(post("/access-tokens/generate")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryAccessTokenList() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pageNo", "1");
        paramsMap.add("pageSize", "20");
        paramsMap.add("searchVal", "");
        MvcResult mvcResult = mockMvc.perform(get("/access-tokens")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryAccessTokenByUser() throws Exception {
        MvcResult mvcResult = this.mockMvc
                .perform(get("/access-tokens/user/1")
                        .header("sessionId", this.sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testDelAccessTokenById() throws Exception {
        testCreateToken();
        MvcResult mvcResult = mockMvc.perform(delete("/access-tokens/1")
                .header("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testUpdateToken() throws Exception {
        testCreateToken();
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId", "4");
        paramsMap.add("expireTime", "2019-12-20 00:00:00");
        paramsMap.add("token", "cxctoken123update");
        MvcResult mvcResult = mockMvc.perform(put("/access-tokens/1")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testUpdateTokenIfAbsent() throws Exception {
        this.testCreateTokenIfAbsent();

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId", "4");
        paramsMap.add("expireTime", "2019-12-20 00:00:00");
        paramsMap.add("token", null);

        MvcResult mvcResult = this.mockMvc
                .perform(put("/access-tokens/2")
                        .header("sessionId", this.sessionId)
                        .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        Assertions.assertNotNull(result.getData());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
