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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



public class AccessTokenControllerTest extends AbstractControllerTest{
    private static Logger logger = LoggerFactory.getLogger(AccessTokenControllerTest.class);


    @Test
    public void testCreateToken() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId","4");
        paramsMap.add("expireTime","2019-12-18 00:00:00");
        paramsMap.add("token","607f5aeaaa2093dbdff5d5522ce00510");
        MvcResult mvcResult = mockMvc.perform(post("/access-token/create")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testExceptionHandler() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId","-1");
        paramsMap.add("expireTime","2019-12-18 00:00:00");
        paramsMap.add("token","507f5aeaaa2093dbdff5d5522ce00510");
        MvcResult mvcResult = mockMvc.perform(post("/access-token/create")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.CREATE_ACCESS_TOKEN_ERROR.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testGenerateToken() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId","4");
        paramsMap.add("expireTime","2019-12-28 00:00:00");
        MvcResult mvcResult = mockMvc.perform(post("/access-token/generate")
                .header("sessionId", "5925a115-1691-47e0-9c46-4c7da03f6bbd")
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryAccessTokenList() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pageNo","1");
        paramsMap.add("pageSize","20");
        paramsMap.add("searchVal","");
        MvcResult mvcResult = mockMvc.perform(get("/access-token/list-paging")
                .header("sessionId", "5925a115-1691-47e0-9c46-4c7da03f6bbd")
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testDelAccessTokenById() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id","13");
        MvcResult mvcResult = mockMvc.perform(post("/access-token/delete")
                .header("sessionId", "5925a115-1691-47e0-9c46-4c7da03f6bbd")
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testUpdateToken() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id","12");
        paramsMap.add("userId","4");
        paramsMap.add("expireTime","2019-12-20 00:00:00");
        paramsMap.add("token","cxctoken123update");
        MvcResult mvcResult = mockMvc.perform(post("/access-token/update")
                .header("sessionId", "5925a115-1691-47e0-9c46-4c7da03f6bbd")
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

}
