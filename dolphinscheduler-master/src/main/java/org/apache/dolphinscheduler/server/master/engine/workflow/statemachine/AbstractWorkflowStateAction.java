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
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
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

import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

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

    /**
     * Try to trigger the tasks if the trigger condition is met.
     * <p> If all the given tasks trigger condition is not met then will try to emit workflow finish event.
     */
    protected void triggerTasks(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                final List<ITaskExecutionRunnable> taskExecutionRunnables) {
        final IWorkflowExecutionGraph workflowExecutionGraph = workflowExecutionRunnable.getWorkflowExecutionGraph();
        final List<ITaskExecutionRunnable> readyTaskExecutionRunnableList = taskExecutionRunnables
                .stream()
                .filter(workflowExecutionGraph::isTriggerConditionMet)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(readyTaskExecutionRunnableList)) {
            emitWorkflowFinishedEventIfApplicable(workflowExecutionRunnable);
            return;
        }
        final WorkflowEventBus workflowEventBus = workflowExecutionRunnable.getWorkflowEventBus();
        for (ITaskExecutionRunnable readyTaskExecutionRunnable : readyTaskExecutionRunnableList) {
            workflowExecutionGraph.markTaskExecutionRunnableActive(readyTaskExecutionRunnable);
            if (workflowExecutionGraph.isTaskExecutionRunnableSkipped(readyTaskExecutionRunnable)
                    || workflowExecutionGraph.isTaskExecutionRunnableForbidden(readyTaskExecutionRunnable)) {
                workflowExecutionGraph.markTaskExecutionRunnableInActive(readyTaskExecutionRunnable);
                workflowEventBus.publish(
                        WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent.of(
                                workflowExecutionRunnable, readyTaskExecutionRunnable));
                continue;
            }
            workflowEventBus.publish(TaskStartLifecycleEvent.of(readyTaskExecutionRunnable));
        }
    }

    protected void onTaskFinish(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                final ITaskExecutionRunnable taskExecutionRunnable) {
        final IWorkflowExecutionGraph workflowExecutionGraph = workflowExecutionRunnable.getWorkflowExecutionGraph();
        if (workflowExecutionGraph.isEndOfTaskChain(taskExecutionRunnable)) {
            emitWorkflowFinishedEventIfApplicable(workflowExecutionRunnable);
            return;
        }

        successorFlowAdjuster.adjustSuccessorFlow(taskExecutionRunnable);
        triggerTasks(workflowExecutionRunnable, workflowExecutionGraph.getSuccessors(taskExecutionRunnable));
    }

    protected void workflowFinish(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                  final WorkflowExecutionStatus workflowExecutionStatus) {
        final WorkflowInstance workflowInstance = workflowExecutionRunnable.getWorkflowInstance();
        workflowInstance.setEndTime(new Date());
        transformWorkflowInstanceState(workflowExecutionRunnable, workflowExecutionStatus);
        workflowExecutionRunnable.getWorkflowEventBus()
                .publish(WorkflowFinalizeLifecycleEvent.of(workflowExecutionRunnable));
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

        log.info("Successfully finalize WorkflowExecuteRunnable: {}", workflowExecutionRunnable.getName());
    }
}
