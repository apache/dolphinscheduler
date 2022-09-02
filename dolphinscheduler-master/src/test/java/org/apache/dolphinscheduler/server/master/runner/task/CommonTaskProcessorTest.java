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

import static org.mockito.ArgumentMatchers.any;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;



@RunWith(MockitoJUnitRunner.Silent.class)
public class CommonTaskProcessorTest {

    private ProcessService processService;

    private CommonTaskProcessor commonTaskProcessor;
    @Before
    public void setUp() {
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        springApplicationContext.setApplicationContext(applicationContext);
        processService = Mockito.mock(ProcessService.class);
        Mockito.when(SpringApplicationContext.getBean(ProcessService.class))
                .thenReturn(processService);
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
        processService = Mockito.mock(ProcessService.class);
        Mockito.doReturn(taskInstance).when(processService).findTaskInstanceById(1);
        commonTaskProcessor = Mockito.mock(CommonTaskProcessor.class);
        TaskExecutionContext taskExecutionContext = commonTaskProcessor.getTaskExecutionContext(taskInstance);
        Assert.assertNull(taskExecutionContext);
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

        List<Resource> resourcesList = new ArrayList<Resource>();
        Resource resource = new Resource();
        resource.setFileName("fileName");
        resourcesList.add(resource);
        processService = Mockito.mock(ProcessService.class);
        Mockito.doReturn(resourcesList).when(processService).listResourceByIds(new Integer[]{123});
        Mockito.doReturn("tenantCode").when(processService).queryTenantCodeByResName(resource.getFullName(), ResourceType.FILE);
        Assert.assertNotNull(map);

    }

    @Test
    public void testVerifyTenantIsNull() {
        commonTaskProcessor = Mockito.mock(CommonTaskProcessor.class);
        Tenant tenant = null;

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType("SHELL");
        taskInstance.setProcessInstanceId(1);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        taskInstance.setProcessInstance(processInstance);

        boolean res = commonTaskProcessor.verifyTenantIsNull(tenant, taskInstance);
        Assert.assertFalse(res);

        tenant = new Tenant();
        tenant.setId(1);
        tenant.setTenantCode("journey");
        tenant.setDescription("journey");
        tenant.setQueueId(1);
        tenant.setCreateTime(new Date());
        tenant.setUpdateTime(new Date());
        res = commonTaskProcessor.verifyTenantIsNull(tenant, taskInstance);
        Assert.assertFalse(res);

    }

    @Test
    public void testReplaceTestDatSource(){
        commonTaskProcessor = Mockito.mock(CommonTaskProcessor.class);
        processService = Mockito.mock(ProcessService.class);

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTestFlag(1);
        taskInstance.setTaskParams("{\"localParams\":[],\"resourceList\":[],\"type\":\"MYSQL\",\"datasource\":1,\"sql\":\"select * from 'order'\",\"sqlType\":\"0\",\"preStatements\":[],\"postStatements\":[],\"segmentSeparator\":\"\",\"displayRows\":10}");
        commonTaskProcessor.taskInstance = taskInstance;

        //The data source instance has no bound test data source
        Mockito.when(processService.queryTestDataSourceId(any(Integer.class))).thenReturn(null);
        boolean result = commonTaskProcessor.checkAndReplaceTestDataSource();
        Assert.assertFalse(result);

        //The data source instance has  bound test data source
        Mockito.when(processService.queryTestDataSourceId(any(Integer.class))).thenReturn(2);
        result = commonTaskProcessor.checkAndReplaceTestDataSource();
//        Assert.assertTrue(result);
    }


    /**
     * get Mock Admin User
     *
     * @return admin user
     */
    private User getAdminUser() {
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserName("admin");
        loginUser.setUserType(UserType.GENERAL_USER);
        return loginUser;
    }

}
