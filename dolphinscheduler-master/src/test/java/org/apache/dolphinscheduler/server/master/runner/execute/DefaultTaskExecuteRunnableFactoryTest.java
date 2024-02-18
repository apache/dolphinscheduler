package org.apache.dolphinscheduler.server.master.runner.execute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.exception.TaskExecuteRunnableCreateException;
import org.apache.dolphinscheduler.server.master.runner.TaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.runner.TaskExecutionRunnableContext;
import org.apache.dolphinscheduler.server.master.runner.TaskExecutionRunnableContextFactory;
import org.apache.dolphinscheduler.server.master.runner.operator.TaskExecuteRunnableOperatorManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultTaskExecuteRunnableFactoryTest {

    @InjectMocks
    private DefaultTaskExecuteRunnableFactory defaultTaskExecuteRunnableFactory;

    @Mock
    private TaskExecuteRunnableOperatorManager taskExecuteRunnableOperatorManager;

    @Mock
    private TaskExecutionRunnableContextFactory taskExecutionRunnableContextFactory;

    @Test
    void createTaskExecuteRunnable() throws TaskExecuteRunnableCreateException {
        TaskExecuteRunnableCreateException taskExecuteRunnableCreateException =
                assertThrows(TaskExecuteRunnableCreateException.class,
                        () -> defaultTaskExecuteRunnableFactory.createTaskExecuteRunnable(null));
        assertEquals("TaskInstance is null", taskExecuteRunnableCreateException.getMessage());

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setName("test");
        doThrow(new NullPointerException("ProcessInstance is null")).when(taskExecutionRunnableContextFactory)
                .createTaskExecutionRunnableContext(taskInstance);
        taskExecuteRunnableCreateException =
                assertThrows(TaskExecuteRunnableCreateException.class,
                        () -> defaultTaskExecuteRunnableFactory.createTaskExecuteRunnable(taskInstance));
        assertEquals("Create DefaultTaskExecuteRunnable for taskInstance: test failed",
                taskExecuteRunnableCreateException.getMessage());

        doReturn(new TaskExecutionRunnableContext()).when(taskExecutionRunnableContextFactory)
                .createTaskExecutionRunnableContext(taskInstance);
        TaskExecutionRunnable taskExecuteRunnable =
                defaultTaskExecuteRunnableFactory.createTaskExecuteRunnable(taskInstance);
        assertNotNull(taskExecuteRunnable);
    }
}
