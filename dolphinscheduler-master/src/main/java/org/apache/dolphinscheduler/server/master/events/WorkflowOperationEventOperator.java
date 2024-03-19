package org.apache.dolphinscheduler.server.master.events;

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.master.cache.IWorkflowExecuteRunnableRepository;
import org.apache.dolphinscheduler.server.master.utils.ExceptionUtils;
import org.apache.dolphinscheduler.server.master.workflow.IWorkflowExecutionRunnable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowOperationEventOperator implements IWorkflowEventOperator<WorkflowOperationEvent> {

    @Autowired
    private IWorkflowExecuteRunnableRepository workflowExecuteRunnableRepository;

    @Override
    public void handleEvent(WorkflowOperationEvent event) {
        IWorkflowExecutionRunnable workflowExecutionRunnable =
                workflowExecuteRunnableRepository.getByProcessInstanceId(event.getWorkflowInstanceId());
        if (workflowExecutionRunnable == null) {
            log.warn(
                    "Handle workflowExecutionRunnableKillOperationEvent: {} failed: WorkflowExecutionRunnable not found",
                    event);
            return;
        }
        switch (event.getWorkflowOperationType()) {
            case TRIGGER:
                triggerWorkflow(workflowExecutionRunnable);
                break;
            case PAUSE:
                pauseWorkflow(workflowExecutionRunnable);
                break;
            case KILL:
                killWorkflow(workflowExecutionRunnable);
                break;
            default:
                log.error("Unknown operationType for event: {}", event);
        }
    }

    private void triggerWorkflow(IWorkflowExecutionRunnable workflowExecutionRunnable) {
        try {
            workflowExecutionRunnable.start();
        } catch (Throwable exception) {
            if (ExceptionUtils.isDatabaseConnectedFailedException(exception)) {
                throw exception;
            }
            ProcessInstance workflowInstance =
                    workflowExecutionRunnable.getWorkflowExecutionContext().getWorkflowInstance();
            log.error("Trigger workflow: {} failed", workflowInstance.getName(), exception);
            WorkflowFailedEvent workflowExecutionRunnableFailedEvent = WorkflowFailedEvent.builder()
                    .workflowInstanceId(workflowInstance.getId()).failedReason(exception.getMessage()).build();
            workflowExecutionRunnable.getEventRepository().storeEventToTail(workflowExecutionRunnableFailedEvent);
        }
    }

    private void pauseWorkflow(IWorkflowExecutionRunnable workflowExecutionRunnable) {
        workflowExecutionRunnable.pause();
    }

    private void killWorkflow(IWorkflowExecutionRunnable workflowExecutionRunnable) {
        workflowExecutionRunnable.kill();
    }
}
