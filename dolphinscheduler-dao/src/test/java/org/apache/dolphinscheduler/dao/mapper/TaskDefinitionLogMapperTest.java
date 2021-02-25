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
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;

import java.util.Date;

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
@Rollback(false)
public class TaskDefinitionLogMapperTest {


    @Autowired
    TaskDefinitionLogMapper taskDefinitionLogMapper;

    public TaskDefinitionLog insertOne(){
        TaskDefinitionLog taskDefinition = new TaskDefinitionLog();
        taskDefinition.setCode(888888L);
        taskDefinition.setName("unit-test");
        taskDefinition.setProjectCode(111111L);
        taskDefinition.setTaskType(TaskType.SHELL);
        taskDefinition.setUserId(99);
        taskDefinition.setVersion(1);
        taskDefinition.setCreateTime(new Date());
        taskDefinition.setUpdateTime(new Date());
        taskDefinitionLogMapper.insert(taskDefinition);
        return taskDefinition;
    }

    @Test
    public void testInsert(){
        TaskDefinitionLog taskDefinitionLog = insertOne();
        Assert.assertNotEquals(taskDefinitionLog.getId(), 0);
    }
}
