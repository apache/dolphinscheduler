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
package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.dto.treeview.Instance;
import org.apache.dolphinscheduler.api.dto.treeview.TreeViewDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.*;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.process.ProcessDag;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.ProcessDao;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dolphinscheduler.api.utils.CheckUtils;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.dao.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_SUB_PROCESS_DEFINE_ID;

/**
 * process definition service
 */
@Service
public class ProcessDefinitionService extends BaseDAGService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionService.class);


    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProcessDefinitionMapper processDefineMapper;

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;


    @Autowired
    private TaskInstanceMapper taskInstanceMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private ProcessDao processDao;

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private WorkerGroupMapper workerGroupMapper;

    /**
     * create process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param name process definition name
     * @param processDefinitionJson process definition json
     * @param desc description
     * @param locations locations for nodes
     * @param connects connects for nodes
     * @return create result code
     * @throws JsonProcessingException JsonProcessingException
     */
    public Map<String, Object> createProcessDefinition(User loginUser, String projectName, String name,
                                                       String processDefinitionJson, String desc, String locations, String connects) throws JsonProcessingException {

        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);
        // check project auth
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }

        ProcessDefinition processDefine = new ProcessDefinition();
        Date now = new Date();

        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);
        Map<String, Object> checkProcessJson = checkProcessNodeList(processData, processDefinitionJson);
        if (checkProcessJson.get(Constants.STATUS) != Status.SUCCESS) {
            return checkProcessJson;
        }

        processDefine.setName(name);
        processDefine.setReleaseState(ReleaseState.OFFLINE);
        processDefine.setProjectId(project.getId());
        processDefine.setUserId(loginUser.getId());
        processDefine.setProcessDefinitionJson(processDefinitionJson);
        processDefine.setDescription(desc);
        processDefine.setLocations(locations);
        processDefine.setConnects(connects);
        processDefine.setTimeout(processData.getTimeout());
        processDefine.setTenantId(processData.getTenantId());

        //custom global params
        List<Property> globalParamsList = processData.getGlobalParams();
        if (globalParamsList != null && globalParamsList.size() > 0) {
            Set<Property> globalParamsSet = new HashSet<>(globalParamsList);
            globalParamsList = new ArrayList<>(globalParamsSet);
            processDefine.setGlobalParamList(globalParamsList);
        }
        processDefine.setCreateTime(now);
        processDefine.setUpdateTime(now);
        processDefine.setFlag(Flag.YES);
        processDefineMapper.insert(processDefine);
        putMsg(result, Status.SUCCESS);
        result.put("processDefinitionId",processDefine.getId());
        return result;
    }


    /**
     * query proccess definition list
     *
     * @param loginUser login user
     * @param projectName project name
     * @return definition list
     */
    public Map<String, Object> queryProccessDefinitionList(User loginUser, String projectName) {

        HashMap<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }

        List<ProcessDefinition> resourceList = processDefineMapper.queryAllDefinitionList(project.getId());
        result.put(Constants.DATA_LIST, resourceList);
        putMsg(result, Status.SUCCESS);

        return result;
    }


    /**
     * query proccess definition list paging
     *
     * @param loginUser login user
     * @param projectName project name
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @param userId user id
     * @return process definition page
     */
    public Map<String, Object> queryProcessDefinitionListPaging(User loginUser, String projectName, String searchVal, Integer pageNo, Integer pageSize, Integer userId) {

        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }

        Page<ProcessDefinition> page = new Page(pageNo, pageSize);
        IPage<ProcessDefinition> processDefinitionIPage = processDefineMapper.queryDefineListPaging(
                page, searchVal, userId, project.getId(),isAdmin(loginUser));

        PageInfo pageInfo = new PageInfo<ProcessData>(pageNo, pageSize);
        pageInfo.setTotalCount((int)processDefinitionIPage.getTotal());
        pageInfo.setLists(processDefinitionIPage.getRecords());
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * query datail of process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processId process definition id
     * @return process definition detail
     */
    public Map<String, Object> queryProccessDefinitionById(User loginUser, String projectName, Integer processId) {


        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }

        ProcessDefinition processDefinition = processDefineMapper.selectById(processId);
        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_INSTANCE_NOT_EXIST, processId);
        } else {
            result.put(Constants.DATA_LIST, processDefinition);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * update  process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param name process definition name
     * @param id process definition id
     * @param processDefinitionJson process definition json
     * @param desc description
     * @param locations locations for nodes
     * @param connects connects for nodes
     * @return update result code
     */
    public Map<String, Object> updateProcessDefinition(User loginUser, String projectName, int id, String name,
                                                       String processDefinitionJson, String desc,
                                                       String locations, String connects) {
        Map<String, Object> result = new HashMap<>(5);

        Project project = projectMapper.queryByName(projectName);
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }

        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);
        Map<String, Object> checkProcessJson = checkProcessNodeList(processData, processDefinitionJson);
        if ((checkProcessJson.get(Constants.STATUS) != Status.SUCCESS)) {
            return checkProcessJson;
        }
        ProcessDefinition processDefinition = processDao.findProcessDefineById(id);
        if (processDefinition == null) {
            // check process definition exists
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, id);
            return result;
        } else if (processDefinition.getReleaseState() == ReleaseState.ONLINE) {
            // online can not permit edit
            putMsg(result, Status.PROCESS_DEFINE_NOT_ALLOWED_EDIT, processDefinition.getName());
            return result;
        } else {
            putMsg(result, Status.SUCCESS);
        }

        ProcessDefinition processDefine = processDao.findProcessDefineById(id);
        Date now = new Date();

        processDefine.setId(id);
        processDefine.setName(name);
        processDefine.setReleaseState(ReleaseState.OFFLINE);
        processDefine.setProjectId(project.getId());
        processDefine.setProcessDefinitionJson(processDefinitionJson);
        processDefine.setDescription(desc);
        processDefine.setLocations(locations);
        processDefine.setConnects(connects);
        processDefine.setTimeout(processData.getTimeout());
        processDefine.setTenantId(processData.getTenantId());

        //custom global params
        List<Property> globalParamsList = new ArrayList<>();
        if (processData.getGlobalParams() != null && processData.getGlobalParams().size() > 0) {
            Set<Property> userDefParamsSet = new HashSet<>(processData.getGlobalParams());
            globalParamsList = new ArrayList<>(userDefParamsSet);
        }
        processDefine.setGlobalParamList(globalParamsList);
        processDefine.setUpdateTime(now);
        processDefine.setFlag(Flag.YES);
        if (processDefineMapper.updateById(processDefine) > 0) {
            putMsg(result, Status.SUCCESS);

        } else {
            putMsg(result, Status.UPDATE_PROCESS_DEFINITION_ERROR);
        }
        return result;
    }

    /**
     * verify process definition name unique
     *
     * @param loginUser login user
     * @param projectName project name
     * @param name name
     * @return true if process definition name not exists, otherwise false
     */
    public Map<String, Object> verifyProccessDefinitionName(User loginUser, String projectName, String name) {

        Map<String, Object> result = new HashMap<>();
            Project project = projectMapper.queryByName(projectName);

            Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
            Status resultEnum = (Status) checkResult.get(Constants.STATUS);
            if (resultEnum != Status.SUCCESS) {
                return checkResult;
            }
            ProcessDefinition processDefinition = processDefineMapper.queryByDefineName(project.getId(), name);
            if (processDefinition == null) {
                putMsg(result, Status.SUCCESS);
            } else {
                putMsg(result, Status.PROCESS_INSTANCE_EXIST, name);
            }
        return result;
    }

    /**
     * delete process definition by id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionId process definition id
     * @return delete result code
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deleteProcessDefinitionById(User loginUser, String projectName, Integer processDefinitionId) {

        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }

        ProcessDefinition processDefinition = processDefineMapper.selectById(processDefinitionId);

        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, processDefinitionId);
            return result;
        }

        // Determine if the login user is the owner of the process definition
        if (loginUser.getId() != processDefinition.getUserId() && loginUser.getUserType() != UserType.ADMIN_USER) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        // check process definition is already online
        if (processDefinition.getReleaseState() == ReleaseState.ONLINE) {
            putMsg(result, Status.PROCESS_DEFINE_STATE_ONLINE,processDefinitionId);
            return result;
        }

        // get the timing according to the process definition
        List<Schedule> schedules = scheduleMapper.queryByProcessDefinitionId(processDefinitionId);
        if (!schedules.isEmpty() && schedules.size() > 1) {
            logger.warn("scheduler num is {},Greater than 1",schedules.size());
            putMsg(result, Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR);
            return result;
        }else if(schedules.size() == 1){
            Schedule schedule = schedules.get(0);
            if(schedule.getReleaseState() == ReleaseState.OFFLINE){
                scheduleMapper.deleteById(schedule.getId());
            }else if(schedule.getReleaseState() == ReleaseState.ONLINE){
                putMsg(result, Status.SCHEDULE_CRON_STATE_ONLINE,schedule.getId());
                return result;
            }
        }

        int delete = processDefineMapper.deleteById(processDefinitionId);

        if (delete > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR);
        }
        return result;
    }

    /**
     * release process definition: online / offline
     *
     * @param loginUser login user
     * @param projectName project name
     * @param id process definition id
     * @param releaseState release state
     * @return release result code
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> releaseProcessDefinition(User loginUser, String projectName, int id, int releaseState) {
        HashMap<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }

        ReleaseState state = ReleaseState.getEnum(releaseState);
        ProcessDefinition processDefinition = processDefineMapper.selectById(id);

        switch (state) {
            case ONLINE: {
                processDefinition.setReleaseState(state);
                processDefineMapper.updateById(processDefinition);
                break;
            }
            case OFFLINE: {
                processDefinition.setReleaseState(state);
                processDefineMapper.updateById(processDefinition);
                List<Schedule> scheduleList = scheduleMapper.selectAllByProcessDefineArray(
                        new int[]{processDefinition.getId()}
                );

                for(Schedule schedule:scheduleList){
                    logger.info("set schedule offline, schedule id: {}, process definition id: {}", project.getId(), schedule.getId(), id);
                    // set status
                    schedule.setReleaseState(ReleaseState.OFFLINE);
                    scheduleMapper.updateById(schedule);
                    SchedulerService.deleteSchedule(project.getId(), schedule.getId());
                }
                break;
            }
            default: {
                putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, "releaseState");
                return result;
            }
        }

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * export process definition by id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionId process definition id
     * @param response response
     */
    public void exportProcessDefinitionById(User loginUser, String projectName, Integer processDefinitionId, HttpServletResponse response) {
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus == Status.SUCCESS) {
            ProcessDefinition processDefinition = processDefineMapper.queryByDefineId(processDefinitionId);
            if (processDefinition != null) {
                JSONObject jsonObject = JSONUtils.parseObject(processDefinition.getProcessDefinitionJson());
                JSONArray jsonArray = (JSONArray) jsonObject.get("tasks");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject taskNode = jsonArray.getJSONObject(i);
                    if (taskNode.get("type") != null && taskNode.get("type") != "") {
                        String taskType = taskNode.getString("type");
                        if(taskType.equals(TaskType.SQL.name())  || taskType.equals(TaskType.PROCEDURE.name())){
                            JSONObject sqlParameters = JSONUtils.parseObject(taskNode.getString("params"));
                            DataSource dataSource = dataSourceMapper.selectById((Integer) sqlParameters.get("datasource"));
                            if (dataSource != null) {
                                sqlParameters.put("datasourceName", dataSource.getName());
                            }
                            taskNode.put("params", sqlParameters);
                        }else if(taskType.equals(TaskType.DEPENDENT.name())){
                            JSONObject dependentParameters =  JSONUtils.parseObject(taskNode.getString("dependence"));
                            if(dependentParameters != null){
                                JSONArray dependTaskList = (JSONArray) dependentParameters.get("dependTaskList");
                                for (int j = 0; j < dependTaskList.size(); j++) {
                                    JSONObject dependentTaskModel = dependTaskList.getJSONObject(j);
                                    JSONArray dependItemList = (JSONArray) dependentTaskModel.get("dependItemList");
                                    for (int k = 0; k < dependItemList.size(); k++) {
                                        JSONObject dependentItem = dependItemList.getJSONObject(k);
                                        int definitionId = dependentItem.getInteger("definitionId");
                                        ProcessDefinition definition = processDefineMapper.selectById(definitionId);
                                        if(definition != null){
                                            dependentItem.put("projectName",definition.getProjectName());
                                            dependentItem.put("definitionName",definition.getName());
                                        }
                                    }
                                }
                                taskNode.put("dependence", dependentParameters);
                            }
                        }
                    }
                }
                jsonObject.put("tasks", jsonArray);
                processDefinition.setProcessDefinitionJson(jsonObject.toString());

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("projectName", processDefinition.getProjectName());
                row.put("processDefinitionName", processDefinition.getName());
                row.put("processDefinitionJson", processDefinition.getProcessDefinitionJson());
                row.put("processDefinitionDescription", processDefinition.getDescription());
                row.put("processDefinitionLocations", processDefinition.getLocations());
                row.put("processDefinitionConnects", processDefinition.getConnects());

                List<Schedule> schedules = scheduleMapper.queryByProcessDefinitionId(processDefinitionId);
                if (schedules.size() > 0) {
                    Schedule schedule = schedules.get(0);
                    row.put("scheduleWarningType", schedule.getWarningType());
                    row.put("scheduleWarningGroupId", schedule.getWarningGroupId());
                    row.put("scheduleStartTime", DateUtils.dateToString(schedule.getStartTime()));
                    row.put("scheduleEndTime", DateUtils.dateToString(schedule.getEndTime()));
                    row.put("scheduleCrontab", schedule.getCrontab());
                    row.put("scheduleFailureStrategy", schedule.getFailureStrategy());
                    row.put("scheduleReleaseState", ReleaseState.OFFLINE);
                    row.put("scheduleProcessInstancePriority", schedule.getProcessInstancePriority());
                    if(schedule.getId() == -1){
                        row.put("scheduleWorkerGroupId", -1);
                    }else{
                        WorkerGroup workerGroup = workerGroupMapper.selectById(schedule.getWorkerGroupId());
                        if(workerGroup != null){
                            row.put("scheduleWorkerGroupName", workerGroup.getName());
                        }
                    }

                }
                String rowsJson = JSONUtils.toJsonString(row);
                response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
                response.setHeader("Content-Disposition", "attachment;filename="+processDefinition.getName()+".json");
                BufferedOutputStream buff = null;
                ServletOutputStream out = null;
                try {
                    out = response.getOutputStream();
                    buff = new BufferedOutputStream(out);
                    buff.write(rowsJson.getBytes("UTF-8"));
                    buff.flush();
                    buff.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        buff.close();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importProcessDefinition(User loginUser, MultipartFile file) {
        Map<String, Object> result = new HashMap<>(5);

        JSONObject json = null;
        try(InputStreamReader inputStreamReader = new InputStreamReader( file.getInputStream(), "UTF-8" )) {
            BufferedReader streamReader = new BufferedReader(inputStreamReader);
            StringBuilder respomseStrBuilder = new StringBuilder();
            String inputStr = "";
            while ((inputStr = streamReader.readLine())!= null){
                respomseStrBuilder.append( inputStr );
            }
            json = JSONObject.parseObject( respomseStrBuilder.toString() );
            if(json != null){
                String projectName = null;
                String processDefinitionName = null;
                String processDefinitionJson = null;
                String processDefinitionDesc = null;
                String processDefinitionLocations = null;
                String processDefinitionConnects = null;

                String scheduleWarningType = null;
                String scheduleWarningGroupId = null;
                String scheduleStartTime = null;
                String scheduleEndTime = null;
                String scheduleCrontab = null;
                String scheduleFailureStrategy = null;
                String scheduleReleaseState = null;
                String scheduleProcessInstancePriority = null;
                String scheduleWorkerGroupId = null;
                String scheduleWorkerGroupName = null;

                if (ObjectUtils.allNotNull(json.get("projectName"))) {
                    projectName = json.get("projectName").toString();
                } else {
                    putMsg(result, Status.DATA_IS_NULL, "processDefinitionName");
                    return result;
                }
                if (ObjectUtils.allNotNull(json.get("processDefinitionName"))) {
                    processDefinitionName = json.get("processDefinitionName").toString();
                } else {
                    putMsg(result, Status.DATA_IS_NULL, "processDefinitionName");
                    return result;
                }
                if (ObjectUtils.allNotNull(json.get("processDefinitionJson"))) {
                    processDefinitionJson = json.get("processDefinitionJson").toString();
                } else {
                    putMsg(result, Status.DATA_IS_NULL, "processDefinitionJson");
                    return result;
                }
                if (ObjectUtils.allNotNull(json.get("processDefinitionDescription"))) {
                    processDefinitionDesc = json.get("processDefinitionDescription").toString();
                }
                if (ObjectUtils.allNotNull(json.get("processDefinitionLocations"))) {
                    processDefinitionLocations = json.get("processDefinitionLocations").toString();
                }
                if (ObjectUtils.allNotNull(json.get("processDefinitionConnects"))) {
                    processDefinitionConnects = json.get("processDefinitionConnects").toString();
                }

                Project project = projectMapper.queryByName(projectName);
                if(project != null){
                    processDefinitionName = recursionProcessDefinitionName(project.getId(), processDefinitionName, 1);
                }

                JSONObject jsonObject = JSONUtils.parseObject(processDefinitionJson);
                JSONArray jsonArray = (JSONArray) jsonObject.get("tasks");
                for (int j = 0; j < jsonArray.size(); j++) {
                    JSONObject taskNode = jsonArray.getJSONObject(j);
                    String taskType = taskNode.getString("type");
                    if(taskType.equals(TaskType.SQL.name())  || taskType.equals(TaskType.PROCEDURE.name())) {
                        JSONObject sqlParameters = JSONUtils.parseObject(taskNode.getString("params"));
                        List<DataSource> dataSources = dataSourceMapper.queryDataSourceByName(sqlParameters.getString("datasourceName"));
                        if (dataSources.size() > 0) {
                            DataSource dataSource = dataSources.get(0);
                            sqlParameters.put("datasource", dataSource.getId());
                        }
                        taskNode.put("params", sqlParameters);
                    }else if(taskType.equals(TaskType.DEPENDENT.name())){
                        JSONObject dependentParameters =  JSONUtils.parseObject(taskNode.getString("dependence"));
                        if(dependentParameters != null){
                            JSONArray dependTaskList = (JSONArray) dependentParameters.get("dependTaskList");
                            for (int h = 0; h < dependTaskList.size(); h++) {
                                JSONObject dependentTaskModel = dependTaskList.getJSONObject(h);
                                JSONArray dependItemList = (JSONArray) dependentTaskModel.get("dependItemList");
                                for (int k = 0; k < dependItemList.size(); k++) {
                                    JSONObject dependentItem = dependItemList.getJSONObject(k);
                                    Project dependentItemProject = projectMapper.queryByName(dependentItem.getString("projectName"));
                                    if(dependentItemProject != null){
                                        ProcessDefinition definition = processDefineMapper.queryByDefineName(dependentItemProject.getId(),dependentItem.getString("definitionName"));
                                        if(definition != null){
                                            dependentItem.put("projectId",dependentItemProject.getId());
                                            dependentItem.put("definitionId",definition.getId());
                                        }
                                    }
                                }
                            }
                            taskNode.put("dependence", dependentParameters);
                        }
                    }
                }
                jsonObject.put("tasks", jsonArray);

                Map<String, Object> createProcessDefinitionResult = createProcessDefinition(loginUser,projectName,processDefinitionName,jsonObject.toString(),processDefinitionDesc,processDefinitionLocations,processDefinitionConnects);
                Integer processDefinitionId = null;
                if (ObjectUtils.allNotNull(createProcessDefinitionResult.get("processDefinitionId"))) {
                    processDefinitionId = Integer.parseInt(createProcessDefinitionResult.get("processDefinitionId").toString());
                }
                if (ObjectUtils.allNotNull(json.get("scheduleCrontab")) && processDefinitionId != null) {
                    Date now = new Date();
                    Schedule scheduleObj = new Schedule();
                    scheduleObj.setProjectName(projectName);
                    scheduleObj.setProcessDefinitionId(processDefinitionId);
                    scheduleObj.setProcessDefinitionName(processDefinitionName);
                    scheduleObj.setCreateTime(now);
                    scheduleObj.setUpdateTime(now);
                    scheduleObj.setUserId(loginUser.getId());
                    scheduleObj.setUserName(loginUser.getUserName());


                    scheduleCrontab = json.get("scheduleCrontab").toString();
                    scheduleObj.setCrontab(scheduleCrontab);
                    if (ObjectUtils.allNotNull(json.get("scheduleStartTime"))) {
                        scheduleStartTime = json.get("scheduleStartTime").toString();
                        scheduleObj.setStartTime(DateUtils.stringToDate(scheduleStartTime));
                    }
                    if (ObjectUtils.allNotNull(json.get("scheduleEndTime"))) {
                        scheduleEndTime = json.get("scheduleEndTime").toString();
                        scheduleObj.setEndTime(DateUtils.stringToDate(scheduleEndTime));
                    }
                    if (ObjectUtils.allNotNull(json.get("scheduleWarningType"))) {
                        scheduleWarningType = json.get("scheduleWarningType").toString();
                        scheduleObj.setWarningType(WarningType.valueOf(scheduleWarningType));
                    }
                    if (ObjectUtils.allNotNull(json.get("scheduleWarningGroupId"))) {
                        scheduleWarningGroupId = json.get("scheduleWarningGroupId").toString();
                        scheduleObj.setWarningGroupId(Integer.parseInt(scheduleWarningGroupId));
                    }
                    if (ObjectUtils.allNotNull(json.get("scheduleFailureStrategy"))) {
                        scheduleFailureStrategy = json.get("scheduleFailureStrategy").toString();
                        scheduleObj.setFailureStrategy(FailureStrategy.valueOf(scheduleFailureStrategy));
                    }
                    if (ObjectUtils.allNotNull(json.get("scheduleReleaseState"))) {
                        scheduleReleaseState = json.get("scheduleReleaseState").toString();
                        scheduleObj.setReleaseState(ReleaseState.valueOf(scheduleReleaseState));
                    }
                    if (ObjectUtils.allNotNull(json.get("scheduleProcessInstancePriority"))) {
                        scheduleProcessInstancePriority = json.get("scheduleProcessInstancePriority").toString();
                        scheduleObj.setProcessInstancePriority(Priority.valueOf(scheduleProcessInstancePriority));
                    }
                    if (ObjectUtils.allNotNull(json.get("scheduleWorkerGroupId"))) {
                        scheduleWorkerGroupId = json.get("scheduleWorkerGroupId").toString();
                        if(scheduleWorkerGroupId != null){
                            scheduleObj.setWorkerGroupId(Integer.parseInt(scheduleWorkerGroupId));
                        }else{
                            if (ObjectUtils.allNotNull(json.get("scheduleWorkerGroupName"))) {
                                scheduleWorkerGroupName = json.get("scheduleWorkerGroupName").toString();
                                List<WorkerGroup> workerGroups = workerGroupMapper.queryWorkerGroupByName(scheduleWorkerGroupName);
                                if(workerGroups.size() > 0){
                                    scheduleObj.setWorkerGroupId(workerGroups.get(0).getId());
                                }
                            }
                        }
                    }
                    scheduleMapper.insert(scheduleObj);
                }
            }else{
                putMsg(result, Status.EXPORT_PROCESS_DEFINE_BY_ID_ERROR);
                return result;
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        putMsg(result, Status.SUCCESS);
        return result;
    }



    /**
     * check the process definition node meets the specifications
     *
     * @param processData process data
     * @param processDefinitionJson process definition json
     * @return check result code
     */
    public Map<String, Object> checkProcessNodeList(ProcessData processData, String processDefinitionJson) {

        Map<String, Object> result = new HashMap<>(5);
        try {
            if (processData == null) {
                logger.error("process data is null");
                putMsg(result,Status.DATA_IS_NOT_VALID, processDefinitionJson);
                return result;
            }

            // Check whether the task node is normal
            List<TaskNode> taskNodes = processData.getTasks();

            if (taskNodes == null) {
                logger.error("process node info is empty");
                putMsg(result, Status.DATA_IS_NULL, processDefinitionJson);
                return result;
            }

            // check has cycle
            if (graphHasCycle(taskNodes)) {
                logger.error("process DAG has cycle");
                putMsg(result, Status.PROCESS_NODE_HAS_CYCLE);
                return result;
            }

            // check whether the process definition json is normal
            for (TaskNode taskNode : taskNodes) {
                if (!CheckUtils.checkTaskNodeParameters(taskNode.getParams(), taskNode.getType())) {
                    logger.error("task node {} parameter invalid", taskNode.getName());
                    putMsg(result, Status.PROCESS_NODE_S_PARAMETER_INVALID, taskNode.getName());
                    return result;
                }

                // check extra params
                CheckUtils.checkOtherParams(taskNode.getExtras());
            }
            putMsg(result,Status.SUCCESS);
        } catch (Exception e) {
            result.put(Constants.STATUS, Status.REQUEST_PARAMS_NOT_VALID_ERROR);
            result.put(Constants.MSG, e.getMessage());
        }
        return result;
    }

    /**
     * get task node details based on process definition
     *
     * @param defineId define id
     * @return task node list
     * @throws Exception exception
     */
    public Map<String, Object> getTaskNodeListByDefinitionId(Integer defineId) throws Exception {
        Map<String, Object> result = new HashMap<>();

        ProcessDefinition processDefinition = processDefineMapper.selectById(defineId);
        if (processDefinition == null) {
            logger.info("process define not exists");
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, defineId);
            return result;
        }


        String processDefinitionJson = processDefinition.getProcessDefinitionJson();

        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);

        List<TaskNode> taskNodeList = (processData.getTasks() == null) ? new ArrayList<>() : processData.getTasks();

        result.put(Constants.DATA_LIST, taskNodeList);
        putMsg(result, Status.SUCCESS);

        return result;

    }

    /**
     * get task node details based on process definition
     *
     * @param defineIdList define id list
     * @return task node list
     * @throws Exception exception
     */
    public Map<String, Object> getTaskNodeListByDefinitionIdList(String defineIdList) throws Exception {
        Map<String, Object> result = new HashMap<>();

        Map<Integer, List<TaskNode>> taskNodeMap = new HashMap<>();
        String[] idList = defineIdList.split(",");
        List<String> definitionIdList = Arrays.asList(idList);
        List<Integer> idIntList = new ArrayList<>();
        for(String definitionId : definitionIdList) {
            idIntList.add(Integer.parseInt(definitionId));
        }
        Integer[] idArray = idIntList.toArray(new Integer[idIntList.size()]);
        List<ProcessDefinition> processDefinitionList = processDefineMapper.queryDefinitionListByIdList(idArray);
        if (processDefinitionList == null || processDefinitionList.size() ==0) {
            logger.info("process definition not exists");
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, defineIdList);
            return result;
        }

        for(ProcessDefinition processDefinition : processDefinitionList){
            String processDefinitionJson = processDefinition.getProcessDefinitionJson();
            ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);
            List<TaskNode> taskNodeList = (processData.getTasks() == null) ? new ArrayList<>() : processData.getTasks();
            taskNodeMap.put(processDefinition.getId(), taskNodeList);
        }

        result.put(Constants.DATA_LIST, taskNodeMap);
        putMsg(result, Status.SUCCESS);

        return result;

    }


    /**
     * query proccess definition all by project id
     *
     * @param projectId project id
     * @return process definitions in the project
     */
    public Map<String, Object> queryProccessDefinitionAllByProjectId(Integer projectId) {

        HashMap<String, Object> result = new HashMap<>(5);

        List<ProcessDefinition> resourceList = processDefineMapper.queryAllDefinitionList(projectId);
        result.put(Constants.DATA_LIST, resourceList);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * Encapsulates the TreeView structure
     *
     * @param processId process definition id
     * @param limit limit
     * @return tree view json data
     * @throws Exception exception
     */
    public Map<String, Object> viewTree(Integer processId, Integer limit) throws Exception {
        Map<String, Object> result = new HashMap<>();

        ProcessDefinition processDefinition = processDefineMapper.selectById(processId);
        if (processDefinition == null) {
            logger.info("process define not exists");
            throw new RuntimeException("process define not exists");
        }
        DAG<String, TaskNode, TaskNodeRelation> dag = genDagGraph(processDefinition);
        /**
         * nodes that is running
         */
        Map<String, List<TreeViewDto>> runningNodeMap = new ConcurrentHashMap<>();

        /**
         * nodes that is waiting torun
         */
        Map<String, List<TreeViewDto>> waitingRunningNodeMap = new ConcurrentHashMap<>();

        /**
         * List of process instances
         */
        List<ProcessInstance> processInstanceList = processInstanceMapper.queryByProcessDefineId(processId, limit);

        for(ProcessInstance processInstance:processInstanceList){
            processInstance.setDuration(DateUtils.differSec(processInstance.getStartTime(),processInstance.getEndTime()));
        }

        if (limit > processInstanceList.size()) {
            limit = processInstanceList.size();
        }

        TreeViewDto parentTreeViewDto = new TreeViewDto();
        parentTreeViewDto.setName("DAG");
        parentTreeViewDto.setType("");
        // Specify the process definition, because it is a TreeView for a process definition

        for (int i = limit - 1; i >= 0; i--) {
            ProcessInstance processInstance = processInstanceList.get(i);

            Date endTime = processInstance.getEndTime() == null ? new Date() : processInstance.getEndTime();
            parentTreeViewDto.getInstances().add(new Instance(processInstance.getId(), processInstance.getName(), "", processInstance.getState().toString()
                    , processInstance.getStartTime(), endTime, processInstance.getHost(), DateUtils.format2Readable(endTime.getTime() - processInstance.getStartTime().getTime())));
        }

        List<TreeViewDto> parentTreeViewDtoList = new ArrayList<>();
        parentTreeViewDtoList.add(parentTreeViewDto);
        // Here is the encapsulation task instance
        for (String startNode : dag.getBeginNode()) {
            runningNodeMap.put(startNode, parentTreeViewDtoList);
        }

        while (Stopper.isRunning()) {
            Set<String> postNodeList = null;
            Iterator<Map.Entry<String, List<TreeViewDto>>> iter = runningNodeMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, List<TreeViewDto>> en = iter.next();
                String nodeName = en.getKey();
                parentTreeViewDtoList = en.getValue();

                TreeViewDto treeViewDto = new TreeViewDto();
                treeViewDto.setName(nodeName);
                TaskNode taskNode = dag.getNode(nodeName);
                treeViewDto.setType(taskNode.getType());


                //set treeViewDto instances
                for (int i = limit - 1; i >= 0; i--) {
                    ProcessInstance processInstance = processInstanceList.get(i);
                    TaskInstance taskInstance = taskInstanceMapper.queryByInstanceIdAndName(processInstance.getId(), nodeName);
                    if (taskInstance == null) {
                        treeViewDto.getInstances().add(new Instance(-1, "not running", "null"));
                    } else {
                        Date startTime = taskInstance.getStartTime() == null ? new Date() : taskInstance.getStartTime();
                        Date endTime = taskInstance.getEndTime() == null ? new Date() : taskInstance.getEndTime();

                        int subProcessId = 0;
                        /**
                         * if process is sub process, the return sub id, or sub id=0
                         */
                        if (taskInstance.getTaskType().equals(TaskType.SUB_PROCESS.name())) {
                            String taskJson = taskInstance.getTaskJson();
                            taskNode = JSON.parseObject(taskJson, TaskNode.class);
                            subProcessId = Integer.parseInt(JSON.parseObject(
                                    taskNode.getParams()).getString(CMDPARAM_SUB_PROCESS_DEFINE_ID));
                        }
                        treeViewDto.getInstances().add(new Instance(taskInstance.getId(), taskInstance.getName(), taskInstance.getTaskType(), taskInstance.getState().toString()
                                , taskInstance.getStartTime(), taskInstance.getEndTime(), taskInstance.getHost(), DateUtils.format2Readable(endTime.getTime() - startTime.getTime()), subProcessId));
                    }
                }
                for (TreeViewDto pTreeViewDto : parentTreeViewDtoList) {
                    pTreeViewDto.getChildren().add(treeViewDto);
                }
                postNodeList = dag.getSubsequentNodes(nodeName);
                if (postNodeList != null && postNodeList.size() > 0) {
                    for (String nextNodeName : postNodeList) {
                        List<TreeViewDto> treeViewDtoList = waitingRunningNodeMap.get(nextNodeName);
                        if (treeViewDtoList != null && treeViewDtoList.size() > 0) {
                            treeViewDtoList.add(treeViewDto);
                            waitingRunningNodeMap.put(nextNodeName, treeViewDtoList);
                        } else {
                            treeViewDtoList = new ArrayList<>();
                            treeViewDtoList.add(treeViewDto);
                            waitingRunningNodeMap.put(nextNodeName, treeViewDtoList);
                        }
                    }
                }
                runningNodeMap.remove(nodeName);
            }

            if (waitingRunningNodeMap == null || waitingRunningNodeMap.size() == 0) {
                break;
            } else {
                runningNodeMap.putAll(waitingRunningNodeMap);
                waitingRunningNodeMap.clear();
            }
        }
        result.put(Constants.DATA_LIST, parentTreeViewDto);
        result.put(Constants.STATUS, Status.SUCCESS);
        result.put(Constants.MSG, Status.SUCCESS.getMsg());
        return result;
    }


    /**
     * Generate the DAG Graph based on the process definition id
     *
     * @param processDefinition process definition
     * @return dag graph
     * @throws Exception if exception happens
     */
    private DAG<String, TaskNode, TaskNodeRelation> genDagGraph(ProcessDefinition processDefinition) throws Exception {

        String processDefinitionJson = processDefinition.getProcessDefinitionJson();

        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);

        List<TaskNode> taskNodeList = processData.getTasks();

        processDefinition.setGlobalParamList(processData.getGlobalParams());


        List<TaskNodeRelation> taskNodeRelations = new ArrayList<>();

        // Traverse node information and build relationships
        for (TaskNode taskNode : taskNodeList) {
            String preTasks = taskNode.getPreTasks();
            List<String> preTasksList = JSONUtils.toList(preTasks, String.class);

            // If the dependency is not empty
            if (preTasksList != null) {
                for (String depNode : preTasksList) {
                    taskNodeRelations.add(new TaskNodeRelation(depNode, taskNode.getName()));
                }
            }
        }

        ProcessDag processDag = new ProcessDag();
        processDag.setEdges(taskNodeRelations);
        processDag.setNodes(taskNodeList);


        // Generate concrete Dag to be executed
        return genDagGraph(processDag);


    }

    /**
     * Generate the DAG of process
     *
     * @return DAG
     */
    private DAG<String, TaskNode, TaskNodeRelation> genDagGraph(ProcessDag processDag) {
        DAG<String, TaskNode, TaskNodeRelation> dag = new DAG<>();

        /**
         * Add the ndoes
         */
        if (CollectionUtils.isNotEmpty(processDag.getNodes())) {
            for (TaskNode node : processDag.getNodes()) {
                dag.addNode(node.getName(), node);
            }
        }

        /**
         * Add the edges
         */
        if (CollectionUtils.isNotEmpty(processDag.getEdges())) {
            for (TaskNodeRelation edge : processDag.getEdges()) {
                dag.addEdge(edge.getStartNode(), edge.getEndNode());
            }
        }

        return dag;
    }


    /**
     * whether the graph has a ring
     *
     * @param taskNodeResponseList
     * @return
     */
    private boolean graphHasCycle(List<TaskNode> taskNodeResponseList) {
        DAG<String, TaskNode, String> graph = new DAG<>();

        // Fill the vertices
        for (TaskNode taskNodeResponse : taskNodeResponseList) {
            graph.addNode(taskNodeResponse.getName(), taskNodeResponse);
        }

        // Fill edge relations
        for (TaskNode taskNodeResponse : taskNodeResponseList) {
            taskNodeResponse.getPreTasks();
            List<String> preTasks = JSONUtils.toList(taskNodeResponse.getPreTasks(),String.class);
            if (CollectionUtils.isNotEmpty(preTasks)) {
                for (String preTask : preTasks) {
                    if (!graph.addEdge(preTask, taskNodeResponse.getName())) {
                        return true;
                    }
                }
            }
        }

        return graph.hasCycle();
    }

    private String recursionProcessDefinitionName(Integer projectId,String processDefinitionName,int num){
        ProcessDefinition processDefinition = processDefineMapper.queryByDefineName(projectId, processDefinitionName);
        if (processDefinition != null) {
            if(num>1){
                String str = processDefinitionName.substring(0,processDefinitionName.length() - 3);
                processDefinitionName = str + "("+num+")";
            }else{
                processDefinitionName = processDefinition.getName() + "("+num+")";
            }
        }else{
            return processDefinitionName;
        }
        return recursionProcessDefinitionName(projectId,processDefinitionName,num + 1);
    }

}

