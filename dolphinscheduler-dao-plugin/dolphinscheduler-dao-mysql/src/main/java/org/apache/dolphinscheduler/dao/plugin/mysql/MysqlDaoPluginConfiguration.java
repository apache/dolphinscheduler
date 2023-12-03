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
package org.apache.dolphinscheduler.dao.plugin.mysql;

import org.apache.dolphinscheduler.dao.plugin.api.DaoPluginConfiguration;
import org.apache.dolphinscheduler.dao.plugin.api.dialect.DatabaseDialect;
import org.apache.dolphinscheduler.dao.plugin.api.monitor.DatabaseMonitor;
import org.apache.dolphinscheduler.dao.plugin.mysql.dialect.MysqlDialect;
import org.apache.dolphinscheduler.dao.plugin.mysql.monitor.MysqlMonitor;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.baomidou.mybatisplus.annotation.DbType;

@Profile("mysql")
@Configuration
public class MysqlDaoPluginConfiguration implements DaoPluginConfiguration {

    @Autowired
    private DataSource dataSource;

    @Override
    public DbType dbType() {
        return DbType.MYSQL;
    }

    @Override
    public DatabaseMonitor databaseMonitor() {
        return new MysqlMonitor(dataSource);
    }

    @Override
    public DatabaseDialect databaseDialect() {
        return new MysqlDialect(dataSource);
    }
}
