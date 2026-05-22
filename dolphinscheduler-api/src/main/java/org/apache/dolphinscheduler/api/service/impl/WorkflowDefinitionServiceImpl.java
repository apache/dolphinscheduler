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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_DEFINITION_MOVE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.VERSION_LIST;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_BATCH_COPY;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_DEFINITION;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_DEFINITION_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_ONLINE_OFFLINE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_SWITCH_TO_THIS_VERSION;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_TREE_VIEW;
import static org.apache.dolphinscheduler.api.enums.Status.WORKFLOW_DEFINITION_NOT_EXIST;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_SUB_WORKFLOW_DEFINITION_CODE;
import static org.apache.dolphinscheduler.common.constants.Constants.COPY_SUFFIX;
import static org.apache.dolphinscheduler.common.constants.Constants.LOCAL_PARAMS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.LOCAL_PARAMS_LIST;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager.checkTaskParameters;

import org.apache.dolphinscheduler.api.dto.TaskCodeVersionDto;
import org.apache.dolphinscheduler.api.dto.treeview.Instance;
import org.apache.dolphinscheduler.api.dto.treeview.TreeViewDto;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowDefinitionVariablesDTO;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.service.TaskDefinitionLogService;
import org.apache.dolphinscheduler.api.service.TaskDefinitionService;
import org.apache.dolphinscheduler.api.service.WorkflowDefinitionService;
import org.apache.dolphinscheduler.api.service.WorkflowInstanceService;
import org.apache.dolphinscheduler.api.service.WorkflowLineageService;
import org.apache.dolphinscheduler.api.utils.CheckUtils;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.validator.GlobalParamsValidator;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DagData;
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
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.model.PageListingResult;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.ScheduleDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.UserDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowTaskRelationDao;
import org.apache.dolphinscheduler.plugin.task.api.model.ConditionDependentItem;
import org.apache.dolphinscheduler.plugin.task.api.model.ConditionDependentTaskModel;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentTaskModel;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.SwitchResultVo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ConditionsParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

@Service
@Slf4j
public class WorkflowDefinitionServiceImpl extends BaseServiceImpl implements WorkflowDefinitionService {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private WorkflowDefinitionLogMapper workflowDefinitionLogMapper;

    @Autowired
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Autowired
    private WorkflowDefinitionLogDao workflowDefinitionLogDao;

    @Lazy
    @Autowired
    private WorkflowInstanceService workflowInstanceService;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private ScheduleDao scheduleDao;

    @Autowired
    private ProcessService processService;

    @Autowired
    private TaskDefinitionLogDao taskDefinitionLogDao;

    @Autowired
    private WorkflowTaskRelationDao workflowTaskRelationDao;

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
    private TaskDefinitionDao taskDefinitionDao;

    @Lazy
    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private WorkflowLineageService workflowLineageService;

    @Autowired
    private GlobalParamsValidator globalParamsValidator;

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
    public WorkflowDefinition createWorkflowDefinition(User loginUser,
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
        Project project = projectDao.queryByCode(projectCode);

        // check if user have write perm for project
        projectService.checkHasProjectWritePermissionThrowException(loginUser, project);

        if (checkDescriptionLength(description)) {
            log.warn("Parameter description is too long.");
            throw new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR);
        }
        // check whether the new workflow definition name exist
        WorkflowDefinition definition = workflowDefinitionDao.verifyByDefineName(project.getCode(), name);
        if (definition != null) {
            log.warn("workflow definition with the same name {} already exists, workflowDefinitionCode:{}.",
                    definition.getName(), definition.getCode());
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NAME_EXIST, name);
        }

        globalParamsValidator.validate(globalParams);

        List<TaskDefinitionLog> taskDefinitionLogs = generateTaskDefinitionList(taskDefinitionJson);
        List<WorkflowTaskRelationLog> taskRelationList = generateTaskRelationList(taskRelationJson, taskDefinitionLogs);

        long workflowDefinitionCode = CodeGenerateUtils.genCode();
        WorkflowDefinition workflowDefinition =
                new WorkflowDefinition(projectCode, name, workflowDefinitionCode, description,
                        globalParams, locations, timeout, loginUser.getId());
        workflowDefinition.setExecutionType(executionType);

        return createDagDefine(loginUser, taskRelationList, workflowDefinition, taskDefinitionLogs);
    }

    protected WorkflowDefinition createDagDefine(User loginUser,
                                                 List<WorkflowTaskRelationLog> taskRelationList,
                                                 WorkflowDefinition workflowDefinition,
                                                 List<TaskDefinitionLog> taskDefinitionLogs) {
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
        }
        log.info("Save workflow definition complete, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                workflowDefinition.getCode(), insertVersion);
        workflowDefinition.setVersion(insertVersion);
        int insertResult = processService.saveTaskRelation(loginUser, workflowDefinition.getProjectCode(),
                workflowDefinition.getCode(),
                insertVersion, taskRelationList, taskDefinitionLogs, Boolean.TRUE);
        if (insertResult != Constants.EXIT_CODE_SUCCESS) {
            log.error(
                    "Save workflow task relations error, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                    workflowDefinition.getProjectCode(), workflowDefinition.getCode(), insertVersion);
            throw new ServiceException(Status.CREATE_WORKFLOW_TASK_RELATION_ERROR);
        }
        log.info(
                "Save workflow task relations complete, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                workflowDefinition.getProjectCode(), workflowDefinition.getCode(), insertVersion);

        saveWorkflowLineage(workflowDefinition.getProjectCode(), workflowDefinition.getCode(),
                insertVersion, taskDefinitionLogs);
        return workflowDefinition;
    }

    @Override
    public void saveWorkflowLineage(long projectCode,
                                    long workflowDefinitionCode,
                                    int workflowDefinitionVersion,
                                    List<TaskDefinitionLog> taskDefinitionLogList) {
        List<WorkflowTaskLineage> workflowTaskLineageList = generateWorkflowLineageList(taskDefinitionLogList,
                workflowDefinitionCode, workflowDefinitionVersion);

        workflowLineageService.updateWorkflowLineage(workflowDefinitionCode, workflowTaskLineageList);
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

            Set<String> taskNameSet = new HashSet<>();
            for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
                if (!taskNameSet.add(taskDefinitionLog.getName())) {
                    log.error(
                            "Generate task definition list failed, the given task definition name is duplicate, taskName: {}, taskDefinition: {}",
                            taskDefinitionLog.getName(), taskDefinitionLog);
                    throw new ServiceException(Status.TASK_NAME_DUPLICATE_ERROR, taskDefinitionLog.getName());
                }

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
    public List<DagData> queryWorkflowDefinitionList(User loginUser, long projectCode) {
        Project project = projectDao.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_DEFINITION);

        List<WorkflowDefinition> resourceList = workflowDefinitionDao.queryAllDefinitionList(projectCode);
        return resourceList.stream().map(processService::genDagData).collect(Collectors.toList());
    }

    /**
     * query workflow definition simple list
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return definition simple list
     */
    @Override
    public ArrayNode queryWorkflowDefinitionSimpleList(User loginUser, long projectCode) {
        Project project = projectDao.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_DEFINITION);

        List<WorkflowDefinition> workflowDefinitions = workflowDefinitionDao.queryAllDefinitionList(projectCode);
        ArrayNode arrayNode = JSONUtils.createArrayNode();
        for (WorkflowDefinition workflowDefinition : workflowDefinitions) {
            ObjectNode workflowDefinitionNode = JSONUtils.createObjectNode();
            workflowDefinitionNode.put("id", workflowDefinition.getId());
            workflowDefinitionNode.put("code", workflowDefinition.getCode());
            workflowDefinitionNode.put("name", workflowDefinition.getName());
            workflowDefinitionNode.put("projectCode", workflowDefinition.getProjectCode());
            arrayNode.add(workflowDefinitionNode);
        }
        return arrayNode;
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
        List<UserWithWorkflowDefinitionCode> userWithCodes = userDao.queryUserWithWorkflowDefinitionCode(
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
     * query detail of workflow definition
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param code        workflow definition code
     * @return workflow definition detail
     */
    @Override
    public DagData queryWorkflowDefinitionByCode(User loginUser, long projectCode, long code) {
        Project project = projectDao.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_DEFINITION);

        WorkflowDefinition workflowDefinition = workflowDefinitionDao.queryByCode(code).orElse(null);
        if (workflowDefinition == null || projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, workflowDefinitionCode:{}.", code);
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST, String.valueOf(code));
        }
        return processService.genDagData(workflowDefinition);
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
    public DagData queryWorkflowDefinitionByName(User loginUser, long projectCode, String name) {
        Project project = projectDao.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_DEFINITION);

        WorkflowDefinition workflowDefinition = workflowDefinitionDao.queryByDefineName(projectCode, name);

        if (workflowDefinition == null) {
            log.error("workflow definition does not exist, projectCode:{}.", projectCode);
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST, name);
        }
        return processService.genDagData(workflowDefinition);
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
    public WorkflowDefinition updateWorkflowDefinition(User loginUser,
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
        Project project = projectDao.queryByCode(projectCode);
        // check if user have write perm for project
        projectService.checkHasProjectWritePermissionThrowException(loginUser, project);

        if (checkDescriptionLength(description)) {
            log.warn("Parameter description is too long.");
            throw new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR);
        }

        globalParamsValidator.validate(globalParams);

        List<TaskDefinitionLog> taskDefinitionLogs = generateTaskDefinitionList(taskDefinitionJson);
        List<WorkflowTaskRelationLog> taskRelationList = generateTaskRelationList(taskRelationJson, taskDefinitionLogs);

        WorkflowDefinition workflowDefinition = workflowDefinitionDao.queryByCode(code).orElse(null);
        // check workflow definition exists
        if (workflowDefinition == null || projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, workflowDefinitionCode:{}.", code);
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST, String.valueOf(code));
        }
        if (workflowDefinition.getReleaseState() == ReleaseState.ONLINE) {
            // online can not permit edit
            log.warn("workflow definition is not allowed to be modified due to {}, workflowDefinitionCode:{}.",
                    ReleaseState.ONLINE.getDescp(), workflowDefinition.getCode());
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_ALLOWED_EDIT, workflowDefinition.getName());
        }
        if (!name.equals(workflowDefinition.getName())) {
            // check whether the new workflow define name exist
            WorkflowDefinition definition = workflowDefinitionDao.verifyByDefineName(project.getCode(), name);
            if (definition != null) {
                log.warn("workflow definition with the same name already exists, workflowDefinitionCode:{}.",
                        definition.getCode());
                throw new ServiceException(Status.WORKFLOW_DEFINITION_NAME_EXIST, name);
            }
        }
        WorkflowDefinition workflowDefinitionDeepCopy =
                JSONUtils.parseObject(JSONUtils.toJsonString(workflowDefinition), WorkflowDefinition.class);
        workflowDefinition.set(projectCode, name, description, globalParams, locations, timeout);
        workflowDefinition.setExecutionType(executionType);
        return updateDagDefine(loginUser, taskRelationList, workflowDefinition, workflowDefinitionDeepCopy,
                taskDefinitionLogs);
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
                workflowTaskRelationDao.queryByWorkflowDefinitionCode(workflowDefinition.getCode());
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

    protected WorkflowDefinition updateDagDefine(User loginUser,
                                                 List<WorkflowTaskRelationLog> taskRelationList,
                                                 WorkflowDefinition workflowDefinition,
                                                 WorkflowDefinition workflowDefinitionDeepCopy,
                                                 List<TaskDefinitionLog> taskDefinitionLogs) {
        int saveTaskResult = processService.saveTaskDefine(loginUser, workflowDefinition.getProjectCode(),
                taskDefinitionLogs, Boolean.TRUE);
        if (saveTaskResult == Constants.EXIT_CODE_SUCCESS) {
            log.info("The task has not changed, so skip");
        }
        if (saveTaskResult == Constants.DEFINITION_FAILURE) {
            log.error("Update task definitions error, projectCode:{}, workflowDefinitionCode:{}.",
                    workflowDefinition.getProjectCode(), workflowDefinition.getCode());
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
        if (!isChange) {
            log.info(
                    "workflow definition does not need to be updated because there is no change, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                    workflowDefinition.getProjectCode(), workflowDefinition.getCode(), workflowDefinition.getVersion());
            return workflowDefinition;
        }
        log.info(
                "workflow definition needs to be updated, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                workflowDefinition.getProjectCode(), workflowDefinition.getCode(), workflowDefinition.getVersion());
        workflowDefinition.setUpdateTime(new Date());
        int insertVersion =
                processService.saveWorkflowDefine(loginUser, workflowDefinition, Boolean.TRUE, Boolean.TRUE);
        if (insertVersion <= 0) {
            log.error("Update workflow definition error, workflowDefinitionCode:{}.", workflowDefinition.getCode());
            throw new ServiceException(Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
        }
        log.info(
                "Update workflow definition complete, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                workflowDefinition.getCode(), insertVersion);
        workflowDefinition.setVersion(insertVersion);

        taskUsedInOtherTaskValid(workflowDefinition, taskRelationList);
        int insertResult = processService.saveTaskRelation(loginUser, workflowDefinition.getProjectCode(),
                workflowDefinition.getCode(), insertVersion, taskRelationList, taskDefinitionLogs, Boolean.TRUE);
        if (insertResult != Constants.EXIT_CODE_SUCCESS) {
            log.error(
                    "Update workflow task relations error, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                    workflowDefinition.getProjectCode(), workflowDefinition.getCode(), insertVersion);
            throw new ServiceException(Status.UPDATE_WORKFLOW_DEFINITION_ERROR);
        }
        log.info(
                "Update workflow task relations complete, projectCode:{}, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                workflowDefinition.getProjectCode(), workflowDefinition.getCode(), insertVersion);

        saveWorkflowLineage(workflowDefinition.getProjectCode(), workflowDefinition.getCode(),
                insertVersion, taskDefinitionLogs);
        return workflowDefinition;
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
    public void verifyWorkflowDefinitionName(User loginUser, long projectCode, String name,
                                             long workflowDefinitionCode) {
        Project project = projectDao.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_CREATE);

        WorkflowDefinition workflowDefinition =
                workflowDefinitionDao.verifyByDefineName(project.getCode(), name.trim());
        if (workflowDefinition == null) {
            return;
        }
        if (workflowDefinitionCode != 0 && workflowDefinitionCode == workflowDefinition.getCode()) {
            return;
        }
        log.warn("workflow definition with the same name {} already exists, workflowDefinitionCode:{}.",
                workflowDefinition.getName(), workflowDefinition.getCode());
        throw new ServiceException(Status.WORKFLOW_DEFINITION_NAME_EXIST, name.trim());
    }

    @Override
    @Transactional
    public void batchDeleteWorkflowDefinitionByCodes(User loginUser, long projectCode, String codes) {
        if (StringUtils.isEmpty(codes)) {
            log.error("Parameter workflowDefinitionCodes is empty, projectCode is {}.", projectCode);
            throw new ServiceException(Status.WORKFLOW_DEFINITION_CODES_IS_EMPTY);
        }

        Set<Long> definitionCodes = Lists.newArrayList(codes.split(Constants.COMMA)).stream().map(Long::parseLong)
                .collect(Collectors.toSet());
        List<WorkflowDefinition> workflowDefinitionList = workflowDefinitionDao.queryByCodes(definitionCodes);
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
            } catch (Exception e) {
                throw new ServiceException(Status.DELETE_WORKFLOW_DEFINE_ERROR, workflowDefinition.getName(),
                        e.getMessage());
            }
        }
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
                workflowDefinition.getCode(), WorkflowExecutionStatus.NOT_TERMINAL_STATES);
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

        Project project = projectDao.queryByCode(workflowDefinition.getProjectCode());
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_DEFINITION_DELETE);

        // Determine if the login user is the owner of the workflow definition
        if (loginUser.getId() != workflowDefinition.getUserId() && loginUser.getUserType() != UserType.ADMIN_USER) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        workflowDefinitionUsedInOtherTaskValid(workflowDefinition);

        // get the timing according to the workflow definition
        Schedule scheduleObj = scheduleDao.queryByWorkflowDefinitionCode(code);
        if (scheduleObj != null) {
            if (scheduleObj.getReleaseState() == ReleaseState.OFFLINE) {
                boolean delete = scheduleDao.deleteById(scheduleObj.getId());
                if (!delete) {
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

        // delete workflow lineage (lineage data only keeps one record per workflow code)
        // It's safe to return 0 if no lineage exists (idempotent)
        int deleteWorkflowLineageResult = workflowLineageService
                .deleteWorkflowLineage(Collections.singletonList(workflowDefinition.getCode()));
        if (deleteWorkflowLineageResult <= 0) {
            if (deleteWorkflowLineageResult < 0) {
                throw new ServiceException(Status.DELETE_WORKFLOW_LINEAGE_ERROR);
            } else {
                log.warn("No workflow lineage to delete, workflowDefinitionCode: {}", code);
            }
        }
        log.info("Success delete workflow definition workflowDefinitionCode: {}", code);
    }

    /**
     * check the workflow task relation json
     *
     * @param workflowTaskRelationJson workflow task relation json
     * @return check result code
     */
    @Override
    public void checkWorkflowNodeList(String workflowTaskRelationJson,
                                      List<TaskDefinitionLog> taskDefinitionLogsList) {
        try {
            if (workflowTaskRelationJson == null) {
                log.error("workflow task relation data is null.");
                throw new ServiceException(Status.DATA_IS_NOT_VALID, "null");
            }

            List<WorkflowTaskRelation> taskRelationList =
                    JSONUtils.toList(workflowTaskRelationJson, WorkflowTaskRelation.class);
            // Check whether the task node is normal
            List<TaskNode> taskNodes = processService.transformTask(taskRelationList, taskDefinitionLogsList);

            if (CollectionUtils.isEmpty(taskNodes)) {
                log.error("Task node data is empty.");
                throw new ServiceException(Status.WORKFLOW_DAG_IS_EMPTY);
            }

            // check has cycle
            if (graphHasCycle(taskNodes)) {
                log.error("workflow DAG has cycle.");
                throw new ServiceException(Status.WORKFLOW_NODE_HAS_CYCLE);
            }

            // check whether the workflow definition json is normal
            for (TaskNode taskNode : taskNodes) {
                if (!checkTaskParameters(taskNode.getType(), taskNode.getParams())) {
                    throw new ServiceException(Status.WORKFLOW_NODE_S_PARAMETER_INVALID, taskNode.getName());
                }

                // check extra params
                CheckUtils.checkOtherParams(taskNode.getExtras());
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error(Status.INTERNAL_SERVER_ERROR_ARGS.getMsg(), e);
            throw new ServiceException(Status.INTERNAL_SERVER_ERROR_ARGS, e.getMessage());
        }
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
    public List<TaskDefinition> getTaskNodeListByDefinitionCode(User loginUser, long projectCode, long code) {
        Project project = projectDao.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, null);

        WorkflowDefinition workflowDefinition = workflowDefinitionDao.queryByCode(code).orElse(null);
        if (workflowDefinition == null || projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, workflowDefinitionCode:{}.", code);
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST, String.valueOf(code));
        }
        DagData dagData = processService.genDagData(workflowDefinition);
        return dagData.getTaskDefinitionList();
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
    public Map<Long, List<TaskDefinition>> getNodeListMapByDefinitionCodes(User loginUser, long projectCode,
                                                                           String codes) {
        Project project = projectDao.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, null);

        Set<Long> defineCodeSet = Lists.newArrayList(codes.split(Constants.COMMA)).stream().map(Long::parseLong)
                .collect(Collectors.toSet());
        List<WorkflowDefinition> workflowDefinitionList = workflowDefinitionDao.queryByCodes(defineCodeSet);
        if (CollectionUtils.isEmpty(workflowDefinitionList)) {
            log.error("workflow definitions do not exist, codes:{}.", defineCodeSet);
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST, codes);
        }
        HashMap<Long, Project> userProjects = new HashMap<>(Constants.DEFAULT_HASH_MAP_SIZE);
        projectDao.queryProjectCreatedAndAuthorizedByUserId(loginUser.getId())
                .forEach(userProject -> userProjects.put(userProject.getCode(), userProject));

        // check workflowDefinition exist in project
        List<WorkflowDefinition> workflowDefinitionListInProject = workflowDefinitionList.stream()
                .filter(o -> userProjects.containsKey(o.getProjectCode())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(workflowDefinitionListInProject)) {
            log.error("workflow definitions do not exist in project, codes:{}.", codes);
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST, codes);
        }
        Map<Long, List<TaskDefinition>> taskNodeMap = new HashMap<>();
        for (WorkflowDefinition workflowDefinition : workflowDefinitionListInProject) {
            DagData dagData = processService.genDagData(workflowDefinition);
            taskNodeMap.put(workflowDefinition.getCode(), dagData.getTaskDefinitionList());
        }
        return taskNodeMap;
    }

    /**
     * query workflow definition all by project code
     *
     * @param loginUser   loginUser
     * @param projectCode project code
     * @return workflow definitions in the project
     */
    @Override
    public List<DagData> queryAllWorkflowDefinitionByProjectCode(User loginUser, long projectCode) {
        Project project = projectDao.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_DEFINITION);

        List<WorkflowDefinition> workflowDefinitions = workflowDefinitionDao.queryAllDefinitionList(projectCode);
        return workflowDefinitions.stream().map(processService::genDagData).collect(Collectors.toList());
    }

    /**
     * query workflow definition list by project code
     *
     * @param projectCode project code
     * @return workflow definition list in the project
     */
    @Override
    public List<DependentSimplifyDefinition> queryWorkflowDefinitionListByProjectCode(long projectCode) {
        return workflowDefinitionDao.queryDefinitionListByProjectCodeAndWorkflowDefinitionCodes(projectCode, null);
    }

    /**
     * query workflow definition list by workflow definition code
     *
     * @param projectCode           project code
     * @param workflowDefinitionCode workflow definition code
     * @return task definition list in the workflow definition
     */
    @Override
    public List<DependentSimplifyDefinition> queryTaskDefinitionListByWorkflowDefinitionCode(long projectCode,
                                                                                             Long workflowDefinitionCode) {
        Set<Long> definitionCodesSet = new HashSet<>();
        definitionCodesSet.add(workflowDefinitionCode);
        List<DependentSimplifyDefinition> workflowDefinitions = workflowDefinitionDao
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
        return taskDefinitionList;
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
    public TreeViewDto viewTree(User loginUser, long projectCode, long code, Integer limit) {
        Project project = projectDao.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_TREE_VIEW);

        WorkflowDefinition workflowDefinition = workflowDefinitionDao.queryByCode(code).orElse(null);
        if (null == workflowDefinition || projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, code:{}.", code);
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST, String.valueOf(code));
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
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR);
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
                            taskInstanceDao.queryByWorkflowInstanceIdAndTaskCode(workflowInstance.getId(), nodeCode);
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
        return parentTreeViewDto;
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
    public void batchCopyWorkflowDefinition(User loginUser,
                                            long projectCode,
                                            String codes,
                                            long targetProjectCode) {
        checkParams(loginUser, projectCode, codes, targetProjectCode, WORKFLOW_BATCH_COPY);
        List<String> failedWorkflowList = new ArrayList<>();
        doBatchOperateWorkflowDefinition(loginUser, targetProjectCode, failedWorkflowList, codes, true);
        checkBatchOperateResult(projectCode, targetProjectCode, failedWorkflowList, true);
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
    public void batchMoveWorkflowDefinition(User loginUser,
                                            long projectCode,
                                            String codes,
                                            long targetProjectCode) {
        checkParams(loginUser, projectCode, codes, targetProjectCode, TASK_DEFINITION_MOVE);
        if (projectCode == targetProjectCode) {
            log.warn("Project code is same as target project code, projectCode:{}.", projectCode);
            return;
        }

        List<String> failedWorkflowList = new ArrayList<>();
        doBatchOperateWorkflowDefinition(loginUser, targetProjectCode, failedWorkflowList, codes, false);
        checkBatchOperateResult(projectCode, targetProjectCode, failedWorkflowList, false);
    }

    private void checkParams(User loginUser,
                             long projectCode,
                             String workflowDefinitionCodes,
                             long targetProjectCode, String perm) {
        Project project = projectDao.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, perm);

        if (StringUtils.isEmpty(workflowDefinitionCodes)) {
            log.error("Parameter workflowDefinitionCodes is empty, projectCode is {}.", projectCode);
            throw new ServiceException(Status.WORKFLOW_DEFINITION_CODES_IS_EMPTY);
        }

        if (projectCode != targetProjectCode) {
            Project targetProject = projectDao.queryByCode(targetProjectCode);
            // check user access for project
            projectService.checkProjectAndAuthThrowException(loginUser, targetProject, perm);
        }
    }

    protected void doBatchOperateWorkflowDefinition(User loginUser,
                                                    long targetProjectCode,
                                                    List<String> failedWorkflowList,
                                                    String workflowDefinitionCodes,
                                                    boolean isCopy) {
        Set<Long> definitionCodes = Arrays.stream(workflowDefinitionCodes.split(Constants.COMMA)).map(Long::parseLong)
                .collect(Collectors.toSet());
        List<WorkflowDefinition> workflowDefinitionList = workflowDefinitionDao.queryByCodes(definitionCodes);
        Set<Long> queryCodes =
                workflowDefinitionList.stream().map(WorkflowDefinition::getCode).collect(Collectors.toSet());
        // definitionCodes - queryCodes
        Set<Long> diffCode =
                definitionCodes.stream().filter(code -> !queryCodes.contains(code)).collect(Collectors.toSet());
        diffCode.forEach(code -> failedWorkflowList.add(code + "[null]"));
        for (WorkflowDefinition workflowDefinition : workflowDefinitionList) {
            List<WorkflowTaskRelation> workflowTaskRelations =
                    workflowTaskRelationDao.queryByWorkflowDefinitionCode(workflowDefinition.getCode());
            List<WorkflowTaskRelationLog> taskRelationList =
                    workflowTaskRelations.stream().map(WorkflowTaskRelationLog::new).collect(Collectors.toList());
            workflowDefinition.setProjectCode(targetProjectCode);
            if (isCopy) {
                log.info("Copy workflow definition...");
                List<TaskDefinitionLog> taskDefinitionLogs =
                        taskDefinitionLogDao.queryTaskDefineLogList(workflowTaskRelations);
                Map<Long, Long> taskCodeMap = new HashMap<>();
                taskDefinitionLogs.forEach(
                        taskDefinitionLog -> taskCodeMap.put(taskDefinitionLog.getCode(), CodeGenerateUtils.genCode()));
                for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
                    taskDefinitionLog.setCode(taskCodeMap.get(taskDefinitionLog.getCode()));
                    taskDefinitionLog.setProjectCode(targetProjectCode);
                    taskDefinitionLog.setVersion(0);
                    taskDefinitionLog.setName(taskDefinitionLog.getName());

                    if (TaskTypeUtils.isSwitchTask(taskDefinitionLog.getTaskType())) {
                        replaceTaskCodeForSwitchTaskParams(taskDefinitionLog, taskCodeMap);
                    }

                    if (TaskTypeUtils.isConditionTask(taskDefinitionLog.getTaskType())) {
                        replaceTaskCodeForConditionTaskParams(taskDefinitionLog, taskCodeMap);
                    }
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
                workflowDefinition.setCode(CodeGenerateUtils.genCode());

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
                Schedule scheduleObj = scheduleDao.queryByWorkflowDefinitionCode(oldWorkflowDefinitionCode);
                if (scheduleObj != null) {
                    scheduleObj.setId(null);
                    scheduleObj.setUserId(loginUser.getId());
                    scheduleObj.setWorkflowDefinitionCode(workflowDefinition.getCode());
                    scheduleObj.setReleaseState(ReleaseState.OFFLINE);
                    scheduleObj.setCreateTime(date);
                    scheduleObj.setUpdateTime(date);
                    int insertResult = scheduleDao.insert(scheduleObj);
                    if (insertResult != 1) {
                        log.error("Schedule create error, workflowDefinitionCode:{}.", workflowDefinition.getCode());
                        throw new ServiceException(Status.CREATE_SCHEDULE_ERROR);
                    }
                }
                try {
                    createDagDefine(loginUser, taskRelationList, workflowDefinition, taskDefinitionLogs);
                } catch (Exception e) {
                    log.error("Copy workflow definition error, workflowDefinitionCode from {} to {}.",
                            oldWorkflowDefinitionCode, workflowDefinition.getCode(), e);
                    failedWorkflowList.add(workflowDefinition.getCode() + "[" + workflowDefinition.getName() + "]");
                }
            } else {
                log.info("Move workflow definition...");
                try {
                    updateDagDefine(loginUser, taskRelationList, workflowDefinition, null,
                            Lists.newArrayList());
                } catch (Exception e) {
                    log.error("Move workflow definition error, workflowDefinitionCode:{}.",
                            workflowDefinition.getCode(), e);
                    failedWorkflowList.add(workflowDefinition.getCode() + "[" + workflowDefinition.getName() + "]");
                }
            }
        }
    }

    /**
     * Replaces old task codes with new ones in the parameters of a Switch task.
     * Used during workflow duplication or import to preserve correct task dependencies.
     */
    private void replaceTaskCodeForSwitchTaskParams(TaskDefinitionLog taskDefLog, Map<Long, Long> taskCodeMap) {
        String taskParams = taskDefLog.getTaskParams();
        SwitchParameters params;

        try {
            params = JSONUtils.parseObject(taskParams, SwitchParameters.class);
        } catch (Exception e) {
            log.warn("Invalid Switch task params: {}", taskParams, e);
            throw new IllegalArgumentException("Failed to parse Switch task params: " + taskParams, e);
        }

        if (params == null) {
            log.warn("Parsed Switch task params is null: {}", taskParams);
            throw new IllegalArgumentException("Failed to parse Switch task params: " + taskParams);
        }

        // Update nextBranch if mapped
        Long nextBranch = params.getNextBranch();
        if (nextBranch != null && taskCodeMap.containsKey(nextBranch)) {
            params.setNextBranch(taskCodeMap.get(nextBranch));
        }

        // Update switch result nodes
        SwitchParameters.SwitchResult result = params.getSwitchResult();
        if (result != null) {
            Long nextNode = result.getNextNode();
            if (nextNode != null && taskCodeMap.containsKey(nextNode)) {
                result.setNextNode(taskCodeMap.get(nextNode));
            }

            // Update depend task list in result
            for (SwitchResultVo vo : result.getDependTaskList()) {
                Long original = vo.getNextNode();
                if (original != null && taskCodeMap.containsKey(original)) {
                    vo.setNextNode(taskCodeMap.get(original));
                }
            }
        }

        taskDefLog.setTaskParams(JSONUtils.toJsonString(params));
    }

    /**
     * Replaces old task codes with new ones in the parameters of a Condition task.
     * Used during workflow duplication or import to preserve correct task dependencies.
     */
    private void replaceTaskCodeForConditionTaskParams(TaskDefinitionLog taskDefLog, Map<Long, Long> taskCodeMap) {
        String taskParams = taskDefLog.getTaskParams();
        ConditionsParameters params;

        try {
            params = JSONUtils.parseObject(taskParams, ConditionsParameters.class);
        } catch (Exception e) {
            log.warn("Invalid Condition task params: {}", taskParams, e);
            throw new IllegalArgumentException("Failed to parse Condition task params: " + taskParams, e);
        }

        if (params == null) {
            log.warn("Parsed Condition task params is null: {}", taskParams);
            throw new IllegalArgumentException("Failed to parse Condition task params: " + taskParams);
        }

        // Update dependency task codes
        ConditionsParameters.ConditionDependency dep = params.getDependence();
        if (dep != null) {
            for (ConditionDependentTaskModel taskModel : dep.getDependTaskList()) {
                for (ConditionDependentItem item : taskModel.getDependItemList()) {
                    Long oldCode = item.getDepTaskCode();
                    if (taskCodeMap.containsKey(oldCode)) {
                        item.setDepTaskCode(taskCodeMap.get(oldCode));
                    }
                }
            }
        }

        // Update success/failed node lists
        ConditionsParameters.ConditionResult result = params.getConditionResult();
        if (result != null) {
            replaceInNodeList(result::getSuccessNode, result::setSuccessNode, taskCodeMap);
            replaceInNodeList(result::getFailedNode, result::setFailedNode, taskCodeMap);
        }

        taskDefLog.setTaskParams(JSONUtils.toJsonString(params));
    }

    // Helper to avoid duplication for success/failed node lists
    private void replaceInNodeList(Supplier<List<Long>> getter, Consumer<List<Long>> setter,
                                   Map<Long, Long> taskCodeMap) {
        List<Long> original = getter.get();
        if (CollectionUtils.isEmpty(original))
            return;

        List<Long> updated = original.stream()
                .map(code -> code != null && taskCodeMap.containsKey(code) ? taskCodeMap.get(code) : code)
                .collect(Collectors.toList());

        setter.accept(updated);
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
    public void switchWorkflowDefinitionVersion(User loginUser, long projectCode, long code,
                                                int version) {
        Project project = projectDao.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_SWITCH_TO_THIS_VERSION);

        WorkflowDefinition workflowDefinition = workflowDefinitionDao.queryByCode(code).orElse(null);
        if (Objects.isNull(workflowDefinition) || projectCode != workflowDefinition.getProjectCode()) {
            log.error(
                    "Switch workflow definition error because it does not exist, projectCode:{}, workflowDefinitionCode:{}.",
                    projectCode, code);
            throw new ServiceException(Status.SWITCH_WORKFLOW_DEFINITION_VERSION_NOT_EXIST_WORKFLOW_DEFINITION_ERROR,
                    code);
        }

        WorkflowDefinitionLog workflowDefinitionLog =
                workflowDefinitionLogMapper.queryByDefinitionCodeAndVersion(code, version);
        if (Objects.isNull(workflowDefinitionLog)) {
            log.error(
                    "Switch workflow definition error because version does not exist, projectCode:{}, workflowDefinitionCode:{}, version:{}.",
                    projectCode, code, version);
            throw new ServiceException(
                    Status.SWITCH_WORKFLOW_DEFINITION_VERSION_NOT_EXIST_WORKFLOW_DEFINITION_VERSION_ERROR,
                    workflowDefinition.getCode(), version);
        }
        int switchVersion = processService.switchVersion(workflowDefinition, workflowDefinitionLog);
        if (switchVersion <= 0) {
            log.error(
                    "Switch workflow definition version error, projectCode:{}, workflowDefinitionCode:{}, version:{}.",
                    projectCode, code, version);
            throw new ServiceException(Status.SWITCH_WORKFLOW_DEFINITION_VERSION_ERROR);
        }

        List<WorkflowTaskRelation> workflowTaskRelationList = workflowTaskRelationDao
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
                                         List<String> failedWorkflowList, boolean isCopy) {
        if (!failedWorkflowList.isEmpty()) {
            String failedWorkflow = String.join(",", failedWorkflowList);
            if (isCopy) {
                log.error(
                        "Copy workflow definition error, srcProjectCode:{}, targetProjectCode:{}, failedWorkflowList:{}.",
                        srcProjectCode, targetProjectCode, failedWorkflow);
                throw new ServiceException(Status.COPY_WORKFLOW_DEFINITION_ERROR, srcProjectCode, targetProjectCode,
                        failedWorkflow);
            }
            log.error(
                    "Move workflow definition error, srcProjectCode:{}, targetProjectCode:{}, failedWorkflowList:{}.",
                    srcProjectCode, targetProjectCode, failedWorkflow);
            throw new ServiceException(Status.MOVE_WORKFLOW_DEFINITION_ERROR, srcProjectCode, targetProjectCode,
                    failedWorkflow);
        }
        log.info("Batch {} workflow definition complete, srcProjectCode:{}, targetProjectCode:{}.",
                isCopy ? "copy" : "move", srcProjectCode, targetProjectCode);
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
        Project project = projectDao.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, VERSION_LIST);

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

        WorkflowDefinition workflowDefinition = workflowDefinitionDao.queryByCode(code).orElse(null);
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
                WorkflowExecutionStatus.NOT_TERMINAL_STATES);
        if (CollectionUtils.isNotEmpty(workflowInstances)) {
            throw new ServiceException(Status.DELETE_WORKFLOW_DEFINITION_EXECUTING_FAIL, workflowInstances.size());
        }

        int deleteLog = workflowDefinitionLogMapper.deleteByWorkflowDefinitionCodeAndVersion(code, version);
        int deleteRelationLog = workflowTaskRelationLogMapper.deleteByCode(code, version);
        if (deleteLog == 0 || deleteRelationLog == 0) {
            throw new ServiceException(Status.DELETE_WORKFLOW_DEFINE_BY_CODE_ERROR);
        }
        log.info("Delete version: {} of workflow: {}, projectCode: {}", version, code, projectCode);

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
    public WorkflowDefinitionVariablesDTO viewVariables(User loginUser, long projectCode, long code) {

        Project project = projectDao.queryByCode(projectCode);

        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, WORKFLOW_DEFINITION);

        WorkflowDefinition workflowDefinition = workflowDefinitionDao.queryByCode(code).orElse(null);

        if (Objects.isNull(workflowDefinition) || projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, projectCode:{}, workflowDefinitionCode:{}.", projectCode,
                    code);
            throw new ServiceException(WORKFLOW_DEFINITION_NOT_EXIST, code);
        }

        // global params
        List<Property> globalParams = workflowDefinition.getGlobalParamList();

        Map<String, Map<String, Object>> localUserDefParams = getLocalParams(workflowDefinition);

        return new WorkflowDefinitionVariablesDTO(globalParams, localUserDefParams);
    }

    /**
     * get local params
     */
    private Map<String, Map<String, Object>> getLocalParams(WorkflowDefinition workflowDefinition) {
        Map<String, Map<String, Object>> localUserDefParams = new HashMap<>();

        Set<Long> taskCodeSet = new TreeSet<>();

        workflowTaskRelationDao.queryByWorkflowDefinitionCode(workflowDefinition.getCode())
                .forEach(processTaskRelation -> {
                    if (processTaskRelation.getPreTaskCode() > 0) {
                        taskCodeSet.add(processTaskRelation.getPreTaskCode());
                    }
                    if (processTaskRelation.getPostTaskCode() > 0) {
                        taskCodeSet.add(processTaskRelation.getPostTaskCode());
                    }
                });

        taskDefinitionDao.queryByCodes(taskCodeSet)
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
                workflowTaskRelationDao.queryByWorkflowDefinitionCode(workflowDefinitionCode);
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
