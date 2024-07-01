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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.dto.queue.QueueCreateRequest;
import org.apache.dolphinscheduler.api.dto.queue.QueueQueryRequest;
import org.apache.dolphinscheduler.api.dto.queue.QueueUpdateRequest;
import org.apache.dolphinscheduler.api.dto.queue.QueueVerifyRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/**
 * queue v2 controller test
 */
public class QueueV2ControllerTest extends AbstractControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(QueueV2ControllerTest.class);

    private static final String QUEUE_CREATE_NAME = "queue_create";
    private static final String QUEUE_MODIFY_NAME = "queue_modify";
    private static final String QUEUE_NAME_CREATE_NAME = "queue_name_create";
    private static final String QUEUE_NAME_MODIFY_NAME = "queue_name_modify";
    private static final String NOT_EXISTS_NAME = "not_exists";

    @Test
    public void testQueryList() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/v2/queues/list")
                .header(SESSION_ID, sessionId)
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info("query list queue return result:{}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryQueueListPagingEmpty() throws Exception {
        QueueQueryRequest queueQueryRequest = new QueueQueryRequest();
        queueQueryRequest.setSearchVal("");
        queueQueryRequest.setPageNo(1);
        queueQueryRequest.setPageSize(20);

        MvcResult mvcResult = mockMvc.perform(get("/v2/queues")
                .header(SESSION_ID, sessionId)
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtils.toJsonString(queueQueryRequest)))
                .andExpect(status().isOk())
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assertions.assertNotNull(result);
        logger.info("query list-paging queue return result:{}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testCreateQueue() throws Exception {
        QueueCreateRequest queueCreateRequest = new QueueCreateRequest();
        queueCreateRequest.setQueue(QUEUE_CREATE_NAME);
        queueCreateRequest.setQueueName(QUEUE_NAME_CREATE_NAME);
        MvcResult mvcResult = mockMvc.perform(post("/v2/queues")
                .header(SESSION_ID, sessionId)
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtils.toJsonString(queueCreateRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info("create queue return result:{}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testUpdateQueue() throws Exception {
        QueueUpdateRequest queueUpdateRequest = new QueueUpdateRequest();
        queueUpdateRequest.setQueue(QUEUE_MODIFY_NAME);
        queueUpdateRequest.setQueueName(QUEUE_NAME_MODIFY_NAME);
        MvcResult mvcResult = mockMvc.perform(put("/v2/queues/{id}", 1)
                .header(SESSION_ID, sessionId)
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtils.toJsonString(queueUpdateRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info("update queue return result:{}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testVerifyQueue() throws Exception {
        // queue value exist
        QueueVerifyRequest queueVerifyRequest = new QueueVerifyRequest();
        queueVerifyRequest.setQueue(QUEUE_MODIFY_NAME);
        queueVerifyRequest.setQueueName(NOT_EXISTS_NAME);
        MvcResult mvcResult = mockMvc.perform(post("/v2/queues/verify")
                .header(SESSION_ID, sessionId)
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtils.toJsonString(queueVerifyRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.QUEUE_VALUE_EXIST.getCode(), result.getCode().intValue());

        // queue name exist
        queueVerifyRequest.setQueue(NOT_EXISTS_NAME);
        queueVerifyRequest.setQueueName(QUEUE_NAME_CREATE_NAME);
        mvcResult = mockMvc.perform(post("/v2/queues/verify")
                .header(SESSION_ID, sessionId)
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtils.toJsonString(queueVerifyRequest)))
                .andExpect(status().isOk())
                .andReturn();
        result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.QUEUE_NAME_EXIST.getCode(), result.getCode().intValue());

        // success
        queueVerifyRequest.setQueue(NOT_EXISTS_NAME);
        queueVerifyRequest.setQueueName(NOT_EXISTS_NAME);
        mvcResult = mockMvc.perform(post("/v2/queues/verify")
                .header(SESSION_ID, sessionId)
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtils.toJsonString(queueVerifyRequest)))
                .andExpect(status().isOk())
                .andReturn();
        result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
        logger.info("verify queue return result:{}", mvcResult.getResponse().getContentAsString());
    }
}
