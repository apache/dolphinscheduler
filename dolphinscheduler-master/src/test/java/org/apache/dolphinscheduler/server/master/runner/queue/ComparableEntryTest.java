package org.apache.dolphinscheduler.server.master.runner.queue;

import org.apache.commons.lang3.RandomUtils;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.graph.WorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionRunnableBuilder;
import org.apache.dolphinscheduler.server.master.runner.TaskExecutionContextFactory;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComparableEntryTest {

    @Mock
    private ITaskExecutionRunnable mockTaskExecutionRunnable;


    private ITaskExecutionRunnable createTaskExecuteRunnable(Priority workflowInstancePriority,
                                                             Priority taskInstancePriority) {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setWorkflowInstancePriority(workflowInstancePriority);

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(RandomUtils.nextInt());
        taskInstance.setTaskInstancePriority(taskInstancePriority);
        taskInstance.setFirstSubmitTime(new Date());

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
    public void testConstructor_ValidData_Success() {
        long delayTimeMills = 1000L;
        ComparableEntry entry = new ComparableEntry(delayTimeMills, createTaskExecuteRunnable(Priority.HIGHEST, Priority.HIGHEST));
        assertEquals(delayTimeMills, entry.getDelayTimeMills());
        assertEquals(mockTaskExecutionRunnable, entry.getData());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_NullData_ThrowsException() {
        new ComparableEntry(1000L, null);
    }

    @Test
    public void testCompareTo_DifferentData_SameTime() {
        ComparableEntry entry1 = new ComparableEntry(1000L, createTaskExecuteRunnable(Priority.LOW, Priority.HIGHEST));
        ComparableEntry entry2 = new ComparableEntry(1000L, createTaskExecuteRunnable(Priority.HIGHEST, Priority.MEDIUM));
        assertTrue("workflow priority higher than taskInstance", entry1.compareTo(entry2) > 0);
    }

    @Test
    public void testCompareTo_SamePriority_DifferentTime() {
        ComparableEntry entry1 = new ComparableEntry(1000L, createTaskExecuteRunnable(Priority.HIGHEST, Priority.HIGHEST));
        ComparableEntry entry2 = new ComparableEntry(2000L, createTaskExecuteRunnable(Priority.HIGHEST, Priority.HIGHEST));
        assertTrue("time", entry1.compareTo(entry2) < 0);
    }

}
