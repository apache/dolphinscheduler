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

import cn.escheduler.common.enums.DbType;
import cn.escheduler.dao.datasource.ConnectionFactory;
import cn.escheduler.dao.model.DataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

/**
 * datasource mapper test
 */
public class DataSourceMapperTest {


    DataSourceMapper dataSourceMapper;

    @Before
    public void before(){
        dataSourceMapper = ConnectionFactory.getSqlSession().getMapper(DataSourceMapper.class);
    }


    @Test
    public void testMapper(){

        DataSource dataSource = new DataSource();
        dataSource.setType(DbType.MYSQL);
        dataSource.setName("data source");
        dataSource.setConnectionParams("connections");
        dataSource.setNote("mysql test");
        dataSource.setCreateTime(new Date());
        dataSource.setUpdateTime(new Date());

        dataSourceMapper.insert(dataSource);
        Assert.assertNotEquals(dataSource.getId(), 0);

        List<DataSource> dataSources = dataSourceMapper.queryAllDataSourcePaging("", 0, 30);
        DataSource findDataSource = null;
        for(DataSource dataSource1 : dataSources){
            if(dataSource1.getId() == dataSource.getId()){
                findDataSource = dataSource1;
            }
        }

        Assert.assertNotEquals(findDataSource, null);
        int delete = dataSourceMapper.deleteDataSourceById(dataSource.getId());
        Assert.assertEquals(delete, 1);

    }
}
