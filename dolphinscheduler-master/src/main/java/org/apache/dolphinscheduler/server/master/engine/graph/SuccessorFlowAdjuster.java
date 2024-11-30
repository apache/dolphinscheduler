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

package org.apache.dolphinscheduler.server.master.engine.graph;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.SwitchResultVo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ConditionsParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.condition.ConditionLogicTask;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.switchtask.SwitchLogicTask;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * Used to adjust the flow of tasks.
 * <p> Only {@link ConditionLogicTask} and {@link SwitchLogicTask} need to adjust the flow of tasks.
 */
@Slf4j
@Component
public class SuccessorFlowAdjuster {

    /**
     * Control the post flow of task.
     * <p> If the task is forbidden, then will not adjust the successor.
     * <p> If the task is skipped, then will try to mark successors which predecessors are all skipped as skipped.
     * <p> If the task {@link ConditionLogicTask}, then will adjust the flow according to the condition result.
     * <p> If the task {@link SwitchLogicTask}, then will adjust the flow according to the switch result.
     */
    public void adjustSuccessorFlow(final ITaskExecutionRunnable taskExecutionRunnable) {
        final IWorkflowExecutionGraph workflowExecutionGraph = taskExecutionRunnable.getWorkflowExecutionGraph();

        if (workflowExecutionGraph.isTaskExecutionRunnableSkipped(taskExecutionRunnable)) {
            // If the successor flow's all parent is skipped, then mark the successor skipped.
            for (ITaskExecutionRunnable successor : workflowExecutionGraph.getSuccessors(taskExecutionRunnable)) {
                if (workflowExecutionGraph.isAllPredecessorsSkipped(successor)) {
                    workflowExecutionGraph.markTaskSkipped(successor);
                }
            }
            return;
        }

        if (workflowExecutionGraph.isTaskExecutionRunnableForbidden(taskExecutionRunnable)) {
            return;
        }

        final String taskType = taskExecutionRunnable.getTaskInstance().getTaskType();
        if (TaskTypeUtils.isConditionTask(taskType)) {
            adjustConditionTaskSuccessorFlow(taskExecutionRunnable);
            return;
        }

        if (TaskTypeUtils.isSwitchTask(taskType)) {
            adjustSwitchTaskSuccessorFlow(taskExecutionRunnable);
            return;
        }
    }

    private void adjustConditionTaskSuccessorFlow(final ITaskExecutionRunnable taskExecutionRunnable) {
        final String taskParams = taskExecutionRunnable.getTaskInstance().getTaskParams();
        final ConditionsParameters conditionsParameters = JSONUtils.parseObject(taskParams, ConditionsParameters.class);
        if (conditionsParameters == null) {
            throw new IllegalArgumentException("Condition task params: " + taskParams + " is invalid.");
        }
        final ConditionsParameters.ConditionResult conditionResult = conditionsParameters.getConditionResult();
        if (conditionResult == null) {
            throw new IllegalArgumentException("ConditionResult: is null in taskParam: " + taskParams);
        }
        final List<Long> needSkippedBranch;
        if (conditionResult.isConditionSuccess()) {
            needSkippedBranch = conditionResult.getFailedNode();
        } else {
            needSkippedBranch = conditionResult.getSuccessNode();
        }
        markTaskSkipped(taskExecutionRunnable, needSkippedBranch);
    }

    private void adjustSwitchTaskSuccessorFlow(final ITaskExecutionRunnable taskExecutionRunnable) {
        final String taskParams = taskExecutionRunnable.getTaskInstance().getTaskParams();
        final SwitchParameters switchParameters = JSONUtils.parseObject(taskParams, SwitchParameters.class);
        if (switchParameters == null) {
            throw new IllegalArgumentException("Switch task params: " + taskParams + " is invalid.");
        }
        final SwitchParameters.SwitchResult switchResult = switchParameters.getSwitchResult();
        if (switchResult == null) {
            throw new IllegalArgumentException("ConditionResult: is null in taskParam: " + taskParams);
        }
        final Set<Long> needSkippedBranch = new HashSet<>();
        if (switchResult.getNextNode() != null) {
            needSkippedBranch.add(switchResult.getNextNode());
        }
        if (CollectionUtils.isNotEmpty(switchResult.getDependTaskList())) {
            for (SwitchResultVo switchResultVo : switchResult.getDependTaskList()) {
                needSkippedBranch.add(switchResultVo.getNextNode());
            }
        }
        needSkippedBranch.remove(switchResult.getNextNode());
        markTaskSkipped(taskExecutionRunnable, needSkippedBranch);
    }

    private void markTaskSkipped(final ITaskExecutionRunnable taskExecutionRunnable,
                                 final Collection<Long> needSkippedTaskCodes) {
        if (CollectionUtils.isEmpty(needSkippedTaskCodes)) {
            return;
        }
        final IWorkflowExecutionGraph workflowExecutionGraph = taskExecutionRunnable.getWorkflowExecutionGraph();
        for (Long taskCode : needSkippedTaskCodes) {
            final ITaskExecutionRunnable branch = workflowExecutionGraph.getTaskExecutionRunnableByTaskCode(taskCode);
            if (branch == null) {
                log.info("Branch(taskCode={}) is not found in the workflow: {}.", taskCode,
                        taskExecutionRunnable.getWorkflowInstance().getName());
                continue;
            }
            workflowExecutionGraph.markTaskSkipped(taskExecutionRunnable);
        }
    }

}
