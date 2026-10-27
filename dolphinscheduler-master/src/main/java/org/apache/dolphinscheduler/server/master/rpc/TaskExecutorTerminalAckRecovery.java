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

package org.apache.dolphinscheduler.server.master.rpc;

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.ILogicTaskExecutorOperator;
import org.apache.dolphinscheduler.extract.worker.IPhysicalTaskExecutorOperator;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.service.alert.WorkflowAlertManager;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter;
import org.apache.dolphinscheduler.task.executor.events.IReportableTaskExecutorLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorSuccessLifecycleEvent;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Recovers terminal event acknowledgements after a finished workflow has left the Master's cache.
 * The executor may still be retrying a report whose ACK was lost before workflow finalization.
 * Only persisted completion is acknowledged; workflow execution and task state are not changed.
 */
@Slf4j
@Service
public class TaskExecutorTerminalAckRecovery {

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private WorkflowAlertManager workflowAlertManager;

    /**
     * Confirm a terminal report from persisted state when its workflow is no longer cached.
     * Database and alert failures prevent acknowledgement. ACK send failures leave recovery to the next report.
     */
    public void recover(final IReportableTaskExecutorLifecycleEvent event,
                        final TaskExecutionStatus reportedTaskExecutionStatus,
                        final String taskInstanceHost) {
        // Finalization releases the workflow without waiting for the executor to receive its ACK.
        // Confirm the terminal result or a later user operation before acknowledging the original event.
        final TaskInstance taskInstance = taskInstanceDao.queryOptionalById(event.getTaskInstanceId())
                .orElseThrow(() -> new IllegalArgumentException("Cannot find the TaskInstance: "
                        + event.getTaskInstanceId()));
        checkArgument(Objects.equals(taskInstance.getId(), event.getTaskInstanceId())
                && taskInstance.getWorkflowInstanceId() == event.getWorkflowInstanceId()
                && isTaskTerminalStateCompatible(reportedTaskExecutionStatus, taskInstance.getState())
                && StringUtils.isNotEmpty(taskInstanceHost)
                && taskInstanceHost.equals(taskInstance.getHost()),
                "Terminal event does not match persisted task instance: %s", event.getTaskInstanceId());

        final WorkflowInstance workflowInstance =
                workflowInstanceDao.queryOptionalById(event.getWorkflowInstanceId())
                        .orElseThrow(() -> new IllegalArgumentException("Cannot find the WorkflowInstance: "
                                + event.getWorkflowInstanceId()));
        checkArgument(Objects.equals(workflowInstance.getId(), event.getWorkflowInstanceId())
                && workflowInstance.getState().isFinalState(),
                "Cannot confirm finished WorkflowInstance: %s", event.getWorkflowInstanceId());

        if (event instanceof TaskExecutorSuccessLifecycleEvent) {
            final TaskExecutorSuccessLifecycleEvent successEvent = (TaskExecutorSuccessLifecycleEvent) event;
            if (successEvent.isNeedAlert()) {
                // Preserve the normal handler's at-least-once alert delivery before acknowledging.
                workflowAlertManager.sendTaskResultAlert(workflowInstance, taskInstance,
                        successEvent.getTaskAlertInfo());
            }
        }
        final ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck ack =
                new ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck(taskInstance.getId(),
                        event.getType());
        try {
            if (TaskTypeUtils.isLogicTask(taskInstance.getTaskType())) {
                Clients.withService(ILogicTaskExecutorOperator.class)
                        .withHost(taskInstance.getHost())
                        .ackTaskExecutorLifecycleEvent(ack);
            } else {
                Clients.withService(IPhysicalTaskExecutorOperator.class)
                        .withHost(taskInstance.getHost())
                        .ackPhysicalTaskExecutorLifecycleEvent(ack);
            }
        } catch (Exception ex) {
            // The executor keeps the event pending and reports it again until it receives the ACK.
            log.error("Recover taskExecutorLifecycleEventAck: {} failed", ack, ex);
        }
    }

    private boolean isTaskTerminalStateCompatible(final TaskExecutionStatus reportedState,
                                                  final TaskExecutionStatus persistedState) {
        if (persistedState == reportedState) {
            return true;
        }
        // A failed task awaiting retry can be paused or killed without another executor event.
        // Failed and killed tasks can also be forced to success after the workflow has finished.
        switch (reportedState) {
            case FAILURE:
                return persistedState == TaskExecutionStatus.PAUSE
                        || persistedState == TaskExecutionStatus.KILL
                        || persistedState == TaskExecutionStatus.FORCED_SUCCESS;
            case KILL:
                return persistedState == TaskExecutionStatus.FORCED_SUCCESS;
            default:
                return false;
        }
    }

}
