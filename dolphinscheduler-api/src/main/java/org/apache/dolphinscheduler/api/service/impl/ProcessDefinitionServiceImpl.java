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

import org.apache.dolphinscheduler.api.dto.DagDataSchedule;
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
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.SnowFlakeUtils;
import org.apache.dolphinscheduler.common.utils.SnowFlakeUtils.SnowFlakeException;
import org.apache.dolphinscheduler.dao.entity.DagData;
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
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.service.permission.PermissionCheck;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import com.google.common.collect.Lists;

/**
 * process definition service impl
 */
@Service
public class ProcessDefinitionServiceImpl extends BaseServiceImpl implements ProcessDefinitionService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionServiceImpl.class);

    private static final String RELEASESTATE = "releaseState";

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
    TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

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
     * @param locations locations for nodes
     * @param timeout timeout
     * @param tenantCode tenantCode
     * @param taskRelationJson relation json for nodes
     * @param taskDefinitionJson taskDefinitionJson
     * @return create result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, Object> createProcessDefinition(User loginUser,
                                                       long projectCode,
                                                       String name,
                                                       String description,
                                                       String globalParams,
                                                       String locations,
                                                       int timeout,
                                                       String tenantCode,
                                                       String taskRelationJson,
                                                       String taskDefinitionJson) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        // check whether the new process define name exist
        ProcessDefinition definition = processDefinitionMapper.verifyByDefineName(project.getCode(), name);
        if (definition != null) {
            putMsg(result, Status.PROCESS_DEFINITION_NAME_EXIST, name);
            return result;
        }
        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);
        createTaskDefinition(result, loginUser, projectCode, taskDefinitionLogs, taskDefinitionJson);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        List<ProcessTaskRelationLog> taskRelationList = JSONUtils.toList(taskRelationJson, ProcessTaskRelationLog.class);
        Map<String, Object> checkRelationJson = checkTaskRelationList(taskRelationList, taskRelationJson, taskDefinitionLogs);
        if (checkRelationJson.get(Constants.STATUS) != Status.SUCCESS) {
            return checkRelationJson;
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

        long processDefinitionCode;
        try {
            processDefinitionCode = SnowFlakeUtils.getInstance().nextId();
        } catch (SnowFlakeException e) {
            putMsg(result, Status.CREATE_PROCESS_DEFINITION_ERROR);
            return result;
        }
        ProcessDefinition processDefinition = new ProcessDefinition(projectCode, name, processDefinitionCode, description,
            globalParams, locations, timeout, loginUser.getId(), tenantId);

        return createProcessDefine(loginUser, result, taskRelationList, processDefinition, taskDefinitionLogs);
    }

    private void createTaskDefinition(Map<String, Object> result,
                                      User loginUser,
                                      long projectCode,
                                      List<TaskDefinitionLog> taskDefinitionLogs,
                                      String taskDefinitionJson) {
        if (taskDefinitionLogs.isEmpty()) {
            logger.error("taskDefinitionJson invalid: {}", taskDefinitionJson);
            putMsg(result, Status.DATA_IS_NOT_VALID, taskDefinitionJson);
            return;
        }
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
            if (!CheckUtils.checkTaskDefinitionParameters(taskDefinitionLog)) {
                logger.error("task definition {} parameter invalid", taskDefinitionLog.getName());
                putMsg(result, Status.PROCESS_NODE_S_PARAMETER_INVALID, taskDefinitionLog.getName());
                return;
            }
        }
        if (processService.saveTaskDefine(loginUser, projectCode, taskDefinitionLogs)) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.CREATE_TASK_DEFINITION_ERROR);
        }
    }

    private Map<String, Object> createProcessDefine(User loginUser,
                                                    Map<String, Object> result,
                                                    List<ProcessTaskRelationLog> taskRelationList,
                                                    ProcessDefinition processDefinition,
                                                    List<TaskDefinitionLog> taskDefinitionLogs) {
        int insertVersion = processService.saveProcessDefine(loginUser, processDefinition, true);
        if (insertVersion > 0) {
            int insertResult = processService.saveTaskRelation(loginUser, processDefinition.getProjectCode(), processDefinition.getCode(), insertVersion, taskRelationList, taskDefinitionLogs);
            if (insertResult == Constants.EXIT_CODE_SUCCESS) {
                putMsg(result, Status.SUCCESS);
                result.put(Constants.DATA_LIST, processDefinition);
            } else {
                putMsg(result, Status.CREATE_PROCESS_DEFINITION_ERROR);
            }
        } else {
            putMsg(result, Status.CREATE_PROCESS_DEFINITION_ERROR);
        }
        return result;
    }

    private Map<String, Object> checkTaskRelationList(List<ProcessTaskRelationLog> taskRelationList, String taskRelationJson, List<TaskDefinitionLog> taskDefinitionLogs) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (taskRelationList == null || taskRelationList.isEmpty()) {
                logger.error("task relation list is null");
                putMsg(result, Status.DATA_IS_NOT_VALID, taskRelationJson);
                return result;
            }

            List<TaskNode> taskNodeList = processService.transformTask(taskRelationList, taskDefinitionLogs);
            if (taskNodeList.size() != taskRelationList.size()) {
                Set<Long> postTaskCodes = taskRelationList.stream().map(ProcessTaskRelationLog::getPostTaskCode).collect(Collectors.toSet());
                Set<Long> taskNodeCodes = taskNodeList.stream().map(TaskNode::getCode).collect(Collectors.toSet());
                Collection<Long> codes = CollectionUtils.subtract(postTaskCodes, taskNodeCodes);
                if (CollectionUtils.isNotEmpty(codes)) {
                    logger.error("the task code is not exit");
                    putMsg(result, Status.TASK_DEFINE_NOT_EXIST, StringUtils.join(codes, Constants.COMMA));
                    return result;
                }
            }
            if (graphHasCycle(taskNodeList)) {
                logger.error("process DAG has cycle");
                putMsg(result, Status.PROCESS_NODE_HAS_CYCLE);
                return result;
            }

            // check whether the task relation json is normal
            for (ProcessTaskRelationLog processTaskRelationLog : taskRelationList) {
                if (processTaskRelationLog.getPostTaskCode() == 0) {
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
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
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
     * @param userId user id
     * @param pageNo page number
     * @param pageSize page size
     * @return process definition page
     */
    @Override
    public Result queryProcessDefinitionListPaging(User loginUser, long projectCode, String searchVal, Integer userId, Integer pageNo, Integer pageSize) {
        Result result = new Result();
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            putMsg(result, resultStatus);
            return result;
        }

        Page<ProcessDefinition> page = new Page<>(pageNo, pageSize);
        IPage<ProcessDefinition> processDefinitionIPage = processDefinitionMapper.queryDefineListPaging(
            page, searchVal, userId, project.getCode(), isAdmin(loginUser));

        List<ProcessDefinition> records = processDefinitionIPage.getRecords();
        for (ProcessDefinition pd : records) {
            ProcessDefinitionLog processDefinitionLog = processDefinitionLogMapper.queryByDefinitionCodeAndVersion(pd.getCode(), pd.getVersion());
            User user = userMapper.selectById(processDefinitionLog.getOperator());
            pd.setModifyBy(user.getUserName());
        }
        processDefinitionIPage.setRecords(records);
        PageInfo<ProcessDefinition> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) processDefinitionIPage.getTotal());
        pageInfo.setTotalList(processDefinitionIPage.getRecords());
        result.setData(pageInfo);
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
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);
        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, code);
        } else {
            Tenant tenant = tenantMapper.queryById(processDefinition.getTenantId());
            if (tenant != null) {
                processDefinition.setTenantCode(tenant.getTenantCode());
            }
            DagData dagData = processService.genDagData(processDefinition);
            result.put(Constants.DATA_LIST, dagData);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    @Override
    public Map<String, Object> queryProcessDefinitionByName(User loginUser, long projectCode, String name) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByDefineName(projectCode, name);

        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, name);
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
     * @param locations locations for nodes
     * @param timeout timeout
     * @param tenantCode tenantCode
     * @param taskRelationJson relation json for nodes
     * @param taskDefinitionJson taskDefinitionJson
     * @return update result code
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> updateProcessDefinition(User loginUser,
                                                       long projectCode,
                                                       String name,
                                                       long code,
                                                       String description,
                                                       String globalParams,
                                                       String locations,
                                                       int timeout,
                                                       String tenantCode,
                                                       String taskRelationJson,
                                                       String taskDefinitionJson) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);
        createTaskDefinition(result, loginUser, projectCode, taskDefinitionLogs, taskDefinitionJson);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        List<ProcessTaskRelationLog> taskRelationList = JSONUtils.toList(taskRelationJson, ProcessTaskRelationLog.class);
        Map<String, Object> checkRelationJson = checkTaskRelationList(taskRelationList, taskRelationJson, taskDefinitionLogs);
        if (checkRelationJson.get(Constants.STATUS) != Status.SUCCESS) {
            return checkRelationJson;
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
        ProcessDefinition processDefinitionDeepCopy = JSONUtils.parseObject(JSONUtils.toJsonString(processDefinition), ProcessDefinition.class);
        processDefinition.set(projectCode, name, description, globalParams, locations, timeout, tenantId);
        return updateProcessDefine(loginUser, result, taskRelationList, processDefinition, processDefinitionDeepCopy, taskDefinitionLogs);
    }

    private Map<String, Object> updateProcessDefine(User loginUser,
                                                    Map<String, Object> result,
                                                    List<ProcessTaskRelationLog> taskRelationList,
                                                    ProcessDefinition processDefinition,
                                                    ProcessDefinition processDefinitionDeepCopy,
                                                    List<TaskDefinitionLog> taskDefinitionLogs) {
        int insertVersion;
        if (processDefinition.equals(processDefinitionDeepCopy)) {
            insertVersion = processDefinitionDeepCopy.getVersion();
        } else {
            processDefinition.setUpdateTime(new Date());
            insertVersion = processService.saveProcessDefine(loginUser, processDefinition, true);
        }
        if (insertVersion > 0) {
            int insertResult = processService.saveTaskRelation(loginUser, processDefinition.getProjectCode(),
                processDefinition.getCode(), insertVersion, taskRelationList, taskDefinitionLogs);
            if (insertResult == Constants.EXIT_CODE_SUCCESS) {
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
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
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
     * delete process definition by code
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param code process definition code
     * @return delete result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, Object> deleteProcessDefinitionByCode(User loginUser, long projectCode, long code) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);
        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, code);
            return result;
        }

        // Determine if the login user is the owner of the process definition
        if (loginUser.getId() != processDefinition.getUserId() && loginUser.getUserType() != UserType.ADMIN_USER) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        // check process definition is already online
        if (processDefinition.getReleaseState() == ReleaseState.ONLINE) {
            putMsg(result, Status.PROCESS_DEFINE_STATE_ONLINE, code);
            return result;
        }
        // check process instances is already running
        List<ProcessInstance> processInstances = processInstanceService.queryByProcessDefineCodeAndStatus(processDefinition.getCode(), Constants.NOT_TERMINATED_STATES);
        if (CollectionUtils.isNotEmpty(processInstances)) {
            putMsg(result, Status.DELETE_PROCESS_DEFINITION_BY_CODE_FAIL, processInstances.size());
            return result;
        }

        // get the timing according to the process definition
        List<Schedule> schedules = scheduleMapper.queryByProcessDefinitionCode(code);
        if (!schedules.isEmpty() && schedules.size() > 1) {
            logger.warn("scheduler num is {},Greater than 1", schedules.size());
            putMsg(result, Status.DELETE_PROCESS_DEFINE_BY_CODE_ERROR);
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

        int delete = processDefinitionMapper.deleteById(processDefinition.getId());
        processTaskRelationMapper.deleteByCode(project.getCode(), processDefinition.getCode());
        if (delete > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.DELETE_PROCESS_DEFINE_BY_CODE_ERROR);
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
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
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
                    new long[]{processDefinition.getCode()}
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
     * batch export process definition by codes
     */
    @Override
    public void batchExportProcessDefinitionByCodes(User loginUser, long projectCode, String codes, HttpServletResponse response) {
        if (StringUtils.isEmpty(codes)) {
            return;
        }
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return;
        }
        Set<Long> defineCodeSet = Lists.newArrayList(codes.split(Constants.COMMA)).stream().map(Long::parseLong).collect(Collectors.toSet());
        List<ProcessDefinition> processDefinitionList = processDefinitionMapper.queryByCodes(defineCodeSet);
        List<DagDataSchedule> dagDataSchedules = processDefinitionList.stream().map(this::exportProcessDagData).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(dagDataSchedules)) {
            downloadProcessDefinitionFile(response, dagDataSchedules);
        }
    }

    /**
     * download the process definition file
     */
    private void downloadProcessDefinitionFile(HttpServletResponse response, List<DagDataSchedule> dagDataSchedules) {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        BufferedOutputStream buff = null;
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            buff = new BufferedOutputStream(out);
            buff.write(JSONUtils.toJsonString(dagDataSchedules).getBytes(StandardCharsets.UTF_8));
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
     * get export process dag data
     *
     * @param processDefinition process definition
     * @return DagDataSchedule
     */
    public DagDataSchedule exportProcessDagData(ProcessDefinition processDefinition) {
        List<Schedule> schedules = scheduleMapper.queryByProcessDefinitionCode(processDefinition.getCode());
        DagDataSchedule dagDataSchedule = new DagDataSchedule(processService.genDagData(processDefinition));
        if (!schedules.isEmpty()) {
            Schedule schedule = schedules.get(0);
            schedule.setReleaseState(ReleaseState.OFFLINE);
            dagDataSchedule.setSchedule(schedule);
        }
        return dagDataSchedule;
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
        String dagDataScheduleJson = FileUtils.file2String(file);
        List<DagDataSchedule> dagDataScheduleList = JSONUtils.toList(dagDataScheduleJson, DagDataSchedule.class);
        //check file content
        if (CollectionUtils.isEmpty(dagDataScheduleList)) {
            putMsg(result, Status.DATA_IS_NULL, "fileContent");
            return result;
        }
        for (DagDataSchedule dagDataSchedule : dagDataScheduleList) {
            if (!checkAndImport(loginUser, projectCode, result, dagDataSchedule)) {
                return result;
            }
        }
        return result;
    }

    /**
     * check and import
     */
    private boolean checkAndImport(User loginUser, long projectCode, Map<String, Object> result, DagDataSchedule dagDataSchedule) {
        if (!checkImportanceParams(dagDataSchedule, result)) {
            return false;
        }
        ProcessDefinition processDefinition = dagDataSchedule.getProcessDefinition();
        //unique check
        Map<String, Object> checkResult = verifyProcessDefinitionName(loginUser, projectCode, processDefinition.getName());
        if (Status.SUCCESS.equals(checkResult.get(Constants.STATUS))) {
            putMsg(result, Status.SUCCESS);
        } else {
            result.putAll(checkResult);
            return false;
        }
        String processDefinitionName = recursionProcessDefinitionName(projectCode, processDefinition.getName(), 1);
        processDefinition.setName(processDefinitionName + "_import_" + DateUtils.getCurrentTimeStamp());
        processDefinition.setUserId(loginUser.getId());
        try {
            processDefinition.setCode(SnowFlakeUtils.getInstance().nextId());
        } catch (SnowFlakeException e) {
            putMsg(result, Status.CREATE_PROCESS_DEFINITION_ERROR);
            return false;
        }
        List<TaskDefinitionLog> taskDefinitionList = dagDataSchedule.getTaskDefinitionList();
        Map<Long, Long> taskCodeMap = new HashMap<>();
        Date now = new Date();
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionList) {
            taskDefinitionLog.setName(taskDefinitionLog.getName() + "_import_" + DateUtils.getCurrentTimeStamp());
            taskDefinitionLog.setProjectCode(projectCode);
            taskDefinitionLog.setUserId(loginUser.getId());
            taskDefinitionLog.setVersion(Constants.VERSION_FIRST);
            taskDefinitionLog.setCreateTime(now);
            taskDefinitionLog.setUpdateTime(now);
            taskDefinitionLog.setOperator(loginUser.getId());
            taskDefinitionLog.setOperateTime(now);
            try {
                long code = SnowFlakeUtils.getInstance().nextId();
                taskCodeMap.put(taskDefinitionLog.getCode(), code);
                taskDefinitionLog.setCode(code);
            } catch (SnowFlakeException e) {
                logger.error("Task code get error, ", e);
                putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS, "Error generating task definition code");
                return false;
            }
        }
        int insert = taskDefinitionMapper.batchInsert(taskDefinitionList);
        int logInsert = taskDefinitionLogMapper.batchInsert(taskDefinitionList);
        if ((logInsert & insert) == 0) {
            putMsg(result, Status.CREATE_TASK_DEFINITION_ERROR);
            return false;
        }

        List<ProcessTaskRelationLog> taskRelationList = dagDataSchedule.getProcessTaskRelationList();
        taskRelationList.forEach(processTaskRelationLog -> {
            processTaskRelationLog.setPreTaskCode(taskCodeMap.get(processTaskRelationLog.getPreTaskCode()));
            processTaskRelationLog.setPostTaskCode(taskCodeMap.get(processTaskRelationLog.getPostTaskCode()));
            processTaskRelationLog.setPreTaskVersion(Constants.VERSION_FIRST);
            processTaskRelationLog.setPostTaskVersion(Constants.VERSION_FIRST);
        });
        Map<String, Object> createProcessResult = createProcessDefine(loginUser, result, taskRelationList, processDefinition, null);
        if (Status.SUCCESS.equals(createProcessResult.get(Constants.STATUS))) {
            putMsg(createProcessResult, Status.SUCCESS);
        } else {
            result.putAll(createProcessResult);
            return false;
        }

        Schedule schedule = dagDataSchedule.getSchedule();
        if (null != schedule) {
            ProcessDefinition newProcessDefinition = processDefinitionMapper.queryByCode(processDefinition.getCode());
            schedule.setProcessDefinitionCode(newProcessDefinition.getCode());
            schedule.setUserId(loginUser.getId());
            schedule.setCreateTime(now);
            schedule.setUpdateTime(now);
            int scheduleInsert = scheduleMapper.insert(schedule);
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
    private boolean checkImportanceParams(DagDataSchedule dagDataSchedule, Map<String, Object> result) {
        if (dagDataSchedule.getProcessDefinition() == null) {
            putMsg(result, Status.DATA_IS_NULL, "ProcessDefinition");
            return false;
        }
        if (CollectionUtils.isEmpty(dagDataSchedule.getTaskDefinitionList())) {
            putMsg(result, Status.DATA_IS_NULL, "TaskDefinitionList");
            return false;
        }
        if (CollectionUtils.isEmpty(dagDataSchedule.getProcessTaskRelationList())) {
            putMsg(result, Status.DATA_IS_NULL, "ProcessTaskRelationList");
            return false;
        }
        return true;
    }

    private String recursionProcessDefinitionName(long projectCode, String processDefinitionName, int num) {
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
     * check the process task relation json
     *
     * @param processTaskRelationJson process task relation json
     * @return check result code
     */
    @Override
    public Map<String, Object> checkProcessNodeList(String processTaskRelationJson) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (processTaskRelationJson == null) {
                logger.error("process data is null");
                putMsg(result, Status.DATA_IS_NOT_VALID, processTaskRelationJson);
                return result;
            }

            List<ProcessTaskRelationLog> taskRelationList = JSONUtils.toList(processTaskRelationJson, ProcessTaskRelationLog.class);
            // Check whether the task node is normal
            List<TaskNode> taskNodes = processService.transformTask(taskRelationList, Lists.newArrayList());

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
     * @param code process definition code
     * @return task node list
     */
    @Override
    public Map<String, Object> getTaskNodeListByDefinitionCode(User loginUser, long projectCode, long code) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);
        if (processDefinition == null) {
            logger.info("process define not exists");
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, code);
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
     * @param codes define codes
     * @return task node list
     */
    @Override
    public Map<String, Object> getNodeListMapByDefinitionCodes(User loginUser, long projectCode, String codes) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        Set<Long> defineCodeSet = Lists.newArrayList(codes.split(Constants.COMMA)).stream().map(Long::parseLong).collect(Collectors.toSet());
        List<ProcessDefinition> processDefinitionList = processDefinitionMapper.queryByCodes(defineCodeSet);
        if (CollectionUtils.isEmpty(processDefinitionList)) {
            logger.info("process definition not exists");
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, codes);
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
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
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

    /**
     * batch copy process definition
     *
     * @param loginUser loginUser
     * @param projectCode projectCode
     * @param codes processDefinitionCodes
     * @param targetProjectCode targetProjectCode
     */
    @Override
    public Map<String, Object> batchCopyProcessDefinition(User loginUser,
                                                          long projectCode,
                                                          String codes,
                                                          long targetProjectCode) {
        Map<String, Object> result = checkParams(loginUser, projectCode, codes, targetProjectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        List<String> failedProcessList = new ArrayList<>();
        doBatchOperateProcessDefinition(loginUser, targetProjectCode, failedProcessList, codes, result, true);
        checkBatchOperateResult(projectCode, targetProjectCode, result, failedProcessList, true);
        return result;
    }

    /**
     * batch move process definition
     *
     * @param loginUser loginUser
     * @param projectCode projectCode
     * @param codes processDefinitionCodes
     * @param targetProjectCode targetProjectCode
     */
    @Override
    public Map<String, Object> batchMoveProcessDefinition(User loginUser,
                                                          long projectCode,
                                                          String codes,
                                                          long targetProjectCode) {
        Map<String, Object> result = checkParams(loginUser, projectCode, codes, targetProjectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        if (projectCode == targetProjectCode) {
            return result;
        }
        List<String> failedProcessList = new ArrayList<>();
        doBatchOperateProcessDefinition(loginUser, targetProjectCode, failedProcessList, codes, result, false);
        checkBatchOperateResult(projectCode, targetProjectCode, result, failedProcessList, false);
        return result;
    }

    private Map<String, Object> checkParams(User loginUser,
                                            long projectCode,
                                            String processDefinitionCodes,
                                            long targetProjectCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
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
            Map<String, Object> targetResult = projectService.checkProjectAndAuth(loginUser, targetProject, targetProjectCode);
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
                createProcessDefine(loginUser, result, taskRelationList, processDefinition, Lists.newArrayList());
            } else {
                updateProcessDefine(loginUser, result, taskRelationList, processDefinition, null, Lists.newArrayList());
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
     * @param code process definition code
     * @param version the version user want to switch
     * @return switch process definition version result code
     */
    @Override
    public Map<String, Object> switchProcessDefinitionVersion(User loginUser, long projectCode, long code, int version) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);
        if (Objects.isNull(processDefinition)) {
            putMsg(result, Status.SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_ERROR, code);
            return result;
        }

        ProcessDefinitionLog processDefinitionLog = processDefinitionLogMapper
            .queryByDefinitionCodeAndVersion(code, version);

        if (Objects.isNull(processDefinitionLog)) {
            putMsg(result, Status.SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_VERSION_ERROR, processDefinition.getCode(), version);
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
     * query the pagination versions info by one certain process definition code
     *
     * @param loginUser login user info to check auth
     * @param projectCode project code
     * @param pageNo page number
     * @param pageSize page size
     * @param code process definition code
     * @return the pagination process definition versions info of the certain process definition
     */
    @Override
    public Result queryProcessDefinitionVersions(User loginUser, long projectCode, int pageNo, int pageSize, long code) {
        Result result = new Result();
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            putMsg(result, resultStatus);
            return result;
        }
        PageInfo<ProcessDefinitionLog> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<ProcessDefinitionLog> page = new Page<>(pageNo, pageSize);
        IPage<ProcessDefinitionLog> processDefinitionVersionsPaging = processDefinitionLogMapper.queryProcessDefinitionVersionsPaging(page, code);
        List<ProcessDefinitionLog> processDefinitionLogs = processDefinitionVersionsPaging.getRecords();

        pageInfo.setTotalList(processDefinitionLogs);
        pageInfo.setTotal((int) processDefinitionVersionsPaging.getTotal());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }


    /**
     * delete one certain process definition by version number and process definition code
     *
     * @param loginUser login user info to check auth
     * @param projectCode project code
     * @param code process definition code
     * @param version version number
     * @return delele result code
     */
    @Override
    public Map<String, Object> deleteProcessDefinitionVersion(User loginUser, long projectCode, long code, int version) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);

        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, code);
        } else {
            processDefinitionLogMapper.deleteByProcessDefinitionCodeAndVersion(code, version);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }
}
