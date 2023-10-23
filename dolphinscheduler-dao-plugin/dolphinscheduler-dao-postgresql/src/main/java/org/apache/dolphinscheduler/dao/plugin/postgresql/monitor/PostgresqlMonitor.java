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

package org.apache.dolphinscheduler.dao.plugin.postgresql.monitor;

import org.apache.dolphinscheduler.dao.plugin.api.monitor.DatabaseMetrics;
import org.apache.dolphinscheduler.dao.plugin.api.monitor.DatabaseMonitor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import javax.sql.DataSource;

import lombok.SneakyThrows;

import com.baomidou.mybatisplus.annotation.DbType;

public class PostgresqlMonitor implements DatabaseMonitor {

    private final DataSource dataSource;

    public PostgresqlMonitor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    @SneakyThrows
    public DatabaseMetrics getDatabaseMetrics() {
        DatabaseMetrics monitorRecord = new DatabaseMetrics();
        monitorRecord.setDate(new Date());
        monitorRecord.setState(DatabaseMetrics.DatabaseHealthStatus.YES);
        monitorRecord.setDbType(DbType.POSTGRE_SQL);

        try (
                Connection connection = dataSource.getConnection();
                Statement pstmt = connection.createStatement()) {

            try (ResultSet rs1 = pstmt.executeQuery("select count(*) from pg_stat_activity;")) {
                if (rs1.next()) {
                    monitorRecord.setThreadsConnections(rs1.getInt("count"));
                }
            }

            try (ResultSet rs2 = pstmt.executeQuery("show max_connections")) {
                if (rs2.next()) {
                    monitorRecord.setMaxConnections(rs2.getInt("max_connections"));
                }
            }

            try (
                    ResultSet rs3 =
                            pstmt.executeQuery("select count(*) from pg_stat_activity pg where pg.state = 'active';")) {
                if (rs3.next()) {
                    monitorRecord.setThreadsRunningConnections(rs3.getInt("count"));
                }
            }
        }
        return monitorRecord;
    }
}
