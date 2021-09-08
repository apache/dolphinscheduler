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

package org.apache.dolphinscheduler.plugin.task.datasource.postgresql;

import static org.apache.dolphinscheduler.plugin.task.datasource.PasswordUtils.decodePassword;
import static org.apache.dolphinscheduler.plugin.task.datasource.PasswordUtils.encodePassword;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.COLON;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.COMMA;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.DOUBLE_SLASH;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.JDBC_POSTGRESQL;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.ORG_POSTGRESQL_DRIVER;

import org.apache.dolphinscheduler.plugin.task.datasource.AbstractDatasourceProcessor;
import org.apache.dolphinscheduler.plugin.task.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.plugin.task.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.task.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.commons.collections.MapUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class PostgreSqlDatasourceProcessor extends AbstractDatasourceProcessor {

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        PostgreSqlConnectionParam connectionParams = (PostgreSqlConnectionParam) createConnectionParams(connectionJson);
        PostgreSqlDatasourceParamDTO postgreSqlDatasourceParamDTO = new PostgreSqlDatasourceParamDTO();
        postgreSqlDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        postgreSqlDatasourceParamDTO.setUserName(connectionParams.getUser());
        postgreSqlDatasourceParamDTO.setOther(parseOther(connectionParams.getOther()));

        String address = connectionParams.getAddress();
        String[] hostSeperator = address.split(DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(COMMA);
        postgreSqlDatasourceParamDTO.setHost(hostPortArray[0].split(COLON)[0]);
        postgreSqlDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(COLON)[1]));

        return postgreSqlDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        PostgreSqlDatasourceParamDTO postgreSqlParam = (PostgreSqlDatasourceParamDTO) datasourceParam;
        String address = String.format("%s%s:%s", JDBC_POSTGRESQL, postgreSqlParam.getHost(), postgreSqlParam.getPort());
        String jdbcUrl = String.format("%s/%s", address, postgreSqlParam.getDatabase());

        PostgreSqlConnectionParam postgreSqlConnectionParam = new PostgreSqlConnectionParam();
        postgreSqlConnectionParam.setJdbcUrl(jdbcUrl);
        postgreSqlConnectionParam.setAddress(address);
        postgreSqlConnectionParam.setDatabase(postgreSqlParam.getDatabase());
        postgreSqlConnectionParam.setUser(postgreSqlParam.getUserName());
        postgreSqlConnectionParam.setPassword(encodePassword(postgreSqlParam.getPassword()));
        postgreSqlConnectionParam.setOther(transformOther(postgreSqlParam.getOther()));

        return postgreSqlConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, PostgreSqlConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return ORG_POSTGRESQL_DRIVER;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        PostgreSqlConnectionParam postgreSqlConnectionParam = (PostgreSqlConnectionParam) connectionParam;
        if (StringUtils.isNotEmpty(postgreSqlConnectionParam.getOther())) {
            return String.format("%s?%s", postgreSqlConnectionParam.getJdbcUrl(), postgreSqlConnectionParam.getOther());
        }
        return postgreSqlConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        PostgreSqlConnectionParam postgreSqlConnectionParam = (PostgreSqlConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(postgreSqlConnectionParam),
                postgreSqlConnectionParam.getUser(), decodePassword(postgreSqlConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.POSTGRESQL;
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        otherMap.forEach((key, value) -> stringBuilder.append(String.format("%s=%s&", key, value)));
        return stringBuilder.toString();
    }

    private Map<String, String> parseOther(String other) {
        if (StringUtils.isEmpty(other)) {
            return null;
        }
        Map<String, String> otherMap = new LinkedHashMap<>();
        for (String config : other.split("&")) {
            String[] split = config.split("=");
            otherMap.put(split[0], split[1]);
        }
        return otherMap;
    }
}
