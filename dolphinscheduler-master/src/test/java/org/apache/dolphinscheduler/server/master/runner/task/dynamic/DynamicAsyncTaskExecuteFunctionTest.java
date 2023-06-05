package org.apache.dolphinscheduler.server.master.runner.task.dynamic;

import static org.apache.dolphinscheduler.server.master.runner.execute.AsyncTaskExecuteFunction.AsyncTaskExecutionStatus;

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DynamicParameters;
import org.apache.dolphinscheduler.service.subworkflow.SubWorkflowService;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DynamicAsyncTaskExecuteFunctionTest {

    @Mock
    private ProcessInstance processInstance;

    @Mock
    private TaskInstance taskInstance;

    @Mock
    private SubWorkflowService subWorkflowService;

    @Mock
    private CommandMapper commandMapper;

    @Mock
    private DynamicLogicTask dynamicLogicTask;

    private DynamicAsyncTaskExecuteFunction function;

    @BeforeEach
    void setUp() {
        processInstance = new ProcessInstance();
        taskInstance = new TaskInstance();

        processInstance.setId(1);
        taskInstance.setTaskCode(2L);

        function = new DynamicAsyncTaskExecuteFunction(
                null,
                processInstance,
                taskInstance,
                dynamicLogicTask,
                commandMapper,
                subWorkflowService,
                0);
    }

    @Test
    void shouldReturnSuccessWhenAllSubProcessInstancesFinishedSuccessfully() {
        // Given
        List<ProcessInstance> processInstances = Arrays.asList(Mockito.mock(ProcessInstance.class));

        Mockito.when(processInstances.get(0).getCommandParam()).thenReturn("{}");
        Mockito.when(subWorkflowService.getAllDynamicSubWorkflow(1, 2L)).thenReturn(processInstances);
        Mockito.when(subWorkflowService.filterFinishProcessInstances(Mockito.any())).thenReturn(processInstances);
        Mockito.when(subWorkflowService.filterSuccessProcessInstances(Mockito.any())).thenReturn(processInstances);

        // When
        DynamicParameters dynamicParameters = new DynamicParameters();
        Mockito.when(dynamicLogicTask.getTaskParameters()).thenReturn(dynamicParameters);
        AsyncTaskExecutionStatus status = function.getAsyncTaskExecutionStatus();

        // Then
        Assertions.assertEquals(AsyncTaskExecutionStatus.SUCCESS, status);
    }

    @Test
    void shouldReturnFailedWhenSomeSubProcessInstancesFinishedUnsuccessfully() {
        // Given
        List<ProcessInstance> processInstances =
                Arrays.asList(Mockito.mock(ProcessInstance.class), Mockito.mock(ProcessInstance.class));
        Mockito.when(subWorkflowService.getAllDynamicSubWorkflow(1, 2L)).thenReturn(processInstances);
        Mockito.when(subWorkflowService.filterFinishProcessInstances(Mockito.anyList())).thenReturn(processInstances);
        Mockito.when(subWorkflowService.filterSuccessProcessInstances(Mockito.anyList()))
                .thenReturn(Arrays.asList(processInstances.get(0)));

        // When
        AsyncTaskExecutionStatus status = function.getAsyncTaskExecutionStatus();

        // Then
        Assertions.assertEquals(AsyncTaskExecutionStatus.FAILED, status);
    }

    @Test
    void shouldReturnRunningWhenSomeSubProcessInstancesAreRunning() {
        // Given
        List<ProcessInstance> processInstances = Arrays.asList(Mockito.mock(ProcessInstance.class));
        Mockito.when(subWorkflowService.getAllDynamicSubWorkflow(1, 2L)).thenReturn(processInstances);
        Mockito.when(subWorkflowService.filterFinishProcessInstances(Mockito.anyList())).thenReturn(Arrays.asList());

        // When
        AsyncTaskExecutionStatus status = function.getAsyncTaskExecutionStatus();

        // Then
        Assertions.assertEquals(AsyncTaskExecutionStatus.RUNNING, status);
    }

    @Test
    void shouldReturnFailedWhenLogicTaskIsCancelled() {
        // Given
        List<ProcessInstance> processInstances = Arrays.asList(Mockito.mock(ProcessInstance.class));
        Mockito.when(subWorkflowService.getAllDynamicSubWorkflow(1, 2L)).thenReturn(processInstances);
        Mockito.when(dynamicLogicTask.isCancel()).thenReturn(true);

        // When
        AsyncTaskExecutionStatus status = function.getAsyncTaskExecutionStatus();

        // Then
        Assertions.assertEquals(AsyncTaskExecutionStatus.FAILED, status);
    }

}
