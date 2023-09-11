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

package org.apache.dolphinscheduler.plugin.datasource.xugu.param;

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
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.auto.service.AutoService;
@AutoService(DataSourceProcessor.class)
public class XuguDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, XuguDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        XuguConnectionParam connectionParams = (XuguConnectionParam) createConnectionParams(connectionJson);
        XuguDataSourceParamDTO xuguDatasourceParamDTO = new XuguDataSourceParamDTO();

        xuguDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        xuguDatasourceParamDTO.setUserName(connectionParams.getUser());
        xuguDatasourceParamDTO.setOther(parseOther(connectionParams.getOther()));

        String address = connectionParams.getAddress();
        String[] hostSeperator = address.split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);
        xuguDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        xuguDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);

        return xuguDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        XuguDataSourceParamDTO xuguParam = (XuguDataSourceParamDTO) datasourceParam;
        String address;
        String jdbcUrl;

        address = String.format("%s%s:%s",
                DataSourceConstants.JDBC_XUGU, xuguParam.getHost(), xuguParam.getPort());
        jdbcUrl = address + "/" + xuguParam.getDatabase();

        XuguConnectionParam xuguConnectionParam = new XuguConnectionParam();
        xuguConnectionParam.setUser(xuguParam.getUserName());
        xuguConnectionParam.setPassword(PasswordUtils.encodePassword(xuguParam.getPassword()));
        xuguConnectionParam.setAddress(address);
        xuguConnectionParam.setJdbcUrl(jdbcUrl);
        xuguConnectionParam.setDatabase(xuguParam.getDatabase());
        xuguConnectionParam.setDriverClassName(getDatasourceDriver());
        xuguConnectionParam.setValidationQuery(getValidationQuery());
        xuguConnectionParam.setOther(transformOther(xuguParam.getOther()));
        xuguConnectionParam.setProps(xuguParam.getOther());

        return xuguConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, XuguConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_XUGU_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.XUGU_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        XuguConnectionParam xuguConnectionParam = (XuguConnectionParam) connectionParam;
        if (!StringUtils.isEmpty(xuguConnectionParam.getOther())) {
            return String.format("%s?%s", xuguConnectionParam.getJdbcUrl(), xuguConnectionParam.getOther());
        }
        return xuguConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        XuguConnectionParam xuguConnectionParam = (XuguConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(connectionParam),
                xuguConnectionParam.getUser(), PasswordUtils.decodePassword(xuguConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.XUGU;
    }

    @Override
    public DataSourceProcessor create() {
        return new XuguDataSourceProcessor();
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        List<String> list = new ArrayList<>();
        otherMap.forEach((key, value) -> list.add(String.format("%s=%s", key, value)));
        return String.join("&", list);
    }

    private Map<String, String> parseOther(String other) {
        if (StringUtils.isEmpty(other)) {
            return null;
        }
        Map<String, String> otherMap = new LinkedHashMap<>();
        String[] configs = other.split("&");
        for (String config : configs) {
            otherMap.put(config.split("=")[0], config.split("=")[1]);
        }
        return otherMap;
    }
}
