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
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskStartLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowFinalizeLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
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
    protected void triggerTasks(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                final List<ITaskExecutionRunnable> triggerCandidateTasks) {
        final IWorkflowExecutionGraph workflowExecutionGraph = workflowExecutionRunnable.getWorkflowExecutionGraph();
        final List<ITaskExecutionRunnable> readyToTriggerTasks = triggerCandidateTasks
                .stream()
                .filter(workflowExecutionGraph::isTriggerConditionMet)
                .sorted(Comparator.comparing(ITaskExecutionRunnable::getName))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(readyToTriggerTasks)) {
            return;
        }
        final WorkflowEventBus workflowEventBus = workflowExecutionRunnable.getWorkflowEventBus();
        for (ITaskExecutionRunnable readyToTriggerTask : readyToTriggerTasks) {
            workflowExecutionGraph.markTaskExecutionRunnableActive(readyToTriggerTask);
            if (workflowExecutionGraph.isTaskExecutionRunnableSkipped(readyToTriggerTask)
                    || workflowExecutionGraph.isTaskExecutionRunnableForbidden(readyToTriggerTask)) {
                workflowEventBus.publish(
                        WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent.of(
                                workflowExecutionRunnable, readyToTriggerTask));
                continue;
            }
            workflowEventBus.publish(TaskStartLifecycleEvent.of(readyToTriggerTask));
        }
    }

    protected void killActiveTask(final IWorkflowExecutionRunnable workflowExecutionRunnable) {
        try {
            LogUtils.setWorkflowInstanceIdMDC(workflowExecutionRunnable.getId());
            workflowExecutionRunnable
                    .getWorkflowExecutionGraph()
                    .getActiveTaskExecutionRunnable()
                    .forEach(ITaskExecutionRunnable::kill);
        } finally {
            LogUtils.removeWorkflowInstanceIdMDC();
        }
    }

    protected void pauseActiveTask(final IWorkflowExecutionRunnable workflowExecutionRunnable) {
        try {
            LogUtils.setWorkflowInstanceIdMDC(workflowExecutionRunnable.getId());
            workflowExecutionRunnable
                    .getWorkflowExecutionGraph()
                    .getActiveTaskExecutionRunnable()
                    .forEach(ITaskExecutionRunnable::pause);
        } finally {
            LogUtils.removeWorkflowInstanceIdMDC();
        }
    }

    protected void tryToTriggerSuccessorsAfterTaskFinish(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                                         final ITaskExecutionRunnable taskExecutionRunnable) {
        final IWorkflowExecutionGraph workflowExecutionGraph = workflowExecutionRunnable.getWorkflowExecutionGraph();
        if (workflowExecutionGraph.isEndOfTaskChain(taskExecutionRunnable)) {
            emitWorkflowFinishedEventIfApplicable(workflowExecutionRunnable);
            return;
        }

        successorFlowAdjuster.adjustSuccessorFlow(taskExecutionRunnable);
        final List<ITaskExecutionRunnable> successors = workflowExecutionGraph.getSuccessors(taskExecutionRunnable);
        if (successors.isEmpty()) {
            log.debug("The task: {} has no successor, try to emit workflow finished event",
                    taskExecutionRunnable.getName());
            emitWorkflowFinishedEventIfApplicable(workflowExecutionRunnable);
            return;
        }
        triggerTasks(workflowExecutionRunnable, successors);
    }

    protected void workflowFinish(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                  final WorkflowExecutionStatus workflowExecutionStatus) {
        // todo: add transaction configuration in lifecycle event, all sync lifecycle should be in transaction
        transactionTemplate.execute(status -> {
            final WorkflowInstance workflowInstance = workflowExecutionRunnable.getWorkflowInstance();
            workflowInstance.setEndTime(new Date());
            transformWorkflowInstanceState(workflowExecutionRunnable, workflowExecutionStatus);
            if (workflowExecutionRunnable.getWorkflowExecuteContext().getWorkflowDefinition().getExecutionType()
                    .isSerial()) {
                if (serialCommandDao.deleteByWorkflowInstanceId(workflowInstance.getId()) > 0) {
                    log.info("Success clear SerialCommand for WorkflowExecuteRunnable: {}",
                            workflowExecutionRunnable.getName());
                }
            }
            workflowExecutionRunnable.getWorkflowEventBus()
                    .publish(WorkflowFinalizeLifecycleEvent.of(workflowExecutionRunnable));
            return null;
        });
    }

    /**
     * Transformer the workflow instance state to targetState. This method will both update the
     * workflow instance state in memory and in the database.
     */
    protected void transformWorkflowInstanceState(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                                  final WorkflowExecutionStatus targetState) {
        final WorkflowInstance workflowInstance = workflowExecutionRunnable.getWorkflowInstance();
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
    protected abstract void emitWorkflowFinishedEventIfApplicable(final IWorkflowExecutionRunnable workflowExecutionRunnable);

    protected boolean isWorkflowFinishable(final IWorkflowExecutionRunnable workflowExecutionRunnable) {
        return workflowExecutionRunnable.getWorkflowExecutionGraph().isAllTaskExecutionRunnableChainFinish();
    }

    /**
     * Assert that the state of the task is the expected state.
     *
     * @throws IllegalStateException if the state of the task is not the expected state.
     */
    protected void throwExceptionIfStateIsNotMatch(final IWorkflowExecutionRunnable workflowExecutionRunnable) {
        checkNotNull(workflowExecutionRunnable, "workflowExecutionRunnable is null");
        final WorkflowExecutionStatus actualState = workflowExecutionRunnable.getState();
        final WorkflowExecutionStatus expectState = matchState();
        if (actualState != expectState) {
            final String workflowName = workflowExecutionRunnable.getName();
            throw new IllegalStateException(
                    "The workflow: " + workflowName + " state: " + actualState + " is not match:" + expectState);
        }
    }

    protected void logWarningIfCannotDoAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                              final AbstractLifecycleEvent event) {
        log.warn("Workflow {} state is {} cannot do action on event: {}",
                workflowExecutionRunnable.getName(),
                workflowExecutionRunnable.getState(),
                event);
    }

    protected void finalizeEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable) {
        log.info(WorkflowInstanceUtils.logWorkflowInstanceInDetails(workflowExecutionRunnable));

        workflowCacheRepository.remove(workflowExecutionRunnable.getId());
        workflowEventBusCoordinator.unRegisterWorkflowEventBus(workflowExecutionRunnable);
        workflowAlertManager.sendAlertWorkflowInstance(workflowExecutionRunnable.getWorkflowInstance());

        log.info("Successfully finalize WorkflowExecuteRunnable: {}", workflowExecutionRunnable.getName());
    }
}
