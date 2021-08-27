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

import org.apache.dolphinscheduler.dao.entity.EnvironmentWorkerGroupRelation;

import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
public class EnvironmentWorkerGroupRelationMapperTest {

    @Autowired
    EnvironmentWorkerGroupRelationMapper environmentWorkerGroupRelationMapper;

    @Before
    public void setUp() {
        clearTestData();
    }

    @After
    public void after() {
        clearTestData();
    }

    public void clearTestData() {
        environmentWorkerGroupRelationMapper.selectList(null).stream().forEach(environment -> {
            environmentWorkerGroupRelationMapper.deleteById(environment.getId());
        });
    }

    /**
     * insert
     *
     * @return ProcessDefinition
     */
    private EnvironmentWorkerGroupRelation insertOne() {
        //insertOne
        EnvironmentWorkerGroupRelation relation = new EnvironmentWorkerGroupRelation();
        relation.setEnvironmentCode(1L);
        relation.setWorkerGroup("default");
        relation.setOperator(1);
        relation.setUpdateTime(new Date());
        relation.setCreateTime(new Date());
        environmentWorkerGroupRelationMapper.insert(relation);
        return relation;
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        insertOne();
        //query
        List<EnvironmentWorkerGroupRelation> relations = environmentWorkerGroupRelationMapper.selectList(null);
        Assert.assertEquals(relations.size(), 1);
    }

    @Test
    public void testQueryByEnvironmentCode() {
        EnvironmentWorkerGroupRelation relation = insertOne();
        List<EnvironmentWorkerGroupRelation> environmentWorkerGroupRelations = environmentWorkerGroupRelationMapper.queryByEnvironmentCode(1L);
        Assert.assertNotEquals(environmentWorkerGroupRelations.size(), 0);
    }

    @Test
    public void testQueryByWorkerGroupName() {
        EnvironmentWorkerGroupRelation relation = insertOne();
        List<EnvironmentWorkerGroupRelation> environmentWorkerGroupRelations = environmentWorkerGroupRelationMapper.queryByWorkerGroupName("default");
        Assert.assertNotEquals(environmentWorkerGroupRelations.size(), 0);
    }

    @Test
    public void testDeleteByCode() {
        EnvironmentWorkerGroupRelation relation = insertOne();
        int i = environmentWorkerGroupRelationMapper.deleteByCode(1L, "default");
        Assert.assertNotEquals(i, 0);
    }
}
