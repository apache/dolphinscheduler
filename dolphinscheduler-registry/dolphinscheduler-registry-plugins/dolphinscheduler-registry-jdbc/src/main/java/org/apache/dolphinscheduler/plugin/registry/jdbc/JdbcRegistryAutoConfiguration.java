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

import lombok.extern.slf4j.Slf4j;

import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;

@Slf4j
@ComponentScan
@Configuration(proxyBeanMethods = false)
@MapperScan("org.apache.dolphinscheduler.plugin.registry.jdbc.mapper")
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "jdbc")
@AutoConfigureAfter(MybatisPlusAutoConfiguration.class)
public class JdbcRegistryAutoConfiguration {

    public JdbcRegistryAutoConfiguration() {
        log.info("Load JdbcRegistryAutoConfiguration");
    }

    @Bean
    public JdbcOperator jdbcOperator(JdbcRegistryProperties jdbcRegistryProperties,
                                     JdbcRegistryDataMapper jdbcRegistryDataMapper,
                                     JdbcRegistryLockMapper jdbcRegistryLockMapper) {
        return new JdbcOperator(jdbcRegistryProperties, jdbcRegistryDataMapper, jdbcRegistryLockMapper);
    }

    @Bean
    public JdbcRegistry jdbcRegistry(JdbcRegistryProperties jdbcRegistryProperties, JdbcOperator jdbcOperator) {
        return new JdbcRegistry(jdbcRegistryProperties, jdbcOperator);
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(JdbcRegistryProperties jdbcRegistryProperties) throws Exception {
        log.info("Initialize jdbcRegistrySqlSessionFactory");
        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(new HikariDataSource(jdbcRegistryProperties.getHikariConfig()));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory jdbcRegistrySqlSessionFactory) {
        log.info("Initialize jdbcRegistrySqlSessionTemplate");
        return new SqlSessionTemplate(jdbcRegistrySqlSessionFactory);
    }

    @Bean
    public JdbcRegistryDataMapper jdbcRegistryDataMapper(SqlSessionTemplate jdbcRegistrySqlSessionTemplate) {
        jdbcRegistrySqlSessionTemplate.getConfiguration().addMapper(JdbcRegistryDataMapper.class);
        return jdbcRegistrySqlSessionTemplate.getMapper(JdbcRegistryDataMapper.class);
    }

    @Bean
    public JdbcRegistryLockMapper jdbcRegistryLockMapper(SqlSessionTemplate jdbcRegistrySqlSessionTemplate) {
        jdbcRegistrySqlSessionTemplate.getConfiguration().addMapper(JdbcRegistryLockMapper.class);
        return jdbcRegistrySqlSessionTemplate.getMapper(JdbcRegistryLockMapper.class);
    }

}
