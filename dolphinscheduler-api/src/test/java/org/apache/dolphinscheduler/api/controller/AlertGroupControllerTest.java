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
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * alert group controller test
 */
public class AlertGroupControllerTest extends AbstractControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(AlertGroupController.class);

    @Test
    public void test010CreateAlertgroup() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("groupName", "cxc test group name");
        paramsMap.add("groupType", AlertType.EMAIL.toString());
        paramsMap.add("description", "cxc junit 测试告警描述");
        paramsMap.add("alertInstanceIds", "");
        MvcResult mvcResult = mockMvc.perform(post("/alert-groups")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void test020List() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        MvcResult mvcResult = mockMvc.perform(get("/alert-groups/list")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());

    }

    @Test
    public void test030ListPaging() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pageNo", "1");
        paramsMap.add("searchVal", AlertType.EMAIL.toString());
        paramsMap.add("pageSize", "1");
        MvcResult mvcResult = mockMvc.perform(get("/alert-groups")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void test040QueryAlertGroupById() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id", "1");
        MvcResult mvcResult = mockMvc.perform(post("/alert-groups/query")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void test050UpdateAlertgroup() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("groupName", "cxc test group name");
        paramsMap.add("groupType", AlertType.EMAIL.toString());
        paramsMap.add("description", "update alter group");
        paramsMap.add("alertInstanceIds", "");
        MvcResult mvcResult = mockMvc.perform(put("/alert-groups/1")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void test060VerifyGroupName() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("groupName", "cxc test group name");
        MvcResult mvcResult = mockMvc.perform(get("/alert-groups/verify-name")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void test070VerifyGroupNameNotExit() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("groupName", "cxc test group name xx");
        MvcResult mvcResult = mockMvc.perform(get("/alert-groups/verify-name")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void test080DelAlertgroupById() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        MvcResult mvcResult = mockMvc.perform(delete("/alert-groups/1")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
