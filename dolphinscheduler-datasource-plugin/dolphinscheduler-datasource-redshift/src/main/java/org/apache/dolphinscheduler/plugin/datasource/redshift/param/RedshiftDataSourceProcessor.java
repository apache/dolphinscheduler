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

package org.apache.dolphinscheduler.plugin.datasource.redshift.param;

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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.auto.service.AutoService;

@AutoService(DataSourceProcessor.class)
public class RedshiftDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, RedshiftDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        RedshiftConnectionParam
                connectionParams = (RedshiftConnectionParam) createConnectionParams(connectionJson);

        String[] hostSeperator = connectionParams.getAddress().split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);

        RedshiftDataSourceParamDTO
                redshiftDatasourceParamDTO = new RedshiftDataSourceParamDTO();
        redshiftDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        redshiftDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);
        redshiftDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        redshiftDatasourceParamDTO.setUserName(connectionParams.getUser());
        redshiftDatasourceParamDTO.setOther(parseOther(connectionParams.getOther()));

        return redshiftDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        RedshiftDataSourceParamDTO redshiftParam = (RedshiftDataSourceParamDTO) datasourceParam;
        String address =
                String.format("%s%s:%s", DataSourceConstants.JDBC_REDSHIFT, redshiftParam.getHost(),
                        redshiftParam.getPort());
        String jdbcUrl = address + Constants.SLASH + redshiftParam.getDatabase();

        RedshiftConnectionParam
                redshiftConnectionParam = new RedshiftConnectionParam();
        redshiftConnectionParam.setUser(redshiftParam.getUserName());
        redshiftConnectionParam.setPassword(PasswordUtils.encodePassword(redshiftParam.getPassword()));
        redshiftConnectionParam.setOther(transformOther(redshiftParam.getOther()));
        redshiftConnectionParam.setAddress(address);
        redshiftConnectionParam.setJdbcUrl(jdbcUrl);
        redshiftConnectionParam.setDatabase(redshiftParam.getDatabase());
        redshiftConnectionParam.setDriverClassName(getDatasourceDriver());
        redshiftConnectionParam.setValidationQuery(getValidationQuery());
        redshiftConnectionParam.setProps(redshiftParam.getOther());

        return redshiftConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, RedshiftConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_REDSHIFT_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.REDHIFT_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        RedshiftConnectionParam
                redshiftConnectionParam = (RedshiftConnectionParam) connectionParam;
        if (!StringUtils.isEmpty(redshiftConnectionParam.getOther())) {
            return String.format("%s?%s", redshiftConnectionParam.getJdbcUrl(), redshiftConnectionParam.getOther());
        }
        return redshiftConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        RedshiftConnectionParam redshiftConnectionParam = (RedshiftConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(connectionParam),
                redshiftConnectionParam.getUser(), PasswordUtils.decodePassword(redshiftConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.REDSHIFT;
    }

    @Override
    public DataSourceProcessor create() {
        return new RedshiftDataSourceProcessor();
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
}
