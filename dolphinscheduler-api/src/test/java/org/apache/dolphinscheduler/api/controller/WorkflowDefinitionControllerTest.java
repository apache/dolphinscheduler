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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.WorkflowDefinitionServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * process definition controller test
 */
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
        String relationJson =
                "[{\"name\":\"\",\"pre_task_code\":0,\"pre_task_version\":0,\"post_task_code\":123456789,\"post_task_version\":1,"
                        + "\"condition_type\":0,\"condition_params\":\"{}\"},{\"name\":\"\",\"pre_task_code\":123456789,\"pre_task_version\":1,"
                        + "\"post_task_code\":123451234,\"post_task_version\":1,\"condition_type\":0,\"condition_params\":\"{}\"}]";
        String taskDefinitionJson =
                "[{\"name\":\"detail_up\",\"description\":\"\",\"taskType\":\"SHELL\",\"taskParams\":"
                        + "\"{\\\"resourceList\\\":[],\\\"localParams\\\":[{\\\"prop\\\":\\\"datetime\\\",\\\"direct\\\":\\\"IN\\\","
                        + "\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"${system.datetime}\\\"}],\\\"rawScript\\\":"
                        + "\\\"echo ${datetime}\\\",\\\"conditionResult\\\":\\\"{\\\\\\\"successNode\\\\\\\":[\\\\\\\"\\\\\\\"],"
                        + "\\\\\\\"failedNode\\\\\\\":[\\\\\\\"\\\\\\\"]}\\\",\\\"dependence\\\":{}}\",\"flag\":0,\"taskPriority\":0,"
                        + "\"workerGroup\":\"default\",\"failRetryTimes\":0,\"failRetryInterval\":0,\"timeoutFlag\":0,"
                        + "\"timeoutNotifyStrategy\":0,\"timeout\":0,\"delayTime\":0,\"resourceIds\":\"\"}]";
        long projectCode = 1L;
        String name = "dag_test";
        String description = "desc test";
        String globalParams = "[]";
        String locations = "[]";
        int timeout = 0;
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, 1);

        Mockito.when(
                processDefinitionService.createWorkflowDefinition(user, projectCode, name, description, globalParams,
                        locations, timeout, relationJson, taskDefinitionJson, "",
                        WorkflowExecutionTypeEnum.PARALLEL))
                .thenReturn(result);

        Result response =
                workflowDefinitionController.createWorkflowDefinition(user, projectCode, name, description,
                        globalParams,
                        locations, timeout, relationJson, taskDefinitionJson, "",
                        WorkflowExecutionTypeEnum.PARALLEL);
        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
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
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.WORKFLOW_DEFINITION_NAME_EXIST);
        long projectCode = 1L;
        String name = "dag_test";

        Mockito.when(processDefinitionService.verifyWorkflowDefinitionName(user, projectCode, name, 0))
                .thenReturn(result);

        Result response = workflowDefinitionController.verifyWorkflowDefinitionName(user, projectCode, name, 0);
        Assertions.assertTrue(response.isStatus(Status.WORKFLOW_DEFINITION_NAME_EXIST));
    }

    @Test
    public void updateWorkflowDefinition() {
        String relationJson =
                "[{\"name\":\"\",\"pre_task_code\":0,\"pre_task_version\":0,\"post_task_code\":123456789,\"post_task_version\":1,"
                        + "\"condition_type\":0,\"condition_params\":\"{}\"},{\"name\":\"\",\"pre_task_code\":123456789,\"pre_task_version\":1,"
                        + "\"post_task_code\":123451234,\"post_task_version\":1,\"condition_type\":0,\"condition_params\":\"{}\"}]";
        String taskDefinitionJson =
                "[{\"name\":\"detail_up\",\"description\":\"\",\"taskType\":\"SHELL\",\"taskParams\":"
                        + "\"{\\\"resourceList\\\":[],\\\"localParams\\\":[{\\\"prop\\\":\\\"datetime\\\",\\\"direct\\\":\\\"IN\\\","
                        + "\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"${system.datetime}\\\"}],\\\"rawScript\\\":"
                        + "\\\"echo ${datetime}\\\",\\\"conditionResult\\\":\\\"{\\\\\\\"successNode\\\\\\\":[\\\\\\\"\\\\\\\"],"
                        + "\\\\\\\"failedNode\\\\\\\":[\\\\\\\"\\\\\\\"]}\\\",\\\"dependence\\\":{}}\",\"flag\":0,\"taskPriority\":0,"
                        + "\"workerGroup\":\"default\",\"failRetryTimes\":0,\"failRetryInterval\":0,\"timeoutFlag\":0,"
                        + "\"timeoutNotifyStrategy\":0,\"timeout\":0,\"delayTime\":0,\"resourceIds\":\"\"}]";
        String locations = "{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}";
        long projectCode = 1L;
        String name = "dag_test";
        String description = "desc test";
        String globalParams = "[]";
        int timeout = 0;
        long code = 123L;
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put("processDefinitionId", 1);

        Mockito.when(processDefinitionService.updateWorkflowDefinition(user, projectCode, name, code, description,
                globalParams,
                locations, timeout, relationJson, taskDefinitionJson,
                WorkflowExecutionTypeEnum.PARALLEL)).thenReturn(result);

        Result response = workflowDefinitionController.updateWorkflowDefinition(user, projectCode, name, code,
                description, globalParams,
                locations, timeout, relationJson, taskDefinitionJson, WorkflowExecutionTypeEnum.PARALLEL,
                ReleaseState.OFFLINE);
        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testReleaseWorkflowDefinition() {
        long projectCode = 1L;
        long id = 1L;
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        doNothing().when(processDefinitionService)
                .offlineWorkflowDefinition(user, projectCode, id);
        Result<Boolean> response =
                workflowDefinitionController.releaseWorkflowDefinition(user, projectCode, id, ReleaseState.OFFLINE);
        Assertions.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testQueryWorkflowDefinitionByCode() {
        String locations = "{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}";
        long projectCode = 1L;
        String name = "dag_test";
        String description = "desc test";
        long code = 1L;

        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setProjectCode(projectCode);
        workflowDefinition.setDescription(description);
        workflowDefinition.setCode(code);
        workflowDefinition.setLocations(locations);
        workflowDefinition.setName(name);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, workflowDefinition);

        Mockito.when(processDefinitionService.queryWorkflowDefinitionByCode(user, projectCode, code))
                .thenReturn(result);
        Result response = workflowDefinitionController.queryWorkflowDefinitionByCode(user, projectCode, code);

        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testBatchCopyWorkflowDefinition() {
        long projectCode = 1L;
        long targetProjectCode = 2L;
        String code = "1";

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.batchCopyWorkflowDefinition(user, projectCode, code, targetProjectCode))
                .thenReturn(result);
        Result response =
                workflowDefinitionController.copyWorkflowDefinition(user, projectCode, code, targetProjectCode);

        Assertions.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testBatchMoveWorkflowDefinition() {
        long projectCode = 1L;
        long targetProjectCode = 2L;
        String id = "1";

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.batchMoveWorkflowDefinition(user, projectCode, id, targetProjectCode))
                .thenReturn(result);
        Result response = workflowDefinitionController.moveWorkflowDefinition(user, projectCode, id, targetProjectCode);

        Assertions.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testQueryWorkflowDefinitionList() {
        long projectCode = 1L;
        List<WorkflowDefinition> resourceList = getDefinitionList();

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, resourceList);

        Mockito.when(processDefinitionService.queryWorkflowDefinitionList(user, projectCode)).thenReturn(result);
        Result response = workflowDefinitionController.queryWorkflowDefinitionList(user, projectCode);

        Assertions.assertTrue(response != null && response.isSuccess());
    }

    public List<WorkflowDefinition> getDefinitionList() {
        List<WorkflowDefinition> resourceList = new ArrayList<>();
        String locations = "{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}";
        String projectName = "test";
        String name = "dag_test";
        String description = "desc test";
        int id = 1;

        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setProjectName(projectName);
        workflowDefinition.setDescription(description);
        workflowDefinition.setId(id);
        workflowDefinition.setLocations(locations);
        workflowDefinition.setName(name);

        String name2 = "dag_test";
        int id2 = 2;

        WorkflowDefinition workflowDefinition2 = new WorkflowDefinition();
        workflowDefinition2.setProjectName(projectName);
        workflowDefinition2.setDescription(description);
        workflowDefinition2.setId(id2);
        workflowDefinition2.setLocations(locations);
        workflowDefinition2.setName(name2);

        resourceList.add(workflowDefinition);
        resourceList.add(workflowDefinition2);

        return resourceList;
    }

    @Test
    public void testDeleteWorkflowDefinitionByCode() {
        long projectCode = 1L;
        long code = 1L;
        // not throw error mean pass
        Assertions.assertDoesNotThrow(
                () -> workflowDefinitionController.deleteWorkflowDefinitionByCode(user, projectCode, code));
    }

    @Test
    public void testGetNodeListByDefinitionId() {
        long projectCode = 1L;
        Long code = 1L;

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.getTaskNodeListByDefinitionCode(user, projectCode, code))
                .thenReturn(result);
        Result response = workflowDefinitionController.getNodeListByDefinitionCode(user, projectCode, code);

        Assertions.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testGetNodeListByDefinitionIdList() {
        long projectCode = 1L;
        String codeList = "1,2,3";

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.getNodeListMapByDefinitionCodes(user, projectCode, codeList))
                .thenReturn(result);
        Result response = workflowDefinitionController.getNodeListMapByDefinitionCodes(user, projectCode, codeList);

        Assertions.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testQueryProcessDefinitionAllByProjectId() {
        long projectCode = 1L;
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.queryAllWorkflowDefinitionByProjectCode(user, projectCode))
                .thenReturn(result);
        Result response = workflowDefinitionController.queryAllWorkflowDefinitionByProjectCode(user, projectCode);

        Assertions.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testViewTree() throws Exception {
        long projectCode = 1L;
        int processId = 1;
        int limit = 2;
        User user = new User();
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.viewTree(user, projectCode, processId, limit)).thenReturn(result);
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
    public void testBatchExportWorkflowDefinitionByCodes() {
        String processDefinitionIds = "1,2";
        long projectCode = 1L;
        HttpServletResponse response = new MockHttpServletResponse();
        doNothing().when(this.processDefinitionService).batchExportWorkflowDefinitionByCodes(user, projectCode,
                processDefinitionIds, response);
        workflowDefinitionController.batchExportWorkflowDefinitionByCodes(user, projectCode, processDefinitionIds,
                response);
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
        Map<String, Object> resultMap = new HashMap<>();
        putMsg(resultMap, Status.SUCCESS);
        Mockito.when(processDefinitionService.switchWorkflowDefinitionVersion(user, projectCode, 1, 10))
                .thenReturn(resultMap);
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
        Map<String, Object> resultMap = new HashMap<>();
        putMsg(resultMap, Status.SUCCESS);

        Mockito.when(processDefinitionService.viewVariables(user, projectCode, 1))
                .thenReturn(resultMap);

        Result result = workflowDefinitionController.viewVariables(user, projectCode, 1L);

        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

}
