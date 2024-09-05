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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.WorkflowInstanceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class WorkflowInstanceControllerTest extends AbstractControllerTest {

    @MockBean
    private WorkflowInstanceService workflowInstanceService;

    @Test
    public void testQueryWorkflowInstanceList() throws Exception {
        Result mockResult = new Result<>();
        mockResult.setCode(Status.SUCCESS.getCode());
        Mockito.when(workflowInstanceService
                .queryWorkflowInstanceList(Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.any(),
                        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.any()))
                .thenReturn(mockResult);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("workflowDefinitionCode", "91");
        paramsMap.add("searchVal", "cxc");
        paramsMap.add("stateType", WorkflowExecutionStatus.SUCCESS.name());
        paramsMap.add("host", "192.168.1.13");
        paramsMap.add("startDate", "2019-12-15 00:00:00");
        paramsMap.add("endDate", "2019-12-16 00:00:00");
        paramsMap.add("pageNo", "2");
        paramsMap.add("pageSize", "2");

        MvcResult mvcResult = mockMvc.perform(get("/projects/1113/workflow-instances")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testQueryTaskListByWorkflowInstanceId() throws Exception {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put(Constants.STATUS, Status.PROJECT_NOT_FOUND);
        Mockito
                .when(workflowInstanceService.queryTaskListByWorkflowInstanceId(Mockito.any(), Mockito.anyLong(),
                        Mockito.any()))
                .thenReturn(mockResult);

        MvcResult mvcResult = mockMvc
                .perform(get("/projects/{projectCode}/workflow-instances/{id}/tasks", "1113", "123")
                        .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), result.getCode().intValue());
    }

    @Test
    public void testUpdateWorkflowInstance() throws Exception {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put(Constants.STATUS, Status.SUCCESS);
        Mockito.when(workflowInstanceService
                .updateWorkflowInstance(Mockito.any(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(mockResult);

        String json =
                "[{\"name\":\"\",\"pre_task_code\":0,\"pre_task_version\":0,\"post_task_code\":123456789,\"post_task_version\":1,"
                        + "\"condition_type\":0,\"condition_params\":\"{}\"},{\"name\":\"\",\"pre_task_code\":123456789,\"pre_task_version\":1,"
                        + "\"post_task_code\":123451234,\"post_task_version\":1,\"condition_type\":0,\"condition_params\":\"{}\"}]";

        String locations = "{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("taskRelationJson", json);
        paramsMap.add("taskDefinitionJson", "");
        paramsMap.add("workflowInstanceId", "91");
        paramsMap.add("scheduleTime", "2019-12-15 00:00:00");
        paramsMap.add("syncDefine", "false");
        paramsMap.add("locations", locations);
        paramsMap.add("tenantCode", "123");

        MvcResult mvcResult = mockMvc.perform(put("/projects/{projectCode}/workflow-instances/{id}", "1113", "123")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testQueryWorkflowInstanceById() throws Exception {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put(Constants.STATUS, Status.SUCCESS);
        Mockito.when(
                workflowInstanceService.queryWorkflowInstanceById(Mockito.any(), Mockito.anyLong(), Mockito.anyInt()))
                .thenReturn(mockResult);
        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectCode}/workflow-instances/{id}", "1113", "123")
                .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testQuerySubWorkflowInstanceByTaskId() throws Exception {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put(Constants.STATUS, Status.TASK_INSTANCE_NOT_EXISTS);
        Mockito.when(workflowInstanceService.querySubWorkflowInstanceByTaskId(Mockito.any(), Mockito.anyLong(),
                Mockito.anyInt())).thenReturn(mockResult);

        MvcResult mvcResult = mockMvc
                .perform(get("/projects/{projectCode}/workflow-instances/query-sub-by-parent", "1113")
                        .header(SESSION_ID, sessionId)
                        .param("taskId", "1203"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.TASK_INSTANCE_NOT_EXISTS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testQueryParentInstanceBySubId() throws Exception {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put(Constants.STATUS, Status.WORKFLOW_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE);
        Mockito.when(
                workflowInstanceService.queryParentInstanceBySubId(Mockito.any(), Mockito.anyLong(), Mockito.anyInt()))
                .thenReturn(mockResult);

        MvcResult mvcResult = mockMvc
                .perform(get("/projects/{projectCode}/workflow-instances/query-parent-by-sub", "1113")
                        .header(SESSION_ID, sessionId)
                        .param("subId", "1204"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.WORKFLOW_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE.getCode(),
                result.getCode().intValue());
    }

    @Test
    public void testViewVariables() throws Exception {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put(Constants.STATUS, Status.SUCCESS);
        Mockito.when(workflowInstanceService.viewVariables(1113L, 123)).thenReturn(mockResult);
        MvcResult mvcResult = mockMvc
                .perform(get("/projects/{projectCode}/workflow-instances/{id}/view-variables", "1113", "123")
                        .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testDeleteWorkflowInstanceById() throws Exception {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put(Constants.STATUS, Status.SUCCESS);
        Mockito.doNothing().when(workflowInstanceService).deleteWorkflowInstanceById(Mockito.any(), Mockito.anyInt());

        MvcResult mvcResult = mockMvc.perform(delete("/projects/{projectCode}/workflow-instances/{id}", "1113", "123")
                .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testBatchDeleteWorkflowInstanceByIds() throws Exception {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put(Constants.STATUS, Status.WORKFLOW_INSTANCE_NOT_EXIST);

        Mockito.doNothing().when(workflowInstanceService).deleteWorkflowInstanceById(Mockito.any(), Mockito.anyInt());
        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectCode}/workflow-instances/batch-delete", "1113")
                .header(SESSION_ID, sessionId)
                .param("workflowInstanceIds", "1205,1206"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void queryWorkflowInstancesByTriggerCode() throws Exception {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put(Constants.STATUS, Status.SUCCESS);

        Mockito.when(workflowInstanceService
                .queryByTriggerCode(Mockito.any(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(mockResult);

        MvcResult mvcResult = mockMvc.perform(get("/projects/1113/workflow-instances/trigger")
                .header("sessionId", sessionId)
                .param("triggerCode", "12051206"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }
}
