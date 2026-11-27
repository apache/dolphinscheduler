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
import static org.apache.dolphinscheduler.api.AssertionsHelper.assertThrowsServiceException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.AssertionsHelper;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.executor.logging.LogClientDelegate;
import org.apache.dolphinscheduler.api.service.impl.LoggerServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

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
    private ProjectService projectService;

    @Mock
    private LogClientDelegate logClientDelegate;

    private final int nettyServerPort = 18080;

    @Test
    public void unscopedLogReadAndDownloadRequireInstanceProjectPermission() {
        User user = new User();
        TaskInstance task = new TaskInstance();
        task.setProjectCode(200L);
        task.setHost("127.0.0.1:18080");
        task.setLogPath("/temp/log");
        when(taskInstanceDao.queryById(1)).thenReturn(task);
        doThrow(new ServiceException(Status.USER_NO_OPERATION_PERM)).when(projectService)
                .checkProjectPermission(user, 200L, ProjectPermission.VIEW);

        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> loggerService.queryLog(user, 1, 0, 10));
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> loggerService.getLogBytes(user, 1));
        verifyNoInteractions(logClientDelegate);
    }

    @Test
    public void logsCannotBeReadThroughAnotherProjectWithoutCurrentTaskDefinition() {
        User user = new User();
        TaskInstance task = new TaskInstance();
        task.setProjectCode(200L);
        task.setHost("127.0.0.1:18080");
        task.setLogPath("/temp/log");
        when(taskInstanceDao.queryById(1)).thenReturn(task);

        assertThrowsServiceException(Status.TASK_INSTANCE_NOT_FOUND,
                () -> loggerService.queryLog(user, 100L, 1, 0, 10));
        assertThrowsServiceException(Status.INTERNAL_SERVER_ERROR_ARGS,
                () -> loggerService.getLogBytes(user, 100L, 1));
        task.setProjectCode(null);
        assertThrowsServiceException(Status.TASK_INSTANCE_NOT_FOUND,
                () -> loggerService.queryLog(user, 100L, 1, 0, 10));
        assertThrowsServiceException(Status.INTERNAL_SERVER_ERROR_ARGS,
                () -> loggerService.getLogBytes(user, 100L, 1));
        verifyNoInteractions(logClientDelegate);
    }

    @Test
    public void testQueryLog() {

        User loginUser = new User();
        loginUser.setId(1);
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setProjectCode(1L);
        taskInstance.setExecutorId(loginUser.getId() + 1);
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);
        Result result = loggerService.queryLog(loginUser, 2, 1, 1);
        // TASK_INSTANCE_NOT_FOUND
        Assertions.assertEquals(Status.TASK_INSTANCE_NOT_FOUND.getCode(), result.getCode().intValue());

        try {
            // HOST NOT FOUND OR ILLEGAL
            result = loggerService.queryLog(loginUser, 1, 1, 1);
        } catch (RuntimeException e) {
            Assertions.assertTrue(true);
            logger.error("testQueryDataSourceList error {}", e.getMessage());
        }
        Assertions.assertEquals(Status.TASK_INSTANCE_HOST_IS_NULL.getCode(), result.getCode().intValue());

        // PROJECT_NOT_EXIST
        taskInstance.setHost("127.0.0.1:" + nettyServerPort);
        taskInstance.setLogPath("/temp/log");
        doThrow(new ServiceException(Status.PROJECT_NOT_EXIST)).when(projectService)
                .checkProjectPermission(loginUser, taskInstance.getProjectCode(), ProjectPermission.VIEW);
        AssertionsHelper.assertThrowsServiceException(Status.PROJECT_NOT_EXIST,
                () -> loggerService.queryLog(loginUser, 1, 1, 1));

        // USER_NO_OPERATION_PERM
        doThrow(new ServiceException(Status.USER_NO_OPERATION_PERM)).when(projectService)
                .checkProjectPermission(loginUser, taskInstance.getProjectCode(), ProjectPermission.VIEW);
        AssertionsHelper.assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> loggerService.queryLog(loginUser, 1, 1, 1));

        // SUCCESS
        doNothing().when(projectService).checkProjectPermission(loginUser, taskInstance.getProjectCode(),
                ProjectPermission.VIEW);
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);
        result = loggerService.queryLog(loginUser, 1, 1, 1);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());

        result = loggerService.queryLog(loginUser, 1, 0, 1);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());

        taskInstance.setLogPath("");
        assertThrowsServiceException(Status.QUERY_TASK_INSTANCE_LOG_ERROR,
                () -> loggerService.queryLog(loginUser, 1, 1, 1));
    }

    @Test
    public void testGetLogBytes() {

        User loginUser = new User();
        loginUser.setId(1);
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setProjectCode(1L);
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
        taskInstance.setHost("127.0.0.1:" + nettyServerPort);
        taskInstance.setLogPath("/temp/log");
        doThrow(new ServiceException(Status.PROJECT_NOT_EXIST)).when(projectService)
                .checkProjectPermission(loginUser, taskInstance.getProjectCode(), ProjectPermission.VIEW);
        AssertionsHelper.assertThrowsServiceException(Status.PROJECT_NOT_EXIST,
                () -> loggerService.getLogBytes(loginUser, 1));

        // USER_NO_OPERATION_PERM
        doThrow(new ServiceException(Status.USER_NO_OPERATION_PERM)).when(projectService)
                .checkProjectPermission(loginUser, taskInstance.getProjectCode(), ProjectPermission.VIEW);
        AssertionsHelper.assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> loggerService.getLogBytes(loginUser, 1));

        // SUCCESS
        when(logClientDelegate.getWholeLogBytes(any())).thenReturn(new byte[0]);
        doNothing().when(projectService).checkProjectPermission(loginUser, taskInstance.getProjectCode(),
                ProjectPermission.VIEW);
        when(logClientDelegate.getWholeLogBytes(any())).thenReturn(new byte[0]);
        byte[] logBytes = loggerService.getLogBytes(loginUser, 1);
        Assertions.assertEquals(42, logBytes.length - String.valueOf(nettyServerPort).length());
    }

    @Test
    public void testQueryLogInSpecifiedProject() {
        long projectCode = 1L;

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setProjectCode(1L);
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);
        when(taskInstanceDao.queryById(10)).thenReturn(null);

        assertThrowsServiceException(Status.TASK_INSTANCE_NOT_FOUND,
                () -> loggerService.queryLog(loginUser, projectCode, 10, 1, 1));

        // SUCCESS
        taskInstance.setTaskCode(1L);
        taskInstance.setId(1);
        taskInstance.setHost("127.0.0.1:" + nettyServerPort);
        taskInstance.setLogPath("/temp/log");
        doNothing().when(projectService).checkProjectPermission(loginUser, projectCode, ProjectPermission.VIEW);
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);
        assertDoesNotThrow(() -> loggerService.queryLog(loginUser, projectCode, 1, 1, 1));

        taskInstance.setProjectCode(10L);
        assertThrowsServiceException(Status.TASK_INSTANCE_NOT_FOUND,
                () -> loggerService.queryLog(loginUser, projectCode, 1, 1, 1));

        taskInstance.setProjectCode(1L);
        taskInstance.setId(10);
        when(taskInstanceDao.queryById(10)).thenReturn(taskInstance);

        when(logClientDelegate.getPartLogString(any(), anyInt(), anyInt())).thenReturn("log content");

        String result = loggerService.queryLog(loginUser, projectCode, 10, 1, 1);
        assertEquals("log content", result);

        taskInstance.setId(100);
        when(taskInstanceDao.queryById(100)).thenReturn(taskInstance);
        doThrow(new ServiceException("query log error")).when(logClientDelegate).getPartLogString(any(), anyInt(),
                anyInt());
        assertThrowsServiceException(Status.QUERY_TASK_INSTANCE_LOG_ERROR,
                () -> loggerService.queryLog(loginUser, projectCode, 10, 1, 1));
    }

    @Test
    public void testGetLogBytesInSpecifiedProject() {
        long projectCode = 1L;

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setProjectCode(1L);
        // SUCCESS
        taskInstance.setTaskCode(1L);
        taskInstance.setId(1);
        taskInstance.setHost("127.0.0.1:" + nettyServerPort);
        taskInstance.setLogPath("/temp/log");
        doNothing().when(projectService).checkProjectPermission(loginUser, projectCode, ProjectPermission.VIEW);

        when(taskInstanceDao.queryById(1)).thenReturn(null);
        assertThrowsServiceException(
                Status.INTERNAL_SERVER_ERROR_ARGS, () -> loggerService.getLogBytes(loginUser, projectCode, 1));

        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);
        when(logClientDelegate.getWholeLogBytes(any())).thenReturn(new byte[0]);
        assertDoesNotThrow(() -> loggerService.getLogBytes(loginUser, projectCode, 1));

        taskInstance.setProjectCode(2L);
        assertThrowsServiceException(Status.INTERNAL_SERVER_ERROR_ARGS,
                () -> loggerService.getLogBytes(loginUser, projectCode, 1));

        taskInstance.setProjectCode(1L);
        taskInstance.setId(100);
        when(taskInstanceDao.queryById(100)).thenReturn(taskInstance);
        doThrow(new ServiceException("download error")).when(logClientDelegate).getWholeLogBytes(any());
        assertThrowsServiceException(Status.DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR,
                () -> loggerService.getLogBytes(loginUser, projectCode, 100));
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
