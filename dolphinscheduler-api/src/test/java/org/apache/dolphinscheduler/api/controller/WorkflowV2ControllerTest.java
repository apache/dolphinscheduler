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

import static org.apache.dolphinscheduler.common.Constants.EMPTY_STRING;

import org.apache.dolphinscheduler.api.dto.workflow.WorkflowCreateRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowFilterRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowUpdateRequest;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * project v2 controller test
 */
@ExtendWith(MockitoExtension.class)
public class WorkflowV2ControllerTest {

    protected User user;
    @InjectMocks
    private WorkflowV2Controller workflowV2Controller;
    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    private TenantMapper tenantMapper;

    private final static String name = "workflowName";
    private final static String newName = "workflowNameNew";
    private final static String releaseState = "ONLINE";
    private final static int projectCode = 13579;
    private final static String description = "the workflow description";
    private final static int timeout = 30;
    private final static String tenantCode = "dolphinscheduler";
    private final static int warningGroupId = 0;
    private final static String executionType = "PARALLEL";

    @BeforeEach
    public void before() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("admin");
        user = loginUser;
    }

    @Test
    public void testCreateWorkflow() {
        WorkflowCreateRequest workflowCreateRequest = new WorkflowCreateRequest();
        workflowCreateRequest.setName(name);
        workflowCreateRequest.setReleaseState(releaseState);
        workflowCreateRequest.setProjectCode(projectCode);
        workflowCreateRequest.setDescription(description);
        workflowCreateRequest.setGlobalParams(EMPTY_STRING);
        workflowCreateRequest.setTimeout(timeout);
        workflowCreateRequest.setTenantCode(tenantCode);
        workflowCreateRequest.setWarningGroupId(warningGroupId);
        workflowCreateRequest.setExecutionType(executionType);

        Mockito.when(processDefinitionService.createSingleProcessDefinition(user, workflowCreateRequest))
                .thenReturn(this.getProcessDefinition(name));
        Result<ProcessDefinition> resourceResponse = workflowV2Controller.createWorkflow(user, workflowCreateRequest);
        Assertions.assertEquals(this.getProcessDefinition(name), resourceResponse.getData());
    }

    @Test
    public void testUpdateWorkflow() {
        WorkflowUpdateRequest workflowUpdateRequest = new WorkflowUpdateRequest();
        workflowUpdateRequest.setName(newName);

        Mockito.when(processDefinitionService.updateSingleProcessDefinition(user, 1L, workflowUpdateRequest))
                .thenReturn(this.getProcessDefinition(newName));
        Result<ProcessDefinition> resourceResponse =
                workflowV2Controller.updateWorkflow(user, 1L, workflowUpdateRequest);

        Assertions.assertEquals(this.getProcessDefinition(newName), resourceResponse.getData());
    }

    @Test
    public void testGetWorkflow() {
        Mockito.when(processDefinitionService.getProcessDefinition(user, 1L))
                .thenReturn(this.getProcessDefinition(name));
        Result<ProcessDefinition> resourceResponse = workflowV2Controller.getWorkflow(user, 1L);
        Assertions.assertEquals(this.getProcessDefinition(name), resourceResponse.getData());
    }

    @Test
    public void testFilterWorkflow() {
        WorkflowFilterRequest workflowFilterRequest = new WorkflowFilterRequest();
        workflowFilterRequest.setWorkflowName(name);

        Mockito.when(processDefinitionService.filterProcessDefinition(user, workflowFilterRequest))
                .thenReturn(this.getProcessDefinitionPage(name));
        Result<PageInfo<ProcessDefinition>> pageResourceResponse =
                workflowV2Controller.filterWorkflows(user, workflowFilterRequest);

        PageInfo<ProcessDefinition> processDefinitionPage = pageResourceResponse.getData();
        Assertions.assertIterableEquals(this.getProcessDefinitionPage(name).getTotalList(),
                processDefinitionPage.getTotalList());
    }

    private ProcessDefinition getProcessDefinition(String pdName) {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(1);
        processDefinition.setName(pdName);
        processDefinition.setDescription(description);
        processDefinition.setReleaseState(ReleaseState.valueOf(releaseState));
        processDefinition.setProjectCode(projectCode);
        processDefinition.setTenantId(1);
        processDefinition.setExecutionType(ProcessExecutionTypeEnum.valueOf(executionType));
        processDefinition.setWarningGroupId(warningGroupId);
        processDefinition.setGlobalParams(EMPTY_STRING);
        return processDefinition;
    }

    private PageInfo<ProcessDefinition> getProcessDefinitionPage(String pdName) {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(1);
        processDefinition.setName(pdName);
        processDefinition.setDescription(description);
        processDefinition.setReleaseState(ReleaseState.valueOf(releaseState));
        processDefinition.setProjectCode(projectCode);
        processDefinition.setTenantId(1);
        processDefinition.setExecutionType(ProcessExecutionTypeEnum.valueOf(executionType));
        processDefinition.setWarningGroupId(warningGroupId);
        processDefinition.setGlobalParams(EMPTY_STRING);

        PageInfo<ProcessDefinition> pageInfoProcessDefinitions = new PageInfo<ProcessDefinition>();
        List<ProcessDefinition> processDefinitions = new ArrayList<ProcessDefinition>();
        processDefinitions.add(processDefinition);
        pageInfoProcessDefinitions.setTotalList(processDefinitions);
        return pageInfoProcessDefinitions;
    }
}
