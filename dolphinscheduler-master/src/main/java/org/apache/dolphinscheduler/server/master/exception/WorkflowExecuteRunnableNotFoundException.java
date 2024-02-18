package org.apache.dolphinscheduler.server.master.exception;

public class WorkflowExecuteRunnableNotFoundException extends RuntimeException {

    public WorkflowExecuteRunnableNotFoundException(Integer workflowInstanceId) {
        super("WorkflowExecuteRunnable not found: [id=" + workflowInstanceId + "]");
    }

    public WorkflowExecuteRunnableNotFoundException(String workflowInstanceName) {
        super("WorkflowExecuteRunnable not found: [name=" + workflowInstanceName + "]");
    }

}
