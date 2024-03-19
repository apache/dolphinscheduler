package org.apache.dolphinscheduler.server.master.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowOperationEvent implements IWorkflowEvent, ISyncEvent {

    private Integer workflowInstanceId;
    private WorkflowOperationType workflowOperationType;

    public static WorkflowOperationEvent of(Integer workflowInstanceId, WorkflowOperationType workflowOperationType) {
        return WorkflowOperationEvent.builder().workflowInstanceId(workflowInstanceId)
                .workflowOperationType(workflowOperationType).build();
    }

}
