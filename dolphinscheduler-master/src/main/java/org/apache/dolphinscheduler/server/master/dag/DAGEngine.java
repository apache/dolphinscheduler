package org.apache.dolphinscheduler.server.master.dag;

import org.apache.dolphinscheduler.server.master.events.EventRepository;
import org.apache.dolphinscheduler.server.master.events.TaskOperationEvent;
import org.apache.dolphinscheduler.server.master.events.TaskOperationType;
import org.apache.dolphinscheduler.server.master.runner.TaskExecuteRunnableFactory;
import org.apache.dolphinscheduler.server.master.runner.TaskExecutionRunnable;

import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;

public class DAGEngine implements IDAGEngine {

    private final IWorkflowExecutionDAG workflowExecutionDAG;

    private final List<TaskTriggerConditionChecker> taskTriggerConditionCheckers;

    private TaskExecuteRunnableFactory<TaskExecutionRunnable> taskExecuteRunnableFactory;

    private final EventRepository eventRepository;

    public DAGEngine(IWorkflowExecutionDAG workflowExecutionDAG, EventRepository eventRepository) {
        this.workflowExecutionDAG = workflowExecutionDAG;
        this.taskTriggerConditionCheckers = new ArrayList<>();
        this.eventRepository = eventRepository;
    }

    @Override
    public void triggerNextTasks(String parentTaskNodeName) {
        workflowExecutionDAG.getWorkflowDAG().getPostNodeNames(parentTaskNodeName).forEach(this::triggerTask);
    }

    @Override
    @SneakyThrows
    public void triggerTask(String taskName) {
        for (TaskTriggerConditionChecker taskTriggerConditionChecker : taskTriggerConditionCheckers) {
            if (!taskTriggerConditionChecker.taskCanTrigger(taskName)) {
                return;
            }
        }
        // todo: create Task ExecutionRunnable
        TaskExecutionRunnable taskExecuteRunnable = taskExecuteRunnableFactory.createTaskExecuteRunnable(null);
        TaskOperationEvent taskOperationEvent = TaskOperationEvent.builder()
                .workflowInstanceId(taskExecuteRunnable.getTaskExecutionRunnableContext().getWorkflowInstance().getId())
                .taskInstanceId(taskExecuteRunnable.getTaskInstanceId())
                .taskOperationType(TaskOperationType.DISPATCH)
                .build();
        eventRepository.storeEventToTail(taskOperationEvent);
        workflowExecutionDAG.markTaskSubmitted(taskExecuteRunnable);

    }

    @Override
    public void pauseTask(Integer taskInstanceId) {

    }

    @Override
    public void killTask(Integer taskInstanceId) {

    }

    @Override
    public void finalizeTask(Integer taskInstanceId) {

    }

    @Override
    public IWorkflowExecutionDAG getWorkflowExecutionDAG() {
        return workflowExecutionDAG;
    }
}
