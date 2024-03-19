package org.apache.dolphinscheduler.server.master.events;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowFinishEvent implements IWorkflowEvent, ISyncEvent {

    private Integer workflowInstanceId;

    private WorkflowExecutionStatus workflowExecutionStatus;

}
