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
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.dispatch.ExecutorDispatcher;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.TaskPriority;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueue;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Disabled
public class TaskPriorityQueueConsumerTest {

    @Autowired
    private TaskPriorityQueue<TaskPriority> taskPriorityQueue;

    @Autowired
    private TaskPriorityQueueConsumer taskPriorityQueueConsumer;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ExecutorDispatcher dispatcher;

    @BeforeEach
    public void init() {

        Tenant tenant = new Tenant();
        tenant.setId(1);
        tenant.setTenantCode("journey");
        tenant.setDescription("journey");
        tenant.setQueueId(1);
        tenant.setCreateTime(new Date());
        tenant.setUpdateTime(new Date());

        Mockito.doReturn(tenant).when(processService).getTenantForProcess(1, 2);
    }

    @Test
    public void testSHELLTask() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType("SHELL");
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(TaskExecutionStatus.KILL);
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
        taskInstance.setProcessDefine(processDefinition);

        TaskPriority taskPriority = new TaskPriority(2, 1, 2, 1, 1, "default");
        taskPriorityQueue.put(taskPriority);

        TimeUnit.SECONDS.sleep(10);

        Assertions.assertNotNull(taskInstance);
    }

    @Test
    public void testSQLTask() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType("SQL");
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(TaskExecutionStatus.KILL);
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("default");
        taskInstance.setExecutorId(2);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setTenantId(1);
        processInstance.setCommandType(CommandType.START_PROCESS);
        taskInstance.setProcessInstance(processInstance);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setUserId(2);
        taskInstance.setProcessDefine(processDefinition);
        TaskPriority taskPriority = new TaskPriority(2, 1, 2, 1, 1, "default");
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
        Assertions.assertNotNull(taskInstance);
    }

    @Test
    public void testDataxTask() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType("DATAX");
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(TaskExecutionStatus.KILL);
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("default");
        taskInstance.setExecutorId(2);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setTenantId(1);
        processInstance.setCommandType(CommandType.START_PROCESS);
        taskInstance.setProcessInstance(processInstance);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setUserId(2);
        taskInstance.setProcessDefine(processDefinition);
        TaskPriority taskPriority = new TaskPriority(2, 1, 2, 1, 1, "default");
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
        Assertions.assertNotNull(taskInstance);
    }

    @Test
    public void testSqoopTask() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType("SQOOP");
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(TaskExecutionStatus.KILL);
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("default");
        taskInstance.setExecutorId(2);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setTenantId(1);
        processInstance.setCommandType(CommandType.START_PROCESS);
        taskInstance.setProcessInstance(processInstance);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setUserId(2);
        taskInstance.setProcessDefine(processDefinition);
        TaskPriority taskPriority = new TaskPriority(2, 1, 2, 1, 1, "default");
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
        Assertions.assertNotNull(taskInstance);
    }

    @Test
    public void testTaskInstanceIsFinalState() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType("SHELL");
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(TaskExecutionStatus.KILL);
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("default");
        taskInstance.setExecutorId(2);

        Mockito.doReturn(taskInstance).when(processService).findTaskInstanceById(1);

        Boolean state = taskPriorityQueueConsumer.taskInstanceIsFinalState(1);
        Assertions.assertNotNull(state);
    }

    @Test
    public void testNotFoundWorkerGroup() throws Exception {
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
        taskInstance.setProcessDefine(processDefinition);

        Mockito.doReturn(taskInstance).when(processService).findTaskInstanceById(1);

        TaskPriority taskPriority = new TaskPriority(2, 1, 2, 1, 1, "NoWorkGroup");
        taskPriorityQueue.put(taskPriority);

        TimeUnit.SECONDS.sleep(10);

        Assertions.assertNotNull(taskInstance);

    }

    @Test
    public void testDispatch() {
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

        TaskPriority taskPriority = new TaskPriority();
        taskPriority.setTaskId(1);
        boolean res = taskPriorityQueueConsumer.dispatchTask(taskPriority);

        Assertions.assertFalse(res);
    }

    @Test
    public void testRun() throws Exception {
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
        taskInstance.setProcessDefine(processDefinition);

        Mockito.doReturn(taskInstance).when(processService).findTaskInstanceById(1);

        TaskPriority taskPriority = new TaskPriority(2, 1, 2, 1, 1, "NoWorkGroup");
        taskPriorityQueue.put(taskPriority);

        taskPriorityQueueConsumer.run();

        TimeUnit.SECONDS.sleep(10);
        Assertions.assertNotEquals(-1, taskPriorityQueue.size());

    }

    @AfterEach
    public void close() {
        ServerLifeCycleManager.toStopped();
    }

}
