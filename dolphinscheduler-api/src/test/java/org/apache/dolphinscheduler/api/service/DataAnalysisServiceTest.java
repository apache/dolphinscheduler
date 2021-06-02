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

package org.apache.dolphinscheduler.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import org.apache.dolphinscheduler.api.dto.CommandStateCount;
import org.apache.dolphinscheduler.api.dto.TaskStateCount;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.DataAnalysisServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.CommandCount;
import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ErrorCommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * data analysis service test
 */
@RunWith(PowerMockRunner.class)
public class DataAnalysisServiceTest {

    @InjectMocks
    private DataAnalysisServiceImpl dataAnalysisService;

    @Mock
    ProjectMapper projectMapper;

    @Mock
    ProjectServiceImpl projectService;

    @Mock
    ProcessInstanceMapper processInstanceMapper;

    @Mock
    ProcessDefinitionMapper processDefinitionMapper;

    @Mock
    CommandMapper commandMapper;

    @Mock
    ErrorCommandMapper errorCommandMapper;

    @Mock
    TaskInstanceMapper taskInstanceMapper;

    @Mock
    ProcessService processService;

    private Map<String, Object> resultMap;

    private User user;

    @Before
    public void setUp() {

        user = new User();
        Project project = new Project();
        project.setId(1);
        resultMap = new HashMap<>();
        Mockito.when(projectMapper.selectById(1)).thenReturn(project);
        Mockito.when(projectService.hasProjectAndPerm(user, project, resultMap)).thenReturn(true);

    }


    @After
    public void after() {

        user = null;
        projectMapper = null;
        resultMap = null;
    }

    @Test
    public void testCountTaskStateByProject() {

        String startDate = "2020-02-11 16:02:18";
        String endDate = "2020-02-11 16:03:18";

        //checkProject false
        Map<String, Object> result = dataAnalysisService.countTaskStateByProject(user, 2, startDate, endDate);
        Assert.assertTrue(result.isEmpty());


        //SUCCESS
        Mockito.when(taskInstanceMapper.countTaskInstanceStateByUser(DateUtils.getScheduleDate(startDate),
                DateUtils.getScheduleDate(endDate), new Long[]{1L})).thenReturn(getTaskInstanceStateCounts());
        Mockito.when(projectMapper.selectById(Mockito.any())).thenReturn(getProject("test"));
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

        result = dataAnalysisService.countTaskStateByProject(user, 1, startDate, endDate);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        // when date in illegal format then return error message
        String startDate2 = "illegalDateString";
        String endDate2 = "illegalDateString";
        result = dataAnalysisService.countTaskStateByProject(user, 0, startDate2, endDate2);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

        // when one of date in illegal format then return error message
        String startDate3 = "2020-08-28 14:13:40";
        String endDate3 = "illegalDateString";
        result = dataAnalysisService.countTaskStateByProject(user, 0, startDate3, endDate3);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

        // when one of date in illegal format then return error message
        String startDate4 = "illegalDateString";
        String endDate4 = "2020-08-28 14:13:40";
        result = dataAnalysisService.countTaskStateByProject(user, 0, startDate4, endDate4);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

        // when counting general user's task status then return user's task status count
        user.setUserType(UserType.GENERAL_USER);
        Mockito.when(processService.getProjectIdListHavePerm(anyInt()))
                .thenReturn(Collections.singletonList(123L));
        ExecuteStatusCount executeStatusCount = new ExecuteStatusCount();
        executeStatusCount.setExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
        executeStatusCount.setCount(10);
        Mockito.when(taskInstanceMapper.countTaskInstanceStateByUser(any(), any(), any()))
                .thenReturn(Collections.singletonList(executeStatusCount));
        result = dataAnalysisService.countTaskStateByProject(user, 0, startDate, null);
        assertThat(result.get(Constants.DATA_LIST)).extracting("taskCountDtos").first().asList()
                .hasSameSizeAs(ExecutionStatus.values());
        assertThat(result.get(Constants.DATA_LIST)).extracting("totalCount").first().isEqualTo(10);
        TaskStateCount taskStateCount = new TaskStateCount(ExecutionStatus.RUNNING_EXECUTION, 10);
        assertThat(result.get(Constants.DATA_LIST)).extracting("taskCountDtos").first().asList().containsOnlyOnce(taskStateCount);

        // when general user doesn't have any task then return all count are 0
        user.setUserType(UserType.GENERAL_USER);
        Mockito.when(processService.getProjectIdListHavePerm(anyInt()))
                .thenReturn(new ArrayList<>());
        Mockito.when(taskInstanceMapper.countTaskInstanceStateByUser(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        result = dataAnalysisService.countTaskStateByProject(user, 0, null, null);
        assertThat(result.get(Constants.DATA_LIST)).extracting("totalCount").first().isEqualTo(0);
        assertThat(result.get(Constants.DATA_LIST)).extracting("taskCountDtos").first().asList()
                .hasSameSizeAs(ExecutionStatus.values());
        assertThat(result.get(Constants.DATA_LIST)).extracting("taskCountDtos").first().asList()
                .extracting("count").allMatch(count -> count.equals(0));

        // when general user doesn't have any task then return all count are 0
        user.setUserType(UserType.GENERAL_USER);
        Mockito.when(processService.getProjectIdListHavePerm(anyInt()))
                .thenReturn(new ArrayList<>());
        Mockito.when(taskInstanceMapper.countTaskInstanceStateByUser(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        result = dataAnalysisService.countTaskStateByProject(user, 0, null, null);
        assertThat(result.get(Constants.DATA_LIST)).extracting("totalCount").first().isEqualTo(0);
        assertThat(result.get(Constants.DATA_LIST)).extracting("taskCountDtos").first().asList()
                .hasSameSizeAs(ExecutionStatus.values());
        assertThat(result.get(Constants.DATA_LIST)).extracting("taskCountDtos").first().asList()
                .extracting("count").allMatch(count -> count.equals(0));

        // when instanceStateCounter return null, then return nothing
        user.setUserType(UserType.GENERAL_USER);
        Mockito.when(processService.getProjectIdListHavePerm(anyInt()))
                .thenReturn(new ArrayList<>());
        Mockito.when(taskInstanceMapper.countTaskInstanceStateByUser(any(), any(), any()))
                .thenReturn(null);
        result = dataAnalysisService.countTaskStateByProject(user, 0, null, null);
        assertThat(result).isEmpty();
    }

    @Test
    public void testCountProcessInstanceStateByProject() {

        String startDate = "2020-02-11 16:02:18";
        String endDate = "2020-02-11 16:03:18";
        //checkProject false
        Map<String, Object> result = dataAnalysisService.countProcessInstanceStateByProject(user, 2, startDate, endDate);
        Assert.assertTrue(result.isEmpty());

        //SUCCESS
        Mockito.when(projectMapper.selectById(Mockito.any())).thenReturn(getProject("test"));
        Mockito.when(processInstanceMapper.countInstanceStateByUser(DateUtils.getScheduleDate(startDate),
                DateUtils.getScheduleDate(endDate), new Long[]{1L})).thenReturn(getTaskInstanceStateCounts());
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

        result = dataAnalysisService.countProcessInstanceStateByProject(user, 1, startDate, endDate);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testCountDefinitionByUser() {
        Mockito.when(projectMapper.selectById(Mockito.any())).thenReturn(getProject("test"));
        Map<String, Object> result = dataAnalysisService.countDefinitionByUser(user, 0);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testCountCommandState() {

        String startDate = "2020-02-11 16:02:18";
        String endDate = "2020-02-11 16:03:18";
        //checkProject false
        Map<String, Object> result = dataAnalysisService.countCommandState(user, 2, startDate, endDate);
        Assert.assertTrue(result.isEmpty());
        List<CommandCount> commandCounts = new ArrayList<>(1);
        CommandCount commandCount = new CommandCount();
        commandCount.setCommandType(CommandType.START_PROCESS);
        commandCounts.add(commandCount);
        Mockito.when(commandMapper.countCommandState(0, DateUtils.getScheduleDate(startDate),
                DateUtils.getScheduleDate(endDate), new Long[]{1L})).thenReturn(commandCounts);

        Mockito.when(errorCommandMapper.countCommandState(DateUtils.getScheduleDate(startDate),
                DateUtils.getScheduleDate(endDate), new Long[]{1L})).thenReturn(commandCounts);
        Mockito.when(projectMapper.selectById(Mockito.any())).thenReturn(getProject("test"));
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

        result = dataAnalysisService.countCommandState(user, 1, startDate, endDate);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        // when all date in illegal format then return error message
        String startDate2 = "illegalDateString";
        String endDate2 = "illegalDateString";
        Map<String, Object> result2 = dataAnalysisService.countCommandState(user, 0, startDate2, endDate2);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result2.get(Constants.STATUS));

        // when one of date in illegal format then return error message
        String startDate3 = "2020-08-22 09:23:10";
        String endDate3 = "illegalDateString";
        Map<String, Object> result3 = dataAnalysisService.countCommandState(user, 0, startDate3, endDate3);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result3.get(Constants.STATUS));

        // when one of date in illegal format then return error message
        String startDate4 = "illegalDateString";
        String endDate4 = "2020-08-22 09:23:10";
        Map<String, Object> result4 = dataAnalysisService.countCommandState(user, 0, startDate4, endDate4);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result4.get(Constants.STATUS));

        // when no command found then return all count are 0
        Mockito.when(commandMapper.countCommandState(anyInt(), any(), any(), any())).thenReturn(Collections.emptyList());
        Mockito.when(errorCommandMapper.countCommandState(any(), any(), any())).thenReturn(Collections.emptyList());
        Map<String, Object> result5 = dataAnalysisService.countCommandState(user, 0, startDate, null);
        assertThat(result5).containsEntry(Constants.STATUS, Status.SUCCESS);
        assertThat(result5.get(Constants.DATA_LIST)).asList().extracting("errorCount").allMatch(count -> count.equals(0));
        assertThat(result5.get(Constants.DATA_LIST)).asList().extracting("normalCount").allMatch(count -> count.equals(0));

        // when command found then return combination result
        CommandCount normalCommandCount = new CommandCount();
        normalCommandCount.setCommandType(CommandType.START_PROCESS);
        normalCommandCount.setCount(10);
        CommandCount errorCommandCount = new CommandCount();
        errorCommandCount.setCommandType(CommandType.START_PROCESS);
        errorCommandCount.setCount(5);
        Mockito.when(commandMapper.countCommandState(anyInt(), any(), any(), any())).thenReturn(Collections.singletonList(normalCommandCount));
        Mockito.when(errorCommandMapper.countCommandState(any(), any(), any())).thenReturn(Collections.singletonList(errorCommandCount));

        Map<String, Object> result6 = dataAnalysisService.countCommandState(user, 0, null, null);

        assertThat(result6).containsEntry(Constants.STATUS, Status.SUCCESS);
        CommandStateCount commandStateCount = new CommandStateCount();
        commandStateCount.setCommandState(CommandType.START_PROCESS);
        commandStateCount.setNormalCount(10);
        commandStateCount.setErrorCount(5);
        assertThat(result6.get(Constants.DATA_LIST)).asList().containsOnlyOnce(commandStateCount);
    }

    @Test
    public void testCountQueueState() {
        // when project check fail then return nothing
        Map<String, Object> result1 = dataAnalysisService.countQueueState(user, 2);
        Assert.assertTrue(result1.isEmpty());

        // when project check success when return all count are 0
        Map<String, Object> result2 = dataAnalysisService.countQueueState(user, 1);
        assertThat(result2.get(Constants.DATA_LIST)).extracting("taskQueue", "taskKill")
                .isNotEmpty()
                .allMatch(count -> count.equals(0));
    }

    /**
     * get list
     */
    private List<ExecuteStatusCount> getTaskInstanceStateCounts() {

        List<ExecuteStatusCount> taskInstanceStateCounts = new ArrayList<>(1);
        ExecuteStatusCount executeStatusCount = new ExecuteStatusCount();
        executeStatusCount.setExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
        taskInstanceStateCounts.add(executeStatusCount);

        return taskInstanceStateCounts;
    }

    /**
     * get mock Project
     *
     * @param projectName projectName
     * @return Project
     */
    private Project getProject(String projectName) {
        Project project = new Project();
        project.setCode(11L);
        project.setId(1);
        project.setName(projectName);
        project.setUserId(1);
        return project;
    }
}