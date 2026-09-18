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

import static org.apache.dolphinscheduler.api.AssertionsHelper.assertThrowsServiceException;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.TaskDefinitionService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.model.TaskWorkflowSearchResult;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class TaskWorkflowSearchControllerTest {

    @InjectMocks
    private TaskDefinitionController taskDefinitionController;

    @Mock
    private TaskDefinitionService taskDefinitionService;

    @Test
    public void testGetSearchEndpoint() throws Exception {
        User user = new User();
        TaskWorkflowSearchResult task = new TaskWorkflowSearchResult();
        task.setTaskCode(101L);
        task.setTaskName("orders");
        task.setTaskType("SHELL");
        task.setWorkflowDefinitionCode(201L);
        task.setWorkflowDefinitionName("orders_workflow");
        PageInfo<TaskWorkflowSearchResult> page = new PageInfo<>(1, 10);
        page.setTotal(1);
        page.setTotalList(Collections.singletonList(task));
        when(taskDefinitionService.searchTaskWorkflows(user, 1L, "orders", 1, 10)).thenReturn(page);

        MockMvcBuilders.standaloneSetup(taskDefinitionController).build()
                .perform(get("/projects/1/task-definition/search")
                        .accept(MediaType.APPLICATION_JSON)
                        .requestAttr(Constants.SESSION_USER, user)
                        .param("searchVal", "orders").param("pageNo", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Status.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.totalList[0].taskName").value("orders"))
                .andExpect(jsonPath("$.data.totalList[0].workflowDefinitionCode").value(201L));
    }

    @Test
    public void testRejectsInvalidPageParameters() {
        User user = new User();
        assertThrowsServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                () -> taskDefinitionController.searchTaskWorkflows(user, 1L, "orders", 0, 10));
        assertThrowsServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                () -> taskDefinitionController.searchTaskWorkflows(user, 1L, "orders", 1, 0));
        verifyNoInteractions(taskDefinitionService);
    }

    @Test
    public void testMissingRequiredPageParameters() throws Exception {
        MockMvcBuilders.standaloneSetup(taskDefinitionController).build()
                .perform(get("/projects/1/task-definition/search")
                        .requestAttr(Constants.SESSION_USER, new User()))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(taskDefinitionService);
    }
}
