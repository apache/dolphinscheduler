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

import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TaskGroupQueueMapperTest extends BaseDaoTest {

    @Autowired
    TaskGroupQueueMapper taskGroupQueueMapper;

    int userId = 1;

    public TaskGroupQueue insertOne() {
        TaskGroupQueue taskGroupQueue = new TaskGroupQueue();
        taskGroupQueue.setTaskName("task1");
        taskGroupQueue.setGroupId(10);
        taskGroupQueue.setProcessId(11);
        taskGroupQueue.setPriority(10);
        taskGroupQueue.setStatus(TaskGroupQueueStatus.ACQUIRE_SUCCESS);
        Date date = new Date(System.currentTimeMillis());
        taskGroupQueue.setUpdateTime(date);
        taskGroupQueue.setUpdateTime(date);

        taskGroupQueueMapper.insert(taskGroupQueue);
        return taskGroupQueue;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {
        TaskGroupQueue taskGroupQueue = insertOne();
        taskGroupQueue.setStatus(TaskGroupQueueStatus.ACQUIRE_SUCCESS);
        taskGroupQueue.setUpdateTime(new Date(System.currentTimeMillis()));
        int i = taskGroupQueueMapper.updateById(taskGroupQueue);
        Assertions.assertEquals(i, 1);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        TaskGroupQueue taskGroupQueue = insertOne();
        int i = taskGroupQueueMapper.deleteByTaskId(taskGroupQueue.getId());
        Assertions.assertEquals(i, 0);
    }

    /**
     * test select
     */
    @Test
    public void testSelect() {
        TaskGroupQueue taskGroupQueue = insertOne();
        TaskGroupQueue result = taskGroupQueueMapper.selectById(taskGroupQueue.getId());
        Assertions.assertEquals(result.getTaskName(), "task1");

        List<TaskGroupQueue> taskGroupQueues = taskGroupQueueMapper.queryByStatus(taskGroupQueue.getStatus().getCode());
        Assertions.assertEquals(taskGroupQueues.size(), 1);

    }

    @Test
    public void testUpdateStatusByTaskId() {
        TaskGroupQueue taskGroupQueue = insertOne();
        int i = taskGroupQueueMapper.updateStatusByTaskId(taskGroupQueue.getTaskId(), 7);
        Assertions.assertEquals(i, 1);
    }

    @Test
    public void testDeleteByTaskId() {
        TaskGroupQueue taskGroupQueue = insertOne();
        int i = taskGroupQueueMapper.deleteByTaskId(taskGroupQueue.getTaskId());
        Assertions.assertEquals(i, 1);
    }
}
