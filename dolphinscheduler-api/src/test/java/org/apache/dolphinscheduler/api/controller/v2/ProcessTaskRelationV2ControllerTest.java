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

package org.apache.dolphinscheduler.api.controller.v2;

import org.apache.dolphinscheduler.api.controller.AbstractControllerTest;
import org.apache.dolphinscheduler.api.dto.taskRelation.TaskRelationCreateRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ProcessTaskRelationService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * process task relation controller test
 */
public class ProcessTaskRelationV2ControllerTest extends AbstractControllerTest {
    @MockBean(name = "processTaskRelationServiceImpl")
    private ProcessTaskRelationService processTaskRelationService;

    @Test
    public void testCreateTaskRelation() throws Exception{
        ProcessTaskRelation mockResult = new ProcessTaskRelation();

        TaskRelationCreateRequest taskRelationCreateRequest=new TaskRelationCreateRequest();
        taskRelationCreateRequest.setPostTaskCode(111L);
        taskRelationCreateRequest.setPreTaskCode(222L);
        taskRelationCreateRequest.setProjectCode(333L);
        taskRelationCreateRequest.setWorkflowCode(444L);
        Mockito.when(
                        processTaskRelationService.createProcessTaskRelationV2(Mockito.any(), Mockito.any()))
                .thenReturn(mockResult);

        MvcResult mvcResult = mockMvc
                .perform(post("/v2/relations", taskRelationCreateRequest)
                        .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testDeleteTaskRelation() throws Exception {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put(Constants.STATUS, Status.SUCCESS);

        MvcResult mvcResult = mockMvc
                .perform(delete("/v2/relations/{code-pair}", "1113")
                        .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }
    @Test
    public void testUpdateUpstreamTaskDefinition() throws Exception {
        List<ProcessTaskRelation> mockResult= new ArrayList<>();

        Mockito.when(
                        processTaskRelationService.updateUpstreamTaskDefinitionWithSyncDag(Mockito.any(), Mockito.anyLong(),Mockito.anyBoolean(), Mockito.any()))
                .thenReturn(mockResult);

        MvcResult mvcResult = mockMvc
                .perform(put("/v2/relations/{code}", "1113", false)
                        .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }
}
