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

import static org.apache.dolphinscheduler.common.utils.DateUtils.stringToDate;
import static org.mockito.ArgumentMatchers.isA;

import org.apache.dolphinscheduler.api.dto.schedule.ScheduleCreateRequest;
import org.apache.dolphinscheduler.api.dto.schedule.ScheduleFilterRequest;
import org.apache.dolphinscheduler.api.dto.schedule.ScheduleUpdateRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.SchedulerServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.scheduler.api.SchedulerApi;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Map;

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
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Mock
    private MonitorService monitorService;

    @Mock
    private ProcessService processService;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProcessDefinitionMapper processDefinitionMapper;

    @Mock
    private ProjectService projectService;

    @Mock
    private SchedulerApi schedulerApi;

    @Mock
    private ExecutorService executorService;

    @Mock
    private EnvironmentMapper environmentMapper;

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
    private static final String startTime = "2020-01-01 12:13:14";
    private static final String endTime = "2020-02-01 12:13:14";
    private static final String crontab = "0 0 * * * ? *";

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUserName(userName);
        user.setId(userId);
    }

    @Test
    public void testSetScheduleState() {
        Map<String, Object> result;
        Project project = getProject();

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setProjectCode(projectCode);

        Schedule schedule = new Schedule();
        schedule.setId(1);
        schedule.setProcessDefinitionCode(1);
        schedule.setReleaseState(ReleaseState.OFFLINE);

        Mockito.when(scheduleMapper.selectById(1)).thenReturn(schedule);

        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        Mockito.when(processDefinitionMapper.queryByCode(1)).thenReturn(processDefinition);

        // hash no auth
        result = schedulerService.setScheduleState(user, project.getCode(), 1, ReleaseState.ONLINE);

        Mockito.when(projectService.hasProjectAndPerm(user, project, result, null)).thenReturn(true);
        // schedule not exists
        result = schedulerService.setScheduleState(user, project.getCode(), 2, ReleaseState.ONLINE);
        Assertions.assertEquals(Status.SCHEDULE_CRON_NOT_EXISTS, result.get(Constants.STATUS));

        // SCHEDULE_CRON_REALEASE_NEED_NOT_CHANGE
        result = schedulerService.setScheduleState(user, project.getCode(), 1, ReleaseState.OFFLINE);
        Assertions.assertEquals(Status.SCHEDULE_CRON_REALEASE_NEED_NOT_CHANGE, result.get(Constants.STATUS));

        // PROCESS_DEFINE_NOT_EXIST
        schedule.setProcessDefinitionCode(2);
        result = schedulerService.setScheduleState(user, project.getCode(), 1, ReleaseState.ONLINE);
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, result.get(Constants.STATUS));
        schedule.setProcessDefinitionCode(1);

        result = schedulerService.setScheduleState(user, project.getCode(), 1, ReleaseState.ONLINE);
        Assertions.assertEquals(Status.PROCESS_DAG_IS_EMPTY, result.get(Constants.STATUS));

        processDefinition.setReleaseState(ReleaseState.ONLINE);

        result = schedulerService.setScheduleState(user, project.getCode(), 1, ReleaseState.ONLINE);
        Assertions.assertEquals(Status.PROCESS_DAG_IS_EMPTY, result.get(Constants.STATUS));

        // SUCCESS
        result = schedulerService.setScheduleState(user, project.getCode(), 1, ReleaseState.ONLINE);
        Assertions.assertEquals(Status.PROCESS_DAG_IS_EMPTY, result.get(Constants.STATUS));
    }

    @Test
    public void testCreateSchedulesV2() {
        Project project = this.getProject();
        ProcessDefinition processDefinition = this.getProcessDefinition();
        Schedule schedule = this.getSchedule();

        ScheduleCreateRequest scheduleCreateRequest = new ScheduleCreateRequest();
        scheduleCreateRequest.setProcessDefinitionCode(processDefinitionCode);
        scheduleCreateRequest.setEnvironmentCode(environmentCode);

        // error process definition not exists
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.createSchedulesV2(user, scheduleCreateRequest));
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST.getCode(), ((ServiceException) exception).getCode());

        // error project permissions
        Mockito.when(processDefinitionMapper.queryByCode(processDefinitionCode)).thenReturn(processDefinition);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM)).when(projectService)
                .checkProjectAndAuthThrowException(user, project, null);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.createSchedulesV2(user, scheduleCreateRequest));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(),
                ((ServiceException) exception).getCode());

        // we do not check method `executorService.checkProcessDefinitionValid` because it should be check in
        // executorServiceTest
        // error process definition already exists schedule
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(user, project, null);
        Mockito.when(scheduleMapper.queryByProcessDefinitionCode(processDefinitionCode)).thenReturn(schedule);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.createSchedulesV2(user, scheduleCreateRequest));
        Assertions.assertEquals(Status.SCHEDULE_ALREADY_EXISTS.getCode(), ((ServiceException) exception).getCode());

        // error environment do not exists
        Mockito.when(scheduleMapper.queryByProcessDefinitionCode(processDefinitionCode)).thenReturn(null);
        Mockito.when(environmentMapper.queryByEnvironmentCode(environmentCode)).thenReturn(null);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.createSchedulesV2(user, scheduleCreateRequest));
        Assertions.assertEquals(Status.QUERY_ENVIRONMENT_BY_CODE_ERROR.getCode(),
                ((ServiceException) exception).getCode());

        // error schedule parameter same start time and end time
        Mockito.when(environmentMapper.queryByEnvironmentCode(environmentCode)).thenReturn(this.getEnvironment());
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.createSchedulesV2(user, scheduleCreateRequest));
        Assertions.assertEquals(Status.SCHEDULE_START_TIME_END_TIME_SAME.getCode(),
                ((ServiceException) exception).getCode());

        // error schedule parameter same start time after than end time
        scheduleCreateRequest.setEndTime(endTime);
        String badStartTime = "2022-01-01 12:13:14";
        scheduleCreateRequest.setStartTime(badStartTime);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.createSchedulesV2(user, scheduleCreateRequest));
        Assertions.assertEquals(Status.START_TIME_BIGGER_THAN_END_TIME_ERROR.getCode(),
                ((ServiceException) exception).getCode());

        // error schedule crontab
        String badCrontab = "0 0 123 * * ? *";
        scheduleCreateRequest.setStartTime(startTime);
        scheduleCreateRequest.setCrontab(badCrontab);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.createSchedulesV2(user, scheduleCreateRequest));
        Assertions.assertEquals(Status.SCHEDULE_CRON_CHECK_FAILED.getCode(), ((ServiceException) exception).getCode());

        // error create error
        scheduleCreateRequest.setCrontab(crontab);
        Mockito.when(scheduleMapper.insert(isA(Schedule.class))).thenReturn(0);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.createSchedulesV2(user, scheduleCreateRequest));
        Assertions.assertEquals(Status.CREATE_SCHEDULE_ERROR.getCode(), ((ServiceException) exception).getCode());

        // success
        scheduleCreateRequest.setCrontab(crontab);
        Mockito.when(scheduleMapper.insert(isA(Schedule.class))).thenReturn(1);
        Schedule scheduleCreated = schedulerService.createSchedulesV2(user, scheduleCreateRequest);
        Assertions.assertEquals(scheduleCreateRequest.getProcessDefinitionCode(),
                scheduleCreated.getProcessDefinitionCode());
        Assertions.assertEquals(scheduleCreateRequest.getEnvironmentCode(), scheduleCreated.getEnvironmentCode());
        Assertions.assertEquals(stringToDate(scheduleCreateRequest.getStartTime()), scheduleCreated.getStartTime());
        Assertions.assertEquals(stringToDate(scheduleCreateRequest.getEndTime()), scheduleCreated.getEndTime());
        Assertions.assertEquals(scheduleCreateRequest.getCrontab(), scheduleCreated.getCrontab());
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
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST.getCode(), ((ServiceException) exception).getCode());

        // error project permissions
        Mockito.when(processDefinitionMapper.queryByCode(processDefinitionCode))
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

    @Test
    public void testFilterSchedules() {
        Project project = this.getProject();
        ScheduleFilterRequest scheduleFilterRequest = new ScheduleFilterRequest();
        scheduleFilterRequest.setProjectName(project.getName());

        // project permission error
        Mockito.when(projectMapper.queryByName(project.getName())).thenReturn(project);
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM)).when(projectService)
                .checkProjectAndAuthThrowException(user, project, null);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.filterSchedules(user, scheduleFilterRequest));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(),
                ((ServiceException) exception).getCode());
    }

    @Test
    public void testGetSchedules() {
        // error schedule not exists
        exception =
                Assertions.assertThrows(ServiceException.class, () -> schedulerService.getSchedule(user, scheduleId));
        Assertions.assertEquals(Status.SCHEDULE_NOT_EXISTS.getCode(), ((ServiceException) exception).getCode());

        // error process definition not exists
        Mockito.when(scheduleMapper.selectById(scheduleId)).thenReturn(this.getSchedule());
        exception =
                Assertions.assertThrows(ServiceException.class, () -> schedulerService.getSchedule(user, scheduleId));
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST.getCode(), ((ServiceException) exception).getCode());

        // error project permissions
        Mockito.when(processDefinitionMapper.queryByCode(processDefinitionCode))
                .thenReturn(this.getProcessDefinition());
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(this.getProject());
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM)).when(projectService)
                .checkProjectAndAuthThrowException(user, this.getProject(), null);
        exception =
                Assertions.assertThrows(ServiceException.class, () -> schedulerService.getSchedule(user, scheduleId));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(),
                ((ServiceException) exception).getCode());

        // success
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(user, this.getProject(), null);
        Schedule schedule = schedulerService.getSchedule(user, scheduleId);
        Assertions.assertEquals(this.getSchedule().getId(), schedule.getId());
    }

    @Test
    public void testUpdateSchedulesV2() {
        ScheduleUpdateRequest scheduleUpdateRequest = new ScheduleUpdateRequest();

        // error schedule not exists
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.updateSchedulesV2(user, scheduleId, scheduleUpdateRequest));
        Assertions.assertEquals(Status.SCHEDULE_NOT_EXISTS.getCode(), ((ServiceException) exception).getCode());

        // error schedule parameter same start time and end time
        scheduleUpdateRequest.setEndTime(endTime);
        scheduleUpdateRequest.setStartTime(endTime);
        Mockito.when(scheduleMapper.selectById(scheduleId)).thenReturn(this.getSchedule());
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.updateSchedulesV2(user, scheduleId, scheduleUpdateRequest));
        Assertions.assertEquals(Status.SCHEDULE_START_TIME_END_TIME_SAME.getCode(),
                ((ServiceException) exception).getCode());

        // error schedule parameter same start time after than end time
        String badStartTime = "2022-01-01 12:13:14";
        scheduleUpdateRequest.setStartTime(badStartTime);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.updateSchedulesV2(user, scheduleId, scheduleUpdateRequest));
        Assertions.assertEquals(Status.START_TIME_BIGGER_THAN_END_TIME_ERROR.getCode(),
                ((ServiceException) exception).getCode());
        scheduleUpdateRequest.setStartTime(startTime);

        // error schedule crontab
        String badCrontab = "0 0 123 * * ? *";
        scheduleUpdateRequest.setCrontab(badCrontab);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.updateSchedulesV2(user, scheduleId, scheduleUpdateRequest));
        Assertions.assertEquals(Status.SCHEDULE_CRON_CHECK_FAILED.getCode(), ((ServiceException) exception).getCode());
        scheduleUpdateRequest.setCrontab(crontab);

        // error process definition not exists
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.updateSchedulesV2(user, scheduleId, scheduleUpdateRequest));
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST.getCode(), ((ServiceException) exception).getCode());

        // error project permissions
        Mockito.when(processDefinitionMapper.queryByCode(processDefinitionCode))
                .thenReturn(this.getProcessDefinition());
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(this.getProject());
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM)).when(projectService)
                .checkProjectAndAuthThrowException(user, this.getProject(), null);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.updateSchedulesV2(user, scheduleId, scheduleUpdateRequest));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(),
                ((ServiceException) exception).getCode());

        // error environment do not exists
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(user, this.getProject(), null);
        Mockito.when(environmentMapper.queryByEnvironmentCode(environmentCode)).thenReturn(null);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.updateSchedulesV2(user, scheduleId, scheduleUpdateRequest));
        Assertions.assertEquals(Status.QUERY_ENVIRONMENT_BY_CODE_ERROR.getCode(),
                ((ServiceException) exception).getCode());

        // error environment do not exists
        Mockito.when(environmentMapper.queryByEnvironmentCode(environmentCode)).thenReturn(this.getEnvironment());
        Mockito.when(scheduleMapper.updateById(isA(Schedule.class))).thenReturn(0);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> schedulerService.updateSchedulesV2(user, scheduleId, scheduleUpdateRequest));
        Assertions.assertEquals(Status.UPDATE_SCHEDULE_ERROR.getCode(), ((ServiceException) exception).getCode());

        // success
        Mockito.when(scheduleMapper.updateById(isA(Schedule.class))).thenReturn(1);
        Schedule schedule = schedulerService.updateSchedulesV2(user, scheduleId, scheduleUpdateRequest);
        Assertions.assertEquals(scheduleUpdateRequest.getCrontab(), schedule.getCrontab());
        Assertions.assertEquals(stringToDate(scheduleUpdateRequest.getStartTime()), schedule.getStartTime());
        Assertions.assertEquals(stringToDate(scheduleUpdateRequest.getEndTime()), schedule.getEndTime());
    }

    private Project getProject() {
        Project project = new Project();
        project.setName(projectName);
        project.setCode(projectCode);
        project.setUserId(userId);
        return project;
    }

    private ProcessDefinition getProcessDefinition() {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setName(processDefinitionName);
        processDefinition.setCode(processDefinitionCode);
        processDefinition.setProjectCode(projectCode);
        processDefinition.setVersion(processDefinitionVersion);
        processDefinition.setUserId(userId);
        return processDefinition;
    }

    private Schedule getSchedule() {
        Schedule schedule = new Schedule();
        schedule.setId(scheduleId);
        schedule.setProcessDefinitionCode(processDefinitionCode);
        schedule.setEnvironmentCode(environmentCode);
        schedule.setUserId(userId);
        return schedule;
    }

    private Environment getEnvironment() {
        Environment environment = new Environment();
        environment.setCode(environmentCode);
        return environment;
    }

}
