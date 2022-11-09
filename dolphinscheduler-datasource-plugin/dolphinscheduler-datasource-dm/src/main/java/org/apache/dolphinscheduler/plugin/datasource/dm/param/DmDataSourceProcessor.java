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

package org.apache.dolphinscheduler.plugin.datasource.dm.param;

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
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;

@AutoService(DataSourceProcessor.class)
public class DmDataSourceProcessor extends AbstractDataSourceProcessor {

    private final Logger logger = LoggerFactory.getLogger(DmDataSourceProcessor.class);

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, DmDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        DmConnectionParam connectionParams = (DmConnectionParam) createConnectionParams(connectionJson);
        DmDataSourceParamDTO dmDatasourceParamDTO = new DmDataSourceParamDTO();

        dmDatasourceParamDTO.setUserName(connectionParams.getUser());
        dmDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        dmDatasourceParamDTO.setOther(parseOther(connectionParams.getOther()));

        String address = connectionParams.getAddress();
        String[] hostSeperator = address.split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);
        dmDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        dmDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);

        return dmDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO dataSourceParam) {
        DmDataSourceParamDTO dmDatasourceParam = (DmDataSourceParamDTO) dataSourceParam;
        String address = String
                .format("%s%s:%s", DataSourceConstants.JDBC_DM, dmDatasourceParam.getHost(),
                        dmDatasourceParam.getPort());
        String jdbcUrl = StringUtils.isEmpty(dmDatasourceParam.getDatabase()) ? address
                : String.format("%s/%s", address,
                        dmDatasourceParam.getDatabase());

        DmConnectionParam dmConnectionParam = new DmConnectionParam();
        dmConnectionParam.setJdbcUrl(jdbcUrl);
        dmConnectionParam.setDatabase(dmConnectionParam.getDatabase());
        dmConnectionParam.setAddress(address);
        dmConnectionParam.setUser(dmDatasourceParam.getUserName());
        dmConnectionParam.setPassword(PasswordUtils.encodePassword(dmDatasourceParam.getPassword()));
        dmConnectionParam.setDriverClassName(getDatasourceDriver());
        dmConnectionParam.setValidationQuery(getValidationQuery());
        dmConnectionParam.setOther(transformOther(dmDatasourceParam.getOther()));
        dmConnectionParam.setProps(dmDatasourceParam.getOther());

        return dmConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, DmConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_DM_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.DM_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        DmConnectionParam dmConnectionParam = (DmConnectionParam) connectionParam;
        String jdbcUrl = dmConnectionParam.getJdbcUrl();
        if (!StringUtils.isEmpty(dmConnectionParam.getOther())) {
            return String.format("%s?%s", jdbcUrl, dmConnectionParam.getOther());
        }
        return jdbcUrl;
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        DmConnectionParam dmConnectionParam = (DmConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(dmConnectionParam), dmConnectionParam.getUser(),
                PasswordUtils.decodePassword(dmConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.DM;
    }

    @Override
    public DataSourceProcessor create() {
        return new DmDataSourceProcessor();
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
            otherMap.put(config.split("=")[0], config.split("=")[1]);
        }
        return otherMap;
    }

}
