import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.task.switchtask.SwitchLogicTask;
import org.apache.dolphinscheduler.plugin.task.api.model.SwitchResultVo;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SwitchLogicTaskTest {

    private SwitchLogicTask switchLogicTask;
    private IWorkflowExecutionRunnable workflowExecutionRunnable;
    private TaskExecutionContext taskExecutionContext;

    @Before
    public void setUp() {
        workflowExecutionRunnable = Mockito.mock(IWorkflowExecutionRunnable.class);
        taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn("{}"); // Mock task parameters
        switchLogicTask = new SwitchLogicTask(workflowExecutionRunnable, taskExecutionContext);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandle_EmptyDependTaskList_ShouldThrowException() throws MasterTaskExecuteException {
        // Given
        switchLogicTask.getTaskParameters().getSwitchResult().setDependTaskList(Collections.emptyList());

        // When
        switchLogicTask.handle();
    }

    @Test
    public void testHandle_NextBranchExists_ShouldSetNextBranch() throws MasterTaskExecuteException {
        // Given
        SwitchResultVo switchResultVo = new SwitchResultVo();
        switchResultVo.setCondition("true");
        switchResultVo.setNextNode(1L);
        switchLogicTask.getTaskParameters().getSwitchResult().setDependTaskList(Arrays.asList(switchResultVo));
        switchLogicTask.getTaskParameters().getSwitchResult().setNextNode(2L); // Default branch

        // Mocking the evaluation to return true
        when(SwitchTaskUtils.evaluate(anyString())).thenReturn(true);

        // When
        switchLogicTask.handle();

        // Then
        assertEquals(Long.valueOf(1), switchLogicTask.getTaskParameters().getNextBranch());
    }

    @Test
    public void testMoveToDefaultBranch_WhenNextNodeIsNull_ShouldThrowException() {
        // Given
        switchLogicTask.getTaskParameters().getSwitchResult().setNextNode(null);

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> switchLogicTask.moveToDefaultBranch());
    }

    @Test
    public void testCheckIfBranchExist_BranchNodeIsNull_ShouldThrowException() {
        // Given
        Long branchNode = null;

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> switchLogicTask.checkIfBranchExist(branchNode));
    }

    @Test
    public void testCalculateSwitchBranch_AllConditionsNotSatisfied_ShouldMoveToDefaultBranch() throws MasterTaskExecuteException {
        // Given
        SwitchResultVo switchResultVo1 = new SwitchResultVo();
        switchResultVo1.setCondition("false");
        switchResultVo1.setNextNode(1L);

        SwitchResultVo switchResultVo2 = new SwitchResultVo();
        switchResultVo2.setCondition("false");
        switchResultVo2.setNextNode(2L);

        switchLogicTask.getTaskParameters().getSwitchResult().setDependTaskList(Arrays.asList(switchResultVo1, switchResultVo2));
        switchLogicTask.getTaskParameters().getSwitchResult().setNextNode(999L); // Default branch

        // Mocking the evaluation to return false
        when(SwitchTaskUtils.evaluate(anyString())).thenReturn(false);

        // When
        switchLogicTask.handle();

        // Then
        assertEquals(Long.valueOf(999), switchLogicTask.getTaskParameters().getNextBranch());
    }

    @Test
    public void testCheckIfBranchExist_BranchDoesNotExist_ShouldThrowException() {
        // Given
        Long branchNode = 999L; // Assuming this branch does not exist

        // Mock