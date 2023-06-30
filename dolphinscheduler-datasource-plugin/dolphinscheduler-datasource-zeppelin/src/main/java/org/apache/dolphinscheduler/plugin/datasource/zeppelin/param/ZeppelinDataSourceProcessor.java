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

package org.apache.dolphinscheduler.plugin.datasource.zeppelin.param;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.zeppelin.client.ClientConfig;
import org.apache.zeppelin.client.ZeppelinClient;

import java.sql.Connection;
import java.text.MessageFormat;

@AutoService(DataSourceProcessor.class)
@Slf4j
public class ZeppelinDataSourceProcessor implements DataSourceProcessor {
    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, ZeppelinDataSourceParamDTO.class);
    }

    @Override
    public void checkDatasourceParam(BaseDataSourceParamDTO datasourceParamDTO) {
        ZeppelinDataSourceParamDTO zeppelinDataSourceParamDTO = (ZeppelinDataSourceParamDTO) datasourceParamDTO;
        if (StringUtils.isEmpty(zeppelinDataSourceParamDTO.getRestEndpoint()) || StringUtils.isEmpty(zeppelinDataSourceParamDTO.getUserName())) {
            throw new IllegalArgumentException("zeppelin datasource param is not valid");
        }
    }

    @Override
    public String getDatasourceUniqueId(ConnectionParam connectionParam, DbType dbType) {
        ZeppelinConnectionParam baseConnectionParam = (ZeppelinConnectionParam) connectionParam;
        return MessageFormat.format("{0}@{1}@{2}@{3}", dbType.getDescp(), baseConnectionParam.getRestEndpoint(), baseConnectionParam.getUsername(), PasswordUtils.encodePassword(baseConnectionParam.getPassword()));
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        ZeppelinConnectionParam connectionParams = (ZeppelinConnectionParam) createConnectionParams(connectionJson);
        ZeppelinDataSourceParamDTO zeppelinDataSourceParamDTO = new ZeppelinDataSourceParamDTO();

        zeppelinDataSourceParamDTO.setUserName(connectionParams.getUsername());
        zeppelinDataSourceParamDTO.setPassword(connectionParams.getPassword());
        zeppelinDataSourceParamDTO.setRestEndpoint(connectionParams.getRestEndpoint());
        return zeppelinDataSourceParamDTO;
    }

    @Override
    public ZeppelinConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        ZeppelinDataSourceParamDTO zeppelinDataSourceParam = (ZeppelinDataSourceParamDTO) datasourceParam;
        ZeppelinConnectionParam zeppelinConnectionParam = new ZeppelinConnectionParam();
        zeppelinConnectionParam.setUsername(zeppelinDataSourceParam.getUserName());
        zeppelinConnectionParam.setPassword(zeppelinDataSourceParam.getPassword());
        zeppelinConnectionParam.setRestEndpoint(zeppelinDataSourceParam.getRestEndpoint());

        return zeppelinConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, ZeppelinConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return "";
    }

    @Override
    public String getValidationQuery() {
        return "";
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        return "";
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) {
        return null;
    }

    @Override
    public boolean testConnection(ConnectionParam connectionParam) {
        ZeppelinConnectionParam baseConnectionParam = (ZeppelinConnectionParam) connectionParam;
        ClientConfig clientConfig = new ClientConfig(baseConnectionParam.getRestEndpoint());

        try {
            // If the login fails, an exception will be thrown directly
            ZeppelinClient zeppelinClient = new ZeppelinClient(clientConfig);
            zeppelinClient.login(baseConnectionParam.username, baseConnectionParam.password);
            String version = zeppelinClient.getVersion();
            log.info("zeppelin client connects to server successfully, version is {}", version);
            return true;
        } catch (Exception e) {
            log.info("zeppelin client failed to connect to the server");
            return false;
        }
    }

    @Override
    public DbType getDbType() {
        return DbType.ZEPPELIN;
    }

    @Override
    public DataSourceProcessor create() {
        return new ZeppelinDataSourceProcessor();
    }
}
