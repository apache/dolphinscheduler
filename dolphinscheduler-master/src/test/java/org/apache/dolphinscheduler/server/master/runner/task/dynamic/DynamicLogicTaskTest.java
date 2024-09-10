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

package org.apache.dolphinscheduler.server.master.runner.task.dynamic;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.DynamicInputParameter;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DynamicParameters;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.subworkflow.SubWorkflowService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DynamicLogicTaskTest {

    @Mock
    private WorkflowInstanceDao workflowInstanceDao;

    @Mock
    private TaskInstanceDao taskInstanceDao;

    @Mock
    private SubWorkflowService subWorkflowService;

    @Mock
    private ProcessService processService;

    @Mock
    private WorkflowDefinitionMapper processDefineMapper;

    @Mock
    private CommandMapper commandMapper;

    private DynamicParameters dynamicParameters;

    private WorkflowInstance workflowInstance;

    private TaskExecutionContext taskExecutionContext;

    private DynamicLogicTask dynamicLogicTask;

    @BeforeEach
    public void setUp() {
        // Set up your test environment before each test.
        dynamicParameters = new DynamicParameters();
        taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        workflowInstance = new WorkflowInstance();
        Mockito.when(workflowInstanceDao.queryById(Mockito.any())).thenReturn(workflowInstance);
        dynamicLogicTask = new DynamicLogicTask(
                taskExecutionContext,
                workflowInstanceDao,
                taskInstanceDao,
                subWorkflowService,
                processService,
                processDefineMapper,
                commandMapper);
    }

    @Test
    void testGenerateParameterGroup() {
        DynamicInputParameter dynamicInputParameter1 = new DynamicInputParameter();
        dynamicInputParameter1.setName("param1");
        dynamicInputParameter1.setValue("a,b,c");
        dynamicInputParameter1.setSeparator(",");

        DynamicInputParameter dynamicInputParameter2 = new DynamicInputParameter();
        dynamicInputParameter2.setName("param2");
        dynamicInputParameter2.setValue("1. 2 . 3");
        dynamicInputParameter2.setSeparator(".");

        List<DynamicInputParameter> dynamicInputParameters =
                Arrays.asList(dynamicInputParameter1, dynamicInputParameter2);
        dynamicParameters.setListParameters(dynamicInputParameters);
        dynamicParameters.setFilterCondition("b,2");

        Mockito.when(taskExecutionContext.getPrepareParamsMap()).thenReturn(new HashMap<>());
        Mockito.when(taskExecutionContext.getTaskParams())
                .thenReturn(JSONUtils.toJsonString(dynamicParameters));

        dynamicLogicTask = new DynamicLogicTask(
                taskExecutionContext,
                workflowInstanceDao,
                taskInstanceDao,
                subWorkflowService,
                processService,
                processDefineMapper,
                commandMapper);

        List<Map<String, String>> parameterGroup = dynamicLogicTask.generateParameterGroup();

        Assertions.assertEquals(4, parameterGroup.size()); // expected cartesian product without filtered values is 6

        // Assert the value of parameter groups. Adjust these according to your expectations.
        // Here we only check for a few representative cases to keep the test concise.
        Map<String, String> expectedMap1 = new HashMap<>();
        expectedMap1.put("param1", "a");
        expectedMap1.put("param2", "1");

        Map<String, String> expectedMap2 = new HashMap<>();
        expectedMap2.put("param1", "a");
        expectedMap2.put("param2", "3");

        Map<String, String> expectedMap3 = new HashMap<>();
        expectedMap3.put("param1", "c");
        expectedMap3.put("param2", "1");

        Map<String, String> expectedMap4 = new HashMap<>();
        expectedMap4.put("param1", "c");
        expectedMap4.put("param2", "3");

        assert (parameterGroup.containsAll(Arrays.asList(expectedMap1, expectedMap2, expectedMap3, expectedMap4)));
    }

    @Test
    void testResetProcessInstanceStatus_RepeatRunning() {
        workflowInstance.setCommandType(CommandType.REPEAT_RUNNING);
        WorkflowInstance subWorkflowInstance = new WorkflowInstance();
        List<WorkflowInstance> subWorkflowInstances = Arrays.asList(subWorkflowInstance);

        dynamicLogicTask.resetProcessInstanceStatus(subWorkflowInstances);

        Mockito.verify(workflowInstanceDao).updateById(subWorkflowInstance);
        Assertions.assertEquals(WorkflowExecutionStatus.WAIT_TO_RUN, subWorkflowInstance.getState());
    }

    @Test
    void testResetProcessInstanceStatus_StartFailureTaskProcess() {
        workflowInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        WorkflowInstance failedSubWorkflowInstance = new WorkflowInstance();
        failedSubWorkflowInstance.setState(WorkflowExecutionStatus.FAILURE);
        List<WorkflowInstance> subWorkflowInstances = Arrays.asList(failedSubWorkflowInstance);
        Mockito.when(subWorkflowService.filterFailedProcessInstances(subWorkflowInstances))
                .thenReturn(Arrays.asList(failedSubWorkflowInstance));

        dynamicLogicTask.resetProcessInstanceStatus(subWorkflowInstances);

        Mockito.verify(workflowInstanceDao).updateById(failedSubWorkflowInstance);
        Assertions.assertEquals(WorkflowExecutionStatus.WAIT_TO_RUN, failedSubWorkflowInstance.getState());
    }

}
