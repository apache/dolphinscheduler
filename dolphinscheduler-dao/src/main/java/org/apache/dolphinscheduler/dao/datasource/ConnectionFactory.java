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
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;


/**
 * data source connection factory
 */
public class ConnectionFactory {
  private static final Logger logger = LoggerFactory.getLogger(ConnectionFactory.class);

  private static SqlSessionFactory sqlSessionFactory;

  /**
   * Load configuration file
   */
  private static org.apache.commons.configuration.Configuration conf;

  static {
    try {
      conf = new PropertiesConfiguration(Constants.DATA_SOURCE_PROPERTIES);
    }catch (ConfigurationException e){
      logger.error("load configuration excetpion",e);
      System.exit(1);
    }
  }

  /**
   * get the data source
   */
  public static DruidDataSource getDataSource() {
    DruidDataSource druidDataSource = new DruidDataSource();
//    Map<String, String> allMap = YmlConfig.allMap;
    druidDataSource.setDriverClassName(conf.getString("spring.datasource.driver-class-name"));
    druidDataSource.setUrl(conf.getString("spring.datasource.url"));
    druidDataSource.setUsername(conf.getString("spring.datasource.username"));
    druidDataSource.setPassword(conf.getString("spring.datasource.password"));
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
  public static SqlSessionFactory getSqlSessionFactory() throws Exception {
    if (sqlSessionFactory == null) {
      synchronized (ConnectionFactory.class) {
        if (sqlSessionFactory == null) {
          DataSource dataSource = getDataSource();
          TransactionFactory transactionFactory = new JdbcTransactionFactory();

          Environment environment = new Environment("development", transactionFactory, dataSource);

          MybatisConfiguration configuration = new MybatisConfiguration();
          configuration.setEnvironment(environment);
          configuration.setLazyLoadingEnabled(true);
          configuration.addMappers("org.apache.dolphinscheduler.dao.mapper");

          MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
          sqlSessionFactoryBean.setDataSource(dataSource);
          sqlSessionFactoryBean.setTypeEnumsPackage("org.apache.dolphinscheduler.*.enums");
          sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:org/apache/dolphinscheduler/dao/mapper/*.xml"));
          sqlSessionFactoryBean.setConfiguration(configuration);
          return sqlSessionFactoryBean.getObject();
        }
      }
    }

    return sqlSessionFactory;
  }

  /**
   * get sql session
   */
  public static SqlSession getSqlSession() {
    try {
      return new SqlSessionTemplate(getSqlSessionFactory());
    } catch (Exception e) {
      logger.error(e.getMessage(),e);
      throw new RuntimeException("get sqlSession failed!");
    }
  }

  public static <T> T getMapper(Class<T> type){
    try {
      return getSqlSession().getMapper(type);
    } catch (Exception e) {
      logger.error(e.getMessage(),e);
      throw new RuntimeException("get mapper failed!");
    }
  }
}
