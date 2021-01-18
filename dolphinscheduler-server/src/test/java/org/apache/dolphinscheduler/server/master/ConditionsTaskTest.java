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
package org.apache.dolphinscheduler.server.master;

import org.apache.dolphinscheduler.common.enums.DependentRelation;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.model.DependentItem;
import org.apache.dolphinscheduler.common.model.DependentTaskModel;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.conditions.ConditionsParameters;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.ConditionsTaskExecThread;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ConditionsTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(DependentTaskTest.class);

    /**
     * TaskNode.runFlag : task can be run normally
     */
    public static final String FLOWNODE_RUN_FLAG_NORMAL = "NORMAL";

    private ProcessService processService;

    private ProcessInstance processInstance;

    @Before
    public void before() {
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);

        MasterConfig config = new MasterConfig();
        Mockito.when(applicationContext.getBean(MasterConfig.class)).thenReturn(config);
        config.setMasterTaskCommitRetryTimes(3);
        config.setMasterTaskCommitInterval(1000);

        processService = Mockito.mock(ProcessService.class);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);

        processInstance = getProcessInstance();
        Mockito.when(processService
                .findProcessInstanceById(processInstance.getId()))
                .thenReturn(processInstance);
    }

    private TaskInstance testBasicInit(ExecutionStatus expectResult) {
        TaskInstance taskInstance = getTaskInstance(getTaskNode(), processInstance);

        // for MasterBaseTaskExecThread.submit
        Mockito.when(processService
                .submitTask(taskInstance))
                .thenReturn(taskInstance);
        // for MasterBaseTaskExecThread.call
        Mockito.when(processService
                .findTaskInstanceById(taskInstance.getId()))
                .thenReturn(taskInstance);
        // for ConditionsTaskExecThread.initTaskParameters
        Mockito.when(processService
                .saveTaskInstance(taskInstance))
                .thenReturn(true);
        // for ConditionsTaskExecThread.updateTaskState
        Mockito.when(processService
                .updateTaskInstance(taskInstance))
                .thenReturn(true);

        // for ConditionsTaskExecThread.waitTaskQuit
        List<TaskInstance> conditions = Stream.of(
                getTaskInstanceForValidTaskList(1001, "1", expectResult)
        ).collect(Collectors.toList());
        Mockito.when(processService
                .findValidTaskListByProcessId(processInstance.getId()))
                .thenReturn(conditions);

        return taskInstance;
    }

    @Test
    public void testBasicSuccess() throws Exception {
        TaskInstance taskInstance = testBasicInit(ExecutionStatus.SUCCESS);
        ConditionsTaskExecThread taskExecThread = new ConditionsTaskExecThread(taskInstance);
        taskExecThread.call();
        Assert.assertEquals(ExecutionStatus.SUCCESS, taskExecThread.getTaskInstance().getState());
    }

    @Test
    public void testBasicFailure() throws Exception {
        TaskInstance taskInstance = testBasicInit(ExecutionStatus.FAILURE);
        ConditionsTaskExecThread taskExecThread = new ConditionsTaskExecThread(taskInstance);
        taskExecThread.call();
        Assert.assertEquals(ExecutionStatus.FAILURE, taskExecThread.getTaskInstance().getState());
    }

    private TaskNode getTaskNode() {
        TaskNode taskNode = new TaskNode();
        taskNode.setId("tasks-1000");
        taskNode.setName("C");
        taskNode.setType(TaskType.CONDITIONS.toString());
        taskNode.setRunFlag(FLOWNODE_RUN_FLAG_NORMAL);

        DependentItem dependentItem = new DependentItem();
        dependentItem.setDepTasks("1");
        dependentItem.setStatus(ExecutionStatus.SUCCESS);

        DependentTaskModel dependentTaskModel = new DependentTaskModel();
        dependentTaskModel.setDependItemList(Stream.of(dependentItem).collect(Collectors.toList()));
        dependentTaskModel.setRelation(DependentRelation.AND);

        DependentParameters dependentParameters = new DependentParameters();
        dependentParameters.setDependTaskList(Stream.of(dependentTaskModel).collect(Collectors.toList()));
        dependentParameters.setRelation(DependentRelation.AND);

        // in: AND(AND(1 is SUCCESS))
        taskNode.setDependence(JSONUtils.toJsonString(dependentParameters));

        ConditionsParameters conditionsParameters = new ConditionsParameters();
        conditionsParameters.setSuccessNode(Stream.of("2").collect(Collectors.toList()));
        conditionsParameters.setFailedNode(Stream.of("3").collect(Collectors.toList()));

        // out: SUCCESS => 2, FAILED => 3
        taskNode.setConditionResult(JSONUtils.toJsonString(conditionsParameters));

        return taskNode;
    }

    private ProcessInstance getProcessInstance() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1000);
        processInstance.setProcessDefinitionId(1000);
        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);

        return processInstance;
    }

    private TaskInstance getTaskInstance(TaskNode taskNode, ProcessInstance processInstance) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1000);
        taskInstance.setTaskJson(JSONUtils.toJsonString(taskNode));
        taskInstance.setName(taskNode.getName());
        taskInstance.setTaskType(taskNode.getType());
        taskInstance.setProcessInstanceId(processInstance.getId());
        taskInstance.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        return taskInstance;
    }

    private TaskInstance getTaskInstanceForValidTaskList(int id, String name, ExecutionStatus state) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(id);
        taskInstance.setName(name);
        taskInstance.setState(state);
        return taskInstance;
    }
}
