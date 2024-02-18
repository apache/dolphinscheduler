package org.apache.dolphinscheduler.server.master.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskOperationEvent implements ITaskEvent, ISyncEvent {

    private Integer workflowInstanceId;
    private Integer taskInstanceId;

    private TaskOperationType taskOperationType;
}
