package org.apache.dolphinscheduler.server.master.runner.task.switchtask;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.SwitchResultVo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowGraph;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.WorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.IWorkflowExecuteContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

class SwitchLogicTaskTest {

    @Mock
    private TaskExecutionContext taskExecutionContext;

    @Mock
    private IWorkflowGraph workflowGraph;

    @Mock
    private IWorkflowExecutionRunnable workflowExecutionRunnable;

    private SwitchLogicTask switchLogicTask;

    private SwitchParameters switchParameters;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mocking WorkflowExecutionContext and Graph
        IWorkflowExecuteContext mockContext = mock(IWorkflowExecuteContext.class);
        IWorkflowExecutionGraph mockGraph = mock(IWorkflowExecutionGraph.class);
        given(mockContext.getWorkflowExecutionGraph()).willReturn(mockGraph);

        workflowExecutionRunnable = mock(WorkflowExecutionRunnable.class);
        given(workflowExecutionRunnable.getWorkflowExecuteContext()).willReturn(mockContext);

        // Initializing taskExecutionContext and setting expected behaviors
        taskExecutionContext = mock(TaskExecutionContext.class);
        given(taskExecutionContext.getTaskParams()).willReturn("{}");
        given(taskExecutionContext.getPrepareParamsMap()).willReturn(new HashMap<>());

        // Mocking task execution runnable and task instance
        ITaskExecutionRunnable mockTaskExecutionRunnable = mock(ITaskExecutionRunnable.class);
        TaskInstance mockTaskInstance = mock(TaskInstance.class);
        given(mockTaskExecutionRunnable.getTaskInstance()).willReturn(mockTaskInstance);
        given(mockGraph.getTaskExecutionRunnableById(anyInt())).willReturn(mockTaskExecutionRunnable);

        // Mock SwitchParameters and set up a basic SwitchResult
        switchParameters = mock(SwitchParameters.class);
        List<SwitchResultVo> switchResultVoList = new ArrayList<>();
        switchResultVoList.add(new SwitchResultVo("someCondition", 123L));
        SwitchParameters.SwitchResult switchResult = new SwitchParameters.SwitchResult(switchResultVoList, 999L);
        given(switchParameters.getSwitchResult()).willReturn(switchResult);

        // Initializing the SwitchLogicTask
        switchLogicTask = new SwitchLogicTask(workflowExecutionRunnable, taskExecutionContext);
//        switchLogicTask.setSwitchParameters(switchParameters);  // Ensure switchParameters is set
    }


    @Test
    void testHandle_ShouldReturnSuccess_WhenConditionMatches() throws MasterTaskExecuteException {
        // Given
        List<SwitchResultVo> switchResultVoList = new ArrayList<>();
        SwitchResultVo switchResultVo = new SwitchResultVo("true", 123L);
        switchResultVoList.add(switchResultVo);
        switchParameters.setSwitchResult(new SwitchParameters.SwitchResult(switchResultVoList, 999L));

        Map<String, Property> globalParams = new HashMap<>();
        given(taskExecutionContext.getPrepareParamsMap()).willReturn(globalParams);

        // When
        switchLogicTask.handle();

        // Then
        then(taskExecutionContext).should().setCurrentExecutionStatus(TaskExecutionStatus.SUCCESS);
    }

    @Test
    void testHandle_DefaultBranch_WhenNoConditionMatches() throws MasterTaskExecuteException {
        // Given
        List<SwitchResultVo> switchResultVoList = new ArrayList<>();
        SwitchResultVo switchResultVo = new SwitchResultVo("false", 123L);
        switchResultVoList.add(switchResultVo);
        switchParameters.setSwitchResult(new SwitchParameters.SwitchResult(switchResultVoList, 999L));

        // When
        switchLogicTask.handle();

        // Then
        then(taskExecutionContext).should().setCurrentExecutionStatus(TaskExecutionStatus.SUCCESS);
        assertThat(switchParameters.getNextBranch()).isEqualTo(999L); // Default branch should be selected
    }

//    @Test
//    void testHandle_ThrowsException_WhenBranchDoesNotExist() {
//        // Given
//        given(workflowGraph.getTaskNodeByCode(anyLong())).willReturn(null);
//
//        // When/Then
//        assertThatThrownBy(() -> switchLogicTask.handle())
//                .isInstanceOf(MasterTaskExecuteException.class)
//                .hasMessageContaining("please check the switch task configuration");
//    }
//
//    @Test
//    void testHandle_ThrowsException_WhenDefaultBranchIsMissing() {
//        // Given
//        switchParameters.setSwitchResult(new SwitchParameters.SwitchResult(new ArrayList<>(), null));
//
//        // When/Then
//        assertThatThrownBy(() -> switchLogicTask.handle())
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("please check the switch task configuration");
//    }
}
