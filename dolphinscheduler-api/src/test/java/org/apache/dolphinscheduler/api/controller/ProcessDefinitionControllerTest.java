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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * process definition controller test
 */
public class ProcessDefinitionControllerTest extends AbstractControllerTest{

    @Test
    public void testCreateProcessDefinition() throws Exception {
        String json = "{\"globalParams\":[],\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-36196\",\"name\""
                + ":\"ssh_test1\",\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"aa=\\\"1234\\\"\\"
                + "necho ${aa}\"},\"desc\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\""
                + ",\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},"
                + "\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":-1,\"timeout\":0}";
        String locations = "{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}";
        String projectName = "test";
        String name = "dag_test";
        String description = "desc test";
        String connects = "[]";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("name", name);
        paramsMap.add("processDefinitionJson", json);
        paramsMap.add("locations", locations);
        paramsMap.add("connects", connects);
        paramsMap.add("description", description);

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/process/save", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testVerifyProcessDefinitionName() throws Exception {
        String projectName = "test";
        String name = "dag_test";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("name", name);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/verify-name", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());

    }

    @Test
    public void updateProcessDefinition() throws Exception {

        String json = "{\"globalParams\":[],\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-36196\",\"name\":\"ssh_test1\""
                + ",\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"aa=\\\"1234\\\"\\necho ${aa}\"}"
                + ",\"desc\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\",\"retryInterval\""
                + ":\"1\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},\"taskInstancePriority\""
                + ":\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":-1,\"timeout\":0}";
        String locations = "{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}";
        String projectName = "test";
        String name = "dag_test";
        String description = "desc test";
        String connects = "[]";
        String id = "1";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id", id);
        paramsMap.add("name", name);
        paramsMap.add("processDefinitionJson", json);
        paramsMap.add("locations", locations);
        paramsMap.add("connects", connects);
        paramsMap.add("description", description);
        paramsMap.add("releaseState", ReleaseState.OFFLINE.toString());

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/process/update", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testReleaseProcessDefinition() throws Exception {
        String projectName = "test";
        String id = "1";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processId", id);
        paramsMap.add("releaseState", ReleaseState.OFFLINE.toString());


        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/process/release", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testQueryProcessDefinitionById() throws Exception {

        String projectName = "test";
        String id = "1";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processId", id);
        paramsMap.add("releaseState", ReleaseState.OFFLINE.toString());

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/select-by-id", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testBatchCopyProcessDefinition() throws Exception {

        String projectName = "test";
        String targetProjectId = "2";
        String id = "1";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("targetProjectId", targetProjectId);
        paramsMap.add("processDefinitionIds", id);

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/process/copy", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testBatchMoveProcessDefinition() throws Exception {

        String projectName = "test";
        String targetProjectId = "2";
        String id = "1";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("targetProjectId", targetProjectId);
        paramsMap.add("processDefinitionIds", id);

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/process/move", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testQueryProcessDefinitionList() throws Exception {

        String projectName = "test";

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/list", projectName)
                .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testDeleteProcessDefinitionById() throws Exception {
        String projectName = "test";
        String id = "1";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionId", id);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/delete", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testGetNodeListByDefinitionId() throws Exception {
        String projectName = "test";
        String id = "1";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionId", id);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/gen-task-list", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testGetNodeListByDefinitionIdList() throws Exception {
        String projectName = "test";
        String idList = "1,2,3";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionIdList", idList);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/gen-task-list", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testQueryProcessDefinitionAllByProjectId() throws Exception {
        String projectName = "test";
        String projectId = "1";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("projectId", projectId);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/queryProcessDefinitionAllByProjectId", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testViewTree() throws Exception {
        String projectName = "test";
        String processId = "1";
        String limit = "2";
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processId", processId);
        paramsMap.add("limit", limit);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/view-tree", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testQueryProcessDefinitionListPaging() throws Exception {
        String projectName = "test";
        String pageNo = "1";
        String pageSize = "10";
        String searchVal = "";
        String userId = "1";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pageNo", pageNo);
        paramsMap.add("pageSize", pageSize);
        paramsMap.add("searchVal", searchVal);
        paramsMap.add("userId", userId);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/list-paging", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testBatchExportProcessDefinitionByIds() throws Exception {

        String processDefinitionIds = "1,2";
        String projectName = "test";
        HttpServletResponse response = new MockHttpServletResponse();
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionIds", processDefinitionIds);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/export", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Assert.assertNull(mvcResult);
    }

    @Test
    public void testQueryProcessDefinitionVersions() throws Exception {
        String projectName = "test";
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pageNo", "1");
        paramsMap.add("pageSize", "-10");
        paramsMap.add("processDefinitionId", "1");

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/versions", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), result.getCode().intValue());

        paramsMap.set("pageNo", "-1");
        paramsMap.set("pageSize", "10");
        mvcResult = mockMvc.perform(get("/projects/{projectName}/process/versions", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), result.getCode().intValue());

        paramsMap.set("pageNo", "1");
        paramsMap.set("pageSize", "10");
        mvcResult = mockMvc.perform(get("/projects/{projectName}/process/versions", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    @Test
    public void testSwitchProcessDefinitionVersion() throws Exception {
        String projectName = "test";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionId", "1");
        paramsMap.add("version", "10");

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/version/switch", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testDeleteProcessDefinitionVersion() throws Exception {
        String projectName = "test";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionId", "1");
        paramsMap.add("version", "10");

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process//version/delete", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

}
