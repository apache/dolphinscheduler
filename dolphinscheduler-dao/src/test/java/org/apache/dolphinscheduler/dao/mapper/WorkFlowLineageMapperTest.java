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
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessLineage;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class WorkFlowLineageMapperTest {

    @Autowired
    private WorkFlowLineageMapper workFlowLineageMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    ProcessTaskRelationMapper processTaskRelationMapper;

    /**
     * insert
     *
     * @return ProcessDefinition
     */
    private ProcessTaskRelation insertOneProcessTaskRelation() {
        //insertOne
        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();
        processTaskRelation.setName("def 1");

        processTaskRelation.setProjectCode(1L);
        processTaskRelation.setProcessDefinitionCode(1L);
        processTaskRelation.setPostTaskCode(3L);
        processTaskRelation.setPostTaskVersion(1);
        processTaskRelation.setPreTaskCode(2L);
        processTaskRelation.setPreTaskVersion(1);
        processTaskRelation.setUpdateTime(new Date());
        processTaskRelation.setCreateTime(new Date());
        processTaskRelationMapper.insert(processTaskRelation);
        return processTaskRelation;
    }

    /**
     * insert
     *
     * @return ProcessDefinition
     */
    private ProcessDefinition insertOneProcessDefinition() {
        //insertOne
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(1L);
        processDefinition.setName("def 1");
        processDefinition.setProjectCode(1L);
        processDefinition.setUserId(101);
        processDefinition.setUpdateTime(new Date());
        processDefinition.setCreateTime(new Date());
        processDefinitionMapper.insert(processDefinition);
        return processDefinition;
    }

    /**
     * insert
     *
     * @return Schedule
     */
    private Schedule insertOneSchedule(int id) {
        //insertOne
        Schedule schedule = new Schedule();
        schedule.setStartTime(new Date());
        schedule.setEndTime(new Date());
        schedule.setCrontab("");
        schedule.setFailureStrategy(FailureStrategy.CONTINUE);
        schedule.setReleaseState(ReleaseState.OFFLINE);
        schedule.setWarningType(WarningType.NONE);
        schedule.setCreateTime(new Date());
        schedule.setUpdateTime(new Date());
        schedule.setProcessDefinitionId(id);
        scheduleMapper.insert(schedule);
        return schedule;
    }

    @Test
    public void testQueryByName() {
        insertOneProcessDefinition();
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(1L);
        insertOneSchedule(processDefinition.getId());

        List<WorkFlowLineage> workFlowLineages = workFlowLineageMapper.queryByName(processDefinition.getName(), processDefinition.getProjectCode());
        Assert.assertNotEquals(workFlowLineages.size(), 0);
    }

    @Test
    public void testQueryCodeRelation() {
        ProcessTaskRelation processTaskRelation = insertOneProcessTaskRelation();

        List<ProcessLineage> workFlowLineages = workFlowLineageMapper.queryCodeRelation(processTaskRelation.getPreTaskCode()
                , processTaskRelation.getPreTaskVersion(), 11L, 1L);
        Assert.assertNotEquals(workFlowLineages.size(), 0);
    }

    @Test
    public void testQueryRelationByIds() {
        insertOneProcessDefinition();
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(1L);
        insertOneProcessTaskRelation();

        HashSet<Integer> set = new HashSet<>();
        set.add(processDefinition.getId());
        List<ProcessLineage> workFlowLineages = workFlowLineageMapper.queryRelationByIds(set, processDefinition.getProjectCode());
        Assert.assertNotEquals(workFlowLineages.size(), 0);
    }

    @Test
    public void testQueryWorkFlowLineageByCode() {
        insertOneProcessDefinition();
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(1L);
        insertOneSchedule(processDefinition.getId());

        WorkFlowLineage workFlowLineages = workFlowLineageMapper.queryWorkFlowLineageByCode(processDefinition.getCode(), processDefinition.getProjectCode());
        Assert.assertNotNull(workFlowLineages);
    }

}
