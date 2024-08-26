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

package org.apache.dolphinscheduler.api.executor.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExecutorClient {

    @Autowired
    private TriggerWorkflowExecutorDelegate triggerWorkflowExecutorDelegate;

    @Autowired
    private BackfillWorkflowExecutorDelegate backfillWorkflowExecutorDelegate;

    @Autowired
    private RepeatRunningWorkflowInstanceExecutorDelegate repeatRunningWorkflowInstanceExecutorDelegate;

    @Autowired
    private RecoverFailureTaskInstanceExecutorDelegate recoverFailureTaskInstanceExecutorDelegate;

    @Autowired
    private RecoverSuspendedWorkflowInstanceExecutorDelegate recoverSuspendedWorkflowInstanceExecutorDelegate;

    @Autowired
    private PauseWorkflowInstanceExecutorDelegate pauseWorkflowInstanceExecutorDelegate;
    @Autowired
    private StopWorkflowInstanceExecutorDelegate stopWorkflowInstanceExecutorDelegate;

    public TriggerWorkflowExecutorDelegate triggerWorkflowDefinition() {
        return triggerWorkflowExecutorDelegate;
    }

    public BackfillWorkflowExecutorDelegate backfillWorkflowDefinition() {
        return backfillWorkflowExecutorDelegate;
    }

    public RepeatRunningWorkflowInstanceExecutorDelegate.RepeatRunningWorkflowInstanceOperation repeatRunningWorkflowInstance() {
        return new RepeatRunningWorkflowInstanceExecutorDelegate.RepeatRunningWorkflowInstanceOperation(
                repeatRunningWorkflowInstanceExecutorDelegate);
    }

    public RecoverFailureTaskInstanceExecutorDelegate.RecoverFailureTaskInstanceOperation recoverFailureTaskInstance() {
        return new RecoverFailureTaskInstanceExecutorDelegate.RecoverFailureTaskInstanceOperation(
                recoverFailureTaskInstanceExecutorDelegate);
    }

    public RecoverSuspendedWorkflowInstanceExecutorDelegate.RecoverSuspendedWorkflowInstanceOperation recoverSuspendedWorkflowInstanceOperation() {
        return new RecoverSuspendedWorkflowInstanceExecutorDelegate.RecoverSuspendedWorkflowInstanceOperation(
                recoverSuspendedWorkflowInstanceExecutorDelegate);
    }

    public PauseWorkflowInstanceExecutorDelegate.PauseWorkflowInstanceOperation pauseWorkflowInstance() {
        return new PauseWorkflowInstanceExecutorDelegate.PauseWorkflowInstanceOperation(
                pauseWorkflowInstanceExecutorDelegate);
    }

    public StopWorkflowInstanceExecutorDelegate.StopWorkflowInstanceOperation stopWorkflowInstance() {
        return new StopWorkflowInstanceExecutorDelegate.StopWorkflowInstanceOperation(
                stopWorkflowInstanceExecutorDelegate);
    }

}
