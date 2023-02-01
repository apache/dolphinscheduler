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

import static org.apache.dolphinscheduler.common.constants.Constants.DATA_LIST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.apache.dolphinscheduler.api.controller.AbstractControllerTest;
import org.apache.dolphinscheduler.api.dto.workflowInstance.WorkflowInstanceQueryRequest;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

public class WorkflowInstanceV2ControllerTest extends AbstractControllerTest {

    @InjectMocks
    private WorkflowInstanceV2Controller workflowInstanceV2Controller;

    @Mock
    private ProcessInstanceService processInstanceService;

    @Mock
    private ExecutorService execService;

    @Test
    public void testQueryWorkFlowInstanceListPaging() {
        User loginUser = getLoginUser();

        WorkflowInstanceQueryRequest workflowInstanceQueryRequest = new WorkflowInstanceQueryRequest();
        workflowInstanceQueryRequest.setProjectName("test");
        workflowInstanceQueryRequest.setWorkflowName("shell");
        workflowInstanceQueryRequest.setPageNo(1);
        workflowInstanceQueryRequest.setPageSize(10);

        Result result = new Result();
        PageInfo<ProcessInstance> pageInfo =
                new PageInfo<>(workflowInstanceQueryRequest.getPageNo(), workflowInstanceQueryRequest.getPageSize());
        pageInfo.setTotalList(Collections.singletonList(new ProcessInstance()));
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);

        Mockito.when(processInstanceService.queryProcessInstanceList(any(),
                any(WorkflowInstanceQueryRequest.class))).thenReturn(result);

        Result result1 =
                workflowInstanceV2Controller.queryWorkflowInstanceListPaging(loginUser, workflowInstanceQueryRequest);
        Assertions.assertTrue(result1.isSuccess());
    }

    @Test
    public void testQueryWorkflowInstanceById() {
        User loginUser = getLoginUser();

        Map<String, Object> result = new HashMap<>();
        result.put(DATA_LIST, new ProcessInstance());
        putMsg(result, Status.SUCCESS);

        Mockito.when(processInstanceService.queryProcessInstanceById(any(), eq(1))).thenReturn(result);
        Result result1 = workflowInstanceV2Controller.queryWorkflowInstanceById(loginUser, 1);
        Assertions.assertTrue(result1.isSuccess());
    }

    @Test
    public void testDeleteWorkflowInstanceById() {
        User loginUser = getLoginUser();

        Mockito.doNothing().when(processInstanceService).deleteProcessInstanceById(any(), eq(1));
        Result result = workflowInstanceV2Controller.deleteWorkflowInstance(loginUser, 1);
        Assertions.assertTrue(result.isSuccess());
    }

    @Test
    public void testExecuteWorkflowInstance() {
        User loginUser = getLoginUser();

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Mockito.when(execService.execute(any(), eq(1), any(ExecuteType.class))).thenReturn(result);

        Result result1 = workflowInstanceV2Controller.execute(loginUser, 1, ExecuteType.STOP);
        Assertions.assertTrue(result1.isSuccess());
    }

    private User getLoginUser() {
        User user = new User();
        user.setId(1);
        user.setUserName("admin");
        return user;
    }
}
