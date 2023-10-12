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

import static java.util.stream.Collectors.toSet;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_DEFINITION_MOVE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.VERSION_LIST;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_BATCH_COPY;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_DEFINITION;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_DEFINITION_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_DEFINITION_EXPORT;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_IMPORT;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_ONLINE_OFFLINE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_SWITCH_TO_THIS_VERSION;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_TREE_VIEW;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_UPDATE;
import static org.apache.dolphinscheduler.api.enums.Status.PROCESS_DEFINE_NOT_EXIST;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_SUB_PROCESS_DEFINE_CODE;
import static org.apache.dolphinscheduler.common.constants.Constants.COPY_SUFFIX;
import static org.apache.dolphinscheduler.common.constants.Constants.DATA_LIST;
import static org.apache.dolphinscheduler.common.constants.Constants.DEFAULT_WORKER_GROUP;
import static org.apache.dolphinscheduler.common.constants.Constants.EMPTY_STRING;
import static org.apache.dolphinscheduler.common.constants.Constants.GLOBAL_PARAMS;
import static org.apache.dolphinscheduler.common.constants.Constants.IMPORT_SUFFIX;
import static org.apache.dolphinscheduler.common.constants.Constants.LOCAL_PARAMS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.LOCAL_PARAMS_LIST;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SQL;

import org.apache.dolphinscheduler.api.dto.DagDataSchedule;
import org.apache.dolphinscheduler.api.dto.ScheduleParam;
import org.apache.dolphinscheduler.api.dto.treeview.Instance;
import org.apache.dolphinscheduler.api.dto.treeview.TreeViewDto;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowCreateRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowFilterRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowUpdateRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.MetricsCleanUpService;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.service.TaskDefinitionLogService;
import org.apache.dolphinscheduler.api.service.TaskDefinitionService;
import org.apache.dolphinscheduler.api.service.WorkFlowLineageService;
import org.apache.dolphinscheduler.api.utils.CheckUtils;
import org.apache.dolphinscheduler.api.utils.FileUtils;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ConditionType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils.CodeGenerateException;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.DependentSimplifyDefinition;
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
import org.apache.dolphinscheduler.dao.entity.TaskMainInfo;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.UserWithProcessDefinitionCode;
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
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.dao.model.PageListingResult;
import org.apache.dolphinscheduler.dao.repository.ProcessDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.ProcessDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.SqlType;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ParametersNode;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SqlParameters;
import org.apache.dolphinscheduler.service.cron.CronUtils;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
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
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
@Slf4j
public class ProcessDefinitionServiceImpl extends BaseServiceImpl implements ProcessDefinitionService {

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
    private ProcessDefinitionDao processDefinitionDao;

    @Autowired
    private ProcessDefinitionLogDao processDefinitionLogDao;
    @Lazy
    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private TaskInstanceMapper taskInstanceMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private ProcessService processService;

    @Autowired
    private TaskDefinitionLogDao taskDefinitionLogDao;

    @Autowired
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Autowired
    private ProcessTaskRelationLogMapper processTaskRelationLogMapper;

    @Autowired
    TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Lazy
    @Autowired
    private TaskDefinitionService taskDefinitionService;

    @Autowired
    private TaskDefinitionLogService taskDefinitionLogService;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Lazy
    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private TaskPluginManager taskPluginManager;

    @Autowired
    private WorkFlowLineageService workFlowLineageService;

    @Autowired
    private MetricsCleanUpService metricsCleanUpService;

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
     * @param taskRelationJson relation json for nodes
     * @param taskDefinitionJson taskDefinitionJson
     * @return create result code
     */
    @Override
    @Transactional
    public Map<String, Object> createProcessDefinition(User loginUser,
                                                       long projectCode,
                                                       String name,
                                                       String description,
                                                       String globalParams,
                                                       String locations,
                                                       int timeout,
                                                       String taskRelationJson,
                                                       String taskDefinitionJson,
                                                       String otherParamsJson,
                                                       ProcessExecutionTypeEnum executionType) {
        Project project = projectMapper.queryByCode(projectCode);

        // check if user have write perm for project
        Map<String, Object> result = new HashMap<>();
        boolean hasProjectAndWritePerm = projectService.hasProjectAndWritePerm(loginUser, project, result);
        if (!hasProjectAndWritePerm) {
            return result;
        }
        if (checkDescriptionLength(description)) {
            log.warn("Parameter description is too long.");
            throw new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR);
        }
        // check whether the new process define name exist
        ProcessDefinition definition = processDefinitionMapper.verifyByDefineName(project.getCode(), name);
        if (definition != null) {
            log.warn("Process definition with the same name {} already exists, processDefinitionCode:{}.",
                    definition.getName(), definition.getCode());
            throw new ServiceException(Status.PROCESS_DEFINITION_NAME_EXIST, name);
        }
        List<TaskDefinitionLog> taskDefinitionLogs = generateTaskDefinitionList(taskDefinitionJson);
        List<ProcessTaskRelationLog> taskRelationList = generateTaskRelationList(taskRelationJson, taskDefinitionLogs);

        long processDefinitionCode = CodeGenerateUtils.getInstance().genCode();
        ProcessDefinition processDefinition =
                new ProcessDefinition(projectCode, name, processDefinitionCode, description,
                        globalParams, locations, timeout, loginUser.getId());
        processDefinition.setExecutionType(executionType);

        return createDagDefine(loginUser, taskRelationList, processDefinition, taskDefinitionLogs, otherParamsJson);
    }

    private void createWorkflowValid(User user, ProcessDefinition processDefinition) {
        Project project = projectMapper.queryByCode(processDefinition.getProjectCode());
        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_FOUND, processDefinition.getProjectCode());
        }
        // check user access for project
        projectService.checkProjectAndAuthThrowException(user, project, WORKFLOW_CREATE);

        if (checkDescriptionLength(processDefinition.getDescription())) {
            throw new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR);
        }

        // check whether the new process define name exist
        ProcessDefinition definition =
                processDefinitionMapper.verifyByDefineName(project.getCode(), processDefinition.getName());
        if (definition != null) {
            throw new ServiceException(Status.PROCESS_DEFINITION_NAME_EXIST, processDefinition.getName());
        }

    }

    private void syncObj2Log(User user, ProcessDefinition processDefinition) {
        ProcessDefinitionLog processDefinitionLog = new ProcessDefinitionLog(processDefinition);
        processDefinitionLog.setOperator(user.getId());
        int result = processDefinitionLogMapper.insert(processDefinitionLog);
        if (result <= 0) {
            throw new ServiceException(Status.CREATE_PROCESS_DEFINITION_LOG_ERROR);
        }
    }

    /**
     * create single process definition
     *
     * @param loginUser login user
     * @param workflowCreateRequest the new workflow object will be created
     * @return New ProcessDefinition object created just now
     */
    @Override
    @Transactional
    public ProcessDefinition createSingleProcessDefinition(User loginUser,
                                                           WorkflowCreateRequest workflowCreateRequest) {
        ProcessDefinition processDefinition = workflowCreateRequest.convert2ProcessDefinition();
        this.createWorkflowValid(loginUser, processDefinition);

        long processDefinitionCode;
        try {
            processDefinitionCode = CodeGenerateUtils.getInstance().genCode();
        } catch (CodeGenerateException e) {
            throw new ServiceException(Status.INTERNAL_SERVER_ERROR_ARGS);
        }

        processDefinition.setCode(processDefinitionCode);
        processDefinition.setUserId(loginUser.getId());

        int create = processDefinitionMapper.insert(processDefinition);
        if (create <= 0) {
            throw new ServiceException(Status.CREATE_PROCESS_DEFINITION_ERROR);
        }
        this.syncObj2Log(loginUser, processDefinition);
        return processDefinition;
    }

    protected Map<String, Object> createDagDefine(User loginUser,
                                                  List<ProcessTaskRelationLog> taskRelationList,
                                                  ProcessDefinition processDefinition,
                                                  List<TaskDefinitionLog> taskDefinitionLogs, String otherParamsJson) {
        Map<String, Object> result = new HashMap<>();
        int saveTaskResult = processService.saveTaskDefine(loginUser, processDefinition.getProjectCode(),
                taskDefinitionLogs, Boolean.TRUE);
        if (saveTaskResult == Constants.EXIT_CODE_SUCCESS) {
            log.info("The task has not changed, so skip");
        }
        if (saveTaskResult == Constants.DEFINITION_FAILURE) {
            log.error("Save task definition error.");
            throw new ServiceException(Status.CREATE_TASK_DEFINITION_ERROR);
        }
        int insertVersion = processService.saveProcessDefine(loginUser, processDefinition, Boolean.TRUE, Boolean.TRUE);
        if (insertVersion == 0) {
            log.error("Save process definition error, processCode:{}.", processDefinition.getCode());
            throw new ServiceException(Status.CREATE_PROCESS_DEFINITION_ERROR);
        } else {
            log.info("Save process definition complete, processCode:{}, processVersion:{}.",
                    processDefinition.getCode(), insertVersion);
        }
        int insertResult = processService.saveTaskRelation(loginUser, processDefinition.getProjectCode(),
                processDefinition.getCode(),
                insertVersion, taskRelationList, taskDefinitionLogs, Boolean.TRUE);
        if (insertResult != Constants.EXIT_CODE_SUCCESS) {
            log.error("Save process task relations error, projectCode:{}, processCode:{}, processVersion:{}.",
                    processDefinition.getProjectCode(), processDefinition.getCode(), insertVersion);
            throw new ServiceException(Status.CREATE_PROCESS_TASK_RELATION_ERROR);
        } else {
            log.info("Save process task relations complete, projectCode:{}, processCode:{}, processVersion:{}.",
                    processDefinition.getProjectCode(), processDefinition.getCode(), insertVersion);
        }

        saveOtherRelation(loginUser, processDefinition, result, otherParamsJson);

        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, processDefinition);
        return result;
    }

    private List<TaskDefinitionLog> generateTaskDefinitionList(String taskDefinitionJson) {
        try {
            List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);
            if (CollectionUtils.isEmpty(taskDefinitionLogs)) {
                log.error("Generate task definition list failed, the given taskDefinitionJson is invalided: {}",
                        taskDefinitionJson);
                throw new ServiceException(Status.DATA_IS_NOT_VALID, taskDefinitionJson);
            }
            for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
                if (!taskPluginManager.checkTaskParameters(ParametersNode.builder()
                        .taskType(taskDefinitionLog.getTaskType())
                        .taskParams(taskDefinitionLog.getTaskParams())
                        .dependence(taskDefinitionLog.getDependence())
                        .build())) {
                    log.error(
                            "Generate task definition list failed, the given task definition parameter is invalided, taskName: {}, taskDefinition: {}",
                            taskDefinitionLog.getName(), taskDefinitionLog);
                    throw new ServiceException(Status.PROCESS_NODE_S_PARAMETER_INVALID, taskDefinitionLog.getName());
                }
            }
            return taskDefinitionLogs;
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Generate task definition list failed, meet an unknown exception", e);
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR);
        }
    }

    private List<ProcessTaskRelationLog> generateTaskRelationList(String taskRelationJson,
                                                                  List<TaskDefinitionLog> taskDefinitionLogs) {
        try {
            List<ProcessTaskRelationLog> taskRelationList =
                    JSONUtils.toList(taskRelationJson, ProcessTaskRelationLog.class);
            if (CollectionUtils.isEmpty(taskRelationList)) {
                log.error("Generate task relation list failed the taskRelation list is empty, taskRelationJson: {}",
                        taskRelationJson);
                throw new ServiceException(Status.DATA_IS_NOT_VALID);
            }
            List<ProcessTaskRelation> processTaskRelations = taskRelationList.stream()
                    .map(processTaskRelationLog -> JSONUtils.parseObject(JSONUtils.toJsonString(processTaskRelationLog),
                            ProcessTaskRelation.class))
                    .collect(Collectors.toList());
            List<TaskNode> taskNodeList = processService.transformTask(processTaskRelations, taskDefinitionLogs);
            if (taskNodeList.size() != taskRelationList.size()) {
                Set<Long> postTaskCodes = taskRelationList.stream().map(ProcessTaskRelationLog::getPostTaskCode)
                        .collect(Collectors.toSet());
                Set<Long> taskNodeCodes = taskNodeList.stream().map(TaskNode::getCode).collect(Collectors.toSet());
                Collection<Long> codes = CollectionUtils.subtract(postTaskCodes, taskNodeCodes);
                if (CollectionUtils.isNotEmpty(codes)) {
                    String taskCodes = StringUtils.join(codes, Constants.COMMA);
                    log.error("Task definitions do not exist, taskCodes:{}.", taskCodes);
                    throw new ServiceException(Status.TASK_DEFINE_NOT_EXIST, taskCodes);
                }
            }
            if (graphHasCycle(taskNodeList)) {
                log.error("Process DAG has cycle.");
                throw new ServiceException(Status.PROCESS_NODE_HAS_CYCLE);
            }

            // check whether the task relation json is normal
            for (ProcessTaskRelationLog processTaskRelationLog : taskRelationList) {
                if (processTaskRelationLog.getPostTaskCode() == 0) {
                    log.error("The post_task_code or post_task_version of processTaskRelationLog can not be zero, " +
                            "processTaskRelationLogId:{}.", processTaskRelationLog.getId());
                    throw new ServiceException(Status.CHECK_PROCESS_TASK_RELATION_ERROR);
                }
            }
            return taskRelationList;
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Check task relation list error, meet an unknown exception, given taskRelationJson: {}",
                    taskRelationJson, e);
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR);
        }
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
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_DEFINITION);
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
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_DEFINITION);
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
    public PageInfo<ProcessDefinition> queryProcessDefinitionListPaging(@NonNull User loginUser,
                                                                        long projectCode,
                                                                        String searchVal,
                                                                        String otherParamsJson,
                                                                        Integer userId,
                                                                        Integer pageNo,
                                                                        Integer pageSize) {
        Project project = projectMapper.queryByCode(projectCode);

        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_DEFINITION);

        PageListingResult<ProcessDefinition> processDefinitionsPageListingResult =
                processDefinitionDao.listingProcessDefinition(
                        pageNo, pageSize, searchVal, userId, projectCode);
        List<ProcessDefinition> processDefinitions = processDefinitionsPageListingResult.getRecords();

        List<Long> processDefinitionCodes =
                processDefinitions.stream().map(ProcessDefinition::getCode).collect(Collectors.toList());
        Map<Long, Schedule> scheduleMap = schedulerService.queryScheduleByProcessDefinitionCodes(processDefinitionCodes)
                .stream()
                .collect(Collectors.toMap(Schedule::getProcessDefinitionCode, Function.identity()));
        List<UserWithProcessDefinitionCode> userWithCodes = userMapper.queryUserWithProcessDefinitionCode(
                processDefinitionCodes);
        for (ProcessDefinition pd : processDefinitions) {
            userWithCodes.stream()
                    .filter(userWithCode -> userWithCode.getProcessDefinitionCode() == pd.getCode()
                            && userWithCode.getProcessDefinitionVersion() == pd.getVersion())
                    .findAny().ifPresent(userWithCode -> {
                        pd.setModifyBy(userWithCode.getModifierName());
                        pd.setUserName(userWithCode.getCreatorName());
                    });
            Schedule schedule = scheduleMap.get(pd.getCode());
            pd.setScheduleReleaseState(schedule == null ? null : schedule.getReleaseState());
        }

        PageInfo<ProcessDefinition> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) processDefinitionsPageListingResult.getTotalCount());
        pageInfo.setTotalList(processDefinitions);

        return pageInfo;
    }

    /**
     * Filter resource process definitions
     *
     * @param loginUser login user
     * @param workflowFilterRequest workflow filter requests
     * @return List process definition
     */
    @Override
    public PageInfo<ProcessDefinition> filterProcessDefinition(User loginUser,
                                                               WorkflowFilterRequest workflowFilterRequest) {
        ProcessDefinition processDefinition = workflowFilterRequest.convert2ProcessDefinition();
        if (workflowFilterRequest.getProjectName() != null) {
            Project project = projectMapper.queryByName(workflowFilterRequest.getProjectName());
            // check user access for project
            projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_DEFINITION);
            processDefinition.setProjectCode(project.getCode());
        }

        Page<ProcessDefinition> page =
                new Page<>(workflowFilterRequest.getPageNo(), workflowFilterRequest.getPageSize());
        IPage<ProcessDefinition> processDefinitionIPage =
                processDefinitionMapper.filterProcessDefinition(page, processDefinition);

        List<ProcessDefinition> records = processDefinitionIPage.getRecords();
        for (ProcessDefinition pd : records) {
            ProcessDefinitionLog processDefinitionLog =
                    processDefinitionLogMapper.queryByDefinitionCodeAndVersion(pd.getCode(), pd.getVersion());
            User user = userMapper.selectById(processDefinitionLog.getOperator());
            pd.setModifyBy(user.getUserName());
        }

        processDefinitionIPage.setRecords(records);
        PageInfo<ProcessDefinition> pageInfo =
                new PageInfo<>(workflowFilterRequest.getPageNo(), workflowFilterRequest.getPageSize());
        pageInfo.setTotal((int) processDefinitionIPage.getTotal());
        pageInfo.setTotalList(processDefinitionIPage.getRecords());

        return pageInfo;
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
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_DEFINITION);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            log.error("Process definition does not exist, processCode:{}.", code);
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(code));
        } else {
            DagData dagData = processService.genDagData(processDefinition);
            result.put(Constants.DATA_LIST, dagData);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * query detail of process definition
     *
     * @param loginUser login user
     * @param code process definition code
     * @return process definition detail
     */
    @Override
    public ProcessDefinition getProcessDefinition(User loginUser, long code) {
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);
        if (processDefinition == null) {
            throw new ServiceException(Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(code));
        }

        Project project = projectMapper.queryByCode(processDefinition.getProjectCode());
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_DEFINITION);

        return processDefinition;
    }

    @Override
    public Optional<ProcessDefinition> queryWorkflowDefinition(long workflowDefinitionCode,
                                                               int workflowDefinitionVersion) {
        ProcessDefinition workflowDefinition = processDefinitionDao.queryByCode(workflowDefinitionCode).orElse(null);
        if (workflowDefinition == null || workflowDefinition.getVersion() != workflowDefinitionVersion) {
            workflowDefinition = processDefinitionLogDao.queryByDefinitionCodeAndVersion(workflowDefinitionCode,
                    workflowDefinitionVersion);
        }
        return Optional.ofNullable(workflowDefinition);
    }

    @Override
    public ProcessDefinition queryWorkflowDefinitionThrowExceptionIfNotFound(long workflowDefinitionCode,
                                                                             int workflowDefinitionVersion) {
        return queryWorkflowDefinition(workflowDefinitionCode, workflowDefinitionVersion)
                .orElseThrow(() -> new ServiceException(Status.PROCESS_DEFINE_NOT_EXIST,
                        String.valueOf(workflowDefinitionCode)));
    }

    @Override
    public Map<String, Object> queryProcessDefinitionByName(User loginUser, long projectCode, String name) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_DEFINITION);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByDefineName(projectCode, name);

        if (processDefinition == null) {
            log.error("Process definition does not exist, projectCode:{}.", projectCode);
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
     * @param taskRelationJson relation json for nodes
     * @param taskDefinitionJson taskDefinitionJson
     * @param otherParamsJson otherParamsJson handle other params
     * @return update result code
     */
    @Override
    @Transactional
    public Map<String, Object> updateProcessDefinition(User loginUser,
                                                       long projectCode,
                                                       String name,
                                                       long code,
                                                       String description,
                                                       String globalParams,
                                                       String locations,
                                                       int timeout,
                                                       String taskRelationJson,
                                                       String taskDefinitionJson,
                                                       String otherParamsJson,
                                                       ProcessExecutionTypeEnum executionType) {
        Project project = projectMapper.queryByCode(projectCode);
        // check if user have write perm for project
        Map<String, Object> result = new HashMap<>();
        boolean hasProjectAndWritePerm = projectService.hasProjectAndWritePerm(loginUser, project, result);
        if (!hasProjectAndWritePerm) {
            return result;
        }

        if (checkDescriptionLength(description)) {
            log.warn("Parameter description is too long.");
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }
        List<TaskDefinitionLog> taskDefinitionLogs = generateTaskDefinitionList(taskDefinitionJson);
        List<ProcessTaskRelationLog> taskRelationList = generateTaskRelationList(taskRelationJson, taskDefinitionLogs);

        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);
        // check process definition exists
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            log.error("Process definition does not exist, processCode:{}.", code);
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(code));
            return result;
        }
        if (processDefinition.getReleaseState() == ReleaseState.ONLINE) {
            // online can not permit edit
            log.warn("Process definition is not allowed to be modified due to {}, processDefinitionCode:{}.",
                    ReleaseState.ONLINE.getDescp(), processDefinition.getCode());
            putMsg(result, Status.PROCESS_DEFINE_NOT_ALLOWED_EDIT, processDefinition.getName());
            return result;
        }
        if (!name.equals(processDefinition.getName())) {
            // check whether the new process define name exist
            ProcessDefinition definition = processDefinitionMapper.verifyByDefineName(project.getCode(), name);
            if (definition != null) {
                log.warn("Process definition with the same name already exists, processDefinitionCode:{}.",
                        definition.getCode());
                putMsg(result, Status.PROCESS_DEFINITION_NAME_EXIST, name);
                return result;
            }
        }
        ProcessDefinition processDefinitionDeepCopy =
                JSONUtils.parseObject(JSONUtils.toJsonString(processDefinition), ProcessDefinition.class);
        processDefinition.set(projectCode, name, description, globalParams, locations, timeout);
        processDefinition.setExecutionType(executionType);
        return updateDagDefine(loginUser, taskRelationList, processDefinition, processDefinitionDeepCopy,
                taskDefinitionLogs, otherParamsJson);
    }

    /**
     * Task want to delete whether used in other task, should throw exception when have be used.
     *
     * This function avoid delete task already dependencies by other tasks by accident.
     *
     * @param processDefinition ProcessDefinition you change task definition and task relation
     * @param taskRelationList All the latest task relation list from process definition
     */
    private void taskUsedInOtherTaskValid(ProcessDefinition processDefinition,
                                          List<ProcessTaskRelationLog> taskRelationList) {
        List<ProcessTaskRelation> oldProcessTaskRelationList = processTaskRelationMapper
                .queryByProcessCode(processDefinition.getProjectCode(), processDefinition.getCode());
        Set<ProcessTaskRelationLog> oldProcessTaskRelationSet =
                oldProcessTaskRelationList.stream().map(ProcessTaskRelationLog::new).collect(Collectors.toSet());
        StringBuilder sb = new StringBuilder();
        for (ProcessTaskRelationLog oldProcessTaskRelation : oldProcessTaskRelationSet) {
            boolean oldTaskExists = taskRelationList.stream()
                    .anyMatch(relation -> oldProcessTaskRelation.getPostTaskCode() == relation.getPostTaskCode());
            if (!oldTaskExists) {
                Optional<String> taskDepMsg = workFlowLineageService.taskDepOnTaskMsg(
                        processDefinition.getProjectCode(), oldProcessTaskRelation.getProcessDefinitionCode(),
                        oldProcessTaskRelation.getPostTaskCode());
                taskDepMsg.ifPresent(sb::append);
            }
            if (sb.length() != 0) {
                log.error("Task cannot be deleted because it is dependent");
                throw new ServiceException(sb.toString());
            }
        }
    }

    protected Map<String, Object> updateDagDefine(User loginUser,
                                                  List<ProcessTaskRelationLog> taskRelationList,
                                                  ProcessDefinition processDefinition,
                                                  ProcessDefinition processDefinitionDeepCopy,
                                                  List<TaskDefinitionLog> taskDefinitionLogs,
                                                  String otherParamsJson) {
        Map<String, Object> result = new HashMap<>();
        int saveTaskResult = processService.saveTaskDefine(loginUser, processDefinition.getProjectCode(),
                taskDefinitionLogs, Boolean.TRUE);
        if (saveTaskResult == Constants.EXIT_CODE_SUCCESS) {
            log.info("The task has not changed, so skip");
        }
        if (saveTaskResult == Constants.DEFINITION_FAILURE) {
            log.error("Update task definitions error, projectCode:{}, processCode:{}.",
                    processDefinition.getProjectCode(), processDefinition.getCode());
            putMsg(result, Status.UPDATE_TASK_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_TASK_DEFINITION_ERROR);
        }
        boolean isChange = false;
        if (processDefinition.equals(processDefinitionDeepCopy) && saveTaskResult == Constants.EXIT_CODE_SUCCESS) {
            List<ProcessTaskRelationLog> processTaskRelationLogList = processTaskRelationLogMapper
                    .queryByProcessCodeAndVersion(processDefinition.getCode(), processDefinition.getVersion());
            if (taskRelationList.size() == processTaskRelationLogList.size()) {
                Set<ProcessTaskRelationLog> taskRelationSet = new HashSet<>(taskRelationList);
                Set<ProcessTaskRelationLog> processTaskRelationLogSet = new HashSet<>(processTaskRelationLogList);
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
            log.info("Process definition needs to be updated, projectCode:{}, processCode:{}, processVersion:{}.",
                    processDefinition.getProjectCode(), processDefinition.getCode(), processDefinition.getVersion());
            processDefinition.setUpdateTime(new Date());
            int insertVersion =
                    processService.saveProcessDefine(loginUser, processDefinition, Boolean.TRUE, Boolean.TRUE);
            if (insertVersion <= 0) {
                log.error("Update process definition error, processCode:{}.", processDefinition.getCode());
                putMsg(result, Status.UPDATE_PROCESS_DEFINITION_ERROR);
                throw new ServiceException(Status.UPDATE_PROCESS_DEFINITION_ERROR);
            } else {
                log.info("Update process definition complete, processCode:{}, processVersion:{}.",
                        processDefinition.getCode(), insertVersion);
            }

            taskUsedInOtherTaskValid(processDefinition, taskRelationList);
            int insertResult = processService.saveTaskRelation(loginUser, processDefinition.getProjectCode(),
                    processDefinition.getCode(), insertVersion, taskRelationList, taskDefinitionLogs, Boolean.TRUE);
            if (insertResult == Constants.EXIT_CODE_SUCCESS) {
                log.info(
                        "Update process task relations complete, projectCode:{}, processCode:{}, processVersion:{}.",
                        processDefinition.getProjectCode(), processDefinition.getCode(), insertVersion);
                putMsg(result, Status.SUCCESS);
                result.put(Constants.DATA_LIST, processDefinition);
            } else {
                log.error("Update process task relations error, projectCode:{}, processCode:{}, processVersion:{}.",
                        processDefinition.getProjectCode(), processDefinition.getCode(), insertVersion);
                putMsg(result, Status.UPDATE_PROCESS_DEFINITION_ERROR);
                throw new ServiceException(Status.UPDATE_PROCESS_DEFINITION_ERROR);
            }
            saveOtherRelation(loginUser, processDefinition, result, otherParamsJson);
        } else {
            log.info(
                    "Process definition does not need to be updated because there is no change, projectCode:{}, processCode:{}, processVersion:{}.",
                    processDefinition.getProjectCode(), processDefinition.getCode(), processDefinition.getVersion());
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
    public Map<String, Object> verifyProcessDefinitionName(User loginUser, long projectCode, String name,
                                                           long processDefinitionCode) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_CREATE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessDefinition processDefinition =
                processDefinitionMapper.verifyByDefineName(project.getCode(), name.trim());
        if (processDefinition == null) {
            putMsg(result, Status.SUCCESS);
            return result;
        }
        if (processDefinitionCode != 0 && processDefinitionCode == processDefinition.getCode()) {
            putMsg(result, Status.SUCCESS);
            return result;
        }
        log.warn("Process definition with the same name {} already exists, processDefinitionCode:{}.",
                processDefinition.getName(), processDefinition.getCode());
        putMsg(result, Status.PROCESS_DEFINITION_NAME_EXIST, name.trim());
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> batchDeleteProcessDefinitionByCodes(User loginUser, long projectCode, String codes) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isEmpty(codes)) {
            log.error("Parameter processDefinitionCodes is empty, projectCode is {}.", projectCode);
            putMsg(result, Status.PROCESS_DEFINITION_CODES_IS_EMPTY);
            return result;
        }

        Set<Long> definitionCodes = Lists.newArrayList(codes.split(Constants.COMMA)).stream().map(Long::parseLong)
                .collect(Collectors.toSet());
        List<ProcessDefinition> processDefinitionList = processDefinitionMapper.queryByCodes(definitionCodes);
        Set<Long> queryCodes =
                processDefinitionList.stream().map(ProcessDefinition::getCode).collect(Collectors.toSet());
        // definitionCodes - queryCodes
        Set<Long> diffCode =
                definitionCodes.stream().filter(code -> !queryCodes.contains(code)).collect(Collectors.toSet());

        if (CollectionUtils.isNotEmpty(diffCode)) {
            log.error("Process definition does not exist, processCodes:{}.",
                    diffCode.stream().map(String::valueOf).collect(Collectors.joining(Constants.COMMA)));
            throw new ServiceException(Status.BATCH_DELETE_PROCESS_DEFINE_BY_CODES_ERROR,
                    diffCode.stream().map(code -> code + "[process definition not exist]")
                            .collect(Collectors.joining(Constants.COMMA)));
        }

        for (ProcessDefinition process : processDefinitionList) {
            try {
                this.deleteProcessDefinitionByCode(loginUser, process.getCode());
                metricsCleanUpService.cleanUpWorkflowMetricsByDefinitionCode(process.getCode());
            } catch (Exception e) {
                throw new ServiceException(Status.DELETE_PROCESS_DEFINE_ERROR, process.getName(), e.getMessage());
            }
        }
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * Process definition want to delete whether used in other task, should throw exception when have be used.
     *
     * This function avoid delete process definition already dependencies by other tasks by accident.
     *
     * @param processDefinition ProcessDefinition you change task definition and task relation
     */
    private void processDefinitionUsedInOtherTaskValid(ProcessDefinition processDefinition) {
        // check process definition is already online
        if (processDefinition.getReleaseState() == ReleaseState.ONLINE) {
            throw new ServiceException(Status.PROCESS_DEFINE_STATE_ONLINE, processDefinition.getName());
        }

        // check process instances is already running
        List<ProcessInstance> processInstances = processInstanceService
                .queryByProcessDefineCodeAndStatus(processDefinition.getCode(),
                        org.apache.dolphinscheduler.service.utils.Constants.NOT_TERMINATED_STATES);
        if (CollectionUtils.isNotEmpty(processInstances)) {
            throw new ServiceException(Status.DELETE_PROCESS_DEFINITION_EXECUTING_FAIL, processInstances.size());
        }

        // check process used by other task, including subprocess and dependent task type
        Set<TaskMainInfo> taskDepOnProcess = workFlowLineageService
                .queryTaskDepOnProcess(processDefinition.getProjectCode(), processDefinition.getCode());
        if (CollectionUtils.isNotEmpty(taskDepOnProcess)) {
            String taskDepDetail = taskDepOnProcess.stream()
                    .map(task -> String.format(Constants.FORMAT_S_S_COLON, task.getProcessDefinitionName(),
                            task.getTaskName()))
                    .collect(Collectors.joining(Constants.COMMA));
            throw new ServiceException(Status.DELETE_PROCESS_DEFINITION_USE_BY_OTHER_FAIL, taskDepDetail);
        }
    }

    public void deleteProcessDefinitionByCode(User loginUser, long code) {
        ProcessDefinition processDefinition = processDefinitionDao.queryByCode(code)
                .orElseThrow(() -> new ServiceException(PROCESS_DEFINE_NOT_EXIST, String.valueOf(code)));

        Project project = projectMapper.queryByCode(processDefinition.getProjectCode());
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_DEFINITION_DELETE);

        // Determine if the login user is the owner of the process definition
        if (loginUser.getId() != processDefinition.getUserId() && loginUser.getUserType() != UserType.ADMIN_USER) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        processDefinitionUsedInOtherTaskValid(processDefinition);

        // get the timing according to the process definition
        Schedule scheduleObj = scheduleMapper.queryByProcessDefinitionCode(code);
        if (scheduleObj != null) {
            if (scheduleObj.getReleaseState() == ReleaseState.OFFLINE) {
                int delete = scheduleMapper.deleteById(scheduleObj.getId());
                if (delete == 0) {
                    throw new ServiceException(Status.DELETE_SCHEDULE_BY_ID_ERROR);
                }
            }
            if (scheduleObj.getReleaseState() == ReleaseState.ONLINE) {
                throw new ServiceException(Status.SCHEDULE_STATE_ONLINE, scheduleObj.getId());
            }
        }

        // delete workflow instance, will delete workflow instance, sub workflow instance, task instance, alert
        processInstanceService.deleteProcessInstanceByWorkflowDefinitionCode(processDefinition.getCode());
        // delete task definition
        taskDefinitionService.deleteTaskByWorkflowDefinitionCode(processDefinition.getCode(),
                processDefinition.getVersion());
        // delete task definition log
        taskDefinitionLogService.deleteTaskByWorkflowDefinitionCode(processDefinition.getCode());
        // delete workflow definition log
        processDefinitionLogDao.deleteByWorkflowDefinitionCode(processDefinition.getCode());
        deleteOtherRelation(project, new HashMap<>(), processDefinition);

        // we delete the workflow definition at last to avoid using transaction here.
        // If delete error, we can call this interface again.
        processDefinitionDao.deleteByWorkflowDefinitionCode(processDefinition.getCode());
        metricsCleanUpService.cleanUpWorkflowMetricsByDefinitionCode(code);
        log.info("Success delete workflow definition workflowDefinitionCode: {}", code);
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
    @Transactional
    public Map<String, Object> releaseProcessDefinition(User loginUser, long projectCode, long code,
                                                        ReleaseState releaseState) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_ONLINE_OFFLINE);
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
            log.error("Process definition does not exist, processDefinitionCode:{}.", code);
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(code));
            return result;
        }
        switch (releaseState) {
            case ONLINE:
                List<ProcessTaskRelation> relationList =
                        processService.findRelationByCode(code, processDefinition.getVersion());
                if (CollectionUtils.isEmpty(relationList)) {
                    log.warn("Process definition has no task relation, processDefinitionCode:{}.", code);
                    putMsg(result, Status.PROCESS_DAG_IS_EMPTY);
                    return result;
                }
                processDefinition.setReleaseState(releaseState);
                processDefinitionMapper.updateById(processDefinition);
                log.info("Set process definition online, projectCode:{}, processDefinitionCode:{}.", projectCode,
                        code);
                break;
            case OFFLINE:
                processDefinition.setReleaseState(releaseState);
                int updateProcess = processDefinitionMapper.updateById(processDefinition);
                Schedule schedule = scheduleMapper.queryByProcessDefinitionCode(code);
                if (updateProcess > 0) {
                    log.info("Set process definition offline, projectCode:{}, processDefinitionCode:{}.",
                            projectCode, code);
                    if (schedule != null) {
                        // set status
                        schedule.setReleaseState(releaseState);
                        int updateSchedule = scheduleMapper.updateById(schedule);
                        if (updateSchedule == 0) {
                            log.error(
                                    "Set schedule offline error, projectCode:{}, processDefinitionCode:{}, scheduleId:{}",
                                    projectCode, code, schedule.getId());
                            putMsg(result, Status.OFFLINE_SCHEDULE_ERROR);
                            throw new ServiceException(Status.OFFLINE_SCHEDULE_ERROR);
                        } else {
                            log.info("Set schedule offline, projectCode:{}, processDefinitionCode:{}, scheduleId:{}",
                                    projectCode, code, schedule.getId());
                        }
                        schedulerService.deleteSchedule(project.getId(), schedule.getId());
                    }
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
    public void batchExportProcessDefinitionByCodes(User loginUser, long projectCode, String codes,
                                                    HttpServletResponse response) {
        if (StringUtils.isEmpty(codes)) {
            log.warn("Process definition codes to be exported is empty.");
            return;
        }
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_DEFINITION_EXPORT);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return;
        }
        Set<Long> defineCodeSet = Lists.newArrayList(codes.split(Constants.COMMA)).stream().map(Long::parseLong)
                .collect(Collectors.toSet());
        List<ProcessDefinition> processDefinitionList = processDefinitionMapper.queryByCodes(defineCodeSet);
        if (CollectionUtils.isEmpty(processDefinitionList)) {
            log.error("Process definitions to be exported do not exist, processDefinitionCodes:{}.", defineCodeSet);
            return;
        }
        // check processDefinition exist in project
        List<ProcessDefinition> processDefinitionListInProject = processDefinitionList.stream()
                .filter(o -> projectCode == o.getProjectCode()).collect(Collectors.toList());
        List<DagDataSchedule> dagDataSchedules =
                processDefinitionListInProject.stream().map(this::exportProcessDagData).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(dagDataSchedules)) {
            log.info("Start download process definition file, processDefinitionCodes:{}.", defineCodeSet);
            downloadProcessDefinitionFile(response, dagDataSchedules);
        } else {
            log.error("There is no exported process dag data.");
        }
    }

    /**
     * download the process definition file
     */
    protected void downloadProcessDefinitionFile(HttpServletResponse response, List<DagDataSchedule> dagDataSchedules) {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        BufferedOutputStream buff = null;
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            buff = new BufferedOutputStream(out);
            buff.write(JSONUtils.toPrettyJsonString(dagDataSchedules).getBytes(StandardCharsets.UTF_8));
            buff.flush();
            buff.close();
        } catch (IOException e) {
            log.warn("Export process definition fail", e);
        } finally {
            if (null != buff) {
                try {
                    buff.close();
                } catch (Exception e) {
                    log.warn("Buffer does not close", e);
                }
            }
            if (null != out) {
                try {
                    out.close();
                } catch (Exception e) {
                    log.warn("Output stream does not close", e);
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
    @Transactional
    public Map<String, Object> importProcessDefinition(User loginUser, long projectCode, MultipartFile file) {
        Map<String, Object> result;
        String dagDataScheduleJson = FileUtils.file2String(file);
        List<DagDataSchedule> dagDataScheduleList = JSONUtils.toList(dagDataScheduleJson, DagDataSchedule.class);
        Project project = projectMapper.queryByCode(projectCode);
        result = projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_IMPORT);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        // check file content
        if (CollectionUtils.isEmpty(dagDataScheduleList)) {
            log.warn("Process definition file content is empty.");
            putMsg(result, Status.DATA_IS_NULL, "fileContent");
            return result;
        }
        for (DagDataSchedule dagDataSchedule : dagDataScheduleList) {
            if (!checkAndImport(loginUser, projectCode, result, dagDataSchedule, EMPTY_STRING)) {
                return result;
            }
        }
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> importSqlProcessDefinition(User loginUser, long projectCode, MultipartFile file) {
        Map<String, Object> result;
        Project project = projectMapper.queryByCode(projectCode);
        result = projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_IMPORT);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        String processDefinitionName = file.getOriginalFilename() == null ? file.getName() : file.getOriginalFilename();
        int index = processDefinitionName.lastIndexOf(".");
        if (index > 0) {
            processDefinitionName = processDefinitionName.substring(0, index);
        }
        processDefinitionName = getNewName(processDefinitionName, IMPORT_SUFFIX);

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
        try (
                ZipInputStream zIn = new ZipInputStream(file.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zIn))) {
            // build process definition
            processDefinition = new ProcessDefinition(projectCode,
                    processDefinitionName,
                    CodeGenerateUtils.getInstance().genCode(),
                    "",
                    "[]", null,
                    0, loginUser.getId());

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
                            throw new IllegalStateException(
                                    "Ratio between compressed and uncompressed data is highly suspicious, looks like a Zip Bomb Attack.");
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
                        log.error("Datasource does not found, may be its name is illegal.");
                        putMsg(result, Status.DATASOURCE_NAME_ILLEGAL);
                        return result;
                    }
                    dataSourceCache.put(datasourceName, dataSource);

                    TaskDefinitionLog taskDefinition =
                            buildNormalSqlTaskDefinition(taskName, dataSource, sql.substring(0, sql.length() - 1));

                    taskDefinitionList.add(taskDefinition);
                    taskNameToCode.put(taskDefinition.getName(), taskDefinition.getCode());
                    taskNameToUpstream.put(taskDefinition.getName(), upstreams);
                }

                if (totalSizeArchive > THRESHOLD_SIZE) {
                    throw new IllegalStateException(
                            "the uncompressed data size is too much for the application resource capacity");
                }

                if (totalEntryArchive > THRESHOLD_ENTRIES) {
                    throw new IllegalStateException(
                            "too much entries in this archive, can lead to inodes exhaustion of the system");
                }
            }
        } catch (Exception e) {
            log.error("Import process definition error.", e);
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
                ProcessTaskRelationLog processTaskRelation =
                        buildNormalTaskRelation(taskNameToCode.get(upstream), entry.getValue());
                processTaskRelationList.add(processTaskRelation);
            }
        }

        return createDagDefine(loginUser, processTaskRelationList, processDefinition, taskDefinitionList, EMPTY_STRING);
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
            List<DataSource> dataSources = dataSourceMapper.queryDataSourceByName(datasourceName);
            if (CollectionUtils.isNotEmpty(dataSources)) {
                return dataSources.get(0);
            }
        } else {
            return dataSourceMapper.queryDataSourceByNameAndUserId(loginUser.getId(), datasourceName);
        }
        return null;
    }

    private TaskDefinitionLog buildNormalSqlTaskDefinition(String taskName, DataSource dataSource,
                                                           String sql) throws CodeGenerateException {
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
    protected boolean checkAndImport(User loginUser, long projectCode, Map<String, Object> result,
                                     DagDataSchedule dagDataSchedule, String otherParamsJson) {
        if (!checkImportanceParams(dagDataSchedule, result)) {
            return false;
        }
        ProcessDefinition processDefinition = dagDataSchedule.getProcessDefinition();

        // generate import processDefinitionName
        String processDefinitionName = recursionProcessDefinitionName(projectCode, processDefinition.getName(), 1);
        String importProcessDefinitionName = getNewName(processDefinitionName, IMPORT_SUFFIX);
        // unique check
        Map<String, Object> checkResult =
                verifyProcessDefinitionName(loginUser, projectCode, importProcessDefinitionName, 0);
        if (Status.SUCCESS.equals(checkResult.get(Constants.STATUS))) {
            putMsg(result, Status.SUCCESS);
        } else {
            result.putAll(checkResult);
            return false;
        }
        processDefinition.setName(importProcessDefinitionName);
        processDefinition.setId(null);
        processDefinition.setProjectCode(projectCode);
        processDefinition.setUserId(loginUser.getId());
        try {
            processDefinition.setCode(CodeGenerateUtils.getInstance().genCode());
        } catch (CodeGenerateException e) {
            log.error(
                    "Save process definition error because generate process definition code error, projectCode:{}.",
                    projectCode, e);
            putMsg(result, Status.CREATE_PROCESS_DEFINITION_ERROR);
            return false;
        }
        List<TaskDefinition> taskDefinitionList = dagDataSchedule.getTaskDefinitionList();
        Map<Long, Long> taskCodeMap = new HashMap<>();
        Date now = new Date();
        List<TaskDefinitionLog> taskDefinitionLogList = new ArrayList<>();
        for (TaskDefinition taskDefinition : taskDefinitionList) {
            TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog(taskDefinition);
            taskDefinitionLog.setName(taskDefinitionLog.getName());
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
                log.error("Generate task definition code error, projectCode:{}, processDefinitionCode:{}",
                        projectCode, processDefinition.getCode(), e);
                putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS, "Error generating task definition code");
                return false;
            }
            taskDefinitionLogList.add(taskDefinitionLog);
        }
        int insert = taskDefinitionMapper.batchInsert(taskDefinitionLogList);
        int logInsert = taskDefinitionLogMapper.batchInsert(taskDefinitionLogList);
        if ((logInsert & insert) == 0) {
            log.error("Save task definition error, projectCode:{}, processDefinitionCode:{}", projectCode,
                    processDefinition.getCode());
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
        if (StringUtils.isNotEmpty(processDefinition.getLocations())
                && JSONUtils.checkJsonValid(processDefinition.getLocations())) {
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
        Map<String, Object> createDagResult = createDagDefine(loginUser, taskRelationLogList, processDefinition,
                Lists.newArrayList(), otherParamsJson);
        if (Status.SUCCESS.equals(createDagResult.get(Constants.STATUS))) {
            putMsg(createDagResult, Status.SUCCESS);
        } else {
            result.putAll(createDagResult);
            log.error("Import process definition error, projectCode:{}, processDefinitionCode:{}.", projectCode,
                    processDefinition.getCode());
            throw new ServiceException(Status.IMPORT_PROCESS_DEFINE_ERROR);
        }

        Schedule schedule = dagDataSchedule.getSchedule();
        if (null != schedule) {
            ProcessDefinition newProcessDefinition = processDefinitionMapper.queryByCode(processDefinition.getCode());
            schedule.setProcessDefinitionCode(newProcessDefinition.getCode());
            schedule.setId(null);
            schedule.setUserId(loginUser.getId());
            schedule.setCreateTime(now);
            schedule.setUpdateTime(now);
            int scheduleInsert = scheduleMapper.insert(schedule);
            if (0 == scheduleInsert) {
                log.error(
                        "Import process definition error due to save schedule fail, projectCode:{}, processDefinitionCode:{}.",
                        projectCode, processDefinition.getCode());
                putMsg(result, Status.IMPORT_PROCESS_DEFINE_ERROR);
                throw new ServiceException(Status.IMPORT_PROCESS_DEFINE_ERROR);
            }
        }

        log.info("Import process definition complete, projectCode:{}, processDefinitionCode:{}.", projectCode,
                processDefinition.getCode());
        return true;
    }

    /**
     * check importance params
     */
    private boolean checkImportanceParams(DagDataSchedule dagDataSchedule, Map<String, Object> result) {
        if (dagDataSchedule.getProcessDefinition() == null) {
            log.warn("Process definition is null.");
            putMsg(result, Status.DATA_IS_NULL, "ProcessDefinition");
            return false;
        }
        if (CollectionUtils.isEmpty(dagDataSchedule.getTaskDefinitionList())) {
            log.warn("Task definition list is null.");
            putMsg(result, Status.DATA_IS_NULL, "TaskDefinitionList");
            return false;
        }
        if (CollectionUtils.isEmpty(dagDataSchedule.getProcessTaskRelationList())) {
            log.warn("Process task relation list is null.");
            putMsg(result, Status.DATA_IS_NULL, "ProcessTaskRelationList");
            return false;
        }
        return true;
    }

    private String recursionProcessDefinitionName(long projectCode, String processDefinitionName, int num) {
        ProcessDefinition processDefinition =
                processDefinitionMapper.queryByDefineName(projectCode, processDefinitionName);
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
    public Map<String, Object> checkProcessNodeList(String processTaskRelationJson,
                                                    List<TaskDefinitionLog> taskDefinitionLogsList) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (processTaskRelationJson == null) {
                log.error("Process task relation data is null.");
                putMsg(result, Status.DATA_IS_NOT_VALID, processTaskRelationJson);
                return result;
            }

            List<ProcessTaskRelation> taskRelationList =
                    JSONUtils.toList(processTaskRelationJson, ProcessTaskRelation.class);
            // Check whether the task node is normal
            List<TaskNode> taskNodes = processService.transformTask(taskRelationList, taskDefinitionLogsList);

            if (CollectionUtils.isEmpty(taskNodes)) {
                log.error("Task node data is empty.");
                putMsg(result, Status.PROCESS_DAG_IS_EMPTY);
                return result;
            }

            // check has cycle
            if (graphHasCycle(taskNodes)) {
                log.error("Process DAG has cycle.");
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
                    log.error("Task node {} parameter invalid.", taskNode.getName());
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
            log.error(Status.INTERNAL_SERVER_ERROR_ARGS.getMsg(), e);
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
        // check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode, null);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            log.error("Process definition does not exist, processDefinitionCode:{}.", code);
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
        // check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode, null);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        Set<Long> defineCodeSet = Lists.newArrayList(codes.split(Constants.COMMA)).stream().map(Long::parseLong)
                .collect(Collectors.toSet());
        List<ProcessDefinition> processDefinitionList = processDefinitionMapper.queryByCodes(defineCodeSet);
        if (CollectionUtils.isEmpty(processDefinitionList)) {
            log.error("Process definitions do not exist, codes:{}.", defineCodeSet);
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, codes);
            return result;
        }
        HashMap<Long, Project> userProjects = new HashMap<>(Constants.DEFAULT_HASH_MAP_SIZE);
        projectMapper.queryProjectCreatedAndAuthorizedByUserId(loginUser.getId())
                .forEach(userProject -> userProjects.put(userProject.getCode(), userProject));

        // check processDefinition exist in project
        List<ProcessDefinition> processDefinitionListInProject = processDefinitionList.stream()
                .filter(o -> userProjects.containsKey(o.getProjectCode())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(processDefinitionListInProject)) {
            Set<Long> codesInProject = processDefinitionListInProject.stream()
                    .map(ProcessDefinition::getCode).collect(Collectors.toSet());
            log.error("Process definitions do not exist in project, projectCode:{}, processDefinitionsCodes:{}.",
                    processDefinitionListInProject.get(0).getProjectCode(), codesInProject);
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
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_DEFINITION);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        List<ProcessDefinition> processDefinitions = processDefinitionMapper.queryAllDefinitionList(projectCode);
        List<DagData> dagDataList =
                processDefinitions.stream().map(processService::genDagData).collect(Collectors.toList());
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
        List<DependentSimplifyDefinition> processDefinitions =
                processDefinitionMapper.queryDefinitionListByProjectCodeAndProcessDefinitionCodes(projectCode, null);
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
    public Map<String, Object> queryTaskDefinitionListByProcessDefinitionCode(long projectCode,
                                                                              Long processDefinitionCode) {
        Map<String, Object> result = new HashMap<>();

        Set<Long> definitionCodesSet = new HashSet<>();
        definitionCodesSet.add(processDefinitionCode);
        List<DependentSimplifyDefinition> processDefinitions = processDefinitionMapper
                .queryDefinitionListByProjectCodeAndProcessDefinitionCodes(projectCode, definitionCodesSet);

        // query task definition log
        List<TaskDefinitionLog> taskDefinitionLogsList = taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(
                processDefinitions.get(0).getCode(), processDefinitions.get(0).getVersion());

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
    public Map<String, Object> viewTree(User loginUser, long projectCode, long code, Integer limit) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        result = projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_TREE_VIEW);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);
        if (null == processDefinition || projectCode != processDefinition.getProjectCode()) {
            log.error("Process definition does not exist, code:{}.", code);
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(code));
            return result;
        }
        DAG<Long, TaskNode, TaskNodeRelation> dag = processService.genDagGraph(processDefinition);
        // nodes that are running
        Map<Long, List<TreeViewDto>> runningNodeMap = new ConcurrentHashMap<>();

        // nodes that are waiting to run
        Map<Long, List<TreeViewDto>> waitingRunningNodeMap = new ConcurrentHashMap<>();

        // List of process instances
        List<ProcessInstance> processInstanceList = processInstanceService.queryByProcessDefineCode(code, limit);
        processInstanceList.forEach(processInstance -> processInstance
                .setDuration(DateUtils.format2Duration(processInstance.getStartTime(), processInstance.getEndTime())));
        List<TaskDefinitionLog> taskDefinitionList = taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(
                processDefinition.getCode(), processDefinition.getVersion());
        Map<Long, TaskDefinitionLog> taskDefinitionMap = taskDefinitionList.stream()
                .collect(Collectors.toMap(TaskDefinitionLog::getCode, taskDefinitionLog -> taskDefinitionLog));

        if (limit < 0) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR);
            return result;
        }
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
            parentTreeViewDto.getInstances()
                    .add(new Instance(processInstance.getId(), processInstance.getName(),
                            processInstance.getProcessDefinitionCode(),
                            "", processInstance.getState().name(), processInstance.getStartTime(), endTime,
                            processInstance.getHost(),
                            DateUtils.format2Readable(endTime.getTime() - processInstance.getStartTime().getTime())));
        }

        List<TreeViewDto> parentTreeViewDtoList = new ArrayList<>();
        parentTreeViewDtoList.add(parentTreeViewDto);
        // Here is the encapsulation task instance
        for (Long startNode : dag.getBeginNode()) {
            runningNodeMap.put(startNode, parentTreeViewDtoList);
        }

        while (!ServerLifeCycleManager.isStopped()) {
            Set<Long> postNodeList;
            Iterator<Map.Entry<Long, List<TreeViewDto>>> iter = runningNodeMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Long, List<TreeViewDto>> en = iter.next();
                Long nodeCode = en.getKey();
                parentTreeViewDtoList = en.getValue();

                TreeViewDto treeViewDto = new TreeViewDto();
                TaskNode taskNode = dag.getNode(nodeCode);
                treeViewDto.setType(taskNode.getType());
                treeViewDto.setCode(taskNode.getCode());
                treeViewDto.setName(taskNode.getName());
                // set treeViewDto instances
                for (int i = limit - 1; i >= 0; i--) {
                    ProcessInstance processInstance = processInstanceList.get(i);
                    TaskInstance taskInstance =
                            taskInstanceMapper.queryByInstanceIdAndCode(processInstance.getId(), nodeCode);
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
                        treeViewDto.getInstances().add(new Instance(taskInstance.getId(), taskInstance.getName(),
                                taskInstance.getTaskCode(),
                                taskInstance.getTaskType(), taskInstance.getState().name(),
                                taskInstance.getStartTime(), taskInstance.getEndTime(),
                                taskInstance.getHost(),
                                DateUtils.format2Readable(endTime.getTime() - startTime.getTime()), subProcessCode));
                    }
                }
                for (TreeViewDto pTreeViewDto : parentTreeViewDtoList) {
                    pTreeViewDto.getChildren().add(treeViewDto);
                }
                postNodeList = dag.getSubsequentNodes(nodeCode);
                if (CollectionUtils.isNotEmpty(postNodeList)) {
                    for (Long nextNodeCode : postNodeList) {
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
    @Transactional
    public Map<String, Object> batchCopyProcessDefinition(User loginUser,
                                                          long projectCode,
                                                          String codes,
                                                          long targetProjectCode) {
        Map<String, Object> result = checkParams(loginUser, projectCode, codes, targetProjectCode, WORKFLOW_BATCH_COPY);
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
     * Will be deleted
     * @param loginUser loginUser
     * @param projectCode projectCode
     * @param codes processDefinitionCodes
     * @param targetProjectCode targetProjectCode
     */
    @Override
    @Transactional
    public Map<String, Object> batchMoveProcessDefinition(User loginUser,
                                                          long projectCode,
                                                          String codes,
                                                          long targetProjectCode) {
        Map<String, Object> result =
                checkParams(loginUser, projectCode, codes, targetProjectCode, TASK_DEFINITION_MOVE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        if (projectCode == targetProjectCode) {
            log.warn("Project code is same as target project code, projectCode:{}.", projectCode);
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
                                            long targetProjectCode, String perm) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode, perm);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        if (StringUtils.isEmpty(processDefinitionCodes)) {
            log.error("Parameter processDefinitionCodes is empty, projectCode is {}.", projectCode);
            putMsg(result, Status.PROCESS_DEFINITION_CODES_IS_EMPTY, processDefinitionCodes);
            return result;
        }

        if (projectCode != targetProjectCode) {
            Project targetProject = projectMapper.queryByCode(targetProjectCode);
            // check user access for project
            Map<String, Object> targetResult =
                    projectService.checkProjectAndAuth(loginUser, targetProject, targetProjectCode, perm);
            if (targetResult.get(Constants.STATUS) != Status.SUCCESS) {
                return targetResult;
            }
        }
        return result;
    }

    protected void doBatchOperateProcessDefinition(User loginUser,
                                                   long targetProjectCode,
                                                   List<String> failedProcessList,
                                                   String processDefinitionCodes,
                                                   Map<String, Object> result,
                                                   boolean isCopy) {
        Set<Long> definitionCodes = Arrays.stream(processDefinitionCodes.split(Constants.COMMA)).map(Long::parseLong)
                .collect(Collectors.toSet());
        List<ProcessDefinition> processDefinitionList = processDefinitionMapper.queryByCodes(definitionCodes);
        Set<Long> queryCodes =
                processDefinitionList.stream().map(ProcessDefinition::getCode).collect(Collectors.toSet());
        // definitionCodes - queryCodes
        Set<Long> diffCode =
                definitionCodes.stream().filter(code -> !queryCodes.contains(code)).collect(Collectors.toSet());
        diffCode.forEach(code -> failedProcessList.add(code + "[null]"));
        for (ProcessDefinition processDefinition : processDefinitionList) {
            List<ProcessTaskRelation> processTaskRelations =
                    processTaskRelationMapper.queryByProcessCode(processDefinition.getProjectCode(),
                            processDefinition.getCode());
            List<ProcessTaskRelationLog> taskRelationList =
                    processTaskRelations.stream().map(ProcessTaskRelationLog::new).collect(Collectors.toList());
            processDefinition.setProjectCode(targetProjectCode);
            String otherParamsJson = doOtherOperateProcess(loginUser, processDefinition);
            if (isCopy) {
                log.info("Copy process definition...");
                List<TaskDefinitionLog> taskDefinitionLogs =
                        taskDefinitionLogDao.queryTaskDefineLogList(processTaskRelations);
                Map<Long, Long> taskCodeMap = new HashMap<>();
                for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
                    try {
                        long taskCode = CodeGenerateUtils.getInstance().genCode();
                        taskCodeMap.put(taskDefinitionLog.getCode(), taskCode);
                        taskDefinitionLog.setCode(taskCode);
                    } catch (CodeGenerateException e) {
                        log.error("Generate task definition code error, projectCode:{}.", targetProjectCode, e);
                        putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS);
                        throw new ServiceException(Status.INTERNAL_SERVER_ERROR_ARGS);
                    }
                    taskDefinitionLog.setProjectCode(targetProjectCode);
                    taskDefinitionLog.setVersion(0);
                    taskDefinitionLog.setName(taskDefinitionLog.getName());
                }
                for (ProcessTaskRelationLog processTaskRelationLog : taskRelationList) {
                    if (processTaskRelationLog.getPreTaskCode() > 0) {
                        processTaskRelationLog.setPreTaskCode(taskCodeMap.get(processTaskRelationLog.getPreTaskCode()));
                    }
                    if (processTaskRelationLog.getPostTaskCode() > 0) {
                        processTaskRelationLog
                                .setPostTaskCode(taskCodeMap.get(processTaskRelationLog.getPostTaskCode()));
                    }
                }
                final long oldProcessDefinitionCode = processDefinition.getCode();
                try {
                    processDefinition.setCode(CodeGenerateUtils.getInstance().genCode());
                } catch (CodeGenerateException e) {
                    log.error("Generate process definition code error, projectCode:{}.", targetProjectCode, e);
                    putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS);
                    throw new ServiceException(Status.INTERNAL_SERVER_ERROR_ARGS);
                }
                processDefinition.setId(null);
                processDefinition.setUserId(loginUser.getId());
                processDefinition.setName(getNewName(processDefinition.getName(), COPY_SUFFIX));
                final Date date = new Date();
                processDefinition.setCreateTime(date);
                processDefinition.setUpdateTime(date);
                processDefinition.setReleaseState(ReleaseState.OFFLINE);
                if (StringUtils.isNotBlank(processDefinition.getLocations())) {
                    ArrayNode jsonNodes = JSONUtils.parseArray(processDefinition.getLocations());
                    for (int i = 0; i < jsonNodes.size(); i++) {
                        ObjectNode node = (ObjectNode) jsonNodes.path(i);
                        node.put("taskCode", taskCodeMap.get(node.get("taskCode").asLong()));
                        jsonNodes.set(i, node);
                    }
                    processDefinition.setLocations(JSONUtils.toJsonString(jsonNodes));
                }
                // copy timing configuration
                Schedule scheduleObj = scheduleMapper.queryByProcessDefinitionCode(oldProcessDefinitionCode);
                if (scheduleObj != null) {
                    scheduleObj.setId(null);
                    scheduleObj.setUserId(loginUser.getId());
                    scheduleObj.setProcessDefinitionCode(processDefinition.getCode());
                    scheduleObj.setReleaseState(ReleaseState.OFFLINE);
                    scheduleObj.setCreateTime(date);
                    scheduleObj.setUpdateTime(date);
                    int insertResult = scheduleMapper.insert(scheduleObj);
                    if (insertResult != 1) {
                        log.error("Schedule create error, processDefinitionCode:{}.", processDefinition.getCode());
                        putMsg(result, Status.CREATE_SCHEDULE_ERROR);
                        throw new ServiceException(Status.CREATE_SCHEDULE_ERROR);
                    }
                }
                try {
                    result.putAll(createDagDefine(loginUser, taskRelationList, processDefinition, taskDefinitionLogs,
                            otherParamsJson));
                } catch (Exception e) {
                    log.error("Copy process definition error, processDefinitionCode from {} to {}.",
                            oldProcessDefinitionCode, processDefinition.getCode(), e);
                    putMsg(result, Status.COPY_PROCESS_DEFINITION_ERROR);
                    throw new ServiceException(Status.COPY_PROCESS_DEFINITION_ERROR);
                }
            } else {
                log.info("Move process definition...");
                try {
                    result.putAll(updateDagDefine(loginUser, taskRelationList, processDefinition, null,
                            Lists.newArrayList(), otherParamsJson));
                } catch (Exception e) {
                    log.error("Move process definition error, processDefinitionCode:{}.",
                            processDefinition.getCode(), e);
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
     * get new Task name or Process name when copy or import operate
     * @param originalName Task or Process original name
     * @param suffix "_copy_" or "_import_"
     * @return new name
     */
    public String getNewName(String originalName, String suffix) {
        StringBuilder newName = new StringBuilder();
        String regex = String.format(".*%s\\d{17}$", suffix);
        if (originalName.matches(regex)) {
            // replace timestamp of originalName
            return newName.append(originalName, 0, originalName.lastIndexOf(suffix))
                    .append(suffix)
                    .append(DateUtils.getCurrentTimeStamp())
                    .toString();
        }
        return newName.append(originalName)
                .append(suffix)
                .append(DateUtils.getCurrentTimeStamp())
                .toString();
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
    @Transactional
    public Map<String, Object> switchProcessDefinitionVersion(User loginUser, long projectCode, long code,
                                                              int version) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_SWITCH_TO_THIS_VERSION);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);
        if (Objects.isNull(processDefinition) || projectCode != processDefinition.getProjectCode()) {
            log.error(
                    "Switch process definition error because it does not exist, projectCode:{}, processDefinitionCode:{}.",
                    projectCode, code);
            putMsg(result, Status.SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_ERROR, code);
            return result;
        }

        ProcessDefinitionLog processDefinitionLog =
                processDefinitionLogMapper.queryByDefinitionCodeAndVersion(code, version);
        if (Objects.isNull(processDefinitionLog)) {
            log.error(
                    "Switch process definition error because version does not exist, projectCode:{}, processDefinitionCode:{}, version:{}.",
                    projectCode, code, version);
            putMsg(result, Status.SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_VERSION_ERROR,
                    processDefinition.getCode(), version);
            return result;
        }
        int switchVersion = processService.switchVersion(processDefinition, processDefinitionLog);
        if (switchVersion <= 0) {
            log.error(
                    "Switch process definition version error, projectCode:{}, processDefinitionCode:{}, version:{}.",
                    projectCode, code, version);
            putMsg(result, Status.SWITCH_PROCESS_DEFINITION_VERSION_ERROR);
            throw new ServiceException(Status.SWITCH_PROCESS_DEFINITION_VERSION_ERROR);
        }
        log.info("Switch process definition version complete, projectCode:{}, processDefinitionCode:{}, version:{}.",
                projectCode, code, version);
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
            String failedProcess = String.join(",", failedProcessList);
            if (isCopy) {
                log.error(
                        "Copy process definition error, srcProjectCode:{}, targetProjectCode:{}, failedProcessList:{}.",
                        srcProjectCode, targetProjectCode, failedProcess);
                putMsg(result, Status.COPY_PROCESS_DEFINITION_ERROR, srcProjectCode, targetProjectCode, failedProcess);
            } else {
                log.error(
                        "Move process definition error, srcProjectCode:{}, targetProjectCode:{}, failedProcessList:{}.",
                        srcProjectCode, targetProjectCode, failedProcess);
                putMsg(result, Status.MOVE_PROCESS_DEFINITION_ERROR, srcProjectCode, targetProjectCode, failedProcess);
            }
        } else {
            log.info("Batch {} process definition complete, srcProjectCode:{}, targetProjectCode:{}.",
                    isCopy ? "copy" : "move", srcProjectCode, targetProjectCode);
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
    public Result queryProcessDefinitionVersions(User loginUser, long projectCode, int pageNo, int pageSize,
                                                 long code) {
        Result result = new Result();
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> checkResult =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, VERSION_LIST);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            putMsg(result, resultStatus);
            return result;
        }
        PageInfo<ProcessDefinitionLog> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<ProcessDefinitionLog> page = new Page<>(pageNo, pageSize);
        IPage<ProcessDefinitionLog> processDefinitionVersionsPaging =
                processDefinitionLogMapper.queryProcessDefinitionVersionsPaging(page, code, projectCode);
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
    @Transactional
    public Map<String, Object> deleteProcessDefinitionVersion(User loginUser, long projectCode, long code,
                                                              int version) {
        Project project = projectMapper.queryByCode(projectCode);
        // check if user have write perm for project
        Map<String, Object> result = new HashMap<>();
        boolean hasProjectAndWritePerm = projectService.hasProjectAndWritePerm(loginUser, project, result);
        if (!hasProjectAndWritePerm) {
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);

        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            log.error("Process definition does not exist, code:{}.", code);
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(code));
        } else {
            if (processDefinition.getVersion() == version) {
                log.warn(
                        "Process definition can not be deleted due to version is being used, projectCode:{}, processDefinitionCode:{}, version:{}.",
                        projectCode, code, version);
                putMsg(result, Status.MAIN_TABLE_USING_VERSION);
                return result;
            }
            int deleteLog = processDefinitionLogMapper.deleteByProcessDefinitionCodeAndVersion(code, version);
            int deleteRelationLog = processTaskRelationLogMapper.deleteByCode(code, version);
            if (deleteLog == 0 || deleteRelationLog == 0) {
                log.error(
                        "Delete process definition version error, projectCode:{}, processDefinitionCode:{}, version:{}.",
                        projectCode, code, version);
                putMsg(result, Status.DELETE_PROCESS_DEFINE_BY_CODE_ERROR);
                throw new ServiceException(Status.DELETE_PROCESS_DEFINE_BY_CODE_ERROR);
            }
            deleteOtherRelation(project, result, processDefinition);
            log.info(
                    "Delete process definition version complete, projectCode:{}, processDefinitionCode:{}, version:{}.",
                    projectCode, code, version);
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
     * @param scheduleJson scheduleJson
     * @return process definition code
     */
    @Override
    @Transactional
    public Map<String, Object> createEmptyProcessDefinition(User loginUser,
                                                            long projectCode,
                                                            String name,
                                                            String description,
                                                            String globalParams,
                                                            int timeout,
                                                            String scheduleJson,
                                                            ProcessExecutionTypeEnum executionType) {
        Project project = projectMapper.queryByCode(projectCode);
        // check if user have write perm for project
        Map<String, Object> result = new HashMap<>();
        boolean hasProjectAndWritePerm = projectService.hasProjectAndWritePerm(loginUser, project, result);
        if (!hasProjectAndWritePerm) {
            return result;
        }
        if (checkDescriptionLength(description)) {
            log.warn("Parameter description is too long.");
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }
        // check whether the new process define name exist
        ProcessDefinition definition = processDefinitionMapper.verifyByDefineName(project.getCode(), name);
        if (definition != null) {
            log.warn("Process definition with the same name {} already exists, processDefinitionCode:{}.",
                    definition.getName(), definition.getCode());
            putMsg(result, Status.PROCESS_DEFINITION_NAME_EXIST, name);
            return result;
        }

        long processDefinitionCode;
        try {
            processDefinitionCode = CodeGenerateUtils.getInstance().genCode();
        } catch (CodeGenerateException e) {
            log.error("Generate process definition code error, projectCode:{}.", projectCode, e);
            putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS);
            return result;
        }
        ProcessDefinition processDefinition =
                new ProcessDefinition(projectCode, name, processDefinitionCode, description,
                        globalParams, "", timeout, loginUser.getId());
        processDefinition.setExecutionType(executionType);
        result = createEmptyDagDefine(loginUser, processDefinition);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            log.error("Create empty process definition error.");
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

    protected Map<String, Object> createEmptyDagDefine(User loginUser, ProcessDefinition processDefinition) {
        Map<String, Object> result = new HashMap<>();
        int insertVersion = processService.saveProcessDefine(loginUser, processDefinition, Boolean.TRUE, Boolean.TRUE);
        if (insertVersion == 0) {
            log.error("Save process definition error, processDefinitionCode:{}.", processDefinition.getCode());
            putMsg(result, Status.CREATE_PROCESS_DEFINITION_ERROR);
            throw new ServiceException(Status.CREATE_PROCESS_DEFINITION_ERROR);
        }
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, processDefinition);
        return result;
    }

    protected Map<String, Object> createDagSchedule(User loginUser, ProcessDefinition processDefinition,
                                                    String scheduleJson) {
        Map<String, Object> result = new HashMap<>();
        Schedule scheduleObj = JSONUtils.parseObject(scheduleJson, Schedule.class);
        if (scheduleObj == null) {
            putMsg(result, Status.DATA_IS_NOT_VALID, scheduleJson);
            throw new ServiceException(Status.DATA_IS_NOT_VALID);
        }
        Date now = new Date();
        scheduleObj.setProcessDefinitionCode(processDefinition.getCode());
        if (DateUtils.differSec(scheduleObj.getStartTime(), scheduleObj.getEndTime()) == 0) {
            log.warn("The schedule start time must not be the same as the end, processDefinitionCode:{}.",
                    processDefinition.getCode());
            putMsg(result, Status.SCHEDULE_START_TIME_END_TIME_SAME);
            return result;
        }
        if (!CronUtils.isValidExpression(scheduleObj.getCrontab())) {
            log.error("CronExpression verify failure, cron:{}.", scheduleObj.getCrontab());
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, scheduleObj.getCrontab());
            return result;
        }
        scheduleObj
                .setWarningType(scheduleObj.getWarningType() == null ? WarningType.NONE : scheduleObj.getWarningType());
        scheduleObj.setWarningGroupId(scheduleObj.getWarningGroupId() == 0 ? 1 : scheduleObj.getWarningGroupId());
        scheduleObj.setFailureStrategy(
                scheduleObj.getFailureStrategy() == null ? FailureStrategy.CONTINUE : scheduleObj.getFailureStrategy());
        scheduleObj.setCreateTime(now);
        scheduleObj.setUpdateTime(now);
        scheduleObj.setUserId(loginUser.getId());
        scheduleObj.setReleaseState(ReleaseState.OFFLINE);
        scheduleObj.setProcessInstancePriority(scheduleObj.getProcessInstancePriority() == null ? Priority.MEDIUM
                : scheduleObj.getProcessInstancePriority());
        scheduleObj.setWorkerGroup(scheduleObj.getWorkerGroup() == null ? "default" : scheduleObj.getWorkerGroup());
        scheduleObj
                .setEnvironmentCode(scheduleObj.getEnvironmentCode() == null ? -1 : scheduleObj.getEnvironmentCode());
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
     * @param scheduleJson  scheduleJson
     * @param otherParamsJson otherParamsJson handle other params
     * @param executionType executionType
     * @return update result code
     */
    @Override
    @Transactional
    public Map<String, Object> updateProcessDefinitionBasicInfo(User loginUser,
                                                                long projectCode,
                                                                String name,
                                                                long code,
                                                                String description,
                                                                String globalParams,
                                                                int timeout,
                                                                String scheduleJson,
                                                                String otherParamsJson,
                                                                ProcessExecutionTypeEnum executionType) {
        Project project = projectMapper.queryByCode(projectCode);
        // check if user have write perm for project
        Map<String, Object> result = new HashMap<>();
        boolean hasProjectAndWritePerm = projectService.hasProjectAndWritePerm(loginUser, project, result);
        if (!hasProjectAndWritePerm) {
            return result;
        }
        if (checkDescriptionLength(description)) {
            log.warn("Parameter description is too long.");
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);
        // check process definition exists
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            log.error("Process definition does not exist, code:{}.", code);
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(code));
            return result;
        }
        if (processDefinition.getReleaseState() == ReleaseState.ONLINE) {
            // online can not permit edit
            log.warn("Process definition is not allowed to be modified due to {}, processDefinitionCode:{}.",
                    ReleaseState.ONLINE.getDescp(), processDefinition.getCode());
            putMsg(result, Status.PROCESS_DEFINE_NOT_ALLOWED_EDIT, processDefinition.getName());
            return result;
        }
        if (!name.equals(processDefinition.getName())) {
            // check whether the new process define name exist
            ProcessDefinition definition = processDefinitionMapper.verifyByDefineName(project.getCode(), name);
            if (definition != null) {
                log.warn("Process definition with the same name {} already exists, processDefinitionCode:{}.",
                        definition.getName(), definition.getCode());
                putMsg(result, Status.PROCESS_DEFINITION_NAME_EXIST, name);
                return result;
            }
        }
        ProcessDefinition processDefinitionDeepCopy =
                JSONUtils.parseObject(JSONUtils.toJsonString(processDefinition), ProcessDefinition.class);
        processDefinition.set(projectCode, name, description, globalParams, "", timeout);
        processDefinition.setExecutionType(executionType);
        List<ProcessTaskRelationLog> taskRelationList = processTaskRelationLogMapper
                .queryByProcessCodeAndVersion(processDefinition.getCode(), processDefinition.getVersion());
        result = updateDagDefine(loginUser, taskRelationList, processDefinition, processDefinitionDeepCopy,
                Lists.newArrayList(), otherParamsJson);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            log.error("Update process definition basic info error.");
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

    private void updateWorkflowValid(User user, ProcessDefinition oldProcessDefinition,
                                     ProcessDefinition newProcessDefinition) {
        // online can not permit edit
        if (oldProcessDefinition.getReleaseState() == ReleaseState.ONLINE) {
            throw new ServiceException(Status.PROCESS_DEFINE_NOT_ALLOWED_EDIT, oldProcessDefinition.getName());
        }

        Project project = projectMapper.queryByCode(oldProcessDefinition.getProjectCode());
        // check user access for project
        projectService.checkProjectAndAuthThrowException(user, project, WORKFLOW_UPDATE);

        if (checkDescriptionLength(newProcessDefinition.getDescription())) {
            throw new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR);
        }

        // check whether the new process define name exist
        if (!oldProcessDefinition.getName().equals(newProcessDefinition.getName())) {
            ProcessDefinition definition = processDefinitionMapper
                    .verifyByDefineName(newProcessDefinition.getProjectCode(), newProcessDefinition.getName());
            if (definition != null) {
                throw new ServiceException(Status.PROCESS_DEFINITION_NAME_EXIST, newProcessDefinition.getName());
            }
        }
    }

    /**
     * update single resource workflow
     *
     * @param loginUser     login user
     * @param workflowCode     workflow resource code want to update
     * @param workflowUpdateRequest   workflow update resource object
     * @return Process definition
     */
    @Override
    @Transactional
    public ProcessDefinition updateSingleProcessDefinition(User loginUser,
                                                           long workflowCode,
                                                           WorkflowUpdateRequest workflowUpdateRequest) {
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(workflowCode);
        // check process definition exists
        if (processDefinition == null) {
            throw new ServiceException(Status.PROCESS_DEFINE_NOT_EXIST, workflowCode);
        }

        ProcessDefinition processDefinitionUpdate = workflowUpdateRequest.mergeIntoProcessDefinition(processDefinition);
        this.updateWorkflowValid(loginUser, processDefinition, processDefinitionUpdate);

        int insertVersion = this.saveProcessDefine(loginUser, processDefinitionUpdate);
        if (insertVersion == 0) {
            log.error("Update process definition error, projectCode:{}, processDefinitionName:{}.",
                    processDefinitionUpdate.getCode(),
                    processDefinitionUpdate.getName());
            throw new ServiceException(Status.UPDATE_PROCESS_DEFINITION_ERROR);
        }

        int insertRelationVersion = this.saveTaskRelation(loginUser, processDefinitionUpdate, insertVersion);
        if (insertRelationVersion != Constants.EXIT_CODE_SUCCESS) {
            log.error("Save process task relations error, projectCode:{}, processCode:{}, processVersion:{}.",
                    processDefinition.getProjectCode(), processDefinition.getCode(), insertVersion);
            throw new ServiceException(Status.CREATE_PROCESS_TASK_RELATION_ERROR);
        }
        log.info("Save process task relations complete, projectCode:{}, processCode:{}, processVersion:{}.",
                processDefinition.getProjectCode(), processDefinition.getCode(), insertVersion);
        processDefinitionUpdate.setVersion(insertVersion);
        return processDefinitionUpdate;
    }

    public int saveProcessDefine(User loginUser, ProcessDefinition processDefinition) {
        ProcessDefinitionLog processDefinitionLog = new ProcessDefinitionLog(processDefinition);
        Integer version = processDefinitionLogMapper.queryMaxVersionForDefinition(processDefinition.getCode());
        int insertVersion = version == null || version == 0 ? Constants.VERSION_FIRST : version + 1;
        processDefinitionLog.setVersion(insertVersion);
        processDefinition.setVersion(insertVersion);

        processDefinitionLog.setOperator(loginUser.getId());
        processDefinition.setUserId(loginUser.getId());
        processDefinitionLog.setOperateTime(processDefinition.getUpdateTime());
        processDefinition.setUpdateTime(processDefinition.getUpdateTime());
        processDefinitionLog.setId(null);
        int result = processDefinitionMapper.updateById(processDefinition);

        int insertLog = processDefinitionLogMapper.insert(processDefinitionLog);
        processDefinitionLog.setId(processDefinition.getId());
        return (insertLog & result) > 0 ? insertVersion : 0;
    }

    public int saveTaskRelation(User loginUser, ProcessDefinition processDefinition,
                                int processDefinitionVersion) {
        long projectCode = processDefinition.getProjectCode();
        long processDefinitionCode = processDefinition.getCode();
        List<ProcessTaskRelation> taskRelations =
                processTaskRelationMapper.queryByProcessCode(projectCode, processDefinitionCode);
        List<ProcessTaskRelationLog> taskRelationList =
                taskRelations.stream().map(ProcessTaskRelationLog::new).collect(Collectors.toList());

        List<Long> taskCodeList =
                taskRelations.stream().map(ProcessTaskRelation::getPostTaskCode).collect(Collectors.toList());
        List<TaskDefinition> taskDefinitions = taskDefinitionMapper.queryByCodeList(taskCodeList);
        List<TaskDefinitionLog> taskDefinitionLogs =
                taskDefinitions.stream().map(TaskDefinitionLog::new).collect(Collectors.toList());

        if (taskRelationList.isEmpty()) {
            return Constants.EXIT_CODE_SUCCESS;
        }
        Map<Long, TaskDefinitionLog> taskDefinitionLogMap = null;
        if (CollectionUtils.isNotEmpty(taskDefinitionLogs)) {
            taskDefinitionLogMap = taskDefinitionLogs
                    .stream()
                    .collect(Collectors.toMap(TaskDefinition::getCode, taskDefinitionLog -> taskDefinitionLog));
        }
        Date now = new Date();
        for (ProcessTaskRelationLog processTaskRelationLog : taskRelationList) {
            processTaskRelationLog.setProjectCode(projectCode);
            processTaskRelationLog.setProcessDefinitionCode(processDefinitionCode);
            processTaskRelationLog.setProcessDefinitionVersion(processDefinitionVersion);
            if (taskDefinitionLogMap != null) {
                TaskDefinitionLog preTaskDefinitionLog =
                        taskDefinitionLogMap.get(processTaskRelationLog.getPreTaskCode());
                if (preTaskDefinitionLog != null) {
                    processTaskRelationLog.setPreTaskVersion(preTaskDefinitionLog.getVersion());
                }
                TaskDefinitionLog postTaskDefinitionLog =
                        taskDefinitionLogMap.get(processTaskRelationLog.getPostTaskCode());
                if (postTaskDefinitionLog != null) {
                    processTaskRelationLog.setPostTaskVersion(postTaskDefinitionLog.getVersion());
                }
            }
            processTaskRelationLog.setCreateTime(now);
            processTaskRelationLog.setUpdateTime(now);
            processTaskRelationLog.setOperator(loginUser.getId());
            processTaskRelationLog.setOperateTime(now);
        }
        if (!taskRelations.isEmpty()) {
            Set<Integer> processTaskRelationSet =
                    taskRelations.stream().map(ProcessTaskRelation::hashCode).collect(toSet());
            Set<Integer> taskRelationSet =
                    taskRelationList.stream().map(ProcessTaskRelationLog::hashCode).collect(toSet());
            boolean isSame = CollectionUtils.isEqualCollection(processTaskRelationSet,
                    taskRelationSet);
            if (isSame) {
                log.info("process task relations is non-existent, projectCode:{}, processCode:{}.",
                        processDefinition.getProjectCode(), processDefinition.getCode());
                return Constants.EXIT_CODE_SUCCESS;
            }
            processTaskRelationMapper.deleteByCode(projectCode, processDefinitionCode);
        }
        List<ProcessTaskRelation> processTaskRelations =
                taskRelationList.stream().map(ProcessTaskRelation::new).collect(Collectors.toList());
        int insert = processTaskRelationMapper.batchInsert(processTaskRelations);
        int resultLog = processTaskRelationLogMapper.batchInsert(taskRelationList);
        return (insert & resultLog) > 0 ? Constants.EXIT_CODE_SUCCESS : Constants.EXIT_CODE_FAILURE;
    }

    protected Map<String, Object> updateDagSchedule(User loginUser,
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
        FailureStrategy failureStrategy =
                ObjectUtils.defaultIfNull(schedule.getFailureStrategy(), FailureStrategy.CONTINUE);
        WarningType warningType = ObjectUtils.defaultIfNull(schedule.getWarningType(), WarningType.NONE);
        Priority processInstancePriority =
                ObjectUtils.defaultIfNull(schedule.getProcessInstancePriority(), Priority.MEDIUM);
        int warningGroupId = schedule.getWarningGroupId() == 0 ? 1 : schedule.getWarningGroupId();
        String workerGroup = StringUtils.defaultIfBlank(schedule.getWorkerGroup(), DEFAULT_WORKER_GROUP);
        String tenantCode = StringUtils.defaultIfBlank(schedule.getTenantCode(), Constants.DEFAULT);
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
                tenantCode,
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
    @Transactional
    @Override
    public Map<String, Object> releaseWorkflowAndSchedule(User loginUser, long projectCode, long code,
                                                          ReleaseState releaseState) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_ONLINE_OFFLINE);
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
            log.error("Process definition does not exist, code:{}.", code);
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(code));
            return result;
        }
        Schedule scheduleObj = scheduleMapper.queryByProcessDefinitionCode(code);
        if (scheduleObj == null) {
            log.error("Schedule cron does not exist, processDefinitionCode:{}.", code);
            putMsg(result, Status.SCHEDULE_CRON_NOT_EXISTS, "processDefinitionCode:" + code);
            return result;
        }
        switch (releaseState) {
            case ONLINE:
                List<ProcessTaskRelation> relationList =
                        processService.findRelationByCode(code, processDefinition.getVersion());
                if (CollectionUtils.isEmpty(relationList)) {
                    log.warn("Process definition has no task relation, processDefinitionCode:{}.", code);
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
                    log.info("Set schedule offline, projectCode:{}, processDefinitionCode:{}, scheduleId:{}.",
                            projectCode, code, scheduleObj.getId());
                    // set status
                    scheduleObj.setReleaseState(ReleaseState.OFFLINE);
                    int updateSchedule = scheduleMapper.updateById(scheduleObj);
                    if (updateSchedule == 0) {
                        log.error(
                                "Set schedule offline error, projectCode:{}, processDefinitionCode:{}, scheduleId:{}",
                                projectCode, code, scheduleObj.getId());
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

    /**
     * save other relation
     * @param loginUser
     * @param processDefinition
     * @param result
     * @param otherParamsJson
     */
    @Override
    public void saveOtherRelation(User loginUser, ProcessDefinition processDefinition, Map<String, Object> result,
                                  String otherParamsJson) {

    }

    /**
     * get Json String
     * @param loginUser
     * @param processDefinition
     * @return Json String
     */
    @Override
    public String doOtherOperateProcess(User loginUser, ProcessDefinition processDefinition) {
        return null;
    }

    /**
     * view process variables
     * @param loginUser    login user
     * @param projectCode project code
     * @param code        process definition code
     * @return variables data
     */
    @Override
    public Map<String, Object> viewVariables(User loginUser, long projectCode, long code) {

        Project project = projectMapper.queryByCode(projectCode);

        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_DEFINITION);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(code);

        if (Objects.isNull(processDefinition) || projectCode != processDefinition.getProjectCode()) {
            log.error("Process definition does not exist, projectCode:{}, processDefinitionCode:{}.", projectCode,
                    code);
            putMsg(result, PROCESS_DEFINE_NOT_EXIST, code);
            return result;
        }

        // global params
        List<Property> globalParams = processDefinition.getGlobalParamList();

        Map<String, Map<String, Object>> localUserDefParams = getLocalParams(processDefinition);

        Map<String, Object> resultMap = new HashMap<>();

        if (Objects.nonNull(globalParams)) {
            resultMap.put(GLOBAL_PARAMS, globalParams);
        }

        if (Objects.nonNull(localUserDefParams)) {
            resultMap.put(LOCAL_PARAMS, localUserDefParams);
        }

        result.put(DATA_LIST, resultMap);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * get local params
     */
    private Map<String, Map<String, Object>> getLocalParams(ProcessDefinition processDefinition) {
        Map<String, Map<String, Object>> localUserDefParams = new HashMap<>();

        Set<Long> taskCodeSet = new TreeSet<>();

        processTaskRelationMapper.queryByProcessCode(processDefinition.getProjectCode(), processDefinition.getCode())
                .forEach(processTaskRelation -> {
                    if (processTaskRelation.getPreTaskCode() > 0) {
                        taskCodeSet.add(processTaskRelation.getPreTaskCode());
                    }
                    if (processTaskRelation.getPostTaskCode() > 0) {
                        taskCodeSet.add(processTaskRelation.getPostTaskCode());
                    }
                });

        taskDefinitionMapper.queryByCodeList(taskCodeSet)
                .stream().forEach(taskDefinition -> {
                    Map<String, Object> localParamsMap = new HashMap<>();
                    String localParams = JSONUtils.getNodeString(taskDefinition.getTaskParams(), LOCAL_PARAMS);
                    if (!StringUtils.isEmpty(localParams)) {
                        List<Property> localParamsList = JSONUtils.toList(localParams, Property.class);
                        localParamsMap.put(TASK_TYPE, taskDefinition.getTaskType());
                        localParamsMap.put(LOCAL_PARAMS_LIST, localParamsList);
                        if (CollectionUtils.isNotEmpty(localParamsList)) {
                            localUserDefParams.put(taskDefinition.getName(), localParamsMap);
                        }
                    }
                });

        return localUserDefParams;
    }

    /**
     * delete other relation
     * @param project
     * @param result
     * @param processDefinition
     */
    @Override
    public void deleteOtherRelation(Project project, Map<String, Object> result, ProcessDefinition processDefinition) {

    }

}
