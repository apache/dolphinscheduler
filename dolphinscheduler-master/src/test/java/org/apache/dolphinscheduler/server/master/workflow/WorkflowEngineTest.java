package org.apache.dolphinscheduler.server.master.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.apache.dolphinscheduler.server.master.cache.IWorkflowExecuteRunnableRepository;
import org.apache.dolphinscheduler.server.master.cache.impl.WorkflowExecuteRunnableRepositoryImpl;
import org.apache.dolphinscheduler.server.master.exception.WorkflowExecuteRunnableNotFoundException;

import org.apache.commons.lang3.RandomUtils;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

class WorkflowEngineTest {

    @InjectMocks
    private WorkflowEngine workflowEngine;

    @Mock
    private final IWorkflowExecuteRunnableRepository<IWorkflowExecutionRunnable> workflowExecuteRunnableRepository =
            new WorkflowExecuteRunnableRepositoryImpl();

    @Test
    void start() {
        workflowEngine.start();
    }

    @Test
    void triggerWorkflow() {
        int workflowInstanceId = RandomUtils.nextInt(0, 100);
        WorkflowExecutionRunnable workflowExecuteRunnable = createWorkflowExecuteRunnable(workflowInstanceId);
        workflowEngine.triggerWorkflow(workflowExecuteRunnable);
        assertEquals(workflowExecuteRunnable,
                workflowExecuteRunnableRepository.getByProcessInstanceId(workflowInstanceId));
        workflowExecuteRunnableRepository.removeByProcessInstanceId(workflowInstanceId);

        doThrow(new RuntimeException()).when(workflowExecuteRunnable).start();
        assertThrows(RuntimeException.class, () -> workflowEngine.triggerWorkflow(workflowExecuteRunnable));
        assertNull(workflowExecuteRunnableRepository.getByProcessInstanceId(workflowInstanceId));
    }

    @Test
    void pauseWorkflow() {
        int workflowInstanceId = RandomUtils.nextInt(0, 100);
        assertThrows(WorkflowExecuteRunnableNotFoundException.class,
                () -> workflowEngine.pauseWorkflow(workflowInstanceId));

        WorkflowExecutionRunnable workflowExecuteRunnable = createWorkflowExecuteRunnable(workflowInstanceId);
        workflowExecuteRunnableRepository.cache(workflowInstanceId, workflowExecuteRunnable);
        workflowEngine.pauseWorkflow(workflowInstanceId);
        verify(workflowExecuteRunnable).pause();
    }

    @Test
    void stopWorkflow() {
        int workflowInstanceId = RandomUtils.nextInt(0, 100);
        assertThrows(WorkflowExecuteRunnableNotFoundException.class,
                () -> workflowEngine.pauseWorkflow(workflowInstanceId));

        WorkflowExecutionRunnable workflowExecuteRunnable = createWorkflowExecuteRunnable(workflowInstanceId);
        workflowExecuteRunnableRepository.cache(workflowInstanceId, workflowExecuteRunnable);
        workflowEngine.killWorkflow(workflowInstanceId);
        verify(workflowExecuteRunnable).kill();
    }

    @Test
    void finalizeWorkflow() {
        int workflowInstanceId = RandomUtils.nextInt(0, 100);
        WorkflowExecutionRunnable workflowExecuteRunnable = createWorkflowExecuteRunnable(workflowInstanceId);
        workflowExecuteRunnableRepository.cache(workflowInstanceId, workflowExecuteRunnable);
        workflowEngine.finalizeWorkflow(workflowInstanceId);
        assertNull(workflowExecuteRunnableRepository.getByProcessInstanceId(workflowInstanceId));
    }

    @Test
    void stop() {
        workflowEngine.stop();
    }

    private WorkflowExecutionRunnable createWorkflowExecuteRunnable(Integer workflowInstanceId) {
        WorkflowExecutionRunnable workflowExecuteRunnable =
                Mockito.mock(WorkflowExecutionRunnable.class, Answers.RETURNS_DEEP_STUBS);
        Mockito.when(workflowExecuteRunnable.getWorkflowExecuteContext().getWorkflowInstance().getId())
                .thenReturn(workflowInstanceId);
        return workflowExecuteRunnable;
    }

}
