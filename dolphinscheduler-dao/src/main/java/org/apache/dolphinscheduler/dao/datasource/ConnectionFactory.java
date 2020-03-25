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
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;


/**
 *  not spring manager connection, only use for init db, and alert module for non-spring application
 * data source connection factory
 */
public class ConnectionFactory extends SpringConnectionFactory{

    private static final Logger logger = LoggerFactory.getLogger(ConnectionFactory.class);


    /**
     * sql session factory
     */
    private static SqlSessionFactory sqlSessionFactory;

    /**
     * sql session template
     */
    private static SqlSessionTemplate sqlSessionTemplate;

    /**
     * get the data source
     * @return druid dataSource
     */
    public static DruidDataSource getDataSource() {

        DruidDataSource druidDataSource = dataSource();
        return druidDataSource;
    }

    /**
     * * get sql session factory
     * @return sqlSessionFactory
     * @throws Exception sqlSessionFactory exception
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
                    configuration.addInterceptor(new PaginationInterceptor());

                    MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
                    sqlSessionFactoryBean.setConfiguration(configuration);
                    sqlSessionFactoryBean.setDataSource(dataSource);

                    sqlSessionFactoryBean.setTypeEnumsPackage("org.apache.dolphinscheduler.*.enums");
                    sqlSessionFactory = sqlSessionFactoryBean.getObject();
                }
            }
        }

        return sqlSessionFactory;
    }

    /**
     * get sql session
     * @return sqlSession
     */
    public static SqlSession getSqlSession() {
        if (sqlSessionTemplate == null) {
            synchronized (ConnectionFactory.class) {
                if (sqlSessionTemplate == null) {
                    try {
                        sqlSessionTemplate = new SqlSessionTemplate(getSqlSessionFactory());
                        return sqlSessionTemplate;
                    } catch (Exception e) {
                        logger.error("getSqlSession error", e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return sqlSessionTemplate;
    }

    /**
     * get mapper
     * @param type target class
     * @param <T> generic
     * @return target object
     */
    public static <T> T getMapper(Class<T> type) {
        try {
            return getSqlSession().getMapper(type);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("get mapper failed");
        }
    }

}
