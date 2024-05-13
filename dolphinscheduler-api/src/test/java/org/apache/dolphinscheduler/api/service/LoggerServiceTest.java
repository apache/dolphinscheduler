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
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.DOWNLOAD_LOG;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.VIEW_LOG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.AssertionsHelper;
import org.apache.dolphinscheduler.api.enums.Status;
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
import org.apache.dolphinscheduler.extract.base.config.NettyServerConfig;
import org.apache.dolphinscheduler.extract.base.server.SpringServerMethodInvokerDiscovery;
import org.apache.dolphinscheduler.extract.common.ILogService;
import org.apache.dolphinscheduler.extract.common.transportor.GetAppIdRequest;
import org.apache.dolphinscheduler.extract.common.transportor.GetAppIdResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;

import java.io.IOException;
import java.net.ServerSocket;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
    private TaskDefinitionMapper taskDefinitionMapper;

    private SpringServerMethodInvokerDiscovery springServerMethodInvokerDiscovery;

    private int nettyServerPort = 18080;

    @BeforeEach
    public void setUp() {
        try (ServerSocket s = new ServerSocket(0)) {
            nettyServerPort = s.getLocalPort();
        } catch (IOException e) {
            return;
        }

        springServerMethodInvokerDiscovery = new SpringServerMethodInvokerDiscovery(
                NettyServerConfig.builder().serverName("TestLogServer").listenPort(nettyServerPort).build());
        springServerMethodInvokerDiscovery.start();
        springServerMethodInvokerDiscovery.registerServerMethodInvokerProvider(new ILogService() {

            @Override
            public TaskInstanceLogFileDownloadResponse getTaskInstanceWholeLogFileBytes(TaskInstanceLogFileDownloadRequest taskInstanceLogFileDownloadRequest) {
                if (taskInstanceLogFileDownloadRequest.getTaskInstanceId() == 1) {
                    return new TaskInstanceLogFileDownloadResponse(new byte[0]);
                } else if (taskInstanceLogFileDownloadRequest.getTaskInstanceId() == 10) {
                    return new TaskInstanceLogFileDownloadResponse("log content".getBytes());
                }

                throw new ServiceException("download error");
            }

            @Override
            public TaskInstanceLogPageQueryResponse pageQueryTaskInstanceLog(TaskInstanceLogPageQueryRequest taskInstanceLogPageQueryRequest) {
                if (taskInstanceLogPageQueryRequest.getTaskInstanceId() != null) {
                    if (taskInstanceLogPageQueryRequest.getTaskInstanceId() == 100) {
                        throw new ServiceException("query log error");
                    } else if (taskInstanceLogPageQueryRequest.getTaskInstanceId() == 10) {
                        return new TaskInstanceLogPageQueryResponse("log content");
                    }
                }

                return new TaskInstanceLogPageQueryResponse();
            }

            @Override
            public GetAppIdResponse getAppId(GetAppIdRequest getAppIdRequest) {
                return new GetAppIdResponse();
            }

            @Override
            public void removeTaskInstanceLog(String taskInstanceLogAbsolutePath) {

            }
        });
        springServerMethodInvokerDiscovery.start();
    }

    @AfterEach
    public void tearDown() {
        if (springServerMethodInvokerDiscovery != null) {
            springServerMethodInvokerDiscovery.close();
        }
    }

    @Test
    public void testQueryLog() {

        User loginUser = new User();
        loginUser.setId(1);
        TaskInstance taskInstance = new TaskInstance();
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
                .checkProjectAndAuthThrowException(loginUser, taskInstance.getProjectCode(), VIEW_LOG);
        AssertionsHelper.assertThrowsServiceException(Status.PROJECT_NOT_EXIST,
                () -> loggerService.queryLog(loginUser, 1, 1, 1));

        // USER_NO_OPERATION_PERM
        doThrow(new ServiceException(Status.USER_NO_OPERATION_PERM)).when(projectService)
                .checkProjectAndAuthThrowException(loginUser, taskInstance.getProjectCode(), VIEW_LOG);
        AssertionsHelper.assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> loggerService.queryLog(loginUser, 1, 1, 1));

        // SUCCESS
        doNothing().when(projectService).checkProjectAndAuthThrowException(loginUser, taskInstance.getProjectCode(),
                VIEW_LOG);
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
                .checkProjectAndAuthThrowException(loginUser, taskInstance.getProjectCode(), VIEW_LOG);
        AssertionsHelper.assertThrowsServiceException(Status.PROJECT_NOT_EXIST,
                () -> loggerService.queryLog(loginUser, 1, 1, 1));

        // USER_NO_OPERATION_PERM
        doThrow(new ServiceException(Status.USER_NO_OPERATION_PERM)).when(projectService)
                .checkProjectAndAuthThrowException(loginUser, taskInstance.getProjectCode(), VIEW_LOG);
        AssertionsHelper.assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> loggerService.queryLog(loginUser, 1, 1, 1));

        // SUCCESS
        doNothing().when(projectService).checkProjectAndAuthThrowException(loginUser, taskInstance.getProjectCode(),
                DOWNLOAD_LOG);
        byte[] logBytes = loggerService.getLogBytes(loginUser, 1);
        Assertions.assertEquals(43, logBytes.length - String.valueOf(nettyServerPort).length());
    }

    @Test
    public void testQueryLogInSpecifiedProject() {
        long projectCode = 1L;

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);
        TaskInstance taskInstance = new TaskInstance();
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);
        when(taskInstanceDao.queryById(10)).thenReturn(null);

        assertThrowsServiceException(Status.TASK_INSTANCE_NOT_FOUND,
                () -> loggerService.queryLog(loginUser, projectCode, 10, 1, 1));

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(projectCode);
        taskDefinition.setCode(1L);

        // SUCCESS
        taskInstance.setTaskCode(1L);
        taskInstance.setId(1);
        taskInstance.setHost("127.0.0.1:" + nettyServerPort);
        taskInstance.setLogPath("/temp/log");
        doNothing().when(projectService).checkProjectAndAuthThrowException(loginUser, projectCode, VIEW_LOG);
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);
        when(taskDefinitionMapper.queryByCode(taskInstance.getTaskCode())).thenReturn(taskDefinition);
        assertDoesNotThrow(() -> loggerService.queryLog(loginUser, projectCode, 1, 1, 1));

        taskDefinition.setProjectCode(10);
        assertThrowsServiceException(Status.TASK_INSTANCE_NOT_FOUND,
                () -> loggerService.queryLog(loginUser, projectCode, 1, 1, 1));

        taskDefinition.setProjectCode(1);
        taskInstance.setId(10);
        when(taskInstanceDao.queryById(10)).thenReturn(taskInstance);
        String result = loggerService.queryLog(loginUser, projectCode, 10, 1, 1);
        assertEquals("log content", result);

        taskInstance.setId(100);
        when(taskInstanceDao.queryById(100)).thenReturn(taskInstance);
        assertThrowsServiceException(Status.QUERY_TASK_INSTANCE_LOG_ERROR,
                () -> loggerService.queryLog(loginUser, projectCode, 10, 1, 1));
    }

    @Test
    public void testGetLogBytesInSpecifiedProject() {
        long projectCode = 1L;
        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        TaskInstance taskInstance = new TaskInstance();
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(projectCode);
        taskDefinition.setCode(1L);
        // SUCCESS
        taskInstance.setTaskCode(1L);
        taskInstance.setId(1);
        taskInstance.setHost("127.0.0.1:" + nettyServerPort);
        taskInstance.setLogPath("/temp/log");
        doNothing().when(projectService).checkProjectAndAuthThrowException(loginUser, projectCode, DOWNLOAD_LOG);

        when(taskInstanceDao.queryById(1)).thenReturn(null);
        assertThrowsServiceException(
                Status.INTERNAL_SERVER_ERROR_ARGS, () -> loggerService.getLogBytes(loginUser, projectCode, 1));

        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);
        when(taskDefinitionMapper.queryByCode(taskInstance.getTaskCode())).thenReturn(taskDefinition);
        assertDoesNotThrow(() -> loggerService.getLogBytes(loginUser, projectCode, 1));

        taskDefinition.setProjectCode(2L);
        assertThrowsServiceException(Status.INTERNAL_SERVER_ERROR_ARGS,
                () -> loggerService.getLogBytes(loginUser, projectCode, 1));

        taskDefinition.setProjectCode(1L);
        taskInstance.setId(100);
        when(taskInstanceDao.queryById(100)).thenReturn(taskInstance);
        assertThrowsServiceException(Status.DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR,
                () -> loggerService.getLogBytes(loginUser, projectCode, 100));
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
