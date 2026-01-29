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
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;

import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * alert group controller test
 */
public class AlertGroupControllerTest extends AbstractControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(AlertGroupController.class);

    private static final String defaultTestAlertGroupName = "cxc test group name";

    @Autowired
    AlertGroupMapper alertGroupMapper;

    private int createEntity() {
        AlertGroup alertGroup = new AlertGroup();
        alertGroup.setGroupName(defaultTestAlertGroupName);
        alertGroup.setCreateTime(new Date());
        alertGroup.setUpdateTime(new Date());
        alertGroupMapper.insert(alertGroup);
        return alertGroup.getId();
    }

    @AfterEach
    public void clear() {
        alertGroupMapper.delete(
                new QueryWrapper<AlertGroup>().lambda().eq(AlertGroup::getGroupName, defaultTestAlertGroupName));
    }

    @Test
    public void test010CreateAlertGroup() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("groupName", defaultTestAlertGroupName);
        paramsMap.add("groupType", "email");
        paramsMap.add("description", "cxc junit test alert description");
        paramsMap.add("alertInstanceIds", "");
        MvcResult mvcResult = mockMvc.perform(post("/alert-groups")
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
    public void test020List() throws Exception {
        createEntity();
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        MvcResult mvcResult = mockMvc.perform(get("/alert-groups/list")
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
    public void test030ListPaging() throws Exception {
        createEntity();
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pageNo", "1");
        paramsMap.add("searchVal", "email");
        paramsMap.add("pageSize", "1");
        MvcResult mvcResult = mockMvc.perform(get("/alert-groups")
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
    public void test040QueryAlertGroupById() throws Exception {
        int entityId = createEntity();
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id", Integer.toString(entityId));
        MvcResult mvcResult = mockMvc.perform(post("/alert-groups/query")
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
    public void test050UpdateAlertGroup() throws Exception {
        int entityId = createEntity();
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("groupName", defaultTestAlertGroupName);
        paramsMap.add("groupType", "email");
        paramsMap.add("description", "update alter group");
        paramsMap.add("alertInstanceIds", "");
        MvcResult mvcResult = mockMvc.perform(put("/alert-groups/" + entityId)
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
    public void test060VerifyGroupName() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("groupName", defaultTestAlertGroupName);
        MvcResult mvcResult = mockMvc.perform(get("/alert-groups/verify-name")
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
    public void test070VerifyGroupNameNotExit() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("groupName", "cxc test group name xx");
        MvcResult mvcResult = mockMvc.perform(get("/alert-groups/verify-name")
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
    public void test080DelAlertGroupById() throws Exception {
        int entityId = createEntity();
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        MvcResult mvcResult = mockMvc.perform(delete("/alert-groups/" + entityId)
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
    public void test090DelAlertGroupById() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        MvcResult mvcResult = mockMvc.perform(delete("/alert-groups/1")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.NOT_ALLOW_TO_DELETE_DEFAULT_ALARM_GROUP.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
