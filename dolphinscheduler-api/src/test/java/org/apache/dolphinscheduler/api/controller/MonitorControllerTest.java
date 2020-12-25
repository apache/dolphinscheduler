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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * monitor controller test
 */
public class MonitorControllerTest extends AbstractControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(MonitorControllerTest.class);


    @Test
    public void testListMaster() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/monitor/master/list")
            .header(SESSION_ID, sessionId)
           /* .param("type", ResourceType.FILE.name())*/   )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testListWorker() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/monitor/worker/list")
            .header(SESSION_ID, sessionId)
           /* .param("type", ResourceType.FILE.name())*/   )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testQueryDatabaseState() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/monitor/database")
            .header(SESSION_ID, sessionId)
            /* .param("type", ResourceType.FILE.name())*/   )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testQueryZookeeperState() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/monitor/zookeeper/list")
            .header(SESSION_ID, sessionId)
            /* .param("type", ResourceType.FILE.name())*/   )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
