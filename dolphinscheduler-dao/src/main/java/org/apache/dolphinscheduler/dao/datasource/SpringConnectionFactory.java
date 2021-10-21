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

import static org.apache.dolphinscheduler.common.Constants.DATASOURCE_PROPERTIES;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ProfileType;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;


/**
 * data source connection factory
 */
@Configuration
@MapperScan("org.apache.dolphinscheduler.*.mapper")
public class SpringConnectionFactory {

    private static final Logger logger = LoggerFactory.getLogger(SpringConnectionFactory.class);

    static {
        PropertyUtils.loadPropertyFile(DATASOURCE_PROPERTIES);
    }

    /**
     * pagination interceptor
     *
     * @return pagination interceptor
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    /**
     * get the data source
     *
     * @return druid dataSource
     */
    @Bean(destroyMethod = "", name = "datasource")
    @Profile(value = {ProfileType.NON_H2})
    public DataSource dataSource() throws SQLException {

        DruidDataSource druidDataSource = new DruidDataSource();

        druidDataSource.setDriverClassName(PropertyUtils.getString(Constants.SPRING_DATASOURCE_DRIVER_CLASS_NAME));
        druidDataSource.setUrl(PropertyUtils.getString(Constants.SPRING_DATASOURCE_URL));
        druidDataSource.setUsername(PropertyUtils.getString(Constants.SPRING_DATASOURCE_USERNAME));
        druidDataSource.setPassword(PropertyUtils.getString(Constants.SPRING_DATASOURCE_PASSWORD));
        druidDataSource.setValidationQuery(PropertyUtils.getString(Constants.SPRING_DATASOURCE_VALIDATION_QUERY, "SELECT 1"));

        druidDataSource.setPoolPreparedStatements(PropertyUtils.getBoolean(Constants.SPRING_DATASOURCE_POOL_PREPARED_STATEMENTS, true));
        druidDataSource.setTestWhileIdle(PropertyUtils.getBoolean(Constants.SPRING_DATASOURCE_TEST_WHILE_IDLE, true));
        druidDataSource.setTestOnBorrow(PropertyUtils.getBoolean(Constants.SPRING_DATASOURCE_TEST_ON_BORROW, true));
        druidDataSource.setTestOnReturn(PropertyUtils.getBoolean(Constants.SPRING_DATASOURCE_TEST_ON_RETURN, false));
        druidDataSource.setKeepAlive(PropertyUtils.getBoolean(Constants.SPRING_DATASOURCE_KEEP_ALIVE, true));

        druidDataSource.setMinIdle(PropertyUtils.getInt(Constants.SPRING_DATASOURCE_MIN_IDLE, 5));
        druidDataSource.setMaxActive(PropertyUtils.getInt(Constants.SPRING_DATASOURCE_MAX_ACTIVE, 50));
        druidDataSource.setMaxWait(PropertyUtils.getInt(Constants.SPRING_DATASOURCE_MAX_WAIT, 60000));
        druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(PropertyUtils.getInt(Constants.SPRING_DATASOURCE_MAX_POOL_PREPARED_STATEMENT_PER_CONNECTION_SIZE, 20));
        druidDataSource.setInitialSize(PropertyUtils.getInt(Constants.SPRING_DATASOURCE_INITIAL_SIZE, 5));
        druidDataSource.setTimeBetweenEvictionRunsMillis(PropertyUtils.getLong(Constants.SPRING_DATASOURCE_TIME_BETWEEN_EVICTION_RUNS_MILLIS, 60000));
        druidDataSource.setTimeBetweenConnectErrorMillis(PropertyUtils.getLong(Constants.SPRING_DATASOURCE_TIME_BETWEEN_CONNECT_ERROR_MILLIS, 60000));
        druidDataSource.setMinEvictableIdleTimeMillis(PropertyUtils.getLong(Constants.SPRING_DATASOURCE_MIN_EVICTABLE_IDLE_TIME_MILLIS, 300000));
        druidDataSource.setValidationQueryTimeout(PropertyUtils.getInt(Constants.SPRING_DATASOURCE_VALIDATION_QUERY_TIMEOUT, 3));
        //auto commit
        druidDataSource.setDefaultAutoCommit(PropertyUtils.getBoolean(Constants.SPRING_DATASOURCE_DEFAULT_AUTO_COMMIT, true));
        druidDataSource.init();
        logger.info("Initialize Druid DataSource success");
        return druidDataSource;
    }

    @Bean(name = "datasource")
    @Profile(value = {ProfileType.H2})
    public DataSource h2DataSource() {
        EmbeddedDatabaseBuilder embeddedDatabaseBuilder = new EmbeddedDatabaseBuilder(new FileSystemResourceLoader());
        EmbeddedDatabase datasource = embeddedDatabaseBuilder.setType(EmbeddedDatabaseType.H2)
                .setName("DolphinSchedulerUT;MODE=MySQL")
                .addScript("file:../sql/dolphinscheduler_h2.sql")
                .build();
        logger.info("Initialize H2 DataSource success");
        return datasource;
    }

    /**
     * * get transaction manager
     *
     * @return DataSourceTransactionManager
     */
    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * * get sql session factory
     *
     * @return sqlSessionFactory
     * @throws Exception sqlSessionFactory exception
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(false);
        configuration.setCallSettersOnNulls(true);
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        configuration.addInterceptor(paginationInterceptor());
        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        sqlSessionFactoryBean.setConfiguration(configuration);
        sqlSessionFactoryBean.setDataSource(dataSource);

        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        dbConfig.setIdType(IdType.AUTO);
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setDbConfig(dbConfig);
        sqlSessionFactoryBean.setGlobalConfig(globalConfig);
        sqlSessionFactoryBean.setTypeAliasesPackage("org.apache.dolphinscheduler.dao.entity");
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("org/apache/dolphinscheduler/dao/mapper/*Mapper.xml"));
        sqlSessionFactoryBean.setTypeEnumsPackage("org.apache.dolphinscheduler.*.enums");
        sqlSessionFactoryBean.setDatabaseIdProvider(databaseIdProvider());
        return sqlSessionFactoryBean.getObject();
    }

    /**
     * get sql session
     *
     * @return SqlSession
     */
    @Bean
    public SqlSession sqlSession(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean
    public DatabaseIdProvider databaseIdProvider() {
        DatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
        Properties properties = new Properties();
        properties.setProperty("MySQL", "mysql");
        properties.setProperty("PostgreSQL", "pg");
        properties.setProperty("h2", "h2");
        databaseIdProvider.setProperties(properties);
        return databaseIdProvider;
    }
}
