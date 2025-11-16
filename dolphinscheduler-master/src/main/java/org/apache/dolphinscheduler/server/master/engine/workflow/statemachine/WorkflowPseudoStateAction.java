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

import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowFinalizeLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;

/**
 * Abstract state action class for PseudoStates,including: WorkflowSerialWaitState,
 * WorkflowSubmittedState,and WorkflowFailoverState, at present.
 * A PseudoState does not receive any events and serves purely as a marker state.
 */
public abstract class WorkflowPseudoStateAction extends WorkflowTerminalStateAction {

    @Override
    public void onFinalizeEvent(IWorkflowExecutionRunnable workflowExecutionRunnable,
                                WorkflowFinalizeLifecycleEvent workflowFinalizeEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        logWarningIfCannotDoAction(workflowExecutionRunnable, workflowFinalizeEvent);
    }
}
