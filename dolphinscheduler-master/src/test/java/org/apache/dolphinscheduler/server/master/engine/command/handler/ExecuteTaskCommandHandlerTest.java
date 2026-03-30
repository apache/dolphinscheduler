/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.server.master.engine.command.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.constants.CommandKeyConstants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowGraph;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteContext;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteContext.WorkflowExecuteContextBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ExecuteTaskCommandHandlerTest {

    private ExecuteTaskCommandHandler executeTaskCommandHandler;

    @Mock
    private WorkflowInstanceDao workflowInstanceDao;

    @Mock
    private TaskInstanceDao taskInstanceDao;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private IWorkflowGraph workflowGraph;

    private MasterConfig masterConfig;

    @BeforeEach
    void setUp() {
        executeTaskCommandHandler = new ExecuteTaskCommandHandler();
        masterConfig = new MasterConfig();
        masterConfig.setMasterAddress("127.0.0.1:5678");
        ReflectionTestUtils.setField(executeTaskCommandHandler, "workflowInstanceDao", workflowInstanceDao);
        ReflectionTestUtils.setField(executeTaskCommandHandler, "taskInstanceDao", taskInstanceDao);
        ReflectionTestUtils.setField(executeTaskCommandHandler, AbstractCommandHandler.class, "taskInstanceDao",
                taskInstanceDao, TaskInstanceDao.class);
        ReflectionTestUtils.setField(executeTaskCommandHandler, "applicationContext", applicationContext);
        ReflectionTestUtils.setField(executeTaskCommandHandler, "masterConfig", masterConfig);
    }

    @Test
    void testExecuteTaskCommandType() {
        assertEquals(CommandType.EXECUTE_TASK, executeTaskCommandHandler.commandType());
    }

    @Test
    void testAssembleWorkflowInstance() {
        Command command = new Command();
        command.setWorkflowInstanceId(1);
        command.setCommandType(CommandType.EXECUTE_TASK);
        command.setTaskDependType(TaskDependType.TASK_POST);
        WorkflowExecuteContextBuilder contextBuilder = WorkflowExecuteContext.builder().withCommand(command);

        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setTaskDependType(TaskDependType.TASK_ONLY);
        when(workflowInstanceDao.queryOptionalById(1)).thenReturn(Optional.of(workflowInstance));

        executeTaskCommandHandler.assembleWorkflowInstance(contextBuilder);

        assertSame(workflowInstance, contextBuilder.getWorkflowInstance());
        assertEquals(WorkflowExecutionStatus.RUNNING_EXECUTION, workflowInstance.getState());
        assertEquals(CommandType.EXECUTE_TASK, workflowInstance.getCommandType());
        assertEquals(TaskDependType.TASK_POST, workflowInstance.getTaskDependType());
        assertEquals("127.0.0.1:5678", workflowInstance.getHost());
        verify(workflowInstanceDao).updateById(workflowInstance);
    }

    @Test
    void testThrowExceptionWhenWorkflowInstanceNotExists() {
        Command command = new Command();
        command.setWorkflowInstanceId(100);
        WorkflowExecuteContextBuilder contextBuilder = WorkflowExecuteContext.builder().withCommand(command);
        when(workflowInstanceDao.queryOptionalById(100)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> executeTaskCommandHandler.assembleWorkflowInstance(contextBuilder));

        assertEquals("Cannot find WorkflowInstance:100", ex.getMessage());
    }

    @Test
    void testAssembleWorkflowExecutionGraph() {
        Command command = new Command();
        command.setWorkflowInstanceId(1);
        command.setCommandType(CommandType.EXECUTE_TASK);
        Map<String, Object> commandParam = new HashMap<>();
        commandParam.put(CommandKeyConstants.CMD_PARAM_START_NODES, "101");
        command.setCommandParam(JSONUtils.toJsonString(commandParam));
        WorkflowExecuteContextBuilder contextBuilder = WorkflowExecuteContext.builder().withCommand(command);

        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setTaskDependType(TaskDependType.TASK_POST);
        contextBuilder.setWorkflowInstance(workflowInstance);
        contextBuilder.setWorkflowDefinition(new WorkflowDefinition());
        contextBuilder.setProject(new Project());
        contextBuilder.setWorkflowEventBus(new WorkflowEventBus());
        contextBuilder.setWorkflowGraph(workflowGraph);

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setCode(101L);
        taskDefinition.setName("shell1");
        when(workflowGraph.getTaskNodeByCode(101L)).thenReturn(taskDefinition);
        when(workflowGraph.getTaskNodeByName("shell1")).thenReturn(taskDefinition);
        when(workflowGraph.getAllTaskNodes()).thenReturn(Collections.singletonList(taskDefinition));
        when(workflowGraph.getPredecessors("shell1")).thenReturn(Collections.emptySet());
        when(workflowGraph.getSuccessors("shell1")).thenReturn(Collections.<String>emptySet());
        when(taskInstanceDao.queryValidTaskListByWorkflowInstanceId(1)).thenReturn(Collections.emptyList());

        executeTaskCommandHandler.assembleWorkflowExecutionGraph(contextBuilder);

        assertNotNull(contextBuilder.getWorkflowExecutionGraph());
        assertEquals(1, contextBuilder.getWorkflowExecutionGraph().getAllTaskExecutionRunnable().size());
    }

    @Test
    void testThrowExceptionWhenStartNodesMissing() {
        Command command = new Command();
        command.setCommandType(CommandType.EXECUTE_TASK);
        command.setCommandParam("{}");
        WorkflowExecuteContextBuilder contextBuilder = WorkflowExecuteContext.builder().withCommand(command);
        contextBuilder.setWorkflowGraph(workflowGraph);

        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setTaskDependType(TaskDependType.TASK_POST);
        contextBuilder.setWorkflowInstance(workflowInstance);

        assertThrows(IllegalArgumentException.class,
                () -> executeTaskCommandHandler.assembleWorkflowExecutionGraph(contextBuilder));
    }
}
