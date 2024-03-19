package org.apache.dolphinscheduler.server.master.events;

public interface IWorkflowEvent extends IEvent {

    /**
     * The id of WorkflowInstance which the event is related to
     *
     * @return workflowInstanceId, shouldn't be null
     */
    Integer getWorkflowInstanceId();

}
