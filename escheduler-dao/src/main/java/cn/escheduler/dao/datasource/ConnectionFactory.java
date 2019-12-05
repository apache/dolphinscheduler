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
package cn.escheduler.dao.datasource;

import cn.escheduler.common.Constants;
import cn.escheduler.common.utils.CommonUtils;
import cn.escheduler.dao.mapper.ProjectMapper;
import com.alibaba.druid.pool.DruidDataSource;
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
import java.sql.SQLException;

import static cn.escheduler.dao.utils.PropertyUtils.*;


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

    druidDataSource.setDriverClassName(getString(Constants.SPRING_DATASOURCE_DRIVER_CLASS_NAME));
    druidDataSource.setUrl(getString(Constants.SPRING_DATASOURCE_URL));
    druidDataSource.setUsername(getString(Constants.SPRING_DATASOURCE_USERNAME));
    druidDataSource.setPassword(getString(Constants.SPRING_DATASOURCE_PASSWORD));
    druidDataSource.setValidationQuery(getString(Constants.SPRING_DATASOURCE_VALIDATION_QUERY));

    druidDataSource.setPoolPreparedStatements(getBoolean(Constants.SPRING_DATASOURCE_POOL_PREPARED_STATEMENTS));
    druidDataSource.setTestWhileIdle(getBoolean(Constants.SPRING_DATASOURCE_TEST_WHILE_IDLE));
    druidDataSource.setTestOnBorrow(getBoolean(Constants.SPRING_DATASOURCE_TEST_ON_BORROW));
    druidDataSource.setTestOnReturn(getBoolean(Constants.SPRING_DATASOURCE_TEST_ON_RETURN));
    druidDataSource.setKeepAlive(getBoolean(Constants.SPRING_DATASOURCE_KEEP_ALIVE));
    //just for development
    /*if (CommonUtils.isDevelopMode()) {
      //Configure filters that are intercepted by monitoring statistics, and SQL can not be counted after removing them.'wall'is used for firewall
      try {
        druidDataSource.setFilters("stat,wall,log4j");
      } catch (SQLException e) {
        logger.error(e.getMessage(), e);
      }
    }*/

    druidDataSource.setMinIdle(getInt(Constants.SPRING_DATASOURCE_MIN_IDLE));
    druidDataSource.setMaxActive(getInt(Constants.SPRING_DATASOURCE_MAX_ACTIVE));
    druidDataSource.setMaxWait(getInt(Constants.SPRING_DATASOURCE_MAX_WAIT));
    druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(getInt(Constants.SPRING_DATASOURCE_MAX_POOL_PREPARED_STATEMENT_PER_CONNECTION_SIZE));
    druidDataSource.setInitialSize(getInt(Constants.SPRING_DATASOURCE_INITIAL_SIZE));
    druidDataSource.setTimeBetweenEvictionRunsMillis(getLong(Constants.SPRING_DATASOURCE_TIME_BETWEEN_EVICTION_RUNS_MILLIS));
    druidDataSource.setTimeBetweenConnectErrorMillis(getLong(Constants.SPRING_DATASOURCE_TIME_BETWEEN_CONNECT_ERROR_MILLIS));
    druidDataSource.setMinEvictableIdleTimeMillis(getLong(Constants.SPRING_DATASOURCE_MIN_EVICTABLE_IDLE_TIME_MILLIS));
    druidDataSource.setValidationQueryTimeout(getInt(Constants.SPRING_DATASOURCE_VALIDATION_QUERY_TIMEOUT));
    //auto commit
    druidDataSource.setDefaultAutoCommit(getBoolean(Constants.SPRING_DATASOURCE_DEFAULT_AUTO_COMMIT));

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

          Environment environment = new Environment(Constants.DEVELOPMENT, transactionFactory, dataSource);

          Configuration configuration = new Configuration(environment);
          configuration.setLazyLoadingEnabled(true);

          configuration.addMappers(ProjectMapper.class.getPackage().getName());

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
