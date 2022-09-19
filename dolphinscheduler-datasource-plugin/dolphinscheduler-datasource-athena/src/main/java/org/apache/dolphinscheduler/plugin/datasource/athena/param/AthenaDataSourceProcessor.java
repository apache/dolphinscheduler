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

package org.apache.dolphinscheduler.plugin.datasource.athena.param;

import com.google.auto.service.AutoService;
import org.apache.commons.collections4.MapUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.AbstractDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@AutoService(DataSourceProcessor.class)
public class AthenaDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, AthenaDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        AthenaConnectionParam
            connectionParams = (AthenaConnectionParam) this.createConnectionParams(connectionJson);

        AthenaDataSourceParamDTO
            athenaDatasourceParamDTO = new AthenaDataSourceParamDTO();
        athenaDatasourceParamDTO.setAwsRegion(connectionParams.getAwsRegion());
        athenaDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        athenaDatasourceParamDTO.setUserName(connectionParams.getUser());
        athenaDatasourceParamDTO.setOther(this.parseOther(connectionParams.getOther()));

        return athenaDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        AthenaDataSourceParamDTO athenaParam = (AthenaDataSourceParamDTO) datasourceParam;
        String address = String.format("%s%s=%s;", Constants.JDBC_ATHENA, "AwsRegion", athenaParam.getAwsRegion());

        AthenaConnectionParam
            athenaConnectionParam = new AthenaConnectionParam();
        athenaConnectionParam.setUser(athenaParam.getUserName());
        athenaConnectionParam.setPassword(PasswordUtils.encodePassword(athenaParam.getPassword()));
        athenaConnectionParam.setAwsRegion(athenaParam.getAwsRegion());
        athenaConnectionParam.setOther(this.transformOther(athenaParam.getOther()));
        athenaConnectionParam.setAddress(address);
        athenaConnectionParam.setJdbcUrl(address);
        athenaConnectionParam.setDatabase(athenaParam.getDatabase());
        athenaConnectionParam.setDriverClassName(this.getDatasourceDriver());
        athenaConnectionParam.setValidationQuery(this.getValidationQuery());
        athenaConnectionParam.setProps(athenaParam.getOther());

        return athenaConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, AthenaConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return Constants.COM_ATHENA_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return Constants.ATHENA_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        AthenaConnectionParam
            athenaConnectionParam = (AthenaConnectionParam) connectionParam;
        if (!StringUtils.isEmpty(athenaConnectionParam.getOther())) {
            return String.format("%s%s", athenaConnectionParam.getJdbcUrl(), athenaConnectionParam.getOther());
        }
        return athenaConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        AthenaConnectionParam athenaConnectionParam = (AthenaConnectionParam) connectionParam;
        Class.forName(this.getDatasourceDriver());
        return DriverManager.getConnection(this.getJdbcUrl(connectionParam),
            athenaConnectionParam.getUser(), PasswordUtils.decodePassword(athenaConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.ATHENA;
    }

    @Override
    public DataSourceProcessor create() {
        return new AthenaDataSourceProcessor();
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isNotEmpty(otherMap)) {
            List<String> list = new ArrayList<>(otherMap.size());
            otherMap.forEach((key, value) -> list.add(String.format("%s=%s", key, value)));
            return String.join(Constants.SEMICOLON, list);
        }
        return null;
    }

    private Map<String, String> parseOther(String other) {
        Map<String, String> otherMap = new LinkedHashMap<>();
        if (StringUtils.isEmpty(other)) {
            return otherMap;
        }
        String[] configs = other.split(Constants.SEMICOLON);
        for (String config : configs) {
            otherMap.put(config.split(Constants.EQUAL_SIGN)[0], config.split(Constants.EQUAL_SIGN)[1]);
        }
        return otherMap;
    }

    @Override
    protected void checkHost(String host) {
        // Do not need to set the host, nothing to do
    }

    @Override
    protected void checkDatabasePatter(String database) {
        // Do not need to set the database, nothing to do
    }
}
