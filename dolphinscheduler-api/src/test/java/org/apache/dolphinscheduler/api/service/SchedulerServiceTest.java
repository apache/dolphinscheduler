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
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.SchedulerServiceImpl;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.scheduler.api.SchedulerApi;
import org.apache.dolphinscheduler.scheduler.quartz.QuartzScheduler;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * scheduler service test
 */
@RunWith(PowerMockRunner.class)
public class SchedulerServiceTest {

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
    private ProjectServiceImpl projectService;

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
        Project project = getProject();

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setProjectCode(projectCode);

        Schedule schedule = new Schedule();
        schedule.setId(1);
        schedule.setProcessDefinitionCode(1);
        schedule.setReleaseState(ReleaseState.OFFLINE);

        List<Server> masterServers = new ArrayList<>();
        masterServers.add(new Server());

        Mockito.when(scheduleMapper.selectById(1)).thenReturn(schedule);
        Mockito.when(processDefinitionMapper.queryByCode(1)).thenReturn(processDefinition);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(project);

        // schedule not exists
        exception = Assertions.assertThrows(ServiceException.class, () -> {
            schedulerService.setScheduleState(user, project.getCode(), 2, ReleaseState.ONLINE);
        });
        Assertions.assertEquals(Status.SCHEDULE_CRON_NOT_EXISTS.getCode(), ((ServiceException) exception).getCode());

        // SCHEDULE_CRON_RELEASE_NEED_NOT_CHANGE
        exception = Assertions.assertThrows(ServiceException.class, () -> {
            schedulerService.setScheduleState(user, project.getCode(), 1, ReleaseState.OFFLINE);
        });
        Assertions.assertEquals(Status.SCHEDULE_CRON_REALEASE_NEED_NOT_CHANGE.getCode(),
                ((ServiceException) exception).getCode());

        //PROCESS_DEFINE_NOT_EXIST
        schedule.setProcessDefinitionCode(2);
        exception = Assertions.assertThrows(ServiceException.class, () -> {
            schedulerService.setScheduleState(user, project.getCode(), 1, ReleaseState.ONLINE);
        });
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST.getCode(), ((ServiceException) exception).getCode());
        schedule.setProcessDefinitionCode(1);

        // online also success
        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();
        List<ProcessTaskRelation> processTaskRelationList = new ArrayList<>();
        processTaskRelationList.add(processTaskRelation);
        Mockito.when(processTaskRelationMapper.queryByProcessCode(projectCode, 1)).thenReturn(processTaskRelationList);
        exception = Assertions.assertThrows(ServiceException.class, () -> {
            schedulerService.setScheduleState(user, project.getCode(), 1, ReleaseState.ONLINE);
        });
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_RELEASE.getCode(), ((ServiceException) exception).getCode());

        // SUCCESS
        Server server = new Server();
        List<Server> serverList = new ArrayList<>();
        serverList.add(server);
        Mockito.when(monitorService.getServerListFromRegistry(true)).thenReturn(serverList);
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        Assertions.assertDoesNotThrow(() -> {
            schedulerService.setScheduleState(user, project.getCode(), 1, ReleaseState.ONLINE);
        });
    }

    private Project getProject() {
        Project project = new Project();
        project.setId(1);
        project.setName(projectName);
        project.setCode(projectCode);
        project.setUserId(userId);
        return project;
    }

}
