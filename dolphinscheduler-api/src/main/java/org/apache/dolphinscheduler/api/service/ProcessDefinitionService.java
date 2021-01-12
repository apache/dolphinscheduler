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

import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_SUB_PROCESS_DEFINE_ID;

import org.apache.dolphinscheduler.api.dto.ProcessMeta;
import org.apache.dolphinscheduler.api.dto.treeview.Instance;
import org.apache.dolphinscheduler.api.dto.treeview.TreeViewDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.CheckUtils;
import org.apache.dolphinscheduler.api.utils.FileUtils;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.exportprocess.ProcessAddTaskParam;
import org.apache.dolphinscheduler.api.utils.exportprocess.TaskNodeParamFactory;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.process.ProcessDag;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.common.utils.TaskParametersUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessData;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.utils.DagHelper;
import org.apache.dolphinscheduler.service.permission.PermissionCheck;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * process definition service
 */
@Service
public class ProcessDefinitionService extends BaseDAGService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionService.class);

    private static final String PROCESSDEFINITIONID = "processDefinitionId";

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
    private ProcessService processService;

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
    public Map<String, Object> createProcessDefinition(User loginUser,
                                                       String projectName,
                                                       String name,
                                                       String processDefinitionJson,
                                                       String desc,
                                                       String locations,
                                                       String connects) throws JsonProcessingException {

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
        processDefine.setModifyBy(loginUser.getUserName());
        processDefine.setResourceIds(getResourceIds(processData));

        //custom global params
        List<Property> globalParamsList = processData.getGlobalParams();
        if (CollectionUtils.isNotEmpty(globalParamsList)) {
            Set<Property> globalParamsSet = new HashSet<>(globalParamsList);
            globalParamsList = new ArrayList<>(globalParamsSet);
            processDefine.setGlobalParamList(globalParamsList);
        }
        processDefine.setCreateTime(now);
        processDefine.setUpdateTime(now);
        processDefine.setFlag(Flag.YES);
        processDefineMapper.insert(processDefine);
        putMsg(result, Status.SUCCESS);
        result.put(PROCESSDEFINITIONID,processDefine.getId());
        return result;
    }

    /**
     * get resource ids
     * @param processData process data
     * @return resource ids
     */
    private String getResourceIds(ProcessData processData) {
        List<TaskNode> tasks = processData.getTasks();
        Set<Integer> resourceIds = new HashSet<>();
        for (TaskNode taskNode : tasks) {
            String taskParameter = taskNode.getParams();
            AbstractParameters params = TaskParametersUtils.getParameters(taskNode.getType(), taskParameter);
            if (CollectionUtils.isNotEmpty(params.getResourceFilesList())) {
                Set<Integer> tempSet = params.getResourceFilesList().stream().filter(t -> t.getId() != 0).map(ResourceInfo::getId).collect(Collectors.toSet());
                resourceIds.addAll(tempSet);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i : resourceIds) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(i);
        }
        return sb.toString();
    }

    /**
     * query process definition list
     *
     * @param loginUser login user
     * @param projectName project name
     * @return definition list
     */
    public Map<String, Object> queryProcessDefinitionList(User loginUser, String projectName) {

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
     * query process definition list paging
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
    public Map<String, Object> queryProcessDefinitionById(User loginUser, String projectName, Integer processId) {

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
     * copy process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processId process definition id
     * @return copy result code
     */
    public Map<String, Object> copyProcessDefinition(User loginUser, String projectName, Integer processId) throws JsonProcessingException {

        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }

        ProcessDefinition processDefinition = processDefineMapper.selectById(processId);
        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, processId);
            return result;
        } else {
            return createProcessDefinition(
                    loginUser,
                    projectName,
                    processDefinition.getName() + "_copy_" + System.currentTimeMillis(),
                    processDefinition.getProcessDefinitionJson(),
                    processDefinition.getDescription(),
                    processDefinition.getLocations(),
                    processDefinition.getConnects());
        }
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
        ProcessDefinition processDefine = processService.findProcessDefineById(id);
        // check process definition exists
        if (processDefine == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, id);
            return result;
        }

        if (processDefine.getReleaseState() == ReleaseState.ONLINE) {
            // online can not permit edit
            putMsg(result, Status.PROCESS_DEFINE_NOT_ALLOWED_EDIT, processDefine.getName());
            return result;
        }

        if (!name.equals(processDefine.getName())) {
            // check whether the new process define name exist
            ProcessDefinition definition = processDefineMapper.verifyByDefineName(project.getId(), name);
            if (definition != null) {
                putMsg(result, Status.VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR, name);
                return result;
            }
        }

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
        processDefine.setModifyBy(loginUser.getUserName());
        processDefine.setResourceIds(getResourceIds(processData));

        //custom global params
        List<Property> globalParamsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(processData.getGlobalParams())) {
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
    public Map<String, Object> verifyProcessDefinitionName(User loginUser, String projectName, String name) {

        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }
        ProcessDefinition processDefinition = processDefineMapper.verifyByDefineName(project.getId(), name);
        if (processDefinition == null) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR, name);
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
            logger.warn("scheduler num is {},Greater than 1", schedules.size());
            putMsg(result, Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR);
            return result;
        } else if (schedules.size() == 1) {
            Schedule schedule = schedules.get(0);
            if (schedule.getReleaseState() == ReleaseState.OFFLINE) {
                scheduleMapper.deleteById(schedule.getId());
            } else if (schedule.getReleaseState() == ReleaseState.ONLINE) {
                putMsg(result, Status.SCHEDULE_CRON_STATE_ONLINE, schedule.getId());
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

        // check state
        if (null == state) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, "releaseState");
            return result;
        }

        ProcessDefinition processDefinition = processDefineMapper.selectById(id);

        switch (state) {
            case ONLINE:
                // To check resources whether they are already cancel authorized or deleted
                String resourceIds = processDefinition.getResourceIds();
                if (StringUtils.isNotBlank(resourceIds)) {
                    Integer[] resourceIdArray = Arrays.stream(resourceIds.split(",")).map(Integer::parseInt).toArray(Integer[]::new);
                    PermissionCheck<Integer> permissionCheck = new PermissionCheck(AuthorizationType.RESOURCE_FILE_ID,processService,resourceIdArray,loginUser.getId(),logger);
                    try {
                        permissionCheck.checkPermission();
                    } catch (Exception e) {
                        logger.error(e.getMessage(),e);
                        putMsg(result, Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION, "releaseState");
                        return result;
                    }
                }

                processDefinition.setReleaseState(state);
                processDefineMapper.updateById(processDefinition);
                break;
            case OFFLINE:
                processDefinition.setReleaseState(state);
                processDefineMapper.updateById(processDefinition);
                List<Schedule> scheduleList = scheduleMapper.selectAllByProcessDefineArray(
                        new int[]{processDefinition.getId()}
                );

                for (Schedule schedule:scheduleList) {
                    logger.info("set schedule offline, project id: {}, schedule id: {}, process definition id: {}", project.getId(), schedule.getId(), id);
                    // set status
                    schedule.setReleaseState(ReleaseState.OFFLINE);
                    scheduleMapper.updateById(schedule);
                    SchedulerService.deleteSchedule(project.getId(), schedule.getId());
                }
                break;
            default:
                putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, "releaseState");
                return result;
        }

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * batch export process definition by ids
     * @param loginUser
     * @param projectName
     * @param processDefinitionIds
     * @param response
     */
    public void batchExportProcessDefinitionByIds(User loginUser, String projectName, String processDefinitionIds, HttpServletResponse response) {

        if (StringUtils.isEmpty(processDefinitionIds)) {
            return;
        }

        //export project info
        Project project = projectMapper.queryByName(projectName);

        //check user access for project
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);

        if (resultStatus != Status.SUCCESS) {
            return;
        }

        List<ProcessMeta> processDefinitionList =
                getProcessDefinitionList(processDefinitionIds);

        if (CollectionUtils.isNotEmpty(processDefinitionList)) {
            downloadProcessDefinitionFile(response, processDefinitionList);
        }
    }

    /**
     * get process definition list by ids
     * @param processDefinitionIds
     * @return
     */
    private List<ProcessMeta> getProcessDefinitionList(String processDefinitionIds) {
        List<ProcessMeta> processDefinitionList = new ArrayList<>();
        String[] processDefinitionIdArray = processDefinitionIds.split(",");
        for (String strProcessDefinitionId : processDefinitionIdArray) {
            //get workflow info
            int processDefinitionId = Integer.parseInt(strProcessDefinitionId);
            ProcessDefinition processDefinition = processDefineMapper.queryByDefineId(processDefinitionId);
            if (null != processDefinition) {
                processDefinitionList.add(exportProcessMetaData(processDefinitionId, processDefinition));
            }
        }

        return processDefinitionList;
    }

    /**
     * download the process definition file
     * @param response
     * @param processDefinitionList
     */
    private void downloadProcessDefinitionFile(HttpServletResponse response, List<ProcessMeta> processDefinitionList) {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        BufferedOutputStream buff = null;
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            buff = new BufferedOutputStream(out);
            buff.write(JSON.toJSONString(processDefinitionList).getBytes(StandardCharsets.UTF_8));
            buff.flush();
            buff.close();
        } catch (IOException e) {
            logger.warn("export process fail", e);
        } finally {
            if (null != buff) {
                try {
                    buff.close();
                } catch (Exception e) {
                    logger.warn("export process buffer not close", e);
                }
            }
            if (null != out) {
                try {
                    out.close();
                } catch (Exception e) {
                    logger.warn("export process output stream not close", e);
                }
            }
        }
    }

    /**
     * get export process metadata string
     * @param processDefinitionId process definition id
     * @param processDefinition process definition
     * @return export process metadata string
     */
    public String exportProcessMetaDataStr(Integer processDefinitionId, ProcessDefinition processDefinition) {
        //create workflow json file
        return JSONUtils.toJsonString(exportProcessMetaData(processDefinitionId,processDefinition));
    }

    /**
     * get export process metadata string
     * @param processDefinitionId process definition id
     * @param processDefinition process definition
     * @return export process metadata string
     */
    public ProcessMeta exportProcessMetaData(Integer processDefinitionId, ProcessDefinition processDefinition) {
        //correct task param which has data source or dependent param
        String correctProcessDefinitionJson = addExportTaskNodeSpecialParam(processDefinition.getProcessDefinitionJson());
        processDefinition.setProcessDefinitionJson(correctProcessDefinitionJson);

        //export process metadata
        ProcessMeta exportProcessMeta = new ProcessMeta();
        exportProcessMeta.setProjectName(processDefinition.getProjectName());
        exportProcessMeta.setProcessDefinitionName(processDefinition.getName());
        exportProcessMeta.setProcessDefinitionJson(processDefinition.getProcessDefinitionJson());
        exportProcessMeta.setProcessDefinitionLocations(processDefinition.getLocations());
        exportProcessMeta.setProcessDefinitionConnects(processDefinition.getConnects());

        //schedule info
        List<Schedule> schedules = scheduleMapper.queryByProcessDefinitionId(processDefinitionId);
        if (!schedules.isEmpty()) {
            Schedule schedule = schedules.get(0);
            exportProcessMeta.setScheduleWarningType(schedule.getWarningType().toString());
            exportProcessMeta.setScheduleWarningGroupId(schedule.getWarningGroupId());
            exportProcessMeta.setScheduleStartTime(DateUtils.dateToString(schedule.getStartTime()));
            exportProcessMeta.setScheduleEndTime(DateUtils.dateToString(schedule.getEndTime()));
            exportProcessMeta.setScheduleCrontab(schedule.getCrontab());
            exportProcessMeta.setScheduleFailureStrategy(String.valueOf(schedule.getFailureStrategy()));
            exportProcessMeta.setScheduleReleaseState(String.valueOf(ReleaseState.OFFLINE));
            exportProcessMeta.setScheduleProcessInstancePriority(String.valueOf(schedule.getProcessInstancePriority()));
            exportProcessMeta.setScheduleWorkerGroupName(schedule.getWorkerGroup());
        }
        //create workflow json file
        return exportProcessMeta;
    }

    /**
     * correct task param which has datasource or dependent
     * @param processDefinitionJson processDefinitionJson
     * @return correct processDefinitionJson
     */
    public String addExportTaskNodeSpecialParam(String processDefinitionJson) {
        JSONObject jsonObject = JSONUtils.parseObject(processDefinitionJson);
        JSONArray jsonArray = (JSONArray) jsonObject.get("tasks");

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject taskNode = jsonArray.getJSONObject(i);
            if (StringUtils.isNotEmpty(taskNode.getString("type"))) {
                String taskType = taskNode.getString("type");

                ProcessAddTaskParam addTaskParam = TaskNodeParamFactory.getByTaskType(taskType);
                if (null != addTaskParam) {
                    addTaskParam.addExportSpecialParam(taskNode);
                }
            }
        }
        jsonObject.put("tasks", jsonArray);
        return jsonObject.toString();
    }

    /**
     * check task if has sub process
     * @param taskType task type
     * @return if task has sub process return true else false
     */
    private boolean checkTaskHasSubProcess(String taskType) {
        return taskType.equals(TaskType.SUB_PROCESS.name());
    }

    /**
     * import process definition
     * @param loginUser login user
     * @param file process metadata json file
     * @param currentProjectName current project name
     * @return import process
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importProcessDefinition(User loginUser, MultipartFile file, String currentProjectName) {
        Map<String, Object> result = new HashMap<>(5);
        String processMetaJson = FileUtils.file2String(file);
        List<ProcessMeta> processMetaList = JSON.parseArray(processMetaJson,ProcessMeta.class);

        //check file content
        if (CollectionUtils.isEmpty(processMetaList)) {
            putMsg(result, Status.DATA_IS_NULL, "fileContent");
            return result;
        }

        for (ProcessMeta processMeta:processMetaList) {

            if (!checkAndImportProcessDefinition(loginUser, currentProjectName, result, processMeta)) {
                return result;
            }
        }

        return result;
    }

    /**
     * check and import process definition
     * @param loginUser
     * @param currentProjectName
     * @param result
     * @param processMeta
     * @return
     */
    private boolean checkAndImportProcessDefinition(User loginUser, String currentProjectName, Map<String, Object> result, ProcessMeta processMeta) {

        if (!checkImportanceParams(processMeta,result)) {
            return false;
        }

        //deal with process name
        String processDefinitionName = processMeta.getProcessDefinitionName();
        //use currentProjectName to query
        Project targetProject = projectMapper.queryByName(currentProjectName);
        if (null != targetProject) {
            processDefinitionName = recursionProcessDefinitionName(targetProject.getId(),
                    processDefinitionName, 1);
        }

        //unique check
        Map<String, Object> checkResult = verifyProcessDefinitionName(loginUser, currentProjectName, processDefinitionName);
        Status status = (Status) checkResult.get(Constants.STATUS);
        if (Status.SUCCESS.equals(status)) {
            putMsg(result, Status.SUCCESS);
        } else {
            result.putAll(checkResult);
            return false;
        }

        // get create process result
        Map<String, Object> createProcessResult =
                getCreateProcessResult(loginUser,
                        currentProjectName,
                        result,
                        processMeta,
                        processDefinitionName,
                        addImportTaskNodeParam(loginUser, processMeta.getProcessDefinitionJson(), targetProject));

        if (createProcessResult == null) {
            return false;
        }

        //create process definition
        Integer processDefinitionId =
                Objects.isNull(createProcessResult.get(PROCESSDEFINITIONID)) ? null : Integer.parseInt(createProcessResult.get(PROCESSDEFINITIONID).toString());

        //scheduler param
        return getImportProcessScheduleResult(loginUser,
                currentProjectName,
                result,
                processMeta,
                processDefinitionName,
                processDefinitionId);

    }

    /**
     * get create process result
     * @param loginUser
     * @param currentProjectName
     * @param result
     * @param processMeta
     * @param processDefinitionName
     * @param importProcessParam
     * @return
     */
    private Map<String, Object> getCreateProcessResult(User loginUser,
                                                       String currentProjectName,
                                                       Map<String, Object> result,
                                                       ProcessMeta processMeta,
                                                       String processDefinitionName,
                                                       String importProcessParam) {
        Map<String, Object> createProcessResult = null;
        try {
            createProcessResult = createProcessDefinition(loginUser
                    ,currentProjectName,
                    processDefinitionName + "_import_" + System.currentTimeMillis(),
                    importProcessParam,
                    processMeta.getProcessDefinitionDescription(),
                    processMeta.getProcessDefinitionLocations(),
                    processMeta.getProcessDefinitionConnects());
            putMsg(result, Status.SUCCESS);
        } catch (JsonProcessingException e) {
            logger.error("import process meta json data: {}", e.getMessage(), e);
            putMsg(result, Status.IMPORT_PROCESS_DEFINE_ERROR);
        }

        return createProcessResult;
    }

    /**
     * get import process schedule result
     * @param loginUser
     * @param currentProjectName
     * @param result
     * @param processMeta
     * @param processDefinitionName
     * @param processDefinitionId
     * @return
     */
    private boolean getImportProcessScheduleResult(User loginUser,
                                                   String currentProjectName,
                                                   Map<String, Object> result,
                                                   ProcessMeta processMeta,
                                                   String processDefinitionName,
                                                   Integer processDefinitionId) {
        if (null != processMeta.getScheduleCrontab() && null != processDefinitionId) {
            int scheduleInsert = importProcessSchedule(loginUser,
                    currentProjectName,
                    processMeta,
                    processDefinitionName,
                    processDefinitionId);

            if (0 == scheduleInsert) {
                putMsg(result, Status.IMPORT_PROCESS_DEFINE_ERROR);
                return false;
            }
        }
        return true;
    }

    /**
     * check importance params
     * @param processMeta
     * @param result
     * @return
     */
    private boolean checkImportanceParams(ProcessMeta processMeta,Map<String, Object> result) {
        if (StringUtils.isEmpty(processMeta.getProjectName())) {
            putMsg(result, Status.DATA_IS_NULL, "projectName");
            return false;
        }
        if (StringUtils.isEmpty(processMeta.getProcessDefinitionName())) {
            putMsg(result, Status.DATA_IS_NULL, "processDefinitionName");
            return false;
        }
        if (StringUtils.isEmpty(processMeta.getProcessDefinitionJson())) {
            putMsg(result, Status.DATA_IS_NULL, "processDefinitionJson");
            return false;
        }

        return true;
    }

    /**
     * import process add special task param
     * @param loginUser login user
     * @param processDefinitionJson process definition json
     * @param targetProject target project
     * @return import process param
     */
    private String addImportTaskNodeParam(User loginUser, String processDefinitionJson, Project targetProject) {
        JSONObject jsonObject = JSONUtils.parseObject(processDefinitionJson);
        JSONArray jsonArray = (JSONArray) jsonObject.get("tasks");
        //add sql and dependent param
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject taskNode = jsonArray.getJSONObject(i);
            String taskType = taskNode.getString("type");
            ProcessAddTaskParam addTaskParam = TaskNodeParamFactory.getByTaskType(taskType);
            if (null != addTaskParam) {
                addTaskParam.addImportSpecialParam(taskNode);
            }
        }

        //recursive sub-process parameter correction map key for old process id value for new process id
        Map<Integer, Integer> subProcessIdMap = new HashMap<>(20);

        List<Object> subProcessList = jsonArray.stream()
                .filter(elem -> checkTaskHasSubProcess(JSONUtils.parseObject(elem.toString()).getString("type")))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(subProcessList)) {
            importSubProcess(loginUser, targetProject, jsonArray, subProcessIdMap);
        }

        jsonObject.put("tasks", jsonArray);
        return jsonObject.toString();
    }

    /**
     * import process schedule
     * @param loginUser login user
     * @param currentProjectName current project name
     * @param processMeta process meta data
     * @param processDefinitionName process definition name
     * @param processDefinitionId process definition id
     * @return insert schedule flag
     */
    public int importProcessSchedule(User loginUser, String currentProjectName, ProcessMeta processMeta,
                                     String processDefinitionName, Integer processDefinitionId) {
        Date now = new Date();
        Schedule scheduleObj = new Schedule();
        scheduleObj.setProjectName(currentProjectName);
        scheduleObj.setProcessDefinitionId(processDefinitionId);
        scheduleObj.setProcessDefinitionName(processDefinitionName);
        scheduleObj.setCreateTime(now);
        scheduleObj.setUpdateTime(now);
        scheduleObj.setUserId(loginUser.getId());
        scheduleObj.setUserName(loginUser.getUserName());

        scheduleObj.setCrontab(processMeta.getScheduleCrontab());

        if (null != processMeta.getScheduleStartTime()) {
            scheduleObj.setStartTime(DateUtils.stringToDate(processMeta.getScheduleStartTime()));
        }
        if (null != processMeta.getScheduleEndTime()) {
            scheduleObj.setEndTime(DateUtils.stringToDate(processMeta.getScheduleEndTime()));
        }
        if (null != processMeta.getScheduleWarningType()) {
            scheduleObj.setWarningType(WarningType.valueOf(processMeta.getScheduleWarningType()));
        }
        if (null != processMeta.getScheduleWarningGroupId()) {
            scheduleObj.setWarningGroupId(processMeta.getScheduleWarningGroupId());
        }
        if (null != processMeta.getScheduleFailureStrategy()) {
            scheduleObj.setFailureStrategy(FailureStrategy.valueOf(processMeta.getScheduleFailureStrategy()));
        }
        if (null != processMeta.getScheduleReleaseState()) {
            scheduleObj.setReleaseState(ReleaseState.valueOf(processMeta.getScheduleReleaseState()));
        }
        if (null != processMeta.getScheduleProcessInstancePriority()) {
            scheduleObj.setProcessInstancePriority(Priority.valueOf(processMeta.getScheduleProcessInstancePriority()));
        }

        if (null != processMeta.getScheduleWorkerGroupName()) {
            scheduleObj.setWorkerGroup(processMeta.getScheduleWorkerGroupName());
        }

        return scheduleMapper.insert(scheduleObj);
    }

    /**
     * check import process has sub process
     * recursion create sub process
     * @param loginUser login user
     * @param targetProject target project
     * @param jsonArray process task array
     * @param subProcessIdMap correct sub process id map
     */
    public void importSubProcess(User loginUser, Project targetProject, JSONArray jsonArray, Map<Integer, Integer> subProcessIdMap) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject taskNode = jsonArray.getJSONObject(i);
            String taskType = taskNode.getString("type");

            if (checkTaskHasSubProcess(taskType)) {
                //get sub process info
                JSONObject subParams = JSONUtils.parseObject(taskNode.getString("params"));
                Integer subProcessId = subParams.getInteger(PROCESSDEFINITIONID);
                ProcessDefinition subProcess = processDefineMapper.queryByDefineId(subProcessId);
                //check is sub process exist in db
                if (null != subProcess) {
                    String subProcessJson = subProcess.getProcessDefinitionJson();
                    //check current project has sub process
                    ProcessDefinition currentProjectSubProcess = processDefineMapper.queryByDefineName(targetProject.getId(), subProcess.getName());

                    if (null == currentProjectSubProcess) {
                        JSONArray subJsonArray = (JSONArray) JSONUtils.parseObject(subProcess.getProcessDefinitionJson()).get("tasks");

                        List<Object> subProcessList = subJsonArray.stream()
                                .filter(item -> checkTaskHasSubProcess(JSONUtils.parseObject(item.toString()).getString("type")))
                                .collect(Collectors.toList());

                        if (CollectionUtils.isNotEmpty(subProcessList)) {
                            importSubProcess(loginUser, targetProject, subJsonArray, subProcessIdMap);
                            //sub process processId correct
                            if (!subProcessIdMap.isEmpty()) {

                                for (Map.Entry<Integer, Integer> entry : subProcessIdMap.entrySet()) {
                                    String oldSubProcessId = "\"processDefinitionId\":" + entry.getKey();
                                    String newSubProcessId = "\"processDefinitionId\":" + entry.getValue();
                                    subProcessJson = subProcessJson.replaceAll(oldSubProcessId, newSubProcessId);
                                }

                                subProcessIdMap.clear();
                            }
                        }

                        //if sub-process recursion
                        Date now = new Date();
                        //create sub process in target project
                        ProcessDefinition processDefine = new ProcessDefinition();
                        processDefine.setName(subProcess.getName());
                        processDefine.setVersion(subProcess.getVersion());
                        processDefine.setReleaseState(subProcess.getReleaseState());
                        processDefine.setProjectId(targetProject.getId());
                        processDefine.setUserId(loginUser.getId());
                        processDefine.setProcessDefinitionJson(subProcessJson);
                        processDefine.setDescription(subProcess.getDescription());
                        processDefine.setLocations(subProcess.getLocations());
                        processDefine.setConnects(subProcess.getConnects());
                        processDefine.setTimeout(subProcess.getTimeout());
                        processDefine.setTenantId(subProcess.getTenantId());
                        processDefine.setGlobalParams(subProcess.getGlobalParams());
                        processDefine.setCreateTime(now);
                        processDefine.setUpdateTime(now);
                        processDefine.setFlag(subProcess.getFlag());
                        processDefine.setReceivers(subProcess.getReceivers());
                        processDefine.setReceiversCc(subProcess.getReceiversCc());
                        processDefineMapper.insert(processDefine);

                        logger.info("create sub process, project: {}, process name: {}", targetProject.getName(), processDefine.getName());

                        //modify task node
                        ProcessDefinition newSubProcessDefine = processDefineMapper.queryByDefineName(processDefine.getProjectId(),processDefine.getName());

                        if (null != newSubProcessDefine) {
                            subProcessIdMap.put(subProcessId, newSubProcessDefine.getId());
                            subParams.put(PROCESSDEFINITIONID, newSubProcessDefine.getId());
                            taskNode.put("params", subParams);
                        }
                    }
                }
            }
        }
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

        //process data check
        if (null == processData) {
            logger.error("process data is null");
            putMsg(result,Status.DATA_IS_NOT_VALID, processDefinitionJson);
            return result;
        }

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
        List<Integer> idIntList = new ArrayList<>();
        for (String definitionId : idList) {
            idIntList.add(Integer.parseInt(definitionId));
        }
        Integer[] idArray = idIntList.toArray(new Integer[idIntList.size()]);
        List<ProcessDefinition> processDefinitionList = processDefineMapper.queryDefinitionListByIdList(idArray);
        if (CollectionUtils.isEmpty(processDefinitionList)) {
            logger.info("process definition not exists");
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, defineIdList);
            return result;
        }

        for (ProcessDefinition processDefinition : processDefinitionList) {
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
     * query process definition all by project id
     *
     * @param projectId project id
     * @return process definitions in the project
     */
    public Map<String, Object> queryProcessDefinitionAllByProjectId(Integer projectId) {

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
        if (null == processDefinition) {
            logger.info("process define not exists");
            putMsg(result,Status.PROCESS_DEFINE_NOT_EXIST, processDefinition);
            return result;
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

        for (ProcessInstance processInstance:processInstanceList) {
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
                        treeViewDto.getInstances().add(new Instance(-1, "not running", null));
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
                if (CollectionUtils.isNotEmpty(postNodeList)) {
                    for (String nextNodeName : postNodeList) {
                        List<TreeViewDto> treeViewDtoList = waitingRunningNodeMap.get(nextNodeName);
                        if (CollectionUtils.isNotEmpty(treeViewDtoList)) {
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

        //check process data
        if (null != processData) {
            List<TaskNode> taskNodeList = processData.getTasks();
            processDefinition.setGlobalParamList(processData.getGlobalParams());
            ProcessDag processDag = DagHelper.getProcessDag(taskNodeList);

            // Generate concrete Dag to be executed
            return DagHelper.buildDagGraph(processDag);
        }

        return new DAG<>();
    }

    /**
     * whether the graph has a ring
     *
     * @param taskNodeResponseList task node response list
     * @return if graph has cycle flag
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

    private String recursionProcessDefinitionName(Integer projectId,String processDefinitionName,int num) {
        ProcessDefinition processDefinition = processDefineMapper.queryByDefineName(projectId, processDefinitionName);
        if (processDefinition != null) {
            if (num > 1) {
                String str = processDefinitionName.substring(0,processDefinitionName.length() - 3);
                processDefinitionName = str + "(" + num + ")";
            } else {
                processDefinitionName = processDefinition.getName() + "(" + num + ")";
            }
        } else {
            return processDefinitionName;
        }
        return recursionProcessDefinitionName(projectId,processDefinitionName,num + 1);
    }

}

