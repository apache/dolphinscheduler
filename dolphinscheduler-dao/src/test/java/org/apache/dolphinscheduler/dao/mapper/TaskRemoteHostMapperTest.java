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
import org.apache.dolphinscheduler.dao.entity.TaskRemoteHost;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class TaskRemoteHostMapperTest extends BaseDaoTest {

    @Autowired
    private TaskRemoteHostMapper taskRemoteHostMapper;

    private TaskRemoteHost insertOne() {
        TaskRemoteHost taskRemoteHost = new TaskRemoteHost();
        taskRemoteHost.setCode(1L);
        taskRemoteHost.setName("app01 server");
        taskRemoteHost.setIp("localhost");
        taskRemoteHost.setPort(22);
        taskRemoteHost.setAccount("foo");
        taskRemoteHost.setPassword("foo123");
        taskRemoteHost.setDescription("app01 server description");
        taskRemoteHost.setOperator(1);
        taskRemoteHostMapper.insert(taskRemoteHost);
        return taskRemoteHost;
    }

    @BeforeEach
    public void setUp() {
        clearTestData();
    }

    @AfterEach
    public void after() {
        clearTestData();
    }

    public void clearTestData() {
        taskRemoteHostMapper.queryAllTaskRemoteHostList().forEach(taskRemoteHost -> {
            taskRemoteHostMapper.deleteByCode(taskRemoteHost.getCode());
        });
    }

    @Test
    public void testUpdate() {
        TaskRemoteHost taskRemoteHost = insertOne();
        taskRemoteHost.setDescription("update description");
        int update = taskRemoteHostMapper.updateById(taskRemoteHost);
        Assertions.assertEquals(1, update);
    }

    @Test
    public void testDelete() {
        TaskRemoteHost taskRemoteHost = insertOne();
        int delete = taskRemoteHostMapper.deleteById(taskRemoteHost);
        Assertions.assertEquals(1, delete);
    }

    @Test
    public void testQueryByTaskRemoteHostName() {
        TaskRemoteHost taskRemoteHost = insertOne();
        TaskRemoteHost result = taskRemoteHostMapper.queryByTaskRemoteHostName(taskRemoteHost.getName());
        Assertions.assertEquals(taskRemoteHost.getName(), result.getName());
    }

    @Test
    public void testQueryByTaskRemoteHostCode() {
        TaskRemoteHost taskRemoteHost = insertOne();
        TaskRemoteHost result = taskRemoteHostMapper.queryByTaskRemoteHostCode(taskRemoteHost.getCode());
        Assertions.assertEquals(taskRemoteHost.getCode(), result.getCode());
    }

    @Test
    public void testQueryTaskRemoteHostListPaging() {
        TaskRemoteHost entity = insertOne();
        Page<TaskRemoteHost> page = new Page<>(1, 10);
        IPage<TaskRemoteHost> taskRemoteHostIPage = taskRemoteHostMapper.queryTaskRemoteHostListPaging(page, "");
        List<TaskRemoteHost> taskRemoteHostList = taskRemoteHostIPage.getRecords();
        Assertions.assertEquals(1, taskRemoteHostList.size());

        taskRemoteHostIPage = taskRemoteHostMapper.queryTaskRemoteHostListPaging(page, "abc");
        taskRemoteHostList = taskRemoteHostIPage.getRecords();
        Assertions.assertEquals(0, taskRemoteHostList.size());
    }
}
