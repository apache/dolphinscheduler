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

package org.apache.dolphinscheduler.plugin.datasource.databend.param;

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
public class DatabendDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, DatabendDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        DatabendConnectionParam connectionParams = (DatabendConnectionParam) createConnectionParams(connectionJson);

        DatabendDataSourceParamDTO databendDatasourceParamDTO = new DatabendDataSourceParamDTO();
        databendDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        databendDatasourceParamDTO.setUserName(connectionParams.getUser());
        databendDatasourceParamDTO.setOther(connectionParams.getOther());

        String[] hostSeperator = connectionParams.getAddress().split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);
        databendDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        databendDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);

        return databendDatasourceParamDTO;
    }

    @Override
    public ConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        DatabendDataSourceParamDTO databendParam = (DatabendDataSourceParamDTO) datasourceParam;
        String address = String.format("%s%s:%s", DataSourceConstants.JDBC_DATABEND, databendParam.getHost(),
                databendParam.getPort());
        String jdbcUrl = address + "/" + databendParam.getDatabase();

        DatabendConnectionParam databendConnectionParam = new DatabendConnectionParam();
        databendConnectionParam.setDatabase(databendParam.getDatabase());
        databendConnectionParam.setAddress(address);
        databendConnectionParam.setJdbcUrl(jdbcUrl);
        databendConnectionParam.setUser(databendParam.getUserName());
        databendConnectionParam.setPassword(PasswordUtils.encodePassword(databendParam.getPassword()));
        databendConnectionParam.setDriverClassName(getDatasourceDriver());
        databendConnectionParam.setValidationQuery(getValidationQuery());
        databendConnectionParam.setOther(databendParam.getOther());
        return databendConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, DatabendConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_DATABEND_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.DATABEND_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        DatabendConnectionParam databendConnectionParam = (DatabendConnectionParam) connectionParam;
        String jdbcUrl = databendConnectionParam.getJdbcUrl();
        if (MapUtils.isNotEmpty(databendConnectionParam.getOther())) {
            jdbcUrl = String.format("%s?%s", jdbcUrl, transformOther(databendConnectionParam.getOther()));
        }
        return jdbcUrl;
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        DatabendConnectionParam databendConnectionParam = (DatabendConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(databendConnectionParam),
                databendConnectionParam.getUser(),
                PasswordUtils.decodePassword(databendConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.DATABEND;
    }

    @Override
    public DataSourceProcessor create() {
        return new DatabendDataSourceProcessor();
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
