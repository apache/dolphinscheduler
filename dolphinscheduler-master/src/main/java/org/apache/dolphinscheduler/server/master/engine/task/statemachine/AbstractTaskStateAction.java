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

package org.apache.dolphinscheduler.server.master.engine.task.statemachine;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus.DISPATCH;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.VarPoolUtils;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.engine.AbstractLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.TaskGroupCoordinator;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskDispatchLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskDispatchedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskFailedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskKilledLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskPausedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRetryLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRunningLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRuntimeContextChangedEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskSuccessLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskInstanceFactories;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

@Slf4j
public abstract class AbstractTaskStateAction implements ITaskStateAction {

    @Autowired
    protected TaskGroupCoordinator taskGroupCoordinator;

    @Autowired
    protected TaskInstanceDao taskInstanceDao;

    @Autowired
    protected TaskInstanceFactories taskInstanceFactories;

    @Autowired
    protected IWorkflowRepository workflowRepository;

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    protected ITaskExecutorClient taskExecutorClient;

    /**
     * Whether the task needs to acquire the task group slot.
     */
    protected boolean isTaskNeedAcquireTaskGroupSlot(final ITaskExecutionRunnable taskExecutionRunnable) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        return taskGroupCoordinator.needAcquireTaskGroupSlot(taskInstance);
    }

    /**
     * Acquire the resources needed by the task instance.
     * <p> If the task instance is using a task group, the task group slot will be acquired.
     */
    protected void acquireTaskGroupSlot(final ITaskExecutionRunnable taskExecutionRunnable) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        taskGroupCoordinator.acquireTaskGroupSlot(taskInstance);
    }

    /**
     * Release the resources needed by the task instance.
     */
    protected void releaseTaskInstanceResourcesIfNeeded(final ITaskExecutionRunnable taskExecutionRunnable) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        if (taskGroupCoordinator.needToReleaseTaskGroupSlot(taskInstance)) {
            taskGroupCoordinator.releaseTaskGroupSlot(taskInstance);
        }
    }

    @Override
    public void dispatchedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                      final ITaskExecutionRunnable taskExecutionRunnable,
                                      final TaskDispatchedLifecycleEvent taskDispatchedEvent) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        taskInstance.setState(DISPATCH);
        taskInstance.setHost(taskDispatchedEvent.getExecutorHost());
        taskInstanceDao.updateById(taskInstance);
    }

    @Override
    public void runtimeContextChangedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                                 final ITaskExecutionRunnable taskExecutionRunnable,
                                                 final TaskRuntimeContextChangedEvent taskRuntimeContextChangedEvent) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        if (StringUtils.isNotEmpty(taskRuntimeContextChangedEvent.getRuntimeContext())) {
            taskInstance.setAppLink(taskRuntimeContextChangedEvent.getRuntimeContext());
        }
        taskInstanceDao.updateById(taskInstance);
    }

    protected void persistentTaskInstanceStartedEventToDB(final ITaskExecutionRunnable taskExecutionRunnable,
                                                          final TaskRunningLifecycleEvent taskRunningEvent) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        taskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(taskRunningEvent.getStartTime());
        taskInstance.setLogPath(taskRunningEvent.getLogPath());
        taskInstanceDao.updateById(taskInstance);
    }

    @Override
    public void pausedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                  final ITaskExecutionRunnable taskExecutionRunnable,
                                  final TaskPausedLifecycleEvent taskPausedEvent) {
        releaseTaskInstanceResourcesIfNeeded(taskExecutionRunnable);
        persistentTaskInstancePausedEventToDB(taskExecutionRunnable, taskPausedEvent);
        taskExecutionRunnable.getWorkflowExecutionGraph().markTaskExecutionRunnableChainPause(taskExecutionRunnable);
        publishWorkflowInstanceTopologyLogicalTransitionEvent(taskExecutionRunnable);
    }

    private void persistentTaskInstancePausedEventToDB(final ITaskExecutionRunnable taskExecutionRunnable,
                                                       final TaskPausedLifecycleEvent taskPausedEvent) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        taskInstance.setState(TaskExecutionStatus.PAUSE);
        taskInstanceDao.updateById(taskInstance);
    }

    @Override
    public void killedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                  final ITaskExecutionRunnable taskExecutionRunnable,
                                  final TaskKilledLifecycleEvent taskInstanceKillEvent) {
        releaseTaskInstanceResourcesIfNeeded(taskExecutionRunnable);
        persistentTaskInstanceKilledEventToDB(taskExecutionRunnable, taskInstanceKillEvent);
        taskExecutionRunnable.getWorkflowExecutionGraph().markTaskExecutionRunnableChainKill(taskExecutionRunnable);
        publishWorkflowInstanceTopologyLogicalTransitionEvent(taskExecutionRunnable);
    }

    private void persistentTaskInstanceKilledEventToDB(final ITaskExecutionRunnable taskExecutionRunnable,
                                                       final TaskKilledLifecycleEvent taskKilledEvent) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        taskInstance.setState(TaskExecutionStatus.KILL);
        taskInstance.setEndTime(taskKilledEvent.getEndTime());
        taskInstanceDao.updateById(taskInstance);

    }

    @Override
    public void failedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                  final ITaskExecutionRunnable taskExecutionRunnable,
                                  final TaskFailedLifecycleEvent taskFailedEvent) {
        releaseTaskInstanceResourcesIfNeeded(taskExecutionRunnable);
        persistentTaskInstanceFailedEventToDB(taskExecutionRunnable, taskFailedEvent);

        if (taskExecutionRunnable.isTaskInstanceCanRetry()) {
            taskExecutionRunnable.getWorkflowEventBus().publish(TaskRetryLifecycleEvent.of(taskExecutionRunnable));
            return;
        }
        // If all successors are condition tasks, then the task will not be marked as failure.
        // And the DAG will continue to execute.
        final IWorkflowExecutionGraph workflowExecutionGraph = taskExecutionRunnable.getWorkflowExecutionGraph();
        if (workflowExecutionGraph.isAllSuccessorsAreConditionTask(taskExecutionRunnable)) {
            publishWorkflowInstanceTopologyLogicalTransitionEvent(taskExecutionRunnable);
            return;
        }
        taskExecutionRunnable.getWorkflowExecutionGraph().markTaskExecutionRunnableChainFailure(taskExecutionRunnable);
        publishWorkflowInstanceTopologyLogicalTransitionEvent(taskExecutionRunnable);
    }

    private void persistentTaskInstanceFailedEventToDB(final ITaskExecutionRunnable taskExecutionRunnable,
                                                       final TaskFailedLifecycleEvent taskFailedEvent) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        taskInstance.setState(TaskExecutionStatus.FAILURE);
        taskInstance.setEndTime(taskFailedEvent.getEndTime());
        taskInstanceDao.updateById(taskInstance);
    }

    @Override
    public void succeedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                   final ITaskExecutionRunnable taskExecutionRunnable,
                                   final TaskSuccessLifecycleEvent taskSuccessEvent) {
        releaseTaskInstanceResourcesIfNeeded(taskExecutionRunnable);
        persistentTaskInstanceSuccessEventToDB(taskExecutionRunnable, taskSuccessEvent);
        mergeTaskVarPoolToWorkflow(workflowExecutionRunnable, taskExecutionRunnable);
        publishWorkflowInstanceTopologyLogicalTransitionEvent(taskExecutionRunnable);
    }

    protected void mergeTaskVarPoolToWorkflow(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                              final ITaskExecutionRunnable taskExecutionRunnable) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        final WorkflowInstance workflowInstance = workflowExecutionRunnable.getWorkflowInstance();
        final String finalVarPool = VarPoolUtils.mergeVarPoolJsonString(
                Lists.newArrayList(workflowInstance.getVarPool(), taskInstance.getVarPool()));
        workflowInstance.setVarPool(finalVarPool);
    }

    protected void persistentTaskInstanceSuccessEventToDB(final ITaskExecutionRunnable taskExecutionRunnable,
                                                          final TaskSuccessLifecycleEvent taskSuccessEvent) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        taskInstance.setState(TaskExecutionStatus.SUCCESS);
        taskInstance.setEndTime(taskSuccessEvent.getEndTime());
        taskInstance.setVarPool(taskSuccessEvent.getVarPool());
        taskInstanceDao.updateById(taskInstance);
    }

    /**
     * Failover task.
     * <p> Will try to take over the task from remote executor, if take-over success, the task has no effect.
     * <p> If the take-over fails, will generate a failover task-instance and mark the task instance status to {@link TaskExecutionStatus#NEED_FAULT_TOLERANCE}.
     */
    protected void failoverTask(final ITaskExecutionRunnable taskExecutionRunnable) {
        taskExecutionRunnable.failover();
    }

    protected void tryToDispatchTask(final ITaskExecutionRunnable taskExecutionRunnable) {
        if (isTaskNeedAcquireTaskGroupSlot(taskExecutionRunnable)) {
            acquireTaskGroupSlot(taskExecutionRunnable);
            return;
        }
        taskExecutionRunnable.getWorkflowEventBus().publish(TaskDispatchLifecycleEvent.of(taskExecutionRunnable));
    }

    protected void publishWorkflowInstanceTopologyLogicalTransitionEvent(final ITaskExecutionRunnable taskExecutionRunnable) {
        final Integer workflowInstanceId = taskExecutionRunnable.getWorkflowInstance().getId();
        final IWorkflowExecutionRunnable workflowExecutionRunnable = workflowRepository.get(workflowInstanceId);
        taskExecutionRunnable.getWorkflowExecutionGraph().markTaskExecutionRunnableInActive(taskExecutionRunnable);
        taskExecutionRunnable
                .getWorkflowEventBus()
                .publish(
                        WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent.of(
                                workflowExecutionRunnable,
                                taskExecutionRunnable));
    }

    protected void throwExceptionIfStateIsNotMatch(final ITaskExecutionRunnable taskExecutionRunnable) {
        checkNotNull(taskExecutionRunnable, "taskExecutionRunnable is null");
        final TaskInstance taskInstance = checkNotNull(taskExecutionRunnable.getTaskInstance(), "taskInstance is null");
        final TaskExecutionStatus actualState = taskInstance.getState();
        final TaskExecutionStatus expectState = matchState();
        if (actualState != expectState) {
            final String taskName = taskInstance.getName();
            throw new IllegalStateException(
                    "The task: " + taskName + " state: " + actualState + " is not match:" + expectState);
        }
    }

    protected void logWarningIfCannotDoAction(final ITaskExecutionRunnable taskExecutionRunnable,
                                              final AbstractLifecycleEvent event) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        log.warn("Task {} state is {} cannot do action on event: {}",
                taskInstance.getName(),
                taskInstance.getState(),
                event);
    }
}
