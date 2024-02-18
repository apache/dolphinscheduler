package org.apache.dolphinscheduler.server.master.dag;

public interface IWorkflowExecutionDAGBuilder {

    IWorkflowExecutionDAG buildWorkflowExecutionDAG(IWorkflowDAG workflowDAG);
}
