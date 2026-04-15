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

package org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.AbstractWorkflowLifecycleLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.WorkflowLifecycleEventType;

import java.util.Optional;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The event of topology logical transition in workflow, right now the transaction is only used for the task finish and trigger the next step.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent
        extends
            AbstractWorkflowLifecycleLifecycleEvent {

    private final IWorkflowExecution workflowExecution;

    private final ITaskExecution taskExecution;

    public static WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent of(
                                                                                   final IWorkflowExecution workflowExecution,
                                                                                   final ITaskExecution taskExecution) {
        checkNotNull(workflowExecution, "workflowExecution is null");
        checkNotNull(taskExecution, "taskExecution is null");
        return new WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent(
                workflowExecution,
                taskExecution);
    }

    @Override
    public ILifecycleEventType getEventType() {
        return WorkflowLifecycleEventType.TOPOLOGY_LOGICAL_TRANSACTION_WITH_TASK_FINISH;
    }

    @Override
    public String toString() {
        return "WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent{"
                + "task=" + taskExecution.getName()
                + "taskState="
                + Optional.ofNullable(taskExecution.getTaskInstance()).map(TaskInstance::getState)
                        .map(Enum::name).orElse(null)
                + '}';
    }
}
