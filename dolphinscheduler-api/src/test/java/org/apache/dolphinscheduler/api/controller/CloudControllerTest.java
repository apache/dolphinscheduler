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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.service.CloudService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * cloud controller test
 */
public class CloudControllerTest extends AbstractControllerTest {

    protected User user;
    @MockBean(name = "cloudService")
    private CloudService cloudService;
    @Autowired
    private UsersService usersService;

    @Test
    public void testListDataFactory() throws Exception {
        user = usersService.queryUser(1);
        when(cloudService.listDataFactory(user))
                .thenReturn(Collections.emptyList());

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        MvcResult mvcResult = mockMvc.perform(get("/cloud/azure/datafactory/factories")
                .header(sessionId, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        final Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertTrue(result != null && result.isSuccess());
    }

    @Test
    public void testListResourceGroup() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        MvcResult mvcResult = mockMvc.perform(get("/cloud/azure/datafactory/resourceGroups")
                .header(sessionId, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        final Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertTrue(result != null && result.isSuccess());
    }

    @Test
    public void testListPipeline() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("factoryName", "test name");
        paramsMap.add("resourceGroupName", "test group");
        MvcResult mvcResult = mockMvc.perform(get("/cloud/azure/datafactory/pipelines")
                .header(sessionId, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        final Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertTrue(result != null && result.isSuccess());
    }
}
