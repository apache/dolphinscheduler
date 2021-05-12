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

package org.apache.dolphinscheduler.server.master.consumer;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.dao.datasource.SpringConnectionFactory;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.server.entity.DataxTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.ExecutorDispatcher;
import org.apache.dolphinscheduler.server.master.dispatch.executor.NettyExecutorManager;
import org.apache.dolphinscheduler.server.master.registry.MasterRegistry;
import org.apache.dolphinscheduler.server.master.registry.ServerNodeManager;
import org.apache.dolphinscheduler.server.master.zk.ZKMasterClient;
import org.apache.dolphinscheduler.server.registry.DependencyConfig;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.server.zk.SpringZKServer;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.TaskPriority;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueue;
import org.apache.dolphinscheduler.service.zk.CuratorZookeeperClient;
import org.apache.dolphinscheduler.service.zk.RegisterOperator;
import org.apache.dolphinscheduler.service.zk.ZookeeperConfig;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DependencyConfig.class, SpringApplicationContext.class, SpringZKServer.class, CuratorZookeeperClient.class,
        NettyExecutorManager.class, ExecutorDispatcher.class, ZookeeperRegistryCenter.class, ZKMasterClient.class, TaskPriorityQueueConsumer.class,
        ServerNodeManager.class, RegisterOperator.class, ZookeeperConfig.class, MasterConfig.class, MasterRegistry.class,
        CuratorZookeeperClient.class, SpringConnectionFactory.class})
public class TaskPriorityQueueConsumerTest {

    @Autowired
    private TaskPriorityQueue<TaskPriority> taskPriorityQueue;

    @Autowired
    private TaskPriorityQueueConsumer taskPriorityQueueConsumer;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ExecutorDispatcher dispatcher;

    @Before
    public void init() {

        Tenant tenant = new Tenant();
        tenant.setId(1);
        tenant.setTenantCode("journey");
        tenant.setDescription("journey");
        tenant.setQueueId(1);
        tenant.setCreateTime(new Date());
        tenant.setUpdateTime(new Date());

        Mockito.doReturn(tenant).when(processService).getTenantForProcess(1, 2);

        Mockito.doReturn("default").when(processService).queryUserQueueByProcessInstanceId(1);
    }

    @Test
    public void testSHELLTask() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType(TaskType.SHELL.getDesc());
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(ExecutionStatus.KILL);
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("default");
        taskInstance.setExecutorId(2);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setTenantId(1);
        processInstance.setCommandType(CommandType.START_PROCESS);
        taskInstance.setProcessInstance(processInstance);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setUserId(2);
        processDefinition.setProjectId(1);
        taskInstance.setProcessDefine(processDefinition);

        Mockito.doReturn(taskInstance).when(processService).getTaskInstanceDetailByTaskId(1);
        TaskPriority taskPriority = new TaskPriority(2, 1, 2, 1, "default");
        taskPriorityQueue.put(taskPriority);

        TimeUnit.SECONDS.sleep(10);

        Assert.assertNotNull(taskInstance);
    }

    @Test
    public void testSQLTask() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType(TaskType.SQL.getDesc());
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(ExecutionStatus.KILL);
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("default");
        taskInstance.setExecutorId(2);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setTenantId(1);
        processInstance.setCommandType(CommandType.START_PROCESS);
        taskInstance.setProcessInstance(processInstance);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setUserId(2);
        processDefinition.setProjectId(1);
        taskInstance.setProcessDefine(processDefinition);
        Mockito.doReturn(taskInstance).when(processService).getTaskInstanceDetailByTaskId(1);
        TaskPriority taskPriority = new TaskPriority(2, 1, 2, 1, "default");
        taskPriorityQueue.put(taskPriority);

        DataSource dataSource = new DataSource();
        dataSource.setId(1);
        dataSource.setName("sqlDatasource");
        dataSource.setType(DbType.MYSQL);
        dataSource.setUserId(2);
        dataSource.setConnectionParams("{\"address\":\"jdbc:mysql://192.168.221.185:3306\","
                + "\"database\":\"dolphinscheduler_qiaozhanwei\","
                + "\"jdbcUrl\":\"jdbc:mysql://192.168.221.185:3306/dolphinscheduler_qiaozhanwei\","
                + "\"user\":\"root\","
                + "\"password\":\"root@123\"}");
        dataSource.setCreateTime(new Date());
        dataSource.setUpdateTime(new Date());

        Mockito.doReturn(dataSource).when(processService).findDataSourceById(1);

        TimeUnit.SECONDS.sleep(10);
        Assert.assertNotNull(taskInstance);
    }

    @Test
    public void testDataxTask() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType(TaskType.DATAX.getDesc());
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(ExecutionStatus.KILL);
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("default");
        taskInstance.setExecutorId(2);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setTenantId(1);
        processInstance.setCommandType(CommandType.START_PROCESS);
        taskInstance.setProcessInstance(processInstance);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setUserId(2);
        processDefinition.setProjectId(1);
        taskInstance.setProcessDefine(processDefinition);
        Mockito.doReturn(taskInstance).when(processService).getTaskInstanceDetailByTaskId(1);
        TaskPriority taskPriority = new TaskPriority(2, 1, 2, 1, "default");
        taskPriorityQueue.put(taskPriority);

        DataSource dataSource = new DataSource();
        dataSource.setId(80);
        dataSource.setName("datax");
        dataSource.setType(DbType.MYSQL);
        dataSource.setUserId(2);
        dataSource.setConnectionParams("{\"address\":\"jdbc:mysql://192.168.221.185:3306\","
                + "\"database\":\"dolphinscheduler_qiaozhanwei\","
                + "\"jdbcUrl\":\"jdbc:mysql://192.168.221.185:3306/dolphinscheduler_qiaozhanwei\","
                + "\"user\":\"root\","
                + "\"password\":\"root@123\"}");
        dataSource.setCreateTime(new Date());
        dataSource.setUpdateTime(new Date());
        Mockito.doReturn(dataSource).when(processService).findDataSourceById(80);
        TimeUnit.SECONDS.sleep(10);
        Assert.assertNotNull(taskInstance);
    }

    @Test
    public void testSqoopTask() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType(TaskType.SQOOP.getDesc());
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(ExecutionStatus.KILL);
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("default");
        taskInstance.setExecutorId(2);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setTenantId(1);
        processInstance.setCommandType(CommandType.START_PROCESS);
        taskInstance.setProcessInstance(processInstance);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setUserId(2);
        processDefinition.setProjectId(1);
        taskInstance.setProcessDefine(processDefinition);
        Mockito.doReturn(taskInstance).when(processService).getTaskInstanceDetailByTaskId(1);
        TaskPriority taskPriority = new TaskPriority(2, 1, 2, 1, "default");
        taskPriorityQueue.put(taskPriority);

        DataSource dataSource = new DataSource();
        dataSource.setId(1);
        dataSource.setName("datax");
        dataSource.setType(DbType.MYSQL);
        dataSource.setUserId(2);
        dataSource.setConnectionParams("{\"address\":\"jdbc:mysql://192.168.221.185:3306\","
                + "\"database\":\"dolphinscheduler_qiaozhanwei\","
                + "\"jdbcUrl\":\"jdbc:mysql://192.168.221.185:3306/dolphinscheduler_qiaozhanwei\","
                + "\"user\":\"root\","
                + "\"password\":\"root@123\"}");
        dataSource.setCreateTime(new Date());
        dataSource.setUpdateTime(new Date());
        Mockito.doReturn(dataSource).when(processService).findDataSourceById(1);
        TimeUnit.SECONDS.sleep(10);
        Assert.assertNotNull(taskInstance);
    }

    @Test
    public void testTaskInstanceIsFinalState() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType(TaskType.SHELL.getDesc());
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(ExecutionStatus.KILL);
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("default");
        taskInstance.setExecutorId(2);

        Mockito.doReturn(taskInstance).when(processService).findTaskInstanceById(1);

        Boolean state = taskPriorityQueueConsumer.taskInstanceIsFinalState(1);
        Assert.assertNotNull(state);
    }

    @Test
    public void testNotFoundWorkerGroup() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType(TaskType.SHELL.getDesc());
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(ExecutionStatus.KILL);
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("NoWorkGroup");
        taskInstance.setExecutorId(2);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setTenantId(1);
        processInstance.setCommandType(CommandType.START_PROCESS);
        taskInstance.setProcessInstance(processInstance);
        taskInstance.setState(ExecutionStatus.DELAY_EXECUTION);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setUserId(2);
        processDefinition.setProjectId(1);
        taskInstance.setProcessDefine(processDefinition);

        Mockito.doReturn(taskInstance).when(processService).getTaskInstanceDetailByTaskId(1);
        Mockito.doReturn(taskInstance).when(processService).findTaskInstanceById(1);

        TaskPriority taskPriority = new TaskPriority(2, 1, 2, 1, "NoWorkGroup");
        taskPriorityQueue.put(taskPriority);

        TimeUnit.SECONDS.sleep(10);

        Assert.assertNotNull(taskInstance);

    }

    @Test
    public void testDispatch() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType(TaskType.SHELL.getDesc());
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(ExecutionStatus.KILL);
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("NoWorkGroup");
        taskInstance.setExecutorId(2);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setTenantId(1);
        processInstance.setCommandType(CommandType.START_PROCESS);
        taskInstance.setProcessInstance(processInstance);
        taskInstance.setState(ExecutionStatus.DELAY_EXECUTION);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setUserId(2);
        processDefinition.setProjectCode(1L);
        taskInstance.setProcessDefine(processDefinition);

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setTimeoutFlag(TimeoutFlag.OPEN);
        taskInstance.setTaskDefine(taskDefinition);

        Mockito.doReturn(taskInstance).when(processService).getTaskInstanceDetailByTaskId(1);
        Mockito.doReturn(taskInstance).when(processService).findTaskInstanceById(1);

        TaskPriority taskPriority = new TaskPriority();
        taskPriority.setTaskId(1);
        boolean res = taskPriorityQueueConsumer.dispatch(taskPriority);

        Assert.assertFalse(res);
    }

    @Test
    public void testGetTaskExecutionContext() throws Exception {

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType(TaskType.SHELL.getDesc());
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(ExecutionStatus.KILL);
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("NoWorkGroup");
        taskInstance.setExecutorId(2);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setTenantId(1);
        processInstance.setCommandType(CommandType.START_PROCESS);
        taskInstance.setProcessInstance(processInstance);
        taskInstance.setState(ExecutionStatus.DELAY_EXECUTION);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setUserId(2);
        processDefinition.setProjectCode(1L);
        taskInstance.setProcessDefine(processDefinition);

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setTimeoutFlag(TimeoutFlag.OPEN);
        taskInstance.setTaskDefine(taskDefinition);

        Mockito.doReturn(taskInstance).when(processService).getTaskInstanceDetailByTaskId(1);
        Mockito.doReturn(taskInstance).when(processService).findTaskInstanceById(1);

        TaskExecutionContext taskExecutionContext = taskPriorityQueueConsumer.getTaskExecutionContext(1);

        Assert.assertNotNull(taskExecutionContext);
    }

    @Test
    public void testGetResourceFullNames() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType(TaskType.SHELL.getDesc());
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(ExecutionStatus.KILL);
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("NoWorkGroup");
        taskInstance.setExecutorId(2);
        // task node

        Map<String, String> map = taskPriorityQueueConsumer.getResourceFullNames(taskInstance);

        List<Resource> resourcesList = new ArrayList<Resource>();
        Resource resource = new Resource();
        resource.setFileName("fileName");
        resourcesList.add(resource);

        Mockito.doReturn(resourcesList).when(processService).listResourceByIds(new Integer[]{123});
        Mockito.doReturn("tenantCode").when(processService).queryTenantCodeByResName(resource.getFullName(), ResourceType.FILE);
        Assert.assertNotNull(map);

    }

    @Test
    public void testVerifyTenantIsNull() {
        Tenant tenant = null;

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType(TaskType.SHELL.getDesc());
        taskInstance.setProcessInstanceId(1);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        taskInstance.setProcessInstance(processInstance);

        boolean res = taskPriorityQueueConsumer.verifyTenantIsNull(tenant, taskInstance);
        Assert.assertTrue(res);

        tenant = new Tenant();
        tenant.setId(1);
        tenant.setTenantCode("journey");
        tenant.setDescription("journey");
        tenant.setQueueId(1);
        tenant.setCreateTime(new Date());
        tenant.setUpdateTime(new Date());
        res = taskPriorityQueueConsumer.verifyTenantIsNull(tenant, taskInstance);
        Assert.assertFalse(res);

    }

    @Test
    public void testSetDataxTaskRelation() throws Exception {

        DataxTaskExecutionContext dataxTaskExecutionContext = new DataxTaskExecutionContext();
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskParams("{\"dataSource\":1,\"dataTarget\":1}");
        DataSource dataSource = new DataSource();
        dataSource.setId(1);
        dataSource.setConnectionParams("");
        dataSource.setType(DbType.MYSQL);
        Mockito.doReturn(dataSource).when(processService).findDataSourceById(1);

        taskPriorityQueueConsumer.setDataxTaskRelation(dataxTaskExecutionContext, taskInstance);

        Assert.assertEquals(1, dataxTaskExecutionContext.getDataSourceId());
        Assert.assertEquals(1, dataxTaskExecutionContext.getDataTargetId());
    }

    @Test
    public void testRun() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType(TaskType.SHELL.getDesc());
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(ExecutionStatus.KILL);
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("NoWorkGroup");
        taskInstance.setExecutorId(2);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setTenantId(1);
        processInstance.setCommandType(CommandType.START_PROCESS);
        taskInstance.setProcessInstance(processInstance);
        taskInstance.setState(ExecutionStatus.DELAY_EXECUTION);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setUserId(2);
        processDefinition.setProjectId(1);
        taskInstance.setProcessDefine(processDefinition);

        Mockito.doReturn(taskInstance).when(processService).getTaskInstanceDetailByTaskId(1);
        Mockito.doReturn(taskInstance).when(processService).findTaskInstanceById(1);

        TaskPriority taskPriority = new TaskPriority(2, 1, 2, 1, "NoWorkGroup");
        taskPriorityQueue.put(taskPriority);

        taskPriorityQueueConsumer.run();

        TimeUnit.SECONDS.sleep(10);
        Assert.assertNotEquals(-1, taskPriorityQueue.size());

    }

    @After
    public void close() {
        Stopper.stop();
    }

}
