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

package org.apache.dolphinscheduler.plugin.task.api.utils;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.COLON;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.DOUBLE_SLASH;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.QUESTION;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.SEMICOLON;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.SINGLE_SLASH;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.MYSQL;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.POSTGRESQL;

import org.apache.dolphinscheduler.plugin.task.api.model.JdbcInfo;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

/**
 * JdbcUrlParser
 */
public class JdbcUrlParser {

    private JdbcUrlParser() {
        throw new IllegalStateException("Utility class");
    }

    public static DbType getDbType(String datasourceType) {
        switch (datasourceType.toUpperCase()) {
            case MYSQL:
                return DbType.MYSQL;
            case POSTGRESQL:
                return DbType.POSTGRESQL;
            default:
                return null;
        }
    }

    public static JdbcInfo getJdbcInfo(String jdbcUrl) {

        JdbcInfo jdbcInfo = new JdbcInfo();

        int pos;
        int pos1;
        int pos2;
        String tempUri;

        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:") || (pos1 = jdbcUrl.indexOf(COLON, 5)) == -1) {
            return null;
        }

        String driverName = jdbcUrl.substring(5, pos1);
        String params = "";
        String host = "";
        String database = "";
        String port = "";
        if (((pos2 = jdbcUrl.indexOf(SEMICOLON, pos1)) == -1) && ((pos2 = jdbcUrl.indexOf(QUESTION, pos1)) == -1)) {
            tempUri = jdbcUrl.substring(pos1 + 1);
        } else {
            tempUri = jdbcUrl.substring(pos1 + 1, pos2);
            params = jdbcUrl.substring(pos2 + 1);
        }

        if (tempUri.startsWith(DOUBLE_SLASH)) {
            if ((pos = tempUri.indexOf(SINGLE_SLASH, 2)) != -1) {
                host = tempUri.substring(2, pos);
                database = tempUri.substring(pos + 1);

                if ((pos = host.indexOf(COLON)) != -1) {
                    port = host.substring(pos + 1);
                    host = host.substring(0, pos);
                }
            }
        } else {
            database = tempUri;
        }

        if (StringUtils.isEmpty(database)) {
            return null;
        }

        if (database.contains(QUESTION)) {
            database = database.substring(0, database.indexOf(QUESTION));
        }

        if (database.contains(SEMICOLON)) {
            database = database.substring(0, database.indexOf(SEMICOLON));
        }

        jdbcInfo.setDriverName(driverName);
        jdbcInfo.setHost(host);
        jdbcInfo.setPort(port);
        jdbcInfo.setDatabase(database);
        jdbcInfo.setParams(params);
        jdbcInfo.setAddress("jdbc:" + driverName + "://" + host + COLON + port);

        return jdbcInfo;
    }
}
