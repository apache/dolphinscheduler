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

package org.apache.dolphinscheduler.dao.plugin.h2.monitor;

import org.apache.dolphinscheduler.dao.plugin.api.monitor.DatabaseMetrics;
import org.apache.dolphinscheduler.dao.plugin.api.monitor.DatabaseMonitor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import javax.sql.DataSource;

import lombok.SneakyThrows;

import com.baomidou.mybatisplus.annotation.DbType;

public class H2Monitor implements DatabaseMonitor {

    private final DataSource dataSource;

    public H2Monitor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @SneakyThrows
    @Override
    public DatabaseMetrics getDatabaseMetrics() {
        DatabaseMetrics monitorRecord = new DatabaseMetrics();
        monitorRecord.setDate(new Date());
        monitorRecord.setDbType(DbType.H2);
        monitorRecord.setState(DatabaseMetrics.DatabaseHealthStatus.YES);

        try (
                Connection connection = dataSource.getConnection();
                Statement pstmt = connection.createStatement()) {

            try (
                    ResultSet rs1 = pstmt
                            .executeQuery("select count(1) as total from information_schema.sessions;")) {
                if (rs1.next()) {
                    int currentSessions = rs1.getInt("total");
                    monitorRecord.setThreadsConnections(currentSessions);
                    monitorRecord.setMaxUsedConnections(currentSessions);
                }
            }

        }
        return monitorRecord;
    }

}
