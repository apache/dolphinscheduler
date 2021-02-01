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
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private ProcessTaskRelationMapper processTaskRelationMapper;

    /**
     * create task definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param taskDefinitionJson task definition json
     */
    @Transactional(rollbackFor = Exception.class)
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
        checkTaskNode(result, taskNode, taskDefinitionJson);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        long code = 0L;
        try {
            code = SnowFlakeUtils.getInstance().nextId();
        } catch (SnowFlakeException e) {
            logger.error("Task code get error, ", e);
        }
        if (code == 0L) {
            putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS, "Error generating task definition code");
            return result;
        }
        Date now = new Date();
        TaskDefinition taskDefinition = new TaskDefinition(code,
                taskNode.getName(),
                1,
                taskNode.getDesc(),
                project.getCode(),
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

    /**
     * query task definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param taskName task name
     */
    @Override
    public Map<String, Object> queryTaskDefinitionByName(User loginUser, String projectName, String taskName) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }

        TaskDefinition taskDefinition = taskDefinitionMapper.queryByDefinitionName(project.getCode(), taskName);
        if (taskDefinition == null) {
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, taskName);
        } else {
            result.put(Constants.DATA_LIST, taskDefinition);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * delete task definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param taskCode task code
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> deleteTaskDefinitionByCode(User loginUser, String projectName, Long taskCode) {
        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }
        checkTaskRelation(result, taskCode);
        resultEnum = (Status) result.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return result;
        }
        int delete = taskDefinitionMapper.deleteByCode(taskCode);
        if (delete > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR);
        }
        return result;
    }

    /**
     * update task definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param taskCode task code
     * @param taskDefinitionJson task definition json
     */
    @Override
    public Map<String, Object> updateTaskDefinition(User loginUser, String projectName, Long taskCode, String taskDefinitionJson) {
        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }
        checkTaskRelation(result, taskCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        TaskNode taskNode = JSONUtils.parseObject(taskDefinitionJson, TaskNode.class);
        checkTaskNode(result, taskNode, taskDefinitionJson);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        List<TaskDefinitionLog> taskDefinitionLogs = taskDefinitionLogMapper.queryByDefinitionCode(taskCode);
        int version = taskDefinitionLogs
                .stream()
                .map(TaskDefinitionLog::getVersion)
                .max((x, y) -> x > y ? x : y)
                .orElse(0) + 1;

        Date now = new Date();
        TaskDefinition taskDefinition = new TaskDefinition(taskCode,
                taskNode.getName(),
                version,
                taskNode.getDesc(),
                project.getCode(),
                loginUser.getId(),
                TaskType.of(taskNode.getType()),
                taskNode.getParams(),
                taskNode.isForbidden() ? Flag.NO : Flag.YES, taskNode.getTaskInstancePriority(),
                taskNode.getWorkerGroup(), taskNode.getMaxRetryTimes(),
                taskNode.getRetryInterval(),
                taskNode.getTaskTimeoutParameter().getEnable() ? TimeoutFlag.OPEN : TimeoutFlag.CLOSE,
                taskNode.getTaskTimeoutParameter().getStrategy(),
                taskNode.getTaskTimeoutParameter().getInterval(),
                null,
                now);
        taskDefinition.setResourceIds(getResourceIds(taskDefinition));
        taskDefinitionMapper.updateByCode(taskDefinition);
        // save task definition log
        TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
        taskDefinitionLog.set(taskDefinition);
        taskDefinitionLog.setOperator(loginUser.getId());
        taskDefinitionLog.setOperateTime(now);
        taskDefinitionLogMapper.insert(taskDefinitionLog);
        result.put(Constants.DATA_LIST, taskCode);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    public void checkTaskRelation(Map<String, Object> result, Long taskCode) {
        List<ProcessTaskRelation> processTaskRelationList = processTaskRelationMapper.queryByTaskCode(taskCode, taskCode);
        if (!processTaskRelationList.isEmpty()) {
            Set<Long> processDefinitionCodes = processTaskRelationList
                    .stream()
                    .map(ProcessTaskRelation::getProcessDefinitionCode)
                    .collect(Collectors.toSet());
            // check process definition is already online  TODO
//            if (processDefinition.getReleaseState() == ReleaseState.ONLINE) {
//                putMsg(result, Status.PROCESS_DEFINE_STATE_ONLINE, processDefinition.getCode);
//                return result;
//            }
        }
    }

    public void checkTaskNode(Map<String, Object> result, TaskNode taskNode, String taskDefinitionJson) {
        if (taskNode == null) {
            logger.error("taskDefinitionJson is not valid json");
            putMsg(result, Status.DATA_IS_NOT_VALID, taskDefinitionJson);
            return;
        }
        if (!CheckUtils.checkTaskNodeParameters(taskNode.getParams(), taskNode.getName())) {
            logger.error("task node {} parameter invalid", taskNode.getName());
            putMsg(result, Status.PROCESS_NODE_S_PARAMETER_INVALID, taskNode.getName());
        }
    }

    /**
     * update task definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param taskCode task code
     * @param version the version user want to switch
     */
    @Override
    public Map<String, Object> switchVersion(User loginUser, String projectName, Long taskCode, int version) {
        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }
        checkTaskRelation(result, taskCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        TaskDefinitionLog taskDefinitionLog = taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(taskCode, version);
        TaskDefinition taskDefinition = new TaskDefinition(taskCode,
                taskDefinitionLog.getName(),
                version,
                taskDefinitionLog.getDescription(),
                taskDefinitionLog.getProjectCode(),
                loginUser.getId(),
                taskDefinitionLog.getTaskType(),
                taskDefinitionLog.getTaskParams(),
                taskDefinitionLog.getFlag(),
                taskDefinitionLog.getTaskPriority(),
                taskDefinitionLog.getWorkerGroup(),
                taskDefinitionLog.getFailRetryTimes(),
                taskDefinitionLog.getFailRetryInterval(),
                taskDefinitionLog.getTimeoutFlag(),
                taskDefinitionLog.getTaskTimeoutStrategy(),
                taskDefinitionLog.getTimeout(),
                null,
                new Date());
        taskDefinition.setResourceIds(taskDefinitionLog.getResourceIds());
        taskDefinitionMapper.updateByCode(taskDefinition);
        result.put(Constants.DATA_LIST, taskCode);
        putMsg(result, Status.SUCCESS);
        return result;
    }
}

