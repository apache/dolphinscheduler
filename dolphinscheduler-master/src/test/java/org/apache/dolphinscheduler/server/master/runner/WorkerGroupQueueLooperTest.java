package org.apache.dolphinscheduler.server.master.runner;

import java.util.*;

import org.apache.commons.lang3.RandomUtils;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.graph.WorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionRunnableBuilder;
import org.apache.dolphinscheduler.server.master.exception.dispatch.TaskDispatchException;
import org.apache.dolphinscheduler.server.master.runner.queue.WorkerGroupQueueMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerGroupQueueLooperTest {

    @Mock
    private WorkerGroupQueueMap workerGroupQueueMap;

    @Mock
    private ITaskExecutorClient taskExecutorClient;

    @InjectMocks
    private WorkerGroupQueueLooper workerGroupQueueLooper;

    private ITaskExecutionRunnable createTaskExecuteRunnable(TaskExecutionStatus state) {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setWorkflowInstancePriority(Priority.MEDIUM);

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(RandomUtils.nextInt());
        taskInstance.setTaskInstancePriority(Priority.MEDIUM);
        taskInstance.setFirstSubmitTime(new Date());
        taskInstance.setState(state);

        final ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(TaskExecutionContextFactory.class))
                .thenReturn(mock(TaskExecutionContextFactory.class));
        final TaskExecutionRunnableBuilder taskExecutionRunnableBuilder = TaskExecutionRunnableBuilder.builder()
                .applicationContext(applicationContext)
                .workflowInstance(workflowInstance)
                .taskInstance(taskInstance)
                .workflowExecutionGraph(new WorkflowExecutionGraph())
                .workflowDefinition(new WorkflowDefinition())
                .project(new Project())
                .taskDefinition(new TaskDefinition())
                .workflowEventBus(new WorkflowEventBus())
                .build();
        return new TaskExecutionRunnable(taskExecutionRunnableBuilder);
    }
    @Test
    void testDoDispatchSuccess() throws TaskDispatchException {
        ITaskExecutionRunnable taskExecutionRunnable = createTaskExecuteRunnable(TaskExecutionStatus.SUBMITTED_SUCCESS);

        Map<String, ITaskExecutionRunnable> taskMap = new HashMap<>();
        taskMap.put("testWorkerGroup", taskExecutionRunnable);
        when(workerGroupQueueMap.poll()).thenReturn(taskMap);

        workerGroupQueueLooper.doDispatch();

        verify(taskExecutorClient, times(1)).dispatch(taskExecutionRunnable);
    }

    @Test
    void testDoDispatchFail() throws TaskDispatchException {
        ITaskExecutionRunnable taskExecutionRunnable =  createTaskExecuteRunnable(TaskExecutionStatus.SUBMITTED_SUCCESS);
        doThrow(new RuntimeException("Dispatch failed")).when(taskExecutorClient).dispatch(taskExecutionRunnable);

        Map<String, ITaskExecutionRunnable> taskMap = new HashMap<>();
        taskMap.put("testWorkerGroup", taskExecutionRunnable);
        when(workerGroupQueueMap.poll()).thenReturn(taskMap);

        assertThrows(RuntimeException.class, () -> workerGroupQueueLooper.doDispatch());
        verify(workerGroupQueueMap, times(1)).add(anyString(), eq(taskExecutionRunnable), anyLong());
    }

    @Test
    void testDoDispatchTaskStateNotEligible() throws TaskDispatchException{
        ITaskExecutionRunnable taskExecutionRunnable = createTaskExecuteRunnable(TaskExecutionStatus.SUCCESS);

        Map<String, ITaskExecutionRunnable> taskMap = new HashMap<>();
        taskMap.put("testWorkerGroup", taskExecutionRunnable);
        when(workerGroupQueueMap.poll()).thenReturn(taskMap);

        workerGroupQueueLooper.doDispatch();

        verify(taskExecutorClient, never()).dispatch(taskExecutionRunnable);
    }
}
