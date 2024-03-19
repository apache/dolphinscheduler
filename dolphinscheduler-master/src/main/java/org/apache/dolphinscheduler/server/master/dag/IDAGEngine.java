package org.apache.dolphinscheduler.server.master.dag;

/**
 * The IDAGEngine is responsible for triggering, killing, pausing, and finalizing task in {@link IWorkflowExecutionDAG}.
 * <p>All DAG operation should directly use the method in IDAGEngine, new {@link IWorkflowExecutionDAG} should be triggered by new IDAGEngine.
 */
public interface IDAGEngine {

    /**
     * Trigger the tasks which are post of the given task.
     * <P> If there are no task after the given taskNode, will try to finish the WorkflowExecutionRunnable.
     * <p> If the
     *
     * @param parentTaskNodeName the parent task name
     */
    void triggerNextTasks(String parentTaskNodeName);

    /**
     * Trigger the given task
     *
     * @param taskName task name
     */
    void triggerTask(String taskName);

    /**
     * Pause the given task.
     *
     * @param taskInstanceId taskInstanceId
     */
    void pauseTask(Integer taskInstanceId);

    /**
     * Kill the given task.
     *
     * @param taskInstanceId taskInstanceId.
     */
    void killTask(Integer taskInstanceId);

    /**
     * Finalize a task instance. Once a taskInstance has been finalized, then it cannot receive new operation, and will be removed from memory.
     *
     * @param taskInstanceId the ID of the task instance to finalize
     */
    void finalizeTask(Integer taskInstanceId);

    /**
     * Get {@link IWorkflowExecutionDAG} belong to the Engine.
     *
     * @return workflow execution DAG.
     */
    IWorkflowExecutionDAG getWorkflowExecutionDAG();

}
