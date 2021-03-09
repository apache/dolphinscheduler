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

import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.ArrayList;
import java.util.Date;
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
public class TaskDefinitionLogMapperTest {

    @Autowired
    TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ProjectMapper projectMapper;

    public TaskDefinitionLog insertOne() {
        return insertOne(99);
    }

    public TaskDefinitionLog insertOne(int userId) {
        TaskDefinitionLog taskDefinition = new TaskDefinitionLog();
        taskDefinition.setCode(888888L);
        taskDefinition.setName("unit-test");
        taskDefinition.setProjectCode(1L);
        taskDefinition.setTaskType(TaskType.SHELL);
        taskDefinition.setUserId(userId);
        taskDefinition.setVersion(1);
        taskDefinition.setCreateTime(new Date());
        taskDefinition.setUpdateTime(new Date());
        taskDefinitionLogMapper.insert(taskDefinition);
        return taskDefinition;
    }

    @Test
    public void testInsert() {
        TaskDefinitionLog taskDefinitionLog = insertOne();
        Assert.assertNotEquals(taskDefinitionLog.getId(), 0);
    }

    @Test
    public void queryByDefinitionName() {
        User user = new User();
        user.setUserName("un");
        userMapper.insert(user);
        User un = userMapper.queryByUserNameAccurately("un");

        Project project = new Project();
        project.setCode(1L);
        project.setCreateTime(new Date());
        project.setUpdateTime(new Date());
        projectMapper.insert(project);

        TaskDefinitionLog taskDefinitionLog = insertOne(un.getId());

        List<TaskDefinitionLog> taskDefinitionLogs = taskDefinitionLogMapper
                .queryByDefinitionName(taskDefinitionLog.getProjectCode(), taskDefinitionLog.getName());
        Assert.assertNotEquals(taskDefinitionLogs.size(), 0);
    }

    @Test
    public void queryByDefinitionCode() {
        TaskDefinitionLog taskDefinitionLog = insertOne();
        List<TaskDefinitionLog> taskDefinitionLogs = taskDefinitionLogMapper
                .queryByDefinitionCode(taskDefinitionLog.getCode());
        Assert.assertNotEquals(taskDefinitionLogs.size(), 0);
    }

    @Test
    public void queryByDefinitionCodeAndVersion() {
        TaskDefinitionLog taskDefinitionLog = insertOne();
        TaskDefinitionLog tdl = taskDefinitionLogMapper
                .queryByDefinitionCodeAndVersion(taskDefinitionLog.getCode(), taskDefinitionLog.getVersion());
        Assert.assertNotNull(tdl);
    }

    @Test
    public void queryByTaskDefinitions() {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setCode(888888L);
        taskDefinition.setName("unit-test");
        taskDefinition.setProjectCode(1L);
        taskDefinition.setTaskType(TaskType.SHELL);
        taskDefinition.setUserId(1);
        taskDefinition.setResourceIds("1");
        taskDefinition.setVersion(1);
        ArrayList<TaskDefinition> taskDefinitions = new ArrayList<>();
        taskDefinitions.add(taskDefinition);

        TaskDefinitionLog taskDefinitionLog = insertOne();
        List<TaskDefinitionLog> taskDefinitionLogs = taskDefinitionLogMapper.queryByTaskDefinitions(taskDefinitions);
        Assert.assertNotEquals(taskDefinitionLogs.size(), 0);
    }

}
