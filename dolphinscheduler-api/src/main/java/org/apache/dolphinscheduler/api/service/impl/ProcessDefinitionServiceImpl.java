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

import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_SUB_PROCESS_DEFINE_CODE;
import static org.apache.dolphinscheduler.common.Constants.DEFAULT_WORKER_GROUP;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.COMPLEX_TASK_TYPES;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SQL;

import org.apache.dolphinscheduler.api.dto.DagDataSchedule;
import org.apache.dolphinscheduler.api.dto.ScheduleParam;
import org.apache.dolphinscheduler.api.dto.treeview.Instance;
import org.apache.dolphinscheduler.api.dto.treeview.TreeViewDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.utils.CheckUtils;
import org.apache.dolphinscheduler.api.utils.FileUtils;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ConditionType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.DependentSimplifyDefinition;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils.CodeGenerateException;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.DataSource;
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
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.plugin.task.api.enums.SqlType;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ParametersNode;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SqlParameters;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.task.TaskPluginManager;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    private ProcessTaskRelationLogMapper processTaskRelationLogMapper;

    @Autowired
    TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private TaskPluginManager taskPluginManager;

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
                                                       String taskDefinitionJson,
                                                       ProcessExecutionTypeEnum executionType) {
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
        Map<String, Object> checkTaskDefinitions = checkTaskDefinitionList(taskDefinitionLogs, taskDefinitionJson);
        if (checkTaskDefinitions.get(Constants.STATUS) != Status.SUCCESS) {
            return checkTaskDefinitions;
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
            processDefinitionCode = CodeGenerateUtils.getInstance().genCode();
        } catch (CodeGenerateException e) {
            putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS);
            return result;
        }
        ProcessDefinition processDefinition = new ProcessDefinition(projectCode, name, processDefinitionCode, description,
            globalParams, locations, timeout, loginUser.getId(), tenantId);
        processDefinition.setExecutionType(executionType);

        return createDagDefine(loginUser, taskRelationList, processDefinition, taskDefinitionLogs);
    }

    private Map<String, Object> createDagDefine(User loginUser,
                                                List<ProcessTaskRelationLog> taskRelationList,
                                                ProcessDefinition processDefinition,
                                                List<TaskDefinitionLog> taskDefinitionLogs) {
        Map<String, Object> result = new HashMap<>();
        int saveTaskResult = processService.saveTaskDefine(loginUser, processDefinition.getProjectCode(), taskDefinitionLogs, Boolean.TRUE);
        if (saveTaskResult == Constants.EXIT_CODE_SUCCESS) {
            logger.info("The task has not changed, so skip");
        }
        if (saveTaskResult == Constants.DEFINITION_FAILURE) {
            putMsg(result, Status.CREATE_TASK_DEFINITION_ERROR);
            throw new ServiceException(Status.CREATE_TASK_DEFINITION_ERROR);
        }
        int insertVersion = processService.saveProcessDefine(loginUser, processDefinition, Boolean.TRUE, Boolean.TRUE);
        if (insertVersion == 0) {
            putMsg(result, Status.CREATE_PROCESS_DEFINITION_ERROR);
            throw new ServiceException(Status.CREATE_PROCESS_DEFINITION_ERROR);
        }
        int insertResult = processService.saveTaskRelation(loginUser, processDefinition.getProjectCode(), processDefinition.getCode(),
            insertVersion, taskRelationList, taskDefinitionLogs, Boolean.TRUE);
        if (insertResult == Constants.EXIT_CODE_SUCCESS) {
            putMsg(result, Status.SUCCESS);
            result.put(Constants.DATA_LIST, processDefinition);
        } else {
            putMsg(result, Status.CREATE_PROCESS_TASK_RELATION_ERROR);
            throw new ServiceException(Status.CREATE_PROCESS_TASK_RELATION_ERROR);
        }
        return result;
    }

    private Map<String, Object> checkTaskDefinitionList(List<TaskDefinitionLog> taskDefinitionLogs, String taskDefinitionJson) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (taskDefinitionLogs.isEmpty()) {
                logger.error("taskDefinitionJson invalid: {}", taskDefinitionJson);
                putMsg(result, Status.DATA_IS_NOT_VALID, taskDefinitionJson);
                return result;
            }
            for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {

                if (!taskPluginManager.checkTaskParameters(ParametersNode.builder()
                        .taskType(taskDefinitionLog.getTaskType())
                        .taskParams(taskDefinitionLog.getTaskParams())
                        .dependence(taskDefinitionLog.getDependence())
                        .build())) {
                    logger.error("task definition {} parameter invalid", taskDefinitionLog.getName());
                    putMsg(result, Status.PROCESS_NODE_S_PARAMETER_INVALID, taskDefinitionLog.getName());
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

    private Map<String, Object> checkTaskRelationList(List<ProcessTaskRelationLog> taskRelationList, String taskRelationJson, List<TaskDefinitionLog> taskDefinitionLogs) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (taskRelationList == null || taskRelationList.isEmpty()) {
                logger.error("task relation list is null");
                putMsg(result, Status.DATA_IS_NOT_VALID, taskRelationJson);
                return result;
            }
            List<ProcessTaskRelation> processTaskRelations = taskRelationList.stream()
                .map(processTaskRelationLog -> JSONUtils.parseObject(JSONUtils.toJsonString(processTaskRelationLog), ProcessTaskRelation.class))
                .collect(Collectors.toList());
            List<TaskNode> taskNodeList = processService.transformTask(processTaskRelations, taskDefinitionLogs);
            if (taskNodeList.size() != taskRelationList.size()) {
                Set<Long> postTaskCodes = taskRelationList.stream().map(ProcessTaskRelationLog::getPostTaskCode).collect(Collectors.toSet());
                Set<Long> taskNodeCodes = taskNodeList.stream().map(TaskNode::getCode).collect(Collectors.toSet());
                Collection<Long> codes = CollectionUtils.subtract(postTaskCodes, taskNodeCodes);
                if (CollectionUtils.isNotEmpty(codes)) {
                    logger.error("the task code is not exist");
                    putMsg(result, Status.TASK_DEFINE_NOT_EXIST, org.apache.commons.lang.StringUtils.join(codes, Constants.COMMA));
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
     * query process definition simple list
     *
     * @param loginUser login user
     * @param projectCode project code
     * @return definition simple list
     */
    @Override
    public Map<String, Object> queryProcessDefinitionSimpleList(User loginUser, long projectCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        List<ProcessDefinition> processDefinitions = processDefinitionMapper.queryAllDefinitionList(projectCode);
        ArrayNode arrayNode = JSONUtils.createArrayNode();
        for (ProcessDefinition processDefinition : processDefinitions) {
            ObjectNode processDefinitionNode = JSONUtils.createObjectNode();
            processDefinitionNode.put("id", processDefinition.getId());
            processDefinitionNode.put("code", processDefinition.getCode());
            processDefinitionNode.put("name", processDefinition.getName());
            processDefinitionNode.put("projectCode", processDefinition.getProjectCode());
            arrayNode.add(processDefinitionNode);
        }
        result.put(Constants.DATA_LIST, arrayNode);
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
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(code));
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
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
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
                                                       String taskDefinitionJson,
                                                       ProcessExecutionTypeEnum executionType) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);
        Map<String, Object> checkTaskDefinitions = checkTaskDefinitionList(taskDefinitionLogs, taskDefinitionJson);
        if (checkTaskDefinitions.get(Constants.STATUS) != Status.SUCCESS) {
            return checkTaskDefinitions;
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
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(code));
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
        processDefinition.setExecutionType(executionType);
        return updateDagDefine(loginUser, taskRelationList, processDefinition, processDefinitionDeepCopy, taskDefinitionLogs);
    }

    private Map<String, Object> updateDagDefine(User loginUser,
                                                List<ProcessTaskRelationLog> taskRelationList,
                                                ProcessDefinition processDefinition,
                                                ProcessDefinition processDefinitionDeepCopy,
                                                List<TaskDefinitionLog> taskDefinitionLogs) {
        Map<String, Object> result = new HashMap<>();
        int saveTaskResult = processService.saveTaskDefine(loginUser, processDefinition.getProjectCode(), taskDefinitionLogs, Boolean.TRUE);
        if (saveTaskResult == Constants.EXIT_CODE_SUCCESS) {
            logger.info("The task has not changed, so skip");
        }
        if (saveTaskResult == Constants.DEFINITION_FAILURE) {
            putMsg(result, Status.UPDATE_TASK_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_TASK_DEFINITION_ERROR);
        }
        boolean isChange = false;
        if (processDefinition.equals(processDefinitionDeepCopy) && saveTaskResult == Constants.EXIT_CODE_SUCCESS) {
            List<ProcessTaskRelationLog> processTaskRelationLogList = processTaskRelationLogMapper.queryByProcessCodeAndVersion(processDefinition.getCode(), processDefinition.getVersion());
            if (taskRelationList.size() == processTaskRelationLogList.size()) {
                Set<ProcessTaskRelationLog> taskRelationSet = taskRelationList.stream().collect(Collectors.toSet());
                Set<ProcessTaskRelationLog> processTaskRelationLogSet = processTaskRelationLogList.stream().collect(Collectors.toSet());
                if (taskRelationSet.size() == processTaskRelationLogSet.size()) {
                    taskRelationSet.removeAll(processTaskRelationLogSet);
                    if (!taskRelationSet.isEmpty()) {
                        isChange = true;
                    }
                } else {
                    isChange = true;
                }
            } else {
                isChange = true;
            }
        } else {
            isChange = true;
        }
        if (isChange) {
            processDefinition.setUpdateTime(new Date());
            int insertVersion = processService.saveProcessDefine(loginUser, processDefinition, Boolean.TRUE, Boolean.TRUE);
            if (insertVersion <= 0) {
                putMsg(result, Status.UPDATE_PROCESS_DEFINITION_ERROR);
                throw new ServiceException(Status.UPDATE_PROCESS_DEFINITION_ERROR);
            }
            int insertResult = processService.saveTaskRelation(loginUser, processDefinition.getProjectCode(),
                processDefinition.getCode(), insertVersion, taskRelationList, taskDefinitionLogs, Boolean.TRUE);
            if (insertResult == Constants.EXIT_CODE_SUCCESS) {
                putMsg(result, Status.SUCCESS);
                result.put(Constants.DATA_LIST, processDefinition);
            } else {
                putMsg(result, Status.UPDATE_PROCESS_DEFINITION_ERROR);
                throw new ServiceException(Status.UPDATE_PROCESS_DEFINITION_ERROR);
            }
        } else {
            putMsg(result, Status.SUCCESS);
            result.put(Constants.DATA_LIST, processDefinition);
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
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(code));
            return result;
        }

        // Determine if the login user is the owner of the process definition
        if (loginUser.getId() != processDefinition.getUserId() && loginUser.getUserType() != UserType.ADMIN_USER) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        // check process definition is already online
        if (processDefinition.getReleaseState() == ReleaseState.ONLINE) {
            putMsg(result, Status.PROCESS_DEFINE_STATE_ONLINE, String.valueOf(code));
            return result;
        }
        // check process instances is already running
        List<ProcessInstance> processInstances = processInstanceService.queryByProcessDefineCodeAndStatus(processDefinition.getCode(), Constants.NOT_TERMINATED_STATES);
        if (CollectionUtils.isNotEmpty(processInstances)) {
            putMsg(result, Status.DELETE_PROCESS_DEFINITION_BY_CODE_FAIL, processInstances.size());
            return result;
        }

        // get the timing according to the process definition
        Schedule scheduleObj = scheduleMapper.queryByProcessDefinitionCode(code);
        if (scheduleObj != null) {
            if (scheduleObj.getReleaseState() == ReleaseState.OFFLINE) {
                int delete = scheduleMapper.deleteById(scheduleObj.getId());
                if (delete == 0) {
                    putMsg(result, Status.DELETE_SCHEDULE_CRON_BY_ID_ERROR);
                    throw new ServiceException(Status.DELETE_SCHEDULE_CRON_BY_ID_ERROR);
                }
            }
            if (scheduleObj.getReleaseState() == ReleaseState.ONLINE) {
                putMsg(result, Status.SCHEDULE_CRON_STATE_ONLINE, scheduleObj.getId());
                return result;
            }
        }

        int delete = processDefinitionMapper.deleteById(processDefinition.getId());
        if (delete == 0) {
            putMsg(result, Status.DELETE_PROCESS_DEFINE_BY_CODE_ERROR);
            throw new ServiceException(Status.DELETE_PROCESS_DEFINE_BY_CODE_ERROR);
        }
        int deleteRelation = processTaskRelationMapper.deleteByCode(project.getCode(), processDefinition.getCode());
        if (deleteRelation == 0) {
            logger.warn("The process definition has not relation, it will be delete successfully");
        }
        putMsg(result, Status.SUCCESS);
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
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(code));
            return result;
        }
        switch (releaseState) {
            case ONLINE:
                List<ProcessTaskRelation> relationList = processService.findRelationByCode(code, processDefinition.getVersion());
                if (CollectionUtils.isEmpty(relationList)) {
                    putMsg(result, Status.PROCESS_DAG_IS_EMPTY);
                    return result;
                }
                processDefinition.setReleaseState(releaseState);
                processDefinitionMapper.updateById(processDefinition);
                break;
            case OFFLINE:
                processDefinition.setReleaseState(releaseState);
                int updateProcess = processDefinitionMapper.updateById(processDefinition);
                Schedule schedule = scheduleMapper.queryByProcessDefinitionCode(code);
                if (updateProcess > 0 && schedule != null) {
                    logger.info("set schedule offline, project code: {}, schedule id: {}, process definition code: {}", projectCode, schedule.getId(), code);
                    // set status
                    schedule.setReleaseState(releaseState);
                    int updateSchedule = scheduleMapper.updateById(schedule);
                    if (updateSchedule == 0) {
                        putMsg(result, Status.OFFLINE_SCHEDULE_ERROR);
                        throw new ServiceException(Status.OFFLINE_SCHEDULE_ERROR);
                    }
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
        if (org.apache.commons.lang.StringUtils.isEmpty(codes)) {
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
        if (CollectionUtils.isEmpty(processDefinitionList)) {
            return;
        }
        // check processDefinition exist in project
        List<ProcessDefinition> processDefinitionListInProject = processDefinitionList.stream().filter(o -> projectCode == o.getProjectCode()).collect(Collectors.toList());
        List<DagDataSchedule> dagDataSchedules = processDefinitionListInProject.stream().map(this::exportProcessDagData).collect(Collectors.toList());
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
        Schedule scheduleObj = scheduleMapper.queryByProcessDefinitionCode(processDefinition.getCode());
        DagDataSchedule dagDataSchedule = new DagDataSchedule(processService.genDagData(processDefinition));
        if (scheduleObj != null) {
            scheduleObj.setReleaseState(ReleaseState.OFFLINE);
            dagDataSchedule.setSchedule(scheduleObj);
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

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, Object> importSqlProcessDefinition(User loginUser, long projectCode, MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        String processDefinitionName = file.getOriginalFilename() == null ? file.getName() : file.getOriginalFilename();
        int index = processDefinitionName.lastIndexOf(".");
        if (index > 0) {
            processDefinitionName = processDefinitionName.substring(0, index);
        }
        processDefinitionName = processDefinitionName + "_import_" + DateUtils.getCurrentTimeStamp();

        ProcessDefinition processDefinition;
        List<TaskDefinitionLog> taskDefinitionList = new ArrayList<>();
        List<ProcessTaskRelationLog> processTaskRelationList = new ArrayList<>();

        // for Zip Bomb Attack
        final int THRESHOLD_ENTRIES = 10000;
        final int THRESHOLD_SIZE = 1000000000; // 1 GB
        final double THRESHOLD_RATIO = 10;
        int totalEntryArchive = 0;
        int totalSizeEntry = 0;
        // In most cases, there will be only one data source
        Map<String, DataSource> dataSourceCache = new HashMap<>(1);
        Map<String, Long> taskNameToCode = new HashMap<>(16);
        Map<String, List<String>> taskNameToUpstream = new HashMap<>(16);
        try (ZipInputStream zIn = new ZipInputStream(file.getInputStream());
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zIn))) {
            // build process definition
            processDefinition = new ProcessDefinition(projectCode,
                processDefinitionName,
                CodeGenerateUtils.getInstance().genCode(),
                "",
                "[]", null,
                0, loginUser.getId(), loginUser.getTenantId());

            ZipEntry entry;
            while ((entry = zIn.getNextEntry()) != null) {
                totalEntryArchive++;
                int totalSizeArchive = 0;
                if (!entry.isDirectory()) {
                    StringBuilder sql = new StringBuilder();
                    String taskName = null;
                    String datasourceName = null;
                    List<String> upstreams = Collections.emptyList();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        int nBytes = line.getBytes(StandardCharsets.UTF_8).length;
                        totalSizeEntry += nBytes;
                        totalSizeArchive += nBytes;
                        long compressionRatio = totalSizeEntry / entry.getCompressedSize();
                        if (compressionRatio > THRESHOLD_RATIO) {
                            throw new IllegalStateException("ratio between compressed and uncompressed data is highly suspicious, looks like a Zip Bomb Attack");
                        }
                        int commentIndex = line.indexOf("-- ");
                        if (commentIndex >= 0) {
                            int colonIndex = line.indexOf(":", commentIndex);
                            if (colonIndex > 0) {
                                String key = line.substring(commentIndex + 3, colonIndex).trim().toLowerCase();
                                String value = line.substring(colonIndex + 1).trim();
                                switch (key) {
                                    case "name":
                                        taskName = value;
                                        line = line.substring(0, commentIndex);
                                        break;
                                    case "upstream":
                                        upstreams = Arrays.stream(value.split(",")).map(String::trim)
                                            .filter(s -> !"".equals(s)).collect(Collectors.toList());
                                        line = line.substring(0, commentIndex);
                                        break;
                                    case "datasource":
                                        datasourceName = value;
                                        line = line.substring(0, commentIndex);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                        if (!"".equals(line)) {
                            sql.append(line).append("\n");
                        }
                    }
                    // import/sql1.sql -> sql1
                    if (taskName == null) {
                        taskName = entry.getName();
                        index = taskName.indexOf("/");
                        if (index > 0) {
                            taskName = taskName.substring(index + 1);
                        }
                        index = taskName.lastIndexOf(".");
                        if (index > 0) {
                            taskName = taskName.substring(0, index);
                        }
                    }
                    DataSource dataSource = dataSourceCache.get(datasourceName);
                    if (dataSource == null) {
                        dataSource = queryDatasourceByNameAndUser(datasourceName, loginUser);
                    }
                    if (dataSource == null) {
                        putMsg(result, Status.DATASOURCE_NAME_ILLEGAL);
                        return result;
                    }
                    dataSourceCache.put(datasourceName, dataSource);

                    TaskDefinitionLog taskDefinition = buildNormalSqlTaskDefinition(taskName, dataSource, sql.substring(0, sql.length() - 1));

                    taskDefinitionList.add(taskDefinition);
                    taskNameToCode.put(taskDefinition.getName(), taskDefinition.getCode());
                    taskNameToUpstream.put(taskDefinition.getName(), upstreams);
                }

                if (totalSizeArchive > THRESHOLD_SIZE) {
                    throw new IllegalStateException("the uncompressed data size is too much for the application resource capacity");
                }

                if (totalEntryArchive > THRESHOLD_ENTRIES) {
                    throw new IllegalStateException("too much entries in this archive, can lead to inodes exhaustion of the system");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            putMsg(result, Status.IMPORT_PROCESS_DEFINE_ERROR);
            return result;
        }

        // build task relation
        for (Map.Entry<String, Long> entry : taskNameToCode.entrySet()) {
            List<String> upstreams = taskNameToUpstream.get(entry.getKey());
            if (CollectionUtils.isEmpty(upstreams)
                || (upstreams.size() == 1 && upstreams.contains("root") && !taskNameToCode.containsKey("root"))) {
                ProcessTaskRelationLog processTaskRelation = buildNormalTaskRelation(0, entry.getValue());
                processTaskRelationList.add(processTaskRelation);
                continue;
            }
            for (String upstream : upstreams) {
                ProcessTaskRelationLog processTaskRelation = buildNormalTaskRelation(taskNameToCode.get(upstream), entry.getValue());
                processTaskRelationList.add(processTaskRelation);
            }
        }

        return createDagDefine(loginUser, processTaskRelationList, processDefinition, taskDefinitionList);
    }

    private ProcessTaskRelationLog buildNormalTaskRelation(long preTaskCode, long postTaskCode) {
        ProcessTaskRelationLog processTaskRelation = new ProcessTaskRelationLog();
        processTaskRelation.setPreTaskCode(preTaskCode);
        processTaskRelation.setPreTaskVersion(0);
        processTaskRelation.setPostTaskCode(postTaskCode);
        processTaskRelation.setPostTaskVersion(0);
        processTaskRelation.setConditionType(ConditionType.NONE);
        processTaskRelation.setName("");
        return processTaskRelation;
    }

    private DataSource queryDatasourceByNameAndUser(String datasourceName, User loginUser) {
        if (isAdmin(loginUser)) {
            List<DataSource> dataSources  = dataSourceMapper.queryDataSourceByName(datasourceName);
            if (CollectionUtils.isNotEmpty(dataSources)) {
                return dataSources.get(0);
            }
        } else {
            return dataSourceMapper.queryDataSourceByNameAndUserId(loginUser.getId(), datasourceName);
        }
        return null;
    }

    private TaskDefinitionLog buildNormalSqlTaskDefinition(String taskName, DataSource dataSource, String sql) throws CodeGenerateException {
        TaskDefinitionLog taskDefinition = new TaskDefinitionLog();
        taskDefinition.setName(taskName);
        taskDefinition.setFlag(Flag.YES);
        SqlParameters sqlParameters = new SqlParameters();
        sqlParameters.setType(dataSource.getType().name());
        sqlParameters.setDatasource(dataSource.getId());
        sqlParameters.setSql(sql.substring(0, sql.length() - 1));
        // it may be a query type, but it can only be determined by parsing SQL
        sqlParameters.setSqlType(SqlType.NON_QUERY.ordinal());
        sqlParameters.setLocalParams(Collections.emptyList());
        taskDefinition.setTaskParams(JSONUtils.toJsonString(sqlParameters));
        taskDefinition.setCode(CodeGenerateUtils.getInstance().genCode());
        taskDefinition.setTaskType(TASK_TYPE_SQL);
        taskDefinition.setFailRetryTimes(0);
        taskDefinition.setFailRetryInterval(0);
        taskDefinition.setTimeoutFlag(TimeoutFlag.CLOSE);
        taskDefinition.setWorkerGroup(DEFAULT_WORKER_GROUP);
        taskDefinition.setTaskPriority(Priority.MEDIUM);
        taskDefinition.setEnvironmentCode(-1);
        taskDefinition.setTimeout(0);
        taskDefinition.setDelayTime(0);
        taskDefinition.setTimeoutNotifyStrategy(TaskTimeoutStrategy.WARN);
        taskDefinition.setVersion(0);
        taskDefinition.setResourceIds("");
        return taskDefinition;
    }

    /**
     * check and import
     */
    private boolean checkAndImport(User loginUser, long projectCode, Map<String, Object> result, DagDataSchedule dagDataSchedule) {
        if (!checkImportanceParams(dagDataSchedule, result)) {
            return false;
        }
        ProcessDefinition processDefinition = dagDataSchedule.getProcessDefinition();

        // generate import processDefinitionName
        String processDefinitionName = recursionProcessDefinitionName(projectCode, processDefinition.getName(), 1);
        String importProcessDefinitionName = processDefinitionName + "_import_" + DateUtils.getCurrentTimeStamp();

        //unique check
        Map<String, Object> checkResult = verifyProcessDefinitionName(loginUser, projectCode, importProcessDefinitionName);
        if (Status.SUCCESS.equals(checkResult.get(Constants.STATUS))) {
            putMsg(result, Status.SUCCESS);
        } else {
            result.putAll(checkResult);
            return false;
        }
        processDefinition.setName(importProcessDefinitionName);
        processDefinition.setId(0);
        processDefinition.setProjectCode(projectCode);
        processDefinition.setUserId(loginUser.getId());
        try {
            processDefinition.setCode(CodeGenerateUtils.getInstance().genCode());
        } catch (CodeGenerateException e) {
            putMsg(result, Status.CREATE_PROCESS_DEFINITION_ERROR);
            return false;
        }
        List<TaskDefinition> taskDefinitionList = dagDataSchedule.getTaskDefinitionList();
        Map<Long, Long> taskCodeMap = new HashMap<>();
        Date now = new Date();
        List<TaskDefinitionLog> taskDefinitionLogList = new ArrayList<>();
        for (TaskDefinition taskDefinition : taskDefinitionList) {
            TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog(taskDefinition);
            taskDefinitionLog.setName(taskDefinitionLog.getName() + "_import_" + DateUtils.getCurrentTimeStamp());
            taskDefinitionLog.setProjectCode(projectCode);
            taskDefinitionLog.setUserId(loginUser.getId());
            taskDefinitionLog.setVersion(Constants.VERSION_FIRST);
            taskDefinitionLog.setCreateTime(now);
            taskDefinitionLog.setUpdateTime(now);
            taskDefinitionLog.setOperator(loginUser.getId());
            taskDefinitionLog.setOperateTime(now);
            try {
                long code = CodeGenerateUtils.getInstance().genCode();
                taskCodeMap.put(taskDefinitionLog.getCode(), code);
                taskDefinitionLog.setCode(code);
            } catch (CodeGenerateException e) {
                logger.error("Task code get error, ", e);
                putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS, "Error generating task definition code");
                return false;
            }
            taskDefinitionLogList.add(taskDefinitionLog);
        }
        int insert = taskDefinitionMapper.batchInsert(taskDefinitionLogList);
        int logInsert = taskDefinitionLogMapper.batchInsert(taskDefinitionLogList);
        if ((logInsert & insert) == 0) {
            putMsg(result, Status.CREATE_TASK_DEFINITION_ERROR);
            throw new ServiceException(Status.CREATE_TASK_DEFINITION_ERROR);
        }

        List<ProcessTaskRelation> taskRelationList = dagDataSchedule.getProcessTaskRelationList();
        List<ProcessTaskRelationLog> taskRelationLogList = new ArrayList<>();
        for (ProcessTaskRelation processTaskRelation : taskRelationList) {
            ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog(processTaskRelation);
            if (taskCodeMap.containsKey(processTaskRelationLog.getPreTaskCode())) {
                processTaskRelationLog.setPreTaskCode(taskCodeMap.get(processTaskRelationLog.getPreTaskCode()));
            }
            if (taskCodeMap.containsKey(processTaskRelationLog.getPostTaskCode())) {
                processTaskRelationLog.setPostTaskCode(taskCodeMap.get(processTaskRelationLog.getPostTaskCode()));
            }
            processTaskRelationLog.setPreTaskVersion(Constants.VERSION_FIRST);
            processTaskRelationLog.setPostTaskVersion(Constants.VERSION_FIRST);
            taskRelationLogList.add(processTaskRelationLog);
        }
        if (StringUtils.isNotEmpty(processDefinition.getLocations()) && JSONUtils.checkJsonValid(processDefinition.getLocations())) {
            ArrayNode arrayNode = JSONUtils.parseArray(processDefinition.getLocations());
            ArrayNode newArrayNode = JSONUtils.createArrayNode();
            for (int i = 0; i < arrayNode.size(); i++) {
                ObjectNode newObjectNode = newArrayNode.addObject();
                JsonNode jsonNode = arrayNode.get(i);
                Long taskCode = taskCodeMap.get(jsonNode.get("taskCode").asLong());
                if (Objects.nonNull(taskCode)) {
                    newObjectNode.put("taskCode", taskCode);
                    newObjectNode.set("x", jsonNode.get("x"));
                    newObjectNode.set("y", jsonNode.get("y"));
                }
            }
            processDefinition.setLocations(newArrayNode.toString());
        }
        processDefinition.setCreateTime(new Date());
        processDefinition.setUpdateTime(new Date());
        Map<String, Object> createDagResult = createDagDefine(loginUser, taskRelationLogList, processDefinition, Lists.newArrayList());
        if (Status.SUCCESS.equals(createDagResult.get(Constants.STATUS))) {
            putMsg(createDagResult, Status.SUCCESS);
        } else {
            result.putAll(createDagResult);
            throw new ServiceException(Status.IMPORT_PROCESS_DEFINE_ERROR);
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
                throw new ServiceException(Status.IMPORT_PROCESS_DEFINE_ERROR);
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
    public Map<String, Object> checkProcessNodeList(String processTaskRelationJson, List<TaskDefinitionLog> taskDefinitionLogsList) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (processTaskRelationJson == null) {
                logger.error("process data is null");
                putMsg(result, Status.DATA_IS_NOT_VALID, processTaskRelationJson);
                return result;
            }

            List<ProcessTaskRelation> taskRelationList = JSONUtils.toList(processTaskRelationJson, ProcessTaskRelation.class);
            // Check whether the task node is normal
            List<TaskNode> taskNodes = processService.transformTask(taskRelationList, taskDefinitionLogsList);

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
                if (!taskPluginManager.checkTaskParameters(ParametersNode.builder()
                        .taskType(taskNode.getType())
                        .taskParams(taskNode.getTaskParams())
                        .dependence(taskNode.getDependence())
                        .switchResult(taskNode.getSwitchResult())
                        .build())) {
                    logger.error("task node {} parameter invalid", taskNode.getName());
                    putMsg(result, Status.PROCESS_NODE_S_PARAMETER_INVALID, taskNode.getName());
                    return result;
                }

                // check extra params
                CheckUtils.checkOtherParams(taskNode.getExtras());
            }
            putMsg(result, Status.SUCCESS);
        } catch (Exception e) {
            result.put(Constants.STATUS, Status.INTERNAL_SERVER_ERROR_ARGS);
            putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS, e.getMessage());
            logger.error(Status.INTERNAL_SERVER_ERROR_ARGS.getMsg(), e);
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
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            logger.info("process define not exists");
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(code));
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
        HashMap<Long, Project> userProjects =  new HashMap<>(Constants.DEFAULT_HASH_MAP_SIZE);
        projectMapper.queryProjectCreatedAndAuthorizedByUserId(loginUser.getId())
            .forEach(userProject -> userProjects.put(userProject.getCode(), userProject));

        // check processDefinition exist in project
        List<ProcessDefinition> processDefinitionListInProject = processDefinitionList.stream()
            .filter(o -> userProjects.containsKey(o.getProjectCode())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(processDefinitionListInProject)) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, codes);
            return result;
        }
        Map<Long, List<TaskDefinition>> taskNodeMap = new HashMap<>();
        for (ProcessDefinition processDefinition : processDefinitionListInProject) {
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
     * query process definition list by project code
     *
     * @param projectCode project code
     * @return process definition list in the project
     */
    @Override
    public Map<String, Object> queryProcessDefinitionListByProjectCode(long projectCode) {
        Map<String, Object> result = new HashMap<>();
        List<DependentSimplifyDefinition> processDefinitions = processDefinitionMapper.queryDefinitionListByProjectCodeAndProcessDefinitionCodes(projectCode, null);
        result.put(Constants.DATA_LIST, processDefinitions);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query process definition list by process definition code
     *
     * @param projectCode project code
     * @param processDefinitionCode process definition code
     * @return task definition list in the process definition
     */
    @Override
    public Map<String, Object> queryTaskDefinitionListByProcessDefinitionCode(long projectCode, Long processDefinitionCode) {
        Map<String, Object> result = new HashMap<>();

        Set<Long> definitionCodesSet = new HashSet<>();
        definitionCodesSet.add(processDefinitionCode);
        List<DependentSimplifyDefinition> processDefinitions = processDefinitionMapper.queryDefinitionListByProjectCodeAndProcessDefinitionCodes(projectCode, definitionCodesSet);

        //query process task relation
        List<ProcessTaskRelation> processTaskRelations = processTaskRelationMapper.queryProcessTaskRelationsByProcessDefinitionCode(
                processDefinitions.get(0).getCode(),
                processDefinitions.get(0).getVersion());

        //query task definition log
        List<TaskDefinitionLog> taskDefinitionLogsList = processService.genTaskDefineList(processTaskRelations);

        List<DependentSimplifyDefinition> taskDefinitionList = new ArrayList<>();
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogsList) {
            DependentSimplifyDefinition dependentSimplifyDefinition = new DependentSimplifyDefinition();
            dependentSimplifyDefinition.setCode(taskDefinitionLog.getCode());
            dependentSimplifyDefinition.setName(taskDefinitionLog.getName());
            dependentSimplifyDefinition.setVersion(taskDefinitionLog.getVersion());
            taskDefinitionList.add(dependentSimplifyDefinition);
        }

        result.put(Constants.DATA_LIST, taskDefinitionList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * Encapsulates the TreeView structure
     *
     * @param projectCode project code
     * @param code  process definition code
     * @param limit limit
     * @return tree view json data
     */
    @Override
    public Map<String, Object> viewTree(long projectCode, long code, Integer limit) {
        Map<String, Object> result = new HashMap<>();
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);
        if (null == processDefinition || projectCode != processDefinition.getProjectCode()) {
            logger.info("process define not exists");
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(code));
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
        List<TaskDefinitionLog> taskDefinitionList = processService.genTaskDefineList(processTaskRelationMapper.queryByProcessCode(processDefinition.getProjectCode(), processDefinition.getCode()));
        Map<Long, TaskDefinitionLog> taskDefinitionMap = taskDefinitionList.stream()
            .collect(Collectors.toMap(TaskDefinitionLog::getCode, taskDefinitionLog -> taskDefinitionLog));

        if (limit > processInstanceList.size()) {
            limit = processInstanceList.size();
        }

        TreeViewDto parentTreeViewDto = new TreeViewDto();
        parentTreeViewDto.setName("DAG");
        parentTreeViewDto.setType("");
        parentTreeViewDto.setCode(0L);
        // Specify the process definition, because it is a TreeView for a process definition
        for (int i = limit - 1; i >= 0; i--) {
            ProcessInstance processInstance = processInstanceList.get(i);
            Date endTime = processInstance.getEndTime() == null ? new Date() : processInstance.getEndTime();
            parentTreeViewDto.getInstances().add(new Instance(processInstance.getId(), processInstance.getName(), processInstance.getProcessDefinitionCode(),
                "", processInstance.getState().toString(), processInstance.getStartTime(), endTime, processInstance.getHost(),
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
                String nodeCode = en.getKey();
                parentTreeViewDtoList = en.getValue();

                TreeViewDto treeViewDto = new TreeViewDto();
                TaskNode taskNode = dag.getNode(nodeCode);
                treeViewDto.setType(taskNode.getType());
                treeViewDto.setCode(taskNode.getCode());
                treeViewDto.setName(taskNode.getName());
                //set treeViewDto instances
                for (int i = limit - 1; i >= 0; i--) {
                    ProcessInstance processInstance = processInstanceList.get(i);
                    TaskInstance taskInstance = taskInstanceMapper.queryByInstanceIdAndCode(processInstance.getId(), Long.parseLong(nodeCode));
                    if (taskInstance == null) {
                        treeViewDto.getInstances().add(new Instance(-1, "not running", 0, "null"));
                    } else {
                        Date startTime = taskInstance.getStartTime() == null ? new Date() : taskInstance.getStartTime();
                        Date endTime = taskInstance.getEndTime() == null ? new Date() : taskInstance.getEndTime();

                        long subProcessCode = 0L;
                        // if process is sub process, the return sub id, or sub id=0
                        if (taskInstance.isSubProcess()) {
                            TaskDefinition taskDefinition = taskDefinitionMap.get(taskInstance.getTaskCode());
                            subProcessCode = Long.parseLong(JSONUtils.parseObject(
                                    taskDefinition.getTaskParams()).path(CMD_PARAM_SUB_PROCESS_DEFINE_CODE).asText());
                        }
                        treeViewDto.getInstances().add(new Instance(taskInstance.getId(), taskInstance.getName(), taskInstance.getTaskCode(),
                                taskInstance.getTaskType(), taskInstance.getState().toString(), taskInstance.getStartTime(), taskInstance.getEndTime(),
                                taskInstance.getHost(), DateUtils.format2Readable(endTime.getTime() - startTime.getTime()), subProcessCode));
                    }
                }
                for (TreeViewDto pTreeViewDto : parentTreeViewDtoList) {
                    pTreeViewDto.getChildren().add(treeViewDto);
                }
                postNodeList = dag.getSubsequentNodes(nodeCode);
                if (CollectionUtils.isNotEmpty(postNodeList)) {
                    for (String nextNodeCode : postNodeList) {
                        List<TreeViewDto> treeViewDtoList = waitingRunningNodeMap.get(nextNodeCode);
                        if (CollectionUtils.isEmpty(treeViewDtoList)) {
                            treeViewDtoList = new ArrayList<>();
                        }
                        treeViewDtoList.add(treeViewDto);
                        waitingRunningNodeMap.put(nextNodeCode, treeViewDtoList);
                    }
                }
                runningNodeMap.remove(nodeCode);
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
            graph.addNode(Long.toString(taskNodeResponse.getCode()), taskNodeResponse);
        }
        // Fill edge relations
        for (TaskNode taskNodeResponse : taskNodeResponseList) {
            List<String> preTasks = JSONUtils.toList(taskNodeResponse.getPreTasks(), String.class);
            if (CollectionUtils.isNotEmpty(preTasks)) {
                for (String preTask : preTasks) {
                    if (!graph.addEdge(preTask, Long.toString(taskNodeResponse.getCode()))) {
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
        if (result.get(Constants.STATUS) == Status.NOT_SUPPORT_COPY_TASK_TYPE) {
            return result;
        }
        checkBatchOperateResult(projectCode, targetProjectCode, result, failedProcessList, true);
        return result;
    }

    /**
     * batch move process definition
     * Will be deleted
     * @param loginUser loginUser
     * @param projectCode projectCode
     * @param codes processDefinitionCodes
     * @param targetProjectCode targetProjectCode
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
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

        if (org.apache.commons.lang.StringUtils.isEmpty(processDefinitionCodes)) {
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
                List<TaskDefinitionLog> taskDefinitionLogs = processService.genTaskDefineList(processTaskRelations);
                Map<Long, Long> taskCodeMap = new HashMap<>();
                for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
                    if (COMPLEX_TASK_TYPES.contains(taskDefinitionLog.getTaskType())) {
                        putMsg(result, Status.NOT_SUPPORT_COPY_TASK_TYPE, taskDefinitionLog.getTaskType());
                        return;
                    }
                    try {
                        long taskCode = CodeGenerateUtils.getInstance().genCode();
                        taskCodeMap.put(taskDefinitionLog.getCode(), taskCode);
                        taskDefinitionLog.setCode(taskCode);
                    } catch (CodeGenerateException e) {
                        putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS);
                        throw new ServiceException(Status.INTERNAL_SERVER_ERROR_ARGS);
                    }
                    taskDefinitionLog.setProjectCode(targetProjectCode);
                    taskDefinitionLog.setVersion(0);
                    taskDefinitionLog.setName(taskDefinitionLog.getName() + "_copy_" + DateUtils.getCurrentTimeStamp());
                }
                for (ProcessTaskRelationLog processTaskRelationLog : taskRelationList) {
                    if (processTaskRelationLog.getPreTaskCode() > 0) {
                        processTaskRelationLog.setPreTaskCode(taskCodeMap.get(processTaskRelationLog.getPreTaskCode()));
                    }
                    if (processTaskRelationLog.getPostTaskCode() > 0) {
                        processTaskRelationLog.setPostTaskCode(taskCodeMap.get(processTaskRelationLog.getPostTaskCode()));
                    }
                }
                try {
                    processDefinition.setCode(CodeGenerateUtils.getInstance().genCode());
                } catch (CodeGenerateException e) {
                    putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS);
                    throw new ServiceException(Status.INTERNAL_SERVER_ERROR_ARGS);
                }
                processDefinition.setId(0);
                processDefinition.setUserId(loginUser.getId());
                processDefinition.setName(processDefinition.getName() + "_copy_" + DateUtils.getCurrentTimeStamp());
                if (StringUtils.isNotBlank(processDefinition.getLocations())) {
                    ArrayNode jsonNodes = JSONUtils.parseArray(processDefinition.getLocations());
                    for (int i = 0; i < jsonNodes.size(); i++) {
                        ObjectNode node = (ObjectNode) jsonNodes.path(i);
                        node.put("taskCode", taskCodeMap.get(node.get("taskCode").asLong()));
                        jsonNodes.set(i, node);
                    }
                    processDefinition.setLocations(JSONUtils.toJsonString(jsonNodes));
                }
                try {
                    result.putAll(createDagDefine(loginUser, taskRelationList, processDefinition, taskDefinitionLogs));
                } catch (Exception e) {
                    putMsg(result, Status.COPY_PROCESS_DEFINITION_ERROR);
                    throw new ServiceException(Status.COPY_PROCESS_DEFINITION_ERROR);
                }
            } else {
                try {
                    result.putAll(updateDagDefine(loginUser, taskRelationList, processDefinition, null, Lists.newArrayList()));
                } catch (Exception e) {
                    putMsg(result, Status.MOVE_PROCESS_DEFINITION_ERROR);
                    throw new ServiceException(Status.MOVE_PROCESS_DEFINITION_ERROR);
                }
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
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, Object> switchProcessDefinitionVersion(User loginUser, long projectCode, long code, int version) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);
        if (Objects.isNull(processDefinition) || projectCode != processDefinition.getProjectCode()) {
            putMsg(result, Status.SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_ERROR, code);
            return result;
        }

        ProcessDefinitionLog processDefinitionLog = processDefinitionLogMapper.queryByDefinitionCodeAndVersion(code, version);
        if (Objects.isNull(processDefinitionLog)) {
            putMsg(result, Status.SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_VERSION_ERROR, processDefinition.getCode(), version);
            return result;
        }
        int switchVersion = processService.switchVersion(processDefinition, processDefinitionLog);
        if (switchVersion <= 0) {
            putMsg(result, Status.SWITCH_PROCESS_DEFINITION_VERSION_ERROR);
            throw new ServiceException(Status.SWITCH_PROCESS_DEFINITION_VERSION_ERROR);
        }
        putMsg(result, Status.SUCCESS);
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
        IPage<ProcessDefinitionLog> processDefinitionVersionsPaging = processDefinitionLogMapper.queryProcessDefinitionVersionsPaging(page, code, projectCode);
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
     * @return delete result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, Object> deleteProcessDefinitionVersion(User loginUser, long projectCode, long code, int version) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);

        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(code));
        } else {
            if (processDefinition.getVersion() == version) {
                putMsg(result, Status.MAIN_TABLE_USING_VERSION);
                return result;
            }
            int deleteLog = processDefinitionLogMapper.deleteByProcessDefinitionCodeAndVersion(code, version);
            int deleteRelationLog = processTaskRelationLogMapper.deleteByCode(code, version);
            if (deleteLog == 0 || deleteRelationLog == 0) {
                putMsg(result, Status.DELETE_PROCESS_DEFINE_BY_CODE_ERROR);
                throw new ServiceException(Status.DELETE_PROCESS_DEFINE_BY_CODE_ERROR);
            }
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * create empty process definition
     *
     * @param loginUser    login user
     * @param projectCode  project code
     * @param name         process definition name
     * @param description  description
     * @param globalParams globalParams
     * @param timeout      timeout
     * @param tenantCode   tenantCode
     * @param scheduleJson scheduleJson
     * @return process definition code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, Object> createEmptyProcessDefinition(User loginUser,
                                                            long projectCode,
                                                            String name,
                                                            String description,
                                                            String globalParams,
                                                            int timeout,
                                                            String tenantCode,
                                                            String scheduleJson,
                                                            ProcessExecutionTypeEnum executionType) {
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
            processDefinitionCode = CodeGenerateUtils.getInstance().genCode();
        } catch (CodeGenerateException e) {
            putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS);
            return result;
        }
        ProcessDefinition processDefinition = new ProcessDefinition(projectCode, name, processDefinitionCode, description,
            globalParams, "", timeout, loginUser.getId(), tenantId);
        processDefinition.setExecutionType(executionType);
        result = createEmptyDagDefine(loginUser, processDefinition);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        if (StringUtils.isBlank(scheduleJson)) {
            return result;
        }

        // save dag schedule
        Map<String, Object> scheduleResult = createDagSchedule(loginUser, processDefinition, scheduleJson);
        if (scheduleResult.get(Constants.STATUS) != Status.SUCCESS) {
            Status scheduleResultStatus = (Status) scheduleResult.get(Constants.STATUS);
            putMsg(result, scheduleResultStatus);
            throw new ServiceException(scheduleResultStatus);
        }
        return result;
    }

    private Map<String, Object> createEmptyDagDefine(User loginUser, ProcessDefinition processDefinition) {
        Map<String, Object> result = new HashMap<>();
        int insertVersion = processService.saveProcessDefine(loginUser, processDefinition, Boolean.TRUE, Boolean.TRUE);
        if (insertVersion == 0) {
            putMsg(result, Status.CREATE_PROCESS_DEFINITION_ERROR);
            throw new ServiceException(Status.CREATE_PROCESS_DEFINITION_ERROR);
        }
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, processDefinition);
        return result;
    }

    private Map<String, Object> createDagSchedule(User loginUser, ProcessDefinition processDefinition, String scheduleJson) {
        Map<String, Object> result = new HashMap<>();
        Schedule scheduleObj = JSONUtils.parseObject(scheduleJson, Schedule.class);
        if (scheduleObj == null) {
            putMsg(result, Status.DATA_IS_NOT_VALID, scheduleJson);
            throw new ServiceException(Status.DATA_IS_NOT_VALID);
        }
        Date now = new Date();
        scheduleObj.setProcessDefinitionCode(processDefinition.getCode());
        if (DateUtils.differSec(scheduleObj.getStartTime(), scheduleObj.getEndTime()) == 0) {
            logger.warn("The start time must not be the same as the end");
            putMsg(result, Status.SCHEDULE_START_TIME_END_TIME_SAME);
            return result;
        }
        if (!org.quartz.CronExpression.isValidExpression(scheduleObj.getCrontab())) {
            logger.error("{} verify failure", scheduleObj.getCrontab());
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, scheduleObj.getCrontab());
            return result;
        }
        scheduleObj.setWarningType(scheduleObj.getWarningType() == null ? WarningType.NONE : scheduleObj.getWarningType());
        scheduleObj.setWarningGroupId(scheduleObj.getWarningGroupId() == 0 ? 1 : scheduleObj.getWarningGroupId());
        scheduleObj.setFailureStrategy(scheduleObj.getFailureStrategy() == null ? FailureStrategy.CONTINUE : scheduleObj.getFailureStrategy());
        scheduleObj.setCreateTime(now);
        scheduleObj.setUpdateTime(now);
        scheduleObj.setUserId(loginUser.getId());
        scheduleObj.setReleaseState(ReleaseState.OFFLINE);
        scheduleObj.setProcessInstancePriority(scheduleObj.getProcessInstancePriority() == null ? Priority.MEDIUM : scheduleObj.getProcessInstancePriority());
        scheduleObj.setWorkerGroup(scheduleObj.getWorkerGroup() == null ? "default" : scheduleObj.getWorkerGroup());
        scheduleObj.setEnvironmentCode(scheduleObj.getEnvironmentCode() == null ? -1 : scheduleObj.getEnvironmentCode());
        scheduleMapper.insert(scheduleObj);

        putMsg(result, Status.SUCCESS);
        result.put("scheduleId", scheduleObj.getId());
        return result;
    }

    /**
     * update process definition basic info
     *
     * @param loginUser     login user
     * @param projectCode   project code
     * @param name          process definition name
     * @param code          process definition code
     * @param description   description
     * @param globalParams  globalParams
     * @param timeout       timeout
     * @param tenantCode    tenantCode
     * @param scheduleJson  scheduleJson
     * @param executionType executionType
     * @return update result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, Object> updateProcessDefinitionBasicInfo(User loginUser,
                                                                long projectCode,
                                                                String name,
                                                                long code,
                                                                String description,
                                                                String globalParams,
                                                                int timeout,
                                                                String tenantCode,
                                                                String scheduleJson,
                                                                ProcessExecutionTypeEnum executionType) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
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

        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);
        // check process definition exists
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(code));
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
        processDefinition.set(projectCode, name, description, globalParams, "", timeout, tenantId);
        processDefinition.setExecutionType(executionType);
        List<ProcessTaskRelationLog> taskRelationList = processTaskRelationLogMapper.queryByProcessCodeAndVersion(processDefinition.getCode(), processDefinition.getVersion());
        result = updateDagDefine(loginUser, taskRelationList, processDefinition, processDefinitionDeepCopy, Lists.newArrayList());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        if (StringUtils.isBlank(scheduleJson)) {
            return result;
        }
        // update dag schedule
        Map<String, Object> scheduleResult = updateDagSchedule(loginUser, projectCode, code, scheduleJson);
        if (scheduleResult.get(Constants.STATUS) != Status.SUCCESS) {
            Status scheduleResultStatus = (Status) scheduleResult.get(Constants.STATUS);
            putMsg(result, scheduleResultStatus);
            throw new ServiceException(scheduleResultStatus);
        }
        return result;
    }

    private Map<String, Object> updateDagSchedule(User loginUser,
                                                  long projectCode,
                                                  long processDefinitionCode,
                                                  String scheduleJson) {
        Map<String, Object> result = new HashMap<>();
        Schedule schedule = JSONUtils.parseObject(scheduleJson, Schedule.class);
        if (schedule == null) {
            putMsg(result, Status.DATA_IS_NOT_VALID, scheduleJson);
            throw new ServiceException(Status.DATA_IS_NOT_VALID);
        }
        // set default value
        FailureStrategy failureStrategy = schedule.getFailureStrategy() == null ? FailureStrategy.CONTINUE : schedule.getFailureStrategy();
        WarningType warningType = schedule.getWarningType() == null ? WarningType.NONE : schedule.getWarningType();
        Priority processInstancePriority = schedule.getProcessInstancePriority() == null ? Priority.MEDIUM : schedule.getProcessInstancePriority();
        int warningGroupId = schedule.getWarningGroupId() == 0 ? 1 : schedule.getWarningGroupId();
        String workerGroup = schedule.getWorkerGroup() == null ? "default" : schedule.getWorkerGroup();
        long environmentCode = schedule.getEnvironmentCode() == null ? -1 : schedule.getEnvironmentCode();

        ScheduleParam param = new ScheduleParam();
        param.setStartTime(schedule.getStartTime());
        param.setEndTime(schedule.getEndTime());
        param.setCrontab(schedule.getCrontab());
        param.setTimezoneId(schedule.getTimezoneId());

        return schedulerService.updateScheduleByProcessDefinitionCode(
            loginUser,
            projectCode,
            processDefinitionCode,
            JSONUtils.toJsonString(param),
            warningType,
            warningGroupId,
            failureStrategy,
            processInstancePriority,
            workerGroup,
            environmentCode);
    }

    /**
     * release process definition and schedule
     *
     * @param loginUser    login user
     * @param projectCode  project code
     * @param code         process definition code
     * @param releaseState releaseState
     * @return update result code
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> releaseWorkflowAndSchedule(User loginUser, long projectCode, long code, ReleaseState releaseState) {
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
        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(code));
            return result;
        }
        Schedule scheduleObj = scheduleMapper.queryByProcessDefinitionCode(code);
        if (scheduleObj == null) {
            putMsg(result, Status.SCHEDULE_CRON_NOT_EXISTS, "processDefinitionCode:" + code);
            return result;
        }
        switch (releaseState) {
            case ONLINE:
                List<ProcessTaskRelation> relationList = processService.findRelationByCode(code, processDefinition.getVersion());
                if (CollectionUtils.isEmpty(relationList)) {
                    putMsg(result, Status.PROCESS_DAG_IS_EMPTY);
                    return result;
                }
                processDefinition.setReleaseState(releaseState);
                processDefinitionMapper.updateById(processDefinition);
                schedulerService.setScheduleState(loginUser, projectCode, scheduleObj.getId(), ReleaseState.ONLINE);
                break;
            case OFFLINE:
                processDefinition.setReleaseState(releaseState);
                int updateProcess = processDefinitionMapper.updateById(processDefinition);
                if (updateProcess > 0) {
                    logger.info("set schedule offline, project code: {}, schedule id: {}, process definition code: {}", projectCode, scheduleObj.getId(), code);
                    // set status
                    scheduleObj.setReleaseState(ReleaseState.OFFLINE);
                    int updateSchedule = scheduleMapper.updateById(scheduleObj);
                    if (updateSchedule == 0) {
                        putMsg(result, Status.OFFLINE_SCHEDULE_ERROR);
                        throw new ServiceException(Status.OFFLINE_SCHEDULE_ERROR);
                    }
                    schedulerService.deleteSchedule(project.getId(), scheduleObj.getId());
                }
                break;
            default:
                putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, RELEASESTATE);
                return result;
        }
        putMsg(result, Status.SUCCESS);
        return result;
    }
}
