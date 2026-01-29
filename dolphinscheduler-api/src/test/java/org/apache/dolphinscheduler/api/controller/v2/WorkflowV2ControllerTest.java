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

package org.apache.dolphinscheduler.api.controller.v2;

import static org.apache.dolphinscheduler.common.constants.Constants.EMPTY_STRING;

import org.apache.dolphinscheduler.api.dto.workflow.WorkflowCreateRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowFilterRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowUpdateRequest;
import org.apache.dolphinscheduler.api.service.WorkflowDefinitionService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
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
    private WorkflowDefinitionService workflowDefinitionService;
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
        workflowCreateRequest.setWarningGroupId(warningGroupId);
        workflowCreateRequest.setExecutionType(executionType);

        Mockito.when(workflowDefinitionService.createSingleWorkflowDefinition(user, workflowCreateRequest))
                .thenReturn(this.getProcessDefinition(name));
        Result<WorkflowDefinition> resourceResponse = workflowV2Controller.createWorkflow(user, workflowCreateRequest);
        Assertions.assertEquals(this.getProcessDefinition(name), resourceResponse.getData());
    }

    @Test
    public void testUpdateWorkflow() {
        WorkflowUpdateRequest workflowUpdateRequest = new WorkflowUpdateRequest();
        workflowUpdateRequest.setName(newName);

        Mockito.when(workflowDefinitionService.updateSingleWorkflowDefinition(user, 1L, workflowUpdateRequest))
                .thenReturn(this.getProcessDefinition(newName));
        Result<WorkflowDefinition> resourceResponse =
                workflowV2Controller.updateWorkflow(user, 1L, workflowUpdateRequest);

        Assertions.assertEquals(this.getProcessDefinition(newName), resourceResponse.getData());
    }

    @Test
    public void testGetWorkflow() {
        Mockito.when(workflowDefinitionService.getWorkflowDefinition(user, 1L))
                .thenReturn(this.getProcessDefinition(name));
        Result<WorkflowDefinition> resourceResponse = workflowV2Controller.getWorkflow(user, 1L);
        Assertions.assertEquals(this.getProcessDefinition(name), resourceResponse.getData());
    }

    @Test
    public void testFilterWorkflow() {
        WorkflowFilterRequest workflowFilterRequest = new WorkflowFilterRequest();
        workflowFilterRequest.setWorkflowName(name);

        Mockito.when(workflowDefinitionService.filterWorkflowDefinition(user, workflowFilterRequest))
                .thenReturn(this.getProcessDefinitionPage(name));
        Result<PageInfo<WorkflowDefinition>> pageResourceResponse =
                workflowV2Controller.filterWorkflows(user, workflowFilterRequest);

        PageInfo<WorkflowDefinition> processDefinitionPage = pageResourceResponse.getData();
        Assertions.assertIterableEquals(this.getProcessDefinitionPage(name).getTotalList(),
                processDefinitionPage.getTotalList());
    }

    private WorkflowDefinition getProcessDefinition(String pdName) {
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setId(1);
        workflowDefinition.setName(pdName);
        workflowDefinition.setDescription(description);
        workflowDefinition.setReleaseState(ReleaseState.valueOf(releaseState));
        workflowDefinition.setProjectCode(projectCode);
        workflowDefinition.setExecutionType(WorkflowExecutionTypeEnum.valueOf(executionType));
        workflowDefinition.setWarningGroupId(warningGroupId);
        workflowDefinition.setGlobalParams(EMPTY_STRING);
        return workflowDefinition;
    }

    private PageInfo<WorkflowDefinition> getProcessDefinitionPage(String pdName) {
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setId(1);
        workflowDefinition.setName(pdName);
        workflowDefinition.setDescription(description);
        workflowDefinition.setReleaseState(ReleaseState.valueOf(releaseState));
        workflowDefinition.setProjectCode(projectCode);
        workflowDefinition.setExecutionType(WorkflowExecutionTypeEnum.valueOf(executionType));
        workflowDefinition.setWarningGroupId(warningGroupId);
        workflowDefinition.setGlobalParams(EMPTY_STRING);

        PageInfo<WorkflowDefinition> pageInfoProcessDefinitions = new PageInfo<WorkflowDefinition>();
        List<WorkflowDefinition> workflowDefinitions = new ArrayList<WorkflowDefinition>();
        workflowDefinitions.add(workflowDefinition);
        pageInfoProcessDefinitions.setTotalList(workflowDefinitions);
        return pageInfoProcessDefinitions;
    }
}
