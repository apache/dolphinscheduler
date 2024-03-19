package org.apache.dolphinscheduler.server.master.dag;

public interface IWorkflowDAGBuilder {

    IWorkflowDAG buildWorkflowDAG(WorkflowIdentify workflowIdentify);

}
