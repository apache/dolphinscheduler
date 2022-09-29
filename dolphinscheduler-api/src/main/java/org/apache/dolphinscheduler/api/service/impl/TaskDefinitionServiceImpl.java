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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_DEFINITION;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_DEFINITION_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_DEFINITION_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_DEFINITION_UPDATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_VERSION_VIEW;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_DEFINITION;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_SWITCH_TO_THIS_VERSION;

import org.apache.dolphinscheduler.api.dto.task.TaskCreateRequest;
import org.apache.dolphinscheduler.api.dto.task.TaskFilterRequest;
import org.apache.dolphinscheduler.api.dto.task.TaskUpdateRequest;
import org.apache.dolphinscheduler.api.dto.taskRelation.TaskRelationUpdateUpstreamRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowUpdateRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.permission.PermissionCheck;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.service.ProcessTaskRelationService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.TaskDefinitionService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.ConditionType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils.CodeGenerateException;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskMainInfo;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ParametersNode;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.task.TaskPluginManager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;

/**
 * task definition service impl
 */
@Service
public class TaskDefinitionServiceImpl extends BaseServiceImpl implements TaskDefinitionService {

    private static final Logger logger = LoggerFactory.getLogger(TaskDefinitionServiceImpl.class);

    private static final String RELEASESTATE = "releaseState";

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

    @Autowired
    private ProcessTaskRelationService processTaskRelationService;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private ProcessService processService;

    @Autowired
    private TaskPluginManager taskPluginManager;

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    /**
     * create task definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskDefinitionJson task definition json
     */
    @Transactional
    @Override
    public Map<String, Object> createTaskDefinition(User loginUser,
                                                    long projectCode,
                                                    String taskDefinitionJson) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, TASK_DEFINITION_CREATE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);
        if (taskDefinitionLogs.isEmpty()) {
            logger.warn("Parameter taskDefinitionJson is invalid.");
            putMsg(result, Status.DATA_IS_NOT_VALID, taskDefinitionJson);
            return result;
        }
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
            if (!taskPluginManager.checkTaskParameters(ParametersNode.builder()
                    .taskType(taskDefinitionLog.getTaskType())
                    .taskParams(taskDefinitionLog.getTaskParams())
                    .dependence(taskDefinitionLog.getDependence())
                    .build())) {
                logger.warn("Task definition {} parameters are invalid.", taskDefinitionLog.getName());
                putMsg(result, Status.PROCESS_NODE_S_PARAMETER_INVALID, taskDefinitionLog.getName());
                return result;
            }
        }
        int saveTaskResult = processService.saveTaskDefine(loginUser, projectCode, taskDefinitionLogs, Boolean.TRUE);
        if (saveTaskResult == Constants.DEFINITION_FAILURE) {
            logger.error("Create task definition error, projectCode:{}.", projectCode);
            putMsg(result, Status.CREATE_TASK_DEFINITION_ERROR);
            throw new ServiceException(Status.CREATE_TASK_DEFINITION_ERROR);
        }
        Map<String, Object> resData = new HashMap<>();
        resData.put("total", taskDefinitionLogs.size());
        resData.put("code", StringUtils
                .join(taskDefinitionLogs.stream().map(TaskDefinition::getCode).collect(Collectors.toList()), ","));
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, resData);
        return result;
    }

    private TaskDefinitionLog persist2TaskDefinitionLog(User user, TaskDefinition taskDefinition) {
        TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog(taskDefinition);
        taskDefinitionLog.setOperator(user.getId());
        taskDefinitionLog.setOperateTime(new Date());
        int result = taskDefinitionLogMapper.insert(taskDefinitionLog);
        if (result <= 0) {
            throw new ServiceException(Status.CREATE_TASK_DEFINITION_LOG_ERROR, taskDefinitionLog.getName());
        }
        return taskDefinitionLog;
    }

    private void checkTaskDefinitionValid(User user, TaskDefinition taskDefinition, String permissions) {
        // check user access for project
        Project project = projectMapper.queryByCode(taskDefinition.getProjectCode());
        projectService.checkProjectAndAuthThrowException(user, project, permissions);

        if (!taskPluginManager.checkTaskParameters(ParametersNode.builder()
                .taskType(taskDefinition.getTaskType())
                .taskParams(taskDefinition.getTaskParams())
                .dependence(taskDefinition.getDependence())
                .build())) {
            throw new ServiceException(Status.PROCESS_NODE_S_PARAMETER_INVALID, taskDefinition.getName());
        }
    }

    private List<ProcessTaskRelation> updateTaskUpstreams(User user, long workflowCode, long taskCode,
                                                          String upstreamCodes) {
        TaskRelationUpdateUpstreamRequest taskRelationUpdateUpstreamRequest = new TaskRelationUpdateUpstreamRequest();
        taskRelationUpdateUpstreamRequest.setWorkflowCode(workflowCode);
        taskRelationUpdateUpstreamRequest.setUpstreams(upstreamCodes);
        return processTaskRelationService.updateUpstreamTaskDefinition(user, taskCode,
                taskRelationUpdateUpstreamRequest);
    }

    private ProcessDefinition updateWorkflowLocation(User user, ProcessDefinition processDefinition) {
        WorkflowUpdateRequest workflowUpdateRequest = new WorkflowUpdateRequest();
        workflowUpdateRequest.setLocation(null);
        return processDefinitionService.updateSingleProcessDefinition(user, processDefinition.getCode(),
                workflowUpdateRequest);
    }

    /**
     * Create resource task definition
     *
     * @param loginUser login user
     * @param taskCreateRequest task definition json
     * @return new TaskDefinition have created
     */
    @Override
    @Transactional
    public TaskDefinition createTaskDefinitionV2(User loginUser,
                                                 TaskCreateRequest taskCreateRequest) {
        TaskDefinition taskDefinition = taskCreateRequest.convert2TaskDefinition();
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(taskCreateRequest.getWorkflowCode());
        if (processDefinition == null) {
            throw new ServiceException(Status.PROCESS_DEFINE_NOT_EXIST, taskCreateRequest.getWorkflowCode());
        }
        // Add project code from process definition if not exists
        if (taskDefinition.getProjectCode() == 0L) {
            taskDefinition.setProjectCode(processDefinition.getProjectCode());
        }
        this.checkTaskDefinitionValid(loginUser, taskDefinition, TASK_DEFINITION_CREATE);

        long taskDefinitionCode;
        try {
            taskDefinitionCode = CodeGenerateUtils.getInstance().genCode();
        } catch (CodeGenerateException e) {
            throw new ServiceException(Status.INTERNAL_SERVER_ERROR_ARGS);
        }
        taskDefinition.setCode(taskDefinitionCode);

        int create = taskDefinitionMapper.insert(taskDefinition);
        if (create <= 0) {
            throw new ServiceException(Status.CREATE_TASK_DEFINITION_ERROR);
        }
        this.persist2TaskDefinitionLog(loginUser, taskDefinition);

        // update related objects: task relationship, workflow's location(need to set to null and front-end will auto
        // format it)
        this.updateTaskUpstreams(loginUser, taskCreateRequest.getWorkflowCode(), taskDefinition.getCode(),
                taskCreateRequest.getUpstreamTasksCodes());
        this.updateWorkflowLocation(loginUser, processDefinition);
        return taskDefinition;
    }

    /**
     * create single task definition that binds the workflow
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCode process definition code
     * @param taskDefinitionJsonObj task definition json object
     * @param upstreamCodes upstream task codes, sep comma
     * @return create result code
     */
    @Transactional
    @Override
    public Map<String, Object> createTaskBindsWorkFlow(User loginUser,
                                                       long projectCode,
                                                       long processDefinitionCode,
                                                       String taskDefinitionJsonObj,
                                                       String upstreamCodes) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, TASK_DEFINITION_CREATE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(processDefinitionCode);
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            logger.error("Process definition does not exist, processDefinitionCode:{}.", processDefinitionCode);
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(processDefinitionCode));
            return result;
        }
        if (processDefinition.getReleaseState() == ReleaseState.ONLINE) {
            logger.warn("Task definition can not be created due to process definition is {}, processDefinitionCode:{}.",
                    ReleaseState.ONLINE.getDescp(), processDefinition.getCode());
            putMsg(result, Status.PROCESS_DEFINE_STATE_ONLINE, String.valueOf(processDefinitionCode));
            return result;
        }
        TaskDefinitionLog taskDefinition = JSONUtils.parseObject(taskDefinitionJsonObj, TaskDefinitionLog.class);
        if (taskDefinition == null) {
            logger.warn("Parameter taskDefinitionJsonObj is invalid json.");
            putMsg(result, Status.DATA_IS_NOT_VALID, taskDefinitionJsonObj);
            return result;
        }
        if (!taskPluginManager.checkTaskParameters(ParametersNode.builder()
                .taskType(taskDefinition.getTaskType())
                .taskParams(taskDefinition.getTaskParams())
                .dependence(taskDefinition.getDependence())
                .build())) {
            logger.error("Task definition {} parameters are invalid", taskDefinition.getName());
            putMsg(result, Status.PROCESS_NODE_S_PARAMETER_INVALID, taskDefinition.getName());
            return result;
        }
        long taskCode = taskDefinition.getCode();
        if (taskCode == 0) {
            taskDefinition.setCode(CodeGenerateUtils.getInstance().genCode());
        }
        List<ProcessTaskRelationLog> processTaskRelationLogList =
                processTaskRelationMapper.queryByProcessCode(projectCode, processDefinitionCode)
                        .stream()
                        .map(ProcessTaskRelationLog::new)
                        .collect(Collectors.toList());

        if (StringUtils.isNotBlank(upstreamCodes)) {
            Set<Long> upstreamTaskCodes = Arrays.stream(upstreamCodes.split(Constants.COMMA)).map(Long::parseLong)
                    .collect(Collectors.toSet());
            List<TaskDefinition> upstreamTaskDefinitionList = taskDefinitionMapper.queryByCodeList(upstreamTaskCodes);
            Set<Long> queryUpStreamTaskCodes =
                    upstreamTaskDefinitionList.stream().map(TaskDefinition::getCode).collect(Collectors.toSet());
            // upstreamTaskCodes - queryUpStreamTaskCodes
            Set<Long> diffCode = upstreamTaskCodes.stream().filter(code -> !queryUpStreamTaskCodes.contains(code))
                    .collect(Collectors.toSet());
            if (!diffCode.isEmpty()) {
                String taskCodes = StringUtils.join(diffCode, Constants.COMMA);
                logger.error("Some task definitions with parameter upstreamCodes do not exist, taskDefinitionCodes:{}.",
                        taskCodes);
                putMsg(result, Status.TASK_DEFINE_NOT_EXIST, taskCodes);
                return result;
            }
            for (TaskDefinition upstreamTask : upstreamTaskDefinitionList) {
                ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
                processTaskRelationLog.setPreTaskCode(upstreamTask.getCode());
                processTaskRelationLog.setPreTaskVersion(upstreamTask.getVersion());
                processTaskRelationLog.setPostTaskCode(taskCode);
                processTaskRelationLog.setPostTaskVersion(Constants.VERSION_FIRST);
                processTaskRelationLog.setConditionType(ConditionType.NONE);
                processTaskRelationLog.setConditionParams("{}");
                processTaskRelationLogList.add(processTaskRelationLog);
            }
        } else {
            ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
            processTaskRelationLog.setPreTaskCode(0);
            processTaskRelationLog.setPreTaskVersion(0);
            processTaskRelationLog.setPostTaskCode(taskCode);
            processTaskRelationLog.setPostTaskVersion(Constants.VERSION_FIRST);
            processTaskRelationLog.setConditionType(ConditionType.NONE);
            processTaskRelationLog.setConditionParams("{}");
            processTaskRelationLogList.add(processTaskRelationLog);
        }
        int insertResult = processService.saveTaskRelation(loginUser, projectCode, processDefinition.getCode(),
                processDefinition.getVersion(),
                processTaskRelationLogList, Lists.newArrayList(), Boolean.TRUE);
        if (insertResult != Constants.EXIT_CODE_SUCCESS) {
            logger.error(
                    "Save new version process task relations error, processDefinitionCode:{}, processDefinitionVersion:{}.",
                    processDefinition.getCode(), processDefinition.getVersion());
            putMsg(result, Status.CREATE_PROCESS_TASK_RELATION_ERROR);
            throw new ServiceException(Status.CREATE_PROCESS_TASK_RELATION_ERROR);
        } else
            logger.info(
                    "Save new version process task relations complete, processDefinitionCode:{}, processDefinitionVersion:{}.",
                    processDefinition.getCode(), processDefinition.getVersion());

        int saveTaskResult =
                processService.saveTaskDefine(loginUser, projectCode, Lists.newArrayList(taskDefinition), Boolean.TRUE);
        if (saveTaskResult == Constants.DEFINITION_FAILURE) {
            logger.error("Save task definition error, projectCode:{}, taskDefinitionCode:{}.", projectCode,
                    taskDefinition.getCode());
            putMsg(result, Status.CREATE_TASK_DEFINITION_ERROR);
            throw new ServiceException(Status.CREATE_TASK_DEFINITION_ERROR);
        } else
            logger.info("Save task definition complete, projectCode:{}, taskDefinitionCode:{}.", projectCode,
                    taskDefinition.getCode());
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, taskDefinition);
        return result;
    }

    /**
     * query task definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processCode process code
     * @param taskName task name
     */
    @Override
    public Map<String, Object> queryTaskDefinitionByName(User loginUser, long projectCode, long processCode,
                                                         String taskName) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, TASK_DEFINITION);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        TaskDefinition taskDefinition = taskDefinitionMapper.queryByName(project.getCode(), processCode, taskName);
        if (taskDefinition == null) {
            logger.error("Task definition does not exist, taskName:{}.", taskName);
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, taskName);
        } else {
            result.put(Constants.DATA_LIST, taskDefinition);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * Whether task definition can be deleted or not
     */
    private void taskCanDeleteValid(User user, TaskDefinition taskDefinition) {
        // check user access for project
        Project project = projectMapper.queryByCode(taskDefinition.getProjectCode());
        projectService.checkProjectAndAuthThrowException(user, project, TASK_DEFINITION_DELETE);

        // Whether task relation workflow is online
        if (processService.isTaskOnline(taskDefinition.getCode()) && taskDefinition.getFlag() == Flag.YES) {
            throw new ServiceException(Status.TASK_DEFINE_STATE_ONLINE, taskDefinition.getCode());
        }

        // Whether task have downstream tasks
        List<ProcessTaskRelation> processTaskRelationList =
                processTaskRelationMapper.queryDownstreamByTaskCode(taskDefinition.getCode());
        if (!processTaskRelationList.isEmpty()) {
            Set<Long> postTaskCodes = processTaskRelationList
                    .stream()
                    .map(ProcessTaskRelation::getPostTaskCode)
                    .collect(Collectors.toSet());
            String postTaskCodesStr = StringUtils.join(postTaskCodes, Constants.COMMA);
            throw new ServiceException(Status.TASK_HAS_DOWNSTREAM, postTaskCodesStr);
        }
    }

    /**
     * Delete resource task definition by code
     *
     * Only task release state offline and no downstream tasks can be deleted, will also remove the exists
     * task relation [upstreamTaskCode, taskCode]
     *
     * @param loginUser login user
     * @param taskCode task code
     */
    @Transactional
    @Override
    public void deleteTaskDefinitionByCode(User loginUser, long taskCode) {
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (taskDefinition == null) {
            throw new ServiceException(Status.TASK_DEFINE_NOT_EXIST, taskCode);
        }

        this.taskCanDeleteValid(loginUser, taskDefinition);
        int delete = taskDefinitionMapper.deleteByCode(taskCode);
        if (delete <= 0) {
            throw new ServiceException(Status.DELETE_TASK_DEFINE_BY_CODE_MSG_ERROR, taskDefinition.getCode());
        }

        // Delete task upstream tasks if exists
        List<ProcessTaskRelation> taskRelationList =
                processTaskRelationMapper.queryUpstreamByCode(taskDefinition.getProjectCode(), taskCode);
        if (!taskRelationList.isEmpty()) {
            logger.debug(
                    "Task definition has upstream tasks, start handle them after delete task, taskDefinitionCode:{}.",
                    taskCode);
            long processDefinitionCode = taskRelationList.get(0).getProcessDefinitionCode();
            List<ProcessTaskRelation> processTaskRelations = processTaskRelationMapper
                    .queryByProcessCode(taskDefinition.getProjectCode(), processDefinitionCode);
            List<ProcessTaskRelation> relationList = processTaskRelations.stream()
                    .filter(r -> r.getPostTaskCode() != taskCode).collect(Collectors.toList());
            updateDag(loginUser, processDefinitionCode, relationList, Lists.newArrayList());
        }
    }

    private void updateDag(User loginUser, long processDefinitionCode,
                           List<ProcessTaskRelation> processTaskRelationList,
                           List<TaskDefinitionLog> taskDefinitionLogs) {
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(processDefinitionCode);
        if (processDefinition == null) {
            logger.error("Process definition does not exist, processDefinitionCode:{}.", processDefinitionCode);
            throw new ServiceException(Status.PROCESS_DEFINE_NOT_EXIST);
        }
        int insertVersion = processService.saveProcessDefine(loginUser, processDefinition, Boolean.TRUE, Boolean.TRUE);
        if (insertVersion <= 0) {
            logger.error("Update process definition error, projectCode:{}, processDefinitionCode:{}.",
                    processDefinition.getProjectCode(), processDefinitionCode);
            throw new ServiceException(Status.UPDATE_PROCESS_DEFINITION_ERROR);
        } else
            logger.info(
                    "Save new version process definition complete, projectCode:{}, processDefinitionCode:{}, newVersion:{}.",
                    processDefinition.getProjectCode(), processDefinitionCode, insertVersion);
        List<ProcessTaskRelationLog> relationLogs =
                processTaskRelationList.stream().map(ProcessTaskRelationLog::new).collect(Collectors.toList());
        int insertResult = processService.saveTaskRelation(loginUser, processDefinition.getProjectCode(),
                processDefinition.getCode(),
                insertVersion, relationLogs, taskDefinitionLogs, Boolean.TRUE);
        if (insertResult == Constants.EXIT_CODE_SUCCESS) {
            logger.info(
                    "Save new version task relations complete, projectCode:{}, processDefinitionCode:{}, newVersion:{}.",
                    processDefinition.getProjectCode(), processDefinitionCode, insertVersion);
        } else {
            logger.error("Update task relations error, projectCode:{}, processDefinitionCode:{}.",
                    processDefinition.getProjectCode(), processDefinitionCode);
            throw new ServiceException(Status.UPDATE_PROCESS_DEFINITION_ERROR);
        }
    }

    /**
     * update task definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskCode task code
     * @param taskDefinitionJsonObj task definition json object
     */
    @Transactional
    @Override
    public Map<String, Object> updateTaskDefinition(User loginUser, long projectCode, long taskCode,
                                                    String taskDefinitionJsonObj) {
        Map<String, Object> result = new HashMap<>();
        TaskDefinitionLog taskDefinitionToUpdate =
                updateTask(loginUser, projectCode, taskCode, taskDefinitionJsonObj, result);
        if (taskDefinitionToUpdate == null) {
            return result;
        }
        List<ProcessTaskRelation> taskRelationList =
                processTaskRelationMapper.queryUpstreamByCode(projectCode, taskCode);
        if (!taskRelationList.isEmpty()) {
            logger.info(
                    "Task definition has upstream tasks, start handle them after update task, taskDefinitionCode:{}.",
                    taskCode);
            long processDefinitionCode = taskRelationList.get(0).getProcessDefinitionCode();
            List<ProcessTaskRelation> processTaskRelations =
                    processTaskRelationMapper.queryByProcessCode(projectCode, processDefinitionCode);
            updateDag(loginUser, processDefinitionCode, processTaskRelations,
                    Lists.newArrayList(taskDefinitionToUpdate));
        }
        logger.info("Update task definition complete, projectCode:{}, taskDefinitionCode:{}.", projectCode, taskCode);
        result.put(Constants.DATA_LIST, taskCode);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private void TaskDefinitionUpdateValid(TaskDefinition taskDefinitionOriginal, TaskDefinition taskDefinitionUpdate) {
        // Task already online
        if (processService.isTaskOnline(taskDefinitionOriginal.getCode())
                && taskDefinitionOriginal.getFlag() == Flag.YES) {
            // if stream, can update task definition without online check
            if (taskDefinitionOriginal.getTaskExecuteType() != TaskExecuteType.STREAM) {
                throw new ServiceException(Status.NOT_SUPPORT_UPDATE_TASK_DEFINITION);
            }
        }

        // not update anything
        if (taskDefinitionOriginal.equals(taskDefinitionUpdate)) {
            throw new ServiceException(Status.TASK_DEFINITION_NOT_CHANGE, taskDefinitionOriginal.getCode());
        }

        // check version invalid
        Integer version = taskDefinitionLogMapper.queryMaxVersionForDefinition(taskDefinitionOriginal.getCode());
        if (version == null || version == 0) {
            throw new ServiceException(Status.DATA_IS_NOT_VALID, taskDefinitionOriginal.getCode());
        }
    }

    /**
     * update task definition
     *
     * @param loginUser login user
     * @param taskCode task code
     * @param taskUpdateRequest task definition json object
     * @return new TaskDefinition have updated
     */
    @Transactional
    @Override
    public TaskDefinition updateTaskDefinitionV2(User loginUser,
                                                 long taskCode,
                                                 TaskUpdateRequest taskUpdateRequest) {
        TaskDefinition taskDefinitionOriginal = taskDefinitionMapper.queryByCode(taskCode);
        if (taskDefinitionOriginal == null) {
            throw new ServiceException(Status.TASK_DEFINITION_NOT_EXISTS, taskCode);
        }

        TaskDefinition taskDefinitionUpdate;
        try {
            taskDefinitionUpdate = taskUpdateRequest.mergeIntoTaskDefinition(taskDefinitionOriginal);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException
                | NoSuchMethodException e) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, taskUpdateRequest.toString());
        }
        this.checkTaskDefinitionValid(loginUser, taskDefinitionUpdate, TASK_DEFINITION_UPDATE);
        this.TaskDefinitionUpdateValid(taskDefinitionOriginal, taskDefinitionUpdate);

        int update = taskDefinitionMapper.updateById(taskDefinitionUpdate);
        if (update <= 0) {
            throw new ServiceException(Status.UPDATE_TASK_DEFINITION_ERROR);
        }
        TaskDefinitionLog taskDefinitionLog = this.persist2TaskDefinitionLog(loginUser, taskDefinitionUpdate);

        List<ProcessTaskRelation> taskRelationList =
                processTaskRelationMapper.queryUpstreamByCode(taskDefinitionUpdate.getProjectCode(), taskCode);
        if (!taskRelationList.isEmpty()) {
            logger.info(
                    "Task definition has upstream tasks, start handle them after update task, taskDefinitionCode:{}.",
                    taskCode);
            long processDefinitionCode = taskRelationList.get(0).getProcessDefinitionCode();
            List<ProcessTaskRelation> processTaskRelations = processTaskRelationMapper
                    .queryByProcessCode(taskDefinitionUpdate.getProjectCode(), processDefinitionCode);
            updateDag(loginUser, processDefinitionCode, processTaskRelations, Lists.newArrayList(taskDefinitionLog));
        }

        this.updateTaskUpstreams(loginUser, taskUpdateRequest.getWorkflowCode(), taskDefinitionUpdate.getCode(),
                taskUpdateRequest.getUpstreamTasksCodes());

        return taskDefinitionUpdate;
    }

    /**
     * Get resource task definition by code
     *
     * @param loginUser login user
     * @param taskCode task code
     * @return TaskDefinition
     */
    @Override
    public TaskDefinition getTaskDefinition(User loginUser,
                                            long taskCode) {
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (taskDefinition == null) {
            throw new ServiceException(Status.TASK_DEFINE_NOT_EXIST, taskCode);
        }
        Project project = projectMapper.queryByCode(taskDefinition.getProjectCode());
        projectService.checkProjectAndAuthThrowException(loginUser, project, TASK_DEFINITION);
        return taskDefinition;
    }

    /**
     * Get resource task definition according to query parameter
     *
     * @param loginUser login user
     * @param taskFilterRequest taskFilterRequest object you want to filter the resource task definitions
     * @return TaskDefinitions of page
     */
    @Override
    public PageInfo<TaskDefinition> filterTaskDefinition(User loginUser,
                                                         TaskFilterRequest taskFilterRequest) {
        TaskDefinition taskDefinition = taskFilterRequest.convert2TaskDefinition();
        if (taskDefinition.getProjectName() != null) {
            Project project = projectMapper.queryByName(taskDefinition.getProjectName());
            // check user access for project
            projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_DEFINITION);
            taskDefinition.setProjectCode(project.getCode());
        }

        Page<TaskDefinition> page =
                new Page<>(taskFilterRequest.getPageNo(), taskFilterRequest.getPageSize());
        IPage<TaskDefinition> taskDefinitionIPage =
                taskDefinitionMapper.filterTaskDefinition(page, taskDefinition);

        PageInfo<TaskDefinition> pageInfo =
                new PageInfo<>(taskFilterRequest.getPageNo(), taskFilterRequest.getPageSize());
        pageInfo.setTotal((int) taskDefinitionIPage.getTotal());
        pageInfo.setTotalList(taskDefinitionIPage.getRecords());
        return pageInfo;
    }

    private TaskDefinitionLog updateTask(User loginUser, long projectCode, long taskCode, String taskDefinitionJsonObj,
                                         Map<String, Object> result) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        result.putAll(projectService.checkProjectAndAuth(loginUser, project, projectCode, TASK_DEFINITION_UPDATE));
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return null;
        }
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (taskDefinition == null) {
            logger.error("Task definition does not exist, taskDefinitionCode:{}.", taskCode);
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, String.valueOf(taskCode));
            return null;
        }
        if (processService.isTaskOnline(taskCode) && taskDefinition.getFlag() == Flag.YES) {
            // if stream, can update task definition without online check
            if (taskDefinition.getTaskExecuteType() != TaskExecuteType.STREAM) {
                logger.warn("Only {} type task can be updated without online check, taskDefinitionCode:{}.",
                        TaskExecuteType.STREAM, taskCode);
                putMsg(result, Status.NOT_SUPPORT_UPDATE_TASK_DEFINITION);
                return null;
            }
        }
        TaskDefinitionLog taskDefinitionToUpdate =
                JSONUtils.parseObject(taskDefinitionJsonObj, TaskDefinitionLog.class);
        if (taskDefinition.equals(taskDefinitionToUpdate)) {
            logger.warn("Task definition does not need update because no change, taskDefinitionCode:{}.", taskCode);
            return null;
        }
        if (taskDefinitionToUpdate == null) {
            logger.warn("Parameter taskDefinitionJson is invalid.");
            putMsg(result, Status.DATA_IS_NOT_VALID, taskDefinitionJsonObj);
            return null;
        }
        if (!taskPluginManager.checkTaskParameters(ParametersNode.builder()
                .taskType(taskDefinitionToUpdate.getTaskType())
                .taskParams(taskDefinitionToUpdate.getTaskParams())
                .dependence(taskDefinitionToUpdate.getDependence())
                .build())) {
            logger.warn("Task definition parameters are invalid, taskDefinitionName:{}.",
                    taskDefinitionToUpdate.getName());
            putMsg(result, Status.PROCESS_NODE_S_PARAMETER_INVALID, taskDefinitionToUpdate.getName());
            return null;
        }
        Integer version = taskDefinitionLogMapper.queryMaxVersionForDefinition(taskCode);
        if (version == null || version == 0) {
            logger.error("Max version task definitionLog can not be found in database, taskDefinitionCode:{}.",
                    taskCode);
            putMsg(result, Status.DATA_IS_NOT_VALID, taskCode);
            return null;
        }
        Date now = new Date();
        taskDefinitionToUpdate.setCode(taskCode);
        taskDefinitionToUpdate.setId(taskDefinition.getId());
        taskDefinitionToUpdate.setProjectCode(projectCode);
        taskDefinitionToUpdate.setUserId(taskDefinition.getUserId());
        taskDefinitionToUpdate.setVersion(++version);
        taskDefinitionToUpdate.setTaskType(taskDefinitionToUpdate.getTaskType().toUpperCase());
        taskDefinitionToUpdate.setResourceIds(processService.getResourceIds(taskDefinitionToUpdate));
        taskDefinitionToUpdate.setUpdateTime(now);
        int update = taskDefinitionMapper.updateById(taskDefinitionToUpdate);
        taskDefinitionToUpdate.setOperator(loginUser.getId());
        taskDefinitionToUpdate.setOperateTime(now);
        taskDefinitionToUpdate.setCreateTime(now);
        taskDefinitionToUpdate.setId(null);
        int insert = taskDefinitionLogMapper.insert(taskDefinitionToUpdate);
        if ((update & insert) != 1) {
            logger.error("Update task definition or definitionLog error, projectCode:{}, taskDefinitionCode:{}.",
                    projectCode, taskCode);
            putMsg(result, Status.UPDATE_TASK_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_TASK_DEFINITION_ERROR);
        } else
            logger.info(
                    "Update task definition and definitionLog complete, projectCode:{}, taskDefinitionCode:{}, newTaskVersion:{}.",
                    projectCode, taskCode, taskDefinitionToUpdate.getVersion());
        return taskDefinitionToUpdate;
    }

    /**
     * update task definition and upstream
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskCode task definition code
     * @param taskDefinitionJsonObj task definition json object
     * @param upstreamCodes upstream task codes, sep comma
     * @return update result code
     */
    @Override
    public Map<String, Object> updateTaskWithUpstream(User loginUser, long projectCode, long taskCode,
                                                      String taskDefinitionJsonObj, String upstreamCodes) {
        Map<String, Object> result = new HashMap<>();
        TaskDefinitionLog taskDefinitionToUpdate =
                updateTask(loginUser, projectCode, taskCode, taskDefinitionJsonObj, result);
        if (result.get(Constants.STATUS) != Status.SUCCESS && taskDefinitionToUpdate == null) {
            return result;
        }
        List<ProcessTaskRelation> upstreamTaskRelations =
                processTaskRelationMapper.queryUpstreamByCode(projectCode, taskCode);
        Set<Long> upstreamCodeSet =
                upstreamTaskRelations.stream().map(ProcessTaskRelation::getPreTaskCode).collect(Collectors.toSet());
        Set<Long> upstreamTaskCodes = Collections.emptySet();
        if (StringUtils.isNotEmpty(upstreamCodes)) {
            upstreamTaskCodes = Arrays.stream(upstreamCodes.split(Constants.COMMA)).map(Long::parseLong)
                    .collect(Collectors.toSet());
        }
        if (CollectionUtils.isEqualCollection(upstreamCodeSet, upstreamTaskCodes) && taskDefinitionToUpdate == null) {
            putMsg(result, Status.SUCCESS);
            return result;
        } else {
            if (taskDefinitionToUpdate == null) {
                taskDefinitionToUpdate = JSONUtils.parseObject(taskDefinitionJsonObj, TaskDefinitionLog.class);
            }
        }
        Map<Long, TaskDefinition> queryUpStreamTaskCodeMap;
        if (!upstreamTaskCodes.isEmpty()) {
            List<TaskDefinition> upstreamTaskDefinitionList = taskDefinitionMapper.queryByCodeList(upstreamTaskCodes);
            queryUpStreamTaskCodeMap = upstreamTaskDefinitionList.stream()
                    .collect(Collectors.toMap(TaskDefinition::getCode, taskDefinition -> taskDefinition));
            // upstreamTaskCodes - queryUpStreamTaskCodeMap.keySet
            upstreamTaskCodes.removeAll(queryUpStreamTaskCodeMap.keySet());
            if (!upstreamTaskCodes.isEmpty()) {
                String notExistTaskCodes = StringUtils.join(upstreamTaskCodes, Constants.COMMA);
                logger.error("Some task definitions in parameter upstreamTaskCodes do not exist, notExistTaskCodes:{}.",
                        notExistTaskCodes);
                putMsg(result, Status.TASK_DEFINE_NOT_EXIST, notExistTaskCodes);
                return result;
            }
        } else {
            queryUpStreamTaskCodeMap = new HashMap<>();
        }
        if (!upstreamTaskRelations.isEmpty()) {
            ProcessTaskRelation taskRelation = upstreamTaskRelations.get(0);
            List<ProcessTaskRelation> processTaskRelations =
                    processTaskRelationMapper.queryByProcessCode(projectCode, taskRelation.getProcessDefinitionCode());
            List<ProcessTaskRelation> processTaskRelationList = Lists.newArrayList(processTaskRelations);
            List<ProcessTaskRelation> relationList = Lists.newArrayList();
            for (ProcessTaskRelation processTaskRelation : processTaskRelationList) {
                if (processTaskRelation.getPostTaskCode() == taskCode) {
                    if (queryUpStreamTaskCodeMap.containsKey(processTaskRelation.getPreTaskCode())
                            && processTaskRelation.getPreTaskCode() != 0L) {
                        queryUpStreamTaskCodeMap.remove(processTaskRelation.getPreTaskCode());
                    } else {
                        processTaskRelation.setPreTaskCode(0L);
                        processTaskRelation.setPreTaskVersion(0);
                        relationList.add(processTaskRelation);
                    }
                }
            }
            processTaskRelationList.removeAll(relationList);
            for (Map.Entry<Long, TaskDefinition> queryUpStreamTask : queryUpStreamTaskCodeMap.entrySet()) {
                taskRelation.setPreTaskCode(queryUpStreamTask.getKey());
                taskRelation.setPreTaskVersion(queryUpStreamTask.getValue().getVersion());
                processTaskRelationList.add(taskRelation);
            }
            if (queryUpStreamTaskCodeMap.isEmpty() && !processTaskRelationList.isEmpty()) {
                processTaskRelationList.add(processTaskRelationList.get(0));
            }
            updateDag(loginUser, taskRelation.getProcessDefinitionCode(), processTaskRelations,
                    Lists.newArrayList(taskDefinitionToUpdate));
        }
        logger.info(
                "Update task with upstream tasks complete, projectCode:{}, taskDefinitionCode:{}, upstreamTaskCodes:{}.",
                projectCode, taskCode, upstreamTaskCodes);
        result.put(Constants.DATA_LIST, taskCode);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * switch task definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskCode task code
     * @param version the version user want to switch
     */
    @Transactional
    @Override
    public Map<String, Object> switchVersion(User loginUser, long projectCode, long taskCode, int version) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_SWITCH_TO_THIS_VERSION);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        if (processService.isTaskOnline(taskCode)) {
            logger.warn(
                    "Task definition version can not be switched due to process definition is {}, taskDefinitionCode:{}.",
                    ReleaseState.ONLINE.getDescp(), taskCode);
            putMsg(result, Status.PROCESS_DEFINE_STATE_ONLINE);
            return result;
        }
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (taskDefinition == null || projectCode != taskDefinition.getProjectCode()) {
            logger.error("Task definition does not exist, taskDefinitionCode:{}.", taskCode);
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, String.valueOf(taskCode));
            return result;
        }
        TaskDefinitionLog taskDefinitionUpdate =
                taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(taskCode, version);
        taskDefinitionUpdate.setUserId(loginUser.getId());
        taskDefinitionUpdate.setUpdateTime(new Date());
        taskDefinitionUpdate.setId(taskDefinition.getId());
        int switchVersion = taskDefinitionMapper.updateById(taskDefinitionUpdate);
        if (switchVersion > 0) {
            List<ProcessTaskRelation> taskRelationList =
                    processTaskRelationMapper.queryUpstreamByCode(projectCode, taskCode);
            if (!taskRelationList.isEmpty()) {
                logger.info(
                        "Task definition has upstream tasks, start handle them after switch task, taskDefinitionCode:{}.",
                        taskCode);
                long processDefinitionCode = taskRelationList.get(0).getProcessDefinitionCode();
                List<ProcessTaskRelation> processTaskRelations =
                        processTaskRelationMapper.queryByProcessCode(projectCode, processDefinitionCode);
                updateDag(loginUser, processDefinitionCode, processTaskRelations,
                        Lists.newArrayList(taskDefinitionUpdate));
            } else {
                logger.info(
                        "Task definition version switch complete, switch task version to {}, taskDefinitionCode:{}.",
                        version, taskCode);
                putMsg(result, Status.SUCCESS);
            }
        } else {
            logger.error("Task definition version switch error, taskDefinitionCode:{}.", taskCode);
            putMsg(result, Status.SWITCH_TASK_DEFINITION_VERSION_ERROR);
        }
        return result;
    }

    @Override
    public Result queryTaskDefinitionVersions(User loginUser,
                                              long projectCode,
                                              long taskCode,
                                              int pageNo,
                                              int pageSize) {
        Result result = new Result();
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> checkResult =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, TASK_VERSION_VIEW);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            putMsg(result, resultStatus);
            return result;
        }
        PageInfo<TaskDefinitionLog> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<TaskDefinitionLog> page = new Page<>(pageNo, pageSize);
        IPage<TaskDefinitionLog> taskDefinitionVersionsPaging =
                taskDefinitionLogMapper.queryTaskDefinitionVersionsPaging(page, taskCode, projectCode);
        List<TaskDefinitionLog> taskDefinitionLogs = taskDefinitionVersionsPaging.getRecords();

        pageInfo.setTotalList(taskDefinitionLogs);
        pageInfo.setTotal((int) taskDefinitionVersionsPaging.getTotal());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Map<String, Object> deleteByCodeAndVersion(User loginUser, long projectCode, long taskCode, int version) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, TASK_DEFINITION_DELETE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);

        if (taskDefinition == null) {
            logger.error("Task definition does not exist, taskDefinitionCode:{}.", taskCode);
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, String.valueOf(taskCode));
        } else {
            if (taskDefinition.getVersion() == version) {
                logger.warn(
                        "Task definition can not be deleted due to version is being used, projectCode:{}, taskDefinitionCode:{}, version:{}.",
                        projectCode, taskCode, version);
                putMsg(result, Status.MAIN_TABLE_USING_VERSION);
                return result;
            }
            int delete = taskDefinitionLogMapper.deleteByCodeAndVersion(taskCode, version);
            if (delete > 0) {
                logger.info(
                        "Task definition version delete complete, projectCode:{}, taskDefinitionCode:{}, version:{}.",
                        projectCode, taskCode, version);
                putMsg(result, Status.SUCCESS);
            } else {
                logger.error("Task definition version delete error, projectCode:{}, taskDefinitionCode:{}, version:{}.",
                        projectCode, taskCode, version);
                putMsg(result, Status.DELETE_TASK_DEFINITION_VERSION_ERROR);
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> queryTaskDefinitionDetail(User loginUser, long projectCode, long taskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, TASK_DEFINITION);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (taskDefinition == null || projectCode != taskDefinition.getProjectCode()) {
            logger.error("Task definition does not exist, taskDefinitionCode:{}.", taskCode);
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, String.valueOf(taskCode));
        } else {
            result.put(Constants.DATA_LIST, taskDefinition);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    @Override
    public Result queryTaskDefinitionListPaging(User loginUser,
                                                long projectCode,
                                                String searchWorkflowName,
                                                String searchTaskName,
                                                String taskType,
                                                TaskExecuteType taskExecuteType,
                                                Integer pageNo,
                                                Integer pageSize) {
        Result result = new Result();
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> checkResult =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, TASK_DEFINITION);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            putMsg(result, resultStatus);
            return result;
        }
        taskType = taskType == null ? StringUtils.EMPTY : taskType;
        Page<TaskMainInfo> page = new Page<>(pageNo, pageSize);
        IPage<TaskMainInfo> taskMainInfoIPage =
                taskDefinitionMapper.queryDefineListPaging(page, projectCode, searchWorkflowName,
                        searchTaskName, taskType, taskExecuteType);
        List<TaskMainInfo> records = taskMainInfoIPage.getRecords();
        if (!records.isEmpty()) {
            Map<Long, TaskMainInfo> taskMainInfoMap = new HashMap<>();
            for (TaskMainInfo info : records) {
                taskMainInfoMap.compute(info.getTaskCode(), (k, v) -> {
                    if (v == null) {
                        Map<Long, String> upstreamTaskMap = new HashMap<>();
                        if (info.getUpstreamTaskCode() != 0) {
                            upstreamTaskMap.put(info.getUpstreamTaskCode(), info.getUpstreamTaskName());
                            info.setUpstreamTaskCode(0L);
                            info.setUpstreamTaskName(StringUtils.EMPTY);
                        }
                        info.setUpstreamTaskMap(upstreamTaskMap);
                        v = info;
                    }
                    if (info.getUpstreamTaskCode() != 0) {
                        v.getUpstreamTaskMap().put(info.getUpstreamTaskCode(), info.getUpstreamTaskName());
                    }
                    return v;
                });
            }
            taskMainInfoIPage.setRecords(Lists.newArrayList(taskMainInfoMap.values()));
        }
        PageInfo<TaskMainInfo> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) taskMainInfoIPage.getTotal());
        pageInfo.setTotalList(taskMainInfoIPage.getRecords());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Map<String, Object> genTaskCodeList(Integer genNum) {
        Map<String, Object> result = new HashMap<>();
        if (genNum == null || genNum < 1 || genNum > 100) {
            logger.warn("Parameter genNum must be great than 1 and less than 100.");
            putMsg(result, Status.DATA_IS_NOT_VALID, genNum);
            return result;
        }
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < genNum; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateException e) {
            logger.error("Generate task definition code error.", e);
            putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS, "Error generating task definition code");
        }
        putMsg(result, Status.SUCCESS);
        // return processDefinitionCode
        result.put(Constants.DATA_LIST, taskCodes);
        return result;
    }

    /**
     * release task definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param code task definition code
     * @param releaseState releaseState
     * @return update result code
     */
    @Transactional
    @Override
    public Map<String, Object> releaseTaskDefinition(User loginUser, long projectCode, long code,
                                                     ReleaseState releaseState) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode, null);
        Status resultStatus = (Status) result.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return result;
        }
        if (null == releaseState) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, RELEASESTATE);
            return result;
        }
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(code);
        if (taskDefinition == null || projectCode != taskDefinition.getProjectCode()) {
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, String.valueOf(code));
            return result;
        }
        TaskDefinitionLog taskDefinitionLog =
                taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(code, taskDefinition.getVersion());
        if (taskDefinitionLog == null) {
            logger.error("Task definition does not exist, taskDefinitionCode:{}.", code);
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, String.valueOf(code));
            return result;
        }
        switch (releaseState) {
            case OFFLINE:
                taskDefinition.setFlag(Flag.NO);
                taskDefinitionLog.setFlag(Flag.NO);
                break;
            case ONLINE:
                String resourceIds = taskDefinition.getResourceIds();
                if (StringUtils.isNotBlank(resourceIds)) {
                    Integer[] resourceIdArray =
                            Arrays.stream(resourceIds.split(",")).map(Integer::parseInt).toArray(Integer[]::new);
                    PermissionCheck<Integer> permissionCheck = new PermissionCheck(AuthorizationType.RESOURCE_FILE_ID,
                            processService, resourceIdArray, loginUser.getId(), logger);
                    try {
                        permissionCheck.checkPermission();
                    } catch (Exception e) {
                        logger.error("Resources permission check error, resourceIds:{}.", resourceIds, e);
                        putMsg(result, Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION);
                        return result;
                    }
                }
                taskDefinition.setFlag(Flag.YES);
                taskDefinitionLog.setFlag(Flag.YES);
                break;
            default:
                logger.warn("Parameter releaseState is invalid.");
                putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, RELEASESTATE);
                return result;
        }
        int update = taskDefinitionMapper.updateById(taskDefinition);
        int updateLog = taskDefinitionLogMapper.updateById(taskDefinitionLog);
        if ((update == 0 && updateLog == 1) || (update == 1 && updateLog == 0)) {
            logger.error("Update taskDefinition state or taskDefinitionLog state error, taskDefinitionCode:{}.", code);
            putMsg(result, Status.UPDATE_TASK_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_TASK_DEFINITION_ERROR);
        }
        logger.error("Update taskDefinition state or taskDefinitionLog state to complete, taskDefinitionCode:{}.",
                code);
        putMsg(result, Status.SUCCESS);
        return result;
    }
}
