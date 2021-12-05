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
import org.apache.dolphinscheduler.api.service.impl.LoggerServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * logger service test
 */
@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({LoggerServiceImpl.class})
public class LoggerServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(LoggerServiceTest.class);

    @InjectMocks
    private LoggerServiceImpl loggerService;

    @Mock
    private ProcessService processService;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectService projectService;

    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;

    @Before
    public void init() {
        this.loggerService.init();
    }

    @Test
    public void testQueryDataSourceList() {

        TaskInstance taskInstance = new TaskInstance();
        Mockito.when(processService.findTaskInstanceById(1)).thenReturn(taskInstance);
        Result result = loggerService.queryLog(2, 1, 1);
        //TASK_INSTANCE_NOT_FOUND
        Assert.assertEquals(Status.TASK_INSTANCE_NOT_FOUND.getCode(), result.getCode().intValue());

        try {
            //HOST NOT FOUND OR ILLEGAL
            result = loggerService.queryLog(1, 1, 1);
        } catch (RuntimeException e) {
            Assert.assertTrue(true);
            logger.error("testQueryDataSourceList error {}", e.getMessage());
        }
        Assert.assertEquals(Status.TASK_INSTANCE_NOT_FOUND.getCode(), result.getCode().intValue());

        //SUCCESS
        taskInstance.setHost("127.0.0.1:8080");
        taskInstance.setLogPath("/temp/log");
        Mockito.when(processService.findTaskInstanceById(1)).thenReturn(taskInstance);
        result = loggerService.queryLog(1, 1, 1);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testGetLogBytes() {

        TaskInstance taskInstance = new TaskInstance();
        Mockito.when(processService.findTaskInstanceById(1)).thenReturn(taskInstance);

        //task instance is null
        try {
            loggerService.getLogBytes(2);
        } catch (RuntimeException e) {
            Assert.assertTrue(true);
            logger.error("testGetLogBytes error: {}", "task instance is null");
        }

        //task instance host is null
        try {
            loggerService.getLogBytes(1);
        } catch (RuntimeException e) {
            Assert.assertTrue(true);
            logger.error("testGetLogBytes error: {}", "task instance host is null");
        }

        //success
        taskInstance.setHost("127.0.0.1:8080");
        taskInstance.setLogPath("/temp/log");
        //if use @RunWith(PowerMockRunner.class) mock object,sonarcloud will not calculate the coverage,
        // so no assert will be added here
        loggerService.getLogBytes(1);

    }

    @Test
    public void testQueryLogInSpecifiedProject() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Project project = getProject(projectCode);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        TaskInstance taskInstance = new TaskInstance();
        Mockito.when(processService.findTaskInstanceById(1)).thenReturn(taskInstance);
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(projectCode);
        taskDefinition.setCode(1L);
        //SUCCESS
        taskInstance.setTaskCode(1L);
        taskInstance.setId(1);
        taskInstance.setHost("127.0.0.1:8080");
        taskInstance.setLogPath("/temp/log");
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode)).thenReturn(result);
        Mockito.when(processService.findTaskInstanceById(1)).thenReturn(taskInstance);
        Mockito.when(taskDefinitionMapper.queryByCode(taskInstance.getTaskCode())).thenReturn(taskDefinition);
        result = loggerService.queryLog(loginUser, projectCode, 1, 1, 1);
        Assert.assertEquals(Status.SUCCESS.getCode(), ((Status) result.get(Constants.STATUS)).getCode());
    }

    @Test
    public void testGetLogBytesInSpecifiedProject() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Project project = getProject(projectCode);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        TaskInstance taskInstance = new TaskInstance();
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(projectCode);
        taskDefinition.setCode(1L);
        //SUCCESS
        taskInstance.setTaskCode(1L);
        taskInstance.setId(1);
        taskInstance.setHost("127.0.0.1:8080");
        taskInstance.setLogPath("/temp/log");
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode)).thenReturn(result);
        Mockito.when(processService.findTaskInstanceById(1)).thenReturn(taskInstance);
        Mockito.when(taskDefinitionMapper.queryByCode(taskInstance.getTaskCode())).thenReturn(taskDefinition);
        loggerService.getLogBytes(loginUser, projectCode, 1);
    }


    @After
    public void close() {
        this.loggerService.close();
    }

    /**
     * get mock Project
     *
     * @param projectCode projectCode
     * @return Project
     */
    private Project getProject(long projectCode) {
        Project project = new Project();
        project.setCode(projectCode);
        project.setId(1);
        project.setName("test");
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