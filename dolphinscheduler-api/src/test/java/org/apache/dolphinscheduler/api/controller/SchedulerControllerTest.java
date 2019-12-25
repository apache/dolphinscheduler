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
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.WarningType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * scheduler controller test
 */
public class SchedulerControllerTest extends AbstractControllerTest{
    private static Logger logger = LoggerFactory.getLogger(SchedulerControllerTest.class);

    @Test
    public void testCreateSchedule() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionId","40");
        paramsMap.add("schedule","{'startTime':'2019-12-16 00:00:00','endTime':'2019-12-17 00:00:00','crontab':'0 0 6 * * ? *'}");
        paramsMap.add("warningType",String.valueOf(WarningType.NONE));
        paramsMap.add("warningGroupId","1");
        paramsMap.add("failureStrategy",String.valueOf(FailureStrategy.CONTINUE));
        paramsMap.add("receivers","");
        paramsMap.add("receiversCc","");
        paramsMap.add("workerGroupId","1");
        paramsMap.add("processInstancePriority",String.valueOf(Priority.HIGH));

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/schedule/create","cxc_1113")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testUpdateSchedule() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id","37");
        paramsMap.add("schedule","{'startTime':'2019-12-16 00:00:00','endTime':'2019-12-17 00:00:00','crontab':'0 0 7 * * ? *'}");
        paramsMap.add("warningType",String.valueOf(WarningType.NONE));
        paramsMap.add("warningGroupId","1");
        paramsMap.add("failureStrategy",String.valueOf(FailureStrategy.CONTINUE));
        paramsMap.add("receivers","");
        paramsMap.add("receiversCc","");
        paramsMap.add("workerGroupId","1");
        paramsMap.add("processInstancePriority",String.valueOf(Priority.HIGH));

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/schedule/update","cxc_1113")
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
    public void testOnline() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id","37");

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/schedule/online","cxc_1113")
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
    public void testOffline() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id","28");

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/schedule/offline","cxc_1113")
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
    public void testQueryScheduleListPaging() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionId","40");
        paramsMap.add("searchVal","test");
        paramsMap.add("pageNo","1");
        paramsMap.add("pageSize","30");

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/schedule/list-paging","cxc_1113")
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
    public void testQueryScheduleList() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/schedule/list","cxc_1113")
                .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testPreviewSchedule() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/schedule/preview","cxc_1113")
                .header(SESSION_ID, sessionId)
                .param("schedule","{'startTime':'2019-06-10 00:00:00','endTime':'2019-06-13 00:00:00','crontab':'0 0 3/6 * * ? *'}"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testDeleteScheduleById() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("scheduleId","37");

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/schedule/delete","cxc_1113")
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
