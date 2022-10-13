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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.SwitchResultVo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SwitchTaskTest {

    private ProcessService processService;

    private ProcessInstance processInstance;

    @BeforeEach
    public void before() {
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);

        MasterConfig config = new MasterConfig();
        Mockito.when(applicationContext.getBean(MasterConfig.class)).thenReturn(config);
        config.setTaskCommitRetryTimes(3);
        config.setTaskCommitInterval(Duration.ofSeconds(1));

        processService = Mockito.mock(ProcessService.class);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);

        processInstance = getProcessInstance();
        Mockito.when(processService
                .findProcessInstanceById(processInstance.getId()))
                .thenReturn(processInstance);
    }

    private TaskInstance testBasicInit(WorkflowExecutionStatus expectResult) {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setTimeoutFlag(TimeoutFlag.OPEN);
        taskDefinition.setTimeoutNotifyStrategy(TaskTimeoutStrategy.WARN);
        taskDefinition.setTimeout(0);
        Mockito.when(processService.findTaskDefinition(1L, 1))
                .thenReturn(taskDefinition);
        TaskInstance taskInstance = getTaskInstance(getTaskNode(), processInstance);

        // for MasterBaseTaskExecThread.submit
        Mockito.when(processService
                .submitTask(processInstance, taskInstance))
                .thenReturn(taskInstance);
        // for MasterBaseTaskExecThread.call
        Mockito.when(processService
                .findTaskInstanceById(taskInstance.getId()))
                .thenReturn(taskInstance);
        // for SwitchTaskExecThread.initTaskParameters
        Mockito.when(processService
                .saveTaskInstance(taskInstance))
                .thenReturn(true);
        // for SwitchTaskExecThread.updateTaskState
        Mockito.when(processService
                .updateTaskInstance(taskInstance))
                .thenReturn(true);

        return taskInstance;
    }

    private SwitchParameters getTaskNode() {
        SwitchParameters conditionsParameters = new SwitchParameters();

        SwitchResultVo switchResultVo1 = new SwitchResultVo();
        switchResultVo1.setCondition(" 2 == 1");
        switchResultVo1.setNextNode("t1");
        SwitchResultVo switchResultVo2 = new SwitchResultVo();
        switchResultVo2.setCondition(" 2 == 2");
        switchResultVo2.setNextNode("t2");
        SwitchResultVo switchResultVo3 = new SwitchResultVo();
        switchResultVo3.setCondition(" 3 == 2");
        switchResultVo3.setNextNode("t3");
        List<SwitchResultVo> list = new ArrayList<>();
        list.add(switchResultVo1);
        list.add(switchResultVo2);
        list.add(switchResultVo3);
        conditionsParameters.setDependTaskList(list);
        conditionsParameters.setNextNode("t");
        conditionsParameters.setRelation("AND");

        return conditionsParameters;
    }

    private ProcessInstance getProcessInstance() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1000);
        processInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        processInstance.setProcessDefinitionCode(1L);
        return processInstance;
    }

    private TaskInstance getTaskInstance(SwitchParameters conditionsParameters, ProcessInstance processInstance) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1000);
        Map<String, Object> taskParamsMap = new HashMap<>();
        taskParamsMap.put(Constants.SWITCH_RESULT, "");
        taskInstance.setTaskParams(JSONUtils.toJsonString(taskParamsMap));
        taskInstance.setSwitchDependency(conditionsParameters);
        taskInstance.setName("C");
        taskInstance.setTaskType("SWITCH");
        taskInstance.setProcessInstanceId(processInstance.getId());
        taskInstance.setTaskCode(1L);
        taskInstance.setTaskDefinitionVersion(1);
        return taskInstance;
    }
}
