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

package org.apache.dolphinscheduler.common.datasource.presto;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.datasource.AbstractDatasourceProcessor;
import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.common.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.ConnectionParam;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;

import org.apache.commons.collections4.MapUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PrestoDatasourceProcessor extends AbstractDatasourceProcessor {

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        PrestoConnectionParam connectionParams = (PrestoConnectionParam) createConnectionParams(connectionJson);

        String[] hostSeperator = connectionParams.getAddress().split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);

        PrestoDatasourceParamDTO prestoDatasourceParamDTO = new PrestoDatasourceParamDTO();
        prestoDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        prestoDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);
        prestoDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        prestoDatasourceParamDTO.setUserName(connectionParams.getUser());
        prestoDatasourceParamDTO.setOther(parseOther(connectionParams.getOther()));

        return prestoDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        PrestoDatasourceParamDTO prestoParam = (PrestoDatasourceParamDTO) datasourceParam;
        String address = String.format("%s%s:%s", Constants.JDBC_PRESTO, prestoParam.getHost(), prestoParam.getPort());
        String jdbcUrl = address + "/" + prestoParam.getDatabase();

        PrestoConnectionParam prestoConnectionParam = new PrestoConnectionParam();
        prestoConnectionParam.setUser(prestoParam.getUserName());
        prestoConnectionParam.setPassword(CommonUtils.encodePassword(prestoParam.getPassword()));
        prestoConnectionParam.setOther(transformOther(prestoParam.getOther()));
        prestoConnectionParam.setAddress(address);
        prestoConnectionParam.setJdbcUrl(jdbcUrl);
        prestoConnectionParam.setDatabase(prestoParam.getDatabase());

        return prestoConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, PrestoConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return Constants.COM_PRESTO_JDBC_DRIVER;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        PrestoConnectionParam prestoConnectionParam = (PrestoConnectionParam) connectionParam;
        if (StringUtils.isNotEmpty(prestoConnectionParam.getOther())) {
            return String.format("%s?%s", prestoConnectionParam.getJdbcUrl(), prestoConnectionParam.getOther());
        }
        return prestoConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        PrestoConnectionParam prestoConnectionParam = (PrestoConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(connectionParam),
                prestoConnectionParam.getUser(), CommonUtils.decodePassword(prestoConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.PRESTO;
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isNotEmpty(otherMap)) {
            List<String> list = new ArrayList<>();
            otherMap.forEach((key, value) -> list.add(String.format("%s=%s", key, value)));
            return String.join("&", list);
        }
        return null;
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
