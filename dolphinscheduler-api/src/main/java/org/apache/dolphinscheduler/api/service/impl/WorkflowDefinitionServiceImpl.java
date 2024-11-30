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
import static org.apache.dolphinscheduler.api.enums.Status.WORKFLOW_DEFINITION_NOT_EXIST;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_SUB_WORKFLOW_DEFINITION_CODE;
import static org.apache.dolphinscheduler.common.constants.Constants.COPY_SUFFIX;
import static org.apache.dolphinscheduler.common.constants.Constants.DATA_LIST;
import static org.apache.dolphinscheduler.common.constants.Constants.GLOBAL_PARAMS;
import static org.apache.dolphinscheduler.common.constants.Constants.IMPORT_SUFFIX;
import static org.apache.dolphinscheduler.common.constants.Constants.LOCAL_PARAMS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.LOCAL_PARAMS_LIST;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager.checkTaskParameters;

import org.apache.dolphinscheduler.api.dto.DagDataSchedule;
import org.apache.dolphinscheduler.api.dto.TaskCodeVersionDto;
import org.apache.dolphinscheduler.api.dto.treeview.Instance;
import org.apache.dolphinscheduler.api.dto.treeview.TreeViewDto;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowCreateRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowFilterRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowUpdateRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.MetricsCleanUpService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.service.TaskDefinitionLogService;
import org.apache.dolphinscheduler.api.service.TaskDefinitionService;
import org.apache.dolphinscheduler.api.service.WorkflowDefinitionService;
import org.apache.dolphinscheduler.api.service.WorkflowInstanceService;
import org.apache.dolphinscheduler.api.service.WorkflowLineageService;
import org.apache.dolphinscheduler.api.utils.CheckUtils;
import org.apache.dolphinscheduler.api.utils.FileUtils;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ConditionType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
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
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.UserWithWorkflowDefinitionCode;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskLineage;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationMapper;
import org.apache.dolphinscheduler.dao.model.PageListingResult;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;
import org.apache.dolphinscheduler.dao.utils.WorkerGroupUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.SqlType;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentTaskModel;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SqlParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.process.ProcessService;

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
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
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

@Service
@Slf4j
public class WorkflowDefinitionServiceImpl extends BaseServiceImpl implements WorkflowDefinitionService {

    private static final String RELEASESTATE = "releaseState";

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkflowDefinitionLogMapper workflowDefinitionLogMapper;

    @Autowired
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Autowired
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Autowired
    private WorkflowDefinitionLogDao workflowDefinitionLogDao;
    @Lazy
    @Autowired
    private WorkflowInstanceService workflowInstanceService;

    @Autowired
    private TaskInstanceMapper taskInstanceMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private ProcessService processService;

    @Autowired
    private TaskDefinitionLogDao taskDefinitionLogDao;

    @Autowired
    private WorkflowTaskRelationMapper workflowTaskRelationMapper;

    @Autowired
    private WorkflowTaskRelationLogMapper workflowTaskRelationLogMapper;

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
    private WorkflowLineageService workflowLineageService;

    @Autowired
    private MetricsCleanUpService metricsCleanUpService;

    /**
     * create workflow definition
     *
     * @param loginUser          login user
     * @param projectCode        project code
     * @param name               workflow definition name
     * @param description        description
     * @param globalParams       global params
     * @param locations          locations for nodes
     * @param timeout            timeout
     * @param taskRelationJson   relation json for nodes
     * @param taskDefinitionJson taskDefinitionJson
     * @return create result code
     */
    @Override
    @Transactional
    public Map<String, Object> createWorkflowDefinition(User loginUser,
                                                        long projectCode,
                                                        String name,
                                                        String description,
                                                        String globalParams,
                                                        String locations,
                                                        int timeout,
                                                        String taskRelationJson,
                                                        String taskDefinitionJson,
                                                        String otherParamsJson,
                                                        WorkflowExecutionTypeEnum executionType) {
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
        // check whether the new workflow definition name exist
        WorkflowDefinition definition = workflowDefinitionMapper.verifyByDefineName(project.getCode(), name);
        if (definition != null) {
            log.warn("workflow definition with the same name {} already exists, workflowDefinitionCode:{}.",
                    definition.getName(), definition.getCode());
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NAME_EXIST, name);
        }
        List<TaskDefinitionLog> taskDefinitionLogs = generateTaskDefinitionList(taskDefinitionJson);
        List<WorkflowTaskRelationLog> taskRelationList = generateTaskRelationList(taskRelationJson, taskDefinitionLogs);

        long workflowDefinitionCode = CodeGenerateUtils.genCode();
        WorkflowDefinition workflowDefinition =
                new WorkflowDefinition(projectCode, name, workflowDefinitionCode, description,
                        globalParams, locations, timeout, loginUser.getId());
        workflowDefinition.setExecutionType(executionType);

        result = createDagDefine(loginUser, taskRelationList, workflowDefinition, taskDefinitionLogs);
        return result;
    }

    private void createWorkflowValid(User user, WorkflowDefinition workflowDefinition) {
        Project project = projectMapper.queryByCode(workflowDefinition.getProjectCode());
        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_FOUND, workflowDefinition.getProjectCode());
        }
        // check user access for project
        projectService.checkProjectAndAuthThrowException(user, project, WORKFLOW_CREATE);

        if (checkDescriptionLength(workflowDefinition.getDescription())) {
            throw new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR);
        }

        // check whether the new workflow definition name exist
        WorkflowDefinition definition =
                workflowDefinitionMapper.verifyByDefineName(project.getCode(), workflowDefinition.getName());
        if (definition != null) {
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NAME_EXIST, workflowDefinition.getName());
        }

    }

    private void syncObj2Log(User user, WorkflowDefinition workflowDefinition) {
        WorkflowDefinitionLog workflowDefinitionLog = new WorkflowDefinitionLog(workflowDefinition);
        workflowDefinitionLog.setOperator(user.getId());
        int result = workflowDefinitionLogMapper.insert(workflowDefinitionLog);
        if (result <= 0) {
            throw new ServiceException(Status.CREATE_WORKFLOW_DEFINITION_LOG_ERROR);
        }
    }

    /**
     * create single workflow definition
     *
     * @param loginUser             login user
     * @param workflowCreateRequest the new workflow object will be created
     * @return New WorkflowDefinition object created just now
     */
    @Override
    @Transactional
    public WorkflowDefinition createSingleWorkflowDefinition(User loginUser,
                                                             WorkflowCreateRequest workflowCreateRequest) {
        WorkflowDefinition workflowDefinition = workflowCreateRequest.convert2WorkflowDefinition();
        this.createWorkflowValid(loginUser, workflowDefinition);

        long workflowDefinitionCode;
        try {
            workflowDefinitionCode = CodeGenerateUtils.genCode();
        } catch (CodeGenerateException e) {
            throw new ServiceException(Status.INTERNAL_SERVER_ERROR_ARGS);
        }

        workflowDefinition.setCode(workflowDefinitionCode);
        workflowDefinition.setUserId(loginUser.getId());

        int create = workflowDefinitionMapper.insert(workflowDefinition);
        if (create <= 0) {
            throw new ServiceException(Status.CREATE_WORKFLOW_DEFINITION_ERROR);
        }
        this.syncObj2Log(loginUser, workflowDefinition);
        return workflowDefinition;
    }

    protected Map<String, Object> createDagDefine(User loginUser,
                                                  List<WorkflowTaskRelationLog> taskRelationList,
                                                  WorkflowDefinition workflowDefinition,
                                                  List<TaskDefinitionLog> taskDefinitionLogs) {
        Map<String, Object> result = new HashMap<>();
        int saveTaskResult = processService.saveTaskDefine(loginUser, workflowDefinition.getProjectCode(),
                taskDefinitionLogs, Boolean.TRUE);
        if (saveTaskResult == Constants.EXIT_CODE_SUCCESS) {
            log.info("The task has not changed, so skip");
        }
        if (saveTaskResult == Constants.DEFINITION_FAILURE) {
            log.error("Save task definition error.");
            throw new ServiceException(Status.CREATE_TASK_DEFINITION_ERROR);
        }
        int insertVersion =
                processService.saveWorkflowDefine(loginUser, workflowDefinition, Boolean.TRUE, Boolean.TRUE);
        if (insertVersion == 0) {
            log.error("Save workflow definition error, workflowDefinitionCode:{}.", workflowDefinition.getCode());
            throw new ServiceException(Status.CREATE_WORKFLOW_DEFINITION_ERROR);
        } else {
            log.info("Save workflow definition complete, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                    workflowDefinition.getCode(), insertVersion);
        }
        int insertResult = processService.saveTaskRelation(loginUser, workflowDefinition.getProjectCode(),
                workflowDefinition.getCode(),
                insertVersion, taskRelationList, taskDefinitionLogs, Boolean.TRUE);
        if (insertResult != Constants.EXIT_CODE_SUCCESS) {
            log.error(
                    "Save workflow task relations error, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                    workflowDefinition.getProjectCode(), workflowDefinition.getCode(), insertVersion);
            throw new ServiceException(Status.CREATE_WORKFLOW_TASK_RELATION_ERROR);
        } else {
            log.info(
                    "Save workflow task relations complete, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                    workflowDefinition.getProjectCode(), workflowDefinition.getCode(), insertVersion);
        }

        saveWorkflowLineage(workflowDefinition.getProjectCode(), workflowDefinition.getCode(),
                insertVersion, taskDefinitionLogs);

        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, workflowDefinition);
        return result;
    }

    @Override
    public void saveWorkflowLineage(long projectCode,
                                    long workflowDefinitionCode,
                                    int workflowDefinitionVersion,
                                    List<TaskDefinitionLog> taskDefinitionLogList) {
        List<WorkflowTaskLineage> workflowTaskLineageList =
                generateWorkflowLineageList(taskDefinitionLogList, workflowDefinitionCode, workflowDefinitionVersion);
        if (workflowTaskLineageList.isEmpty()) {
            return;
        }

        int insertWorkflowLineageResult = workflowLineageService.updateWorkflowLineage(workflowTaskLineageList);
        if (insertWorkflowLineageResult <= 0) {
            log.error(
                    "Save workflow lineage error, projectCode: {}, workflowDefinitionCode: {}, workflowDefinitionVersion: {}",
                    projectCode, workflowDefinitionCode, workflowDefinitionVersion);
            throw new ServiceException(Status.CREATE_WORKFLOW_LINEAGE_ERROR);
        } else {
            log.info(
                    "Save workflow lineage complete, projectCode: {}, workflowDefinitionCode: {}, workflowDefinitionVersion: {}",
                    projectCode, workflowDefinitionCode, workflowDefinitionVersion);
        }
    }

    private List<WorkflowTaskLineage> generateWorkflowLineageList(List<TaskDefinitionLog> taskDefinitionLogList,
                                                                  long workflowDefinitionCode,
                                                                  int workflowDefinitionVersion) {
        List<WorkflowTaskLineage> workflowTaskLineageList = new ArrayList<>();
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogList) {
            if (!TaskTypeUtils.isDependentTask(taskDefinitionLog.getTaskType())) {
                continue;
            }

            for (DependentTaskModel dependentTaskModel : JSONUtils
                    .parseObject(taskDefinitionLog.getTaskParams(), DependentParameters.class)
                    .getDependence().getDependTaskList()) {
                for (DependentItem dependentItem : dependentTaskModel.getDependItemList()) {
                    WorkflowTaskLineage workflowTaskLineage = new WorkflowTaskLineage();
                    workflowTaskLineage.setWorkflowDefinitionCode(workflowDefinitionCode);
                    workflowTaskLineage.setWorkflowDefinitionVersion(workflowDefinitionVersion);
                    workflowTaskLineage.setTaskDefinitionCode(taskDefinitionLog.getCode());
                    workflowTaskLineage.setTaskDefinitionVersion(taskDefinitionLog.getVersion());
                    workflowTaskLineage.setDeptProjectCode(taskDefinitionLog.getProjectCode());
                    workflowTaskLineage.setDeptWorkflowDefinitionCode(dependentItem.getDefinitionCode());
                    workflowTaskLineage.setDeptTaskDefinitionCode(dependentItem.getDepTaskCode());
                    workflowTaskLineageList.add(workflowTaskLineage);
                }
            }
        }
        return workflowTaskLineageList;
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
                if (!checkTaskParameters(taskDefinitionLog.getTaskType(), taskDefinitionLog.getTaskParams())) {
                    log.error(
                            "Generate task definition list failed, the given task definition parameter is invalided, taskName: {}, taskDefinition: {}",
                            taskDefinitionLog.getName(), taskDefinitionLog);
                    throw new ServiceException(Status.WORKFLOW_NODE_S_PARAMETER_INVALID, taskDefinitionLog.getName());
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

    private List<WorkflowTaskRelationLog> generateTaskRelationList(String taskRelationJson,
                                                                   List<TaskDefinitionLog> taskDefinitionLogs) {
        try {
            List<WorkflowTaskRelationLog> taskRelationList =
                    JSONUtils.toList(taskRelationJson, WorkflowTaskRelationLog.class);
            if (CollectionUtils.isEmpty(taskRelationList)) {
                log.error("Generate task relation list failed the taskRelation list is empty, taskRelationJson: {}",
                        taskRelationJson);
                throw new ServiceException(Status.DATA_IS_NOT_VALID);
            }
            List<WorkflowTaskRelation> workflowTaskRelations = taskRelationList.stream()
                    .map(workflowTaskRelationLog -> JSONUtils.parseObject(
                            JSONUtils.toJsonString(workflowTaskRelationLog),
                            WorkflowTaskRelation.class))
                    .collect(Collectors.toList());
            List<TaskNode> taskNodeList = processService.transformTask(workflowTaskRelations, taskDefinitionLogs);
            if (taskNodeList.size() != taskRelationList.size()) {
                Set<Long> postTaskCodes = taskRelationList.stream().map(WorkflowTaskRelationLog::getPostTaskCode)
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
                log.error("workflow DAG has cycle.");
                throw new ServiceException(Status.WORKFLOW_NODE_HAS_CYCLE);
            }

            // check whether the task relation json is normal
            for (WorkflowTaskRelationLog workflowTaskRelationLog : taskRelationList) {
                if (workflowTaskRelationLog.getPostTaskCode() == 0) {
                    log.error("The post_task_code or post_task_version of workflowTaskRelationLog can not be zero, " +
                            "workflowTaskRelationLogId:{}.", workflowTaskRelationLog.getId());
                    throw new ServiceException(Status.CHECK_WORKFLOW_TASK_RELATION_ERROR);
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
     * query workflow definition list
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return definition list
     */
    @Override
    public Map<String, Object> queryWorkflowDefinitionList(User loginUser, long projectCode) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_DEFINITION);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        List<WorkflowDefinition> resourceList = workflowDefinitionMapper.queryAllDefinitionList(projectCode);
        List<DagData> dagDataList = resourceList.stream().map(processService::genDagData).collect(Collectors.toList());
        result.put(Constants.DATA_LIST, dagDataList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query workflow definition simple list
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return definition simple list
     */
    @Override
    public Map<String, Object> queryWorkflowDefinitionSimpleList(User loginUser, long projectCode) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_DEFINITION);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        List<WorkflowDefinition> workflowDefinitions = workflowDefinitionMapper.queryAllDefinitionList(projectCode);
        ArrayNode arrayNode = JSONUtils.createArrayNode();
        for (WorkflowDefinition workflowDefinition : workflowDefinitions) {
            ObjectNode workflowDefinitionNode = JSONUtils.createObjectNode();
            workflowDefinitionNode.put("id", workflowDefinition.getId());
            workflowDefinitionNode.put("code", workflowDefinition.getCode());
            workflowDefinitionNode.put("name", workflowDefinition.getName());
            workflowDefinitionNode.put("projectCode", workflowDefinition.getProjectCode());
            arrayNode.add(workflowDefinitionNode);
        }
        result.put(Constants.DATA_LIST, arrayNode);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query workflow definition list paging
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param searchVal   search value
     * @param userId      user id
     * @param pageNo      page number
     * @param pageSize    page size
     * @return workflow definition page
     */
    @Override
    public PageInfo<WorkflowDefinition> queryWorkflowDefinitionListPaging(@NonNull User loginUser,
                                                                          long projectCode,
                                                                          String searchVal,
                                                                          String otherParamsJson,
                                                                          Integer userId,
                                                                          Integer pageNo,
                                                                          Integer pageSize) {

        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, WORKFLOW_DEFINITION);

        PageListingResult<WorkflowDefinition> workflowDefinitionPageListingResult =
                workflowDefinitionDao.listingWorkflowDefinition(
                        pageNo, pageSize, searchVal, userId, projectCode);
        List<WorkflowDefinition> workflowDefinitions = workflowDefinitionPageListingResult.getRecords();

        List<Long> workflowDefinitionCodes =
                workflowDefinitions.stream().map(WorkflowDefinition::getCode).collect(Collectors.toList());
        Map<Long, Schedule> scheduleMap =
                schedulerService.queryScheduleByWorkflowDefinitionCodes(workflowDefinitionCodes)
                        .stream()
                        .collect(Collectors.toMap(Schedule::getWorkflowDefinitionCode, Function.identity()));
        List<UserWithWorkflowDefinitionCode> userWithCodes = userMapper.queryUserWithWorkflowDefinitionCode(
                workflowDefinitionCodes);
        for (WorkflowDefinition pd : workflowDefinitions) {
            userWithCodes.stream()
                    .filter(userWithCode -> userWithCode.getWorkflowDefinitionCode() == pd.getCode()
                            && userWithCode.getWorkflowDefinitionVersion() == pd.getVersion())
                    .findAny().ifPresent(userWithCode -> {
                        pd.setModifyBy(userWithCode.getModifierName());
                        pd.setUserName(userWithCode.getCreatorName());
                    });
            Schedule schedule = scheduleMap.get(pd.getCode());
            pd.setScheduleReleaseState(schedule == null ? null : schedule.getReleaseState());
            pd.setSchedule(schedule);
        }

        PageInfo<WorkflowDefinition> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) workflowDefinitionPageListingResult.getTotalCount());
        pageInfo.setTotalList(workflowDefinitions);

        return pageInfo;
    }

    /**
     * Filter resource workflow definitions
     *
     * @param loginUser             login user
     * @param workflowFilterRequest workflow filter requests
     * @return List workflow definition
     */
    @Override
    public PageInfo<WorkflowDefinition> filterWorkflowDefinition(User loginUser,
                                                                 WorkflowFilterRequest workflowFilterRequest) {
        WorkflowDefinition workflowDefinition = workflowFilterRequest.convert2WorkflowDefinition();
        if (workflowFilterRequest.getProjectName() != null) {
            Project project = projectMapper.queryByName(workflowFilterRequest.getProjectName());
            // check user access for project
            projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_DEFINITION);
            workflowDefinition.setProjectCode(project.getCode());
        }

        Page<WorkflowDefinition> page =
                new Page<>(workflowFilterRequest.getPageNo(), workflowFilterRequest.getPageSize());
        IPage<WorkflowDefinition> workflowDefinitionIPage =
                workflowDefinitionMapper.filterWorkflowDefinition(page, workflowDefinition);

        List<WorkflowDefinition> records = workflowDefinitionIPage.getRecords();
        for (WorkflowDefinition pd : records) {
            WorkflowDefinitionLog workflowDefinitionLog =
                    workflowDefinitionLogMapper.queryByDefinitionCodeAndVersion(pd.getCode(), pd.getVersion());
            User user = userMapper.selectById(workflowDefinitionLog.getOperator());
            pd.setModifyBy(user.getUserName());
        }

        workflowDefinitionIPage.setRecords(records);
        PageInfo<WorkflowDefinition> pageInfo =
                new PageInfo<>(workflowFilterRequest.getPageNo(), workflowFilterRequest.getPageSize());
        pageInfo.setTotal((int) workflowDefinitionIPage.getTotal());
        pageInfo.setTotalList(workflowDefinitionIPage.getRecords());

        return pageInfo;
    }

    /**
     * query detail of workflow definition
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param code        workflow definition code
     * @return workflow definition detail
     */
    @Override
    public Map<String, Object> queryWorkflowDefinitionByCode(User loginUser, long projectCode, long code) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_DEFINITION);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(code);
        if (workflowDefinition == null || projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, workflowDefinitionCode:{}.", code);
            putMsg(result, Status.WORKFLOW_DEFINITION_NOT_EXIST, String.valueOf(code));
        } else {
            DagData dagData = processService.genDagData(workflowDefinition);
            result.put(Constants.DATA_LIST, dagData);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * query detail of workflow definition
     *
     * @param loginUser login user
     * @param code      workflow definition code
     * @return workflow definition detail
     */
    @Override
    public WorkflowDefinition getWorkflowDefinition(User loginUser, long code) {
        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(code);
        if (workflowDefinition == null) {
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST, String.valueOf(code));
        }

        Project project = projectMapper.queryByCode(workflowDefinition.getProjectCode());
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_DEFINITION);

        return workflowDefinition;
    }

    @Override
    public Optional<WorkflowDefinition> queryWorkflowDefinition(long workflowDefinitionCode,
                                                                int workflowDefinitionVersion) {
        WorkflowDefinition workflowDefinition = workflowDefinitionDao.queryByCode(workflowDefinitionCode).orElse(null);
        if (workflowDefinition == null || workflowDefinition.getVersion() != workflowDefinitionVersion) {
            workflowDefinition = workflowDefinitionLogDao.queryByDefinitionCodeAndVersion(workflowDefinitionCode,
                    workflowDefinitionVersion);
        }
        return Optional.ofNullable(workflowDefinition);
    }

    @Override
    public WorkflowDefinition queryWorkflowDefinitionThrowExceptionIfNotFound(long workflowDefinitionCode,
                                                                              int workflowDefinitionVersion) {
        return queryWorkflowDefinition(workflowDefinitionCode, workflowDefinitionVersion)
                .orElseThrow(() -> new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST,
                        String.valueOf(workflowDefinitionCode)));
    }

    @Override
    public Map<String, Object> queryWorkflowDefinitionByName(User loginUser, long projectCode, String name) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_DEFINITION);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByDefineName(projectCode, name);

        if (workflowDefinition == null) {
            log.error("workflow definition does not exist, projectCode:{}.", projectCode);
            putMsg(result, Status.WORKFLOW_DEFINITION_NOT_EXIST, name);
        } else {
            DagData dagData = processService.genDagData(workflowDefinition);
            result.put(Constants.DATA_LIST, dagData);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * update workflow definition
     *
     * @param loginUser          login user
     * @param projectCode        project code
     * @param name               workflow definition name
     * @param code               workflow definition code
     * @param description        description
     * @param globalParams       global params
     * @param locations          locations for nodes
     * @param timeout            timeout
     * @param taskRelationJson   relation json for nodes
     * @param taskDefinitionJson taskDefinitionJson
     * @return update result code
     */
    @Override
    @Transactional
    public Map<String, Object> updateWorkflowDefinition(User loginUser,
                                                        long projectCode,
                                                        String name,
                                                        long code,
                                                        String description,
                                                        String globalParams,
                                                        String locations,
                                                        int timeout,
                                                        String taskRelationJson,
                                                        String taskDefinitionJson,
                                                        WorkflowExecutionTypeEnum executionType) {
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
        List<WorkflowTaskRelationLog> taskRelationList = generateTaskRelationList(taskRelationJson, taskDefinitionLogs);

        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(code);
        // check workflow definition exists
        if (workflowDefinition == null || projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, workflowDefinitionCode:{}.", code);
            putMsg(result, Status.WORKFLOW_DEFINITION_NOT_EXIST, String.valueOf(code));
            return result;
        }
        if (workflowDefinition.getReleaseState() == ReleaseState.ONLINE) {
            // online can not permit edit
            log.warn("workflow definition is not allowed to be modified due to {}, workflowDefinitionCode:{}.",
                    ReleaseState.ONLINE.getDescp(), workflowDefinition.getCode());
            putMsg(result, Status.WORKFLOW_DEFINITION_NOT_ALLOWED_EDIT, workflowDefinition.getName());
            return result;
        }
        if (!name.equals(workflowDefinition.getName())) {
            // check whether the new workflow define name exist
            WorkflowDefinition definition = workflowDefinitionMapper.verifyByDefineName(project.getCode(), name);
            if (definition != null) {
                log.warn("workflow definition with the same name already exists, workflowDefinitionCode:{}.",
                        definition.getCode());
                putMsg(result, Status.WORKFLOW_DEFINITION_NAME_EXIST, name);
                return result;
            }
        }
        WorkflowDefinition workflowDefinitionDeepCopy =
                JSONUtils.parseObject(JSONUtils.toJsonString(workflowDefinition), WorkflowDefinition.class);
        workflowDefinition.set(projectCode, name, description, globalParams, locations, timeout);
        workflowDefinition.setExecutionType(executionType);
        result = updateDagDefine(loginUser, taskRelationList, workflowDefinition, workflowDefinitionDeepCopy,
                taskDefinitionLogs);
        return result;
    }

    /**
     * Task want to delete whether used in other task, should throw exception when have be used.
     * <p>
     * This function avoid delete task already dependencies by other tasks by accident.
     *
     * @param workflowDefinition WorkflowDefinition you change task definition and task relation
     * @param taskRelationList  All the latest task relation list from workflow definition
     */
    private void taskUsedInOtherTaskValid(WorkflowDefinition workflowDefinition,
                                          List<WorkflowTaskRelationLog> taskRelationList) {
        List<WorkflowTaskRelation> oldWorkflowTaskRelationList =
                workflowTaskRelationMapper.queryByWorkflowDefinitionCode(workflowDefinition.getCode());
        Set<WorkflowTaskRelationLog> oldWorkflowTaskRelationSet =
                oldWorkflowTaskRelationList.stream().map(WorkflowTaskRelationLog::new).collect(Collectors.toSet());
        StringBuilder sb = new StringBuilder();
        for (WorkflowTaskRelationLog oldWorkflowTaskRelation : oldWorkflowTaskRelationSet) {
            boolean oldTaskExists = taskRelationList.stream()
                    .anyMatch(relation -> oldWorkflowTaskRelation.getPostTaskCode() == relation.getPostTaskCode());
            if (!oldTaskExists) {
                Optional<String> taskDepMsg = workflowLineageService.taskDependentMsg(
                        workflowDefinition.getProjectCode(), oldWorkflowTaskRelation.getWorkflowDefinitionCode(),
                        oldWorkflowTaskRelation.getPostTaskCode());
                taskDepMsg.ifPresent(sb::append);
            }
            if (sb.length() != 0) {
                log.error("Task cannot be deleted because it is dependent");
                throw new ServiceException(sb.toString());
            }
        }
    }

    protected Map<String, Object> updateDagDefine(User loginUser,
                                                  List<WorkflowTaskRelationLog> taskRelationList,
                                                  WorkflowDefinition workflowDefinition,
                                                  WorkflowDefinition workflowDefinitionDeepCopy,
                                                  List<TaskDefinitionLog> taskDefinitionLogs) {
        Map<String, Object> result = new HashMap<>();
        int saveTaskResult = processService.saveTaskDefine(loginUser, workflowDefinition.getProjectCode(),
                taskDefinitionLogs, Boolean.TRUE);
        if (saveTaskResult == Constants.EXIT_CODE_SUCCESS) {
            log.info("The task has not changed, so skip");
        }
        if (saveTaskResult == Constants.DEFINITION_FAILURE) {
            log.error("Update task definitions error, projectCode:{}, workflowDefinitionCode:{}.",
                    workflowDefinition.getProjectCode(), workflowDefinition.getCode());
            putMsg(result, Status.UPDATE_TASK_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_TASK_DEFINITION_ERROR);
        }
        boolean isChange = false;
        if (workflowDefinition.equals(workflowDefinitionDeepCopy) && saveTaskResult == Constants.EXIT_CODE_SUCCESS) {
            List<WorkflowTaskRelationLog> workflowTaskRelationLogList = workflowTaskRelationLogMapper
                    .queryByWorkflowCodeAndVersion(workflowDefinition.getCode(), workflowDefinition.getVersion());
            if (taskRelationList.size() == workflowTaskRelationLogList.size()) {
                Set<WorkflowTaskRelationLog> taskRelationSet = new HashSet<>(taskRelationList);
                Set<WorkflowTaskRelationLog> workflowTaskRelationLogSet = new HashSet<>(workflowTaskRelationLogList);
                if (taskRelationSet.size() == workflowTaskRelationLogSet.size()) {
                    taskRelationSet.removeAll(workflowTaskRelationLogSet);
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
            log.info(
                    "workflow definition needs to be updated, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                    workflowDefinition.getProjectCode(), workflowDefinition.getCode(), workflowDefinition.getVersion());
            workflowDefinition.setUpdateTime(new Date());
            int insertVersion =
                    processService.saveWorkflowDefine(loginUser, workflowDefinition, Boolean.TRUE, Boolean.TRUE);
            if (insertVersion <= 0) {
                log.error("Update workflow definition error, workflowDefinitionCode:{}.", workflowDefinition.getCode());
                putMsg(result, Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
                throw new ServiceException(Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
            } else {
                log.info(
                        "Update workflow definition complete, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                        workflowDefinition.getCode(), insertVersion);
            }

            taskUsedInOtherTaskValid(workflowDefinition, taskRelationList);
            int insertResult = processService.saveTaskRelation(loginUser, workflowDefinition.getProjectCode(),
                    workflowDefinition.getCode(), insertVersion, taskRelationList, taskDefinitionLogs, Boolean.TRUE);
            if (insertResult == Constants.EXIT_CODE_SUCCESS) {
                log.info(
                        "Update workflow task relations complete, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                        workflowDefinition.getProjectCode(), workflowDefinition.getCode(), insertVersion);
                putMsg(result, Status.SUCCESS);
                result.put(Constants.DATA_LIST, workflowDefinition);
            } else {
                log.error(
                        "Update workflow task relations error, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                        workflowDefinition.getProjectCode(), workflowDefinition.getCode(), insertVersion);
                putMsg(result, Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
                throw new ServiceException(Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
            }

            saveWorkflowLineage(workflowDefinition.getProjectCode(), workflowDefinition.getCode(),
                    insertVersion, taskDefinitionLogs);
        } else {
            log.info(
                    "workflow definition does not need to be updated because there is no change, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                    workflowDefinition.getProjectCode(), workflowDefinition.getCode(), workflowDefinition.getVersion());
            putMsg(result, Status.SUCCESS);
            result.put(Constants.DATA_LIST, workflowDefinition);
        }
        return result;
    }

    /**
     * verify workflow definition name unique
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param name        name
     * @return true if workflow definition name not exists, otherwise false
     */
    @Override
    public Map<String, Object> verifyWorkflowDefinitionName(User loginUser, long projectCode, String name,
                                                            long workflowDefinitionCode) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_CREATE);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        WorkflowDefinition workflowDefinition =
                workflowDefinitionMapper.verifyByDefineName(project.getCode(), name.trim());
        if (workflowDefinition == null) {
            putMsg(result, Status.SUCCESS);
            return result;
        }
        if (workflowDefinitionCode != 0 && workflowDefinitionCode == workflowDefinition.getCode()) {
            putMsg(result, Status.SUCCESS);
            return result;
        }
        log.warn("workflow definition with the same name {} already exists, workflowDefinitionCode:{}.",
                workflowDefinition.getName(), workflowDefinition.getCode());
        putMsg(result, Status.WORKFLOW_DEFINITION_NAME_EXIST, name.trim());
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> batchDeleteWorkflowDefinitionByCodes(User loginUser, long projectCode, String codes) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isEmpty(codes)) {
            log.error("Parameter workflowDefinitionCodes is empty, projectCode is {}.", projectCode);
            putMsg(result, Status.WORKFLOW_DEFINITION_CODES_IS_EMPTY);
            return result;
        }

        Set<Long> definitionCodes = Lists.newArrayList(codes.split(Constants.COMMA)).stream().map(Long::parseLong)
                .collect(Collectors.toSet());
        List<WorkflowDefinition> workflowDefinitionList = workflowDefinitionMapper.queryByCodes(definitionCodes);
        Set<Long> queryCodes =
                workflowDefinitionList.stream().map(WorkflowDefinition::getCode).collect(Collectors.toSet());
        // definitionCodes - queryCodes
        Set<Long> diffCode =
                definitionCodes.stream().filter(code -> !queryCodes.contains(code)).collect(Collectors.toSet());

        if (CollectionUtils.isNotEmpty(diffCode)) {
            log.error("workflow definition does not exist, workflowDefinitionCodes:{}.",
                    diffCode.stream().map(String::valueOf).collect(Collectors.joining(Constants.COMMA)));
            throw new ServiceException(Status.BATCH_DELETE_WORKFLOW_DEFINE_BY_CODES_ERROR,
                    diffCode.stream().map(code -> code + "[workflow definition not exist]")
                            .collect(Collectors.joining(Constants.COMMA)));
        }

        for (WorkflowDefinition workflowDefinition : workflowDefinitionList) {
            try {
                this.deleteWorkflowDefinitionByCode(loginUser, workflowDefinition.getCode());
                metricsCleanUpService.cleanUpWorkflowMetricsByDefinitionCode(workflowDefinition.getCode());
            } catch (Exception e) {
                throw new ServiceException(Status.DELETE_WORKFLOW_DEFINE_ERROR, workflowDefinition.getName(),
                        e.getMessage());
            }
        }
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * workflow definition want to delete whether used in other task, should throw exception when have be used.
     * <p>
     * This function avoid delete workflow definition already dependencies by other tasks by accident.
     *
     * @param workflowDefinition WorkflowDefinition you change task definition and task relation
     */
    private void workflowDefinitionUsedInOtherTaskValid(WorkflowDefinition workflowDefinition) {
        // check workflow definition is already online
        if (workflowDefinition.getReleaseState() == ReleaseState.ONLINE) {
            throw new ServiceException(Status.WORKFLOW_DEFINE_STATE_ONLINE, workflowDefinition.getName());
        }

        // check workflow instances is already running
        List<WorkflowInstance> workflowInstances = workflowInstanceService.queryByWorkflowDefinitionCodeAndStatus(
                workflowDefinition.getCode(), WorkflowExecutionStatus.getNotTerminalStatus());
        if (CollectionUtils.isNotEmpty(workflowInstances)) {
            throw new ServiceException(Status.DELETE_WORKFLOW_DEFINITION_EXECUTING_FAIL, workflowInstances.size());
        }

        // check workflow used by other task, including sub workflow and dependent task type
        Optional<String> taskDepMsg = workflowLineageService.taskDependentMsg(workflowDefinition.getProjectCode(),
                workflowDefinition.getCode(), 0);

        if (taskDepMsg.isPresent()) {
            String errorMeg = "workflow definition cannot be deleted because it has dependent, " + taskDepMsg.get();
            log.error(errorMeg);
            throw new ServiceException(errorMeg);
        }
    }

    public void deleteWorkflowDefinitionByCode(User loginUser, long code) {
        WorkflowDefinition workflowDefinition = workflowDefinitionDao.queryByCode(code)
                .orElseThrow(() -> new ServiceException(WORKFLOW_DEFINITION_NOT_EXIST, String.valueOf(code)));

        Project project = projectMapper.queryByCode(workflowDefinition.getProjectCode());
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_DEFINITION_DELETE);

        // Determine if the login user is the owner of the workflow definition
        if (loginUser.getId() != workflowDefinition.getUserId() && loginUser.getUserType() != UserType.ADMIN_USER) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        workflowDefinitionUsedInOtherTaskValid(workflowDefinition);

        // get the timing according to the workflow definition
        Schedule scheduleObj = scheduleMapper.queryByWorkflowDefinitionCode(code);
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
        workflowInstanceService.deleteWorkflowInstanceByWorkflowDefinitionCode(workflowDefinition.getCode());
        // delete task definition
        taskDefinitionService.deleteTaskByWorkflowDefinitionCode(workflowDefinition.getCode(),
                workflowDefinition.getVersion());
        // delete task definition log
        taskDefinitionLogService.deleteTaskByWorkflowDefinitionCode(workflowDefinition.getCode());
        // delete workflow definition log
        workflowDefinitionLogDao.deleteByWorkflowDefinitionCode(workflowDefinition.getCode());

        // we delete the workflow definition at last to avoid using transaction here.
        // If delete error, we can call this interface again.
        workflowDefinitionDao.deleteByWorkflowDefinitionCode(workflowDefinition.getCode());
        metricsCleanUpService.cleanUpWorkflowMetricsByDefinitionCode(code);
        log.info("Success delete workflow definition workflowDefinitionCode: {}", code);
    }

    /**
     * batch export workflow definition by codes
     */
    @Override
    public void batchExportWorkflowDefinitionByCodes(User loginUser, long projectCode, String codes,
                                                     HttpServletResponse response) {
        if (StringUtils.isEmpty(codes)) {
            log.warn("workflow definition codes to be exported is empty.");
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
        List<WorkflowDefinition> workflowDefinitionList = workflowDefinitionMapper.queryByCodes(defineCodeSet);
        if (CollectionUtils.isEmpty(workflowDefinitionList)) {
            log.error("workflow definitions to be exported do not exist, workflowDefinitionCodes:{}.", defineCodeSet);
            return;
        }
        // check workflowDefinition exist in project
        List<WorkflowDefinition> workflowDefinitionListInProject = workflowDefinitionList.stream()
                .filter(o -> projectCode == o.getProjectCode()).collect(Collectors.toList());
        List<DagDataSchedule> dagDataSchedules =
                workflowDefinitionListInProject.stream().map(this::exportWorkflowDagData).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(dagDataSchedules)) {
            log.info("Start download workflow definition file, workflowDefinitionCodes:{}.", defineCodeSet);
            downloadWorkflowDefinitionFile(response, dagDataSchedules);
        } else {
            log.error("There is no exported workflow dag data.");
        }
    }

    /**
     * download the workflow definition file
     */
    protected void downloadWorkflowDefinitionFile(HttpServletResponse response,
                                                  List<DagDataSchedule> dagDataSchedules) {
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
            log.warn("Export workflow definition fail", e);
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
     * get export workflow dag data
     *
     * @param workflowDefinition workflow definition
     * @return DagDataSchedule
     */
    public DagDataSchedule exportWorkflowDagData(WorkflowDefinition workflowDefinition) {
        Schedule scheduleObj = scheduleMapper.queryByWorkflowDefinitionCode(workflowDefinition.getCode());
        DagDataSchedule dagDataSchedule = new DagDataSchedule(processService.genDagData(workflowDefinition));
        if (scheduleObj != null) {
            scheduleObj.setReleaseState(ReleaseState.OFFLINE);
            dagDataSchedule.setSchedule(scheduleObj);
        }
        return dagDataSchedule;
    }

    /**
     * import workflow definition
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param file        workflow metadata json file
     * @return import workflow
     */
    @Override
    @Transactional
    public Map<String, Object> importWorkflowDefinition(User loginUser, long projectCode, MultipartFile file) {
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
            log.warn("workflow definition file content is empty.");
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
    @Transactional
    public Map<String, Object> importSqlWorkflowDefinition(User loginUser, long projectCode, MultipartFile file) {
        Map<String, Object> result;
        Project project = projectMapper.queryByCode(projectCode);
        result = projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_IMPORT);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        String workflowDefinitionName =
                file.getOriginalFilename() == null ? file.getName() : file.getOriginalFilename();
        int index = workflowDefinitionName.lastIndexOf(".");
        if (index > 0) {
            workflowDefinitionName = workflowDefinitionName.substring(0, index);
        }
        workflowDefinitionName = getNewName(workflowDefinitionName, IMPORT_SUFFIX);

        WorkflowDefinition workflowDefinition;
        List<TaskDefinitionLog> taskDefinitionList = new ArrayList<>();
        List<WorkflowTaskRelationLog> workflowTaskRelationLogList = new ArrayList<>();

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
            // build workflow definition
            workflowDefinition = new WorkflowDefinition(projectCode,
                    workflowDefinitionName,
                    CodeGenerateUtils.genCode(),
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
            log.error("Import workflow definition error.", e);
            putMsg(result, Status.IMPORT_WORKFLOW_DEFINE_ERROR);
            return result;
        }

        // build task relation
        for (Map.Entry<String, Long> entry : taskNameToCode.entrySet()) {
            List<String> upstreams = taskNameToUpstream.get(entry.getKey());
            if (CollectionUtils.isEmpty(upstreams)
                    || (upstreams.size() == 1 && upstreams.contains("root") && !taskNameToCode.containsKey("root"))) {
                WorkflowTaskRelationLog workflowTaskRelationLog = buildNormalTaskRelation(0, entry.getValue());
                workflowTaskRelationLogList.add(workflowTaskRelationLog);
                continue;
            }
            for (String upstream : upstreams) {
                WorkflowTaskRelationLog workflowTaskRelationLog =
                        buildNormalTaskRelation(taskNameToCode.get(upstream), entry.getValue());
                workflowTaskRelationLogList.add(workflowTaskRelationLog);
            }
        }

        return createDagDefine(loginUser, workflowTaskRelationLogList, workflowDefinition, taskDefinitionList);
    }

    private WorkflowTaskRelationLog buildNormalTaskRelation(long preTaskCode, long postTaskCode) {
        WorkflowTaskRelationLog workflowTaskRelationLog = new WorkflowTaskRelationLog();
        workflowTaskRelationLog.setPreTaskCode(preTaskCode);
        workflowTaskRelationLog.setPreTaskVersion(0);
        workflowTaskRelationLog.setPostTaskCode(postTaskCode);
        workflowTaskRelationLog.setPostTaskVersion(0);
        workflowTaskRelationLog.setConditionType(ConditionType.NONE);
        workflowTaskRelationLog.setName("");
        return workflowTaskRelationLog;
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
        taskDefinition.setCode(CodeGenerateUtils.genCode());
        taskDefinition.setTaskType("SQL");
        taskDefinition.setFailRetryTimes(0);
        taskDefinition.setFailRetryInterval(0);
        taskDefinition.setTimeoutFlag(TimeoutFlag.CLOSE);
        taskDefinition.setWorkerGroup(WorkerGroupUtils.getDefaultWorkerGroup());
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
    protected boolean checkAndImport(User loginUser,
                                     long projectCode,
                                     Map<String, Object> result,
                                     DagDataSchedule dagDataSchedule) {
        if (!checkImportanceParams(dagDataSchedule, result)) {
            return false;
        }
        WorkflowDefinition workflowDefinition = dagDataSchedule.getWorkflowDefinition();

        // generate import workflowDefinitionName
        String workflowDefinitionName = recursionWorkflowDefinitionName(projectCode, workflowDefinition.getName(), 1);
        String importWorkflowDefinitionName = getNewName(workflowDefinitionName, IMPORT_SUFFIX);
        // unique check
        Map<String, Object> checkResult =
                verifyWorkflowDefinitionName(loginUser, projectCode, importWorkflowDefinitionName, 0);
        if (Status.SUCCESS.equals(checkResult.get(Constants.STATUS))) {
            putMsg(result, Status.SUCCESS);
        } else {
            result.putAll(checkResult);
            return false;
        }
        workflowDefinition.setName(importWorkflowDefinitionName);
        workflowDefinition.setId(null);
        workflowDefinition.setProjectCode(projectCode);
        workflowDefinition.setUserId(loginUser.getId());
        try {
            workflowDefinition.setCode(CodeGenerateUtils.genCode());
        } catch (CodeGenerateException e) {
            log.error(
                    "Save workflow definition error because generate workflow definition code error, projectCode:{}.",
                    projectCode, e);
            putMsg(result, Status.CREATE_WORKFLOW_DEFINITION_ERROR);
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
                long code = CodeGenerateUtils.genCode();
                taskCodeMap.put(taskDefinitionLog.getCode(), code);
                taskDefinitionLog.setCode(code);
            } catch (CodeGenerateException e) {
                log.error("Generate task definition code error, projectCode:{}, workflowDefinitionCode:{}",
                        projectCode, workflowDefinition.getCode(), e);
                putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS, "Error generating task definition code");
                return false;
            }
            taskDefinitionLogList.add(taskDefinitionLog);
        }
        int insert = taskDefinitionMapper.batchInsert(taskDefinitionLogList);
        int logInsert = taskDefinitionLogMapper.batchInsert(taskDefinitionLogList);
        if ((logInsert & insert) == 0) {
            log.error("Save task definition error, projectCode:{}, workflowDefinitionCode:{}", projectCode,
                    workflowDefinition.getCode());
            putMsg(result, Status.CREATE_TASK_DEFINITION_ERROR);
            throw new ServiceException(Status.CREATE_TASK_DEFINITION_ERROR);
        }

        List<WorkflowTaskRelation> taskRelationList = dagDataSchedule.getWorkflowTaskRelationList();
        List<WorkflowTaskRelationLog> taskRelationLogList = new ArrayList<>();
        for (WorkflowTaskRelation workflowTaskRelation : taskRelationList) {
            WorkflowTaskRelationLog workflowTaskRelationLog = new WorkflowTaskRelationLog(workflowTaskRelation);
            if (taskCodeMap.containsKey(workflowTaskRelationLog.getPreTaskCode())) {
                workflowTaskRelationLog.setPreTaskCode(taskCodeMap.get(workflowTaskRelationLog.getPreTaskCode()));
            }
            if (taskCodeMap.containsKey(workflowTaskRelationLog.getPostTaskCode())) {
                workflowTaskRelationLog.setPostTaskCode(taskCodeMap.get(workflowTaskRelationLog.getPostTaskCode()));
            }
            workflowTaskRelationLog.setPreTaskVersion(Constants.VERSION_FIRST);
            workflowTaskRelationLog.setPostTaskVersion(Constants.VERSION_FIRST);
            taskRelationLogList.add(workflowTaskRelationLog);
        }
        if (StringUtils.isNotEmpty(workflowDefinition.getLocations())
                && JSONUtils.checkJsonValid(workflowDefinition.getLocations())) {
            ArrayNode arrayNode = JSONUtils.parseArray(workflowDefinition.getLocations());
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
            workflowDefinition.setLocations(newArrayNode.toString());
        }
        workflowDefinition.setCreateTime(new Date());
        workflowDefinition.setUpdateTime(new Date());
        Map<String, Object> createDagResult =
                createDagDefine(loginUser, taskRelationLogList, workflowDefinition, Lists.newArrayList());
        if (Status.SUCCESS.equals(createDagResult.get(Constants.STATUS))) {
            putMsg(createDagResult, Status.SUCCESS);
        } else {
            result.putAll(createDagResult);
            log.error("Import workflow definition error, projectCode:{}, workflowDefinitionCode:{}.", projectCode,
                    workflowDefinition.getCode());
            throw new ServiceException(Status.IMPORT_WORKFLOW_DEFINE_ERROR);
        }

        Schedule schedule = dagDataSchedule.getSchedule();
        if (null != schedule) {
            WorkflowDefinition newWorkflowDefinition =
                    workflowDefinitionMapper.queryByCode(workflowDefinition.getCode());
            schedule.setWorkflowDefinitionCode(newWorkflowDefinition.getCode());
            schedule.setId(null);
            schedule.setUserId(loginUser.getId());
            schedule.setCreateTime(now);
            schedule.setUpdateTime(now);
            int scheduleInsert = scheduleMapper.insert(schedule);
            if (0 == scheduleInsert) {
                log.error(
                        "Import workflow definition error due to save schedule fail, projectCode:{}, workflowDefinitionCode:{}.",
                        projectCode, workflowDefinition.getCode());
                putMsg(result, Status.IMPORT_WORKFLOW_DEFINE_ERROR);
                throw new ServiceException(Status.IMPORT_WORKFLOW_DEFINE_ERROR);
            }
        }

        result.put(Constants.DATA_LIST, workflowDefinition);
        log.info("Import workflow definition complete, projectCode:{}, workflowDefinitionCode:{}.", projectCode,
                workflowDefinition.getCode());
        return true;
    }

    /**
     * check importance params
     */
    private boolean checkImportanceParams(DagDataSchedule dagDataSchedule, Map<String, Object> result) {
        if (dagDataSchedule.getWorkflowDefinition() == null) {
            log.warn("workflow definition is null.");
            putMsg(result, Status.DATA_IS_NULL, "WorkflowDefinition");
            return false;
        }
        if (CollectionUtils.isEmpty(dagDataSchedule.getTaskDefinitionList())) {
            log.warn("Task definition list is null.");
            putMsg(result, Status.DATA_IS_NULL, "TaskDefinitionList");
            return false;
        }
        if (CollectionUtils.isEmpty(dagDataSchedule.getWorkflowTaskRelationList())) {
            log.warn("workflow task relation list is null.");
            putMsg(result, Status.DATA_IS_NULL, "WorkflowTaskRelationList");
            return false;
        }
        return true;
    }

    private String recursionWorkflowDefinitionName(long projectCode, String workflowDefinitionName, int num) {
        WorkflowDefinition workflowDefinition =
                workflowDefinitionMapper.queryByDefineName(projectCode, workflowDefinitionName);
        if (workflowDefinition != null) {
            if (num > 1) {
                String str = workflowDefinitionName.substring(0, workflowDefinitionName.length() - 3);
                workflowDefinitionName = str + "(" + num + ")";
            } else {
                workflowDefinitionName = workflowDefinition.getName() + "(" + num + ")";
            }
        } else {
            return workflowDefinitionName;
        }
        return recursionWorkflowDefinitionName(projectCode, workflowDefinitionName, num + 1);
    }

    /**
     * check the workflow task relation json
     *
     * @param workflowTaskRelationJson workflow task relation json
     * @return check result code
     */
    @Override
    public Map<String, Object> checkWorkflowNodeList(String workflowTaskRelationJson,
                                                     List<TaskDefinitionLog> taskDefinitionLogsList) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (workflowTaskRelationJson == null) {
                log.error("workflow task relation data is null.");
                putMsg(result, Status.DATA_IS_NOT_VALID, workflowTaskRelationJson);
                return result;
            }

            List<WorkflowTaskRelation> taskRelationList =
                    JSONUtils.toList(workflowTaskRelationJson, WorkflowTaskRelation.class);
            // Check whether the task node is normal
            List<TaskNode> taskNodes = processService.transformTask(taskRelationList, taskDefinitionLogsList);

            if (CollectionUtils.isEmpty(taskNodes)) {
                log.error("Task node data is empty.");
                putMsg(result, Status.WORKFLOW_DAG_IS_EMPTY);
                return result;
            }

            // check has cycle
            if (graphHasCycle(taskNodes)) {
                log.error("workflow DAG has cycle.");
                putMsg(result, Status.WORKFLOW_NODE_HAS_CYCLE);
                return result;
            }

            // check whether the workflow definition json is normal
            for (TaskNode taskNode : taskNodes) {
                if (!checkTaskParameters(taskNode.getType(), taskNode.getParams())) {
                    putMsg(result, Status.WORKFLOW_NODE_S_PARAMETER_INVALID, taskNode.getName());
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
     * get task node details based on workflow definition
     *
     * @param loginUser   loginUser
     * @param projectCode project code
     * @param code        workflow definition code
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
        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(code);
        if (workflowDefinition == null || projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, workflowDefinitionCode:{}.", code);
            putMsg(result, Status.WORKFLOW_DEFINITION_NOT_EXIST, String.valueOf(code));
            return result;
        }
        DagData dagData = processService.genDagData(workflowDefinition);
        result.put(Constants.DATA_LIST, dagData.getTaskDefinitionList());
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * get task node details map based on workflow definition
     *
     * @param loginUser   loginUser
     * @param projectCode project code
     * @param codes       define codes
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
        List<WorkflowDefinition> workflowDefinitionList = workflowDefinitionMapper.queryByCodes(defineCodeSet);
        if (CollectionUtils.isEmpty(workflowDefinitionList)) {
            log.error("workflow definitions do not exist, codes:{}.", defineCodeSet);
            putMsg(result, Status.WORKFLOW_DEFINITION_NOT_EXIST, codes);
            return result;
        }
        HashMap<Long, Project> userProjects = new HashMap<>(Constants.DEFAULT_HASH_MAP_SIZE);
        projectMapper.queryProjectCreatedAndAuthorizedByUserId(loginUser.getId())
                .forEach(userProject -> userProjects.put(userProject.getCode(), userProject));

        // check workflowDefinition exist in project
        List<WorkflowDefinition> workflowDefinitionListInProject = workflowDefinitionList.stream()
                .filter(o -> userProjects.containsKey(o.getProjectCode())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(workflowDefinitionListInProject)) {
            Set<Long> codesInProject = workflowDefinitionListInProject.stream()
                    .map(WorkflowDefinition::getCode).collect(Collectors.toSet());
            log.error("workflow definitions do not exist in project, projectCode:{}, workflowDefinitionsCodes:{}.",
                    workflowDefinitionListInProject.get(0).getProjectCode(), codesInProject);
            putMsg(result, Status.WORKFLOW_DEFINITION_NOT_EXIST, codes);
            return result;
        }
        Map<Long, List<TaskDefinition>> taskNodeMap = new HashMap<>();
        for (WorkflowDefinition workflowDefinition : workflowDefinitionListInProject) {
            DagData dagData = processService.genDagData(workflowDefinition);
            taskNodeMap.put(workflowDefinition.getCode(), dagData.getTaskDefinitionList());
        }

        result.put(Constants.DATA_LIST, taskNodeMap);
        putMsg(result, Status.SUCCESS);

        return result;

    }

    /**
     * query workflow definition all by project code
     *
     * @param loginUser   loginUser
     * @param projectCode project code
     * @return workflow definitions in the project
     */
    @Override
    public Map<String, Object> queryAllWorkflowDefinitionByProjectCode(User loginUser, long projectCode) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_DEFINITION);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        List<WorkflowDefinition> workflowDefinitions = workflowDefinitionMapper.queryAllDefinitionList(projectCode);
        List<DagData> dagDataList =
                workflowDefinitions.stream().map(processService::genDagData).collect(Collectors.toList());
        result.put(Constants.DATA_LIST, dagDataList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query workflow definition list by project code
     *
     * @param projectCode project code
     * @return workflow definition list in the project
     */
    @Override
    public Map<String, Object> queryWorkflowDefinitionListByProjectCode(long projectCode) {
        Map<String, Object> result = new HashMap<>();
        List<DependentSimplifyDefinition> workflowDefinitions =
                workflowDefinitionMapper.queryDefinitionListByProjectCodeAndWorkflowDefinitionCodes(projectCode, null);
        result.put(Constants.DATA_LIST, workflowDefinitions);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query workflow definition list by workflow definition code
     *
     * @param projectCode           project code
     * @param workflowDefinitionCode workflow definition code
     * @return task definition list in the workflow definition
     */
    @Override
    public Map<String, Object> queryTaskDefinitionListByWorkflowDefinitionCode(long projectCode,
                                                                               Long workflowDefinitionCode) {
        Map<String, Object> result = new HashMap<>();

        Set<Long> definitionCodesSet = new HashSet<>();
        definitionCodesSet.add(workflowDefinitionCode);
        List<DependentSimplifyDefinition> workflowDefinitions = workflowDefinitionMapper
                .queryDefinitionListByProjectCodeAndWorkflowDefinitionCodes(projectCode, definitionCodesSet);

        // query task definition log
        List<TaskDefinitionLog> taskDefinitionLogsList = taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(
                workflowDefinitions.get(0).getCode(), workflowDefinitions.get(0).getVersion());

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
     * @param code        workflow definition code
     * @param limit       limit
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
        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(code);
        if (null == workflowDefinition || projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, code:{}.", code);
            putMsg(result, Status.WORKFLOW_DEFINITION_NOT_EXIST, String.valueOf(code));
            return result;
        }
        DAG<Long, TaskNode, TaskNodeRelation> dag = processService.genDagGraph(workflowDefinition);
        // nodes that are running
        Map<Long, List<TreeViewDto>> runningNodeMap = new ConcurrentHashMap<>();

        // nodes that are waiting to run
        Map<Long, List<TreeViewDto>> waitingRunningNodeMap = new ConcurrentHashMap<>();

        // List of workflow instances
        List<WorkflowInstance> workflowInstanceList =
                workflowInstanceService.queryByWorkflowDefinitionCode(code, limit);
        workflowInstanceList.forEach(workflowInstance -> workflowInstance
                .setDuration(
                        DateUtils.format2Duration(workflowInstance.getStartTime(), workflowInstance.getEndTime())));
        List<TaskDefinitionLog> taskDefinitionList = taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(
                workflowDefinition.getCode(), workflowDefinition.getVersion());
        Map<Long, TaskDefinitionLog> taskDefinitionMap = taskDefinitionList.stream()
                .collect(Collectors.toMap(TaskDefinitionLog::getCode, taskDefinitionLog -> taskDefinitionLog));

        if (limit < 0) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR);
            return result;
        }
        if (limit > workflowInstanceList.size()) {
            limit = workflowInstanceList.size();
        }

        TreeViewDto parentTreeViewDto = new TreeViewDto();
        parentTreeViewDto.setName("DAG");
        parentTreeViewDto.setType("");
        parentTreeViewDto.setCode(0L);
        // Specify the workflow definition, because it is a TreeView for a workflow definition
        for (int i = limit - 1; i >= 0; i--) {
            WorkflowInstance workflowInstance = workflowInstanceList.get(i);
            Date endTime = workflowInstance.getEndTime() == null ? new Date() : workflowInstance.getEndTime();
            parentTreeViewDto.getInstances()
                    .add(new Instance(workflowInstance.getId(), workflowInstance.getName(),
                            workflowInstance.getWorkflowDefinitionCode(),
                            "", workflowInstance.getState().name(), workflowInstance.getStartTime(), endTime,
                            workflowInstance.getHost(),
                            DateUtils.format2Readable(endTime.getTime() - workflowInstance.getStartTime().getTime())));
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
                    WorkflowInstance workflowInstance = workflowInstanceList.get(i);
                    TaskInstance taskInstance =
                            taskInstanceMapper.queryByInstanceIdAndCode(workflowInstance.getId(), nodeCode);
                    if (taskInstance == null) {
                        treeViewDto.getInstances().add(new Instance(-1, "not running", 0, "null"));
                    } else {
                        Date startTime = taskInstance.getStartTime() == null ? new Date() : taskInstance.getStartTime();
                        Date endTime = taskInstance.getEndTime() == null ? new Date() : taskInstance.getEndTime();

                        long subWorkflowCode = 0L;
                        // if workflow is sub workflow, the return sub id, or sub id=0
                        if (TaskTypeUtils.isSubWorkflowTask(taskInstance.getTaskType())) {
                            TaskDefinition taskDefinition = taskDefinitionMap.get(taskInstance.getTaskCode());
                            subWorkflowCode = Long.parseLong(JSONUtils.parseObject(
                                    taskDefinition.getTaskParams()).path(CMD_PARAM_SUB_WORKFLOW_DEFINITION_CODE)
                                    .asText());
                        }
                        treeViewDto.getInstances().add(new Instance(taskInstance.getId(), taskInstance.getName(),
                                taskInstance.getTaskCode(),
                                taskInstance.getTaskType(), taskInstance.getState().name(),
                                taskInstance.getStartTime(), taskInstance.getEndTime(),
                                taskInstance.getHost(),
                                DateUtils.format2Readable(endTime.getTime() - startTime.getTime()), subWorkflowCode));
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
     * batch copy workflow definition
     *
     * @param loginUser         loginUser
     * @param projectCode       projectCode
     * @param codes             workflowDefinitionCodes
     * @param targetProjectCode targetProjectCode
     */
    @Override
    @Transactional
    public Map<String, Object> batchCopyWorkflowDefinition(User loginUser,
                                                           long projectCode,
                                                           String codes,
                                                           long targetProjectCode) {
        Map<String, Object> result = checkParams(loginUser, projectCode, codes, targetProjectCode, WORKFLOW_BATCH_COPY);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        List<String> failedWorkflowList = new ArrayList<>();
        doBatchOperateWorkflowDefinition(loginUser, targetProjectCode, failedWorkflowList, codes, result, true);
        checkBatchOperateResult(projectCode, targetProjectCode, result, failedWorkflowList, true);
        return result;
    }

    /**
     * batch move workflow definition
     * Will be deleted
     *
     * @param loginUser         loginUser
     * @param projectCode       projectCode
     * @param codes             workflowDefinitionCodes
     * @param targetProjectCode targetProjectCode
     */
    @Override
    @Transactional
    public Map<String, Object> batchMoveWorkflowDefinition(User loginUser,
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

        List<String> failedWorkflowList = new ArrayList<>();
        doBatchOperateWorkflowDefinition(loginUser, targetProjectCode, failedWorkflowList, codes, result, false);
        checkBatchOperateResult(projectCode, targetProjectCode, result, failedWorkflowList, false);
        return result;
    }

    private Map<String, Object> checkParams(User loginUser,
                                            long projectCode,
                                            String workflowDefinitionCodes,
                                            long targetProjectCode, String perm) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode, perm);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        if (StringUtils.isEmpty(workflowDefinitionCodes)) {
            log.error("Parameter workflowDefinitionCodes is empty, projectCode is {}.", projectCode);
            putMsg(result, Status.WORKFLOW_DEFINITION_CODES_IS_EMPTY, workflowDefinitionCodes);
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

    protected void doBatchOperateWorkflowDefinition(User loginUser,
                                                    long targetProjectCode,
                                                    List<String> failedWorkflowList,
                                                    String workflowDefinitionCodes,
                                                    Map<String, Object> result,
                                                    boolean isCopy) {
        Set<Long> definitionCodes = Arrays.stream(workflowDefinitionCodes.split(Constants.COMMA)).map(Long::parseLong)
                .collect(Collectors.toSet());
        List<WorkflowDefinition> workflowDefinitionList = workflowDefinitionMapper.queryByCodes(definitionCodes);
        Set<Long> queryCodes =
                workflowDefinitionList.stream().map(WorkflowDefinition::getCode).collect(Collectors.toSet());
        // definitionCodes - queryCodes
        Set<Long> diffCode =
                definitionCodes.stream().filter(code -> !queryCodes.contains(code)).collect(Collectors.toSet());
        diffCode.forEach(code -> failedWorkflowList.add(code + "[null]"));
        for (WorkflowDefinition workflowDefinition : workflowDefinitionList) {
            List<WorkflowTaskRelation> workflowTaskRelations =
                    workflowTaskRelationMapper.queryByWorkflowDefinitionCode(workflowDefinition.getCode());
            List<WorkflowTaskRelationLog> taskRelationList =
                    workflowTaskRelations.stream().map(WorkflowTaskRelationLog::new).collect(Collectors.toList());
            workflowDefinition.setProjectCode(targetProjectCode);
            if (isCopy) {
                log.info("Copy workflow definition...");
                List<TaskDefinitionLog> taskDefinitionLogs =
                        taskDefinitionLogDao.queryTaskDefineLogList(workflowTaskRelations);
                Map<Long, Long> taskCodeMap = new HashMap<>();
                for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
                    try {
                        long taskCode = CodeGenerateUtils.genCode();
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
                for (WorkflowTaskRelationLog workflowTaskRelationLog : taskRelationList) {
                    if (workflowTaskRelationLog.getPreTaskCode() > 0) {
                        workflowTaskRelationLog
                                .setPreTaskCode(taskCodeMap.get(workflowTaskRelationLog.getPreTaskCode()));
                    }
                    if (workflowTaskRelationLog.getPostTaskCode() > 0) {
                        workflowTaskRelationLog
                                .setPostTaskCode(taskCodeMap.get(workflowTaskRelationLog.getPostTaskCode()));
                    }
                }
                final long oldWorkflowDefinitionCode = workflowDefinition.getCode();
                try {
                    workflowDefinition.setCode(CodeGenerateUtils.genCode());
                } catch (CodeGenerateException e) {
                    log.error("Generate workflow definition code error, projectCode:{}.", targetProjectCode, e);
                    putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS);
                    throw new ServiceException(Status.INTERNAL_SERVER_ERROR_ARGS);
                }
                workflowDefinition.setId(null);
                workflowDefinition.setUserId(loginUser.getId());
                workflowDefinition.setName(getNewName(workflowDefinition.getName(), COPY_SUFFIX));
                final Date date = new Date();
                workflowDefinition.setCreateTime(date);
                workflowDefinition.setUpdateTime(date);
                workflowDefinition.setReleaseState(ReleaseState.OFFLINE);
                if (StringUtils.isNotBlank(workflowDefinition.getLocations())) {
                    ArrayNode jsonNodes = JSONUtils.parseArray(workflowDefinition.getLocations());
                    for (int i = 0; i < jsonNodes.size(); i++) {
                        ObjectNode node = (ObjectNode) jsonNodes.path(i);
                        node.put("taskCode", taskCodeMap.get(node.get("taskCode").asLong()));
                        jsonNodes.set(i, node);
                    }
                    workflowDefinition.setLocations(JSONUtils.toJsonString(jsonNodes));
                }
                // copy timing configuration
                Schedule scheduleObj = scheduleMapper.queryByWorkflowDefinitionCode(oldWorkflowDefinitionCode);
                if (scheduleObj != null) {
                    scheduleObj.setId(null);
                    scheduleObj.setUserId(loginUser.getId());
                    scheduleObj.setWorkflowDefinitionCode(workflowDefinition.getCode());
                    scheduleObj.setReleaseState(ReleaseState.OFFLINE);
                    scheduleObj.setCreateTime(date);
                    scheduleObj.setUpdateTime(date);
                    int insertResult = scheduleMapper.insert(scheduleObj);
                    if (insertResult != 1) {
                        log.error("Schedule create error, workflowDefinitionCode:{}.", workflowDefinition.getCode());
                        putMsg(result, Status.CREATE_SCHEDULE_ERROR);
                        throw new ServiceException(Status.CREATE_SCHEDULE_ERROR);
                    }
                }
                try {
                    result.putAll(createDagDefine(loginUser, taskRelationList, workflowDefinition, taskDefinitionLogs));
                } catch (Exception e) {
                    log.error("Copy workflow definition error, workflowDefinitionCode from {} to {}.",
                            oldWorkflowDefinitionCode, workflowDefinition.getCode(), e);
                    putMsg(result, Status.COPY_WORKFLOW_DEFINITION_ERROR);
                    throw new ServiceException(Status.COPY_WORKFLOW_DEFINITION_ERROR);
                }
            } else {
                log.info("Move workflow definition...");
                try {
                    result.putAll(updateDagDefine(loginUser, taskRelationList, workflowDefinition, null,
                            Lists.newArrayList()));
                } catch (Exception e) {
                    log.error("Move workflow definition error, workflowDefinitionCode:{}.",
                            workflowDefinition.getCode(), e);
                    putMsg(result, Status.MOVE_WORKFLOW_DEFINITION_ERROR);
                    throw new ServiceException(Status.MOVE_WORKFLOW_DEFINITION_ERROR);
                }
            }
            if (result.get(Constants.STATUS) != Status.SUCCESS) {
                failedWorkflowList.add(workflowDefinition.getCode() + "[" + workflowDefinition.getName() + "]");
            }
        }
    }

    /**
     * get new task name or workflow name when copy or import operate
     *
     * @param originalName task or workflow original name
     * @param suffix       "_copy_" or "_import_"
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
     * switch the defined workflow definition version
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param code        workflow definition code
     * @param version     the version user want to switch
     * @return switch workflow definition version result code
     */
    @Override
    @Transactional
    public Map<String, Object> switchWorkflowDefinitionVersion(User loginUser, long projectCode, long code,
                                                               int version) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_SWITCH_TO_THIS_VERSION);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(code);
        if (Objects.isNull(workflowDefinition) || projectCode != workflowDefinition.getProjectCode()) {
            log.error(
                    "Switch workflow definition error because it does not exist, projectCode:{}, workflowDefinitionCode:{}.",
                    projectCode, code);
            putMsg(result, Status.SWITCH_WORKFLOW_DEFINITION_VERSION_NOT_EXIST_WORKFLOW_DEFINITION_ERROR, code);
            return result;
        }

        WorkflowDefinitionLog workflowDefinitionLog =
                workflowDefinitionLogMapper.queryByDefinitionCodeAndVersion(code, version);
        if (Objects.isNull(workflowDefinitionLog)) {
            log.error(
                    "Switch workflow definition error because version does not exist, projectCode:{}, workflowDefinitionCode:{}, version:{}.",
                    projectCode, code, version);
            putMsg(result, Status.SWITCH_WORKFLOW_DEFINITION_VERSION_NOT_EXIST_WORKFLOW_DEFINITION_VERSION_ERROR,
                    workflowDefinition.getCode(), version);
            return result;
        }
        int switchVersion = processService.switchVersion(workflowDefinition, workflowDefinitionLog);
        if (switchVersion <= 0) {
            log.error(
                    "Switch workflow definition version error, projectCode:{}, workflowDefinitionCode:{}, version:{}.",
                    projectCode, code, version);
            putMsg(result, Status.SWITCH_WORKFLOW_DEFINITION_VERSION_ERROR);
            throw new ServiceException(Status.SWITCH_WORKFLOW_DEFINITION_VERSION_ERROR);
        }

        List<WorkflowTaskRelation> workflowTaskRelationList = workflowTaskRelationMapper
                .queryWorkflowTaskRelationsByWorkflowDefinitionCode(workflowDefinitionLog.getCode(),
                        workflowDefinitionLog.getVersion());
        List<TaskCodeVersionDto> taskDefinitionList = getTaskCodeVersionDtos(workflowTaskRelationList);
        List<TaskDefinitionLog> taskDefinitionLogList =
                taskDefinitionLogMapper.queryByTaskDefinitions(taskDefinitionList.stream()
                        .flatMap(taskCodeVersionDto -> {
                            TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
                            taskDefinitionLog.setCode(taskCodeVersionDto.getCode());
                            taskDefinitionLog.setVersion(taskCodeVersionDto.getVersion());
                            return Stream.of(taskDefinitionLog);
                        }).collect(Collectors.toList()));
        saveWorkflowLineage(workflowDefinitionLog.getProjectCode(), workflowDefinitionLog.getCode(),
                workflowDefinitionLog.getVersion(), taskDefinitionLogList);

        log.info("Switch workflow definition version complete, projectCode:{}, workflowDefinitionCode:{}, version:{}.",
                projectCode, code, version);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private static @NotNull List<TaskCodeVersionDto> getTaskCodeVersionDtos(List<WorkflowTaskRelation> workflowTaskRelationList) {
        List<TaskCodeVersionDto> taskDefinitionList = new ArrayList<>();
        for (WorkflowTaskRelation workflowTaskRelation : workflowTaskRelationList) {
            if (workflowTaskRelation.getPreTaskCode() != 0) {
                TaskCodeVersionDto taskCodeVersionDto = new TaskCodeVersionDto();
                taskCodeVersionDto.setCode(workflowTaskRelation.getPreTaskCode());
                taskCodeVersionDto.setVersion(workflowTaskRelation.getPreTaskVersion());
                taskDefinitionList.add(taskCodeVersionDto);
            }
            if (workflowTaskRelation.getPostTaskCode() != 0) {
                TaskCodeVersionDto taskCodeVersionDto = new TaskCodeVersionDto();
                taskCodeVersionDto.setCode(workflowTaskRelation.getPostTaskCode());
                taskCodeVersionDto.setVersion(workflowTaskRelation.getPostTaskVersion());
                taskDefinitionList.add(taskCodeVersionDto);
            }
        }
        return taskDefinitionList;
    }

    /**
     * check batch operate result
     *
     * @param srcProjectCode    srcProjectCode
     * @param targetProjectCode targetProjectCode
     * @param result            result
     * @param failedWorkflowList failedWorkflowList
     * @param isCopy            isCopy
     */
    private void checkBatchOperateResult(long srcProjectCode, long targetProjectCode,
                                         Map<String, Object> result, List<String> failedWorkflowList, boolean isCopy) {
        if (!failedWorkflowList.isEmpty()) {
            String failedWorkflow = String.join(",", failedWorkflowList);
            if (isCopy) {
                log.error(
                        "Copy workflow definition error, srcProjectCode:{}, targetProjectCode:{}, failedWorkflowList:{}.",
                        srcProjectCode, targetProjectCode, failedWorkflow);
                putMsg(result, Status.COPY_WORKFLOW_DEFINITION_ERROR, srcProjectCode, targetProjectCode,
                        failedWorkflow);
            } else {
                log.error(
                        "Move workflow definition error, srcProjectCode:{}, targetProjectCode:{}, failedWorkflowList:{}.",
                        srcProjectCode, targetProjectCode, failedWorkflow);
                putMsg(result, Status.MOVE_WORKFLOW_DEFINITION_ERROR, srcProjectCode, targetProjectCode,
                        failedWorkflow);
            }
        } else {
            log.info("Batch {} workflow definition complete, srcProjectCode:{}, targetProjectCode:{}.",
                    isCopy ? "copy" : "move", srcProjectCode, targetProjectCode);
            putMsg(result, Status.SUCCESS);
        }
    }

    /**
     * query the pagination versions info by one certain workflow definition code
     *
     * @param loginUser   login user info to check auth
     * @param projectCode project code
     * @param pageNo      page number
     * @param pageSize    page size
     * @param code        workflow definition code
     * @return the pagination workflow definition versions info of the certain workflow definition
     */
    @Override
    public Result queryWorkflowDefinitionVersions(User loginUser, long projectCode, int pageNo, int pageSize,
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
        PageInfo<WorkflowDefinitionLog> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<WorkflowDefinitionLog> page = new Page<>(pageNo, pageSize);
        IPage<WorkflowDefinitionLog> workflowDefinitionLogIPage =
                workflowDefinitionLogMapper.queryWorkflowDefinitionVersionsPaging(page, code, projectCode);
        List<WorkflowDefinitionLog> workflowDefinitionLogs = workflowDefinitionLogIPage.getRecords();

        pageInfo.setTotalList(workflowDefinitionLogs);
        pageInfo.setTotal((int) workflowDefinitionLogIPage.getTotal());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * delete one certain workflow definition by version number and workflow definition code
     *
     * @param loginUser   login user info to check auth
     * @param projectCode project code
     * @param code        workflow definition code
     * @param version     version number
     */
    @Override
    @Transactional
    public void deleteWorkflowDefinitionVersion(User loginUser,
                                                long projectCode,
                                                long code,
                                                int version) {
        projectService.checkHasProjectWritePermissionThrowException(loginUser, projectCode);

        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(code);
        if (workflowDefinition == null || projectCode != workflowDefinition.getProjectCode()) {
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST, code);
        }
        if (workflowDefinition.getVersion() == version) {
            log.warn("This version: {} of workflow: {} is the main version cannot delete by version", code, version);
            throw new ServiceException(Status.MAIN_TABLE_USING_VERSION);
        }
        // check whether there exist running workflow instance under the workflow definition
        List<WorkflowInstance> workflowInstances = workflowInstanceService.queryByWorkflowCodeVersionStatus(
                code,
                version,
                WorkflowExecutionStatus.getNotTerminalStatus());
        if (CollectionUtils.isNotEmpty(workflowInstances)) {
            throw new ServiceException(Status.DELETE_WORKFLOW_DEFINITION_EXECUTING_FAIL, workflowInstances.size());
        }

        int deleteLog = workflowDefinitionLogMapper.deleteByWorkflowDefinitionCodeAndVersion(code, version);
        int deleteRelationLog = workflowTaskRelationLogMapper.deleteByCode(code, version);
        if (deleteLog == 0 || deleteRelationLog == 0) {
            throw new ServiceException(Status.DELETE_WORKFLOW_DEFINE_BY_CODE_ERROR);
        }
        log.info("Delete version: {} of workflow: {}, projectCode: {}", version, code, projectCode);

        // delete workflow lineage
        int deleteWorkflowLineageResult = workflowLineageService.deleteWorkflowLineage(Collections.singletonList(code));
        if (deleteWorkflowLineageResult <= 0) {
            log.error("Delete workflow lineage by workflow definition code error, workflowDefinitionCode: {}", code);
            throw new ServiceException(Status.DELETE_WORKFLOW_LINEAGE_ERROR);
        }
    }

    private void updateWorkflowValid(User user, WorkflowDefinition oldWorkflowDefinition,
                                     WorkflowDefinition newWorkflowDefinition) {
        // online can not permit edit
        if (oldWorkflowDefinition.getReleaseState() == ReleaseState.ONLINE) {
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_ALLOWED_EDIT, oldWorkflowDefinition.getName());
        }

        Project project = projectMapper.queryByCode(oldWorkflowDefinition.getProjectCode());
        // check user access for project
        projectService.checkProjectAndAuthThrowException(user, project, WORKFLOW_UPDATE);

        if (checkDescriptionLength(newWorkflowDefinition.getDescription())) {
            throw new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR);
        }

        // check whether the new workflow define name exist
        if (!oldWorkflowDefinition.getName().equals(newWorkflowDefinition.getName())) {
            WorkflowDefinition definition = workflowDefinitionMapper
                    .verifyByDefineName(newWorkflowDefinition.getProjectCode(), newWorkflowDefinition.getName());
            if (definition != null) {
                throw new ServiceException(Status.WORKFLOW_DEFINITION_NAME_EXIST, newWorkflowDefinition.getName());
            }
        }
    }

    /**
     * update single resource workflow
     *
     * @param loginUser             login user
     * @param workflowCode          workflow resource code want to update
     * @param workflowUpdateRequest workflow update resource object
     * @return workflow definition
     */
    @Override
    @Transactional
    public WorkflowDefinition updateSingleWorkflowDefinition(User loginUser,
                                                             long workflowCode,
                                                             WorkflowUpdateRequest workflowUpdateRequest) {
        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(workflowCode);
        // check workflow definition exists
        if (workflowDefinition == null) {
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST, workflowCode);
        }

        WorkflowDefinition workflowDefinitionUpdate =
                workflowUpdateRequest.mergeIntoWorkflowDefinition(workflowDefinition);
        this.updateWorkflowValid(loginUser, workflowDefinition, workflowDefinitionUpdate);

        int insertVersion = this.saveWorkflowDefine(loginUser, workflowDefinitionUpdate);
        if (insertVersion == 0) {
            log.error("Update workflow definition error, projectCode:{}, workflowDefinitionName:{}.",
                    workflowDefinitionUpdate.getCode(),
                    workflowDefinitionUpdate.getName());
            throw new ServiceException(Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
        }

        int insertRelationVersion = this.saveTaskRelation(loginUser, workflowDefinitionUpdate, insertVersion);
        if (insertRelationVersion != Constants.EXIT_CODE_SUCCESS) {
            log.error(
                    "Save workflow task relations error, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                    workflowDefinition.getProjectCode(), workflowDefinition.getCode(), insertVersion);
            throw new ServiceException(Status.CREATE_WORKFLOW_TASK_RELATION_ERROR);
        }
        log.info(
                "Save workflow task relations complete, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                workflowDefinition.getProjectCode(), workflowDefinition.getCode(), insertVersion);
        workflowDefinitionUpdate.setVersion(insertVersion);
        return workflowDefinitionUpdate;
    }

    public int saveWorkflowDefine(User loginUser, WorkflowDefinition workflowDefinition) {
        WorkflowDefinitionLog workflowDefinitionLog = new WorkflowDefinitionLog(workflowDefinition);
        Integer version = workflowDefinitionLogMapper.queryMaxVersionForDefinition(workflowDefinition.getCode());
        int insertVersion = version == null || version == 0 ? Constants.VERSION_FIRST : version + 1;
        workflowDefinitionLog.setVersion(insertVersion);
        workflowDefinition.setVersion(insertVersion);

        workflowDefinitionLog.setOperator(loginUser.getId());
        workflowDefinition.setUserId(loginUser.getId());
        workflowDefinitionLog.setOperateTime(workflowDefinition.getUpdateTime());
        workflowDefinition.setUpdateTime(workflowDefinition.getUpdateTime());
        workflowDefinitionLog.setId(null);
        int result = workflowDefinitionMapper.updateById(workflowDefinition);

        int insertLog = workflowDefinitionLogMapper.insert(workflowDefinitionLog);
        workflowDefinitionLog.setId(workflowDefinition.getId());
        return (insertLog & result) > 0 ? insertVersion : 0;
    }

    public int saveTaskRelation(User loginUser, WorkflowDefinition workflowDefinition,
                                int workflowDefinitionVersion) {
        long projectCode = workflowDefinition.getProjectCode();
        long workflowDefinitionCode = workflowDefinition.getCode();
        List<WorkflowTaskRelation> taskRelations =
                workflowTaskRelationMapper.queryByWorkflowDefinitionCode(workflowDefinitionCode);
        List<WorkflowTaskRelationLog> taskRelationList =
                taskRelations.stream().map(WorkflowTaskRelationLog::new).collect(Collectors.toList());

        List<Long> taskCodeList =
                taskRelations.stream().map(WorkflowTaskRelation::getPostTaskCode).collect(Collectors.toList());
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
        for (WorkflowTaskRelationLog workflowTaskRelationLog : taskRelationList) {
            workflowTaskRelationLog.setProjectCode(projectCode);
            workflowTaskRelationLog.setWorkflowDefinitionCode(workflowDefinitionCode);
            workflowTaskRelationLog.setWorkflowDefinitionVersion(workflowDefinitionVersion);
            if (taskDefinitionLogMap != null) {
                TaskDefinitionLog preTaskDefinitionLog =
                        taskDefinitionLogMap.get(workflowTaskRelationLog.getPreTaskCode());
                if (preTaskDefinitionLog != null) {
                    workflowTaskRelationLog.setPreTaskVersion(preTaskDefinitionLog.getVersion());
                }
                TaskDefinitionLog postTaskDefinitionLog =
                        taskDefinitionLogMap.get(workflowTaskRelationLog.getPostTaskCode());
                if (postTaskDefinitionLog != null) {
                    workflowTaskRelationLog.setPostTaskVersion(postTaskDefinitionLog.getVersion());
                }
            }
            workflowTaskRelationLog.setCreateTime(now);
            workflowTaskRelationLog.setUpdateTime(now);
            workflowTaskRelationLog.setOperator(loginUser.getId());
            workflowTaskRelationLog.setOperateTime(now);
        }
        if (!taskRelations.isEmpty()) {
            Set<Integer> workflowTaskRelationSet =
                    taskRelations.stream().map(WorkflowTaskRelation::hashCode).collect(toSet());
            Set<Integer> taskRelationSet =
                    taskRelationList.stream().map(WorkflowTaskRelationLog::hashCode).collect(toSet());
            boolean isSame = CollectionUtils.isEqualCollection(workflowTaskRelationSet,
                    taskRelationSet);
            if (isSame) {
                log.info("workflow task relations is non-existent, projectCode:{}, workflowDefinitionCode:{}.",
                        workflowDefinition.getProjectCode(), workflowDefinition.getCode());
                return Constants.EXIT_CODE_SUCCESS;
            }
            workflowTaskRelationMapper.deleteByWorkflowDefinitionCode(projectCode, workflowDefinitionCode);
        }
        List<WorkflowTaskRelation> workflowTaskRelations =
                taskRelationList.stream().map(WorkflowTaskRelation::new).collect(Collectors.toList());
        int insert = workflowTaskRelationMapper.batchInsert(workflowTaskRelations);
        int resultLog = workflowTaskRelationLogMapper.batchInsert(taskRelationList);
        return (insert & resultLog) > 0 ? Constants.EXIT_CODE_SUCCESS : Constants.EXIT_CODE_FAILURE;
    }

    @Transactional
    @Override
    public void onlineWorkflowDefinition(User loginUser, Long projectCode, Long workflowDefinitionCode) {
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, WORKFLOW_ONLINE_OFFLINE);

        WorkflowDefinition workflowDefinition = workflowDefinitionDao.queryByCode(workflowDefinitionCode)
                .orElseThrow(() -> new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST, workflowDefinitionCode));

        if (ReleaseState.ONLINE.equals(workflowDefinition.getReleaseState())) {
            // do nothing if the workflow is already online
            return;
        }

        checkWorkflowDefinitionIsValidated(workflowDefinition.getCode());
        checkAllSubWorkflowDefinitionIsOnline(workflowDefinition.getCode());

        workflowDefinition.setReleaseState(ReleaseState.ONLINE);
        workflowDefinitionDao.updateById(workflowDefinition);
    }

    @Transactional
    @Override
    public void offlineWorkflowDefinition(User loginUser, Long projectCode, Long workflowDefinitionCode) {
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, WORKFLOW_ONLINE_OFFLINE);

        WorkflowDefinition workflowDefinition = workflowDefinitionDao.queryByCode(workflowDefinitionCode)
                .orElseThrow(() -> new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST, workflowDefinitionCode));

        if (ReleaseState.OFFLINE.equals(workflowDefinition.getReleaseState())) {
            // do nothing if the workflow is already offline
            return;
        }

        workflowDefinition.setReleaseState(ReleaseState.OFFLINE);
        workflowDefinitionDao.updateById(workflowDefinition);

        schedulerService.offlineSchedulerByWorkflowCode(workflowDefinitionCode);
    }

    /**
     * view workflow variables
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param code        workflow definition code
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

        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(code);

        if (Objects.isNull(workflowDefinition) || projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, projectCode:{}, workflowDefinitionCode:{}.", projectCode,
                    code);
            putMsg(result, WORKFLOW_DEFINITION_NOT_EXIST, code);
            return result;
        }

        // global params
        List<Property> globalParams = workflowDefinition.getGlobalParamList();

        Map<String, Map<String, Object>> localUserDefParams = getLocalParams(workflowDefinition);

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
    private Map<String, Map<String, Object>> getLocalParams(WorkflowDefinition workflowDefinition) {
        Map<String, Map<String, Object>> localUserDefParams = new HashMap<>();

        Set<Long> taskCodeSet = new TreeSet<>();

        workflowTaskRelationMapper.queryByWorkflowDefinitionCode(workflowDefinition.getCode())
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

    private void checkWorkflowDefinitionIsValidated(Long workflowDefinitionCode) {
        // todo: build dag check if the dag is validated
        List<WorkflowTaskRelation> workflowTaskRelations =
                workflowTaskRelationMapper.queryByWorkflowDefinitionCode(workflowDefinitionCode);
        if (CollectionUtils.isEmpty(workflowTaskRelations)) {
            throw new ServiceException(Status.WORKFLOW_DAG_IS_EMPTY);
        }
        // todo : check Workflow is validate
    }

    private void checkAllSubWorkflowDefinitionIsOnline(Long workflowDefinitionCode) {
        List<Long> allSubWorkflowDefinitionCodes =
                processService.findAllSubWorkflowDefinitionCode(workflowDefinitionCode);
        if (CollectionUtils.isEmpty(allSubWorkflowDefinitionCodes)) {
            return;
        }
        for (Long subWorkflowDefinitionCode : allSubWorkflowDefinitionCodes) {
            WorkflowDefinition subWorkflowDefinition = workflowDefinitionDao.queryByCode(subWorkflowDefinitionCode)
                    .orElseThrow(() -> new ServiceException(WORKFLOW_DEFINITION_NOT_EXIST, workflowDefinitionCode));
            if (!ReleaseState.ONLINE.equals(subWorkflowDefinition.getReleaseState())) {
                throw new ServiceException(
                        "SubWorkflowDefinition " + subWorkflowDefinition.getName() + " is not online");
            }
        }
    }
}
