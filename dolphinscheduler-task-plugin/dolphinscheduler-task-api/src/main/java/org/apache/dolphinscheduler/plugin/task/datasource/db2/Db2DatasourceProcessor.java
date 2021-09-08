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

package org.apache.dolphinscheduler.plugin.task.datasource.db2;

import static org.apache.dolphinscheduler.plugin.task.datasource.PasswordUtils.decodePassword;
import static org.apache.dolphinscheduler.plugin.task.datasource.PasswordUtils.encodePassword;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.COLON;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.COMMA;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.COM_DB2_JDBC_DRIVER;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.DOUBLE_SLASH;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.JDBC_DB2;

import org.apache.dolphinscheduler.plugin.task.datasource.AbstractDatasourceProcessor;
import org.apache.dolphinscheduler.plugin.task.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.plugin.task.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.task.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.commons.collections.MapUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Db2DatasourceProcessor extends AbstractDatasourceProcessor {

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        Db2ConnectionParam connectionParams = (Db2ConnectionParam) createConnectionParams(connectionJson);

        Db2DatasourceParamDTO db2DatasourceParamDTO = new Db2DatasourceParamDTO();
        db2DatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        db2DatasourceParamDTO.setOther(parseOther(connectionParams.getOther()));
        db2DatasourceParamDTO.setUserName(db2DatasourceParamDTO.getUserName());

        String[] hostSeperator = connectionParams.getAddress().split(DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(COMMA);
        db2DatasourceParamDTO.setHost(hostPortArray[0].split(COLON)[0]);
        db2DatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(COLON)[1]));

        return db2DatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        Db2DatasourceParamDTO db2Param = (Db2DatasourceParamDTO) datasourceParam;
        String address = String.format("%s%s:%s", JDBC_DB2, db2Param.getHost(), db2Param.getPort());
        String jdbcUrl = String.format("%s/%s", address, db2Param.getDatabase());

        Db2ConnectionParam db2ConnectionParam = new Db2ConnectionParam();
        db2ConnectionParam.setAddress(address);
        db2ConnectionParam.setDatabase(db2Param.getDatabase());
        db2ConnectionParam.setJdbcUrl(jdbcUrl);
        db2ConnectionParam.setUser(db2Param.getUserName());
        db2ConnectionParam.setPassword(encodePassword(db2Param.getPassword()));
        db2ConnectionParam.setOther(transformOther(db2Param.getOther()));

        return db2ConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, Db2ConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return COM_DB2_JDBC_DRIVER;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        Db2ConnectionParam db2ConnectionParam = (Db2ConnectionParam) connectionParam;
        if (StringUtils.isNotEmpty(db2ConnectionParam.getOther())) {
            return String.format("%s;%s", db2ConnectionParam.getJdbcUrl(), db2ConnectionParam.getOther());
        }
        return db2ConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        Db2ConnectionParam db2ConnectionParam = (Db2ConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(db2ConnectionParam),
                db2ConnectionParam.getUser(), decodePassword(db2ConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.DB2;
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        otherMap.forEach((key, value) -> stringBuilder.append(String.format("%s=%s%s", key, value, ";")));
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    private Map<String, String> parseOther(String other) {
        if (other == null) {
            return null;
        }
        Map<String, String> otherMap = new LinkedHashMap<>();
        for (String config : other.split("&")) {
            otherMap.put(config.split("=")[0], config.split("=")[1]);
        }
        return otherMap;
    }
}
