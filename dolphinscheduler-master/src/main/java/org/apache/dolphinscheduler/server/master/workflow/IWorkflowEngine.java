package org.apache.dolphinscheduler.server.master.workflow;

import org.apache.dolphinscheduler.server.master.exception.WorkflowExecuteRunnableNotFoundException;

/**
 * The WorkflowEngine is responsible for starting, stopping, pausing, and finalizing workflows.
 */
public interface IWorkflowEngine {

    /**
     * Start the workflow engine.
     */
    void start();

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

    /**
     * Stop the workflow engine.
     */
    void stop();
}
