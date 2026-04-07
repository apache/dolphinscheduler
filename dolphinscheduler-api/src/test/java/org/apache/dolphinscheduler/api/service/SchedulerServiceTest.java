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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.SchedulerServiceImpl;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;

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

/**
 * scheduler service test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SchedulerServiceTest extends BaseServiceTestTool {

    @InjectMocks
    private SchedulerServiceImpl schedulerService;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Mock
    private ProjectService projectService;

    protected static User user;
    protected Exception exception;
    private static final String userName = "userName";
    private static final String projectName = "projectName";
    private static final long projectCode = 1L;
    private static final int userId = 1;
    private static final String processDefinitionName = "processDefinitionName";
    private static final long processDefinitionCode = 2L;
    private static final int processDefinitionVersion = 3;
    private static final int scheduleId = 3;
    private static final long environmentCode = 4L;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUserName(userName);
        user.setId(userId);
    }

    @Test
    public void testDeleteSchedules() {
        Schedule schedule = this.getSchedule();

        // error schedule not exists
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.deleteSchedulesById(user, scheduleId));
        Assertions.assertEquals(Status.SCHEDULE_NOT_EXISTS.getCode(), ((ServiceException) exception).getCode());

        // error schedule already online
        schedule.setReleaseState(ReleaseState.ONLINE);
        Mockito.when(scheduleMapper.selectById(scheduleId)).thenReturn(schedule);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.deleteSchedulesById(user, scheduleId));
        Assertions.assertEquals(Status.SCHEDULE_STATE_ONLINE.getCode(), ((ServiceException) exception).getCode());
        schedule.setReleaseState(ReleaseState.OFFLINE);

        // error user not own schedule
        int notOwnUserId = 2;
        schedule.setUserId(notOwnUserId);
        Mockito.when(scheduleMapper.selectById(scheduleId)).thenReturn(schedule);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.deleteSchedulesById(user, scheduleId));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM.getMsg(), exception.getMessage());
        schedule.setUserId(userId);

        // error process definition not exists
        Mockito.when(scheduleMapper.selectById(scheduleId)).thenReturn(schedule);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.deleteSchedulesById(user, scheduleId));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST.getCode(),
                ((ServiceException) exception).getCode());

        // error project permissions
        Mockito.when(workflowDefinitionMapper.queryByCode(processDefinitionCode))
                .thenReturn(this.getProcessDefinition());
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(this.getProject());
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM)).when(projectService)
                .checkProjectAndAuthThrowException(user, this.getProject(), null);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.deleteSchedulesById(user, scheduleId));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(),
                ((ServiceException) exception).getCode());

        // error delete mapper
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(user, this.getProject(), null);
        Mockito.when(scheduleMapper.deleteById(scheduleId)).thenReturn(0);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.deleteSchedulesById(user, scheduleId));
        Assertions.assertEquals(Status.DELETE_SCHEDULE_BY_ID_ERROR.getCode(), ((ServiceException) exception).getCode());

        // success
        Mockito.when(scheduleMapper.deleteById(scheduleId)).thenReturn(1);
        Assertions.assertDoesNotThrow(() -> schedulerService.deleteSchedulesById(user, scheduleId));
    }

    private Project getProject() {
        Project project = new Project();
        project.setId(1);
        project.setName(projectName);
        project.setCode(projectCode);
        project.setUserId(userId);
        return project;
    }

    private WorkflowDefinition getProcessDefinition() {
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setName(processDefinitionName);
        workflowDefinition.setCode(processDefinitionCode);
        workflowDefinition.setProjectCode(projectCode);
        workflowDefinition.setVersion(processDefinitionVersion);
        workflowDefinition.setUserId(userId);
        return workflowDefinition;
    }

    private Schedule getSchedule() {
        Schedule schedule = new Schedule();
        schedule.setId(scheduleId);
        schedule.setWorkflowDefinitionCode(processDefinitionCode);
        schedule.setEnvironmentCode(environmentCode);
        schedule.setUserId(userId);
        return schedule;
    }

}
