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
import org.apache.dolphinscheduler.api.executor.logging.LogClientDelegate;
import org.apache.dolphinscheduler.api.executor.logging.TaskLogType;
import org.apache.dolphinscheduler.api.service.LoggerService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ResponseTaskLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.primitives.Bytes;

@Service
@Slf4j
public class LoggerServiceImpl extends BaseServiceImpl implements LoggerService {

    private static final String LOG_HEAD_FORMAT = "[LOG-PATH]: %s, [HOST]: %s%s";

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private LogClientDelegate logClientDelegate;

    /**
     * view log
     *
     * @param loginUser   login user
     * @param taskInstId  task instance id
     * @param skipLineNum skip line number
     * @param limit       limit
     * @return log string data
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result<ResponseTaskLog> queryTaskLog(User loginUser, int taskInstId, int skipLineNum, int limit) {
        return queryLog(loginUser, taskInstId, skipLineNum, limit, TaskLogType.LOG);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<ResponseTaskLog> queryTaskOutput(User loginUser, int taskInstId, int skipLineNum, int limit) {
        return queryLog(loginUser, taskInstId, skipLineNum, limit, TaskLogType.OUTPUT);
    }

    private Result<ResponseTaskLog> queryLog(User loginUser,
                                             int taskInstId,
                                             int skipLineNum,
                                             int limit,
                                             TaskLogType taskLogType) {
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
        String log = queryLog(taskInstance, skipLineNum, limit, taskLogType);
        int lineNum = log.split("\\r\\n").length;
        result.setData(new ResponseTaskLog(lineNum, log));
        return result;
    }

    /**
     * get log size
     *
     * @param loginUser  login user
     * @param taskInstId task instance id
     * @return log byte array
     */
    @Override
    public byte[] getTaskLogBytes(User loginUser, int taskInstId) {
        return getLogBytes(loginUser, taskInstId, TaskLogType.LOG);
    }

    @Override
    public byte[] getTaskOutputBytes(User loginUser, int taskInstId) {
        return getLogBytes(loginUser, taskInstId, TaskLogType.OUTPUT);
    }

    private byte[] getLogBytes(User loginUser, int taskInstId, TaskLogType taskLogType) {
        TaskInstance taskInstance = taskInstanceDao.queryById(taskInstId);
        if (taskInstance == null || StringUtils.isBlank(taskInstance.getHost())) {
            throw new ServiceException("task instance is null or host is null");
        }
        Project project = projectMapper.queryProjectByTaskInstanceId(taskInstId);
        projectService.checkProjectAndAuthThrowException(loginUser, project, DOWNLOAD_LOG);
        return getLogBytes(taskInstance, taskLogType);
    }

    /**
     * query log
     *
     * @param taskInstance task instance
     * @param skipLineNum  skip line number
     * @param limit        limit
     * @return log string data
     */
    private String queryLog(TaskInstance taskInstance, int skipLineNum, int limit, TaskLogType taskLogType) {
        String logPath = taskLogType.getLogPath(taskInstance);
        log.info("Query task instance log, taskInstanceId:{}, taskInstanceName:{}, host: {}, logPath:{}",
                taskInstance.getId(), taskInstance.getName(), taskInstance.getHost(), logPath);
        if (StringUtils.isBlank(logPath)) {
            throw new ServiceException(Status.QUERY_TASK_INSTANCE_LOG_ERROR,
                    "TaskInstanceLogPath is empty, maybe the taskInstance doesn't be dispatched");
        }

        StringBuilder sb = new StringBuilder();
        if (shouldAppendLogHead(taskLogType) && skipLineNum == 0) {
            String head = String.format(LOG_HEAD_FORMAT,
                    logPath,
                    taskInstance.getHost(),
                    Constants.SYSTEM_LINE_SEPARATOR);
            sb.append(head);
        }

        try {
            String logContent = taskLogType == TaskLogType.LOG
                    ? logClientDelegate.getTaskLogString(taskInstance, skipLineNum, limit)
                    : logClientDelegate.getTaskOutputString(taskInstance, skipLineNum, limit);
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
    private byte[] getLogBytes(TaskInstance taskInstance, TaskLogType taskLogType) {
        String host = taskInstance.getHost();
        String logPath = taskLogType.getLogPath(taskInstance);

        byte[] head = String.format(LOG_HEAD_FORMAT,
                logPath,
                host,
                Constants.SYSTEM_LINE_SEPARATOR).getBytes(StandardCharsets.UTF_8);

        byte[] logBytes;

        try {
            logBytes = taskLogType == TaskLogType.LOG
                    ? logClientDelegate.getTaskLogBytes(taskInstance)
                    : logClientDelegate.getTaskOutputBytes(taskInstance);
            if (!shouldAppendLogHead(taskLogType)) {
                return logBytes;
            }
            return Bytes.concat(head, logBytes);
        } catch (Exception ex) {
            log.error("Download TaskInstance: {} Log Error", taskInstance.getName(), ex);
            throw new ServiceException(Status.DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR);
        }
    }

    private boolean shouldAppendLogHead(TaskLogType taskLogType) {
        return taskLogType == TaskLogType.LOG;
    }
}
