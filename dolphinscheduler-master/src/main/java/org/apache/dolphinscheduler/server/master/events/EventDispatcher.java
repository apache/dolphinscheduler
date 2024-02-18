package org.apache.dolphinscheduler.server.master.events;

import org.apache.dolphinscheduler.server.master.cache.IWorkflowExecuteRunnableRepository;
import org.apache.dolphinscheduler.server.master.exception.WorkflowExecuteRunnableNotFoundException;
import org.apache.dolphinscheduler.server.master.workflow.IWorkflowExecutionRunnable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventDispatcher implements IEventDispatcher<IEvent> {

    @Autowired
    private EventEngine eventEngine;

    @Autowired
    private IWorkflowExecuteRunnableRepository workflowExecuteRunnableRepository;

    @Override
    public void start() {
        log.info(getClass().getName() + " started");
    }

    @Override
    public void dispatchEvent(IEvent iEvent) {
        Integer workflowInstanceId;
        if (iEvent instanceof IWorkflowEvent) {
            workflowInstanceId = ((IWorkflowEvent) iEvent).getWorkflowInstanceId();
        } else if (iEvent instanceof ITaskEvent) {
            workflowInstanceId = ((ITaskEvent) iEvent).getWorkflowInstanceId();
        } else {
            throw new IllegalArgumentException("Unsupported event type: " + iEvent.getClass().getName());
        }

        IWorkflowExecutionRunnable workflowExecuteRunnable =
                workflowExecuteRunnableRepository.getByProcessInstanceId(workflowInstanceId);
        if (workflowExecuteRunnable == null) {
            throw new WorkflowExecuteRunnableNotFoundException(workflowInstanceId);
        }
        workflowExecuteRunnable.getEventRepository().storeEventToTail(iEvent);
        log.debug("Success dispatch event {} to EventRepository", iEvent);
        eventEngine.notify();
    }

    @Override
    public void stop() {
        log.info(getClass().getName() + " stopped");
    }

}
