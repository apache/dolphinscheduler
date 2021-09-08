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

package org.apache.dolphinscheduler.plugin.task.datasource.clickhouse;

import static org.apache.dolphinscheduler.plugin.task.datasource.PasswordUtils.decodePassword;
import static org.apache.dolphinscheduler.plugin.task.datasource.PasswordUtils.encodePassword;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.COLON;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.COMMA;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.COM_CLICKHOUSE_JDBC_DRIVER;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.DOUBLE_SLASH;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.JDBC_CLICKHOUSE;

import org.apache.dolphinscheduler.plugin.task.datasource.AbstractDatasourceProcessor;
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

public class ClickHouseDatasourceProcessor extends AbstractDatasourceProcessor {

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        ClickhouseConnectionParam connectionParams = (ClickhouseConnectionParam) createConnectionParams(connectionJson);

        ClickHouseDatasourceParamDTO clickHouseDatasourceParamDTO = new ClickHouseDatasourceParamDTO();
        clickHouseDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        clickHouseDatasourceParamDTO.setUserName(connectionParams.getUser());
        clickHouseDatasourceParamDTO.setOther(parseOther(connectionParams.getOther()));

        String[] hostSeperator = connectionParams.getAddress().split(DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(COMMA);
        clickHouseDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(COLON)[1]));
        clickHouseDatasourceParamDTO.setHost(hostPortArray[0].split(COLON)[0]);

        return clickHouseDatasourceParamDTO;
    }

    @Override
    public ConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        ClickHouseDatasourceParamDTO clickHouseParam = (ClickHouseDatasourceParamDTO) datasourceParam;
        String address = String.format("%s%s:%s", JDBC_CLICKHOUSE, clickHouseParam.getHost(), clickHouseParam.getPort());
        String jdbcUrl = address + "/" + clickHouseParam.getDatabase();

        ClickhouseConnectionParam clickhouseConnectionParam = new ClickhouseConnectionParam();
        clickhouseConnectionParam.setDatabase(clickHouseParam.getDatabase());
        clickhouseConnectionParam.setAddress(address);
        clickhouseConnectionParam.setJdbcUrl(jdbcUrl);
        clickhouseConnectionParam.setUser(clickHouseParam.getUserName());
        clickhouseConnectionParam.setPassword(encodePassword(clickHouseParam.getPassword()));
        clickhouseConnectionParam.setOther(transformOther(clickHouseParam.getOther()));
        return clickhouseConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, ClickhouseConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return COM_CLICKHOUSE_JDBC_DRIVER;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        ClickhouseConnectionParam clickhouseConnectionParam = (ClickhouseConnectionParam) connectionParam;
        String jdbcUrl = clickhouseConnectionParam.getJdbcUrl();
        if (StringUtils.isNotEmpty(clickhouseConnectionParam.getOther())) {
            jdbcUrl = String.format("%s?%s", jdbcUrl, clickhouseConnectionParam.getOther());
        }
        return jdbcUrl;
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        ClickhouseConnectionParam clickhouseConnectionParam = (ClickhouseConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(clickhouseConnectionParam),
                clickhouseConnectionParam.getUser(), decodePassword(clickhouseConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.CLICKHOUSE;
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        otherMap.forEach((key, value) -> stringBuilder.append(String.format("%s=%s%s", key, value, "&")));
        return stringBuilder.toString();
    }

    private Map<String, String> parseOther(String other) {
        if (other == null) {
            return null;
        }
        Map<String, String> otherMap = new LinkedHashMap<>();
        String[] configs = other.split("&");
        for (String config : configs) {
            otherMap.put(config.split("=")[0], config.split("=")[1]);
        }
        return otherMap;
    }
}
