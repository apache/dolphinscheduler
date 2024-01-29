/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.dolphinscheduler.dao;

import org.apache.dolphinscheduler.dao.plugin.api.DaoPluginConfiguration;
import org.apache.dolphinscheduler.dao.plugin.api.dialect.DatabaseDialect;
import org.apache.dolphinscheduler.dao.plugin.api.monitor.DatabaseMonitor;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

@Configuration
@EnableAutoConfiguration
@ComponentScan({"org.apache.dolphinscheduler.dao.plugin"})
@MapperScan(basePackages = "org.apache.dolphinscheduler.dao.mapper", sqlSessionFactoryRef = "sqlSessionFactory")
public class DaoConfiguration {

    /**
     * Inject this field to make sure the database is initialized, this can solve the table not found issue #8432.
     */
    @Autowired(required = false)
    public DataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer;

    /**
     * Inject this field to make sure the DaoPluginConfiguration is initialized before SpringConnectionFactory.
     */
    @Autowired
    public DaoPluginConfiguration daoPluginConfiguration;

    @Bean
    public MybatisPlusInterceptor paginationInterceptor(DbType dbType) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbType));
        return interceptor;
    }

    @Bean
    public DatabaseIdProvider databaseIdProvider() {
        return new VendorDatabaseIdProvider();
    }

    @Bean
    public DbType dbType() {
        return daoPluginConfiguration.dbType();
    }

    @Bean
    public DatabaseMonitor databaseMonitor() {
        return daoPluginConfiguration.databaseMonitor();
    }

    @Bean
    public DatabaseDialect databaseDialect() {
        return daoPluginConfiguration.databaseDialect();
    }

}
