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
package cn.escheduler.dao.mapper;

import cn.escheduler.dao.datasource.ConnectionFactory;
import cn.escheduler.dao.model.WorkerGroup;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

/**
 * worker group mapper test
 */
public class WorkerGroupMapperTest {

    WorkerGroupMapper workerGroupMapper;


    @Before
    public void before() {
        workerGroupMapper = ConnectionFactory.getSqlSession().getMapper(WorkerGroupMapper.class);
    }


    @Test
    public void test() {
        WorkerGroup workerGroup = new WorkerGroup();

        String name = "workerGroup3";
        workerGroup.setName(name);
        workerGroup.setIpList("192.168.220.154,192.168.220.188");
        workerGroup.setCreateTime(new Date());
        workerGroup.setUpdateTime(new Date());
        workerGroupMapper.insert(workerGroup);
        Assert.assertNotEquals(workerGroup.getId(), 0);

        List<WorkerGroup> workerGroups2 = workerGroupMapper.queryWorkerGroupByName(name);
        Assert.assertEquals(workerGroups2.size(), 1);

        workerGroup.setName("workerGroup11");
        workerGroupMapper.update(workerGroup);

        List<WorkerGroup> workerGroups = workerGroupMapper.queryAllWorkerGroup();
        Assert.assertNotEquals(workerGroups.size(), 0);

        workerGroupMapper.deleteById(workerGroup.getId());

        workerGroups = workerGroupMapper.queryAllWorkerGroup();
        Assert.assertEquals(workerGroups.size(), 0);
    }

}