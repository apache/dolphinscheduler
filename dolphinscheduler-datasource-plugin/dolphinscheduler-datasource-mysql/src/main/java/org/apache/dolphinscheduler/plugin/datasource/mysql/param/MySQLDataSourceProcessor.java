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

package org.apache.dolphinscheduler.plugin.datasource.mysql.param;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.google.auto.service.AutoService;

@AutoService(DataSourceProcessor.class)
@Slf4j
public class MySQLDataSourceProcessor extends AbstractDataSourceProcessor {

    private static final String ALLOW_LOAD_LOCAL_IN_FILE_NAME = "allowLoadLocalInfile";

    private static final String AUTO_DESERIALIZE = "autoDeserialize";

    private static final String ALLOW_LOCAL_IN_FILE_NAME = "allowLocalInfile";

    private static final String ALLOW_URL_IN_LOCAL_IN_FILE_NAME = "allowUrlInLocalInfile";

    private static final String APPEND_PARAMS =
            "allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false";

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, MySQLDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        MySQLConnectionParam connectionParams = (MySQLConnectionParam) createConnectionParams(connectionJson);
        MySQLDataSourceParamDTO mysqlDatasourceParamDTO = new MySQLDataSourceParamDTO();

        mysqlDatasourceParamDTO.setUserName(connectionParams.getUser());
        mysqlDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        mysqlDatasourceParamDTO.setOther(connectionParams.getOther());

        String address = connectionParams.getAddress();
        String[] hostSeperator = address.split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);
        mysqlDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        mysqlDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);

        return mysqlDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO dataSourceParam) {
        MySQLDataSourceParamDTO mysqlDatasourceParam = (MySQLDataSourceParamDTO) dataSourceParam;
        String address = String.format("%s%s:%s", DataSourceConstants.JDBC_MYSQL, mysqlDatasourceParam.getHost(),
                mysqlDatasourceParam.getPort());
        String jdbcUrl = String.format("%s/%s", address, mysqlDatasourceParam.getDatabase());

        MySQLConnectionParam mysqlConnectionParam = new MySQLConnectionParam();
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
        return JSONUtils.parseObject(connectionJson, MySQLConnectionParam.class);
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
        MySQLConnectionParam mysqlConnectionParam = (MySQLConnectionParam) connectionParam;
        String jdbcUrl = mysqlConnectionParam.getJdbcUrl();
        if (MapUtils.isNotEmpty(mysqlConnectionParam.getOther())) {
            return String.format("%s?%s&%s", jdbcUrl, transformOther(mysqlConnectionParam.getOther()), APPEND_PARAMS);
        }
        return String.format("%s?%s", jdbcUrl, APPEND_PARAMS);
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        MySQLConnectionParam mysqlConnectionParam = (MySQLConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        String user = mysqlConnectionParam.getUser();
        if (user.contains(AUTO_DESERIALIZE)) {
            log.warn("sensitive param : {} in username field is filtered", AUTO_DESERIALIZE);
            user = user.replace(AUTO_DESERIALIZE, "");
        }
        String password = PasswordUtils.decodePassword(mysqlConnectionParam.getPassword());
        if (password.contains(AUTO_DESERIALIZE)) {
            log.warn("sensitive param : {} in password field is filtered", AUTO_DESERIALIZE);
            password = password.replace(AUTO_DESERIALIZE, "");
        }
        return DriverManager.getConnection(getJdbcUrl(connectionParam), user, password);
    }

    @Override
    public DbType getDbType() {
        return DbType.MYSQL;
    }

    @Override
    public DataSourceProcessor create() {
        return new MySQLDataSourceProcessor();
    }

    private String transformOther(Map<String, String> paramMap) {
        if (MapUtils.isEmpty(paramMap)) {
            return null;
        }
        Map<String, String> otherMap = new HashMap<>();
        paramMap.forEach((k, v) -> {
            if (!checkKeyIsLegitimate(k)) {
                return;
            }
            otherMap.put(k, v);
        });
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        List<String> otherList = new ArrayList<>();
        otherMap.forEach((key, value) -> otherList.add(String.format("%s=%s", key, value)));
        return String.join("&", otherList);
    }

    private static boolean checkKeyIsLegitimate(String key) {
        return !key.contains(ALLOW_LOAD_LOCAL_IN_FILE_NAME)
                && !key.contains(AUTO_DESERIALIZE)
                && !key.contains(ALLOW_LOCAL_IN_FILE_NAME)
                && !key.contains(ALLOW_URL_IN_LOCAL_IN_FILE_NAME);
    }

}
