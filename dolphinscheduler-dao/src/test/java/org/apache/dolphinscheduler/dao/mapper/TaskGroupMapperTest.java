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
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * task group mapper test
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
public class TaskGroupMapperTest {

    private static final Logger logger = LoggerFactory.getLogger(TaskGroupMapperTest.class);


    @Autowired
    TaskGroupMapper taskGroupMapper;



    /**
     * test insert
     */
    @Test
    public void testInsert() throws Exception {
        TaskGroup taskGroup = new TaskGroup();
        taskGroup.setName("task group");
        taskGroup.setGroupSize(10);
        taskGroup.setDescription("this is a task group");
        Date date = new Date(System.currentTimeMillis());
        taskGroup.setUpdateTime(date);
        taskGroup.setUpdateTime(date);

        int i = taskGroupMapper.insert(taskGroup);
        Assert.assertEquals(i, 1);

    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {
        TaskGroup taskGroup = new TaskGroup();
        taskGroup.setId(1);
        taskGroup.setGroupSize(100);
        taskGroup.setUpdateTime(new Date(System.currentTimeMillis()));
        int i = taskGroupMapper.updateById(taskGroup);
        Assert.assertEquals(i, 1);
    }

    /**
     * test select
     */
    @Test
    public void testSelect() {
        TaskGroup taskGroup = taskGroupMapper.selectById(7);
        Assert.assertEquals(taskGroup.getDescription(), "for test");
    }

    /**
     * test CheckName
     */
    @Test
    public void testCheckName() {

        TaskGroup taskGroup = taskGroupMapper.queryByName(1, "task5");
        Assert.assertNotNull(taskGroup);

        taskGroup = taskGroupMapper.queryByName(0, "task5");
        Assert.assertNull(taskGroup);
    }

    /**
     * test compardAndUpdateUsedStatus
     */
    @Test
    public void testCompardAndUpdateUsedStatusFailed() {
        int i = taskGroupMapper.compardAndUpdateUsedStatus(7, 1, 2);
        Assert.assertEquals(i, 0);
    }

    /**
     * test queryTaskGroupPaging
     */
    @Test
    public void testQueryTaskGroupPaging() {
        Page<TaskGroup> page = new Page(1, 3);
        IPage<TaskGroup> taskGroupIPage = taskGroupMapper.queryTaskGroupPaging(
                page,
                1,
                "1", 1);

        for (TaskGroup record : taskGroupIPage.getRecords()) {
            System.out.println(record);
        }
        Page<TaskGroup> page1 = new Page(1, 3);
        IPage<TaskGroup> taskGroupIPage1 = taskGroupMapper.queryTaskGroupPaging(
                page1,
                0,
                null, 1);
        for (TaskGroup record : taskGroupIPage1.getRecords()) {
            System.out.println(record);
        }
        Assert.assertEquals(taskGroupIPage.getTotal(), 1);
        Assert.assertEquals(taskGroupIPage1.getTotal(), 1);
    }
}