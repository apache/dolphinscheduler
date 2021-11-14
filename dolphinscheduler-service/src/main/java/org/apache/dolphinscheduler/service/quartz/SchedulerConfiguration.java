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

package org.apache.dolphinscheduler.service.quartz;

import java.util.Properties;

import javax.sql.DataSource;

import org.quartz.impl.jdbcjobstore.PostgreSQLDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Configuration
public class SchedulerConfiguration {
    @Value("${spring.datasource.driver-class-name}")
    private String dataSourceDriverClass;

    @Bean
    public SchedulerFactoryBean schedulerFactory(DataSource dataSource) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setConfigLocation(new ClassPathResource("quartz.properties"));
        if (dataSourceDriverClass.equals(org.postgresql.Driver.class.getName())) {
            final Properties overrideProperties = new Properties();
            overrideProperties.setProperty("org.quartz.jobStore.driverDelegateClass", PostgreSQLDelegate.class.getName());
            schedulerFactory.setQuartzProperties(overrideProperties);
        }
        schedulerFactory.setDataSource(dataSource);
        schedulerFactory.setJobFactory(new SpringBeanJobFactory());
        return schedulerFactory;
    }
}
