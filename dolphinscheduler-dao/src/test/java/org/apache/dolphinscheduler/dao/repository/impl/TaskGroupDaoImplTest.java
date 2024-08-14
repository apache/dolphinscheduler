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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.repository.TaskGroupDao;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TaskGroupDaoImplTest extends BaseDaoTest {

    @Autowired
    private TaskGroupDao taskGroupDao;

    @Test
    void queryAllTaskGroups() {
        TaskGroup taskGroup = createTaskGroup("test", 0, 1);
        taskGroupDao.insert(taskGroup);
        List<TaskGroup> taskGroups = taskGroupDao.queryAllTaskGroups();
        assertEquals(1, taskGroups.size());
    }

    @Test
    void queryUsedTaskGroups() {
        // Insert a unused task group
        TaskGroup taskGroup = createTaskGroup("testUnused", 0, 1);
        taskGroupDao.insert(taskGroup);
        assertEquals(0, taskGroupDao.queryUsedTaskGroups().size());

        // Insert a used task group
        taskGroup = createTaskGroup("testUsed", 1, 1);
        taskGroupDao.insert(taskGroup);
        assertEquals(1, taskGroupDao.queryUsedTaskGroups().size());
    }

    @Test
    void queryAvailableTaskGroups() {
        // Insert a full task group
        TaskGroup taskGroup = createTaskGroup("testFull", 1, 1);
        taskGroupDao.insert(taskGroup);
        assertEquals(0, taskGroupDao.queryAvailableTaskGroups().size());

        // Insert a used task group
        taskGroup = createTaskGroup("testNotFull", 0, 1);
        taskGroupDao.insert(taskGroup);
        assertEquals(1, taskGroupDao.queryAvailableTaskGroups().size());
    }

    @Test
    void acquireTaskGroupSlot() {
        // Insert a full task group will acquire failed
        TaskGroup taskGroup = createTaskGroup("testFull", 1, 1);
        taskGroupDao.insert(taskGroup);
        assertFalse(taskGroupDao.acquireTaskGroupSlot(taskGroup.getId()));

        taskGroup.setUseSize(0);
        taskGroupDao.updateById(taskGroup);
        assertTrue(taskGroupDao.acquireTaskGroupSlot(taskGroup.getId()));

        taskGroup = taskGroupDao.queryById(taskGroup.getId());
        assertEquals(1, taskGroup.getUseSize());
    }

    @Test
    void releaseTaskGroupSlot() {
        // Insert an empty task group will release failed
        TaskGroup taskGroup = createTaskGroup("testEmpty", 0, 1);
        taskGroupDao.insert(taskGroup);
        assertFalse(taskGroupDao.releaseTaskGroupSlot(taskGroup.getId()));

        taskGroup.setUseSize(1);
        taskGroupDao.updateById(taskGroup);
        assertTrue(taskGroupDao.releaseTaskGroupSlot(taskGroup.getId()));

        taskGroup = taskGroupDao.queryById(taskGroup.getId());
        assertEquals(0, taskGroup.getUseSize());
    }

    private TaskGroup createTaskGroup(String name, int useSize, int groupSize) {
        return TaskGroup.builder()
                .name(name)
                .description("test")
                .groupSize(groupSize)
                .useSize(useSize)
                .userId(1)
                .status(Flag.YES)
                .createTime(new Date())
                .updateTime(new Date())
                .projectCode(1)
                .build();
    }

}
