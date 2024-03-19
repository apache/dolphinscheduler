package org.apache.dolphinscheduler.server.master.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSuccessEvent implements ITaskEvent {

    private Integer workflowInstanceId;

    private Integer taskInstanceId;

}
