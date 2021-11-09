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

package org.apache.dolphinscheduler.plugin.datasource.api.datasource.oracle;

import org.apache.dolphinscheduler.plugin.datasource.api.datasource.AbstractDatasourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbConnectType;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OracleDatasourceProcessor extends AbstractDatasourceProcessor {

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        OracleConnectionParam connectionParams = (OracleConnectionParam) createConnectionParams(connectionJson);
        OracleDatasourceParamDTO oracleDatasourceParamDTO = new OracleDatasourceParamDTO();

        oracleDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        oracleDatasourceParamDTO.setUserName(connectionParams.getUser());
        oracleDatasourceParamDTO.setOther(parseOther(connectionParams.getOther()));

        String hostSeperator = Constants.DOUBLE_SLASH;
        if (DbConnectType.ORACLE_SID.equals(connectionParams.connectType)) {
            hostSeperator = Constants.AT_SIGN;
        }
        String[] hostPort = connectionParams.getAddress().split(hostSeperator);
        String[] hostPortArray = hostPort[hostPort.length - 1].split(Constants.COMMA);
        oracleDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        oracleDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);

        return oracleDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        OracleDatasourceParamDTO oracleParam = (OracleDatasourceParamDTO) datasourceParam;
        String address;
        if (DbConnectType.ORACLE_SID.equals(oracleParam.getConnectType())) {
            address = String.format("%s%s:%s",
                    Constants.JDBC_ORACLE_SID, oracleParam.getHost(), oracleParam.getPort());
        } else {
            address = String.format("%s%s:%s",
                    Constants.JDBC_ORACLE_SERVICE_NAME, oracleParam.getHost(), oracleParam.getPort());
        }
        String jdbcUrl = address + "/" + oracleParam.getDatabase();

        OracleConnectionParam oracleConnectionParam = new OracleConnectionParam();
        oracleConnectionParam.setUser(oracleParam.getUserName());
        oracleConnectionParam.setPassword(PasswordUtils.encodePassword(oracleParam.getPassword()));
        oracleConnectionParam.setAddress(address);
        oracleConnectionParam.setJdbcUrl(jdbcUrl);
        oracleConnectionParam.setDatabase(oracleParam.getDatabase());
        oracleConnectionParam.setConnectType(oracleParam.getConnectType());
        oracleConnectionParam.setDriverClassName(getDatasourceDriver());
        oracleConnectionParam.setValidationQuery(getValidationQuery());
        oracleConnectionParam.setOther(transformOther(oracleParam.getOther()));
        oracleConnectionParam.setProps(oracleParam.getOther());

        return oracleConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, OracleConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return Constants.COM_ORACLE_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return Constants.ORACLE_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        OracleConnectionParam oracleConnectionParam = (OracleConnectionParam) connectionParam;
        if (!StringUtils.isEmpty(oracleConnectionParam.getOther())) {
            return String.format("%s?%s", oracleConnectionParam.getJdbcUrl(), oracleConnectionParam.getOther());
        }
        return oracleConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        OracleConnectionParam oracleConnectionParam = (OracleConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(connectionParam),
                oracleConnectionParam.getUser(), PasswordUtils.decodePassword(oracleConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.ORACLE;
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        List<String> list = new ArrayList<>();
        otherMap.forEach((key, value) -> list.add(String.format("%s=%s", key, value)));
        return String.join("&", list);
    }

    private Map<String, String> parseOther(String other) {
        if (StringUtils.isEmpty(other)) {
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
