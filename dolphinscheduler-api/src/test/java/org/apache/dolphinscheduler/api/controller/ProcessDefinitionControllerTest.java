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
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * process definition controller test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class ProcessDefinitionControllerTest {

    private static Logger logger = LoggerFactory.getLogger(ProcessDefinitionControllerTest.class);

    @InjectMocks
    private ProcessDefinitionController processDefinitionController;

    @Mock
    private ProcessDefinitionService processDefinitionService;

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
        String json = "{\"globalParams\":[],"
                + "\"tasks\":["
                + "{\"type\":\"SHELL\",\"id\":\"tasks-36196\",\"name\":\"ssh_test1\",\"params\":{\"resourceList\":[],"
                + "\"localParams\":[],\"rawScript\":\"aa=\\\"1234\\\"\\necho ${aa}\"},\"desc\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},"
                + "\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},"
                + "\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[]}]"
                + ",\"tenantId\":-1,\"timeout\":0}";
        String locations = "{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}";

        String projectName = "test";
        String name = "dag_test";
        String description = "desc test";
        String connects = "[]";
        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.SUCCESS);
        result.put("processDefinitionId",1);

        Mockito.when(processDefinitionService.createProcessDefinition(user, projectName, name, json,
                description, locations, connects)).thenReturn(result);

        Result response = processDefinitionController.createProcessDefinition(user, projectName, name, json,
                locations, connects, description);
        Assert.assertEquals(Status.SUCCESS.getCode(),response.getCode().intValue());
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
    public void testVerifyProcessDefinitionName() throws Exception {

        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR);
        String projectName = "test";
        String name = "dag_test";

        Mockito.when(processDefinitionService.verifyProcessDefinitionName(user,projectName,name)).thenReturn(result);

        Result response = processDefinitionController.verifyProcessDefinitionName(user,projectName,name);
        Assert.assertEquals(Status.VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR.getCode(), response.getCode().intValue());

    }

    @Test
    public void updateProcessDefinition() throws Exception {

        String json = "{\"globalParams\":[],"
                + "\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-36196\",\"name\":\"ssh_test1\","
                + "\"params\":{\"resourceList\":[],\"localParams\":[],"
                + "\"rawScript\":\"aa=\\\"1234\\\"\\necho ${aa}\"},\"desc\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},"
                + "\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\","
                + "\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},"
                + "\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":-1,\"timeout\":0}";
        String locations = "{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}";
        String projectName = "test";
        String name = "dag_test";
        String description = "desc test";
        String connects = "[]";
        int id = 1;
        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.SUCCESS);
        result.put("processDefinitionId",1);

        Mockito.when(processDefinitionService.updateProcessDefinition(user, projectName, id,name, json,
                description, locations, connects)).thenReturn(result);

        Result response = processDefinitionController.updateProcessDefinition(user, projectName, name,id, json,
                locations, connects, description);
        Assert.assertEquals(Status.SUCCESS.getCode(),response.getCode().intValue());
    }

    @Test
    public void testReleaseProcessDefinition() throws Exception {
        String projectName = "test";
        int id = 1;
        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.releaseProcessDefinition(user, projectName,id,ReleaseState.OFFLINE.ordinal())).thenReturn(result);
        Result response = processDefinitionController.releaseProcessDefinition(user, projectName,id,ReleaseState.OFFLINE.ordinal());
        Assert.assertEquals(Status.SUCCESS.getCode(),response.getCode().intValue());
    }

    @Test
    public void testQueryProcessDefinitionById() throws Exception {

        String json = "{\"globalParams\":[],"
                + "\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-36196\","
                + "\"name\":\"ssh_test1\","
                + "\"params\":{\"resourceList\":[],\"localParams\":[],"
                + "\"rawScript\":\"aa=\\\"1234\\\"\\necho ${aa}\"},\"desc\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},"
                + "\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},"
                + "\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":-1,\"timeout\":0}";
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
        processDefinition.setProcessDefinitionJson(json);

        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, processDefinition);

        Mockito.when(processDefinitionService.queryProcessDefinitionById(user, projectName,id)).thenReturn(result);
        Result response = processDefinitionController.queryProcessDefinitionById(user, projectName,id);

        Assert.assertEquals(Status.SUCCESS.getCode(),response.getCode().intValue());
    }

    @Test
    public void testCopyProcessDefinition() throws Exception {

        String projectName = "test";
        int id = 1;

        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.copyProcessDefinition(user, projectName,id)).thenReturn(result);
        Result response = processDefinitionController.copyProcessDefinition(user, projectName,id);

        Assert.assertEquals(Status.SUCCESS.getCode(),response.getCode().intValue());
    }

    @Test
    public void testQueryProcessDefinitionList() throws Exception {

        String projectName = "test";
        List<ProcessDefinition> resourceList =  getDefinitionList();

        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, resourceList);

        Mockito.when(processDefinitionService.queryProcessDefinitionList(user, projectName)).thenReturn(result);
        Result response = processDefinitionController.queryProcessDefinitionList(user, projectName);

        Assert.assertEquals(Status.SUCCESS.getCode(),response.getCode().intValue());
    }

    public List<ProcessDefinition> getDefinitionList() {

        List<ProcessDefinition> resourceList = new ArrayList<>();

        String json = "{\"globalParams\":[],"
                + "\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-36196\","
                + "\"name\":\"ssh_test1\","
                + "\"params\":{\"resourceList\":[],\"localParams\":[],"
                + "\"rawScript\":\"aa=\\\"1234\\\"\\necho ${aa}\"},\"desc\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},"
                + "\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},"
                + "\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,\""
                + "preTasks\":[]}],\"tenantId\":-1,\"timeout\":0}";
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
        processDefinition.setProcessDefinitionJson(json);

        String name2 = "dag_test";
        int id2 = 2;

        ProcessDefinition processDefinition2 = new ProcessDefinition();
        processDefinition2.setProjectName(projectName);
        processDefinition2.setConnects(connects);
        processDefinition2.setDescription(description);
        processDefinition2.setId(id2);
        processDefinition2.setLocations(locations);
        processDefinition2.setName(name2);
        processDefinition2.setProcessDefinitionJson(json);

        resourceList.add(processDefinition);
        resourceList.add(processDefinition2);

        return  resourceList;
    }

    @Test
    public void testDeleteProcessDefinitionById() throws Exception {
        String projectName = "test";
        int id = 1;

        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.deleteProcessDefinitionById(user, projectName,id)).thenReturn(result);
        Result response = processDefinitionController.deleteProcessDefinitionById(user, projectName,id);

        Assert.assertEquals(Status.SUCCESS.getCode(),response.getCode().intValue());
    }

    @Test
    public void testGetNodeListByDefinitionId() throws Exception {
        String projectName = "test";
        int id = 1;

        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.getTaskNodeListByDefinitionId(id)).thenReturn(result);
        Result response = processDefinitionController.getNodeListByDefinitionId(user,projectName,id);

        Assert.assertEquals(Status.SUCCESS.getCode(),response.getCode().intValue());
    }

    @Test
    public void testGetNodeListByDefinitionIdList() throws Exception {
        String projectName = "test";
        String idList = "1,2,3";

        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.getTaskNodeListByDefinitionIdList(idList)).thenReturn(result);
        Result response = processDefinitionController.getNodeListByDefinitionIdList(user,projectName,idList);

        Assert.assertEquals(Status.SUCCESS.getCode(),response.getCode().intValue());
    }

    @Test
    public void testQueryProcessDefinitionAllByProjectId() throws Exception {
        int projectId = 1;
        Map<String,Object> result = new HashMap<>();
        putMsg(result,Status.SUCCESS);

        Mockito.when(processDefinitionService.queryProcessDefinitionAllByProjectId(projectId)).thenReturn(result);
        Result response = processDefinitionController.queryProcessDefinitionAllByProjectId(user,projectId);

        Assert.assertEquals(Status.SUCCESS.getCode(),response.getCode().intValue());
    }

    @Test
    public void testViewTree() throws Exception {
        String projectName = "test";
        int processId = 1;
        int limit = 2;
        Map<String,Object> result = new HashMap<>();
        putMsg(result,Status.SUCCESS);

        Mockito.when(processDefinitionService.viewTree(processId,limit)).thenReturn(result);
        Result response = processDefinitionController.viewTree(user,projectName,processId,limit);

        Assert.assertEquals(Status.SUCCESS.getCode(),response.getCode().intValue());
    }

    @Test
    public void testQueryProcessDefinitionListPaging() throws Exception {
        String projectName = "test";
        int pageNo = 1;
        int pageSize = 10;
        String searchVal = "";
        int userId = 1;

        Map<String,Object> result = new HashMap<>();
        putMsg(result,Status.SUCCESS);
        result.put(Constants.DATA_LIST,new PageInfo<Resource>(1,10));

        Mockito.when(processDefinitionService.queryProcessDefinitionListPaging(user,projectName, searchVal, pageNo, pageSize, userId)).thenReturn(result);
        Result response = processDefinitionController.queryProcessDefinitionListPaging(user,projectName,pageNo,searchVal,userId,pageSize);

        Assert.assertEquals(Status.SUCCESS.getCode(),response.getCode().intValue());
    }

    @Test
    public void testBatchExportProcessDefinitionByIds() throws Exception {

        String processDefinitionIds = "1,2";
        String projectName = "test";
        HttpServletResponse response = new MockHttpServletResponse();
        ProcessDefinitionService service = new ProcessDefinitionService();
        ProcessDefinitionService spy = Mockito.spy(service);
        Mockito.doNothing().when(spy).batchExportProcessDefinitionByIds(user, projectName, processDefinitionIds, response);
        processDefinitionController.batchExportProcessDefinitionByIds(user, projectName, processDefinitionIds, response);
    }

}
