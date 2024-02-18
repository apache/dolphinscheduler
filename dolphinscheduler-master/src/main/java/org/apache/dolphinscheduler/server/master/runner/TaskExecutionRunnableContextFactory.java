package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.cache.IWorkflowExecuteRunnableRepository;
import org.apache.dolphinscheduler.server.master.workflow.IWorkflowExecutionRunnable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The factory used to create {@link TaskExecutionRunnableContext}
 */
@Component
public class TaskExecutionRunnableContextFactory {

    @Autowired
    private IWorkflowExecuteRunnableRepository workflowExecuteRunnableRepository;

    @Autowired
    private TaskExecutionContextFactory taskExecutionContextFactory;

    /**
     * Create TaskExecutionRunnableContext
     *
     * @param taskInstance task instance
     * @return TaskExecutionRunnableContext
     */
    public TaskExecutionRunnableContext createTaskExecutionRunnableContext(TaskInstance taskInstance) {
        IWorkflowExecutionRunnable workflowExecuteRunnable =
                workflowExecuteRunnableRepository.getByProcessInstanceId(taskInstance.getProcessInstanceId());
        return TaskExecutionRunnableContext.builder()
                .workflowInstance(workflowExecuteRunnable.getWorkflowExecutionContext().getWorkflowInstance())
                .taskInstance(taskInstance)
                .taskExecutionContext(taskExecutionContextFactory.createTaskExecutionContext(taskInstance))
                .build();
    }
}
