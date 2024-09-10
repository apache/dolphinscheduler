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

import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelationDetail;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskLineage;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class WorkflowTaskLineageMapperTest extends BaseDaoTest {

    @Autowired
    private WorkflowTaskLineageMapper workflowTaskLineageMapper;

    @Autowired
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private WorkflowTaskRelationMapper workflowTaskRelationMapper;

    /**
     * insert
     */
    private void insertOneProcessTaskRelation() {
        // insertOne
        WorkflowTaskRelation workflowTaskRelation = new WorkflowTaskRelation();
        workflowTaskRelation.setName("def 1");

        workflowTaskRelation.setProjectCode(1L);
        workflowTaskRelation.setWorkflowDefinitionCode(1L);
        workflowTaskRelation.setPostTaskCode(3L);
        workflowTaskRelation.setPostTaskVersion(1);
        workflowTaskRelation.setPreTaskCode(2L);
        workflowTaskRelation.setPreTaskVersion(1);
        workflowTaskRelation.setUpdateTime(new Date());
        workflowTaskRelation.setCreateTime(new Date());
        workflowTaskRelationMapper.insert(workflowTaskRelation);
    }

    /**
     * insert
     *
     */
    private void insertOneProcessDefinition() {
        // insertOne
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(1L);
        workflowDefinition.setName("def 1");
        workflowDefinition.setProjectCode(1L);
        workflowDefinition.setUserId(101);
        workflowDefinition.setUpdateTime(new Date());
        workflowDefinition.setCreateTime(new Date());
        workflowDefinitionMapper.insert(workflowDefinition);
    }

    private void insertOneProcessLineage() {
        // insertOne
        WorkflowTaskLineage workflowTaskLineage = new WorkflowTaskLineage();
        workflowTaskLineage.setWorkflowDefinitionCode(1L);
        workflowTaskLineage.setWorkflowDefinitionVersion(1);
        workflowTaskLineage.setTaskDefinitionCode(1L);
        workflowTaskLineage.setTaskDefinitionVersion(1);
        workflowTaskLineage.setDeptProjectCode(1L);
        workflowTaskLineage.setDeptWorkflowDefinitionCode(1L);
        workflowTaskLineage.setDeptTaskDefinitionCode(1L);
        workflowTaskLineage.setUpdateTime(new Date());
        workflowTaskLineage.setCreateTime(new Date());
        workflowTaskLineageMapper.insert(workflowTaskLineage);
    }

    /**
     * insert
     *
     */
    private void insertOneSchedule(int id) {
        // insertOne
        Schedule schedule = new Schedule();
        schedule.setStartTime(new Date());
        schedule.setEndTime(new Date());
        schedule.setCrontab("");
        schedule.setFailureStrategy(FailureStrategy.CONTINUE);
        schedule.setReleaseState(ReleaseState.OFFLINE);
        schedule.setWarningType(WarningType.NONE);
        schedule.setCreateTime(new Date());
        schedule.setUpdateTime(new Date());
        schedule.setWorkflowDefinitionCode(id);
        scheduleMapper.insert(schedule);
    }

    @Test
    public void testQueryWorkFlowLineageByName() {
        insertOneProcessDefinition();
        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(1L);
        insertOneSchedule(workflowDefinition.getId());
        List<WorkFlowRelationDetail> workFlowLineages = workflowTaskLineageMapper
                .queryWorkFlowLineageByName(workflowDefinition.getProjectCode(), workflowDefinition.getName());
        Assertions.assertNotEquals(0, workFlowLineages.size());
    }

    @Test
    public void testQueryWorkFlowLineage() {
        insertOneProcessDefinition();
        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(1L);
        insertOneProcessTaskRelation();
        insertOneProcessLineage();
        List<WorkflowTaskLineage> workflowTaskLineages =
                workflowTaskLineageMapper.queryByProjectCode(workflowDefinition.getProjectCode());
        Assertions.assertNotEquals(0, workflowTaskLineages.size());
    }

    @Test
    public void testQueryWorkFlowLineageByCode() {
        insertOneProcessDefinition();
        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(1L);
        insertOneSchedule(workflowDefinition.getId());
        List<WorkFlowRelationDetail> workFlowLineages = workflowTaskLineageMapper
                .queryWorkFlowLineageByCode(workflowDefinition.getCode());
        Assertions.assertNotNull(workFlowLineages);
    }

}
