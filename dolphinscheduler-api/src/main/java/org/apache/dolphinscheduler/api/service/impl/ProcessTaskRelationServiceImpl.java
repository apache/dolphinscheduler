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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_CONDITIONS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_DEPENDENT;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SUB_PROCESS;

import org.apache.dolphinscheduler.api.dto.taskRelation.TaskRelationCreateRequest;
import org.apache.dolphinscheduler.api.dto.taskRelation.TaskRelationFilterRequest;
import org.apache.dolphinscheduler.api.dto.taskRelation.TaskRelationUpdateUpstreamRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProcessTaskRelationService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ConditionType;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
 * process task relation service impl
 */
@Service
public class ProcessTaskRelationServiceImpl extends BaseServiceImpl implements ProcessTaskRelationService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessTaskRelationServiceImpl.class);

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
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(processDefinitionCode);
        if (processDefinition == null) {
            logger.error("Process definition does not exist, processCode:{}.", processDefinitionCode);
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(processDefinitionCode));
            return result;
        }
        if (processDefinition.getProjectCode() != projectCode) {
            logger.error("Process definition's project does not match project {}.", projectCode);
            putMsg(result, Status.PROJECT_PROCESS_NOT_MATCH);
            return result;
        }
        updateProcessDefiniteVersion(loginUser, result, processDefinition);
        List<ProcessTaskRelation> processTaskRelationList =
                processTaskRelationMapper.queryByProcessCode(projectCode, processDefinitionCode);
        List<ProcessTaskRelation> processTaskRelations = Lists.newArrayList(processTaskRelationList);
        if (!processTaskRelations.isEmpty()) {
            Map<Long, ProcessTaskRelation> preTaskCodeMap =
                    processTaskRelations.stream().filter(r -> r.getPostTaskCode() == postTaskCode)
                            .collect(Collectors.toMap(ProcessTaskRelation::getPreTaskCode,
                                    processTaskRelation -> processTaskRelation));
            if (!preTaskCodeMap.isEmpty()) {
                if (preTaskCodeMap.containsKey(preTaskCode) || (!preTaskCodeMap.containsKey(0L) && preTaskCode == 0L)) {
                    putMsg(result, Status.PROCESS_TASK_RELATION_EXIST, String.valueOf(processDefinitionCode));
                    return result;
                }
                if (preTaskCodeMap.containsKey(0L) && preTaskCode != 0L) {
                    // delete no upstream
                    processTaskRelations.remove(preTaskCodeMap.get(0L));
                }
            }
        }
        TaskDefinition postTaskDefinition = taskDefinitionMapper.queryByCode(postTaskCode);
        ProcessTaskRelation processTaskRelation = setRelation(processDefinition, postTaskDefinition);
        if (preTaskCode != 0L) {
            TaskDefinition preTaskDefinition = taskDefinitionMapper.queryByCode(preTaskCode);
            List<ProcessTaskRelation> upstreamTaskRelationList = processTaskRelations.stream()
                    .filter(r -> r.getPostTaskCode() == preTaskCode).collect(Collectors.toList());
            // upstream is or not exist
            if (upstreamTaskRelationList.isEmpty()) {
                ProcessTaskRelation preProcessTaskRelation = setRelation(processDefinition, preTaskDefinition);
                preProcessTaskRelation.setPreTaskCode(0L);
                preProcessTaskRelation.setPreTaskVersion(0);
                processTaskRelations.add(preProcessTaskRelation);
            }
            processTaskRelation.setPreTaskCode(preTaskDefinition.getCode());
            processTaskRelation.setPreTaskVersion(preTaskDefinition.getVersion());
        } else {
            processTaskRelation.setPreTaskCode(0L);
            processTaskRelation.setPreTaskVersion(0);
        }
        processTaskRelations.add(processTaskRelation);
        updateRelation(loginUser, result, processDefinition, processTaskRelations);
        return result;
    }

    private ProcessTaskRelationLog persist2ProcessTaskRelationLog(User user, ProcessTaskRelation processTaskRelation) {
        ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog(processTaskRelation);
        processTaskRelationLog.setOperator(user.getId());
        processTaskRelationLog.setOperateTime(new Date());
        int result = processTaskRelationLogMapper.insert(processTaskRelationLog);
        if (result <= 0) {
            throw new ServiceException(Status.CREATE_PROCESS_TASK_RELATION_LOG_ERROR,
                    processTaskRelationLog.getPreTaskCode(), processTaskRelationLog.getPostTaskCode());
        }
        return processTaskRelationLog;
    }

    private List<ProcessTaskRelationLog> batchPersist2ProcessTaskRelationLog(User user,
                                                                             List<ProcessTaskRelation> processTaskRelations) {
        Date now = new Date();
        List<ProcessTaskRelationLog> processTaskRelationLogs = new ArrayList<>();

        for (ProcessTaskRelation processTaskRelation : processTaskRelations) {
            ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog(processTaskRelation);
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

    private void updateVersions(ProcessTaskRelation processTaskRelation) {
        // workflow
        ProcessDefinition processDefinition =
                processDefinitionMapper.queryByCode(processTaskRelation.getProcessDefinitionCode());
        processTaskRelation.setProcessDefinitionVersion(processDefinition.getVersion());

        // tasks
        TaskDefinition preTaskDefinition = taskDefinitionMapper.queryByCode(processTaskRelation.getPreTaskCode());
        processTaskRelation.setPreTaskVersion(preTaskDefinition.getVersion());
        TaskDefinition postTaskDefinition = taskDefinitionMapper.queryByCode(processTaskRelation.getPostTaskCode());
        processTaskRelation.setPostTaskVersion(postTaskDefinition.getVersion());
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
    public ProcessTaskRelation createProcessTaskRelationV2(User loginUser,
                                                           TaskRelationCreateRequest taskRelationCreateRequest) {
        ProcessTaskRelation processTaskRelation = taskRelationCreateRequest.convert2ProcessTaskRelation();
        ProcessDefinition processDefinition =
                processDefinitionMapper.queryByCode(processTaskRelation.getProcessDefinitionCode());
        if (processDefinition == null) {
            throw new ServiceException(Status.PROCESS_DEFINE_NOT_EXIST,
                    String.valueOf(processTaskRelation.getProcessDefinitionCode()));
        }
        if (processTaskRelation.getProjectCode() == 0) {
            processTaskRelation.setProjectCode(processDefinition.getProjectCode());
        }
        Project project = projectMapper.queryByCode(processTaskRelation.getProjectCode());
        projectService.checkProjectAndAuthThrowException(loginUser, project, null);

        // persistence process task relation and process task relation log to database
        this.updateVersions(processTaskRelation);
        int insert = processTaskRelationMapper.insert(processTaskRelation);
        if (insert <= 0) {
            throw new ServiceException(Status.CREATE_PROCESS_TASK_RELATION_ERROR, processTaskRelation.getPreTaskCode(),
                    processTaskRelation.getPostTaskCode());
        }
        this.persist2ProcessTaskRelationLog(loginUser, processTaskRelation);

        return processTaskRelation;
    }

    private ProcessTaskRelation setRelation(ProcessDefinition processDefinition, TaskDefinition taskDefinition) {
        Date now = new Date();
        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();
        processTaskRelation.setProjectCode(processDefinition.getProjectCode());
        processTaskRelation.setProcessDefinitionCode(processDefinition.getCode());
        processTaskRelation.setProcessDefinitionVersion(processDefinition.getVersion());
        processTaskRelation.setPostTaskCode(taskDefinition.getCode());
        processTaskRelation.setPostTaskVersion(taskDefinition.getVersion());
        processTaskRelation.setConditionType(ConditionType.NONE);
        processTaskRelation.setConditionParams("{}");
        processTaskRelation.setCreateTime(now);
        processTaskRelation.setUpdateTime(now);
        return processTaskRelation;
    }

    private void updateProcessDefiniteVersion(User loginUser, Map<String, Object> result,
                                              ProcessDefinition processDefinition) {
        int insertVersion = processService.saveProcessDefine(loginUser, processDefinition, Boolean.TRUE, Boolean.TRUE);
        if (insertVersion <= 0) {
            logger.error("Update process definition error, projectCode:{}, processDefinitionCode:{}.",
                    processDefinition.getProjectCode(), processDefinition.getCode());
            putMsg(result, Status.UPDATE_PROCESS_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_PROCESS_DEFINITION_ERROR);
        } else
            logger.info(
                    "Update process definition complete, new version is {}, projectCode:{}, processDefinitionCode:{}.",
                    insertVersion, processDefinition.getProjectCode(), processDefinition.getCode());
        processDefinition.setVersion(insertVersion);
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
            logger.error(
                    "Delete task process relation error due to parameter taskCode is 0, projectCode:{}, processDefinitionCode:{}.",
                    projectCode, processDefinitionCode);
            putMsg(result, Status.DELETE_TASK_PROCESS_RELATION_ERROR);
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(processDefinitionCode);
        if (processDefinition == null) {
            logger.error("Process definition does not exist, processDefinitionCode:{}.", processDefinitionCode);
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(processDefinitionCode));
            return result;
        }
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (null == taskDefinition) {
            logger.error("Task definition does not exist, taskDefinitionCode:{}.", taskCode);
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, String.valueOf(taskCode));
            return result;
        }
        List<ProcessTaskRelation> processTaskRelations =
                processTaskRelationMapper.queryByProcessCode(projectCode, processDefinitionCode);
        List<ProcessTaskRelation> processTaskRelationList = Lists.newArrayList(processTaskRelations);
        if (CollectionUtils.isEmpty(processTaskRelationList)) {
            logger.error("Process task relations are empty, projectCode:{}, processDefinitionCode:{}.", projectCode,
                    processDefinitionCode);
            putMsg(result, Status.DATA_IS_NULL, "processTaskRelationList");
            return result;
        }
        List<Long> downstreamList = Lists.newArrayList();
        for (ProcessTaskRelation processTaskRelation : processTaskRelations) {
            if (processTaskRelation.getPreTaskCode() == taskCode) {
                downstreamList.add(processTaskRelation.getPostTaskCode());
            }
            if (processTaskRelation.getPostTaskCode() == taskCode) {
                processTaskRelationList.remove(processTaskRelation);
            }
        }
        if (CollectionUtils.isNotEmpty(downstreamList)) {
            String downstream = StringUtils.join(downstreamList, ",");
            logger.warn(
                    "Relation can not be deleted because task has downstream tasks:[{}], projectCode:{}, processDefinitionCode:{}, taskDefinitionCode:{}.",
                    downstream, projectCode, processDefinitionCode, taskCode);
            putMsg(result, Status.TASK_HAS_DOWNSTREAM, downstream);
            return result;
        }
        updateProcessDefiniteVersion(loginUser, result, processDefinition);
        updateRelation(loginUser, result, processDefinition, processTaskRelationList);
        if (TASK_TYPE_CONDITIONS.equals(taskDefinition.getTaskType())
                || TASK_TYPE_DEPENDENT.equals(taskDefinition.getTaskType())
                || TASK_TYPE_SUB_PROCESS.equals(taskDefinition.getTaskType())) {
            int deleteTaskDefinition = taskDefinitionMapper.deleteByCode(taskCode);
            if (0 == deleteTaskDefinition) {
                logger.error("Delete task definition error, taskDefinitionCode:{}.", taskCode);
                putMsg(result, Status.DELETE_TASK_DEFINE_BY_CODE_ERROR);
                throw new ServiceException(Status.DELETE_TASK_DEFINE_BY_CODE_ERROR);
            } else
                logger.info("Delete {} type task definition complete, taskDefinitionCode:{}.",
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
        ProcessTaskRelation processTaskRelation =
                new TaskRelationFilterRequest(preTaskCode, postTaskCode).convert2TaskDefinition();

        Page<ProcessTaskRelation> page =
                new Page<>(new TaskRelationFilterRequest(preTaskCode, postTaskCode).getPageNo(),
                        new TaskRelationFilterRequest(preTaskCode, postTaskCode).getPageSize());
        IPage<ProcessTaskRelation> processTaskRelationIPage =
                processTaskRelationMapper.filterProcessTaskRelation(page, processTaskRelation);

        List<ProcessTaskRelation> processTaskRelations = processTaskRelationIPage.getRecords();
        if (processTaskRelations.size() != 1) {
            throw new ServiceException(Status.PROCESS_TASK_RELATION_NOT_EXPECT, 1, processTaskRelations.size());
        }

        ProcessTaskRelation processTaskRelationDb = processTaskRelations.get(0);
        Project project = projectMapper.queryByCode(processTaskRelationDb.getProjectCode());
        projectService.checkProjectAndAuthThrowException(loginUser, project, null);
        processTaskRelationMapper.deleteById(processTaskRelationDb.getId());
    }

    /**
     * delete process task relation, will delete exists relation upstream -> downstream, throw error if not exists
     *
     * @param loginUser login user
     * @param taskCode relation upstream code
     * @param taskRelationUpdateUpstreamRequest relation downstream code
     */
    @Override
    @Transactional
    public List<ProcessTaskRelation> updateUpstreamTaskDefinition(User loginUser,
                                                                  long taskCode,
                                                                  TaskRelationUpdateUpstreamRequest taskRelationUpdateUpstreamRequest) {
        TaskDefinition downstreamTask = taskDefinitionMapper.queryByCode(taskCode);
        if (downstreamTask == null) {
            throw new ServiceException(Status.TASK_DEFINE_NOT_EXIST, taskCode);
        }
        List<Long> upstreamTaskCodes = taskRelationUpdateUpstreamRequest.getUpstreams();

        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();
        processTaskRelation.setPostTaskCode(taskCode);

        Page<ProcessTaskRelation> page = new Page<>(taskRelationUpdateUpstreamRequest.getPageNo(),
                taskRelationUpdateUpstreamRequest.getPageSize());
        IPage<ProcessTaskRelation> processTaskRelationExistsIPage =
                processTaskRelationMapper.filterProcessTaskRelation(page, processTaskRelation);
        List<ProcessTaskRelation> processTaskRelationExists = processTaskRelationExistsIPage.getRecords();

        ProcessDefinition processDefinition = null;
        if (CollectionUtils.isNotEmpty(processTaskRelationExists)) {
            processDefinition =
                    processDefinitionMapper.queryByCode(processTaskRelationExists.get(0).getProcessDefinitionCode());
        } else if (taskRelationUpdateUpstreamRequest.getWorkflowCode() != 0L) {
            processDefinition =
                    processDefinitionMapper.queryByCode(taskRelationUpdateUpstreamRequest.getWorkflowCode());
        }
        if (processDefinition == null) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                    taskRelationUpdateUpstreamRequest.toString());
        }

        // get new relation to create and out of date relation to delete
        List<Long> taskCodeCreates = upstreamTaskCodes
                .stream()
                .filter(upstreamTaskCode -> processTaskRelationExists.stream().noneMatch(
                        processTaskRelationExist -> processTaskRelationExist.getPreTaskCode() == upstreamTaskCode))
                .collect(Collectors.toList());
        List<Long> taskCodeDeletes = processTaskRelationExists
                .stream()
                .map(ProcessTaskRelation::getPreTaskCode)
                .filter(preTaskCode -> !upstreamTaskCodes.contains(preTaskCode))
                .collect(Collectors.toList());

        // delete relation not exists
        if (CollectionUtils.isNotEmpty(taskCodeDeletes)) {
            int delete = processTaskRelationMapper.deleteBatchIds(taskCodeDeletes);
            if (delete != taskCodeDeletes.size()) {
                throw new ServiceException(Status.PROCESS_TASK_RELATION_BATCH_DELETE_ERROR, taskCodeDeletes);
            }
        }

        // create relation not exists
        List<ProcessTaskRelation> processTaskRelations = new ArrayList<>();
        for (long createCode : taskCodeCreates) {
            TaskDefinition upstreamTask = taskDefinitionMapper.queryByCode(createCode);
            ProcessTaskRelation processTaskRelationCreate =
                    new ProcessTaskRelation(null, processDefinition.getVersion(), downstreamTask.getProjectCode(),
                            processDefinition.getCode(), upstreamTask.getCode(), upstreamTask.getVersion(),
                            downstreamTask.getCode(), downstreamTask.getVersion(), null, null);
            processTaskRelations.add(processTaskRelationCreate);
        }
        int batchInsert = processTaskRelationMapper.batchInsert(processTaskRelations);
        if (batchInsert != processTaskRelations.size()) {
            throw new ServiceException(Status.PROCESS_TASK_RELATION_BATCH_CREATE_ERROR, taskCodeCreates);
        }

        // batch sync to process task relation log
        this.batchPersist2ProcessTaskRelationLog(loginUser, processTaskRelations);
        return processTaskRelations;
    }

    private void updateRelation(User loginUser, Map<String, Object> result, ProcessDefinition processDefinition,
                                List<ProcessTaskRelation> processTaskRelationList) {
        List<ProcessTaskRelationLog> relationLogs =
                processTaskRelationList.stream().map(ProcessTaskRelationLog::new).collect(Collectors.toList());
        int insertResult = processService.saveTaskRelation(loginUser, processDefinition.getProjectCode(),
                processDefinition.getCode(),
                processDefinition.getVersion(), relationLogs, Lists.newArrayList(), Boolean.TRUE);
        if (insertResult == Constants.EXIT_CODE_SUCCESS) {
            logger.info(
                    "Update task relations complete, projectCode:{}, processDefinitionCode:{}, processDefinitionVersion:{}.",
                    processDefinition.getProjectCode(), processDefinition.getCode(), processDefinition.getVersion());
            putMsg(result, Status.SUCCESS);
            result.put(Constants.DATA_LIST, processDefinition);
        } else {
            logger.error(
                    "Update task relations error, projectCode:{}, processDefinitionCode:{}, processDefinitionVersion:{}.",
                    processDefinition.getProjectCode(), processDefinition.getCode(), processDefinition.getVersion());
            putMsg(result, Status.UPDATE_PROCESS_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_PROCESS_DEFINITION_ERROR);
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
            logger.warn("Parameter preTaskCodes is empty.");
            putMsg(result, Status.DATA_IS_NULL, "preTaskCodes");
            return result;
        }
        List<ProcessTaskRelation> upstreamList = processTaskRelationMapper.queryUpstreamByCode(projectCode, taskCode);
        if (CollectionUtils.isEmpty(upstreamList)) {
            logger.error("Upstream tasks based on the task do not exist, theTaskDefinitionCode:{}.", taskCode);
            putMsg(result, Status.DATA_IS_NULL, "taskCode");
            return result;
        }

        List<Long> preTaskCodeList = Lists.newArrayList(preTaskCodes.split(Constants.COMMA)).stream()
                .map(Long::parseLong).collect(Collectors.toList());
        if (preTaskCodeList.contains(0L)) {
            logger.warn("Parameter preTaskCodes contain 0.");
            putMsg(result, Status.DATA_IS_NULL, "preTaskCodes");
            return result;
        }
        List<Long> currentUpstreamList =
                upstreamList.stream().map(ProcessTaskRelation::getPreTaskCode).collect(Collectors.toList());
        if (currentUpstreamList.contains(0L)) {
            logger.error("Upstream taskCodes based on the task contain, theTaskDefinitionCode:{}.", taskCode);
            putMsg(result, Status.DATA_IS_NOT_VALID, "currentUpstreamList");
            return result;
        }
        List<Long> tmpCurrent = Lists.newArrayList(currentUpstreamList);
        tmpCurrent.removeAll(preTaskCodeList);
        preTaskCodeList.removeAll(currentUpstreamList);
        if (!preTaskCodeList.isEmpty()) {
            String invalidPreTaskCodes = StringUtils.join(preTaskCodeList, Constants.COMMA);
            logger.error("Some upstream taskCodes are invalid, preTaskCodeList:{}.", invalidPreTaskCodes);
            putMsg(result, Status.DATA_IS_NOT_VALID, invalidPreTaskCodes);
            return result;
        }
        ProcessDefinition processDefinition =
                processDefinitionMapper.queryByCode(upstreamList.get(0).getProcessDefinitionCode());
        if (processDefinition == null) {
            logger.error("Process definition does not exist, processDefinitionCode:{}.",
                    upstreamList.get(0).getProcessDefinitionCode());
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST,
                    String.valueOf(upstreamList.get(0).getProcessDefinitionCode()));
            return result;
        }
        List<ProcessTaskRelation> processTaskRelations =
                processTaskRelationMapper.queryByProcessCode(projectCode, processDefinition.getCode());
        List<ProcessTaskRelation> processTaskRelationList = Lists.newArrayList(processTaskRelations);
        List<ProcessTaskRelation> processTaskRelationWaitRemove = Lists.newArrayList();
        for (ProcessTaskRelation processTaskRelation : processTaskRelationList) {
            if (currentUpstreamList.size() > 1) {
                if (currentUpstreamList.contains(processTaskRelation.getPreTaskCode())) {
                    currentUpstreamList.remove(processTaskRelation.getPreTaskCode());
                    processTaskRelationWaitRemove.add(processTaskRelation);
                }
            } else {
                if (processTaskRelation.getPostTaskCode() == taskCode
                        && (currentUpstreamList.isEmpty() || tmpCurrent.isEmpty())) {
                    processTaskRelation.setPreTaskVersion(0);
                    processTaskRelation.setPreTaskCode(0L);
                }
            }
        }
        processTaskRelationList.removeAll(processTaskRelationWaitRemove);
        updateProcessDefiniteVersion(loginUser, result, processDefinition);
        updateRelation(loginUser, result, processDefinition, processTaskRelationList);
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
            logger.warn("Parameter postTaskCodes is empty.");
            putMsg(result, Status.DATA_IS_NULL, "postTaskCodes");
            return result;
        }
        List<ProcessTaskRelation> downstreamList =
                processTaskRelationMapper.queryDownstreamByCode(projectCode, taskCode);
        if (CollectionUtils.isEmpty(downstreamList)) {
            logger.error("Downstream tasks based on the task do not exist, theTaskDefinitionCode:{}.", taskCode);
            putMsg(result, Status.DATA_IS_NULL, "taskCode");
            return result;
        }
        List<Long> postTaskCodeList = Lists.newArrayList(postTaskCodes.split(Constants.COMMA)).stream()
                .map(Long::parseLong).collect(Collectors.toList());
        if (postTaskCodeList.contains(0L)) {
            logger.warn("Parameter postTaskCodes contains 0.");
            putMsg(result, Status.DATA_IS_NULL, "postTaskCodes");
            return result;
        }
        ProcessDefinition processDefinition =
                processDefinitionMapper.queryByCode(downstreamList.get(0).getProcessDefinitionCode());
        if (processDefinition == null) {
            logger.error("Process definition does not exist, processDefinitionCode:{}.",
                    downstreamList.get(0).getProcessDefinitionCode());
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST,
                    String.valueOf(downstreamList.get(0).getProcessDefinitionCode()));
            return result;
        }
        List<ProcessTaskRelation> processTaskRelations =
                processTaskRelationMapper.queryByProcessCode(projectCode, processDefinition.getCode());
        List<ProcessTaskRelation> processTaskRelationList = Lists.newArrayList(processTaskRelations);
        processTaskRelationList
                .removeIf(processTaskRelation -> postTaskCodeList.contains(processTaskRelation.getPostTaskCode())
                        && processTaskRelation.getPreTaskCode() == taskCode);
        updateProcessDefiniteVersion(loginUser, result, processDefinition);
        updateRelation(loginUser, result, processDefinition, processTaskRelationList);
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
        List<ProcessTaskRelation> processTaskRelationList =
                processTaskRelationMapper.queryUpstreamByCode(projectCode, taskCode);
        List<TaskDefinitionLog> taskDefinitionLogList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(processTaskRelationList)) {
            Set<TaskDefinition> taskDefinitions = processTaskRelationList
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
        List<ProcessTaskRelation> processTaskRelationList =
                processTaskRelationMapper.queryDownstreamByCode(projectCode, taskCode);
        List<TaskDefinitionLog> taskDefinitionLogList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(processTaskRelationList)) {
            Set<TaskDefinition> taskDefinitions = processTaskRelationList
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
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(processDefinitionCode);
        if (processDefinition == null) {
            logger.error("Process definition does not exist, projectCode：{}， processDefinitionCode:{}.", projectCode,
                    processDefinitionCode);
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(processDefinitionCode));
            return result;
        }
        List<ProcessTaskRelation> processTaskRelations =
                processTaskRelationMapper.queryByProcessCode(projectCode, processDefinitionCode);
        List<ProcessTaskRelation> processTaskRelationList = Lists.newArrayList(processTaskRelations);
        if (CollectionUtils.isEmpty(processTaskRelationList)) {
            logger.error("Process task relations are empty, projectCode:{}, processDefinitionCode:{}.", projectCode,
                    processDefinitionCode);
            putMsg(result, Status.DATA_IS_NULL, "processTaskRelationList");
            return result;
        }
        Map<Long, List<ProcessTaskRelation>> taskRelationMap = new HashMap<>();
        for (ProcessTaskRelation processTaskRelation : processTaskRelationList) {
            taskRelationMap.compute(processTaskRelation.getPostTaskCode(), (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }
                v.add(processTaskRelation);
                return v;
            });
        }
        if (!taskRelationMap.containsKey(postTaskCode)) {
            putMsg(result, Status.DATA_IS_NULL, "postTaskCode");
            return result;
        }
        if (taskRelationMap.get(postTaskCode).size() > 1) {
            for (ProcessTaskRelation processTaskRelation : taskRelationMap.get(postTaskCode)) {
                if (processTaskRelation.getPreTaskCode() == preTaskCode) {
                    int delete = processTaskRelationMapper.deleteById(processTaskRelation.getId());
                    if (delete == 0) {
                        logger.error(
                                "Delete task relation edge error, processTaskRelationId:{}, preTaskCode:{}, postTaskCode:{}",
                                processTaskRelation.getId(), preTaskCode, postTaskCode);
                        putMsg(result, Status.DELETE_EDGE_ERROR);
                        throw new ServiceException(Status.DELETE_EDGE_ERROR);
                    } else
                        logger.info(
                                "Delete task relation edge complete, processTaskRelationId:{}, preTaskCode:{}, postTaskCode:{}",
                                processTaskRelation.getId(), preTaskCode, postTaskCode);
                    processTaskRelationList.remove(processTaskRelation);
                }
            }
        } else {
            ProcessTaskRelation processTaskRelation = taskRelationMap.get(postTaskCode).get(0);
            processTaskRelationList.remove(processTaskRelation);
            processTaskRelation.setPreTaskVersion(0);
            processTaskRelation.setPreTaskCode(0L);
            processTaskRelationList.add(processTaskRelation);
            logger.info(
                    "Delete task relation through set invalid value for it: preTaskCode from {} to 0, processTaskRelationId:{}.",
                    preTaskCode, processTaskRelation.getId());
        }
        updateProcessDefiniteVersion(loginUser, result, processDefinition);
        updateRelation(loginUser, result, processDefinition, processTaskRelationList);
        return result;
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
