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
package cn.escheduler.api.controller;

import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.utils.JSONUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class QueueControllerTest {
    private static Logger logger = LoggerFactory.getLogger(QueueControllerTest.class);

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    @Test
    public void queryList() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/queue/list")
                .header("sessionId", "d199af92-d8e0-4f1b-bacb-5b3414567e5c"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void queryPagingList() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        //paramsMap.add("processInstanceId","1380");
        paramsMap.add("searchVal","");
        paramsMap.add("pageNo","1");
        paramsMap.add("pageSize","20");

        MvcResult mvcResult = mockMvc.perform(get("/queue/list-paging")
                .header("sessionId", "d4541e0d-0349-4f05-9c68-300176cd3c91")
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void createQueue() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("queue","ait111134");
        paramsMap.add("queueName","aitName1");

        MvcResult mvcResult = mockMvc.perform(post("/queue/create")
                .header("sessionId", "d4541e0d-0349-4f05-9c68-300176cd3c91")
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        //Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void updateQueue() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id","2");
        paramsMap.add("queue","ait12");
        paramsMap.add("queueName","aitName");

        MvcResult mvcResult = mockMvc.perform(post("/queue/update")
                .header("sessionId", "d4541e0d-0349-4f05-9c68-300176cd3c91")
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        //Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void verifyQueue() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("queue","ait123");
        paramsMap.add("queueName","aitName");

        MvcResult mvcResult = mockMvc.perform(post("/queue/verify-queue")
                .header("sessionId", "d4541e0d-0349-4f05-9c68-300176cd3c91")
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        //Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}