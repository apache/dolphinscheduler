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

import static org.apache.dolphinscheduler.api.AssertionsHelper.assertDoesNotThrow;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.DOWNLOAD_LOG;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.VIEW_LOG;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.v2.BaseStatus;
import org.apache.dolphinscheduler.api.enums.v2.ProjectStatus;
import org.apache.dolphinscheduler.api.enums.v2.TaskStatus;
import org.apache.dolphinscheduler.api.enums.v2.UserStatus;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.LoggerServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
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
 * logger service test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LoggerServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(LoggerServiceTest.class);

    @InjectMocks
    private LoggerServiceImpl loggerService;

    @Mock
    private TaskInstanceDao taskInstanceDao;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectService projectService;

    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;

    @Test
    public void testQueryLog() {

        User loginUser = new User();
        loginUser.setId(1);
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setExecutorId(loginUser.getId() + 1);
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);
        Result result = loggerService.queryLog(loginUser, 2, 1, 1);
        // TASK_INSTANCE_NOT_FOUND
        Assertions.assertEquals(TaskStatus.TASK_INSTANCE_NOT_FOUND.getCode(), result.getCode().intValue());

        try {
            // HOST NOT FOUND OR ILLEGAL
            result = loggerService.queryLog(loginUser, 1, 1, 1);
        } catch (RuntimeException e) {
            Assertions.assertTrue(true);
            logger.error("testQueryDataSourceList error {}", e.getMessage());
        }
        Assertions.assertEquals(TaskStatus.TASK_INSTANCE_HOST_IS_NULL.getCode(), result.getCode().intValue());

        // PROJECT_NOT_EXIST
        taskInstance.setHost("127.0.0.1:8080");
        taskInstance.setLogPath("/temp/log");
        Project project = getProject(1);
        Mockito.when(projectMapper.queryProjectByTaskInstanceId(1)).thenReturn(project);
        try {
            Mockito.doThrow(new ServiceException(ProjectStatus.PROJECT_NOT_EXIST)).when(projectService)
                    .checkProjectAndAuthThrowException(loginUser, project, VIEW_LOG);
            loggerService.queryLog(loginUser, 1, 1, 1);
        } catch (ServiceException serviceException) {
            Assertions.assertEquals(ProjectStatus.PROJECT_NOT_EXIST.getCode(), serviceException.getCode());
        }

        // USER_NO_OPERATION_PERM
        try {
            Mockito.doThrow(new ServiceException(UserStatus.USER_NO_OPERATION_PERM)).when(projectService)
                    .checkProjectAndAuthThrowException(loginUser, project, VIEW_LOG);
            loggerService.queryLog(loginUser, 1, 1, 1);
        } catch (ServiceException serviceException) {
            Assertions.assertEquals(UserStatus.USER_NO_OPERATION_PERM.getCode(), serviceException.getCode());
        }

        // SUCCESS
        doNothing().when(projectService).checkProjectAndAuthThrowException(loginUser, project, VIEW_LOG);
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);
        result = loggerService.queryLog(loginUser, 1, 1, 1);
        Assertions.assertEquals(BaseStatus.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testGetLogBytes() {

        User loginUser = new User();
        loginUser.setId(1);
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setExecutorId(loginUser.getId() + 1);
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);

        // task instance is null
        try {
            loggerService.getLogBytes(loginUser, 2);
        } catch (ServiceException e) {
            Assertions.assertEquals(new ServiceException("task instance is null or host is null").getMessage(),
                    e.getMessage());
            logger.error("testGetLogBytes error: {}", "task instance is null");
        }

        // task instance host is null
        try {
            loggerService.getLogBytes(loginUser, 1);
        } catch (ServiceException e) {
            Assertions.assertEquals(new ServiceException("task instance is null or host is null").getMessage(),
                    e.getMessage());
            logger.error("testGetLogBytes error: {}", "task instance host is null");
        }

        // PROJECT_NOT_EXIST
        taskInstance.setHost("127.0.0.1:8080");
        taskInstance.setLogPath("/temp/log");
        try {
            Mockito.doThrow(new ServiceException(ProjectStatus.PROJECT_NOT_EXIST)).when(projectService)
                    .checkProjectAndAuthThrowException(loginUser, taskInstance.getProjectCode(), DOWNLOAD_LOG);
            loggerService.queryLog(loginUser, 1, 1, 1);
        } catch (ServiceException serviceException) {
            Assertions.assertEquals(ProjectStatus.PROJECT_NOT_EXIST.getCode(), serviceException.getCode());
        }

        // USER_NO_OPERATION_PERM
        Project project = getProject(1);
        when(projectMapper.queryProjectByTaskInstanceId(1)).thenReturn(project);
        try {
            Mockito.doThrow(new ServiceException(UserStatus.USER_NO_OPERATION_PERM)).when(projectService)
                    .checkProjectAndAuthThrowException(loginUser, project, DOWNLOAD_LOG);
            loggerService.queryLog(loginUser, 1, 1, 1);
        } catch (ServiceException serviceException) {
            Assertions.assertEquals(UserStatus.USER_NO_OPERATION_PERM.getCode(), serviceException.getCode());
        }

        // SUCCESS
        doNothing().when(projectService).checkProjectAndAuthThrowException(loginUser, project, DOWNLOAD_LOG);
        when(projectMapper.queryProjectByTaskInstanceId(1)).thenReturn(project);
        byte[] result = loggerService.getLogBytes(loginUser, 1);
        Assertions.assertEquals(47, result.length);
    }

    @Test
    public void testQueryLogInSpecifiedProject() {
        long projectCode = 1L;

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);
        TaskInstance taskInstance = new TaskInstance();
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(projectCode);
        taskDefinition.setCode(1L);
        // SUCCESS
        taskInstance.setTaskCode(1L);
        taskInstance.setId(1);
        taskInstance.setHost("127.0.0.1:8080");
        taskInstance.setLogPath("/temp/log");
        doNothing().when(projectService).checkProjectAndAuthThrowException(loginUser, projectCode, VIEW_LOG);
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);
        when(taskDefinitionMapper.queryByCode(taskInstance.getTaskCode())).thenReturn(taskDefinition);
        assertDoesNotThrow(() -> loggerService.queryLog(loginUser, projectCode, 1, 1, 1));
    }

    @Test
    public void testGetLogBytesInSpecifiedProject() {
        long projectCode = 1L;
        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Project project = getProject(projectCode);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, BaseStatus.SUCCESS, projectCode);
        TaskInstance taskInstance = new TaskInstance();
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(projectCode);
        taskDefinition.setCode(1L);
        // SUCCESS
        taskInstance.setTaskCode(1L);
        taskInstance.setId(1);
        taskInstance.setHost("127.0.0.1:8080");
        taskInstance.setLogPath("/temp/log");
        doNothing().when(projectService).checkProjectAndAuthThrowException(loginUser, projectCode, DOWNLOAD_LOG);
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);
        when(taskDefinitionMapper.queryByCode(taskInstance.getTaskCode())).thenReturn(taskDefinition);
        assertDoesNotThrow(() -> loggerService.getLogBytes(loginUser, projectCode, 1));
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

    private void putMsg(Map<String, Object> result, BaseStatus status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }
}
