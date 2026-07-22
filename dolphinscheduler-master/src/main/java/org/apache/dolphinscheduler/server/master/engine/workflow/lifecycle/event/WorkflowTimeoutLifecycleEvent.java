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

import static com.google.common.base.Preconditions.checkState;

import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.AbstractWorkflowLifecycleLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.WorkflowLifecycleEventType;

import java.util.concurrent.TimeUnit;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkflowTimeoutLifecycleEvent extends AbstractWorkflowLifecycleLifecycleEvent {

    private IWorkflowExecution workflowExecution;

    protected WorkflowTimeoutLifecycleEvent(final IWorkflowExecution workflowExecution,
                                            final long timeout) {
        super(timeout);
        this.workflowExecution = workflowExecution;
    }

    public static WorkflowTimeoutLifecycleEvent of(IWorkflowExecution workflowExecution) {
        final WorkflowInstance workflowInstance = workflowExecution.getWorkflowInstance();
        checkState(workflowInstance != null,
                "The workflow instance must be initialized before creating workflow timeout event.");

        final int timeout = workflowInstance.getTimeout();
        checkState(timeout > 0, "The workflow timeout: %s must > 0 minutes", timeout);

        long delayTime = System.currentTimeMillis() - workflowInstance.getRestartTime().getTime()
                + TimeUnit.MINUTES.toMillis(timeout);
        return new WorkflowTimeoutLifecycleEvent(workflowExecution, delayTime);
    }

    @Override
    public ILifecycleEventType getEventType() {
        return WorkflowLifecycleEventType.TIMEOUT;
    }

    @Override
    public String toString() {
        return "WorkflowTimeoutLifecycleEvent{" +
                "workflow=" + workflowExecution.getWorkflowExecuteContext().getWorkflowInstance().getName() +
                '}';
    }
}
