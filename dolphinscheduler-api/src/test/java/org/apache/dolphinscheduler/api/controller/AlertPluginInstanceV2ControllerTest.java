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
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.dto.alert.AlertPluginInstanceListPagingResponse;
import org.apache.dolphinscheduler.api.dto.alert.AlertPluginInstanceListResponse;
import org.apache.dolphinscheduler.api.dto.alert.AlertPluginInstanceResponse;
import org.apache.dolphinscheduler.api.dto.alert.AlertPluginQueryRequest;
import org.apache.dolphinscheduler.api.dto.alert.CreatePluginRequest;
import org.apache.dolphinscheduler.api.dto.alert.UpdatePluginRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.AlertPluginInstanceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.AlertPluginInstanceVO;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * alert plugin instance controller test
 */
public class AlertPluginInstanceV2ControllerTest extends AbstractControllerTest {

    private static final int pluginDefineId = 1;
    private static final String instanceName = "instanceName";
    private static final String pluginInstanceParams = "pluginInstanceParams";
    private static final Result expectResponseContent = JSONUtils.parseObject(
            "{\"code\":0,\"msg\":\"success\",\"data\":\"Test Data\",\"success\":true,\"failed\":false}", Result.class);

    private static final Result result = new Result();

    @MockBean(name = "alertPluginInstanceServiceImpl")
    private AlertPluginInstanceService alertPluginInstanceService;

    @Before
    public void before() {
        LocaleContextHolder.setLocale(new Locale("en", ""));
        putMsg(result, Status.SUCCESS);
        result.setData("Test Data");
    }

    @Test
    public void testCreateAlertPluginInstance() throws Exception {
        // Given
        CreatePluginRequest request = CreatePluginRequest.builder()
                .id(pluginDefineId)
                .instanceName(instanceName)
                .pluginInstanceParams(pluginInstanceParams)
                .build();

        when(alertPluginInstanceService.create(any(User.class), eq(pluginDefineId), eq(instanceName),
                eq(pluginInstanceParams)))
                        .thenReturn(result);

        // When
        final MvcResult mvcResult = mockMvc.perform(post("/v2/alert-plugin-instances")
                .contentType(APPLICATION_JSON)
                .header(SESSION_ID, sessionId)
                .content(JSONUtils.toJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();

        // Then
        final Result actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(actualResponseContent.toString()).isEqualTo(expectResponseContent.toString());
    }

    @Test
    public void testUpdateAlertPluginInstance() throws Exception {
        // Given
        UpdatePluginRequest request = UpdatePluginRequest.builder()
                .instanceName(instanceName)
                .pluginInstanceParams(pluginInstanceParams)
                .build();

        when(alertPluginInstanceService.update(any(User.class), eq(pluginDefineId), eq(instanceName),
                eq(pluginInstanceParams)))
                        .thenReturn(result);

        // When
        final MvcResult mvcResult = mockMvc.perform(put("/v2/alert-plugin-instances/{id}", pluginDefineId)
                .contentType(APPLICATION_JSON)
                .header(SESSION_ID, sessionId)
                .content(JSONUtils.toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();

        // Then
        final Result actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(actualResponseContent.toString()).isEqualTo(expectResponseContent.toString());
    }

    @Test
    public void testDeleteAlertPluginInstance() throws Exception {
        // Given
        final MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pluginDefineId", String.valueOf(pluginDefineId));

        when(alertPluginInstanceService.delete(any(User.class), eq(pluginDefineId)))
                .thenReturn(result);

        // When
        final MvcResult mvcResult = mockMvc.perform(delete("/v2/alert-plugin-instances/{id}", pluginDefineId)
                .contentType(APPLICATION_JSON)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();

        // Then
        final Result actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(actualResponseContent.toString()).isEqualTo(expectResponseContent.toString());
    }

    @Test
    public void testGetAlertPluginInstance() throws Exception {
        // Given
        final MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pluginDefineId", String.valueOf(pluginDefineId));

        AlertPluginInstance instance = new AlertPluginInstance();
        result.setData(instance);

        when(alertPluginInstanceService.get(any(User.class), eq(pluginDefineId)))
                .thenReturn(result);

        // When
        final MvcResult mvcResult = mockMvc.perform(get("/v2/alert-plugin-instances/{id}", pluginDefineId)
                .contentType(APPLICATION_JSON)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();

        // Then
        final AlertPluginInstanceResponse actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), AlertPluginInstanceResponse.class);
        assertThat(actualResponseContent.getData()).isNotNull();
    }

    @Test
    public void testGetAlertPluginInstanceList() throws Exception {
        // Given
        List<AlertPluginInstance> list = new ArrayList<>();
        result.setData(list);

        when(alertPluginInstanceService.queryAll())
                .thenReturn(result);

        // When
        final MvcResult mvcResult = mockMvc.perform(get("/v2/alert-plugin-instances/list")
                .contentType(APPLICATION_JSON)
                .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();

        // Then
        final AlertPluginInstanceListResponse actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(),
                        AlertPluginInstanceListResponse.class);
        assertThat(actualResponseContent.getData()).isNotNull();
    }

    @Test
    public void testVerifyGroupName() throws Exception {
        // Given
        final MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pluginDefineId", String.valueOf(pluginDefineId));
        paramsMap.add("alertInstanceName", instanceName);

        when(alertPluginInstanceService.checkExistPluginInstanceName(eq(instanceName)))
                .thenReturn(false);

        Result expectResponseContent = JSONUtils.parseObject(
                "{\"code\":0,\"msg\":\"success\",\"data\":null,\"failed\":false,\"success\":true}", Result.class);

        // When
        final MvcResult mvcResult = mockMvc.perform(get("/v2/alert-plugin-instances/verify-name")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        final Result actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(actualResponseContent.toString()).isEqualTo(expectResponseContent.toString());
    }

    @Test
    public void testVerifyGroupNamePluginInstanceNameExist() throws Exception {
        // Given
        final MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pluginDefineId", String.valueOf(pluginDefineId));
        paramsMap.add("alertInstanceName", instanceName);

        when(alertPluginInstanceService.checkExistPluginInstanceName(eq(instanceName)))
                .thenReturn(true);

        Result expectResponseContent = JSONUtils.parseObject(
                "{\"code\":110010,\"msg\":\"plugin instance already exit\",\"data\":null,\"failed\":true,\"success\":false}",
                Result.class);

        // When
        final MvcResult mvcResult = mockMvc.perform(get("/v2/alert-plugin-instances/verify-name")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        final Result actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(actualResponseContent.toString()).isEqualTo(expectResponseContent.toString());
    }

    @Test
    public void testListPaging() throws Exception {
        // Given
        AlertPluginQueryRequest request = new AlertPluginQueryRequest();
        request.setSearchVal("searchVal");
        request.setPageNo(1);
        request.setPageSize(10);
        PageInfo<AlertPluginInstanceVO> pageInfo = new PageInfo();
        result.setData(pageInfo);

        when(alertPluginInstanceService.listPaging(eq(user), eq("searchVal"), eq(1), eq(10)))
                .thenReturn(result);

        // When
        final MvcResult mvcResult = mockMvc.perform(get("/v2/alert-plugin-instances")
                .header(SESSION_ID, sessionId)
                .contentType(APPLICATION_JSON)
                .content(JSONUtils.toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();

        // Then
        final AlertPluginInstanceListPagingResponse actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(),
                        AlertPluginInstanceListPagingResponse.class);
        assertThat(actualResponseContent.getData()).isNotNull();
    }

    @Test
    public void testListPagingResultFalse() throws Exception {
        // Given
        AlertPluginQueryRequest request = new AlertPluginQueryRequest();
        request.setSearchVal("searchVal");
        request.setPageNo(0);
        request.setPageSize(0);

        when(alertPluginInstanceService.listPaging(eq(user), eq("searchVal"), eq(0), eq(0)))
                .thenReturn(result);

        // When
        final MvcResult mvcResult = mockMvc.perform(get("/v2/alert-plugin-instances")
                .header(SESSION_ID, sessionId)
                .contentType(APPLICATION_JSON)
                .content(JSONUtils.toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();

        // Then
        final AlertPluginInstanceListPagingResponse actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(),
                        AlertPluginInstanceListPagingResponse.class);
        assertThat(actualResponseContent.getData()).isNull();
    }
}
