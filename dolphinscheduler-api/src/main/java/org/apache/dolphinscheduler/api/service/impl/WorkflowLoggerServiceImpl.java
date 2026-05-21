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
import org.apache.dolphinscheduler.api.executor.logging.LogClientDelegate;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.WorkflowLoggerService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.ResponseWorkflowLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.primitives.Bytes;

/**
 * Workflow logger service impl
 */
@Service
@Slf4j
public class WorkflowLoggerServiceImpl extends BaseServiceImpl implements WorkflowLoggerService {

    private static final String LOG_HEAD_FORMAT = "[LOG-PATH]: %s, [HOST]: %s%s";

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private LogClientDelegate logClientDelegate;

    @Override
    public Result<ResponseWorkflowLog> queryWorkflowLog(User loginUser, int workflowInstanceId, int skipLineNum,
                                                        int limit) {
        WorkflowInstance workflowInstance = workflowInstanceDao.queryById(workflowInstanceId);
        if (workflowInstance == null) {
            log.error("Workflow instance does not exist, workflowInstanceId:{}.", workflowInstanceId);
            return Result.error(Status.WORKFLOW_INSTANCE_NOT_FOUND);
        }
        if (StringUtils.isBlank(workflowInstance.getHost())) {
            log.error("Host of workflow instance is null, workflowInstanceId:{}.", workflowInstanceId);
            return Result.error(Status.WORKFLOW_INSTANCE_HOST_IS_NULL);
        }
        projectService.checkProjectAndAuthThrowException(loginUser, workflowInstance.getProjectCode(), VIEW_LOG);
        String log = queryWorkflowLog(workflowInstance, skipLineNum, limit);
        int lineNum = log.split("\\r\\n").length;
        return Result.success(new ResponseWorkflowLog(lineNum, log));
    }

    @Override
    public byte[] getWorkflowLogBytes(User loginUser, int workflowInstanceId) {
        WorkflowInstance workflowInstance = workflowInstanceDao.queryById(workflowInstanceId);
        if (workflowInstance == null || StringUtils.isBlank(workflowInstance.getHost())) {
            throw new RuntimeException("workflow instance is null or host is null");
        }
        projectService.checkProjectAndAuthThrowException(loginUser, workflowInstance.getProjectCode(), DOWNLOAD_LOG);
        return getWorkflowLogBytes(workflowInstance);
    }

    @Override
    public String queryWorkflowLog(User loginUser, long projectCode, int workflowInstanceId, int skipLineNum,
                                   int limit) {
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, VIEW_LOG);
        WorkflowInstance workflowInstance = workflowInstanceDao.queryById(workflowInstanceId);
        if (workflowInstance == null || StringUtils.isBlank(workflowInstance.getHost())) {
            throw new RuntimeException("Workflow instance not found or host is null");
        }
        if (projectCode != workflowInstance.getProjectCode()) {
            throw new RuntimeException("Workflow instance does not exist in project");
        }
        return queryWorkflowLog(workflowInstance, skipLineNum, limit);
    }

    @Override
    public byte[] getWorkflowLogBytes(User loginUser, long projectCode, int workflowInstanceId) {
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, DOWNLOAD_LOG);
        WorkflowInstance workflowInstance = workflowInstanceDao.queryById(workflowInstanceId);
        if (workflowInstance == null || StringUtils.isBlank(workflowInstance.getHost())) {
            throw new RuntimeException("Workflow instance not found or host is null");
        }
        if (projectCode != workflowInstance.getProjectCode()) {
            throw new RuntimeException("Workflow instance does not exist in project");
        }
        return getWorkflowLogBytes(workflowInstance);
    }

    private String queryWorkflowLog(WorkflowInstance workflowInstance, int skipLineNum, int limit) {
        final String logPath = workflowInstance.getLogPath();
        log.info("Query workflow instance log, workflowInstanceId:{}, workflowInstanceName:{}, host: {}, logPath:{}",
                workflowInstance.getId(), workflowInstance.getName(), workflowInstance.getHost(), logPath);
        if (StringUtils.isBlank(logPath)) {
            throw new RuntimeException("WorkflowInstanceLogPath is empty");
        }

        StringBuilder sb = new StringBuilder();
        if (skipLineNum == 0) {
            String head = String.format(LOG_HEAD_FORMAT,
                    logPath,
                    workflowInstance.getHost(),
                    Constants.SYSTEM_LINE_SEPARATOR);
            sb.append(head);
        }

        try {
            String logContent = logClientDelegate.getWorkflowPartLogString(workflowInstance, skipLineNum, limit);
            if (logContent != null) {
                sb.append(logContent);
            }
            return sb.toString();
        } catch (Throwable ex) {
            log.error("Query workflow instance log error", ex);
            throw new RuntimeException("Query workflow instance log error: " + ex.getMessage(), ex);
        }
    }

    private byte[] getWorkflowLogBytes(WorkflowInstance workflowInstance) {
        String host = workflowInstance.getHost();
        String logPath = workflowInstance.getLogPath();

        byte[] head = String.format(LOG_HEAD_FORMAT,
                logPath,
                host,
                Constants.SYSTEM_LINE_SEPARATOR).getBytes(StandardCharsets.UTF_8);

        byte[] logBytes;

        try {
            logBytes = logClientDelegate.getWorkflowWholeLogBytes(workflowInstance);
            return Bytes.concat(head, logBytes);
        } catch (Exception ex) {
            log.error("Download WorkflowInstance: {} Log Error", workflowInstance.getName(), ex);
            throw new RuntimeException("Download workflow instance log error", ex);
        }
    }
}
