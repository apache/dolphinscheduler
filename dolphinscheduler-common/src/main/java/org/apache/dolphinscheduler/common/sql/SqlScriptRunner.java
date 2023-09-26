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

package org.apache.dolphinscheduler.common.sql;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlScriptRunner {

    private final DataSource dataSource;

    private final SqlScriptParser sqlScriptParser;

    /**
     * @param dataSource        DataSource which used to execute the sql script.
     * @param sqlScriptFilePath Sqk script file path, the path should under classpath.
     */
    public SqlScriptRunner(DataSource dataSource, String sqlScriptFilePath) {
        this.dataSource = dataSource;
        this.sqlScriptParser = new ClasspathSqlScriptParser(sqlScriptFilePath);
    }

    public void execute() throws SQLException, IOException {
        List<String> allSql = sqlScriptParser.getAllSql();
        try (Connection connection = dataSource.getConnection()) {
            for (String sql : allSql) {
                if (StringUtils.isBlank(sql)) {
                    continue;
                }
                try (Statement statement = connection.createStatement()) {
                    // Since some sql doesn't have result so we believe if there is no exception then we think the sql
                    // execute success.
                    statement.execute(sql);
                    log.info("Execute sql: {} success", sql);
                }
            }
        }
    }

}
