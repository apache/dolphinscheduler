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
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.AbstractWorkflowLifecycleLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.WorkflowLifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;

import java.util.concurrent.TimeUnit;

import lombok.Getter;

@Getter
public class WorkflowTimeoutLifecycleEvent extends AbstractWorkflowLifecycleLifecycleEvent {

    private final IWorkflowExecutionRunnable workflowExecutionRunnable;

    protected WorkflowTimeoutLifecycleEvent(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                            final long timeout) {
        super(timeout);
        this.workflowExecutionRunnable = workflowExecutionRunnable;
    }

    public static WorkflowTimeoutLifecycleEvent of(final IWorkflowExecutionRunnable workflowExecutionRunnable) {
        final WorkflowInstance workflowInstance = workflowExecutionRunnable.getWorkflowInstance();
        checkState(workflowInstance != null, "The workflow instance must be initialized before timeout monitoring.");

        final int timeout = workflowInstance.getTimeout();
        checkState(timeout >= 0, "The workflow timeout: %s must >=0 minutes", timeout);

        // Calculate remaining time until timeout: timeout - elapsed time
        long delayTime = TimeUnit.MINUTES.toMillis(timeout)
                - (System.currentTimeMillis() - workflowInstance.getStartTime().getTime());
        // Ensure delayTime is not negative (trigger immediately if already timeout)
        delayTime = Math.max(0, delayTime);
        return new WorkflowTimeoutLifecycleEvent(workflowExecutionRunnable, delayTime);
    }

    @Override
    public ILifecycleEventType getEventType() {
        return WorkflowLifecycleEventType.TIMEOUT;
    }

    @Override
    public String toString() {
        return "WorkflowTimeoutLifecycleEvent{" +
                "workflow=" + workflowExecutionRunnable.getWorkflowExecuteContext().getWorkflowInstance().getName() +
                ", timeout=" + delayTime +
                '}';
    }
}
