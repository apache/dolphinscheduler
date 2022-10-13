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

import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TaskDefinitionLogMapperTest extends BaseDaoTest {

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    public TaskDefinitionLog insertOne() {
        return insertOne(99);
    }

    public TaskDefinitionLog insertOne(int userId) {
        TaskDefinitionLog taskDefinition = new TaskDefinitionLog();
        taskDefinition.setCode(888888L);
        taskDefinition.setName("unit-test");
        taskDefinition.setProjectCode(1L);
        taskDefinition.setTaskType("SHELL");
        taskDefinition.setUserId(userId);
        taskDefinition.setEnvironmentCode(1L);
        taskDefinition.setWorkerGroup("default");
        taskDefinition.setVersion(1);
        taskDefinition.setCreateTime(new Date());
        taskDefinition.setUpdateTime(new Date());
        taskDefinitionLogMapper.insert(taskDefinition);
        return taskDefinition;
    }

    @Test
    public void testInsert() {
        TaskDefinitionLog taskDefinitionLog = insertOne();
        Assertions.assertNotEquals(taskDefinitionLog.getId().intValue(), 0);
    }

    @Test
    public void testQueryMaxVersionForDefinition() {
        TaskDefinitionLog taskDefinitionLog = insertOne();
        int version = taskDefinitionLogMapper
                .queryMaxVersionForDefinition(taskDefinitionLog.getCode());
        Assertions.assertNotEquals(version, 0);
    }

    @Test
    public void testQueryByDefinitionCodeAndVersion() {
        TaskDefinitionLog taskDefinitionLog = insertOne();
        TaskDefinitionLog tdl = taskDefinitionLogMapper
                .queryByDefinitionCodeAndVersion(taskDefinitionLog.getCode(), taskDefinitionLog.getVersion());
        Assertions.assertNotNull(tdl);
    }

    @Test
    public void testQueryByTaskDefinitions() {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setCode(888888L);
        taskDefinition.setName("unit-test");
        taskDefinition.setProjectCode(1L);
        taskDefinition.setTaskType("SHELL");
        taskDefinition.setUserId(1);
        taskDefinition.setResourceIds("1");
        taskDefinition.setVersion(1);
        ArrayList<TaskDefinition> taskDefinitions = new ArrayList<>();
        taskDefinitions.add(taskDefinition);

        TaskDefinitionLog taskDefinitionLog = insertOne();
        List<TaskDefinitionLog> taskDefinitionLogs = taskDefinitionLogMapper.queryByTaskDefinitions(taskDefinitions);
        Assertions.assertNotEquals(taskDefinitionLogs.size(), 0);
    }

}
