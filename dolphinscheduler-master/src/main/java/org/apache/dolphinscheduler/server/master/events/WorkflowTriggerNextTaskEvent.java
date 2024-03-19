package org.apache.dolphinscheduler.server.master.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTriggerNextTaskEvent implements IWorkflowEvent, ISyncEvent {

    private int workflowInstanceId;

    /**
     * The task name of the parent task, if it is the root task, the value is null
     */
    private String parentTaskNodeName;

}
