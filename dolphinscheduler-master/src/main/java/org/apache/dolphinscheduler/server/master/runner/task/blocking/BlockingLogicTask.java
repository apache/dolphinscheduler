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

package org.apache.dolphinscheduler.server.master.runner.task.blocking;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.BlockingOpportunity;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentTaskModel;
import org.apache.dolphinscheduler.plugin.task.api.parameters.BlockingParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.DependentUtils;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.task.BaseSyncLogicTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;

@Slf4j
public class BlockingLogicTask extends BaseSyncLogicTask<BlockingParameters> {

    public static final String TASK_TYPE = "BLOCKING";

    private final ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    private final ProcessInstanceDao processInstanceDao;

    private final TaskInstanceDao taskInstanceDao;

    public BlockingLogicTask(TaskExecutionContext taskExecutionContext,
                             ProcessInstanceExecCacheManager processInstanceExecCacheManager,
                             ProcessInstanceDao processInstanceDao,
                             TaskInstanceDao taskInstanceDao) {
        super(taskExecutionContext,
                JSONUtils.parseObject(taskExecutionContext.getTaskParams(), new TypeReference<BlockingParameters>() {
                }));
        this.processInstanceExecCacheManager = processInstanceExecCacheManager;
        this.processInstanceDao = processInstanceDao;
        this.taskInstanceDao = taskInstanceDao;
    }

    @Override
    public void handle() throws MasterTaskExecuteException {
        DependResult conditionResult = calculateConditionResult();
        DependResult expected = taskParameters.getBlockingOpportunity()
                .equals(BlockingOpportunity.BLOCKING_ON_SUCCESS.getDesc())
                        ? DependResult.SUCCESS
                        : DependResult.FAILED;
        boolean isBlocked = (expected == conditionResult);
        log.info("blocking opportunity: expected-->{}, actual-->{}", expected, conditionResult);
        ProcessInstance workflowInstance = processInstanceExecCacheManager
                .getByProcessInstanceId(taskExecutionContext.getProcessInstanceId()).getWorkflowExecuteContext()
                .getWorkflowInstance();
        workflowInstance.setBlocked(isBlocked);
        if (isBlocked) {
            workflowInstance.setStateWithDesc(WorkflowExecutionStatus.READY_BLOCK, "ready block");
        }
        taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.SUCCESS);
    }

    private DependResult calculateConditionResult() throws MasterTaskExecuteException {
        // todo: Directly get the task instance from the cache
        Map<Long, TaskInstance> completeTaskList = taskInstanceDao
                .queryValidTaskListByWorkflowInstanceId(taskExecutionContext.getProcessInstanceId(),
                        taskExecutionContext.getTestFlag())
                .stream()
                .collect(Collectors.toMap(TaskInstance::getTaskCode, Function.identity()));

        // todo: we need to parse the task parameter from TaskExecutionContext
        TaskInstance taskInstance =
                processInstanceExecCacheManager.getByProcessInstanceId(taskExecutionContext.getProcessInstanceId())
                        .getTaskInstance(taskExecutionContext.getTaskInstanceId())
                        .orElseThrow(() -> new MasterTaskExecuteException("Task instance not found"));
        DependentParameters dependentParameters = taskInstance.getDependency();

        List<DependResult> tempResultList = new ArrayList<>();
        for (DependentTaskModel dependentTaskModel : dependentParameters.getDependTaskList()) {
            List<DependResult> itemDependResult = new ArrayList<>();
            for (DependentItem item : dependentTaskModel.getDependItemList()) {
                itemDependResult.add(getDependResultForItem(item, completeTaskList));
            }
            DependResult tempResult =
                    DependentUtils.getDependResultForRelation(dependentTaskModel.getRelation(), itemDependResult);
            tempResultList.add(tempResult);
        }
        return DependentUtils.getDependResultForRelation(dependentParameters.getRelation(), tempResultList);
    }

    private DependResult getDependResultForItem(DependentItem item, Map<Long, TaskInstance> completeTaskList) {

        DependResult dependResult = DependResult.SUCCESS;
        if (!completeTaskList.containsKey(item.getDepTaskCode())) {
            log.info("depend item: {} have not completed yet.", item.getDepTaskCode());
            dependResult = DependResult.FAILED;
            return dependResult;
        }
        TaskInstance taskInstance = completeTaskList.get(item.getDepTaskCode());
        if (taskInstance.getState() != item.getStatus()) {
            log.info("depend item : {} expect status: {}, actual status: {}", item.getDepTaskCode(), item.getStatus(),
                    taskInstance.getState().name());
            dependResult = DependResult.FAILED;
        }
        log.info("Dependent item complete {} {},{}",
                Constants.DEPENDENT_SPLIT, item.getDepTaskCode(), dependResult);
        return dependResult;
    }

}
