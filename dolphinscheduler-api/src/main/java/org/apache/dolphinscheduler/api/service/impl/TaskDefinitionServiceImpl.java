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
import org.apache.dolphinscheduler.api.service.BaseService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.TaskDefinitionService;
import org.apache.dolphinscheduler.api.utils.CheckUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.SnowFlakeUtils;
import org.apache.dolphinscheduler.common.utils.SnowFlakeUtils.SnowFlakeException;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.common.utils.TaskParametersUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * task definition service impl
 */
@Service
public class TaskDefinitionServiceImpl extends BaseService implements
        TaskDefinitionService {

    private static final Logger logger = LoggerFactory.getLogger(TaskDefinitionServiceImpl.class);

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;


    /**
     * create task definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param taskDefinitionJson task definition json
     */
    @Transactional
    @Override
    public Map<String, Object> createTaskDefinition(User loginUser,
                                                    String projectName,
                                                    String taskDefinitionJson) {

        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByName(projectName);
        // check project auth
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }

        TaskNode taskNode = JSONUtils.parseObject(taskDefinitionJson, TaskNode.class);
        if (taskNode == null) {
            logger.error("taskDefinitionJson is not valid json");
            putMsg(result, Status.DATA_IS_NOT_VALID, taskDefinitionJson);
            return result;
        }
        if (!CheckUtils.checkTaskNodeParameters(taskNode.getParams(), taskNode.getName())) {
            logger.error("task node {} parameter invalid", taskNode.getName());
            putMsg(result, Status.PROCESS_NODE_S_PARAMETER_INVALID, taskNode.getName());
            return result;
        }
        long code = 0L;
        try {
            code = SnowFlakeUtils.getInstance().nextId();
        } catch (SnowFlakeException e) {
            logger.error("Task code get error, ", e);
        }
        if (code == 0L) {
            putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS);// TODO code message
            return result;
        }
        Date now = new Date();
        TaskDefinition taskDefinition = new TaskDefinition(code,
                taskNode.getName(),
                1,
                taskNode.getDesc(),
                0L, // TODO  project.getCode()
                loginUser.getId(),
                TaskType.of(taskNode.getType()),
                taskNode.getParams(),
                taskNode.isForbidden() ? Flag.NO : Flag.YES, taskNode.getTaskInstancePriority(),
                taskNode.getWorkerGroup(), taskNode.getMaxRetryTimes(),
                taskNode.getRetryInterval(),
                taskNode.getTaskTimeoutParameter().getEnable() ? TimeoutFlag.OPEN : TimeoutFlag.CLOSE,
                taskNode.getTaskTimeoutParameter().getStrategy(),
                taskNode.getTaskTimeoutParameter().getInterval(),
                now,
                now);
        taskDefinition.setResourceIds(getResourceIds(taskDefinition));
        // save the new task definition
        taskDefinitionMapper.insert(taskDefinition);
        // save task definition log
        TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
        taskDefinitionLog.set(taskDefinition);
        taskDefinitionLog.setOperator(loginUser.getId());
        taskDefinitionLog.setOperateTime(now);
        taskDefinitionLogMapper.insert(taskDefinitionLog);
        // return taskDefinition object with code
        result.put(Constants.DATA_LIST, code);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * get resource ids
     *
     * @param taskDefinition taskDefinition
     * @return resource ids
     */
    private String getResourceIds(TaskDefinition taskDefinition) {
        Set<Integer> resourceIds = null;
        // TODO modify taskDefinition.getTaskType()
        AbstractParameters params = TaskParametersUtils.getParameters(taskDefinition.getTaskType().getDescp(), taskDefinition.getTaskParams());

        if (params != null && CollectionUtils.isNotEmpty(params.getResourceFilesList())) {
            resourceIds = params.getResourceFilesList().
                    stream()
                    .filter(t -> t.getId() != 0)
                    .map(ResourceInfo::getId)
                    .collect(Collectors.toSet());
        }
        if (CollectionUtils.isEmpty(resourceIds)) {
            return StringUtils.EMPTY;
        }
        return StringUtils.join(resourceIds, ",");
    }

}

