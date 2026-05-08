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
import static org.apache.dolphinscheduler.common.constants.Constants.LOCAL_PARAMS;
import static org.apache.dolphinscheduler.common.constants.Constants.PARENT_WORKFLOW_INSTANCE;
import static org.apache.dolphinscheduler.common.constants.Constants.SUBWORKFLOW_INSTANCE_ID;
import static org.apache.dolphinscheduler.common.utils.JSONUtils.parseObject;
import static org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager.checkTaskParameters;

import org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant;
import org.apache.dolphinscheduler.api.dto.DynamicSubWorkflowDto;
import org.apache.dolphinscheduler.api.dto.gantt.GanttDto;
import org.apache.dolphinscheduler.api.dto.gantt.Task;
import org.apache.dolphinscheduler.api.dto.workflowInstance.WorkflowInstanceTaskListDTO;
import org.apache.dolphinscheduler.api.dto.workflowInstance.WorkflowInstanceVariablesDTO;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
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
import org.apache.dolphinscheduler.dao.repository.TaskInstanceContextDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceMapDao;
import org.apache.dolphinscheduler.dao.utils.WorkflowUtils;
import org.apache.dolphinscheduler.extract.master.command.ICommandParam;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.GlobalParameterUtils;
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
    TaskInstanceMapper taskInstanceMapper;

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

    @Override
    public List<WorkflowInstance> queryTopNLongestRunningWorkflowInstance(User loginUser, long projectCode, int size,
                                                                          String startTime, String endTime) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_INSTANCE);

        if (0 > size) {
            throw new ServiceException(Status.NEGTIVE_SIZE_NUMBER_ERROR, size);
        }
        if (Objects.isNull(startTime)) {
            throw new ServiceException(Status.DATA_IS_NULL, Constants.START_TIME);
        }
        Date start = DateUtils.stringToDate(startTime);
        if (Objects.isNull(endTime)) {
            throw new ServiceException(Status.DATA_IS_NULL, Constants.END_TIME);
        }
        Date end = DateUtils.stringToDate(endTime);
        if (start == null || end == null) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.START_END_DATE);
        }
        if (start.getTime() > end.getTime()) {
            throw new ServiceException(Status.START_TIME_BIGGER_THAN_END_TIME_ERROR, startTime, endTime);
        }

        return workflowInstanceMapper.queryTopNWorkflowInstance(size, start, end, WorkflowExecutionStatus.SUCCESS,
                projectCode);
    }

    @Override
    public WorkflowInstance queryWorkflowInstanceById(User loginUser, long projectCode, Integer workflowInstanceId) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_INSTANCE);

        WorkflowInstance workflowInstance = processService.findWorkflowInstanceDetailById(workflowInstanceId)
                .orElseThrow(() -> new ServiceException(WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId));

        WorkflowDefinition workflowDefinition =
                processService.findWorkflowDefinition(workflowInstance.getWorkflowDefinitionCode(),
                        workflowInstance.getWorkflowDefinitionVersion());

        if (workflowDefinition == null || projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, projectCode: {}.", projectCode);
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST, workflowInstanceId);
        }
        workflowInstance.setLocations(workflowDefinition.getLocations());
        workflowInstance.setDagData(processService.genDagData(workflowDefinition));
        return workflowInstance;
    }

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

    @Override
    public WorkflowInstanceTaskListDTO queryTaskListByWorkflowInstanceId(User loginUser, long projectCode,
                                                                         Integer workflowInstanceId) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_INSTANCE);

        WorkflowInstance workflowInstance = processService.findWorkflowInstanceDetailById(workflowInstanceId)
                .orElseThrow(() -> new ServiceException(WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId));
        WorkflowDefinition workflowDefinition =
                workflowDefinitionMapper.queryByCode(workflowInstance.getWorkflowDefinitionCode());
        if (workflowDefinition != null && projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, projectCode:{}, workflowInstanceId:{}.", projectCode,
                    workflowInstanceId);
            throw new ServiceException(WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId);
        }
        List<TaskInstance> taskInstanceList =
                taskInstanceDao.queryValidTaskListByWorkflowInstanceId(workflowInstanceId);
        List<TaskInstanceDependentDetails<AbstractTaskInstanceContext>> taskInstanceDependentDetailsList =
                setTaskInstanceDependentResult(taskInstanceList);
        return new WorkflowInstanceTaskListDTO(workflowInstance.getState().toString(),
                taskInstanceDependentDetailsList);
    }

    private List<TaskInstanceDependentDetails<AbstractTaskInstanceContext>> setTaskInstanceDependentResult(List<TaskInstance> taskInstanceList) {
        List<TaskInstanceDependentDetails<AbstractTaskInstanceContext>> taskInstanceDependentDetailsList =
                taskInstanceList.stream()
                        .map(taskInstance -> {
                            TaskInstanceDependentDetails<AbstractTaskInstanceContext> taskInstanceDependentDetails =
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
            for (TaskInstanceDependentDetails<AbstractTaskInstanceContext> taskInstanceDependentDetails : taskInstanceDependentDetailsList) {
                if (taskInstanceDependentDetails.getId().equals(taskInstanceContext.getTaskInstanceId())) {
                    taskInstanceDependentDetails
                            .setTaskInstanceDependentResults(taskInstanceContext.getTaskInstanceContext());
                }
            }
        }
        return taskInstanceDependentDetailsList;
    }

    @Override
    public List<DynamicSubWorkflowDto> queryDynamicSubWorkflowInstances(User loginUser, Integer taskId) {
        TaskInstance taskInstance = taskInstanceDao.queryById(taskId);
        if (taskInstance == null) {
            throw new ServiceException(Status.TASK_INSTANCE_NOT_EXISTS, taskId);
        }

        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskInstance.getTaskCode());
        if (taskDefinition == null) {
            throw new ServiceException(Status.TASK_INSTANCE_NOT_EXISTS, taskId);
        }

        List<RelationSubWorkflow> relationSubWorkflows = relationSubWorkflowMapper
                .queryAllSubWorkflowInstance((long) taskInstance.getWorkflowInstanceId(),
                        taskInstance.getTaskCode());
        List<Long> allSubWorkflowInstanceId = relationSubWorkflows.stream()
                .map(RelationSubWorkflow::getSubWorkflowInstanceId).collect(Collectors.toList());
        List<WorkflowInstance> allSubWorkflows = workflowInstanceDao.queryByIds(allSubWorkflowInstanceId);

        if (allSubWorkflows == null || allSubWorkflows.isEmpty()) {
            throw new ServiceException(Status.SUB_WORKFLOW_INSTANCE_NOT_EXIST, taskId);
        }
        Long subWorkflowCode = allSubWorkflows.get(0).getWorkflowDefinitionCode();
        int subWorkflowVersion = allSubWorkflows.get(0).getWorkflowDefinitionVersion();
        WorkflowDefinition subWorkflowDefinition =
                processService.findWorkflowDefinition(subWorkflowCode, subWorkflowVersion);
        if (subWorkflowDefinition == null) {
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

    @Override
    public Map<String, Integer> querySubWorkflowInstanceByTaskId(User loginUser, long projectCode, Integer taskId) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_INSTANCE);

        TaskInstance taskInstance = taskInstanceDao.queryById(taskId);
        if (taskInstance == null) {
            log.error("Task instance does not exist, projectCode:{}, taskInstanceId{}.", projectCode, taskId);
            throw new ServiceException(Status.TASK_INSTANCE_NOT_EXISTS, taskId);
        }

        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskInstance.getTaskCode());
        if (taskDefinition != null && projectCode != taskDefinition.getProjectCode()) {
            log.error("Task definition does not exist, projectCode:{}, taskDefinitionCode:{}.", projectCode,
                    taskInstance.getTaskCode());
            throw new ServiceException(Status.TASK_INSTANCE_NOT_EXISTS, taskId);
        }

        if (!TaskTypeUtils.isSubWorkflowTask(taskInstance.getTaskType())) {
            throw new ServiceException(Status.TASK_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE, taskInstance.getName());
        }

        WorkflowInstance subWorkflowInstance = processService.findSubWorkflowInstance(
                taskInstance.getWorkflowInstanceId(), taskInstance.getId());
        if (subWorkflowInstance == null) {
            log.error("Sub workflow instance does not exist, projectCode:{}, taskInstanceId:{}.", projectCode,
                    taskInstance.getId());
            throw new ServiceException(Status.SUB_WORKFLOW_INSTANCE_NOT_EXIST, taskId);
        }
        return Collections.singletonMap(SUBWORKFLOW_INSTANCE_ID, subWorkflowInstance.getId());
    }

    @Transactional
    @Override
    public WorkflowDefinition updateWorkflowInstance(User loginUser, long projectCode, Integer workflowInstanceId,
                                                     String taskRelationJson,
                                                     String taskDefinitionJson, String scheduleTime,
                                                     Boolean syncDefine,
                                                     String globalParams,
                                                     String locations, int timeout) {
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode,
                ApiFuncIdentificationConstant.INSTANCE_UPDATE);
        // check workflow instance exists
        WorkflowInstance workflowInstance = processService.findWorkflowInstanceDetailById(workflowInstanceId)
                .orElseThrow(() -> new ServiceException(WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId));
        // check workflow instance exists in project
        WorkflowDefinition workflowDefinition0 =
                workflowDefinitionMapper.queryByCode(workflowInstance.getWorkflowDefinitionCode());
        if (workflowDefinition0 != null && projectCode != workflowDefinition0.getProjectCode()) {
            log.error("workflow definition does not exist, projectCode:{}, workflowDefinitionCode:{}.", projectCode,
                    workflowInstance.getWorkflowDefinitionCode());
            throw new ServiceException(WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId);
        }
        // check workflow instance status
        if (!workflowInstance.getState().isFinalState()) {
            log.warn("workflow Instance state is {} so can not update workflow instance, workflowInstanceId:{}.",
                    workflowInstance.getState().name(), workflowInstanceId);
            throw new ServiceException(WORKFLOW_INSTANCE_STATE_OPERATION_ERROR,
                    workflowInstance.getName(), workflowInstance.getState().toString(), "update");
        }

        String timezoneId;
        final ICommandParam commandParam = parseObject(workflowInstance.getCommandParam(), ICommandParam.class);
        if (commandParam == null || StringUtils.isEmpty(commandParam.getTimeZone())) {
            timezoneId = loginUser.getTimeZone();
        } else {
            timezoneId = commandParam.getTimeZone();
        }

        setWorkflowInstance(workflowInstance, scheduleTime, globalParams, timeout, timezoneId);
        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);
        if (taskDefinitionLogs.isEmpty()) {
            log.warn("Parameter taskDefinitionJson is empty");
            throw new ServiceException(Status.DATA_IS_NOT_VALID, taskDefinitionJson);
        }
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
            if (!checkTaskParameters(taskDefinitionLog.getTaskType(), taskDefinitionLog.getTaskParams())) {
                log.error("Task parameters are invalid,  taskDefinitionName:{}.", taskDefinitionLog.getName());
                throw new ServiceException(Status.WORKFLOW_NODE_S_PARAMETER_INVALID, taskDefinitionLog.getName());
            }
        }
        int saveTaskResult = processService.saveTaskDefine(loginUser, projectCode, taskDefinitionLogs, syncDefine);
        if (saveTaskResult == Constants.DEFINITION_FAILURE) {
            log.error("Update task definition error, projectCode:{}, workflowInstanceId:{}", projectCode,
                    workflowInstanceId);
            throw new ServiceException(Status.UPDATE_TASK_DEFINITION_ERROR);
        }
        WorkflowDefinition workflowDefinition =
                workflowDefinitionMapper.queryByCode(workflowInstance.getWorkflowDefinitionCode());
        List<WorkflowTaskRelationLog> taskRelationList =
                JSONUtils.toList(taskRelationJson, WorkflowTaskRelationLog.class);
        // check workflow json is valid
        Map<String, Object> checkResult =
                workflowDefinitionService.checkWorkflowNodeList(taskRelationJson, taskDefinitionLogs);
        Status checkStatus = (Status) checkResult.get(Constants.STATUS);
        if (checkStatus != Status.SUCCESS) {
            throw new ServiceException(checkStatus.getCode(), (String) checkResult.get(Constants.MSG));
        }

        workflowDefinition.set(projectCode, workflowDefinition.getName(), workflowDefinition.getDescription(),
                globalParams, locations, timeout);
        workflowDefinition.setUpdateTime(new Date());
        int insertVersion = processService.saveWorkflowDefine(loginUser, workflowDefinition, syncDefine, Boolean.FALSE);
        if (insertVersion == 0) {
            log.error("Update workflow definition error, projectCode:{}, workflowDefinitionName:{}.", projectCode,
                    workflowDefinition.getName());
            throw new ServiceException(Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
        }
        log.info("Update workflow definition complete, projectCode:{}, workflowDefinitionName:{}.", projectCode,
                workflowDefinition.getName());

        // save workflow lineage
        if (syncDefine) {
            workflowDefinitionService.saveWorkflowLineage(projectCode, workflowDefinition.getCode(),
                    insertVersion, taskDefinitionLogs);
        }

        int insertResult = processService.saveTaskRelation(loginUser, workflowDefinition.getProjectCode(),
                workflowDefinition.getCode(), insertVersion, taskRelationList, taskDefinitionLogs, syncDefine);
        if (insertResult != Constants.EXIT_CODE_SUCCESS) {
            log.info(
                    "Update task relations error, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                    projectCode, workflowDefinition.getCode(), insertVersion);
            throw new ServiceException(Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
        }
        log.info(
                "Update task relations complete, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                projectCode, workflowDefinition.getCode(), insertVersion);
        workflowInstance.setWorkflowDefinitionVersion(insertVersion);
        boolean update = workflowInstanceDao.updateById(workflowInstance);
        if (!update) {
            log.error(
                    "Update workflow instance version error, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}",
                    projectCode, workflowDefinition.getCode(), insertVersion);
            throw new ServiceException(Status.UPDATE_WORKFLOW_INSTANCE_ERROR);
        }
        log.info(
                "Update workflow instance complete, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}, workflowInstanceId:{}",
                projectCode, workflowDefinition.getCode(), insertVersion, workflowInstanceId);
        return workflowDefinition;
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
        List<Property> globalParamList = GlobalParameterUtils.deserializeGlobalParameter(globalParams);
        Map<String, String> globalParamMap =
                globalParamList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));
        globalParams = curingGlobalParamsService.curingGlobalParams(workflowInstance.getId(), globalParamMap,
                globalParamList, workflowInstance.getCmdTypeIfComplement(), schedule, timezone);
        workflowInstance.setTimeout(timeout);
        workflowInstance.setGlobalParams(globalParams);
    }

    @Override
    public Map<String, Integer> queryParentInstanceBySubId(User loginUser, long projectCode, Integer subId) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_INSTANCE);

        WorkflowInstance subInstance = processService.findWorkflowInstanceDetailById(subId)
                .orElseThrow(() -> new ServiceException(WORKFLOW_INSTANCE_NOT_EXIST, subId));
        if (subInstance.getIsSubWorkflow() == Flag.NO) {
            log.warn(
                    "workflow instance is not sub workflow instance type, workflowInstanceId:{}, workflowInstanceName:{}.",
                    subId, subInstance.getName());
            throw new ServiceException(Status.WORKFLOW_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE, subInstance.getName());
        }

        WorkflowInstance parentWorkflowInstance = processService.findParentWorkflowInstance(subId);
        if (parentWorkflowInstance == null) {
            log.error("Parent workflow instance does not exist, projectCode:{}, subWorkflowInstanceId:{}.",
                    projectCode, subId);
            throw new ServiceException(Status.SUB_WORKFLOW_INSTANCE_NOT_EXIST);
        }
        return Collections.singletonMap(PARENT_WORKFLOW_INSTANCE, parentWorkflowInstance.getId());
    }

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
        if (!workflowInstance.getState().isFinalState()) {
            log.warn("workflow Instance state is {} so can not delete workflow instance, workflowInstanceId:{}.",
                    workflowInstance.getState().name(), workflowInstanceId);
            throw new ServiceException(WORKFLOW_INSTANCE_STATE_OPERATION_ERROR, workflowInstance.getName(),
                    workflowInstance.getState(), "delete");
        }
        deleteWorkflowInstanceById(workflowInstanceId);
    }

    @Override
    public WorkflowInstanceVariablesDTO viewVariables(User loginUser, long projectCode, Integer workflowInstanceId) {
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, WORKFLOW_INSTANCE);

        WorkflowInstance workflowInstance = workflowInstanceMapper.queryDetailById(workflowInstanceId);

        if (workflowInstance == null) {
            log.error("workflow instance does not exist, projectCode:{}, workflowInstanceId:{}.", projectCode,
                    workflowInstanceId);
            throw new ServiceException(WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId);
        }

        WorkflowDefinition workflowDefinition =
                workflowDefinitionMapper.queryByCode(workflowInstance.getWorkflowDefinitionCode());
        if (workflowDefinition != null && projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, projectCode:{}, workflowDefinitionCode:{}.", projectCode,
                    workflowInstance.getWorkflowDefinitionCode());
            throw new ServiceException(WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId);
        }

        String timezone = null;
        final ICommandParam commandParam = parseObject(workflowInstance.getCommandParam(), ICommandParam.class);
        if (commandParam != null && StringUtils.isNotEmpty(commandParam.getTimeZone())) {
            timezone = commandParam.getTimeZone();
        }

        Map<String, String> parameterMap = BusinessTimeUtils
                .getBusinessTime(workflowInstance.getCmdTypeIfComplement(),
                        workflowInstance.getScheduleTime(), timezone);

        // finalGlobalParams
        List<Property> finalGlobalParams = processGlobalParams(workflowInstance, parameterMap);

        // localUserDefParams
        Map<String, Map<String, Object>> localUserDefParams = processLocalParams(workflowInstance, parameterMap);

        return new WorkflowInstanceVariablesDTO(finalGlobalParams, localUserDefParams);
    }

    /**
     * Process global parameters: resolve placeholders and merge into context.
     *
     * @param workflowInstance The workflow instance.
     * @param parameterMap Context parameters for placeholder replacement and merging
     * @return Deserialized global properties list
     */
    private List<Property> processGlobalParams(WorkflowInstance workflowInstance, Map<String, String> parameterMap) {
        List<Property> finalGlobalParams = new ArrayList<>();

        String globalParamsJson = workflowInstance.getGlobalParams();
        if (StringUtils.isNotEmpty(globalParamsJson)) {
            // Replace placeholders
            String replacedJsonStr = ParameterUtils.convertParameterPlaceholders(globalParamsJson, parameterMap);
            finalGlobalParams = GlobalParameterUtils.deserializeGlobalParameter(replacedJsonStr);

            // Merge into context map
            if (finalGlobalParams != null) {
                for (Property property : finalGlobalParams) {
                    if (property.getProp() != null && property.getValue() != null) {
                        parameterMap.put(property.getProp(), property.getValue());
                    }
                }
            }
        }
        return finalGlobalParams;
    }

    /**
     * Process local parameters for tasks within a workflow instance.
     *
     * @param workflowInstance The workflow instance.
     * @param parameterMap     Context parameters for placeholder replacement.
     * @return Map of task name to its local parameters and type.
     */
    private Map<String, Map<String, Object>> processLocalParams(WorkflowInstance workflowInstance,
                                                                Map<String, String> parameterMap) {
        Map<String, Map<String, Object>> localUserDefParams = new HashMap<>();

        // Fetch valid task instances for the workflow
        List<TaskInstance> taskInstanceList =
                taskInstanceMapper.findValidTaskListByWorkflowInstanceId(workflowInstance.getId(), Flag.YES);

        for (TaskInstance taskInstance : taskInstanceList) {
            TaskDefinitionLog taskDefinitionLog = taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(
                    taskInstance.getTaskCode(), taskInstance.getTaskDefinitionVersion());

            String localParams = JSONUtils.getNodeString(taskDefinitionLog.getTaskParams(), LOCAL_PARAMS);

            if (!StringUtils.isEmpty(localParams)) {
                // Replace placeholders and deserialize
                localParams = ParameterUtils.convertParameterPlaceholders(localParams, parameterMap);
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

    @Override
    public GanttDto viewGantt(User loginUser, long projectCode, Integer workflowInstanceId) throws Exception {
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, WORKFLOW_INSTANCE);
        WorkflowInstance workflowInstance = workflowInstanceMapper.queryDetailById(workflowInstanceId);

        if (workflowInstance == null) {
            log.error("workflow instance does not exist, projectCode:{}, workflowInstanceId:{}.", projectCode,
                    workflowInstanceId);
            throw new ServiceException(WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId);
        }

        WorkflowDefinition workflowDefinition = workflowDefinitionLogMapper.queryByDefinitionCodeAndVersion(
                workflowInstance.getWorkflowDefinitionCode(),
                workflowInstance.getWorkflowDefinitionVersion());
        if (workflowDefinition == null || projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, projectCode:{}, workflowDefinitionCode:{}.", projectCode,
                    workflowInstance.getWorkflowDefinitionCode());
            throw new ServiceException(WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId);
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
        return ganttDto;
    }

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

    @Override
    public List<WorkflowInstance> queryByWorkflowDefinitionCode(Long workflowDefinitionCode, int size) {
        return workflowInstanceMapper.queryByWorkflowDefinitionCode(workflowDefinitionCode, size);
    }

    @Override
    public List<WorkflowInstance> queryByTriggerCode(User loginUser, long projectCode, Long triggerCode) {

        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_INSTANCE);

        if (triggerCode == null) {
            return Collections.emptyList();
        }
        return workflowInstanceMapper.queryByTriggerCode(triggerCode);
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
                if (!workflowInstance.getState().isFinalState()) {
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
