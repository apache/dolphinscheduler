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

package org.apache.dolphinscheduler.plugin.datasource.dolphindb.param;

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
public class DolphinDBDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public void checkDatasourceParam(BaseDataSourceParamDTO baseDataSourceParamDTO) {
        if (!baseDataSourceParamDTO.getType().equals(DbType.REDSHIFT)) {
            // due to redshift use not regular hosts
            checkHost(baseDataSourceParamDTO.getHost());
        }
        checkOther(baseDataSourceParamDTO.getOther());
    }

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, DolphinDBDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        DolphinDBConnectionParam connectionParams = (DolphinDBConnectionParam) createConnectionParams(connectionJson);
        DolphinDBDataSourceParamDTO dataSourceParamDTO = new DolphinDBDataSourceParamDTO();

        dataSourceParamDTO.setUserName(connectionParams.getUser());
        dataSourceParamDTO.setDatabase(connectionParams.getDatabase());
        dataSourceParamDTO.setOther(connectionParams.getOther());

        String[] hostSeperator = connectionParams.getAddress().split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);
        dataSourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        dataSourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);

        return dataSourceParamDTO;
    }

    @Override
    public ConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        DolphinDBDataSourceParamDTO dolphinDBParam = (DolphinDBDataSourceParamDTO) datasourceParam;
        String address = String.format("%s%s:%s", DataSourceConstants.JDBC_DOLPHINDB, dolphinDBParam.getHost(),
                dolphinDBParam.getPort());
        String jdbcUrl = String.format("%s", address);

        DolphinDBConnectionParam dolphinDBConnectionParam = new DolphinDBConnectionParam();
        dolphinDBConnectionParam.setJdbcUrl(jdbcUrl);
        dolphinDBConnectionParam.setDatabase(dolphinDBParam.getDatabase());
        dolphinDBConnectionParam.setAddress(address);
        dolphinDBConnectionParam.setUser(dolphinDBParam.getUserName());
        dolphinDBConnectionParam.setPassword(PasswordUtils.encodePassword(dolphinDBParam.getPassword()));
        dolphinDBConnectionParam.setDriverClassName(getDatasourceDriver());
        dolphinDBConnectionParam.setValidationQuery(getValidationQuery());
        dolphinDBConnectionParam.setOther(dolphinDBParam.getOther());

        return dolphinDBConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, DolphinDBConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_DOLPHINDB_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.DOLPHINDB_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        DolphinDBConnectionParam dolphinDBConnectionParam = (DolphinDBConnectionParam) connectionParam;
        if (MapUtils.isNotEmpty(dolphinDBConnectionParam.getOther())) {
            return String.format("%s?%s", dolphinDBConnectionParam.getJdbcUrl(),
                    transformOther(dolphinDBConnectionParam.getOther()));
        }
        return dolphinDBConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        DolphinDBConnectionParam dolphinDBConnectionParam = (DolphinDBConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(connectionParam),
                dolphinDBConnectionParam.getUser(),
                PasswordUtils.decodePassword(dolphinDBConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.DOLPHINDB;
    }

    @Override
    public DataSourceProcessor create() {
        return new DolphinDBDataSourceProcessor();
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }

        List<String> list = new ArrayList<>(otherMap.size());
        otherMap.forEach((key, value) -> list.add(String.format("%s=%s", key, value)));
        return String.join("&", list);
    }
}
