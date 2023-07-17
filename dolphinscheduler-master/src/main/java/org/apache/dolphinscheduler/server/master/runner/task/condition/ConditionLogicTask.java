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

package org.apache.dolphinscheduler.server.master.runner.task.condition;

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.DependentUtils;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.exception.LogicTaskInitializeException;
import org.apache.dolphinscheduler.server.master.runner.task.BaseSyncLogicTask;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConditionLogicTask extends BaseSyncLogicTask<DependentParameters> {

    public static final String TASK_TYPE = "CONDITIONS";

    private final TaskInstanceDao taskInstanceDao;
    private final ProcessInstanceDao workflowInstanceDao;

    public ConditionLogicTask(TaskExecutionContext taskExecutionContext,
                              ProcessInstanceExecCacheManager processInstanceExecCacheManager,
                              TaskInstanceDao taskInstanceDao,
                              ProcessInstanceDao workflowInstanceDao) throws LogicTaskInitializeException {
        // todo: we need to change the parameter in front-end, so that we can directly use json to parse
        super(taskExecutionContext,
                processInstanceExecCacheManager.getByProcessInstanceId(taskExecutionContext.getProcessInstanceId())
                        .getTaskInstance(taskExecutionContext.getTaskInstanceId())
                        .orElseThrow(() -> new LogicTaskInitializeException(
                                "Cannot find the task instance in workflow execute runnable"))
                        .getDependency());
        // todo：check the parameters, why we don't use conditionTask? taskInstance.getDependency();
        this.taskInstanceDao = taskInstanceDao;
        this.workflowInstanceDao = workflowInstanceDao;
    }

    @Override
    public void handle() {
        // calculate the conditionResult
        DependResult conditionResult = calculateConditionResult();
        TaskExecutionStatus taskExecutionStatus =
                (conditionResult == DependResult.SUCCESS) ? TaskExecutionStatus.SUCCESS : TaskExecutionStatus.FAILURE;
        log.info("The condition result is {}, task instance statue will be: {}", conditionResult, taskExecutionStatus);
        taskExecutionContext.setCurrentExecutionStatus(taskExecutionStatus);
    }

    private DependResult calculateConditionResult() {
        final ProcessInstance processInstance =
                workflowInstanceDao.queryById(taskExecutionContext.getProcessInstanceId());
        final List<TaskInstance> taskInstances =
                taskInstanceDao.queryValidTaskListByWorkflowInstanceId(processInstance.getId(),
                        processInstance.getTestFlag());
        final Map<Long, TaskInstance> taskInstanceMap =
                taskInstances.stream().collect(Collectors.toMap(TaskInstance::getTaskCode, Function.identity()));

        List<DependResult> dependResults = taskParameters.getDependTaskList().stream()
                .map(dependentTaskModel -> DependentUtils.getDependResultForRelation(
                        dependentTaskModel.getRelation(),
                        dependentTaskModel.getDependItemList()
                                .stream()
                                .map(dependentItem -> getDependResultForItem(dependentItem, taskInstanceMap))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
        return DependentUtils.getDependResultForRelation(taskParameters.getRelation(), dependResults);
    }

    private DependResult getDependResultForItem(DependentItem item, Map<Long, TaskInstance> taskInstanceMap) {
        TaskInstance taskInstance = taskInstanceMap.get(item.getDepTaskCode());
        if (taskInstance == null) {
            log.info("The depend item: {} has not completed yet", DependResult.FAILED);
            log.info("The dependent result will be {}", DependResult.FAILED);
            return DependResult.FAILED;
        }

        DependResult dependResult =
                Objects.equals(item.getStatus(), taskInstance.getState()) ? DependResult.SUCCESS : DependResult.FAILED;
        log.info("The depend item: {}", item);
        log.info("Expect status: {}", item.getStatus());
        log.info("Actual status: {}", taskInstance.getState());
        log.info("The dependent result will be: {}", dependResult);
        return dependResult;
    }
}
