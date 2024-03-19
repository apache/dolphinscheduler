package org.apache.dolphinscheduler.server.master.dag;

public interface IDAGEngineBuilder {

    IDAGEngine buildDAGEngine(IWorkflowExecutionDAG workflowExecutionDAG);

}
