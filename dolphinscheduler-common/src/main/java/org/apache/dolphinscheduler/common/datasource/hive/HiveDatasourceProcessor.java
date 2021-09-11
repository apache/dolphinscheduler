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

package org.apache.dolphinscheduler.common.datasource.hive;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.datasource.AbstractDatasourceProcessor;
import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.common.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.ConnectionParam;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.HiveConfUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class HiveDatasourceProcessor extends AbstractDatasourceProcessor {

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        HiveDataSourceParamDTO hiveDataSourceParamDTO = new HiveDataSourceParamDTO();
        HiveConnectionParam hiveConnectionParam = (HiveConnectionParam) createConnectionParams(connectionJson);

        hiveDataSourceParamDTO.setDatabase(hiveConnectionParam.getDatabase());
        hiveDataSourceParamDTO.setUserName(hiveConnectionParam.getUser());
        hiveDataSourceParamDTO.setOther(parseOther(hiveConnectionParam.getOther()));
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
        address.append(Constants.JDBC_HIVE_2);
        for (String zkHost : hiveParam.getHost().split(",")) {
            address.append(String.format("%s:%s,", zkHost, hiveParam.getPort()));
        }
        address.deleteCharAt(address.length() - 1);
        String jdbcUrl = address.toString() + "/" + hiveParam.getDatabase();
        if (CommonUtils.getKerberosStartupState()) {
            jdbcUrl += ";principal=" + hiveParam.getPrincipal();
        }

        HiveConnectionParam hiveConnectionParam = new HiveConnectionParam();
        hiveConnectionParam.setDatabase(hiveParam.getDatabase());
        hiveConnectionParam.setAddress(address.toString());
        hiveConnectionParam.setJdbcUrl(jdbcUrl);
        hiveConnectionParam.setUser(hiveParam.getUserName());
        hiveConnectionParam.setPassword(CommonUtils.encodePassword(hiveParam.getPassword()));

        if (CommonUtils.getKerberosStartupState()) {
            hiveConnectionParam.setPrincipal(hiveParam.getPrincipal());
            hiveConnectionParam.setJavaSecurityKrb5Conf(hiveParam.getJavaSecurityKrb5Conf());
            hiveConnectionParam.setLoginUserKeytabPath(hiveParam.getLoginUserKeytabPath());
            hiveConnectionParam.setLoginUserKeytabUsername(hiveParam.getLoginUserKeytabUsername());
        }
        hiveConnectionParam.setOther(transformOther(hiveParam.getOther()));
        return hiveConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, HiveConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return Constants.ORG_APACHE_HIVE_JDBC_HIVE_DRIVER;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        HiveConnectionParam hiveConnectionParam = (HiveConnectionParam) connectionParam;
        String jdbcUrl = hiveConnectionParam.getJdbcUrl();
        String otherParams = filterOther(hiveConnectionParam.getOther());
        if (StringUtils.isNotEmpty(otherParams) && !"?".equals(otherParams.substring(0, 1))) {
            jdbcUrl += ";";
        }
        return jdbcUrl + otherParams;
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws IOException, ClassNotFoundException, SQLException {
        HiveConnectionParam hiveConnectionParam = (HiveConnectionParam) connectionParam;
        CommonUtils.loadKerberosConf(hiveConnectionParam.getJavaSecurityKrb5Conf(),
                hiveConnectionParam.getLoginUserKeytabUsername(), hiveConnectionParam.getLoginUserKeytabPath());
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(connectionParam),
                hiveConnectionParam.getUser(), CommonUtils.decodePassword(hiveConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.HIVE;
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        otherMap.forEach((key, value) -> stringBuilder.append(String.format("%s=%s;", key, value)));
        return stringBuilder.toString();
    }

    private String filterOther(String otherParams) {
        if (StringUtils.isBlank(otherParams)) {
            return "";
        }

        StringBuilder hiveConfListSb = new StringBuilder();
        hiveConfListSb.append("?");
        StringBuilder sessionVarListSb = new StringBuilder();

        String[] otherArray = otherParams.split(";", -1);

        for (String conf : otherArray) {
            if (HiveConfUtils.isHiveConfVar(conf)) {
                hiveConfListSb.append(conf).append(";");
            } else {
                sessionVarListSb.append(conf).append(";");
            }
        }

        // remove the last ";"
        if (sessionVarListSb.length() > 0) {
            sessionVarListSb.deleteCharAt(sessionVarListSb.length() - 1);
        }

        if (hiveConfListSb.length() > 0) {
            hiveConfListSb.deleteCharAt(hiveConfListSb.length() - 1);
        }

        return sessionVarListSb.toString() + hiveConfListSb.toString();
    }

    private Map<String, String> parseOther(String other) {
        if (other == null) {
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
