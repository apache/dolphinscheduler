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

package org.apache.dolphinscheduler.plugin.datasource.hana.param;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;
@AutoService(DataSourceProcessor.class)
public class HanaDataSourceProcessor extends AbstractDataSourceProcessor {

    private final Logger logger = LoggerFactory.getLogger(HanaDataSourceProcessor.class);

    private static final String APPEND_PARAMS = "reconnect=true";
    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, HanaDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        HanaConnectionParam connectionParams = (HanaConnectionParam) createConnectionParams(connectionJson);
        HanaDataSourceParamDTO mysqlDatasourceParamDTO = new HanaDataSourceParamDTO();
        mysqlDatasourceParamDTO.setUserName(connectionParams.getUser());
        mysqlDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        String address = connectionParams.getAddress();
        String[] hostSeperator = address.split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);
        mysqlDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        mysqlDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);

        return mysqlDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO dataSourceParam) {
        HanaDataSourceParamDTO hanaDatasourceParam = (HanaDataSourceParamDTO) dataSourceParam;
        String address = String.format("%s%s:%s", DataSourceConstants.JDBC_HANA, hanaDatasourceParam.getHost(),
                hanaDatasourceParam.getPort());
        String jdbcUrl = String.format("%s?currentschema=%s", address, hanaDatasourceParam.getDatabase());

        HanaConnectionParam hanaConnectionParam = new HanaConnectionParam();
        hanaConnectionParam.setJdbcUrl(jdbcUrl);
        hanaConnectionParam.setValidationQuery("select 1 from DUMMY");
        hanaConnectionParam.setDatabase(hanaDatasourceParam.getDatabase());
        hanaConnectionParam.setAddress(address);
        hanaConnectionParam.setUser(hanaDatasourceParam.getUserName());
        hanaConnectionParam.setPassword(PasswordUtils.encodePassword(hanaDatasourceParam.getPassword()));
        hanaConnectionParam.setDriverClassName(getDatasourceDriver());
        hanaConnectionParam.setOther(hanaDatasourceParam.getOther());

        return hanaConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, HanaConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_HANA_DB_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.COM_HANA_DB_JDBC_DRIVER;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        HanaConnectionParam hanaConnectionParam = (HanaConnectionParam) connectionParam;
        String jdbcUrl = hanaConnectionParam.getJdbcUrl();
        if (MapUtils.isNotEmpty(hanaConnectionParam.getOther())) {
            return String.format("%s?%s&%s", jdbcUrl, hanaConnectionParam.getOther(), APPEND_PARAMS);
        }
        return String.format("%s&%s", jdbcUrl, APPEND_PARAMS);
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        HanaConnectionParam mysqlConnectionParam = (HanaConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        String user = mysqlConnectionParam.getUser();
        String password = PasswordUtils.decodePassword(mysqlConnectionParam.getPassword());
        return DriverManager.getConnection(getJdbcUrl(connectionParam), user, password);
    }

    @Override
    public DbType getDbType() {
        return DbType.HANA;
    }

    @Override
    public DataSourceProcessor create() {
        return new HanaDataSourceProcessor();
    }
}
