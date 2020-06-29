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


import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class ProcessInstanceMapMapperTest {


    @Autowired
    ProcessInstanceMapMapper processInstanceMapMapper;


    /**
     * insert
     * @return ProcessInstanceMap
     */
    private ProcessInstanceMap insertOne(){
        //insertOne
        ProcessInstanceMap processInstanceMap = new ProcessInstanceMap();
        processInstanceMap.setProcessInstanceId(0);
        processInstanceMap.setParentTaskInstanceId(0);
        processInstanceMap.setParentProcessInstanceId(0);
        processInstanceMapMapper.insert(processInstanceMap);
        return processInstanceMap;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        ProcessInstanceMap processInstanceMap = insertOne();
        //update
        processInstanceMap.setParentProcessInstanceId(1);
        int update = processInstanceMapMapper.updateById(processInstanceMap);
        Assert.assertEquals(1, update);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        ProcessInstanceMap processInstanceMap = insertOne();
        int delete = processInstanceMapMapper.deleteById(processInstanceMap.getId());
        Assert.assertEquals(1, delete);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        ProcessInstanceMap processInstanceMap = insertOne();
        //query
        List<ProcessInstanceMap> dataSources = processInstanceMapMapper.selectList(null);
        Assert.assertNotEquals(dataSources.size(), 0);
    }

    /**
     * test query by process instance parentId
     */
    @Test
    public void testQueryByParentId() {
        ProcessInstanceMap processInstanceMap = insertOne();

        processInstanceMap.setParentProcessInstanceId(100);
        processInstanceMapMapper.updateById(processInstanceMap);


    }



    /**
     * test delete by parent process instance id
     */
    @Test
    public void testDeleteByParentProcessId() {
        ProcessInstanceMap processInstanceMap = insertOne();

        processInstanceMap.setParentProcessInstanceId(100);
        processInstanceMapMapper.updateById(processInstanceMap);
        int delete = processInstanceMapMapper.deleteByParentProcessId(
                processInstanceMap.getParentProcessInstanceId()
        );
        Assert.assertEquals(1, delete);
    }

    /**
     *
     * test query sub ids by process instance parentId
     */
    @Test
    public void querySubIdListByParentId() {
        ProcessInstanceMap processInstanceMap = insertOne();
        processInstanceMap.setProcessInstanceId(1);
        processInstanceMap.setParentProcessInstanceId(1010);

        processInstanceMapMapper.updateById(processInstanceMap);

        List<Integer> subIds = processInstanceMapMapper.querySubIdListByParentId(processInstanceMap.getParentProcessInstanceId());

        Assert.assertNotEquals(subIds.size(), 0);


    }
}