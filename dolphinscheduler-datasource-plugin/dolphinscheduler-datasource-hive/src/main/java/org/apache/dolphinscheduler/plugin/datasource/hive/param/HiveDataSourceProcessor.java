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

package org.apache.dolphinscheduler.plugin.datasource.hive.param;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.AbstractDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.collections4.MapUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.auto.service.AutoService;

@AutoService(DataSourceProcessor.class)
public class HiveDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, HiveDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        HiveDataSourceParamDTO hiveDataSourceParamDTO = new HiveDataSourceParamDTO();
        HiveConnectionParam hiveConnectionParam = (HiveConnectionParam) createConnectionParams(connectionJson);

        hiveDataSourceParamDTO.setDatabase(hiveConnectionParam.getDatabase());
        hiveDataSourceParamDTO.setUserName(hiveConnectionParam.getUser());
        hiveDataSourceParamDTO.setOther(hiveConnectionParam.getOther());
        hiveDataSourceParamDTO.setLoginUserKeytabUsername(hiveConnectionParam.getLoginUserKeytabUsername());
        hiveDataSourceParamDTO.setLoginUserKeytabPath(hiveConnectionParam.getLoginUserKeytabPath());
        hiveDataSourceParamDTO.setJavaSecurityKrb5Conf(hiveConnectionParam.getJavaSecurityKrb5Conf());

        String[] tmpArray = hiveConnectionParam.getAddress().split(Constants.DOUBLE_SLASH);
        StringBuilder hosts = new StringBuilder();
        String[] hostPortArray = tmpArray[tmpArray.length - 1].split(Constants.COMMA);
        for (String hostPort : hostPortArray) {
            hosts.append(hostPort.split(Constants.COLON)[0]).append(Constants.COMMA);
        }
        hosts.deleteCharAt(hosts.length() - 1);
        hiveDataSourceParamDTO.setHost(hosts.toString());
        hiveDataSourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));

        return hiveDataSourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        HiveDataSourceParamDTO hiveParam = (HiveDataSourceParamDTO) datasourceParam;
        StringBuilder address = new StringBuilder();
        address.append(DataSourceConstants.JDBC_HIVE_2);
        for (String zkHost : hiveParam.getHost().split(",")) {
            address.append(String.format("%s:%s,", zkHost, hiveParam.getPort()));
        }
        address.deleteCharAt(address.length() - 1);
        String jdbcUrl = address.toString() + "/" + hiveParam.getDatabase();

        HiveConnectionParam hiveConnectionParam = new HiveConnectionParam();
        hiveConnectionParam.setDatabase(hiveParam.getDatabase());
        hiveConnectionParam.setAddress(address.toString());
        hiveConnectionParam.setJdbcUrl(jdbcUrl);
        hiveConnectionParam.setUser(hiveParam.getUserName());
        hiveConnectionParam.setPassword(PasswordUtils.encodePassword(hiveParam.getPassword()));
        hiveConnectionParam.setDriverClassName(getDatasourceDriver());
        hiveConnectionParam.setValidationQuery(getValidationQuery());

        if (CommonUtils.getKerberosStartupState()) {
            hiveConnectionParam.setPrincipal(hiveParam.getPrincipal());
            hiveConnectionParam.setJavaSecurityKrb5Conf(hiveParam.getJavaSecurityKrb5Conf());
            hiveConnectionParam.setLoginUserKeytabPath(hiveParam.getLoginUserKeytabPath());
            hiveConnectionParam.setLoginUserKeytabUsername(hiveParam.getLoginUserKeytabUsername());
        }
        hiveConnectionParam.setOther(hiveParam.getOther());
        return hiveConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, HiveConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.ORG_APACHE_HIVE_JDBC_HIVE_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.HIVE_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        HiveConnectionParam hiveConnectionParam = (HiveConnectionParam) connectionParam;
        String jdbcUrl = hiveConnectionParam.getJdbcUrl();
        if (MapUtils.isNotEmpty(hiveConnectionParam.getOther())) {
            return jdbcUrl + "?" + transformOther(hiveConnectionParam.getOther());
        }
        return jdbcUrl;
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws IOException, ClassNotFoundException, SQLException {
        HiveConnectionParam hiveConnectionParam = (HiveConnectionParam) connectionParam;
        CommonUtils.loadKerberosConf(hiveConnectionParam.getJavaSecurityKrb5Conf(),
                hiveConnectionParam.getLoginUserKeytabUsername(), hiveConnectionParam.getLoginUserKeytabPath());
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(connectionParam),
                hiveConnectionParam.getUser(), PasswordUtils.decodePassword(hiveConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.HIVE;
    }

    @Override
    public DataSourceProcessor create() {
        return new HiveDataSourceProcessor();
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        List<String> otherList = new ArrayList<>();
        otherMap.forEach((key, value) -> otherList.add(String.format("%s=%s", key, value)));
        return String.join(";", otherList);
    }

}
