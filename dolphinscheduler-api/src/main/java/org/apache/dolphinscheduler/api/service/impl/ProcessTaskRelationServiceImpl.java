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

import static java.util.stream.Collectors.toSet;

import org.apache.dolphinscheduler.api.dto.taskRelation.TaskRelationCreateRequest;
import org.apache.dolphinscheduler.api.dto.taskRelation.TaskRelationFilterRequest;
import org.apache.dolphinscheduler.api.dto.taskRelation.TaskRelationUpdateUpstreamRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProcessTaskRelationService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ConditionType;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;

/**
 * process task relation service impl
 */
@Service
@Slf4j
public class ProcessTaskRelationServiceImpl extends BaseServiceImpl implements ProcessTaskRelationService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private ProcessDefinitionLogMapper processDefinitionLogMapper;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ProcessTaskRelationLogMapper processTaskRelationLogMapper;

    /**
     * create process task relation
     *
     * @param loginUser             login user
     * @param projectCode           project code
     * @param processDefinitionCode processDefinitionCode
     * @param preTaskCode           preTaskCode
     * @param postTaskCode          postTaskCode
     * @return create result code
     */
    @Transactional
    @Override
    public Map<String, Object> createProcessTaskRelation(User loginUser, long projectCode, long processDefinitionCode,
                                                         long preTaskCode, long postTaskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode, null);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        WorkflowDefinition workflowDefinition = processDefinitionMapper.queryByCode(processDefinitionCode);
        if (workflowDefinition == null) {
            log.error("Process definition does not exist, processCode:{}.", processDefinitionCode);
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(processDefinitionCode));
            return result;
        }
        if (workflowDefinition.getProjectCode() != projectCode) {
            log.error("Process definition's project does not match project {}.", projectCode);
            putMsg(result, Status.PROJECT_PROCESS_NOT_MATCH);
            return result;
        }
        updateProcessDefiniteVersion(loginUser, result, workflowDefinition);
        List<WorkflowTaskRelation> workflowTaskRelationList =
                processTaskRelationMapper.queryByProcessCode(processDefinitionCode);
        List<WorkflowTaskRelation> workflowTaskRelations = Lists.newArrayList(workflowTaskRelationList);
        if (!workflowTaskRelations.isEmpty()) {
            Map<Long, WorkflowTaskRelation> preTaskCodeMap =
                    workflowTaskRelations.stream().filter(r -> r.getPostTaskCode() == postTaskCode)
                            .collect(Collectors.toMap(WorkflowTaskRelation::getPreTaskCode,
                                    processTaskRelation -> processTaskRelation));
            if (!preTaskCodeMap.isEmpty()) {
                if (preTaskCodeMap.containsKey(preTaskCode) || (!preTaskCodeMap.containsKey(0L) && preTaskCode == 0L)) {
                    putMsg(result, Status.PROCESS_TASK_RELATION_EXIST, String.valueOf(processDefinitionCode));
                    return result;
                }
                if (preTaskCodeMap.containsKey(0L) && preTaskCode != 0L) {
                    // delete no upstream
                    workflowTaskRelations.remove(preTaskCodeMap.get(0L));
                }
            }
        }
        TaskDefinition postTaskDefinition = taskDefinitionMapper.queryByCode(postTaskCode);
        WorkflowTaskRelation workflowTaskRelation = setRelation(workflowDefinition, postTaskDefinition);
        if (preTaskCode != 0L) {
            TaskDefinition preTaskDefinition = taskDefinitionMapper.queryByCode(preTaskCode);
            List<WorkflowTaskRelation> upstreamTaskRelationList = workflowTaskRelations.stream()
                    .filter(r -> r.getPostTaskCode() == preTaskCode).collect(Collectors.toList());
            // upstream is or not exist
            if (upstreamTaskRelationList.isEmpty()) {
                WorkflowTaskRelation preWorkflowTaskRelation = setRelation(workflowDefinition, preTaskDefinition);
                preWorkflowTaskRelation.setPreTaskCode(0L);
                preWorkflowTaskRelation.setPreTaskVersion(0);
                workflowTaskRelations.add(preWorkflowTaskRelation);
            }
            workflowTaskRelation.setPreTaskCode(preTaskDefinition.getCode());
            workflowTaskRelation.setPreTaskVersion(preTaskDefinition.getVersion());
        } else {
            workflowTaskRelation.setPreTaskCode(0L);
            workflowTaskRelation.setPreTaskVersion(0);
        }
        workflowTaskRelations.add(workflowTaskRelation);
        updateRelation(loginUser, result, workflowDefinition, workflowTaskRelations);
        return result;
    }

    private WorkflowTaskRelationLog persist2ProcessTaskRelationLog(User user, WorkflowTaskRelation workflowTaskRelation) {
        WorkflowTaskRelationLog processTaskRelationLog = new WorkflowTaskRelationLog(workflowTaskRelation);
        processTaskRelationLog.setOperator(user.getId());
        processTaskRelationLog.setOperateTime(new Date());
        int result = processTaskRelationLogMapper.insert(processTaskRelationLog);
        if (result <= 0) {
            throw new ServiceException(Status.CREATE_PROCESS_TASK_RELATION_LOG_ERROR,
                    processTaskRelationLog.getPreTaskCode(), processTaskRelationLog.getPostTaskCode());
        }
        return processTaskRelationLog;
    }

    private List<WorkflowTaskRelationLog> batchPersist2ProcessTaskRelationLog(User user,
                                                                              List<WorkflowTaskRelation> workflowTaskRelations) {
        Date now = new Date();
        List<WorkflowTaskRelationLog> processTaskRelationLogs = new ArrayList<>();

        for (WorkflowTaskRelation workflowTaskRelation : workflowTaskRelations) {
            WorkflowTaskRelationLog processTaskRelationLog = new WorkflowTaskRelationLog(workflowTaskRelation);
            processTaskRelationLog.setOperator(user.getId());
            processTaskRelationLog.setOperateTime(now);
            processTaskRelationLogs.add(processTaskRelationLog);
        }
        int result = processTaskRelationLogMapper.batchInsert(processTaskRelationLogs);
        if (result != processTaskRelationLogs.size()) {
            throw new ServiceException(Status.CREATE_PROCESS_TASK_RELATION_LOG_ERROR);
        }
        return processTaskRelationLogs;
    }

    private void updateVersions(WorkflowTaskRelation workflowTaskRelation) {
        // workflow
        WorkflowDefinition workflowDefinition =
                processDefinitionMapper.queryByCode(workflowTaskRelation.getProcessDefinitionCode());
        workflowTaskRelation.setProcessDefinitionVersion(workflowDefinition.getVersion());

        // tasks
        TaskDefinition preTaskDefinition = taskDefinitionMapper.queryByCode(workflowTaskRelation.getPreTaskCode());
        workflowTaskRelation.setPreTaskVersion(preTaskDefinition.getVersion());
        TaskDefinition postTaskDefinition = taskDefinitionMapper.queryByCode(workflowTaskRelation.getPostTaskCode());
        workflowTaskRelation.setPostTaskVersion(postTaskDefinition.getVersion());
    }

    /**
     * create resource process task relation
     *
     * @param loginUser login user
     * @param taskRelationCreateRequest project code
     * @return ProcessTaskRelation object
     */
    @Override
    @Transactional
    public WorkflowTaskRelation createProcessTaskRelationV2(User loginUser,
                                                            TaskRelationCreateRequest taskRelationCreateRequest) {
        WorkflowTaskRelation workflowTaskRelation = taskRelationCreateRequest.convert2ProcessTaskRelation();
        WorkflowDefinition workflowDefinition =
                processDefinitionMapper.queryByCode(workflowTaskRelation.getProcessDefinitionCode());
        if (workflowDefinition == null) {
            throw new ServiceException(Status.PROCESS_DEFINE_NOT_EXIST,
                    String.valueOf(workflowTaskRelation.getProcessDefinitionCode()));
        }
        if (workflowTaskRelation.getProjectCode() == 0) {
            workflowTaskRelation.setProjectCode(workflowDefinition.getProjectCode());
        }
        Project project = projectMapper.queryByCode(workflowTaskRelation.getProjectCode());
        projectService.checkProjectAndAuthThrowException(loginUser, project, null);

        // persistence process task relation and process task relation log to database
        this.updateVersions(workflowTaskRelation);
        int insert = processTaskRelationMapper.insert(workflowTaskRelation);
        if (insert <= 0) {
            throw new ServiceException(Status.CREATE_PROCESS_TASK_RELATION_ERROR, workflowTaskRelation.getPreTaskCode(),
                    workflowTaskRelation.getPostTaskCode());
        }
        this.persist2ProcessTaskRelationLog(loginUser, workflowTaskRelation);

        return workflowTaskRelation;
    }

    private WorkflowTaskRelation setRelation(WorkflowDefinition workflowDefinition, TaskDefinition taskDefinition) {
        Date now = new Date();
        WorkflowTaskRelation workflowTaskRelation = new WorkflowTaskRelation();
        workflowTaskRelation.setProjectCode(workflowDefinition.getProjectCode());
        workflowTaskRelation.setProcessDefinitionCode(workflowDefinition.getCode());
        workflowTaskRelation.setProcessDefinitionVersion(workflowDefinition.getVersion());
        workflowTaskRelation.setPostTaskCode(taskDefinition.getCode());
        workflowTaskRelation.setPostTaskVersion(taskDefinition.getVersion());
        workflowTaskRelation.setConditionType(ConditionType.NONE);
        workflowTaskRelation.setConditionParams("{}");
        workflowTaskRelation.setCreateTime(now);
        workflowTaskRelation.setUpdateTime(now);
        return workflowTaskRelation;
    }

    private void updateProcessDefiniteVersion(User loginUser, Map<String, Object> result,
                                              WorkflowDefinition workflowDefinition) {
        int insertVersion = processService.saveProcessDefine(loginUser, workflowDefinition, Boolean.TRUE, Boolean.TRUE);
        if (insertVersion <= 0) {
            log.error("Update process definition error, projectCode:{}, processDefinitionCode:{}.",
                    workflowDefinition.getProjectCode(), workflowDefinition.getCode());
            putMsg(result, Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
        } else
            log.info(
                    "Update process definition complete, new version is {}, projectCode:{}, processDefinitionCode:{}.",
                    insertVersion, workflowDefinition.getProjectCode(), workflowDefinition.getCode());
        workflowDefinition.setVersion(insertVersion);
    }

    /**
     * delete process task relation
     *
     * @param loginUser             login user
     * @param projectCode           project code
     * @param processDefinitionCode process definition code
     * @param taskCode              the post task code
     * @return delete result code
     */
    @Transactional
    @Override
    public Map<String, Object> deleteTaskProcessRelation(User loginUser, long projectCode, long processDefinitionCode,
                                                         long taskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode, null);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        if (taskCode == 0) {
            log.error(
                    "Delete task process relation error due to parameter taskCode is 0, projectCode:{}, processDefinitionCode:{}.",
                    projectCode, processDefinitionCode);
            putMsg(result, Status.DELETE_TASK_PROCESS_RELATION_ERROR);
            return result;
        }
        WorkflowDefinition workflowDefinition = processDefinitionMapper.queryByCode(processDefinitionCode);
        if (workflowDefinition == null) {
            log.error("Process definition does not exist, processDefinitionCode:{}.", processDefinitionCode);
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(processDefinitionCode));
            return result;
        }
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (null == taskDefinition) {
            log.error("Task definition does not exist, taskDefinitionCode:{}.", taskCode);
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, String.valueOf(taskCode));
            return result;
        }
        List<WorkflowTaskRelation> workflowTaskRelations =
                processTaskRelationMapper.queryByProcessCode(processDefinitionCode);
        List<WorkflowTaskRelation> workflowTaskRelationList = Lists.newArrayList(workflowTaskRelations);
        if (CollectionUtils.isEmpty(workflowTaskRelationList)) {
            log.error("Process task relations are empty, projectCode:{}, processDefinitionCode:{}.", projectCode,
                    processDefinitionCode);
            putMsg(result, Status.DATA_IS_NULL, "processTaskRelationList");
            return result;
        }
        List<Long> downstreamList = Lists.newArrayList();
        for (WorkflowTaskRelation workflowTaskRelation : workflowTaskRelations) {
            if (workflowTaskRelation.getPreTaskCode() == taskCode) {
                downstreamList.add(workflowTaskRelation.getPostTaskCode());
            }
            if (workflowTaskRelation.getPostTaskCode() == taskCode) {
                workflowTaskRelationList.remove(workflowTaskRelation);
            }
        }
        if (CollectionUtils.isNotEmpty(downstreamList)) {
            String downstream = StringUtils.join(downstreamList, ",");
            log.warn(
                    "Relation can not be deleted because task has downstream tasks:[{}], projectCode:{}, processDefinitionCode:{}, taskDefinitionCode:{}.",
                    downstream, projectCode, processDefinitionCode, taskCode);
            putMsg(result, Status.TASK_HAS_DOWNSTREAM, downstream);
            return result;
        }
        updateProcessDefiniteVersion(loginUser, result, workflowDefinition);
        updateRelation(loginUser, result, workflowDefinition, workflowTaskRelationList);
        if (TaskTypeUtils.isConditionTask(taskDefinition.getTaskType())
                || TaskTypeUtils.isSubWorkflowTask(taskDefinition.getTaskType())
                || TaskTypeUtils.isDependentTask(taskDefinition.getTaskType())) {
            int deleteTaskDefinition = taskDefinitionMapper.deleteByCode(taskCode);
            if (0 == deleteTaskDefinition) {
                log.error("Delete task definition error, taskDefinitionCode:{}.", taskCode);
                putMsg(result, Status.DELETE_TASK_DEFINE_BY_CODE_ERROR);
                throw new ServiceException(Status.DELETE_TASK_DEFINE_BY_CODE_ERROR);
            } else
                log.info("Delete {} type task definition complete, taskDefinitionCode:{}.",
                        taskDefinition.getTaskType(), taskCode);
        }
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * delete process task relation, will delete exists relation preTaskCode -> postTaskCode, throw error if not exists
     *
     * @param loginUser login user
     * @param preTaskCode relation upstream code
     * @param postTaskCode relation downstream code
     */
    @Override
    @Transactional
    public void deleteTaskProcessRelationV2(User loginUser,
                                            long preTaskCode,
                                            long postTaskCode) {
        WorkflowTaskRelation workflowTaskRelation =
                new TaskRelationFilterRequest(preTaskCode, postTaskCode).convert2TaskDefinition();

        Page<WorkflowTaskRelation> page =
                new Page<>(new TaskRelationFilterRequest(preTaskCode, postTaskCode).getPageNo(),
                        new TaskRelationFilterRequest(preTaskCode, postTaskCode).getPageSize());
        IPage<WorkflowTaskRelation> processTaskRelationIPage =
                processTaskRelationMapper.filterProcessTaskRelation(page, workflowTaskRelation);

        List<WorkflowTaskRelation> workflowTaskRelations = processTaskRelationIPage.getRecords();
        if (workflowTaskRelations.size() != 1) {
            throw new ServiceException(Status.PROCESS_TASK_RELATION_NOT_EXPECT, 1, workflowTaskRelations.size());
        }

        WorkflowTaskRelation workflowTaskRelationDb = workflowTaskRelations.get(0);
        Project project = projectMapper.queryByCode(workflowTaskRelationDb.getProjectCode());
        projectService.checkProjectAndAuthThrowException(loginUser, project, null);
        processTaskRelationMapper.deleteById(workflowTaskRelationDb.getId());
    }

    /**
     * delete process task relation, will delete exists relation upstream -> downstream, throw error if not exists
     *
     * @param loginUser login user
     * @param taskCode relation upstream code
     * @param needSyncDag needSyncDag
     * @param taskRelationUpdateUpstreamRequest relation downstream code
     */
    @Override
    @Transactional
    public List<WorkflowTaskRelation> updateUpstreamTaskDefinitionWithSyncDag(User loginUser,
                                                                              long taskCode,
                                                                              Boolean needSyncDag,
                                                                              TaskRelationUpdateUpstreamRequest taskRelationUpdateUpstreamRequest) {
        TaskDefinition downstreamTask = taskDefinitionMapper.queryByCode(taskCode);
        if (downstreamTask == null) {
            throw new ServiceException(Status.TASK_DEFINE_NOT_EXIST, taskCode);
        }
        List<Long> upstreamTaskCodes = taskRelationUpdateUpstreamRequest.getUpstreams();

        WorkflowTaskRelation workflowTaskRelation = new WorkflowTaskRelation();
        workflowTaskRelation.setPostTaskCode(taskCode);

        Page<WorkflowTaskRelation> page = new Page<>(taskRelationUpdateUpstreamRequest.getPageNo(),
                taskRelationUpdateUpstreamRequest.getPageSize());
        IPage<WorkflowTaskRelation> processTaskRelationExistsIPage =
                processTaskRelationMapper.filterProcessTaskRelation(page, workflowTaskRelation);
        List<WorkflowTaskRelation> workflowTaskRelationExists = processTaskRelationExistsIPage.getRecords();

        WorkflowDefinition workflowDefinition = null;
        if (CollectionUtils.isNotEmpty(workflowTaskRelationExists)) {
            workflowDefinition =
                    processDefinitionMapper.queryByCode(workflowTaskRelationExists.get(0).getProcessDefinitionCode());
        } else if (taskRelationUpdateUpstreamRequest.getWorkflowCode() != 0L) {
            workflowDefinition =
                    processDefinitionMapper.queryByCode(taskRelationUpdateUpstreamRequest.getWorkflowCode());
        }
        if (workflowDefinition == null) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                    taskRelationUpdateUpstreamRequest.toString());
        }
        workflowDefinition.setUpdateTime(new Date());
        int insertVersion = workflowDefinition.getVersion();
        if (needSyncDag) {
            insertVersion =
                    this.saveProcessDefine(loginUser, workflowDefinition);
            if (insertVersion <= 0) {
                throw new ServiceException(Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
            }
        }
        // get new relation to create and out of date relation to delete
        List<Long> taskCodeCreates = upstreamTaskCodes
                .stream()
                .filter(upstreamTaskCode -> workflowTaskRelationExists.stream().noneMatch(
                        processTaskRelationExist -> processTaskRelationExist.getPreTaskCode() == upstreamTaskCode))
                .collect(Collectors.toList());
        List<Integer> taskCodeDeletes = workflowTaskRelationExists.stream()
                .filter(ptr -> !upstreamTaskCodes.contains(ptr.getPreTaskCode()))
                .map(WorkflowTaskRelation::getId)
                .collect(Collectors.toList());

        // delete relation not exists
        if (CollectionUtils.isNotEmpty(taskCodeDeletes)) {
            int delete = processTaskRelationMapper.deleteBatchIds(taskCodeDeletes);
            if (delete != taskCodeDeletes.size()) {
                throw new ServiceException(Status.PROCESS_TASK_RELATION_BATCH_DELETE_ERROR, taskCodeDeletes);
            }
        }

        // create relation not exists
        List<WorkflowTaskRelation> workflowTaskRelations = new ArrayList<>();
        for (long createCode : taskCodeCreates) {
            long upstreamCode = 0L;
            int version = 0;
            if (createCode != 0L) {
                // 0 for DAG root, should not, it may already exists and skip to create anymore
                TaskDefinition upstreamTask = taskDefinitionMapper.queryByCode(createCode);
                if (upstreamTask == null) {
                    throw new ServiceException(Status.TASK_DEFINE_NOT_EXIST, createCode);
                }
                upstreamCode = upstreamTask.getCode();
                version = upstreamTask.getVersion();
            }
            WorkflowTaskRelation workflowTaskRelationCreate =
                    new WorkflowTaskRelation(null, workflowDefinition.getVersion(), downstreamTask.getProjectCode(),
                            workflowDefinition.getCode(), upstreamCode, version,
                            downstreamTask.getCode(), downstreamTask.getVersion(), null, null);
            workflowTaskRelations.add(workflowTaskRelationCreate);
        }
        int batchInsert = processTaskRelationMapper.batchInsert(workflowTaskRelations);
        if (batchInsert != workflowTaskRelations.size()) {
            throw new ServiceException(Status.PROCESS_TASK_RELATION_BATCH_CREATE_ERROR, taskCodeCreates);
        }

        // batch sync to process task relation log
        int saveTaskRelationResult = saveTaskRelation(loginUser, workflowDefinition, insertVersion);
        if (saveTaskRelationResult != Constants.EXIT_CODE_SUCCESS) {
            log.error("Save process task relations error, projectCode:{}, processCode:{}, processVersion:{}.",
                    workflowDefinition.getProjectCode(), workflowDefinition.getCode(), insertVersion);
            throw new ServiceException(Status.CREATE_PROCESS_TASK_RELATION_ERROR);
        }
        log.info("Save process task relations complete, projectCode:{}, processCode:{}, processVersion:{}.",
                workflowDefinition.getProjectCode(), workflowDefinition.getCode(), insertVersion);
        workflowTaskRelations.get(0).setProcessDefinitionVersion(insertVersion);
        return workflowTaskRelations;
    }

    public int saveTaskRelation(User loginUser, WorkflowDefinition workflowDefinition,
                                int processDefinitionVersion) {
        long projectCode = workflowDefinition.getProjectCode();
        long processDefinitionCode = workflowDefinition.getCode();
        List<WorkflowTaskRelation> taskRelations =
                processTaskRelationMapper.queryByProcessCode(processDefinitionCode);
        List<WorkflowTaskRelationLog> taskRelationList =
                taskRelations.stream().map(WorkflowTaskRelationLog::new).collect(Collectors.toList());

        List<Long> taskCodeList =
                taskRelations.stream().map(WorkflowTaskRelation::getPostTaskCode).collect(Collectors.toList());
        List<TaskDefinition> taskDefinitions = taskDefinitionMapper.queryByCodeList(taskCodeList);
        List<TaskDefinitionLog> taskDefinitionLogs =
                taskDefinitions.stream().map(TaskDefinitionLog::new).collect(Collectors.toList());

        if (taskRelationList.isEmpty()) {
            return Constants.EXIT_CODE_SUCCESS;
        }
        Map<Long, TaskDefinitionLog> taskDefinitionLogMap = null;
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(taskDefinitionLogs)) {
            taskDefinitionLogMap = taskDefinitionLogs
                    .stream()
                    .collect(Collectors.toMap(TaskDefinition::getCode, taskDefinitionLog -> taskDefinitionLog));
        }
        Date now = new Date();
        for (WorkflowTaskRelationLog processTaskRelationLog : taskRelationList) {
            processTaskRelationLog.setProjectCode(projectCode);
            processTaskRelationLog.setProcessDefinitionCode(processDefinitionCode);
            processTaskRelationLog.setProcessDefinitionVersion(processDefinitionVersion);
            if (taskDefinitionLogMap != null) {
                TaskDefinitionLog preTaskDefinitionLog =
                        taskDefinitionLogMap.get(processTaskRelationLog.getPreTaskCode());
                if (preTaskDefinitionLog != null) {
                    processTaskRelationLog.setPreTaskVersion(preTaskDefinitionLog.getVersion());
                }
                TaskDefinitionLog postTaskDefinitionLog =
                        taskDefinitionLogMap.get(processTaskRelationLog.getPostTaskCode());
                if (postTaskDefinitionLog != null) {
                    processTaskRelationLog.setPostTaskVersion(postTaskDefinitionLog.getVersion());
                }
            }
            processTaskRelationLog.setCreateTime(now);
            processTaskRelationLog.setUpdateTime(now);
            processTaskRelationLog.setOperator(loginUser.getId());
            processTaskRelationLog.setOperateTime(now);
        }
        if (CollectionUtils.isNotEmpty(taskRelations)) {
            Set<Integer> processTaskRelationSet =
                    taskRelations.stream().map(WorkflowTaskRelation::hashCode).collect(toSet());
            Set<Integer> taskRelationSet =
                    taskRelationList.stream().map(WorkflowTaskRelationLog::hashCode).collect(toSet());
            boolean isSame = org.apache.commons.collections.CollectionUtils.isEqualCollection(processTaskRelationSet,
                    taskRelationSet);
            if (isSame) {
                return Constants.EXIT_CODE_SUCCESS;
            }
            processTaskRelationMapper.deleteByCode(projectCode, processDefinitionCode);
        }
        List<WorkflowTaskRelation> workflowTaskRelations =
                taskRelationList.stream().map(WorkflowTaskRelation::new).collect(Collectors.toList());
        int insert = processTaskRelationMapper.batchInsert(workflowTaskRelations);
        int resultLog = processTaskRelationLogMapper.batchInsert(taskRelationList);
        return (insert & resultLog) > 0 ? Constants.EXIT_CODE_SUCCESS : Constants.EXIT_CODE_FAILURE;
    }

    public int saveProcessDefine(User loginUser, WorkflowDefinition workflowDefinition) {
        WorkflowDefinitionLog processDefinitionLog = new WorkflowDefinitionLog(workflowDefinition);
        Integer version = processDefinitionLogMapper.queryMaxVersionForDefinition(workflowDefinition.getCode());
        int insertVersion = version == null || version == 0 ? Constants.VERSION_FIRST : version + 1;
        processDefinitionLog.setVersion(insertVersion);
        processDefinitionLog.setOperator(loginUser.getId());
        processDefinitionLog.setOperateTime(workflowDefinition.getUpdateTime());
        processDefinitionLog.setId(null);
        int insertLog = processDefinitionLogMapper.insert(processDefinitionLog);

        processDefinitionLog.setId(workflowDefinition.getId());
        int result = processDefinitionMapper.updateById(processDefinitionLog);
        return (insertLog & result) > 0 ? insertVersion : 0;
    }

    private void updateRelation(User loginUser, Map<String, Object> result, WorkflowDefinition workflowDefinition,
                                List<WorkflowTaskRelation> workflowTaskRelationList) {
        List<WorkflowTaskRelationLog> relationLogs =
                workflowTaskRelationList.stream().map(WorkflowTaskRelationLog::new).collect(Collectors.toList());
        int insertResult = processService.saveTaskRelation(loginUser, workflowDefinition.getProjectCode(),
                workflowDefinition.getCode(),
                workflowDefinition.getVersion(), relationLogs, Lists.newArrayList(), Boolean.TRUE);
        if (insertResult == Constants.EXIT_CODE_SUCCESS) {
            log.info(
                    "Update task relations complete, projectCode:{}, processDefinitionCode:{}, processDefinitionVersion:{}.",
                    workflowDefinition.getProjectCode(), workflowDefinition.getCode(), workflowDefinition.getVersion());
            putMsg(result, Status.SUCCESS);
            result.put(Constants.DATA_LIST, workflowDefinition);
        } else {
            log.error(
                    "Update task relations error, projectCode:{}, processDefinitionCode:{}, processDefinitionVersion:{}.",
                    workflowDefinition.getProjectCode(), workflowDefinition.getCode(), workflowDefinition.getVersion());
            putMsg(result, Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
        }
    }

    /**
     * delete task upstream relation
     *
     * @param loginUser    login user
     * @param projectCode  project code
     * @param preTaskCodes the pre task codes, sep ','
     * @param taskCode     the post task code
     * @return delete result code
     */
    @Transactional
    @Override
    public Map<String, Object> deleteUpstreamRelation(User loginUser, long projectCode, String preTaskCodes,
                                                      long taskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode, null);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        if (StringUtils.isEmpty(preTaskCodes)) {
            log.warn("Parameter preTaskCodes is empty.");
            putMsg(result, Status.DATA_IS_NULL, "preTaskCodes");
            return result;
        }
        List<WorkflowTaskRelation> upstreamList = processTaskRelationMapper.queryUpstreamByCode(projectCode, taskCode);
        if (CollectionUtils.isEmpty(upstreamList)) {
            log.error("Upstream tasks based on the task do not exist, theTaskDefinitionCode:{}.", taskCode);
            putMsg(result, Status.DATA_IS_NULL, "taskCode");
            return result;
        }

        List<Long> preTaskCodeList = Lists.newArrayList(preTaskCodes.split(Constants.COMMA)).stream()
                .map(Long::parseLong).collect(Collectors.toList());
        if (preTaskCodeList.contains(0L)) {
            log.warn("Parameter preTaskCodes contain 0.");
            putMsg(result, Status.DATA_IS_NULL, "preTaskCodes");
            return result;
        }
        List<Long> currentUpstreamList =
                upstreamList.stream().map(WorkflowTaskRelation::getPreTaskCode).collect(Collectors.toList());
        if (currentUpstreamList.contains(0L)) {
            log.error("Upstream taskCodes based on the task contain, theTaskDefinitionCode:{}.", taskCode);
            putMsg(result, Status.DATA_IS_NOT_VALID, "currentUpstreamList");
            return result;
        }
        List<Long> tmpCurrent = Lists.newArrayList(currentUpstreamList);
        tmpCurrent.removeAll(preTaskCodeList);
        preTaskCodeList.removeAll(currentUpstreamList);
        if (!preTaskCodeList.isEmpty()) {
            String invalidPreTaskCodes = StringUtils.join(preTaskCodeList, Constants.COMMA);
            log.error("Some upstream taskCodes are invalid, preTaskCodeList:{}.", invalidPreTaskCodes);
            putMsg(result, Status.DATA_IS_NOT_VALID, invalidPreTaskCodes);
            return result;
        }
        WorkflowDefinition workflowDefinition =
                processDefinitionMapper.queryByCode(upstreamList.get(0).getProcessDefinitionCode());
        if (workflowDefinition == null) {
            log.error("Process definition does not exist, processDefinitionCode:{}.",
                    upstreamList.get(0).getProcessDefinitionCode());
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST,
                    String.valueOf(upstreamList.get(0).getProcessDefinitionCode()));
            return result;
        }
        List<WorkflowTaskRelation> workflowTaskRelations =
                processTaskRelationMapper.queryByProcessCode(workflowDefinition.getCode());
        List<WorkflowTaskRelation> workflowTaskRelationList = Lists.newArrayList(workflowTaskRelations);
        List<WorkflowTaskRelation> workflowTaskRelationWaitRemove = Lists.newArrayList();
        for (WorkflowTaskRelation workflowTaskRelation : workflowTaskRelationList) {
            if (currentUpstreamList.size() > 1) {
                if (currentUpstreamList.contains(workflowTaskRelation.getPreTaskCode())) {
                    currentUpstreamList.remove(workflowTaskRelation.getPreTaskCode());
                    workflowTaskRelationWaitRemove.add(workflowTaskRelation);
                }
            } else {
                if (workflowTaskRelation.getPostTaskCode() == taskCode
                        && (currentUpstreamList.isEmpty() || tmpCurrent.isEmpty())) {
                    workflowTaskRelation.setPreTaskVersion(0);
                    workflowTaskRelation.setPreTaskCode(0L);
                }
            }
        }
        workflowTaskRelationList.removeAll(workflowTaskRelationWaitRemove);
        updateProcessDefiniteVersion(loginUser, result, workflowDefinition);
        updateRelation(loginUser, result, workflowDefinition, workflowTaskRelationList);
        return result;
    }

    /**
     * delete task downstream relation
     *
     * @param loginUser     login user
     * @param projectCode   project code
     * @param postTaskCodes the post task codes, sep ','
     * @param taskCode      the pre task code
     * @return delete result code
     */
    @Transactional
    @Override
    public Map<String, Object> deleteDownstreamRelation(User loginUser, long projectCode, String postTaskCodes,
                                                        long taskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode, null);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        if (StringUtils.isEmpty(postTaskCodes)) {
            log.warn("Parameter postTaskCodes is empty.");
            putMsg(result, Status.DATA_IS_NULL, "postTaskCodes");
            return result;
        }
        List<WorkflowTaskRelation> downstreamList =
                processTaskRelationMapper.queryDownstreamByCode(projectCode, taskCode);
        if (CollectionUtils.isEmpty(downstreamList)) {
            log.error("Downstream tasks based on the task do not exist, theTaskDefinitionCode:{}.", taskCode);
            putMsg(result, Status.DATA_IS_NULL, "taskCode");
            return result;
        }
        List<Long> postTaskCodeList = Lists.newArrayList(postTaskCodes.split(Constants.COMMA)).stream()
                .map(Long::parseLong).collect(Collectors.toList());
        if (postTaskCodeList.contains(0L)) {
            log.warn("Parameter postTaskCodes contains 0.");
            putMsg(result, Status.DATA_IS_NULL, "postTaskCodes");
            return result;
        }
        WorkflowDefinition workflowDefinition =
                processDefinitionMapper.queryByCode(downstreamList.get(0).getProcessDefinitionCode());
        if (workflowDefinition == null) {
            log.error("Process definition does not exist, processDefinitionCode:{}.",
                    downstreamList.get(0).getProcessDefinitionCode());
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST,
                    String.valueOf(downstreamList.get(0).getProcessDefinitionCode()));
            return result;
        }
        List<WorkflowTaskRelation> workflowTaskRelations =
                processTaskRelationMapper.queryByProcessCode(workflowDefinition.getCode());
        List<WorkflowTaskRelation> workflowTaskRelationList = Lists.newArrayList(workflowTaskRelations);
        workflowTaskRelationList
                .removeIf(processTaskRelation -> postTaskCodeList.contains(processTaskRelation.getPostTaskCode())
                        && processTaskRelation.getPreTaskCode() == taskCode);
        updateProcessDefiniteVersion(loginUser, result, workflowDefinition);
        updateRelation(loginUser, result, workflowDefinition, workflowTaskRelationList);
        return result;
    }

    /**
     * query task upstream relation
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param taskCode    current task code (post task code)
     * @return the upstream task definitions
     */
    @Override
    public Map<String, Object> queryUpstreamRelation(User loginUser, long projectCode, long taskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode, null);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        List<WorkflowTaskRelation> workflowTaskRelationList =
                processTaskRelationMapper.queryUpstreamByCode(projectCode, taskCode);
        List<TaskDefinitionLog> taskDefinitionLogList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(workflowTaskRelationList)) {
            Set<TaskDefinition> taskDefinitions = workflowTaskRelationList
                    .stream()
                    .map(processTaskRelation -> {
                        TaskDefinition taskDefinition = buildTaskDefinition();
                        taskDefinition.setProjectCode(processTaskRelation.getProjectCode());
                        taskDefinition.setCode(processTaskRelation.getPreTaskCode());
                        taskDefinition.setVersion(processTaskRelation.getPreTaskVersion());
                        return taskDefinition;
                    })
                    .collect(Collectors.toSet());
            taskDefinitionLogList = taskDefinitionLogMapper.queryByTaskDefinitions(taskDefinitions);
        }
        result.put(Constants.DATA_LIST, taskDefinitionLogList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query task downstream relation
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param taskCode    pre task code
     * @return the downstream task definitions
     */
    @Override
    public Map<String, Object> queryDownstreamRelation(User loginUser, long projectCode, long taskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode, null);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        List<WorkflowTaskRelation> workflowTaskRelationList =
                processTaskRelationMapper.queryDownstreamByCode(projectCode, taskCode);
        List<TaskDefinitionLog> taskDefinitionLogList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(workflowTaskRelationList)) {
            Set<TaskDefinition> taskDefinitions = workflowTaskRelationList
                    .stream()
                    .map(processTaskRelation -> {
                        TaskDefinition taskDefinition = buildTaskDefinition();
                        taskDefinition.setProjectCode(processTaskRelation.getProjectCode());
                        taskDefinition.setCode(processTaskRelation.getPostTaskCode());
                        taskDefinition.setVersion(processTaskRelation.getPostTaskVersion());
                        return taskDefinition;
                    })
                    .collect(Collectors.toSet());
            taskDefinitionLogList = taskDefinitionLogMapper.queryByTaskDefinitions(taskDefinitions);
        }
        result.put(Constants.DATA_LIST, taskDefinitionLogList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * delete edge
     *
     * @param loginUser             login user
     * @param projectCode           project code
     * @param processDefinitionCode process definition code
     * @param preTaskCode           pre task code
     * @param postTaskCode          post task code
     * @return delete result code
     */
    @Transactional
    @Override
    public Map<String, Object> deleteEdge(User loginUser, long projectCode, long processDefinitionCode,
                                          long preTaskCode, long postTaskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode, null);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        WorkflowDefinition workflowDefinition = processDefinitionMapper.queryByCode(processDefinitionCode);
        if (workflowDefinition == null) {
            log.error("Process definition does not exist, projectCode：{}， processDefinitionCode:{}.", projectCode,
                    processDefinitionCode);
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(processDefinitionCode));
            return result;
        }
        List<WorkflowTaskRelation> workflowTaskRelations =
                processTaskRelationMapper.queryByProcessCode(processDefinitionCode);
        List<WorkflowTaskRelation> workflowTaskRelationList = Lists.newArrayList(workflowTaskRelations);
        if (CollectionUtils.isEmpty(workflowTaskRelationList)) {
            log.error("Process task relations are empty, projectCode:{}, processDefinitionCode:{}.", projectCode,
                    processDefinitionCode);
            putMsg(result, Status.DATA_IS_NULL, "processTaskRelationList");
            return result;
        }
        Map<Long, List<WorkflowTaskRelation>> taskRelationMap = new HashMap<>();
        for (WorkflowTaskRelation workflowTaskRelation : workflowTaskRelationList) {
            taskRelationMap.compute(workflowTaskRelation.getPostTaskCode(), (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }
                v.add(workflowTaskRelation);
                return v;
            });
        }
        if (!taskRelationMap.containsKey(postTaskCode)) {
            putMsg(result, Status.DATA_IS_NULL, "postTaskCode");
            return result;
        }
        if (taskRelationMap.get(postTaskCode).size() > 1) {
            for (WorkflowTaskRelation workflowTaskRelation : taskRelationMap.get(postTaskCode)) {
                if (workflowTaskRelation.getPreTaskCode() == preTaskCode) {
                    int delete = processTaskRelationMapper.deleteById(workflowTaskRelation.getId());
                    if (delete == 0) {
                        log.error(
                                "Delete task relation edge error, processTaskRelationId:{}, preTaskCode:{}, postTaskCode:{}",
                                workflowTaskRelation.getId(), preTaskCode, postTaskCode);
                        putMsg(result, Status.DELETE_EDGE_ERROR);
                        throw new ServiceException(Status.DELETE_EDGE_ERROR);
                    } else
                        log.info(
                                "Delete task relation edge complete, processTaskRelationId:{}, preTaskCode:{}, postTaskCode:{}",
                                workflowTaskRelation.getId(), preTaskCode, postTaskCode);
                    workflowTaskRelationList.remove(workflowTaskRelation);
                }
            }
        } else {
            WorkflowTaskRelation workflowTaskRelation = taskRelationMap.get(postTaskCode).get(0);
            workflowTaskRelationList.remove(workflowTaskRelation);
            workflowTaskRelation.setPreTaskVersion(0);
            workflowTaskRelation.setPreTaskCode(0L);
            workflowTaskRelationList.add(workflowTaskRelation);
            log.info(
                    "Delete task relation through set invalid value for it: preTaskCode from {} to 0, processTaskRelationId:{}.",
                    preTaskCode, workflowTaskRelation.getId());
        }
        updateProcessDefiniteVersion(loginUser, result, workflowDefinition);
        updateRelation(loginUser, result, workflowDefinition, workflowTaskRelationList);
        return result;
    }

    @Override
    public List<WorkflowTaskRelation> queryByWorkflowDefinitionCode(long workflowDefinitionCode,
                                                                    int workflowDefinitionVersion) {
        return processTaskRelationMapper.queryProcessTaskRelationsByProcessDefinitionCode(workflowDefinitionCode,
                workflowDefinitionVersion);
    }

    @Override
    public void deleteByWorkflowDefinitionCode(long workflowDefinitionCode, int workflowDefinitionVersion) {
        processTaskRelationMapper.deleteByWorkflowDefinitionCode(workflowDefinitionCode, workflowDefinitionVersion);
    }

    /**
     * build task definition
     *
     * @return task definition
     */
    private TaskDefinition buildTaskDefinition() {

        return new TaskDefinition() {

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof TaskDefinition)) {
                    return false;
                }
                TaskDefinition that = (TaskDefinition) o;
                return getCode() == that.getCode()
                        && getVersion() == that.getVersion()
                        && getProjectCode() == that.getProjectCode();
            }

            @Override
            public int hashCode() {
                return Objects.hash(getCode(), getVersion(), getProjectCode());
            }
        };
    }
}
