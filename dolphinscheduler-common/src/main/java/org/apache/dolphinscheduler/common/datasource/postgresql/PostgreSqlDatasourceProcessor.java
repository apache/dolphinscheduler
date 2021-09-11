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

package org.apache.dolphinscheduler.common.datasource.postgresql;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.datasource.AbstractDatasourceProcessor;
import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.common.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.ConnectionParam;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;

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
        String[] hostSeperator = address.split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);
        postgreSqlDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);
        postgreSqlDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));

        return postgreSqlDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        PostgreSqlDatasourceParamDTO postgreSqlParam = (PostgreSqlDatasourceParamDTO) datasourceParam;
        String address = String.format("%s%s:%s", Constants.JDBC_POSTGRESQL, postgreSqlParam.getHost(), postgreSqlParam.getPort());
        String jdbcUrl = String.format("%s/%s", address, postgreSqlParam.getDatabase());

        PostgreSqlConnectionParam postgreSqlConnectionParam = new PostgreSqlConnectionParam();
        postgreSqlConnectionParam.setJdbcUrl(jdbcUrl);
        postgreSqlConnectionParam.setAddress(address);
        postgreSqlConnectionParam.setDatabase(postgreSqlParam.getDatabase());
        postgreSqlConnectionParam.setUser(postgreSqlParam.getUserName());
        postgreSqlConnectionParam.setPassword(CommonUtils.encodePassword(postgreSqlParam.getPassword()));
        postgreSqlConnectionParam.setOther(transformOther(postgreSqlParam.getOther()));

        return postgreSqlConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, PostgreSqlConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return Constants.ORG_POSTGRESQL_DRIVER;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        PostgreSqlConnectionParam postgreSqlConnectionParam = (PostgreSqlConnectionParam) connectionParam;
        if (!StringUtils.isEmpty(postgreSqlConnectionParam.getOther())) {
            return String.format("%s?%s", postgreSqlConnectionParam.getJdbcUrl(), postgreSqlConnectionParam.getOther());
        }
        return postgreSqlConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        PostgreSqlConnectionParam postgreSqlConnectionParam = (PostgreSqlConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(postgreSqlConnectionParam),
                postgreSqlConnectionParam.getUser(), CommonUtils.decodePassword(postgreSqlConnectionParam.getPassword()));
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
