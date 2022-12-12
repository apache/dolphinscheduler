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

package org.apache.dolphinscheduler.plugin.datasource.clickhouse.param;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.AbstractDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
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
public class ClickHouseDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, ClickHouseDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        ClickHouseConnectionParam connectionParams = (ClickHouseConnectionParam) createConnectionParams(connectionJson);

        ClickHouseDataSourceParamDTO clickHouseDatasourceParamDTO = new ClickHouseDataSourceParamDTO();
        clickHouseDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        clickHouseDatasourceParamDTO.setUserName(connectionParams.getUser());
        clickHouseDatasourceParamDTO.setOther(connectionParams.getOther());

        String[] hostSeperator = connectionParams.getAddress().split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);
        clickHouseDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        clickHouseDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);

        return clickHouseDatasourceParamDTO;
    }

    @Override
    public ConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        ClickHouseDataSourceParamDTO clickHouseParam = (ClickHouseDataSourceParamDTO) datasourceParam;
        String address = String.format("%s%s:%s", DataSourceConstants.JDBC_CLICKHOUSE, clickHouseParam.getHost(),
                clickHouseParam.getPort());
        String jdbcUrl = address + "/" + clickHouseParam.getDatabase();

        ClickHouseConnectionParam clickhouseConnectionParam = new ClickHouseConnectionParam();
        clickhouseConnectionParam.setDatabase(clickHouseParam.getDatabase());
        clickhouseConnectionParam.setAddress(address);
        clickhouseConnectionParam.setJdbcUrl(jdbcUrl);
        clickhouseConnectionParam.setUser(clickHouseParam.getUserName());
        clickhouseConnectionParam.setPassword(PasswordUtils.encodePassword(clickHouseParam.getPassword()));
        clickhouseConnectionParam.setDriverClassName(getDatasourceDriver());
        clickhouseConnectionParam.setValidationQuery(getValidationQuery());
        clickhouseConnectionParam.setOther(clickHouseParam.getOther());
        return clickhouseConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, ClickHouseConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_CLICKHOUSE_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.CLICKHOUSE_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        ClickHouseConnectionParam clickhouseConnectionParam = (ClickHouseConnectionParam) connectionParam;
        String jdbcUrl = clickhouseConnectionParam.getJdbcUrl();
        if (MapUtils.isNotEmpty(clickhouseConnectionParam.getOther())) {
            jdbcUrl = String.format("%s?%s", jdbcUrl, transformOther(clickhouseConnectionParam.getOther()));
        }
        return jdbcUrl;
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        ClickHouseConnectionParam clickhouseConnectionParam = (ClickHouseConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(clickhouseConnectionParam),
                clickhouseConnectionParam.getUser(),
                PasswordUtils.decodePassword(clickhouseConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.CLICKHOUSE;
    }

    @Override
    public DataSourceProcessor create() {
        return new ClickHouseDataSourceProcessor();
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
