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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.VarPoolUtils;
import org.apache.dolphinscheduler.server.master.engine.AbstractLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.ITaskGroupCoordinator;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskDispatchLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskDispatchedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskFailedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskFatalLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskKilledLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskPausedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRetryLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRunningLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRuntimeContextChangedEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskSuccessLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

@Slf4j
public abstract class AbstractTaskStateAction implements ITaskStateAction {

    @Autowired
    protected ITaskGroupCoordinator taskGroupCoordinator;

    @Autowired
    protected TaskInstanceDao taskInstanceDao;

    @Autowired
    protected IWorkflowRepository workflowRepository;

    @Autowired
    protected ITaskExecutorClient taskExecutorClient;

    /**
     * Whether the task needs to acquire the task group slot.
     */
    protected boolean isTaskNeedAcquireTaskGroupSlot(final ITaskExecution taskExecution) {
        final TaskInstance taskInstance = taskExecution.getTaskInstance();
        return taskGroupCoordinator.needAcquireTaskGroupSlot(taskInstance);
    }

    /**
     * Acquire the resources needed by the task instance.
     * <p> If the task instance is using a task group, the task group slot will be acquired.
     */
    protected void acquireTaskGroupSlot(final ITaskExecution taskExecution) {
        final TaskInstance taskInstance = taskExecution.getTaskInstance();
        final TaskDefinition taskDefinition = taskExecution.getTaskDefinition();
        taskGroupCoordinator.acquireTaskGroupSlot(taskInstance, taskDefinition);
    }

    /**
     * Release the resources needed by the task instance.
     */
    protected void releaseTaskInstanceResourcesIfNeeded(final ITaskExecution taskExecution) {
        final TaskInstance taskInstance = taskExecution.getTaskInstance();
        if (taskGroupCoordinator.needToReleaseTaskGroupSlot(taskInstance)) {
            taskGroupCoordinator.releaseTaskGroupSlot(taskInstance);
        }
    }

    @Override
    public void onFatalEvent(final IWorkflowExecution workflowExecution,
                             final ITaskExecution taskExecution,
                             final TaskFatalLifecycleEvent taskFatalEvent) {
        releaseTaskInstanceResourcesIfNeeded(taskExecution);
        persistentTaskInstanceFatalEventToDB(taskExecution, taskFatalEvent);

        if (taskExecution.isTaskInstanceCanRetry()) {
            taskExecution.getWorkflowEventBus().publish(TaskRetryLifecycleEvent.of(taskExecution));
            return;
        }

        // If all successors are condition tasks, then the task will not be marked as failure.
        // And the DAG will continue to execute.
        final IWorkflowExecutionGraph workflowExecutionGraph = taskExecution.getWorkflowExecutionGraph();
        if (workflowExecutionGraph.isAllSuccessorsAreConditionTask(taskExecution)) {
            mergeTaskVarPoolToWorkflow(workflowExecution, taskExecution);
            publishWorkflowInstanceTopologyLogicalTransitionEvent(workflowExecution, taskExecution);
            return;
        }

        taskExecution.getWorkflowExecutionGraph().markTaskExecutionChainFailure(taskExecution);
        publishWorkflowInstanceTopologyLogicalTransitionEvent(workflowExecution, taskExecution);
    }

    private void persistentTaskInstanceFatalEventToDB(final ITaskExecution taskExecution,
                                                      final TaskFatalLifecycleEvent taskFatalEvent) {
        final TaskInstance taskInstance = taskExecution.getTaskInstance();
        taskInstance.setState(TaskExecutionStatus.FAILURE);
        taskInstance.setEndTime(taskFatalEvent.getEndTime());
        taskInstanceDao.updateById(taskInstance);
    }

    @Override
    public void onDispatchedEvent(final IWorkflowExecution workflowExecution,
                                  final ITaskExecution taskExecution,
                                  final TaskDispatchedLifecycleEvent taskDispatchedEvent) {
        final TaskInstance taskInstance = taskExecution.getTaskInstance();
        taskInstance.setState(DISPATCH);
        taskInstance.setHost(taskDispatchedEvent.getExecutorHost());
        taskInstanceDao.updateById(taskInstance);
    }

    @Override
    public void onRuntimeContextChangedEvent(final IWorkflowExecution workflowExecution,
                                             final ITaskExecution taskExecution,
                                             final TaskRuntimeContextChangedEvent taskRuntimeContextChangedEvent) {
        final TaskInstance taskInstance = taskExecution.getTaskInstance();
        if (StringUtils.isNotEmpty(taskRuntimeContextChangedEvent.getRuntimeContext())) {
            taskInstance.setAppLink(taskRuntimeContextChangedEvent.getRuntimeContext());
        }
        taskInstanceDao.updateById(taskInstance);
    }

    protected void persistentTaskInstanceStartedEventToDB(final ITaskExecution taskExecution,
                                                          final TaskRunningLifecycleEvent taskRunningEvent) {
        final TaskInstance taskInstance = taskExecution.getTaskInstance();
        taskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(taskRunningEvent.getStartTime());
        taskInstance.setLogPath(taskRunningEvent.getLogPath());
        taskInstanceDao.updateById(taskInstance);
    }

    @Override
    public void onPausedEvent(final IWorkflowExecution workflowExecution,
                              final ITaskExecution taskExecution,
                              final TaskPausedLifecycleEvent taskPausedEvent) {
        releaseTaskInstanceResourcesIfNeeded(taskExecution);
        persistentTaskInstancePausedEventToDB(taskExecution, taskPausedEvent);
        taskExecution.getWorkflowExecutionGraph().markTaskExecutionChainPause(taskExecution);
        publishWorkflowInstanceTopologyLogicalTransitionEvent(workflowExecution, taskExecution);
    }

    private void persistentTaskInstancePausedEventToDB(final ITaskExecution taskExecution,
                                                       final TaskPausedLifecycleEvent taskPausedEvent) {
        final TaskInstance taskInstance = taskExecution.getTaskInstance();
        taskInstance.setState(TaskExecutionStatus.PAUSE);
        taskInstanceDao.updateById(taskInstance);
    }

    @Override
    public void onKilledEvent(final IWorkflowExecution workflowExecution,
                              final ITaskExecution taskExecution,
                              final TaskKilledLifecycleEvent taskInstanceKillEvent) {
        releaseTaskInstanceResourcesIfNeeded(taskExecution);
        persistentTaskInstanceKilledEventToDB(taskExecution, taskInstanceKillEvent);
        taskExecution.getWorkflowExecutionGraph().markTaskExecutionChainKill(taskExecution);
        publishWorkflowInstanceTopologyLogicalTransitionEvent(workflowExecution, taskExecution);
    }

    private void persistentTaskInstanceKilledEventToDB(final ITaskExecution taskExecution,
                                                       final TaskKilledLifecycleEvent taskKilledEvent) {
        final TaskInstance taskInstance = taskExecution.getTaskInstance();
        taskInstance.setState(TaskExecutionStatus.KILL);
        taskInstance.setEndTime(taskKilledEvent.getEndTime());
        taskInstanceDao.updateById(taskInstance);

    }

    @Override
    public void onFailedEvent(final IWorkflowExecution workflowExecution,
                              final ITaskExecution taskExecution,
                              final TaskFailedLifecycleEvent taskFailedEvent) {
        releaseTaskInstanceResourcesIfNeeded(taskExecution);
        persistentTaskInstanceFailedEventToDB(taskExecution, taskFailedEvent);

        if (taskExecution.isTaskInstanceCanRetry()) {
            taskExecution.getWorkflowEventBus().publish(TaskRetryLifecycleEvent.of(taskExecution));
            return;
        }
        // If all successors are condition tasks, then the task will not be marked as failure.
        // And the DAG will continue to execute.
        final IWorkflowExecutionGraph workflowExecutionGraph = taskExecution.getWorkflowExecutionGraph();
        if (workflowExecutionGraph.isAllSuccessorsAreConditionTask(taskExecution)) {
            mergeTaskVarPoolToWorkflow(workflowExecution, taskExecution);
            publishWorkflowInstanceTopologyLogicalTransitionEvent(workflowExecution, taskExecution);
            return;
        }
        // todo: Use Plugin to extend the act strategy on xxEvent.
        taskExecution.getWorkflowExecutionGraph().markTaskExecutionChainFailure(taskExecution);
        publishWorkflowInstanceTopologyLogicalTransitionEvent(workflowExecution, taskExecution);
    }

    private void persistentTaskInstanceFailedEventToDB(final ITaskExecution taskExecution,
                                                       final TaskFailedLifecycleEvent taskFailedEvent) {
        final TaskInstance taskInstance = taskExecution.getTaskInstance();
        taskInstance.setState(TaskExecutionStatus.FAILURE);
        taskInstance.setEndTime(taskFailedEvent.getEndTime());
        taskInstanceDao.updateById(taskInstance);
    }

    @Override
    public void onSucceedEvent(final IWorkflowExecution workflowExecution,
                               final ITaskExecution taskExecution,
                               final TaskSuccessLifecycleEvent taskSuccessEvent) {
        releaseTaskInstanceResourcesIfNeeded(taskExecution);
        persistentTaskInstanceSuccessEventToDB(taskExecution, taskSuccessEvent);
        mergeTaskVarPoolToWorkflow(workflowExecution, taskExecution);
        publishWorkflowInstanceTopologyLogicalTransitionEvent(workflowExecution, taskExecution);
    }

    protected void mergeTaskVarPoolToWorkflow(final IWorkflowExecution workflowExecution,
                                              final ITaskExecution taskExecution) {
        final TaskInstance taskInstance = taskExecution.getTaskInstance();
        final WorkflowInstance workflowInstance = workflowExecution.getWorkflowInstance();
        final List<Property> finalVarPool = VarPoolUtils.mergeVarPoolJsonString(
                Lists.newArrayList(workflowInstance.getVarPool(), taskInstance.getVarPool()));
        workflowInstance.setVarPool(VarPoolUtils.serializeVarPool(finalVarPool));
    }

    protected void persistentTaskInstanceSuccessEventToDB(final ITaskExecution taskExecution,
                                                          final TaskSuccessLifecycleEvent taskSuccessEvent) {
        final TaskInstance taskInstance = taskExecution.getTaskInstance();
        taskInstance.setState(TaskExecutionStatus.SUCCESS);
        taskInstance.setEndTime(taskSuccessEvent.getEndTime());
        final List<Property> finalVarPool = VarPoolUtils.mergeVarPoolJsonString(taskInstance.getVarPool(),
                JSONUtils.toJsonString(taskSuccessEvent.getVarPool()));
        taskInstance.setVarPool(VarPoolUtils.serializeVarPool(finalVarPool));
        taskInstanceDao.updateById(taskInstance);
    }

    /**
     * Failover task.
     * <p> Will try to take over the task from remote executor, if take-over success, the task has no effect.
     * <p> If the take-over fails, will generate a failover task-instance and mark the task instance status to {@link TaskExecutionStatus#NEED_FAULT_TOLERANCE}.
     */
    protected void failoverTask(final ITaskExecution taskExecution) {
        taskExecution.failover();
    }

    protected void tryToDispatchTask(final ITaskExecution taskExecution) {
        if (isTaskNeedAcquireTaskGroupSlot(taskExecution)) {
            acquireTaskGroupSlot(taskExecution);
            log.info("Task[name={}] using taskGroup, success acquire taskGroup slot", taskExecution.getName());
            return;
        }
        taskExecution.getWorkflowEventBus().publish(TaskDispatchLifecycleEvent.of(taskExecution));
    }

    protected void publishWorkflowInstanceTopologyLogicalTransitionEvent(
                                                                         final IWorkflowExecution workflowExecution,
                                                                         final ITaskExecution taskExecution) {
        taskExecution
                .getWorkflowEventBus()
                .publish(
                        WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent.of(
                                workflowExecution,
                                taskExecution));
    }

    protected void throwExceptionIfStateIsNotMatch(final ITaskExecution taskExecution) {
        checkNotNull(taskExecution, "taskExecution is null");
        final TaskInstance taskInstance = checkNotNull(taskExecution.getTaskInstance(), "taskInstance is null");
        final TaskExecutionStatus actualState = taskInstance.getState();
        final TaskExecutionStatus expectState = matchState();
        if (actualState != expectState) {
            final String taskName = taskInstance.getName();
            throw new IllegalStateException(
                    "The task: " + taskName + " state: " + actualState + " is not match:" + expectState);
        }
    }

    protected void logWarningIfCannotDoAction(final ITaskExecution taskExecution,
                                              final AbstractLifecycleEvent event) {
        final TaskInstance taskInstance = taskExecution.getTaskInstance();
        log.warn("Task[name={}] state is {} cannot do action on event: {}",
                taskInstance.getName(),
                taskInstance.getState(),
                event);
    }
}
