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

package org.apache.dolphinscheduler.scheduler.quartz;

import java.util.Optional;

import javax.sql.DataSource;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.quartz.Scheduler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Slf4j
@AutoConfiguration(after = {DataSourceAutoConfiguration.class}, before = QuartzSchedulerAutoConfiguration.class)
@ConditionalOnClass(value = Scheduler.class)
@ConditionalOnProperty(prefix = "scheduler-plugin.quartz", name = "using-separate-datasource", havingValue = "true")
@EnableConfigurationProperties(QuartzSchedulerDataSourceAutoConfiguration.QuartzDataSourceConfig.class)
public class QuartzSchedulerDataSourceAutoConfiguration {

    /**
     * Initialize After {@link QuartzAutoConfiguration.JdbcStoreTypeConfiguration#dataSourceCustomizer}
     */
    @Bean
    @Order(1)
    public SchedulerFactoryBeanCustomizer schedulerFactoryBeanCustomizer(QuartzDataSourceConfig quartzDataSourceConfig,
                                                                         DataSource defaultDataSource) {
        return schedulerFactoryBean -> {
            if (!quartzDataSourceConfig.isUsingSeparateDatasource()) {
                return;
            }
            final HikariConfig quartzHikariConfig = Optional.ofNullable(quartzDataSourceConfig.getHikari())
                    .orElse(generateQuartzHikariConfigFromDataSource((HikariDataSource) defaultDataSource));
            final HikariDataSource quartzDataSource = new HikariDataSource(quartzHikariConfig);
            schedulerFactoryBean.setDataSource(quartzDataSource);
            schedulerFactoryBean.setTransactionManager(new DataSourceTransactionManager(quartzDataSource));
        };
    }

    @Data
    @ConfigurationProperties(prefix = "scheduler-plugin.quartz")
    public static class QuartzDataSourceConfig {

        private boolean usingSeparateDatasource = false;
        private HikariConfig hikari;
    }

    private HikariConfig generateQuartzHikariConfigFromDataSource(HikariDataSource hikariDataSource) {
        HikariConfig defaultQuartzHikariConfig = new HikariConfig();
        hikariDataSource.copyStateTo(defaultQuartzHikariConfig);
        defaultQuartzHikariConfig.setPoolName("QuartzHikariCP");
        return defaultQuartzHikariConfig;
    }

}
