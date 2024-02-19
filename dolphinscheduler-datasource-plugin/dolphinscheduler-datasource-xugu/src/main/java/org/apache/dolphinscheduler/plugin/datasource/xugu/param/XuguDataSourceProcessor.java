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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.google.auto.service.AutoService;

@AutoService(DataSourceProcessor.class)
public class XuguDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, XuguDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) throws NumberFormatException {
        XuguConnectionParam connectionParams = (XuguConnectionParam) createConnectionParams(connectionJson);
        XuguDataSourceParamDTO xuguDataSourceParamDTO = new XuguDataSourceParamDTO();

        xuguDataSourceParamDTO.setUserName(connectionParams.getUser());
        xuguDataSourceParamDTO.setDatabase(connectionParams.getDatabase());
        xuguDataSourceParamDTO.setOther(connectionParams.getOther());

        String address = connectionParams.getAddress();
        String[] hostSeparator = address.split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeparator[hostSeparator.length - 1].split(Constants.COMMA);
        xuguDataSourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        xuguDataSourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);

        return xuguDataSourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        XuguDataSourceParamDTO xgDataSourceParamDTO = (XuguDataSourceParamDTO) datasourceParam;
        String address = String.format("%s%s:%s", DataSourceConstants.JDBC_XUGU, xgDataSourceParamDTO.getHost(),
                xgDataSourceParamDTO.getPort());
        String jdbcUrl = String.format("%s/%s", address, xgDataSourceParamDTO.getDatabase());

        XuguConnectionParam xuguConnectionParam = new XuguConnectionParam();
        xuguConnectionParam.setUser(xgDataSourceParamDTO.getUserName());
        xuguConnectionParam.setPassword(PasswordUtils.encodePassword(xgDataSourceParamDTO.getPassword()));
        xuguConnectionParam.setAddress(address);
        xuguConnectionParam.setJdbcUrl(jdbcUrl);
        xuguConnectionParam.setDatabase(xgDataSourceParamDTO.getDatabase());
        xuguConnectionParam.setDriverClassName(getDatasourceDriver());
        xuguConnectionParam.setValidationQuery(getValidationQuery());
        xuguConnectionParam.setOther(xgDataSourceParamDTO.getOther());

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
        String jdbcUrl = xuguConnectionParam.getJdbcUrl();
        if (MapUtils.isNotEmpty(xuguConnectionParam.getOther())) {
            return String.format("%s?%s", jdbcUrl, transformOther(xuguConnectionParam.getOther()));
        }
        return jdbcUrl;
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        XuguConnectionParam xuguConnectionParam = (XuguConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());

        return DriverManager.getConnection(getJdbcUrl(connectionParam), xuguConnectionParam.getUser(),
                PasswordUtils.decodePassword(xuguConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.XUGU;
    }

    @Override
    public DataSourceProcessor create() {
        return new XuguDataSourceProcessor();
    }

    @Override
    public List<String> splitAndRemoveComment(String sql) {
        return SQLParserUtils.splitAndRemoveComment(sql, com.alibaba.druid.DbType.xugu);
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
