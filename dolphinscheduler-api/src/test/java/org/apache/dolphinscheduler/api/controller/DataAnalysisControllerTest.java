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
import org.apache.dolphinscheduler.api.vo.TaskInstanceCountVO;
import org.apache.dolphinscheduler.api.vo.WorkflowInstanceCountVO;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * data analysis controller test
 */
public class DataAnalysisControllerTest extends AbstractControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(DataAnalysisControllerTest.class);

    @Autowired
    private ProjectMapper projectMapper;

    private int createProject() {
        Project project = new Project();
        project.setCode(16L);
        project.setName("ut project");
        project.setUserId(1);
        project.setCreateTime(new Date());
        projectMapper.insert(project);
        return project.getId();
    }

    @Test
    public void testGetTaskInstanceStateCount() throws Exception {
        int projectId = createProject();

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("startDate", "2019-12-01 00:00:00");
        paramsMap.add("endDate", "2019-12-28 00:00:00");
        paramsMap.add("projectCode", "16");
        MvcResult mvcResult = mockMvc.perform(get("/projects/analysis/task-state-count")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result<TaskInstanceCountVO> result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(),
                new TypeReference<Result<TaskInstanceCountVO>>() {
                });
        assertThat(result.getCode())
                .isNotNull()
                .isEqualTo(Status.SUCCESS.getCode());
        projectMapper.deleteById(projectId);
    }

    @Test
    public void testGetWorkflowInstanceStateCount() throws Exception {
        int projectId = createProject();

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("startDate", "2019-12-01 00:00:00");
        paramsMap.add("endDate", "2019-12-28 00:00:00");
        paramsMap.add("projectCode", "16");

        MvcResult mvcResult = mockMvc.perform(get("/projects/analysis/process-state-count")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result<WorkflowInstanceCountVO> result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(),
                new TypeReference<Result<WorkflowInstanceCountVO>>() {
                });
        assertThat(result.getCode())
                .isEqualTo(Status.SUCCESS.getCode());

        projectMapper.deleteById(projectId);
    }

    @Test
    public void testCountDefinitionByUser() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("projectId", "16");

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

    @Test
    public void testListCommand() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("projectCode", "16");
        paramsMap.add("pageNo", "1");
        paramsMap.add("pageSize", "10");

        MvcResult mvcResult = mockMvc.perform(get("/projects/analysis/listCommand")
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
    public void testListErrorCommand() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("projectCode", "16");
        paramsMap.add("pageNo", "1");
        paramsMap.add("pageSize", "10");

        MvcResult mvcResult = mockMvc.perform(get("/projects/analysis/listErrorCommand")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        assertThat(result.getCode().intValue()).isEqualTo(Status.SUCCESS.getCode());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
