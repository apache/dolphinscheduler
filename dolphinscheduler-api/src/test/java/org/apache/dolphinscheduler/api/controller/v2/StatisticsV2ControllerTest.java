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

import static org.apache.dolphinscheduler.api.AssertionsHelper.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.controller.AbstractControllerTest;
import org.apache.dolphinscheduler.api.dto.DefineUserDto;
import org.apache.dolphinscheduler.api.dto.TaskCountDto;
import org.apache.dolphinscheduler.api.dto.project.StatisticsStateRequest;
import org.apache.dolphinscheduler.api.service.impl.DataAnalysisServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class StatisticsV2ControllerTest extends AbstractControllerTest {

    @InjectMocks
    private StatisticsV2Controller statisticsV2Controller;

    @Mock
    private DataAnalysisServiceImpl dataAnalysisService;

    @Test
    public void testQueryWorkflowStatesCounts() {
        User loginUser = getLoginUser();
        StatisticsStateRequest statisticsStateRequest = new StatisticsStateRequest();
        List<ExecuteStatusCount> executeStatusCounts = new ArrayList<>();
        TaskCountDto taskCountResult = new TaskCountDto(executeStatusCounts);

        when(dataAnalysisService.countWorkflowStates(loginUser, statisticsStateRequest)).thenReturn(taskCountResult);

        Result result1 = statisticsV2Controller.queryWorkflowStatesCounts(loginUser, statisticsStateRequest);

        assertTrue(result1.isSuccess());
    }
    @Test
    public void testQueryOneWorkflowStates() {
        User loginUser = getLoginUser();
        Long workflowCode = 1L;
        List<ExecuteStatusCount> executeStatusCounts = new ArrayList<>();
        TaskCountDto taskCountResult = new TaskCountDto(executeStatusCounts);

        when(dataAnalysisService.countOneWorkflowStates(loginUser, workflowCode)).thenReturn(taskCountResult);

        Result result1 = statisticsV2Controller.queryOneWorkflowStates(loginUser, workflowCode);

        assertTrue(result1.isSuccess());

    }
    @Test
    public void testQueryTaskStatesCounts() {
        User loginUser = getLoginUser();
        StatisticsStateRequest statisticsStateRequest = new StatisticsStateRequest();
        List<ExecuteStatusCount> executeStatusCounts = new ArrayList<>();
        TaskCountDto taskCountResult = new TaskCountDto(executeStatusCounts);

        when(dataAnalysisService.countTaskStates(loginUser, statisticsStateRequest)).thenReturn(taskCountResult);

        Result result1 = statisticsV2Controller.queryTaskStatesCounts(loginUser, statisticsStateRequest);

        assertTrue(result1.isSuccess());

    }
    @Test
    public void testQueryOneTaskStatesCounts() {
        User loginUser = getLoginUser();
        Long taskCode = 1L;

        List<ExecuteStatusCount> executeStatusCounts = new ArrayList<>();
        TaskCountDto taskCountResult = new TaskCountDto(executeStatusCounts);

        when(dataAnalysisService.countOneTaskStates(loginUser, taskCode)).thenReturn(taskCountResult);

        Result result1 = statisticsV2Controller.queryOneTaskStatesCounts(loginUser, taskCode);

        assertTrue(result1.isSuccess());

    }

    @Test
    public void testCountDefinitionByUserId() {
        User loginUser = getLoginUser();
        Integer userId = 1;

        DefineUserDto defineUserDto = new DefineUserDto(Collections.emptyList());
        when(dataAnalysisService.countDefinitionByUserV2(loginUser, userId, null)).thenReturn(defineUserDto);

        assertDoesNotThrow(() -> statisticsV2Controller.countDefinitionByUserId(loginUser, userId));
    }

    private User getLoginUser() {
        User user = new User();
        user.setId(1);
        user.setUserName("admin");
        return user;
    }
}
