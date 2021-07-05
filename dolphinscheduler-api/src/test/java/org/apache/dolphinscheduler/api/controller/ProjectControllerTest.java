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
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * project controller test
 */
public class ProjectControllerTest extends AbstractControllerTest {

    private static Logger logger = LoggerFactory.getLogger(ProjectControllerTest.class);

    private String projectId;

    @Before
    public void before() throws Exception {
        projectId = testCreateProject("project_test1", "the test project");
    }

    @After
    public void after() throws Exception {
        testDeleteProject(projectId);
    }

    private String testCreateProject(String projectName, String description) throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("projectName",projectName);
        paramsMap.add("description",description);

        MvcResult mvcResult = mockMvc.perform(post("/projects/create")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), new TypeReference<Result<String>>() {});
        Assert.assertTrue(result != null && result.isSuccess());
        Assert.assertNotNull(result.getData());
        logger.info("create project return result:{}", mvcResult.getResponse().getContentAsString());

        return (String)result.getData();
    }

    @Test
    public void testUpdateProject() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("projectId", projectId);
        paramsMap.add("projectName","project_test_update");
        paramsMap.add("desc","the test project update");
        paramsMap.add("userName", "the project owner");

        MvcResult mvcResult = mockMvc.perform(post("/projects/update")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertTrue(result != null && result.isStatus(Status.USER_NOT_EXIST));
        logger.info("update project return result:{}", mvcResult.getResponse().getContentAsString());

    }

    @Test
    public void testQueryProjectById() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("projectId", projectId);

        MvcResult mvcResult = mockMvc.perform(get("/projects/query-by-id")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertTrue(result != null && result.isSuccess());
        logger.info(mvcResult.getResponse().getContentAsString());
        logger.info("query project by id :{}, return result:{}", projectId, mvcResult.getResponse().getContentAsString());

    }

    @Test
    public void testQueryProjectListPaging() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("searchVal","test");
        paramsMap.add("pageSize","2");
        paramsMap.add("pageNo","2");

        MvcResult mvcResult = mockMvc.perform(get("/projects/list-paging")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertTrue(result != null && result.isSuccess());
        logger.info("query list-paging project return result:{}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryUnauthorizedProject() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId","2");

        MvcResult mvcResult = mockMvc.perform(get("/projects/unauth-project")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertTrue(result != null && result.isSuccess());
        logger.info("query unauth project return result:{}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryAuthorizedProject() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId",String.valueOf(user.getId()));

        MvcResult mvcResult = mockMvc.perform(get("/projects/authed-project")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertTrue(result != null && result.isSuccess());
        logger.info("query authed project return result:{}", mvcResult.getResponse().getContentAsString());

    }

    @Test
    public void testQueryAllProjectList() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();

        MvcResult mvcResult = mockMvc.perform(get("/projects/query-project-list")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertTrue(result != null && result.isSuccess());
        logger.info("query all project return result:{}", mvcResult.getResponse().getContentAsString());

    }

    @Ignore
    @Test
    public void testImportProcessDefinition() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("file","test");

        MvcResult mvcResult = mockMvc.perform(post("/projects/import-definition")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertTrue(result != null && result.isStatus(Status.IMPORT_PROCESS_DEFINE_ERROR));
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    private void testDeleteProject(String projectId) throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("projectId", projectId);

        MvcResult mvcResult = mockMvc.perform(get("/projects/delete")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertTrue(result != null && result.isSuccess());
        logger.info("delete project return result:{}", mvcResult.getResponse().getContentAsString());
    }

}
