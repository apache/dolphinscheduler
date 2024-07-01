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

package org.apache.dolphinscheduler.plugin.datasource.kyuubi.param;

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
public class KyuubiDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, KyuubiDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        KyuubiDataSourceParamDTO kyuubiDataSourceParamDTO = new KyuubiDataSourceParamDTO();
        KyuubiConnectionParam kyuubiConnectionParam = (KyuubiConnectionParam) createConnectionParams(connectionJson);
        kyuubiDataSourceParamDTO.setDatabase(kyuubiConnectionParam.getDatabase());
        kyuubiDataSourceParamDTO.setUserName(kyuubiConnectionParam.getUser());
        kyuubiDataSourceParamDTO.setOther(kyuubiConnectionParam.getOther());

        String[] tmpArray = kyuubiConnectionParam.getAddress().split(Constants.DOUBLE_SLASH);
        StringBuilder hosts = new StringBuilder();
        String[] hostPortArray = tmpArray[tmpArray.length - 1].split(Constants.COMMA);
        for (String hostPort : hostPortArray) {
            hosts.append(hostPort.split(Constants.COLON)[0]).append(Constants.COMMA);
        }
        hosts.deleteCharAt(hosts.length() - 1);
        kyuubiDataSourceParamDTO.setHost(hosts.toString());
        kyuubiDataSourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));

        return kyuubiDataSourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        KyuubiDataSourceParamDTO kyuubiParam = (KyuubiDataSourceParamDTO) datasourceParam;
        StringBuilder address = new StringBuilder();
        address.append(DataSourceConstants.JDBC_KYUUBI);
        for (String zkHost : kyuubiParam.getHost().split(",")) {
            address.append(String.format("%s:%s,", zkHost, kyuubiParam.getPort()));
        }
        address.deleteCharAt(address.length() - 1);
        String jdbcUrl = address + "/" + kyuubiParam.getDatabase();
        KyuubiConnectionParam kyuubiConnectionParam = new KyuubiConnectionParam();
        kyuubiConnectionParam.setDatabase(kyuubiParam.getDatabase());
        kyuubiConnectionParam.setAddress(address.toString());
        kyuubiConnectionParam.setJdbcUrl(jdbcUrl);
        kyuubiConnectionParam.setUser(kyuubiParam.getUserName());
        kyuubiConnectionParam.setPassword(PasswordUtils.encodePassword(kyuubiParam.getPassword()));
        kyuubiConnectionParam.setDriverClassName(getDatasourceDriver());
        kyuubiConnectionParam.setValidationQuery(getValidationQuery());
        kyuubiConnectionParam.setOther(kyuubiParam.getOther());
        return kyuubiConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, KyuubiConnectionParam.class);

    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.ORG_APACHE_KYUUBI_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.KYUUBI_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        KyuubiConnectionParam kyuubiConnectionParam = (KyuubiConnectionParam) connectionParam;
        String jdbcUrl = kyuubiConnectionParam.getJdbcUrl();

        if (MapUtils.isNotEmpty(kyuubiConnectionParam.getOther())) {
            return jdbcUrl + "?" + transformOther(kyuubiConnectionParam.getOther());
        }
        return jdbcUrl;
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        KyuubiConnectionParam kyuubiConnectionParam = (KyuubiConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        // todo:
        return DriverManager.getConnection(getJdbcUrl(connectionParam),
                kyuubiConnectionParam.getUser(), PasswordUtils.decodePassword(kyuubiConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.KYUUBI;
    }

    @Override
    public DataSourceProcessor create() {
        return new KyuubiDataSourceProcessor();
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
