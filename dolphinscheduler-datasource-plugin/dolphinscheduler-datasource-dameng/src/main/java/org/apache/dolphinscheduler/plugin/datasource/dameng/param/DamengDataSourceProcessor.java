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

package org.apache.dolphinscheduler.plugin.datasource.dameng.param;

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
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.auto.service.AutoService;

@AutoService(DataSourceProcessor.class)
public class DamengDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, DamengDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        DamengConnectionParam connectionParams = (DamengConnectionParam) createConnectionParams(connectionJson);
        DamengDataSourceParamDTO damengDatasourceParamDTO = new DamengDataSourceParamDTO();

        damengDatasourceParamDTO.setUserName(connectionParams.getUser());
        damengDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        damengDatasourceParamDTO.setOther(connectionParams.getOther());

        String address = connectionParams.getAddress();
        String[] hostSeperator = address.split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);
        damengDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        damengDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);

        return damengDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO dataSourceParam) {
        DamengDataSourceParamDTO dmDatasourceParam = (DamengDataSourceParamDTO) dataSourceParam;
        String address = String
                .format("%s%s:%s", DataSourceConstants.JDBC_DAMENG, dmDatasourceParam.getHost(),
                        dmDatasourceParam.getPort());
        String jdbcUrl = StringUtils.isEmpty(dmDatasourceParam.getDatabase()) ? address
                : String.format("%s/%s", address,
                        dmDatasourceParam.getDatabase());

        DamengConnectionParam damengConnectionParam = new DamengConnectionParam();
        damengConnectionParam.setJdbcUrl(jdbcUrl);
        damengConnectionParam.setDatabase(damengConnectionParam.getDatabase());
        damengConnectionParam.setAddress(address);
        damengConnectionParam.setUser(dmDatasourceParam.getUserName());
        damengConnectionParam.setPassword(PasswordUtils.encodePassword(dmDatasourceParam.getPassword()));
        damengConnectionParam.setDriverClassName(getDatasourceDriver());
        damengConnectionParam.setValidationQuery(getValidationQuery());
        damengConnectionParam.setOther(dmDatasourceParam.getOther());

        return damengConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, DamengConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_DAMENG_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.DAMENG_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        DamengConnectionParam damengConnectionParam = (DamengConnectionParam) connectionParam;
        String jdbcUrl = damengConnectionParam.getJdbcUrl();
        if (MapUtils.isNotEmpty(damengConnectionParam.getOther())) {
            return String.format("%s?%s", jdbcUrl, transformOther(damengConnectionParam.getOther()));
        }
        return jdbcUrl;
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        DamengConnectionParam damengConnectionParam = (DamengConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(damengConnectionParam), damengConnectionParam.getUser(),
                PasswordUtils.decodePassword(damengConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.DAMENG;
    }

    @Override
    public DataSourceProcessor create() {
        return new DamengDataSourceProcessor();
    }

    private String transformOther(Map<String, String> paramMap) {
        if (MapUtils.isEmpty(paramMap)) {
            return null;
        }
        List<String> otherList = new ArrayList<>();
        paramMap.forEach((key, value) -> otherList.add(String.format("%s=%s", key, value)));
        return String.join("&", otherList);
    }

}
