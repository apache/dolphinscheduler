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

import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;

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
    void addNode(final ITaskExecutionRunnable taskExecutionRunnable);

    /**
     * Add a new edge to the graph.
     * <p> Right now, this method call after all the tasks are added to the graph.
     */
    void addEdge(final String fromTaskName, final Set<String> toTaskName);

    /**
     * Return the start tasks, the start tasks in the workflow execution graph is the tasks which predecessors is empty.
     */
    List<ITaskExecutionRunnable> getStartNodes();

    /**
     * Get the predecessor tasks of the given task.
     */
    List<ITaskExecutionRunnable> getPredecessors(final String taskName);

    /**
     * Return the successor tasks of the given task.
     */
    List<ITaskExecutionRunnable> getSuccessors(final String taskName);

    /**
     * Return the successor tasks of the given task.
     */
    List<ITaskExecutionRunnable> getSuccessors(final ITaskExecutionRunnable taskExecutionRunnable);

    /**
     * Get the ITaskExecutionRunnable by task code.
     */
    ITaskExecutionRunnable getTaskExecutionRunnableByName(final String taskName);

    /**
     * Get the ITaskExecutionRunnable by task instance id.
     */
    ITaskExecutionRunnable getTaskExecutionRunnableById(final Integer taskInstanceId);

    /**
     * Get the ITaskExecutionRunnable by task code.
     */
    ITaskExecutionRunnable getTaskExecutionRunnableByTaskCode(final Long taskCode);

    /**
     * Whether the given task is active.
     */
    boolean isTaskExecutionRunnableActive(final ITaskExecutionRunnable taskExecutionRunnable);

    /**
     * Whether the given task is killed.
     */
    boolean isTaskExecutionRunnableKilled(final ITaskExecutionRunnable taskExecutionRunnable);

    /**
     * Get the active TaskExecutionRunnable list.
     * <p> The active TaskExecutionRunnable means the task is handling in the workflow execution graph.
     */
    List<ITaskExecutionRunnable> getActiveTaskExecutionRunnable();

    /**
     * Get all the TaskExecutionRunnable in the graph, this method will return all the TaskExecutionRunnable in the graph,
     * include active and inactive TaskExecutionRunnable.
     */
    List<ITaskExecutionRunnable> getAllTaskExecutionRunnable();

    /**
     * Check whether the given task can be trigger now.
     * <p> The task can be trigger if all the predecessors are finished and all predecessors are not failure/pause/kill.
     */
    boolean isTriggerConditionMet(final ITaskExecutionRunnable taskExecutionRunnable);

    /**
     * Mark the TaskExecutionRunnable is active.
     * <p> If the TaskExecutionRunnable is active means the task is handling by the workflow.
     * <p> Once we begin to handle a task, we should mark the TaskExecutionRunnable active.
     */
    void markTaskExecutionRunnableActive(final ITaskExecutionRunnable taskExecutionRunnable);

    /**
     * Mark the TaskExecutionRunnable is inactive.
     * <p> If the TaskExecutionRunnable is inactive means the task has not been handled by the workflow.
     * <p> Once we finish to handle a task, we should mark the TaskExecutionRunnable inactive.
     */
    void markTaskExecutionRunnableInActive(final ITaskExecutionRunnable taskExecutionRunnable);

    /**
     * Mark the TaskExecutionRunnable is skipped.
     * <p> Once the TaskExecutionRunnable is marked as skipped, this means the task will not be trigger.
     */
    void markTaskSkipped(final ITaskExecutionRunnable taskExecutionRunnable);

    /**
     * Mark the Task is skipped.
     * <p> Once the Task is marked as skipped, this means the task will not be trigger.
     */
    void markTaskSkipped(final String taskName);

    /**
     * Mark the TaskExecutionRunnable chain is failure.
     * <p> Once the TaskExecutionRunnable chain is failure, then the successors will not be trigger, and the workflow execution graph might be failure.
     */
    void markTaskExecutionRunnableChainFailure(final ITaskExecutionRunnable taskExecutionRunnable);

    /**
     * Mark the TaskExecutionRunnable chain is pause.
     * <p> Once the TaskExecutionRunnable chain is pause, then the successors will not be trigger, and the workflow execution graph might be paused.
     */
    void markTaskExecutionRunnableChainPause(final ITaskExecutionRunnable taskExecutionRunnable);

    /**
     * Mark the TaskExecutionRunnable chain is kill.
     * <p> Once the TaskExecutionRunnable chain is kill, then the successors will not be trigger, and the workflow execution graph might be stop.
     */
    void markTaskExecutionRunnableChainKill(final ITaskExecutionRunnable taskExecutionRunnable);

    /**
     * Whether all the TaskExecutionRunnable chain in the graph is finish.
     */
    boolean isAllTaskExecutionRunnableChainFinish();

    /**
     * Whether all the TaskExecutionRunnable chain in the graph is finish with success.
     */
    boolean isAllTaskExecutionRunnableChainSuccess();

    /**
     * Whether there exist the TaskExecutionRunnable chain in the graph is finish with failure.
     */
    boolean isExistFailureTaskExecutionRunnableChain();

    /**
     * Whether there exist the TaskExecutionRunnable chain in the graph is finish with paused.
     */
    boolean isExistPauseTaskExecutionRunnableChain();

    /**
     * Whether there exist the TaskExecutionRunnable chain in the graph is finish with kill.
     */
    boolean isExistKillTaskExecutionRunnableChain();

    /**
     * Check whether the given task is the end of the task chain.
     * <p> If the given task has no successor, then it is the end of the task chain.
     * <p> If the given task is killed or paused, then it is the end of the task chain.
     */
    boolean isEndOfTaskChain(final ITaskExecutionRunnable taskExecutionRunnable);

    /**
     * Whether the given task is skipped.
     * <p> Once we mark the task is skipped, then the task will not be trigger, and will trigger its successors.
     */
    boolean isTaskExecutionRunnableSkipped(final ITaskExecutionRunnable taskExecutionRunnable);

    /**
     * Whether the given task is forbidden.
     * <p> Once the task is forbidden then it will be passed, and will trigger its successors.
     */
    boolean isTaskExecutionRunnableForbidden(final ITaskExecutionRunnable taskExecutionRunnable);

    /**
     * Whether all predecessors task is skipped.
     * <p> Once all predecessors are marked as skipped, then the task will be marked as skipped, and will trigger its successors.
     */
    boolean isAllPredecessorsSkipped(final ITaskExecutionRunnable taskExecutionRunnable);

    /**
     * Whether all predecessors task are condition task.
     */
    boolean isAllSuccessorsAreConditionTask(final ITaskExecutionRunnable taskExecutionRunnable);
}
