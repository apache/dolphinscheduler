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

package org.apache.dolphinscheduler.plugin.datasource.api.datasource.spark;

import org.apache.dolphinscheduler.plugin.datasource.api.datasource.AbstractDatasourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

public class SparkDatasourceProcessor extends AbstractDatasourceProcessor {

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        SparkConnectionParam connectionParams = (SparkConnectionParam) createConnectionParams(connectionJson);

        SparkDatasourceParamDTO sparkDatasourceParamDTO = new SparkDatasourceParamDTO();
        sparkDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        sparkDatasourceParamDTO.setUserName(connectionParams.getUser());
        sparkDatasourceParamDTO.setOther(parseOther(getDbType(),connectionParams.getOther()));
        sparkDatasourceParamDTO.setJavaSecurityKrb5Conf(connectionParams.getJavaSecurityKrb5Conf());
        sparkDatasourceParamDTO.setLoginUserKeytabPath(connectionParams.getLoginUserKeytabPath());
        sparkDatasourceParamDTO.setLoginUserKeytabUsername(connectionParams.getLoginUserKeytabUsername());

        StringBuilder hosts = new StringBuilder();
        String[] tmpArray = connectionParams.getAddress().split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = tmpArray[tmpArray.length - 1].split(Constants.COMMA);
        Arrays.stream(hostPortArray).forEach(hostPort -> hosts.append(hostPort.split(Constants.COLON)[0]).append(Constants.COMMA));
        hosts.deleteCharAt(hosts.length() - 1);

        sparkDatasourceParamDTO.setHost(hosts.toString());
        sparkDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));

        return sparkDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO dataSourceParam) {
        StringBuilder address = new StringBuilder();
        SparkDatasourceParamDTO sparkDatasourceParam = (SparkDatasourceParamDTO) dataSourceParam;
        address.append(Constants.JDBC_HIVE_2);
        for (String zkHost : sparkDatasourceParam.getHost().split(",")) {
            address.append(String.format("%s:%s,", zkHost, sparkDatasourceParam.getPort()));
        }
        address.deleteCharAt(address.length() - 1);

        String jdbcUrl = address + "/" + sparkDatasourceParam.getDatabase();
        if (CommonUtils.getKerberosStartupState()) {
            jdbcUrl += ";principal=" + sparkDatasourceParam.getPrincipal();
        }

        SparkConnectionParam sparkConnectionParam = new SparkConnectionParam();
        sparkConnectionParam.setPassword(PasswordUtils.encodePassword(sparkDatasourceParam.getPassword()));
        sparkConnectionParam.setUser(sparkDatasourceParam.getUserName());
        sparkConnectionParam.setOther(transformOther(getDbType(),sparkDatasourceParam.getOther()));
        sparkConnectionParam.setDatabase(sparkDatasourceParam.getDatabase());
        sparkConnectionParam.setAddress(address.toString());
        sparkConnectionParam.setJdbcUrl(jdbcUrl);
        sparkConnectionParam.setDriverClassName(getDatasourceDriver());
        sparkConnectionParam.setValidationQuery(getValidationQuery());
        sparkConnectionParam.setProps(sparkDatasourceParam.getOther());

        if (CommonUtils.getKerberosStartupState()) {
            sparkConnectionParam.setPrincipal(sparkDatasourceParam.getPrincipal());
            sparkConnectionParam.setJavaSecurityKrb5Conf(sparkDatasourceParam.getJavaSecurityKrb5Conf());
            sparkConnectionParam.setLoginUserKeytabPath(sparkDatasourceParam.getLoginUserKeytabPath());
            sparkConnectionParam.setLoginUserKeytabUsername(sparkDatasourceParam.getLoginUserKeytabUsername());
        }

        return sparkConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, SparkConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return Constants.ORG_APACHE_HIVE_JDBC_HIVE_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return Constants.HIVE_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        SparkConnectionParam sparkConnectionParam = (SparkConnectionParam) connectionParam;
        if (!StringUtils.isEmpty(sparkConnectionParam.getOther())) {
            return String.format("%s;%s", sparkConnectionParam.getJdbcUrl(), sparkConnectionParam.getOther());
        }
        return sparkConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws IOException, ClassNotFoundException, SQLException {
        SparkConnectionParam sparkConnectionParam = (SparkConnectionParam) connectionParam;
        CommonUtils.loadKerberosConf(sparkConnectionParam.getJavaSecurityKrb5Conf(),
                sparkConnectionParam.getLoginUserKeytabUsername(), sparkConnectionParam.getLoginUserKeytabPath());
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(sparkConnectionParam),
                sparkConnectionParam.getUser(), PasswordUtils.decodePassword(sparkConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.SPARK;
    }

}
