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

package org.apache.dolphinscheduler.plugin.task.datasource.sqlserver;

import static org.apache.dolphinscheduler.plugin.task.datasource.PasswordUtils.decodePassword;
import static org.apache.dolphinscheduler.plugin.task.datasource.PasswordUtils.encodePassword;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.COLON;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.COMMA;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.COM_SQLSERVER_JDBC_DRIVER;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.DOUBLE_SLASH;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.JDBC_SQLSERVER;

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

public class SqlServerDatasourceProcessor extends AbstractDatasourceProcessor {

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        SqlServerConnectionParam connectionParams = (SqlServerConnectionParam) createConnectionParams(connectionJson);
        String[] hostSeperator = connectionParams.getAddress().split(DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(COMMA);

        SqlServerDatasourceParamDTO sqlServerDatasourceParamDTO = new SqlServerDatasourceParamDTO();
        sqlServerDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        sqlServerDatasourceParamDTO.setUserName(connectionParams.getUser());
        sqlServerDatasourceParamDTO.setOther(parseOther(connectionParams.getOther()));
        sqlServerDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(COLON)[1]));
        sqlServerDatasourceParamDTO.setHost(hostPortArray[0].split(COLON)[0]);
        return sqlServerDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        SqlServerDatasourceParamDTO sqlServerParam = (SqlServerDatasourceParamDTO) datasourceParam;
        String address = String.format("%s%s:%s", JDBC_SQLSERVER, sqlServerParam.getHost(), sqlServerParam.getPort());
        String jdbcUrl = address + ";databaseName=" + sqlServerParam.getDatabase();

        SqlServerConnectionParam sqlServerConnectionParam = new SqlServerConnectionParam();
        sqlServerConnectionParam.setAddress(address);
        sqlServerConnectionParam.setDatabase(sqlServerParam.getDatabase());
        sqlServerConnectionParam.setJdbcUrl(jdbcUrl);
        sqlServerConnectionParam.setOther(transformOther(sqlServerParam.getOther()));
        sqlServerConnectionParam.setUser(sqlServerParam.getUserName());
        sqlServerConnectionParam.setPassword(encodePassword(sqlServerParam.getPassword()));
        return sqlServerConnectionParam;
    }

    @Override
    public BaseConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, SqlServerConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return COM_SQLSERVER_JDBC_DRIVER;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        SqlServerConnectionParam sqlServerConnectionParam = (SqlServerConnectionParam) connectionParam;

        if (StringUtils.isNotEmpty(sqlServerConnectionParam.getOther())) {
            return String.format("%s;%s", sqlServerConnectionParam.getJdbcUrl(), sqlServerConnectionParam.getOther());
        }
        return sqlServerConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        SqlServerConnectionParam sqlServerConnectionParam = (SqlServerConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(connectionParam), sqlServerConnectionParam.getUser(),
                decodePassword(sqlServerConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.SQLSERVER;
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        otherMap.forEach((key, value) -> stringBuilder.append(String.format("%s=%s;", key, value)));
        return stringBuilder.toString();
    }

    private Map<String, String> parseOther(String other) {
        if (StringUtils.isEmpty(other)) {
            return null;
        }
        Map<String, String> otherMap = new LinkedHashMap<>();
        for (String config : other.split(";")) {
            otherMap.put(config.split("=")[0], config.split("=")[1]);
        }
        return otherMap;
    }
}
