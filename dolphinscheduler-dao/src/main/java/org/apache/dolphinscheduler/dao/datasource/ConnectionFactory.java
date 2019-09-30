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

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.dolphinscheduler.dao.config.YmlConfig;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Map;


/**
 * data source connection factory
 */
public class ConnectionFactory {
  private static final Logger logger = LoggerFactory.getLogger(ConnectionFactory.class);

  private static SqlSessionFactory sqlSessionFactory;

  /**
   * get the data source
   */
  public static DruidDataSource getDataSource() {
    DruidDataSource druidDataSource = new DruidDataSource();
    Map<String, String> allMap = YmlConfig.allMap;
    druidDataSource.setDriverClassName(allMap.get("spring.datasource.driver-class-name"));
    druidDataSource.setUrl(allMap.get("spring.datasource.url"));
    druidDataSource.setUsername(allMap.get("spring.datasource.username"));
    druidDataSource.setPassword(allMap.get("spring.datasource.password"));
    druidDataSource.setInitialSize(5);
    druidDataSource.setMinIdle(5);
    druidDataSource.setMaxActive(20);
    druidDataSource.setMaxWait(60000);
    druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
    druidDataSource.setMinEvictableIdleTimeMillis(300000);
    druidDataSource.setValidationQuery("SELECT 1");
    return druidDataSource;
  }

  /**
   * get sql session factory
   */
  public static SqlSessionFactory getSqlSessionFactory() {
    if (sqlSessionFactory == null) {
      synchronized (ConnectionFactory.class) {
        if (sqlSessionFactory == null) {
          DataSource dataSource = getDataSource();
          TransactionFactory transactionFactory = new JdbcTransactionFactory();

          Environment environment = new Environment("development", transactionFactory, dataSource);

          Configuration configuration = new Configuration(environment);
          configuration.setLazyLoadingEnabled(true);
          configuration.addMappers("org.apache.dolphinscheduler.dao.mapper");


          SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
          sqlSessionFactory = builder.build(configuration);
        }
      }
    }

    return sqlSessionFactory;
  }

  /**
   * get sql session
   */
  public static SqlSession getSqlSession() {
    return new SqlSessionTemplate(getSqlSessionFactory());
  }

  public static <T> T getMapper(Class<T> type){
    return getSqlSession().getMapper(type);
  }
}
