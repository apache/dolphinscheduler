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

import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.TaskLifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskKillLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskTimeoutLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.statemachine.ITaskStateAction;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.service.alert.WorkflowAlertManager;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskTimeoutLifecycleEventHandler extends AbstractTaskLifecycleEventHandler<TaskTimeoutLifecycleEvent> {

    private final WorkflowAlertManager workflowAlertManager;

    public TaskTimeoutLifecycleEventHandler(final WorkflowAlertManager workflowAlertManager) {
        this.workflowAlertManager = workflowAlertManager;
    }

    @Override
    public void handle(final ITaskStateAction taskStateAction,
                       final IWorkflowExecutionRunnable workflowExecutionRunnable,
                       final ITaskExecutionRunnable taskExecutionRunnable,
                       final TaskTimeoutLifecycleEvent taskTimeoutLifecycleEvent) {
        final IWorkflowExecutionGraph workflowExecutionGraph = workflowExecutionRunnable.getWorkflowExecutionGraph();
        if (!workflowExecutionGraph.isTaskExecutionRunnableActive(taskExecutionRunnable)) {
            // The task instance is not active, means it is already finished.
            return;
        }
        final TaskDefinition taskDefinition = taskExecutionRunnable.getTaskDefinition();
        final String taskName = taskExecutionRunnable.getName();
        final TaskTimeoutStrategy timeoutNotifyStrategy = taskDefinition.getTimeoutNotifyStrategy();
        if (timeoutNotifyStrategy == null) {
            log.info("The task {} TimeoutStrategy is null.", taskName);
            return;
        }
        switch (timeoutNotifyStrategy) {
            case WARN:
                log.info("The task {} TimeoutStrategy is WARN, try to send a timeout alert.", taskName);
                doTaskTimeoutAlert(taskExecutionRunnable);
                break;
            case FAILED:
                log.info("The task {} TimeoutStrategy is FAILED, try to publish a kill event.", taskName);
                doTaskTimeoutKill(taskExecutionRunnable);
                break;
            case WARNFAILED:
                log.info(
                        "The task {} TimeoutStrategy is WARNFAILED, try to publish a kill event and send a timeout alert.",
                        taskName);
                doTaskTimeoutKill(taskExecutionRunnable);
                doTaskTimeoutAlert(taskExecutionRunnable);
            default:
                log.warn("The task {} TimeoutStrategy is invalided.", taskName);
                break;
        }
    }

    private void doTaskTimeoutKill(final ITaskExecutionRunnable taskExecutionRunnable) {
        taskExecutionRunnable.getWorkflowEventBus().publish(TaskKillLifecycleEvent.of(taskExecutionRunnable));
    }

    private void doTaskTimeoutAlert(final ITaskExecutionRunnable taskExecutionRunnable) {
        final WorkflowInstance workflowInstance = taskExecutionRunnable.getWorkflowInstance();
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        // todo: inject the projectUser
        workflowAlertManager.sendTaskTimeoutAlert(workflowInstance, taskInstance, null);
    }

    @Override
    public ILifecycleEventType matchEventType() {
        return TaskLifecycleEventType.TIMEOUT;
    }
}
