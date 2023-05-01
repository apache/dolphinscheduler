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

package org.apache.dolphinscheduler.plugin.datasource.teradata.param;

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
public class TeradataDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, TeradataDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        TeradataConnectionParam connectionParams = (TeradataConnectionParam) createConnectionParams(connectionJson);
        TeradataDataSourceParamDTO teradataDatasourceParamDTO = new TeradataDataSourceParamDTO();
        teradataDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        teradataDatasourceParamDTO.setUserName(connectionParams.getUser());
        teradataDatasourceParamDTO.setOther(connectionParams.getOther());

        String address = connectionParams.getAddress();
        String[] hostSeperator = address.split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.SLASH);
        teradataDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);
        teradataDatasourceParamDTO.setPort(Integer.parseInt(connectionParams.getJdbcUrl().split("DBS_PORT=")[1]
                .split(Constants.COMMA)[0].trim()));

        return teradataDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        TeradataDataSourceParamDTO teradataParam = (TeradataDataSourceParamDTO) datasourceParam;
        String address = String.format("%s%s", DataSourceConstants.JDBC_TERADATA, teradataParam.getHost());
        String jdbcUrl = String.format("%s/DATABASE=%s,DBS_PORT=%s", address, teradataParam.getDatabase(),
                teradataParam.getPort());

        TeradataConnectionParam teradataConnectionParam = new TeradataConnectionParam();
        teradataConnectionParam.setJdbcUrl(jdbcUrl);
        teradataConnectionParam.setAddress(address);
        teradataConnectionParam.setDatabase(teradataParam.getDatabase());
        teradataConnectionParam.setUser(teradataParam.getUserName());
        teradataConnectionParam.setPassword(PasswordUtils.encodePassword(teradataParam.getPassword()));
        teradataConnectionParam.setDriverClassName(getDatasourceDriver());
        teradataConnectionParam.setValidationQuery(getValidationQuery());
        teradataConnectionParam.setOther(teradataParam.getOther());

        return teradataConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, TeradataConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_TERADATA_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.TERADATA_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        TeradataConnectionParam teradataConnectionParam = (TeradataConnectionParam) connectionParam;
        if (MapUtils.isNotEmpty(teradataConnectionParam.getOther())) {
            return String.format("%s?%s", teradataConnectionParam.getJdbcUrl(),
                    transformOther(teradataConnectionParam.getOther()));
        }
        return teradataConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        TeradataConnectionParam teradataConnectionParam = (TeradataConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(teradataConnectionParam),
                teradataConnectionParam.getUser(),
                PasswordUtils.decodePassword(teradataConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.TERADATA;
    }

    @Override
    public DataSourceProcessor create() {
        return new TeradataDataSourceProcessor();
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
