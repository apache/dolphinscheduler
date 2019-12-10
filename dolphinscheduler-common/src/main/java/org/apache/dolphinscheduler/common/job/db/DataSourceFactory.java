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
package org.apache.dolphinscheduler.common.job.db;

import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * produce datasource in this custom defined datasource factory.
 */
public class DataSourceFactory {

  private static final Logger logger = LoggerFactory.getLogger(DataSourceFactory.class);

  public static BaseDataSource getDatasource(DbType dbType, String parameter) {
    try {
      switch (dbType) {
        case MYSQL:
          return JSONUtils.parseObject(parameter, MySQLDataSource.class);
        case POSTGRESQL:
          return JSONUtils.parseObject(parameter, PostgreDataSource.class);
        case HIVE:
          return JSONUtils.parseObject(parameter, HiveDataSource.class);
        case SPARK:
          return JSONUtils.parseObject(parameter, SparkDataSource.class);
        case CLICKHOUSE:
          return JSONUtils.parseObject(parameter, ClickHouseDataSource.class);
        case ORACLE:
          return JSONUtils.parseObject(parameter, OracleDataSource.class);
        case SQLSERVER:
          return JSONUtils.parseObject(parameter, SQLServerDataSource.class);
        case DB2:
          return JSONUtils.parseObject(parameter, DB2ServerDataSource.class);
        default:
          return null;
      }
    } catch (Exception e) {
      logger.error("get datasource object error", e);
      return null;
    }
  }

  /**
   * load class
   * @param dbType
   * @throws Exception
   */
  public static void loadClass(DbType dbType) throws Exception{
    switch (dbType){
      case MYSQL :
        Class.forName(Constants.JDBC_MYSQL_CLASS_NAME);
        break;
      case POSTGRESQL :
        Class.forName(Constants.JDBC_POSTGRESQL_CLASS_NAME);
        break;
      case HIVE :
        Class.forName(Constants.JDBC_HIVE_CLASS_NAME);
        break;
      case SPARK :
        Class.forName(Constants.JDBC_SPARK_CLASS_NAME);
        break;
      case CLICKHOUSE :
        Class.forName(Constants.JDBC_CLICKHOUSE_CLASS_NAME);
        break;
      case ORACLE :
        Class.forName(Constants.JDBC_ORACLE_CLASS_NAME);
        break;
      case SQLSERVER:
        Class.forName(Constants.JDBC_SQLSERVER_CLASS_NAME);
        break;
      case DB2:
        Class.forName(Constants.JDBC_DB2_CLASS_NAME);
        break;
      default:
        logger.error("not support sql type: {},can't load class", dbType);
        throw new IllegalArgumentException("not support sql type,can't load class");

    }
  }
}
