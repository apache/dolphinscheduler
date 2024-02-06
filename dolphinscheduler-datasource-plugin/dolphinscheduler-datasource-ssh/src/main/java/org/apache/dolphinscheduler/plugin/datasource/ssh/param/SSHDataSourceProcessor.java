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

package org.apache.dolphinscheduler.plugin.datasource.ssh.param;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.AbstractDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.datasource.ssh.SshClientWrapper;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.text.MessageFormat;

import lombok.extern.slf4j.Slf4j;

import com.google.auto.service.AutoService;

@AutoService(DataSourceProcessor.class)
@Slf4j
public class SSHDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, SSHDataSourceParamDTO.class);
    }

    @Override
    public void checkDatasourceParam(BaseDataSourceParamDTO datasourceParamDTO) {
        if (StringUtils.isEmpty(datasourceParamDTO.getHost())
                || StringUtils.isEmpty(datasourceParamDTO.getUserName())) {
            throw new IllegalArgumentException("ssh datasource param is not valid");
        }
    }

    @Override
    public String getDatasourceUniqueId(ConnectionParam connectionParam, DbType dbType) {
        SSHConnectionParam baseConnectionParam = (SSHConnectionParam) connectionParam;
        return MessageFormat.format("{0}@{1}@{2}@{3}", dbType.getDescp(), baseConnectionParam.getHost(),
                baseConnectionParam.getUser(),
                PasswordUtils.encodePassword(baseConnectionParam.getPassword()));
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        SSHConnectionParam connectionParams = (SSHConnectionParam) createConnectionParams(connectionJson);
        SSHDataSourceParamDTO sshDataSourceParamDTO = new SSHDataSourceParamDTO();

        sshDataSourceParamDTO.setUserName(connectionParams.getUser());
        sshDataSourceParamDTO.setPassword(connectionParams.getPassword());
        sshDataSourceParamDTO.setHost(connectionParams.getHost());
        sshDataSourceParamDTO.setPort(connectionParams.getPort());
        sshDataSourceParamDTO.setPublicKey(connectionParams.getPublicKey());

        return sshDataSourceParamDTO;
    }

    @Override
    public SSHConnectionParam createConnectionParams(BaseDataSourceParamDTO dataSourceParam) {
        SSHDataSourceParamDTO sshDataSourceParam = (SSHDataSourceParamDTO) dataSourceParam;
        SSHConnectionParam sshConnectionParam = new SSHConnectionParam();
        sshConnectionParam.setUser(sshDataSourceParam.getUserName());
        sshConnectionParam.setPassword(sshDataSourceParam.getPassword());
        sshConnectionParam.setHost(sshDataSourceParam.getHost());
        sshConnectionParam.setPort(sshDataSourceParam.getPort());
        sshConnectionParam.setPublicKey(sshDataSourceParam.getPublicKey());

        return sshConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, SSHConnectionParam.class);
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
    public boolean checkDataSourceConnectivity(ConnectionParam connectionParam) {
        SSHConnectionParam baseConnectionParam = (SSHConnectionParam) connectionParam;
        try (
                SshClientWrapper sshClientWrapper = new SshClientWrapper(
                        baseConnectionParam.getHost(),
                        baseConnectionParam.getPort(),
                        baseConnectionParam.getUser(),
                        baseConnectionParam.getPassword(),
                        baseConnectionParam.getPublicKey())) {

            return sshClientWrapper.isAuth();
        } catch (Exception e) {
            log.error("ssh test connection failed", e);
            return false;

        }
    }

    @Override
    public DbType getDbType() {
        return DbType.SSH;
    }

    @Override
    public DataSourceProcessor create() {
        return new SSHDataSourceProcessor();
    }

}
