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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ProcessTaskRelationService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/**
 * process task relation controller test
 */
public class ProcessTaskRelationControllerTest extends AbstractControllerTest {

    @MockBean(name = "processTaskRelationServiceImpl")
    private ProcessTaskRelationService processTaskRelationService;

    @Test
    public void testQueryDownstreamRelation() throws Exception {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put(Constants.STATUS, Status.SUCCESS);
        Mockito.when(
                processTaskRelationService.queryDownstreamRelation(Mockito.any(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(mockResult);

        MvcResult mvcResult = mockMvc
                .perform(get("/projects/{projectCode}/process-task-relation/{taskCode}/downstream", "1113", "123")
                        .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testQueryUpstreamRelation() throws Exception {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put(Constants.STATUS, Status.SUCCESS);
        Mockito.when(
                processTaskRelationService.queryUpstreamRelation(Mockito.any(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(mockResult);

        MvcResult mvcResult = mockMvc
                .perform(get("/projects/{projectCode}/process-task-relation/{taskCode}/upstream", "1113", "123")
                        .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }
}
