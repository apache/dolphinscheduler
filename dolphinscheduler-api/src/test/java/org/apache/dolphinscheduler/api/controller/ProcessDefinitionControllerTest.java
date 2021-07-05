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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ProcessDefinitionServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.User;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * process definition controller test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class ProcessDefinitionControllerTest {

    @InjectMocks
    private ProcessDefinitionController processDefinitionController;

    @Mock
    private ProcessDefinitionServiceImpl processDefinitionService;

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
    public void testCreateProcessDefinition() throws Exception {
        String json = "[{\"name\":\"\",\"pre_task_code\":0,\"pre_task_version\":0,\"post_task_code\":123456789,\"post_task_version\":1,"
                + "\"condition_type\":0,\"condition_params\":{}},{\"name\":\"\",\"pre_task_code\":123456789,\"pre_task_version\":1,"
                + "\"post_task_code\":123451234,\"post_task_version\":1,\"condition_type\":0,\"condition_params\":{}}]";

        long projectCode = 1L;
        String name = "dag_test";
        String description = "desc test";
        String globalParams = "[]";
        String connects = "[]";
        String locations = "[]";
        int timeout = 0;
        String tenantCode = "root";
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, 1);

        Mockito.when(processDefinitionService.createProcessDefinition(user, projectCode, name, description, globalParams,
                connects, locations, timeout, tenantCode, json)).thenReturn(result);

        Result response = processDefinitionController.createProcessDefinition(user, projectCode, name, description, globalParams,
                connects, locations, timeout, tenantCode, json);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }

    @Test
    public void testVerifyProcessDefinitionName() {
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROCESS_DEFINITION_NAME_EXIST);
        long projectCode = 1L;
        String name = "dag_test";

        Mockito.when(processDefinitionService.verifyProcessDefinitionName(user, projectCode, name)).thenReturn(result);

        Result response = processDefinitionController.verifyProcessDefinitionName(user, projectCode, name);
        Assert.assertTrue(response.isStatus(Status.PROCESS_DEFINITION_NAME_EXIST));
    }

    @Test
    public void updateProcessDefinition() {
        String json = "[{\"name\":\"\",\"pre_task_code\":0,\"pre_task_version\":0,\"post_task_code\":123456789,\"post_task_version\":1,"
                + "\"condition_type\":0,\"condition_params\":{}},{\"name\":\"\",\"pre_task_code\":123456789,\"pre_task_version\":1,"
                + "\"post_task_code\":123451234,\"post_task_version\":1,\"condition_type\":0,\"condition_params\":{}}]";
        String locations = "{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}";
        long projectCode = 1L;
        String name = "dag_test";
        String description = "desc test";
        String connects = "[]";
        String globalParams = "[]";
        int timeout = 0;
        String tenantCode = "root";
        long code = 123L;
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put("processDefinitionId", 1);

        Mockito.when(processDefinitionService.updateProcessDefinition(user, projectCode, name, code, description, globalParams,
                connects, locations, timeout, tenantCode, json)).thenReturn(result);

        Result response = processDefinitionController.updateProcessDefinition(user, projectCode, name, code, description, globalParams,
                connects, locations, timeout, tenantCode, json, ReleaseState.OFFLINE);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testReleaseProcessDefinition() {
        long projectCode = 1L;
        int id = 1;
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.releaseProcessDefinition(user, projectCode, id, ReleaseState.OFFLINE)).thenReturn(result);
        Result response = processDefinitionController.releaseProcessDefinition(user, projectCode, id, ReleaseState.OFFLINE);
        Assert.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testQueryProcessDefinitionByCode() {
        String locations = "{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}";
        long projectCode = 1L;
        String name = "dag_test";
        String description = "desc test";
        String connects = "[]";
        long code = 1L;

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setProjectCode(projectCode);
        processDefinition.setConnects(connects);
        processDefinition.setDescription(description);
        processDefinition.setCode(code);
        processDefinition.setLocations(locations);
        processDefinition.setName(name);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, processDefinition);

        Mockito.when(processDefinitionService.queryProcessDefinitionByCode(user, projectCode, code)).thenReturn(result);
        Result response = processDefinitionController.queryProcessDefinitionByCode(user, projectCode, code);

        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testBatchCopyProcessDefinition() {
        long projectCode = 1L;
        long targetProjectCode = 2L;
        String code = "1";

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.batchCopyProcessDefinition(user, projectCode, code, targetProjectCode)).thenReturn(result);
        Result response = processDefinitionController.copyProcessDefinition(user, projectCode, code, targetProjectCode);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testBatchMoveProcessDefinition() {
        long projectCode = 1L;
        long targetProjectCode = 2L;
        String id = "1";

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.batchMoveProcessDefinition(user, projectCode, id, targetProjectCode)).thenReturn(result);
        Result response = processDefinitionController.moveProcessDefinition(user, projectCode, id, targetProjectCode);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testQueryProcessDefinitionList() {
        long projectCode = 1L;
        List<ProcessDefinition> resourceList = getDefinitionList();

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, resourceList);

        Mockito.when(processDefinitionService.queryProcessDefinitionList(user, projectCode)).thenReturn(result);
        Result response = processDefinitionController.queryProcessDefinitionList(user, projectCode);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    public List<ProcessDefinition> getDefinitionList() {
        List<ProcessDefinition> resourceList = new ArrayList<>();
        String locations = "{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}";
        String projectName = "test";
        String name = "dag_test";
        String description = "desc test";
        String connects = "[]";
        int id = 1;

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setProjectName(projectName);
        processDefinition.setConnects(connects);
        processDefinition.setDescription(description);
        processDefinition.setId(id);
        processDefinition.setLocations(locations);
        processDefinition.setName(name);

        String name2 = "dag_test";
        int id2 = 2;

        ProcessDefinition processDefinition2 = new ProcessDefinition();
        processDefinition2.setProjectName(projectName);
        processDefinition2.setConnects(connects);
        processDefinition2.setDescription(description);
        processDefinition2.setId(id2);
        processDefinition2.setLocations(locations);
        processDefinition2.setName(name2);

        resourceList.add(processDefinition);
        resourceList.add(processDefinition2);

        return resourceList;
    }

    @Test
    public void testDeleteProcessDefinitionById() {
        long projectCode = 1L;
        int id = 1;

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.deleteProcessDefinitionById(user, projectCode, id)).thenReturn(result);
        Result response = processDefinitionController.deleteProcessDefinitionById(user, projectCode, id);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testGetNodeListByDefinitionId() {
        long projectCode = 1L;
        Long code = 1L;

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.getTaskNodeListByDefinitionCode(code)).thenReturn(result);
        Result response = processDefinitionController.getNodeListByDefinitionCode(user, projectCode, code);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testGetNodeListByDefinitionIdList() throws Exception {
        long projectCode = 1L;
        String codeList = "1,2,3";

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.getTaskNodeListByDefinitionCodeList(codeList)).thenReturn(result);
        Result response = processDefinitionController.getNodeListByDefinitionCodeList(user, projectCode, codeList);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testQueryProcessDefinitionAllByProjectId() {
        long projectCode = 1L;
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.queryAllProcessDefinitionByProjectCode(user, projectCode)).thenReturn(result);
        Result response = processDefinitionController.queryAllProcessDefinitionByProjectCode(user, projectCode);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testViewTree() throws Exception {
        long projectCode = 1L;
        int processId = 1;
        int limit = 2;
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.viewTree(processId, limit)).thenReturn(result);
        Result response = processDefinitionController.viewTree(user, projectCode, processId, limit);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testQueryProcessDefinitionListPaging() throws Exception {
        long projectCode = 1L;
        int pageNo = 1;
        int pageSize = 10;
        String searchVal = "";
        int userId = 1;

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, new PageInfo<Resource>(1, 10));

        Mockito.when(processDefinitionService.queryProcessDefinitionListPaging(user, projectCode, searchVal, pageNo, pageSize, userId)).thenReturn(result);
        Result response = processDefinitionController.queryProcessDefinitionListPaging(user, projectCode, pageNo, searchVal, userId, pageSize);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testBatchExportProcessDefinitionByIds() throws Exception {
        String processDefinitionIds = "1,2";
        long projectCode = 1L;
        HttpServletResponse response = new MockHttpServletResponse();
        Mockito.doNothing().when(this.processDefinitionService).batchExportProcessDefinitionByIds(user, projectCode, processDefinitionIds, response);
        processDefinitionController.batchExportProcessDefinitionByIds(user, projectCode, processDefinitionIds, response);
    }

    @Test
    public void testQueryProcessDefinitionVersions() {
        long projectCode = 1L;
        Map<String, Object> resultMap = new HashMap<>();
        putMsg(resultMap, Status.SUCCESS);
        resultMap.put(Constants.DATA_LIST, new PageInfo<ProcessDefinitionLog>(1, 10));
        Mockito.when(processDefinitionService.queryProcessDefinitionVersions(
                user
                , projectCode
                , 1
                , 10
                , 1))
                .thenReturn(resultMap);
        Result result = processDefinitionController.queryProcessDefinitionVersions(
                user
                , projectCode
                , 1
                , 10
                , 1);

        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    @Test
    public void testSwitchProcessDefinitionVersion() {
        long projectCode = 1L;
        Map<String, Object> resultMap = new HashMap<>();
        putMsg(resultMap, Status.SUCCESS);
        Mockito.when(processDefinitionService.switchProcessDefinitionVersion(user, projectCode, 1, 10)).thenReturn(resultMap);
        Result result = processDefinitionController.switchProcessDefinitionVersion(user, projectCode, 1, 10);

        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    @Test
    public void testDeleteProcessDefinitionVersion() {
        long projectCode = 1L;
        Map<String, Object> resultMap = new HashMap<>();
        putMsg(resultMap, Status.SUCCESS);
        Mockito.when(processDefinitionService.deleteByProcessDefinitionIdAndVersion(
                user
                , projectCode
                , 1
                , 10))
                .thenReturn(resultMap);
        Result result = processDefinitionController.deleteProcessDefinitionVersion(
                user
                , projectCode
                , 1
                , 10);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

}
