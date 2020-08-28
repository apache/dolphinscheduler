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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.TaskInstanceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * task instance controller test
 */
public class TaskInstanceControllerTest extends AbstractControllerTest{
    private static Logger logger = LoggerFactory.getLogger(TaskInstanceControllerTest.class);

    @MockBean
    private TaskInstanceService taskInstanceService;

    @Test
    public void testQueryTaskListPaging() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        //paramsMap.add("processInstanceId","1380");
        paramsMap.add("searchVal","");
        paramsMap.add("taskName","");
        //paramsMap.add("stateType","");
        paramsMap.add("startDate","2019-02-26 19:48:00");
        paramsMap.add("endDate","2019-02-26 19:48:22");
        paramsMap.add("pageNo","1");
        paramsMap.add("pageSize","20");

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/task-instance/list-paging","cxc_1113")
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
    public void testForceSingleTaskSuccess() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("taskInstanceId","104");

        Map<String, Object> mockResult = new HashMap<>(5);
        mockResult.put(Constants.STATUS, Status.SUCCESS);
        mockResult.put(Constants.MSG, Status.SUCCESS.getMsg());
        when(taskInstanceService.forceSingleTaskSuccess(any(User.class), anyString(), anyInt())).thenReturn(mockResult);

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/task-instance/force-success","test")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
    }
}
