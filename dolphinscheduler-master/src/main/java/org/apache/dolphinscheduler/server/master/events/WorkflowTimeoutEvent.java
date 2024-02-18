package org.apache.dolphinscheduler.server.master.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTimeoutEvent implements IWorkflowEvent, IAsyncEvent {

    private Integer workflowInstanceId;

}
