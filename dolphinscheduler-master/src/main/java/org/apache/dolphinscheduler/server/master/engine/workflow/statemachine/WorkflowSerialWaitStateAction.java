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

package org.apache.dolphinscheduler.server.master.engine.workflow.statemachine;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.WorkflowExecutionRunnableFactory;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * The serial wait state shouldn't occur in runtime, it should be transformed to running state by {@link WorkflowExecutionRunnableFactory}
 */
@Slf4j
@Component
public class WorkflowSerialWaitStateAction extends WorkflowPseudoStateAction {

    @Override
    public WorkflowExecutionStatus matchState() {
        return WorkflowExecutionStatus.SERIAL_WAIT;
    }

    /**
     * The running state can only finish with success/failure.
     */
    @Override
    protected void emitWorkflowFinishedEventIfApplicable(final IWorkflowExecutionRunnable workflowExecutionRunnable) {
        log.warn("The workflow: {} is in serial_wait state, shouldn't emit workflow finished event",
                workflowExecutionRunnable.getName());
    }
}
