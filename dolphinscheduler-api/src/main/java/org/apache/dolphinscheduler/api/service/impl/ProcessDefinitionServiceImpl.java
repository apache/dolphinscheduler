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

import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_SUB_PROCESS_DEFINE_ID;

import org.apache.dolphinscheduler.api.dto.ProcessMeta;
import org.apache.dolphinscheduler.api.dto.treeview.Instance;
import org.apache.dolphinscheduler.api.dto.treeview.TreeViewDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.utils.CheckUtils;
import org.apache.dolphinscheduler.api.utils.FileUtils;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.exportprocess.ProcessAddTaskParam;
import org.apache.dolphinscheduler.api.utils.exportprocess.TaskNodeParamFactory;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.SnowFlakeUtils;
import org.apache.dolphinscheduler.common.utils.SnowFlakeUtils.SnowFlakeException;
import org.apache.dolphinscheduler.common.utils.StreamUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.ProcessData;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.service.permission.PermissionCheck;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.collections.map.HashedMap;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * process definition service impl
 */
@Service
public class ProcessDefinitionServiceImpl extends BaseServiceImpl implements ProcessDefinitionService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionServiceImpl.class);

    private static final String PROCESSDEFINITIONCODE = "processDefinitionCode";

    private static final String RELEASESTATE = "releaseState";

    private static final String TASKS = "tasks";

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProcessDefinitionLogMapper processDefinitionLogMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private TaskInstanceMapper taskInstanceMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Autowired
    private ProcessTaskRelationLogMapper processTaskRelationLogMapper;

    @Autowired
    TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private TenantMapper tenantMapper;

    /**
     * create process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param name process definition name
     * @param description description
     * @param globalParams global params
     * @param connects connects for nodes
     * @param locations locations for nodes
     * @param timeout timeout
     * @param tenantCode tenantCode
     * @param taskRelationJson relation json for nodes
     * @return create result code
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createProcessDefinition(User loginUser,
                                                       long projectCode,
                                                       String name,
                                                       String description,
                                                       String globalParams,
                                                       String connects,
                                                       String locations,
                                                       int timeout,
                                                       String tenantCode,
                                                       String taskRelationJson) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, project.getName());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        // check whether the new process define name exist
        ProcessDefinition definition = processDefinitionMapper.verifyByDefineName(project.getCode(), name);
        if (definition != null) {
            putMsg(result, Status.PROCESS_DEFINITION_NAME_EXIST, name);
            return result;
        }

        List<ProcessTaskRelationLog> taskRelationList = JSONUtils.toList(taskRelationJson, ProcessTaskRelationLog.class);
        Map<String, Object> checkRelationJson = checkTaskRelationList(taskRelationList, taskRelationJson);
        if (checkRelationJson.get(Constants.STATUS) != Status.SUCCESS) {
            return checkRelationJson;
        }

        Tenant tenant = tenantMapper.queryByTenantCode(tenantCode);
        if (tenant == null) {
            putMsg(result, Status.TENANT_NOT_EXIST);
            return result;
        }

        long processDefinitionCode;
        try {
            processDefinitionCode = SnowFlakeUtils.getInstance().nextId();
        } catch (SnowFlakeException e) {
            putMsg(result, Status.CREATE_PROCESS_DEFINITION);
            return result;
        }
        ProcessDefinition processDefinition = new ProcessDefinition(projectCode, name, processDefinitionCode, description,
                globalParams, locations, connects, timeout, loginUser.getId(), tenant.getId());

        return createProcessDefine(loginUser, result, taskRelationList, processDefinition);
    }

    private Map<String, Object> createProcessDefine(User loginUser,
                                                    Map<String, Object> result,
                                                    List<ProcessTaskRelationLog> taskRelationList,
                                                    ProcessDefinition processDefinition) {
        int insertVersion = processService.saveProcessDefine(loginUser, processDefinition, true);
        if (insertVersion > 0) {
            int insertResult = processService.saveTaskRelation(loginUser, processDefinition.getProjectCode(), processDefinition.getCode(), insertVersion, taskRelationList);
            if (insertResult > 0) {
                putMsg(result, Status.SUCCESS);
                // return processDefinitionCode
                result.put(Constants.DATA_LIST, processDefinition.getCode());
            } else {
                putMsg(result, Status.CREATE_PROCESS_DEFINITION);
            }
        } else {
            putMsg(result, Status.CREATE_PROCESS_DEFINITION);
        }
        return result;
    }

    private Map<String, Object> checkTaskRelationList(List<ProcessTaskRelationLog> taskRelationList, String taskRelationJson) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (taskRelationList == null || taskRelationList.isEmpty()) {
                logger.error("task relation list is null");
                putMsg(result, Status.DATA_IS_NOT_VALID, taskRelationJson);
                return result;
            }

            // TODO check has cycle
            // if (graphHasCycle(taskRelationList)) {
            //  logger.error("process DAG has cycle");
            //   putMsg(result, Status.PROCESS_NODE_HAS_CYCLE);
            //   return result;
            // }

            // check whether the task relation json is normal
            for (ProcessTaskRelationLog processTaskRelationLog : taskRelationList) {
                if (processTaskRelationLog.getPostTaskCode() == 0 || processTaskRelationLog.getPostTaskVersion() == 0) {
                    logger.error("the post_task_code or post_task_version can't be zero");
                    putMsg(result, Status.CHECK_PROCESS_TASK_RELATION_ERROR);
                    return result;
                }
            }
            putMsg(result, Status.SUCCESS);
        } catch (Exception e) {
            result.put(Constants.STATUS, Status.REQUEST_PARAMS_NOT_VALID_ERROR);
            result.put(Constants.MSG, e.getMessage());
        }
        return result;
    }

    /**
     * query process definition list
     *
     * @param loginUser login user
     * @param projectCode project code
     * @return definition list
     */
    @Override
    public Map<String, Object> queryProcessDefinitionList(User loginUser, long projectCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, project.getName());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        List<ProcessDefinition> resourceList = processDefinitionMapper.queryAllDefinitionList(projectCode);
        List<DagData> dagDataList = resourceList.stream().map(processService::genDagData).collect(Collectors.toList());
        result.put(Constants.DATA_LIST, dagDataList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query process definition list paging
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @param userId user id
     * @return process definition page
     */
    @Override
    public Map<String, Object> queryProcessDefinitionListPaging(User loginUser, long projectCode, String searchVal, Integer pageNo, Integer pageSize, Integer userId) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, project.getName());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        Page<ProcessDefinition> page = new Page<>(pageNo, pageSize);
        IPage<ProcessDefinition> processDefinitionIPage = processDefinitionMapper.queryDefineListPaging(
                page, searchVal, userId, project.getCode(), isAdmin(loginUser));

        List<ProcessDefinition> records = processDefinitionIPage.getRecords();

        for (ProcessDefinition pd : records) {
            ProcessDefinitionLog processDefinitionLog = processDefinitionLogMapper.queryMaxVersionDefinitionLog(pd.getCode());
            int operator = processDefinitionLog.getOperator();
            User user = userMapper.selectById(operator);
            pd.setModifyBy(user.getUserName());
            pd.setProjectId(project.getId());
        }

        processDefinitionIPage.setRecords(records);

        PageInfo<ProcessDefinition> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotalCount((int) processDefinitionIPage.getTotal());
        pageInfo.setLists(records);
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * query detail of process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param code process definition code
     * @return process definition detail
     */
    @Override
    public Map<String, Object> queryProcessDefinitionByCode(User loginUser, long projectCode, long code) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, project.getName());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);
        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, code);
        } else {
            DagData dagData = processService.genDagData(processDefinition);
            result.put(Constants.DATA_LIST, dagData);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    @Override
    public Map<String, Object> queryProcessDefinitionByName(User loginUser, long projectCode, String processDefinitionName) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, project.getName());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByDefineName(projectCode, processDefinitionName);

        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, processDefinitionName);
        } else {
            DagData dagData = processService.genDagData(processDefinition);
            result.put(Constants.DATA_LIST, dagData);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * update  process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param name process definition name
     * @param code process definition code
     * @param description description
     * @param globalParams global params
     * @param connects connects for nodes
     * @param locations locations for nodes
     * @param timeout timeout
     * @param tenantCode tenantCode
     * @param taskRelationJson relation json for nodes
     * @return update result code
     */
    @Override
    public Map<String, Object> updateProcessDefinition(User loginUser,
                                                       long projectCode,
                                                       String name,
                                                       long code,
                                                       String description,
                                                       String globalParams,
                                                       String connects,
                                                       String locations,
                                                       int timeout,
                                                       String tenantCode,
                                                       String taskRelationJson) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, project.getName());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        List<ProcessTaskRelationLog> taskRelationList = JSONUtils.toList(taskRelationJson, ProcessTaskRelationLog.class);
        Map<String, Object> checkRelationJson = checkTaskRelationList(taskRelationList, taskRelationJson);
        if (checkRelationJson.get(Constants.STATUS) != Status.SUCCESS) {
            return checkRelationJson;
        }

        Tenant tenant = tenantMapper.queryByTenantCode(tenantCode);
        if (tenant == null) {
            putMsg(result, Status.TENANT_NOT_EXIST);
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);
        // check process definition exists
        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, code);
            return result;
        }
        if (processDefinition.getReleaseState() == ReleaseState.ONLINE) {
            // online can not permit edit
            putMsg(result, Status.PROCESS_DEFINE_NOT_ALLOWED_EDIT, processDefinition.getName());
            return result;
        }
        if (!name.equals(processDefinition.getName())) {
            // check whether the new process define name exist
            ProcessDefinition definition = processDefinitionMapper.verifyByDefineName(project.getCode(), name);
            if (definition != null) {
                putMsg(result, Status.PROCESS_DEFINITION_NAME_EXIST, name);
                return result;
            }
        }

        processDefinition.set(projectCode, name, description, globalParams, locations, connects, timeout, tenant.getId());
        return updateProcessDefine(loginUser, result, taskRelationList, processDefinition);
    }

    private Map<String, Object> updateProcessDefine(User loginUser,
                                                    Map<String, Object> result,
                                                    List<ProcessTaskRelationLog> taskRelationList,
                                                    ProcessDefinition processDefinition) {
        processDefinition.setUpdateTime(new Date());
        int insertVersion = processService.saveProcessDefine(loginUser, processDefinition, true);
        if (insertVersion > 0) {
            int insertResult = processService.saveTaskRelation(loginUser, processDefinition.getProjectCode(),
                    processDefinition.getCode(), insertVersion, taskRelationList);
            if (insertResult > 0) {
                putMsg(result, Status.SUCCESS);
                result.put(Constants.DATA_LIST, processDefinition);
            } else {
                putMsg(result, Status.UPDATE_PROCESS_DEFINITION_ERROR);
            }
        } else {
            putMsg(result, Status.UPDATE_PROCESS_DEFINITION_ERROR);
        }
        return result;
    }

    /**
     * verify process definition name unique
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param name name
     * @return true if process definition name not exists, otherwise false
     */
    @Override
    public Map<String, Object> verifyProcessDefinitionName(User loginUser, long projectCode, String name) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, project.getName());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.verifyByDefineName(project.getCode(), name.trim());
        if (processDefinition == null) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.PROCESS_DEFINITION_NAME_EXIST, name.trim());
        }
        return result;
    }

    /**
     * delete process definition by id
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionId process definition id
     * @return delete result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, Object> deleteProcessDefinitionById(User loginUser, long projectCode, Integer processDefinitionId) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, project.getName());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionMapper.selectById(processDefinitionId);

        // TODO: replace id to code
        // ProcessDefinition processDefinition = processDefineMapper.selectByCode(processDefinitionCode);

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
            putMsg(result, Status.PROCESS_DEFINE_STATE_ONLINE, processDefinitionId);
            return result;
        }
        // check process instances is already running
        List<ProcessInstance> processInstances = processInstanceService.queryByProcessDefineCodeAndStatus(processDefinition.getCode(), Constants.NOT_TERMINATED_STATES);
        if (CollectionUtils.isNotEmpty(processInstances)) {
            putMsg(result, Status.DELETE_PROCESS_DEFINITION_BY_ID_FAIL, processInstances.size());
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

        int delete = processDefinitionMapper.deleteById(processDefinitionId);
        processTaskRelationMapper.deleteByCode(project.getCode(), processDefinition.getCode());
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
     * @param projectCode project code
     * @param code process definition code
     * @param releaseState release state
     * @return release result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, Object> releaseProcessDefinition(User loginUser, long projectCode, long code, ReleaseState releaseState) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, project.getName());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        // check state
        if (null == releaseState) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, RELEASESTATE);
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);

        switch (releaseState) {
            case ONLINE:
                // To check resources whether they are already cancel authorized or deleted
                String resourceIds = processDefinition.getResourceIds();
                if (StringUtils.isNotBlank(resourceIds)) {
                    Integer[] resourceIdArray = Arrays.stream(resourceIds.split(Constants.COMMA)).map(Integer::parseInt).toArray(Integer[]::new);
                    PermissionCheck<Integer> permissionCheck = new PermissionCheck<>(AuthorizationType.RESOURCE_FILE_ID, processService, resourceIdArray, loginUser.getId(), logger);
                    try {
                        permissionCheck.checkPermission();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        putMsg(result, Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION, RELEASESTATE);
                        return result;
                    }
                }

                processDefinition.setReleaseState(releaseState);
                processDefinitionMapper.updateById(processDefinition);
                break;
            case OFFLINE:
                processDefinition.setReleaseState(releaseState);
                processDefinitionMapper.updateById(processDefinition);
                List<Schedule> scheduleList = scheduleMapper.selectAllByProcessDefineArray(
                        new int[]{processDefinition.getId()}
                );

                for (Schedule schedule : scheduleList) {
                    logger.info("set schedule offline, project id: {}, schedule id: {}, process definition code: {}", project.getId(), schedule.getId(), code);
                    // set status
                    schedule.setReleaseState(ReleaseState.OFFLINE);
                    scheduleMapper.updateById(schedule);
                    schedulerService.deleteSchedule(project.getId(), schedule.getId());
                }
                break;
            default:
                putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, RELEASESTATE);
                return result;
        }

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * batch export process definition by ids
     */
    @Override
    public void batchExportProcessDefinitionByIds(User loginUser, long projectCode, String processDefinitionIds, HttpServletResponse response) {

        if (StringUtils.isEmpty(processDefinitionIds)) {
            return;
        }
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, project.getName());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
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
     */
    private List<ProcessMeta> getProcessDefinitionList(String processDefinitionIds) {
        String[] processDefinitionIdArray = processDefinitionIds.split(",");

        List<ProcessMeta> processDefinitionList = new ArrayList<>();
        for (String strProcessDefinitionId : processDefinitionIdArray) {
            //get workflow info
            int processDefinitionId = Integer.parseInt(strProcessDefinitionId);
            ProcessDefinition processDefinition = processDefinitionMapper.queryByDefineId(processDefinitionId);
            processDefinitionList.add(exportProcessMetaData(processDefinition));
        }
        return processDefinitionList;
    }

    /**
     * download the process definition file
     */
    private void downloadProcessDefinitionFile(HttpServletResponse response, List<ProcessMeta> processDefinitionList) {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        BufferedOutputStream buff = null;
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            buff = new BufferedOutputStream(out);
            buff.write(JSONUtils.toJsonString(processDefinitionList).getBytes(StandardCharsets.UTF_8));
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
     *
     * @param processDefinition process definition
     * @return export process metadata string
     */
    public ProcessMeta exportProcessMetaData(ProcessDefinition processDefinition) {
        ProcessData processData = processService.genProcessData(processDefinition);
        //correct task param which has data source or dependent param
        addExportTaskNodeSpecialParam(processData);

        //export process metadata
        ProcessMeta exportProcessMeta = new ProcessMeta();
        exportProcessMeta.setProjectName(processDefinition.getProjectName());
        exportProcessMeta.setProcessDefinitionName(processDefinition.getName());
        exportProcessMeta.setProcessDefinitionJson(JSONUtils.toJsonString(processService.genProcessData(processDefinition)));
        exportProcessMeta.setProcessDefinitionDescription(processDefinition.getDescription());
        exportProcessMeta.setProcessDefinitionLocations(processDefinition.getLocations());
        exportProcessMeta.setProcessDefinitionConnects(processDefinition.getConnects());

        //schedule info
        List<Schedule> schedules = scheduleMapper.queryByProcessDefinitionId(processDefinition.getId());
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
     *
     * @param processData process data
     * @return correct processDefinitionJson
     */
    private void addExportTaskNodeSpecialParam(ProcessData processData) {
        List<TaskNode> taskNodeList = processData.getTasks();
        List<TaskNode> tmpNodeList = new ArrayList<>();
        for (TaskNode taskNode : taskNodeList) {
            ProcessAddTaskParam addTaskParam = TaskNodeParamFactory.getByTaskType(taskNode.getType());
            JsonNode jsonNode = JSONUtils.toJsonNode(taskNode);
            if (null != addTaskParam) {
                addTaskParam.addExportSpecialParam(jsonNode);
            }
            tmpNodeList.add(JSONUtils.parseObject(jsonNode.toString(), TaskNode.class));
        }
        processData.setTasks(tmpNodeList);
    }

    /**
     * check task if has sub process
     *
     * @param taskType task type
     * @return if task has sub process return true else false
     */
    private boolean checkTaskHasSubProcess(String taskType) {
        return taskType.equals(TaskType.SUB_PROCESS.getDesc());
    }

    /**
     * import process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param file process metadata json file
     * @return import process
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, Object> importProcessDefinition(User loginUser, long projectCode, MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        String processMetaJson = FileUtils.file2String(file);
        List<ProcessMeta> processMetaList = JSONUtils.toList(processMetaJson, ProcessMeta.class);

        //check file content
        if (CollectionUtils.isEmpty(processMetaList)) {
            putMsg(result, Status.DATA_IS_NULL, "fileContent");
            return result;
        }

        for (ProcessMeta processMeta : processMetaList) {

            if (!checkAndImportProcessDefinition(loginUser, projectCode, result, processMeta)) {
                return result;
            }
        }

        return result;
    }

    /**
     * check and import process definition
     */
    private boolean checkAndImportProcessDefinition(User loginUser, long projectCode, Map<String, Object> result, ProcessMeta processMeta) {

        if (!checkImportanceParams(processMeta, result)) {
            return false;
        }

        //deal with process name
        String processDefinitionName = processMeta.getProcessDefinitionName();
        //use currentProjectName to query
        Project targetProject = projectMapper.queryByCode(projectCode);
        if (null != targetProject) {
            processDefinitionName = recursionProcessDefinitionName(targetProject.getCode(),
                    processDefinitionName, 1);
        }

        //unique check
        Map<String, Object> checkResult = verifyProcessDefinitionName(loginUser, projectCode, processDefinitionName);
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
                        targetProject.getName(),
                        result,
                        processMeta,
                        processDefinitionName,
                        addImportTaskNodeParam(loginUser, processMeta.getProcessDefinitionJson(), targetProject));

        if (createProcessResult == null) {
            return false;
        }

        //create process definition
        Integer processDefinitionId =
                Objects.isNull(createProcessResult.get(Constants.DATA_LIST))
                        ? null : Integer.parseInt(createProcessResult.get(Constants.DATA_LIST).toString());

        //scheduler param
        return getImportProcessScheduleResult(loginUser,
                targetProject.getName(),
                result,
                processMeta,
                processDefinitionName,
                processDefinitionId);
    }

    /**
     * get create process result
     */
    private Map<String, Object> getCreateProcessResult(User loginUser,
                                                       String currentProjectName,
                                                       Map<String, Object> result,
                                                       ProcessMeta processMeta,
                                                       String processDefinitionName,
                                                       String importProcessParam) {
        Map<String, Object> createProcessResult = null;
        try {
            // TODO import
            // createProcessResult = createProcessDefinition(loginUser
            //      , currentProjectName,
            //      processDefinitionName + "_import_" + DateUtils.getCurrentTimeStamp(),
            //      importProcessParam,
            //      processMeta.getProcessDefinitionDescription(),
            //      processMeta.getProcessDefinitionLocations(),
            //      processMeta.getProcessDefinitionConnects());
            putMsg(result, Status.SUCCESS);
        } catch (Exception e) {
            logger.error("import process meta json data: {}", e.getMessage(), e);
            putMsg(result, Status.IMPORT_PROCESS_DEFINE_ERROR);
        }

        return createProcessResult;
    }

    /**
     * get import process schedule result
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
     */
    private boolean checkImportanceParams(ProcessMeta processMeta, Map<String, Object> result) {
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
     *
     * @param loginUser login user
     * @param processDefinitionJson process definition json
     * @param targetProject target project
     * @return import process param
     */
    private String addImportTaskNodeParam(User loginUser, String processDefinitionJson, Project targetProject) {
        ObjectNode jsonObject = JSONUtils.parseObject(processDefinitionJson);
        ArrayNode jsonArray = (ArrayNode) jsonObject.get(TASKS);
        //add sql and dependent param
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonNode taskNode = jsonArray.path(i);
            String taskType = taskNode.path("type").asText();
            ProcessAddTaskParam addTaskParam = TaskNodeParamFactory.getByTaskType(taskType);
            if (null != addTaskParam) {
                addTaskParam.addImportSpecialParam(taskNode);
            }
        }

        //recursive sub-process parameter correction map key for old process code value for new process code
        Map<Long, Long> subProcessCodeMap = new HashMap<>();

        List<Object> subProcessList = StreamUtils.asStream(jsonArray.elements())
                .filter(elem -> checkTaskHasSubProcess(JSONUtils.parseObject(elem.toString()).path("type").asText()))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(subProcessList)) {
            importSubProcess(loginUser, targetProject, jsonArray, subProcessCodeMap);
        }

        jsonObject.set(TASKS, jsonArray);
        return jsonObject.toString();
    }

    /**
     * import process schedule
     *
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
     *
     * @param loginUser login user
     * @param targetProject target project
     * @param jsonArray process task array
     * @param subProcessCodeMap correct sub process id map
     */
    private void importSubProcess(User loginUser, Project targetProject, ArrayNode jsonArray, Map<Long, Long> subProcessCodeMap) {
        for (int i = 0; i < jsonArray.size(); i++) {
            ObjectNode taskNode = (ObjectNode) jsonArray.path(i);
            String taskType = taskNode.path("type").asText();

            if (!checkTaskHasSubProcess(taskType)) {
                continue;
            }
            //get sub process info
            ObjectNode subParams = (ObjectNode) taskNode.path("params");
            Long subProcessCode = subParams.path(PROCESSDEFINITIONCODE).asLong();
            ProcessDefinition subProcess = processDefinitionMapper.queryByCode(subProcessCode);
            //check is sub process exist in db
            if (null == subProcess) {
                continue;
            }

            String subProcessJson = JSONUtils.toJsonString(processService.genProcessData(subProcess));
            //check current project has sub process
            ProcessDefinition currentProjectSubProcess = processDefinitionMapper.queryByDefineName(targetProject.getCode(), subProcess.getName());

            if (null == currentProjectSubProcess) {
                ArrayNode subJsonArray = (ArrayNode) JSONUtils.parseObject(subProcessJson).get(TASKS);

                List<Object> subProcessList = StreamUtils.asStream(subJsonArray.elements())
                        .filter(item -> checkTaskHasSubProcess(JSONUtils.parseObject(item.toString()).path("type").asText()))
                        .collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(subProcessList)) {
                    importSubProcess(loginUser, targetProject, subJsonArray, subProcessCodeMap);
                    //sub process processId correct
                    if (!subProcessCodeMap.isEmpty()) {

                        for (Map.Entry<Long, Long> entry : subProcessCodeMap.entrySet()) {
                            String oldSubProcessCode = "\"processDefinitionCode\":" + entry.getKey();
                            String newSubProcessCode = "\"processDefinitionCode\":" + entry.getValue();
                            subProcessJson = subProcessJson.replaceAll(oldSubProcessCode, newSubProcessCode);
                        }

                        subProcessCodeMap.clear();
                    }
                }

                try {
                    // TODO import subProcess
                    // createProcessDefinition(loginUser
                    //          , targetProject.getName(),
                    //          subProcess.getName(),
                    //          subProcessJson,
                    //          subProcess.getDescription(),
                    //          subProcess.getLocations(),
                    //          subProcess.getConnects());
                    logger.info("create sub process, project: {}, process name: {}", targetProject.getName(), subProcess.getName());

                } catch (Exception e) {
                    logger.error("import process meta json data: {}", e.getMessage(), e);
                }

                //modify task node
                ProcessDefinition newSubProcessDefine = processDefinitionMapper.queryByDefineName(subProcess.getCode(), subProcess.getName());

                if (null != newSubProcessDefine) {
                    subProcessCodeMap.put(subProcessCode, newSubProcessDefine.getCode());
                    subParams.put(PROCESSDEFINITIONCODE, newSubProcessDefine.getId());
                    taskNode.set("params", subParams);
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
    @Override
    public Map<String, Object> checkProcessNodeList(ProcessData processData, String processDefinitionJson) {

        Map<String, Object> result = new HashMap<>();
        try {
            if (processData == null) {
                logger.error("process data is null");
                putMsg(result, Status.DATA_IS_NOT_VALID, processDefinitionJson);
                return result;
            }

            // Check whether the task node is normal
            List<TaskNode> taskNodes = processData.getTasks();

            if (CollectionUtils.isEmpty(taskNodes)) {
                logger.error("process node info is empty");
                putMsg(result, Status.PROCESS_DAG_IS_EMPTY);
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
                if (!CheckUtils.checkTaskNodeParameters(taskNode)) {
                    logger.error("task node {} parameter invalid", taskNode.getName());
                    putMsg(result, Status.PROCESS_NODE_S_PARAMETER_INVALID, taskNode.getName());
                    return result;
                }

                // check extra params
                CheckUtils.checkOtherParams(taskNode.getExtras());
            }
            putMsg(result, Status.SUCCESS);
        } catch (Exception e) {
            result.put(Constants.STATUS, Status.REQUEST_PARAMS_NOT_VALID_ERROR);
            result.put(Constants.MSG, e.getMessage());
        }
        return result;
    }

    /**
     * get task node details based on process definition
     *
     * @param loginUser loginUser
     * @param projectCode project code
     * @param defineCode define code
     * @return task node list
     */
    public Map<String, Object> getTaskNodeListByDefinitionCode(User loginUser, long projectCode, long defineCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, project.getName());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(defineCode);
        if (processDefinition == null) {
            logger.info("process define not exists");
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, defineCode);
            return result;
        }
        DagData dagData = processService.genDagData(processDefinition);
        result.put(Constants.DATA_LIST, dagData.getTaskDefinitionList());
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * get task node details map based on process definition
     *
     * @param loginUser loginUser
     * @param projectCode project code
     * @param defineCodes define codes
     * @return task node list
     */
    @Override
    public Map<String, Object> getNodeListMapByDefinitionCodes(User loginUser, long projectCode, String defineCodes) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, project.getName());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        Set<Long> defineCodeSet = Lists.newArrayList(defineCodes.split(Constants.COMMA)).stream().map(Long::parseLong).collect(Collectors.toSet());
        List<ProcessDefinition> processDefinitionList = processDefinitionMapper.queryByCodes(defineCodeSet);
        if (CollectionUtils.isEmpty(processDefinitionList)) {
            logger.info("process definition not exists");
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, defineCodes);
            return result;
        }
        Map<Long, List<TaskDefinitionLog>> taskNodeMap = new HashMap<>();
        for (ProcessDefinition processDefinition : processDefinitionList) {
            DagData dagData = processService.genDagData(processDefinition);
            taskNodeMap.put(processDefinition.getCode(), dagData.getTaskDefinitionList());
        }

        result.put(Constants.DATA_LIST, taskNodeMap);
        putMsg(result, Status.SUCCESS);

        return result;

    }

    /**
     * query process definition all by project code
     *
     * @param loginUser loginUser
     * @param projectCode project code
     * @return process definitions in the project
     */
    @Override
    public Map<String, Object> queryAllProcessDefinitionByProjectCode(User loginUser, long projectCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, project.getName());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        List<ProcessDefinition> processDefinitions = processDefinitionMapper.queryAllDefinitionList(projectCode);
        List<DagData> dagDataList = processDefinitions.stream().map(processService::genDagData).collect(Collectors.toList());
        result.put(Constants.DATA_LIST, dagDataList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * Encapsulates the TreeView structure
     *
     * @param code process definition code
     * @param limit limit
     * @return tree view json data
     */
    @Override
    public Map<String, Object> viewTree(long code, Integer limit) {
        Map<String, Object> result = new HashMap<>();
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);
        if (null == processDefinition) {
            logger.info("process define not exists");
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, code);
            return result;
        }
        DAG<String, TaskNode, TaskNodeRelation> dag = processService.genDagGraph(processDefinition);
        // nodes that is running
        Map<String, List<TreeViewDto>> runningNodeMap = new ConcurrentHashMap<>();

        //nodes that is waiting to run
        Map<String, List<TreeViewDto>> waitingRunningNodeMap = new ConcurrentHashMap<>();

        // List of process instances
        List<ProcessInstance> processInstanceList = processInstanceService.queryByProcessDefineCode(code, limit);
        processInstanceList.forEach(processInstance -> processInstance.setDuration(DateUtils.format2Duration(processInstance.getStartTime(), processInstance.getEndTime())));
        List<TaskDefinitionLog> taskDefinitionList = processService.queryTaskDefinitionListByProcess(code, processDefinition.getVersion());
        Map<Long, TaskDefinitionLog> taskDefinitionMap = taskDefinitionList.stream()
                .collect(Collectors.toMap(TaskDefinitionLog::getCode, taskDefinitionLog -> taskDefinitionLog));

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
            parentTreeViewDto.getInstances().add(new Instance(processInstance.getId(), processInstance.getName(), "",
                    processInstance.getState().toString(), processInstance.getStartTime(), endTime, processInstance.getHost(),
                    DateUtils.format2Readable(endTime.getTime() - processInstance.getStartTime().getTime())));
        }

        List<TreeViewDto> parentTreeViewDtoList = new ArrayList<>();
        parentTreeViewDtoList.add(parentTreeViewDto);
        // Here is the encapsulation task instance
        for (String startNode : dag.getBeginNode()) {
            runningNodeMap.put(startNode, parentTreeViewDtoList);
        }

        while (Stopper.isRunning()) {
            Set<String> postNodeList;
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
                        // if process is sub process, the return sub id, or sub id=0
                        if (taskInstance.isSubProcess()) {
                            TaskDefinition taskDefinition = taskDefinitionMap.get(taskInstance.getTaskCode());
                            subProcessId = Integer.parseInt(JSONUtils.parseObject(
                                    taskDefinition.getTaskParams()).path(CMD_PARAM_SUB_PROCESS_DEFINE_ID).asText());
                        }
                        treeViewDto.getInstances().add(new Instance(taskInstance.getId(), taskInstance.getName(), taskInstance.getTaskType(),
                                taskInstance.getState().toString(), taskInstance.getStartTime(), taskInstance.getEndTime(), taskInstance.getHost(),
                                DateUtils.format2Readable(endTime.getTime() - startTime.getTime()), subProcessId));
                    }
                }
                for (TreeViewDto pTreeViewDto : parentTreeViewDtoList) {
                    pTreeViewDto.getChildren().add(treeViewDto);
                }
                postNodeList = dag.getSubsequentNodes(nodeName);
                if (CollectionUtils.isNotEmpty(postNodeList)) {
                    for (String nextNodeName : postNodeList) {
                        List<TreeViewDto> treeViewDtoList = waitingRunningNodeMap.get(nextNodeName);
                        if (CollectionUtils.isEmpty(treeViewDtoList)) {
                            treeViewDtoList = new ArrayList<>();
                        }
                        treeViewDtoList.add(treeViewDto);
                        waitingRunningNodeMap.put(nextNodeName, treeViewDtoList);
                    }
                }
                runningNodeMap.remove(nodeName);
            }
            if (waitingRunningNodeMap.size() == 0) {
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
            List<String> preTasks = JSONUtils.toList(taskNodeResponse.getPreTasks(), String.class);
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

    private String recursionProcessDefinitionName(Long projectCode, String processDefinitionName, int num) {
        ProcessDefinition processDefinition = processDefinitionMapper.queryByDefineName(projectCode, processDefinitionName);
        if (processDefinition != null) {
            if (num > 1) {
                String str = processDefinitionName.substring(0, processDefinitionName.length() - 3);
                processDefinitionName = str + "(" + num + ")";
            } else {
                processDefinitionName = processDefinition.getName() + "(" + num + ")";
            }
        } else {
            return processDefinitionName;
        }
        return recursionProcessDefinitionName(projectCode, processDefinitionName, num + 1);
    }

    /**
     * batch copy process definition
     *
     * @param loginUser loginUser
     * @param projectCode projectCode
     * @param processDefinitionCodes processDefinitionCodes
     * @param targetProjectCode targetProjectCode
     */
    @Override
    public Map<String, Object> batchCopyProcessDefinition(User loginUser,
                                                          long projectCode,
                                                          String processDefinitionCodes,
                                                          long targetProjectCode) {
        Map<String, Object> result = checkParams(loginUser, projectCode, processDefinitionCodes, targetProjectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        List<String> failedProcessList = new ArrayList<>();
        doBatchOperateProcessDefinition(loginUser, targetProjectCode, failedProcessList, processDefinitionCodes, result, true);
        checkBatchOperateResult(projectCode, targetProjectCode, result, failedProcessList, true);
        return result;
    }

    /**
     * batch move process definition
     *
     * @param loginUser loginUser
     * @param projectCode projectCode
     * @param processDefinitionCodes processDefinitionCodes
     * @param targetProjectCode targetProjectCode
     */
    @Override
    public Map<String, Object> batchMoveProcessDefinition(User loginUser,
                                                          long projectCode,
                                                          String processDefinitionCodes,
                                                          long targetProjectCode) {
        Map<String, Object> result = checkParams(loginUser, projectCode, processDefinitionCodes, targetProjectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        if (projectCode == targetProjectCode) {
            return result;
        }
        List<String> failedProcessList = new ArrayList<>();
        doBatchOperateProcessDefinition(loginUser, targetProjectCode, failedProcessList, processDefinitionCodes, result, false);
        checkBatchOperateResult(projectCode, targetProjectCode, result, failedProcessList, false);
        return result;
    }

    private Map<String, Object> checkParams(User loginUser,
                                            long projectCode,
                                            String processDefinitionCodes,
                                            long targetProjectCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, project.getName());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        if (StringUtils.isEmpty(processDefinitionCodes)) {
            putMsg(result, Status.PROCESS_DEFINITION_CODES_IS_EMPTY, processDefinitionCodes);
            return result;
        }

        if (projectCode != targetProjectCode) {
            Project targetProject = projectMapper.queryByCode(targetProjectCode);
            //check user access for project
            Map<String, Object> targetResult = projectService.checkProjectAndAuth(loginUser, targetProject, targetProject.getName());
            if (targetResult.get(Constants.STATUS) != Status.SUCCESS) {
                return targetResult;
            }
        }
        return result;
    }

    private void doBatchOperateProcessDefinition(User loginUser,
                                                 long targetProjectCode,
                                                 List<String> failedProcessList,
                                                 String processDefinitionCodes,
                                                 Map<String, Object> result,
                                                 boolean isCopy) {
        Set<Long> definitionCodes = Arrays.stream(processDefinitionCodes.split(Constants.COMMA)).map(Long::parseLong).collect(Collectors.toSet());
        List<ProcessDefinition> processDefinitionList = processDefinitionMapper.queryByCodes(definitionCodes);
        Set<Long> queryCodes = processDefinitionList.stream().map(ProcessDefinition::getCode).collect(Collectors.toSet());
        // definitionCodes - queryCodes
        Set<Long> diffCode = definitionCodes.stream().filter(code -> !queryCodes.contains(code)).collect(Collectors.toSet());
        diffCode.forEach(code -> failedProcessList.add(code + "[null]"));
        for (ProcessDefinition processDefinition : processDefinitionList) {
            List<ProcessTaskRelation> processTaskRelations =
                    processTaskRelationMapper.queryByProcessCode(processDefinition.getProjectCode(), processDefinition.getCode());
            List<ProcessTaskRelationLog> taskRelationList = processTaskRelations.stream().map(ProcessTaskRelationLog::new).collect(Collectors.toList());
            processDefinition.setProjectCode(targetProjectCode);
            if (isCopy) {
                processDefinition.setName(processDefinition.getName() + "_copy_" + DateUtils.getCurrentTimeStamp());
                createProcessDefine(loginUser, result, taskRelationList, processDefinition);
            } else {
                updateProcessDefine(loginUser, result, taskRelationList, processDefinition);
            }
            if (result.get(Constants.STATUS) != Status.SUCCESS) {
                failedProcessList.add(processDefinition.getCode() + "[" + processDefinition.getName() + "]");
            }
        }
    }

    /**
     * switch the defined process definition version
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionId process definition id
     * @param version the version user want to switch
     * @return switch process definition version result code
     */
    @Override
    public Map<String, Object> switchProcessDefinitionVersion(User loginUser, long projectCode, int processDefinitionId, long version) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, project.getName());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionMapper.queryByDefineId(processDefinitionId);
        if (Objects.isNull(processDefinition)) {
            putMsg(result
                    , Status.SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_ERROR
                    , processDefinitionId);
            return result;
        }

        ProcessDefinitionLog processDefinitionLog = processDefinitionLogMapper
                .queryByDefinitionCodeAndVersion(processDefinition.getCode(), version);

        if (Objects.isNull(processDefinitionLog)) {
            putMsg(result
                    , Status.SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_VERSION_ERROR
                    , processDefinition.getCode()
                    , version);
            return result;
        }
        int switchVersion = processService.switchVersion(processDefinition, processDefinitionLog);
        if (switchVersion > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.SWITCH_PROCESS_DEFINITION_VERSION_ERROR);
        }
        return result;
    }

    /**
     * check batch operate result
     *
     * @param srcProjectCode srcProjectCode
     * @param targetProjectCode targetProjectCode
     * @param result result
     * @param failedProcessList failedProcessList
     * @param isCopy isCopy
     */
    private void checkBatchOperateResult(long srcProjectCode, long targetProjectCode,
                                         Map<String, Object> result, List<String> failedProcessList, boolean isCopy) {
        if (!failedProcessList.isEmpty()) {
            if (isCopy) {
                putMsg(result, Status.COPY_PROCESS_DEFINITION_ERROR, srcProjectCode, targetProjectCode, String.join(",", failedProcessList));
            } else {
                putMsg(result, Status.MOVE_PROCESS_DEFINITION_ERROR, srcProjectCode, targetProjectCode, String.join(",", failedProcessList));
            }
        } else {
            putMsg(result, Status.SUCCESS);
        }
    }

    /**
     * check has associated process definition
     *
     * @param processDefinitionId process definition id
     * @param version version
     * @return The query result has a specific process definition return true
     */
    @Override
    public boolean checkHasAssociatedProcessDefinition(int processDefinitionId, long version) {
        Integer hasAssociatedDefinitionId = processDefinitionMapper.queryHasAssociatedDefinitionByIdAndVersion(processDefinitionId, version);
        return Objects.nonNull(hasAssociatedDefinitionId);
    }

    /**
     * query the pagination versions info by one certain process definition code
     *
     * @param loginUser login user info to check auth
     * @param projectCode project code
     * @param pageNo page number
     * @param pageSize page size
     * @param processDefinitionCode process definition code
     * @return the pagination process definition versions info of the certain process definition
     */
    @Override
    public Map<String, Object> queryProcessDefinitionVersions(User loginUser, long projectCode, int pageNo, int pageSize, long processDefinitionCode) {

        Map<String, Object> result = new HashMap<>();

        // check the if pageNo or pageSize less than 1
        if (pageNo <= 0 || pageSize <= 0) {
            putMsg(result, Status.QUERY_PROCESS_DEFINITION_VERSIONS_PAGE_NO_OR_PAGE_SIZE_LESS_THAN_1_ERROR, pageNo, pageSize);
            return result;
        }

        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        result.putAll(projectService.checkProjectAndAuth(loginUser, project, project.getName()));
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(processDefinitionCode);

        PageInfo<ProcessDefinitionLog> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<ProcessDefinitionLog> page = new Page<>(pageNo, pageSize);
        IPage<ProcessDefinitionLog> processDefinitionVersionsPaging = processDefinitionLogMapper.queryProcessDefinitionVersionsPaging(page, processDefinition.getCode());
        List<ProcessDefinitionLog> processDefinitionLogs = processDefinitionVersionsPaging.getRecords();

        pageInfo.setLists(processDefinitionLogs);
        pageInfo.setTotalCount((int) processDefinitionVersionsPaging.getTotal());
        return ImmutableMap.of(
                Constants.MSG, Status.SUCCESS.getMsg()
                , Constants.STATUS, Status.SUCCESS
                , Constants.DATA_LIST, pageInfo);
    }


    /**
     * delete one certain process definition by version number and process definition id
     *
     * @param loginUser login user info to check auth
     * @param projectCode project code
     * @param processDefinitionId process definition id
     * @param version version number
     * @return delele result code
     */
    @Override
    public Map<String, Object> deleteByProcessDefinitionIdAndVersion(User loginUser, long projectCode, int processDefinitionId, long version) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, project.getName());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByDefineId(processDefinitionId);

        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, processDefinitionId);
        } else {
            processDefinitionLogMapper.deleteByProcessDefinitionCodeAndVersion(processDefinition.getCode(), version);
            putMsg(result, Status.SUCCESS);
        }
        return result;

    }
}
