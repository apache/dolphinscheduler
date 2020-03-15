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
import org.junit.Assert;
import org.junit.Test;

public class BaseDataSourceTest {

  @Test
  public void testDriverClassSelector() {
    String mysqlDriverClass = new MySQLDataSource().driverClassSelector();
    Assert.assertEquals(Constants.COM_MYSQL_JDBC_DRIVER, mysqlDriverClass);

    String clickHouseDriverClass = new ClickHouseDataSource().driverClassSelector();
    Assert.assertEquals(Constants.COM_CLICKHOUSE_JDBC_DRIVER, clickHouseDriverClass);

    String db2ServerDriverClass = new DB2ServerDataSource().driverClassSelector();
    Assert.assertEquals(Constants.COM_DB2_JDBC_DRIVER, db2ServerDriverClass);

    String oracleDriverClass = new OracleDataSource().driverClassSelector();
    Assert.assertEquals(Constants.COM_ORACLE_JDBC_DRIVER, oracleDriverClass);

    String postgreDriverClass = new PostgreDataSource().driverClassSelector();
    Assert.assertEquals(Constants.ORG_POSTGRESQL_DRIVER, postgreDriverClass);

    String sqlServerDriverClass = new SQLServerDataSource().driverClassSelector();
    Assert.assertEquals(Constants.COM_SQLSERVER_JDBC_DRIVER, sqlServerDriverClass);

    String hiveDriverClass = new HiveDataSource().driverClassSelector();
    Assert.assertEquals(Constants.ORG_APACHE_HIVE_JDBC_HIVE_DRIVER, hiveDriverClass);

    String sparkDriverClass = new SparkDataSource().driverClassSelector();
    Assert.assertEquals(Constants.ORG_APACHE_HIVE_JDBC_HIVE_DRIVER, sparkDriverClass);
  }

  @Test
  public void testGetJdbcUrl() {
    BaseDataSource hiveDataSource = new HiveDataSource();
    hiveDataSource.setAddress("jdbc:hive2://127.0.0.1:10000");
    hiveDataSource.setDatabase("test");
    hiveDataSource.setPassword("123456");
    hiveDataSource.setUser("test");
    Assert.assertEquals("jdbc:hive2://127.0.0.1:10000/test", hiveDataSource.getJdbcUrl());
    //set principal
    hiveDataSource.setPrincipal("hive/test.com@TEST.COM");
    Assert.assertEquals("jdbc:hive2://127.0.0.1:10000/test;principal=hive/test.com@TEST.COM",
        hiveDataSource.getJdbcUrl());
    //set fake other
    hiveDataSource.setOther("charset=UTF-8");
    Assert.assertEquals(
        "jdbc:hive2://127.0.0.1:10000/test;principal=hive/test.com@TEST.COM;charset=UTF-8",
        hiveDataSource.getJdbcUrl());

    BaseDataSource clickHouseDataSource = new ClickHouseDataSource();
    clickHouseDataSource.setAddress("jdbc:clickhouse://127.0.0.1:8123");
    clickHouseDataSource.setDatabase("test");
    clickHouseDataSource.setPassword("123456");
    clickHouseDataSource.setUser("test");
    Assert.assertEquals("jdbc:clickhouse://127.0.0.1:8123/test", clickHouseDataSource.getJdbcUrl());
    //set fake principal
    clickHouseDataSource.setPrincipal("fake principal");
    Assert.assertEquals("jdbc:clickhouse://127.0.0.1:8123/test", clickHouseDataSource.getJdbcUrl());
    //set fake other
    clickHouseDataSource.setOther("charset=UTF-8");
    Assert.assertEquals("jdbc:clickhouse://127.0.0.1:8123/test?charset=UTF-8",
        clickHouseDataSource.getJdbcUrl());

    BaseDataSource sqlServerDataSource = new SQLServerDataSource();
    sqlServerDataSource.setAddress("jdbc:sqlserver://127.0.0.1:1433");
    sqlServerDataSource.setDatabase("test");
    sqlServerDataSource.setPassword("123456");
    sqlServerDataSource.setUser("test");
    Assert.assertEquals("jdbc:sqlserver://127.0.0.1:1433;databaseName=test",
        sqlServerDataSource.getJdbcUrl());
    //set fake principal
    sqlServerDataSource.setPrincipal("fake principal");
    Assert.assertEquals("jdbc:sqlserver://127.0.0.1:1433;databaseName=test",
        sqlServerDataSource.getJdbcUrl());
    //set fake other
    sqlServerDataSource.setOther("charset=UTF-8");
    Assert.assertEquals("jdbc:sqlserver://127.0.0.1:1433;databaseName=test;charset=UTF-8",
        sqlServerDataSource.getJdbcUrl());

    BaseDataSource db2DataSource = new DB2ServerDataSource();
    db2DataSource.setAddress("jdbc:db2://127.0.0.1:50000");
    db2DataSource.setDatabase("test");
    db2DataSource.setPassword("123456");
    db2DataSource.setUser("test");
    Assert.assertEquals("jdbc:db2://127.0.0.1:50000/test", db2DataSource.getJdbcUrl());
    //set fake principal
    db2DataSource.setPrincipal("fake principal");
    Assert.assertEquals("jdbc:db2://127.0.0.1:50000/test", db2DataSource.getJdbcUrl());
    //set fake other
    db2DataSource.setOther("charset=UTF-8");
    Assert.assertEquals("jdbc:db2://127.0.0.1:50000/test:charset=UTF-8", db2DataSource.getJdbcUrl());


  }
}
