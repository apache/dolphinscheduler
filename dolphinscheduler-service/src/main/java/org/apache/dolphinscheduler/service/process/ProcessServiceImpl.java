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

package org.apache.dolphinscheduler.service.process;

import static java.util.stream.Collectors.toSet;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_SUB_WORKFLOW_DEFINITION_CODE;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Cluster;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstanceRelation;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.dao.mapper.ClusterMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowInstanceRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationMapper;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SubWorkflowParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.TaskTimeoutParameter;
import org.apache.dolphinscheduler.service.expand.CuringParamsService;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.utils.ClusterConfUtils;
import org.apache.dolphinscheduler.service.utils.DagHelper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * process relative dao that some mappers in this.
 */
@Component
@Slf4j
public class ProcessServiceImpl implements ProcessService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Autowired
    private WorkflowDefinitionLogMapper workflowDefinitionLogMapper;

    // todo replace with workflowInstanceDao
    @Autowired
    private WorkflowInstanceMapper workflowInstanceMapper;

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private TaskDefinitionLogDao taskDefinitionLogDao;

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private WorkflowInstanceRelationMapper workflowInstanceRelationMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    private WorkflowTaskRelationMapper workflowTaskRelationMapper;

    @Autowired
    private WorkflowTaskRelationLogMapper workflowTaskRelationLogMapper;

    @Autowired
    private ClusterMapper clusterMapper;

    @Autowired
    private CuringParamsService curingGlobalParamsService;

    /**
     * find workflow instance detail by id
     *
     * @param workflowInstanceId workflowInstanceId
     * @return workflow instance
     */
    @Override
    public Optional<WorkflowInstance> findWorkflowInstanceDetailById(int workflowInstanceId) {
        return Optional.ofNullable(workflowInstanceMapper.queryDetailById(workflowInstanceId));
    }

    /**
     * find workflow instance by id
     *
     * @param workflowInstanceId workflowInstanceId
     * @return workflow instance
     */
    @Override
    public WorkflowInstance findWorkflowInstanceById(int workflowInstanceId) {
        return workflowInstanceMapper.selectById(workflowInstanceId);
    }

    /**
     * find workflow define by code and version.
     *
     * @param workflowDefinitionCode workflowDefinitionCode
     * @return workflow definition
     */
    @Override
    public WorkflowDefinition findWorkflowDefinition(Long workflowDefinitionCode, int workflowDefinitionVersion) {
        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(workflowDefinitionCode);
        if (workflowDefinition == null || workflowDefinition.getVersion() != workflowDefinitionVersion) {
            workflowDefinition = workflowDefinitionLogMapper.queryByDefinitionCodeAndVersion(workflowDefinitionCode,
                    workflowDefinitionVersion);
            if (workflowDefinition != null) {
                workflowDefinition.setId(0);
            }
        }
        return workflowDefinition;
    }

    /**
     * delete work workflow instance by id
     *
     * @param workflowInstanceId workflowInstanceId
     * @return delete workflow instance result
     */
    @Override
    public int deleteWorkflowInstanceById(int workflowInstanceId) {
        return workflowInstanceMapper.deleteById(workflowInstanceId);
    }

    /**
     * recursive query sub workflow definition id by parent id.
     *
     * @param parentCode parentCode
     */
    @Override
    public List<Long> findAllSubWorkflowDefinitionCode(long parentCode) {
        List<TaskDefinition> taskNodeList = taskDefinitionDao.getTaskDefinitionListByDefinition(parentCode);
        if (CollectionUtils.isEmpty(taskNodeList)) {
            return Collections.emptyList();
        }
        List<Long> subWorkflowDefinitionCodes = new ArrayList<>();

        for (TaskDefinition taskNode : taskNodeList) {
            String parameter = taskNode.getTaskParams();
            ObjectNode parameterJson = JSONUtils.parseObject(parameter);
            if (parameterJson.get(CMD_PARAM_SUB_WORKFLOW_DEFINITION_CODE) != null) {
                SubWorkflowParameters subProcessParam = JSONUtils.parseObject(parameter, SubWorkflowParameters.class);
                long subWorkflowDefinitionCode = subProcessParam.getWorkflowDefinitionCode();
                subWorkflowDefinitionCodes.add(subWorkflowDefinitionCode);
                subWorkflowDefinitionCodes.addAll(findAllSubWorkflowDefinitionCode(subWorkflowDefinitionCode));
            }
        }
        return subWorkflowDefinitionCodes;
    }

    /**
     * Get workflow runtime tenant
     * <p>
     * the workflow provides a tenant and uses the provided tenant;
     * when no tenant is provided or the provided tenant is the default tenant, \
     * the user's tenant created by the workflow is used
     *
     * @param tenantCode tenantCode
     * @param userId     userId
     * @return tenant code
     */
    @Override
    public String getTenantForWorkflow(String tenantCode, int userId) {
        if (StringUtils.isNoneBlank(tenantCode) && !Constants.DEFAULT.equals(tenantCode)) {
            return tenantCode;
        }

        if (userId == 0) {
            return null;
        }

        User user = userMapper.selectById(userId);
        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        return tenant.getTenantCode();
    }

    /**
     * find sub workflow instance
     *
     * @param parentWorkflowInstanceId parentWorkflowInstanceId
     * @param parentTaskId    parentTaskId
     * @return workflow instance
     */
    @Override
    public WorkflowInstance findSubWorkflowInstance(Integer parentWorkflowInstanceId, Integer parentTaskId) {
        WorkflowInstance workflowInstance = null;
        WorkflowInstanceRelation workflowInstanceRelation =
                workflowInstanceRelationMapper.queryByParentId(parentWorkflowInstanceId, parentTaskId);
        if (workflowInstanceRelation == null || workflowInstanceRelation.getWorkflowInstanceId() == 0) {
            return workflowInstance;
        }
        workflowInstance = findWorkflowInstanceById(workflowInstanceRelation.getWorkflowInstanceId());
        return workflowInstance;
    }

    /**
     * find parent workflow instance
     *
     * @param subWorkflowInstanceId subWorkflowId
     * @return workflow instance
     */
    @Override
    public WorkflowInstance findParentWorkflowInstance(Integer subWorkflowInstanceId) {
        WorkflowInstance workflowInstance = null;
        WorkflowInstanceRelation workflowInstanceRelation =
                workflowInstanceRelationMapper.queryBySubWorkflowId(subWorkflowInstanceId);
        if (workflowInstanceRelation == null || workflowInstanceRelation.getWorkflowInstanceId() == 0) {
            return workflowInstance;
        }
        workflowInstance = findWorkflowInstanceById(workflowInstanceRelation.getParentWorkflowInstanceId());
        return workflowInstance;
    }

    /**
     * query Schedule by workflowDefinitionCode
     *
     * @param workflowDefinitionCode workflowDefinitionCode
     * @see Schedule
     */
    @Override
    public List<Schedule> queryReleaseSchedulerListByWorkflowDefinitionCode(long workflowDefinitionCode) {
        return scheduleMapper.queryReleaseSchedulerListByWorkflowDefinitionCode(workflowDefinitionCode);
    }

    /**
     * find data source by id
     *
     * @param id id
     * @return datasource
     */
    @Override
    public DataSource findDataSourceById(int id) {
        return dataSourceMapper.selectById(id);
    }

    /**
     * get user by user id
     *
     * @param userId user id
     * @return User
     */
    @Override
    public User getUserById(int userId) {
        return userMapper.selectById(userId);
    }

    /**
     * list unauthorized
     *
     * @param userId     user id
     * @param needChecks data source id array
     * @return unauthorized
     */
    @Override
    public <T> List<T> listUnauthorized(int userId, T[] needChecks, AuthorizationType authorizationType) {
        List<T> resultList = new ArrayList<>();

        if (Objects.nonNull(needChecks) && needChecks.length > 0) {
            Set<T> originResSet = new HashSet<>(Arrays.asList(needChecks));

            switch (authorizationType) {
                case DATASOURCE:
                    Set<Integer> authorizedDatasources = dataSourceMapper.listAuthorizedDataSource(userId, needChecks)
                            .stream().map(DataSource::getId).collect(toSet());
                    originResSet.removeAll(authorizedDatasources);
                    break;
                default:
                    break;
            }

            resultList.addAll(originResSet);
        }

        return resultList;
    }

    /**
     * switch workflow definition version to workflow definition log version
     */
    @Override
    public int switchVersion(WorkflowDefinition workflowDefinition, WorkflowDefinitionLog workflowDefinitionLog) {
        if (null == workflowDefinition || null == workflowDefinitionLog) {
            return Constants.DEFINITION_FAILURE;
        }
        workflowDefinitionLog.setId(workflowDefinition.getId());
        workflowDefinitionLog.setReleaseState(ReleaseState.OFFLINE);
        workflowDefinitionLog.setFlag(Flag.YES);

        int result = workflowDefinitionMapper.updateById(workflowDefinitionLog);
        if (result > 0) {
            result = switchWorkflowTaskRelationVersion(workflowDefinitionLog);
            if (result <= 0) {
                return Constants.EXIT_CODE_FAILURE;
            }
        }
        return result;
    }

    @Override
    public int switchWorkflowTaskRelationVersion(WorkflowDefinition workflowDefinition) {
        List<WorkflowTaskRelation> workflowTaskRelationList =
                workflowTaskRelationMapper.queryByWorkflowDefinitionCode(workflowDefinition.getCode());
        if (!workflowTaskRelationList.isEmpty()) {
            workflowTaskRelationMapper.deleteByWorkflowDefinitionCode(workflowDefinition.getProjectCode(),
                    workflowDefinition.getCode());
        }
        List<WorkflowTaskRelation> workflowTaskRelationListFromLog = workflowTaskRelationLogMapper
                .queryByWorkflowCodeAndVersion(workflowDefinition.getCode(), workflowDefinition.getVersion()).stream()
                .map(WorkflowTaskRelation::new).collect(Collectors.toList());
        int batchInsert = workflowTaskRelationMapper.batchInsert(workflowTaskRelationListFromLog);
        if (batchInsert == 0) {
            return Constants.EXIT_CODE_FAILURE;
        } else {
            int result = 0;
            for (WorkflowTaskRelation taskRelation : workflowTaskRelationListFromLog) {
                int switchResult = switchTaskDefinitionVersion(taskRelation.getPostTaskCode(),
                        taskRelation.getPostTaskVersion());
                if (switchResult != Constants.EXIT_CODE_FAILURE) {
                    result++;
                }
            }
            return result;
        }
    }

    @Override
    public int switchTaskDefinitionVersion(long taskCode, int taskVersion) {
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (taskDefinition == null) {
            return Constants.EXIT_CODE_FAILURE;
        }
        if (taskDefinition.getVersion() == taskVersion) {
            return Constants.EXIT_CODE_SUCCESS;
        }
        TaskDefinitionLog taskDefinitionUpdate =
                taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(taskCode, taskVersion);
        if (taskDefinitionUpdate == null) {
            return Constants.EXIT_CODE_FAILURE;
        }
        taskDefinitionUpdate.setUpdateTime(new Date());
        taskDefinitionUpdate.setId(taskDefinition.getId());
        return taskDefinitionMapper.updateById(taskDefinitionUpdate);
    }

    @Override
    public int saveTaskDefine(User operator, long projectCode, List<TaskDefinitionLog> taskDefinitionLogs,
                              Boolean syncDefine) {
        Date now = new Date();
        List<TaskDefinitionLog> newTaskDefinitionLogs = new ArrayList<>();
        List<TaskDefinitionLog> updateTaskDefinitionLogs = new ArrayList<>();
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
            taskDefinitionLog.setProjectCode(projectCode);
            taskDefinitionLog.setUpdateTime(now);
            taskDefinitionLog.setOperateTime(now);
            taskDefinitionLog.setOperator(operator.getId());
            if (taskDefinitionLog.getCode() == 0) {
                taskDefinitionLog.setCode(CodeGenerateUtils.genCode());
            }
            if (taskDefinitionLog.getVersion() == 0) {
                // init first version
                taskDefinitionLog.setVersion(Constants.VERSION_FIRST);
            }

            TaskDefinitionLog definitionCodeAndVersion = taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(
                    taskDefinitionLog.getCode(), taskDefinitionLog.getVersion());
            if (definitionCodeAndVersion == null) {
                taskDefinitionLog.setUserId(operator.getId());
                taskDefinitionLog.setCreateTime(now);
                newTaskDefinitionLogs.add(taskDefinitionLog);
                continue;
            }
            if (taskDefinitionLog.equals(definitionCodeAndVersion)) {
                // do nothing if equals
                continue;
            }
            taskDefinitionLog.setUserId(definitionCodeAndVersion.getUserId());
            Integer version = taskDefinitionLogMapper.queryMaxVersionForDefinition(taskDefinitionLog.getCode());
            taskDefinitionLog.setVersion(version + 1);
            taskDefinitionLog.setCreateTime(definitionCodeAndVersion.getCreateTime());
            updateTaskDefinitionLogs.add(taskDefinitionLog);
        }

        if (CollectionUtils.isNotEmpty(updateTaskDefinitionLogs)) {
            List<Long> taskDefinitionCodes = updateTaskDefinitionLogs
                    .stream()
                    .map(TaskDefinition::getCode)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, TaskDefinition> taskDefinitionMap = taskDefinitionMapper.queryByCodeList(taskDefinitionCodes)
                    .stream()
                    .collect(Collectors.toMap(TaskDefinition::getCode, Function.identity()));
            for (TaskDefinitionLog taskDefinitionToUpdate : updateTaskDefinitionLogs) {
                TaskDefinition task = taskDefinitionMap.get(taskDefinitionToUpdate.getCode());
                if (task == null) {
                    newTaskDefinitionLogs.add(taskDefinitionToUpdate);
                } else {
                    taskDefinitionToUpdate.setId(task.getId());
                }
            }
        }

        // for each taskDefinitionLog, we will insert a new version into db
        // and update the origin one if exist
        int updateResult = 0;
        int insertResult = 0;

        // only insert new task definitions if they not in updateTaskDefinitionLogs
        List<TaskDefinitionLog> newInsertTaskDefinitionLogs = newTaskDefinitionLogs.stream()
                .filter(taskDefinitionLog -> !updateTaskDefinitionLogs.contains(taskDefinitionLog))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(newInsertTaskDefinitionLogs)) {
            insertResult = taskDefinitionLogMapper.batchInsert(newInsertTaskDefinitionLogs);
        }
        if (CollectionUtils.isNotEmpty(updateTaskDefinitionLogs)) {
            insertResult += taskDefinitionLogMapper.batchInsert(updateTaskDefinitionLogs);
        }

        if (CollectionUtils.isNotEmpty(newTaskDefinitionLogs) && Boolean.TRUE.equals(syncDefine)) {
            updateResult += taskDefinitionMapper.batchInsert(newTaskDefinitionLogs);
        }
        if (CollectionUtils.isNotEmpty(updateTaskDefinitionLogs) && Boolean.TRUE.equals(syncDefine)) {
            for (TaskDefinitionLog taskDefinitionLog : updateTaskDefinitionLogs) {
                updateResult += taskDefinitionMapper.updateById(taskDefinitionLog);
            }
        }

        return (insertResult & updateResult) > 0 ? 1 : Constants.EXIT_CODE_SUCCESS;
    }

    /**
     * save workflowDefinition (including create or update workflowDefinition)
     */
    @Override
    public int saveWorkflowDefine(User operator, WorkflowDefinition workflowDefinition, Boolean syncDefine,
                                  Boolean isFromWorkflowDefinition) {
        WorkflowDefinitionLog workflowDefinitionLog = new WorkflowDefinitionLog(workflowDefinition);
        Integer version = workflowDefinitionLogMapper.queryMaxVersionForDefinition(workflowDefinition.getCode());
        int insertVersion = version == null || version == 0 ? Constants.VERSION_FIRST : version + 1;
        workflowDefinitionLog.setVersion(insertVersion);
        workflowDefinitionLog
                .setReleaseState(
                        !isFromWorkflowDefinition || workflowDefinitionLog.getReleaseState() == ReleaseState.ONLINE
                                ? ReleaseState.ONLINE
                                : ReleaseState.OFFLINE);
        workflowDefinitionLog.setOperator(operator.getId());
        workflowDefinitionLog.setOperateTime(workflowDefinition.getUpdateTime());
        workflowDefinitionLog.setId(null);
        int insertLog = workflowDefinitionLogMapper.insert(workflowDefinitionLog);
        int result = 1;
        if (Boolean.TRUE.equals(syncDefine)) {
            if (workflowDefinition.getId() == null) {
                result = workflowDefinitionMapper.insert(workflowDefinitionLog);
                workflowDefinition.setId(workflowDefinitionLog.getId());
            } else {
                workflowDefinitionLog.setId(workflowDefinition.getId());
                result = workflowDefinitionMapper.updateById(workflowDefinitionLog);
            }
        }
        return (insertLog & result) > 0 ? insertVersion : 0;
    }

    /**
     * save task relations
     */
    @Override
    public int saveTaskRelation(User operator, long projectCode, long workflowDefinitionCode,
                                int workflowDefinitionVersion,
                                List<WorkflowTaskRelationLog> taskRelationList,
                                List<TaskDefinitionLog> taskDefinitionLogs,
                                Boolean syncDefine) {
        if (taskRelationList.isEmpty()) {
            return Constants.EXIT_CODE_SUCCESS;
        }
        Map<Long, TaskDefinitionLog> taskDefinitionLogMap = null;
        if (CollectionUtils.isNotEmpty(taskDefinitionLogs)) {
            taskDefinitionLogMap = taskDefinitionLogs
                    .stream()
                    .collect(Collectors.toMap(TaskDefinition::getCode, taskDefinitionLog -> taskDefinitionLog));
        }
        Date now = new Date();
        for (WorkflowTaskRelationLog workflowTaskRelationLog : taskRelationList) {
            workflowTaskRelationLog.setProjectCode(projectCode);
            workflowTaskRelationLog.setWorkflowDefinitionCode(workflowDefinitionCode);
            workflowTaskRelationLog.setWorkflowDefinitionVersion(workflowDefinitionVersion);
            if (taskDefinitionLogMap != null) {
                TaskDefinitionLog preTaskDefinitionLog =
                        taskDefinitionLogMap.get(workflowTaskRelationLog.getPreTaskCode());
                if (preTaskDefinitionLog != null) {
                    workflowTaskRelationLog.setPreTaskVersion(preTaskDefinitionLog.getVersion());
                }
                TaskDefinitionLog postTaskDefinitionLog =
                        taskDefinitionLogMap.get(workflowTaskRelationLog.getPostTaskCode());
                if (postTaskDefinitionLog != null) {
                    workflowTaskRelationLog.setPostTaskVersion(postTaskDefinitionLog.getVersion());
                }
            }
            workflowTaskRelationLog.setCreateTime(now);
            workflowTaskRelationLog.setUpdateTime(now);
            workflowTaskRelationLog.setOperator(operator.getId());
            workflowTaskRelationLog.setOperateTime(now);
        }
        int insert = taskRelationList.size();
        if (Boolean.TRUE.equals(syncDefine)) {
            List<WorkflowTaskRelation> workflowTaskRelationList =
                    workflowTaskRelationMapper.queryByWorkflowDefinitionCode(workflowDefinitionCode);
            if (!workflowTaskRelationList.isEmpty()) {
                Set<Integer> workflowTaskRelationSet =
                        workflowTaskRelationList.stream().map(WorkflowTaskRelation::hashCode).collect(toSet());
                Set<Integer> taskRelationSet =
                        taskRelationList.stream().map(WorkflowTaskRelationLog::hashCode).collect(toSet());
                boolean result = CollectionUtils.isEqualCollection(workflowTaskRelationSet, taskRelationSet);
                if (result) {
                    return Constants.EXIT_CODE_SUCCESS;
                }
                workflowTaskRelationMapper.deleteByWorkflowDefinitionCode(projectCode, workflowDefinitionCode);
            }
            List<WorkflowTaskRelation> workflowTaskRelations =
                    taskRelationList.stream().map(WorkflowTaskRelation::new).collect(Collectors.toList());
            insert = workflowTaskRelationMapper.batchInsert(workflowTaskRelations);
        }
        int resultLog = workflowTaskRelationLogMapper.batchInsert(taskRelationList);
        return (insert & resultLog) > 0 ? Constants.EXIT_CODE_SUCCESS : Constants.EXIT_CODE_FAILURE;
    }

    @Override
    public boolean isTaskOnline(long taskCode) {
        List<WorkflowTaskRelation> workflowTaskRelationList = workflowTaskRelationMapper.queryByTaskCode(taskCode);
        if (!workflowTaskRelationList.isEmpty()) {
            Set<Long> processDefinitionCodes = workflowTaskRelationList
                    .stream()
                    .map(WorkflowTaskRelation::getWorkflowDefinitionCode)
                    .collect(toSet());
            List<WorkflowDefinition> workflowDefinitionList =
                    workflowDefinitionMapper.queryByCodes(processDefinitionCodes);
            // check process definition is already online
            for (WorkflowDefinition workflowDefinition : workflowDefinitionList) {
                if (workflowDefinition.getReleaseState() == ReleaseState.ONLINE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generate the DAG Graph based on the workflow definition id
     * Use temporarily before refactoring taskNode
     *
     * @param workflowDefinition workflow definition
     * @return dag graph
     */
    @Override
    public DAG<Long, TaskNode, TaskNodeRelation> genDagGraph(WorkflowDefinition workflowDefinition) {
        List<WorkflowTaskRelation> taskRelations =
                this.findRelationByCode(workflowDefinition.getCode(), workflowDefinition.getVersion());
        List<TaskNode> taskNodeList = transformTask(taskRelations, Lists.newArrayList());
        WorkflowDag workflowDag = DagHelper.getWorkflowDag(taskNodeList, new ArrayList<>(taskRelations));
        // Generate concrete Dag to be executed
        return DagHelper.buildDagGraph(workflowDag);
    }

    /**
     * generate DagData
     */
    @Override
    public DagData genDagData(WorkflowDefinition workflowDefinition) {
        List<WorkflowTaskRelation> taskRelations =
                findRelationByCode(workflowDefinition.getCode(), workflowDefinition.getVersion());
        List<TaskDefinition> taskDefinitions = taskDefinitionLogDao.queryTaskDefineLogList(taskRelations)
                .stream()
                .map(t -> (TaskDefinition) t)
                .collect(Collectors.toList());
        return new DagData(workflowDefinition, taskRelations, taskDefinitions);
    }

    /**
     * find workflow task relation list by workflow
     */
    @Override
    public List<WorkflowTaskRelation> findRelationByCode(long workflowDefinitionCode, int workflowDefinitionVersion) {
        List<WorkflowTaskRelationLog> workflowTaskRelationLogList = workflowTaskRelationLogMapper
                .queryByWorkflowCodeAndVersion(workflowDefinitionCode, workflowDefinitionVersion);
        return workflowTaskRelationLogList.stream().map(r -> (WorkflowTaskRelation) r).collect(Collectors.toList());
    }

    /**
     * Use temporarily before refactoring taskNode
     */
    @Override
    public List<TaskNode> transformTask(List<WorkflowTaskRelation> taskRelationList,
                                        List<TaskDefinitionLog> taskDefinitionLogs) {
        Map<Long, List<Long>> taskCodeMap = new HashMap<>();
        for (WorkflowTaskRelation workflowTaskRelation : taskRelationList) {
            taskCodeMap.compute(workflowTaskRelation.getPostTaskCode(), (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }
                if (workflowTaskRelation.getPreTaskCode() != 0L) {
                    v.add(workflowTaskRelation.getPreTaskCode());
                }
                return v;
            });
        }
        if (CollectionUtils.isEmpty(taskDefinitionLogs)) {
            taskDefinitionLogs = taskDefinitionLogDao.queryTaskDefineLogList(taskRelationList);
        }
        Map<Long, TaskDefinitionLog> taskDefinitionLogMap = taskDefinitionLogs.stream()
                .collect(Collectors.toMap(TaskDefinitionLog::getCode, taskDefinitionLog -> taskDefinitionLog));
        List<TaskNode> taskNodeList = new ArrayList<>();
        for (Entry<Long, List<Long>> code : taskCodeMap.entrySet()) {
            TaskDefinitionLog taskDefinitionLog = taskDefinitionLogMap.get(code.getKey());
            if (taskDefinitionLog != null) {
                TaskNode taskNode = new TaskNode();
                taskNode.setCode(taskDefinitionLog.getCode());
                taskNode.setVersion(taskDefinitionLog.getVersion());
                taskNode.setName(taskDefinitionLog.getName());
                taskNode.setDesc(taskDefinitionLog.getDescription());
                taskNode.setType(taskDefinitionLog.getTaskType().toUpperCase());
                taskNode.setRunFlag(taskDefinitionLog.getFlag() == Flag.YES ? Constants.FLOWNODE_RUN_FLAG_NORMAL
                        : Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
                taskNode.setMaxRetryTimes(taskDefinitionLog.getFailRetryTimes());
                taskNode.setRetryInterval(taskDefinitionLog.getFailRetryInterval());
                taskNode.setParams(taskDefinitionLog.getTaskParams());
                taskNode.setTaskInstancePriority(taskDefinitionLog.getTaskPriority());
                taskNode.setWorkerGroup(taskDefinitionLog.getWorkerGroup());
                taskNode.setEnvironmentCode(taskDefinitionLog.getEnvironmentCode());
                taskNode.setTimeout(JSONUtils
                        .toJsonString(new TaskTimeoutParameter(taskDefinitionLog.getTimeoutFlag() == TimeoutFlag.OPEN,
                                taskDefinitionLog.getTimeoutNotifyStrategy(),
                                taskDefinitionLog.getTimeout())));
                taskNode.setDelayTime(taskDefinitionLog.getDelayTime());
                taskNode.setPreTasks(JSONUtils.toJsonString(code.getValue().stream().map(taskDefinitionLogMap::get)
                        .map(TaskDefinition::getCode).collect(Collectors.toList())));
                taskNode.setTaskGroupId(taskDefinitionLog.getTaskGroupId());
                taskNode.setTaskGroupPriority(taskDefinitionLog.getTaskGroupPriority());
                taskNode.setCpuQuota(taskDefinitionLog.getCpuQuota());
                taskNode.setMemoryMax(taskDefinitionLog.getMemoryMax());
                taskNode.setTaskExecuteType(taskDefinitionLog.getTaskExecuteType());
                taskNodeList.add(taskNode);
            }
        }
        return taskNodeList;
    }

    /**
     * find k8s config yaml by clusterName
     *
     * @param clusterName clusterName
     * @return datasource
     */

    @Override
    public String findConfigYamlByName(String clusterName) {
        if (Strings.isNullOrEmpty(clusterName)) {
            return null;
        }

        QueryWrapper<Cluster> nodeWrapper = new QueryWrapper<>();
        nodeWrapper.eq("name", clusterName);
        Cluster cluster = clusterMapper.selectOne(nodeWrapper);
        return cluster == null ? null : ClusterConfUtils.getK8sConfig(cluster.getConfig());
    }

    @Override
    public void forceWorkflowInstanceSuccessByTaskInstanceId(TaskInstance task) {
        WorkflowInstance workflowInstance = findWorkflowInstanceDetailById(task.getWorkflowInstanceId()).orElse(null);
        if (workflowInstance != null
                && (workflowInstance.getState().isFailure() || workflowInstance.getState().isStopped())) {
            List<TaskInstance> validTaskList =
                    taskInstanceDao.queryValidTaskListByWorkflowInstanceId(workflowInstance.getId());
            List<Long> instanceTaskCodeList =
                    validTaskList.stream().map(TaskInstance::getTaskCode).collect(Collectors.toList());
            List<WorkflowTaskRelation> taskRelations = findRelationByCode(workflowInstance.getWorkflowDefinitionCode(),
                    workflowInstance.getWorkflowDefinitionVersion());
            List<TaskDefinitionLog> taskDefinitionLogs = taskDefinitionLogDao.queryTaskDefineLogList(taskRelations);
            List<Long> definiteTaskCodeList =
                    taskDefinitionLogs.stream().filter(definitionLog -> definitionLog.getFlag() == Flag.YES)
                            .map(TaskDefinitionLog::getCode).collect(Collectors.toList());
            // only all tasks have instances
            if (CollectionUtils.isEqualCollection(instanceTaskCodeList,
                    definiteTaskCodeList)) {
                List<Integer> failTaskList = validTaskList.stream()
                        .filter(instance -> instance.getState().isFailure() || instance.getState().isKill())
                        .map(TaskInstance::getId).collect(Collectors.toList());
                if (failTaskList.size() == 1 && failTaskList.contains(task.getId())) {
                    workflowInstance.setStateWithDesc(WorkflowExecutionStatus.SUCCESS, "success by task force success");
                    workflowInstanceDao.updateById(workflowInstance);
                }
            }
        }
    }

}
