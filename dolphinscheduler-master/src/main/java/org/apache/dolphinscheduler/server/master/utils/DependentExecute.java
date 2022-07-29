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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependentRelation;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.DateInterval;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.utils.DependentUtils;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * dependent item execute
 */
public class DependentExecute {
    /**
     * process service
     */
    private final ProcessService processService = SpringApplicationContext.getBean(ProcessService.class);

    /**
     * depend item list
     */
    private List<DependentItem> dependItemList;

    /**
     * dependent relation
     */
    private DependentRelation relation;

    /**
     * depend result
     */
    private DependResult modelDependResult = DependResult.WAITING;

    /**
     * depend result map
     */
    private Map<String, DependResult> dependResultMap = new HashMap<>();

    /**
     * logger
     */
    protected final Logger logger = LoggerFactory.getLogger(String.format(TaskConstants.TASK_LOG_LOGGER_NAME_FORMAT, getClass()));

    /**
     * constructor
     *
     * @param itemList item list
     * @param relation relation
     */
    public DependentExecute(List<DependentItem> itemList, DependentRelation relation) {
        this.dependItemList = itemList;
        this.relation = relation;
    }

    /**
     * get dependent item for one dependent item
     *
     * @param dependentItem dependent item
     * @param currentTime   current time
     * @return DependResult
     */
    private DependResult getDependentResultForItem(DependentItem dependentItem, Date currentTime) {
        List<DateInterval> dateIntervals = DependentUtils.getDateIntervalList(currentTime, dependentItem.getDateValue());
        return calculateResultForTasks(dependentItem, dateIntervals);
    }

    /**
     * calculate dependent result for one dependent item.
     *
     * @param dependentItem dependent item
     * @param dateIntervals date intervals
     * @return dateIntervals
     */
    private DependResult calculateResultForTasks(DependentItem dependentItem, List<DateInterval> dateIntervals) {
        DependResult result = DependResult.FAILED;
        for (DateInterval dateInterval : dateIntervals) {
            ProcessInstance processInstance = findLastProcessInterval(dependentItem.getDefinitionCode(), dateInterval);
            if (processInstance == null) {
                logger.info("Cannot find dependent processInstance, waiting for workflow to run, processDefiniteCode:{}, taskCode:{}",
                        dependentItem.getDefinitionCode(), dependentItem.getDepTaskCode());
                return DependResult.WAITING;
            }
            // need to check workflow for updates, so get all task and check the task state
            if (dependentItem.getDepTaskCode() == Constants.DEPENDENT_ALL_TASK_CODE) {
                if (!processInstance.getState().typeIsFinished()) {
                    logger.info("Wait for the dependent workflow to complete, processDefiniteCode:{}, taskCode:{}, processInstanceId:{}, processInstance state:{}",
                            dependentItem.getDefinitionCode(), dependentItem.getDepTaskCode(), processInstance.getId(), processInstance.getState());
                    return DependResult.WAITING;
                }
                result = dependResultByProcessInstance(processInstance, dateInterval);
            } else {
                result = getDependTaskResult(processInstance, dependentItem.getDepTaskCode(), dateInterval);
            }
            if (result != DependResult.SUCCESS) {
                break;
            }
        }
        return result;
    }

    /**
     * depend type = depend_all
     */
    private DependResult dependResultByProcessInstance(ProcessInstance processInstance, DateInterval dateInterval) {
        if (processInstance.getState().typeIsSuccess()) {
            List<ProcessTaskRelation> taskRelations = processService.findRelationByCode(processInstance.getProcessDefinitionCode(),
                    processInstance.getProcessDefinitionVersion());
            if (!taskRelations.isEmpty()) {
                List<TaskDefinitionLog> taskDefinitionLogs = processService.genTaskDefineList(taskRelations);
                Map<Long, String> definiteTask = taskDefinitionLogs.stream().filter(log -> !log.getTaskType().equals(TaskConstants.TASK_TYPE_SUB_PROCESS)
                        || !log.getTaskType().equals(TaskConstants.TASK_TYPE_DEPENDENT)
                        || !log.getTaskType().equals(TaskConstants.TASK_TYPE_CONDITIONS))
                        .collect(Collectors.toMap(TaskDefinition::getCode, TaskDefinitionLog::getName));
                if (!definiteTask.isEmpty()) {
                    List<TaskInstance> taskInstanceList = processService.findLastTaskInstanceListInterval(definiteTask.keySet(), dateInterval);
                    if (taskInstanceList.isEmpty()) {
                        logger.warn("Cannot find the task instance: {}", JSONUtils.toJsonString(definiteTask));
                        return DependResult.FAILED;
                    }
                    Map<Long, TaskInstance> taskInstanceMap = new HashMap<>();
                    for (TaskInstance instance : taskInstanceList) {
                        taskInstanceMap.compute(instance.getTaskCode(), (k, v) -> {
                           if (v == null) {
                               v = instance;
                           } else {
                               if (v.getId() < instance.getId()) {
                                   v = instance;
                               }
                           }
                           return v;
                        });
                        definiteTask.remove(instance.getTaskCode());
                    }
                    List<TaskInstance> instanceFail = taskInstanceMap.values().stream().filter(instance -> instance.getState().typeIsFailure()).collect(Collectors.toList());
                    if (!instanceFail.isEmpty()) {
                        List<String> log = instanceFail.stream().map(instance -> instance.getId() + "|" + instance.getTaskCode() + "|" + instance.getName()).collect(Collectors.toList());
                        logger.warn("The fail task: {}", StringUtils.join(log, Constants.COMMA));
                        return DependResult.FAILED;
                    }
                    List<TaskInstance> instanceRunning = taskInstanceMap.values().stream().filter(instance -> instance.getState().typeIsRunning()).collect(Collectors.toList());
                    if (!instanceRunning.isEmpty()) {
                        List<String> log = instanceRunning.stream().map(instance -> instance.getId() + "|" + instance.getTaskCode() + "|" + instance.getName()).collect(Collectors.toList());
                        logger.info("The running task: {}", StringUtils.join(log, Constants.COMMA));
                        return DependResult.WAITING;
                    }
                    if (!definiteTask.isEmpty()) {
                        logger.warn("Cannot find the task instance: {}", JSONUtils.toJsonString(definiteTask));
                        return DependResult.FAILED;
                    }
                }
            }
            return DependResult.SUCCESS;
        }
        return DependResult.FAILED;
    }

    /**
     * get depend task result
     */
    private DependResult getDependTaskResult(ProcessInstance processInstance, long taskCode, DateInterval dateInterval) {
        TaskInstance taskInstance = processService.findLastTaskInstanceInterval(taskCode, dateInterval);
        DependResult result;
        if (taskInstance == null) {
            if (!processInstance.getState().typeIsFinished()) {
                logger.info("Wait for the dependent workflow to complete, taskCode:{}, processInstanceId:{}, processInstance state:{}",
                        taskCode, processInstance.getId(), processInstance.getState());
                return DependResult.WAITING;
            }
            TaskDefinition taskDefinition = processService.findTaskDefinitionByCode(taskCode);
            if (taskDefinition == null) {
                logger.error("Cannot find the task definition, something error, taskCode: {}", taskCode);
            } else {
                logger.warn("Cannot find the task in the process instance when the ProcessInstance is finish, taskCode: {}, taskName: {}", taskCode, taskDefinition.getName());
            }
            result = DependResult.FAILED;
        } else {
            logger.info("The running task, taskId:{}, taskCode:{}, taskName:{}", taskInstance.getId(), taskInstance.getTaskCode(), taskInstance.getName());
            result = getDependResultByState(taskInstance.getState());
        }
        return result;
    }

    /**
     * find the last one process instance that :
     * 1. manual run and finish between the interval
     * 2. schedule run and schedule time between the interval
     *
     * @param definitionCode definition code
     * @param dateInterval   date interval
     * @return ProcessInstance
     */
    private ProcessInstance findLastProcessInterval(Long definitionCode, DateInterval dateInterval) {
        ProcessInstance runningProcess = processService.findLastRunningProcess(definitionCode, dateInterval.getStartTime(), dateInterval.getEndTime());
        if (runningProcess != null) {
            return runningProcess;
        }

        ProcessInstance lastSchedulerProcess = processService.findLastSchedulerProcessInterval(definitionCode, dateInterval);

        ProcessInstance lastManualProcess = processService.findLastManualProcessInterval(definitionCode, dateInterval);

        if (lastManualProcess == null) {
            return lastSchedulerProcess;
        }
        if (lastSchedulerProcess == null) {
            return lastManualProcess;
        }

        return (lastManualProcess.getEndTime().after(lastSchedulerProcess.getEndTime())) ? lastManualProcess : lastSchedulerProcess;
    }

    /**
     * get dependent result by task/process instance state
     *
     * @param state state
     * @return DependResult
     */
    private DependResult getDependResultByState(ExecutionStatus state) {
        if (!state.typeIsFinished()) {
            return DependResult.WAITING;
        } else if (state.typeIsSuccess()) {
            return DependResult.SUCCESS;
        } else {
            return DependResult.FAILED;
        }
    }

    /**
     * judge depend item finished
     *
     * @param currentTime current time
     * @return boolean
     */
    public boolean finish(Date currentTime) {
        if (modelDependResult == DependResult.WAITING || modelDependResult == DependResult.NON_EXEC) {
            modelDependResult = getModelDependResult(currentTime);
            return modelDependResult == DependResult.SUCCESS || modelDependResult == DependResult.FAILED;
        }
        return true;
    }

    /**
     * get model depend result
     *
     * @param currentTime current time
     * @return DependResult
     */
    public DependResult getModelDependResult(Date currentTime) {

        List<DependResult> dependResultList = new ArrayList<>();

        for (DependentItem dependentItem : dependItemList) {
            DependResult dependResult = getDependResultForItem(dependentItem, currentTime);
            if (dependResult != DependResult.WAITING) {
                dependResultMap.put(dependentItem.getKey(), dependResult);
            }
            dependResultList.add(dependResult);
        }
        modelDependResult = DependentUtils.getDependResultForRelation(this.relation, dependResultList);
        return modelDependResult;
    }

    /**
     * get dependent item result
     *
     * @param item        item
     * @param currentTime current time
     * @return DependResult
     */
    private DependResult getDependResultForItem(DependentItem item, Date currentTime) {
        String key = item.getKey();
        if (dependResultMap.containsKey(key)) {
            return dependResultMap.get(key);
        }
        return getDependentResultForItem(item, currentTime);
    }

    public Map<String, DependResult> getDependResultMap() {
        return dependResultMap;
    }

}
