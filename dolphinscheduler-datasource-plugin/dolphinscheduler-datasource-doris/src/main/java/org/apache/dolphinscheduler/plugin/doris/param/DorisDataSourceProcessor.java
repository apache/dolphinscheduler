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
package org.apache.dolphinscheduler.plugin.doris.param;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.auto.service.AutoService;

@AutoService(DataSourceProcessor.class)
public class DorisDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, DorisDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) throws NumberFormatException {
        DorisConnectionParam connectionParams = (DorisConnectionParam) createConnectionParams(connectionJson);
        DorisDataSourceParamDTO dorisDataSourceParamDTO = new DorisDataSourceParamDTO();

        dorisDataSourceParamDTO.setUserName(connectionParams.getUser());
        dorisDataSourceParamDTO.setDatabase(connectionParams.getDatabase());
        dorisDataSourceParamDTO.setOther(connectionParams.getOther());

        String address = connectionParams.getAddress();
        String[] hostSeperator = address.split(Constants.DOUBLE_SLASH);
        String[] hostPortArrays = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);

        dorisDataSourceParamDTO.setPort(Integer.parseInt(hostPortArrays[0].split(Constants.COLON)[1]));

        for (int i = 0; i < hostPortArrays.length; i++) {
            hostPortArrays[i] = hostPortArrays[i].split(Constants.COLON)[0];
        }
        dorisDataSourceParamDTO.setHost(String.join(",", hostPortArrays));

        return dorisDataSourceParamDTO;
    }

    @Override
    public ConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        DorisDataSourceParamDTO mysqlDatasourceParam = (DorisDataSourceParamDTO) datasourceParam;
        String[] hosts = datasourceParam.getHost().split(Constants.COMMA);

        for (int i = 0; i < hosts.length; i++) {
            hosts[i] = String.format(Constants.FORMAT_S_S_COLON, hosts[i], mysqlDatasourceParam.getPort());
        }

        String address = String.format("%s%s", DataSourceConstants.JDBC_MYSQL_LOADBALANCE, String.join(",", hosts));
        String jdbcUrl = String.format(Constants.FORMAT_S_S, address, mysqlDatasourceParam.getDatabase());

        DorisConnectionParam mysqlConnectionParam = new DorisConnectionParam();
        mysqlConnectionParam.setJdbcUrl(jdbcUrl);
        mysqlConnectionParam.setDatabase(mysqlDatasourceParam.getDatabase());
        mysqlConnectionParam.setAddress(address);
        mysqlConnectionParam.setUser(mysqlDatasourceParam.getUserName());
        mysqlConnectionParam.setPassword(PasswordUtils.encodePassword(mysqlDatasourceParam.getPassword()));
        mysqlConnectionParam.setDriverClassName(getDatasourceDriver());
        mysqlConnectionParam.setValidationQuery(getValidationQuery());
        mysqlConnectionParam.setOther(mysqlDatasourceParam.getOther());

        return mysqlConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, DorisConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_MYSQL_CJ_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.MYSQL_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        DorisConnectionParam mysqlConnectionParam = (DorisConnectionParam) connectionParam;
        String jdbcUrl = mysqlConnectionParam.getJdbcUrl();
        if (MapUtils.isNotEmpty(mysqlConnectionParam.getOther())) {
            return String.format("%s?%s", jdbcUrl, transformOther(mysqlConnectionParam.getOther()));
        }
        return String.format("%s", jdbcUrl);
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        DorisConnectionParam dorisConnectionParam = (DorisConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());

        return DriverManager.getConnection(getJdbcUrl(connectionParam), dorisConnectionParam.getUser(),
                PasswordUtils.decodePassword(dorisConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.DORIS;
    }

    @Override
    public DataSourceProcessor create() {
        return new DorisDataSourceProcessor();
    }

    private String transformOther(Map<String, String> paramMap) {
        if (MapUtils.isEmpty(paramMap)) {
            return null;
        }
        Map<String, String> otherMap = new HashMap<>();
        paramMap.forEach((k, v) -> otherMap.put(k, v));
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        List<String> otherList = new ArrayList<>();
        otherMap.forEach((key, value) -> otherList.add(String.format("%s=%s", key, value)));
        return String.join("&", otherList);
    }

}
