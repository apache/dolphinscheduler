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

package org.apache.dolphinscheduler.server.master.utils;

import static org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters.DependentFailurePolicyEnum.DEPENDENT_FAILURE_WAITING;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependentRelation;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.DateInterval;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.DependentUtils;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * dependent item execute
 */
@Slf4j
public class DependentExecute {

    private final WorkflowInstanceDao workflowInstanceDao = SpringApplicationContext.getBean(WorkflowInstanceDao.class);

    private final TaskInstanceDao taskInstanceDao = SpringApplicationContext.getBean(TaskInstanceDao.class);

    /**
     * depend item list
     */
    private List<DependentItem> dependItemList;

    /**
     * dependent relation
     */
    private DependentRelation relation;

    private WorkflowInstance workflowInstance;

    private TaskInstance taskInstance;

    /**
     * depend result map
     */
    @Getter
    private Map<String, DependResult> dependResultMap = new HashMap<>();

    /**
     * process service
     */
    private final ProcessService processService = SpringApplicationContext.getBean(ProcessService.class);

    /**
     * task definition log dao
     */
    private final TaskDefinitionLogDao taskDefinitionLogDao =
            SpringApplicationContext.getBean(TaskDefinitionLogDao.class);

    /**
     * task definition dao
     */
    private final TaskDefinitionDao taskDefinitionDao = SpringApplicationContext.getBean(TaskDefinitionDao.class);

    @Getter
    private Map<String, Property> dependTaskVarPoolPropertyMap = new HashMap<>();

    @Getter
    private Map<String, Long> dependTaskVarPoolEndTimeMap = new HashMap<>();

    private Map<String, Property> dependItemVarPoolPropertyMap = new HashMap<>();

    private Map<String, Long> dependItemVarPoolEndTimeMap = new HashMap<>();

    /**
     * constructor
     *
     * @param itemList item list
     * @param relation relation
     */
    public DependentExecute(List<DependentItem> itemList, DependentRelation relation, WorkflowInstance workflowInstance,
                            TaskInstance taskInstance) {
        this.dependItemList = itemList;
        this.relation = relation;
        this.workflowInstance = workflowInstance;
        this.taskInstance = taskInstance;
    }

    /**
     * get dependent item for one dependent item
     *
     * @param dependentItem dependent item
     * @param currentTime   current time
     * @return DependResult
     */
    private DependResult getDependentResultForItem(DependentItem dependentItem, Date currentTime, int testFlag) {
        List<DateInterval> dateIntervals =
                DependentUtils.getDateIntervalList(currentTime, dependentItem.getDateValue());
        return calculateResultForTasks(dependentItem, dateIntervals, testFlag);
    }

    /**
     * calculate dependent result for one dependent item.
     *
     * @param dependentItem dependent item
     * @param dateIntervals date intervals
     * @return dateIntervals
     */
    private DependResult calculateResultForTasks(DependentItem dependentItem,
                                                 List<DateInterval> dateIntervals,
                                                 int testFlag) {

        DependResult result = DependResult.FAILED;
        for (DateInterval dateInterval : dateIntervals) {
            WorkflowInstance workflowInstance =
                    findLastWorkflowInterval(dependentItem.getDefinitionCode(), dependentItem.getDepTaskCode(),
                            dateInterval, testFlag);
            if (workflowInstance == null) {
                return DependResult.WAITING;
            }
            // need to check workflow for updates, so get all task and check the task state
            if (dependentItem.getDepTaskCode() == Constants.DEPENDENT_WORKFLOW_CODE) {
                result = dependResultByWorkflowInstance(workflowInstance);
            } else if (dependentItem.getDepTaskCode() == Constants.DEPENDENT_ALL_TASK_CODE) {
                result = dependResultByAllTaskOfWorkflowInstance(workflowInstance, testFlag);
            } else {
                result = dependResultBySingleTaskInstance(workflowInstance, dependentItem.getDepTaskCode(), testFlag);
            }
            if (result != DependResult.SUCCESS) {
                break;
            }
        }
        return result;
    }

    /**
     * depend type = depend_work_flow
     *
     * @return
     */
    private DependResult dependResultByWorkflowInstance(WorkflowInstance workflowInstance) {
        if (!workflowInstance.getState().isFinished()) {
            return DependResult.WAITING;
        }
        if (workflowInstance.getState().isSuccess()) {
            addItemVarPool(workflowInstance.getVarPool(), workflowInstance.getEndTime().getTime());
            return DependResult.SUCCESS;
        }
        log.warn(
                "The dependent workflow did not execute successfully, so return depend failed. workflowDefinitionCode: {}, workflowInstanceName: {}",
                workflowInstance.getWorkflowDefinitionCode(), workflowInstance.getName());
        return DependResult.FAILED;
    }

    /**
     * depend type = depend_all
     *
     * @return
     */
    private DependResult dependResultByAllTaskOfWorkflowInstance(WorkflowInstance workflowInstance, int testFlag) {
        if (!workflowInstance.getState().isFinished()) {
            log.info(
                    "Wait for the dependent workflow to complete, workflowDefinitionCode: {}, pworkflowInstanceId: {}.",
                    workflowInstance.getWorkflowDefinitionCode(), workflowInstance.getId());
            return DependResult.WAITING;
        }
        if (workflowInstance.getState().isSuccess()) {
            List<WorkflowTaskRelation> workflowTaskRelations =
                    processService.findRelationByCode(workflowInstance.getWorkflowDefinitionCode(),
                            workflowInstance.getWorkflowDefinitionVersion());
            List<TaskDefinitionLog> taskDefinitionLogs =
                    taskDefinitionLogDao.queryTaskDefineLogList(workflowTaskRelations);
            Map<Long, String> taskDefinitionCodeMap =
                    taskDefinitionLogs.stream().filter(taskDefinitionLog -> taskDefinitionLog.getFlag() == Flag.YES)
                            .collect(Collectors.toMap(TaskDefinitionLog::getCode, TaskDefinitionLog::getName));

            List<TaskInstance> taskInstanceList =
                    taskInstanceDao.queryLastTaskInstanceListIntervalInWorkflowInstance(workflowInstance.getId(),
                            taskDefinitionCodeMap.keySet(), testFlag);
            Map<Long, TaskExecutionStatus> taskExecutionStatusMap =
                    taskInstanceList.stream()
                            .filter(taskInstance -> taskInstance.getTaskExecuteType() != TaskExecuteType.STREAM)
                            .collect(Collectors.toMap(TaskInstance::getTaskCode, TaskInstance::getState));

            for (Long taskCode : taskDefinitionCodeMap.keySet()) {
                if (!taskExecutionStatusMap.containsKey(taskCode)) {
                    log.warn(
                            "The task of the workflow is not being executed, taskCode: {}, workflowInstanceId: {}, workflowInstanceName: {}.",
                            taskCode, workflowInstance.getWorkflowDefinitionCode(), workflowInstance.getName());
                    return DependResult.FAILED;
                } else {
                    if (!taskExecutionStatusMap.get(taskCode).isSuccess()) {
                        log.warn(
                                "The task of the workflow is not being executed successfully, taskCode: {}, workflowInstanceId: {}, workflowInstanceName: {}.",
                                taskCode, workflowInstance.getWorkflowDefinitionCode(), workflowInstance.getName());
                        return DependResult.FAILED;
                    }
                }
            }
            addItemVarPool(workflowInstance.getVarPool(), workflowInstance.getEndTime().getTime());
            return DependResult.SUCCESS;
        }
        return DependResult.FAILED;
    }

    /**
     * depend type = depend_task
     *
     * @param workflowInstance last workflow instance in the date interval
     * @param depTaskCode the dependent task code
     * @param testFlag test flag
     * @return depend result
     */
    private DependResult dependResultBySingleTaskInstance(WorkflowInstance workflowInstance, long depTaskCode,
                                                          int testFlag) {
        TaskInstance taskInstance =
                taskInstanceDao.queryLastTaskInstanceIntervalInWorkflowInstance(workflowInstance.getId(),
                        depTaskCode, testFlag);

        if (taskInstance == null) {
            TaskDefinition taskDefinition = taskDefinitionDao.queryByCode(depTaskCode);

            if (taskDefinition == null) {
                log.error("The dependent task definition can not be find, so return depend failed, taskCode: {}",
                        depTaskCode);
                return DependResult.FAILED;
            }

            if (taskDefinition.getFlag() == Flag.NO) {
                log.info(
                        "The dependent task is a forbidden task, so return depend success. Task code: {}, task name: {}",
                        taskDefinition.getCode(), taskDefinition.getName());
                return DependResult.SUCCESS;
            }

            if (!workflowInstance.getState().isFinished()) {
                log.info(
                        "Wait for the dependent workflow to complete, workflowDefinitionCode: {}, workflowInstanceId: {}.",
                        workflowInstance.getWorkflowDefinitionCode(), workflowInstance.getId());
                return DependResult.WAITING;
            }

            return DependResult.FAILED;
        } else {
            if (TaskExecuteType.STREAM == taskInstance.getTaskExecuteType()) {
                log.info(
                        "The dependent task is a streaming task, so return depend success. Task code: {}, task name: {}.",
                        taskInstance.getTaskCode(), taskInstance.getName());
                addItemVarPool(taskInstance.getVarPool(), taskInstance.getEndTime().getTime());
                return DependResult.SUCCESS;
            }
            return getDependResultOfTask(workflowInstance, taskInstance);
        }
    }

    /**
     * add varPool to dependItemVarPoolMap
     *
     * @param varPoolStr
     * @param endTime
     */
    private void addItemVarPool(String varPoolStr, Long endTime) {
        List<Property> varPool = new ArrayList<>(JSONUtils.toList(varPoolStr, Property.class));
        if (!varPool.isEmpty()) {
            Map<String, Property> varPoolPropertyMap = varPool.stream().filter(p -> p.getDirect().equals(Direct.OUT))
                    .collect(Collectors.toMap(Property::getProp, Function.identity()));
            Map<String, Long> varPoolEndTimeMap = varPool.stream().filter(p -> p.getDirect().equals(Direct.OUT))
                    .collect(Collectors.toMap(Property::getProp, d -> endTime));
            dependItemVarPoolPropertyMap.putAll(varPoolPropertyMap);
            dependItemVarPoolEndTimeMap.putAll(varPoolEndTimeMap);
        }
    }

    /**
     * find the last one workflow instance that :
     * 1. manual run and finish between the interval
     * 2. schedule run and schedule time between the interval
     *
     * @param definitionCode definition code
     * @param taskCode task code
     * @param dateInterval   date interval
     * @return workflowInstance
     */
    private WorkflowInstance findLastWorkflowInterval(Long definitionCode, Long taskCode, DateInterval dateInterval,
                                                      int testFlag) {

        WorkflowInstance lastSchedulerWorkflowInstance =
                workflowInstanceDao.queryLastSchedulerWorkflowInterval(definitionCode, taskCode, dateInterval,
                        testFlag);

        WorkflowInstance lastManualWorkflowInstance =
                workflowInstanceDao.queryLastManualWorkflowInterval(definitionCode, taskCode, dateInterval, testFlag);

        if (lastManualWorkflowInstance == null) {
            return lastSchedulerWorkflowInstance;
        }
        if (lastSchedulerWorkflowInstance == null) {
            return lastManualWorkflowInstance;
        }

        // In the time range, there are both manual and scheduled workflow instances, return the last workflow instance
        return lastManualWorkflowInstance.getId() > lastSchedulerWorkflowInstance.getId() ? lastManualWorkflowInstance
                : lastSchedulerWorkflowInstance;
    }

    /**
     * get dependent result by task/workflow instance
     *
     * @param workflowInstance workflow instance
     * @param taskInstance task instance
     * @return DependResult
     */
    private DependResult getDependResultOfTask(WorkflowInstance workflowInstance, TaskInstance taskInstance) {

        TaskExecutionStatus state = taskInstance.getState();
        if (!state.isFinished()) {
            return DependResult.WAITING;
        } else if (state.isSuccess()) {
            return DependResult.SUCCESS;
        } else {
            if (workflowInstance.getState().isRunning()
                    && taskInstance.getRetryTimes() < taskInstance.getMaxRetryTimes()) {
                log.info("taskDefinitionCode: {}, taskDefinitionName: {}, retryTimes: {}, maxRetryTimes: {}",
                        taskInstance.getTaskCode(), taskInstance.getName(), taskInstance.getRetryTimes(),
                        taskInstance.getMaxRetryTimes());
                return DependResult.WAITING;
            }
            log.warn(
                    "The dependent task were not executed successfully, so return depend failed. Task code: {}, task name: {}.",
                    taskInstance.getTaskCode(), taskInstance.getName());
            return DependResult.FAILED;
        }
    }

    /**
     * judge depend item finished
     *
     * @param currentTime current time
     * @return boolean
     */
    public boolean finish(Date currentTime, int testFlag, DependentParameters.DependentFailurePolicyEnum failurePolicy,
                          Integer failureWaitingTime) {
        DependResult modelDependResult = getModelDependResult(currentTime, testFlag);
        if (modelDependResult == DependResult.WAITING) {
            return false;
        } else if (modelDependResult == DependResult.FAILED && DEPENDENT_FAILURE_WAITING == failurePolicy
                && failureWaitingTime != null) {
            return Duration.between(currentTime.toInstant(), Instant.now())
                    .compareTo(Duration.ofMinutes(failureWaitingTime)) > 0;
        }
        return true;
    }

    /**
     * get model depend result
     *
     * @param currentTime current time
     * @return DependResult
     */
    public DependResult getModelDependResult(Date currentTime, int testFlag) {

        List<DependResult> dependResultList = new ArrayList<>();

        for (DependentItem dependentItem : dependItemList) {
            if (isSelfDependent(dependentItem) && isFirstWorkflowInstance(dependentItem)) {
                // if self-dependent, default success at first time
                dependResultMap.put(dependentItem.getKey(), DependResult.SUCCESS);
                dependResultList.add(DependResult.SUCCESS);
                log.info(
                        "This dependent item is self-dependent and run at first time, default success, workflowDefinitionCode:{}, depTaskCode:{}",
                        dependentItem.getDefinitionCode(), dependentItem.getDepTaskCode());
                continue;
            }
            DependResult dependResult = getDependResultForItem(dependentItem, currentTime, testFlag);
            if (dependResult != DependResult.WAITING) {
                dependResultMap.put(dependentItem.getKey(), dependResult);
                if (dependentItem.getParameterPassing() && !dependItemVarPoolPropertyMap.isEmpty()) {
                    DependentUtils.addTaskVarPool(dependItemVarPoolPropertyMap, dependItemVarPoolEndTimeMap,
                            dependTaskVarPoolPropertyMap, dependTaskVarPoolEndTimeMap);
                }
            }
            dependItemVarPoolPropertyMap.clear();
            dependItemVarPoolEndTimeMap.clear();
            dependResultList.add(dependResult);
        }
        return DependentUtils.getDependResultForRelation(this.relation, dependResultList);
    }

    /**
     * get dependent item result
     *
     * @param item        item
     * @param currentTime current time
     * @return DependResult
     */
    private DependResult getDependResultForItem(DependentItem item, Date currentTime, int testFlag) {
        String key = item.getKey();
        if (dependResultMap.containsKey(key)) {
            return dependResultMap.get(key);
        }
        return getDependentResultForItem(item, currentTime, testFlag);
    }

    /**
     * check for self-dependent
     * @param dependentItem
     * @return
     */
    public boolean isSelfDependent(DependentItem dependentItem) {
        if (workflowInstance.getWorkflowDefinitionCode().equals(dependentItem.getDefinitionCode())) {
            if (dependentItem.getDepTaskCode() == Constants.DEPENDENT_ALL_TASK_CODE) {
                return true;
            }
            if (dependentItem.getDepTaskCode() == taskInstance.getTaskCode()) {
                return true;
            }
        }
        return false;
    }

    /**
     * check for first-running
     * query the first workflowInstance by scheduleTime(or startTime if scheduleTime is null)
     */
    public boolean isFirstWorkflowInstance(DependentItem dependentItem) {
        WorkflowInstance firstWorkflowInstance =
                workflowInstanceDao.queryFirstScheduleWorkflowInstance(dependentItem.getDefinitionCode());
        if (firstWorkflowInstance == null) {
            firstWorkflowInstance =
                    workflowInstanceDao.queryFirstStartWorkflowInstance(dependentItem.getDefinitionCode());
            if (firstWorkflowInstance == null) {
                log.warn("First workflow instance is null, workflowDefinitionCode: {}",
                        dependentItem.getDefinitionCode());
                return false;
            }
        }
        return Objects.equals(firstWorkflowInstance.getId(), workflowInstance.getId());
    }
}
