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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_INSTANCE;
import static org.apache.dolphinscheduler.api.enums.Status.WORKFLOW_INSTANCE_NOT_EXIST;
import static org.apache.dolphinscheduler.api.enums.Status.WORKFLOW_INSTANCE_STATE_OPERATION_ERROR;
import static org.apache.dolphinscheduler.common.constants.Constants.DATA_LIST;
import static org.apache.dolphinscheduler.common.constants.Constants.GLOBAL_PARAMS;
import static org.apache.dolphinscheduler.common.constants.Constants.LOCAL_PARAMS;
import static org.apache.dolphinscheduler.common.constants.Constants.TASK_LIST;
import static org.apache.dolphinscheduler.common.constants.Constants.WORKFLOW_INSTANCE_STATE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager.checkTaskParameters;

import org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant;
import org.apache.dolphinscheduler.api.dto.DynamicSubWorkflowDto;
import org.apache.dolphinscheduler.api.dto.gantt.GanttDto;
import org.apache.dolphinscheduler.api.dto.gantt.Task;
import org.apache.dolphinscheduler.api.dto.workflowInstance.WorkflowInstanceQueryRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.LoggerService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.TaskInstanceService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.service.WorkflowDefinitionService;
import org.apache.dolphinscheduler.api.service.WorkflowInstanceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.CommandKeyConstants;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ContextType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.placeholder.BusinessTimeUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.AbstractTaskInstanceContext;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.RelationSubWorkflow;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstanceContext;
import org.apache.dolphinscheduler.dao.entity.TaskInstanceDependentDetails;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.RelationSubWorkflowMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowInstanceMapper;
import org.apache.dolphinscheduler.dao.model.ITaskInstanceContext;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceContextDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceMapDao;
import org.apache.dolphinscheduler.dao.utils.WorkflowUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.service.expand.CuringParamsService;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
@Slf4j
public class WorkflowInstanceServiceImpl extends BaseServiceImpl implements WorkflowInstanceService {

    public static final String TASK_TYPE = "taskType";

    public static final String LOCAL_PARAMS_LIST = "localParamsList";

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    ProjectService projectService;

    @Autowired
    ProcessService processService;

    @Autowired
    TaskInstanceDao taskInstanceDao;

    @Lazy
    @Autowired
    private TaskInstanceService taskInstanceService;

    @Autowired
    WorkflowInstanceMapper workflowInstanceMapper;

    @Autowired
    WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private WorkflowInstanceMapDao workflowInstanceMapDao;

    @Autowired
    WorkflowDefinitionMapper workflowDefinitionMapper;

    @Autowired
    WorkflowDefinitionService workflowDefinitionService;

    @Autowired
    ExecutorService execService;

    @Autowired
    TaskInstanceMapper taskInstanceMapper;

    @Autowired
    LoggerService loggerService;

    @Autowired
    WorkflowDefinitionLogMapper workflowDefinitionLogMapper;

    @Autowired
    TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    UsersService usersService;

    @Autowired
    TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private RelationSubWorkflowMapper relationSubWorkflowMapper;

    @Autowired
    private AlertDao alertDao;

    @Autowired
    private CuringParamsService curingGlobalParamsService;

    @Autowired
    private TaskInstanceContextDao taskInstanceContextDao;

    /**
     * return top n SUCCESS workflow instance order by running time which started between startTime and endTime
     */
    @Override
    public Map<String, Object> queryTopNLongestRunningWorkflowInstance(User loginUser, long projectCode, int size,
                                                                       String startTime, String endTime) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode,
                        WORKFLOW_INSTANCE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        if (0 > size) {
            putMsg(result, Status.NEGTIVE_SIZE_NUMBER_ERROR, size);
            return result;
        }
        if (Objects.isNull(startTime)) {
            putMsg(result, Status.DATA_IS_NULL, Constants.START_TIME);
            return result;
        }
        Date start = DateUtils.stringToDate(startTime);
        if (Objects.isNull(endTime)) {
            putMsg(result, Status.DATA_IS_NULL, Constants.END_TIME);
            return result;
        }
        Date end = DateUtils.stringToDate(endTime);
        if (start == null || end == null) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.START_END_DATE);
            return result;
        }
        if (start.getTime() > end.getTime()) {
            putMsg(result, Status.START_TIME_BIGGER_THAN_END_TIME_ERROR, startTime, endTime);
            return result;
        }

        List<WorkflowInstance> workflowInstances = workflowInstanceMapper.queryTopNWorkflowInstance(size, start, end,
                WorkflowExecutionStatus.SUCCESS, projectCode);
        result.put(DATA_LIST, workflowInstances);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query workflow instance by id
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param workflowInstanceId   workflow instance id
     * @return workflow instance detail
     */
    @Override
    public Map<String, Object> queryWorkflowInstanceById(User loginUser, long projectCode, Integer workflowInstanceId) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode,
                        WORKFLOW_INSTANCE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        WorkflowInstance workflowInstance = processService.findWorkflowInstanceDetailById(workflowInstanceId)
                .orElseThrow(() -> new ServiceException(WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId));

        WorkflowDefinition workflowDefinition =
                processService.findWorkflowDefinition(workflowInstance.getWorkflowDefinitionCode(),
                        workflowInstance.getWorkflowDefinitionVersion());

        if (workflowDefinition == null || projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, projectCode: {}.", projectCode);
            putMsg(result, Status.WORKFLOW_DEFINITION_NOT_EXIST, workflowInstanceId);
        } else {
            workflowInstance.setLocations(workflowDefinition.getLocations());
            workflowInstance.setDagData(processService.genDagData(workflowDefinition));
            result.put(DATA_LIST, workflowInstance);
            putMsg(result, Status.SUCCESS);
        }

        return result;
    }

    @Override
    public WorkflowInstance queryByWorkflowInstanceIdThrowExceptionIfNotFound(Integer workflowInstanceId) {
        WorkflowInstance workflowInstance = workflowInstanceDao.queryById(workflowInstanceId);
        if (workflowInstance == null) {
            throw new ServiceException(WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId);
        }
        return workflowInstance;
    }

    /**
     * query workflow instance by id
     *
     * @param loginUser          login user
     * @param workflowInstanceId workflow instance id
     * @return workflow instance detail
     */
    @Override
    public Map<String, Object> queryWorkflowInstanceById(User loginUser, Integer workflowInstanceId) {
        WorkflowInstance workflowInstance = workflowInstanceMapper.selectById(workflowInstanceId);
        WorkflowDefinition workflowDefinition =
                workflowDefinitionMapper.queryByCode(workflowInstance.getWorkflowDefinitionCode());

        return queryWorkflowInstanceById(loginUser, workflowDefinition.getProjectCode(), workflowInstanceId);
    }

    /**
     * paging query workflow instance list, filtering according to project, workflow definition, time range, keyword, workflow status
     *
     * @param loginUser         login user
     * @param projectCode       project code
     * @param workflowDefinitionCode workflow definition code
     * @param pageNo            page number
     * @param pageSize          page size
     * @param searchVal         search value
     * @param stateType         state type
     * @param host              host
     * @param startDate         start time
     * @param endDate           end time
     * @param otherParamsJson   otherParamsJson handle other params
     * @return workflow instance list
     */
    @Override
    public Result<PageInfo<WorkflowInstance>> queryWorkflowInstanceList(User loginUser,
                                                                        long projectCode,
                                                                        long workflowDefinitionCode,
                                                                        String startDate,
                                                                        String endDate,
                                                                        String searchVal,
                                                                        String executorName,
                                                                        WorkflowExecutionStatus stateType,
                                                                        String host,
                                                                        String otherParamsJson,
                                                                        Integer pageNo,
                                                                        Integer pageSize) {

        Result result = new Result();
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, WORKFLOW_INSTANCE);

        int[] statusArray = null;
        // filter by state
        if (stateType != null) {
            statusArray = new int[]{stateType.getCode()};
        }

        Date start = checkAndParseDateParameters(startDate);
        Date end = checkAndParseDateParameters(endDate);

        Page<WorkflowInstance> page = new Page<>(pageNo, pageSize);
        PageInfo<WorkflowInstance> pageInfo = new PageInfo<>(pageNo, pageSize);

        IPage<WorkflowInstance> workflowInstanceList = workflowInstanceMapper.queryWorkflowInstanceListPaging(
                page,
                projectCode,
                workflowDefinitionCode,
                searchVal,
                executorName,
                statusArray,
                host,
                start,
                end);

        List<WorkflowInstance> workflowInstances = workflowInstanceList.getRecords();
        List<Integer> userIds = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(workflowInstances)) {
            userIds = workflowInstances.stream().map(WorkflowInstance::getExecutorId).collect(Collectors.toList());
        }
        List<User> users = usersService.queryUser(userIds);
        Map<Integer, User> idToUserMap = Collections.emptyMap();
        if (CollectionUtils.isNotEmpty(users)) {
            idToUserMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        }

        for (WorkflowInstance workflowInstance : workflowInstances) {
            workflowInstance.setDuration(WorkflowUtils.getWorkflowInstanceDuration(workflowInstance));
            User executor = idToUserMap.get(workflowInstance.getExecutorId());
            if (null != executor) {
                workflowInstance.setExecutorName(executor.getUserName());
            }
        }

        pageInfo.setTotal((int) workflowInstanceList.getTotal());
        pageInfo.setTotalList(workflowInstances);
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * paging query workflow instance list, filtering according to project, workflow definition, time range, keyword, process status
     *
     * @param loginUser                    login user
     * @param workflowInstanceQueryRequest workflowInstanceQueryRequest
     * @return workflow instance list
     */
    @Override
    public Result queryWorkflowInstanceList(User loginUser, WorkflowInstanceQueryRequest workflowInstanceQueryRequest) {
        Result result = new Result();
        WorkflowInstance workflowInstance = workflowInstanceQueryRequest.convert2WorkflowInstance();
        String projectName = workflowInstanceQueryRequest.getProjectName();
        if (!StringUtils.isBlank(projectName)) {
            Project project = projectMapper.queryByName(projectName);
            projectService.checkProjectAndAuthThrowException(loginUser, project,
                    ApiFuncIdentificationConstant.WORKFLOW_DEFINITION);
            WorkflowDefinition workflowDefinition =
                    workflowDefinitionMapper.queryByDefineName(project.getCode(), workflowInstance.getName());
            workflowInstance.setWorkflowDefinitionCode(workflowDefinition.getCode());
            workflowInstance.setProjectCode(project.getCode());
        }

        Page<WorkflowInstance> page =
                new Page<>(workflowInstanceQueryRequest.getPageNo(), workflowInstanceQueryRequest.getPageSize());
        PageInfo<WorkflowInstance> pageInfo =
                new PageInfo<>(workflowInstanceQueryRequest.getPageNo(), workflowInstanceQueryRequest.getPageSize());

        IPage<WorkflowInstance> workflowInstanceList = workflowInstanceMapper.queryWorkflowInstanceListV2Paging(
                page,
                workflowInstance.getProjectCode(),
                workflowInstance.getWorkflowDefinitionCode(),
                workflowInstance.getName(),
                workflowInstanceQueryRequest.getStartTime(),
                workflowInstanceQueryRequest.getEndTime(),
                workflowInstanceQueryRequest.getState(),
                workflowInstance.getHost());

        List<WorkflowInstance> workflowInstances = workflowInstanceList.getRecords();
        List<Integer> userIds = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(workflowInstances)) {
            userIds = workflowInstances.stream().map(WorkflowInstance::getExecutorId).collect(Collectors.toList());
        }
        List<User> users = usersService.queryUser(userIds);
        Map<Integer, User> idToUserMap = Collections.emptyMap();
        if (CollectionUtils.isNotEmpty(users)) {
            idToUserMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        }

        for (WorkflowInstance Instance : workflowInstances) {
            Instance.setDuration(WorkflowUtils.getWorkflowInstanceDuration(Instance));
            User executor = idToUserMap.get(Instance.getExecutorId());
            if (null != executor) {
                Instance.setExecutorName(executor.getUserName());
            }
        }

        pageInfo.setTotal((int) workflowInstanceList.getTotal());
        pageInfo.setTotalList(workflowInstances);
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query task list by workflow instance id
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param workflowInstanceId   workflow instance id
     * @return task list for the workflow instance
     */
    @Override
    public Map<String, Object> queryTaskListByWorkflowInstanceId(User loginUser, long projectCode,
                                                                 Integer workflowInstanceId) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode,
                        WORKFLOW_INSTANCE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        WorkflowInstance workflowInstance = processService.findWorkflowInstanceDetailById(workflowInstanceId)
                .orElseThrow(() -> new ServiceException(WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId));
        WorkflowDefinition workflowDefinition =
                workflowDefinitionMapper.queryByCode(workflowInstance.getWorkflowDefinitionCode());
        if (workflowDefinition != null && projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, projectCode:{}, workflowInstanceId:{}.", projectCode,
                    workflowInstanceId);
            putMsg(result, WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId);
            return result;
        }
        List<TaskInstance> taskInstanceList =
                taskInstanceDao.queryValidTaskListByWorkflowInstanceId(workflowInstanceId,
                        workflowInstance.getTestFlag());
        List<TaskInstanceDependentDetails<ITaskInstanceContext>> taskInstanceDependentDetailsList =
                setTaskInstanceDependentResult(taskInstanceList);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(WORKFLOW_INSTANCE_STATE, workflowInstance.getState().toString());
        resultMap.put(TASK_LIST, taskInstanceDependentDetailsList);
        result.put(DATA_LIST, resultMap);

        putMsg(result, Status.SUCCESS);
        return result;
    }

    private List<TaskInstanceDependentDetails<ITaskInstanceContext>> setTaskInstanceDependentResult(List<TaskInstance> taskInstanceList) {
        List<TaskInstanceDependentDetails<ITaskInstanceContext>> taskInstanceDependentDetailsList =
                taskInstanceList.stream()
                        .map(taskInstance -> {
                            TaskInstanceDependentDetails<ITaskInstanceContext> taskInstanceDependentDetails =
                                    new TaskInstanceDependentDetails<>();
                            BeanUtils.copyProperties(taskInstance, taskInstanceDependentDetails);
                            return taskInstanceDependentDetails;
                        }).collect(Collectors.toList());
        List<Integer> taskInstanceIdList = taskInstanceList.stream()
                .map(TaskInstance::getId).collect(Collectors.toList());
        List<TaskInstanceContext> taskInstanceContextList =
                taskInstanceContextDao.batchQueryByTaskInstanceIdsAndContextType(taskInstanceIdList,
                        ContextType.DEPENDENT_RESULT_CONTEXT);
        for (TaskInstanceContext taskInstanceContext : taskInstanceContextList) {
            for (AbstractTaskInstanceContext dependentResultTaskInstanceContext : taskInstanceContext
                    .getTaskInstanceContext()) {
                for (TaskInstanceDependentDetails<ITaskInstanceContext> taskInstanceDependentDetails : taskInstanceDependentDetailsList) {
                    if (taskInstanceDependentDetails.getId().equals(taskInstanceContext.getTaskInstanceId())) {
                        taskInstanceDependentDetails
                                .setTaskInstanceDependentResult(
                                        dependentResultTaskInstanceContext);
                    }
                }
            }
        }
        return taskInstanceDependentDetailsList;
    }

    @Override
    public List<DynamicSubWorkflowDto> queryDynamicSubWorkflowInstances(User loginUser, Integer taskId) {
        TaskInstance taskInstance = taskInstanceDao.queryById(taskId);
        Map<String, Object> result = new HashMap<>();
        if (taskInstance == null) {
            putMsg(result, Status.TASK_INSTANCE_NOT_EXISTS, taskId);
            throw new ServiceException(Status.TASK_INSTANCE_NOT_EXISTS, taskId);
        }

        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskInstance.getTaskCode());
        if (taskDefinition == null) {
            putMsg(result, Status.TASK_INSTANCE_NOT_EXISTS, taskId);
            throw new ServiceException(Status.TASK_INSTANCE_NOT_EXISTS, taskId);
        }

        List<RelationSubWorkflow> relationSubWorkflows = relationSubWorkflowMapper
                .queryAllSubWorkflowInstance((long) taskInstance.getWorkflowInstanceId(),
                        taskInstance.getTaskCode());
        List<Long> allSubWorkflowInstanceId = relationSubWorkflows.stream()
                .map(RelationSubWorkflow::getSubWorkflowInstanceId).collect(Collectors.toList());
        List<WorkflowInstance> allSubWorkflows = workflowInstanceDao.queryByIds(allSubWorkflowInstanceId);

        if (allSubWorkflows == null || allSubWorkflows.isEmpty()) {
            putMsg(result, Status.SUB_WORKFLOW_INSTANCE_NOT_EXIST, taskId);
            throw new ServiceException(Status.SUB_WORKFLOW_INSTANCE_NOT_EXIST, taskId);
        }
        Long subWorkflowCode = allSubWorkflows.get(0).getWorkflowDefinitionCode();
        int subWorkflowVersion = allSubWorkflows.get(0).getWorkflowDefinitionVersion();
        WorkflowDefinition subWorkflowDefinition =
                processService.findWorkflowDefinition(subWorkflowCode, subWorkflowVersion);
        if (subWorkflowDefinition == null) {
            putMsg(result, Status.WORKFLOW_DEFINITION_NOT_EXIST, subWorkflowCode);
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST, subWorkflowCode);
        }

        allSubWorkflows.sort(Comparator.comparing(WorkflowInstance::getId));

        List<DynamicSubWorkflowDto> allDynamicSubWorkflowDtos = new ArrayList<>();
        int index = 1;
        for (WorkflowInstance workflowInstance : allSubWorkflows) {
            DynamicSubWorkflowDto dynamicSubWorkflowDto = new DynamicSubWorkflowDto();
            dynamicSubWorkflowDto.setWorkflowInstanceId(workflowInstance.getId());
            dynamicSubWorkflowDto.setIndex(index);
            dynamicSubWorkflowDto.setState(workflowInstance.getState());
            dynamicSubWorkflowDto.setName(subWorkflowDefinition.getName());
            Map<String, String> commandParamMap = JSONUtils.toMap(workflowInstance.getCommandParam());
            String parameter = commandParamMap.get(CommandKeyConstants.CMD_DYNAMIC_START_PARAMS);
            dynamicSubWorkflowDto.setParameters(JSONUtils.toMap(parameter));
            allDynamicSubWorkflowDtos.add(dynamicSubWorkflowDto);
            index++;

        }

        return allDynamicSubWorkflowDtos;
    }

    /**
     * query sub workflow instance detail info by task id
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param taskId      task id
     * @return sub workflow instance detail
     */
    @Override
    public Map<String, Object> querySubWorkflowInstanceByTaskId(User loginUser, long projectCode, Integer taskId) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode,
                        WORKFLOW_INSTANCE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        TaskInstance taskInstance = taskInstanceDao.queryById(taskId);
        if (taskInstance == null) {
            log.error("Task instance does not exist, projectCode:{}, taskInstanceId{}.", projectCode, taskId);
            putMsg(result, Status.TASK_INSTANCE_NOT_EXISTS, taskId);
            return result;
        }

        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskInstance.getTaskCode());
        if (taskDefinition != null && projectCode != taskDefinition.getProjectCode()) {
            log.error("Task definition does not exist, projectCode:{}, taskDefinitionCode:{}.", projectCode,
                    taskInstance.getTaskCode());
            putMsg(result, Status.TASK_INSTANCE_NOT_EXISTS, taskId);
            return result;
        }

        if (!TaskTypeUtils.isSubWorkflowTask(taskInstance.getTaskType())) {
            putMsg(result, Status.TASK_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE, taskInstance.getName());
            return result;
        }

        WorkflowInstance subWorkflowInstance = processService.findSubWorkflowInstance(
                taskInstance.getWorkflowInstanceId(), taskInstance.getId());
        if (subWorkflowInstance == null) {
            log.error("Sub workflow instance does not exist, projectCode:{}, taskInstanceId:{}.", projectCode,
                    taskInstance.getId());
            putMsg(result, Status.SUB_WORKFLOW_INSTANCE_NOT_EXIST, taskId);
            return result;
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(Constants.SUBWORKFLOW_INSTANCE_ID, subWorkflowInstance.getId());
        result.put(DATA_LIST, dataMap);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * update workflow instance
     *
     * @param loginUser          login user
     * @param projectCode        project code
     * @param taskRelationJson   workflow task relation json
     * @param taskDefinitionJson taskDefinitionJson
     * @param workflowInstanceId  workflow instance id
     * @param scheduleTime       schedule time
     * @param syncDefine         sync define
     * @param globalParams       global params
     * @param locations          locations for nodes
     * @param timeout            timeout
     * @return update result code
     */
    @Transactional
    @Override
    public Map<String, Object> updateWorkflowInstance(User loginUser, long projectCode, Integer workflowInstanceId,
                                                      String taskRelationJson,
                                                      String taskDefinitionJson, String scheduleTime,
                                                      Boolean syncDefine,
                                                      String globalParams,
                                                      String locations, int timeout) {
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode,
                ApiFuncIdentificationConstant.INSTANCE_UPDATE);
        Map<String, Object> result = new HashMap<>();
        // check workflow instance exists
        WorkflowInstance workflowInstance = processService.findWorkflowInstanceDetailById(workflowInstanceId)
                .orElseThrow(() -> new ServiceException(WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId));
        // check workflow instance exists in project
        WorkflowDefinition workflowDefinition0 =
                workflowDefinitionMapper.queryByCode(workflowInstance.getWorkflowDefinitionCode());
        if (workflowDefinition0 != null && projectCode != workflowDefinition0.getProjectCode()) {
            log.error("workflow definition does not exist, projectCode:{}, workflowDefinitionCode:{}.", projectCode,
                    workflowInstance.getWorkflowDefinitionCode());
            putMsg(result, WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId);
            return result;
        }
        // check workflow instance status
        if (!workflowInstance.getState().isFinished()) {
            log.warn("workflow Instance state is {} so can not update workflow instance, workflowInstanceId:{}.",
                    workflowInstance.getState().getDesc(), workflowInstanceId);
            putMsg(result, WORKFLOW_INSTANCE_STATE_OPERATION_ERROR,
                    workflowInstance.getName(), workflowInstance.getState().toString(), "update");
            return result;
        }

        //
        Map<String, String> commandParamMap = JSONUtils.toMap(workflowInstance.getCommandParam());
        String timezoneId = null;
        if (commandParamMap == null || StringUtils.isBlank(commandParamMap.get(Constants.SCHEDULE_TIMEZONE))) {
            timezoneId = loginUser.getTimeZone();
        } else {
            timezoneId = commandParamMap.get(Constants.SCHEDULE_TIMEZONE);
        }

        setWorkflowInstance(workflowInstance, scheduleTime, globalParams, timeout, timezoneId);
        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);
        if (taskDefinitionLogs.isEmpty()) {
            log.warn("Parameter taskDefinitionJson is empty");
            putMsg(result, Status.DATA_IS_NOT_VALID, taskDefinitionJson);
            return result;
        }
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
            if (!checkTaskParameters(taskDefinitionLog.getTaskType(), taskDefinitionLog.getTaskParams())) {
                log.error("Task parameters are invalid,  taskDefinitionName:{}.", taskDefinitionLog.getName());
                putMsg(result, Status.WORKFLOW_NODE_S_PARAMETER_INVALID, taskDefinitionLog.getName());
                return result;
            }
        }
        int saveTaskResult = processService.saveTaskDefine(loginUser, projectCode, taskDefinitionLogs, syncDefine);
        if (saveTaskResult == Constants.DEFINITION_FAILURE) {
            log.error("Update task definition error, projectCode:{}, workflowInstanceId:{}", projectCode,
                    workflowInstanceId);
            putMsg(result, Status.UPDATE_TASK_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_TASK_DEFINITION_ERROR);
        }
        WorkflowDefinition workflowDefinition =
                workflowDefinitionMapper.queryByCode(workflowInstance.getWorkflowDefinitionCode());
        List<WorkflowTaskRelationLog> taskRelationList =
                JSONUtils.toList(taskRelationJson, WorkflowTaskRelationLog.class);
        // check workflow json is valid
        result = workflowDefinitionService.checkWorkflowNodeList(taskRelationJson, taskDefinitionLogs);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        workflowDefinition.set(projectCode, workflowDefinition.getName(), workflowDefinition.getDescription(),
                globalParams, locations, timeout);
        workflowDefinition.setUpdateTime(new Date());
        int insertVersion = processService.saveWorkflowDefine(loginUser, workflowDefinition, syncDefine, Boolean.FALSE);
        if (insertVersion == 0) {
            log.error("Update workflow definition error, projectCode:{}, workflowDefinitionName:{}.", projectCode,
                    workflowDefinition.getName());
            putMsg(result, Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
        } else
            log.info("Update workflow definition complete, projectCode:{}, workflowDefinitionName:{}.", projectCode,
                    workflowDefinition.getName());

        // save workflow lineage
        if (syncDefine) {
            workflowDefinitionService.saveWorkflowLineage(projectCode, workflowDefinition.getCode(),
                    insertVersion, taskDefinitionLogs);
        }

        int insertResult = processService.saveTaskRelation(loginUser, workflowDefinition.getProjectCode(),
                workflowDefinition.getCode(), insertVersion, taskRelationList, taskDefinitionLogs, syncDefine);
        if (insertResult == Constants.EXIT_CODE_SUCCESS) {
            log.info(
                    "Update task relations complete, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                    projectCode, workflowDefinition.getCode(), insertVersion);
            putMsg(result, Status.SUCCESS);
            result.put(DATA_LIST, workflowDefinition);
        } else {
            log.info(
                    "Update task relations error, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                    projectCode, workflowDefinition.getCode(), insertVersion);
            putMsg(result, Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
        }
        workflowInstance.setWorkflowDefinitionVersion(insertVersion);
        boolean update = workflowInstanceDao.updateById(workflowInstance);
        if (!update) {
            log.error(
                    "Update workflow instance version error, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}",
                    projectCode, workflowDefinition.getCode(), insertVersion);
            putMsg(result, Status.UPDATE_WORKFLOW_INSTANCE_ERROR);
            throw new ServiceException(Status.UPDATE_WORKFLOW_INSTANCE_ERROR);
        }
        log.info(
                "Update workflow instance complete, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}, workflowInstanceId:{}",
                projectCode, workflowDefinition.getCode(), insertVersion, workflowInstanceId);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * update workflow instance attributes
     */
    private void setWorkflowInstance(WorkflowInstance workflowInstance, String scheduleTime,
                                     String globalParams, int timeout, String timezone) {
        Date schedule = workflowInstance.getScheduleTime();
        if (scheduleTime != null) {
            schedule = DateUtils.stringToDate(scheduleTime);
        }
        workflowInstance.setScheduleTime(schedule);
        List<Property> globalParamList = JSONUtils.toList(globalParams, Property.class);
        Map<String, String> globalParamMap =
                globalParamList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));
        globalParams = curingGlobalParamsService.curingGlobalParams(workflowInstance.getId(), globalParamMap,
                globalParamList, workflowInstance.getCmdTypeIfComplement(), schedule, timezone);
        workflowInstance.setTimeout(timeout);
        workflowInstance.setGlobalParams(globalParams);
    }

    /**
     * query parent workflow instance detail info by sub workflow instance id
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param subId       sub workflow id
     * @return parent instance detail
     */
    @Override
    public Map<String, Object> queryParentInstanceBySubId(User loginUser, long projectCode, Integer subId) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode,
                        WORKFLOW_INSTANCE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        WorkflowInstance subInstance = processService.findWorkflowInstanceDetailById(subId)
                .orElseThrow(() -> new ServiceException(WORKFLOW_INSTANCE_NOT_EXIST, subId));
        if (subInstance.getIsSubWorkflow() == Flag.NO) {
            log.warn(
                    "workflow instance is not sub workflow instance type, workflowInstanceId:{}, workflowInstanceName:{}.",
                    subId, subInstance.getName());
            putMsg(result, Status.WORKFLOW_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE, subInstance.getName());
            return result;
        }

        WorkflowInstance parentWorkflowInstance = processService.findParentWorkflowInstance(subId);
        if (parentWorkflowInstance == null) {
            log.error("Parent workflow instance does not exist, projectCode:{}, subWorkflowInstanceId:{}.",
                    projectCode, subId);
            putMsg(result, Status.SUB_WORKFLOW_INSTANCE_NOT_EXIST);
            return result;
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(Constants.PARENT_WORKFLOW_INSTANCE, parentWorkflowInstance.getId());
        result.put(DATA_LIST, dataMap);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * delete workflow instance by id, at the same time，delete task instance and their mapping relation data
     *
     * @param loginUser         login user
     * @param workflowInstanceId workflow instance id
     * @return delete result code
     */
    @Override
    @Transactional
    public void deleteWorkflowInstanceById(User loginUser, Integer workflowInstanceId) {
        WorkflowInstance workflowInstance = processService.findWorkflowInstanceDetailById(workflowInstanceId)
                .orElseThrow(() -> new ServiceException(WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId));
        WorkflowDefinition workflowDefinition = workflowDefinitionLogMapper.queryByDefinitionCodeAndVersion(
                workflowInstance.getWorkflowDefinitionCode(), workflowInstance.getWorkflowDefinitionVersion());

        Project project = projectMapper.queryByCode(workflowDefinition.getProjectCode());
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project,
                ApiFuncIdentificationConstant.INSTANCE_DELETE);
        // check workflow instance status
        if (!workflowInstance.getState().isFinished()) {
            log.warn("workflow Instance state is {} so can not delete workflow instance, workflowInstanceId:{}.",
                    workflowInstance.getState().getDesc(), workflowInstanceId);
            throw new ServiceException(WORKFLOW_INSTANCE_STATE_OPERATION_ERROR, workflowInstance.getName(),
                    workflowInstance.getState(), "delete");
        }
        deleteWorkflowInstanceById(workflowInstanceId);
    }

    /**
     * view workflow instance variables
     *
     * @param projectCode       project code
     * @param workflowInstanceId workflow instance id
     * @return variables data
     */
    @Override
    public Map<String, Object> viewVariables(long projectCode, Integer workflowInstanceId) {
        Map<String, Object> result = new HashMap<>();

        WorkflowInstance workflowInstance = workflowInstanceMapper.queryDetailById(workflowInstanceId);

        if (workflowInstance == null) {
            log.error("workflow instance does not exist, projectCode:{}, workflowInstanceId:{}.", projectCode,
                    workflowInstanceId);
            putMsg(result, WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId);
            return result;
        }

        WorkflowDefinition workflowDefinition =
                workflowDefinitionMapper.queryByCode(workflowInstance.getWorkflowDefinitionCode());
        if (workflowDefinition != null && projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, projectCode:{}, workflowDefinitionCode:{}.", projectCode,
                    workflowInstance.getWorkflowDefinitionCode());
            putMsg(result, WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId);
            return result;
        }

        Map<String, String> commandParam = JSONUtils.toMap(workflowInstance.getCommandParam());
        String timezone = null;
        if (commandParam != null) {
            timezone = commandParam.get(Constants.SCHEDULE_TIMEZONE);
        }
        Map<String, String> timeParams = BusinessTimeUtils
                .getBusinessTime(workflowInstance.getCmdTypeIfComplement(),
                        workflowInstance.getScheduleTime(), timezone);
        String userDefinedParams = workflowInstance.getGlobalParams();
        // global params
        List<Property> globalParams = new ArrayList<>();

        // global param string
        String globalParamStr =
                ParameterUtils.convertParameterPlaceholders(JSONUtils.toJsonString(globalParams), timeParams);
        globalParams = JSONUtils.toList(globalParamStr, Property.class);
        for (Property property : globalParams) {
            timeParams.put(property.getProp(), property.getValue());
        }

        if (userDefinedParams != null && userDefinedParams.length() > 0) {
            globalParams = JSONUtils.toList(userDefinedParams, Property.class);
        }

        Map<String, Map<String, Object>> localUserDefParams = getLocalParams(workflowInstance, timeParams);

        Map<String, Object> resultMap = new HashMap<>();

        resultMap.put(GLOBAL_PARAMS, globalParams);
        resultMap.put(LOCAL_PARAMS, localUserDefParams);

        result.put(DATA_LIST, resultMap);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * get local params
     */
    private Map<String, Map<String, Object>> getLocalParams(WorkflowInstance workflowInstance,
                                                            Map<String, String> timeParams) {
        Map<String, Map<String, Object>> localUserDefParams = new HashMap<>();
        List<TaskInstance> taskInstanceList =
                taskInstanceMapper.findValidTaskListByWorkflowInstanceId(workflowInstance.getId(), Flag.YES,
                        workflowInstance.getTestFlag());
        for (TaskInstance taskInstance : taskInstanceList) {
            TaskDefinitionLog taskDefinitionLog = taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(
                    taskInstance.getTaskCode(), taskInstance.getTaskDefinitionVersion());

            String localParams = JSONUtils.getNodeString(taskDefinitionLog.getTaskParams(), LOCAL_PARAMS);
            if (!StringUtils.isEmpty(localParams)) {
                localParams = ParameterUtils.convertParameterPlaceholders(localParams, timeParams);
                List<Property> localParamsList = JSONUtils.toList(localParams, Property.class);

                Map<String, Object> localParamsMap = new HashMap<>();
                localParamsMap.put(TASK_TYPE, taskDefinitionLog.getTaskType());
                localParamsMap.put(LOCAL_PARAMS_LIST, localParamsList);
                if (CollectionUtils.isNotEmpty(localParamsList)) {
                    localUserDefParams.put(taskDefinitionLog.getName(), localParamsMap);
                }
            }
        }
        return localUserDefParams;
    }

    /**
     * encapsulation gantt structure
     *
     * @param projectCode       project code
     * @param workflowInstanceId workflow instance id
     * @return gantt tree data
     * @throws Exception exception when json parse
     */
    @Override
    public Map<String, Object> viewGantt(long projectCode, Integer workflowInstanceId) throws Exception {
        Map<String, Object> result = new HashMap<>();
        WorkflowInstance workflowInstance = workflowInstanceMapper.queryDetailById(workflowInstanceId);

        if (workflowInstance == null) {
            log.error("workflow instance does not exist, projectCode:{}, workflowInstanceId:{}.", projectCode,
                    workflowInstanceId);
            putMsg(result, WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId);
            return result;
        }

        WorkflowDefinition workflowDefinition = workflowDefinitionLogMapper.queryByDefinitionCodeAndVersion(
                workflowInstance.getWorkflowDefinitionCode(),
                workflowInstance.getWorkflowDefinitionVersion());
        if (workflowDefinition == null || projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, projectCode:{}, workflowDefinitionCode:{}.", projectCode,
                    workflowInstance.getWorkflowDefinitionCode());
            putMsg(result, WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId);
            return result;
        }
        GanttDto ganttDto = new GanttDto();
        DAG<Long, TaskNode, TaskNodeRelation> dag = processService.genDagGraph(workflowDefinition);
        // topological sort
        List<Long> nodeList = dag.topologicalSort();

        ganttDto.setTaskNames(nodeList);

        List<Task> taskList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(nodeList)) {
            List<TaskInstance> taskInstances = taskInstanceMapper.queryByWorkflowInstanceIdsAndTaskCodes(
                    Collections.singletonList(workflowInstanceId), nodeList);
            for (Long node : nodeList) {
                TaskInstance taskInstance = null;
                for (TaskInstance instance : taskInstances) {
                    if (instance.getWorkflowInstanceId() == workflowInstanceId && instance.getTaskCode() == node) {
                        taskInstance = instance;
                        break;
                    }
                }
                if (taskInstance == null) {
                    continue;
                }
                Date startTime = taskInstance.getStartTime() == null ? new Date() : taskInstance.getStartTime();
                Date endTime = taskInstance.getEndTime() == null ? new Date() : taskInstance.getEndTime();
                Task task = new Task();
                task.setTaskName(taskInstance.getName());
                task.getStartDate().add(startTime.getTime());
                task.getEndDate().add(endTime.getTime());
                task.setIsoStart(startTime);
                task.setIsoEnd(endTime);
                task.setStatus(taskInstance.getState().name());
                task.setExecutionDate(taskInstance.getStartTime());
                task.setDuration(DateUtils.format2Readable(endTime.getTime() - startTime.getTime()));
                taskList.add(task);
            }
        }
        ganttDto.setTasks(taskList);

        result.put(DATA_LIST, ganttDto);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query workflow instance by workflowDefinitionCode and stateArray
     *
     * @param workflowDefinitionCode workflowDefinitionCode
     * @param states                states array
     * @return workflow instance list
     */
    @Override
    public List<WorkflowInstance> queryByWorkflowDefinitionCodeAndStatus(Long workflowDefinitionCode, int[] states) {
        return workflowInstanceMapper.queryByWorkflowDefinitionCodeAndStatus(workflowDefinitionCode, states);
    }

    @Override
    public List<WorkflowInstance> queryByWorkflowCodeVersionStatus(Long workflowDefinitionCode,
                                                                   int workflowDefinitionVersion, int[] states) {
        return workflowInstanceDao.queryByWorkflowCodeVersionStatus(workflowDefinitionCode, workflowDefinitionVersion,
                states);
    }

    /**
     * query workflow instance by workflowDefinitionCode
     *
     * @param workflowDefinitionCode workflowDefinitionCode
     * @param size                  size
     * @return workflow instance list
     */
    @Override
    public List<WorkflowInstance> queryByWorkflowDefinitionCode(Long workflowDefinitionCode, int size) {
        return workflowInstanceMapper.queryByWorkflowDefinitionCode(workflowDefinitionCode, size);
    }

    /**
     * query workflow instance list bt trigger code
     *
     * @param loginUser
     * @param projectCode
     * @param triggerCode
     * @return
     */
    @Override
    public Map<String, Object> queryByTriggerCode(User loginUser, long projectCode, Long triggerCode) {

        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE);
        if (result.get(Constants.STATUS) != Status.SUCCESS || triggerCode == null) {
            return result;
        }

        List<WorkflowInstance> workflowInstances = workflowInstanceMapper.queryByTriggerCode(
                triggerCode);
        result.put(DATA_LIST, workflowInstances);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public void deleteWorkflowInstanceByWorkflowDefinitionCode(long workflowDefinitionCode) {
        while (true) {
            List<WorkflowInstance> workflowInstances =
                    workflowInstanceMapper.queryByWorkflowDefinitionCode(workflowDefinitionCode, 100);
            if (CollectionUtils.isEmpty(workflowInstances)) {
                break;
            }
            log.info("Begin to delete workflow instance, workflow definition code: {}", workflowDefinitionCode);
            for (WorkflowInstance workflowInstance : workflowInstances) {
                if (!workflowInstance.getState().isFinished()) {
                    log.warn("Workflow instance is not finished cannot delete, workflow instance id:{}",
                            workflowInstance.getId());
                    throw new ServiceException(WORKFLOW_INSTANCE_STATE_OPERATION_ERROR, workflowInstance.getName(),
                            workflowInstance.getState(), "delete");
                }
                deleteWorkflowInstanceById(workflowInstance.getId());
            }
            log.info("Success delete workflow instance, workflow definition code: {}, size: {}",
                    workflowDefinitionCode, workflowInstances.size());
        }
    }

    @Override
    public void deleteWorkflowInstanceById(int workflowInstanceId) {
        // delete task instance
        taskInstanceService.deleteByWorkflowInstanceId(workflowInstanceId);
        // delete sub workflow instances
        deleteSubWorkflowInstanceIfNeeded(workflowInstanceId);
        // delete alert
        alertDao.deleteByWorkflowInstanceId(workflowInstanceId);
        // delete workflow instance
        workflowInstanceDao.deleteById(workflowInstanceId);
    }

    private void deleteSubWorkflowInstanceIfNeeded(int workflowInstanceId) {
        List<Integer> subWorkflowInstanceIds = workflowInstanceMapDao.querySubWorkflowInstanceIds(workflowInstanceId);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(subWorkflowInstanceIds)) {
            return;
        }
        for (Integer subWorkflowInstanceId : subWorkflowInstanceIds) {
            deleteWorkflowInstanceById(subWorkflowInstanceId);
        }
        workflowInstanceMapDao.deleteByParentId(workflowInstanceId);
    }
}
