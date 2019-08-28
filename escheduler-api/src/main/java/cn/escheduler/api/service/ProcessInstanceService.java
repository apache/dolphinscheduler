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
package cn.escheduler.api.service;

import cn.escheduler.api.dto.gantt.GanttDto;
import cn.escheduler.api.dto.gantt.Task;
import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.PageInfo;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.enums.DependResult;
import cn.escheduler.common.enums.ExecutionStatus;
import cn.escheduler.common.enums.Flag;
import cn.escheduler.common.enums.TaskType;
import cn.escheduler.common.graph.DAG;
import cn.escheduler.common.model.TaskNode;
import cn.escheduler.common.model.TaskNodeRelation;
import cn.escheduler.common.process.Property;
import cn.escheduler.common.queue.ITaskQueue;
import cn.escheduler.common.queue.TaskQueueFactory;
import cn.escheduler.common.utils.CollectionUtils;
import cn.escheduler.common.utils.DateUtils;
import cn.escheduler.common.utils.JSONUtils;
import cn.escheduler.common.utils.ParameterUtils;
import cn.escheduler.common.utils.placeholder.BusinessTimeUtils;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.mapper.*;
import cn.escheduler.dao.model.*;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static cn.escheduler.common.Constants.*;

/**
 * process instance service
 */
@Service
public class ProcessInstanceService extends BaseDAGService {


    private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceService.class);

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    ProjectService projectService;

    @Autowired
    ProcessDao processDao;

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
    WorkerGroupMapper workerGroupMapper;

    /**
     * query process instance by id
     *
     * @param loginUser
     * @param projectName
     * @param processId
     * @return
     */
    public Map<String, Object> queryProcessInstanceById(User loginUser, String projectName, Integer processId) {
        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }
        ProcessInstance processInstance = processDao.findProcessInstanceDetailById(processId);
        String workerGroupName = "";
        if(processInstance.getWorkerGroupId() == -1){
            workerGroupName = DEFAULT;
        }else{
            WorkerGroup workerGroup = workerGroupMapper.queryById(processInstance.getWorkerGroupId());
            if(workerGroup != null){
                workerGroupName = DEFAULT;
            }else{
                workerGroupName = workerGroup.getName();
            }
        }
        processInstance.setWorkerGroupName(workerGroupName);
        ProcessDefinition processDefinition = processDao.findProcessDefineById(processInstance.getProcessDefinitionId());
        processInstance.setReceivers(processDefinition.getReceivers());
        processInstance.setReceiversCc(processDefinition.getReceiversCc());
        result.put(Constants.DATA_LIST, processInstance);
        putMsg(result, Status.SUCCESS);

        return result;
    }


    /**
     * paging query process instance list, filtering according to project, process definition, time range, keyword, process status
     *
     * @param loginUser
     * @param projectName
     * @param processDefineId
     * @param startDate
     * @param endDate
     * @param searchVal
     * @param stateType
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Map<String, Object> queryProcessInstanceList(User loginUser, String projectName, Integer processDefineId,
                                                        String startDate, String endDate,
                                                        String searchVal, ExecutionStatus stateType, String host,
                                                        Integer pageNo, Integer pageSize) {

        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }

        int[] statusArray = null;
        String statesStr = null;
        // filter by state
        if (stateType != null) {
            statusArray = new int[]{stateType.ordinal()};
        }
        if (statusArray != null) {
            statesStr = Arrays.toString(statusArray).replace("[", "").replace("]", "");
        }

        Date start = null;
        Date end = null;
        try {
            if (StringUtils.isNotEmpty(startDate)) {
                start = DateUtils.getScheduleDate(startDate);
            }
            if (StringUtils.isNotEmpty(endDate)) {
                end = DateUtils.getScheduleDate(endDate);
            }
        } catch (Exception e) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, "startDate,endDate");
            return result;
        }
        Integer count = processInstanceMapper.countProcessInstance(project.getId(), processDefineId, statesStr,
                host, start, end, searchVal);

        PageInfo pageInfo = new PageInfo<ProcessInstance>(pageNo, pageSize);
        List<ProcessInstance> processInstanceList = processInstanceMapper.queryProcessInstanceListPaging(
                project.getId(), processDefineId, searchVal, statesStr, host, start, end, pageInfo.getStart(), pageSize);

        Set<String> exclusionSet = new HashSet<String>(){{
            add(Constants.CLASS);
            add("locations");
            add("connects");
            add("processInstanceJson");
        }};

        pageInfo.setTotalCount(count);
        pageInfo.setLists(CollectionUtils.getListByExclusion(processInstanceList, exclusionSet));
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }



    /**
     * query task list by process instance id
     *
     * @param loginUser
     * @param projectName
     * @param processId
     * @return
     */
    public Map<String, Object> queryTaskListByProcessId(User loginUser, String projectName, Integer processId) throws IOException {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }
        ProcessInstance processInstance = processDao.findProcessInstanceDetailById(processId);
        List<TaskInstance> taskInstanceList = processDao.findValidTaskListByProcessId(processId);
        AddDependResultForTaskList(taskInstanceList);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(PROCESS_INSTANCE_STATE, processInstance.getState().toString());
        resultMap.put(TASK_LIST, taskInstanceList);
        result.put(Constants.DATA_LIST, resultMap);

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * add dependent result for dependent task
     * @param taskInstanceList
     */
    private void AddDependResultForTaskList(List<TaskInstance> taskInstanceList) throws IOException {
        for(TaskInstance taskInstance: taskInstanceList){
            if(taskInstance.getTaskType().toUpperCase().equals(TaskType.DEPENDENT.toString())){
                Result logResult = loggerService.queryLog(
                        taskInstance.getId(), 0, 4098);
                if(logResult.getCode() == Status.SUCCESS.ordinal()){
                    String log = (String) logResult.getData();
                    Map<String, DependResult> resultMap = parseLogForDependentResult(log);
                    taskInstance.setDependentResult(JSONUtils.toJson(resultMap));
                }
            }
        }
    }

    public Map<String,DependResult> parseLogForDependentResult(String log) throws IOException {
        Map<String, DependResult> resultMap = new HashMap<>();
        if(StringUtils.isEmpty(log)){
            return resultMap;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(log.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
        String line;
        while ((line = br.readLine()) != null) {
            if(line.contains(DEPENDENT_SPLIT)){
                String[] tmpStringArray = line.split(":\\|\\|");
                if(tmpStringArray.length != 2){
                    continue;
                }
                String dependResultString = tmpStringArray[1];
                String[] dependStringArray = dependResultString.split(",");
                if(dependStringArray.length != 2){
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
     * @param loginUser
     * @param projectName
     * @param taskId
     * @return
     */
    public Map<String, Object> querySubProcessInstanceByTaskId(User loginUser, String projectName, Integer taskId) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }

        TaskInstance taskInstance = processDao.findTaskInstanceById(taskId);
        if (taskInstance == null) {
            putMsg(result, Status.TASK_INSTANCE_NOT_EXISTS, taskId);
            return result;
        }
        if (!taskInstance.isSubProcess()) {
            putMsg(result, Status.TASK_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE, taskInstance.getName());
            return result;
        }

        ProcessInstance subWorkflowInstance = processDao.findSubProcessInstance(
                taskInstance.getProcessInstanceId(), taskInstance.getId());
        if (subWorkflowInstance == null) {
            putMsg(result, Status.SUB_PROCESS_INSTANCE_NOT_EXIST, taskId);
            return result;
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("subProcessInstanceId", subWorkflowInstance.getId());
        result.put(Constants.DATA_LIST, dataMap);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * update process instance
     *
     * @param loginUser
     * @param projectName
     * @param processInstanceId
     * @param processInstanceJson
     * @param scheduleTime
     * @param syncDefine
     * @param flag
     * @param locations
     * @param connects
     * @return
     */
    public Map<String, Object> updateProcessInstance(User loginUser, String projectName, Integer processInstanceId,
                                                     String processInstanceJson, String scheduleTime, Boolean syncDefine,
                                                     Flag flag, String locations, String connects) throws ParseException {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByName(projectName);

        //check project permission
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }

        //check process instance exists
        ProcessInstance processInstance = processDao.findProcessInstanceDetailById(processInstanceId);
        if (processInstance == null) {
            putMsg(result, Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceId);
            return result;
        }

        //check process instance status
        if (!processInstance.getState().typeIsFinished()) {
            putMsg(result, Status.PROCESS_INSTANCE_STATE_OPERATION_ERROR,
                    processInstance.getName(), processInstance.getState().toString(), "update");
            return result;
        }
        Date schedule = null;
        if (scheduleTime != null) {
            schedule = DateUtils.getScheduleDate(scheduleTime);
        } else {
            schedule = processInstance.getScheduleTime();
        }
        processInstance.setScheduleTime(schedule);
        processInstance.setLocations(locations);
        processInstance.setConnects(connects);
        String globalParams = null;
        String originDefParams = null;
        int timeout = processInstance.getTimeout();
        ProcessDefinition processDefinition = processDao.findProcessDefineById(processInstance.getProcessDefinitionId());
        if (StringUtils.isNotEmpty(processInstanceJson)) {
            ProcessData processData = JSONUtils.parseObject(processInstanceJson, ProcessData.class);
            //check workflow json is valid
            Map<String, Object> checkFlowJson = processDefinitionService.checkProcessNodeList(processData, processInstanceJson);
            if (checkFlowJson.get(Constants.STATUS) != Status.SUCCESS) {
                return result;
            }

            originDefParams = JSONUtils.toJson(processData.getGlobalParams());
            List<Property> globalParamList = processData.getGlobalParams();
            Map<String, String> globalParamMap = globalParamList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));
            globalParams = ParameterUtils.curingGlobalParams(globalParamMap, globalParamList,
                    processInstance.getCmdTypeIfComplement(), schedule);
            timeout = processData.getTimeout();
            processInstance.setTimeout(timeout);
            Tenant tenant = processDao.getTenantForProcess(processData.getTenantId(),
                    processDefinition.getUserId());
            if(tenant != null){
                processInstance.setTenantCode(tenant.getTenantCode());
            }
            processInstance.setProcessInstanceJson(processInstanceJson);
            processInstance.setGlobalParams(globalParams);
        }
//        int update = processDao.updateProcessInstance(processInstanceId, processInstanceJson,
//                globalParams, schedule, flag, locations, connects);
        int update = processDao.updateProcessInstance(processInstance);
        int updateDefine = 1;
        if (syncDefine && StringUtils.isNotEmpty(processInstanceJson)) {
            processDefinition.setProcessDefinitionJson(processInstanceJson);
            processDefinition.setGlobalParams(originDefParams);
            processDefinition.setLocations(locations);
            processDefinition.setConnects(connects);
            processDefinition.setTimeout(timeout);
            updateDefine = processDefineMapper.update(processDefinition);
        }
        if (update > 0 && updateDefine > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.UPDATE_PROCESS_INSTANCE_ERROR);
        }


        return result;

    }

    /**
     * query parent process instance detail info by sub process instance id
     *
     * @param loginUser
     * @param projectName
     * @param subId
     * @return
     */
    public Map<String, Object> queryParentInstanceBySubId(User loginUser, String projectName, Integer subId) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }

        ProcessInstance subInstance = processDao.findProcessInstanceDetailById(subId);
        if (subInstance == null) {
            putMsg(result, Status.PROCESS_INSTANCE_NOT_EXIST, subId);
            return result;
        }
        if (subInstance.getIsSubProcess() == Flag.NO) {
            putMsg(result, Status.PROCESS_INSTANCE_NOT_SUB_PROCESS_INSTANCE, subInstance.getName());
            return result;
        }

        ProcessInstance parentWorkflowInstance = processDao.findParentProcessInstance(subId);
        if (parentWorkflowInstance == null) {
            putMsg(result, Status.SUB_PROCESS_INSTANCE_NOT_EXIST);
            return result;
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("parentWorkflowInstance", parentWorkflowInstance.getId());
        result.put(Constants.DATA_LIST, dataMap);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * delete process instance by id, at the same time，delete task instance and their mapping relation data
     * @param loginUser
     * @param projectName
     * @param processInstanceId
     * @param tasksQueue
     * @return
     */
    public Map<String, Object> deleteProcessInstanceById(User loginUser, String projectName, Integer processInstanceId,ITaskQueue tasksQueue) {

        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }
        ProcessInstance processInstance = processDao.findProcessInstanceDetailById(processInstanceId);
        List<TaskInstance> taskInstanceList = processDao.findValidTaskListByProcessId(processInstanceId);
        //process instance priority
        int processInstancePriority = processInstance.getProcessInstancePriority().ordinal();
        if (processInstance == null) {
            putMsg(result, Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceId);
            return result;
        }

        int delete = processDao.deleteWorkProcessInstanceById(processInstanceId);
        processDao.deleteAllSubWorkProcessByParentId(processInstanceId);
        processDao.deleteWorkProcessMapByParentId(processInstanceId);

        if (delete > 0) {
            if (CollectionUtils.isNotEmpty(taskInstanceList)){
                for (TaskInstance taskInstance : taskInstanceList){
                    // task instance priority
                    int taskInstancePriority = taskInstance.getTaskInstancePriority().ordinal();
                    String nodeValue=processInstancePriority + "_" + processInstanceId + "_" +taskInstancePriority + "_" + taskInstance.getId();
                    try {
                        logger.info("delete task queue node : {}",nodeValue);
                        tasksQueue.removeNode(cn.escheduler.common.Constants.SCHEDULER_TASKS_QUEUE, nodeValue);
                    }catch (Exception e){
                        logger.error("delete task queue node : {}", nodeValue);
                    }
                }
            }

            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.DELETE_PROCESS_INSTANCE_BY_ID_ERROR);
        }
        return result;
    }

    /**
     * batch delete process instance by ids, at the same time，delete task instance and their mapping relation data
     *
     * @param loginUser
     * @param projectName
     * @param processInstanceIds
     * @return
     */
    public Map<String, Object> batchDeleteProcessInstanceByIds(User loginUser, String projectName, String processInstanceIds) {
        // task queue
        ITaskQueue tasksQueue = TaskQueueFactory.getTaskQueueInstance();

        Map<String, Object> result = new HashMap<>(5);
        List<Integer> deleteFailedIdList = new ArrayList<Integer>();

        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }

        if(StringUtils.isNotEmpty(processInstanceIds)){
            String[] processInstanceIdArray = processInstanceIds.split(",");

            for (String strProcessInstanceId:processInstanceIdArray) {
                int processInstanceId = Integer.parseInt(strProcessInstanceId);
                try {
                    deleteProcessInstanceById(loginUser, projectName, processInstanceId,tasksQueue);
                } catch (Exception e) {
                    deleteFailedIdList.add(processInstanceId);
                }
            }
        }
        if(deleteFailedIdList.size() > 0){
            putMsg(result, Status.BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR,StringUtils.join(deleteFailedIdList.toArray(),","));
        }else{
            putMsg(result, Status.SUCCESS);
        }

        return result;
    }

    /**
     * view process instance variables
     *
     * @param processInstanceId
     * @return
     */
    public Map<String, Object> viewVariables( Integer processInstanceId) throws Exception {
        Map<String, Object> result = new HashMap<>(5);

        ProcessInstance processInstance = processInstanceMapper.queryDetailById(processInstanceId);

        if (processInstance == null) {
            throw new RuntimeException("workflow instance is null");
        }

        Map<String, String> timeParams = BusinessTimeUtils
                .getBusinessTime(processInstance.getCmdTypeIfComplement(),
                        processInstance.getScheduleTime());


        String workflowInstanceJson = processInstance.getProcessInstanceJson();

        ProcessData workflowData = JSONUtils.parseObject(workflowInstanceJson, ProcessData.class);

        String userDefinedParams = processInstance.getGlobalParams();

        // global params
        List<Property> globalParams = new ArrayList<>();

        if (userDefinedParams != null && userDefinedParams.length() > 0) {
            globalParams = JSON.parseArray(userDefinedParams, Property.class);
        }


        List<TaskNode> taskNodeList = workflowData.getTasks();

        // global param string
        String globalParamStr = JSON.toJSONString(globalParams);
        globalParamStr = ParameterUtils.convertParameterPlaceholders(globalParamStr, timeParams);
        globalParams = JSON.parseArray(globalParamStr, Property.class);
        for (Property property : globalParams) {
            timeParams.put(property.getProp(), property.getValue());
        }

        // local params
        Map<String, Map<String,Object>> localUserDefParams = new HashMap<>();
        for (TaskNode taskNode : taskNodeList) {
            String parameter = taskNode.getParams();
            Map<String, String> map = JSONUtils.toMap(parameter);
            String localParams = map.get(LOCAL_PARAMS);
            if (localParams != null && !localParams.isEmpty()) {
                localParams = ParameterUtils.convertParameterPlaceholders(localParams, timeParams);
                List<Property> localParamsList = JSON.parseArray(localParams, Property.class);
                Map<String,Object> localParamsMap = new HashMap<>();
                localParamsMap.put("taskType",taskNode.getType());
                localParamsMap.put("localParamsList",localParamsList);
                if (localParamsList.size() > 0) {
                    localUserDefParams.put(taskNode.getName(), localParamsMap);
                }
            }

        }

        Map<String, Object> resultMap = new HashMap<>();

        resultMap.put(GLOBAL_PARAMS, globalParams);
        resultMap.put(LOCAL_PARAMS, localUserDefParams);

        result.put(Constants.DATA_LIST, resultMap);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * encapsulation gantt structure
     *
     * @param processInstanceId
     * @return
     * @throws Exception
     */
    public Map<String, Object> viewGantt(Integer processInstanceId) throws Exception {
        Map<String, Object> result = new HashMap<>();

        ProcessInstance processInstance = processInstanceMapper.queryDetailById(processInstanceId);

        if (processInstance == null) {
            throw new RuntimeException("workflow instance is null");
        }

        GanttDto ganttDto = new GanttDto();

        DAG<String, TaskNode, TaskNodeRelation> dag = processInstance2DAG(processInstance);
        //topological sort
        List<String> nodeList = dag.topologicalSort();

        ganttDto.setTaskNames(nodeList);

        List<Task> taskList = new ArrayList<>();
        for (String node : nodeList) {
            TaskInstance taskInstance = taskInstanceMapper.queryByInstanceIdAndName(processInstanceId, node);
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

        result.put(Constants.DATA_LIST, ganttDto);
        putMsg(result, Status.SUCCESS);
        return result;
    }

}
