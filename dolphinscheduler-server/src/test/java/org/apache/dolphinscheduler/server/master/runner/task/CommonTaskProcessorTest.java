package org.apache.dolphinscheduler.server.master.runner.task;

import org.apache.dolphinscheduler.common.enums.*;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.task.request.DataxTaskExecutionContext;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

        TaskExecutionContext taskExecutionContext = commonTaskProcessor.getTaskExecutionContext(taskInstance);
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

        Map<String, String> map = commonTaskProcessor.getResourceFullNames(taskInstance);

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

        commonTaskProcessor.setDataxTaskRelation(dataxTaskExecutionContext, taskInstance);

        Assert.assertEquals(1, dataxTaskExecutionContext.getDataSourceId());
        Assert.assertEquals(1, dataxTaskExecutionContext.getDataTargetId());
    }
}
