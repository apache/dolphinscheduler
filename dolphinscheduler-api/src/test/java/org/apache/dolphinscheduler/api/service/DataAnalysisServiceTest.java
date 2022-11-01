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
import static org.mockito.ArgumentMatchers.anyLong;

import org.apache.dolphinscheduler.api.dto.CommandStateCount;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.DataAnalysisServiceImpl;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.UserType;
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
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * data analysis service test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DataAnalysisServiceTest {

    private static final Logger baseServiceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);

    private static final Logger serviceLogger = LoggerFactory.getLogger(DataAnalysisServiceImpl.class);

    @InjectMocks
    private DataAnalysisServiceImpl dataAnalysisServiceImpl;

    @Mock
    ProjectMapper projectMapper;

    @Mock
    ProjectService projectService;

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
    private ResourcePermissionCheckService resourcePermissionCheckService;

    private Map<String, Object> resultMap;

    private User user;

    @BeforeEach
    public void setUp() {

        user = new User();
        user.setId(1);
        Project project = new Project();
        project.setId(1);
        project.setName("test");
        resultMap = new HashMap<>();

        Mockito.when(projectMapper.queryByCode(1L)).thenReturn(project);
    }

    @AfterEach
    public void after() {
        user = null;
        projectMapper = null;
        resultMap = null;
    }

    @Test
    public void testCountTaskStateByProject_success() {
        String startDate = "2020-02-11 16:02:18";
        String endDate = "2020-02-11 16:03:18";

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, null);
        Mockito.when(projectService.checkProjectAndAuth(any(), any(), anyLong(), any())).thenReturn(result);
        Mockito.when(projectMapper.queryByCode(1L)).thenReturn(getProject("test"));

        // SUCCESS
        result = dataAnalysisServiceImpl.countTaskStateByProject(user, 1, startDate, endDate);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testCountTaskStateByProject_projectNotFound() {
        String startDate = "2020-02-11 16:02:18";
        String endDate = "2020-02-11 16:03:18";

        // checkProject false
        Map<String, Object> failResult = new HashMap<>();
        putMsg(failResult, Status.PROJECT_NOT_FOUND, 1);
        Mockito.when(projectService.checkProjectAndAuth(any(), any(), anyLong(), any())).thenReturn(failResult);
        failResult = dataAnalysisServiceImpl.countTaskStateByProject(user, 1, startDate, endDate);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, failResult.get(Constants.STATUS));
    }

    @Test
    public void testCountTaskStateByProject_paramValid() {
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, null);
        Mockito.when(projectService.checkProjectAndAuth(any(), any(), anyLong(), any())).thenReturn(result);
        Mockito.when(projectMapper.queryByCode(1L)).thenReturn(getProject("test"));

        // when date in illegal format then return error message
        String startDate2 = "illegalDateString";
        String endDate2 = "illegalDateString";
        result = dataAnalysisServiceImpl.countTaskStateByProject(user, 1, startDate2, endDate2);
        Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

        // when one of date in illegal format then return error message
        String startDate3 = "2020-08-28 14:13:40";
        String endDate3 = "illegalDateString";
        result = dataAnalysisServiceImpl.countTaskStateByProject(user, 1, startDate3, endDate3);
        Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

        // when one of date in illegal format then return error message
        String startDate4 = "illegalDateString";
        String endDate4 = "2020-08-28 14:13:40";
        result = dataAnalysisServiceImpl.countTaskStateByProject(user, 1, startDate4, endDate4);
        Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));
    }

    @Test
    public void testCountTaskStateByProject_allCountZero() {
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, null);
        Mockito.when(projectService.checkProjectAndAuth(any(), any(), anyLong(), any())).thenReturn(result);
        Mockito.when(projectMapper.queryByCode(1L)).thenReturn(getProject("test"));

        // when general user doesn't have any task then return all count are 0
        user.setUserType(UserType.GENERAL_USER);
        Mockito.when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, 1,
                serviceLogger))
                .thenReturn(projectIds());
        Mockito.when(taskInstanceMapper.countTaskInstanceStateByProjectCodes(any(), any(), any())).thenReturn(
                Collections.emptyList());
        result = dataAnalysisServiceImpl.countTaskStateByProject(user, 1, null, null);
        assertThat(result.get(Constants.DATA_LIST)).extracting("totalCount").isEqualTo(0);
        assertThat(result.get(Constants.DATA_LIST)).extracting("taskCountDtos").asList().hasSameSizeAs(
                TaskExecutionStatus.values());
        assertThat(result.get(Constants.DATA_LIST)).extracting("taskCountDtos").asList().extracting(
                "count").allMatch(count -> count.equals(0));
    }

    @Test
    public void testCountTaskStateByProject_noData() {
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, null);
        Mockito.when(projectService.checkProjectAndAuth(any(), any(), anyLong(), any())).thenReturn(result);
        Mockito.when(projectMapper.queryByCode(1L)).thenReturn(getProject("test"));
        Mockito.when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, 1,
                serviceLogger)).thenReturn(projectIds());

        // when instanceStateCounter return null, then return nothing
        user.setUserType(UserType.GENERAL_USER);
        Mockito.when(taskInstanceMapper.countTaskInstanceStateByProjectCodes(any(), any(), any()))
                .thenReturn(null);
        result = dataAnalysisServiceImpl.countTaskStateByProject(user, 1, null, null);
        Assertions.assertNull(result.get(Constants.DATA_LIST));
    }

    @Test
    public void testCountProcessInstanceStateByProject() {
        String startDate = "2020-02-11 16:02:18";
        String endDate = "2020-02-11 16:03:18";

        Mockito.when(projectMapper.queryByCode(1L)).thenReturn(getProject("test"));

        // checkProject false
        Map<String, Object> failResult = new HashMap<>();
        putMsg(failResult, Status.PROJECT_NOT_FOUND, 1);
        Mockito.when(projectService.checkProjectAndAuth(any(), any(), anyLong(), any())).thenReturn(failResult);
        failResult = dataAnalysisServiceImpl.countProcessInstanceStateByProject(user, 1, startDate, endDate);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, failResult.get(Constants.STATUS));

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, null);
        Mockito.when(projectService.checkProjectAndAuth(any(), any(), anyLong(), any())).thenReturn(result);

        // SUCCESS
        result = dataAnalysisServiceImpl.countProcessInstanceStateByProject(user, 1, startDate, endDate);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testCountDefinitionByUser() {

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, null);

        Mockito.when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, 1,
                serviceLogger)).thenReturn(projectIds());
        result = dataAnalysisServiceImpl.countDefinitionByUser(user, 0);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        Mockito.when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, 1,
                serviceLogger)).thenReturn(Collections.emptySet());
        result = dataAnalysisServiceImpl.countDefinitionByUser(user, 0);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testCountCommandState() {
        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
        user.setId(1);

        Map<String, Object> result = dataAnalysisServiceImpl.countCommandState(user);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        // when no command found then return all count are 0
        Mockito.when(commandMapper.countCommandState(any(), any(), any())).thenReturn(Collections.emptyList());
        Mockito.when(errorCommandMapper.countCommandState(any(), any(), any())).thenReturn(Collections.emptyList());
        Mockito.when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, 1,
                serviceLogger)).thenReturn(projectIds());

        Map<String, Object> result5 = dataAnalysisServiceImpl.countCommandState(user);
        assertThat(result5).containsEntry(Constants.STATUS, Status.SUCCESS);
        assertThat(result5.get(Constants.DATA_LIST)).asList().extracting("errorCount")
                .allMatch(count -> count.equals(0));
        assertThat(result5.get(Constants.DATA_LIST)).asList().extracting("normalCount")
                .allMatch(count -> count.equals(0));

        // when command found then return combination result
        CommandCount normalCommandCount = new CommandCount();
        normalCommandCount.setCommandType(CommandType.START_PROCESS);
        normalCommandCount.setCount(10);
        CommandCount errorCommandCount = new CommandCount();
        errorCommandCount.setCommandType(CommandType.START_PROCESS);
        errorCommandCount.setCount(5);
        Mockito.when(commandMapper.countCommandState(any(), any(), any()))
                .thenReturn(Collections.singletonList(normalCommandCount));
        Mockito.when(errorCommandMapper.countCommandState(any(), any(), any()))
                .thenReturn(Collections.singletonList(errorCommandCount));

        Map<String, Object> result6 = dataAnalysisServiceImpl.countCommandState(user);

        assertThat(result6).containsEntry(Constants.STATUS, Status.SUCCESS);
        CommandStateCount commandStateCount = new CommandStateCount();
        commandStateCount.setCommandState(CommandType.START_PROCESS);
        commandStateCount.setNormalCount(10);
        commandStateCount.setErrorCount(5);
        assertThat(result6.get(Constants.DATA_LIST)).asList().containsOnlyOnce(commandStateCount);
    }

    @Test
    public void testCountQueueState() {
        // when project check success when return all count are 0
        Map<String, Object> result2 = dataAnalysisServiceImpl.countQueueState(user);
        assertThat(result2.get(Constants.DATA_LIST)).extracting("taskQueue", "taskKill")
                .isNotEmpty()
                .allMatch(count -> count.equals(0));
    }

    private Set<Integer> projectIds() {
        Set<Integer> projectIds = new HashSet<>();
        projectIds.add(1);
        return projectIds;
    }

    /**
     * get list
     */
    private List<ExecuteStatusCount> getTaskInstanceStateCounts() {
        List<ExecuteStatusCount> taskInstanceStateCounts = new ArrayList<>(1);
        ExecuteStatusCount executeStatusCount = new ExecuteStatusCount();
        executeStatusCount.setState(TaskExecutionStatus.RUNNING_EXECUTION);
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
        project.setCode(1L);
        project.setId(1);
        project.setName(projectName);
        project.setUserId(1);
        return project;
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
