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

package org.apache.dolphinscheduler.dao.plugin.mysql.dialect;

import org.apache.dolphinscheduler.dao.plugin.api.dialect.DatabaseDialect;

import java.sql.Connection;
import java.sql.ResultSet;

import javax.sql.DataSource;

import lombok.SneakyThrows;

public class MysqlDialect implements DatabaseDialect {

    private final DataSource dataSource;

    public MysqlDialect(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @SneakyThrows
    @Override
    public boolean tableExists(String tableName) {
        try (
                Connection conn = dataSource.getConnection();
                ResultSet rs = conn.getMetaData().getTables(conn.getCatalog(), conn.getSchema(), tableName, null)) {
            return rs.next();
        }
    }

    @SneakyThrows
    @Override
    public boolean columnExists(String tableName, String columnName) {
        try (
                Connection conn = dataSource.getConnection();
                ResultSet rs =
                        conn.getMetaData().getColumns(conn.getCatalog(), conn.getSchema(), tableName, columnName)) {
            return rs.next();

        }
    }
}
