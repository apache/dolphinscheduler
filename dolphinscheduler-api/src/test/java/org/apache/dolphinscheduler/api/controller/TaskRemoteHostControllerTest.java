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

import org.apache.dolphinscheduler.api.dto.TaskRemoteHostDTO;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.TaskRemoteHostService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class TaskRemoteHostControllerTest extends AbstractControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(TaskRemoteHostControllerTest.class);

    @MockBean(name = "taskRemoteHostService")
    private TaskRemoteHostService taskRemoteHostService;

    @Test
    void createTaskRemoteHost() throws Exception {
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("name", "app01");
        paramsMap.put("ip", "127.0.0.1");
        paramsMap.put("port", 22);
        paramsMap.put("password", "123");
        paramsMap.put("account", "foo");
        paramsMap.put("description", "description");

        Mockito.when(taskRemoteHostService.createTaskRemoteHost(Mockito.any(User.class),
                Mockito.any(TaskRemoteHostDTO.class))).thenReturn(1);

        MvcResult mvcResult = mockMvc.perform(post("/remote_host")
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
    void updateTaskRemoteHost() throws Exception {
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("name", "app01");
        paramsMap.put("ip", "127.0.0.1");
        paramsMap.put("port", 22);
        paramsMap.put("password", "123");
        paramsMap.put("account", "foo");
        paramsMap.put("description", "description");

        Mockito.when(taskRemoteHostService.updateTaskRemoteHost(Mockito.any(Long.class), Mockito.any(User.class),
                Mockito.any(TaskRemoteHostDTO.class))).thenReturn(1);
        MvcResult mvcResult = mockMvc.perform(put("/remote_host/1")
                .header("sessionId", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtils.toJsonString(paramsMap)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    void deleteTaskRemoteHost() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("code", "1");
        Mockito.when(taskRemoteHostService.deleteByCode(Mockito.any(Long.class), Mockito.any(User.class)))
                .thenReturn(1);
        MvcResult mvcResult = mockMvc.perform(delete("/remote_host/1")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    void queryTaskRemoteHostListPaging() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("searchVal", "test");
        paramsMap.add("pageSize", "2");
        paramsMap.add("pageNo", "2");

        Mockito.when(taskRemoteHostService.queryTaskRemoteHostListPaging(Mockito.any(User.class), Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
        MvcResult mvcResult = mockMvc.perform(get("/remote_host/list-paging")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        logger.info(result.toString());
        Assertions.assertTrue(result != null && result.isSuccess());
    }

    @Test
    void queryTaskRemoteHostList() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();

        MvcResult mvcResult = mockMvc.perform(get("/remote_host/query-remote-host-list")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        logger.info(result.toString());
        Assertions.assertTrue(result != null && result.isSuccess());
    }

    @Test
    void testConnect() throws Exception {
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("name", "app01");
        paramsMap.put("ip", "127.0.0.1");
        paramsMap.put("port", 22);
        paramsMap.put("password", "123");
        paramsMap.put("account", "foo");
        paramsMap.put("description", "description");

        Mockito.when(taskRemoteHostService.testConnect(Mockito.any(TaskRemoteHostDTO.class))).thenReturn(true);
        MvcResult mvcResult = mockMvc.perform(post("/remote_host/test-connect")
                .header("sessionId", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtils.toJsonString(paramsMap)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    void verifyTaskRemoteHost() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("taskRemoteHostName", "environmentName");

        Mockito.when(taskRemoteHostService.verifyTaskRemoteHost(Mockito.anyString())).thenReturn(true);
        MvcResult mvcResult = mockMvc.perform(post("/remote_host/verify-host")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }
}
