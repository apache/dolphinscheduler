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

package org.apache.dolphinscheduler.server.master.runner.task.dependent;

import static org.apache.dolphinscheduler.common.constants.Constants.DEPENDENT_SPLIT;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.ProcessDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentTaskModel;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.DependentUtils;
import org.apache.dolphinscheduler.server.master.runner.execute.AsyncTaskExecuteFunction;
import org.apache.dolphinscheduler.server.master.utils.DependentExecute;

import java.time.Duration;
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
public class DependentAsyncTaskExecuteFunction implements AsyncTaskExecuteFunction {

    private static final Duration DEFAULT_STATE_CHECK_INTERVAL = Duration.ofSeconds(10);

    private final TaskExecutionContext taskExecutionContext;
    private final DependentParameters dependentParameters;
    private final ProjectDao projectDao;
    private final ProcessDefinitionDao processDefinitionDao;
    private final TaskDefinitionDao taskDefinitionDao;
    private final TaskInstanceDao taskInstanceDao;

    private final ProcessInstance processInstance;
    private final Date dependentDate;
    private final List<DependentExecute> dependentTaskList;
    private final Map<String, DependResult> dependResultMap;

    public DependentAsyncTaskExecuteFunction(TaskExecutionContext taskExecutionContext,
                                             DependentParameters dependentParameters,
                                             ProjectDao projectDao,
                                             ProcessDefinitionDao processDefinitionDao,
                                             TaskDefinitionDao taskDefinitionDao,
                                             TaskInstanceDao taskInstanceDao,
                                             ProcessInstanceDao processInstanceDao) {
        this.taskExecutionContext = taskExecutionContext;
        this.dependentParameters = dependentParameters;
        this.projectDao = projectDao;
        this.processDefinitionDao = processDefinitionDao;
        this.taskDefinitionDao = taskDefinitionDao;
        this.taskInstanceDao = taskInstanceDao;
        this.processInstance =
                processInstanceDao.queryById(taskExecutionContext.getProcessInstanceId());
        this.dependentDate = calculateDependentDate();
        this.dependentTaskList = initializeDependentTaskList();
        log.info("Initialized dependent task list successfully");
        this.dependResultMap = new HashMap<>();
    }

    @Override
    public @NonNull AsyncTaskExecutionStatus getAsyncTaskExecutionStatus() {
        if (isAllDependentTaskFinished()) {
            log.info("All dependent task finished, will calculate the dependent result");
            DependResult dependResult = calculateDependResult();
            log.info("The Dependent result is: {}", dependResult);
            return dependResult == DependResult.SUCCESS ? AsyncTaskExecutionStatus.SUCCESS
                    : AsyncTaskExecutionStatus.FAILED;
        }
        return AsyncTaskExecutionStatus.RUNNING;
    }

    private Date calculateDependentDate() {
        if (processInstance.getScheduleTime() != null) {
            return processInstance.getScheduleTime();
        } else {
            return new Date();
        }
    }

    private List<DependentExecute> initializeDependentTaskList() {
        log.info("Begin to initialize dependent task list");
        final Set<Long> projectCodes = new HashSet<>();
        final Set<Long> processDefinitionCodes = new HashSet<>();
        final Set<Long> taskDefinitionCodes = new HashSet<>();
        for (DependentTaskModel taskModel : dependentParameters.getDependTaskList()) {
            for (DependentItem dependentItem : taskModel.getDependItemList()) {
                projectCodes.add(dependentItem.getProjectCode());
                processDefinitionCodes.add(dependentItem.getDefinitionCode());
                taskDefinitionCodes.add(dependentItem.getDepTaskCode());
            }
        }

        final Map<Long, Project> projectCodeMap = projectDao.queryByCodes(new ArrayList<>(projectCodes)).stream()
                .collect(Collectors.toMap(Project::getCode, Function.identity()));
        final Map<Long, ProcessDefinition> processDefinitionMap =
                processDefinitionDao.queryByCodes(processDefinitionCodes).stream()
                        .collect(Collectors.toMap(ProcessDefinition::getCode, Function.identity()));
        final Map<Long, TaskDefinition> taskDefinitionMap = taskDefinitionDao.queryByCodes(taskDefinitionCodes).stream()
                .collect(Collectors.toMap(TaskDefinition::getCode, Function.identity()));
        final TaskInstance taskInstance =
                taskInstanceDao.queryById(taskExecutionContext.getTaskInstanceId());
        List<DependentExecute> dependentExecutes = dependentParameters.getDependTaskList()
                .stream()
                .map(dependentTaskModel -> {
                    for (DependentItem dependentItem : dependentTaskModel.getDependItemList()) {
                        Project project = projectCodeMap.get(dependentItem.getProjectCode());
                        if (project == null) {
                            log.error("The dependent task's project is not exist, dependentItem: {}", dependentItem);
                            throw new RuntimeException(
                                    "The dependent task's project is not exist, dependentItem: " + dependentItem);
                        }
                        ProcessDefinition processDefinition =
                                processDefinitionMap.get(dependentItem.getDefinitionCode());
                        if (processDefinition == null) {
                            log.error("The dependent task's workflow is not exist, dependentItem: {}", dependentItem);
                            throw new RuntimeException(
                                    "The dependent task's workflow is not exist, dependentItem: " + dependentItem);
                        }
                        if (dependentItem.getDepTaskCode() == Constants.DEPENDENT_ALL_TASK_CODE) {
                            log.info("Add dependent task:");
                            log.info("DependentRelation: {}", dependentTaskModel.getRelation());
                            log.info("ProjectName: {}", project.getName());
                            log.info("WorkflowName: {}", processDefinition.getName());
                            log.info("TaskName: {}", "ALL");
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
                            log.info("WorkflowName: {}", processDefinition.getName());
                            log.info("TaskName: {}", taskDefinition.getName());
                            log.info("DependentKey: {}", dependentItem.getKey());
                        }
                    }
                    return new DependentExecute(dependentTaskModel.getDependItemList(),
                            dependentTaskModel.getRelation(), processInstance, taskInstance);
                }).collect(Collectors.toList());
        log.info("Initialized dependent task list");
        return dependentExecutes;
    }

    private DependResult calculateDependResult() {
        List<DependResult> dependResultList = new ArrayList<>();
        for (DependentExecute dependentExecute : dependentTaskList) {
            DependResult dependResult =
                    dependentExecute.getModelDependResult(dependentDate, processInstance.getTestFlag());
            dependResultList.add(dependResult);
        }
        return DependentUtils.getDependResultForRelation(this.dependentParameters.getRelation(),
                dependResultList);
    }

    private boolean isAllDependentTaskFinished() {
        boolean isAllDependentTaskFinished = true;
        for (DependentExecute dependentExecute : dependentTaskList) {
            if (!dependentExecute.finish(dependentDate, processInstance.getTestFlag(),
                    dependentParameters.getFailurePolicy(), dependentParameters.getFailureWaitingTime())) {
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

    @Override
    public @NonNull Duration getAsyncTaskStateCheckInterval() {
        return dependentParameters.getCheckInterval() == null ? DEFAULT_STATE_CHECK_INTERVAL
                : Duration.ofSeconds(dependentParameters.getCheckInterval());
    }
}
