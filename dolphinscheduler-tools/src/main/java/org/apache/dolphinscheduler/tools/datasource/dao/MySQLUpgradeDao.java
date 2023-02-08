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

package org.apache.dolphinscheduler.tools.datasource.dao;

import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MySQLUpgradeDao extends UpgradeDao {

    private MySQLUpgradeDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String initSqlPath() {
        return "create/release-1.0.0_schema/mysql";
    }

    @Override
    public DbType getDbType() {
        return DbType.MYSQL;
    }

    /**
     * determines whether a table exists
     * @param tableName tableName
     * @return if table exist return true，else return false
     */
    @Override
    public boolean isExistsTable(String tableName) {
        try (
                Connection conn = dataSource.getConnection();
                ResultSet rs = conn.getMetaData().getTables(conn.getCatalog(), conn.getSchema(), tableName, null)) {
            return rs.next();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    /**
     * determines whether a field exists in the specified table
     * @param tableName tableName
     * @param columnName columnName
     * @return  if column name exist return true，else return false
     */
    @Override
    public boolean isExistsColumn(String tableName, String columnName) {
        try (
                Connection conn = dataSource.getConnection();
                ResultSet rs =
                        conn.getMetaData().getColumns(conn.getCatalog(), conn.getSchema(), tableName, columnName)) {
            return rs.next();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
