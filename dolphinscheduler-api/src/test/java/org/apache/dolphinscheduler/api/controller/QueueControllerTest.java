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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * queue controller test
 */
public class QueueControllerTest extends AbstractControllerTest {

    private static Logger logger = LoggerFactory.getLogger(QueueControllerTest.class);

    private static final String QUEUE_CREATE_STRING = "queue1";

    @Test
    public void testQueryList() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/queue/list")
                .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info("query list queue return result:{}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryQueueListPaging() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("searchVal","");
        paramsMap.add("pageNo","1");
        paramsMap.add("pageSize","20");

        MvcResult mvcResult = mockMvc.perform(get("/queue/list-paging")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info("query list-paging queue return result:{}", mvcResult.getResponse().getContentAsString());

    }

    @Test
    public void testCreateQueue() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("queue", QUEUE_CREATE_STRING);
        paramsMap.add("queueName","root.queue1");

        MvcResult mvcResult = mockMvc.perform(post("/queue/create")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info("create queue return result:{}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testUpdateQueue() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id","1");
        paramsMap.add("queue","queue2");
        paramsMap.add("queueName","root.queue2");

        MvcResult mvcResult = mockMvc.perform(post("/queue/update")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info("update queue return result:{}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testVerifyQueue() throws Exception {

        // queue value exist
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("queue",QUEUE_CREATE_STRING);
        paramsMap.add("queueName","queue.name");

        MvcResult mvcResult = mockMvc.perform(post("/queue/verify-queue")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.QUEUE_VALUE_EXIST.getCode(),result.getCode().intValue());

        // success
        paramsMap.clear();
        paramsMap.add("queue","ait123");
        paramsMap.add("queueName","aitName");

        mvcResult = mockMvc.perform(post("/queue/verify-queue")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
        logger.info("verify queue return result:{}", mvcResult.getResponse().getContentAsString());
    }
}
