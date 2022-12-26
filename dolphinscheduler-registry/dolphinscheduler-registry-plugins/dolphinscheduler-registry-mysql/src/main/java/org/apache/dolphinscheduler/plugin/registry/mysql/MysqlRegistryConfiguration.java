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

package org.apache.dolphinscheduler.plugin.registry.mysql;

import org.apache.dolphinscheduler.plugin.registry.mysql.mapper.MysqlRegistryDataMapper;
import org.apache.dolphinscheduler.plugin.registry.mysql.mapper.MysqlRegistryLockMapper;

import org.apache.ibatis.session.SqlSessionFactory;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "mysql")
public class MysqlRegistryConfiguration {

    @Bean
    public SqlSessionFactory mysqlRegistrySqlSessionFactory(MysqlRegistryProperties mysqlRegistryProperties) throws Exception {
        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(new HikariDataSource(mysqlRegistryProperties.getHikariConfig()));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate mysqlRegistrySqlSessionTemplate(SqlSessionFactory mysqlRegistrySqlSessionFactory) {
        mysqlRegistrySqlSessionFactory.getConfiguration().addMapper(MysqlRegistryDataMapper.class);
        mysqlRegistrySqlSessionFactory.getConfiguration().addMapper(MysqlRegistryLockMapper.class);
        return new SqlSessionTemplate(mysqlRegistrySqlSessionFactory);
    }

    @Bean
    public MysqlRegistryDataMapper mysqlRegistryDataMapper(SqlSessionTemplate mysqlRegistrySqlSessionTemplate) {
        return mysqlRegistrySqlSessionTemplate.getMapper(MysqlRegistryDataMapper.class);
    }

    @Bean
    public MysqlRegistryLockMapper mysqlRegistryLockMapper(SqlSessionTemplate mysqlRegistrySqlSessionTemplate) {
        return mysqlRegistrySqlSessionTemplate.getMapper(MysqlRegistryLockMapper.class);
    }

}
