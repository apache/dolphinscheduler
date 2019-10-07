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


import org.apache.dolphinscheduler.dao.entity.DatasourceUser;
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
public class DataSourceUserMapperTest {

    @Autowired
    DataSourceUserMapper dataSourceUserMapper;


    private DatasourceUser insertOne(){
        //insertOne
        DatasourceUser dataSourceUser = new DatasourceUser();
        dataSourceUser.setUserId(4);
        dataSourceUser.setDatasourceId(1010);
        dataSourceUser.setPerm(7);
        dataSourceUser.setUpdateTime(new Date());
        dataSourceUser.setCreateTime(new Date());
        dataSourceUserMapper.insert(dataSourceUser);
        return dataSourceUser;
    }

    @Test
    public void testUpdate(){
        //insertOne
        DatasourceUser dataSourceUser = insertOne();
        //update
        dataSourceUser.setUpdateTime(new Date());
        int update = dataSourceUserMapper.updateById(dataSourceUser);
        Assert.assertEquals(update, 1);
        dataSourceUserMapper.deleteById(dataSourceUser.getId());
    }

    @Test
    public void testDelete(){

        DatasourceUser dataSourceUser = insertOne();
        int delete = dataSourceUserMapper.deleteById(dataSourceUser.getId());
        Assert.assertEquals(delete, 1);
    }

    @Test
    public void testQuery() {
        DatasourceUser dataSourceUser = insertOne();
        //query
        List<DatasourceUser> dataSources = dataSourceUserMapper.selectList(null);
        Assert.assertNotEquals(dataSources.size(), 0);
        dataSourceUserMapper.deleteById(dataSourceUser.getId());
    }

    @Test
    public void testDeleteByUserId() {
        DatasourceUser dataSourceUser = insertOne();
        int delete = dataSourceUserMapper.deleteByUserId(dataSourceUser.getUserId());
        Assert.assertNotEquals(delete, 0);
    }

    @Test
    public void testDeleteByDatasourceId() {
        DatasourceUser dataSourceUser = insertOne();
        int delete = dataSourceUserMapper.deleteByDatasourceId(dataSourceUser.getDatasourceId());
        Assert.assertNotEquals(delete, 0);
    }
}