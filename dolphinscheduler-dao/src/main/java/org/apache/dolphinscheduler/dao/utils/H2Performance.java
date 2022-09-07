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

package org.apache.dolphinscheduler.dao.utils;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.dao.entity.MonitorRecord;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * H2 MEMORY DB Performance Monitor
 */
public class H2Performance extends BaseDBPerformance {

    private static final Logger logger = LoggerFactory.getLogger(H2Performance.class);

    /**
     * return the current database performance
     *
     * @param conn connection
     * @return MonitorRecord
     */
    @Override
    public MonitorRecord getMonitorRecord(Connection conn) {
        MonitorRecord monitorRecord = new MonitorRecord();
        monitorRecord.setDate(new Date());
        monitorRecord.setDbType(DbType.H2);
        monitorRecord.setState(Flag.YES);

        try (Statement pstmt = conn.createStatement()) {
            try (
                    ResultSet rs1 = pstmt
                            .executeQuery("select count(1) as total from information_schema.sessions;")) {
                if (rs1.next()) {
                    monitorRecord.setThreadsConnections(rs1.getInt("total"));
                }
            }
        } catch (SQLException e) {
            monitorRecord.setState(Flag.NO);
            logger.error("SQLException ", e);
        }
        return monitorRecord;
    }
}
