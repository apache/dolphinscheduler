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

package org.apache.dolphinscheduler.server.master.engine.executor.plugin.dependent;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ContextType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DependentResultTaskInstanceContext;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstanceContext;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceContextDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentTaskModel;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.DependentUtils;
import org.apache.dolphinscheduler.server.master.utils.DependentExecute;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Lists;

@Slf4j
public class DependentTaskTracker {

    private final TaskExecutionContext taskExecutionContext;
    private final DependentParameters dependentParameters;
    private final ProjectDao projectDao;
    private final WorkflowDefinitionDao workflowDefinitionDao;
    private final TaskDefinitionDao taskDefinitionDao;
    private final TaskInstanceDao taskInstanceDao;
    private final TaskInstanceContextDao taskInstanceContextDao;

    private final WorkflowInstance workflowInstance;
    private final Date dependentDate;
    private final List<DependentExecute> dependentTaskList;
    private final Map<String, DependResult> dependResultMap;
    private final Map<String, Property> dependVarPoolPropertyMap;

    private Map<Long, WorkflowDefinition> processDefinitionMap;
    private Map<Long, TaskDefinition> taskDefinitionMap;
    private Map<Long, Project> projectCodeMap;
    private TaskInstanceContext taskInstanceContext;

    public DependentTaskTracker(TaskExecutionContext taskExecutionContext,
                                DependentParameters dependentParameters,
                                ProjectDao projectDao,
                                WorkflowDefinitionDao workflowDefinitionDao,
                                TaskDefinitionDao taskDefinitionDao,
                                TaskInstanceDao taskInstanceDao,
                                WorkflowInstanceDao workflowInstanceDao,
                                TaskInstanceContextDao taskInstanceContextDao) {
        this.taskExecutionContext = taskExecutionContext;
        this.dependentParameters = dependentParameters;
        this.projectDao = projectDao;
        this.workflowDefinitionDao = workflowDefinitionDao;
        this.taskDefinitionDao = taskDefinitionDao;
        this.taskInstanceDao = taskInstanceDao;
        this.taskInstanceContextDao = taskInstanceContextDao;
        this.workflowInstance =
                workflowInstanceDao.queryById(taskExecutionContext.getWorkflowInstanceId());
        this.dependentDate = calculateDependentDate();
        this.dependentTaskList = initializeDependentTaskList();
        this.dependResultMap = new HashMap<>();
        this.dependVarPoolPropertyMap = new HashMap<>();
        this.taskInstanceContext = new TaskInstanceContext();
        initTaskDependentResult();
    }

    public @NonNull TaskExecutionStatus getDependentTaskStatus() {
        if (isAllDependentTaskFinished()) {
            log.info("All dependent task finished, will calculate the dependent result");
            DependResult dependResult = calculateDependResult();
            log.info("The final Dependent result is: {}", dependResult);
            if (dependResult == DependResult.SUCCESS) {
                dependentParameters.setVarPool(JSONUtils.toJsonString(dependVarPoolPropertyMap.values()));
                log.info("Set dependentParameters varPool: {}", dependentParameters.getVarPool());
                return TaskExecutionStatus.SUCCESS;
            } else {
                return TaskExecutionStatus.FAILURE;
            }
        }
        return TaskExecutionStatus.RUNNING_EXECUTION;
    }

    private Date calculateDependentDate() {
        if (workflowInstance.getScheduleTime() != null) {
            return workflowInstance.getScheduleTime();
        } else {
            return new Date();
        }
    }

    private List<DependentExecute> initializeDependentTaskList() {
        log.info("Begin to initialize dependent task list");
        List<DependentTaskModel> dependTaskList = dependentParameters.getDependence().getDependTaskList();

        final Set<Long> projectCodes = new HashSet<>();
        final Set<Long> processDefinitionCodes = new HashSet<>();
        final Set<Long> taskDefinitionCodes = new HashSet<>();
        for (DependentTaskModel taskModel : dependTaskList) {
            for (DependentItem dependentItem : taskModel.getDependItemList()) {
                projectCodes.add(dependentItem.getProjectCode());
                processDefinitionCodes.add(dependentItem.getDefinitionCode());
                taskDefinitionCodes.add(dependentItem.getDepTaskCode());
            }
        }

        projectCodeMap = projectDao.queryByCodes(new ArrayList<>(projectCodes)).stream()
                .collect(Collectors.toMap(Project::getCode, Function.identity()));
        processDefinitionMap =
                workflowDefinitionDao.queryByCodes(processDefinitionCodes).stream()
                        .collect(Collectors.toMap(WorkflowDefinition::getCode, Function.identity()));
        taskDefinitionMap = taskDefinitionDao.queryByCodes(taskDefinitionCodes).stream()
                .collect(Collectors.toMap(TaskDefinition::getCode, Function.identity()));
        final TaskInstance taskInstance =
                taskInstanceDao.queryById(taskExecutionContext.getTaskInstanceId());
        List<DependentExecute> dependentExecutes = dependTaskList
                .stream()
                .map(dependentTaskModel -> {
                    for (DependentItem dependentItem : dependentTaskModel.getDependItemList()) {
                        Project project = projectCodeMap.get(dependentItem.getProjectCode());
                        if (project == null) {
                            log.error("The dependent task's project is not exist, dependentItem: {}", dependentItem);
                            throw new RuntimeException(
                                    "The dependent task's project is not exist, dependentItem: " + dependentItem);
                        }
                        WorkflowDefinition workflowDefinition =
                                processDefinitionMap.get(dependentItem.getDefinitionCode());
                        if (workflowDefinition == null) {
                            log.error("The dependent task's workflow is not exist, dependentItem: {}", dependentItem);
                            throw new RuntimeException(
                                    "The dependent task's workflow is not exist, dependentItem: " + dependentItem);
                        }
                        if (dependentItem.getDepTaskCode() == Constants.DEPENDENT_ALL_TASK_CODE) {
                            log.info(
                                    "Add dependent all task, ProjectName: {}, WorkflowName: {}, WorkflowCode: {}, DependentCycle: {}, DependentCycleDate: {}, DependentRelation: {}",
                                    project.getName(), workflowDefinition.getName(), workflowDefinition.getCode(),
                                    dependentItem.getCycle(), dependentItem.getDateValue(),
                                    dependentTaskModel.getRelation());
                        } else if (dependentItem.getDepTaskCode() == Constants.DEPENDENT_WORKFLOW_CODE) {
                            log.info(
                                    "Add dependent workflow task, ProjectName: {}, WorkflowName: {}, WorkflowCode: {}, DependentCycle: {}, DependentCycleDate: {}, DependentRelation: {}",
                                    project.getName(), workflowDefinition.getName(), workflowDefinition.getCode(),
                                    dependentItem.getCycle(), dependentItem.getDateValue(),
                                    dependentTaskModel.getRelation());
                        } else {
                            TaskDefinition taskDefinition = taskDefinitionMap.get(dependentItem.getDepTaskCode());
                            if (taskDefinition == null) {
                                log.error("The dependent task's taskDefinition is not exist, dependentItem: {}",
                                        dependentItem);
                                throw new RuntimeException(
                                        "The dependent task's taskDefinition is not exist, dependentItem: "
                                                + dependentItem);
                            }
                            log.info(
                                    "Add dependent task, ProjectName: {}, WorkflowName: {}, WorkflowCode: {}, TaskName: {}, DependentCycle: {}, DependentCycleDate: {}, DependentRelation: {}",
                                    project.getName(), workflowDefinition.getName(), workflowDefinition.getCode(),
                                    taskDefinition.getName(), dependentItem.getCycle(), dependentItem.getDateValue(),
                                    dependentTaskModel.getRelation());
                        }
                    }
                    return new DependentExecute(dependentTaskModel.getDependItemList(),
                            dependentTaskModel.getRelation(), workflowInstance, taskInstance);
                }).collect(Collectors.toList());
        log.info("Initialized dependent task list successfully");
        return dependentExecutes;
    }

    private void initTaskDependentResult() {
        taskInstanceContextDao.deleteByTaskInstanceIdAndContextType(taskExecutionContext.getTaskInstanceId(),
                ContextType.DEPENDENT_RESULT_CONTEXT);
        taskInstanceContext.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        taskInstanceContext.setContextType(ContextType.DEPENDENT_RESULT_CONTEXT);
        taskInstanceContext.setCreateTime(new Date());
        taskInstanceContext.setUpdateTime(new Date());
    }

    private DependResult calculateDependResult() {
        List<DependResult> dependResultList = new ArrayList<>();
        Map<String, Long> dependVarPoolEndTimeMap = new HashMap<>();
        for (DependentExecute dependentExecute : dependentTaskList) {
            DependResult dependResult =
                    dependentExecute.getModelDependResult(dependentDate, workflowInstance.getTestFlag());
            if (dependResult == DependResult.SUCCESS) {
                Map<String, Property> varPoolPropertyMap = dependentExecute.getDependTaskVarPoolPropertyMap();
                Map<String, Long> varPoolEndTimeMap = dependentExecute.getDependTaskVarPoolEndTimeMap();
                DependentUtils.addTaskVarPool(varPoolPropertyMap, varPoolEndTimeMap, dependVarPoolPropertyMap,
                        dependVarPoolEndTimeMap);
            }
            dependResultList.add(dependResult);
        }
        return DependentUtils.getDependResultForRelation(dependentParameters.getDependence().getRelation(),
                dependResultList);
    }

    private boolean isAllDependentTaskFinished() {
        boolean isAllDependentTaskFinished = true;
        for (DependentExecute dependentExecute : dependentTaskList) {
            if (!dependentExecute.finish(dependentDate, workflowInstance.getTestFlag(),
                    dependentParameters.getDependence().getFailurePolicy(),
                    dependentParameters.getDependence().getFailureWaitingTime())) {
                isAllDependentTaskFinished = false;
            }
            dependentExecute.getDependResultMap().forEach((dependentKey, dependResult) -> {
                if (!dependResultMap.containsKey(dependentKey)) {
                    dependResultMap.put(dependentKey, dependResult);
                    DependentItem dependentItem = new DependentItem().fromKey(dependentKey);
                    WorkflowDefinition workflowDefinition = processDefinitionMap.get(dependentItem.getDefinitionCode());
                    Project project = projectCodeMap.get(workflowDefinition.getProjectCode());
                    DependentResultTaskInstanceContext dependentResultTaskInstanceContext =
                            new DependentResultTaskInstanceContext();
                    dependentResultTaskInstanceContext.setProjectCode(project.getCode());
                    dependentResultTaskInstanceContext.setWorkflowDefinitionCode(workflowDefinition.getCode());
                    dependentResultTaskInstanceContext.setDependentResult(dependResult);
                    dependentResultTaskInstanceContext.setContextType(ContextType.DEPENDENT_RESULT_CONTEXT);
                    if (dependentItem.getDepTaskCode() == Constants.DEPENDENT_ALL_TASK_CODE) {
                        dependentResultTaskInstanceContext.setTaskDefinitionCode(Constants.DEPENDENT_ALL_TASK_CODE);
                        dependentResultTaskInstanceContext.setDateCycle(dependentItem.getDateValue());
                        taskInstanceContext
                                .setTaskInstanceContext(Lists.newArrayList(dependentResultTaskInstanceContext));
                        taskInstanceContextDao.upsertTaskInstanceContext(taskInstanceContext);
                        log.info(
                                "Dependent type all task check finished, DependentResult: {}, DependentDate: {}, ProjectName: {}, WorkflowName: {}, WorkflowCode: {}, DependentCycle: {}, DependentCycleDate: {}",
                                dependResult, dependentDate, project.getName(), workflowDefinition.getName(),
                                workflowDefinition.getCode(), dependentItem.getCycle(), dependentItem.getDateValue());
                    } else if (dependentItem.getDepTaskCode() == Constants.DEPENDENT_WORKFLOW_CODE) {
                        dependentResultTaskInstanceContext.setTaskDefinitionCode(Constants.DEPENDENT_WORKFLOW_CODE);
                        dependentResultTaskInstanceContext.setDateCycle(dependentItem.getDateValue());
                        taskInstanceContext
                                .setTaskInstanceContext(Lists.newArrayList(dependentResultTaskInstanceContext));
                        taskInstanceContextDao.upsertTaskInstanceContext(taskInstanceContext);
                        log.info(
                                "Dependent type workflow task check finished, DependentResult: {}, DependentDate: {}, ProjectName: {}, WorkflowName: {}, WorkflowCode: {}, DependentCycle: {}, DependentCycleDate: {}",
                                dependResult, dependentDate, project.getName(), workflowDefinition.getName(),
                                workflowDefinition.getCode(), dependentItem.getCycle(), dependentItem.getDateValue());
                    } else {
                        TaskDefinition taskDefinition = taskDefinitionMap.get(dependentItem.getDepTaskCode());
                        dependentResultTaskInstanceContext.setTaskDefinitionCode(taskDefinition.getCode());
                        dependentResultTaskInstanceContext.setDateCycle(dependentItem.getDateValue());
                        taskInstanceContext
                                .setTaskInstanceContext(Lists.newArrayList(dependentResultTaskInstanceContext));
                        taskInstanceContextDao.upsertTaskInstanceContext(taskInstanceContext);
                        log.info(
                                "Dependent type task check finished, DependentResult: {}, DependentDate: {}, ProjectName: {}, WorkflowName: {}, WorkflowCode: {}, TaskName: {}, TaskCode: {}, DependentCycle: {}, DependentCycleDate: {}",
                                dependResult, dependentDate, project.getName(), workflowDefinition.getName(),
                                workflowDefinition.getCode(), taskDefinition.getName(), taskDefinition.getCode(),
                                dependentItem.getCycle(), dependentItem.getDateValue());
                    }
                }
            });
        }
        return isAllDependentTaskFinished;
    }

}
