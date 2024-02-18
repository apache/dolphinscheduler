package org.apache.dolphinscheduler.server.master.workflow;

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.master.cache.IWorkflowExecuteRunnableRepository;
import org.apache.dolphinscheduler.server.master.events.WorkflowOperationEvent;
import org.apache.dolphinscheduler.server.master.events.WorkflowOperationType;
import org.apache.dolphinscheduler.server.master.exception.WorkflowExecuteRunnableNotFoundException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowEngine implements IWorkflowEngine {

    @Autowired
    private IWorkflowExecuteRunnableRepository workflowExecuteRunnableRepository;

    @Override
    public void start() {
        log.info("{} started", getClass().getName());
    }

    @Override
    public void triggerWorkflow(IWorkflowExecutionRunnable workflowExecuteRunnable) {
        ProcessInstance workflowInstance = workflowExecuteRunnable.getWorkflowExecutionContext().getWorkflowInstance();
        Integer workflowInstanceId = workflowInstance.getId();
        log.info("Triggering WorkflowExecutionRunnable: {}", workflowInstance.getName());
        workflowExecuteRunnableRepository.cache(workflowInstanceId, workflowExecuteRunnable);
        workflowExecuteRunnable.getEventRepository()
                .storeEventToTail(WorkflowOperationEvent.of(workflowInstanceId, WorkflowOperationType.TRIGGER));
    }

    @Override
    public void pauseWorkflow(Integer workflowInstanceId) {
        IWorkflowExecutionRunnable workflowExecuteRunnable =
                workflowExecuteRunnableRepository.getByProcessInstanceId(workflowInstanceId);
        if (workflowExecuteRunnable == null) {
            throw new WorkflowExecuteRunnableNotFoundException(workflowInstanceId);
        }
        log.info("Pausing WorkflowExecutionRunnable: {}", workflowExecuteRunnable.getWorkflowInstanceName());
        workflowExecuteRunnable.getEventRepository()
                .storeEventToTail(WorkflowOperationEvent.of(workflowInstanceId, WorkflowOperationType.PAUSE));
    }

    @Override
    public void killWorkflow(Integer workflowInstanceId) {
        IWorkflowExecutionRunnable workflowExecuteRunnable =
                workflowExecuteRunnableRepository.getByProcessInstanceId(workflowInstanceId);
        if (workflowExecuteRunnable == null) {
            throw new WorkflowExecuteRunnableNotFoundException(workflowInstanceId);
        }
        log.info("Killing WorkflowExecutionRunnable: {}", workflowExecuteRunnable.getWorkflowInstanceName());
        workflowExecuteRunnable.getEventRepository()
                .storeEventToTail(WorkflowOperationEvent.of(workflowInstanceId, WorkflowOperationType.KILL));
    }

    @Override
    public void finalizeWorkflow(Integer workflowInstanceId) {
        IWorkflowExecutionRunnable workflowExecutionRunnable =
                workflowExecuteRunnableRepository.getByProcessInstanceId(workflowInstanceId);
        if (workflowExecutionRunnable == null) {
            return;
        }
        log.info("Finalizing WorkflowExecutionRunnable: {}", workflowExecutionRunnable.getWorkflowInstanceName());
        workflowExecuteRunnableRepository.removeByProcessInstanceId(workflowInstanceId);
    }

    @Override
    public void stop() {
        log.info("{} stopped", getClass().getName());
    }

}
