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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ConditionsParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.DependentUtils;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.runner.task.BaseSyncLogicTask;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;

@Slf4j
public class ConditionLogicTask extends BaseSyncLogicTask<ConditionsParameters> {

    public static final String TASK_TYPE = "CONDITIONS";

    private final TaskInstanceDao taskInstanceDao;

    public ConditionLogicTask(IWorkflowExecutionRunnable workflowExecutionRunnable,
                              TaskExecutionContext taskExecutionContext,
                              TaskInstanceDao taskInstanceDao) {
        super(workflowExecutionRunnable, taskExecutionContext,
                JSONUtils.parseObject(taskExecutionContext.getTaskParams(), new TypeReference<ConditionsParameters>() {
                }));
        this.taskInstanceDao = taskInstanceDao;
    }

    @Override
    public void handle() {
        DependResult conditionResult = calculateConditionResult();
        log.info("The condition result is {}", conditionResult);
        taskParameters.getConditionResult().setConditionSuccess(conditionResult == DependResult.SUCCESS);
        taskInstance.setTaskParams(JSONUtils.toJsonString(taskParameters));
        taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.SUCCESS);
    }

    private DependResult calculateConditionResult() {
        final List<TaskInstance> taskInstances = taskInstanceDao.queryValidTaskListByWorkflowInstanceId(
                taskExecutionContext.getProcessInstanceId(), taskExecutionContext.getTestFlag());
        final Map<Long, TaskInstance> taskInstanceMap = taskInstances.stream()
                .collect(Collectors.toMap(TaskInstance::getTaskCode, Function.identity()));

        ConditionsParameters.ConditionDependency dependence = taskParameters.getDependence();
        List<DependResult> dependResults = dependence.getDependTaskList()
                .stream()
                .map(dependentTaskModel -> DependentUtils.getDependResultForRelation(
                        dependentTaskModel.getRelation(),
                        dependentTaskModel.getDependItemList()
                                .stream()
                                .map(dependentItem -> getDependResultForItem(dependentItem, taskInstanceMap))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
        return DependentUtils.getDependResultForRelation(dependence.getRelation(), dependResults);
    }

    private DependResult getDependResultForItem(DependentItem item, Map<Long, TaskInstance> taskInstanceMap) {
        TaskInstance taskInstance = taskInstanceMap.get(item.getDepTaskCode());
        if (taskInstance == null) {
            log.info("The depend item: {} has not completed yet", DependResult.FAILED);
            log.info("The dependent result will be {}", DependResult.FAILED);
            return DependResult.FAILED;
        }

        DependResult dependResult = Objects.equals(item.getStatus(), taskInstance.getState())
                ? DependResult.SUCCESS
                : DependResult.FAILED;
        log.info("The depend item: {}", item);
        log.info("Expect status: {}", item.getStatus());
        log.info("Actual status: {}", taskInstance.getState());
        log.info("The dependent result will be: {}", dependResult);
        return dependResult;
    }
}
