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

package org.apache.dolphinscheduler.plugin.datasource.postgresql.param;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.auto.service.AutoService;

@AutoService(DataSourceProcessor.class)
public class PostgreSQLDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, PostgreSQLDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        PostgreSQLConnectionParam connectionParams = (PostgreSQLConnectionParam) createConnectionParams(connectionJson);
        PostgreSQLDataSourceParamDTO postgreSqlDatasourceParamDTO = new PostgreSQLDataSourceParamDTO();
        postgreSqlDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        postgreSqlDatasourceParamDTO.setUserName(connectionParams.getUser());
        postgreSqlDatasourceParamDTO.setOther(connectionParams.getOther());

        String address = connectionParams.getAddress();
        String[] hostSeperator = address.split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);
        postgreSqlDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);
        postgreSqlDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));

        return postgreSqlDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        PostgreSQLDataSourceParamDTO postgreSqlParam = (PostgreSQLDataSourceParamDTO) datasourceParam;
        String address = String.format("%s%s:%s", DataSourceConstants.JDBC_POSTGRESQL, postgreSqlParam.getHost(),
                postgreSqlParam.getPort());
        String jdbcUrl = String.format("%s/%s", address, postgreSqlParam.getDatabase());

        PostgreSQLConnectionParam postgreSqlConnectionParam = new PostgreSQLConnectionParam();
        postgreSqlConnectionParam.setJdbcUrl(jdbcUrl);
        postgreSqlConnectionParam.setAddress(address);
        postgreSqlConnectionParam.setDatabase(postgreSqlParam.getDatabase());
        postgreSqlConnectionParam.setUser(postgreSqlParam.getUserName());
        postgreSqlConnectionParam.setPassword(PasswordUtils.encodePassword(postgreSqlParam.getPassword()));
        postgreSqlConnectionParam.setDriverClassName(getDatasourceDriver());
        postgreSqlConnectionParam.setValidationQuery(getValidationQuery());
        postgreSqlConnectionParam.setOther(postgreSqlParam.getOther());

        return postgreSqlConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, PostgreSQLConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.ORG_POSTGRESQL_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.POSTGRESQL_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        PostgreSQLConnectionParam postgreSqlConnectionParam = (PostgreSQLConnectionParam) connectionParam;
        if (MapUtils.isNotEmpty(postgreSqlConnectionParam.getOther())) {
            return String.format("%s?%s", postgreSqlConnectionParam.getJdbcUrl(),
                    transformOther(postgreSqlConnectionParam.getOther()));
        }
        return postgreSqlConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        PostgreSQLConnectionParam postgreSqlConnectionParam = (PostgreSQLConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(postgreSqlConnectionParam),
                postgreSqlConnectionParam.getUser(),
                PasswordUtils.decodePassword(postgreSqlConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.POSTGRESQL;
    }

    @Override
    public DataSourceProcessor create() {
        return new PostgreSQLDataSourceProcessor();
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        List<String> otherList = new ArrayList<>();
        otherMap.forEach((key, value) -> otherList.add(String.format("%s=%s", key, value)));
        return String.join("&", otherList);
    }

}
