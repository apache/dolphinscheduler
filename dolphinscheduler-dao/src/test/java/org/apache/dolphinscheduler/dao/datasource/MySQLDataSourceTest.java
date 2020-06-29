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
package org.apache.dolphinscheduler.dao.datasource;

import org.junit.Assert;
import org.junit.Test;

/**
 * test data source of mySQL
 */
public class MySQLDataSourceTest {

    @Test
    public void testGetUser(){
        MySQLDataSource dataSource = new MySQLDataSource();
        String safeUsername= "test123";
        dataSource.setUser(safeUsername);
        Assert.assertEquals("test123", dataSource.getUser());
        String sensitiveUsername= "test123?autoDeserialize=true";
        dataSource.setUser(sensitiveUsername);
        Assert.assertEquals("test123?", dataSource.getUser());
    }

    @Test
    public void testGetPassword(){
        MySQLDataSource dataSource = new MySQLDataSource();
        String safePwd= "test_pwd";
        dataSource.setPassword(safePwd);
        Assert.assertEquals("test_pwd", dataSource.getPassword());
        String sensitivePwd= "test_pwd?autoDeserialize=true";
        dataSource.setPassword(sensitivePwd);
        Assert.assertEquals("test_pwd?", dataSource.getPassword());
    }

    @Test
    public void testFilterOther(){
        MySQLDataSource dataSource = new MySQLDataSource();
        String other = dataSource.filterOther("serverTimezone=Asia/Shanghai&characterEncoding=utf8");
        Assert.assertEquals("serverTimezone=Asia/Shanghai&characterEncoding=utf8", other);
        //at the first
        other = dataSource.filterOther("autoDeserialize=true&serverTimezone=Asia/Shanghai&characterEncoding=utf8");
        Assert.assertEquals("serverTimezone=Asia/Shanghai&characterEncoding=utf8", other);
        //at the end
        other = dataSource.filterOther("serverTimezone=Asia/Shanghai&characterEncoding=utf8&autoDeserialize=true");
        Assert.assertEquals("serverTimezone=Asia/Shanghai&characterEncoding=utf8", other);
        //in the middle
        other = dataSource.filterOther("serverTimezone=Asia/Shanghai&autoDeserialize=true&characterEncoding=utf8");
        Assert.assertEquals("serverTimezone=Asia/Shanghai&characterEncoding=utf8", other);
    }
}
