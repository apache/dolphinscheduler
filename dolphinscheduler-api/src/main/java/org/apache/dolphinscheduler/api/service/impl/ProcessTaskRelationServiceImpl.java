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

import static org.apache.dolphinscheduler.api.enums.Status.DATA_IS_NOT_VALID;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProcessTaskRelationService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ConditionType;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
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
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private ProcessTaskRelationLogMapper processTaskRelationLogMapper;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

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
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, processDefinitionCode);
            return result;
        }
        if (processDefinition.getProjectCode() != projectCode) {
            putMsg(result, Status.PROJECT_PROCESS_NOT_MATCH);
            return result;
        }
        List<ProcessTaskRelation> processTaskRelations = processTaskRelationMapper.queryByCode(projectCode, processDefinitionCode, 0L, postTaskCode);
        if (!processTaskRelations.isEmpty()) {
            Map<Long, ProcessTaskRelation> preTaskCodeMap = processTaskRelations.stream()
                .collect(Collectors.toMap(ProcessTaskRelation::getPreTaskCode, processTaskRelation -> processTaskRelation));
            if (preTaskCodeMap.containsKey(preTaskCode) || (!preTaskCodeMap.containsKey(0L) && preTaskCode == 0L)) {
                putMsg(result, Status.PROCESS_TASK_RELATION_EXIST, processDefinitionCode);
                return result;
            }
            if (preTaskCodeMap.containsKey(0L) && preTaskCode != 0L) {
                ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog(preTaskCodeMap.get(0L));
                // delete no upstream
                int delete = processTaskRelationMapper.deleteRelation(processTaskRelationLog);
                int deleteLog = processTaskRelationLogMapper.deleteRelation(processTaskRelationLog);
                if ((delete & deleteLog) == 0) {
                    putMsg(result, Status.CREATE_PROCESS_TASK_RELATION_ERROR);
                    throw new ServiceException(Status.CREATE_PROCESS_TASK_RELATION_ERROR);
                }
            }
        }
        Date now = new Date();
        List<ProcessTaskRelationLog> processTaskRelationLogs = new ArrayList<>();
        if (preTaskCode != 0L) {
            // upstream is or not exist
            List<ProcessTaskRelation> upstreamProcessTaskRelations = processTaskRelationMapper.queryByCode(projectCode, processDefinitionCode, 0L, preTaskCode);
            TaskDefinition preTaskDefinition = taskDefinitionMapper.queryByCode(preTaskCode);
            if (upstreamProcessTaskRelations.isEmpty()) {
                ProcessTaskRelationLog processTaskRelationLog = setRelationLog(processDefinition, now, loginUser.getId(), preTaskDefinition);
                processTaskRelationLog.setPreTaskCode(0L);
                processTaskRelationLog.setPreTaskVersion(0);
                processTaskRelationLogs.add(processTaskRelationLog);
            }
            TaskDefinition postTaskDefinition = taskDefinitionMapper.queryByCode(postTaskCode);
            ProcessTaskRelationLog processTaskRelationLog = setRelationLog(processDefinition, now, loginUser.getId(), postTaskDefinition);
            processTaskRelationLog.setPreTaskCode(preTaskDefinition.getCode());
            processTaskRelationLog.setPreTaskVersion(preTaskDefinition.getVersion());
            processTaskRelationLogs.add(processTaskRelationLog);
        } else {
            TaskDefinition postTaskDefinition = taskDefinitionMapper.queryByCode(postTaskCode);
            ProcessTaskRelationLog processTaskRelationLog = setRelationLog(processDefinition, now, loginUser.getId(), postTaskDefinition);
            processTaskRelationLog.setPreTaskCode(0L);
            processTaskRelationLog.setPreTaskVersion(0);
            processTaskRelationLogs.add(processTaskRelationLog);
        }
        int insert = processTaskRelationMapper.batchInsert(processTaskRelationLogs);
        int insertLog = processTaskRelationLogMapper.batchInsert(processTaskRelationLogs);
        if ((insert & insertLog) > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.CREATE_PROCESS_TASK_RELATION_ERROR);
            throw new ServiceException(Status.CREATE_PROCESS_TASK_RELATION_ERROR);
        }
        return result;
    }

    private ProcessTaskRelationLog setRelationLog(ProcessDefinition processDefinition, Date now, int userId, TaskDefinition taskDefinition) {
        ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
        processTaskRelationLog.setProjectCode(processDefinition.getProjectCode());
        processTaskRelationLog.setProcessDefinitionCode(processDefinition.getCode());
        processTaskRelationLog.setProcessDefinitionVersion(processDefinition.getVersion());
        processTaskRelationLog.setPostTaskCode(taskDefinition.getCode());
        processTaskRelationLog.setPostTaskVersion(taskDefinition.getVersion());
        processTaskRelationLog.setConditionType(ConditionType.NONE);
        processTaskRelationLog.setConditionParams("{}");
        processTaskRelationLog.setCreateTime(now);
        processTaskRelationLog.setUpdateTime(now);
        processTaskRelationLog.setOperator(userId);
        processTaskRelationLog.setOperateTime(now);
        return processTaskRelationLog;
    }

    /**
     * move task to other processDefinition
     *
     * @param loginUser                   login user info
     * @param projectCode                 project code
     * @param processDefinitionCode       process definition code
     * @param targetProcessDefinitionCode target process definition code
     * @param taskCode                    the current task code (the post task code)
     * @return move result code
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> moveTaskProcessRelation(User loginUser, long projectCode, long processDefinitionCode, long targetProcessDefinitionCode, long taskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(targetProcessDefinitionCode);
        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, targetProcessDefinitionCode);
            return result;
        }
        if (processDefinition.getProjectCode() != projectCode) {
            putMsg(result, Status.PROJECT_PROCESS_NOT_MATCH);
            return result;
        }
        List<ProcessTaskRelation> downstreamList = processTaskRelationMapper.queryByCode(projectCode, processDefinitionCode, taskCode, 0L);
        if (CollectionUtils.isNotEmpty(downstreamList)) {
            Set<Long> postTaskCodes = downstreamList
                .stream()
                .map(ProcessTaskRelation::getPostTaskCode)
                .collect(Collectors.toSet());
            putMsg(result, Status.TASK_HAS_DOWNSTREAM, org.apache.commons.lang.StringUtils.join(postTaskCodes, ","));
            return result;
        }
        List<ProcessTaskRelation> upstreamList = processTaskRelationMapper.queryByCode(projectCode, processDefinitionCode, 0L, taskCode);
        if (upstreamList.isEmpty()) {
            putMsg(result, Status.PROCESS_TASK_RELATION_NOT_EXIST, "taskCode:" + taskCode);
            return result;
        } else {
            Set<Long> preTaskCodes = upstreamList
                .stream()
                .map(ProcessTaskRelation::getPreTaskCode)
                .collect(Collectors.toSet());
            if (preTaskCodes.size() > 1 || !preTaskCodes.contains(0L)) {
                putMsg(result, Status.TASK_HAS_UPSTREAM, org.apache.commons.lang.StringUtils.join(preTaskCodes, ","));
                return result;
            }
        }
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (null == taskDefinition) {
            putMsg(result, Status.DATA_IS_NULL, "taskDefinition");
            return result;
        }
        ObjectNode paramNode = JSONUtils.parseObject(taskDefinition.getTaskParams());
        if (TaskType.DEPENDENT.getDesc().equals(taskDefinition.getTaskType())) {
            Set<Long> depProcessDefinitionCodes = new HashSet<>();
            ObjectNode dependence = (ObjectNode) paramNode.get("dependence");
            ArrayNode dependTaskList = JSONUtils.parseArray(JSONUtils.toJsonString(dependence.get("dependTaskList")));
            for (int i = 0; i < dependTaskList.size(); i++) {
                ObjectNode dependTask = (ObjectNode) dependTaskList.path(i);
                ArrayNode dependItemList = JSONUtils.parseArray(JSONUtils.toJsonString(dependTask.get("dependItemList")));
                for (int j = 0; j < dependItemList.size(); j++) {
                    ObjectNode dependItem = (ObjectNode) dependItemList.path(j);
                    long definitionCode = dependItem.get("definitionCode").asLong();
                    depProcessDefinitionCodes.add(definitionCode);
                }
            }
            if (depProcessDefinitionCodes.contains(targetProcessDefinitionCode)) {
                putMsg(result, DATA_IS_NOT_VALID, "targetProcessDefinitionCode");
                return result;
            }
        }
        if (TaskType.SUB_PROCESS.getDesc().equals(taskDefinition.getTaskType())) {
            long subProcessDefinitionCode = paramNode.get("processDefinitionCode").asLong();
            if (targetProcessDefinitionCode == subProcessDefinitionCode) {
                putMsg(result, DATA_IS_NOT_VALID, "targetProcessDefinitionCode");
                return result;
            }
        }
        Date now = new Date();
        ProcessTaskRelation processTaskRelation = upstreamList.get(0);
        ProcessTaskRelationLog processTaskRelationLog = processTaskRelationLogMapper.queryRelationLogByRelation(processTaskRelation);
        processTaskRelation.setProcessDefinitionCode(processDefinition.getCode());
        processTaskRelation.setProcessDefinitionVersion(processDefinition.getVersion());
        processTaskRelation.setUpdateTime(now);
        processTaskRelationLog.setProcessDefinitionCode(processDefinition.getCode());
        processTaskRelationLog.setProcessDefinitionVersion(processDefinition.getVersion());
        processTaskRelationLog.setUpdateTime(now);
        processTaskRelationLog.setOperator(loginUser.getId());
        processTaskRelationLog.setOperateTime(now);
        int update = processTaskRelationMapper.updateById(processTaskRelation);
        int updateLog = processTaskRelationLogMapper.updateById(processTaskRelationLog);
        if (update == 0 || updateLog == 0) {
            putMsg(result, Status.MOVE_PROCESS_TASK_RELATION_ERROR);
            throw new ServiceException(Status.MOVE_PROCESS_TASK_RELATION_ERROR);
        } else {
            putMsg(result, Status.SUCCESS);
        }
        return result;
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
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, processDefinitionCode);
            return result;
        }
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (null == taskDefinition) {
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, taskCode);
            return result;
        }
        List<ProcessTaskRelation> downstreamList = processTaskRelationMapper.queryByCode(projectCode, processDefinitionCode, taskCode, 0L);
        if (CollectionUtils.isNotEmpty(downstreamList)) {
            Set<Long> postTaskCodes = downstreamList
                .stream()
                .map(ProcessTaskRelation::getPostTaskCode)
                .collect(Collectors.toSet());
            putMsg(result, Status.TASK_HAS_DOWNSTREAM, org.apache.commons.lang.StringUtils.join(postTaskCodes, ","));
            return result;
        }

        ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
        processTaskRelationLog.setProjectCode(projectCode);
        processTaskRelationLog.setPostTaskCode(taskCode);
        processTaskRelationLog.setPostTaskVersion(taskDefinition.getVersion());
        processTaskRelationLog.setProcessDefinitionCode(processDefinitionCode);
        processTaskRelationLog.setProcessDefinitionVersion(processDefinition.getVersion());
        int deleteRelation = processTaskRelationMapper.deleteRelation(processTaskRelationLog);
        int deleteRelationLog = processTaskRelationLogMapper.deleteRelation(processTaskRelationLog);
        if (0 == deleteRelation || 0 == deleteRelationLog) {
            putMsg(result, Status.DELETE_TASK_PROCESS_RELATION_ERROR);
            throw new ServiceException(Status.DELETE_TASK_PROCESS_RELATION_ERROR);
        }
        if (TaskType.CONDITIONS.getDesc().equals(taskDefinition.getTaskType())
            || TaskType.DEPENDENT.getDesc().equals(taskDefinition.getTaskType())
            || TaskType.SUB_PROCESS.getDesc().equals(taskDefinition.getTaskType())) {
            int deleteTaskDefinition = taskDefinitionMapper.deleteByCode(taskCode);
            if (0 == deleteTaskDefinition) {
                putMsg(result, Status.DELETE_TASK_DEFINE_BY_CODE_ERROR);
                throw new ServiceException(Status.DELETE_TASK_DEFINE_BY_CODE_ERROR);
            }
        }
        putMsg(result, Status.SUCCESS);
        return result;
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
        Status status = deleteUpstreamRelation(loginUser.getId(), projectCode,
            Lists.newArrayList(preTaskCodes.split(Constants.COMMA)).stream().map(Long::parseLong).distinct().toArray(Long[]::new), taskCode);
        if (status != Status.SUCCESS) {
            putMsg(result, status);
        }
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
        List<ProcessTaskRelation> processTaskRelationList = processTaskRelationMapper.queryDownstreamByCode(projectCode, taskCode);
        Map<Long, ProcessTaskRelationLog> taskRelationLogMap =
            processTaskRelationList.stream()
                .map(ProcessTaskRelationLog::new)
                .collect(Collectors.toMap(ProcessTaskRelationLog::getPostTaskCode, processTaskRelationLog -> processTaskRelationLog));
        Set<Long> postTaskCodesSet = Lists.newArrayList(postTaskCodes.split(Constants.COMMA)).stream().map(Long::parseLong).collect(Collectors.toSet());
        int delete = 0;
        int deleteLog = 0;
        for (long postTaskCode : postTaskCodesSet) {
            ProcessTaskRelationLog processTaskRelationLog = taskRelationLogMap.get(postTaskCode);
            if (processTaskRelationLog != null) {
                delete += processTaskRelationMapper.deleteRelation(processTaskRelationLog);
                deleteLog += processTaskRelationLogMapper.deleteRelation(processTaskRelationLog);
            }
        }
        if ((delete & deleteLog) == 0) {
            throw new ServiceException(Status.DELETE_TASK_PROCESS_RELATION_ERROR);
        } else {
            putMsg(result, Status.SUCCESS);
        }
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
    @Override
    public Map<String, Object> deleteEdge(User loginUser, long projectCode, long processDefinitionCode, long preTaskCode, long postTaskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        List<ProcessTaskRelation> processTaskRelationList = processTaskRelationMapper.queryByCode(projectCode, processDefinitionCode, preTaskCode, postTaskCode);
        if (CollectionUtils.isEmpty(processTaskRelationList)) {
            putMsg(result, Status.DATA_IS_NULL, "processTaskRelationList");
            return result;
        }
        if (processTaskRelationList.size() > 1) {
            putMsg(result, Status.DATA_IS_NOT_VALID, "processTaskRelationList");
            return result;
        }
        ProcessTaskRelation processTaskRelation = processTaskRelationList.get(0);
        int upstreamCount = processTaskRelationMapper.countByCode(projectCode, processTaskRelation.getProcessDefinitionCode(),
            0L, processTaskRelation.getPostTaskCode());

        if (upstreamCount == 0) {
            putMsg(result, Status.DATA_IS_NULL, "upstreamCount");
            return result;
        }
        if (upstreamCount > 1) {
            int delete = processTaskRelationMapper.deleteById(processTaskRelation.getId());
            if (delete == 0) {
                putMsg(result, Status.DELETE_EDGE_ERROR);
            }
            return result;
        }
        processTaskRelation.setPreTaskVersion(0);
        processTaskRelation.setPreTaskCode(0L);
        int update = processTaskRelationMapper.updateById(processTaskRelation);
        if (update == 0) {
            putMsg(result, Status.DELETE_EDGE_ERROR);
        }
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

    /**
     * delete upstream relation
     *
     * @param projectCode  project code
     * @param preTaskCodes pre task codes
     * @param taskCode     pre task code
     * @return status
     */
    private Status deleteUpstreamRelation(int userId, long projectCode, Long[] preTaskCodes, long taskCode) {
        List<ProcessTaskRelation> upstreamList = processTaskRelationMapper.queryUpstreamByCodes(projectCode, taskCode, preTaskCodes);
        if (CollectionUtils.isEmpty(upstreamList)) {
            return Status.SUCCESS;
        }
        List<ProcessTaskRelationLog> upstreamLogList = new ArrayList<>();
        Date now = new Date();
        for (ProcessTaskRelation processTaskRelation : upstreamList) {
            ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog(processTaskRelation);
            processTaskRelationLog.setOperator(userId);
            processTaskRelationLog.setOperateTime(now);
            processTaskRelationLog.setUpdateTime(now);
            upstreamLogList.add(processTaskRelationLog);
        }
        Map<Long, List<ProcessTaskRelationLog>> processTaskRelationListGroupByProcessDefinitionCode = upstreamLogList.stream()
            .collect(Collectors.groupingBy(ProcessTaskRelationLog::getProcessDefinitionCode));
        // count upstream relation group by process definition code
        List<Map<String, Long>> countListGroupByProcessDefinitionCode = processTaskRelationMapper
            .countUpstreamByCodeGroupByProcessDefinitionCode(projectCode, processTaskRelationListGroupByProcessDefinitionCode.keySet().toArray(new Long[0]), taskCode);

        List<ProcessTaskRelationLog> deletes = new ArrayList<>();
        List<ProcessTaskRelationLog> updates = new ArrayList<>();
        for (Map<String, Long> codeCountMap : countListGroupByProcessDefinitionCode) {
            long processDefinitionCode = codeCountMap.get("processDefinitionCode");
            long countValue = codeCountMap.get("countValue");
            List<ProcessTaskRelationLog> processTaskRelationLogList = processTaskRelationListGroupByProcessDefinitionCode.get(processDefinitionCode);
            if (countValue <= processTaskRelationLogList.size()) {
                ProcessTaskRelationLog processTaskRelationLog = processTaskRelationLogList.remove(0);
                if (processTaskRelationLog.getPreTaskCode() != 0) {
                    processTaskRelationLog.setPreTaskCode(0);
                    processTaskRelationLog.setPreTaskVersion(0);
                    updates.add(processTaskRelationLog);
                }
            }
            if (!processTaskRelationLogList.isEmpty()) {
                deletes.addAll(processTaskRelationLogList);
            }
        }
        deletes.addAll(updates);
        int delete = 0;
        int deleteLog = 0;
        for (ProcessTaskRelationLog processTaskRelationLog : deletes) {
            delete += processTaskRelationMapper.deleteRelation(processTaskRelationLog);
            deleteLog += processTaskRelationLogMapper.deleteRelation(processTaskRelationLog);
        }
        if ((delete & deleteLog) == 0) {
            throw new ServiceException(Status.DELETE_TASK_PROCESS_RELATION_ERROR);
        } else {
            if (!updates.isEmpty()) {
                int insert = processTaskRelationMapper.batchInsert(updates);
                int insertLog = processTaskRelationLogMapper.batchInsert(updates);
                if ((insert & insertLog) == 0) {
                    throw new ServiceException(Status.CREATE_PROCESS_TASK_RELATION_ERROR);
                }
            }
        }
        return Status.SUCCESS;
    }
}
