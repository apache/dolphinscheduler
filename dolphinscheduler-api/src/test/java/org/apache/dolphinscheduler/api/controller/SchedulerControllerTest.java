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

import static org.mockito.ArgumentMatchers.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.User;

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

public class SchedulerControllerTest extends AbstractControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerControllerTest.class);

    @MockBean(name = "schedulerService")
    private SchedulerService schedulerService;

    @Test
    public void testCreateSchedule() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionCode", "40");
        paramsMap.add("schedule",
                "{'startTime':'2019-12-16 00:00:00','endTime':'2019-12-17 00:00:00','crontab':'0 0 6 * * ? *'}");
        paramsMap.add("warningType", String.valueOf(WarningType.NONE));
        paramsMap.add("warningGroupId", "1");
        paramsMap.add("failureStrategy", String.valueOf(FailureStrategy.CONTINUE));
        paramsMap.add("receivers", "");
        paramsMap.add("receiversCc", "");
        paramsMap.add("workerGroupId", "1");
        paramsMap.add("tenantCode", "root");
        paramsMap.add("processInstancePriority", String.valueOf(Priority.HIGH));

        Mockito.when(schedulerService.insertSchedule(isA(User.class), isA(Long.class), isA(Long.class),
                isA(String.class), isA(WarningType.class), isA(int.class), isA(FailureStrategy.class),
                isA(Priority.class), isA(String.class), isA(String.class), isA(Long.class))).thenReturn(success());

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectCode}/schedules/", 123)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testUpdateSchedule() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id", "37");
        paramsMap.add("schedule",
                "{'startTime':'2019-12-16 00:00:00','endTime':'2019-12-17 00:00:00','crontab':'0 0 7 * * ? *'}");
        paramsMap.add("warningType", String.valueOf(WarningType.NONE));
        paramsMap.add("warningGroupId", "1");
        paramsMap.add("failureStrategy", String.valueOf(FailureStrategy.CONTINUE));
        paramsMap.add("receivers", "");
        paramsMap.add("receiversCc", "");
        paramsMap.add("workerGroupId", "1");
        paramsMap.add("tenantCode", "root");
        paramsMap.add("processInstancePriority", String.valueOf(Priority.HIGH));

        Mockito.when(schedulerService.updateSchedule(isA(User.class), isA(Long.class), isA(Integer.class),
                isA(String.class), isA(WarningType.class), isA(Integer.class), isA(FailureStrategy.class),
                isA(Priority.class), isA(String.class), isA(String.class), isA(Long.class))).thenReturn(success());

        MvcResult mvcResult = mockMvc.perform(put("/projects/{projectCode}/schedules/{id}", 123, 37)
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
    public void testOnline() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id", "37");

        Mockito.doNothing().when(schedulerService).setScheduleState(isA(User.class), isA(Long.class),
                isA(Integer.class),
                isA(ReleaseState.class));

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectCode}/schedules/{id}/online", 123, 37)
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
    public void testOffline() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id", "28");

        Mockito.doNothing().when(schedulerService).setScheduleState(isA(User.class), isA(Long.class),
                isA(Integer.class),
                isA(ReleaseState.class));

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectCode}/schedules/{id}/offline", 123, 28)
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
    public void testQueryScheduleListPaging() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionCode", "40");
        paramsMap.add("searchVal", "test");
        paramsMap.add("pageNo", "1");
        paramsMap.add("pageSize", "30");

        PageInfo<Resource> pageInfo = new PageInfo<>(1, 10);
        Result mockResult = Result.success(pageInfo);

        Mockito.when(schedulerService.querySchedule(isA(User.class), isA(Long.class), isA(Long.class),
                isA(String.class), isA(Integer.class), isA(Integer.class))).thenReturn(mockResult);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectCode}/schedules/", 123)
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
    public void testQueryScheduleList() throws Exception {
        Mockito.when(schedulerService.queryScheduleList(isA(User.class), isA(Long.class))).thenReturn(success());

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectCode}/schedules/list", 123)
                .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testPreviewSchedule() throws Exception {
        Mockito.when(schedulerService.previewSchedule(isA(User.class), isA(String.class)))
                .thenReturn(success());

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectCode}/schedules/preview", 123)
                .header(SESSION_ID, sessionId)
                .param("schedule",
                        "{'startTime':'2019-06-10 00:00:00','endTime':'2019-06-13 00:00:00','crontab':'0 0 3/6 * * ? *','timezoneId':'Asia/Shanghai'}"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testDeleteScheduleById() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("scheduleId", "37");

        Mockito.doNothing().when(schedulerService).deleteSchedulesById(isA(User.class), isA(Integer.class));

        MvcResult mvcResult = mockMvc.perform(delete("/projects/{projectCode}/schedules/{id}", 123, 37)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
