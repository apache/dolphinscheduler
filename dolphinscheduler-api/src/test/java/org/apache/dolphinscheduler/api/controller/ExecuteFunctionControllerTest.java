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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.ExecutionOrder;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * executor controller test
 */
public class ExecuteFunctionControllerTest extends AbstractControllerTest {

    final Gson gson = new Gson();
    final long projectCode = 1L;
    final long processDefinitionCode = 2L;
    final String scheduleTime = "scheduleTime";
    final FailureStrategy failureStrategy = FailureStrategy.END;
    final String startNodeList = "startNodeList";
    final TaskDependType taskDependType = TaskDependType.TASK_ONLY;
    final CommandType execType = CommandType.PAUSE;
    final WarningType warningType = WarningType.NONE;
    final int warningGroupId = 3;
    final RunMode runMode = RunMode.RUN_MODE_SERIAL;
    final ExecutionOrder executionOrder = ExecutionOrder.DESC_ORDER;
    final Priority processInstancePriority = Priority.HIGH;
    final String workerGroup = "workerGroup";
    final String tenantCode = "root";
    final Long environmentCode = 4L;
    final Integer timeout = 5;
    final List<Property> startParams =
            Collections.singletonList(new Property("start", Direct.IN, DataType.VARCHAR, "params"));
    final Integer expectedParallelismNumber = 6;
    final int dryRun = 7;
    final int testFlag = 0;
    final ComplementDependentMode complementDependentMode = ComplementDependentMode.OFF_MODE;
    final Integer version = 1;
    final boolean allLevelDependent = false;
    final JsonObject expectResponseContent = gson
            .fromJson("{\"code\":0,\"msg\":\"success\",\"data\":\"Test Data\",\"success\":true,\"failed\":false}",
                    JsonObject.class);

    final ImmutableMap<String, Object> executeServiceResult =
            ImmutableMap.of(Constants.STATUS, Status.SUCCESS, Constants.DATA_LIST, "Test Data");

    @MockBean(name = "executorServiceImpl")
    private ExecutorService executorService;

    @Test
    public void testStartProcessInstanceWithFullParams() throws Exception {
        // Given
        final MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionCode", String.valueOf(processDefinitionCode));
        paramsMap.add("scheduleTime", scheduleTime);
        paramsMap.add("failureStrategy", String.valueOf(failureStrategy));
        paramsMap.add("startNodeList", startNodeList);
        paramsMap.add("taskDependType", String.valueOf(taskDependType));
        paramsMap.add("execType", String.valueOf(execType));
        paramsMap.add("warningType", String.valueOf(warningType));
        paramsMap.add("warningGroupId", String.valueOf(warningGroupId));
        paramsMap.add("runMode", String.valueOf(runMode));
        paramsMap.add("processInstancePriority", String.valueOf(processInstancePriority));
        paramsMap.add("workerGroup", workerGroup);
        paramsMap.add("tenantCode", tenantCode);
        paramsMap.add("environmentCode", String.valueOf(environmentCode));
        paramsMap.add("timeout", String.valueOf(timeout));
        paramsMap.add("startParams", gson.toJson(startParams));
        paramsMap.add("expectedParallelismNumber", String.valueOf(expectedParallelismNumber));
        paramsMap.add("dryRun", String.valueOf(dryRun));
        paramsMap.add("testFlag", String.valueOf(testFlag));
        paramsMap.add("executionOrder", String.valueOf(executionOrder));
        paramsMap.add("version", String.valueOf(version));

        when(executorService.execProcessInstance(any(User.class), eq(projectCode), eq(processDefinitionCode),
                eq(scheduleTime), eq(execType), eq(failureStrategy), eq(startNodeList), eq(taskDependType),
                eq(warningType),
                eq(warningGroupId), eq(runMode), eq(processInstancePriority), eq(workerGroup), eq(tenantCode),
                eq(environmentCode),
                eq(timeout), eq(startParams), eq(expectedParallelismNumber), eq(dryRun), eq(testFlag),
                eq(complementDependentMode), eq(version),
                eq(allLevelDependent), eq(executionOrder)))
                        .thenReturn(executeServiceResult);

        // When
        final MvcResult mvcResult = mockMvc
                .perform(post("/projects/{projectCode}/executors/start-process-instance", projectCode)
                        .header("sessionId", sessionId)
                        .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        // Then
        final JsonObject actualResponseContent =
                gson.fromJson(mvcResult.getResponse().getContentAsString(), JsonObject.class);
        assertThat(actualResponseContent).isEqualTo(expectResponseContent);
    }

    @Test
    public void testStartProcessInstanceWithoutTimeout() throws Exception {
        // Given
        final MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionCode", String.valueOf(processDefinitionCode));
        paramsMap.add("scheduleTime", scheduleTime);
        paramsMap.add("failureStrategy", String.valueOf(failureStrategy));
        paramsMap.add("startNodeList", startNodeList);
        paramsMap.add("taskDependType", String.valueOf(taskDependType));
        paramsMap.add("execType", String.valueOf(execType));
        paramsMap.add("warningType", String.valueOf(warningType));
        paramsMap.add("warningGroupId", String.valueOf(warningGroupId));
        paramsMap.add("runMode", String.valueOf(runMode));
        paramsMap.add("processInstancePriority", String.valueOf(processInstancePriority));
        paramsMap.add("workerGroup", workerGroup);
        paramsMap.add("tenantCode", tenantCode);
        paramsMap.add("environmentCode", String.valueOf(environmentCode));
        paramsMap.add("startParams", gson.toJson(startParams));
        paramsMap.add("expectedParallelismNumber", String.valueOf(expectedParallelismNumber));
        paramsMap.add("dryRun", String.valueOf(dryRun));
        paramsMap.add("testFlag", String.valueOf(testFlag));
        paramsMap.add("executionOrder", String.valueOf(executionOrder));
        paramsMap.add("version", String.valueOf(version));

        when(executorService.execProcessInstance(any(User.class), eq(projectCode), eq(processDefinitionCode),
                eq(scheduleTime), eq(execType), eq(failureStrategy), eq(startNodeList), eq(taskDependType),
                eq(warningType),
                eq(warningGroupId), eq(runMode), eq(processInstancePriority), eq(workerGroup), eq(tenantCode),
                eq(environmentCode),
                eq(Constants.MAX_TASK_TIMEOUT), eq(startParams), eq(expectedParallelismNumber), eq(dryRun),
                eq(testFlag),
                eq(complementDependentMode), eq(version), eq(allLevelDependent), eq(executionOrder)))
                        .thenReturn(executeServiceResult);

        // When
        final MvcResult mvcResult = mockMvc
                .perform(post("/projects/{projectCode}/executors/start-process-instance", projectCode)
                        .header("sessionId", sessionId)
                        .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        // Then
        final JsonObject actualResponseContent =
                gson.fromJson(mvcResult.getResponse().getContentAsString(), JsonObject.class);
        assertThat(actualResponseContent).isEqualTo(expectResponseContent);
    }

    @Test
    public void testStartProcessInstanceWithoutStartParams() throws Exception {
        // Given
        final MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionCode", String.valueOf(processDefinitionCode));
        paramsMap.add("scheduleTime", scheduleTime);
        paramsMap.add("failureStrategy", String.valueOf(failureStrategy));
        paramsMap.add("startNodeList", startNodeList);
        paramsMap.add("taskDependType", String.valueOf(taskDependType));
        paramsMap.add("execType", String.valueOf(execType));
        paramsMap.add("warningType", String.valueOf(warningType));
        paramsMap.add("warningGroupId", String.valueOf(warningGroupId));
        paramsMap.add("runMode", String.valueOf(runMode));
        paramsMap.add("processInstancePriority", String.valueOf(processInstancePriority));
        paramsMap.add("workerGroup", workerGroup);
        paramsMap.add("tenantCode", tenantCode);
        paramsMap.add("environmentCode", String.valueOf(environmentCode));
        paramsMap.add("timeout", String.valueOf(timeout));
        paramsMap.add("expectedParallelismNumber", String.valueOf(expectedParallelismNumber));
        paramsMap.add("dryRun", String.valueOf(dryRun));
        paramsMap.add("testFlag", String.valueOf(testFlag));
        paramsMap.add("executionOrder", String.valueOf(executionOrder));
        paramsMap.add("version", String.valueOf(version));

        when(executorService.execProcessInstance(any(User.class), eq(projectCode), eq(processDefinitionCode),
                eq(scheduleTime), eq(execType), eq(failureStrategy), eq(startNodeList), eq(taskDependType),
                eq(warningType),
                eq(warningGroupId), eq(runMode), eq(processInstancePriority), eq(workerGroup), eq(tenantCode),
                eq(environmentCode),
                eq(timeout), eq(null), eq(expectedParallelismNumber), eq(dryRun), eq(testFlag),
                eq(complementDependentMode), eq(version), eq(allLevelDependent), eq(executionOrder)))
                        .thenReturn(executeServiceResult);

        // When
        final MvcResult mvcResult = mockMvc
                .perform(post("/projects/{projectCode}/executors/start-process-instance", projectCode)
                        .header("sessionId", sessionId)
                        .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        // Then
        final JsonObject actualResponseContent =
                gson.fromJson(mvcResult.getResponse().getContentAsString(), JsonObject.class);
        assertThat(actualResponseContent).isEqualTo(expectResponseContent);
    }

    @Test
    public void testStartProcessInstanceWithRequiredParams() throws Exception {
        // Given
        final MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionCode", String.valueOf(processDefinitionCode));
        paramsMap.add("failureStrategy", String.valueOf(failureStrategy));
        paramsMap.add("warningType", String.valueOf(warningType));
        paramsMap.add("scheduleTime", scheduleTime);
        paramsMap.add("version", String.valueOf(version));

        when(executorService.execProcessInstance(any(User.class), eq(projectCode), eq(processDefinitionCode),
                eq(scheduleTime), eq(null), eq(failureStrategy), eq(null), eq(null), eq(warningType),
                eq(null), eq(null), eq(null), eq("default"), eq("default"), eq(-1L),
                eq(Constants.MAX_TASK_TIMEOUT), eq(null), eq(null), eq(0), eq(0),
                eq(complementDependentMode), eq(version), eq(allLevelDependent), eq(null)))
                        .thenReturn(executeServiceResult);

        // When
        final MvcResult mvcResult = mockMvc
                .perform(post("/projects/{projectCode}/executors/start-process-instance", projectCode)
                        .header("sessionId", sessionId)
                        .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        // Then
        final JsonObject actualResponseContent =
                gson.fromJson(mvcResult.getResponse().getContentAsString(), JsonObject.class);
        assertThat(actualResponseContent).isEqualTo(expectResponseContent);
    }

    @Test
    public void testExecuteWithSuccessStatus() throws Exception {
        // Given
        final ExecuteType executeType = ExecuteType.NONE;
        final int processInstanceId = 40;
        final long projectCode = 1113;
        final MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processInstanceId", Integer.toString(processInstanceId));
        paramsMap.add("executeType", String.valueOf(executeType));
        final Map<String, Object> executeServiceResult = new HashMap<>();
        executeServiceResult.put(Constants.STATUS, Status.SUCCESS);
        executeServiceResult.put(Constants.DATA_LIST, "Test Data");

        final JsonObject expectResponseContent = gson
                .fromJson("{\"code\":0,\"msg\":\"success\",\"data\":\"Test Data\",\"success\":true,\"failed\":false}",
                        JsonObject.class);

        when(executorService.execute(any(User.class), eq(projectCode), eq(processInstanceId), eq(ExecuteType.NONE)))
                .thenReturn(executeServiceResult);

        // When
        final MvcResult mvcResult = mockMvc.perform(post("/projects/{projectCode}/executors/execute", projectCode)
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        // Then
        final JsonObject actualResponseContent =
                gson.fromJson(mvcResult.getResponse().getContentAsString(), JsonObject.class);
        assertThat(actualResponseContent).isEqualTo(expectResponseContent);
    }

    @Test
    public void testStartCheckProcessDefinition() throws Exception {
        // Given
        when(executorService.startCheckByProcessDefinedCode(processDefinitionCode))
                .thenReturn(executeServiceResult);
        // When
        final MvcResult mvcResult = mockMvc.perform(post("/projects/{projectCode}/executors/start-check", projectCode)
                .header(SESSION_ID, sessionId)
                .param("processDefinitionCode", String.valueOf(processDefinitionCode)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        // Then
        final JsonObject actualResponseContent =
                gson.fromJson(mvcResult.getResponse().getContentAsString(), JsonObject.class);
        assertThat(actualResponseContent).isEqualTo(expectResponseContent);
    }

}
