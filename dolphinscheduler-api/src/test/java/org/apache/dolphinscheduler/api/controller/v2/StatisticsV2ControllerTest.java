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

import org.apache.dolphinscheduler.api.controller.AbstractControllerTest;
import org.apache.dolphinscheduler.api.dto.TaskCountDto;
import org.apache.dolphinscheduler.api.dto.project.StatisticsStateRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.DataAnalysisServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

public class StatisticsV2ControllerTest extends AbstractControllerTest {

    @InjectMocks
    private StatisticsV2Controller statisticsV2Controller;

    @Mock
    private DataAnalysisServiceImpl dataAnalysisService;

    @Test
    public void testQueryWorkflowInstanceCounts() {
        User loginUser = getLoginUser();
        int count = 0;
        Map<String, Object> result = new HashMap<>();
        result.put("data", "AllWorkflowCounts = " + count);
        putMsg(result, Status.SUCCESS);

        Mockito.when(dataAnalysisService.queryAllWorkflowCounts(loginUser)).thenReturn(result);

        Result result1 = statisticsV2Controller.queryWorkflowInstanceCounts(loginUser);

        Assertions.assertTrue(result1.isSuccess());

    }
    @Test
    public void testQueryWorkflowStatesCounts() {
        User loginUser = getLoginUser();
        Map<String, Object> result = new HashMap<>();
        StatisticsStateRequest statisticsStateRequest = new StatisticsStateRequest();
        List<ExecuteStatusCount> executeStatusCounts = new ArrayList<>();
        TaskCountDto taskCountResult = new TaskCountDto(executeStatusCounts);
        result.put(Constants.DATA_LIST, taskCountResult);
        putMsg(result, Status.SUCCESS);

        Mockito.when(dataAnalysisService.countWorkflowStates(loginUser, statisticsStateRequest)).thenReturn(result);

        Result result1 = statisticsV2Controller.queryWorkflowStatesCounts(loginUser, statisticsStateRequest);

        Assertions.assertTrue(result1.isSuccess());
    }
    @Test
    public void testQueryOneWorkflowStates() {
        User loginUser = getLoginUser();
        Long workflowCode = 1L;
        Map<String, Object> result = new HashMap<>();
        List<ExecuteStatusCount> executeStatusCounts = new ArrayList<>();
        TaskCountDto taskCountResult = new TaskCountDto(executeStatusCounts);
        result.put(Constants.DATA_LIST, taskCountResult);
        putMsg(result, Status.SUCCESS);

        Mockito.when(dataAnalysisService.countOneWorkflowStates(loginUser, workflowCode)).thenReturn(result);

        Result result1 = statisticsV2Controller.queryOneWorkflowStates(loginUser, workflowCode);

        Assertions.assertTrue(result1.isSuccess());

    }
    @Test
    public void testQueryTaskStatesCounts() {
        User loginUser = getLoginUser();
        Map<String, Object> result = new HashMap<>();
        StatisticsStateRequest statisticsStateRequest = new StatisticsStateRequest();
        List<ExecuteStatusCount> executeStatusCounts = new ArrayList<>();
        TaskCountDto taskCountResult = new TaskCountDto(executeStatusCounts);
        result.put(Constants.DATA_LIST, taskCountResult);
        putMsg(result, Status.SUCCESS);

        Mockito.when(dataAnalysisService.countTaskStates(loginUser, statisticsStateRequest)).thenReturn(result);

        Result result1 = statisticsV2Controller.queryTaskStatesCounts(loginUser, statisticsStateRequest);

        Assertions.assertTrue(result1.isSuccess());

    }
    @Test
    public void testQueryOneTaskStatesCounts() {
        User loginUser = getLoginUser();
        Long taskCode = 1L;
        Map<String, Object> result = new HashMap<>();

        List<ExecuteStatusCount> executeStatusCounts = new ArrayList<>();
        TaskCountDto taskCountResult = new TaskCountDto(executeStatusCounts);
        result.put(Constants.DATA_LIST, taskCountResult);
        putMsg(result, Status.SUCCESS);

        Mockito.when(dataAnalysisService.countOneTaskStates(loginUser, taskCode)).thenReturn(result);

        Result result1 = statisticsV2Controller.queryOneTaskStatesCounts(loginUser, taskCode);

        Assertions.assertTrue(result1.isSuccess());

    }
    @Test
    public void testCountDefinitionByUser() {
        User loginUser = getLoginUser();

        Map<String, Object> result = new HashMap<>();
        StatisticsStateRequest statisticsStateRequest = new StatisticsStateRequest();

        List<ExecuteStatusCount> executeStatusCounts = new ArrayList<>();
        TaskCountDto taskCountResult = new TaskCountDto(executeStatusCounts);
        result.put(Constants.DATA_LIST, taskCountResult);
        putMsg(result, Status.SUCCESS);
        Mockito.when(dataAnalysisService.countDefinitionByUserV2(loginUser, statisticsStateRequest.getProjectCode(),
                null, null)).thenReturn(result);

        Result result1 = statisticsV2Controller.countDefinitionByUser(loginUser, statisticsStateRequest);

        Assertions.assertTrue(result1.isSuccess());

    }
    @Test
    public void testCountDefinitionByUserId() {
        User loginUser = getLoginUser();
        Map<String, Object> result = new HashMap<>();
        Integer userId = 1;

        putMsg(result, Status.SUCCESS);

        Mockito.when(dataAnalysisService.countDefinitionByUserV2(loginUser, null, userId, null)).thenReturn(result);

        Result result1 = statisticsV2Controller.countDefinitionByUserId(loginUser, userId);

        Assertions.assertTrue(result1.isSuccess());
    }

    private User getLoginUser() {
        User user = new User();
        user.setId(1);
        user.setUserName("admin");
        return user;
    }
}
