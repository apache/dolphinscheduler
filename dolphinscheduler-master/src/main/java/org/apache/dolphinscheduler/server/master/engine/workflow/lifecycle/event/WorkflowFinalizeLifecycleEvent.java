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

import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.AbstractWorkflowLifecycleLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.WorkflowLifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.WorkflowExecutionRunnable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The workflow instance finalize event, used to remove the {@link WorkflowExecutionRunnable} from the master, will
 * clear the workflow instance related resources in memory.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkflowFinalizeLifecycleEvent extends AbstractWorkflowLifecycleLifecycleEvent {

    private IWorkflowExecutionRunnable workflowExecutionRunnable;

    public static WorkflowFinalizeLifecycleEvent of(IWorkflowExecutionRunnable workflowExecutionRunnable) {
        checkNotNull(workflowExecutionRunnable, "workflowExecutionRunnable is null");
        return new WorkflowFinalizeLifecycleEvent(workflowExecutionRunnable);
    }

    @Override
    public ILifecycleEventType getEventType() {
        return WorkflowLifecycleEventType.FINALIZE;
    }

    @Override
    public String toString() {
        return "WorkflowFinalizeLifecycleEvent{" +
                "workflow=" + workflowExecutionRunnable.getName() +
                '}';
    }
}
