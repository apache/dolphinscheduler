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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ResourcesService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.resources.FetchFileContentResponse;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

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

import com.fasterxml.jackson.core.type.TypeReference;

public class ResourcesControllerTest extends AbstractControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(ResourcesControllerTest.class);

    @MockBean(name = "resourcesServiceImpl")
    private ResourcesService resourcesService;

    @Test
    public void testQueryResourceListPaging() throws Exception {
        Result mockResult = new Result<>();
        mockResult.setCode(Status.SUCCESS.getCode());
        // Mockito.when(resourcesService.pagingResourceItem()
        // .thenReturn(mockResult);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("type", String.valueOf(ResourceType.FILE));
        paramsMap.add("id", "123");
        paramsMap.add("pageNo", "1");
        paramsMap.add("searchVal", "test");
        paramsMap.add("pageSize", "1");
        paramsMap.add("fullName", "dolphinscheduler/resourcePath");
        paramsMap.add("tenantCode", "123");

        MvcResult mvcResult = mockMvc.perform(get("/resources")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testViewResource() throws Exception {
        FetchFileContentResponse fetchFileContentResponse = FetchFileContentResponse.builder()
                .content("echo hello")
                .build();
        Mockito.when(resourcesService.fetchResourceFileContent(Mockito.any()))
                .thenReturn(fetchFileContentResponse);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("skipLineNum", "2");
        paramsMap.add("limit", "100");
        paramsMap.add("fullName", "dolphinscheduler/resourcePath");
        paramsMap.add("tenantCode", "123");

        MvcResult mvcResult = mockMvc.perform(get("/resources/view")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result<FetchFileContentResponse> result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(),
                new TypeReference<Result<FetchFileContentResponse>>() {
                });

        assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        assertEquals(fetchFileContentResponse, result.getData());
    }

    @Test
    public void testCreateResourceFile() throws Exception {
        Mockito.doNothing().when(resourcesService).createFileFromContent(Mockito.any());

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("type", String.valueOf(ResourceType.FILE));
        paramsMap.add("fileName", "test_file_1");
        paramsMap.add("suffix", "sh");
        paramsMap.add("description", "test");
        paramsMap.add("content", "echo 1111");
        paramsMap.add("pid", "123");
        paramsMap.add("currentDir", "/xx");

        MvcResult mvcResult = mockMvc.perform(post("/resources/online-create")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result<Void> result =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), new TypeReference<Result<Void>>() {
                });

        assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testUpdateResourceContent() throws Exception {
        Mockito.doNothing().when(resourcesService).updateFileFromContent(Mockito.any());

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id", "1");
        paramsMap.add("content", "echo test_1111");
        paramsMap.add("fullName", "dolphinscheduler/resourcePath");
        paramsMap.add("tenantCode", "123");

        MvcResult mvcResult = mockMvc.perform(put("/resources/update-content")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result<Void> result =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), new TypeReference<Result<Void>>() {
                });

        assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testDownloadResource() throws Exception {

        Mockito.doNothing().when(resourcesService).downloadResource(Mockito.any(), Mockito.any());

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("fullName", "dolphinscheduler/resourcePath");

        MvcResult mvcResult = mockMvc.perform(get("/resources/download")
                .params(paramsMap)
                .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertNotNull(mvcResult);
    }

    @Test
    public void testDeleteResource() throws Exception {
        Mockito.doNothing().when(resourcesService).delete(Mockito.any());
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("fullName", "dolphinscheduler/resourcePath");
        paramsMap.add("tenantCode", "123");
        MvcResult mvcResult = mockMvc.perform(delete("/resources")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result<Void> result =
                JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), new TypeReference<Result<Void>>() {
                });

        assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }
}
