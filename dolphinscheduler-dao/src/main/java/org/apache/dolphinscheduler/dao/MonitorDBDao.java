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

package org.apache.dolphinscheduler.dao;

import org.apache.dolphinscheduler.dao.entity.MonitorRecord;
import org.apache.dolphinscheduler.dao.utils.H2Performance;
import org.apache.dolphinscheduler.dao.utils.MySQLPerformance;
import org.apache.dolphinscheduler.dao.utils.PostgreSQLPerformance;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MonitorDBDao {

    private static final Logger logger = LoggerFactory.getLogger(MonitorDBDao.class);

    public static final String VARIABLE_NAME = "variable_name";

    @Autowired
    private DataSource dataSource;

    private MonitorRecord getCurrentDbPerformance() {
        try (final Connection conn = dataSource.getConnection()) {
            String driverClassName = DriverManager.getDriver(conn.getMetaData().getURL()).getClass().getName();
            if (driverClassName.contains(DbType.MYSQL.toString().toLowerCase())) {
                return new MySQLPerformance().getMonitorRecord(conn);
            } else if (driverClassName.contains(DbType.POSTGRESQL.toString().toLowerCase())) {
                return new PostgreSQLPerformance().getMonitorRecord(conn);
            } else if (driverClassName.contains(DbType.H2.toString().toLowerCase())) {
                return new H2Performance().getMonitorRecord(conn);
            }
        } catch (Exception e) {
            logger.error("SQLException: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * query database state
     *
     * @return MonitorRecord list
     */
    public List<MonitorRecord> queryDatabaseState() {
        List<MonitorRecord> list = new ArrayList<>(1);

        MonitorRecord monitorRecord = getCurrentDbPerformance();
        if (monitorRecord != null) {
            list.add(monitorRecord);
        }
        return list;
    }
}
