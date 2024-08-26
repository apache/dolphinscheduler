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

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WorkflowExecutionGraph implements IWorkflowExecutionGraph {

    private final Map<String, ITaskExecutionRunnable> totalTaskExecuteRunnableMap;

    private final Set<String> failureTaskChains;

    private final Set<String> pausedTaskChains;

    private final Set<String> killedTaskChains;

    private final Set<String> skippedTask;

    private final Map<String, Set<String>> predecessors;

    private final Map<String, Set<String>> successors;

    private final Set<String> activeTaskExecutionRunnable;

    public WorkflowExecutionGraph() {
        this.failureTaskChains = new HashSet<>();
        this.pausedTaskChains = new HashSet<>();
        this.killedTaskChains = new HashSet<>();
        this.skippedTask = new HashSet<>();
        this.predecessors = new HashMap<>();
        this.successors = new HashMap<>();
        this.totalTaskExecuteRunnableMap = new HashMap<>();
        this.activeTaskExecutionRunnable = new HashSet<>();
    }

    @Override
    public void addNode(final ITaskExecutionRunnable taskExecutionRunnable) {
        totalTaskExecuteRunnableMap.put(taskExecutionRunnable.getName(), taskExecutionRunnable);
        predecessors.computeIfAbsent(taskExecutionRunnable.getName(), k -> new HashSet<>());
        successors.computeIfAbsent(taskExecutionRunnable.getName(), k -> new HashSet<>());
    }

    @Override
    public void addEdge(String fromTaskName, Set<String> toTaskNames) {
        successors.computeIfAbsent(fromTaskName, k -> new HashSet<>()).addAll(toTaskNames);
        toTaskNames.forEach(toTask -> predecessors.computeIfAbsent(toTask, k -> new HashSet<>()).add(fromTaskName));
    }

    @Override
    public List<ITaskExecutionRunnable> getStartNodes() {
        return totalTaskExecuteRunnableMap.values()
                .stream()
                .filter(taskExecutionRunnable -> CollectionUtils
                        .isEmpty(predecessors.get(taskExecutionRunnable.getName())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ITaskExecutionRunnable> getPredecessors(final String taskName) {
        if (!predecessors.containsKey(taskName)) {
            throw new IllegalArgumentException("Cannot find the task: " + taskName + " in graph");
        }
        return predecessors
                .get(taskName)
                .stream()
                .map(this::getTaskExecutionRunnableByName)
                .collect(Collectors.toList());
    }

    @Override
    public List<ITaskExecutionRunnable> getSuccessors(final String taskName) {
        if (!successors.containsKey(taskName)) {
            throw new IllegalArgumentException("Cannot find the task code in graph");
        }
        return successors
                .get(taskName)
                .stream()
                .map(this::getTaskExecutionRunnableByName)
                .collect(Collectors.toList());
    }

    @Override
    public List<ITaskExecutionRunnable> getSuccessors(final ITaskExecutionRunnable taskExecutionRunnable) {
        return getSuccessors(taskExecutionRunnable.getName());
    }

    @Override
    public ITaskExecutionRunnable getTaskExecutionRunnableByName(final String taskName) {
        return totalTaskExecuteRunnableMap.get(taskName);
    }

    @Override
    public ITaskExecutionRunnable getTaskExecutionRunnableById(final Integer taskInstanceId) {
        return totalTaskExecuteRunnableMap.values()
                .stream()
                .filter(taskExecutionRunnable -> taskExecutionRunnable.getTaskInstance() != null
                        && taskInstanceId.equals(taskExecutionRunnable.getTaskInstance().getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public ITaskExecutionRunnable getTaskExecutionRunnableByTaskCode(final Long taskCode) {
        return totalTaskExecuteRunnableMap.values()
                .stream()
                .filter(taskExecutionRunnable -> taskExecutionRunnable.getTaskDefinition() != null
                        && taskCode.equals(taskExecutionRunnable.getTaskDefinition().getCode()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean isTaskExecutionRunnableActive(final ITaskExecutionRunnable taskExecutionRunnable) {
        return activeTaskExecutionRunnable.add(taskExecutionRunnable.getName());
    }

    @Override
    public boolean isTaskExecutionRunnableKilled(final ITaskExecutionRunnable taskExecutionRunnable) {
        return killedTaskChains.contains(taskExecutionRunnable.getName());
    }

    @Override
    public List<ITaskExecutionRunnable> getActiveTaskExecutionRunnable() {
        return activeTaskExecutionRunnable
                .stream()
                .map(this::getTaskExecutionRunnableByName)
                .collect(Collectors.toList());
    }

    @Override
    public List<ITaskExecutionRunnable> getAllTaskExecutionRunnable() {
        return new ArrayList<>(totalTaskExecuteRunnableMap.values());
    }

    @Override
    public boolean isTriggerConditionMet(final ITaskExecutionRunnable taskExecutionRunnable) {
        return getPredecessors(taskExecutionRunnable.getName())
                .stream()
                .allMatch(predecessor -> isTaskFinish(predecessor)
                        && !isTaskFailure(predecessor)
                        && !isTaskPaused(predecessor)
                        && !isTaskKilled(predecessor));
    }

    @Override
    public boolean isAllTaskExecutionRunnableChainFinish() {
        return activeTaskExecutionRunnable.isEmpty();
    }

    @Override
    public boolean isAllTaskExecutionRunnableChainSuccess() {
        if (!isAllTaskExecutionRunnableChainFinish()) {
            return false;
        }
        return !isExistFailureTaskExecutionRunnableChain()
                && !isExistPauseTaskExecutionRunnableChain()
                && !isExistKillTaskExecutionRunnableChain();
    }

    @Override
    public boolean isExistFailureTaskExecutionRunnableChain() {
        return CollectionUtils.isNotEmpty(failureTaskChains);
    }

    @Override
    public boolean isExistPauseTaskExecutionRunnableChain() {
        return CollectionUtils.isNotEmpty(pausedTaskChains);
    }

    @Override
    public boolean isExistKillTaskExecutionRunnableChain() {
        return CollectionUtils.isNotEmpty(killedTaskChains);
    }

    @Override
    public void markTaskExecutionRunnableActive(final ITaskExecutionRunnable taskExecutionRunnable) {
        activeTaskExecutionRunnable.add(taskExecutionRunnable.getName());
    }

    @Override
    public void markTaskExecutionRunnableInActive(final ITaskExecutionRunnable taskExecutionRunnable) {
        activeTaskExecutionRunnable.remove(taskExecutionRunnable.getName());
    }

    @Override
    public void markTaskExecutionRunnableChainFailure(final ITaskExecutionRunnable taskExecutionRunnable) {
        assertTaskExecutionRunnableState(taskExecutionRunnable, TaskExecutionStatus.FAILURE);
        failureTaskChains.add(taskExecutionRunnable.getName());
    }

    @Override
    public void markTaskExecutionRunnableChainPause(final ITaskExecutionRunnable taskExecutionRunnable) {
        assertTaskExecutionRunnableState(taskExecutionRunnable, TaskExecutionStatus.PAUSE);
        pausedTaskChains.add(taskExecutionRunnable.getName());
    }

    @Override
    public void markTaskExecutionRunnableChainKill(final ITaskExecutionRunnable taskExecutionRunnable) {
        assertTaskExecutionRunnableState(taskExecutionRunnable, TaskExecutionStatus.KILL);
        killedTaskChains.add(taskExecutionRunnable.getName());
    }

    @Override
    public void markTaskSkipped(final ITaskExecutionRunnable taskExecutionRunnable) {
        markTaskSkipped(taskExecutionRunnable.getName());
    }

    @Override
    public void markTaskSkipped(final String taskName) {
        skippedTask.add(taskName);
    }

    @Override
    public boolean isEndOfTaskChain(final ITaskExecutionRunnable taskExecutionRunnable) {
        return successors.get(taskExecutionRunnable.getName()).isEmpty()
                || killedTaskChains.contains(taskExecutionRunnable.getName())
                || pausedTaskChains.contains(taskExecutionRunnable.getName());
    }

    @Override
    public boolean isTaskExecutionRunnableSkipped(final ITaskExecutionRunnable taskExecutionRunnable) {
        return skippedTask.contains(taskExecutionRunnable.getName());
    }

    @Override
    public boolean isTaskExecutionRunnableForbidden(final ITaskExecutionRunnable taskExecutionRunnable) {
        return false;
    }

    /**
     * Whether all predecessors are skipped.
     * <p> Only when all predecessors are skipped, will return true. If the given task doesn't have any predecessors, will return false.
     */
    @Override
    public boolean isAllPredecessorsSkipped(final ITaskExecutionRunnable taskExecutionRunnable) {
        final List<ITaskExecutionRunnable> predecessors = getPredecessors(taskExecutionRunnable.getName());
        if (CollectionUtils.isEmpty(predecessors)) {
            return false;
        }
        return CollectionUtils.isEmpty(predecessors)
                || predecessors.stream().allMatch(this::isTaskExecutionRunnableSkipped);
    }

    @Override
    public boolean isAllSuccessorsAreConditionTask(final ITaskExecutionRunnable taskExecutionRunnable) {
        final List<ITaskExecutionRunnable> successors = getSuccessors(taskExecutionRunnable.getName());
        if (CollectionUtils.isEmpty(successors)) {
            return false;
        }
        return successors.stream().allMatch(
                successor -> isTaskExecutionRunnableSkipped(successor)
                        || TaskTypeUtils.isConditionTask(taskExecutionRunnable.getTaskInstance().getTaskType()));
    }

    private boolean isTaskFinish(final ITaskExecutionRunnable taskExecutionRunnable) {
        return !activeTaskExecutionRunnable.contains(taskExecutionRunnable.getName());
    }

    private boolean isTaskFailure(final ITaskExecutionRunnable taskExecutionRunnable) {
        return failureTaskChains.contains(taskExecutionRunnable.getName());
    }

    private boolean isTaskPaused(final ITaskExecutionRunnable taskExecutionRunnable) {
        return pausedTaskChains.contains(taskExecutionRunnable.getName());
    }

    private boolean isTaskKilled(final ITaskExecutionRunnable taskExecutionRunnable) {
        return killedTaskChains.contains(taskExecutionRunnable.getName());
    }

    private void assertTaskExecutionRunnableState(final ITaskExecutionRunnable taskExecutionRunnable,
                                                  final TaskExecutionStatus taskExecutionStatus) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        if (taskInstance.getState() == taskExecutionStatus) {
            return;
        }
        throw new IllegalStateException(
                "The task: " + taskExecutionRunnable.getName() + " state: " + taskInstance.getState() + " is not "
                        + taskExecutionStatus);
    }

}
