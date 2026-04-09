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

import static org.apache.dolphinscheduler.api.AssertionsHelper.assertThrowsServiceException;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.DOWNLOAD_LOG;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.VIEW_LOG;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.AssertionsHelper;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.executor.logging.LogClientDelegate;
import org.apache.dolphinscheduler.api.service.impl.LoggerServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ResponseTaskLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private LogClientDelegate logClientDelegate;

    private final int nettyServerPort = 18080;

    @Test
    public void testQueryLog() {

        User loginUser = new User();
        loginUser.setId(1);
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setExecutorId(loginUser.getId() + 1);
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);
        Result result = loggerService.queryTaskLog(loginUser, 2, 1, 1);
        // TASK_INSTANCE_NOT_FOUND
        Assertions.assertEquals(Status.TASK_INSTANCE_NOT_FOUND.getCode(), result.getCode().intValue());

        try {
            // HOST NOT FOUND OR ILLEGAL
            result = loggerService.queryTaskLog(loginUser, 1, 1, 1);
        } catch (RuntimeException e) {
            Assertions.assertTrue(true);
            logger.error("testQueryDataSourceList error {}", e.getMessage());
        }
        Assertions.assertEquals(Status.TASK_INSTANCE_HOST_IS_NULL.getCode(), result.getCode().intValue());

        // PROJECT_NOT_EXIST
        taskInstance.setHost("127.0.0.1:" + nettyServerPort);
        taskInstance.setLogPath("/temp/log");
        doThrow(new ServiceException(Status.PROJECT_NOT_EXIST)).when(projectService)
                .checkProjectAndAuthThrowException(loginUser, taskInstance.getProjectCode(), VIEW_LOG);
        AssertionsHelper.assertThrowsServiceException(Status.PROJECT_NOT_EXIST,
                () -> loggerService.queryTaskLog(loginUser, 1, 1, 1));

        // USER_NO_OPERATION_PERM
        doThrow(new ServiceException(Status.USER_NO_OPERATION_PERM)).when(projectService)
                .checkProjectAndAuthThrowException(loginUser, taskInstance.getProjectCode(), VIEW_LOG);
        AssertionsHelper.assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> loggerService.queryTaskLog(loginUser, 1, 1, 1));

        // SUCCESS
        doNothing().when(projectService).checkProjectAndAuthThrowException(loginUser, taskInstance.getProjectCode(),
                VIEW_LOG);
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);
        result = loggerService.queryTaskLog(loginUser, 1, 1, 1);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());

        result = loggerService.queryTaskLog(loginUser, 1, 0, 1);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());

        taskInstance.setLogPath("");
        assertThrowsServiceException(Status.QUERY_TASK_INSTANCE_LOG_ERROR,
                () -> loggerService.queryTaskLog(loginUser, 1, 1, 1));
    }

    @Test
    public void testGetLogBytes() {

        User loginUser = new User();
        loginUser.setId(1);
        Project project = new Project();
        project.setCode(1L);
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setExecutorId(loginUser.getId() + 1);
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);

        // task instance is null
        try {
            loggerService.getTaskLogBytes(loginUser, 2);
        } catch (ServiceException e) {
            Assertions.assertEquals(new ServiceException("task instance is null or host is null").getMessage(),
                    e.getMessage());
            logger.error("testGetLogBytes error: {}", "task instance is null");
        }

        // task instance host is null
        try {
            loggerService.getTaskLogBytes(loginUser, 1);
        } catch (ServiceException e) {
            Assertions.assertEquals(new ServiceException("task instance is null or host is null").getMessage(),
                    e.getMessage());
            logger.error("testGetLogBytes error: {}", "task instance host is null");
        }

        // PROJECT_NOT_EXIST
        taskInstance.setHost("127.0.0.1:" + nettyServerPort);
        taskInstance.setLogPath("/temp/log");
        when(projectMapper.queryProjectByTaskInstanceId(1)).thenReturn(project);
        doThrow(new ServiceException(Status.PROJECT_NOT_EXIST)).when(projectService)
                .checkProjectAndAuthThrowException(loginUser, project, DOWNLOAD_LOG);
        AssertionsHelper.assertThrowsServiceException(Status.PROJECT_NOT_EXIST,
                () -> loggerService.getTaskLogBytes(loginUser, 1));

        // USER_NO_OPERATION_PERM
        doThrow(new ServiceException(Status.USER_NO_OPERATION_PERM)).when(projectService)
                .checkProjectAndAuthThrowException(loginUser, project, DOWNLOAD_LOG);
        AssertionsHelper.assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> loggerService.getTaskLogBytes(loginUser, 1));

        // SUCCESS
        when(logClientDelegate.getTaskLogBytes(any())).thenReturn(new byte[0]);
        doNothing().when(projectService).checkProjectAndAuthThrowException(loginUser, project, DOWNLOAD_LOG);
        when(logClientDelegate.getTaskLogBytes(any())).thenReturn(new byte[0]);
        byte[] logBytes = loggerService.getTaskLogBytes(loginUser, 1);
        Assertions.assertEquals(42, logBytes.length - String.valueOf(nettyServerPort).length());
    }

    @Test
    public void testQueryTaskOutputAndGetOutputBytes() {

        User loginUser = new User();
        loginUser.setId(1);
        Project project = new Project();
        project.setCode(1L);
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setExecutorId(loginUser.getId() + 1);
        taskInstance.setHost("127.0.0.1:" + nettyServerPort);
        taskInstance.setTaskOutputLogPath("/temp/output.log");
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);

        doNothing().when(projectService).checkProjectAndAuthThrowException(loginUser, taskInstance.getProjectCode(),
                VIEW_LOG);
        when(logClientDelegate.getTaskOutputString(any(), anyInt(), anyInt())).thenReturn("output content");

        Result<ResponseTaskLog> result = loggerService.queryTaskOutput(loginUser, 1, 1, 1);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        Assertions.assertEquals("output content", result.getData().getMessage());

        when(projectMapper.queryProjectByTaskInstanceId(1)).thenReturn(project);
        doNothing().when(projectService).checkProjectAndAuthThrowException(loginUser, project, DOWNLOAD_LOG);
        when(logClientDelegate.getTaskOutputBytes(any())).thenReturn(new byte[0]);

        byte[] outputBytes = loggerService.getTaskOutputBytes(loginUser, 1);
        Assertions.assertEquals(0, outputBytes.length);
    }

}
