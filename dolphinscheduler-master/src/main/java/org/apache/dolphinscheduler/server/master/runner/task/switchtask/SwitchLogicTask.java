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

package org.apache.dolphinscheduler.server.master.runner.task.switchtask;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.SwitchResultVo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.exception.LogicTaskInitializeException;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.task.BaseSyncLogicTask;
import org.apache.dolphinscheduler.server.master.utils.SwitchTaskUtils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SwitchLogicTask extends BaseSyncLogicTask<SwitchParameters> {

    public static final String TASK_TYPE = "SWITCH";

    private final WorkflowExecuteRunnable workflowExecuteRunnable;
    private final TaskInstance taskInstance;

    public SwitchLogicTask(TaskExecutionContext taskExecutionContext,
                           ProcessInstanceExecCacheManager processInstanceExecCacheManager) throws LogicTaskInitializeException {
        super(taskExecutionContext,
                // todo: we need to refactor the logic task parameter........
                processInstanceExecCacheManager.getByProcessInstanceId(taskExecutionContext.getProcessInstanceId())
                        .getTaskInstance(taskExecutionContext.getTaskInstanceId())
                        .orElseThrow(() -> new LogicTaskInitializeException(
                                "Cannot find the task instance in workflow execute runnable"))
                        .getSwitchDependency());
        this.workflowExecuteRunnable =
                processInstanceExecCacheManager.getByProcessInstanceId(taskExecutionContext.getProcessInstanceId());
        this.taskInstance = workflowExecuteRunnable.getTaskInstance(taskExecutionContext.getTaskInstanceId())
                .orElseThrow(() -> new LogicTaskInitializeException(
                        "Cannot find the task instance in workflow execute runnable"));
    }

    @Override
    public void handle() throws MasterTaskExecuteException {
        // Calculate the condition result and get the next node
        if (CollectionUtils.isEmpty(taskParameters.getDependTaskList())) {
            moveToDefaultBranch();
        } else {
            calculateSwitchBranch();
        }
        taskInstance.setSwitchDependency(taskParameters);
        log.info("Switch task execute finished");
        taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.SUCCESS);
    }

    private void moveToDefaultBranch() {
        checkIfBranchExist(taskParameters.getNextNode());

        List<SwitchResultVo> switchResultVos = taskParameters.getDependTaskList();
        switchResultVos.add(new SwitchResultVo(null, taskParameters.getNextNode()));
        taskParameters.setResultConditionLocation(switchResultVos.size() - 1);

        log.info("The condition is not satisfied, move to the default branch: {}",
                taskParameters.getNextNode().stream().map(node -> workflowExecuteRunnable.getWorkflowExecuteContext()
                        .getWorkflowGraph().getDag().getNode(node).getName()).collect(Collectors.toList()));
    }

    private void calculateSwitchBranch() {
        List<SwitchResultVo> switchResultVos = taskParameters.getDependTaskList();
        if (CollectionUtils.isEmpty(switchResultVos)) {
            moveToDefaultBranch();
        }
        Map<String, Property> globalParams = taskExecutionContext.getPrepareParamsMap();
        Map<String, Property> varParams = JSONUtils
                .toList(taskInstance.getVarPool(), Property.class)
                .stream()
                .collect(Collectors.toMap(Property::getProp, Property -> Property));

        int finalConditionLocation = -1;
        for (int i = 0; i < switchResultVos.size(); i++) {
            SwitchResultVo switchResultVo = switchResultVos.get(i);
            log.info("Begin to execute {} condition: {} ", i, switchResultVo.getCondition());
            String content = SwitchTaskUtils.generateContentWithTaskParams(switchResultVo.getCondition(), globalParams,
                    varParams);
            log.info("Format condition sentence::{} successfully", content);
            boolean result;
            try {
                result = SwitchTaskUtils.evaluate(content);
                log.info("Execute condition sentence: {} successfully: {}", content, result);
                if (result) {
                    finalConditionLocation = i;
                }
            } catch (Exception e) {
                log.info("Execute condition sentence: {} failed", content, e);
            }
        }
        if (finalConditionLocation >= 0) {
            checkIfBranchExist(switchResultVos.get(finalConditionLocation).getNextNode());
            log.info("The condition is satisfied, move to the branch: {}",
                    switchResultVos.get(finalConditionLocation).getNextNode().stream()
                            .map(node -> workflowExecuteRunnable.getWorkflowExecuteContext().getWorkflowGraph().getDag()
                                    .getNode(node).getName())
                            .collect(Collectors.toList()));
            taskParameters.setResultConditionLocation(finalConditionLocation);
        } else {
            log.info("All conditions are not satisfied, move to the default branch");
            moveToDefaultBranch();
        }
    }

    private void checkIfBranchExist(List<Long> branchNode) {
        if (CollectionUtils.isEmpty(branchNode)) {
            throw new IllegalArgumentException("The branchNode is empty, please check the switch task configuration");
        }
        for (Long branch : branchNode) {
            if (branch == null) {
                throw new IllegalArgumentException("The branch is empty, please check the switch task configuration");
            }
            if (!workflowExecuteRunnable.getWorkflowExecuteContext().getWorkflowGraph().getDag().containsNode(branch)) {
                throw new IllegalArgumentException(
                        "The branch(code= " + branchNode
                                + ") is not in the dag, please check the switch task configuration");
            }
        }
    }

}
