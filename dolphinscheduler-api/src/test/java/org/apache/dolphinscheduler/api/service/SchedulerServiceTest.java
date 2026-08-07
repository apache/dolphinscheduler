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

import org.apache.dolphinscheduler.api.dto.ScheduleParam;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.SchedulerServiceImpl;
import org.apache.dolphinscheduler.api.validator.TenantExistValidator;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.ScheduleMissedFirePolicy;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.ScheduleDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.scheduler.api.SchedulerApi;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SchedulerServiceTest extends BaseServiceTestTool {

    @InjectMocks
    private SchedulerServiceImpl schedulerService;

    @Mock
    private ScheduleDao scheduleDao;

    @Mock
    private ProjectDao projectDao;

    @Mock
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Mock
    private ProjectService projectService;

    @Mock
    private ExecutorService executorService;

    @Mock
    private TenantExistValidator tenantExistValidator;

    @Mock
    private SchedulerApi schedulerApi;

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
    public void testScheduleParamMissedFirePolicyPresence() {
        String scheduleWithoutPolicy = "{\"startTime\":\"2019-12-16 00:00:00\","
                + "\"endTime\":\"2019-12-17 00:00:00\",\"crontab\":\"0 0 6 * * ? *\"}";
        String scheduleWithPolicy = "{\"startTime\":\"2019-12-16 00:00:00\","
                + "\"endTime\":\"2019-12-17 00:00:00\",\"crontab\":\"0 0 6 * * ? *\","
                + "\"missedFirePolicy\":\"SKIP_MISSED\"}";

        ScheduleParam withoutPolicy = JSONUtils.parseObject(scheduleWithoutPolicy, ScheduleParam.class);
        ScheduleParam withPolicy = JSONUtils.parseObject(scheduleWithPolicy, ScheduleParam.class);

        Assertions.assertEquals(ScheduleMissedFirePolicy.FIRE_ALL_MISSED, withoutPolicy.getMissedFirePolicy());
        Assertions.assertFalse(withoutPolicy.isMissedFirePolicySet());
        Assertions.assertEquals(ScheduleMissedFirePolicy.SKIP_MISSED, withPolicy.getMissedFirePolicy());
        Assertions.assertTrue(withPolicy.isMissedFirePolicySet());
    }

    @ParameterizedTest
    @EnumSource(ScheduleMissedFirePolicy.class)
    public void testInsertScheduleWithMissedFirePolicy(ScheduleMissedFirePolicy missedFirePolicy) {
        Project project = this.getProject();
        WorkflowDefinition workflowDefinition = this.getProcessDefinition();
        Schedule insertedSchedule = new Schedule();
        insertedSchedule.setId(scheduleId);
        Mockito.when(projectDao.queryByCode(projectCode)).thenReturn(project);
        Mockito.when(scheduleDao.queryByWorkflowDefinitionCode(processDefinitionCode)).thenReturn(null);
        Mockito.when(workflowDefinitionDao.queryByCode(processDefinitionCode))
                .thenReturn(Optional.of(workflowDefinition));
        Mockito.when(scheduleDao.queryById(Mockito.any())).thenReturn(insertedSchedule);

        Schedule result = schedulerService.insertSchedule(
                user, projectCode, processDefinitionCode, scheduleExpression(missedFirePolicy), WarningType.NONE, 0,
                FailureStrategy.CONTINUE, Priority.MEDIUM, "default", "tenantCode", environmentCode);

        ArgumentCaptor<Schedule> scheduleCaptor = ArgumentCaptor.forClass(Schedule.class);
        Mockito.verify(scheduleDao).insert(scheduleCaptor.capture());
        Assertions.assertEquals(missedFirePolicy, scheduleCaptor.getValue().getMissedFirePolicy());
        Assertions.assertSame(insertedSchedule, result);
    }

    @Test
    public void testInsertScheduleDefaultsMissedFirePolicy() {
        Mockito.when(projectDao.queryByCode(projectCode)).thenReturn(this.getProject());
        Mockito.when(scheduleDao.queryByWorkflowDefinitionCode(processDefinitionCode)).thenReturn(null);
        Mockito.when(workflowDefinitionDao.queryByCode(processDefinitionCode))
                .thenReturn(Optional.of(this.getProcessDefinition()));
        Mockito.when(scheduleDao.queryById(Mockito.anyInt())).thenReturn(new Schedule());

        schedulerService.insertSchedule(
                user, projectCode, processDefinitionCode, scheduleExpression(null), WarningType.NONE, 0,
                FailureStrategy.CONTINUE, Priority.MEDIUM, "default", "tenantCode", environmentCode);

        ArgumentCaptor<Schedule> scheduleCaptor = ArgumentCaptor.forClass(Schedule.class);
        Mockito.verify(scheduleDao).insert(scheduleCaptor.capture());
        Assertions.assertEquals(ScheduleMissedFirePolicy.FIRE_ALL_MISSED,
                scheduleCaptor.getValue().getMissedFirePolicy());
    }

    @ParameterizedTest
    @EnumSource(ScheduleMissedFirePolicy.class)
    public void testUpdateScheduleWithMissedFirePolicy(ScheduleMissedFirePolicy missedFirePolicy) {
        Schedule schedule = this.getSchedule();
        schedule.setReleaseState(ReleaseState.OFFLINE);
        WorkflowDefinition workflowDefinition = this.getProcessDefinition();
        Mockito.when(projectDao.queryByCode(projectCode)).thenReturn(this.getProject());
        Mockito.when(scheduleDao.queryById(scheduleId)).thenReturn(schedule);
        Mockito.when(workflowDefinitionDao.queryByCode(processDefinitionCode))
                .thenReturn(Optional.of(workflowDefinition));

        schedulerService.updateSchedule(
                user, projectCode, scheduleId, scheduleExpression(missedFirePolicy), WarningType.NONE, 0,
                FailureStrategy.CONTINUE, Priority.MEDIUM, "default", "tenantCode", environmentCode);

        Assertions.assertEquals(missedFirePolicy, schedule.getMissedFirePolicy());
    }

    @Test
    public void testUpdateSchedulePreservesMissedFirePolicyWhenOmitted() {
        Schedule schedule = this.getSchedule();
        schedule.setReleaseState(ReleaseState.OFFLINE);
        schedule.setMissedFirePolicy(ScheduleMissedFirePolicy.SKIP_MISSED);
        WorkflowDefinition workflowDefinition = this.getProcessDefinition();
        Mockito.when(projectDao.queryByCode(projectCode)).thenReturn(this.getProject());
        Mockito.when(scheduleDao.queryById(scheduleId)).thenReturn(schedule);
        Mockito.when(workflowDefinitionDao.queryByCode(processDefinitionCode))
                .thenReturn(Optional.of(workflowDefinition));

        schedulerService.updateSchedule(
                user, projectCode, scheduleId, scheduleExpression(null), WarningType.NONE, 0,
                FailureStrategy.CONTINUE, Priority.MEDIUM, "default", "tenantCode", environmentCode);

        Assertions.assertEquals(ScheduleMissedFirePolicy.SKIP_MISSED, schedule.getMissedFirePolicy());
    }

    private String scheduleExpression(ScheduleMissedFirePolicy missedFirePolicy) {
        String policy = missedFirePolicy == null ? "" : ",\"missedFirePolicy\":\"" + missedFirePolicy.name() + "\"";
        return "{\"startTime\":\"2019-12-16 00:00:00\",\"endTime\":\"2019-12-17 00:00:00\","
                + "\"crontab\":\"0 0 6 * * ? *\",\"timezoneId\":\"Asia/Shanghai\"" + policy + "}";
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
        Mockito.when(scheduleDao.queryById(scheduleId)).thenReturn(schedule);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.deleteSchedulesById(user, scheduleId));
        Assertions.assertEquals(Status.SCHEDULE_STATE_ONLINE.getCode(), ((ServiceException) exception).getCode());
        schedule.setReleaseState(ReleaseState.OFFLINE);

        // error user not own schedule
        int notOwnUserId = 2;
        schedule.setUserId(notOwnUserId);
        Mockito.when(scheduleDao.queryById(scheduleId)).thenReturn(schedule);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.deleteSchedulesById(user, scheduleId));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM.getMsg(), exception.getMessage());
        schedule.setUserId(userId);

        // error process definition not exists
        Mockito.when(scheduleDao.queryById(scheduleId)).thenReturn(schedule);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.deleteSchedulesById(user, scheduleId));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST.getCode(),
                ((ServiceException) exception).getCode());

        // error project permissions
        Mockito.when(workflowDefinitionDao.queryByCode(processDefinitionCode))
                .thenReturn(Optional.of(this.getProcessDefinition()));
        Mockito.when(projectDao.queryByCode(projectCode)).thenReturn(this.getProject());
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM)).when(projectService)
                .checkProjectAndAuthThrowException(user, this.getProject(), null);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.deleteSchedulesById(user, scheduleId));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(),
                ((ServiceException) exception).getCode());

        // error delete mapper
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(user, this.getProject(), null);
        Mockito.when(scheduleDao.deleteById(scheduleId)).thenReturn(false);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.deleteSchedulesById(user, scheduleId));
        Assertions.assertEquals(Status.DELETE_SCHEDULE_BY_ID_ERROR.getCode(), ((ServiceException) exception).getCode());

        // success
        Mockito.when(scheduleDao.deleteById(scheduleId)).thenReturn(true);
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
