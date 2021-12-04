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
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.SchedulerServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.quartz.QuartzExecutors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * scheduler service test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(QuartzExecutors.class)
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
    private QuartzExecutors quartzExecutors;

    @Before
    public void setUp() {

        quartzExecutors = PowerMockito.mock(QuartzExecutors.class);
        PowerMockito.mockStatic(QuartzExecutors.class);
        try {
            PowerMockito.doReturn(quartzExecutors).when(QuartzExecutors.class, "getInstance");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testSetScheduleState() {
        String projectName = "test";
        long projectCode = 1L;
        User loginUser = new User();
        loginUser.setId(1);
        Map<String, Object> result = new HashMap<String, Object>();
        Project project = getProject(projectName, projectCode);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setProjectCode(projectCode);

        Schedule schedule = new Schedule();
        schedule.setId(1);
        schedule.setProcessDefinitionCode(1);
        schedule.setReleaseState(ReleaseState.OFFLINE);

        List<Server> masterServers = new ArrayList<>();
        masterServers.add(new Server());

        Mockito.when(scheduleMapper.selectById(1)).thenReturn(schedule);

        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(project);

        Mockito.when(processDefinitionMapper.queryByCode(1)).thenReturn(processDefinition);

        //hash no auth
        result = schedulerService.setScheduleState(loginUser, project.getCode(), 1, ReleaseState.ONLINE);

        Mockito.when(projectService.hasProjectAndPerm(loginUser, project, result)).thenReturn(true);
        //schedule not exists
        result = schedulerService.setScheduleState(loginUser, project.getCode(), 2, ReleaseState.ONLINE);
        Assert.assertEquals(Status.SCHEDULE_CRON_NOT_EXISTS, result.get(Constants.STATUS));

        //SCHEDULE_CRON_REALEASE_NEED_NOT_CHANGE
        result = schedulerService.setScheduleState(loginUser, project.getCode(), 1, ReleaseState.OFFLINE);
        Assert.assertEquals(Status.SCHEDULE_CRON_REALEASE_NEED_NOT_CHANGE, result.get(Constants.STATUS));

        //PROCESS_DEFINE_NOT_EXIST
        schedule.setProcessDefinitionCode(2);
        result = schedulerService.setScheduleState(loginUser, project.getCode(), 1, ReleaseState.ONLINE);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, result.get(Constants.STATUS));
        schedule.setProcessDefinitionCode(1);

        result = schedulerService.setScheduleState(loginUser, project.getCode(), 1, ReleaseState.ONLINE);
        Assert.assertEquals(Status.PROCESS_DAG_IS_EMPTY, result.get(Constants.STATUS));

        processDefinition.setReleaseState(ReleaseState.ONLINE);
        Mockito.when(processService.findProcessDefineById(1)).thenReturn(processDefinition);

        result = schedulerService.setScheduleState(loginUser, project.getCode(), 1, ReleaseState.ONLINE);
        Assert.assertEquals(Status.PROCESS_DAG_IS_EMPTY, result.get(Constants.STATUS));

        //set master
        Mockito.when(monitorService.getServerListFromRegistry(true)).thenReturn(masterServers);

        //SUCCESS
        result = schedulerService.setScheduleState(loginUser, project.getCode(), 1, ReleaseState.ONLINE);
        Assert.assertEquals(Status.PROCESS_DAG_IS_EMPTY, result.get(Constants.STATUS));
    }

    private Project getProject(String name, long code) {
        Project project = new Project();
        project.setName(name);
        project.setCode(code);
        project.setUserId(1);

        return project;
    }

}
