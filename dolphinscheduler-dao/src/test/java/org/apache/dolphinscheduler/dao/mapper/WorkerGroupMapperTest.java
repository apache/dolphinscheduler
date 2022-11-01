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
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorkerGroupMapperTest extends BaseDaoTest {

    @Resource
    private WorkerGroupMapper workerGroupMapper;

    @BeforeEach
    public void setUp() {
        clearTestData();
    }

    @AfterEach
    public void after() {
        clearTestData();
    }

    /**
     * clear all test data
     */
    public void clearTestData() {
        workerGroupMapper.queryAllWorkerGroup()
                .forEach(workerGroup -> workerGroupMapper.deleteById(workerGroup.getId()));
    }

    /**
     * insert a worker group into DB
     *
     * @return worker group
     */
    private WorkerGroup insertOneWorkerGroup() {
        WorkerGroup workerGroup = new WorkerGroup();
        workerGroup.setName("Server1");
        workerGroup.setDescription("Server1");
        workerGroup.setCreateTime(new Date());
        workerGroup.setUpdateTime(new Date());
        workerGroup.setSystemDefault(true);
        workerGroup.setOtherParamsJson("");
        workerGroup.setAddrList("localhost");
        workerGroupMapper.insert(workerGroup);
        return workerGroup;
    }

    /**
     * test query all worker groups
     */
    @Test
    public void testQueryAllWorkerGroups() {
        insertOneWorkerGroup();
        List<WorkerGroup> workerGroups = workerGroupMapper.queryAllWorkerGroup();
        Assertions.assertEquals(1, workerGroups.size());
    }

    /**
     * test query worker group by name
     */
    @Test
    public void testQueryWorkerGroupByName() {
        WorkerGroup workerGroup = insertOneWorkerGroup();
        List<WorkerGroup> workerGroups = workerGroupMapper.queryWorkerGroupByName(workerGroup.getName());
        Assertions.assertEquals(1, workerGroups.size());

        workerGroups = workerGroupMapper.queryWorkerGroupByName("server2");
        Assertions.assertEquals(0, workerGroups.size());
    }

    /**
     * test update workerGroup
     */
    @Test
    public void testUpdate() {
        WorkerGroup workerGroup = insertOneWorkerGroup();
        workerGroup.setDescription("Server Update");
        int update = workerGroupMapper.updateById(workerGroup);
        Assertions.assertEquals(1, update);
    }

    /**
     * test delete workerGroup
     */
    @Test
    public void delete() {
        WorkerGroup workerGroup = insertOneWorkerGroup();
        int delete = workerGroupMapper.deleteById(workerGroup.getId());
        Assertions.assertEquals(1, delete);
    }
}
