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

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.h2.Driver;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;

/**
 * data source connection factory
 */
@Configuration
@MapperScan("org.apache.dolphinscheduler.*.mapper")
public class SpringConnectionFactory {

    private static final Logger logger = LoggerFactory.getLogger(SpringConnectionFactory.class);

    @Autowired
    private Environment environment;

    private static final AtomicBoolean H2_INITIALIZED = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        String datasourceProfile = getSpringActiveProfile().stream()
                .filter(ProfileType.DATASOURCE_PROFILE::contains)
                .findFirst()
                .orElse("");
        if (StringUtils.isEmpty(datasourceProfile) || ProfileType.MYSQL.equals(datasourceProfile)) {
            // default load datasource.properties
            PropertyUtils.loadPropertyFile(DATASOURCE_PROPERTIES.replace("-%s", ""));
        } else {
            // load datasource-{spring.profiles.active}.properties
            PropertyUtils.loadPropertyFile(String.format(DATASOURCE_PROPERTIES, datasourceProfile));
        }
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
     * @return dataSource
     */
    @Bean(destroyMethod = "", name = "datasource")
    public DataSource dataSource() throws SQLException {
        String driverClassName = PropertyUtils.getString(Constants.SPRING_DATASOURCE_DRIVER_CLASS_NAME);
        if (Driver.class.getName().equals(driverClassName)) {
            initializeH2Datasource();
        }

        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(PropertyUtils.getString(Constants.SPRING_DATASOURCE_URL));
        dataSource.setUsername(PropertyUtils.getString(Constants.SPRING_DATASOURCE_USERNAME));
        dataSource.setPassword(PropertyUtils.getString(Constants.SPRING_DATASOURCE_PASSWORD));
        dataSource.setConnectionTestQuery(PropertyUtils.getString(Constants.SPRING_DATASOURCE_VALIDATION_QUERY, "SELECT 1"));

        dataSource.setMinimumIdle(PropertyUtils.getInt(Constants.SPRING_DATASOURCE_MIN_IDLE, 5));
        dataSource.setMaximumPoolSize(PropertyUtils.getInt(Constants.SPRING_DATASOURCE_MAX_ACTIVE, 50));
        dataSource.setConnectionTimeout(PropertyUtils.getInt(Constants.SPRING_DATASOURCE_CONNECTION_TIMEOUT, 30000));
        dataSource.setIdleTimeout(PropertyUtils.getInt(Constants.SPRING_DATASOURCE_IDLE_TIMEOUT, 600000));
        dataSource.setMaxLifetime(PropertyUtils.getInt(Constants.SPRING_DATASOURCE_MAX_LIFE_TIME,  1800000));
        dataSource.setValidationTimeout(PropertyUtils.getInt(Constants.SPRING_DATASOURCE_VALIDATION_TIMEOUT, 5000));
        dataSource.setLeakDetectionThreshold(PropertyUtils.getInt(Constants.SPRING_DATASOURCE_LEAK_DETECTION_THRESHOLD, 0));
        dataSource.setInitializationFailTimeout(PropertyUtils.getInt(Constants.SPRING_DATASOURCE_INITIALIZATION_FAIL_TIMEOUT, 1));

        dataSource.setAutoCommit(PropertyUtils.getBoolean(Constants.SPRING_DATASOURCE_IS_AUTOCOMMIT, true));

        dataSource.addDataSourceProperty(Constants.CACHE_PREP_STMTS, PropertyUtils.getBoolean(Constants.SPRING_DATASOURCE_CACHE_PREP_STMTS, true));
        dataSource.addDataSourceProperty(Constants.PREP_STMT_CACHE_SIZE, PropertyUtils.getInt(Constants.SPRING_DATASOURCE_PREP_STMT_CACHE_SIZE, 250));
        dataSource.addDataSourceProperty(Constants.PREP_STMT_CACHE_SQL_LIMIT, PropertyUtils.getInt(Constants.SPRING_DATASOURCE_PREP_STMT_CACHE_SQL_LIMIT, 2048));

        logger.info("Initialize DataSource DataSource success");
        return dataSource;
    }

    private void initializeH2Datasource() {
        if (H2_INITIALIZED.compareAndSet(false, true)) {
            EmbeddedDatabaseBuilder embeddedDatabaseBuilder = new EmbeddedDatabaseBuilder(new FileSystemResourceLoader());
            embeddedDatabaseBuilder
                    .setType(EmbeddedDatabaseType.H2)
                    .setScriptEncoding(Constants.UTF_8)
                    .setName("dolphinscheduler;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
                    .addScript("classpath:sql/dolphinscheduler_h2.sql")
                    .build();
            logger.info("Initialize H2 DataSource success");
        }
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

    /**
     * Get spring active profile, which will be set by -Dspring.profiles.active=api， or in application.xml
     *
     * @return
     */
    private List<String> getSpringActiveProfile() {
        if (environment != null) {
            return Lists.newArrayList(environment.getActiveProfiles());
        }
        String property = System.getProperty("spring.profiles.active", "");
        return Arrays.stream(property.split(",")).map(String::trim).collect(Collectors.toList());
    }
}
