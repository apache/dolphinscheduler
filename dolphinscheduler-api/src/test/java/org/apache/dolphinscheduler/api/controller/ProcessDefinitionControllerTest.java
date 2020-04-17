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
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * process definition controller test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class ProcessDefinitionControllerTest{

    private static Logger logger = LoggerFactory.getLogger(ProcessDefinitionControllerTest.class);

    @InjectMocks
    private ProcessDefinitionController processDefinitionController;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    protected User user;

    @Before
    public void before(){
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("admin");

        user = loginUser;
    }

    @Test
    public void testCreateProcessDefinition() throws Exception {
        String json = "{\"globalParams\":[],\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-36196\",\"name\":\"ssh_test1\",\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"aa=\\\"1234\\\"\\necho ${aa}\"},\"desc\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":-1,\"timeout\":0}";
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
        putMsg(result, Status.PROCESS_INSTANCE_EXIST);
        String projectName = "test";
        String name = "dag_test";

        Mockito.when(processDefinitionService.verifyProcessDefinitionName(user,projectName,name)).thenReturn(result);

        Result response = processDefinitionController.verifyProcessDefinitionName(user,projectName,name);
        Assert.assertEquals(Status.PROCESS_INSTANCE_EXIST.getCode(),response.getCode().intValue());

    }

    @Test
    public void UpdateProcessDefinition() throws Exception {

        String json = "{\"globalParams\":[],\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-36196\",\"name\":\"ssh_test1\",\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"aa=\\\"1234\\\"\\necho ${aa}\"},\"desc\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":-1,\"timeout\":0}";
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
}
