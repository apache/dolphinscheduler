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
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_DEFINITION_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_VERSION_VIEW;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_DEFINITION;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_SWITCH_TO_THIS_VERSION;
import static org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager.checkTaskParameters;

import org.apache.dolphinscheduler.api.dto.task.TaskFilterRequest;
import org.apache.dolphinscheduler.api.dto.taskRelation.TaskRelationUpdateUpstreamRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowUpdateRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.permission.PermissionCheck;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.TaskDefinitionService;
import org.apache.dolphinscheduler.api.service.WorkflowDefinitionService;
import org.apache.dolphinscheduler.api.service.WorkflowTaskRelationService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.TaskDefinitionVO;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.ConditionType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils.CodeGenerateException;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskMainInfo;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationMapper;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowTaskRelationLogDao;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class TaskDefinitionServiceImpl extends BaseServiceImpl implements TaskDefinitionService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    private WorkflowTaskRelationMapper workflowTaskRelationMapper;

    @Autowired
    private WorkflowTaskRelationLogDao workflowTaskRelationLogDao;

    @Autowired
    private WorkflowTaskRelationService workflowTaskRelationService;

    @Autowired
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Autowired
    private ProcessService processService;

    @Autowired
    private WorkflowDefinitionService workflowDefinitionService;

    @Autowired
    private WorkflowDefinitionLogMapper workflowDefinitionLogMapper;

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

        if (!checkTaskParameters(taskDefinition.getTaskType(), taskDefinition.getTaskParams())) {
            throw new ServiceException(Status.WORKFLOW_NODE_S_PARAMETER_INVALID, taskDefinition.getName());
        }
    }

    private List<WorkflowTaskRelation> updateTaskUpstreams(User user, long workflowCode, long taskCode,
                                                           String upstreamCodes) {
        TaskRelationUpdateUpstreamRequest taskRelationUpdateUpstreamRequest = new TaskRelationUpdateUpstreamRequest();
        taskRelationUpdateUpstreamRequest.setWorkflowCode(workflowCode);
        if (upstreamCodes != null) {
            taskRelationUpdateUpstreamRequest.setUpstreams(upstreamCodes);
        }
        return workflowTaskRelationService.updateUpstreamTaskDefinitionWithSyncDag(user, taskCode, Boolean.FALSE,
                taskRelationUpdateUpstreamRequest);
    }

    private WorkflowDefinition updateWorkflowLocation(User user, WorkflowDefinition workflowDefinition) {
        WorkflowUpdateRequest workflowUpdateRequest = new WorkflowUpdateRequest();
        workflowUpdateRequest.setLocation(null);
        return workflowDefinitionService.updateSingleWorkflowDefinition(user, workflowDefinition.getCode(),
                workflowUpdateRequest);
    }

    /**
     * Create resource task definition
     *
     * @param loginUser         login user
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
            taskDefinitionCode = CodeGenerateUtils.genCode();
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
     * @param loginUser             login user
     * @param projectCode           project code
     * @param processDefinitionCode process definition code
     * @param taskDefinitionJsonObj task definition json object
     * @param upstreamCodes         upstream task codes, sep comma
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
        // check if user have write perm for project
        Map<String, Object> result = new HashMap<>();
        boolean hasProjectAndWritePerm = projectService.hasProjectAndWritePerm(loginUser, project, result);
        if (!hasProjectAndWritePerm) {
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(processDefinitionCode);
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            log.error("Process definition does not exist, processDefinitionCode:{}.", processDefinitionCode);
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(processDefinitionCode));
            return result;
        }
        if (processDefinition.getReleaseState() == ReleaseState.ONLINE) {
            log.warn("Task definition can not be created due to process definition is {}, processDefinitionCode:{}.",
                    ReleaseState.ONLINE.getDescp(), processDefinition.getCode());
            putMsg(result, Status.PROCESS_DEFINE_STATE_ONLINE, String.valueOf(processDefinitionCode));
            return result;
        }
        TaskDefinitionLog taskDefinition = JSONUtils.parseObject(taskDefinitionJsonObj, TaskDefinitionLog.class);
        if (taskDefinition == null) {
            log.warn("Parameter taskDefinitionJsonObj is invalid json.");
            putMsg(result, Status.DATA_IS_NOT_VALID, taskDefinitionJsonObj);
            return result;
        }
        if (!checkTaskParameters(taskDefinition.getTaskType(), taskDefinition.getTaskParams())) {
            putMsg(result, Status.PROCESS_NODE_S_PARAMETER_INVALID, taskDefinition.getName());
            return result;
        }
        long taskCode = taskDefinition.getCode();
        if (taskCode == 0) {
            taskDefinition.setCode(CodeGenerateUtils.genCode());
        }
        List<ProcessTaskRelationLog> processTaskRelationLogList =
                processTaskRelationMapper.queryByProcessCode(processDefinitionCode)
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
            if (CollectionUtils.isNotEmpty(diffCode)) {
                String taskCodes = StringUtils.join(diffCode, Constants.COMMA);
                log.error("Some task definitions with parameter upstreamCodes do not exist, taskDefinitionCodes:{}.",
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
                processDefinition.getVersion()+1,
                processTaskRelationLogList, Lists.newArrayList(), Boolean.TRUE);
        if (insertResult != Constants.EXIT_CODE_SUCCESS) {
            log.error(
                    "Save new version process task relations error, processDefinitionCode:{}, processDefinitionVersion:{}.",
                    processDefinition.getCode(), processDefinition.getVersion());
            putMsg(result, Status.CREATE_PROCESS_TASK_RELATION_ERROR);
            throw new ServiceException(Status.CREATE_PROCESS_TASK_RELATION_ERROR);
        } else
            log.info(
                    "Save new version process task relations complete, processDefinitionCode:{}, processDefinitionVersion:{}.",
                    processDefinition.getCode(), processDefinition.getVersion());

        int saveTaskResult =
                processService.saveTaskDefine(loginUser, projectCode, Lists.newArrayList(taskDefinition), Boolean.TRUE);
        if (saveTaskResult == Constants.DEFINITION_FAILURE) {
            log.error("Save task definition error, projectCode:{}, taskDefinitionCode:{}.", projectCode,
                    taskDefinition.getCode());
            putMsg(result, Status.CREATE_TASK_DEFINITION_ERROR);
            throw new ServiceException(Status.CREATE_TASK_DEFINITION_ERROR);
        } else
            log.info("Save task definition complete, projectCode:{}, taskDefinitionCode:{}.", projectCode,
                    taskDefinition.getCode());
        
        int insertVersion = processService.saveProcessDefine(loginUser, processDefinition, Boolean.TRUE, Boolean.TRUE);
        if (insertVersion == 0) {
            throw new ServiceException(Status.CREATE_PROCESS_DEFINITION_ERROR);
        } else
            log.info("Save process definition complete, processCode:{}, processVersion:{}.",
                    processDefinition.getCode(), insertVersion);
        
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, taskDefinition);
        return result;
    }

    /**
     * query task definition
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param workflowDefinitionCode workflow definition code
     * @param taskName    task name
     */
    @Override
    public Map<String, Object> queryTaskDefinitionByName(User loginUser, long projectCode, long workflowDefinitionCode,
                                                         String taskName) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, TASK_DEFINITION);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        TaskDefinition taskDefinition =
                taskDefinitionMapper.queryByName(project.getCode(), workflowDefinitionCode, taskName);
        if (taskDefinition == null) {
            log.error("Task definition does not exist, taskName:{}.", taskName);
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
    private void taskCanDeleteValid(User user, TaskDefinition taskDefinition, User loginUser) {
        // check user access for project
        Project project = projectMapper.queryByCode(taskDefinition.getProjectCode());
        projectService.checkProjectAndAuthThrowException(user, project, TASK_DEFINITION_DELETE);
        // check if user have write perm for project
        Map<String, Object> result = new HashMap<>();
        boolean hasProjectAndWritePerm = projectService.hasProjectAndWritePerm(loginUser, project, result);
        if (!hasProjectAndWritePerm) {
            throw new ServiceException(Status.TASK_DEFINITION_STATE_ONLINE, taskDefinition.getCode());
        }

        // Whether task relation workflow is online
        if (processService.isTaskOnline(taskDefinition.getCode()) && taskDefinition.getFlag() == Flag.YES) {
            throw new ServiceException(Status.TASK_DEFINITION_STATE_ONLINE, taskDefinition.getCode());
        }

        // Whether task have downstream tasks
        List<WorkflowTaskRelation> workflowTaskRelationList =
                workflowTaskRelationMapper.queryDownstreamByTaskCode(taskDefinition.getCode());
        if (CollectionUtils.isNotEmpty(workflowTaskRelationList)) {
            Set<Long> postTaskCodes = workflowTaskRelationList
                    .stream()
                    .map(WorkflowTaskRelation::getPostTaskCode)
                    .collect(Collectors.toSet());
            String postTaskCodesStr = StringUtils.join(postTaskCodes, Constants.COMMA);
            throw new ServiceException(Status.TASK_HAS_DOWNSTREAM, postTaskCodesStr);
        }
    }

    public void updateDag(User loginUser, long workflowDefinitionCode,
                          List<WorkflowTaskRelation> workflowTaskRelationList,
                          List<TaskDefinitionLog> taskDefinitionLogs) {
        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(workflowDefinitionCode);
        if (workflowDefinition == null) {
            log.error("workflow definition does not exist, workflowDefinitionCode:{}.", workflowDefinitionCode);
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST);
        }
        int insertVersion =
                processService.saveWorkflowDefine(loginUser, workflowDefinition, Boolean.TRUE, Boolean.TRUE);
        if (insertVersion <= 0) {
            log.error("Update workflow definition error, projectCode:{}, workflowDefinitionCode:{}.",
                    workflowDefinition.getProjectCode(), workflowDefinitionCode);
            throw new ServiceException(Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
        } else
            log.info(
                    "Save new version workflow definition complete, projectCode:{}, workflowDefinitionCode:{}, newVersion:{}.",
                    workflowDefinition.getProjectCode(), workflowDefinitionCode, insertVersion);
        List<WorkflowTaskRelationLog> relationLogs =
                workflowTaskRelationList.stream().map(WorkflowTaskRelationLog::new).collect(Collectors.toList());
        int insertResult = processService.saveTaskRelation(loginUser, workflowDefinition.getProjectCode(),
                workflowDefinition.getCode(),
                insertVersion, relationLogs, taskDefinitionLogs, Boolean.TRUE);
        if (insertResult == Constants.EXIT_CODE_SUCCESS) {
            log.info(
                    "Save new version task relations complete, projectCode:{}, workflowDefinitionCode:{}, newVersion:{}.",
                    workflowDefinition.getProjectCode(), workflowDefinitionCode, insertVersion);
        } else {
            log.error("Update task relations error, projectCode:{}, workflowDefinitionCode:{}.",
                    workflowDefinition.getProjectCode(), workflowDefinitionCode);
            throw new ServiceException(Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
        }
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
     * Get resource task definition by code
     *
     * @param loginUser login user
     * @param taskCode  task code
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
     * @param loginUser         login user
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

        // check if user have write perm for project
        boolean hasProjectAndWritePerm = projectService.hasProjectAndWritePerm(loginUser, project, result);
        if (!hasProjectAndWritePerm) {
            return null;
        }

        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (taskDefinition == null) {
            log.error("Task definition does not exist, taskDefinitionCode:{}.", taskCode);
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, String.valueOf(taskCode));
            return null;
        }
        if (processService.isTaskOnline(taskCode) && taskDefinition.getFlag() == Flag.YES) {
            // if stream, can update task definition without online check
            if (taskDefinition.getTaskExecuteType() != TaskExecuteType.STREAM) {
                log.warn("Only {} type task can be updated without online check, taskDefinitionCode:{}.",
                        TaskExecuteType.STREAM, taskCode);
                putMsg(result, Status.NOT_SUPPORT_UPDATE_TASK_DEFINITION);
                return null;
            }
        }
        TaskDefinitionLog taskDefinitionToUpdate =
                JSONUtils.parseObject(taskDefinitionJsonObj, TaskDefinitionLog.class);
        if (TimeoutFlag.CLOSE == taskDefinition.getTimeoutFlag()) {
            taskDefinition.setTimeoutNotifyStrategy(null);
        }
        if (taskDefinition.equals(taskDefinitionToUpdate)) {
            log.warn("Task definition does not need update because no change, taskDefinitionCode:{}.", taskCode);
            putMsg(result, Status.TASK_DEFINITION_NOT_MODIFY_ERROR, String.valueOf(taskCode));
            return null;
        }
        if (taskDefinitionToUpdate == null) {
            log.warn("Parameter taskDefinitionJson is invalid.");
            putMsg(result, Status.DATA_IS_NOT_VALID, taskDefinitionJsonObj);
            return null;
        }
        if (!checkTaskParameters(taskDefinitionToUpdate.getTaskType(), taskDefinitionToUpdate.getTaskParams())) {
            putMsg(result, Status.WORKFLOW_NODE_S_PARAMETER_INVALID, taskDefinitionToUpdate.getName());
            return null;
        }
        Integer version = taskDefinitionLogMapper.queryMaxVersionForDefinition(taskCode);
        if (version == null || version == 0) {
            log.error("Max version task definitionLog can not be found in database, taskDefinitionCode:{}.",
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
            log.error("Update task definition or definitionLog error, projectCode:{}, taskDefinitionCode:{}.",
                    projectCode, taskCode);
            putMsg(result, Status.UPDATE_TASK_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_TASK_DEFINITION_ERROR);
        } else
            log.info(
                    "Update task definition and definitionLog complete, projectCode:{}, taskDefinitionCode:{}, newTaskVersion:{}.",
                    projectCode, taskCode, taskDefinitionToUpdate.getVersion());
        // update workflow task relation
        List<WorkflowTaskRelation> workflowTaskRelations = workflowTaskRelationMapper
                .queryProcessTaskRelationByTaskCodeAndTaskVersion(taskDefinitionToUpdate.getCode(),
                        taskDefinition.getVersion());
        if (CollectionUtils.isNotEmpty(workflowTaskRelations)) {
            Map<Long, List<WorkflowTaskRelation>> workflowTaskRelationGroupList = workflowTaskRelations.stream()
                    .collect(Collectors.groupingBy(WorkflowTaskRelation::getProcessDefinitionCode));
            for (Map.Entry<Long, List<WorkflowTaskRelation>> workflowTaskRelationMap : workflowTaskRelationGroupList
                    .entrySet()) {
                Long workflowDefinitionCode = workflowTaskRelationMap.getKey();
                int workflowDefinitionVersion =
                        workflowDefinitionLogMapper.queryMaxVersionForDefinition(workflowDefinitionCode)
                                + 1;
                List<WorkflowTaskRelation> workflowTaskRelationList = workflowTaskRelationMap.getValue();
                for (WorkflowTaskRelation workflowTaskRelation : workflowTaskRelationList) {
                    if (taskCode == workflowTaskRelation.getPreTaskCode()) {
                        workflowTaskRelation.setPreTaskVersion(version);
                    } else if (taskCode == workflowTaskRelation.getPostTaskCode()) {
                        workflowTaskRelation.setPostTaskVersion(version);
                    }
                    workflowTaskRelation.setProcessDefinitionVersion(workflowDefinitionVersion);
                    int updateWorkflowDefinitionVersionCount =
                            workflowTaskRelationMapper.updateProcessTaskRelationTaskVersion(workflowTaskRelation);
                    if (updateWorkflowDefinitionVersionCount != 1) {
                        log.error("batch update workflow task relation error, projectCode:{}, taskDefinitionCode:{}.",
                                projectCode, taskCode);
                        putMsg(result, Status.WORKFLOW_TASK_RELATION_BATCH_UPDATE_ERROR);
                        throw new ServiceException(Status.WORKFLOW_TASK_RELATION_BATCH_UPDATE_ERROR);
                    }
                    WorkflowTaskRelationLog workflowTaskRelationLog = new WorkflowTaskRelationLog(workflowTaskRelation);
                    workflowTaskRelationLog.setOperator(loginUser.getId());
                    workflowTaskRelationLog.setId(null);
                    workflowTaskRelationLog.setOperateTime(now);
                    int insertWorkflowTaskRelationLogCount = workflowTaskRelationLogDao.insert(workflowTaskRelationLog);
                    if (insertWorkflowTaskRelationLogCount != 1) {
                        log.error("batch update workflow task relation error, projectCode:{}, taskDefinitionCode:{}.",
                                projectCode, taskCode);
                        putMsg(result, Status.CREATE_WORKFLOW_TASK_RELATION_LOG_ERROR);
                        throw new ServiceException(Status.CREATE_WORKFLOW_TASK_RELATION_LOG_ERROR);
                    }
                }
                WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(workflowDefinitionCode);
                workflowDefinition.setVersion(workflowDefinitionVersion);
                workflowDefinition.setUpdateTime(now);
                workflowDefinition.setUserId(loginUser.getId());
                // update workflow definition
                int updateWorkflowDefinitionCount = workflowDefinitionMapper.updateById(workflowDefinition);
                WorkflowDefinitionLog workflowDefinitionLog = new WorkflowDefinitionLog(workflowDefinition);
                workflowDefinitionLog.setOperateTime(now);
                workflowDefinitionLog.setId(null);
                workflowDefinitionLog.setOperator(loginUser.getId());
                int insertWorkflowDefinitionLogCount = workflowDefinitionLogMapper.insert(workflowDefinitionLog);
                if ((updateWorkflowDefinitionCount & insertWorkflowDefinitionLogCount) != 1) {
                    putMsg(result, Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
                    throw new ServiceException(Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
                }
            }
        }
        return taskDefinitionToUpdate;
    }

    /**
     * update task definition and upstream
     *
     * @param loginUser             login user
     * @param projectCode           project code
     * @param taskCode              task definition code
     * @param taskDefinitionJsonObj task definition json object
     * @param upstreamCodes         upstream task codes, sep comma
     * @return update result code
     */
    @Override
    public Map<String, Object> updateTaskWithUpstream(User loginUser, long projectCode, long taskCode,
                                                      String taskDefinitionJsonObj, String upstreamCodes) {
        Map<String, Object> result = new HashMap<>();
        TaskDefinitionLog taskDefinitionToUpdate =
                updateTask(loginUser, projectCode, taskCode, taskDefinitionJsonObj, result);
        List<WorkflowTaskRelation> upstreamTaskRelations =
                workflowTaskRelationMapper.queryUpstreamByCode(projectCode, taskCode);
        Set<Long> upstreamCodeSet =
                upstreamTaskRelations.stream().map(WorkflowTaskRelation::getPreTaskCode).collect(Collectors.toSet());
        Set<Long> upstreamTaskCodes = Collections.emptySet();
        if (StringUtils.isNotEmpty(upstreamCodes)) {
            upstreamTaskCodes = Arrays.stream(upstreamCodes.split(Constants.COMMA)).map(Long::parseLong)
                    .collect(Collectors.toSet());
        }
        if (CollectionUtils.isEqualCollection(upstreamCodeSet, upstreamTaskCodes) && taskDefinitionToUpdate == null) {
            putMsg(result, Status.SUCCESS);
            return result;
        }
        Map<Long, TaskDefinition> queryUpStreamTaskCodeMap;
        if (CollectionUtils.isNotEmpty(upstreamTaskCodes)) {
            List<TaskDefinition> upstreamTaskDefinitionList = taskDefinitionMapper.queryByCodeList(upstreamTaskCodes);
            queryUpStreamTaskCodeMap = upstreamTaskDefinitionList.stream()
                    .collect(Collectors.toMap(TaskDefinition::getCode, taskDefinition -> taskDefinition));
            // upstreamTaskCodes - queryUpStreamTaskCodeMap.keySet
            upstreamTaskCodes.removeAll(queryUpStreamTaskCodeMap.keySet());
            if (CollectionUtils.isNotEmpty(upstreamTaskCodes)) {
                String notExistTaskCodes = StringUtils.join(upstreamTaskCodes, Constants.COMMA);
                log.error("Some task definitions in parameter upstreamTaskCodes do not exist, notExistTaskCodes:{}.",
                        notExistTaskCodes);
                putMsg(result, Status.TASK_DEFINE_NOT_EXIST, notExistTaskCodes);
                return result;
            }
        } else {
            queryUpStreamTaskCodeMap = new HashMap<>();
        }
        if (MapUtils.isNotEmpty(queryUpStreamTaskCodeMap)) {
            WorkflowTaskRelation taskRelation = upstreamTaskRelations.get(0);
            List<WorkflowTaskRelation> workflowTaskRelations =
                    workflowTaskRelationMapper.queryByProcessCode(taskRelation.getProcessDefinitionCode());

            // set upstream code list
            updateUpstreamTask(new HashSet<>(queryUpStreamTaskCodeMap.keySet()),
                    taskCode, projectCode, taskRelation.getProcessDefinitionCode(), loginUser);

            List<WorkflowTaskRelation> workflowTaskRelationList = Lists.newArrayList(workflowTaskRelations);
            List<WorkflowTaskRelation> relationList = Lists.newArrayList();
            for (WorkflowTaskRelation workflowTaskRelation : workflowTaskRelationList) {
                if (workflowTaskRelation.getPostTaskCode() == taskCode) {
                    if (queryUpStreamTaskCodeMap.containsKey(workflowTaskRelation.getPreTaskCode())
                            && workflowTaskRelation.getPreTaskCode() != 0L) {
                        queryUpStreamTaskCodeMap.remove(workflowTaskRelation.getPreTaskCode());
                    } else {
                        workflowTaskRelation.setPreTaskCode(0L);
                        workflowTaskRelation.setPreTaskVersion(0);
                        relationList.add(workflowTaskRelation);
                    }
                }
            }
            workflowTaskRelationList.removeAll(relationList);
            for (Map.Entry<Long, TaskDefinition> queryUpStreamTask : queryUpStreamTaskCodeMap.entrySet()) {
                taskRelation.setPreTaskCode(queryUpStreamTask.getKey());
                taskRelation.setPreTaskVersion(queryUpStreamTask.getValue().getVersion());
                workflowTaskRelationList.add(taskRelation);
            }
            if (MapUtils.isEmpty(queryUpStreamTaskCodeMap) && CollectionUtils.isNotEmpty(workflowTaskRelationList)) {
                workflowTaskRelationList.add(workflowTaskRelationList.get(0));
            }
        }
        log.info(
                "Update task with upstream tasks complete, projectCode:{}, taskDefinitionCode:{}, upstreamTaskCodes:{}.",
                projectCode, taskCode, upstreamTaskCodes);
        result.put(Constants.DATA_LIST, taskCode);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private void updateUpstreamTask(Set<Long> allPreTaskCodeSet, long taskCode, long projectCode,
                                    long workflowDefinitionCode, User loginUser) {
        // query all workflow task relation
        List<WorkflowTaskRelation> hadWorkflowTaskRelationList = workflowTaskRelationMapper
                .queryUpstreamByCode(projectCode, taskCode);
        // remove pre
        Set<Long> removePreTaskSet = new HashSet<>();
        List<WorkflowTaskRelation> removePreTaskList = new ArrayList<>();
        // add pre
        Set<Long> addPreTaskSet = new HashSet<>();
        List<WorkflowTaskRelation> addPreTaskList = new ArrayList<>();

        List<WorkflowTaskRelationLog> workflowTaskRelationLogList = new ArrayList<>();

        // filter all workflow task relation
        if (CollectionUtils.isNotEmpty(hadWorkflowTaskRelationList)) {
            for (WorkflowTaskRelation workflowTaskRelation : hadWorkflowTaskRelationList) {
                if (workflowTaskRelation.getPreTaskCode() == 0) {
                    continue;
                }
                // had
                if (allPreTaskCodeSet.contains(workflowTaskRelation.getPreTaskCode())) {
                    allPreTaskCodeSet.remove(workflowTaskRelation.getPreTaskCode());
                } else {
                    // remove
                    removePreTaskSet.add(workflowTaskRelation.getPreTaskCode());
                    workflowTaskRelation.setPreTaskCode(0);
                    workflowTaskRelation.setPreTaskVersion(0);
                    removePreTaskList.add(workflowTaskRelation);
                    workflowTaskRelationLogList.add(createWorkflowTaskRelationLog(loginUser, workflowTaskRelation));
                }
            }
        }
        // add
        if (allPreTaskCodeSet.size() != 0) {
            addPreTaskSet.addAll(allPreTaskCodeSet);
        }
        // get add task code map
        allPreTaskCodeSet.add(Long.valueOf(taskCode));
        List<TaskDefinition> taskDefinitionList = taskDefinitionMapper.queryByCodeList(allPreTaskCodeSet);
        Map<Long, TaskDefinition> taskCodeMap = taskDefinitionList.stream().collect(Collectors
                .toMap(TaskDefinition::getCode, Function.identity(), (a, b) -> a));

        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(workflowDefinitionCode);
        TaskDefinition taskDefinition = taskCodeMap.get(taskCode);

        for (Long preTaskCode : addPreTaskSet) {
            TaskDefinition preTaskRelation = taskCodeMap.get(preTaskCode);
            WorkflowTaskRelation workflowTaskRelation = new WorkflowTaskRelation(
                    null, workflowDefinition.getVersion(), projectCode, workflowDefinition.getCode(),
                    preTaskRelation.getCode(), preTaskRelation.getVersion(),
                    taskDefinition.getCode(), taskDefinition.getVersion(), ConditionType.NONE, "{}");
            addPreTaskList.add(workflowTaskRelation);
            workflowTaskRelationLogList.add(createWorkflowTaskRelationLog(loginUser, workflowTaskRelation));
        }
        int insert = 0;
        int remove = 0;
        int log = 0;
        // insert workflow task relation table data
        if (CollectionUtils.isNotEmpty(addPreTaskList)) {
            insert = workflowTaskRelationMapper.batchInsert(addPreTaskList);
        }
        if (CollectionUtils.isNotEmpty(removePreTaskList)) {
            for (WorkflowTaskRelation workflowTaskRelation : removePreTaskList) {
                remove += workflowTaskRelationMapper.updateById(workflowTaskRelation);
            }
        }
        if (CollectionUtils.isNotEmpty(workflowTaskRelationLogList)) {
            log = workflowTaskRelationLogDao.batchInsert(workflowTaskRelationLogList);
        }
        if (insert + remove != log) {
            throw new RuntimeException("updateUpstreamTask error");
        }
    }

    private WorkflowTaskRelationLog createWorkflowTaskRelationLog(User loginUser,
                                                                  WorkflowTaskRelation workflowTaskRelation) {
        Date now = new Date();
        WorkflowTaskRelationLog workflowTaskRelationLog = new WorkflowTaskRelationLog(workflowTaskRelation);
        workflowTaskRelationLog.setOperator(loginUser.getId());
        workflowTaskRelationLog.setOperateTime(now);
        workflowTaskRelationLog.setCreateTime(now);
        workflowTaskRelationLog.setUpdateTime(now);
        return workflowTaskRelationLog;
    }

    /**
     * switch task definition
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param taskCode    task code
     * @param version     the version user want to switch
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
            log.warn(
                    "Task definition version can not be switched due to workflow definition is {}, taskDefinitionCode:{}.",
                    ReleaseState.ONLINE.getDescp(), taskCode);
            putMsg(result, Status.WORKFLOW_DEFINE_STATE_ONLINE);
            return result;
        }
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (taskDefinition == null || projectCode != taskDefinition.getProjectCode()) {
            log.error("Task definition does not exist, taskDefinitionCode:{}.", taskCode);
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
            List<WorkflowTaskRelation> taskRelationList =
                    workflowTaskRelationMapper.queryUpstreamByCode(projectCode, taskCode);
            if (CollectionUtils.isNotEmpty(taskRelationList)) {
                log.info(
                        "Task definition has upstream tasks, start handle them after switch task, taskDefinitionCode:{}.",
                        taskCode);
                long workflowDefinitionCode = taskRelationList.get(0).getProcessDefinitionCode();
                List<WorkflowTaskRelation> workflowTaskRelations =
                        workflowTaskRelationMapper.queryByProcessCode(workflowDefinitionCode);
                updateDag(loginUser, workflowDefinitionCode, workflowTaskRelations,
                        Lists.newArrayList(taskDefinitionUpdate));
            } else {
                log.info(
                        "Task definition version switch complete, switch task version to {}, taskDefinitionCode:{}.",
                        version, taskCode);
                putMsg(result, Status.SUCCESS);
            }
        } else {
            log.error("Task definition version switch error, taskDefinitionCode:{}.", taskCode);
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
        // check if user have write perm for project
        Map<String, Object> result = new HashMap<>();
        boolean hasProjectAndWritePerm = projectService.hasProjectAndWritePerm(loginUser, project, result);
        if (!hasProjectAndWritePerm) {
            return result;
        }
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);

        if (taskDefinition == null) {
            log.error("Task definition does not exist, taskDefinitionCode:{}.", taskCode);
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, String.valueOf(taskCode));
        } else {
            if (taskDefinition.getVersion() == version) {
                log.warn(
                        "Task definition can not be deleted due to version is being used, projectCode:{}, taskDefinitionCode:{}, version:{}.",
                        projectCode, taskCode, version);
                putMsg(result, Status.MAIN_TABLE_USING_VERSION);
                return result;
            }
            int delete = taskDefinitionLogMapper.deleteByCodeAndVersion(taskCode, version);
            if (delete > 0) {
                log.info(
                        "Task definition version delete complete, projectCode:{}, taskDefinitionCode:{}, version:{}.",
                        projectCode, taskCode, version);
                putMsg(result, Status.SUCCESS);
            } else {
                log.error("Task definition version delete error, projectCode:{}, taskDefinitionCode:{}, version:{}.",
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
            log.error("Task definition does not exist, taskDefinitionCode:{}.", taskCode);
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, String.valueOf(taskCode));
        } else {
            List<WorkflowTaskRelation> taskRelationList = workflowTaskRelationMapper
                    .queryByCode(projectCode, 0, 0, taskCode);
            if (CollectionUtils.isNotEmpty(taskRelationList)) {
                taskRelationList = taskRelationList.stream()
                        .filter(v -> v.getPreTaskCode() != 0).collect(Collectors.toList());
            }
            TaskDefinitionVO taskDefinitionVo = TaskDefinitionVO.fromTaskDefinition(taskDefinition);
            taskDefinitionVo.setWorkflowTaskRelationList(taskRelationList);
            result.put(Constants.DATA_LIST, taskDefinitionVo);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    private void fillRecords(long projectCode, IPage<TaskMainInfo> taskMainInfoIPage) {
        List<TaskMainInfo> records = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(taskMainInfoIPage.getRecords())) {
            // query task relevant info by task code
            records = taskDefinitionMapper.queryDefineListByCodeList(projectCode,
                    taskMainInfoIPage.getRecords().stream().map(TaskMainInfo::getTaskCode)
                            .collect(Collectors.toList()));
        }
        // because first step, so need init records
        taskMainInfoIPage.setRecords(Collections.emptyList());
        if (CollectionUtils.isNotEmpty(records)) {
            // task code and task info map
            Map<Long, TaskMainInfo> taskMainInfoMap = new HashMap<>();
            // construct task code and relevant upstream task list map
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

            // because taskMainInfoMap's value is TaskMainInfo,
            // TaskMainInfo have task code info, so only need gain taskMainInfoMap's values
            List<TaskMainInfo> resultRecords = Lists.newArrayList(taskMainInfoMap.values());
            resultRecords.sort((o1, o2) -> o2.getTaskUpdateTime().compareTo(o1.getTaskUpdateTime()));
            taskMainInfoIPage.setRecords(resultRecords);
        }
    }

    private void fillWorkflowInfo(long projectCode, IPage<TaskMainInfo> taskMainInfoIPage) {

    }

    @Override
    public Map<String, Object> genTaskCodeList(Integer genNum) {
        Map<String, Object> result = new HashMap<>();
        if (genNum == null || genNum < 1 || genNum > 100) {
            log.warn("Parameter genNum must be great than 1 and less than 100.");
            putMsg(result, Status.DATA_IS_NOT_VALID, genNum);
            return result;
        }
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < genNum; i++) {
                taskCodes.add(CodeGenerateUtils.genCode());
            }
        } catch (CodeGenerateException e) {
            log.error("Generate task definition code error.", e);
            putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS, "Error generating task definition code");
        }
        putMsg(result, Status.SUCCESS);
        // return workflowDefinitionCode
        result.put(Constants.DATA_LIST, taskCodes);
        return result;
    }

    /**
     * release task definition
     *
     * @param loginUser    login user
     * @param projectCode  project code
     * @param code         task definition code
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
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.RELEASE_STATE);
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
            log.error("Task definition does not exist, taskDefinitionCode:{}.", code);
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
                            processService, resourceIdArray, loginUser.getId(), log);
                    try {
                        permissionCheck.checkPermission();
                    } catch (Exception e) {
                        log.error("Resources permission check error, resourceIds:{}.", resourceIds, e);
                        putMsg(result, Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION);
                        return result;
                    }
                }
                taskDefinition.setFlag(Flag.YES);
                taskDefinitionLog.setFlag(Flag.YES);
                break;
            default:
                log.warn("Parameter releaseState is invalid.");
                putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.RELEASE_STATE);
                return result;
        }
        int update = taskDefinitionMapper.updateById(taskDefinition);
        int updateLog = taskDefinitionLogMapper.updateById(taskDefinitionLog);
        if ((update == 0 && updateLog == 1) || (update == 1 && updateLog == 0)) {
            log.error("Update taskDefinition state or taskDefinitionLog state error, taskDefinitionCode:{}.", code);
            putMsg(result, Status.UPDATE_TASK_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_TASK_DEFINITION_ERROR);
        }
        log.error("Update taskDefinition state or taskDefinitionLog state to complete, taskDefinitionCode:{}.",
                code);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public void deleteTaskByWorkflowDefinitionCode(long workflowDefinitionCode, int workflowDefinitionVersion) {
        List<WorkflowTaskRelation> workflowTaskRelations = workflowTaskRelationService
                .queryByWorkflowDefinitionCode(workflowDefinitionCode, workflowDefinitionVersion);
        if (CollectionUtils.isEmpty(workflowTaskRelations)) {
            return;
        }
        // delete task definition
        Set<Long> needToDeleteTaskDefinitionCodes = new HashSet<>();
        for (WorkflowTaskRelation workflowTaskRelation : workflowTaskRelations) {
            needToDeleteTaskDefinitionCodes.add(workflowTaskRelation.getPreTaskCode());
            needToDeleteTaskDefinitionCodes.add(workflowTaskRelation.getPostTaskCode());
        }
        taskDefinitionDao.deleteByTaskDefinitionCodes(needToDeleteTaskDefinitionCodes);
        // delete task workflow relation
        workflowTaskRelationService.deleteByWorkflowDefinitionCode(workflowDefinitionCode, workflowDefinitionVersion);
    }
}
