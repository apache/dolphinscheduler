package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskExecutionRunnableContext {

    private ProcessInstance workflowInstance;
    private TaskInstance taskInstance;
    private TaskExecutionContext taskExecutionContext;
}
