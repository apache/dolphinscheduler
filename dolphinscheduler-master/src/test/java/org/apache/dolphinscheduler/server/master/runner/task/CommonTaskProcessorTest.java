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

package org.apache.dolphinscheduler.server.master.runner.task;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.spi.enums.ResourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CommonTaskProcessorTest {

    private ProcessService processService;

    private CommonTaskProcessor commonTaskProcessor;

    @BeforeEach
    public void setUp() {
        // init spring context
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);

        // mock process service
        processService = Mockito.mock(ProcessService.class);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);

        commonTaskProcessor = Mockito.mock(CommonTaskProcessor.class);
        Mockito.when(applicationContext.getBean(CommonTaskProcessor.class)).thenReturn(commonTaskProcessor);

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setTimeoutFlag(TimeoutFlag.OPEN);
        taskDefinition.setTimeoutNotifyStrategy(TaskTimeoutStrategy.WARN);
        taskDefinition.setTimeout(0);
        Mockito.when(processService.findTaskDefinition(1L, 1))
                .thenReturn(taskDefinition);
    }

    @Test
    public void testGetTaskExecutionContext() throws Exception {

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType("SHELL");
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(TaskExecutionStatus.KILL);
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("NoWorkGroup");
        taskInstance.setExecutorId(2);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setTenantId(1);
        processInstance.setCommandType(CommandType.START_PROCESS);
        taskInstance.setProcessInstance(processInstance);
        taskInstance.setState(TaskExecutionStatus.DELAY_EXECUTION);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setUserId(2);
        processDefinition.setProjectCode(1L);
        taskInstance.setProcessDefine(processDefinition);

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setTimeoutFlag(TimeoutFlag.OPEN);
        taskInstance.setTaskDefine(taskDefinition);
        Mockito.doReturn(taskInstance).when(processService).findTaskInstanceById(1);
        TaskExecutionContext taskExecutionContext = commonTaskProcessor.getTaskExecutionContext(taskInstance);
        Assertions.assertNull(taskExecutionContext);
    }

    @Test
    public void testGetResourceFullNames() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType("SHELL");
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(TaskExecutionStatus.KILL);
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("NoWorkGroup");
        taskInstance.setExecutorId(2);
        // task node
        commonTaskProcessor = Mockito.mock(CommonTaskProcessor.class);
        Map<String, String> map = commonTaskProcessor.getResourceFullNames(taskInstance);

        List<Resource> resourcesList = new ArrayList<>();
        Resource resource = new Resource();
        resource.setFileName("fileName");
        resourcesList.add(resource);
        Mockito.doReturn(resourcesList).when(processService).listResourceByIds(new Integer[]{123});
        Mockito.doReturn("tenantCode").when(processService).queryTenantCodeByResName(resource.getFullName(), ResourceType.FILE);
        Assertions.assertNotNull(map);

    }

    @Test
    public void testVerifyTenantIsNull() {
        Tenant tenant = null;

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType("SHELL");
        taskInstance.setProcessInstanceId(1);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        taskInstance.setProcessInstance(processInstance);

        boolean res = commonTaskProcessor.verifyTenantIsNull(tenant, taskInstance);
        Assertions.assertFalse(res);

        tenant = new Tenant();
        tenant.setId(1);
        tenant.setTenantCode("journey");
        tenant.setDescription("journey");
        tenant.setQueueId(1);
        tenant.setCreateTime(new Date());
        tenant.setUpdateTime(new Date());
        res = commonTaskProcessor.verifyTenantIsNull(tenant, taskInstance);
        Assertions.assertFalse(res);

    }

    @Test
    public void testReplaceTestDatSource() {
        CommonTaskProcessor commonTaskProcessor1 = new CommonTaskProcessor();
        commonTaskProcessor1.processService = processService;
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTestFlag(1);
        taskInstance.setTaskType("SQL");
        taskInstance.setTaskParams("{\"localParams\":[],\"resourceList\":[],\"type\":\"MYSQL\",\"datasource\":1,\"sql\":\"select * from 'order'\",\"sqlType\":\"0\",\"preStatements\":[],\"postStatements\":[],\"segmentSeparator\":\"\",\"displayRows\":10}");
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setTaskParams("{\"localParams\":[],\"resourceList\":[],\"type\":\"MYSQL\",\"datasource\":1,\"sql\":\"select * from 'order'\",\"sqlType\":\"0\",\"preStatements\":[],\"postStatements\":[],\"segmentSeparator\":\"\",\"displayRows\":10}");
        taskInstance.setTaskDefine(taskDefinition);
        commonTaskProcessor1.taskInstance = taskInstance;

        //The data source instance has no bound test data source
        Mockito.when(processService.queryTestDataSourceId(any(Integer.class))).thenReturn(null);
        commonTaskProcessor1.convertExeEnvironmentOnlineToTest();

        //The data source instance has  bound test data source
        Mockito.when(processService.queryTestDataSourceId(any(Integer.class))).thenReturn(2);
        commonTaskProcessor1.convertExeEnvironmentOnlineToTest();
    }
}
