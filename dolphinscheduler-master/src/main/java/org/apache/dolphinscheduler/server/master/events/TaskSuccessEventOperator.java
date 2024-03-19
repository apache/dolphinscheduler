package org.apache.dolphinscheduler.server.master.events;

import org.apache.dolphinscheduler.server.master.cache.IWorkflowExecuteRunnableRepository;
import org.apache.dolphinscheduler.server.master.workflow.IWorkflowExecutionRunnable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskSuccessEventOperator implements ITaskEventOperator {

    @Autowired
    private IWorkflowExecuteRunnableRepository workflowExecuteRunnableRepository;

    @Override
    public void handleEvent(ITaskEvent event) {
        Integer workflowInstanceId = event.getWorkflowInstanceId();
        Integer taskInstanceId = event.getTaskInstanceId();
        IWorkflowExecutionRunnable workflowExecutionRunnable =
                workflowExecuteRunnableRepository.getByProcessInstanceId(workflowInstanceId);
        if (workflowExecutionRunnable == null) {
            log.error("Cannot find the WorkflowExecutionRunnable, the event: {} will be dropped", event);
            return;
        }

    }
}
