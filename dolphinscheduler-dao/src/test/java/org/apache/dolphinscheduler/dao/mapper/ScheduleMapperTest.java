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
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.User;
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
public class ScheduleMapperTest {


    @Autowired
    ScheduleMapper scheduleMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    ProcessDefinitionMapper processDefinitionMapper;

    /**
     * insert
     * @return Schedule
     */
    private Schedule insertOne(){
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
        scheduleMapper.insert(schedule);
        return schedule;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        Schedule schedule = insertOne();
        schedule.setCreateTime(new Date());
        //update
        int update = scheduleMapper.updateById(schedule);
        Assert.assertEquals(update, 1);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        Schedule schedule = insertOne();
        int delete = scheduleMapper.deleteById(schedule.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        Schedule schedule = insertOne();
        //query
        List<Schedule> schedules = scheduleMapper.selectList(null);
        Assert.assertNotEquals(schedules.size(), 0);
    }

    /**
     * test page
     */
    @Test
    public void testQueryByProcessDefineIdPaging() {

        User user = new User();
        user.setUserName("ut name");
        userMapper.insert(user);

        Project project = new Project();
        project.setName("ut project");
        project.setUserId(user.getId());
        projectMapper.insert(project);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setProjectId(project.getId());
        processDefinition.setUserId(user.getId());
        processDefinition.setLocations("");
        processDefinitionMapper.insert(processDefinition);

        Schedule schedule= insertOne();
        schedule.setUserId(user.getId());
        schedule.setProcessDefinitionId(processDefinition.getId());
        scheduleMapper.insert(schedule);

        Page<Schedule> page = new Page(1,3);
        IPage<Schedule> scheduleIPage = scheduleMapper.queryByProcessDefineIdPaging(page,
                processDefinition.getId(), ""
        );
        Assert.assertNotEquals(scheduleIPage.getSize(), 0);


    }

    /**
     * test query schedule list by project name
     */
    @Test
    public void testQuerySchedulerListByProjectName() {


        User user = new User();
        user.setUserName("ut name");
        userMapper.insert(user);

        Project project = new Project();
        project.setName("ut project");
        project.setUserId(user.getId());
        projectMapper.insert(project);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setProjectId(project.getId());
        processDefinition.setUserId(user.getId());
        processDefinition.setLocations("");
        processDefinitionMapper.insert(processDefinition);

        Schedule schedule= insertOne();
        schedule.setUserId(user.getId());
        schedule.setProcessDefinitionId(processDefinition.getId());
        scheduleMapper.insert(schedule);

        Page<Schedule> page = new Page(1,3);
        List<Schedule> schedules = scheduleMapper.querySchedulerListByProjectName(
                project.getName()
        );

        Assert.assertNotEquals(schedules.size(), 0);
    }

    /**
     * test query by process definition ids
     */
    @Test
    public void testSelectAllByProcessDefineArray() {

        Schedule schedule = insertOne();
        schedule.setProcessDefinitionId(12345);
        schedule.setReleaseState(ReleaseState.ONLINE);
        scheduleMapper.updateById(schedule);

        List<Schedule> schedules= scheduleMapper.selectAllByProcessDefineArray(new int[] {schedule.getProcessDefinitionId()});
        Assert.assertNotEquals(schedules.size(), 0);
    }

    /**
     * test query by process definition id
     */
    @Test
    public void queryByProcessDefinitionId() {
        Schedule schedule = insertOne();
        schedule.setProcessDefinitionId(12345);
        scheduleMapper.updateById(schedule);

        List<Schedule> schedules= scheduleMapper.queryByProcessDefinitionId(schedule.getProcessDefinitionId());
        Assert.assertNotEquals(schedules.size(), 0);
    }
}