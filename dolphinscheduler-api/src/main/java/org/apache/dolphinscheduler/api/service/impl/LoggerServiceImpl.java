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

package org.apache.dolphinscheduler.api.service.impl;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.DOWNLOAD_LOG;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.VIEW_LOG;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.LoggerService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ResponseTaskLog;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.extract.base.client.SingletonJdkDynamicRpcClientProxyFactory;
import org.apache.dolphinscheduler.extract.common.ILogService;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.primitives.Bytes;

/**
 * logger service impl
 */
@Service
@Slf4j
public class LoggerServiceImpl extends BaseServiceImpl implements LoggerService {

    private static final String LOG_HEAD_FORMAT = "[LOG-PATH]: %s, [HOST]:  %s%s";

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    /**
     * view log
     *
     * @param loginUser   login user
     * @param taskInstId task instance id
     * @param skipLineNum skip line number
     * @param limit limit
     * @return log string data
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result<ResponseTaskLog> queryLog(User loginUser, int taskInstId, int skipLineNum, int limit) {

        TaskInstance taskInstance = taskInstanceDao.queryById(taskInstId);

        if (taskInstance == null) {
            log.error("Task instance does not exist, taskInstanceId:{}.", taskInstId);
            return Result.error(Status.TASK_INSTANCE_NOT_FOUND);
        }
        if (StringUtils.isBlank(taskInstance.getHost())) {
            log.error("Host of task instance is null, taskInstanceId:{}.", taskInstId);
            return Result.error(Status.TASK_INSTANCE_HOST_IS_NULL);
        }
        projectService.checkProjectAndAuthThrowException(loginUser, taskInstance.getProjectCode(), VIEW_LOG);
        Result<ResponseTaskLog> result = new Result<>(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg());
        String log = queryLog(taskInstance, skipLineNum, limit);
        int lineNum = log.split("\\r\\n").length;
        result.setData(new ResponseTaskLog(lineNum, log));
        return result;
    }

    /**
     * get log size
     *
     * @param loginUser   login user
     * @param taskInstId task instance id
     * @return log byte array
     */
    @Override
    public byte[] getLogBytes(User loginUser, int taskInstId) {
        TaskInstance taskInstance = taskInstanceDao.queryById(taskInstId);
        if (taskInstance == null || StringUtils.isBlank(taskInstance.getHost())) {
            throw new ServiceException("task instance is null or host is null");
        }
        Project project = projectMapper.queryProjectByTaskInstanceId(taskInstId);
        projectService.checkProjectAndAuthThrowException(loginUser, project, DOWNLOAD_LOG);
        return getLogBytes(taskInstance);
    }

    /**
     * query log
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param taskInstId  task instance id
     * @param skipLineNum skip line number
     * @param limit       limit
     * @return log string data
     */
    @Override
    @SuppressWarnings("unchecked")
    public String queryLog(User loginUser, long projectCode, int taskInstId, int skipLineNum, int limit) {
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, VIEW_LOG);
        // check whether the task instance can be found
        TaskInstance task = taskInstanceDao.queryById(taskInstId);
        if (task == null || StringUtils.isBlank(task.getHost())) {
            throw new ServiceException(Status.TASK_INSTANCE_NOT_FOUND);
        }

        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(task.getTaskCode());
        if (taskDefinition != null && projectCode != taskDefinition.getProjectCode()) {
            throw new ServiceException(Status.TASK_INSTANCE_NOT_FOUND, taskInstId);
        }
        return queryLog(task, skipLineNum, limit);
    }

    /**
     * get log bytes
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param taskInstId  task instance id
     * @return log byte array
     */
    @Override
    public byte[] getLogBytes(User loginUser, long projectCode, int taskInstId) {
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, DOWNLOAD_LOG);

        // check whether the task instance can be found
        TaskInstance task = taskInstanceDao.queryById(taskInstId);
        if (task == null || StringUtils.isBlank(task.getHost())) {
            throw new ServiceException("task instance is null or host is null");
        }

        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(task.getTaskCode());
        if (taskDefinition != null && projectCode != taskDefinition.getProjectCode()) {
            throw new ServiceException("task instance does not exist in project");
        }
        return getLogBytes(task);
    }

    /**
     * query log
     *
     * @param taskInstance  task instance
     * @param skipLineNum skip line number
     * @param limit       limit
     * @return log string data
     */
    private String queryLog(TaskInstance taskInstance, int skipLineNum, int limit) {
        final String logPath = taskInstance.getLogPath();
        log.info("Query task instance log, taskInstanceId:{}, taskInstanceName:{}, host: {}, logPath:{}",
                taskInstance.getId(), taskInstance.getName(), taskInstance.getHost(), logPath);
        if (StringUtils.isBlank(logPath)) {
            throw new ServiceException(Status.QUERY_TASK_INSTANCE_LOG_ERROR,
                    "TaskInstanceLogPath is empty, maybe the taskInstance doesn't be dispatched");
        }

        StringBuilder sb = new StringBuilder();
        if (skipLineNum == 0) {
            String head = String.format(LOG_HEAD_FORMAT,
                    logPath,
                    taskInstance.getHost(),
                    Constants.SYSTEM_LINE_SEPARATOR);
            sb.append(head);
        }

        ILogService iLogService =
                SingletonJdkDynamicRpcClientProxyFactory.getProxyClient(taskInstance.getHost(), ILogService.class);
        try {
            TaskInstanceLogPageQueryRequest request = TaskInstanceLogPageQueryRequest.builder()
                    .taskInstanceId(taskInstance.getId())
                    .taskInstanceLogAbsolutePath(logPath)
                    .skipLineNum(skipLineNum)
                    .limit(limit)
                    .build();
            TaskInstanceLogPageQueryResponse response = iLogService.pageQueryTaskInstanceLog(request);
            String logContent = response.getLogContent();
            if (logContent != null) {
                sb.append(logContent);
            }
            return sb.toString();
        } catch (Throwable ex) {
            throw new ServiceException(Status.QUERY_TASK_INSTANCE_LOG_ERROR, ex.getMessage(), ex);
        }
    }

    /**
     * get log bytes
     *
     * @param taskInstance task instance
     * @return log byte array
     */
    private byte[] getLogBytes(TaskInstance taskInstance) {
        String host = taskInstance.getHost();
        String logPath = taskInstance.getLogPath();

        byte[] head = String.format(LOG_HEAD_FORMAT,
                logPath,
                host,
                Constants.SYSTEM_LINE_SEPARATOR).getBytes(StandardCharsets.UTF_8);

        byte[] logBytes = new byte[0];

        ILogService iLogService =
                SingletonJdkDynamicRpcClientProxyFactory.getProxyClient(taskInstance.getHost(), ILogService.class);
        try {
            TaskInstanceLogFileDownloadRequest request =
                    new TaskInstanceLogFileDownloadRequest(taskInstance.getId(), logPath);
            TaskInstanceLogFileDownloadResponse response = iLogService.getTaskInstanceWholeLogFileBytes(request);
            logBytes = response.getLogBytes();
            return Bytes.concat(head, logBytes);
        } catch (Exception ex) {
            log.error("Download TaskInstance: {} Log Error", taskInstance.getName(), ex);
            throw new ServiceException(Status.DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR);
        }

    }
}
