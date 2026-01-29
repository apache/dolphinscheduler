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

import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.AbstractTaskLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.statemachine.ITaskStateAction;
import org.apache.dolphinscheduler.server.master.engine.task.statemachine.TaskStateActionFactory;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AbstractTaskLifecycleEventHandler<T extends AbstractTaskLifecycleEvent>
        implements
            ILifecycleEventHandler<T> {

    @Autowired
    protected TaskStateActionFactory taskStateActionFactory;

    @Override
    public void handle(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                       final T event) {
        final ITaskExecutionRunnable taskExecutionRunnable = event.getTaskExecutionRunnable();
        final TaskExecutionStatus state = taskExecutionRunnable.getTaskInstance().getState();
        final ITaskStateAction taskStateAction = taskStateActionFactory.getTaskStateAction(state);
        handle(taskStateAction, workflowExecutionRunnable, taskExecutionRunnable, event);
        log.info("Fired task {} {} with state {}",
                taskExecutionRunnable.getName(),
                event,
                state.name());
    }

    public abstract void handle(final ITaskStateAction taskStateAction,
                                final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                final ITaskExecutionRunnable taskExecutionRunnable,
                                final T event);

}
