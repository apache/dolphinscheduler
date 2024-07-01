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

package org.apache.dolphinscheduler.dao.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.repository.TaskGroupQueueDao;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.Date;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TaskGroupQueueDaoImplTest extends BaseDaoTest {

    @Autowired
    private TaskGroupQueueDao taskGroupQueueDao;

    @Test
    void deleteByWorkflowInstanceIds() {
        TaskGroupQueue taskGroupQueue = createTaskGroupQueue(Flag.NO, TaskGroupQueueStatus.ACQUIRE_SUCCESS);
        taskGroupQueueDao.insert(taskGroupQueue);
        assertNotNull(taskGroupQueueDao.queryById(taskGroupQueue.getId()));

        taskGroupQueueDao.deleteByWorkflowInstanceIds(Lists.newArrayList(1));
        assertNull(taskGroupQueueDao.queryById(taskGroupQueue.getId()));
    }

    @Test
    void queryAllInQueueTaskGroupQueue() {
        TaskGroupQueue taskGroupQueue = createTaskGroupQueue(Flag.NO, TaskGroupQueueStatus.ACQUIRE_SUCCESS);
        taskGroupQueueDao.insert(taskGroupQueue);
        assertEquals(1, taskGroupQueueDao.queryAllInQueueTaskGroupQueue().size());
    }

    @Test
    void queryInQueueTaskGroupQueue_withMinId() {
        // Insert 1w ~ 10w records
        int insertCount = RandomUtils.nextInt(10000, 100000);
        List<TaskGroupQueue> insertTaskGroupQueue = Lists.newArrayList();
        for (int i = 0; i < insertCount; i++) {
            TaskGroupQueue taskGroupQueue = createTaskGroupQueue(Flag.NO, TaskGroupQueueStatus.ACQUIRE_SUCCESS);
            insertTaskGroupQueue.add(taskGroupQueue);
        }
        taskGroupQueueDao.insertBatch(insertTaskGroupQueue);

        int minTaskGroupQueueId = -1;
        int limit = 1000;
        int queryCount = 0;
        while (true) {
            List<TaskGroupQueue> taskGroupQueues =
                    taskGroupQueueDao.queryInQueueTaskGroupQueue(minTaskGroupQueueId, limit);
            if (CollectionUtils.isEmpty(taskGroupQueues)) {
                break;
            }
            queryCount += taskGroupQueues.size();
            if (taskGroupQueues.size() < limit) {
                break;
            }
            minTaskGroupQueueId = taskGroupQueues.get(taskGroupQueues.size() - 1).getId();
        }
        assertEquals(insertCount, queryCount);
    }

    @Test
    void queryAllInQueueTaskGroupQueueByGroupId() {
        TaskGroupQueue taskGroupQueue = createTaskGroupQueue(Flag.NO, TaskGroupQueueStatus.ACQUIRE_SUCCESS);
        taskGroupQueueDao.insert(taskGroupQueue);
        assertEquals(1, taskGroupQueueDao.queryAllInQueueTaskGroupQueueByGroupId(1).size());
    }

    @Test
    void updateById() {
        TaskGroupQueue taskGroupQueue = createTaskGroupQueue(Flag.NO, TaskGroupQueueStatus.WAIT_QUEUE);
        taskGroupQueueDao.insert(taskGroupQueue);

        taskGroupQueue.setStatus(TaskGroupQueueStatus.ACQUIRE_SUCCESS);
        taskGroupQueueDao.updateById(taskGroupQueue);
        assertEquals(TaskGroupQueueStatus.ACQUIRE_SUCCESS,
                taskGroupQueueDao.queryById(taskGroupQueue.getId()).getStatus());
    }

    @Test
    void queryByTaskInstanceId() {
        TaskGroupQueue taskGroupQueue = createTaskGroupQueue(Flag.NO, TaskGroupQueueStatus.ACQUIRE_SUCCESS);
        taskGroupQueueDao.insert(taskGroupQueue);
        assertEquals(1, taskGroupQueueDao.queryByTaskInstanceId(1).size());
    }

    @Test
    void queryUsingTaskGroupQueueByGroupId() {
        TaskGroupQueue taskGroupQueue = createTaskGroupQueue(Flag.NO, TaskGroupQueueStatus.ACQUIRE_SUCCESS);
        taskGroupQueueDao.insert(taskGroupQueue);
        assertEquals(1, taskGroupQueueDao.queryAcquiredTaskGroupQueueByGroupId(1).size());

        taskGroupQueue = createTaskGroupQueue(Flag.YES, TaskGroupQueueStatus.WAIT_QUEUE);
        taskGroupQueueDao.insert(taskGroupQueue);
        assertEquals(1, taskGroupQueueDao.queryAcquiredTaskGroupQueueByGroupId(1).size());
    }

    @Test
    void countUsingTaskGroupQueueByGroupId() {
        assertEquals(0, taskGroupQueueDao.countUsingTaskGroupQueueByGroupId(1));

        TaskGroupQueue taskGroupQueue = createTaskGroupQueue(Flag.NO, TaskGroupQueueStatus.ACQUIRE_SUCCESS);
        taskGroupQueueDao.insert(taskGroupQueue);
        assertEquals(1, taskGroupQueueDao.countUsingTaskGroupQueueByGroupId(1));

        taskGroupQueue = createTaskGroupQueue(Flag.YES, TaskGroupQueueStatus.WAIT_QUEUE);
        taskGroupQueueDao.insert(taskGroupQueue);
        assertEquals(1, taskGroupQueueDao.countUsingTaskGroupQueueByGroupId(1));
    }

    @Test
    void queryWaitNotifyForceStartTaskGroupQueue() {
        // Insert 1w records
        int insertCount = RandomUtils.nextInt(10000, 20000);
        List<TaskGroupQueue> insertTaskGroupQueue = Lists.newArrayList();
        for (int i = 0; i < insertCount; i++) {
            TaskGroupQueue taskGroupQueue = createTaskGroupQueue(Flag.YES, TaskGroupQueueStatus.ACQUIRE_SUCCESS);

            insertTaskGroupQueue.add(taskGroupQueue);
        }
        taskGroupQueueDao.insertBatch(insertTaskGroupQueue);

        int beginTaskGroupQueueId = -1;
        int limit = 1000;
        int queryCount = 0;
        while (true) {
            List<TaskGroupQueue> taskGroupQueues =
                    taskGroupQueueDao.queryWaitNotifyForceStartTaskGroupQueue(beginTaskGroupQueueId, limit);
            if (CollectionUtils.isEmpty(taskGroupQueues)) {
                break;
            }
            queryCount += taskGroupQueues.size();
            if (taskGroupQueues.size() < limit) {
                break;
            }
            beginTaskGroupQueueId = taskGroupQueues.get(taskGroupQueues.size() - 1).getId();
        }
        assertEquals(insertCount, queryCount);
    }

    private TaskGroupQueue createTaskGroupQueue(Flag forceStart, TaskGroupQueueStatus taskGroupQueueStatus) {
        return TaskGroupQueue.builder()
                .taskId(1)
                .taskName("test")
                .groupId(1)
                .processId(1)
                .priority(0)
                .forceStart(forceStart.getCode())
                .inQueue(Flag.YES.getCode())
                .status(taskGroupQueueStatus)
                .createTime(new Date())
                .updateTime(new Date())
                .build();
    }
}
