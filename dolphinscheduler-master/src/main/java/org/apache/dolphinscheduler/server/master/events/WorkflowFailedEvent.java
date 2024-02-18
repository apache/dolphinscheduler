package org.apache.dolphinscheduler.server.master.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowFailedEvent implements IWorkflowEvent, ISyncEvent {

    private Integer workflowInstanceId;

    private String failedReason;

}
