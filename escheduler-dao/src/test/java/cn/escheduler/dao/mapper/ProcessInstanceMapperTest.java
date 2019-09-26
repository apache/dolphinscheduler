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


import cn.escheduler.dao.entity.ProcessInstance;
import cn.escheduler.dao.entity.ProcessInstanceMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcessInstanceMapperTest {


    @Autowired
    ProcessInstanceMapper processInstanceMapper;


    private ProcessInstance insertOne(){
        //insertOne
        ProcessInstance processInstanceMap = new ProcessInstance();
        processInstanceMapper.insert(processInstanceMap);
        return processInstanceMap;
    }

    @Test
    public void testUpdate(){
        //insertOne
        ProcessInstance processInstanceMap = insertOne();
        //update
        int update = processInstanceMapper.updateById(processInstanceMap);
        Assert.assertEquals(update, 1);
        processInstanceMapper.deleteById(processInstanceMap.getId());
    }

    @Test
    public void testDelete(){
        ProcessInstance processInstanceMap = insertOne();
        int delete = processInstanceMapper.deleteById(processInstanceMap.getId());
        Assert.assertEquals(delete, 1);
    }

    @Test
    public void testQuery() {
        ProcessInstance processInstanceMap = insertOne();
        //query
        List<ProcessInstance> dataSources = processInstanceMapper.selectList(null);
        Assert.assertNotEquals(dataSources.size(), 0);
        processInstanceMapper.deleteById(processInstanceMap.getId());
    }

    @Test
    public void testQueryDetailById() {
    }

    @Test
    public void testQueryByHostAndStatus() {
    }

    @Test
    public void testQueryProcessInstanceListPaging() {
    }

    @Test
    public void testSetFailoverByHostAndStateArray() {
    }

    @Test
    public void testUpdateProcessInstanceByState() {
    }

    @Test
    public void testQueryByTaskId() {
    }

    @Test
    public void testCountInstanceStateByUser() {
    }

    @Test
    public void testQuerySubIdListByParentId() {
    }

    @Test
    public void testQueryByProcessDefineId() {
    }

    @Test
    public void testQueryByScheduleTime() {
    }

    @Test
    public void testQueryLastSchedulerProcess() {
    }

    @Test
    public void testQueryLastRunningProcess() {
    }

    @Test
    public void testQueryLastManualProcess() {
    }
}