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

import org.apache.dolphinscheduler.api.dto.CreateEmptyWorkflowDefinitionRequest;
import org.apache.dolphinscheduler.api.dto.CreateEmptyWorkflowDefinitionResponse;
import org.apache.dolphinscheduler.api.dto.CreateWorkflowDefinitionRequest;
import org.apache.dolphinscheduler.api.dto.CreateWorkflowDefinitionResponse;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ProcessDefinitionServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

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
            + "\"workerGroup\":\"default\",\"failRetryTimes\":0,\"failRetryInterval\":0,\"timeoutFlag\":0,"
            + "\"timeoutNotifyStrategy\":0,\"timeout\":0,\"delayTime\":0,\"resourceIds\":\"\"}]";

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
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, 1);

        Mockito.when(workflowDefinitionService.createProcessDefinition(user, projectCode, name, description, globalParams,
            locations, timeout, tenantCode, relationJson, taskDefinitionJson, "", ProcessExecutionTypeEnum.PARALLEL)).thenReturn(result);

        CreateWorkflowDefinitionResponse response = workflowDefinitionController.createWorkflowDefinition(user, projectCode, request);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
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
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, 1);

        Mockito.when(workflowDefinitionService.createEmptyProcessDefinition(user, projectCode, name, description, globalParams,
            timeout, tenantCode, "", ProcessExecutionTypeEnum.PARALLEL)).thenReturn(result);

        CreateEmptyWorkflowDefinitionResponse response = workflowDefinitionController.createEmptyWorkflowDefinition(user, projectCode, request);
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

}
