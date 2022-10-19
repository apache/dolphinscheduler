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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/**
 * access token v2 controller test
 */
public class AccessTokenV2ControllerTest extends AbstractControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(AccessTokenV2ControllerTest.class);

    @Test
    public void testCreateToken() throws Exception {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("userId", 1);
        paramsMap.put("expireTime", "2022-12-31 00:00:00");
        paramsMap.put("token", "607f5aeaaa2093dbdff5d5522ce00510");
        MvcResult mvcResult = mockMvc.perform(post("/v2/access-tokens")
                .header("sessionId", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtils.toJsonString(paramsMap)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testCreateTokenIfAbsent() throws Exception {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("userId", 1);
        paramsMap.put("expireTime", "2022-12-31 00:00:00");
        paramsMap.put("token", null);

        MvcResult mvcResult = this.mockMvc
                .perform(post("/v2/access-tokens")
                        .header("sessionId", this.sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtils.toJsonString(paramsMap)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testExceptionHandler() throws Exception {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("userId", -1);
        paramsMap.put("expireTime", "2022-12-31 00:00:00");
        paramsMap.put("token", "507f5aeaaa2093dbdff5d5522ce00510");
        MvcResult mvcResult = mockMvc.perform(post("/v2/access-tokens")
                .header("sessionId", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtils.toJsonString(paramsMap)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
