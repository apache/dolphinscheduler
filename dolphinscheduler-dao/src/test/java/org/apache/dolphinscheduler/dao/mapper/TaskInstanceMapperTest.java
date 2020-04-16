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
package org.apache.dolphinscheduler.dao.mapper;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class TaskInstanceMapperTest {


    @Autowired
    TaskInstanceMapper taskInstanceMapper;

    @Autowired
    ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    ProcessInstanceMapper processInstanceMapper;

    /**
     * insert
     * @return TaskInstance
     */
    private TaskInstance insertOne(){
        //insertOne
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setFlag(Flag.YES);
        taskInstance.setName("ut task");
        taskInstance.setState(ExecutionStatus.RUNNING_EXEUTION);
        taskInstance.setStartTime(new Date());
        taskInstance.setEndTime(new Date());
        taskInstance.setTaskJson("{}");
        taskInstance.setTaskType(TaskType.SHELL.toString());
        taskInstanceMapper.insert(taskInstance);
        return taskInstance;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        TaskInstance taskInstance = insertOne();
        //update
        int update = taskInstanceMapper.updateById(taskInstance);
        Assert.assertEquals(1, update);
        taskInstanceMapper.deleteById(taskInstance.getId());
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        TaskInstance taskInstance = insertOne();
        int delete = taskInstanceMapper.deleteById(taskInstance.getId());
        Assert.assertEquals(1, delete);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        TaskInstance taskInstance = insertOne();
        //query
        List<TaskInstance> taskInstances = taskInstanceMapper.selectList(null);
        taskInstanceMapper.deleteById(taskInstance.getId());
        Assert.assertNotEquals(taskInstances.size(), 0);
    }

    /**
     * test query task instance by process instance id and state
     */
    @Test
    public void testQueryTaskByProcessIdAndState() {
        TaskInstance task = insertOne();
        task.setProcessInstanceId(110);
        taskInstanceMapper.updateById(task);
        List<Integer> taskInstances = taskInstanceMapper.queryTaskByProcessIdAndState(
                task.getProcessInstanceId(),
                ExecutionStatus.RUNNING_EXEUTION.ordinal()
        );
        taskInstanceMapper.deleteById(task.getId());
        Assert.assertNotEquals(taskInstances.size(), 0);
    }

    /**
     * test find valid task list by process instance id
     */
    @Test
    public void testFindValidTaskListByProcessId() {
        TaskInstance task = insertOne();
        TaskInstance task2 = insertOne();
        task.setProcessInstanceId(110);
        task2.setProcessInstanceId(110);
        taskInstanceMapper.updateById(task);
        taskInstanceMapper.updateById(task2);

        List<TaskInstance> taskInstances = taskInstanceMapper.findValidTaskListByProcessId(
                task.getProcessInstanceId(),
                Flag.YES
        );

        task2.setFlag(Flag.NO);
        taskInstanceMapper.updateById(task2);
        List<TaskInstance> taskInstances1 = taskInstanceMapper.findValidTaskListByProcessId(task.getProcessInstanceId(),
                Flag.NO);

        taskInstanceMapper.deleteById(task2.getId());
        taskInstanceMapper.deleteById(task.getId());
        Assert.assertNotEquals(taskInstances.size(), 0);
        Assert.assertNotEquals(taskInstances1.size(), 0 );
    }

    /**
     * test query by host and status
     */
    @Test
    public void testQueryByHostAndStatus() {
        TaskInstance task = insertOne();
        task.setHost("111.111.11.11");
        taskInstanceMapper.updateById(task);

        List<TaskInstance> taskInstances = taskInstanceMapper.queryByHostAndStatus(
                task.getHost(), new int[]{ExecutionStatus.RUNNING_EXEUTION.ordinal()}
        );
        taskInstanceMapper.deleteById(task.getId());
        Assert.assertNotEquals(taskInstances.size(), 0);
    }

    /**
     * test set failover by host and state array
     */
    @Test
    public void testSetFailoverByHostAndStateArray() {
        TaskInstance task = insertOne();
        task.setHost("111.111.11.11");
        taskInstanceMapper.updateById(task);

        int setResult = taskInstanceMapper.setFailoverByHostAndStateArray(
                task.getHost(),
                new int[]{ExecutionStatus.RUNNING_EXEUTION.ordinal()},
                ExecutionStatus.NEED_FAULT_TOLERANCE
        );
        taskInstanceMapper.deleteById(task.getId());
        Assert.assertNotEquals(setResult, 0);
    }

    /**
     * test query by task instance id and name
     */
    @Test
    public void testQueryByInstanceIdAndName() {
        TaskInstance task = insertOne();
        task.setHost("111.111.11.11");
        taskInstanceMapper.updateById(task);

        TaskInstance taskInstance = taskInstanceMapper.queryByInstanceIdAndName(
                task.getProcessInstanceId(),
                task.getName()
        );
        taskInstanceMapper.deleteById(task.getId());
        Assert.assertNotEquals(taskInstance, null);
    }

    /**
     * test count task instance
     */
    @Test
    public void testCountTask() {
        TaskInstance task = insertOne();

        ProcessDefinition definition = new ProcessDefinition();
        definition.setProjectId(1111);
        processDefinitionMapper.insert(definition);
        task.setProcessDefinitionId(definition.getId());
        taskInstanceMapper.updateById(task);

        int countTask = taskInstanceMapper.countTask(
                new Integer[0],
                new int[0]
        );
        int countTask2 = taskInstanceMapper.countTask(
                new Integer[]{definition.getProjectId()},
                new int[]{task.getId()}
        );
        taskInstanceMapper.deleteById(task.getId());
        processDefinitionMapper.deleteById(definition.getId());
        Assert.assertNotEquals(countTask, 0);
        Assert.assertNotEquals(countTask2, 0);


    }

    /**
     * test count task instance state by user
     */
    @Test
    public void testCountTaskInstanceStateByUser() {

        TaskInstance task = insertOne();
        ProcessDefinition definition = new ProcessDefinition();
        definition.setProjectId(1111);
        processDefinitionMapper.insert(definition);
        task.setProcessDefinitionId(definition.getId());
        taskInstanceMapper.updateById(task);


        List<ExecuteStatusCount> count = taskInstanceMapper.countTaskInstanceStateByUser(
                null, null,
                new Integer[]{definition.getProjectId()}
        );

        processDefinitionMapper.deleteById(definition.getId());
        taskInstanceMapper.deleteById(task.getId());
    }

    /**
     * test page
     */
    @Test
    public void testQueryTaskInstanceListPaging() {
        TaskInstance task = insertOne();

        ProcessDefinition definition = new ProcessDefinition();
        definition.setProjectId(1111);
        processDefinitionMapper.insert(definition);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setProcessDefinitionId(definition.getId());
        processInstance.setState(ExecutionStatus.RUNNING_EXEUTION);
        processInstance.setName("ut process");
        processInstance.setStartTime(new Date());
        processInstance.setEndTime(new Date());
        processInstance.setCommandType(CommandType.START_PROCESS);
        processInstanceMapper.insert(processInstance);

        task.setProcessDefinitionId(definition.getId());
        task.setProcessInstanceId(processInstance.getId());
        taskInstanceMapper.updateById(task);

        Page<TaskInstance> page = new Page(1,3);
        IPage<TaskInstance> taskInstanceIPage = taskInstanceMapper.queryTaskInstanceListPaging(
                page,
                definition.getProjectId(),
                task.getProcessInstanceId(),
                "",
                "",
                0,
                new int[0],
                "",
                null,null
        );
        processInstanceMapper.deleteById(processInstance.getId());
        taskInstanceMapper.deleteById(task.getId());
        processDefinitionMapper.deleteById(definition.getId());
        Assert.assertNotEquals(taskInstanceIPage.getTotal(), 0);

    }
}