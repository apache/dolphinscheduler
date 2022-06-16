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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.api.dto.gantt.GanttDto;
import org.apache.dolphinscheduler.api.dto.gantt.Task;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.LoggerService;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.placeholder.BusinessTimeUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ParametersNode;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.task.TaskPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.INSTANCE_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.INSTANCE_UPDATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_INSTANCE;
import static org.apache.dolphinscheduler.common.Constants.DATA_LIST;
import static org.apache.dolphinscheduler.common.Constants.DEPENDENT_SPLIT;
import static org.apache.dolphinscheduler.common.Constants.GLOBAL_PARAMS;
import static org.apache.dolphinscheduler.common.Constants.LOCAL_PARAMS;
import static org.apache.dolphinscheduler.common.Constants.PROCESS_INSTANCE_STATE;
import static org.apache.dolphinscheduler.common.Constants.TASK_LIST;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_DEPENDENT;

/**
 * process instance service impl
 */
@Service
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
    ProcessInstanceMapper processInstanceMapper;

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

    /**
     * return top n SUCCESS process instance order by running time which started between startTime and endTime
     */
    @Override
    public Map<String, Object> queryTopNLongestRunningProcessInstance(User loginUser, long projectCode, int size, String startTime, String endTime) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_INSTANCE);
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

        List<ProcessInstance> processInstances = processInstanceMapper.queryTopNProcessInstance(size, start, end, ExecutionStatus.SUCCESS, projectCode);
        result.put(DATA_LIST, processInstances);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query process instance by id
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processId process instance id
     * @return process instance detail
     */
    @Override
    public Map<String, Object> queryProcessInstanceById(User loginUser, long projectCode, Integer processId) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_INSTANCE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessInstance processInstance = processService.findProcessInstanceDetailById(processId);

        ProcessDefinition processDefinition = processService.findProcessDefinition(processInstance.getProcessDefinitionCode(),
            processInstance.getProcessDefinitionVersion());

        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, processId);
        } else {
            processInstance.setLocations(processDefinition.getLocations());
            processInstance.setDagData(processService.genDagData(processDefinition));
            result.put(DATA_LIST, processInstance);
            putMsg(result, Status.SUCCESS);
        }

        return result;
    }

    /**
     * paging query process instance list, filtering according to project, process definition, time range, keyword, process status
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefineCode process definition code
     * @param pageNo page number
     * @param pageSize page size
     * @param searchVal search value
     * @param stateType state type
     * @param host host
     * @param startDate start time
     * @param endDate end time
     * @param otherParamsJson otherParamsJson handle other params
     * @return process instance list
     */
    @Override
    public Result queryProcessInstanceList(User loginUser, long projectCode, long processDefineCode, String startDate, String endDate, String searchVal, String executorName,
                                           ExecutionStatus stateType, String host, String otherParamsJson, Integer pageNo, Integer pageSize) {

        Result result = new Result();
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            putMsg(result,resultEnum);
            return result;
        }

        int[] statusArray = null;
        // filter by state
        if (stateType != null) {
            statusArray = new int[]{stateType.ordinal()};
        }

        Map<String, Object> checkAndParseDateResult = checkAndParseDateParameters(startDate, endDate);
        resultEnum = (Status) checkAndParseDateResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            putMsg(result,resultEnum);
            return result;
        }
        Date start = (Date) checkAndParseDateResult.get(Constants.START_TIME);
        Date end = (Date) checkAndParseDateResult.get(Constants.END_TIME);

        Page<ProcessInstance> page = new Page<>(pageNo, pageSize);
        PageInfo<ProcessInstance> pageInfo = new PageInfo<>(pageNo, pageSize);
        int executorId = usersService.getUserIdByName(executorName);

        IPage<ProcessInstance> processInstanceList = processInstanceMapper.queryProcessInstanceListPaging(page,
            project.getCode(), processDefineCode, searchVal, executorId, statusArray, host, start, end);

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
            processInstance.setDuration(DateUtils.format2Duration(processInstance.getStartTime(), processInstance.getEndTime()));
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
     * query task list by process instance id
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processId process instance id
     * @return task list for the process instance
     * @throws IOException io exception
     */
    @Override
    public Map<String, Object> queryTaskListByProcessId(User loginUser, long projectCode, Integer processId) throws IOException {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_INSTANCE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessInstance processInstance = processService.findProcessInstanceDetailById(processId);
        ProcessDefinition processDefinition = processDefineMapper.queryByCode(processInstance.getProcessDefinitionCode());
        if (processDefinition != null && projectCode != processDefinition.getProjectCode()) {
            putMsg(result, Status.PROCESS_INSTANCE_NOT_EXIST, processId);
            return result;
        }
        List<TaskInstance> taskInstanceList = processService.findValidTaskListByProcessId(processId);
        addDependResultForTaskList(taskInstanceList);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(PROCESS_INSTANCE_STATE, processInstance.getState().toString());
        resultMap.put(TASK_LIST, taskInstanceList);
        result.put(DATA_LIST, resultMap);

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * add dependent result for dependent task
     */
    private void addDependResultForTaskList(List<TaskInstance> taskInstanceList) throws IOException {
        for (TaskInstance taskInstance : taskInstanceList) {
            if (TASK_TYPE_DEPENDENT.equalsIgnoreCase(taskInstance.getTaskType())) {
                Result<String> logResult = loggerService.queryLog(
                    taskInstance.getId(), Constants.LOG_QUERY_SKIP_LINE_NUMBER, Constants.LOG_QUERY_LIMIT);
                if (logResult.getCode() == Status.SUCCESS.ordinal()) {
                    String log = logResult.getData();
                    Map<String, DependResult> resultMap = parseLogForDependentResult(log);
                    taskInstance.setDependentResult(JSONUtils.toJsonString(resultMap));
                }
            }
        }
    }

    @Override
    public Map<String, DependResult> parseLogForDependentResult(String log) throws IOException {
        Map<String, DependResult> resultMap = new HashMap<>();
        if (StringUtils.isEmpty(log)) {
            return resultMap;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(log.getBytes(
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
                if (dependStringArray.length != 2) {
                    continue;
                }
                String key = dependStringArray[0].trim();
                DependResult dependResult = DependResult.valueOf(dependStringArray[1].trim());
                resultMap.put(key, dependResult);
            }
        }
        return resultMap;
    }

    /**
     * query sub process instance detail info by task id
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskId task id
     * @return sub process instance detail
     */
    @Override
    public Map<String, Object> querySubProcessInstanceByTaskId(User loginUser, long projectCode, Integer taskId) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_INSTANCE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        TaskInstance taskInstance = processService.findTaskInstanceById(taskId);
        if (taskInstance == null) {
            putMsg(result, Status.TASK_INSTANCE_NOT_EXISTS, taskId);
            return result;
        }

        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskInstance.getTaskCode());
        if (taskDefinition != null && projectCode != taskDefinition.getProjectCode()) {
            putMsg(result, Status.TASK_INSTANCE_NOT_EXISTS, taskId);
            return result;
        }

        if (!taskInstance.isSubProcess()) {
            putMsg(result, Status.TASK_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE, taskInstance.getName());
            return result;
        }

        ProcessInstance subWorkflowInstance = processService.findSubProcessInstance(
            taskInstance.getProcessInstanceId(), taskInstance.getId());
        if (subWorkflowInstance == null) {
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
     * @param loginUser login user
     * @param projectCode project code
     * @param taskRelationJson process task relation json
     * @param taskDefinitionJson taskDefinitionJson
     * @param processInstanceId process instance id
     * @param scheduleTime schedule time
     * @param syncDefine sync define
     * @param globalParams global params
     * @param locations locations for nodes
     * @param timeout timeout
     * @param tenantCode tenantCode
     * @return update result code
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> updateProcessInstance(User loginUser, long projectCode, Integer processInstanceId, String taskRelationJson,
                                                     String taskDefinitionJson, String scheduleTime, Boolean syncDefine, String globalParams,
                                                     String locations, int timeout, String tenantCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode,INSTANCE_UPDATE );
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        //check process instance exists
        ProcessInstance processInstance = processService.findProcessInstanceDetailById(processInstanceId);
        if (processInstance == null) {
            putMsg(result, Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceId);
            return result;
        }
        //check process instance exists in project
        ProcessDefinition processDefinition0 = processDefineMapper.queryByCode(processInstance.getProcessDefinitionCode());
        if (processDefinition0 != null && projectCode != processDefinition0.getProjectCode()) {
            putMsg(result, Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceId);
            return result;
        }
        //check process instance status
        if (!processInstance.getState().typeIsFinished()) {
            putMsg(result, Status.PROCESS_INSTANCE_STATE_OPERATION_ERROR,
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

        setProcessInstance(processInstance, tenantCode, scheduleTime, globalParams, timeout, timezoneId);
        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);
        if (taskDefinitionLogs.isEmpty()) {
            putMsg(result, Status.DATA_IS_NOT_VALID, taskDefinitionJson);
            return result;
        }
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
            if (!taskPluginManager.checkTaskParameters(ParametersNode.builder()
                    .taskType(taskDefinitionLog.getTaskType())
                    .taskParams(taskDefinitionLog.getTaskParams())
                    .dependence(taskDefinitionLog.getDependence())
                    .build())) {
                putMsg(result, Status.PROCESS_NODE_S_PARAMETER_INVALID, taskDefinitionLog.getName());
                return result;
            }
        }
        int saveTaskResult = processService.saveTaskDefine(loginUser, projectCode, taskDefinitionLogs, syncDefine);
        if (saveTaskResult == Constants.DEFINITION_FAILURE) {
            putMsg(result, Status.UPDATE_TASK_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_TASK_DEFINITION_ERROR);
        }
        ProcessDefinition processDefinition = processDefineMapper.queryByCode(processInstance.getProcessDefinitionCode());
        List<ProcessTaskRelationLog> taskRelationList = JSONUtils.toList(taskRelationJson, ProcessTaskRelationLog.class);
        //check workflow json is valid
        result = processDefinitionService.checkProcessNodeList(taskRelationJson, taskDefinitionLogs);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        int tenantId = -1;
        if (!Constants.DEFAULT.equals(tenantCode)) {
            Tenant tenant = tenantMapper.queryByTenantCode(tenantCode);
            if (tenant == null) {
                putMsg(result, Status.TENANT_NOT_EXIST);
                return result;
            }
            tenantId = tenant.getId();
        }
        processDefinition.set(projectCode, processDefinition.getName(), processDefinition.getDescription(), globalParams, locations, timeout, tenantId);
        processDefinition.setUpdateTime(new Date());
        int insertVersion = processService.saveProcessDefine(loginUser, processDefinition, syncDefine, Boolean.FALSE);
        if (insertVersion == 0) {
            putMsg(result, Status.UPDATE_PROCESS_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_PROCESS_DEFINITION_ERROR);
        }
        int insertResult = processService.saveTaskRelation(loginUser, processDefinition.getProjectCode(),
            processDefinition.getCode(), insertVersion, taskRelationList, taskDefinitionLogs, syncDefine);
        if (insertResult == Constants.EXIT_CODE_SUCCESS) {
            putMsg(result, Status.SUCCESS);
            result.put(Constants.DATA_LIST, processDefinition);
        } else {
            putMsg(result, Status.UPDATE_PROCESS_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_PROCESS_DEFINITION_ERROR);
        }
        processInstance.setProcessDefinitionVersion(insertVersion);
        int update = processService.updateProcessInstance(processInstance);
        if (update == 0) {
            putMsg(result, Status.UPDATE_PROCESS_INSTANCE_ERROR);
            throw new ServiceException(Status.UPDATE_PROCESS_INSTANCE_ERROR);
        }
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * update process instance attributes
     */
    private void setProcessInstance(ProcessInstance processInstance, String tenantCode, String scheduleTime, String globalParams, int timeout, String timezone) {
        Date schedule = processInstance.getScheduleTime();
        if (scheduleTime != null) {
            schedule = DateUtils.getScheduleDate(scheduleTime);
        }
        processInstance.setScheduleTime(schedule);
        List<Property> globalParamList = JSONUtils.toList(globalParams, Property.class);
        Map<String, String> globalParamMap = globalParamList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));
        globalParams = ParameterUtils.curingGlobalParams(globalParamMap, globalParamList, processInstance.getCmdTypeIfComplement(), schedule, timezone);
        processInstance.setTimeout(timeout);
        processInstance.setTenantCode(tenantCode);
        processInstance.setGlobalParams(globalParams);
    }

    /**
     * query parent process instance detail info by sub process instance id
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param subId sub process id
     * @return parent instance detail
     */
    @Override
    public Map<String, Object> queryParentInstanceBySubId(User loginUser, long projectCode, Integer subId) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_INSTANCE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        ProcessInstance subInstance = processService.findProcessInstanceDetailById(subId);
        if (subInstance == null) {
            putMsg(result, Status.PROCESS_INSTANCE_NOT_EXIST, subId);
            return result;
        }
        if (subInstance.getIsSubProcess() == Flag.NO) {
            putMsg(result, Status.PROCESS_INSTANCE_NOT_SUB_PROCESS_INSTANCE, subInstance.getName());
            return result;
        }

        ProcessInstance parentWorkflowInstance = processService.findParentProcessInstance(subId);
        if (parentWorkflowInstance == null) {
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
     * @param loginUser login user
     * @param projectCode project code
     * @param processInstanceId process instance id
     * @return delete result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, Object> deleteProcessInstanceById(User loginUser, long projectCode, Integer processInstanceId) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode,INSTANCE_DELETE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessInstance processInstance = processService.findProcessInstanceDetailById(processInstanceId);
        if (null == processInstance) {
            putMsg(result, Status.PROCESS_INSTANCE_NOT_EXIST, String.valueOf(processInstanceId));
            return result;
        }
        //check process instance status
        if (!processInstance.getState().typeIsFinished()) {
            putMsg(result, Status.PROCESS_INSTANCE_STATE_OPERATION_ERROR,
                    processInstance.getName(), processInstance.getState().toString(), "delete");
            return result;
        }

        ProcessDefinition processDefinition = processDefineMapper.queryByCode(processInstance.getProcessDefinitionCode());
        if (processDefinition != null && projectCode != processDefinition.getProjectCode()) {
            putMsg(result, Status.PROCESS_INSTANCE_NOT_EXIST, String.valueOf(processInstanceId));
            return result;
        }

        try {
            processService.removeTaskLogFile(processInstanceId);
        } catch (Exception ignore) {
            // ignore
        }

        // delete database cascade
        int delete = processService.deleteWorkProcessInstanceById(processInstanceId);

        processService.deleteAllSubWorkProcessByParentId(processInstanceId);
        processService.deleteWorkProcessMapByParentId(processInstanceId);
        processService.deleteWorkTaskInstanceByProcessInstanceId(processInstanceId);

        if (delete > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.DELETE_PROCESS_INSTANCE_BY_ID_ERROR);
            throw new ServiceException(Status.DELETE_PROCESS_INSTANCE_BY_ID_ERROR);
        }

        return result;
    }

    /**
     * view process instance variables
     *
     * @param projectCode project code
     * @param processInstanceId process instance id
     * @return variables data
     */
    @Override
    public Map<String, Object> viewVariables(long projectCode, Integer processInstanceId) {
        Map<String, Object> result = new HashMap<>();

        ProcessInstance processInstance = processInstanceMapper.queryDetailById(processInstanceId);

        if (processInstance == null) {
            throw new RuntimeException("workflow instance is null");
        }

        ProcessDefinition processDefinition = processDefineMapper.queryByCode(processInstance.getProcessDefinitionCode());
        if (processDefinition != null && projectCode != processDefinition.getProjectCode()) {
            putMsg(result, Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceId);
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
        String globalParamStr = ParameterUtils.convertParameterPlaceholders(JSONUtils.toJsonString(globalParams), timeParams);
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
    private Map<String, Map<String, Object>> getLocalParams(ProcessInstance processInstance, Map<String, String> timeParams) {
        Map<String, Map<String, Object>> localUserDefParams = new HashMap<>();
        List<TaskInstance> taskInstanceList = taskInstanceMapper.findValidTaskListByProcessId(processInstance.getId(), Flag.YES);
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
     * @param projectCode project code
     * @param processInstanceId process instance id
     * @return gantt tree data
     * @throws Exception exception when json parse
     */
    @Override
    public Map<String, Object> viewGantt(long projectCode, Integer processInstanceId) throws Exception {
        Map<String, Object> result = new HashMap<>();
        ProcessInstance processInstance = processInstanceMapper.queryDetailById(processInstanceId);

        if (processInstance == null) {
            throw new RuntimeException("workflow instance is null");
        }

        ProcessDefinition processDefinition = processDefinitionLogMapper.queryByDefinitionCodeAndVersion(
            processInstance.getProcessDefinitionCode(),
            processInstance.getProcessDefinitionVersion()
        );
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            putMsg(result, Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceId);
            return result;
        }
        GanttDto ganttDto = new GanttDto();
        DAG<String, TaskNode, TaskNodeRelation> dag = processService.genDagGraph(processDefinition);
        //topological sort
        List<String> nodeList = dag.topologicalSort();

        ganttDto.setTaskNames(nodeList);

        List<Task> taskList = new ArrayList<>();
        for (String node : nodeList) {
            TaskInstance taskInstance = taskInstanceMapper.queryByInstanceIdAndCode(processInstanceId, Long.parseLong(node));
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
            task.setStatus(taskInstance.getState().toString());
            task.setExecutionDate(taskInstance.getStartTime());
            task.setDuration(DateUtils.format2Readable(endTime.getTime() - startTime.getTime()));
            taskList.add(task);
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
     * @param states states array
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
     * @param size size
     * @return process instance list
     */
    @Override
    public List<ProcessInstance> queryByProcessDefineCode(Long processDefinitionCode, int size) {
        return processInstanceMapper.queryByProcessDefineCode(processDefinitionCode, size);
    }

}
