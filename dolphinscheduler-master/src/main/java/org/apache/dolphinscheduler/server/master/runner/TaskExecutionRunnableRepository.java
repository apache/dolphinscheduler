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

package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

/**
 * The repository used to store the {@link ITaskExecutionRunnable} in {@link IWorkflowExecutionRunnable}.
 * This will not store all the {@link ITaskExecutionRunnable} in the memory, it will only store the {@link ITaskExecutionRunnable} which is active.
 */
public class TaskExecutionRunnableRepository {

    /**
     * Store all task execution runnable in the memory.
     */
    private final Map<Integer, ITaskExecutionRunnable> taskExecuteRunnableMap = new ConcurrentHashMap<>();

    /**
     * The active task instance ids.
     */
    private final Set<Integer> activeTaskInstanceIds = ConcurrentHashMap.newKeySet();

    /**
     * The finished task instance ids.
     */
    private final Set<Integer> finishedTaskInstanceIds = ConcurrentHashMap.newKeySet();

    private final Set<Long> createFailureTaskCodes = ConcurrentHashMap.newKeySet();

    public ITaskExecutionRunnable getTaskExecutionRunnable(Integer taskInstanceId) {
        return taskExecuteRunnableMap.get(taskInstanceId);
    }

    public List<ITaskExecutionRunnable> getActiveTaskExecutionRunnable() {
        return taskExecuteRunnableMap.values()
                .stream()
                .filter(
                        taskExecuteRunnable -> activeTaskInstanceIds
                                .contains(taskExecuteRunnable.getTaskInstance().getId()))
                .collect(Collectors.toList());
    }

    public List<ITaskExecutionRunnable> getAllTaskExecutionRunnable() {
        return new ArrayList<>(taskExecuteRunnableMap.values());
    }

    /**
     * Check if all task execution runnable is finish.
     */
    public boolean isAllTaskExecutionRunnableFinish() {
        return taskExecuteRunnableMap.values()
                .stream()
                .allMatch(taskExecuteRunnable -> taskExecuteRunnable.getTaskInstance().getState().isFinished());
    }

    /**
     * Check if all task execution runnable is success.
     */
    public boolean isAllTaskExecutionRunnableSuccess() {
        return taskExecuteRunnableMap.values()
                .stream()
                .allMatch(taskExecuteRunnable -> taskExecuteRunnable.getTaskInstance().getState().isSuccess())
                && createFailureTaskCodes.isEmpty();
    }

    /**
     * Check if there exist a task which state is failure.
     */
    public boolean isExistFailureTaskExecutionRunnable() {
        return !createFailureTaskCodes.isEmpty() ||
                taskExecuteRunnableMap.values()
                        .stream()
                        .anyMatch(taskExecuteRunnable -> taskExecuteRunnable.getTaskInstance()
                                .getState() == TaskExecutionStatus.FAILURE);
    }

    /**
     * Check if there exist a task which state is kill.
     */
    public boolean isExistKillTaskExecutionRunnable() {
        return existTaskExecutionRunnableWithState(TaskExecutionStatus.KILL);
    }

    /**
     * Check if there exist a task which state is pause.
     */
    public boolean isAllTaskExecutionRunnableSuccessOrPause() {
        return allTaskExecutionRunnableWithState(
                Sets.newHashSet(
                        TaskExecutionStatus.PAUSE, TaskExecutionStatus.SUCCESS, TaskExecutionStatus.FORCED_SUCCESS));
    }

    public void putTaskExecutionRunnable(ITaskExecutionRunnable ITaskExecutionRunnable) {
        taskExecuteRunnableMap.put(ITaskExecutionRunnable.getTaskInstance().getId(), ITaskExecutionRunnable);
    }

    public boolean containsTaskExecutionRunnableByCode(Long taskCode) {
        return taskExecuteRunnableMap.values()
                .stream()
                .anyMatch(taskExecuteRunnable -> taskExecuteRunnable.getTaskInstance().getTaskCode() == taskCode);
    }

    public ITaskExecutionRunnable getTaskExecutionRunnableByTaskCode(Long taskCode) {
        List<ITaskExecutionRunnable> ITaskExecutionRunnableList = taskExecuteRunnableMap.values()
                .stream()
                .filter(taskExecuteRunnable -> taskExecuteRunnable.getTaskInstance().getTaskCode() == taskCode)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ITaskExecutionRunnableList)) {
            return null;
        }
        if (ITaskExecutionRunnableList.size() > 1) {
            throw new IllegalArgumentException(
                    "There are more than one TaskExecuteRunnable with the same task code: " + taskCode);
        }
        return ITaskExecutionRunnableList.get(0);
    }

    public void markTaskExecutionRunnableFinished(ITaskExecutionRunnable ITaskExecutionRunnable) {
        Integer taskInstanceId = ITaskExecutionRunnable.getTaskInstance().getId();
        markTaskExecutionRunnableFinished(taskInstanceId);
    }

    public void markTaskExecutionRunnableCreateFailure(Long taskCode) {
        createFailureTaskCodes.add(taskCode);
    }

    public void markTaskExecutionRunnableFinished(Integer taskInstanceId) {
        if (!taskExecuteRunnableMap.containsKey(taskInstanceId)) {
            throw new IllegalArgumentException(
                    "TaskExecuteRunnable: " + taskInstanceId + " is not in the TaskExecutionRunnableRepository");
        }
        if (activeTaskInstanceIds.contains(taskInstanceId)) {
            activeTaskInstanceIds.remove(taskInstanceId);
        }
        finishedTaskInstanceIds.add(taskInstanceId);
    }

    public void markTaskExecutionRunnableActive(ITaskExecutionRunnable ITaskExecutionRunnable) {
        Integer taskInstanceId = ITaskExecutionRunnable.getTaskInstance().getId();
        if (!taskExecuteRunnableMap.containsKey(taskInstanceId)) {
            throw new IllegalArgumentException(
                    "TaskExecuteRunnable: " + taskInstanceId + " is not in the TaskExecutionRunnableRepository");
        }
        markTaskExecutionRunnableActive(ITaskExecutionRunnable.getTaskInstance().getId());
    }

    public void markTaskExecutionRunnableActive(Integer taskInstanceId) {
        if (!taskExecuteRunnableMap.containsKey(taskInstanceId)) {
            throw new IllegalArgumentException(
                    "TaskExecuteRunnable: " + taskInstanceId + " is not in the TaskExecutionRunnableRepository");
        }
        if (activeTaskInstanceIds.contains(taskInstanceId)) {
            throw new IllegalArgumentException(
                    "TaskExecuteRunnable: " + taskInstanceId + " is already in active list");
        }
        activeTaskInstanceIds.add(taskInstanceId);
    }

    public void removeTaskExecutionRunnable(ITaskExecutionRunnable ITaskExecutionRunnable) {
        Integer taskInstanceId = ITaskExecutionRunnable.getTaskInstance().getId();
        taskExecuteRunnableMap.remove(taskInstanceId);
        activeTaskInstanceIds.remove(taskInstanceId);
        finishedTaskInstanceIds.remove(taskInstanceId);
    }

    public void clear() {
        taskExecuteRunnableMap.clear();
    }

    private boolean existTaskExecutionRunnableWithState(TaskExecutionStatus taskExecutionStatus) {
        return taskExecuteRunnableMap.values()
                .stream()
                .anyMatch(
                        taskExecuteRunnable -> taskExecuteRunnable.getTaskInstance().getState() == taskExecutionStatus);
    }

    private boolean allTaskExecutionRunnableWithState(Set<TaskExecutionStatus> taskExecutionStates) {
        return taskExecuteRunnableMap.values()
                .stream()
                .allMatch(
                        taskExecuteRunnable -> taskExecutionStates
                                .contains(taskExecuteRunnable.getTaskInstance().getState()));
    }
}
