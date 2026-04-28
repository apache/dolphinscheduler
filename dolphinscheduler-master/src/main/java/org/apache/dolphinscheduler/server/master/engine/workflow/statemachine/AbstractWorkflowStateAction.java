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

package org.apache.dolphinscheduler.server.master.engine.workflow.statemachine;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.engine.AbstractLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.WorkflowCacheRepository;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBusCoordinator;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.graph.SuccessorFlowAdjuster;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskStartLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowFinalizeLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.policy.IWorkflowFailureStrategy;
import org.apache.dolphinscheduler.server.master.metrics.WorkflowInstanceMetrics;
import org.apache.dolphinscheduler.server.master.utils.WorkflowInstanceUtils;
import org.apache.dolphinscheduler.service.alert.WorkflowAlertManager;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
public abstract class AbstractWorkflowStateAction implements IWorkflowStateAction {

    @Autowired
    protected SuccessorFlowAdjuster successorFlowAdjuster;

    @Autowired
    protected WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    protected WorkflowCacheRepository workflowCacheRepository;

    @Autowired
    protected WorkflowEventBusCoordinator workflowEventBusCoordinator;

    @Autowired
    protected WorkflowAlertManager workflowAlertManager;

    @Autowired
    protected TransactionTemplate transactionTemplate;

    @Autowired
    protected SerialCommandDao serialCommandDao;

    /**
     * Try to trigger the tasks if the trigger condition is met.
     * <p> If all the given tasks trigger condition is not met then will try to emit workflow finish event.
     */
    protected void triggerTasks(final IWorkflowExecution workflowExecution,
                                final List<ITaskExecution> triggerCandidateTasks) {
        final IWorkflowExecutionGraph workflowExecutionGraph = workflowExecution.getWorkflowExecutionGraph();
        final List<ITaskExecution> readyToTriggerTasks = triggerCandidateTasks
                .stream()
                .filter(workflowExecutionGraph::isTriggerConditionMet)
                .sorted(Comparator.comparing(ITaskExecution::getName))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(readyToTriggerTasks)) {
            final boolean isAllCandidateTaskPredecessorsInActive = triggerCandidateTasks.stream()
                    .flatMap(taskExecution -> workflowExecutionGraph
                            .getPredecessors(taskExecution.getName())
                            .stream())
                    .allMatch(workflowExecutionGraph::isTaskExecutionInActive);
            if (isAllCandidateTaskPredecessorsInActive) {
                emitWorkflowFinishedEventIfApplicable(workflowExecution);
            }
            return;
        }
        final WorkflowEventBus workflowEventBus = workflowExecution.getWorkflowEventBus();
        for (ITaskExecution readyToTriggerTask : readyToTriggerTasks) {
            workflowExecutionGraph.markTaskExecutionActive(readyToTriggerTask);
            if (workflowExecutionGraph.isTaskExecutionSkipped(readyToTriggerTask)
                    || workflowExecutionGraph.isTaskExecutionForbidden(readyToTriggerTask)) {
                workflowEventBus.publish(
                        WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent.of(
                                workflowExecution, readyToTriggerTask));
                continue;
            }
            workflowEventBus.publish(TaskStartLifecycleEvent.of(readyToTriggerTask));
        }
    }

    protected void pauseActiveTask(final IWorkflowExecution workflowExecution) {
        try {
            LogUtils.setWorkflowInstanceIdMDC(workflowExecution.getId());
            workflowExecution
                    .getWorkflowExecutionGraph()
                    .getActiveTaskExecution()
                    .forEach(ITaskExecution::pause);
        } finally {
            LogUtils.removeWorkflowInstanceIdMDC();
        }
    }

    protected void tryToTriggerSuccessorsAfterTaskFinish(final IWorkflowExecution workflowExecution,
                                                         final ITaskExecution taskExecution) {
        successorFlowAdjuster.adjustSuccessorFlow(taskExecution);

        final IWorkflowFailureStrategy workflowFailureStrategy = workflowExecution.getWorkflowFailureStrategy();
        if (taskExecution.isFailure()) {
            workflowFailureStrategy.onTaskFailure(workflowExecution, taskExecution);
        }

        final IWorkflowExecutionGraph workflowExecutionGraph = workflowExecution.getWorkflowExecutionGraph();
        if (workflowExecutionGraph.isEndOfTaskChain(taskExecution)) {
            emitWorkflowFinishedEventIfApplicable(workflowExecution);
            return;
        }

        triggerTasks(workflowExecution, workflowExecutionGraph.getSuccessors(taskExecution));
    }

    protected void workflowFinish(final IWorkflowExecution workflowExecution,
                                  final WorkflowExecutionStatus workflowExecutionStatus) {
        // todo: add transaction configuration in lifecycle event, all sync lifecycle should be in transaction
        transactionTemplate.execute(status -> {
            final WorkflowInstance workflowInstance = workflowExecution.getWorkflowInstance();
            workflowInstance.setEndTime(new Date());
            transformWorkflowInstanceState(workflowExecution, workflowExecutionStatus);
            WorkflowInstanceMetrics.recordWorkflowInstanceFinish(
                    workflowExecutionStatus,
                    workflowInstance.getWorkflowDefinitionCode());
            if (workflowExecution.getWorkflowExecuteContext().getWorkflowDefinition().getExecutionType()
                    .isSerial()) {
                if (serialCommandDao.deleteByWorkflowInstanceId(workflowInstance.getId()) > 0) {
                    log.info("Success clear SerialCommand for WorkflowExecuteRunnable: {}",
                            workflowExecution.getName());
                }
            }
            workflowExecution.getWorkflowEventBus()
                    .publish(WorkflowFinalizeLifecycleEvent.of(workflowExecution));
            return null;
        });
    }

    /**
     * Transformer the workflow instance state to targetState. This method will both update the
     * workflow instance state in memory and in the database.
     */
    protected void transformWorkflowInstanceState(final IWorkflowExecution workflowExecution,
                                                  final WorkflowExecutionStatus targetState) {
        final WorkflowInstance workflowInstance = workflowExecution.getWorkflowInstance();
        WorkflowExecutionStatus originState = workflowInstance.getState();
        try {
            workflowInstance.setState(targetState);
            workflowInstanceDao.updateById(workflowInstance);
            log.info("Success set WorkflowExecuteRunnable: {} state from: {} to {}",
                    workflowInstance.getName(), originState.name(), targetState.name());
        } catch (Exception ex) {
            workflowInstance.setState(originState);
            throw ex;
        }
    }

    /**
     * Emit the workflow finished event if the workflow can finish, otherwise do nothing.
     * <p> The workflow finish state is determined by the state of the task in the workflow.
     */
    protected abstract void emitWorkflowFinishedEventIfApplicable(final IWorkflowExecution workflowExecution);

    protected boolean isWorkflowFinishable(final IWorkflowExecution workflowExecution) {
        return workflowExecution.getWorkflowExecutionGraph().isAllTaskExecutionChainFinish();
    }

    /**
     * Assert that the state of the task is the expected state.
     *
     * @throws IllegalStateException if the state of the task is not the expected state.
     */
    protected void throwExceptionIfStateIsNotMatch(final IWorkflowExecution workflowExecution) {
        checkNotNull(workflowExecution, "workflowExecution is null");
        final WorkflowExecutionStatus actualState = workflowExecution.getState();
        final WorkflowExecutionStatus expectState = matchState();
        if (actualState != expectState) {
            final String workflowName = workflowExecution.getName();
            throw new IllegalStateException(
                    "The workflow: " + workflowName + " state: " + actualState + " is not match:" + expectState);
        }
    }

    protected void logWarningIfCannotDoAction(final IWorkflowExecution workflowExecution,
                                              final AbstractLifecycleEvent event) {
        log.warn("Workflow {} state is {} cannot do action on event: {}",
                workflowExecution.getName(),
                workflowExecution.getState(),
                event);
    }

    protected void finalizeEventAction(final IWorkflowExecution workflowExecution) {
        log.info(WorkflowInstanceUtils.logWorkflowInstanceInDetails(workflowExecution));

        workflowCacheRepository.remove(workflowExecution.getId());
        workflowEventBusCoordinator.unRegisterWorkflowEventBus(workflowExecution);
        workflowAlertManager.sendAlertWorkflowInstance(workflowExecution.getWorkflowInstance());

        log.info("Successfully finalize WorkflowExecuteRunnable: {}", workflowExecution.getName());
    }
}
