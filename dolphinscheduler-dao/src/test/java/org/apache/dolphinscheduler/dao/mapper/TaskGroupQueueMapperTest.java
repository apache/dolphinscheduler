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
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
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

/**
 * task group mapper test
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
public class TaskGroupQueueMapperTest {

    @Autowired
    TaskGroupQueueMapper taskGroupQueueMapper;


    Integer userId = 1;

    /**
     * test insert
     */
    @Test
    public void testInsert() throws Exception {
        TaskGroupQueue taskGroupQueue = new TaskGroupQueue();
        taskGroupQueue.setTaskName("task1");
        taskGroupQueue.setGroupId(10);
        taskGroupQueue.setProcessId(11);
        taskGroupQueue.setPriority(10);
        Date date = new Date(System.currentTimeMillis());
        taskGroupQueue.setUpdateTime(date);
        taskGroupQueue.setUpdateTime(date);

        int i = taskGroupQueueMapper.insert(taskGroupQueue);
        Assert.assertEquals(i, 1);

    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {
        TaskGroupQueue taskGroupQueue = new TaskGroupQueue();
        taskGroupQueue.setId(1);
        taskGroupQueue.setStatus(1);
        taskGroupQueue.setUpdateTime(new Date(System.currentTimeMillis()));
        int i = taskGroupQueueMapper.updateById(taskGroupQueue);
        Assert.assertEquals(i, 1);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        int i = taskGroupQueueMapper.deleteByTaskId(1 );
        Assert.assertEquals(i, 1);
    }

    /**
     * test select
     */
    @Test
    public void testSelect() {
        TaskGroupQueue taskGroupQueue = taskGroupQueueMapper.selectById(1);
        Assert.assertEquals(taskGroupQueue.getTaskName(), "task1");

        List<TaskGroupQueue> taskGroupQueues = taskGroupQueueMapper.queryByStatus(1);
        Assert.assertEquals(taskGroupQueues.size(), 3);

    }


    /**
     * test queryTaskGroupPaging
     */
    @Test
    public void testQueryTaskGroupPaging() {
        Page<TaskGroupQueue> page = new Page(1, 3);
        IPage<TaskGroupQueue> taskGroupIPage = taskGroupQueueMapper.queryTaskGroupQueuePaging(
                page,
                1);

        for (TaskGroupQueue record : taskGroupIPage.getRecords()) {
            System.out.println(record);
        }
        Page<TaskGroupQueue> page1 = new Page(1, 3);
        IPage<TaskGroupQueue> taskGroupIPage1 = taskGroupQueueMapper.queryTaskGroupQueuePaging(
                page1,
                2);
        System.out.println("-----------------------");
        for (TaskGroupQueue record : taskGroupIPage1.getRecords()) {
            System.out.println(record);
        }
        Assert.assertEquals(taskGroupIPage.getTotal(), 3);
        Assert.assertEquals(taskGroupIPage1.getTotal(), 3);
    }

    @Test
    public void testUpdateStatusByTaskId() {
        int i = taskGroupQueueMapper.updateStatusByTaskId(1, 7);
        Assert.assertEquals(i,1);
    }

    @Test
    public void testDeleteByTaskId() {
        int i = taskGroupQueueMapper.deleteByTaskId(1);
        Assert.assertEquals(i,1);
    }
}