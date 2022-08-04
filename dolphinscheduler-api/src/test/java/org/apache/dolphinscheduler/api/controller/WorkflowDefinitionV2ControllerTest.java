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

import org.apache.dolphinscheduler.api.dto.workflow.CreateEmptyWorkflowDefinitionRequest;
import org.apache.dolphinscheduler.api.dto.workflow.CreateEmptyWorkflowDefinitionResponse;
import org.apache.dolphinscheduler.api.dto.workflow.CreateWorkflowDefinitionRequest;
import org.apache.dolphinscheduler.api.dto.workflow.CreateWorkflowDefinitionResponse;
import org.apache.dolphinscheduler.api.dto.workflow.ProjectWorkflowRequest;
import org.apache.dolphinscheduler.api.dto.workflow.QueryWorkflowListResponse;
import org.apache.dolphinscheduler.api.dto.workflow.QueryWorkflowRequest;
import org.apache.dolphinscheduler.api.dto.workflow.QueryWorkflowResponse;
import org.apache.dolphinscheduler.api.dto.workflow.UpdateWorkflowBasicInfoRequest;
import org.apache.dolphinscheduler.api.dto.workflow.UpdateWorkflowDefinitionRequest;
import org.apache.dolphinscheduler.api.dto.workflow.UpdateWorkflowDefinitionResponse;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowDetailResponse;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ProcessDefinitionServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.User;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;

/**
 * workflow definition controller test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class WorkflowDefinitionV2ControllerTest {

    @InjectMocks
    private WorkflowDefinitionV2Controller workflowDefinitionController;

    @Mock
    private ProcessDefinitionServiceImpl workflowDefinitionService;

    protected User user;

    @Before
    public void before() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("admin");
        user = loginUser;
    }

    @Test
    public void testCreateWorkflowDefinition() {
        String relationJson = "[{\"name\":\"\",\"pre_task_code\":0,\"pre_task_version\":0,\"post_task_code\":123456789,\"post_task_version\":1,"
            + "\"condition_type\":0,\"condition_params\":\"{}\"},{\"name\":\"\",\"pre_task_code\":123456789,\"pre_task_version\":1,"
            + "\"post_task_code\":123451234,\"post_task_version\":1,\"condition_type\":0,\"condition_params\":\"{}\"}]";
        String taskDefinitionJson = "[{\"name\":\"detail_up\",\"description\":\"\",\"taskType\":\"SHELL\",\"taskParams\":"
            + "\"{\\\"resourceList\\\":[],\\\"localParams\\\":[{\\\"prop\\\":\\\"datetime\\\",\\\"direct\\\":\\\"IN\\\","
            + "\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"${system.datetime}\\\"}],\\\"rawScript\\\":"
            + "\\\"echo ${datetime}\\\",\\\"conditionResult\\\":\\\"{\\\\\\\"successNode\\\\\\\":[\\\\\\\"\\\\\\\"],"
            + "\\\\\\\"failedNode\\\\\\\":[\\\\\\\"\\\\\\\"]}\\\",\\\"dependence\\\":{}}\",\"flag\":0,\"taskPriority\":0,"
            + "\"workerGroup\":\"default\",\"failRetryTimes\":0,\"failRetryInterval\":0,\"timeoutFlag\":0," + "\"timeoutNotifyStrategy\":0,\"timeout\":0,\"delayTime\":0,\"resourceIds\":\"\"}]";

        long projectCode = 1L;
        String name = "dag_test";
        String description = "desc test";
        String globalParams = "[]";
        String locations = "[]";
        int timeout = 0;
        String tenantCode = "root";

        CreateWorkflowDefinitionRequest request = CreateWorkflowDefinitionRequest.builder()
            .name(name)
            .description(description)
            .globalParams(globalParams)
            .locations(locations)
            .timeout(timeout)
            .tenantCode(tenantCode)
            .taskRelationJson(relationJson)
            .taskDefinitionJson(taskDefinitionJson)
            .otherParamsJson("")
            .executionType(ProcessExecutionTypeEnum.PARALLEL)
            .build();
        Result result = new Result();
        putMsg(result, Status.SUCCESS);
        result.setData(new ProcessDefinition());

        Mockito.when(workflowDefinitionService.createProcessDefinition(user, projectCode, name, description, globalParams, locations, timeout, tenantCode, relationJson, taskDefinitionJson, "",
            ProcessExecutionTypeEnum.PARALLEL)).thenReturn(result);

        CreateWorkflowDefinitionResponse response = workflowDefinitionController.createWorkflowDefinition(user, projectCode, request);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testBatchCopyWorkflowDefinition() {
        long projectCode = 1L;
        long targetProjectCode = 2L;
        String code = "1";

        ProjectWorkflowRequest request = ProjectWorkflowRequest.builder()
            .codes(code)
            .targetProjectCode(targetProjectCode)
            .build();
        Result result = new Result();
        putMsg(result, Status.SUCCESS);

        Mockito.when(workflowDefinitionService.batchCopyProcessDefinition(user, projectCode, code, targetProjectCode)).thenReturn(result);
        Result response = workflowDefinitionController.copyWorkflowDefinition(user, projectCode, request);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testBatchMoveWorkflowDefinition() {
        long projectCode = 1L;
        long targetProjectCode = 2L;
        String id = "1";

        ProjectWorkflowRequest request = ProjectWorkflowRequest.builder()
            .codes(id)
            .targetProjectCode(targetProjectCode)
            .build();
        Result result = new Result();
        putMsg(result, Status.SUCCESS);

        Mockito.when(workflowDefinitionService.batchMoveProcessDefinition(user, projectCode, id, targetProjectCode)).thenReturn(result);
        Result response = workflowDefinitionController.moveWorkflowDefinition(user, projectCode, request);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testVerifyWorkflowDefinitionName() {
        Result result = new Result();
        putMsg(result, Status.PROCESS_DEFINITION_NAME_EXIST);
        long projectCode = 1L;
        String name = "dag_test";

        Mockito.when(workflowDefinitionService.verifyProcessDefinitionName(user, projectCode, name)).thenReturn(result);

        Result response = workflowDefinitionController.verifyWorkflowDefinitionName(user, projectCode, name);
        Assert.assertTrue(response.isStatus(Status.PROCESS_DEFINITION_NAME_EXIST));
    }

    @Test
    public void updateWorkflowDefinition() {
        String relationJson = "[{\"name\":\"\",\"pre_task_code\":0,\"pre_task_version\":0,\"post_task_code\":123456789,\"post_task_version\":1,"
            + "\"condition_type\":0,\"condition_params\":\"{}\"},{\"name\":\"\",\"pre_task_code\":123456789,\"pre_task_version\":1,"
            + "\"post_task_code\":123451234,\"post_task_version\":1,\"condition_type\":0,\"condition_params\":\"{}\"}]";
        String taskDefinitionJson = "[{\"name\":\"detail_up\",\"description\":\"\",\"taskType\":\"SHELL\",\"taskParams\":"
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
        String tenantCode = "root";
        long code = 123L;
        UpdateWorkflowDefinitionRequest request = UpdateWorkflowDefinitionRequest.builder()
            .name(name)
            .description(description)
            .globalParams(globalParams)
            .locations(locations)
            .timeout(timeout)
            .tenantCode(tenantCode)
            .taskRelationJson(relationJson)
            .taskDefinitionJson(taskDefinitionJson)
            .otherParamsJson("")
            .executionType(ProcessExecutionTypeEnum.PARALLEL)
            .releaseState(ReleaseState.OFFLINE)
            .build();
        Result result = new Result();
        putMsg(result, Status.SUCCESS, 1);

        Mockito.when(workflowDefinitionService.updateProcessDefinition(user, projectCode, name, code, description, globalParams,
            locations, timeout, tenantCode, relationJson, taskDefinitionJson, "", ProcessExecutionTypeEnum.PARALLEL)).thenReturn(result);

        Result response = workflowDefinitionController.updateWorkflowDefinition(user, projectCode, code, request);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void updateWorkflowDefinitionBasicInfo() {
        long projectCode = 1L;
        String name = "dag_test";
        String description = "desc test";
        String globalParams = "[]";
        int timeout = 0;
        String tenantCode = "root";
        long code = 123L;
        UpdateWorkflowBasicInfoRequest request = UpdateWorkflowBasicInfoRequest.builder()
            .name(name)
            .description(description)
            .globalParams(globalParams)
            .timeout(timeout)
            .tenantCode(tenantCode)
            .scheduleJson("")
            .otherParamsJson("")
            .executionType(ProcessExecutionTypeEnum.PARALLEL)
            .releaseState(ReleaseState.OFFLINE)
            .build();
        Result result = new Result();
        putMsg(result, Status.SUCCESS, 1);

        Mockito.when(workflowDefinitionService.updateProcessDefinitionBasicInfo(user, projectCode, name, code, description, globalParams,
            timeout, tenantCode, "", "", ProcessExecutionTypeEnum.PARALLEL)).thenReturn(result);

        UpdateWorkflowDefinitionResponse response = workflowDefinitionController.updateWorkflowDefinitionBasicInfo(user, projectCode, code, request);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testQueryWorkflowDefinitionVersions() {
        long projectCode = 1L;
        QueryWorkflowRequest request = QueryWorkflowRequest.builder()
            .pageNo(1)
            .pageSize(10)
            .build();
        Result resultMap = new Result();
        putMsg(resultMap, Status.SUCCESS);
        resultMap.setData(new PageInfo<ProcessDefinitionLog>(1, 10));
        Mockito.when(workflowDefinitionService.queryProcessDefinitionVersions(
                user
                , projectCode
                , 1
                , 10
                , 1))
            .thenReturn(resultMap);
        Result result = workflowDefinitionController.queryWorkflowDefinitionVersions(
            user
            , projectCode
            , 1
            , request);

        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    @Test
    public void testSwitchWorkflowDefinitionVersion() {
        long projectCode = 1L;
        Result resultMap = new Result();
        putMsg(resultMap, Status.SUCCESS);
        Mockito.when(workflowDefinitionService.switchProcessDefinitionVersion(user, projectCode, 1, 10)).thenReturn(resultMap);
        Result result = workflowDefinitionController.switchWorkflowDefinitionVersion(user, projectCode, 1, 10);

        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    @Test
    public void testDeleteWorkflowDefinitionVersion() {
        long projectCode = 1L;
        Result resultMap = new Result();
        putMsg(resultMap, Status.SUCCESS);
        Mockito.when(workflowDefinitionService.deleteProcessDefinitionVersion(user, projectCode, 1, 10)).thenReturn(resultMap);
        Result result = workflowDefinitionController.deleteWorkflowDefinitionVersion(user, projectCode, 1, 10);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    @Test
    public void testReleaseWorkflowDefinition() {
        long projectCode = 1L;
        int id = 1;
        Result result = new Result();
        putMsg(result, Status.SUCCESS);

        Mockito.when(workflowDefinitionService.releaseProcessDefinition(user, projectCode, id, ReleaseState.OFFLINE)).thenReturn(result);
        Result response = workflowDefinitionController.releaseWorkflowDefinition(user, projectCode, id, ReleaseState.OFFLINE);
        Assert.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testQueryWorkflowDefinitionByCode() {
        String locations = "{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}";
        long projectCode = 1L;
        String name = "dag_test";
        String description = "desc test";
        long code = 1L;

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setProjectCode(projectCode);
        processDefinition.setDescription(description);
        processDefinition.setCode(code);
        processDefinition.setLocations(locations);
        processDefinition.setName(name);
        DagData dagData = new DagData();
        dagData.setProcessDefinition(processDefinition);

        Result result = new Result();
        putMsg(result, Status.SUCCESS);
        result.setData(dagData);

        Mockito.when(workflowDefinitionService.queryProcessDefinitionByCode(user, projectCode, code)).thenReturn(result);
        WorkflowDetailResponse response = workflowDefinitionController.queryWorkflowDefinitionByCode(user, projectCode, code);

        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testQueryWorkflowDefinitionByName() {
        String locations = "{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}";
        long projectCode = 1L;
        String name = "dag_test";
        String description = "desc test";
        long code = 1L;

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setProjectCode(projectCode);
        processDefinition.setDescription(description);
        processDefinition.setCode(code);
        processDefinition.setLocations(locations);
        processDefinition.setName(name);
        DagData dagData = new DagData();
        dagData.setProcessDefinition(processDefinition);

        Result result = new Result();
        putMsg(result, Status.SUCCESS);
        result.setData(dagData);

        Mockito.when(workflowDefinitionService.queryProcessDefinitionByName(user, projectCode, name)).thenReturn(result);
        WorkflowDetailResponse response = workflowDefinitionController.queryWorkflowDefinitionByName(user, projectCode, name);

        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testQueryWorkflowDefinitionList() {
        long projectCode = 1L;
        List<ProcessDefinition> resourceList = getDefinitionList();

        Result result = new Result();
        putMsg(result, Status.SUCCESS);
        result.setData(resourceList);

        Mockito.when(workflowDefinitionService.queryProcessDefinitionList(user, projectCode)).thenReturn(result);
        QueryWorkflowListResponse response = workflowDefinitionController.queryWorkflowDefinitionList(user, projectCode);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    private List<ProcessDefinition> getDefinitionList() {
        List<ProcessDefinition> resourceList = new ArrayList<>();
        String locations = "{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}";
        String projectName = "test";
        String name = "dag_test";
        String description = "desc test";
        int id = 1;

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setProjectName(projectName);
        processDefinition.setDescription(description);
        processDefinition.setId(id);
        processDefinition.setLocations(locations);
        processDefinition.setName(name);

        String name2 = "dag_test";
        int id2 = 2;

        ProcessDefinition processDefinition2 = new ProcessDefinition();
        processDefinition2.setProjectName(projectName);
        processDefinition2.setDescription(description);
        processDefinition2.setId(id2);
        processDefinition2.setLocations(locations);
        processDefinition2.setName(name2);

        resourceList.add(processDefinition);
        resourceList.add(processDefinition2);

        return resourceList;
    }

    @Test
    public void testQueryWorkflowDefinitionListPaging() {
        long projectCode = 1L;
        int pageNo = 1;
        int pageSize = 10;
        String searchVal = "";
        int userId = 1;
        QueryWorkflowRequest request = QueryWorkflowRequest.builder()
            .pageNo(pageNo)
            .pageSize(pageSize)
            .userId(userId)
            .searchVal(searchVal)
            .userId(userId)
            .otherParamsJson("")
            .build();

        Result result = new Result();
        putMsg(result, Status.SUCCESS);
        result.setData(new PageInfo<Resource>(1, 10));

        Mockito.when(workflowDefinitionService.queryProcessDefinitionListPaging(user, projectCode, searchVal, "", userId, pageNo, pageSize)).thenReturn(result);
        QueryWorkflowResponse response = workflowDefinitionController.queryWorkflowDefinitionListPaging(user, projectCode, request);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testCreateEmptyWorkflowDefinition() {
        long projectCode = 1L;
        String name = "dag_test";
        String description = "desc test";
        String globalParams = "[]";
        String locations = "[]";
        int timeout = 0;
        String tenantCode = "root";

        CreateEmptyWorkflowDefinitionRequest request = CreateEmptyWorkflowDefinitionRequest.builder()
            .name(name)
            .description(description)
            .globalParams(globalParams)
            .locations(locations)
            .timeout(timeout)
            .tenantCode(tenantCode)
            .scheduleJson("")
            .executionType(ProcessExecutionTypeEnum.PARALLEL)
            .build();
        Result result = new Result();
        putMsg(result, Status.SUCCESS);
        result.setData(new ProcessDefinition());

        Mockito.when(workflowDefinitionService.createEmptyProcessDefinition(user, projectCode, name, description, globalParams, timeout, tenantCode, "", ProcessExecutionTypeEnum.PARALLEL))
            .thenReturn(result);

        CreateEmptyWorkflowDefinitionResponse response = workflowDefinitionController.createEmptyWorkflowDefinition(user, projectCode, request);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testViewTree() {
        long projectCode = 1L;
        int processId = 1;
        int limit = 2;
        User user = new User();
        Result result = new Result();
        putMsg(result, Status.SUCCESS);

        Mockito.when(workflowDefinitionService.viewTree(user, projectCode, processId, limit)).thenReturn(result);
        Result response = workflowDefinitionController.viewTree(user, projectCode, processId, limit);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testGetNodeListByDefinitionId() {
        long projectCode = 1L;
        Long code = 1L;

        Result result = new Result();
        putMsg(result, Status.SUCCESS);

        Mockito.when(workflowDefinitionService.getTaskNodeListByDefinitionCode(user, projectCode, code)).thenReturn(result);
        Result response = workflowDefinitionController.getNodeListByDefinitionCode(user, projectCode, code);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testGetNodeListByDefinitionIdList() {
        long projectCode = 1L;
        String codeList = "1,2,3";

        Result result = new Result();
        putMsg(result, Status.SUCCESS);

        Mockito.when(workflowDefinitionService.getNodeListMapByDefinitionCodes(user, projectCode, codeList)).thenReturn(result);
        Result response = workflowDefinitionController.getNodeListMapByDefinitionCodes(user, projectCode, codeList);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testDeleteWorkflowDefinitionByCode() {
        long projectCode = 1L;
        long code = 1L;

        Result result = new Result();
        putMsg(result, Status.SUCCESS);

        Mockito.when(workflowDefinitionService.deleteProcessDefinitionByCode(user, projectCode, code)).thenReturn(result);
        Result response = workflowDefinitionController.deleteWorkflowDefinitionByCode(user, projectCode, code);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testBatchExportWorkflowDefinitionByCodes() {
        String processDefinitionIds = "1,2";
        long projectCode = 1L;
        HttpServletResponse response = new MockHttpServletResponse();
        Mockito.doNothing().when(this.workflowDefinitionService).batchExportProcessDefinitionByCodes(user, projectCode, processDefinitionIds, response);
        workflowDefinitionController.batchExportWorkflowDefinitionByCodes(user, projectCode, processDefinitionIds, response);
    }

    @Test
    public void testQueryWorkflowDefinitionAllByProjectId() {
        long projectCode = 1L;
        Result result = new Result();
        putMsg(result, Status.SUCCESS);

        Mockito.when(workflowDefinitionService.queryAllProcessDefinitionByProjectCode(user, projectCode)).thenReturn(result);
        Result response = workflowDefinitionController.queryAllWorkflowDefinitionByProjectCode(user, projectCode);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    public void putMsg(Result result, Status status, Object... statusParams) {
        result.setCode(status.getCode());
        if (statusParams != null && statusParams.length > 0) {
            result.setMsg(MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.setMsg(status.getMsg());
        }
    }

}
