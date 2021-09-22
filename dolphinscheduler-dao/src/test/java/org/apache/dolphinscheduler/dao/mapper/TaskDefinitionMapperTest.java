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
import org.apache.dolphinscheduler.dao.entity.DefinitionGroupByUser;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
public class TaskDefinitionMapperTest {

    @Autowired
    TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ProjectMapper projectMapper;

    public TaskDefinition insertOne() {
        return insertOne(99);
    }

    public TaskDefinition insertOne(int userId) {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setCode(888888L);
        taskDefinition.setName("unit-test");
        taskDefinition.setProjectCode(1L);
        taskDefinition.setTaskType(TaskType.SHELL.getDesc());
        taskDefinition.setUserId(userId);
        taskDefinition.setResourceIds("1");
        taskDefinition.setWorkerGroup("default");
        taskDefinition.setEnvironmentCode(1L);
        taskDefinition.setVersion(1);
        taskDefinition.setCreateTime(new Date());
        taskDefinition.setUpdateTime(new Date());
        taskDefinitionMapper.insert(taskDefinition);
        return taskDefinition;
    }

    @Test
    public void testInsert() {
        TaskDefinition taskDefinition = insertOne();
        Assert.assertNotEquals(taskDefinition.getId(), 0);
    }

    @Test
    public void testQueryByDefinitionName() {
        TaskDefinition taskDefinition = insertOne();
        TaskDefinition result = taskDefinitionMapper.queryByName(taskDefinition.getProjectCode()
                , taskDefinition.getName());

        Assert.assertNotNull(result);
    }

    @Test
    public void testQueryByDefinitionCode() {
        TaskDefinition taskDefinition = insertOne();
        TaskDefinition result = taskDefinitionMapper.queryByCode(taskDefinition.getCode());
        Assert.assertNotNull(result);

    }

    @Test
    public void testQueryAllDefinitionList() {
        TaskDefinition taskDefinition = insertOne();
        List<TaskDefinition> taskDefinitions = taskDefinitionMapper.queryAllDefinitionList(taskDefinition.getProjectCode());
        Assert.assertNotEquals(taskDefinitions.size(), 0);

    }

    @Test
    public void testCountDefinitionGroupByUser() {
        User user = new User();
        user.setUserName("un");
        userMapper.insert(user);
        User un = userMapper.queryByUserNameAccurately("un");
        TaskDefinition taskDefinition = insertOne(un.getId());

        List<DefinitionGroupByUser> users = taskDefinitionMapper.countDefinitionGroupByUser(new Long[]{taskDefinition.getProjectCode()});
        Assert.assertNotEquals(users.size(), 0);

    }

    @Test
    public void testListResources() {
        TaskDefinition taskDefinition = insertOne();
        List<Map<String, Object>> maps = taskDefinitionMapper.listResources();
        Assert.assertNotEquals(maps.size(), 0);

    }

    @Test
    public void testListResourcesByUser() {
        User user = new User();
        user.setUserName("un");
        userMapper.insert(user);
        User un = userMapper.queryByUserNameAccurately("un");
        TaskDefinition taskDefinition = insertOne(un.getId());

        List<Map<String, Object>> maps = taskDefinitionMapper.listResourcesByUser(taskDefinition.getUserId());
        Assert.assertNotEquals(maps.size(), 0);

    }

    @Test
    public void testDeleteByCode() {
        TaskDefinition taskDefinition = insertOne();
        int i = taskDefinitionMapper.deleteByCode(taskDefinition.getCode());
        Assert.assertNotEquals(i, 0);

    }

}
