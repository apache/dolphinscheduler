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

package org.apache.dolphinscheduler.plugin.datasource.snowflake.param;

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

import lombok.extern.slf4j.Slf4j;

import com.google.auto.service.AutoService;
@AutoService(DataSourceProcessor.class)
@Slf4j
public class SnowflakeDatasourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, SnowflakeDatasourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        SnowflakeConnectionParam snowFlakeConnectionParam =
                (SnowflakeConnectionParam) createConnectionParams(connectionJson);
        String[] tmpArray = snowFlakeConnectionParam.getAddress().split(Constants.DOUBLE_SLASH);
        StringBuilder hosts = new StringBuilder();
        String[] hostPortArray = tmpArray[tmpArray.length - 1].split(Constants.COMMA);
        for (String hostPort : hostPortArray) {
            hosts.append(hostPort.split(Constants.COLON)[0]).append(Constants.COMMA);
        }
        hosts.deleteCharAt(hosts.length() - 1);
        SnowflakeDatasourceParamDTO snowflakeDatasourceParamDTO = new SnowflakeDatasourceParamDTO();
        snowflakeDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        snowflakeDatasourceParamDTO.setHost(hosts.toString());
        snowflakeDatasourceParamDTO.setDatabase(snowFlakeConnectionParam.getDatabase());
        snowflakeDatasourceParamDTO.setUserName(snowFlakeConnectionParam.getUser());
        snowflakeDatasourceParamDTO.setOther(snowFlakeConnectionParam.getOther());

        return snowflakeDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        SnowflakeDatasourceParamDTO snowflakeParam = (SnowflakeDatasourceParamDTO) datasourceParam;
        StringBuilder address = new StringBuilder();
        address.append(DataSourceConstants.JDBC_SNOWFLAKE);
        for (String zkHost : datasourceParam.getHost().split(",")) {
            address.append(String.format("%s:%s,", zkHost, datasourceParam.getPort()));
        }
        address.deleteCharAt(address.length() - 1);
        String jdbcUrl = address.toString() + "/" + datasourceParam.getDatabase();
        SnowflakeConnectionParam snowFlakeConnectionParam = new SnowflakeConnectionParam();
        snowFlakeConnectionParam.setUser(snowflakeParam.getUserName());
        snowFlakeConnectionParam.setPassword(PasswordUtils.encodePassword(snowflakeParam.getPassword()));
        snowFlakeConnectionParam.setOther(snowflakeParam.getOther());
        snowFlakeConnectionParam.setAddress(address.toString());
        snowFlakeConnectionParam.setJdbcUrl(jdbcUrl);
        snowFlakeConnectionParam.setDriverClassName(getDatasourceDriver());
        snowFlakeConnectionParam.setValidationQuery(getValidationQuery());
        return snowFlakeConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, SnowflakeConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.NET_SNOWFLAKE_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.SNOWFLAKE_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        SnowflakeConnectionParam snowFlakeConnectionParam = (SnowflakeConnectionParam) connectionParam;
        if (MapUtils.isNotEmpty(snowFlakeConnectionParam.getOther())) {
            return String.format("%s?%s", snowFlakeConnectionParam.getJdbcUrl(), snowFlakeConnectionParam.getOther());
        }
        return snowFlakeConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        SnowflakeConnectionParam snowFlakeConnectionParam = (SnowflakeConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(connectionParam),
                snowFlakeConnectionParam.getUser(),
                PasswordUtils.decodePassword(snowFlakeConnectionParam.getPassword()));
    }
    @Override
    public DataSourceProcessor create() {
        return new SnowflakeDatasourceProcessor();
    }
    @Override
    public DbType getDbType() {
        return DbType.SNOWFLAKE;
    }

}
