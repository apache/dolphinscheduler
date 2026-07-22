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
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.TaskLifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskStartLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskTimeoutLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.statemachine.ITaskStateAction;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskStartLifecycleEventHandler extends AbstractTaskLifecycleEventHandler<TaskStartLifecycleEvent> {

    @Autowired
    private MasterConfig masterConfig;

    @Override
    public void handle(final IWorkflowExecution workflowExecution,
                       final TaskStartLifecycleEvent taskStartLifecycleEvent) {
        final ITaskExecution taskExecution = taskStartLifecycleEvent.getTaskExecution();
        // Since if the ITaskExecution is start at the first time, then it might not be initialized.
        // So we need to initialize the task instance here.
        // Otherwise, we cannot find the statemachine by task instance state.
        if (!taskExecution.isTaskInstanceInitialized()) {
            taskExecution.initializeFirstRunTaskInstance();
        }
        taskTimeoutMonitor(taskExecution);
        super.handle(workflowExecution, taskStartLifecycleEvent);
    }

    @Override
    public void handle(final ITaskStateAction taskStateAction,
                       final IWorkflowExecution workflowExecution,
                       final ITaskExecution taskExecution,
                       final TaskStartLifecycleEvent event) {
        taskStateAction.onStartEvent(workflowExecution, taskExecution, event);
    }

    @Override
    public ILifecycleEventType matchEventType() {
        return TaskLifecycleEventType.START;
    }

    private void taskTimeoutMonitor(final ITaskExecution taskExecution) {
        final TaskDefinition taskDefinition = taskExecution.getTaskDefinition();
        int taskTimeout = taskDefinition.getTimeout();
        if (taskTimeout > 0 && taskDefinition.getTimeoutNotifyStrategy() != null) {
            log.debug("The task {} timeout {} is invalided, so the timeout monitor will not be started.",
                    taskDefinition.getName(),
                    taskDefinition.getTimeout());
            taskExecution.getWorkflowEventBus().publish(TaskTimeoutLifecycleEvent.of(
                    taskExecution, taskDefinition.getTimeoutNotifyStrategy(), taskTimeout));
        }

        int systemTimeout = (int) masterConfig.getServerLoadProtection().getMaxTaskInstanceRuntime().toMinutes();
        if (systemTimeout > 0) {
            taskExecution.getWorkflowEventBus().publish(TaskTimeoutLifecycleEvent.of(
                    taskExecution, TaskTimeoutStrategy.FAILED, systemTimeout));
        }
    }

}
