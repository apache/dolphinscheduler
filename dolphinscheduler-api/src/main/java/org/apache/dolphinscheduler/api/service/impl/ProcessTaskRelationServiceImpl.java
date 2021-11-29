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
import org.apache.dolphinscheduler.api.service.ProcessTaskRelationService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.TaskType;
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
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCode processDefinitionCode
     * @param preTaskCode preTaskCode
     * @param postTaskCode postTaskCode
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
        processTaskRelationLog.setCreateTime(now);
        processTaskRelationLog.setUpdateTime(now);
        processTaskRelationLog.setOperator(userId);
        processTaskRelationLog.setOperateTime(now);
        return processTaskRelationLog;
    }

    /**
     * move task to other processDefinition
     *
     * @param loginUser login user info
     * @param projectCode project code
     * @param processDefinitionCode process definition code
     * @param targetProcessDefinitionCode target process definition code
     * @param taskCode the current task code (the post task code)
     * @return move result code
     */
    @Override
    public Map<String, Object> moveTaskProcessRelation(User loginUser, long projectCode, long processDefinitionCode, long targetProcessDefinitionCode, long taskCode) {
        return null;
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
        processTaskRelationLog.setPreTaskCode(taskCode);
        processTaskRelationLog.setProcessDefinitionCode(processDefinitionCode);
        int deleteRelation = processTaskRelationMapper.deleteRelation(processTaskRelationLog);
        if (0 == deleteRelation) {
            putMsg(result, Status.DELETE_TASK_PROCESS_RELATION_ERROR);
        }

        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (null == taskDefinition) {
            putMsg(result, Status.DATA_IS_NULL, "taskDefinition");
            return result;
        }
        if (TaskType.CONDITIONS.getDesc().equals(taskDefinition.getTaskType())
                || TaskType.DEPENDENT.getDesc().equals(taskDefinition.getTaskType())
                || TaskType.SUB_PROCESS.getDesc().equals(taskDefinition.getTaskType())) {
            int deleteTaskDefinition = taskDefinitionMapper.deleteByCode(taskCode);
            if (0 == deleteTaskDefinition) {
                putMsg(result, Status.DELETE_TASK_PROCESS_RELATION_ERROR);
            }
        }
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
    @Override
    public Map<String, Object> deleteUpstreamRelation(User loginUser, long projectCode, String preTaskCodes, long taskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        if (StringUtils.isEmpty(preTaskCodes)) {
            putMsg(result,Status.DATA_IS_NULL,"preTaskCodes");
            return result;
        }
        Set<Long> preTaskCodesSet = Lists.newArrayList(preTaskCodes.split(Constants.COMMA)).stream().map(Long::parseLong).collect(Collectors.toSet());
        Status status = deleteUpstreamRelation(projectCode, preTaskCodesSet.toArray(new Long[0]), taskCode);
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
    @Override
    public Map<String, Object> deleteDownstreamRelation(User loginUser, long projectCode, String postTaskCodes, long taskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        if (StringUtils.isEmpty(postTaskCodes)) {
            putMsg(result,Status.DATA_IS_NULL,"postTaskCodes");
            return result;
        }
        Set<Long> postTaskCodesSet = Lists.newArrayList(postTaskCodes.split(Constants.COMMA)).stream().map(Long::parseLong).collect(Collectors.toSet());
        List<Long> deleteFailedCodeList = new ArrayList<>();
        postTaskCodesSet.stream().forEach(
                postTaskCode -> {
                    try {
                        Status status = deleteUpstreamRelation(projectCode, new Long[]{taskCode}, postTaskCode);
                        if (Status.SUCCESS != status) {
                            deleteFailedCodeList.add(postTaskCode);
                        }
                    } catch (Exception e) {
                        deleteFailedCodeList.add(postTaskCode);
                    }

                }
        );
        if (!deleteFailedCodeList.isEmpty()) {
            putMsg(result, Status.DELETE_TASK_PROCESS_RELATION_ERROR, String.join(",", deleteFailedCodeList.stream().map(o -> o + "").collect(Collectors.toList())));
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
    private Status deleteUpstreamRelation(long projectCode, Long[] preTaskCodes, long taskCode) {
        List<ProcessTaskRelation> upstreamList = processTaskRelationMapper.queryUpstreamByCodes(projectCode, taskCode, preTaskCodes);
        if (CollectionUtils.isEmpty(upstreamList)) {
            return Status.SUCCESS;
        }
        Map<Long, List<ProcessTaskRelation>> processTaskRelationListGroupByProcessDefinitionCode = upstreamList.stream()
                .collect(Collectors.groupingBy(ProcessTaskRelation::getProcessDefinitionCode));
        // count upstream relation group by process definition code
        List<Map<Long, Integer>> countListGroupByProcessDefinitionCode = processTaskRelationMapper
                .countUpstreamByCodeGroupByProcessDefinitionCode(projectCode, processTaskRelationListGroupByProcessDefinitionCode.keySet().toArray(new Long[0]), taskCode);

        List<ProcessTaskRelation> deletes = new ArrayList<>();
        List<ProcessTaskRelation> updates = new ArrayList<>();

        countListGroupByProcessDefinitionCode.stream().forEach(
                processDefinitionCodeUpstreamCountMap ->
                        processDefinitionCodeUpstreamCountMap.entrySet().stream().forEach(
                                o -> {
                                    Long processDefinitionCode = o.getKey();
                                    Integer count = o.getValue();
                                    List<ProcessTaskRelation> processTaskRelationList = processTaskRelationListGroupByProcessDefinitionCode.get(processDefinitionCode);
                                    if (count <= processTaskRelationList.size()) {
                                        ProcessTaskRelation processTaskRelation = processTaskRelationList.remove(0);
                                        if (processTaskRelation.getPreTaskCode() != 0) {
                                            processTaskRelation.setPreTaskCode(0);
                                            processTaskRelation.setPreTaskVersion(0);
                                            updates.add(processTaskRelation);
                                        }
                                    }
                                    if (!processTaskRelationList.isEmpty()) {
                                        deletes.addAll(processTaskRelationList);
                                    }
                                }
                        )
        );

        int update = 0;
        if (!updates.isEmpty()) {
            update = processTaskRelationMapper.batchUpdateProcessTaskRelationPreTask(updates);
        }
        int delete = 0;
        if (!deletes.isEmpty()) {
            delete = processTaskRelationMapper.deleteBatchIds(deletes.stream().map(ProcessTaskRelation::getId).collect(Collectors.toList()));
        }
        if (update < 0 || delete < 0) {
            return Status.DELETE_TASK_PROCESS_RELATION_ERROR;
        }
        return Status.SUCCESS;
    }
}
