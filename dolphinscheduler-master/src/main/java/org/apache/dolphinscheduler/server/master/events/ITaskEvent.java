package org.apache.dolphinscheduler.server.master.events;

public interface ITaskEvent extends IEvent {

    Integer getWorkflowInstanceId();

    Integer getTaskInstanceId();

}
