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

import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;

import java.util.List;
import java.util.Set;

/**
 * The workflow execution graph represent the real DAG in runtime, it might be a sub DAG of the workflow DAG.
 *
 * @see WorkflowExecutionGraph
 */
public interface IWorkflowExecutionGraph {

    /**
     * Add a new task to the graph.
     */
    void addNode(final ITaskExecution taskExecution);

    /**
     * Add a new edge to the graph.
     * <p> Right now, this method call after all the tasks are added to the graph.
     */
    void addEdge(final String fromTaskName, final Set<String> toTaskName);

    /**
     * Remove the unreachable edge in the graph.
     */
    void removeUnReachableEdge();

    /**
     * Return the start tasks, the start tasks in the workflow execution graph is the tasks which predecessors is empty.
     */
    List<ITaskExecution> getStartNodes();

    /**
     * Get the predecessor tasks of the given task.
     */
    List<ITaskExecution> getPredecessors(final String taskName);

    /**
     * Return the successor tasks of the given task.
     */
    List<ITaskExecution> getSuccessors(final String taskName);

    /**
     * Return the successor tasks of the given task.
     */
    List<ITaskExecution> getSuccessors(final ITaskExecution taskExecution);

    /**
     * Get the ITaskExecution by task code.
     */
    ITaskExecution getTaskExecutionByName(final String taskName);

    /**
     * Get the ITaskExecution by task instance id.
     */
    ITaskExecution getTaskExecutionById(final Integer taskInstanceId);

    /**
     * Get the ITaskExecution by task code.
     */
    ITaskExecution getTaskExecutionByTaskCode(final Long taskCode);

    /**
     * Whether the given task is active.
     */
    boolean isTaskExecutionActive(final ITaskExecution taskExecution);

    /**
     * Whether the given task is inactive.
     * <p> A task is inactive means the task has been `executed`.
     */
    boolean isTaskExecutionInActive(final ITaskExecution taskExecution);

    /**
     * Whether the given task is killed.
     */
    boolean isTaskExecutionKilled(final ITaskExecution taskExecution);

    /**
     * Whether the given task is failure.
     */
    boolean isTaskExecutionFailed(final ITaskExecution taskExecution);

    /**
     * Whether the given task is paused.
     */
    boolean isTaskExecutionPaused(final ITaskExecution taskExecution);

    /**
     * Get the active TaskExecution list.
     * <p> The active TaskExecution means the task is handling in the workflow execution graph.
     */
    List<ITaskExecution> getActiveTaskExecution();

    /**
     * Get all the TaskExecution in the graph, this method will return all the TaskExecution in the graph,
     * include active and inactive TaskExecution.
     */
    List<ITaskExecution> getAllTaskExecution();

    /**
     * Check whether the given task can be trigger now.
     * <p> The task can be trigger only all the predecessors are finished and all predecessors are not failure/pause/kill.
     * <p> Once the task has been triggered, then will also return false.
     */
    boolean isTriggerConditionMet(final ITaskExecution taskExecution);

    /**
     * Mark the TaskExecution is active.
     * <p> If the TaskExecution is active means the task is handling by the workflow.
     * <p> Once we begin to handle a task, we should mark the TaskExecution active.
     */
    void markTaskExecutionActive(final ITaskExecution taskExecution);

    /**
     * Mark the TaskExecution is inactive.
     * <p> If the TaskExecution is inactive means the task has not been handled by the workflow.
     * <p> Once we finish to handle a task, we should mark the TaskExecution inactive.
     */
    void markTaskExecutionInActive(final ITaskExecution taskExecution);

    /**
     * Mark the TaskExecution is skipped.
     * <p> Once the TaskExecution is marked as skipped, this means the task will not be trigger.
     */
    void markTaskSkipped(final ITaskExecution taskExecution);

    /**
     * Mark the Task is skipped.
     * <p> Once the Task is marked as skipped, this means the task will not be trigger.
     */
    void markTaskSkipped(final String taskName);

    /**
     * Mark the TaskExecution chain is failure.
     * <p> Once the TaskExecution chain is failure, then the successors will not be trigger, and the workflow execution graph might be failure.
     */
    void markTaskExecutionChainFailure(final ITaskExecution taskExecution);

    /**
     * Mark the TaskExecution chain is pause.
     * <p> Once the TaskExecution chain is pause, then the successors will not be trigger, and the workflow execution graph might be paused.
     */
    void markTaskExecutionChainPause(final ITaskExecution taskExecution);

    /**
     * Mark the TaskExecution chain is kill.
     * <p> Once the TaskExecution chain is kill, then the successors will not be trigger, and the workflow execution graph might be stop.
     */
    void markTaskExecutionChainKill(final ITaskExecution taskExecution);

    /**
     * Whether all the TaskExecution chain in the graph is finish.
     */
    boolean isAllTaskExecutionChainFinish();

    /**
     * Whether all the TaskExecution chain in the graph is finish with success.
     */
    boolean isAllTaskExecutionChainSuccess();

    /**
     * Whether there exist the TaskExecution chain in the graph is finish with failure.
     */
    boolean isExistFailureTaskExecutionChain();

    /**
     * Whether there exist the TaskExecution chain in the graph is finish with paused.
     */
    boolean isExistPausedTaskExecutionChain();

    /**
     * Whether there exist the TaskExecution chain in the graph is finish with kill.
     */
    boolean isExistKilledTaskExecutionChain();

    /**
     * Check whether the given task is the end of the task chain.
     * <p> If the given task has no successor, then it is the end of the task chain.
     * <p> If the given task is killed or paused, then it is the end of the task chain.
     * <p> If the given task is failure, and all its successors are condition task then it is not end of a task chain.
     */
    boolean isEndOfTaskChain(final ITaskExecution taskExecution);

    /**
     * Whether the given task is skipped.
     * <p> Once we mark the task is skipped, then the task will not be trigger, and will trigger its successors.
     */
    boolean isTaskExecutionSkipped(final ITaskExecution taskExecution);

    /**
     * Whether the given task is forbidden.
     * <p> Once the task is forbidden then it will be passed, and will trigger its successors.
     */
    boolean isTaskExecutionForbidden(final ITaskExecution taskExecution);

    /**
     * Whether the given task's execution is failure and waiting for retry.
     */
    boolean isTaskExecutionRetrying(final ITaskExecution taskExecution);

    /**
     * Whether all predecessors task is skipped.
     * <p> Once all predecessors are marked as skipped, then the task will be marked as skipped, and will trigger its successors.
     */
    boolean isAllPredecessorsSkipped(final ITaskExecution taskExecution);

    /**
     * Whether all predecessors task are condition task.
     */
    boolean isAllSuccessorsAreConditionTask(final ITaskExecution taskExecution);
}
