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
 * queue controller test
 */
public class TaskGroupControllerTest extends AbstractControllerTest {

    private static Logger logger = LoggerFactory.getLogger(TaskGroupControllerTest.class);

    private static final String QUEUE_CREATE_STRING = "queue1";

    @Test
    public void testQueryListAll() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pageNo", "2");
        paramsMap.add("pageSize", "2");
        paramsMap.add("projectCode", "123456789");
        MvcResult mvcResult = mockMvc.perform(get("/task-group/query-list-by-projectCode")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertTrue(result != null && result.isSuccess());
        logger.info("query list queue return result:{}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryByName() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pageNo", "1");
        paramsMap.add("name", "TGQ");
        paramsMap.add("pageSize", "10");
        MvcResult mvcResult = mockMvc.perform(get("/task-group/list-paging")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertTrue(result != null && result.isSuccess());
        logger.info("query list queue return result:{}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryByStatus() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pageNo", "1");
        paramsMap.add("status", "1");
        paramsMap.add("pageSize", "10");
        MvcResult mvcResult = mockMvc.perform(get("/task-group/query-list-by-status")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertTrue(result != null && result.isSuccess());
        logger.info("query list queue return result:{}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testCreateTaskGroup() throws Exception {

        // success
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("name", "TGQ1");
        paramsMap.add("description", "this is a task group queue!");
        paramsMap.add("groupSize", "10");

        MvcResult mvcResult = mockMvc.perform(post("/task-group/create")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertTrue(result != null && result.isSuccess());
        logger.info("create queue return result:{}", mvcResult.getResponse().getContentAsString());
        // failed
        // name exists
        paramsMap.clear();
        paramsMap.add("name", "TGQ1");
        paramsMap.add("description", "this is a task group queue!");
        paramsMap.add("groupSize", "10");

        MvcResult mvcResult1 = mockMvc.perform(post("/task-group/create")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result1 = JSONUtils.parseObject(mvcResult1.getResponse().getContentAsString(), Result.class);
        Assertions.assertTrue(result1 != null && result1.isFailed());
        logger.info("create queue return result:{}", mvcResult1.getResponse().getContentAsString());
    }

    @Test
    public void testUpdateTaskGroup() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id", "1");
        paramsMap.add("name", "TGQ11");
        paramsMap.add("description", "this is a task group queue!");
        paramsMap.add("groupSize", "10");

        MvcResult mvcResult = mockMvc.perform(post("/task-group/update")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        logger.info("update queue return result:{}", mvcResult.getResponse().getContentAsString());
        Assertions.assertTrue(result != null && result.isSuccess());
        logger.info("update queue return result:{}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testCloseAndStartTaskGroup() throws Exception {

        // close
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id", "1");
        MvcResult mvcResult = mockMvc.perform(post("/task-group/close-task-group")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        logger.info("update queue return result:{}", mvcResult.getResponse().getContentAsString());
        Assertions.assertTrue(result != null && result.isSuccess());
        logger.info("update queue return result:{}", mvcResult.getResponse().getContentAsString());

        // start
        paramsMap.clear();
        paramsMap.add("id", "1");
        MvcResult mvcResult1 = mockMvc.perform(post("/task-group/start-task-group")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result1 = JSONUtils.parseObject(mvcResult1.getResponse().getContentAsString(), Result.class);
        logger.info("update queue return result:{}", mvcResult1.getResponse().getContentAsString());
        Assertions.assertTrue(result1 != null && result1.isSuccess());
        logger.info("update queue return result:{}", mvcResult.getResponse().getContentAsString());
    }

}
