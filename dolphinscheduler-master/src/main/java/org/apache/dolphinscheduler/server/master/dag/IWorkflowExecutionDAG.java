package org.apache.dolphinscheduler.server.master.dag;

import org.apache.dolphinscheduler.server.master.runner.TaskExecutionRunnable;

public interface IWorkflowExecutionDAG {

    IWorkflowDAG getWorkflowDAG();

    void markTaskSubmitted(TaskExecutionRunnable taskExecutionRunnable);
}
