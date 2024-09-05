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
import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.TaskLifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskStartLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskTimeoutLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.statemachine.ITaskStateAction;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskStartLifecycleEventHandler extends AbstractTaskLifecycleEventHandler<TaskStartLifecycleEvent> {

    @Override
    public void handle(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                       final TaskStartLifecycleEvent taskStartLifecycleEvent) {
        final ITaskExecutionRunnable taskExecutionRunnable = taskStartLifecycleEvent.getTaskExecutionRunnable();
        // Since if the ITaskExecutionRunnable is start at the first time, then it might not be initialized.
        // So we need to initialize the task instance here.
        // Otherwise, we cannot find the statemachine by task instance state.
        if (!taskExecutionRunnable.isTaskInstanceInitialized()) {
            taskExecutionRunnable.initializeFirstRunTaskInstance();
        }
        taskTimeoutMonitor(taskExecutionRunnable);
        super.handle(workflowExecutionRunnable, taskStartLifecycleEvent);
    }

    @Override
    public void handle(final ITaskStateAction taskStateAction,
                       final IWorkflowExecutionRunnable workflowExecutionRunnable,
                       final ITaskExecutionRunnable taskExecutionRunnable,
                       final TaskStartLifecycleEvent event) {
        taskStateAction.startEventAction(workflowExecutionRunnable, taskExecutionRunnable, event);
    }

    @Override
    public ILifecycleEventType matchEventType() {
        return TaskLifecycleEventType.START;
    }

    private void taskTimeoutMonitor(final ITaskExecutionRunnable taskExecutionRunnable) {
        final TaskDefinition taskDefinition = taskExecutionRunnable.getTaskDefinition();
        if (taskDefinition.getTimeout() <= 0) {
            log.debug("The task {} timeout {} is invalided, so the timeout monitor will not be started.",
                    taskDefinition.getName(),
                    taskDefinition.getTimeout());
            return;
        }
        taskExecutionRunnable.getWorkflowEventBus().publish(TaskTimeoutLifecycleEvent.of(taskExecutionRunnable));
    }

}
