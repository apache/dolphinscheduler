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
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.model.WorkflowInstanceStatusCountDto;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;

public class WorkflowInstanceMapperTest extends BaseDaoTest {

    @Autowired
    private WorkflowInstanceMapper workflowInstanceMapper;

    @Autowired
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Autowired
    private ProjectMapper projectMapper;

    /**
     * insert process instance with specified start time and end time,set state to SUCCESS
     */
    private WorkflowInstance insertOne(Date startTime, Date endTime) {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        Date start = startTime;
        Date end = endTime;
        workflowInstance.setWorkflowDefinitionCode(1L);
        workflowInstance.setStartTime(start);
        workflowInstance.setEndTime(end);
        workflowInstance.setState(WorkflowExecutionStatus.SUCCESS);

        workflowInstanceMapper.insert(workflowInstance);
        return workflowInstance;
    }

    /**
     * insert
     *
     * @return ProcessInstance
     */
    private WorkflowInstance insertOne() {
        // insertOne
        WorkflowInstance workflowInstance = new WorkflowInstance();
        Date start = new Date(2019 - 1900, 1 - 1, 1, 0, 10, 0);
        Date end = new Date(2019 - 1900, 1 - 1, 1, 1, 0, 0);
        workflowInstance.setWorkflowDefinitionCode(1L);
        workflowInstance.setProjectCode(1L);
        workflowInstance.setStartTime(start);
        workflowInstance.setEndTime(end);
        workflowInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        workflowInstance.setTestFlag(0);
        workflowInstanceMapper.insert(workflowInstance);
        return workflowInstance;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {
        // insertOne
        WorkflowInstance workflowInstanceMap = insertOne();
        // update
        int update = workflowInstanceMapper.updateById(workflowInstanceMap);
        Assertions.assertEquals(1, update);
        workflowInstanceMapper.deleteById(workflowInstanceMap.getId());
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        WorkflowInstance workflowInstanceMap = insertOne();
        int delete = workflowInstanceMapper.deleteById(workflowInstanceMap.getId());
        Assertions.assertEquals(1, delete);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        WorkflowInstance workflowInstance = insertOne();
        // query
        List<WorkflowInstance> dataSources = workflowInstanceMapper.selectList(null);
        Assertions.assertNotEquals(0, dataSources.size());
        workflowInstanceMapper.deleteById(workflowInstance.getId());
    }

    /**
     * test query detail by id
     */
    @Test
    public void testQueryDetailById() {
        WorkflowInstance workflowInstance = insertOne();
        workflowInstanceMapper.updateById(workflowInstance);

        WorkflowInstance workflowInstance1 = workflowInstanceMapper.queryDetailById(workflowInstance.getId());
        Assertions.assertNotNull(workflowInstance1);
        workflowInstanceMapper.deleteById(workflowInstance.getId());
    }

    /**
     * test query by host and states
     */
    @Test
    public void testQueryByHostAndStates() {
        WorkflowInstance workflowInstance = insertOne();
        workflowInstance.setHost("192.168.2.155");
        workflowInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        workflowInstanceMapper.updateById(workflowInstance);

        int[] stateArray = new int[]{
                TaskExecutionStatus.RUNNING_EXECUTION.getCode(),
                TaskExecutionStatus.SUCCESS.getCode()};

        List<WorkflowInstance> workflowInstances = workflowInstanceMapper.queryByHostAndStatus(null, stateArray);

        workflowInstanceMapper.deleteById(workflowInstance.getId());
        Assertions.assertNotEquals(0, workflowInstances.size());
    }

    /**
     * test query process instance page
     */
    @Test
    public void testQueryWorkflowInstanceListPaging() {

        int[] stateArray = new int[]{
                WorkflowExecutionStatus.RUNNING_EXECUTION.getCode(),
                WorkflowExecutionStatus.SUCCESS.getCode()};

        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(1L);
        workflowDefinition.setProjectCode(1L);
        workflowDefinition.setReleaseState(ReleaseState.ONLINE);
        workflowDefinition.setUpdateTime(new Date());
        workflowDefinition.setCreateTime(new Date());
        workflowDefinitionMapper.insert(workflowDefinition);

        WorkflowInstance workflowInstance = insertOne();
        workflowInstance.setProjectCode(workflowDefinition.getProjectCode());
        workflowInstance.setWorkflowDefinitionCode(workflowDefinition.getCode());
        workflowInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        workflowInstance.setIsSubWorkflow(Flag.NO);
        workflowInstance.setStartTime(new Date());

        workflowInstanceMapper.updateById(workflowInstance);

        Page<WorkflowInstance> page = new Page(1, 3);

        IPage<WorkflowInstance> processInstanceIPage = workflowInstanceMapper.queryWorkflowInstanceListPaging(
                page,
                workflowDefinition.getProjectCode(),
                workflowInstance.getWorkflowDefinitionCode(),
                workflowInstance.getName(),
                "",
                stateArray,
                workflowInstance.getHost(),
                null,
                null);
        Assertions.assertNotEquals(0, processInstanceIPage.getTotal());

        workflowDefinitionMapper.deleteById(workflowDefinition.getId());
        workflowInstanceMapper.deleteById(workflowInstance.getId());
    }

    /**
     * test update process instance by state
     */
    @Test
    public void testUpdateProcessInstanceByState() {

        WorkflowInstance workflowInstance = insertOne();

        workflowInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        workflowInstanceMapper.updateById(workflowInstance);

        workflowInstanceMapper.updateWorkflowInstanceState(workflowInstance.getId(),
                WorkflowExecutionStatus.RUNNING_EXECUTION,
                WorkflowExecutionStatus.SUCCESS);

        WorkflowInstance workflowInstance1 = workflowInstanceMapper.selectById(workflowInstance.getId());

        workflowInstanceMapper.deleteById(workflowInstance.getId());
        Assertions.assertEquals(WorkflowExecutionStatus.SUCCESS, workflowInstance1.getState());

    }

    /**
     * test count process instance state by user
     */
    @Test
    public void testCountInstanceStateByUser() {
        WorkflowInstance workflowInstance = insertOne();

        List<WorkflowInstanceStatusCountDto> workflowInstanceStatusCountDtos =
                workflowInstanceMapper.countWorkflowInstanceStateByProjectCodes(null, null,
                        Lists.newArrayList(workflowInstance.getProjectCode()));

        Assertions.assertNotEquals(0, workflowInstanceStatusCountDtos.size());

        workflowInstanceMapper.deleteById(workflowInstance.getId());
    }

    /**
     * test query process instance by process definition id
     */
    @Test
    public void testQueryByProcessDefineId() {
        WorkflowInstance workflowInstance = insertOne();
        WorkflowInstance workflowInstance1 = insertOne();

        List<WorkflowInstance> workflowInstances =
                workflowInstanceMapper.queryByWorkflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode(), 1);
        Assertions.assertEquals(1, workflowInstances.size());

        workflowInstances =
                workflowInstanceMapper.queryByWorkflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode(), 2);
        Assertions.assertEquals(2, workflowInstances.size());

        workflowInstanceMapper.deleteById(workflowInstance.getId());
        workflowInstanceMapper.deleteById(workflowInstance1.getId());
    }

    /**
     * test query last schedule process instance
     */
    @Test
    public void testQueryLastSchedulerWorkflow() {
        WorkflowInstance workflowInstance = insertOne();
        workflowInstance.setScheduleTime(new Date());
        workflowInstanceMapper.updateById(workflowInstance);

        WorkflowInstance workflowInstance1 =
                workflowInstanceMapper.queryLastSchedulerWorkflow(workflowInstance.getWorkflowDefinitionCode(), 0L,
                        null,
                        null,
                        workflowInstance.getTestFlag());
        Assertions.assertNotEquals(null, workflowInstance1);
        workflowInstanceMapper.deleteById(workflowInstance.getId());
    }

    /**
     * test query last manual process instance
     */
    @Test
    public void testQueryLastManualWorkflow() {
        WorkflowInstance workflowInstance = insertOne();
        workflowInstanceMapper.updateById(workflowInstance);

        Date start = new Date(2019 - 1900, 1 - 1, 01, 0, 0, 0);
        Date end = new Date(2019 - 1900, 1 - 1, 01, 5, 0, 0);
        WorkflowInstance workflowInstance1 =
                workflowInstanceMapper.queryLastManualWorkflow(workflowInstance.getWorkflowDefinitionCode(), null,
                        start,
                        end,
                        workflowInstance.getTestFlag());
        Assertions.assertEquals(workflowInstance1.getId(), workflowInstance.getId());

        start = new Date(2019 - 1900, 1 - 1, 01, 1, 0, 0);
        workflowInstance1 =
                workflowInstanceMapper.queryLastManualWorkflow(workflowInstance.getWorkflowDefinitionCode(), null,
                        start,
                        end,
                        workflowInstance.getTestFlag());
        Assertions.assertNull(workflowInstance1);

        workflowInstanceMapper.deleteById(workflowInstance.getId());

    }

    /**
     * test whether it is in descending order by running duration
     */
    private boolean isSortedByDuration(List<WorkflowInstance> workflowInstances) {
        for (int i = 1; i < workflowInstances.size(); i++) {
            long d1 =
                    workflowInstances.get(i).getEndTime().getTime() - workflowInstances.get(i).getStartTime().getTime();
            long d2 = workflowInstances.get(i - 1).getEndTime().getTime()
                    - workflowInstances.get(i - 1).getStartTime().getTime();
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
    public void testQueryTopNWorkflowInstance() {
        Date startTime1 = new Date(2019, 7, 9, 10, 9, 9);
        Date endTime1 = new Date(2019, 7, 9, 10, 9, 14);
        Date startTime2 = new Date(2020, 7, 9, 10, 9, 9);
        Date endTime2 = new Date(2020, 7, 9, 10, 9, 30);
        Date startTime3 = new Date(2020, 6, 9, 10, 9, 9);
        Date endTime3 = new Date(2020, 7, 9, 10, 9, 30);
        WorkflowInstance workflowInstance1 = insertOne(startTime1, endTime1);
        WorkflowInstance workflowInstance2 = insertOne(startTime2, endTime2);
        WorkflowInstance workflowInstance3 = insertOne(startTime3, endTime3);
        Date start = new Date(2020, 1, 1, 1, 1, 1);
        Date end = new Date(2021, 1, 1, 1, 1, 1);
        List<WorkflowInstance> workflowInstances =
                workflowInstanceMapper.queryTopNWorkflowInstance(2, start, end, WorkflowExecutionStatus.SUCCESS, 0L);
        Assertions.assertEquals(2, workflowInstances.size());
        Assertions.assertTrue(isSortedByDuration(workflowInstances));
        for (WorkflowInstance workflowInstance : workflowInstances) {
            Assertions.assertTrue(workflowInstance.getState().isSuccess());
        }
        workflowInstanceMapper.deleteById(workflowInstance1.getId());
        workflowInstanceMapper.deleteById(workflowInstance2.getId());
        workflowInstanceMapper.deleteById(workflowInstance3.getId());

    }
}
