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

import static org.apache.dolphinscheduler.common.constants.Constants.DEPENDENT_SPLIT;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
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

@Slf4j
public class DependentTaskTracker {

    private final TaskExecutionContext taskExecutionContext;
    private final DependentParameters dependentParameters;
    private final ProjectDao projectDao;
    private final WorkflowDefinitionDao workflowDefinitionDao;
    private final TaskDefinitionDao taskDefinitionDao;
    private final TaskInstanceDao taskInstanceDao;

    private final WorkflowInstance workflowInstance;
    private final Date dependentDate;
    private final List<DependentExecute> dependentTaskList;
    private final Map<String, DependResult> dependResultMap;
    private final Map<String, Property> dependVarPoolPropertyMap;

    public DependentTaskTracker(TaskExecutionContext taskExecutionContext,
                                DependentParameters dependentParameters,
                                ProjectDao projectDao,
                                WorkflowDefinitionDao workflowDefinitionDao,
                                TaskDefinitionDao taskDefinitionDao,
                                TaskInstanceDao taskInstanceDao,
                                WorkflowInstanceDao workflowInstanceDao) {
        this.taskExecutionContext = taskExecutionContext;
        this.dependentParameters = dependentParameters;
        this.projectDao = projectDao;
        this.workflowDefinitionDao = workflowDefinitionDao;
        this.taskDefinitionDao = taskDefinitionDao;
        this.taskInstanceDao = taskInstanceDao;
        this.workflowInstance =
                workflowInstanceDao.queryById(taskExecutionContext.getWorkflowInstanceId());
        this.dependentDate = calculateDependentDate();
        this.dependentTaskList = initializeDependentTaskList();
        log.info("Initialized dependent task list successfully");
        this.dependResultMap = new HashMap<>();
        this.dependVarPoolPropertyMap = new HashMap<>();
    }

    public @NonNull TaskExecutionStatus getDependentTaskStatus() {
        if (isAllDependentTaskFinished()) {
            log.info("All dependent task finished, will calculate the dependent result");
            DependResult dependResult = calculateDependResult();
            log.info("The Dependent result is: {}", dependResult);
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

        final Map<Long, Project> projectCodeMap = projectDao.queryByCodes(new ArrayList<>(projectCodes)).stream()
                .collect(Collectors.toMap(Project::getCode, Function.identity()));
        final Map<Long, WorkflowDefinition> processDefinitionMap =
                workflowDefinitionDao.queryByCodes(processDefinitionCodes).stream()
                        .collect(Collectors.toMap(WorkflowDefinition::getCode, Function.identity()));
        final Map<Long, TaskDefinition> taskDefinitionMap = taskDefinitionDao.queryByCodes(taskDefinitionCodes).stream()
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
                            log.info("Add dependent task:");
                            log.info("DependentRelation: {}", dependentTaskModel.getRelation());
                            log.info("ProjectName: {}", project.getName());
                            log.info("WorkflowName: {}", workflowDefinition.getName());
                            log.info("TaskName: {}", "ALL");
                            log.info("DependentKey: {}", dependentItem.getKey());
                        } else if (dependentItem.getDepTaskCode() == Constants.DEPENDENT_WORKFLOW_CODE) {
                            log.info("Add dependent task:");
                            log.info("DependentRelation: {}", dependentTaskModel.getRelation());
                            log.info("ProjectName: {}", project.getName());
                            log.info("WorkflowName: {}", workflowDefinition.getName());
                            log.info("DependentKey: {}", dependentItem.getKey());
                        } else {
                            TaskDefinition taskDefinition = taskDefinitionMap.get(dependentItem.getDepTaskCode());
                            if (taskDefinition == null) {
                                log.error("The dependent task's taskDefinition is not exist, dependentItem: {}",
                                        dependentItem);
                                throw new RuntimeException(
                                        "The dependent task's taskDefinition is not exist, dependentItem: "
                                                + dependentItem);
                            }
                            log.info("Add dependent task:");
                            log.info("DependentRelation: {}", dependentTaskModel.getRelation());
                            log.info("ProjectName: {}", project.getName());
                            log.info("WorkflowName: {}", workflowDefinition.getName());
                            log.info("TaskName: {}", taskDefinition.getName());
                            log.info("DependentKey: {}", dependentItem.getKey());
                        }
                    }
                    return new DependentExecute(dependentTaskModel.getDependItemList(),
                            dependentTaskModel.getRelation(), workflowInstance, taskInstance);
                }).collect(Collectors.toList());
        log.info("Initialized dependent task list");
        return dependentExecutes;
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
                    // The log is applied in: api-server obtains the result of the item dependent in the dependent task
                    // node.{@link ProcessInstanceServiceImpl#parseLogForDependentResult}
                    log.info("Dependent item check finished, {} dependentKey: {}, result: {}, dependentDate: {}",
                            DEPENDENT_SPLIT,
                            dependentKey,
                            dependResult, dependentDate);
                }
            });
        }
        return isAllDependentTaskFinished;
    }

}
