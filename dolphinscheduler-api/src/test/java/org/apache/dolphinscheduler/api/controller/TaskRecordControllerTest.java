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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TaskRecordControllerTest extends AbstractControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(TaskRecordControllerTest.class);

    @Test
    public void testQueryTaskRecordListPaging() throws Exception {
            MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
            paramsMap.add("taskName","taskName");
            paramsMap.add("state","state");
            paramsMap.add("sourceTable","");
            paramsMap.add("destTable","");
            paramsMap.add("taskDate","");
            paramsMap.add("startDate","2019-12-16 00:00:00");
            paramsMap.add("endDate","2019-12-17 00:00:00");
            paramsMap.add("pageNo","1");
            paramsMap.add("pageSize","30");

            MvcResult mvcResult = mockMvc.perform(get("/projects/task-record/list-paging")
                    .header(SESSION_ID, sessionId)
                    .params(paramsMap))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn();

            Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
            Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
            logger.info(mvcResult.getResponse().getContentAsString());
        }


    @Test
    public void testQueryHistoryTaskRecordListPaging() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("taskName","taskName");
        paramsMap.add("state","state");
        paramsMap.add("sourceTable","");
        paramsMap.add("destTable","");
        paramsMap.add("taskDate","");
        paramsMap.add("startDate","2019-12-16 00:00:00");
        paramsMap.add("endDate","2019-12-17 00:00:00");
        paramsMap.add("pageNo","1");
        paramsMap.add("pageSize","30");

        MvcResult mvcResult = mockMvc.perform(get("/projects/task-record/history-list-paging")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());

    }
}
