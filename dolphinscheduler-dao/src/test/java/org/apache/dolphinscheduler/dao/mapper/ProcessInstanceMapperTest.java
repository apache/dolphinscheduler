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


import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
public class ProcessInstanceMapperTest {


    @Autowired
    ProcessInstanceMapper processInstanceMapper;

    @Autowired
    ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    ProjectMapper projectMapper;


    /**
     * insert process instance with specified start time and end time,set state to SUCCESS
     *
     * @param startTime
     * @param endTime
     * @return
     */
    private ProcessInstance insertOne(Date startTime, Date endTime) {
        ProcessInstance processInstance = new ProcessInstance();
        Date start = startTime;
        Date end = endTime;
        processInstance.setStartTime(start);
        processInstance.setEndTime(end);
        processInstance.setState(ExecutionStatus.SUCCESS);

        processInstanceMapper.insert(processInstance);
        return processInstance;
    }

    /**
     * insert
     * @return ProcessInstance
     */
    private ProcessInstance insertOne(){
        //insertOne
        ProcessInstance processInstance = new ProcessInstance();
        Date start = new Date(2019-1900, 1-1, 1, 0, 10,0);
        Date end = new Date(2019-1900, 1-1, 1, 1, 0,0);
        processInstance.setStartTime(start);
        processInstance.setEndTime(end);
        processInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);

        processInstanceMapper.insert(processInstance);
        return processInstance;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        ProcessInstance processInstanceMap = insertOne();
        //update
        int update = processInstanceMapper.updateById(processInstanceMap);
        Assert.assertEquals(1, update);
        processInstanceMapper.deleteById(processInstanceMap.getId());
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        ProcessInstance processInstanceMap = insertOne();
        int delete = processInstanceMapper.deleteById(processInstanceMap.getId());
        Assert.assertEquals(1, delete);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        ProcessInstance processInstance = insertOne();
        //query
        List<ProcessInstance> dataSources = processInstanceMapper.selectList(null);
        Assert.assertNotEquals(dataSources.size(), 0);
        processInstanceMapper.deleteById(processInstance.getId());
    }

    /**
     * test query detail by id
     */
    @Test
    public void testQueryDetailById() {
        ProcessInstance processInstance = insertOne();
        processInstanceMapper.updateById(processInstance);

        ProcessInstance processInstance1 = processInstanceMapper.queryDetailById(processInstance.getId());
        Assert.assertNotNull(processInstance1);
        processInstanceMapper.deleteById(processInstance.getId());
    }

    /**
     * test query by host and states
     */
    @Test
    public void testQueryByHostAndStates() {
        ProcessInstance processInstance = insertOne();
        processInstance.setHost("192.168.2.155");
        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        processInstanceMapper.updateById(processInstance);

        int[] stateArray = new int[]{
                ExecutionStatus.RUNNING_EXECUTION.ordinal(),
                ExecutionStatus.SUCCESS.ordinal()};

        List<ProcessInstance> processInstances = processInstanceMapper.queryByHostAndStatus(null, stateArray);

        processInstanceMapper.deleteById(processInstance.getId());
        Assert.assertNotEquals(processInstances.size(), 0);
    }

    /**
     * test query process instance page
     */
    @Test
    public void testQueryProcessInstanceListPaging() {


        int[] stateArray = new int[]{
                ExecutionStatus.RUNNING_EXECUTION.ordinal(),
                ExecutionStatus.SUCCESS.ordinal()};

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setProjectId(1010);
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        processDefinitionMapper.insert(processDefinition);

        ProcessInstance processInstance = insertOne();
        processInstance.setProcessDefinitionId(processDefinition.getId());
        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        processInstance.setIsSubProcess(Flag.NO);
        processInstance.setStartTime(new Date());

        processInstanceMapper.updateById(processInstance);


        Page<ProcessInstance> page = new Page(1, 3);

        IPage<ProcessInstance> processInstanceIPage = processInstanceMapper.queryProcessInstanceListPaging(
                page,
                processDefinition.getProjectId(),
                processInstance.getProcessDefinitionId(),
                processInstance.getName(),
                0,
                stateArray,
                processInstance.getHost(),
                null,
                null
        );
        Assert.assertNotEquals(processInstanceIPage.getTotal(), 0);

        processDefinitionMapper.deleteById(processDefinition.getId());
        processInstanceMapper.deleteById(processInstance.getId());
    }

    /**
     * test set failover by host and state
     */
    @Test
    public void testSetFailoverByHostAndStateArray() {

        int[] stateArray = new int[]{
                ExecutionStatus.RUNNING_EXECUTION.ordinal(),
                ExecutionStatus.SUCCESS.ordinal()};

        ProcessInstance processInstance = insertOne();

        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        processInstance.setHost("192.168.2.220");
        processInstanceMapper.updateById(processInstance);
        String host = processInstance.getHost();
        int update = processInstanceMapper.setFailoverByHostAndStateArray(host, stateArray);
        Assert.assertNotEquals(update, 0);

        processInstance = processInstanceMapper.selectById(processInstance.getId());
        Assert.assertNull(processInstance.getHost());
        processInstanceMapper.deleteById(processInstance.getId());
    }

    /**
     * test update process instance by state
     */
    @Test
    public void testUpdateProcessInstanceByState() {


        ProcessInstance processInstance = insertOne();

        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        processInstanceMapper.updateById(processInstance);
        processInstanceMapper.updateProcessInstanceByState(ExecutionStatus.RUNNING_EXECUTION, ExecutionStatus.SUCCESS);

        ProcessInstance processInstance1 = processInstanceMapper.selectById(processInstance.getId());

        processInstanceMapper.deleteById(processInstance.getId());
        Assert.assertEquals(ExecutionStatus.SUCCESS, processInstance1.getState());

    }

    /**
     * test count process instance state by user
     */
    @Test
    public void testCountInstanceStateByUser() {

        Project project = new Project();
        project.setName("testProject");
        projectMapper.insert(project);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setProjectId(project.getId());

        processDefinitionMapper.insert(processDefinition);
        ProcessInstance processInstance = insertOne();
        processInstance.setProcessDefinitionId(processDefinition.getId());
        int update = processInstanceMapper.updateById(processInstance);

        Integer[] projectIds = new Integer[]{processDefinition.getProjectId()};

        List<ExecuteStatusCount> executeStatusCounts = processInstanceMapper.countInstanceStateByUser(null, null, projectIds);


        Assert.assertNotEquals(executeStatusCounts.size(), 0);

        projectMapper.deleteById(project.getId());
        processDefinitionMapper.deleteById(processDefinition.getId());
        processInstanceMapper.deleteById(processInstance.getId());
    }

    /**
     * test query process instance by process definition id
     */
    @Test
    public void testQueryByProcessDefineId() {
        ProcessInstance processInstance = insertOne();
        ProcessInstance processInstance1 = insertOne();


        List<ProcessInstance> processInstances = processInstanceMapper.queryByProcessDefineId(processInstance.getProcessDefinitionId(), 1);
        Assert.assertEquals(1, processInstances.size());

        processInstances = processInstanceMapper.queryByProcessDefineId(processInstance.getProcessDefinitionId(), 2);
        Assert.assertEquals(2, processInstances.size());

        processInstanceMapper.deleteById(processInstance.getId());
        processInstanceMapper.deleteById(processInstance1.getId());
    }

    /**
     * test query last schedule process instance
     */
    @Test
    public void testQueryLastSchedulerProcess() {
        ProcessInstance processInstance = insertOne();
        processInstance.setScheduleTime(new Date());
        processInstanceMapper.updateById(processInstance);

        ProcessInstance processInstance1 = processInstanceMapper.queryLastSchedulerProcess(processInstance.getProcessDefinitionId(), null, null );
        Assert.assertNotEquals(processInstance1, null);
        processInstanceMapper.deleteById(processInstance.getId());
    }

    /**
     * test query last running process instance
     */
    @Test
    public void testQueryLastRunningProcess() {
        ProcessInstance processInstance = insertOne();
        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        processInstanceMapper.updateById(processInstance);

        int[] stateArray = new int[]{
                ExecutionStatus.RUNNING_EXECUTION.ordinal(),
                ExecutionStatus.SUBMITTED_SUCCESS.ordinal()};

        ProcessInstance processInstance1 = processInstanceMapper.queryLastRunningProcess(processInstance.getProcessDefinitionId(), null, null , stateArray);

        Assert.assertNotEquals(processInstance1, null);
        processInstanceMapper.deleteById(processInstance.getId());
    }

    /**
     * test query last manual process instance
     */
    @Test
    public void testQueryLastManualProcess() {
        ProcessInstance processInstance = insertOne();
        processInstanceMapper.updateById(processInstance);

        Date start = new Date(2019-1900, 1-1, 01, 0, 0, 0);
        Date end = new Date(2019-1900, 1-1, 01, 5, 0, 0);
        ProcessInstance processInstance1 = processInstanceMapper.queryLastManualProcess(processInstance.getProcessDefinitionId(),start, end
        );
        Assert.assertEquals(processInstance1.getId(), processInstance.getId());

        start = new Date(2019-1900, 1-1, 01, 1, 0, 0);
        processInstance1 = processInstanceMapper.queryLastManualProcess(processInstance.getProcessDefinitionId(),start, end
        );
        Assert.assertNull(processInstance1);

        processInstanceMapper.deleteById(processInstance.getId());

    }


    /**
     * test whether it is in descending order by running duration
     *
     * @param processInstances
     * @return
     */
    private boolean isSortedByDuration(List<ProcessInstance> processInstances) {
        for (int i = 1; i < processInstances.size(); i++) {
            long d1 = processInstances.get(i).getEndTime().getTime() - processInstances.get(i).getStartTime().getTime();
            long d2 = processInstances.get(i - 1).getEndTime().getTime() - processInstances.get(i - 1).getStartTime().getTime();
            if (d1 > d2) {
                return false;
            }
        }
        return true;
    }

    /**
     * test query top n process instance order by running duration
     */
    @Test
    public void testQueryTopNProcessInstance() {
        Date startTime1 = new Date(2019, 7, 9, 10, 9, 9);
        Date endTime1 = new Date(2019, 7, 9, 10, 9, 14);
        Date startTime2 = new Date(2020, 7, 9, 10, 9, 9);
        Date endTime2 = new Date(2020, 7, 9, 10, 9, 30);
        Date startTime3 = new Date(2020, 6, 9, 10, 9, 9);
        Date endTime3 = new Date(2020, 7, 9, 10, 9, 30);
        ProcessInstance processInstance1 = insertOne(startTime1, endTime1);
        ProcessInstance processInstance2 = insertOne(startTime2, endTime2);
        ProcessInstance processInstance3 = insertOne(startTime3, endTime3);
        Date start = new Date(2020, 1, 1, 1, 1, 1);
        Date end = new Date(2021, 1, 1, 1, 1, 1);
        List<ProcessInstance> processInstances = processInstanceMapper.queryTopNProcessInstance(2, start, end,ExecutionStatus.SUCCESS);
        Assert.assertEquals(2, processInstances.size());
        Assert.assertTrue(isSortedByDuration(processInstances));
        for (ProcessInstance processInstance : processInstances) {
            Assert.assertTrue(processInstance.getState().typeIsSuccess());
        }
        processInstanceMapper.deleteById(processInstance1.getId());
        processInstanceMapper.deleteById(processInstance2.getId());
        processInstanceMapper.deleteById(processInstance3.getId());

    }
}