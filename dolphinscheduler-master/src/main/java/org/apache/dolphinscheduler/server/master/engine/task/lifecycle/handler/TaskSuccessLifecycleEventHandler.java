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

package org.apache.dolphinscheduler.server.master.engine.task.lifecycle.handler;

import org.apache.dolphinscheduler.plugin.task.api.model.TaskAlertInfo;
import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.task.client.TaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.TaskLifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskSuccessLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.statemachine.ITaskStateAction;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;
import org.apache.dolphinscheduler.service.alert.WorkflowAlertManager;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorLifecycleEventType;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskSuccessLifecycleEventHandler extends AbstractTaskLifecycleEventHandler<TaskSuccessLifecycleEvent> {

    private final TaskExecutorClient taskExecutorClient;

    private final WorkflowAlertManager workflowAlertManager;

    public TaskSuccessLifecycleEventHandler(final TaskExecutorClient taskExecutorClient,
                                            final WorkflowAlertManager workflowAlertManager) {
        this.taskExecutorClient = taskExecutorClient;
        this.workflowAlertManager = workflowAlertManager;
    }

    @Override
    public void handle(final ITaskStateAction taskStateAction,
                       final IWorkflowExecution workflowExecution,
                       final ITaskExecution taskExecution,
                       final TaskSuccessLifecycleEvent taskSuccessEvent) {
        // 1. State transition + DB persistence (may throw if state mismatch)
        taskStateAction.onSucceedEvent(workflowExecution, taskExecution, taskSuccessEvent);

        // 2. Persist task-result alert only after the success state transition is confirmed
        if (taskSuccessEvent.isNeedAlert()) {
            final TaskAlertInfo taskAlertInfo = taskSuccessEvent.getTaskAlertInfo();
            if (taskAlertInfo != null && taskAlertInfo.getAlertGroupId() != null
                    && taskAlertInfo.getAlertGroupId() > 0) {
                workflowAlertManager.sendTaskResultAlert(
                        taskExecution.getWorkflowInstance(),
                        taskExecution.getTaskInstance(),
                        taskAlertInfo);
            } else {
                log.warn("Task: {} need alert but alertGroupId is invalid, skip sending alert",
                        taskExecution.getName());
            }
        }

        // 3. ACK the worker — only after state transition and alert persistence are done
        taskExecutorClient.ackTaskExecutorLifecycleEvent(
                taskExecution,
                new ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck(
                        taskExecution.getId(),
                        TaskExecutorLifecycleEventType.SUCCESS));
    }

    @Override
    public ILifecycleEventType matchEventType() {
        return TaskLifecycleEventType.SUCCEEDED;
    }
}
