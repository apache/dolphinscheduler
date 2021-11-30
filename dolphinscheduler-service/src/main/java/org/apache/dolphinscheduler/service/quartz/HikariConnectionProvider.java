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

package org.apache.dolphinscheduler.service.quartz;

import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import java.sql.Connection;
import java.sql.SQLException;

import org.quartz.utils.ConnectionProvider;

import com.zaxxer.hikari.HikariDataSource;

/**
 * hikari connection provider
 */
public class HikariConnectionProvider implements ConnectionProvider {

    private final HikariDataSource dataSource;

    public HikariConnectionProvider() {
        this.dataSource = SpringApplicationContext.getBean(HikariDataSource.class);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void shutdown() {
        dataSource.close();
    }

    @Override
    public void initialize() throws SQLException {
        //NOP
    }
}
