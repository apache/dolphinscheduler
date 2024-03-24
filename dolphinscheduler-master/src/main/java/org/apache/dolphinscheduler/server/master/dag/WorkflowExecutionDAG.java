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

package org.apache.dolphinscheduler.server.master.dag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * The WorkflowExecutionDAG represent a running workflow instance DAG.
 */
@Slf4j
@SuperBuilder
public class WorkflowExecutionDAG implements IWorkflowExecutionDAG {

    private final ITaskExecutionContextFactory taskExecutionContextFactory;

    private final ITaskExecutionRunnableFactory taskExecutionRunnableFactory;

    private final TaskExecutionRunnableRepository taskExecutionRunnableRepository;

    private final IWorkflowDAG workflowDAG;

    @Override
    public TaskExecutionRunnable triggerTask(String taskName) {
        DAGNode dagNode = getDAGNode(taskName);
        if (dagNode == null) {
            throw new IllegalArgumentException("Cannot find the DAGNode for task: " + taskName);
        }
        // Create task execution context
        taskExecutionContextFactory.createTaskExecutionContext();
        TaskExecutionRunnable taskExecutionRunnable = taskExecutionRunnableFactory.createTaskExecutionRunnable(null);
        taskExecutionRunnableRepository.storeTaskExecutionRunnable(taskExecutionRunnable);
        return taskExecutionRunnable;
    }

    @Override
    public TaskExecutionRunnable getTaskExecutionRunnableById(Integer taskInstanceId) {
        return taskExecutionRunnableRepository.getTaskExecutionRunnableById(taskInstanceId);
    }

    @Override
    public TaskExecutionRunnable getTaskExecutionRunnableByName(String taskName) {
        return taskExecutionRunnableRepository.getTaskExecutionRunnableByName(taskName);
    }

    @Override
    public List<TaskExecutionRunnable> getActiveTaskExecutionRunnable() {
        return new ArrayList<>(taskExecutionRunnableRepository.getActiveTaskExecutionRunnable());
    }

    @Override
    public List<TaskExecutionRunnable> getDirectPreTaskExecutionRunnable(String taskName) {
        return getDirectPreNodeNames(taskName)
                .stream()
                .map(taskExecutionRunnableRepository::getTaskExecutionRunnableByName)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isTaskAbleToBeTriggered(String taskNodeName) {
        // todo: Check whether the workflow instance is finished or ready to finish.
        List<DAGNode> directPreNodes = getDirectPreNodes(taskNodeName);
        if (log.isDebugEnabled()) {
            log.debug("Begin to check whether the task {} is able to be triggered.", taskNodeName);
            log.debug("Task {} directly dependent on the task: {}.", taskNodeName,
                    directPreNodes.stream().map(DAGNode::getNodeName).collect(Collectors.toList()));
        }
        for (DAGNode directPreNode : directPreNodes) {
            if (directPreNode.isSkip()) {
                log.debug("The task {} is skipped.", directPreNode.getNodeName());
                continue;
            }
            TaskExecutionRunnable taskExecutionRunnable = getTaskExecutionRunnableByName(directPreNode.getNodeName());
            if (taskExecutionRunnable == null || taskExecutionRunnable.canAccessTo(taskNodeName)) {
                log.debug("The task {} is not finished or not able to access to the task {}.",
                        directPreNode.getNodeName(), taskNodeName);
            }
        }
        return true;
    }

    @Override
    public List<DAGNode> getDirectPostNodes(DAGNode dagNode) {
        return workflowDAG.getDirectPostNodes(dagNode);
    }

    @Override
    public List<DAGNode> getDirectPreNodes(DAGNode dagNode) {
        return workflowDAG.getDirectPreNodes(dagNode);
    }

    @Override
    public DAGNode getDAGNode(String nodeName) {
        return workflowDAG.getDAGNode(nodeName);
    }

}
