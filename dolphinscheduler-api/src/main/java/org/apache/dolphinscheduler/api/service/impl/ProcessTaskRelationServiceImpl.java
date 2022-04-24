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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

/**
 * process task relation service impl
 */
@Service
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
    private ProcessService processService;

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
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> createProcessTaskRelation(User loginUser, long projectCode, long processDefinitionCode, long preTaskCode, long postTaskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(processDefinitionCode);
        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(processDefinitionCode));
            return result;
        }
        if (processDefinition.getProjectCode() != projectCode) {
            putMsg(result, Status.PROJECT_PROCESS_NOT_MATCH);
            return result;
        }
        updateProcessDefiniteVersion(loginUser, result, processDefinition);
        List<ProcessTaskRelation> processTaskRelationList = processTaskRelationMapper.queryByProcessCode(projectCode, processDefinitionCode);
        List<ProcessTaskRelation> processTaskRelations = Lists.newArrayList(processTaskRelationList);
        if (!processTaskRelations.isEmpty()) {
            Map<Long, ProcessTaskRelation> preTaskCodeMap = processTaskRelations.stream().filter(r -> r.getPostTaskCode() == postTaskCode)
                .collect(Collectors.toMap(ProcessTaskRelation::getPreTaskCode, processTaskRelation -> processTaskRelation));
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
            List<ProcessTaskRelation> upstreamTaskRelationList = processTaskRelations.stream().filter(r -> r.getPostTaskCode() == preTaskCode).collect(Collectors.toList());
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

    private void updateProcessDefiniteVersion(User loginUser, Map<String, Object> result, ProcessDefinition processDefinition) {
        int insertVersion = processService.saveProcessDefine(loginUser, processDefinition, Boolean.TRUE, Boolean.TRUE);
        if (insertVersion <= 0) {
            putMsg(result, Status.UPDATE_PROCESS_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_PROCESS_DEFINITION_ERROR);
        }
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
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> deleteTaskProcessRelation(User loginUser, long projectCode, long processDefinitionCode, long taskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        if (taskCode == 0) {
            putMsg(result, Status.DELETE_TASK_PROCESS_RELATION_ERROR);
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(processDefinitionCode);
        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(processDefinitionCode));
            return result;
        }
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (null == taskDefinition) {
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, String.valueOf(taskCode));
            return result;
        }
        List<ProcessTaskRelation> processTaskRelations = processTaskRelationMapper.queryByProcessCode(projectCode, processDefinitionCode);
        List<ProcessTaskRelation> processTaskRelationList = Lists.newArrayList(processTaskRelations);
        if (CollectionUtils.isEmpty(processTaskRelationList)) {
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
            putMsg(result, Status.TASK_HAS_DOWNSTREAM, org.apache.commons.lang.StringUtils.join(downstreamList, ","));
            return result;
        }
        updateProcessDefiniteVersion(loginUser, result, processDefinition);
        updateRelation(loginUser, result, processDefinition, processTaskRelationList);
        if (TASK_TYPE_CONDITIONS.equals(taskDefinition.getTaskType())
            || TASK_TYPE_DEPENDENT.equals(taskDefinition.getTaskType())
            || TASK_TYPE_SUB_PROCESS.equals(taskDefinition.getTaskType())) {
            int deleteTaskDefinition = taskDefinitionMapper.deleteByCode(taskCode);
            if (0 == deleteTaskDefinition) {
                putMsg(result, Status.DELETE_TASK_DEFINE_BY_CODE_ERROR);
                throw new ServiceException(Status.DELETE_TASK_DEFINE_BY_CODE_ERROR);
            }
        }
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private void updateRelation(User loginUser, Map<String, Object> result, ProcessDefinition processDefinition,
                                List<ProcessTaskRelation> processTaskRelationList) {
        List<ProcessTaskRelationLog> relationLogs = processTaskRelationList.stream().map(ProcessTaskRelationLog::new).collect(Collectors.toList());
        int insertResult = processService.saveTaskRelation(loginUser, processDefinition.getProjectCode(), processDefinition.getCode(),
            processDefinition.getVersion(), relationLogs, Lists.newArrayList(), Boolean.TRUE);
        if (insertResult == Constants.EXIT_CODE_SUCCESS) {
            putMsg(result, Status.SUCCESS);
            result.put(Constants.DATA_LIST, processDefinition);
        } else {
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
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> deleteUpstreamRelation(User loginUser, long projectCode, String preTaskCodes, long taskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        if (StringUtils.isEmpty(preTaskCodes)) {
            putMsg(result, Status.DATA_IS_NULL, "preTaskCodes");
            return result;
        }
        List<ProcessTaskRelation> upstreamList = processTaskRelationMapper.queryUpstreamByCode(projectCode, taskCode);
        if (CollectionUtils.isEmpty(upstreamList)) {
            putMsg(result, Status.DATA_IS_NULL, "taskCode");
            return result;
        }

        List<Long> preTaskCodeList = Lists.newArrayList(preTaskCodes.split(Constants.COMMA)).stream().map(Long::parseLong).collect(Collectors.toList());
        if (preTaskCodeList.contains(0L)) {
            putMsg(result, Status.DATA_IS_NULL, "preTaskCodes");
            return result;
        }
        List<Long> currentUpstreamList = upstreamList.stream().map(ProcessTaskRelation::getPreTaskCode).collect(Collectors.toList());
        if (currentUpstreamList.contains(0L)) {
            putMsg(result, Status.DATA_IS_NOT_VALID, "currentUpstreamList");
            return result;
        }
        List<Long> tmpCurrent = Lists.newArrayList(currentUpstreamList);
        tmpCurrent.removeAll(preTaskCodeList);
        preTaskCodeList.removeAll(currentUpstreamList);
        if (!preTaskCodeList.isEmpty()) {
            putMsg(result, Status.DATA_IS_NOT_VALID, StringUtils.join(preTaskCodeList, Constants.COMMA));
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(upstreamList.get(0).getProcessDefinitionCode());
        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(upstreamList.get(0).getProcessDefinitionCode()));
            return result;
        }
        List<ProcessTaskRelation> processTaskRelations = processTaskRelationMapper.queryByProcessCode(projectCode, processDefinition.getCode());
        List<ProcessTaskRelation> processTaskRelationList = Lists.newArrayList(processTaskRelations);
        List<ProcessTaskRelation> processTaskRelationWaitRemove = Lists.newArrayList();
        for (ProcessTaskRelation processTaskRelation : processTaskRelationList) {
            if (currentUpstreamList.size() > 1) {
                if (currentUpstreamList.contains(processTaskRelation.getPreTaskCode())) {
                    currentUpstreamList.remove(processTaskRelation.getPreTaskCode());
                    processTaskRelationWaitRemove.add(processTaskRelation);
                }
            } else {
                if (processTaskRelation.getPostTaskCode() == taskCode && (currentUpstreamList.isEmpty() || tmpCurrent.isEmpty())) {
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
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> deleteDownstreamRelation(User loginUser, long projectCode, String postTaskCodes, long taskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        if (StringUtils.isEmpty(postTaskCodes)) {
            putMsg(result, Status.DATA_IS_NULL, "postTaskCodes");
            return result;
        }
        List<ProcessTaskRelation> downstreamList = processTaskRelationMapper.queryDownstreamByCode(projectCode, taskCode);
        if (CollectionUtils.isEmpty(downstreamList)) {
            putMsg(result, Status.DATA_IS_NULL, "taskCode");
            return result;
        }
        List<Long> postTaskCodeList = Lists.newArrayList(postTaskCodes.split(Constants.COMMA)).stream().map(Long::parseLong).collect(Collectors.toList());
        if (postTaskCodeList.contains(0L)) {
            putMsg(result, Status.DATA_IS_NULL, "postTaskCodes");
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(downstreamList.get(0).getProcessDefinitionCode());
        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(downstreamList.get(0).getProcessDefinitionCode()));
            return result;
        }
        List<ProcessTaskRelation> processTaskRelations = processTaskRelationMapper.queryByProcessCode(projectCode, processDefinition.getCode());
        List<ProcessTaskRelation> processTaskRelationList = Lists.newArrayList(processTaskRelations);
        processTaskRelationList.removeIf(processTaskRelation -> postTaskCodeList.contains(processTaskRelation.getPostTaskCode()) && processTaskRelation.getPreTaskCode() == taskCode);
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
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        List<ProcessTaskRelation> processTaskRelationList = processTaskRelationMapper.queryUpstreamByCode(projectCode, taskCode);
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
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        List<ProcessTaskRelation> processTaskRelationList = processTaskRelationMapper.queryDownstreamByCode(projectCode, taskCode);
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
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> deleteEdge(User loginUser, long projectCode, long processDefinitionCode, long preTaskCode, long postTaskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(processDefinitionCode);
        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(processDefinitionCode));
            return result;
        }
        List<ProcessTaskRelation> processTaskRelations = processTaskRelationMapper.queryByProcessCode(projectCode, processDefinitionCode);
        List<ProcessTaskRelation> processTaskRelationList = Lists.newArrayList(processTaskRelations);
        if (CollectionUtils.isEmpty(processTaskRelationList)) {
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
                        putMsg(result, Status.DELETE_EDGE_ERROR);
                        throw new ServiceException(Status.DELETE_EDGE_ERROR);
                    }
                    processTaskRelationList.remove(processTaskRelation);
                }
            }
        } else {
            ProcessTaskRelation processTaskRelation = taskRelationMap.get(postTaskCode).get(0);
            processTaskRelationList.remove(processTaskRelation);
            processTaskRelation.setPreTaskVersion(0);
            processTaskRelation.setPreTaskCode(0L);
            processTaskRelationList.add(processTaskRelation);
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
