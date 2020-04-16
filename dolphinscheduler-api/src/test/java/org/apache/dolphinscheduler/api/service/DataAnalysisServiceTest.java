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
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.CommandCount;
import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.*;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.After;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
public class DataAnalysisServiceTest {
    
    @InjectMocks
    private DataAnalysisService dataAnalysisService;

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
    ProcessService processService;

    private Project project;

    private Map<String, Object> resultMap;

    private User user;

    @Before
    public void setUp() {

        user = new User();
        project = new Project();
        project.setId(1);
        resultMap = new HashMap<>();
        Mockito.when(projectMapper.selectById(1)).thenReturn(project);
        Mockito.when(projectService.hasProjectAndPerm(user,project,resultMap)).thenReturn(true);

    }


    @After
    public void after(){

        user = null;
        projectMapper = null;
        resultMap = null;
    }


    @Test
    public void testCountTaskStateByProject(){

        String startDate = "2020-02-11 16:02:18";
        String endDate = "2020-02-11 16:03:18";

        //checkProject false
        Map<String, Object> result = dataAnalysisService.countTaskStateByProject(user, 2, startDate, endDate);
        Assert.assertTrue(result.isEmpty());


        //SUCCESS
        Mockito.when(taskInstanceMapper.countTaskInstanceStateByUser(DateUtils.getScheduleDate(startDate),
                DateUtils.getScheduleDate(endDate), new Integer[]{1})).thenReturn(getTaskInstanceStateCounts());

        result = dataAnalysisService.countTaskStateByProject(user, 1, startDate, endDate);
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));

    }


    @Test
    public void testCountProcessInstanceStateByProject(){

        String startDate = "2020-02-11 16:02:18";
        String endDate = "2020-02-11 16:03:18";
        //checkProject false
        Map<String, Object> result = dataAnalysisService.countProcessInstanceStateByProject(user,2,startDate,endDate);
        Assert.assertTrue(result.isEmpty());

        //SUCCESS
        Mockito.when(processInstanceMapper.countInstanceStateByUser(DateUtils.getScheduleDate(startDate),
                DateUtils.getScheduleDate(endDate), new Integer[]{1})).thenReturn(getTaskInstanceStateCounts());
        result = dataAnalysisService.countProcessInstanceStateByProject(user,1,startDate,endDate);
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
    }

    @Test
    public void testCountDefinitionByUser(){

        Map<String, Object> result = dataAnalysisService.countDefinitionByUser(user,1);
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
    }


    @Test
    public void testCountCommandState(){

        String startDate = "2020-02-11 16:02:18";
        String endDate = "2020-02-11 16:03:18";
        //checkProject false
        Map<String, Object> result = dataAnalysisService.countCommandState(user,2,startDate,endDate);
        Assert.assertTrue(result.isEmpty());
        List<CommandCount> commandCounts = new ArrayList<>(1);
        CommandCount commandCount = new CommandCount();
        commandCount.setCommandType(CommandType.START_PROCESS);
        commandCounts.add(commandCount);
        Mockito.when(commandMapper.countCommandState(0, DateUtils.getScheduleDate(startDate),
                DateUtils.getScheduleDate(endDate), new Integer[]{1})).thenReturn(commandCounts);

        Mockito.when(errorCommandMapper.countCommandState( DateUtils.getScheduleDate(startDate),
                DateUtils.getScheduleDate(endDate), new Integer[]{1})).thenReturn(commandCounts);

        result = dataAnalysisService.countCommandState(user,1,startDate,endDate);
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));

    }

    /**
     *  get list
     * @return
     */
    private  List<ExecuteStatusCount> getTaskInstanceStateCounts(){

        List<ExecuteStatusCount> taskInstanceStateCounts = new ArrayList<>(1);
        ExecuteStatusCount executeStatusCount = new ExecuteStatusCount();
        executeStatusCount.setExecutionStatus(ExecutionStatus.RUNNING_EXEUTION);
        taskInstanceStateCounts.add(executeStatusCount);

        return  taskInstanceStateCounts;
    }

}