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
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_VERSION_VIEW;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.permission.PermissionCheck;
import org.apache.dolphinscheduler.api.permission.TaskDatasourcePermissionChecker;
import org.apache.dolphinscheduler.api.permission.TaskSubWorkflowPermissionChecker;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.TaskDefinitionService;
import org.apache.dolphinscheduler.api.service.WorkflowTaskRelationService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.utils.SensitivePropertyUtils;
import org.apache.dolphinscheduler.api.vo.TaskDefinitionVO;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowTaskRelationDao;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;

@Service
@Slf4j
public class TaskDefinitionServiceImpl extends BaseServiceImpl implements TaskDefinitionService {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    private WorkflowTaskRelationDao workflowTaskRelationDao;

    @Autowired
    private WorkflowTaskRelationService workflowTaskRelationService;

    @Autowired
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Autowired
    private ProcessService processService;

    @Autowired
    private TaskDatasourcePermissionChecker taskDatasourcePermissionChecker;

    @Autowired
    private TaskSubWorkflowPermissionChecker taskSubWorkflowPermissionChecker;

    /**
     * query task definition
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param workflowDefinitionCode workflow definition code
     * @param taskName    task name
     */
    @Override
    public TaskDefinition queryTaskDefinitionByName(User loginUser, long projectCode, long workflowDefinitionCode,
                                                    String taskName) {
        Project project = projectDao.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, TASK_DEFINITION);

        TaskDefinition taskDefinition =
                taskDefinitionDao.queryByName(project.getCode(), workflowDefinitionCode, taskName);
        if (taskDefinition == null) {
            log.error("Task definition does not exist, taskName:{}.", taskName);
            throw new ServiceException(Status.TASK_DEFINE_NOT_EXIST, taskName);
        }
        return SensitivePropertyUtils.maskTaskDefinition(taskDefinition);
    }

    public void updateDag(User loginUser, long workflowDefinitionCode,
                          List<WorkflowTaskRelation> workflowTaskRelationList,
                          List<TaskDefinitionLog> taskDefinitionLogs) {
        WorkflowDefinition workflowDefinition = workflowDefinitionDao.queryByCode(workflowDefinitionCode).orElse(null);
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
        TaskDefinition taskDefinition = taskDefinitionDao.queryByCode(taskCode);
        if (taskDefinition == null) {
            throw new ServiceException(Status.TASK_DEFINE_NOT_EXIST, taskCode);
        }
        Project project = projectDao.queryByCode(taskDefinition.getProjectCode());
        projectService.checkProjectAndAuthThrowException(loginUser, project, TASK_DEFINITION);
        return SensitivePropertyUtils.maskTaskDefinition(taskDefinition);
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
    public void switchVersion(User loginUser, long projectCode, long taskCode, int version) {
        Project project = projectDao.queryByCode(projectCode);
        projectService.checkHasProjectWritePermissionThrowException(loginUser, project);

        if (processService.isTaskOnline(taskCode)) {
            log.warn(
                    "Task definition version can not be switched due to workflow definition is {}, taskDefinitionCode:{}.",
                    ReleaseState.ONLINE.getDescp(), taskCode);
            throw new ServiceException(Status.WORKFLOW_DEFINE_STATE_ONLINE);
        }
        TaskDefinition taskDefinition = taskDefinitionDao.queryByCode(taskCode);
        if (taskDefinition == null || projectCode != taskDefinition.getProjectCode()) {
            log.error("Task definition does not exist, taskDefinitionCode:{}.", taskCode);
            throw new ServiceException(Status.TASK_DEFINE_NOT_EXIST, String.valueOf(taskCode));
        }
        TaskDefinitionLog taskDefinitionUpdate =
                taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(taskCode, version);
        taskDatasourcePermissionChecker.checkPermission(loginUser, Collections.singletonList(taskDefinitionUpdate));
        taskSubWorkflowPermissionChecker.checkPermission(loginUser, Collections.singletonList(taskDefinitionUpdate));
        taskDefinitionUpdate.setUserId(loginUser.getId());
        taskDefinitionUpdate.setUpdateTime(new Date());
        taskDefinitionUpdate.setId(taskDefinition.getId());
        boolean switchSuccess = taskDefinitionDao.updateById(taskDefinitionUpdate);
        if (!switchSuccess) {
            log.error("Task definition version switch error, taskDefinitionCode:{}.", taskCode);
            throw new ServiceException(Status.SWITCH_TASK_DEFINITION_VERSION_ERROR);
        }
        List<WorkflowTaskRelation> taskRelationList =
                workflowTaskRelationDao.queryUpstreamByCode(projectCode, taskCode);
        if (CollectionUtils.isNotEmpty(taskRelationList)) {
            log.info(
                    "Task definition has upstream tasks, start handle them after switch task, taskDefinitionCode:{}.",
                    taskCode);
            long workflowDefinitionCode = taskRelationList.get(0).getWorkflowDefinitionCode();
            List<WorkflowTaskRelation> workflowTaskRelations =
                    workflowTaskRelationDao.queryByWorkflowDefinitionCode(workflowDefinitionCode);
            updateDag(loginUser, workflowDefinitionCode, workflowTaskRelations,
                    Lists.newArrayList(taskDefinitionUpdate));
        } else {
            log.info(
                    "Task definition version switch complete, switch task version to {}, taskDefinitionCode:{}.",
                    version, taskCode);
        }
    }

    @Override
    public Result queryTaskDefinitionVersions(User loginUser,
                                              long projectCode,
                                              long taskCode,
                                              int pageNo,
                                              int pageSize) {
        Result result = new Result();
        Project project = projectDao.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, TASK_VERSION_VIEW);

        PageInfo<TaskDefinitionLog> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<TaskDefinitionLog> page = new Page<>(pageNo, pageSize);
        IPage<TaskDefinitionLog> taskDefinitionVersionsPaging =
                taskDefinitionLogMapper.queryTaskDefinitionVersionsPaging(page, taskCode, projectCode);
        List<TaskDefinitionLog> taskDefinitionLogs = taskDefinitionVersionsPaging.getRecords();
        if (CollectionUtils.isNotEmpty(taskDefinitionLogs)) {
            taskDefinitionLogs = taskDefinitionLogs.stream()
                    .map(SensitivePropertyUtils::copyAndMaskTaskDefinition)
                    .collect(Collectors.toList());
        }

        pageInfo.setTotalList(taskDefinitionLogs);
        pageInfo.setTotal((int) taskDefinitionVersionsPaging.getTotal());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public void deleteByCodeAndVersion(User loginUser, long projectCode, long taskCode, int version) {
        Project project = projectDao.queryByCode(projectCode);
        // check if user have write perm for project
        projectService.checkHasProjectWritePermissionThrowException(loginUser, project);

        TaskDefinition taskDefinition = taskDefinitionDao.queryByCode(taskCode);
        if (taskDefinition == null || projectCode != taskDefinition.getProjectCode()) {
            log.error("Task definition does not exist, taskDefinitionCode:{}.", taskCode);
            throw new ServiceException(Status.TASK_DEFINE_NOT_EXIST, String.valueOf(taskCode));
        }
        if (taskDefinition.getVersion() == version) {
            log.warn(
                    "Task definition can not be deleted due to version is being used, projectCode:{}, taskDefinitionCode:{}, version:{}.",
                    projectCode, taskCode, version);
            throw new ServiceException(Status.MAIN_TABLE_USING_VERSION);
        }
        int delete = taskDefinitionLogMapper.deleteByCodeAndVersion(taskCode, version);
        if (delete <= 0) {
            log.error("Task definition version delete error, projectCode:{}, taskDefinitionCode:{}, version:{}.",
                    projectCode, taskCode, version);
            throw new ServiceException(Status.DELETE_TASK_DEFINITION_VERSION_ERROR);
        }
        log.info(
                "Task definition version delete complete, projectCode:{}, taskDefinitionCode:{}, version:{}.",
                projectCode, taskCode, version);
    }

    @Override
    public TaskDefinitionVO queryTaskDefinitionDetail(User loginUser, long projectCode, long taskCode) {
        Project project = projectDao.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, TASK_DEFINITION);

        TaskDefinition taskDefinition = taskDefinitionDao.queryByCode(taskCode);
        if (taskDefinition == null || projectCode != taskDefinition.getProjectCode()) {
            log.error("Task definition does not exist, taskDefinitionCode:{}.", taskCode);
            throw new ServiceException(Status.TASK_DEFINE_NOT_EXIST, String.valueOf(taskCode));
        }
        List<WorkflowTaskRelation> taskRelationList = workflowTaskRelationDao
                .queryByCode(projectCode, 0, 0, taskCode);
        if (CollectionUtils.isNotEmpty(taskRelationList)) {
            taskRelationList = taskRelationList.stream()
                    .filter(v -> v.getPreTaskCode() != 0).collect(Collectors.toList());
        }
        TaskDefinitionVO taskDefinitionVo =
                TaskDefinitionVO.fromTaskDefinition(SensitivePropertyUtils.maskTaskDefinition(taskDefinition));
        taskDefinitionVo.setWorkflowTaskRelationList(taskRelationList);
        return taskDefinitionVo;
    }

    @Override
    public List<Long> genTaskCodeList(Integer genNum) {
        if (genNum == null || genNum < 1 || genNum > 100) {
            log.warn("Parameter genNum must be great than 1 and less than 100.");
            throw new ServiceException(Status.DATA_IS_NOT_VALID, genNum);
        }
        List<Long> taskCodes = new ArrayList<>();
        for (int i = 0; i < genNum; i++) {
            taskCodes.add(CodeGenerateUtils.genCode());
        }
        return taskCodes;
    }

    /**
     * release task definition
     *
     * @param loginUser    login user
     * @param projectCode  project code
     * @param code         task definition code
     * @param releaseState releaseState
     */
    @Transactional
    @Override
    public void releaseTaskDefinition(User loginUser, long projectCode, long code, ReleaseState releaseState) {
        Project project = projectDao.queryByCode(projectCode);
        projectService.checkHasProjectWritePermissionThrowException(loginUser, project);

        if (null == releaseState) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.RELEASE_STATE);
        }
        TaskDefinition taskDefinition = taskDefinitionDao.queryByCode(code);
        if (taskDefinition == null || projectCode != taskDefinition.getProjectCode()) {
            throw new ServiceException(Status.TASK_DEFINE_NOT_EXIST, String.valueOf(code));
        }
        TaskDefinitionLog taskDefinitionLog =
                taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(code, taskDefinition.getVersion());
        if (taskDefinitionLog == null) {
            log.error("Task definition does not exist, taskDefinitionCode:{}.", code);
            throw new ServiceException(Status.TASK_DEFINE_NOT_EXIST, String.valueOf(code));
        }
        switch (releaseState) {
            case OFFLINE:
                taskDefinition.setFlag(Flag.NO);
                taskDefinitionLog.setFlag(Flag.NO);
                break;
            case ONLINE:
                taskDatasourcePermissionChecker.checkPermission(loginUser,
                        Collections.singletonList(taskDefinitionLog));
                taskSubWorkflowPermissionChecker.checkPermission(loginUser,
                        Collections.singletonList(taskDefinitionLog));
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
                        throw new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION);
                    }
                }
                taskDefinition.setFlag(Flag.YES);
                taskDefinitionLog.setFlag(Flag.YES);
                break;
            default:
                log.warn("Parameter releaseState is invalid.");
                throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.RELEASE_STATE);
        }
        boolean updateSuccess = taskDefinitionDao.updateById(taskDefinition);
        int updateLog = taskDefinitionLogMapper.updateById(taskDefinitionLog);
        if (updateSuccess != (updateLog == 1)) {
            log.error("Update taskDefinition state or taskDefinitionLog state error, taskDefinitionCode:{}.", code);
            throw new ServiceException(Status.UPDATE_TASK_DEFINITION_ERROR);
        }
        log.info("Update taskDefinition state or taskDefinitionLog state to complete, taskDefinitionCode:{}.",
                code);
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
