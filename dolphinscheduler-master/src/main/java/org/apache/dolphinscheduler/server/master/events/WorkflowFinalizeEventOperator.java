package org.apache.dolphinscheduler.server.master.events;

import org.apache.dolphinscheduler.server.master.cache.IWorkflowExecuteRunnableRepository;
import org.apache.dolphinscheduler.server.master.workflow.IWorkflowExecutionRunnable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowFinalizeEventOperator
        implements
            IWorkflowEventOperator<WorkflowFinalizeEvent> {

    @Autowired
    private IWorkflowExecuteRunnableRepository<IWorkflowExecutionRunnable> workflowExecuteRunnableRepository;

    @Override
    public void handleEvent(WorkflowFinalizeEvent event) {
        Integer workflowInstanceId = event.getWorkflowInstanceId();
        workflowExecuteRunnableRepository.removeByProcessInstanceId(workflowInstanceId);
    }

}
