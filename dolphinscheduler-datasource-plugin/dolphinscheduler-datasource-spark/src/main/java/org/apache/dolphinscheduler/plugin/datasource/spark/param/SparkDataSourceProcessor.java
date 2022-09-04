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

package org.apache.dolphinscheduler.plugin.datasource.spark.param;

import org.apache.dolphinscheduler.plugin.datasource.api.datasource.AbstractDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.commons.collections4.MapUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.auto.service.AutoService;

@AutoService(DataSourceProcessor.class)
public class SparkDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDataSourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, SparkDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDataSourceParamDTO(String connectionJson) {
        SparkConnectionParam
                connectionParams = (SparkConnectionParam) createConnectionParams(connectionJson);

        SparkDataSourceParamDTO
                sparkDataSourceParamDTO = new SparkDataSourceParamDTO();
        sparkDataSourceParamDTO.setDatabase(connectionParams.getDatabase());
        sparkDataSourceParamDTO.setUserName(connectionParams.getUser());
        sparkDataSourceParamDTO.setOther(parseOther(connectionParams.getOther()));
        sparkDataSourceParamDTO.setJavaSecurityKrb5Conf(connectionParams.getJavaSecurityKrb5Conf());
        sparkDataSourceParamDTO.setLoginUserKeytabPath(connectionParams.getLoginUserKeytabPath());
        sparkDataSourceParamDTO.setLoginUserKeytabUsername(connectionParams.getLoginUserKeytabUsername());

        StringBuilder hosts = new StringBuilder();
        String[] tmpArray = connectionParams.getAddress().split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = tmpArray[tmpArray.length - 1].split(Constants.COMMA);
        Arrays.stream(hostPortArray).forEach(hostPort -> hosts.append(hostPort.split(Constants.COLON)[0]).append(Constants.COMMA));
        hosts.deleteCharAt(hosts.length() - 1);

        sparkDataSourceParamDTO.setHost(hosts.toString());
        sparkDataSourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));

        return sparkDataSourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO dataSourceParam) {
        StringBuilder address = new StringBuilder();
        SparkDataSourceParamDTO sparkDataSourceParam = (SparkDataSourceParamDTO) dataSourceParam;
        address.append(Constants.JDBC_HIVE_2);
        for (String zkHost : sparkDataSourceParam.getHost().split(",")) {
            address.append(String.format("%s:%s,", zkHost, sparkDataSourceParam.getPort()));
        }
        address.deleteCharAt(address.length() - 1);

        String jdbcUrl = address + "/" + sparkDataSourceParam.getDatabase();

        SparkConnectionParam
                sparkConnectionParam = new SparkConnectionParam();
        sparkConnectionParam.setPassword(PasswordUtils.encodePassword(sparkDataSourceParam.getPassword()));
        sparkConnectionParam.setUser(sparkDataSourceParam.getUserName());
        sparkConnectionParam.setOther(transformOther(sparkDataSourceParam.getOther()));
        sparkConnectionParam.setDatabase(sparkDataSourceParam.getDatabase());
        sparkConnectionParam.setAddress(address.toString());
        sparkConnectionParam.setJdbcUrl(jdbcUrl);
        sparkConnectionParam.setDriverClassName(getDataSourceDriver());
        sparkConnectionParam.setValidationQuery(getValidationQuery());
        sparkConnectionParam.setProps(sparkDataSourceParam.getOther());

        if (CommonUtils.getKerberosStartupState()) {
            sparkConnectionParam.setPrincipal(sparkDataSourceParam.getPrincipal());
            sparkConnectionParam.setJavaSecurityKrb5Conf(sparkDataSourceParam.getJavaSecurityKrb5Conf());
            sparkConnectionParam.setLoginUserKeytabPath(sparkDataSourceParam.getLoginUserKeytabPath());
            sparkConnectionParam.setLoginUserKeytabUsername(sparkDataSourceParam.getLoginUserKeytabUsername());
        }

        return sparkConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, SparkConnectionParam.class);
    }

    @Override
    public String getDataSourceDriver() {
        return Constants.ORG_APACHE_HIVE_JDBC_HIVE_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return Constants.HIVE_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        SparkConnectionParam
                sparkConnectionParam = (SparkConnectionParam) connectionParam;
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
        Class.forName(getDataSourceDriver());
        return DriverManager.getConnection(getJdbcUrl(sparkConnectionParam),
                sparkConnectionParam.getUser(), PasswordUtils.decodePassword(sparkConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.SPARK;
    }

    @Override
    public DataSourceProcessor create() {
        return new SparkDataSourceProcessor();
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        List<String> stringBuilder = otherMap.entrySet().stream()
                .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue())).collect(Collectors.toList());
        return String.join(";", stringBuilder);
    }

    private Map<String, String> parseOther(String other) {
        if (StringUtils.isEmpty(other)) {
            return null;
        }
        Map<String, String> otherMap = new LinkedHashMap<>();
        String[] configs = other.split(";");
        for (String config : configs) {
            otherMap.put(config.split("=")[0], config.split("=")[1]);
        }
        return otherMap;
    }
}
