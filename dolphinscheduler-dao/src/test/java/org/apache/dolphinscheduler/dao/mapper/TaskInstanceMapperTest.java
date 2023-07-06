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

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class TaskInstanceMapperTest extends BaseDaoTest {

    @Autowired
    private TaskInstanceMapper taskInstanceMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    /**
     * insert
     *
     * @return TaskInstance
     */
    private TaskInstance insertTaskInstance(int processInstanceId) {
        // insertOne
        return insertTaskInstance(processInstanceId, "SHELL");
    }

    /**
     * insert
     *
     * @return ProcessInstance
     */
    private ProcessInstance insertProcessInstance() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setName("taskName");
        processInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        processInstance.setStartTime(new Date());
        processInstance.setEndTime(new Date());
        processInstance.setProcessDefinitionCode(1L);
        processInstance.setTestFlag(0);
        processInstanceMapper.insert(processInstance);
        return processInstanceMapper.queryByProcessDefineCode(1L, 1).get(0);
    }

    /**
     * construct a task instance and then insert
     */
    private TaskInstance insertTaskInstance(int processInstanceId, String taskType) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setFlag(Flag.YES);
        taskInstance.setName("us task");
        taskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(new Date());
        taskInstance.setEndTime(new Date());
        taskInstance.setProcessInstanceId(processInstanceId);
        taskInstance.setTaskType(taskType);
        taskInstanceMapper.insert(taskInstance);
        return taskInstance;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {
        // insert ProcessInstance
        ProcessInstance processInstance = insertProcessInstance();

        // insert taskInstance
        TaskInstance taskInstance = insertTaskInstance(processInstance.getId());
        // update
        int update = taskInstanceMapper.updateById(taskInstance);
        Assertions.assertEquals(1, update);
        taskInstanceMapper.deleteById(taskInstance.getId());
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        // insert ProcessInstance
        ProcessInstance processInstance = insertProcessInstance();

        // insert taskInstance
        TaskInstance taskInstance = insertTaskInstance(processInstance.getId());

        int delete = taskInstanceMapper.deleteById(taskInstance.getId());
        Assertions.assertEquals(1, delete);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        // insert ProcessInstance
        ProcessInstance processInstance = insertProcessInstance();

        // insert taskInstance
        TaskInstance taskInstance = insertTaskInstance(processInstance.getId());
        // query
        List<TaskInstance> taskInstances = taskInstanceMapper.selectList(null);
        taskInstanceMapper.deleteById(taskInstance.getId());
        Assertions.assertNotEquals(0, taskInstances.size());
    }

    /**
     * test query task instance by process instance id and state
     */
    @Test
    public void testQueryTaskByProcessIdAndState() {
        // insert ProcessInstance
        ProcessInstance processInstance = insertProcessInstance();

        // insert taskInstance
        TaskInstance task = insertTaskInstance(processInstance.getId());
        task.setProcessInstanceId(processInstance.getId());
        taskInstanceMapper.updateById(task);
        List<Integer> taskInstances = taskInstanceMapper.queryTaskByProcessIdAndState(
                task.getProcessInstanceId(),
                TaskExecutionStatus.RUNNING_EXECUTION.getCode());
        taskInstanceMapper.deleteById(task.getId());
        Assertions.assertNotEquals(0, taskInstances.size());
    }

    /**
     * test find valid task list by process instance id
     */
    @Test
    public void testFindValidTaskListByProcessId() {
        // insert ProcessInstance
        ProcessInstance processInstance = insertProcessInstance();

        // insert taskInstance
        TaskInstance task = insertTaskInstance(processInstance.getId());
        TaskInstance task2 = insertTaskInstance(processInstance.getId());
        task.setProcessInstanceId(processInstance.getId());
        task2.setProcessInstanceId(processInstance.getId());
        taskInstanceMapper.updateById(task);
        taskInstanceMapper.updateById(task2);

        List<TaskInstance> taskInstances = taskInstanceMapper.findValidTaskListByProcessId(
                task.getProcessInstanceId(),
                Flag.YES,
                processInstance.getTestFlag());

        task2.setFlag(Flag.NO);
        taskInstanceMapper.updateById(task2);
        List<TaskInstance> taskInstances1 = taskInstanceMapper.findValidTaskListByProcessId(task.getProcessInstanceId(),
                Flag.NO,
                processInstance.getTestFlag());
        taskInstanceMapper.deleteById(task2.getId());
        taskInstanceMapper.deleteById(task.getId());
        Assertions.assertNotEquals(0, taskInstances.size());
        Assertions.assertNotEquals(0, taskInstances1.size());
    }

    /**
     * test query by host and status
     */
    @Test
    public void testQueryByHostAndStatus() {
        // insert ProcessInstance
        ProcessInstance processInstance = insertProcessInstance();

        // insert taskInstance
        TaskInstance task = insertTaskInstance(processInstance.getId());
        task.setHost("111.111.11.11");
        taskInstanceMapper.updateById(task);

        List<TaskInstance> taskInstances = taskInstanceMapper.queryByHostAndStatus(
                task.getHost(), new int[]{TaskExecutionStatus.RUNNING_EXECUTION.getCode()});
        taskInstanceMapper.deleteById(task.getId());
        Assertions.assertNotEquals(0, taskInstances.size());
    }

    /**
     * test set failover by host and state array
     */
    @Test
    public void testSetFailoverByHostAndStateArray() {
        // insert ProcessInstance
        ProcessInstance processInstance = insertProcessInstance();

        // insert taskInstance
        TaskInstance task = insertTaskInstance(processInstance.getId());
        task.setHost("111.111.11.11");
        taskInstanceMapper.updateById(task);

        int setResult = taskInstanceMapper.setFailoverByHostAndStateArray(
                task.getHost(),
                new int[]{TaskExecutionStatus.RUNNING_EXECUTION.getCode()},
                TaskExecutionStatus.NEED_FAULT_TOLERANCE);
        taskInstanceMapper.deleteById(task.getId());
        Assertions.assertNotEquals(0, setResult);
    }

    /**
     * test query by task instance id and name
     */
    @Test
    public void testQueryByInstanceIdAndName() {
        // insert ProcessInstance
        ProcessInstance processInstance = insertProcessInstance();

        // insert taskInstance
        TaskInstance task = insertTaskInstance(processInstance.getId());
        task.setHost("111.111.11.11");
        taskInstanceMapper.updateById(task);

        TaskInstance taskInstance = taskInstanceMapper.queryByInstanceIdAndName(
                task.getProcessInstanceId(),
                task.getName());
        taskInstanceMapper.deleteById(task.getId());
        Assertions.assertNotEquals(null, taskInstance);
    }

    /**
     * test query by task instance id and code
     */
    @Test
    public void testQueryByInstanceIdAndCode() {
        // insert ProcessInstance
        ProcessInstance processInstance = insertProcessInstance();

        // insert taskInstance
        TaskInstance task = insertTaskInstance(processInstance.getId());
        task.setHost("111.111.11.11");
        taskInstanceMapper.updateById(task);

        TaskInstance taskInstance = taskInstanceMapper.queryByInstanceIdAndCode(
                task.getProcessInstanceId(),
                task.getTaskCode());
        taskInstanceMapper.deleteById(task.getId());
        Assertions.assertNotEquals(null, taskInstance);
    }

    /**
     * test query by process instance ids and task codes
     */
    @Test
    public void testQueryByProcessInstanceIdsAndTaskCodes() {
        // insert ProcessInstance
        ProcessInstance processInstance = insertProcessInstance();

        // insert taskInstance
        TaskInstance task = insertTaskInstance(processInstance.getId());
        task.setHost("111.111.11.11");
        taskInstanceMapper.updateById(task);

        List<TaskInstance> taskInstances = taskInstanceMapper.queryByProcessInstanceIdsAndTaskCodes(
                Collections.singletonList(task.getProcessInstanceId()),
                Collections.singletonList(task.getTaskCode()));
        taskInstanceMapper.deleteById(task.getId());
        Assertions.assertEquals(1, taskInstances.size());
    }

    /**
     * test count task instance
     */
    @Test
    public void testCountTask() {
        // insert ProcessInstance
        ProcessInstance processInstance = insertProcessInstance();

        // insert taskInstance
        TaskInstance task = insertTaskInstance(processInstance.getId());
        ProcessDefinition definition = new ProcessDefinition();
        definition.setCode(1L);
        definition.setProjectCode(1111L);
        definition.setCreateTime(new Date());
        definition.setUpdateTime(new Date());
        processDefinitionMapper.insert(definition);
        taskInstanceMapper.updateById(task);

        int countTask = taskInstanceMapper.countTask(
                new Long[0],
                new int[0]);
        int countTask2 = taskInstanceMapper.countTask(
                new Long[]{definition.getProjectCode()},
                new int[]{task.getId()});
        taskInstanceMapper.deleteById(task.getId());
        processDefinitionMapper.deleteById(definition.getId());
        Assertions.assertEquals(0, countTask);
        Assertions.assertEquals(0, countTask2);

    }

    /**
     * test count task instance state by user
     */
    @Test
    public void testCountTaskInstanceStateByUser() {

        // insert ProcessInstance
        ProcessInstance processInstance = insertProcessInstance();

        // insert taskInstance
        TaskInstance task = insertTaskInstance(processInstance.getId());
        ProcessDefinition definition = new ProcessDefinition();
        definition.setCode(1111L);
        definition.setProjectCode(1111L);
        definition.setCreateTime(new Date());
        definition.setUpdateTime(new Date());
        processDefinitionMapper.insert(definition);
        taskInstanceMapper.updateById(task);

        List<ExecuteStatusCount> count = taskInstanceMapper.countTaskInstanceStateByProjectCodes(
                null, null,
                new Long[]{definition.getProjectCode()});

        processDefinitionMapper.deleteById(definition.getId());
        taskInstanceMapper.deleteById(task.getId());
    }

    /**
     * test page
     */
    @Test
    public void testQueryTaskInstanceListPaging() {
        ProcessDefinition definition = new ProcessDefinition();
        definition.setCode(1L);
        definition.setProjectCode(1111L);
        definition.setCreateTime(new Date());
        definition.setUpdateTime(new Date());
        processDefinitionMapper.insert(definition);

        // insert ProcessInstance
        ProcessInstance processInstance = insertProcessInstance();

        // insert taskInstance
        TaskInstance task = insertTaskInstance(processInstance.getId());

        Page<TaskInstance> page = new Page(1, 3);
        IPage<TaskInstance> taskInstanceIPage = taskInstanceMapper.queryTaskInstanceListPaging(
                page,
                definition.getProjectCode(),
                task.getProcessInstanceId(),
                "",
                "",
                "",
                "",
                new int[0],
                "",
                TaskExecuteType.BATCH,
                null, null);
        processInstanceMapper.deleteById(processInstance.getId());
        taskInstanceMapper.deleteById(task.getId());
        processDefinitionMapper.deleteById(definition.getId());
        Assertions.assertEquals(0, taskInstanceIPage.getTotal());

    }
}
