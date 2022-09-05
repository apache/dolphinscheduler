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
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.dto.dataAnalysis.CommandStateCountResponse;
import org.apache.dolphinscheduler.api.dto.dataAnalysis.ProcessDefinitionStateCountRequest;
import org.apache.dolphinscheduler.api.dto.dataAnalysis.ProcessDefinitionStateCountResponse;
import org.apache.dolphinscheduler.api.dto.dataAnalysis.ProcessInstanceStateCountRequest;
import org.apache.dolphinscheduler.api.dto.dataAnalysis.ProcessInstanceStateCountResponse;
import org.apache.dolphinscheduler.api.dto.dataAnalysis.QueueStateCountResponse;
import org.apache.dolphinscheduler.api.dto.dataAnalysis.TaskStateCountRequest;
import org.apache.dolphinscheduler.api.dto.dataAnalysis.TaskStateCountResponse;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;

import java.util.Date;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;


/**
 * data analysis controller test
 */
public class DataAnalysisV2ControllerTest extends AbstractControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(DataAnalysisV2ControllerTest.class);

    @Autowired
    private ProjectMapper projectMapper;

    private long createProject() {
        Project project = new Project();
        project.setCode(16L);
        project.setName("mock project");
        project.setUserId(1);
        project.setCreateTime(new Date());
        projectMapper.insert(project);
        return project.getCode();
    }

    private void cleanProjectByCode( long projectCode) {
        QueryWrapper<Project> projectWrapper = new QueryWrapper<>();
        projectWrapper.eq("code", projectCode);
        projectMapper.delete(projectWrapper);
    }

    @Test
    public void testCountTaskState() throws Exception {
        long projectCode = createProject();
        TaskStateCountRequest taskStateCountRequest = new TaskStateCountRequest();
        taskStateCountRequest.setProjectCode(projectCode);
        taskStateCountRequest.setStartDate("2019-12-01 00:00:00");
        taskStateCountRequest.setEndDate("2022-12-28 00:00:00");
        MvcResult mvcResult = mockMvc.perform(get("/v2/projects/analysis/task-state-count")
                .header("sessionId", sessionId)
                .contentType(APPLICATION_JSON_VALUE)
                .content(JSONUtils.toJsonString(taskStateCountRequest)))
                .andExpect(status().isOk())
                //.andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        TaskStateCountResponse result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), TaskStateCountResponse.class);
        assertThat(result.getCode().intValue()).isEqualTo(Status.SUCCESS.getCode());
        logger.info(mvcResult.getResponse().getContentAsString());
        cleanProjectByCode(projectCode);
    }

    @Test
    public void testCountProcessInstanceState() throws Exception {
        long projectCode = createProject();
        ProcessInstanceStateCountRequest processInstanceStateCountRequest = new ProcessInstanceStateCountRequest();
        processInstanceStateCountRequest.setProjectCode(projectCode);
        processInstanceStateCountRequest.setStartDate("2019-12-01 00:00:00");
        processInstanceStateCountRequest.setEndDate("2022-12-28 00:00:00");

        MvcResult mvcResult = mockMvc.perform(get("/v2/projects/analysis/process-state-count")
                .header("sessionId", sessionId)
                .contentType(APPLICATION_JSON_VALUE)
                .content(JSONUtils.toJsonString(processInstanceStateCountRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        ProcessInstanceStateCountResponse result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), ProcessInstanceStateCountResponse.class);
        assertThat(result.getCode().intValue()).isEqualTo(Status.SUCCESS.getCode());
        logger.info(mvcResult.getResponse().getContentAsString());
        cleanProjectByCode(projectCode);
    }

    @Test
    public void testCountDefinitionByUser() throws Exception {
        long projectCode = createProject();
        ProcessDefinitionStateCountRequest processDefinitionStateCountRequest = new ProcessDefinitionStateCountRequest();
        processDefinitionStateCountRequest.setProjectCode(projectCode);

        MvcResult mvcResult = mockMvc.perform(get("/v2/projects/analysis/define-user-count")
                .header("sessionId", sessionId)
                .contentType(APPLICATION_JSON_VALUE)
                .content(JSONUtils.toJsonString(processDefinitionStateCountRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        ProcessDefinitionStateCountResponse result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), ProcessDefinitionStateCountResponse.class);
        assertThat(result.getCode().intValue()).isEqualTo(Status.SUCCESS.getCode());
        logger.info(mvcResult.getResponse().getContentAsString());
        cleanProjectByCode(projectCode);
    }

    @Test
    public void testCountCommandState() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/v2/projects/analysis/command-state-count")
                .header("sessionId", sessionId)
                .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        CommandStateCountResponse result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), CommandStateCountResponse.class);
        assertThat(result.getCode().intValue()).isEqualTo(Status.SUCCESS.getCode());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testCountQueueState() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/v2/projects/analysis/queue-count")
                .header("sessionId", sessionId)
                .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        QueueStateCountResponse result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), QueueStateCountResponse.class);
        assertThat(result.getCode().intValue()).isEqualTo(Status.SUCCESS.getCode());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

}
