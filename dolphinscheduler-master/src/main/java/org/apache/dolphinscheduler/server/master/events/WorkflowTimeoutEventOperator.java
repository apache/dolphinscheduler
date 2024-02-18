package org.apache.dolphinscheduler.server.master.events;

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.server.master.cache.IWorkflowExecuteRunnableRepository;
import org.apache.dolphinscheduler.server.master.workflow.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.service.alert.ProcessAlertManager;
import org.apache.dolphinscheduler.service.process.ProcessService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowTimeoutEventOperator
        implements
            IEventOperator<WorkflowTimeoutEvent> {

    @Autowired
    private IWorkflowExecuteRunnableRepository<IWorkflowExecutionRunnable> workflowExecutionRunnableRepository;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ProcessAlertManager processAlertManager;

    @Override
    public void handleEvent(WorkflowTimeoutEvent event) {
        IWorkflowExecutionRunnable workflowExecutionRunnable =
                workflowExecutionRunnableRepository.getByProcessInstanceId(event.getWorkflowInstanceId());
        if (workflowExecutionRunnable == null) {
            log.warn("Cannot find the workflow instance by id: {}", event.getWorkflowInstanceId());
            return;
        }
        // we only support timeout warning for now
        ProcessInstance workflowInstance = workflowExecutionRunnable.getWorkflowInstance();
        ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(workflowInstance.getId());
        processAlertManager.sendProcessTimeoutAlert(workflowInstance, projectUser);
    }
}
