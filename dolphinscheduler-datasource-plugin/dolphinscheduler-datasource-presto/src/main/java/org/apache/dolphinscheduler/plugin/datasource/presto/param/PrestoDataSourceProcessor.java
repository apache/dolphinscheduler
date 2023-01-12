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

package org.apache.dolphinscheduler.plugin.datasource.presto.param;

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
public class PrestoDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, PrestoDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        PrestoConnectionParam connectionParams = (PrestoConnectionParam) createConnectionParams(connectionJson);

        String[] hostSeperator = connectionParams.getAddress().split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);

        PrestoDataSourceParamDTO prestoDatasourceParamDTO = new PrestoDataSourceParamDTO();
        prestoDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        prestoDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);
        prestoDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        prestoDatasourceParamDTO.setUserName(connectionParams.getUser());
        prestoDatasourceParamDTO.setOther(connectionParams.getOther());

        return prestoDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        PrestoDataSourceParamDTO prestoParam = (PrestoDataSourceParamDTO) datasourceParam;
        String address =
                String.format("%s%s:%s", DataSourceConstants.JDBC_PRESTO, prestoParam.getHost(), prestoParam.getPort());
        String jdbcUrl = address + "/" + prestoParam.getDatabase();

        PrestoConnectionParam prestoConnectionParam = new PrestoConnectionParam();
        prestoConnectionParam.setUser(prestoParam.getUserName());
        prestoConnectionParam.setPassword(PasswordUtils.encodePassword(prestoParam.getPassword()));
        prestoConnectionParam.setOther(prestoParam.getOther());
        prestoConnectionParam.setAddress(address);
        prestoConnectionParam.setJdbcUrl(jdbcUrl);
        prestoConnectionParam.setDatabase(prestoParam.getDatabase());
        prestoConnectionParam.setDriverClassName(getDatasourceDriver());
        prestoConnectionParam.setValidationQuery(getValidationQuery());

        return prestoConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, PrestoConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_PRESTO_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.PRESTO_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        PrestoConnectionParam prestoConnectionParam = (PrestoConnectionParam) connectionParam;
        if (MapUtils.isNotEmpty(prestoConnectionParam.getOther())) {
            return String.format("%s?%s", prestoConnectionParam.getJdbcUrl(),
                    transformOther(prestoConnectionParam.getOther()));
        }
        return prestoConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        PrestoConnectionParam prestoConnectionParam = (PrestoConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(connectionParam),
                prestoConnectionParam.getUser(), PasswordUtils.decodePassword(prestoConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.PRESTO;
    }

    @Override
    public DataSourceProcessor create() {
        return new PrestoDataSourceProcessor();
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isNotEmpty(otherMap)) {
            List<String> list = new ArrayList<>();
            otherMap.forEach((key, value) -> list.add(String.format("%s=%s", key, value)));
            return String.join("&", list);
        }
        return null;
    }

}
