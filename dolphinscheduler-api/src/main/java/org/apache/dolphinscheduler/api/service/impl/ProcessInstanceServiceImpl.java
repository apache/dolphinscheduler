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
import static org.apache.dolphinscheduler.api.enums.Status.PROCESS_INSTANCE_NOT_EXIST;
import static org.apache.dolphinscheduler.api.enums.Status.PROCESS_INSTANCE_STATE_OPERATION_ERROR;
import static org.apache.dolphinscheduler.common.constants.Constants.DATA_LIST;
import static org.apache.dolphinscheduler.common.constants.Constants.DEPENDENT_SPLIT;
import static org.apache.dolphinscheduler.common.constants.Constants.GLOBAL_PARAMS;
import static org.apache.dolphinscheduler.common.constants.Constants.LOCAL_PARAMS;
import static org.apache.dolphinscheduler.common.constants.Constants.PROCESS_INSTANCE_STATE;
import static org.apache.dolphinscheduler.common.constants.Constants.TASK_LIST;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_DEPENDENT;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SUB_PROCESS;

import org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant;
import org.apache.dolphinscheduler.api.dto.DynamicSubWorkflowDto;
import org.apache.dolphinscheduler.api.dto.gantt.GanttDto;
import org.apache.dolphinscheduler.api.dto.gantt.Task;
import org.apache.dolphinscheduler.api.dto.workflowInstance.WorkflowInstanceQueryRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.LoggerService;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.TaskInstanceService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.CommandKeyConstants;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.placeholder.BusinessTimeUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.RelationSubWorkflow;
import org.apache.dolphinscheduler.dao.entity.ResponseTaskLog;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.RelationSubWorkflowMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceMapDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.utils.WorkflowUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ParametersNode;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.service.expand.CuringParamsService;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * process instance service impl
 */
@Service
@Slf4j
public class ProcessInstanceServiceImpl extends BaseServiceImpl implements ProcessInstanceService {

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
    ProcessInstanceMapper processInstanceMapper;

    @Autowired
    ProcessInstanceDao processInstanceDao;

    @Autowired
    private ProcessInstanceMapDao processInstanceMapDao;

    @Autowired
    ProcessDefinitionMapper processDefineMapper;

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    ExecutorService execService;

    @Autowired
    TaskInstanceMapper taskInstanceMapper;

    @Autowired
    LoggerService loggerService;

    @Autowired
    ProcessDefinitionLogMapper processDefinitionLogMapper;

    @Autowired
    TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    UsersService usersService;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private TaskPluginManager taskPluginManager;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private RelationSubWorkflowMapper relationSubWorkflowMapper;

    @Autowired
    private AlertDao alertDao;

    @Autowired
    private CuringParamsService curingGlobalParamsService;

    /**
     * return top n SUCCESS process instance order by running time which started between startTime and endTime
     */
    @Override
    public Map<String, Object> queryTopNLongestRunningProcessInstance(User loginUser, long projectCode, int size,
                                                                      String startTime, String endTime) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode,
                        ApiFuncIdentificationConstant.WORKFLOW_INSTANCE);
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

        List<ProcessInstance> processInstances = processInstanceMapper.queryTopNProcessInstance(size, start, end,
                WorkflowExecutionStatus.SUCCESS, projectCode);
        result.put(DATA_LIST, processInstances);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query process instance by id
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param processId   process instance id
     * @return process instance detail
     */
    @Override
    public Map<String, Object> queryProcessInstanceById(User loginUser, long projectCode, Integer processId) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode,
                        ApiFuncIdentificationConstant.WORKFLOW_INSTANCE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessInstance processInstance = processService.findProcessInstanceDetailById(processId)
                .orElseThrow(() -> new ServiceException(PROCESS_INSTANCE_NOT_EXIST, processId));

        ProcessDefinition processDefinition =
                processService.findProcessDefinition(processInstance.getProcessDefinitionCode(),
                        processInstance.getProcessDefinitionVersion());

        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            log.error("Process definition does not exist, projectCode:{}.", projectCode);
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, processId);
        } else {
            processInstance.setLocations(processDefinition.getLocations());
            processInstance.setDagData(processService.genDagData(processDefinition));
            result.put(DATA_LIST, processInstance);
            putMsg(result, Status.SUCCESS);
        }

        return result;
    }

    @Override
    public ProcessInstance queryByWorkflowInstanceIdThrowExceptionIfNotFound(Integer workflowInstanceId) {
        ProcessInstance processInstance = processInstanceDao.queryById(workflowInstanceId);
        if (processInstance == null) {
            throw new ServiceException(PROCESS_INSTANCE_NOT_EXIST, workflowInstanceId);
        }
        return processInstance;
    }

    /**
     * query workflow instance by id
     *
     * @param loginUser          login user
     * @param workflowInstanceId workflow instance id
     * @return workflow instance detail
     */
    @Override
    public Map<String, Object> queryProcessInstanceById(User loginUser, Integer workflowInstanceId) {
        ProcessInstance processInstance = processInstanceMapper.selectById(workflowInstanceId);
        ProcessDefinition processDefinition =
                processDefineMapper.queryByCode(processInstance.getProcessDefinitionCode());

        return queryProcessInstanceById(loginUser, processDefinition.getProjectCode(), workflowInstanceId);
    }

    /**
     * paging query process instance list, filtering according to project, process definition, time range, keyword, process status
     *
     * @param loginUser         login user
     * @param projectCode       project code
     * @param processDefineCode process definition code
     * @param pageNo            page number
     * @param pageSize          page size
     * @param searchVal         search value
     * @param stateType         state type
     * @param host              host
     * @param startDate         start time
     * @param endDate           end time
     * @param otherParamsJson   otherParamsJson handle other params
     * @return process instance list
     */
    @Override
    public Result<PageInfo<ProcessInstance>> queryProcessInstanceList(User loginUser,
                                                                      long projectCode,
                                                                      long processDefineCode,
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
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project,
                ApiFuncIdentificationConstant.WORKFLOW_INSTANCE);

        int[] statusArray = null;
        // filter by state
        if (stateType != null) {
            statusArray = new int[]{stateType.getCode()};
        }

        Date start = checkAndParseDateParameters(startDate);
        Date end = checkAndParseDateParameters(endDate);

        Page<ProcessInstance> page = new Page<>(pageNo, pageSize);
        PageInfo<ProcessInstance> pageInfo = new PageInfo<>(pageNo, pageSize);

        IPage<ProcessInstance> processInstanceList = processInstanceMapper.queryProcessInstanceListPaging(
                page,
                project.getCode(),
                processDefineCode,
                searchVal,
                executorName,
                statusArray,
                host,
                start,
                end);

        List<ProcessInstance> processInstances = processInstanceList.getRecords();
        List<Integer> userIds = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(processInstances)) {
            userIds = processInstances.stream().map(ProcessInstance::getExecutorId).collect(Collectors.toList());
        }
        List<User> users = usersService.queryUser(userIds);
        Map<Integer, User> idToUserMap = Collections.emptyMap();
        if (CollectionUtils.isNotEmpty(users)) {
            idToUserMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        }

        for (ProcessInstance processInstance : processInstances) {
            processInstance.setDuration(WorkflowUtils.getWorkflowInstanceDuration(processInstance));
            User executor = idToUserMap.get(processInstance.getExecutorId());
            if (null != executor) {
                processInstance.setExecutorName(executor.getUserName());
            }
        }

        pageInfo.setTotal((int) processInstanceList.getTotal());
        pageInfo.setTotalList(processInstances);
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * paging query process instance list, filtering according to project, process definition, time range, keyword, process status
     *
     * @param loginUser                    login user
     * @param workflowInstanceQueryRequest workflowInstanceQueryRequest
     * @return process instance list
     */
    @Override
    public Result queryProcessInstanceList(User loginUser, WorkflowInstanceQueryRequest workflowInstanceQueryRequest) {
        Result result = new Result();
        ProcessInstance processInstance = workflowInstanceQueryRequest.convert2ProcessInstance();
        String projectName = workflowInstanceQueryRequest.getProjectName();
        if (!StringUtils.isBlank(projectName)) {
            Project project = projectMapper.queryByName(projectName);
            projectService.checkProjectAndAuthThrowException(loginUser, project,
                    ApiFuncIdentificationConstant.WORKFLOW_DEFINITION);
            ProcessDefinition processDefinition =
                    processDefineMapper.queryByDefineName(project.getCode(), processInstance.getName());
            processInstance.setProcessDefinitionCode(processDefinition.getCode());
            processInstance.setProjectCode(project.getCode());
        }

        Page<ProcessInstance> page =
                new Page<>(workflowInstanceQueryRequest.getPageNo(), workflowInstanceQueryRequest.getPageSize());
        PageInfo<ProcessInstance> pageInfo =
                new PageInfo<>(workflowInstanceQueryRequest.getPageNo(), workflowInstanceQueryRequest.getPageSize());

        IPage<ProcessInstance> processInstanceList = processInstanceMapper.queryProcessInstanceListV2Paging(
                page,
                processInstance.getProjectCode(),
                processInstance.getProcessDefinitionCode(),
                processInstance.getName(),
                workflowInstanceQueryRequest.getStartTime(),
                workflowInstanceQueryRequest.getEndTime(),
                workflowInstanceQueryRequest.getState(),
                processInstance.getHost());

        List<ProcessInstance> processInstances = processInstanceList.getRecords();
        List<Integer> userIds = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(processInstances)) {
            userIds = processInstances.stream().map(ProcessInstance::getExecutorId).collect(Collectors.toList());
        }
        List<User> users = usersService.queryUser(userIds);
        Map<Integer, User> idToUserMap = Collections.emptyMap();
        if (CollectionUtils.isNotEmpty(users)) {
            idToUserMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        }

        for (ProcessInstance Instance : processInstances) {
            Instance.setDuration(WorkflowUtils.getWorkflowInstanceDuration(Instance));
            User executor = idToUserMap.get(Instance.getExecutorId());
            if (null != executor) {
                Instance.setExecutorName(executor.getUserName());
            }
        }

        pageInfo.setTotal((int) processInstanceList.getTotal());
        pageInfo.setTotalList(processInstances);
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query task list by process instance id
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param processId   process instance id
     * @return task list for the process instance
     * @throws IOException io exception
     */
    @Override
    public Map<String, Object> queryTaskListByProcessId(User loginUser, long projectCode,
                                                        Integer processId) throws IOException {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode,
                        ApiFuncIdentificationConstant.WORKFLOW_INSTANCE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessInstance processInstance = processService.findProcessInstanceDetailById(processId)
                .orElseThrow(() -> new ServiceException(PROCESS_INSTANCE_NOT_EXIST, processId));
        ProcessDefinition processDefinition =
                processDefineMapper.queryByCode(processInstance.getProcessDefinitionCode());
        if (processDefinition != null && projectCode != processDefinition.getProjectCode()) {
            log.error("Process definition does not exist, projectCode:{}, processDefinitionId:{}.", projectCode,
                    processId);
            putMsg(result, PROCESS_INSTANCE_NOT_EXIST, processId);
            return result;
        }
        List<TaskInstance> taskInstanceList =
                taskInstanceDao.queryValidTaskListByWorkflowInstanceId(processId, processInstance.getTestFlag());
        addDependResultForTaskList(loginUser, taskInstanceList);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(PROCESS_INSTANCE_STATE, processInstance.getState().toString());
        resultMap.put(TASK_LIST, taskInstanceList);
        result.put(DATA_LIST, resultMap);

        putMsg(result, Status.SUCCESS);
        return result;
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

        if (!taskInstance.isDynamic()) {
            putMsg(result, Status.TASK_INSTANCE_NOT_DYNAMIC_TASK, taskInstance.getName());
            throw new ServiceException(Status.TASK_INSTANCE_NOT_EXISTS, taskId);
        }
        List<RelationSubWorkflow> relationSubWorkflows = relationSubWorkflowMapper
                .queryAllSubProcessInstance((long) taskInstance.getProcessInstanceId(),
                        taskInstance.getTaskCode());
        List<Long> allSubProcessInstanceId = relationSubWorkflows.stream()
                .map(RelationSubWorkflow::getSubWorkflowInstanceId).collect(java.util.stream.Collectors.toList());
        List<ProcessInstance> allSubWorkflows = processInstanceDao.queryByIds(allSubProcessInstanceId);

        if (allSubWorkflows == null || allSubWorkflows.isEmpty()) {
            putMsg(result, Status.SUB_PROCESS_INSTANCE_NOT_EXIST, taskId);
            throw new ServiceException(Status.SUB_PROCESS_INSTANCE_NOT_EXIST, taskId);
        }
        Long subWorkflowCode = allSubWorkflows.get(0).getProcessDefinitionCode();
        int subWorkflowVersion = allSubWorkflows.get(0).getProcessDefinitionVersion();
        ProcessDefinition subProcessDefinition =
                processService.findProcessDefinition(subWorkflowCode, subWorkflowVersion);
        if (subProcessDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, subWorkflowCode);
            throw new ServiceException(Status.PROCESS_DEFINE_NOT_EXIST, subWorkflowCode);
        }

        allSubWorkflows.sort(Comparator.comparing(ProcessInstance::getId));

        List<DynamicSubWorkflowDto> allDynamicSubWorkflowDtos = new ArrayList<>();
        int index = 1;
        for (ProcessInstance processInstance : allSubWorkflows) {
            DynamicSubWorkflowDto dynamicSubWorkflowDto = new DynamicSubWorkflowDto();
            dynamicSubWorkflowDto.setProcessInstanceId(processInstance.getId());
            dynamicSubWorkflowDto.setIndex(index);
            dynamicSubWorkflowDto.setState(processInstance.getState());
            dynamicSubWorkflowDto.setName(subProcessDefinition.getName());
            Map<String, String> commandParamMap = JSONUtils.toMap(processInstance.getCommandParam());
            String parameter = commandParamMap.get(CommandKeyConstants.CMD_DYNAMIC_START_PARAMS);
            dynamicSubWorkflowDto.setParameters(JSONUtils.toMap(parameter));
            allDynamicSubWorkflowDtos.add(dynamicSubWorkflowDto);
            index++;

        }

        return allDynamicSubWorkflowDtos;
    }

    /**
     * add dependent result for dependent task
     */
    private void addDependResultForTaskList(User loginUser, List<TaskInstance> taskInstanceList) throws IOException {
        for (TaskInstance taskInstance : taskInstanceList) {
            if (TASK_TYPE_DEPENDENT.equalsIgnoreCase(taskInstance.getTaskType())) {
                log.info("DEPENDENT type task instance need to set dependent result, taskCode:{}, taskInstanceId:{}",
                        taskInstance.getTaskCode(), taskInstance.getId());
                // TODO The result of dependent item should not be obtained from the log, waiting for optimization.
                Result<ResponseTaskLog> logResult = loggerService.queryLog(loginUser,
                        taskInstance.getId(), Constants.LOG_QUERY_SKIP_LINE_NUMBER, Constants.LOG_QUERY_LIMIT);
                if (logResult.getCode() == Status.SUCCESS.ordinal()) {
                    String log = logResult.getData().getMessage();
                    Map<String, DependResult> resultMap = parseLogForDependentResult(log);
                    taskInstance.setDependentResult(JSONUtils.toJsonString(resultMap));
                }
            }
        }
    }

    @Override
    public Map<String, DependResult> parseLogForDependentResult(String content) throws IOException {
        Map<String, DependResult> resultMap = new HashMap<>();
        if (StringUtils.isEmpty(content)) {
            log.warn("Log content is empty.");
            return resultMap;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes(
                StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains(DEPENDENT_SPLIT)) {
                String[] tmpStringArray = line.split(":\\|\\|");
                if (tmpStringArray.length != 2) {
                    continue;
                }
                String dependResultString = tmpStringArray[1];
                String[] dependStringArray = dependResultString.split(",");
                if (dependStringArray.length != 3) {
                    continue;
                }
                String key = dependStringArray[0].trim().split(":")[1].trim();
                String result = dependStringArray[1].trim().split(":")[1].trim();
                DependResult dependResult = DependResult.valueOf(result);
                resultMap.put(key, dependResult);
            }
        }
        return resultMap;
    }

    /**
     * query sub process instance detail info by task id
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param taskId      task id
     * @return sub process instance detail
     */
    @Override
    public Map<String, Object> querySubProcessInstanceByTaskId(User loginUser, long projectCode, Integer taskId) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode,
                        ApiFuncIdentificationConstant.WORKFLOW_INSTANCE);
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

        if (!taskInstance.isSubProcess()) {
            log.warn("Task instance is not {} type instance, projectCode:{}, taskInstanceId:{}.",
                    TASK_TYPE_SUB_PROCESS, projectCode, taskId);
            putMsg(result, Status.TASK_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE, taskInstance.getName());
            return result;
        }

        ProcessInstance subWorkflowInstance = processService.findSubProcessInstance(
                taskInstance.getProcessInstanceId(), taskInstance.getId());
        if (subWorkflowInstance == null) {
            log.error("SubProcess instance does not exist, projectCode:{}, taskInstanceId:{}.", projectCode,
                    taskInstance.getId());
            putMsg(result, Status.SUB_PROCESS_INSTANCE_NOT_EXIST, taskId);
            return result;
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(Constants.SUBPROCESS_INSTANCE_ID, subWorkflowInstance.getId());
        result.put(DATA_LIST, dataMap);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * update process instance
     *
     * @param loginUser          login user
     * @param projectCode        project code
     * @param taskRelationJson   process task relation json
     * @param taskDefinitionJson taskDefinitionJson
     * @param processInstanceId  process instance id
     * @param scheduleTime       schedule time
     * @param syncDefine         sync define
     * @param globalParams       global params
     * @param locations          locations for nodes
     * @param timeout            timeout
     * @return update result code
     */
    @Transactional
    @Override
    public Map<String, Object> updateProcessInstance(User loginUser, long projectCode, Integer processInstanceId,
                                                     String taskRelationJson,
                                                     String taskDefinitionJson, String scheduleTime, Boolean syncDefine,
                                                     String globalParams,
                                                     String locations, int timeout) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode,
                        ApiFuncIdentificationConstant.INSTANCE_UPDATE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        // check process instance exists
        ProcessInstance processInstance = processService.findProcessInstanceDetailById(processInstanceId)
                .orElseThrow(() -> new ServiceException(PROCESS_INSTANCE_NOT_EXIST, processInstanceId));
        // check process instance exists in project
        ProcessDefinition processDefinition0 =
                processDefineMapper.queryByCode(processInstance.getProcessDefinitionCode());
        if (processDefinition0 != null && projectCode != processDefinition0.getProjectCode()) {
            log.error("Process definition does not exist, projectCode:{}, processDefinitionCode:{}.", projectCode,
                    processInstance.getProcessDefinitionCode());
            putMsg(result, PROCESS_INSTANCE_NOT_EXIST, processInstanceId);
            return result;
        }
        // check process instance status
        if (!processInstance.getState().isFinished()) {
            log.warn("Process Instance state is {} so can not update process instance, processInstanceId:{}.",
                    processInstance.getState().getDesc(), processInstanceId);
            putMsg(result, PROCESS_INSTANCE_STATE_OPERATION_ERROR,
                    processInstance.getName(), processInstance.getState().toString(), "update");
            return result;
        }

        //
        Map<String, String> commandParamMap = JSONUtils.toMap(processInstance.getCommandParam());
        String timezoneId = null;
        if (commandParamMap == null || StringUtils.isBlank(commandParamMap.get(Constants.SCHEDULE_TIMEZONE))) {
            timezoneId = loginUser.getTimeZone();
        } else {
            timezoneId = commandParamMap.get(Constants.SCHEDULE_TIMEZONE);
        }

        setProcessInstance(processInstance, scheduleTime, globalParams, timeout, timezoneId);
        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);
        if (taskDefinitionLogs.isEmpty()) {
            log.warn("Parameter taskDefinitionJson is empty");
            putMsg(result, Status.DATA_IS_NOT_VALID, taskDefinitionJson);
            return result;
        }
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
            if (!taskPluginManager.checkTaskParameters(ParametersNode.builder()
                    .taskType(taskDefinitionLog.getTaskType())
                    .taskParams(taskDefinitionLog.getTaskParams())
                    .dependence(taskDefinitionLog.getDependence())
                    .build())) {
                log.error("Task parameters are invalid,  taskDefinitionName:{}.", taskDefinitionLog.getName());
                putMsg(result, Status.PROCESS_NODE_S_PARAMETER_INVALID, taskDefinitionLog.getName());
                return result;
            }
        }
        int saveTaskResult = processService.saveTaskDefine(loginUser, projectCode, taskDefinitionLogs, syncDefine);
        if (saveTaskResult == Constants.DEFINITION_FAILURE) {
            log.error("Update task definition error, projectCode:{}, processInstanceId:{}", projectCode,
                    processInstanceId);
            putMsg(result, Status.UPDATE_TASK_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_TASK_DEFINITION_ERROR);
        }
        ProcessDefinition processDefinition =
                processDefineMapper.queryByCode(processInstance.getProcessDefinitionCode());
        List<ProcessTaskRelationLog> taskRelationList =
                JSONUtils.toList(taskRelationJson, ProcessTaskRelationLog.class);
        // check workflow json is valid
        result = processDefinitionService.checkProcessNodeList(taskRelationJson, taskDefinitionLogs);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        processDefinition.set(projectCode, processDefinition.getName(), processDefinition.getDescription(),
                globalParams, locations, timeout);
        processDefinition.setUpdateTime(new Date());
        int insertVersion = processService.saveProcessDefine(loginUser, processDefinition, syncDefine, Boolean.FALSE);
        if (insertVersion == 0) {
            log.error("Update process definition error, projectCode:{}, processDefinitionName:{}.", projectCode,
                    processDefinition.getName());
            putMsg(result, Status.UPDATE_PROCESS_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_PROCESS_DEFINITION_ERROR);
        } else
            log.info("Update process definition complete, projectCode:{}, processDefinitionName:{}.", projectCode,
                    processDefinition.getName());
        int insertResult = processService.saveTaskRelation(loginUser, processDefinition.getProjectCode(),
                processDefinition.getCode(), insertVersion, taskRelationList, taskDefinitionLogs, syncDefine);
        if (insertResult == Constants.EXIT_CODE_SUCCESS) {
            log.info(
                    "Update task relations complete, projectCode:{}, processDefinitionCode:{}, processDefinitionVersion:{}.",
                    projectCode, processDefinition.getCode(), insertVersion);
            putMsg(result, Status.SUCCESS);
            result.put(Constants.DATA_LIST, processDefinition);
        } else {
            log.info(
                    "Update task relations error, projectCode:{}, processDefinitionCode:{}, processDefinitionVersion:{}.",
                    projectCode, processDefinition.getCode(), insertVersion);
            putMsg(result, Status.UPDATE_PROCESS_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_PROCESS_DEFINITION_ERROR);
        }
        processInstance.setProcessDefinitionVersion(insertVersion);
        boolean update = processInstanceDao.updateById(processInstance);
        if (!update) {
            log.error(
                    "Update process instance version error, projectCode:{}, processDefinitionCode:{}, processDefinitionVersion:{}",
                    projectCode, processDefinition.getCode(), insertVersion);
            putMsg(result, Status.UPDATE_PROCESS_INSTANCE_ERROR);
            throw new ServiceException(Status.UPDATE_PROCESS_INSTANCE_ERROR);
        }
        log.info(
                "Update process instance complete, projectCode:{}, processDefinitionCode:{}, processDefinitionVersion:{}, processInstanceId:{}",
                projectCode, processDefinition.getCode(), insertVersion, processInstanceId);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * update process instance attributes
     */
    private void setProcessInstance(ProcessInstance processInstance, String scheduleTime,
                                    String globalParams, int timeout, String timezone) {
        Date schedule = processInstance.getScheduleTime();
        if (scheduleTime != null) {
            schedule = DateUtils.stringToDate(scheduleTime);
        }
        processInstance.setScheduleTime(schedule);
        List<Property> globalParamList = JSONUtils.toList(globalParams, Property.class);
        Map<String, String> globalParamMap =
                globalParamList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));
        globalParams = curingGlobalParamsService.curingGlobalParams(processInstance.getId(), globalParamMap,
                globalParamList, processInstance.getCmdTypeIfComplement(), schedule, timezone);
        processInstance.setTimeout(timeout);
        processInstance.setGlobalParams(globalParams);
    }

    /**
     * query parent process instance detail info by sub process instance id
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param subId       sub process id
     * @return parent instance detail
     */
    @Override
    public Map<String, Object> queryParentInstanceBySubId(User loginUser, long projectCode, Integer subId) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode,
                        ApiFuncIdentificationConstant.WORKFLOW_INSTANCE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        ProcessInstance subInstance = processService.findProcessInstanceDetailById(subId)
                .orElseThrow(() -> new ServiceException(PROCESS_INSTANCE_NOT_EXIST, subId));
        if (subInstance.getIsSubProcess() == Flag.NO) {
            log.warn(
                    "Process instance is not sub process instance type, processInstanceId:{}, processInstanceName:{}.",
                    subId, subInstance.getName());
            putMsg(result, Status.PROCESS_INSTANCE_NOT_SUB_PROCESS_INSTANCE, subInstance.getName());
            return result;
        }

        ProcessInstance parentWorkflowInstance = processService.findParentProcessInstance(subId);
        if (parentWorkflowInstance == null) {
            log.error("Parent process instance does not exist, projectCode:{}, subProcessInstanceId:{}.",
                    projectCode, subId);
            putMsg(result, Status.SUB_PROCESS_INSTANCE_NOT_EXIST);
            return result;
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(Constants.PARENT_WORKFLOW_INSTANCE, parentWorkflowInstance.getId());
        result.put(DATA_LIST, dataMap);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * delete process instance by id, at the same time，delete task instance and their mapping relation data
     *
     * @param loginUser         login user
     * @param processInstanceId process instance id
     * @return delete result code
     */
    @Override
    @Transactional
    public void deleteProcessInstanceById(User loginUser, Integer processInstanceId) {
        ProcessInstance processInstance = processService.findProcessInstanceDetailById(processInstanceId)
                .orElseThrow(() -> new ServiceException(PROCESS_INSTANCE_NOT_EXIST, processInstanceId));
        ProcessDefinition processDefinition = processDefinitionLogMapper.queryByDefinitionCodeAndVersion(
                processInstance.getProcessDefinitionCode(), processInstance.getProcessDefinitionVersion());

        Project project = projectMapper.queryByCode(processDefinition.getProjectCode());
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project,
                ApiFuncIdentificationConstant.INSTANCE_DELETE);
        // check process instance status
        if (!processInstance.getState().isFinished()) {
            log.warn("Process Instance state is {} so can not delete process instance, processInstanceId:{}.",
                    processInstance.getState().getDesc(), processInstanceId);
            throw new ServiceException(PROCESS_INSTANCE_STATE_OPERATION_ERROR, processInstance.getName(),
                    processInstance.getState(), "delete");
        }
        deleteProcessInstanceById(processInstanceId);
    }

    /**
     * view process instance variables
     *
     * @param projectCode       project code
     * @param processInstanceId process instance id
     * @return variables data
     */
    @Override
    public Map<String, Object> viewVariables(long projectCode, Integer processInstanceId) {
        Map<String, Object> result = new HashMap<>();

        ProcessInstance processInstance = processInstanceMapper.queryDetailById(processInstanceId);

        if (processInstance == null) {
            log.error("Process instance does not exist, projectCode:{}, processInstanceId:{}.", projectCode,
                    processInstanceId);
            putMsg(result, Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceId);
            return result;
        }

        ProcessDefinition processDefinition =
                processDefineMapper.queryByCode(processInstance.getProcessDefinitionCode());
        if (processDefinition != null && projectCode != processDefinition.getProjectCode()) {
            log.error("Process definition does not exist, projectCode:{}, processDefinitionCode:{}.", projectCode,
                    processInstance.getProcessDefinitionCode());
            putMsg(result, PROCESS_INSTANCE_NOT_EXIST, processInstanceId);
            return result;
        }

        Map<String, String> commandParam = JSONUtils.toMap(processInstance.getCommandParam());
        String timezone = null;
        if (commandParam != null) {
            timezone = commandParam.get(Constants.SCHEDULE_TIMEZONE);
        }
        Map<String, String> timeParams = BusinessTimeUtils
                .getBusinessTime(processInstance.getCmdTypeIfComplement(),
                        processInstance.getScheduleTime(), timezone);
        String userDefinedParams = processInstance.getGlobalParams();
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

        Map<String, Map<String, Object>> localUserDefParams = getLocalParams(processInstance, timeParams);

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
    private Map<String, Map<String, Object>> getLocalParams(ProcessInstance processInstance,
                                                            Map<String, String> timeParams) {
        Map<String, Map<String, Object>> localUserDefParams = new HashMap<>();
        List<TaskInstance> taskInstanceList =
                taskInstanceMapper.findValidTaskListByProcessId(processInstance.getId(), Flag.YES,
                        processInstance.getTestFlag());
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
     * @param processInstanceId process instance id
     * @return gantt tree data
     * @throws Exception exception when json parse
     */
    @Override
    public Map<String, Object> viewGantt(long projectCode, Integer processInstanceId) throws Exception {
        Map<String, Object> result = new HashMap<>();
        ProcessInstance processInstance = processInstanceMapper.queryDetailById(processInstanceId);

        if (processInstance == null) {
            log.error("Process instance does not exist, projectCode:{}, processInstanceId:{}.", projectCode,
                    processInstanceId);
            putMsg(result, Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceId);
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionLogMapper.queryByDefinitionCodeAndVersion(
                processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion());
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            log.error("Process definition does not exist, projectCode:{}, processDefinitionCode:{}.", projectCode,
                    processInstance.getProcessDefinitionCode());
            putMsg(result, PROCESS_INSTANCE_NOT_EXIST, processInstanceId);
            return result;
        }
        GanttDto ganttDto = new GanttDto();
        DAG<Long, TaskNode, TaskNodeRelation> dag = processService.genDagGraph(processDefinition);
        // topological sort
        List<Long> nodeList = dag.topologicalSort();

        ganttDto.setTaskNames(nodeList);

        List<Task> taskList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(nodeList)) {
            List<TaskInstance> taskInstances = taskInstanceMapper.queryByProcessInstanceIdsAndTaskCodes(
                    Collections.singletonList(processInstanceId), nodeList);
            for (Long node : nodeList) {
                TaskInstance taskInstance = null;
                for (TaskInstance instance : taskInstances) {
                    if (instance.getProcessInstanceId() == processInstanceId && instance.getTaskCode() == node) {
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
     * query process instance by processDefinitionCode and stateArray
     *
     * @param processDefinitionCode processDefinitionCode
     * @param states                states array
     * @return process instance list
     */
    @Override
    public List<ProcessInstance> queryByProcessDefineCodeAndStatus(Long processDefinitionCode, int[] states) {
        return processInstanceMapper.queryByProcessDefineCodeAndStatus(processDefinitionCode, states);
    }

    /**
     * query process instance by processDefinitionCode
     *
     * @param processDefinitionCode processDefinitionCode
     * @param size                  size
     * @return process instance list
     */
    @Override
    public List<ProcessInstance> queryByProcessDefineCode(Long processDefinitionCode, int size) {
        return processInstanceMapper.queryByProcessDefineCode(processDefinitionCode, size);
    }

    /**
     * query process instance list bt trigger code
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

        List<ProcessInstance> processInstances = processInstanceMapper.queryByTriggerCode(
                triggerCode);
        result.put(DATA_LIST, processInstances);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public void deleteProcessInstanceByWorkflowDefinitionCode(long workflowDefinitionCode) {
        while (true) {
            List<ProcessInstance> processInstances =
                    processInstanceMapper.queryByProcessDefineCode(workflowDefinitionCode, 100);
            if (CollectionUtils.isEmpty(processInstances)) {
                break;
            }
            log.info("Begin to delete workflow instance, workflow definition code: {}", workflowDefinitionCode);
            for (ProcessInstance processInstance : processInstances) {
                if (!processInstance.getState().isFinished()) {
                    log.warn("Workflow instance is not finished cannot delete, process instance id:{}",
                            processInstance.getId());
                    throw new ServiceException(PROCESS_INSTANCE_STATE_OPERATION_ERROR, processInstance.getName(),
                            processInstance.getState(), "delete");
                }
                deleteProcessInstanceById(processInstance.getId());
            }
            log.info("Success delete workflow instance, workflow definition code: {}, size: {}",
                    workflowDefinitionCode, processInstances.size());
        }
    }

    @Override
    public void deleteProcessInstanceById(int workflowInstanceId) {
        // delete task instance
        taskInstanceService.deleteByWorkflowInstanceId(workflowInstanceId);
        // delete sub process instances
        deleteSubWorkflowInstanceIfNeeded(workflowInstanceId);
        // delete alert
        alertDao.deleteByWorkflowInstanceId(workflowInstanceId);
        // delete process instance
        processInstanceDao.deleteById(workflowInstanceId);
    }

    private void deleteSubWorkflowInstanceIfNeeded(int workflowInstanceId) {
        List<Integer> subWorkflowInstanceIds = processInstanceMapDao.querySubWorkflowInstanceIds(workflowInstanceId);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(subWorkflowInstanceIds)) {
            return;
        }
        for (Integer subWorkflowInstanceId : subWorkflowInstanceIds) {
            deleteProcessInstanceById(subWorkflowInstanceId);
        }
        processInstanceMapDao.deleteByParentId(workflowInstanceId);
    }
}
