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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.LoggerService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ResponseTaskLog;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.service.log.LogClient;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.primitives.Bytes;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.DOWNLOAD_LOG;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.VIEW_LOG;

/**
 * logger service impl
 */
@Service
public class LoggerServiceImpl extends BaseServiceImpl implements LoggerService {

    private static final Logger logger = LoggerFactory.getLogger(LoggerServiceImpl.class);

    private static final String LOG_HEAD_FORMAT = "[LOG-PATH]: %s, [HOST]:  %s%s";

    @Autowired
    private ProcessService processService;

    @Autowired
    private LogClient logClient;

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    ProjectService projectService;

    @Autowired
    TaskDefinitionMapper taskDefinitionMapper;

    /**
     * view log
     *
     * @param taskInstId task instance id
     * @param skipLineNum skip line number
     * @param limit limit
     * @return log string data
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result<ResponseTaskLog> queryLog(int taskInstId, int skipLineNum, int limit) {

        TaskInstance taskInstance = processService.findTaskInstanceById(taskInstId);

        if (taskInstance == null) {
            logger.error("Task instance does not exist, taskInstanceId:{}.", taskInstId);
            return Result.error(Status.TASK_INSTANCE_NOT_FOUND);
        }
        if (StringUtils.isBlank(taskInstance.getHost())) {
            logger.error("Host of task instance is null, taskInstanceId:{}.", taskInstId);
            return Result.error(Status.TASK_INSTANCE_HOST_IS_NULL);
        }
        Result<ResponseTaskLog> result = new Result<>(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg());
        String log = queryLog(taskInstance, skipLineNum, limit);
        int lineNum = log.split("\\r\\n").length;
        result.setData(new ResponseTaskLog(lineNum, log));
        return result;
    }


    /**
     * get log size
     *
     * @param taskInstId task instance id
     * @return log byte array
     */
    @Override
    public byte[] getLogBytes(int taskInstId) {
        TaskInstance taskInstance = processService.findTaskInstanceById(taskInstId);
        if (taskInstance == null || StringUtils.isBlank(taskInstance.getHost())) {
            throw new ServiceException("task instance is null or host is null");
        }
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
    public Map<String, Object> queryLog(User loginUser, long projectCode, int taskInstId, int skipLineNum, int limit) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode, VIEW_LOG);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        // check whether the task instance can be found
        TaskInstance task = processService.findTaskInstanceById(taskInstId);
        if (task == null || StringUtils.isBlank(task.getHost())) {
            putMsg(result, Status.TASK_INSTANCE_NOT_FOUND);
            return result;
        }

        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(task.getTaskCode());
        if (taskDefinition != null && projectCode != taskDefinition.getProjectCode()) {
            putMsg(result, Status.TASK_INSTANCE_NOT_FOUND, taskInstId);
            return result;
        }
        String log = queryLog(task, skipLineNum, limit);
        result.put(Constants.DATA_LIST, log);
        return result;
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
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode,DOWNLOAD_LOG);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            throw new ServiceException("user has no permission");
        }
        // check whether the task instance can be found
        TaskInstance task = processService.findTaskInstanceById(taskInstId);
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
        Host host = Host.of(taskInstance.getHost());

        logger.info("Query task instance log, taskInstanceId:{}, taskInstanceName:{}, host:{}, logPath:{}, port:{}",
                taskInstance.getId(), taskInstance.getName(), host.getIp(), taskInstance.getLogPath(), host.getPort());

        StringBuilder log = new StringBuilder();
        if (skipLineNum == 0) {
            String head = String.format(LOG_HEAD_FORMAT,
                    taskInstance.getLogPath(),
                    host,
                    Constants.SYSTEM_LINE_SEPARATOR);
            log.append(head);
        }

        log.append(logClient
                .rollViewLog(host.getIp(), host.getPort(), taskInstance.getLogPath(), skipLineNum, limit));

        return log.toString();
    }

    /**
     * get log bytes
     *
     * @param taskInstance task instance
     * @return log byte array
     */
    private byte[] getLogBytes(TaskInstance taskInstance) {
        Host host = Host.of(taskInstance.getHost());
        byte[] head = String.format(LOG_HEAD_FORMAT,
                taskInstance.getLogPath(),
                host,
                Constants.SYSTEM_LINE_SEPARATOR).getBytes(StandardCharsets.UTF_8);
        return Bytes.concat(head,
                logClient.getLogBytes(host.getIp(), host.getPort(), taskInstance.getLogPath()));
    }
}
