package org.apache.dolphinscheduler.server.master.workflow;

import org.apache.dolphinscheduler.server.master.runner.IWorkflowExecutionContext;

public interface IWorkflowExecutionRunnableBuilder {

    IWorkflowExecutionRunnable buildWorkflowExecutionRunnable(IWorkflowExecutionContext workflowExecutionContext);

}
