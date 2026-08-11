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

import org.apache.dolphinscheduler.api.dto.workflowInstance.WorkflowInstanceQueryDTO;
import org.apache.dolphinscheduler.api.dto.workflowInstance.WorkflowInstanceVariablesDTO;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.WorkflowInstanceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    /**
     * All properties that were present on {@link WorkflowInstance} but are
     * intentionally removed from {@link WorkflowInstanceQueryDTO} and thus from
     * the list/topN/trigger API response. This list must stay in sync with the
     * incompatible-change documentation in incompatible.md (version 3.5.0).
     */
    private static final List<String> REMOVED_RESPONSE_PROPERTIES = java.util.Arrays.asList(
            // Heavy DB-backed fields removed from the SQL projection
            "commandParam", "globalParams", "historyCmd", "varPool", "stateHistory",
            // Transient (non-DB) fields that were always null in list responses
            "stateDescList", "workflowDefinition", "dagData", "queue",
            "locations", "dependenceScheduleTimes",
            // Derived getter properties
            "cmdTypeIfComplement", "complementData");

    @Test
    public void testQueryWorkflowInstanceList() throws Exception {
        WorkflowInstanceQueryDTO dto = new WorkflowInstanceQueryDTO();
        dto.setId(1);
        dto.setName("test-workflow");
        PageInfo<WorkflowInstanceQueryDTO> pageInfo = new PageInfo<>(1, 10);
        pageInfo.setTotalList(java.util.Collections.singletonList(dto));
        pageInfo.setTotal(1);

        Result<PageInfo<WorkflowInstanceQueryDTO>> mockResult = new Result<>();
        mockResult.setCode(Status.SUCCESS.getCode());
        mockResult.setData(pageInfo);
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
        String responseBody = mvcResult.getResponse().getContentAsString();
        Result result = JSONUtils.parseObject(responseBody, Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());

        // Parse the JSON tree and navigate to the DTO object in the response
        com.fasterxml.jackson.databind.node.ObjectNode root = JSONUtils.parseObject(responseBody);
        com.fasterxml.jackson.databind.JsonNode dtoNode =
                root.path("data").path("totalList").path(0);
        Assertions.assertTrue(dtoNode.isObject(),
                "Response data.totalList[0] should be a JSON object (the DTO)");
        java.util.Set<String> jsonKeys = new java.util.HashSet<>();
        dtoNode.fieldNames().forEachRemaining(jsonKeys::add);

        // Verify that none of the removed properties appear in the serialized response
        for (String removed : REMOVED_RESPONSE_PROPERTIES) {
            Assertions.assertFalse(jsonKeys.contains(removed),
                    "List endpoint response JSON should not contain key '" + removed
                            + "' — it is removed from WorkflowInstanceQueryDTO");
        }

        // Positive assertion: verify all expected DTO fields are present in the response.
        // This catches accidental omission of fields that should be retained.
        java.util.Set<String> expectedKeys = new java.util.HashSet<>(java.util.Arrays.asList(
                "id", "name", "workflowDefinitionCode", "workflowDefinitionVersion",
                "projectCode", "state", "recovery", "startTime", "endTime",
                "runTimes", "host", "commandType", "taskDependType",
                "maxTryTimes", "failureStrategy", "warningType", "warningGroupId",
                "scheduleTime", "commandStartTime",
                "isSubWorkflow", "executorId", "workflowInstancePriority",
                "workerGroup", "environmentCode", "timeout", "tenantCode",
                "dryRun", "nextWorkflowInstanceId", "restartTime", "duration",
                "executorName"));
        for (String expected : expectedKeys) {
            Assertions.assertTrue(jsonKeys.contains(expected),
                    "List endpoint response JSON should contain key '" + expected
                            + "' — it is a required DTO field");
        }
    }

    @Test
    public void testQueryTaskListByWorkflowInstanceId() throws Exception {
        Mockito
                .when(workflowInstanceService.queryTaskListByWorkflowInstanceId(Mockito.any(), Mockito.anyLong(),
                        Mockito.any()))
                .thenThrow(new ServiceException(Status.PROJECT_NOT_FOUND));

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
        WorkflowDefinition mockResult = new WorkflowDefinition();
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
        Mockito.when(
                workflowInstanceService.queryWorkflowInstanceById(Mockito.any(), Mockito.anyLong(), Mockito.anyInt()))
                .thenReturn(new WorkflowInstance());
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
        Mockito.when(workflowInstanceService.querySubWorkflowInstanceByTaskId(Mockito.any(), Mockito.anyLong(),
                Mockito.anyInt())).thenThrow(new ServiceException(Status.TASK_INSTANCE_NOT_EXISTS));

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
        Mockito.when(
                workflowInstanceService.queryParentInstanceBySubId(Mockito.any(), Mockito.anyLong(), Mockito.anyInt()))
                .thenThrow(new ServiceException(Status.WORKFLOW_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE));

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
        WorkflowInstanceVariablesDTO mockResult =
                new WorkflowInstanceVariablesDTO(Collections.emptyList(), Collections.emptyMap());
        Mockito.when(workflowInstanceService.viewVariables(Mockito.any(), Mockito.eq(1113L), Mockito.eq(123)))
                .thenReturn(mockResult);
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
        Mockito.when(workflowInstanceService
                .queryByTriggerCode(Mockito.any(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(new ArrayList<>());

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

    @Test
    public void testWorkflowInstanceQueryDTO_omitsRemovedProperties() {
        // Verify via reflection that WorkflowInstanceQueryDTO does NOT declare
        // the 5 heavy DB-backed fields removed from the list API response contract.
        List<String> heavyFields = java.util.Arrays.asList(
                "commandParam", "globalParams", "historyCmd", "varPool", "stateHistory");
        for (String field : heavyFields) {
            Assertions.assertThrows(NoSuchFieldException.class,
                    () -> WorkflowInstanceQueryDTO.class.getDeclaredField(field),
                    "WorkflowInstanceQueryDTO should NOT declare field '" + field
                            + "' — it was intentionally removed from the list API response contract");
        }
    }

    @Test
    public void testWorkflowInstanceQueryDTO_includesRequiredFields() {
        // Verify that all fields present in listSql are also declared in the DTO.
        List<String> requiredFields = java.util.Arrays.asList(
                "id", "name", "workflowDefinitionCode", "workflowDefinitionVersion",
                "projectCode", "state", "recovery", "startTime", "endTime",
                "runTimes", "host", "commandType", "taskDependType",
                "maxTryTimes", "failureStrategy", "warningType", "warningGroupId",
                "scheduleTime", "commandStartTime",
                "isSubWorkflow", "executorId", "workflowInstancePriority",
                "workerGroup", "environmentCode", "timeout", "tenantCode",
                "dryRun", "nextWorkflowInstanceId", "restartTime", "duration",
                "executorName");
        for (String field : requiredFields) {
            Assertions.assertDoesNotThrow(() -> WorkflowInstanceQueryDTO.class.getDeclaredField(field),
                    "WorkflowInstanceQueryDTO should declare field '" + field
                            + "' — it must be present in the list API response contract");
        }
    }

    @Test
    public void testFromEntity_MapsAllDtoFields() {
        // Build a minimal WorkflowInstance and ensure fromEntity maps without error
        // and every DTO field is non-null where expected.
        WorkflowInstance instance = new WorkflowInstance();
        instance.setId(1);
        instance.setName("test-workflow");
        instance.setWorkflowDefinitionCode(123456L);
        instance.setWorkflowDefinitionVersion(1);
        instance.setProjectCode(789L);
        instance.setState(WorkflowExecutionStatus.SUCCESS);
        instance.setExecutorName("admin");

        WorkflowInstanceQueryDTO dto = WorkflowInstanceQueryDTO.fromEntity(instance);

        Assertions.assertEquals(1, dto.getId());
        Assertions.assertEquals("test-workflow", dto.getName());
        Assertions.assertEquals(123456L, dto.getWorkflowDefinitionCode());
        Assertions.assertEquals(WorkflowExecutionStatus.SUCCESS, dto.getState());
        Assertions.assertEquals("admin", dto.getExecutorName());
    }
}
