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

import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.TaskLifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskFailoverLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.statemachine.ITaskStateAction;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;

import org.springframework.stereotype.Component;

@Component
public class TaskFailoverLifecycleEventHandler extends AbstractTaskLifecycleEventHandler<TaskFailoverLifecycleEvent> {

    @Override
    public void handle(final ITaskStateAction taskStateAction,
                       final IWorkflowExecution workflowExecution,
                       final ITaskExecution taskExecution,
                       final TaskFailoverLifecycleEvent taskFailoverEvent) {
        taskStateAction.onFailoverEvent(workflowExecution, taskExecution, taskFailoverEvent);
    }

    @Override
    public ILifecycleEventType matchEventType() {
        return TaskLifecycleEventType.FAILOVER;
    }
}
