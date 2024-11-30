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

package org.apache.dolphinscheduler.server.master.engine.executor.plugin.switchtask;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.SwitchResultVo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.AbstractLogicTask;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.ITaskParameterDeserializer;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.runner.IWorkflowExecuteContext;
import org.apache.dolphinscheduler.server.master.utils.SwitchTaskUtils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;

@Slf4j
public class SwitchLogicTask extends AbstractLogicTask<SwitchParameters> {

    private final IWorkflowExecutionRunnable workflowExecutionRunnable;
    private final TaskInstance taskInstance;

    public SwitchLogicTask(IWorkflowExecutionRunnable workflowExecutionRunnable,
                           TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.workflowExecutionRunnable = workflowExecutionRunnable;
        this.taskInstance = workflowExecutionRunnable
                .getWorkflowExecuteContext()
                .getWorkflowExecutionGraph()
                .getTaskExecutionRunnableById(taskExecutionContext.getTaskInstanceId())
                .getTaskInstance();
        onTaskRunning();
    }

    @Override
    public void start() {
        if (CollectionUtils.isEmpty(taskParameters.getSwitchResult().getDependTaskList())) {
            // If the branch is empty then will go into the default branch
            // This case shouldn't happen, we can directly throw exception and forbid the user to set branch
            log.info("The switch items is empty");
            moveToDefaultBranch();
        } else {
            calculateSwitchBranch();
        }
        checkIfBranchExist(taskParameters.getNextBranch());
        taskInstance.setTaskParams(JSONUtils.toJsonString(taskParameters));

        onTaskSuccess();
        log.info("Switch task execute finished");
    }

    private void moveToDefaultBranch() {
        log.info("Begin to move to the default branch");
        if (taskParameters.getSwitchResult().getNextNode() == null) {
            throw new IllegalArgumentException(
                    "The default branch is empty, please check the switch task configuration");
        }
        taskParameters.setNextBranch(taskParameters.getSwitchResult().getNextNode());
        log.info("The condition is not satisfied, move to the default branch: {}",
                getTaskName(taskParameters.getNextBranch()));
    }

    private void calculateSwitchBranch() {
        List<SwitchResultVo> switchResultVos = taskParameters.getSwitchResult().getDependTaskList();
        Map<String, Property> globalParams = taskExecutionContext.getPrepareParamsMap();
        Map<String, Property> varParams = JSONUtils
                .toList(taskInstance.getVarPool(), Property.class)
                .stream()
                .collect(Collectors.toMap(Property::getProp, Property -> Property));

        Long nextBranch = null;
        for (SwitchResultVo switchResultVo : switchResultVos) {
            log.info("Begin to execute switch item: {} ", switchResultVo);
            try {
                String content = SwitchTaskUtils.generateContentWithTaskParams(switchResultVo.getCondition(),
                        globalParams, varParams);
                log.info("Format condition sentence::{} successfully", content);
                boolean conditionResult = SwitchTaskUtils.evaluate(content);
                log.info("Execute condition sentence: {} successfully: {}", content, conditionResult);
                if (conditionResult) {
                    // If matched, break the loop
                    nextBranch = switchResultVo.getNextNode();
                    break;
                }
            } catch (Exception e) {
                log.info("Execute switch item: {} failed", switchResultVo, e);
            }
        }

        if (nextBranch == null) {
            log.info("All switch item is not satisfied");
            moveToDefaultBranch();
        }
    }

    private void checkIfBranchExist(Long branchNode) {
        if (branchNode == null) {
            throw new IllegalArgumentException("The branch is empty, please check the switch task configuration");
        }
        if (workflowExecutionRunnable.getWorkflowExecuteContext().getWorkflowGraph()
                .getTaskNodeByCode(branchNode) == null) {
            throw new IllegalArgumentException(
                    "The branch(code= " + branchNode
                            + ") is not in the dag, please check the switch task configuration");
        }
    }

    private String getTaskName(Long taskCode) {
        return Optional.ofNullable(workflowExecutionRunnable.getWorkflowExecuteContext())
                .map(IWorkflowExecuteContext::getWorkflowGraph)
                .map(iWorkflowGraph -> iWorkflowGraph.getTaskNodeByCode(taskCode))
                .map(TaskDefinition::getName)
                .orElse(null);
    }

    @Override
    public void pause() {
        log.info("The SwitchTask does not support pause operation");
    }

    @Override
    public void kill() {
        log.info("The SwitchTask does not support kill operation");
    }

    @Override
    public ITaskParameterDeserializer<SwitchParameters> getTaskParameterDeserializer() {
        return taskParamsJson -> JSONUtils.parseObject(taskParamsJson, new TypeReference<SwitchParameters>() {
        });
    }

}
