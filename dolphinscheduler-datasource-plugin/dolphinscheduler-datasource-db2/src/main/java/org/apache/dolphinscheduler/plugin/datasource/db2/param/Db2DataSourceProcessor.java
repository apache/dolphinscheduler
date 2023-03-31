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

package org.apache.dolphinscheduler.plugin.datasource.db2.param;

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
public class Db2DataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, Db2DataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        Db2ConnectionParam connectionParams = (Db2ConnectionParam) createConnectionParams(connectionJson);

        Db2DataSourceParamDTO db2DatasourceParamDTO = new Db2DataSourceParamDTO();
        db2DatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        db2DatasourceParamDTO.setOther(connectionParams.getOther());
        db2DatasourceParamDTO.setUserName(connectionParams.getUser());

        String[] hostSeperator = connectionParams.getAddress().split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);
        db2DatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);
        db2DatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));

        return db2DatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        Db2DataSourceParamDTO db2Param = (Db2DataSourceParamDTO) datasourceParam;
        String address = String.format("%s%s:%s", DataSourceConstants.JDBC_DB2, db2Param.getHost(), db2Param.getPort());
        String jdbcUrl = String.format("%s/%s", address, db2Param.getDatabase());

        Db2ConnectionParam db2ConnectionParam = new Db2ConnectionParam();
        db2ConnectionParam.setAddress(address);
        db2ConnectionParam.setDatabase(db2Param.getDatabase());
        db2ConnectionParam.setJdbcUrl(jdbcUrl);
        db2ConnectionParam.setUser(db2Param.getUserName());
        db2ConnectionParam.setPassword(PasswordUtils.encodePassword(db2Param.getPassword()));
        db2ConnectionParam.setDriverClassName(getDatasourceDriver());
        db2ConnectionParam.setValidationQuery(getValidationQuery());
        db2ConnectionParam.setOther(db2Param.getOther());

        return db2ConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, Db2ConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_DB2_JDBC_DRIVER;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        Db2ConnectionParam db2ConnectionParam = (Db2ConnectionParam) connectionParam;
        if (MapUtils.isNotEmpty(db2ConnectionParam.getOther())) {
            return String.format("%s:%s", db2ConnectionParam.getJdbcUrl(),
                    transformOther(db2ConnectionParam.getOther()));
        }
        return db2ConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        Db2ConnectionParam db2ConnectionParam = (Db2ConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(db2ConnectionParam),
                db2ConnectionParam.getUser(), PasswordUtils.decodePassword(db2ConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.DB2;
    }

    @Override
    public DataSourceProcessor create() {
        return new Db2DataSourceProcessor();
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.DB2_VALIDATION_QUERY;
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
