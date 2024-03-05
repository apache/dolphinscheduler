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

package org.apache.dolphinscheduler.workflow.engine.engine;

import org.apache.dolphinscheduler.workflow.engine.exception.WorkflowExecuteRunnableNotFoundException;
import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecutionRunnable;

/**
 * The WorkflowEngine is responsible for starting, stopping, pausing, and finalizing workflows.
 */
public interface IWorkflowEngine {

    /**
     * Trigger a workflow to start.
     *
     * @param workflowExecuteRunnable the workflow to start
     */
    void triggerWorkflow(IWorkflowExecutionRunnable workflowExecuteRunnable);

    /**
     * Pause a workflow instance.
     *
     * @param workflowInstanceId the ID of the workflow to pause
     * @throws WorkflowExecuteRunnableNotFoundException if the workflow is not found
     */
    void pauseWorkflow(Integer workflowInstanceId);

    /**
     * Kill a workflow instance.
     *
     * @param workflowInstanceId the ID of the workflow to stop
     * @throws WorkflowExecuteRunnableNotFoundException if the workflow is not found
     */
    void killWorkflow(Integer workflowInstanceId);

    /**
     * Finalize a workflow instance. Once a workflow has been finalized, then it cannot receive new operation, and will be removed from memory.
     *
     * @param workflowInstanceId the ID of the workflow to finalize
     */
    void finalizeWorkflow(Integer workflowInstanceId);

}
