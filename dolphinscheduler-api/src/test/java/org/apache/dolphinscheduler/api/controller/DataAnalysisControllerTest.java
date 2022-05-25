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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;

import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * data analysis controller test
 */
public class DataAnalysisControllerTest extends AbstractControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(DataAnalysisControllerTest.class);

    @MockBean(name = "projectMapper")
    private ProjectMapper projectMapper;

    @Test
    public void testCountTaskState() throws Exception {
        PowerMockito.when(projectMapper.queryByCode(Mockito.anyLong())).thenReturn(getProject("test"));

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("startDate","2019-12-01 00:00:00");
        paramsMap.add("endDate","2019-12-28 00:00:00");
        paramsMap.add("projectCode","16");

        MvcResult mvcResult = mockMvc.perform(get("/projects/analysis/task-state-count")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(result.getCode().intValue()).isEqualTo(Status.SUCCESS.getCode());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testCountProcessInstanceState() throws Exception {
        PowerMockito.when(projectMapper.queryByCode(Mockito.anyLong())).thenReturn(getProject("test"));

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("startDate","2019-12-01 00:00:00");
        paramsMap.add("endDate","2019-12-28 00:00:00");
        paramsMap.add("projectCode","16");

        MvcResult mvcResult = mockMvc.perform(get("/projects/analysis/process-state-count")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(result.getCode().intValue()).isEqualTo(Status.SUCCESS.getCode());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testCountDefinitionByUser() throws Exception {
        PowerMockito.when(projectMapper.queryByCode(Mockito.anyLong())).thenReturn(getProject("test"));

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("projectId","16");

        MvcResult mvcResult = mockMvc.perform(get("/projects/analysis/define-user-count")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(result.getCode().intValue()).isEqualTo(Status.SUCCESS.getCode());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testCountCommandState() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/projects/analysis/command-state-count")
                .header("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(result.getCode().intValue()).isEqualTo(Status.SUCCESS.getCode());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testCountQueueState() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/projects/analysis/queue-count")
                .header("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(result.getCode().intValue()).isEqualTo(Status.SUCCESS.getCode());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    /**
     * get mock Project
     *
     * @param projectName projectName
     * @return Project
     */
    private Project getProject(String projectName) {
        Project project = new Project();
        project.setCode(11L);
        project.setId(1);
        project.setName(projectName);
        project.setUserId(1);
        return project;
    }
}
