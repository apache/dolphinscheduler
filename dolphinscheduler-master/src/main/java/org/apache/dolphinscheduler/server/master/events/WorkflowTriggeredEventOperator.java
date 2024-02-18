package org.apache.dolphinscheduler.server.master.events;

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.server.master.cache.IWorkflowExecuteRunnableRepository;
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;
import org.apache.dolphinscheduler.server.master.workflow.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.service.alert.ListenerEventAlertManager;
import org.apache.dolphinscheduler.service.process.ProcessService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowTriggeredEventOperator
        implements
            IWorkflowEventOperator<WorkflowTriggeredEvent> {

    @Autowired
    private IWorkflowExecuteRunnableRepository workflowExecuteRunnableRepository;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ListenerEventAlertManager listenerEventAlertManager;

    @Override
    public void handleEvent(WorkflowTriggeredEvent event) {
        int workflowInstanceId = event.getWorkflowInstanceId();
        IWorkflowExecutionRunnable workflowExecutionRunnable =
                workflowExecuteRunnableRepository.getByProcessInstanceId(workflowInstanceId);
        Long workflowDefinitionCode =
                workflowExecutionRunnable.getWorkflowExecutionContext().getWorkflowDefinition().getCode();
        ProcessInstanceMetrics.incProcessInstanceByStateAndProcessDefinitionCode("submit", workflowDefinitionCode);

        ProcessInstance workflowInstance = workflowExecutionRunnable.getWorkflowInstance();
        ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(workflowInstanceId);
        listenerEventAlertManager.publishProcessStartListenerEvent(workflowInstance, projectUser);
    }
}
