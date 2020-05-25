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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbConnectType;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.junit.Assert;
import org.junit.Test;

public class OracleDataSourceTest {

    @Test
    public void getJdbcUrl() {
        //Oracle JDBC Thin ServiceName:Method One
        OracleDataSource oracleDataSource = new OracleDataSource();
        oracleDataSource.setType(DbConnectType.ORACLE_SERVICE_NAME);
        oracleDataSource.setAddress("jdbc:oracle:thin:@//127.0.0.1:1521");
        oracleDataSource.setDatabase("test");
        oracleDataSource.setPassword("123456");
        oracleDataSource.setUser("test");
        Assert.assertEquals("jdbc:oracle:thin:@//127.0.0.1:1521/test",
                oracleDataSource.getJdbcUrl());
        //set fake principal
        oracleDataSource.setPrincipal("fake principal");
        Assert.assertEquals("jdbc:oracle:thin:@//127.0.0.1:1521/test",
                oracleDataSource.getJdbcUrl());

        //Oracle JDBC Thin ServiceName:Method Two
        oracleDataSource.setAddress("jdbc:oracle:thin:@127.0.0.1:1521");
        Assert.assertEquals("jdbc:oracle:thin:@127.0.0.1:1521/test",
                oracleDataSource.getJdbcUrl());
        //set fake principal
        oracleDataSource.setPrincipal("fake principal");
        Assert.assertEquals("jdbc:oracle:thin:@127.0.0.1:1521/test",
                oracleDataSource.getJdbcUrl());


        //Oracle JDBC Thin using SID
        OracleDataSource oracleDataSource2 = new OracleDataSource();
        oracleDataSource2.setType(DbConnectType.ORACLE_SID);
        oracleDataSource2.setAddress("jdbc:oracle:thin:@127.0.0.1:1521");
        oracleDataSource2.setDatabase("test");
        oracleDataSource2.setPassword("123456");
        oracleDataSource2.setUser("test");
        Assert.assertEquals("jdbc:oracle:thin:@127.0.0.1:1521:test",
                oracleDataSource2.getJdbcUrl());
        //set fake principal
        oracleDataSource2.setPrincipal("fake principal");
        Assert.assertEquals("jdbc:oracle:thin:@127.0.0.1:1521:test",
                oracleDataSource2.getJdbcUrl());
    }

    @Test
    public void oracleDBConnectType() {
        String oracleConnectType = "type";
        Assert.assertEquals(Constants.ORACLE_DB_CONNECT_TYPE, oracleConnectType);
    }

    @Test
    public void getType() {
        OracleDataSource oracleDataSource = new OracleDataSource();
        oracleDataSource.setType(DbConnectType.ORACLE_SERVICE_NAME);
        Assert.assertEquals(DbConnectType.ORACLE_SERVICE_NAME, oracleDataSource.getType());
    }

    @Test
    public void driverClassSelector() {
        OracleDataSource oracleDataSource = new OracleDataSource();
        Assert.assertEquals(Constants.COM_ORACLE_JDBC_DRIVER, oracleDataSource.driverClassSelector());
    }

    @Test
    public void dbTypeSelector() {
        OracleDataSource oracleDataSource = new OracleDataSource();
        Assert.assertEquals(DbType.ORACLE, oracleDataSource.dbTypeSelector());
    }
}