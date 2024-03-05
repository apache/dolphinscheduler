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

package org.apache.dolphinscheduler.workflow.engine.assertions;

import org.apache.dolphinscheduler.workflow.engine.event.IEvent;
import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecutionRunnable;

import org.opentest4j.AssertionFailedError;

public class WorkflowExecutionRunnableAssertions {

    private final IWorkflowExecutionRunnable workflowExecutionRunnable;

    private WorkflowExecutionRunnableAssertions(IWorkflowExecutionRunnable workflowExecutionRunnable) {
        this.workflowExecutionRunnable = workflowExecutionRunnable;
    }

    public static WorkflowExecutionRunnableAssertions workflowExecutionRunnable(IWorkflowExecutionRunnable workflowExecutionRunnable) {
        return new WorkflowExecutionRunnableAssertions(workflowExecutionRunnable);
    }

    public void existEvent(IEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        boolean exist = workflowExecutionRunnable.getEventRepository().getAllEvent()
                .stream()
                .anyMatch(event1 -> event1.equals(event1));
        if (!exist) {
            throw new AssertionFailedError("The workflowExecuteRunnable doesn't exist event: " + event);
        }
    }

}
