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

package org.apache.dolphinscheduler.plugin.datasource.sqlserver.param;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.AbstractDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.collections4.MapUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import com.google.auto.service.AutoService;

@AutoService(DataSourceProcessor.class)
public class SQLServerDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, SQLServerDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        SQLServerConnectionParam connectionParams = (SQLServerConnectionParam) createConnectionParams(connectionJson);
        String[] hostSeperator = connectionParams.getAddress().split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);

        SQLServerDataSourceParamDTO sqlServerDatasourceParamDTO = new SQLServerDataSourceParamDTO();
        sqlServerDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        sqlServerDatasourceParamDTO.setUserName(connectionParams.getUser());
        sqlServerDatasourceParamDTO.setOther(connectionParams.getOther());
        sqlServerDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        sqlServerDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);
        return sqlServerDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        SQLServerDataSourceParamDTO sqlServerParam = (SQLServerDataSourceParamDTO) datasourceParam;
        String address =
                String.format("%s%s:%s", DataSourceConstants.JDBC_SQLSERVER, sqlServerParam.getHost(),
                        sqlServerParam.getPort());
        String jdbcUrl = address + ";databaseName=" + sqlServerParam.getDatabase();

        SQLServerConnectionParam sqlServerConnectionParam = new SQLServerConnectionParam();
        sqlServerConnectionParam.setAddress(address);
        sqlServerConnectionParam.setDatabase(sqlServerParam.getDatabase());
        sqlServerConnectionParam.setJdbcUrl(jdbcUrl);
        sqlServerConnectionParam.setOther(sqlServerParam.getOther());
        sqlServerConnectionParam.setUser(sqlServerParam.getUserName());
        sqlServerConnectionParam.setPassword(PasswordUtils.encodePassword(sqlServerParam.getPassword()));
        sqlServerConnectionParam.setDriverClassName(getDatasourceDriver());
        sqlServerConnectionParam.setValidationQuery(getValidationQuery());
        return sqlServerConnectionParam;
    }

    @Override
    public BaseConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, SQLServerConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_SQLSERVER_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.SQLSERVER_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        SQLServerConnectionParam sqlServerConnectionParam = (SQLServerConnectionParam) connectionParam;

        if (MapUtils.isNotEmpty(sqlServerConnectionParam.getOther())) {
            return String.format("%s;%s", sqlServerConnectionParam.getJdbcUrl(),
                    transformOther(sqlServerConnectionParam.getOther()));
        }
        return sqlServerConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        SQLServerConnectionParam sqlServerConnectionParam = (SQLServerConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(connectionParam), sqlServerConnectionParam.getUser(),
                PasswordUtils.decodePassword(sqlServerConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.SQLSERVER;
    }

    @Override
    public DataSourceProcessor create() {
        return new SQLServerDataSourceProcessor();
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        otherMap.forEach((key, value) -> stringBuilder.append(String.format("%s=%s;", key, value)));
        return stringBuilder.toString();
    }

}
