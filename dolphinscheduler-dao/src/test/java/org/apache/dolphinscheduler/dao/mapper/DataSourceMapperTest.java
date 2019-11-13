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


import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.DatasourceUser;
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
public class DataSourceMapperTest {

    @Autowired
    DataSourceMapper dataSourceMapper;

    @Autowired
    DataSourceUserMapper dataSourceUserMapper;

    /**
     * insert
     * @return DataSource
     */
    private DataSource insertOne(){
        //insertOne
        DataSource dataSource = new DataSource();
        dataSource.setUserId(4);
        dataSource.setName("data source test");
        dataSource.setType(DbType.MYSQL);
        dataSource.setNote("mysql test");
        dataSource.setConnectionParams("hello mysql");
        dataSource.setUpdateTime(new Date());
        dataSource.setCreateTime(new Date());
        dataSourceMapper.insert(dataSource);
        return dataSource;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        DataSource dataSource = insertOne();
        //update
        dataSource.setUpdateTime(new Date());
        int update = dataSourceMapper.updateById(dataSource);
        Assert.assertEquals(update, 1);
        dataSourceMapper.deleteById(dataSource.getId());
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){

        DataSource dataSource = insertOne();
        int delete = dataSourceMapper.deleteById(dataSource.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        DataSource dataSource = insertOne();
        //query
        List<DataSource> dataSources = dataSourceMapper.selectList(null);
        Assert.assertNotEquals(dataSources.size(), 0);
        dataSourceMapper.deleteById(dataSource.getId());
    }

    /**
     * test query datasource by type
     */
    @Test
    public void testQueryDataSourceByType() {
        DataSource dataSource = insertOne();
        //query
        List<DataSource> dataSources = dataSourceMapper.queryDataSourceByType(
                0, DbType.MYSQL.ordinal()
        );
        Assert.assertNotEquals(dataSources.size(), 0);
        dataSourceMapper.deleteById(dataSource.getId());
    }

    /**
     * test page
     */
    @Test
    public void testSelectPaging() {
        DataSource dataSource = insertOne();
        Page page = new Page(1, 3);
        IPage<DataSource> dataSourceIPage = dataSourceMapper.selectPaging(page,
                4, null);
        Assert.assertNotEquals(dataSourceIPage.getTotal(), 0);
        dataSourceMapper.deleteById(dataSource.getId());
    }

    /**
     * test query datasource by name
     */
    @Test
    public void testQueryDataSourceByName() {
        DataSource dataSource = insertOne();
        List<DataSource> dataSources = dataSourceMapper.queryDataSourceByName("data source test");
        Assert.assertNotEquals(dataSources.size(), 0);
        dataSourceMapper.deleteById(dataSource.getId());
    }

    /**
     * test query authed datasource
     */
    @Test
    public void testQueryAuthedDatasource() {

        DataSource dataSource = insertOne();
        DatasourceUser datasourceUser = new DatasourceUser();
        datasourceUser.setUserId(3);
        datasourceUser.setDatasourceId(dataSource.getId());
        dataSourceUserMapper.insert(datasourceUser);

        List<DataSource> dataSources = dataSourceMapper.queryAuthedDatasource(3);
        Assert.assertNotEquals(dataSources.size(), 0);
        dataSourceMapper.deleteById(dataSource.getId());
        dataSourceUserMapper.deleteById(datasourceUser.getId());
    }

    /**
     * test query datasource except userId
     */
    @Test
    public void testQueryDatasourceExceptUserId() {
        DataSource dataSource = insertOne();
        List<DataSource> dataSources = dataSourceMapper.queryDatasourceExceptUserId(3);
        Assert.assertNotEquals(dataSources.size(), 0);
        dataSourceMapper.deleteById(dataSource.getId());
    }

    /**
     * test list all datasource by type
     */
    @Test
    public void testListAllDataSourceByType() {

        DataSource dataSource = insertOne();

        List<DataSource> dataSources = dataSourceMapper.queryDataSourceByType(4, DbType.MYSQL.ordinal());
        Assert.assertNotEquals(dataSources.size(), 0);
        List<DataSource> dataSources2 = dataSourceMapper.queryDataSourceByType(10091, DbType.MYSQL.ordinal());
        Assert.assertEquals(dataSources2.size(), 0);
        dataSourceMapper.deleteById(dataSource.getId());
    }
}