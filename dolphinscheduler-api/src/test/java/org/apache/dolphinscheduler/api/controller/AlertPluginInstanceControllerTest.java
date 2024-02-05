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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.service.AlertPluginInstanceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.AlertPluginInstanceType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * alert plugin instance controller test
 */
public class AlertPluginInstanceControllerTest extends AbstractControllerTest {

    private static final int pluginDefineId = 1;
    private static final String instanceName = "instanceName";
    private static final String pluginInstanceParams = "pluginInstanceParams";
    private static final AlertPluginInstanceType pluginInstanceType = AlertPluginInstanceType.NORMAL;
    private static final WarningType warningType = WarningType.ALL;
    private static final Result expectResponseContent = JSONUtils.parseObject(
            "{\"code\":0,\"msg\":\"success\",\"data\":\"null\",\"success\":true,\"failed\":false}", Result.class);

    @MockBean(name = "alertPluginInstanceServiceImpl")
    private AlertPluginInstanceService alertPluginInstanceService;

    @Test
    public void testCreateAlertPluginInstance() throws Exception {
        // Given
        final MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pluginDefineId", String.valueOf(pluginDefineId));
        paramsMap.add("instanceName", instanceName);
        paramsMap.add("instanceType", pluginInstanceType.getDescp());
        paramsMap.add("warningType", warningType.name());
        paramsMap.add("pluginInstanceParams", pluginInstanceParams);

        when(alertPluginInstanceService.create(any(User.class), eq(pluginDefineId), eq(instanceName),
                eq(pluginInstanceType), eq(warningType), eq(pluginInstanceParams))).thenReturn(null);

        // When
        final MvcResult mvcResult = mockMvc.perform(post("/alert-plugin-instances")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        final Result actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(actualResponseContent.getMsg()).isEqualTo(expectResponseContent.getMsg());
        assertThat(actualResponseContent.getCode()).isEqualTo(expectResponseContent.getCode());
    }

    @Test
    public void testSendAlertPluginInstance() throws Exception {
        // Given
        final MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pluginDefineId", String.valueOf(pluginDefineId));
        paramsMap.add("pluginInstanceParams", pluginInstanceParams);

        doNothing().when(alertPluginInstanceService).testSend(eq(pluginDefineId), eq(pluginInstanceParams));

        // When
        final MvcResult mvcResult = mockMvc.perform(post("/alert-plugin-instances/test-send")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        final Result actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(actualResponseContent.getMsg()).isEqualTo(expectResponseContent.getMsg());
        assertThat(actualResponseContent.getCode()).isEqualTo(expectResponseContent.getCode());
    }

    @Test
    public void testUpdateAlertPluginInstance() throws Exception {
        // Given
        final MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pluginDefineId", String.valueOf(pluginDefineId));
        paramsMap.add("instanceName", instanceName);
        paramsMap.add("warningType", warningType.name());
        paramsMap.add("pluginInstanceParams", pluginInstanceParams);

        when(alertPluginInstanceService.updateById(any(User.class), eq(pluginDefineId), eq(instanceName),
                eq(warningType), eq(pluginInstanceParams))).thenReturn(null);

        // When
        final MvcResult mvcResult = mockMvc.perform(put("/alert-plugin-instances/{id}", pluginDefineId)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        final Result actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(actualResponseContent.getMsg()).isEqualTo(expectResponseContent.getMsg());
        assertThat(actualResponseContent.getCode()).isEqualTo(expectResponseContent.getCode());
    }

    @Test
    public void testDeleteAlertPluginInstance() throws Exception {
        // Given
        final MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pluginDefineId", String.valueOf(pluginDefineId));
        paramsMap.add("instanceName", instanceName);
        paramsMap.add("pluginInstanceParams", pluginInstanceParams);

        doNothing().when(alertPluginInstanceService).deleteById(any(User.class), eq(pluginDefineId));

        // When
        final MvcResult mvcResult = mockMvc.perform(delete("/alert-plugin-instances/{id}", pluginDefineId)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        final Result actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(actualResponseContent.getMsg()).isEqualTo(expectResponseContent.getMsg());
        assertThat(actualResponseContent.getCode()).isEqualTo(expectResponseContent.getCode());
    }

    @Test
    public void testGetAlertPluginInstance() throws Exception {
        // Given
        final MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pluginDefineId", String.valueOf(pluginDefineId));

        when(alertPluginInstanceService.getById(any(User.class), eq(pluginDefineId))).thenReturn(null);

        // When
        final MvcResult mvcResult = mockMvc.perform(get("/alert-plugin-instances/{id}", pluginDefineId)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        final Result actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(actualResponseContent.getMsg()).isEqualTo(expectResponseContent.getMsg());
        assertThat(actualResponseContent.getCode()).isEqualTo(expectResponseContent.getCode());
    }

    @Test
    public void testGetAlertPluginInstanceList() throws Exception {
        // Given
        when(alertPluginInstanceService.queryAll()).thenReturn(null);

        // When
        final MvcResult mvcResult = mockMvc.perform(get("/alert-plugin-instances/list")
                .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        final Result actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(actualResponseContent.getMsg()).isEqualTo(expectResponseContent.getMsg());
        assertThat(actualResponseContent.getCode()).isEqualTo(expectResponseContent.getCode());
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
        final MvcResult mvcResult = mockMvc.perform(get("/alert-plugin-instances/verify-name")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        final Result actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(actualResponseContent.getMsg()).isEqualTo(expectResponseContent.getMsg());
        assertThat(actualResponseContent.getCode()).isEqualTo(expectResponseContent.getCode());
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
                "{\"code\":110010,\"msg\":\"plugin instance already exists\",\"data\":null,\"failed\":true,\"success\":false}",
                Result.class);

        // When
        final MvcResult mvcResult = mockMvc.perform(get("/alert-plugin-instances/verify-name")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        final Result actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(actualResponseContent.getMsg()).isEqualTo(expectResponseContent.getMsg());
        assertThat(actualResponseContent.getCode()).isEqualTo(expectResponseContent.getCode());
    }

    @Test
    public void testListPaging() throws Exception {
        // Given
        final MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pluginDefineId", String.valueOf(pluginDefineId));
        paramsMap.add("searchVal", "searchVal");
        paramsMap.add("pageNo", String.valueOf(1));
        paramsMap.add("pageSize", String.valueOf(10));

        when(alertPluginInstanceService.listPaging(eq(user), eq("searchVal"), eq(1), eq(10)))
                .thenReturn(PageInfo.of(1, 10));

        // When
        final MvcResult mvcResult = mockMvc.perform(get("/alert-plugin-instances")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        final Result actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(actualResponseContent.getMsg()).isEqualTo(expectResponseContent.getMsg());
        assertThat(actualResponseContent.getCode()).isEqualTo(expectResponseContent.getCode());
    }

    @Test
    public void testListPagingResultFalse() throws Exception {
        // Given
        final Result expectResponseContent = JSONUtils.parseObject(
                "{\"code\":10001,\"msg\":\"request parameter pageNo is not valid\",\"data\":null,\"success\":false,\"failed\":true}",
                Result.class);

        final MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pluginDefineId", String.valueOf(pluginDefineId));
        paramsMap.add("searchVal", "searchVal");
        paramsMap.add("pageNo", String.valueOf(0));
        paramsMap.add("pageSize", String.valueOf(0));

        // When
        final MvcResult mvcResult = mockMvc.perform(get("/alert-plugin-instances")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        final Result actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(actualResponseContent.getMsg()).isEqualTo(expectResponseContent.getMsg());
        assertThat(actualResponseContent.getCode()).isEqualTo(expectResponseContent.getCode());
    }
}
