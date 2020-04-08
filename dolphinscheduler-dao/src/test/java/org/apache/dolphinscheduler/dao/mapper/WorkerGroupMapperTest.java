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


import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkerGroupMapperTest {
    @Autowired
    WorkerGroupMapper workerGroupMapper;

    /**
     * insert
     * @return WorkerGroup
     */
    private WorkerGroup insertOne(){
        //insertOne
        WorkerGroup workerGroup = new WorkerGroup();

        String name = "workerGroup3";
        workerGroup.setName(name);
        workerGroup.setIpList("192.168.220.154,192.168.220.188");
        workerGroup.setCreateTime(new Date());
        workerGroup.setUpdateTime(new Date());
        workerGroupMapper.insert(workerGroup);
        return workerGroup;
    }


    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        WorkerGroup workerGroup = insertOne();
        //update
        workerGroup.setName("workerGroup11");
        int update = workerGroupMapper.updateById(workerGroup);
        workerGroupMapper.deleteById(workerGroup.getId());
        Assert.assertEquals(1, update);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        //insertOne
        WorkerGroup workerGroup = insertOne();
        //delete
        int delete = workerGroupMapper.deleteById(workerGroup.getId());
        Assert.assertEquals(1, delete);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        //insertOne
        WorkerGroup workerGroup = insertOne();
        //query
        List<WorkerGroup> workerGroupList = workerGroupMapper.selectList(null);
        Assert.assertNotEquals(workerGroupList.size(), 0);
        workerGroupMapper.deleteById(workerGroup.getId());
    }

    /**
     * test query all worker group
     */
    @Test
    public void testQueryAllWorkerGroup() {
        //insertOne
        WorkerGroup workerGroup = insertOne();
        //queryAllWorkerGroup
        List<WorkerGroup> workerGroupList = workerGroupMapper.queryAllWorkerGroup();
        Assert.assertNotEquals(workerGroupList.size(), 0);
        workerGroupMapper.deleteById(workerGroup.getId());
    }

    /**
     * test query work group by name
     */
    @Test
    public void testQueryWorkerGroupByName() {
        //insertOne
        WorkerGroup workerGroup = insertOne();
        //queryWorkerGroupByName
        List<WorkerGroup> workerGroupList = workerGroupMapper.queryWorkerGroupByName(workerGroup.getName());
        Assert.assertNotEquals(workerGroupList.size(), 0);
        workerGroupMapper.deleteById(workerGroup.getId());
    }

    /**
     * test page
     */
    @Test
    public void testQueryListPaging() {
        //insertOne
        WorkerGroup workerGroup = insertOne();
        //queryListPaging
        Page<WorkerGroup> page = new Page(1,3);
        IPage<WorkerGroup> workerGroupIPage = workerGroupMapper.queryListPaging(page, workerGroup.getName());
        Assert.assertNotEquals(workerGroupIPage.getTotal(), 0);
        workerGroupMapper.deleteById(workerGroup.getId());
    }
}