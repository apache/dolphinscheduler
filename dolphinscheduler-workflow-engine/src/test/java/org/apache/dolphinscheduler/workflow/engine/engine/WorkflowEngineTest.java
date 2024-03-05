package org.apache.dolphinscheduler.workflow.engine.engine;

import static org.apache.dolphinscheduler.workflow.engine.assertions.WorkflowExecutionRunnableAssertions.workflowExecutionRunnable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.dolphinscheduler.workflow.engine.assertions.SingletonWorkflowExecuteRunnableRepositoryAssertions;
import org.apache.dolphinscheduler.workflow.engine.event.WorkflowOperationEvent;
import org.apache.dolphinscheduler.workflow.engine.exception.WorkflowExecuteRunnableNotFoundException;
import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.workflow.engine.workflow.MockWorkflowExecutionRunnableFactory;
import org.apache.dolphinscheduler.workflow.engine.workflow.SingletonWorkflowExecuteRunnableRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkflowEngineTest {

    private WorkflowEngine workflowEngine;

    @BeforeEach
    public void before() {
        workflowEngine = new WorkflowEngine(SingletonWorkflowExecuteRunnableRepository.getInstance());
    }

    @AfterEach
    public void after() {
        SingletonWorkflowExecuteRunnableRepository.getInstance().clear();
    }

    @Test
    void triggerWorkflow() {
        IWorkflowExecutionRunnable mockWorkflowExecuteRunnable =
                MockWorkflowExecutionRunnableFactory.createWorkflowExecutionRunnable();
        Integer workflowInstanceId = mockWorkflowExecuteRunnable.getWorkflowExecutionContext().getWorkflowInstanceId();

        workflowEngine.triggerWorkflow(mockWorkflowExecuteRunnable);
        workflowExecutionRunnable(mockWorkflowExecuteRunnable)
                .existEvent(WorkflowOperationEvent.triggerEvent(workflowInstanceId));
    }

    @Test
    void pauseWorkflow_WorkflowNotExist() {
        WorkflowExecuteRunnableNotFoundException exception =
                assertThrows(WorkflowExecuteRunnableNotFoundException.class, () -> workflowEngine.pauseWorkflow(1));
        assertEquals("WorkflowExecuteRunnable not found: [id=1]", exception.getMessage());
    }

    @Test
    void pauseWorkflow_WorkflowExist() {
        IWorkflowExecutionRunnable mockWorkflowExecuteRunnable =
                MockWorkflowExecutionRunnableFactory.createWorkflowExecutionRunnable();
        Integer workflowInstanceId =
                mockWorkflowExecuteRunnable.getWorkflowExecutionContext().getWorkflowInstanceId();
        SingletonWorkflowExecuteRunnableRepository.getInstance()
                .storeWorkflowExecutionRunnable(mockWorkflowExecuteRunnable);

        workflowEngine.pauseWorkflow(workflowInstanceId);
        workflowExecutionRunnable(mockWorkflowExecuteRunnable)
                .existEvent(WorkflowOperationEvent.pauseEvent(workflowInstanceId));
    }

    @Test
    void killWorkflow_WorkflowNotExist() {
        WorkflowExecuteRunnableNotFoundException exception =
                assertThrows(WorkflowExecuteRunnableNotFoundException.class,
                        () -> workflowEngine.killWorkflow(1));
        assertEquals("WorkflowExecuteRunnable not found: [id=1]", exception.getMessage());
    }

    @Test
    void killWorkflow_WorkflowExist() {
        IWorkflowExecutionRunnable mockWorkflowExecuteRunnable =
                MockWorkflowExecutionRunnableFactory.createWorkflowExecutionRunnable();
        Integer workflowInstanceId =
                mockWorkflowExecuteRunnable.getWorkflowExecutionContext().getWorkflowInstanceId();
        SingletonWorkflowExecuteRunnableRepository.getInstance()
                .storeWorkflowExecutionRunnable(mockWorkflowExecuteRunnable);

        workflowEngine.killWorkflow(workflowInstanceId);
        workflowExecutionRunnable(mockWorkflowExecuteRunnable)
                .existEvent(WorkflowOperationEvent.killEvent(workflowInstanceId));
    }

    @Test
    void finalizeWorkflow_WorkflowNotExist() {
        workflowEngine.finalizeWorkflow(-1);
    }

    @Test
    void finalizeWorkflow_WorkflowExist() {
        IWorkflowExecutionRunnable emptyWorkflowExecuteRunnable =
                MockWorkflowExecutionRunnableFactory.createWorkflowExecutionRunnable();
        Integer workflowInstanceId =
                emptyWorkflowExecuteRunnable.getWorkflowExecutionContext().getWorkflowInstanceId();
        SingletonWorkflowExecuteRunnableRepository.getInstance()
                .storeWorkflowExecutionRunnable(emptyWorkflowExecuteRunnable);
        SingletonWorkflowExecuteRunnableRepositoryAssertions.existWorkflowExecutionRunnable(workflowInstanceId);

        workflowEngine.finalizeWorkflow(workflowInstanceId);
        SingletonWorkflowExecuteRunnableRepositoryAssertions.notExistWorkflowExecutionRunnable(workflowInstanceId);
    }

}
