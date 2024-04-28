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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.UiPluginService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.PluginType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * ui plugin controller test
 */
public class UiPluginControllerTest extends AbstractControllerTest {

    private static final PluginType pluginType = PluginType.ALERT;
    private static final int pluginId = 1;
    private static final Result expectResponseContent = JSONUtils.parseObject(
            "{\"code\":0,\"msg\":\"success\",\"data\":\"Test Data\",\"success\":true,\"failed\":false}", Result.class);
    private static final ImmutableMap<String, Object> uiPluginServiceResult =
            ImmutableMap.of(Constants.STATUS, Status.SUCCESS, Constants.DATA_LIST, "Test Data");

    private static final Logger logger = LoggerFactory.getLogger(TenantControllerTest.class);

    @MockBean(name = "uiPluginService")
    private UiPluginService uiPluginService;

    @Test
    public void testQueryUiPluginsByType() throws Exception {
        when(uiPluginService.queryUiPluginsByType(any(PluginType.class)))
                .thenReturn(uiPluginServiceResult);

        final MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pluginType", String.valueOf(pluginType));

        final MvcResult mvcResult = mockMvc.perform(get("/ui-plugins/query-by-type")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        final Result actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(actualResponseContent.toString()).isEqualTo(expectResponseContent.toString());
    }

    @Test
    public void testQueryUiPluginDetailById() throws Exception {
        when(uiPluginService.queryUiPluginDetailById(anyInt()))
                .thenReturn(uiPluginServiceResult);

        final MvcResult mvcResult = mockMvc.perform(get("/ui-plugins/{id}", pluginId)
                .header(SESSION_ID, sessionId))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        final Result actualResponseContent =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(actualResponseContent.toString()).isEqualTo(expectResponseContent.toString());
    }

    @Test
    public void testQueryProductInfo() throws Exception {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put(Constants.STATUS, Status.SUCCESS);
        Mockito.when(uiPluginService.queryProductInfo(Mockito.any(), Mockito.anyInt())).thenReturn(mockResult);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId", "1");

        MvcResult mvcResult = mockMvc.perform(post("/ui-plugins/queryProductInfo")
                        .header(SESSION_ID, sessionId)
                        .params(paramsMap))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    private User getLoginUser() {
        User user = new User();
        user.setId(1);
        user.setUserName("admin");
        return user;
    }
}
