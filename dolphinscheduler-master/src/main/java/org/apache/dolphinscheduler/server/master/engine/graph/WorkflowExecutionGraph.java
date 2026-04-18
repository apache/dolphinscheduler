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

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WorkflowExecutionGraph implements IWorkflowExecutionGraph {

    // Store all the task execution runnable in the execution graph.
    private final Map<String, ITaskExecution> totalTaskExecuteRunnableMap;

    private final Set<String> failureTaskChains;

    private final Set<String> pausedTaskChains;

    private final Set<String> killedTaskChains;

    private final Set<String> skippedTask;

    private final Map<String, Set<String>> predecessors;

    private final Map<String, Set<String>> successors;

    private final Set<String> activeTaskExecution;

    private final Set<String> inActiveTaskExecution;

    public WorkflowExecutionGraph() {
        this.failureTaskChains = new HashSet<>();
        this.pausedTaskChains = new HashSet<>();
        this.killedTaskChains = new HashSet<>();
        this.skippedTask = new HashSet<>();
        this.predecessors = new HashMap<>();
        this.successors = new HashMap<>();
        this.totalTaskExecuteRunnableMap = new HashMap<>();
        this.activeTaskExecution = new HashSet<>();
        this.inActiveTaskExecution = new HashSet<>();
    }

    @Override
    public void addNode(final ITaskExecution taskExecution) {
        totalTaskExecuteRunnableMap.put(taskExecution.getName(), taskExecution);
        predecessors.computeIfAbsent(taskExecution.getName(), k -> new HashSet<>());
        successors.computeIfAbsent(taskExecution.getName(), k -> new HashSet<>());
    }

    @Override
    public void addEdge(String fromTaskName, Set<String> toTaskNames) {
        successors.computeIfAbsent(fromTaskName, k -> new HashSet<>()).addAll(toTaskNames);
        toTaskNames.forEach(toTask -> predecessors.computeIfAbsent(toTask, k -> new HashSet<>()).add(fromTaskName));
    }

    @Override
    public void removeUnReachableEdge() {
        // If the node in successors or predecessors is not in taskExecuteRunnableMap
        // It means that the node is not executable, so we need to filter it out
        Consumer<Map<String, Set<String>>> removeUnReachableEdge = edgeMap -> {
            final Iterator<Map.Entry<String, Set<String>>> iterator = edgeMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Set<String>> entry = iterator.next();
                if (!totalTaskExecuteRunnableMap.containsKey(entry.getKey())) {
                    iterator.remove();
                    continue;
                }
                Set<String> toTasks = entry.getValue();
                toTasks.removeIf(toTask -> !totalTaskExecuteRunnableMap.containsKey(toTask));
            }
        };
        removeUnReachableEdge.accept(successors);
        removeUnReachableEdge.accept(predecessors);
    }

    @Override
    public List<ITaskExecution> getStartNodes() {
        return totalTaskExecuteRunnableMap.values()
                .stream()
                .filter(taskExecution -> CollectionUtils
                        .isEmpty(predecessors.get(taskExecution.getName())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ITaskExecution> getPredecessors(final String taskName) {
        if (!predecessors.containsKey(taskName)) {
            throw new IllegalArgumentException("Cannot find the task: " + taskName + " in graph");
        }
        return predecessors
                .get(taskName)
                .stream()
                .map(this::getTaskExecutionByName)
                .collect(Collectors.toList());
    }

    @Override
    public List<ITaskExecution> getSuccessors(final String taskName) {
        if (!successors.containsKey(taskName)) {
            throw new IllegalArgumentException("Cannot find the task code in graph");
        }
        return successors
                .get(taskName)
                .stream()
                .map(this::getTaskExecutionByName)
                .collect(Collectors.toList());
    }

    @Override
    public List<ITaskExecution> getSuccessors(final ITaskExecution taskExecution) {
        return getSuccessors(taskExecution.getName());
    }

    @Override
    public ITaskExecution getTaskExecutionByName(final String taskName) {
        return totalTaskExecuteRunnableMap.get(taskName);
    }

    @Override
    public ITaskExecution getTaskExecutionById(final Integer taskInstanceId) {
        return totalTaskExecuteRunnableMap.values()
                .stream()
                .filter(taskExecution -> taskExecution.getTaskInstance() != null
                        && taskInstanceId.equals(taskExecution.getTaskInstance().getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public ITaskExecution getTaskExecutionByTaskCode(final Long taskCode) {
        return totalTaskExecuteRunnableMap.values()
                .stream()
                .filter(taskExecution -> taskExecution.getTaskDefinition() != null
                        && taskCode.equals(taskExecution.getTaskDefinition().getCode()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean isTaskExecutionActive(final ITaskExecution taskExecution) {
        return activeTaskExecution.contains(taskExecution.getName());
    }

    @Override
    public boolean isTaskExecutionInActive(ITaskExecution taskExecution) {
        return inActiveTaskExecution.contains(taskExecution.getName());
    }

    @Override
    public boolean isTaskExecutionKilled(final ITaskExecution taskExecution) {
        return killedTaskChains.contains(taskExecution.getName());
    }

    @Override
    public boolean isTaskExecutionFailed(ITaskExecution taskExecution) {
        return failureTaskChains.contains(taskExecution.getName());
    }

    @Override
    public boolean isTaskExecutionPaused(ITaskExecution taskExecution) {
        return pausedTaskChains.contains(taskExecution.getName());
    }

    @Override
    public List<ITaskExecution> getActiveTaskExecution() {
        return activeTaskExecution
                .stream()
                .map(this::getTaskExecutionByName)
                .collect(Collectors.toList());
    }

    @Override
    public List<ITaskExecution> getAllTaskExecution() {
        return new ArrayList<>(totalTaskExecuteRunnableMap.values());
    }

    @Override
    public boolean isTriggerConditionMet(final ITaskExecution taskExecution) {
        if (isTaskExecutionActive(taskExecution)
                || isTaskExecutionInActive(taskExecution)) {
            return false;
        }
        return getPredecessors(taskExecution.getName())
                .stream()
                .allMatch(predecessor -> isTaskExecutionInActive(predecessor)
                        && !isTaskExecutionFailed(predecessor)
                        && !isTaskExecutionPaused(predecessor)
                        && !isTaskExecutionKilled(predecessor));
    }

    @Override
    public boolean isAllTaskExecutionChainFinish() {
        return activeTaskExecution.isEmpty();
    }

    @Override
    public boolean isAllTaskExecutionChainSuccess() {
        if (!isAllTaskExecutionChainFinish()) {
            return false;
        }
        return !isExistFailureTaskExecutionChain()
                && !isExistPausedTaskExecutionChain()
                && !isExistKilledTaskExecutionChain();
    }

    @Override
    public boolean isExistFailureTaskExecutionChain() {
        return CollectionUtils.isNotEmpty(failureTaskChains);
    }

    @Override
    public boolean isExistPausedTaskExecutionChain() {
        return CollectionUtils.isNotEmpty(pausedTaskChains);
    }

    @Override
    public boolean isExistKilledTaskExecutionChain() {
        return CollectionUtils.isNotEmpty(killedTaskChains);
    }

    @Override
    public void markTaskExecutionActive(final ITaskExecution taskExecution) {
        activeTaskExecution.add(taskExecution.getName());
    }

    @Override
    public void markTaskExecutionInActive(final ITaskExecution taskExecution) {
        activeTaskExecution.remove(taskExecution.getName());
        inActiveTaskExecution.add(taskExecution.getName());
    }

    @Override
    public void markTaskExecutionChainFailure(final ITaskExecution taskExecution) {
        assertTaskExecutionState(taskExecution, TaskExecutionStatus.FAILURE);
        failureTaskChains.add(taskExecution.getName());
    }

    @Override
    public void markTaskExecutionChainPause(final ITaskExecution taskExecution) {
        assertTaskExecutionState(taskExecution, TaskExecutionStatus.PAUSE);
        pausedTaskChains.add(taskExecution.getName());
    }

    @Override
    public void markTaskExecutionChainKill(final ITaskExecution taskExecution) {
        assertTaskExecutionState(taskExecution, TaskExecutionStatus.KILL);
        killedTaskChains.add(taskExecution.getName());
    }

    @Override
    public void markTaskSkipped(final ITaskExecution taskExecution) {
        markTaskSkipped(taskExecution.getName());
    }

    @Override
    public void markTaskSkipped(final String taskName) {
        skippedTask.add(taskName);
    }

    @Override
    public boolean isEndOfTaskChain(final ITaskExecution taskExecution) {
        return successors.get(taskExecution.getName()).isEmpty()
                || isTaskExecutionKilled(taskExecution)
                || isTaskExecutionPaused(taskExecution)
                || (isTaskExecutionFailed(taskExecution)
                        && !isAllSuccessorsAreConditionTask(taskExecution));
    }

    @Override
    public boolean isTaskExecutionSkipped(final ITaskExecution taskExecution) {
        return skippedTask.contains(taskExecution.getName());
    }

    @Override
    public boolean isTaskExecutionForbidden(final ITaskExecution taskExecution) {
        return (taskExecution.getTaskDefinition().getFlag() == Flag.NO);
    }

    @Override
    public boolean isTaskExecutionRetrying(final ITaskExecution taskExecution) {
        if (!taskExecution.isTaskInstanceInitialized()) {
            return false;
        }
        final TaskInstance taskInstance = taskExecution.getTaskInstance();
        return taskInstance.getState() == TaskExecutionStatus.FAILURE && taskExecution.isTaskInstanceCanRetry()
                && isTaskExecutionActive(taskExecution);
    }

    /**
     * Whether all predecessors are skipped.
     * <p> Only when all predecessors are skipped, will return true. If the given task doesn't have any predecessors, will return false.
     */
    @Override
    public boolean isAllPredecessorsSkipped(final ITaskExecution taskExecution) {
        final List<ITaskExecution> predecessors = getPredecessors(taskExecution.getName());
        if (CollectionUtils.isEmpty(predecessors)) {
            return false;
        }
        return CollectionUtils.isEmpty(predecessors)
                || predecessors.stream().allMatch(this::isTaskExecutionSkipped);
    }

    @Override
    public boolean isAllSuccessorsAreConditionTask(final ITaskExecution taskExecution) {
        final List<ITaskExecution> successors = getSuccessors(taskExecution.getName());
        if (CollectionUtils.isEmpty(successors)) {
            return false;
        }
        return successors.stream().allMatch(
                successor -> isTaskExecutionSkipped(successor)
                        || (TaskTypeUtils.isConditionTask(successor.getTaskDefinition().getTaskType())
                                && !isTaskExecutionForbidden(successor)));
    }

    private void assertTaskExecutionState(final ITaskExecution taskExecution,
                                          final TaskExecutionStatus taskExecutionStatus) {
        final TaskInstance taskInstance = taskExecution.getTaskInstance();
        if (taskInstance.getState() == taskExecutionStatus) {
            return;
        }
        throw new IllegalStateException(
                "The task: " + taskExecution.getName() + " state: " + taskInstance.getState() + " is not "
                        + taskExecutionStatus);
    }

}
