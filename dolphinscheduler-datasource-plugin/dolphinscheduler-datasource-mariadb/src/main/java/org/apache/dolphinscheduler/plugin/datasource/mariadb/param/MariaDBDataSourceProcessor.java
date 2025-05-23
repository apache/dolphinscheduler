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

package org.apache.dolphinscheduler.plugin.datasource.mariadb.param;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.constants.DataSourceConstants;
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
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.google.auto.service.AutoService;

@AutoService(DataSourceProcessor.class)
@Slf4j
public class MariaDBDataSourceProcessor extends AbstractDataSourceProcessor {

    private static final String ALLOW_LOAD_LOCAL_IN_FILE_NAME = "allowLoadLocalInfile";

    private static final String AUTO_DESERIALIZE = "autoDeserialize";

    private static final String ALLOW_LOCAL_IN_FILE_NAME = "allowLocalInfile";

    private static final String ALLOW_URL_IN_LOCAL_IN_FILE_NAME = "allowUrlInLocalInfile";

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, MariaDBDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        MariaDBConnectionParam connectionParams = (MariaDBConnectionParam) createConnectionParams(connectionJson);
        MariaDBDataSourceParamDTO mariadbDatasourceParamDTO = new MariaDBDataSourceParamDTO();

        mariadbDatasourceParamDTO.setUserName(connectionParams.getUser());
        mariadbDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        mariadbDatasourceParamDTO.setOther(connectionParams.getOther());

        String address = connectionParams.getAddress();
        String[] hostSeperator = address.split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);
        mariadbDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        mariadbDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);

        return mariadbDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO dataSourceParam) {
        MariaDBDataSourceParamDTO mariadbDatasourceParam = (MariaDBDataSourceParamDTO) dataSourceParam;
        String address = String.format("%s%s:%s", DataSourceConstants.JDBC_MARIADB, mariadbDatasourceParam.getHost(),
                mariadbDatasourceParam.getPort());
        String jdbcUrl = String.format("%s/%s", address, mariadbDatasourceParam.getDatabase());

        MariaDBConnectionParam mariadbConnectionParam = new MariaDBConnectionParam();
        mariadbConnectionParam.setJdbcUrl(jdbcUrl);
        mariadbConnectionParam.setDatabase(mariadbDatasourceParam.getDatabase());
        mariadbConnectionParam.setAddress(address);
        mariadbConnectionParam.setUser(mariadbDatasourceParam.getUserName());
        mariadbConnectionParam.setPassword(PasswordUtils.encodePassword(mariadbDatasourceParam.getPassword()));
        mariadbConnectionParam.setDriverClassName(getDatasourceDriver());
        mariadbConnectionParam.setValidationQuery(getValidationQuery());
        mariadbConnectionParam.setOther(mariadbDatasourceParam.getOther());

        return mariadbConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, MariaDBConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_MARIADB_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.MARIADB_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        MariaDBConnectionParam mariadbConnectionParam = (MariaDBConnectionParam) connectionParam;
        if (MapUtils.isNotEmpty(mariadbConnectionParam.getOther())) {
            return String.format("%s?%s", mariadbConnectionParam.getJdbcUrl(),
                    transformOther(mariadbConnectionParam.getOther()));
        }
        return mariadbConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        MariaDBConnectionParam mariadbConnectionParam = (MariaDBConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        String user = mariadbConnectionParam.getUser();
        if (user.contains(AUTO_DESERIALIZE)) {
            log.warn("sensitive param : {} in username field is filtered", AUTO_DESERIALIZE);
            user = user.replace(AUTO_DESERIALIZE, "");
        }
        String password = PasswordUtils.decodePassword(mariadbConnectionParam.getPassword());
        if (password.contains(AUTO_DESERIALIZE)) {
            log.warn("sensitive param : {} in password field is filtered", AUTO_DESERIALIZE);
            password = password.replace(AUTO_DESERIALIZE, "");
        }

        Properties connectionProperties = getConnectionProperties(mariadbConnectionParam, user, password);

        return DriverManager.getConnection(getJdbcUrl(connectionParam), connectionProperties);
    }

    private Properties getConnectionProperties(MariaDBConnectionParam mariadbConnectionParam, String user,
                                               String password) {
        Properties connectionProperties = new Properties();
        connectionProperties.put("user", user);
        connectionProperties.put("password", password);
        Map<String, String> paramMap = mariadbConnectionParam.getOther();
        if (MapUtils.isNotEmpty(paramMap)) {
            paramMap.forEach((k, v) -> {
                if (!checkKeyIsLegitimate(k)) {
                    log.info("Key `{}` is not legitimate for security reason", k);
                    return;
                }
                connectionProperties.put(k, v);
            });
        }
        connectionProperties.put(AUTO_DESERIALIZE, "false");
        connectionProperties.put(ALLOW_LOAD_LOCAL_IN_FILE_NAME, "false");
        connectionProperties.put(ALLOW_LOCAL_IN_FILE_NAME, "false");
        connectionProperties.put(ALLOW_URL_IN_LOCAL_IN_FILE_NAME, "false");
        return connectionProperties;
    }

    @Override
    public DbType getDbType() {
        return DbType.MARIADB;
    }

    @Override
    public DataSourceProcessor create() {
        return new MariaDBDataSourceProcessor();
    }

    @Override
    public List<String> splitAndRemoveComment(String sql) {
        String cleanSQL = SQLParserUtils.removeComment(sql, com.alibaba.druid.DbType.mariadb);
        return SQLParserUtils.split(cleanSQL, com.alibaba.druid.DbType.mariadb);
    }

    private static boolean checkKeyIsLegitimate(String key) {
        return !key.contains(ALLOW_LOAD_LOCAL_IN_FILE_NAME)
                && !key.contains(AUTO_DESERIALIZE)
                && !key.contains(ALLOW_LOCAL_IN_FILE_NAME)
                && !key.contains(ALLOW_URL_IN_LOCAL_IN_FILE_NAME);
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isNotEmpty(otherMap)) {
            List<String> list = new ArrayList<>(otherMap.size());
            otherMap.forEach((key, value) -> list.add(String.format("%s=%s", key, value)));
            return String.join("&", list);
        }
        return null;
    }

}
