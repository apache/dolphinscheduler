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

import static org.mockito.Mockito.doNothing;

import org.apache.dolphinscheduler.api.dto.treeview.TreeViewDto;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowDefinitionVariablesDTO;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.WorkflowDefinitionServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WorkflowDefinitionControllerTest {

    @InjectMocks
    private WorkflowDefinitionController workflowDefinitionController;

    @Mock
    private WorkflowDefinitionServiceImpl processDefinitionService;

    protected User user;

    @BeforeEach
    public void before() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("admin");
        user = loginUser;
    }

    @Test
    public void testCreateWorkflowDefinition() {
        long projectCode = 1L;
        String name = "dag_test";
        String description = "desc test";
        String globalParams = "[]";
        String locations = "[]";
        int timeout = 0;
        String relationJson = "[]";
        String taskDefinitionJson = "[]";

        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setName(name);

        Mockito.when(processDefinitionService.createWorkflowDefinition(user, projectCode, name, description,
                globalParams, locations, timeout, relationJson, taskDefinitionJson, "",
                WorkflowExecutionTypeEnum.PARALLEL))
                .thenReturn(workflowDefinition);

        Result<WorkflowDefinition> response = workflowDefinitionController.createWorkflowDefinition(user, projectCode,
                name, description, globalParams, locations, timeout, relationJson, taskDefinitionJson, "",
                WorkflowExecutionTypeEnum.PARALLEL);
        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    public void putMsg(Result result, Status status, Object... statusParams) {
        result.setCode(status.getCode());
        if (statusParams != null && statusParams.length > 0) {
            result.setMsg(MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.setMsg(status.getMsg());
        }
    }

    @Test
    public void testVerifyWorkflowDefinitionName() {
        long projectCode = 1L;
        String name = "dag_test";

        Mockito.doThrow(new ServiceException(Status.WORKFLOW_DEFINITION_NAME_EXIST, name))
                .when(processDefinitionService).verifyWorkflowDefinitionName(user, projectCode, name, 0);

        Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionController.verifyWorkflowDefinitionName(user, projectCode, name, 0));
    }

    @Test
    public void updateWorkflowDefinition() {
        String relationJson = "[]";
        String taskDefinitionJson = "[]";
        String locations = "{}";
        long projectCode = 1L;
        String name = "dag_test";
        String description = "desc test";
        String globalParams = "[]";
        int timeout = 0;
        long code = 123L;

        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(code);

        Mockito.when(processDefinitionService.updateWorkflowDefinition(user, projectCode, name, code, description,
                globalParams, locations, timeout, relationJson, taskDefinitionJson,
                WorkflowExecutionTypeEnum.PARALLEL)).thenReturn(workflowDefinition);

        Result<WorkflowDefinition> response = workflowDefinitionController.updateWorkflowDefinition(user, projectCode,
                name, code, description, globalParams, locations, timeout, relationJson, taskDefinitionJson,
                WorkflowExecutionTypeEnum.PARALLEL, ReleaseState.OFFLINE);
        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testReleaseWorkflowDefinition() {
        long projectCode = 1L;
        long id = 1L;

        doNothing().when(processDefinitionService)
                .offlineWorkflowDefinition(user, projectCode, id);
        Result<Boolean> response =
                workflowDefinitionController.releaseWorkflowDefinition(user, projectCode, id, ReleaseState.OFFLINE);
        Assertions.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testQueryWorkflowDefinitionByCode() {
        long projectCode = 1L;
        long code = 1L;

        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(code);
        DagData dagData = new DagData(workflowDefinition, Collections.emptyList(), Collections.emptyList());

        Mockito.when(processDefinitionService.queryWorkflowDefinitionByCode(user, projectCode, code))
                .thenReturn(dagData);
        Result<DagData> response = workflowDefinitionController.queryWorkflowDefinitionByCode(user, projectCode, code);

        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testBatchCopyWorkflowDefinition() {
        long projectCode = 1L;
        long targetProjectCode = 2L;
        String code = "1";

        doNothing().when(processDefinitionService)
                .batchCopyWorkflowDefinition(user, projectCode, code, targetProjectCode);
        Result<Void> response =
                workflowDefinitionController.copyWorkflowDefinition(user, projectCode, code, targetProjectCode);

        Assertions.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testBatchMoveWorkflowDefinition() {
        long projectCode = 1L;
        long targetProjectCode = 2L;
        String id = "1";

        doNothing().when(processDefinitionService)
                .batchMoveWorkflowDefinition(user, projectCode, id, targetProjectCode);
        Result<Void> response =
                workflowDefinitionController.moveWorkflowDefinition(user, projectCode, id, targetProjectCode);

        Assertions.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testQueryWorkflowDefinitionList() {
        long projectCode = 1L;

        Mockito.when(processDefinitionService.queryWorkflowDefinitionList(user, projectCode))
                .thenReturn(Collections.emptyList());
        Result<List<DagData>> response = workflowDefinitionController.queryWorkflowDefinitionList(user, projectCode);

        Assertions.assertTrue(response != null && response.isSuccess());
    }

    public List<WorkflowDefinition> getDefinitionList() {
        List<WorkflowDefinition> resourceList = new ArrayList<>();
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setName("dag_test");
        workflowDefinition.setId(1);
        resourceList.add(workflowDefinition);
        return resourceList;
    }

    @Test
    public void testDeleteWorkflowDefinitionByCode() {
        long projectCode = 1L;
        long code = 1L;
        Assertions.assertDoesNotThrow(
                () -> workflowDefinitionController.deleteWorkflowDefinitionByCode(user, projectCode, code));
    }

    @Test
    public void testGetNodeListByDefinitionId() {
        long projectCode = 1L;
        Long code = 1L;

        Mockito.when(processDefinitionService.getTaskNodeListByDefinitionCode(user, projectCode, code))
                .thenReturn(Collections.emptyList());
        Result response = workflowDefinitionController.getNodeListByDefinitionCode(user, projectCode, code);

        Assertions.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testGetNodeListByDefinitionIdList() {
        long projectCode = 1L;
        String codeList = "1,2,3";

        Mockito.when(processDefinitionService.getNodeListMapByDefinitionCodes(user, projectCode, codeList))
                .thenReturn(new HashMap<>());
        Result response = workflowDefinitionController.getNodeListMapByDefinitionCodes(user, projectCode, codeList);

        Assertions.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testQueryProcessDefinitionAllByProjectId() {
        long projectCode = 1L;

        Mockito.when(processDefinitionService.queryAllWorkflowDefinitionByProjectCode(user, projectCode))
                .thenReturn(Collections.emptyList());
        Result response = workflowDefinitionController.queryAllWorkflowDefinitionByProjectCode(user, projectCode);

        Assertions.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testViewTree() throws Exception {
        long projectCode = 1L;
        int processId = 1;
        int limit = 2;
        User user = new User();

        Mockito.when(processDefinitionService.viewTree(user, projectCode, processId, limit))
                .thenReturn(new TreeViewDto());
        Result response = workflowDefinitionController.viewTree(user, projectCode, processId, limit);

        Assertions.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testQueryWorkflowDefinitionListPaging() {
        long projectCode = 1L;
        int pageNo = 1;
        int pageSize = 10;
        String searchVal = "";
        int userId = 1;

        PageInfo<WorkflowDefinition> pageInfo = new PageInfo<>(1, 10);

        Mockito.when(
                processDefinitionService.queryWorkflowDefinitionListPaging(user, projectCode, searchVal, "", userId,
                        pageNo, pageSize))
                .thenReturn(pageInfo);
        Result<PageInfo<WorkflowDefinition>> response = workflowDefinitionController
                .queryWorkflowDefinitionListPaging(user, projectCode, searchVal, "", userId, pageNo, pageSize);

        Assertions.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testQueryWorkflowDefinitionVersions() {

        long projectCode = 1L;
        Result resultMap = new Result();
        putMsg(resultMap, Status.SUCCESS);
        resultMap.setData(new PageInfo<WorkflowDefinitionLog>(1, 10));
        Mockito.when(processDefinitionService.queryWorkflowDefinitionVersions(
                user, projectCode, 1, 10, 1))
                .thenReturn(resultMap);
        Result result = workflowDefinitionController.queryWorkflowDefinitionVersions(
                user, projectCode, 1, 10, 1);

        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    @Test
    public void testSwitchWorkflowDefinitionVersion() {
        long projectCode = 1L;
        doNothing().when(processDefinitionService).switchWorkflowDefinitionVersion(user, projectCode, 1, 10);
        Result result = workflowDefinitionController.switchWorkflowDefinitionVersion(user, projectCode, 1, 10);

        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    @Test
    public void testDeleteWorkflowDefinitionVersion() {
        long projectCode = 1L;
        long workflowCode = 1L;
        int workflowVersion = 10;
        doNothing().when(processDefinitionService).deleteWorkflowDefinitionVersion(user, projectCode, workflowCode,
                workflowVersion);
        workflowDefinitionController.deleteWorkflowDefinitionVersion(user, projectCode, workflowCode, workflowVersion);
    }

    @Test
    public void testViewVariables() {
        long projectCode = 1L;

        Mockito.when(processDefinitionService.viewVariables(user, projectCode, 1L))
                .thenReturn(new WorkflowDefinitionVariablesDTO());

        Result result = workflowDefinitionController.viewVariables(user, projectCode, 1L);

        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

}
