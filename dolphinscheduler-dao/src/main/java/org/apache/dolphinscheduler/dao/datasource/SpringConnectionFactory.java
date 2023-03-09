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

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;

@Configuration
public class SpringConnectionFactory {

    /**
     * Inject this field to make sure the database is initialized, this can solve the table not found issue #8432.
     */
    @Autowired(required = false)
    public DataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer;

    @Bean
    public MybatisPlusInterceptor paginationInterceptor(DbType dbType) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbType));
        return interceptor;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, GlobalConfig globalConfig,
                                               DbType dbType) throws Exception {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(false);
        configuration.setCallSettersOnNulls(true);
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        configuration.addInterceptor(paginationInterceptor(dbType));

        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        sqlSessionFactoryBean.setConfiguration(configuration);
        sqlSessionFactoryBean.setDataSource(dataSource);

        sqlSessionFactoryBean.setGlobalConfig(globalConfig);
        sqlSessionFactoryBean.setTypeAliasesPackage("org.apache.dolphinscheduler.dao.entity");
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean
                .setMapperLocations(resolver.getResources("org/apache/dolphinscheduler/dao/mapper/*Mapper.xml"));
        sqlSessionFactoryBean.setDatabaseIdProvider(databaseIdProvider());
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public GlobalConfig globalConfig() {
        return new GlobalConfig().setDbConfig(new GlobalConfig.DbConfig()
                .setIdType(IdType.AUTO)).setBanner(false);
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

    @Bean
    @Primary
    @Profile("mysql")
    public DbType mysql() {
        return DbType.MYSQL;
    }

    @Bean
    public DbType h2() {
        return DbType.H2;
    }

    @Bean
    @Primary
    @Profile("postgresql")
    public DbType postgresql() {
        return DbType.POSTGRE_SQL;
    }
}
