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
  public void testIsConnectable() throws Exception {
    //TODO
  }

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
}
