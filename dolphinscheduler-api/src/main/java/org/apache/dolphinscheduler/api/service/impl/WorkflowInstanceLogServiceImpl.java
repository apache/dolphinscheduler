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

import org.apache.dolphinscheduler.api.dto.log.RollViewLogDTO;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.WorkflowInstanceLogService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.remote.command.log.RollViewLogResponse;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.service.log.LogClient;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.primitives.Bytes;

@Slf4j
@Service
public class WorkflowInstanceLogServiceImpl implements WorkflowInstanceLogService {

    private static final String LOG_HEAD_FORMAT = "[LOG-PATH]: %s, [HOST]:  %s%s";

    @Autowired
    private ProcessInstanceDao processInstanceDao;

    @Autowired
    private LogClient logClient;

    @Override
    public RollViewLogDTO queryWorkflowInstanceLog(User loginUser, int workflowInstanceId, int skipLineNum, int limit) {
        ProcessInstance workflowInstance = processInstanceDao.queryByWorkflowInstanceId(workflowInstanceId);
        if (workflowInstance == null) {
            throw new ServiceException(Status.PROCESS_INSTANCE_NOT_EXIST, workflowInstanceId);
        }

        if (StringUtils.isBlank(workflowInstance.getWorkflowInstanceLogPath())) {
            throw new ServiceException("WorkflowInstance log is null");
        }
        // todo: check permission
        return queryWorkflowInstanceLog(workflowInstance, skipLineNum, limit);
    }

    @Override
    public byte[] downloadWorkflowInstanceLog(User loginUser, int workflowInstanceId) {
        ProcessInstance workflowInstance = processInstanceDao.queryByWorkflowInstanceId(workflowInstanceId);
        if (workflowInstance == null) {
            throw new ServiceException(Status.PROCESS_INSTANCE_NOT_EXIST, workflowInstanceId);
        }

        if (StringUtils.isBlank(workflowInstance.getWorkflowInstanceLogPath())) {
            throw new ServiceException("WorkflowInstance log is null");
        }

        Host host = Host.of(workflowInstance.getHost());
        byte[] logBytes =
                logClient.queryWholeWorkflowInstanceLogBytes(host, workflowInstance.getWorkflowInstanceLogPath());
        byte[] head = String.format(LOG_HEAD_FORMAT,
                workflowInstance.getWorkflowInstanceLogPath(),
                host,
                Constants.SYSTEM_LINE_SEPARATOR).getBytes(StandardCharsets.UTF_8);
        return Bytes.concat(head, logBytes);
    }

    private RollViewLogDTO queryWorkflowInstanceLog(ProcessInstance workflowInstance, int skipLineNum, int limit) {
        if (StringUtils.isEmpty(workflowInstance.getWorkflowInstanceLogPath())) {
            throw new ServiceException("Workflow instance log path is null");
        }
        Host host = Host.of(workflowInstance.getHost());
        StringBuilder log = new StringBuilder();
        if (skipLineNum == 0) {
            String head = String.format(LOG_HEAD_FORMAT,
                    workflowInstance.getWorkflowInstanceLogPath(),
                    host,
                    Constants.SYSTEM_LINE_SEPARATOR);
            log.append(head);
        }

        RollViewLogResponse rollViewLogResponse = logClient.queryWorkflowInstanceLog(
                host,
                workflowInstance.getWorkflowInstanceLogPath(),
                skipLineNum,
                limit);
        if (rollViewLogResponse.getResponseStatus() != RollViewLogResponse.Status.SUCCESS) {
            log.append(rollViewLogResponse.getResponseStatus().getDesc());
            return RollViewLogDTO.builder()
                    .log(log.toString())
                    .hasNext(false)
                    .build();
        }
        log.append(rollViewLogResponse.getLog());
        // If the task doesn't finish or the log doesn't end can query next
        return RollViewLogDTO.builder()
                .log(log.toString())
                .currentLogLineNumber(rollViewLogResponse.getCurrentLineNumber())
                .hasNext(!workflowInstance.getState().isFinished()
                        || rollViewLogResponse.getCurrentLineNumber() < rollViewLogResponse
                                .getCurrentTotalLineNumber())
                .build();
    }

}
