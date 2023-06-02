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

package org.apache.dolphinscheduler.plugin.datasource.vertica.param;

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
public class VerticaDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, VerticaDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        VerticaConnectionParam connectionParams = (VerticaConnectionParam) createConnectionParams(connectionJson);
        VerticaDataSourceParamDTO verticaDatasourceParamDTO = new VerticaDataSourceParamDTO();

        verticaDatasourceParamDTO.setHostAndPortByAddress(connectionParams.getAddress());
        verticaDatasourceParamDTO.setUserName(connectionParams.getUser());
        verticaDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        verticaDatasourceParamDTO.setOther(connectionParams.getOther());

        return verticaDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO dataSourceParam) {
        VerticaDataSourceParamDTO verticaDatasourceParam = (VerticaDataSourceParamDTO) dataSourceParam;
        // address format: "jdbc:vertica://VerticaHost:portNumber"
        String address = String.format("%s%s:%s", DataSourceConstants.JDBC_VERTICA, verticaDatasourceParam.getHost(),
                verticaDatasourceParam.getPort());
        // jdbc format: "jdbc:vertica://VerticaHost:portNumber/databaseName"
        String jdbcUrl = String.format("%s/%s", address, verticaDatasourceParam.getDatabase());

        VerticaConnectionParam verticaConnectionParam = new VerticaConnectionParam();
        verticaConnectionParam.setJdbcUrl(jdbcUrl);
        verticaConnectionParam.setDatabase(verticaDatasourceParam.getDatabase());
        verticaConnectionParam.setAddress(address);
        verticaConnectionParam.setUser(verticaDatasourceParam.getUserName());
        verticaConnectionParam.setPassword(PasswordUtils.encodePassword(verticaDatasourceParam.getPassword()));
        verticaConnectionParam.setDriverClassName(getDatasourceDriver());
        verticaConnectionParam.setValidationQuery(getValidationQuery());

        return verticaConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, VerticaConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_VERTICA_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.VERTICA_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        VerticaConnectionParam verticaConnectionParam = (VerticaConnectionParam) connectionParam;
        String jdbcUrl = verticaConnectionParam.getJdbcUrl();
        if (MapUtils.isNotEmpty(verticaConnectionParam.getOther())) {
            jdbcUrl = String.format("%s?%s", jdbcUrl, transformOther(verticaConnectionParam.getOther()));
        }
        return jdbcUrl;
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        VerticaConnectionParam verticaConnectionParam = (VerticaConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        String user = verticaConnectionParam.getUser();
        String password = PasswordUtils.decodePassword(verticaConnectionParam.getPassword());
        return DriverManager.getConnection(getJdbcUrl(connectionParam), user, password);
    }

    @Override
    public DbType getDbType() {
        return DbType.VERTICA;
    }

    @Override
    public DataSourceProcessor create() {
        return new VerticaDataSourceProcessor();
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        otherMap.forEach((key, value) -> stringBuilder.append(String.format("%s=%s&", key, value)));
        return stringBuilder.toString();
    }

}
