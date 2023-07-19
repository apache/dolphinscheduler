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

package org.apache.dolphinscheduler.plugin.registry.jdbc;

import org.apache.dolphinscheduler.plugin.registry.jdbc.mapper.JdbcRegistryDataMapper;
import org.apache.dolphinscheduler.plugin.registry.jdbc.mapper.JdbcRegistryLockMapper;

import org.apache.ibatis.session.SqlSessionFactory;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "jdbc")
public class JdbcRegistryConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "registry.hikari-config", name = "jdbc-url")
    public SqlSessionFactory jdbcRegistrySqlSessionFactory(JdbcRegistryProperties jdbcRegistryProperties) throws Exception {
        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(new HikariDataSource(jdbcRegistryProperties.getHikariConfig()));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate jdbcRegistrySqlSessionTemplate(SqlSessionFactory jdbcRegistrySqlSessionFactory) {
        jdbcRegistrySqlSessionFactory.getConfiguration().addMapper(JdbcRegistryDataMapper.class);
        jdbcRegistrySqlSessionFactory.getConfiguration().addMapper(JdbcRegistryLockMapper.class);
        return new SqlSessionTemplate(jdbcRegistrySqlSessionFactory);
    }

    @Bean
    public JdbcRegistryDataMapper jdbcRegistryDataMapper(SqlSessionTemplate jdbcRegistrySqlSessionTemplate) {
        return jdbcRegistrySqlSessionTemplate.getMapper(JdbcRegistryDataMapper.class);
    }

    @Bean
    public JdbcRegistryLockMapper jdbcRegistryLockMapper(SqlSessionTemplate jdbcRegistrySqlSessionTemplate) {
        return jdbcRegistrySqlSessionTemplate.getMapper(JdbcRegistryLockMapper.class);
    }

}
