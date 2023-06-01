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
import org.apache.dolphinscheduler.api.service.TaskInstanceLogService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
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
public class TaskInstanceLogServiceImpl implements TaskInstanceLogService {

    private static final String LOG_HEAD_FORMAT = "[LOG-PATH]: %s, [HOST]:  %s%s";

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private LogClient logClient;

    @Override
    public RollViewLogDTO queryTaskInstanceLog(User loginUser, int taskInstanceId, int skipNum, int limit) {
        TaskInstance taskInstance = taskInstanceDao.findTaskInstanceById(taskInstanceId);
        if (taskInstance == null) {
            throw new ServiceException(Status.TASK_INSTANCE_NOT_EXISTS, taskInstanceId);
        }
        if (StringUtils.isBlank(taskInstance.getLogPath())) {
            throw new ServiceException("TaskInstance log is null");
        }
        return queryTaskInstanceLog(taskInstance, skipNum, limit);
    }

    @Override
    public byte[] downloadTaskInstanceLog(User loginUser, int taskInstanceId) {
        TaskInstance taskInstance = taskInstanceDao.findTaskInstanceById(taskInstanceId);
        if (taskInstance == null) {
            throw new ServiceException(Status.TASK_INSTANCE_NOT_EXISTS, taskInstanceId);
        }
        if (StringUtils.isBlank(taskInstance.getLogPath())) {
            throw new ServiceException("TaskInstance log is null");
        }

        Host host = Host.of(taskInstance.getHost());
        byte[] logBytes = logClient.queryWholeWorkflowInstanceLogBytes(host, taskInstance.getLogPath());
        byte[] head = String.format(LOG_HEAD_FORMAT,
                taskInstance.getLogPath(),
                host,
                Constants.SYSTEM_LINE_SEPARATOR).getBytes(StandardCharsets.UTF_8);
        return Bytes.concat(head, logBytes);
    }

    private RollViewLogDTO queryTaskInstanceLog(TaskInstance taskInstance, int skipLineNum, int limit) {
        Host host = Host.of(taskInstance.getHost());
        StringBuilder log = new StringBuilder();
        if (skipLineNum == 0) {
            String head = String.format(LOG_HEAD_FORMAT,
                    taskInstance.getLogPath(),
                    host,
                    Constants.SYSTEM_LINE_SEPARATOR);
            log.append(head);
        }

        RollViewLogResponse rollViewLogResponse = logClient.queryWorkflowInstanceLog(
                host,
                taskInstance.getLogPath(),
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
                .hasNext(!taskInstance.getState().isFinished()
                        || rollViewLogResponse.getCurrentLineNumber() < rollViewLogResponse
                                .getCurrentTotalLineNumber())
                .build();
    }
}
