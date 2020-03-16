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
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;


/**
 * data source connection factory
 */
@Configuration
@MapperScan("org.apache.dolphinscheduler.*.mapper")
public class SpringConnectionFactory {

    private static final Logger logger = LoggerFactory.getLogger(SpringConnectionFactory.class);

    /**
     * Load configuration file
     */
    protected static org.apache.commons.configuration.Configuration conf;

    static {
        try {
            conf = new PropertiesConfiguration(Constants.APPLICATION_PROPERTIES);
        } catch (ConfigurationException e) {
            logger.error("load configuration exception", e);
            System.exit(1);
        }
    }

    /**
     *  pagination interceptor
     * @return pagination interceptor
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    /**
     * get the data source
     * @return druid dataSource
     */
    @Bean(destroyMethod="")
    public DruidDataSource dataSource() {

        DruidDataSource druidDataSource = new DruidDataSource();

        druidDataSource.setDriverClassName(conf.getString(Constants.SPRING_DATASOURCE_DRIVER_CLASS_NAME));
        druidDataSource.setUrl(conf.getString(Constants.SPRING_DATASOURCE_URL));
        druidDataSource.setUsername(conf.getString(Constants.SPRING_DATASOURCE_USERNAME));
        druidDataSource.setPassword(conf.getString(Constants.SPRING_DATASOURCE_PASSWORD));
        druidDataSource.setValidationQuery(conf.getString(Constants.SPRING_DATASOURCE_VALIDATION_QUERY));

        druidDataSource.setPoolPreparedStatements(conf.getBoolean(Constants.SPRING_DATASOURCE_POOL_PREPARED_STATEMENTS));
        druidDataSource.setTestWhileIdle(conf.getBoolean(Constants.SPRING_DATASOURCE_TEST_WHILE_IDLE));
        druidDataSource.setTestOnBorrow(conf.getBoolean(Constants.SPRING_DATASOURCE_TEST_ON_BORROW));
        druidDataSource.setTestOnReturn(conf.getBoolean(Constants.SPRING_DATASOURCE_TEST_ON_RETURN));
        druidDataSource.setKeepAlive(conf.getBoolean(Constants.SPRING_DATASOURCE_KEEP_ALIVE));

        druidDataSource.setMinIdle(conf.getInt(Constants.SPRING_DATASOURCE_MIN_IDLE));
        druidDataSource.setMaxActive(conf.getInt(Constants.SPRING_DATASOURCE_MAX_ACTIVE));
        druidDataSource.setMaxWait(conf.getInt(Constants.SPRING_DATASOURCE_MAX_WAIT));
        druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(conf.getInt(Constants.SPRING_DATASOURCE_MAX_POOL_PREPARED_STATEMENT_PER_CONNECTION_SIZE));
        druidDataSource.setInitialSize(conf.getInt(Constants.SPRING_DATASOURCE_INITIAL_SIZE));
        druidDataSource.setTimeBetweenEvictionRunsMillis(conf.getLong(Constants.SPRING_DATASOURCE_TIME_BETWEEN_EVICTION_RUNS_MILLIS));
        druidDataSource.setTimeBetweenConnectErrorMillis(conf.getLong(Constants.SPRING_DATASOURCE_TIME_BETWEEN_CONNECT_ERROR_MILLIS));
        druidDataSource.setMinEvictableIdleTimeMillis(conf.getLong(Constants.SPRING_DATASOURCE_MIN_EVICTABLE_IDLE_TIME_MILLIS));
        druidDataSource.setValidationQueryTimeout(conf.getInt(Constants.SPRING_DATASOURCE_VALIDATION_QUERY_TIMEOUT));
        //auto commit
        druidDataSource.setDefaultAutoCommit(conf.getBoolean(Constants.SPRING_DATASOURCE_DEFAULT_AUTO_COMMIT));
        return druidDataSource;
    }

    /**
     * * get transaction manager
     * @return DataSourceTransactionManager
     */
    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    /**
     * * get sql session factory
     * @return sqlSessionFactory
     * @throws Exception sqlSessionFactory exception
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.addMappers("org.apache.dolphinscheduler.dao.mapper");
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(false);
        configuration.setCallSettersOnNulls(true);
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        configuration.addInterceptor(paginationInterceptor());
        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        sqlSessionFactoryBean.setConfiguration(configuration);
        sqlSessionFactoryBean.setDataSource(dataSource());

        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        dbConfig.setIdType(IdType.AUTO);
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setDbConfig(dbConfig);
        sqlSessionFactoryBean.setGlobalConfig(globalConfig);
        sqlSessionFactoryBean.setTypeAliasesPackage("org.apache.dolphinscheduler.dao.entity");
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("org/apache/dolphinscheduler/dao/mapper/*Mapper.xml"));
        sqlSessionFactoryBean.setTypeEnumsPackage("org.apache.dolphinscheduler.*.enums");
        return sqlSessionFactoryBean.getObject();
    }

    /**
     * get sql session
     * @return sqlSession
     */
    @Bean
    public SqlSession sqlSession() throws Exception{
        return new SqlSessionTemplate(sqlSessionFactory());
    }

}
