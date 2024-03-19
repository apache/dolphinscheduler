package org.apache.dolphinscheduler.server.master.exception;

public class TaskExecuteRunnableNotFoundException extends RuntimeException {

    public TaskExecuteRunnableNotFoundException(Integer workflowInstanceId) {
        super("WorkflowExecuteRunnable not found: [id=" + workflowInstanceId + "]");
    }

    public TaskExecuteRunnableNotFoundException(String workflowInstanceName) {
        super("WorkflowExecuteRunnable not found: [name=" + workflowInstanceName + "]");
    }
}
