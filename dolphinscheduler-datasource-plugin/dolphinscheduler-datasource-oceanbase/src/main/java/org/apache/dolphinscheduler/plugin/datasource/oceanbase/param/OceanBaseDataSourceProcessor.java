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

package org.apache.dolphinscheduler.plugin.datasource.oceanbase.param;

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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.google.auto.service.AutoService;

@Slf4j
@AutoService(DataSourceProcessor.class)
public class OceanBaseDataSourceProcessor extends AbstractDataSourceProcessor {

    private static final String ALLOW_LOAD_LOCAL_IN_FILE_NAME = "allowLoadLocalInfile";

    private static final String AUTO_DESERIALIZE = "autoDeserialize";

    private static final String ALLOW_LOCAL_IN_FILE_NAME = "allowLocalInfile";

    private static final String ALLOW_URL_IN_LOCAL_IN_FILE_NAME = "allowUrlInLocalInfile";

    private static final String APPEND_PARAMS =
            "allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false";

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, OceanBaseDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        OceanBaseConnectionParam connectionParams = (OceanBaseConnectionParam) createConnectionParams(connectionJson);
        OceanBaseDataSourceParamDTO dataSourceParamDTO = new OceanBaseDataSourceParamDTO();
        dataSourceParamDTO.setUserName(connectionParams.getUser());
        dataSourceParamDTO.setDatabase(connectionParams.getDatabase());
        dataSourceParamDTO.setOther(connectionParams.getOther());

        String address = connectionParams.getAddress();
        String[] hostSeparator = address.split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeparator[hostSeparator.length - 1].split(Constants.COMMA);
        dataSourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        dataSourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);
        return dataSourceParamDTO;
    }

    @Override
    public ConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        OceanBaseDataSourceParamDTO obDataSourceParamDTO = (OceanBaseDataSourceParamDTO) datasourceParam;
        String address = String.format("%s%s:%s", DataSourceConstants.JDBC_OCEANBASE, obDataSourceParamDTO.getHost(),
                obDataSourceParamDTO.getPort());
        String jdbcUrl = String.format("%s/%s", address, obDataSourceParamDTO.getDatabase());

        OceanBaseConnectionParam connectionParam = new OceanBaseConnectionParam();
        connectionParam.setJdbcUrl(jdbcUrl);
        connectionParam.setDatabase(obDataSourceParamDTO.getDatabase());
        connectionParam.setAddress(address);
        connectionParam.setUser(obDataSourceParamDTO.getUserName());
        connectionParam.setPassword(PasswordUtils.encodePassword(obDataSourceParamDTO.getPassword()));
        connectionParam.setCompatibleMode(obDataSourceParamDTO.getCompatibleMode());
        connectionParam.setDriverClassName(getDatasourceDriver());
        connectionParam.setValidationQuery(getValidationQuery(connectionParam.getCompatibleMode()));
        connectionParam.setOther(obDataSourceParamDTO.getOther());
        return connectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, OceanBaseConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_OCEANBASE_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        throw new UnsupportedOperationException("Can't get validation query without compatible mode");
    }

    public String getValidationQuery(String compatibleMode) {
        if (compatibleMode != null) {
            switch (compatibleMode.trim().toLowerCase()) {
                case "mysql":
                    return DataSourceConstants.MYSQL_VALIDATION_QUERY;
                case "oracle":
                    return DataSourceConstants.ORACLE_VALIDATION_QUERY;
            }
        }
        throw new UnsupportedOperationException("Invalid compatible mode: " + compatibleMode);

    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        OceanBaseConnectionParam obConnectionParam = (OceanBaseConnectionParam) connectionParam;
        String jdbcUrl = obConnectionParam.getJdbcUrl();
        if (MapUtils.isNotEmpty(obConnectionParam.getOther())) {
            return String.format("%s?%s&%s", jdbcUrl, transformOther(obConnectionParam.getOther()), APPEND_PARAMS);
        }
        return String.format("%s?%s", jdbcUrl, APPEND_PARAMS);
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

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException, IOException {
        OceanBaseConnectionParam obConnectionParam = (OceanBaseConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        String user = obConnectionParam.getUser();
        if (user.contains(AUTO_DESERIALIZE)) {
            log.warn("sensitive param : {} in username field is filtered", AUTO_DESERIALIZE);
            user = user.replace(AUTO_DESERIALIZE, "");
        }
        String password = PasswordUtils.decodePassword(obConnectionParam.getPassword());
        if (password.contains(AUTO_DESERIALIZE)) {
            log.warn("sensitive param : {} in password field is filtered", AUTO_DESERIALIZE);
            password = password.replace(AUTO_DESERIALIZE, "");
        }
        return DriverManager.getConnection(getJdbcUrl(connectionParam), user, password);
    }

    @Override
    public DbType getDbType() {
        return DbType.OCEANBASE;
    }

    @Override
    public DataSourceProcessor create() {
        return new OceanBaseDataSourceProcessor();
    }
}
