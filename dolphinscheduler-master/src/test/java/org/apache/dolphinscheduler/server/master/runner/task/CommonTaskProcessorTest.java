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
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@Ignore
public class CommonTaskProcessorTest {

    @Autowired
    private CommonTaskProcessor commonTaskProcessor;

    @Autowired
    private ProcessService processService;

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
        Assert.assertNotNull(taskExecutionContext);
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

        Map<String, String> map = commonTaskProcessor.getResourceFullNames(taskInstance);

        List<Resource> resourcesList = new ArrayList<Resource>();
        Resource resource = new Resource();
        resource.setFileName("fileName");
        resourcesList.add(resource);

        Mockito.doReturn(resourcesList).when(processService).listResourceByIds(new Integer[]{123});
        Mockito.doReturn("tenantCode").when(processService).queryTenantCodeByResName(resource.getFullName(),
                ResourceType.FILE);
        Assert.assertNotNull(map);

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
        Assert.assertTrue(res);

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

}
