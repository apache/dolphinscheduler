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

import org.apache.dolphinscheduler.api.dto.resources.CreateResourceRequest;
import org.apache.dolphinscheduler.api.dto.resources.CreateUdfRequest;
import org.apache.dolphinscheduler.api.dto.resources.UpdateResourceContentRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ResourcesService;
import org.apache.dolphinscheduler.api.service.UdfFuncService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.Maps;

public class ResourcesV2ControllerTest extends AbstractControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(ResourcesV2ControllerTest.class);

    @MockBean(name = "resourcesServiceImpl")
    private ResourcesService resourcesService;

    @MockBean(name = "udfFuncServiceImpl")
    private UdfFuncService udfFuncService;

    @Test
    public void testQueryResourceList() throws Exception {
        Map<String, Object> mockResult = Maps.newHashMap();
        putMsg(mockResult, Status.SUCCESS);
        PowerMockito.when(resourcesService.queryResourceList(Mockito.any(), Mockito.any())).thenReturn(mockResult);

        MvcResult mvcResult = mockMvc.perform(get("/v2/resources/list")
                .header(SESSION_ID, sessionId)
                .param("type", ResourceType.FILE.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryResourceListPaging() throws Exception {
        Result mockResult = new Result<>();
        mockResult.setCode(Status.SUCCESS.getCode());
        PowerMockito.when(resourcesService.queryResourceListPaging(
                Mockito.any(), Mockito.anyInt(), Mockito.any(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt()))
                .thenReturn(mockResult);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("type", String.valueOf(ResourceType.FILE));
        paramsMap.add("id", "123");
        paramsMap.add("pageNo", "1");
        paramsMap.add("searchVal", "test");
        paramsMap.add("pageSize", "1");

        MvcResult mvcResult = mockMvc.perform(get("/v2/resources")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testVerifyResourceName() throws Exception {
        Result mockResult = new Result<>();
        mockResult.setCode(Status.TENANT_NOT_EXIST.getCode());
        PowerMockito.when(resourcesService.verifyResourceName(Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(mockResult);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("fullName", "list_resources_1.sh");
        paramsMap.add("type", "FILE");

        MvcResult mvcResult = mockMvc.perform(get("/v2/resources/verify-name")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.TENANT_NOT_EXIST.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testViewResource() throws Exception {
        Result<Object> mockResult = new Result<>();
        mockResult.setCode(Status.HDFS_NOT_STARTUP.getCode());
        PowerMockito.when(
                resourcesService.readResource(Mockito.any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(mockResult);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("skipLineNum", "2");
        paramsMap.add("limit", "100");

        MvcResult mvcResult = mockMvc.perform(get("/v2/resources/{id}/view", "5")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.HDFS_NOT_STARTUP.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testOnlineCreateResource() throws Exception {
        Result<Object> mockResult = new Result<>();
        mockResult.setCode(Status.TENANT_NOT_EXIST.getCode());
        PowerMockito.when(resourcesService
                .onlineCreateResource(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(mockResult);

        CreateResourceRequest createResourceRequest = new CreateResourceRequest();
        createResourceRequest.setType(ResourceType.FILE);
        createResourceRequest.setFileName("test_file_1");
        createResourceRequest.setSuffix("sh");
        createResourceRequest.setDescription("test");
        createResourceRequest.setContent("echo 1111");
        createResourceRequest.setPid(123);
        createResourceRequest.setCurrentDir("/xx");
        MvcResult mvcResult = mockMvc.perform(post("/v2/resources/online-create")
                .header(SESSION_ID, sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtils.toJsonString(createResourceRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.TENANT_NOT_EXIST.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testUpdateResourceContent() throws Exception {
        Result mockResult = new Result<>();
        mockResult.setCode(Status.TENANT_NOT_EXIST.getCode());
        PowerMockito.when(resourcesService.updateResourceContent(Mockito.any(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(mockResult);

        UpdateResourceContentRequest request = new UpdateResourceContentRequest();
        request.setContent("echo test_1111");
        MvcResult mvcResult = mockMvc.perform(put("/v2/resources/1/update-content")
                .header(SESSION_ID, sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtils.toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.TENANT_NOT_EXIST.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testDownloadResource() throws Exception {

        PowerMockito.when(resourcesService.downloadResource(Mockito.any(), Mockito.anyInt())).thenReturn(null);

        MvcResult mvcResult = mockMvc.perform(get("/v2/resources/{id}/download", 5)
                .header(SESSION_ID, sessionId))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andReturn();

        Assert.assertNotNull(mvcResult);
    }

    @Test
    public void testCreateUdfFunc() throws Exception {
        Result mockResult = new Result<>();
        mockResult.setCode(Status.TENANT_NOT_EXIST.getCode());
        PowerMockito.when(udfFuncService
                .createUdfFunction(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyInt()))
                .thenReturn(mockResult);

        CreateUdfRequest createUdfRequest = new CreateUdfRequest();
        createUdfRequest.setType(UdfType.HIVE);
        createUdfRequest.setFuncName("test_udf");
        createUdfRequest.setClassName("com.test.word.contWord");
        createUdfRequest.setArgTypes("argTypes");
        createUdfRequest.setDatabase("database");
        createUdfRequest.setDescription("description");
        MvcResult mvcResult = mockMvc.perform(post("/v2/resources/{resourceId}/udf-func", "123")
                .header(SESSION_ID, sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtils.toJsonString(createUdfRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.TENANT_NOT_EXIST.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testViewUIUdfFunction() throws Exception {
        Result<Object> mockResult = new Result<>();
        putMsg(mockResult, Status.TENANT_NOT_EXIST);
        PowerMockito.when(udfFuncService
                .queryUdfFuncDetail(Mockito.any(), Mockito.anyInt()))
                .thenReturn(mockResult);

        MvcResult mvcResult = mockMvc.perform(get("/v2/resources/{id}/udf-func", "123")
                .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.TENANT_NOT_EXIST.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testUpdateUdfFunc() throws Exception {
        Result<Object> mockResult = new Result<>();
        mockResult.setCode(Status.TENANT_NOT_EXIST.getCode());
        PowerMockito.when(udfFuncService
                .updateUdfFunc(Mockito.any(), Mockito.anyInt(),
                        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString(), Mockito.any(),
                        Mockito.anyInt()))
                .thenReturn(mockResult);

        CreateUdfRequest createUdfRequest = new CreateUdfRequest();
        createUdfRequest.setType(UdfType.HIVE);
        createUdfRequest.setFuncName("test_udf");
        createUdfRequest.setClassName("com.test.word.contWord");
        createUdfRequest.setArgTypes("argTypes");
        createUdfRequest.setDatabase("database");
        createUdfRequest.setDescription("description");

        MvcResult mvcResult = mockMvc.perform(put("/v2/resources/{resourceId}/udf-func/{id}", "123", "456")
                .header(SESSION_ID, sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtils.toJsonString(createUdfRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.TENANT_NOT_EXIST.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryUdfFuncList() throws Exception {
        Result mockResult = new Result<>();
        mockResult.setCode(Status.SUCCESS.getCode());
        PowerMockito.when(udfFuncService.queryUdfFuncListPaging(Mockito.any(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt())).thenReturn(mockResult);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pageNo", "1");
        paramsMap.add("searchVal", "udf");
        paramsMap.add("pageSize", "1");

        MvcResult mvcResult = mockMvc.perform(get("/v2/resources/udf-func")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryUdfList() throws Exception {
        Result<Object> mockResult = new Result<>();
        mockResult.setCode(Status.SUCCESS.getCode());
        PowerMockito.when(udfFuncService.queryUdfFuncList(Mockito.any(), Mockito.anyInt())).thenReturn(mockResult);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("type", String.valueOf(UdfType.HIVE));

        MvcResult mvcResult = mockMvc.perform(get("/v2/resources/udf-func/list")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testVerifyUdfFuncName() throws Exception {
        Result mockResult = new Result<>();
        mockResult.setCode(Status.SUCCESS.getCode());
        PowerMockito.when(udfFuncService.verifyUdfFuncByName(Mockito.any(), Mockito.anyString()))
                .thenReturn(mockResult);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("name", "test");

        MvcResult mvcResult = mockMvc.perform(get("/v2/resources/udf-func/verify-name")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testAuthorizedFile() throws Exception {
        Map<String, Object> mockResult = Maps.newHashMap();
        putMsg(mockResult, Status.SUCCESS);
        PowerMockito.when(resourcesService.authorizedFile(Mockito.any(), Mockito.anyInt())).thenReturn(mockResult);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId", "2");

        MvcResult mvcResult = mockMvc.perform(get("/v2/resources/authed-file")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testAuthorizedUDFFunction() throws Exception {
        Map<String, Object> mockResult = Maps.newHashMap();
        putMsg(mockResult, Status.SUCCESS);
        PowerMockito.when(resourcesService.authorizedUDFFunction(Mockito.any(), Mockito.anyInt()))
                .thenReturn(mockResult);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId", "2");

        MvcResult mvcResult = mockMvc.perform(get("/v2/resources/authed-udf-func")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testUnauthUDFFunc() throws Exception {
        Map<String, Object> mockResult = Maps.newHashMap();
        putMsg(mockResult, Status.SUCCESS);
        PowerMockito.when(resourcesService.unauthorizedUDFFunction(Mockito.any(), Mockito.anyInt()))
                .thenReturn(mockResult);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId", "2");

        MvcResult mvcResult = mockMvc.perform(get("/v2/resources/unauth-udf-func")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testDeleteUdfFunc() throws Exception {
        Result mockResult = new Result<>();
        mockResult.setCode(Status.SUCCESS.getCode());
        PowerMockito.when(udfFuncService.delete(Mockito.any(), Mockito.anyInt())).thenReturn(mockResult);

        MvcResult mvcResult = mockMvc.perform(delete("/v2/resources/udf-func/{id}", "123")
                .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testDeleteResource() throws Exception {
        Result mockResult = new Result<>();
        mockResult.setCode(Status.SUCCESS.getCode());
        PowerMockito.when(resourcesService.delete(Mockito.any(), Mockito.anyInt())).thenReturn(mockResult);

        MvcResult mvcResult = mockMvc.perform(delete("/v2/resources/{id}", "123")
                .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
