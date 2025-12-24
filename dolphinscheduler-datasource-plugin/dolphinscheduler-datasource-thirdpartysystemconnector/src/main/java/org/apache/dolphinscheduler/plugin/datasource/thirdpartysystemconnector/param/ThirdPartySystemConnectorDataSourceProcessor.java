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

package org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.AbstractDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.AuthenticationUtils;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.text.MessageFormat;

import lombok.extern.slf4j.Slf4j;

import com.google.auto.service.AutoService;

@AutoService(DataSourceProcessor.class)
@Slf4j
public class ThirdPartySystemConnectorDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, ThirdPartySystemConnectorDataSourceParamDTO.class);
    }

    @Override
    public void checkDatasourceParam(BaseDataSourceParamDTO datasourceParamDTO) {
        ThirdPartySystemConnectorDataSourceParamDTO thirdPartySystemConnectorParamDTO =
                (ThirdPartySystemConnectorDataSourceParamDTO) datasourceParamDTO;

        if (StringUtils.isEmpty(thirdPartySystemConnectorParamDTO.getServiceAddress())) {
            throw new IllegalArgumentException("third party system connector datasource param is not valid");
        }
        checkExternalSystemParam(thirdPartySystemConnectorParamDTO);
    }

    @Override
    public String getDatasourceUniqueId(ConnectionParam connectionParam, DbType dbType) {
        ThirdPartySystemConnectorConnectionParam baseConnectionParam =
                (ThirdPartySystemConnectorConnectionParam) connectionParam;
        return MessageFormat.format("{0}@{1}", dbType.getName(), baseConnectionParam.getServiceAddress());
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        ThirdPartySystemConnectorConnectionParam connectionParams =
                (ThirdPartySystemConnectorConnectionParam) createConnectionParams(connectionJson);

        ThirdPartySystemConnectorDataSourceParamDTO thirdPartySystemConnectorDataSourceParamDTO =
                new ThirdPartySystemConnectorDataSourceParamDTO();

        thirdPartySystemConnectorDataSourceParamDTO.setServiceAddress(connectionParams.getServiceAddress());
        thirdPartySystemConnectorDataSourceParamDTO.setAuthConfig(connectionParams.getAuthConfig());
        thirdPartySystemConnectorDataSourceParamDTO.setSelectInterface(connectionParams.getSelectInterface());
        thirdPartySystemConnectorDataSourceParamDTO.setSubmitInterface(connectionParams.getSubmitInterface());
        thirdPartySystemConnectorDataSourceParamDTO.setPollStatusInterface(connectionParams.getPollStatusInterface());
        thirdPartySystemConnectorDataSourceParamDTO.setStopInterface(connectionParams.getStopInterface());
        thirdPartySystemConnectorDataSourceParamDTO.setInterfaceTimeout(connectionParams.getInterfaceTimeout());

        return thirdPartySystemConnectorDataSourceParamDTO;
    }

    @Override
    public ThirdPartySystemConnectorConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        ThirdPartySystemConnectorDataSourceParamDTO thirdPartySystemConnectorDataSourceParamDTO =
                (ThirdPartySystemConnectorDataSourceParamDTO) datasourceParam;

        ThirdPartySystemConnectorConnectionParam thirdPartySystemConnectorConnectionParam =
                new ThirdPartySystemConnectorConnectionParam();

        thirdPartySystemConnectorConnectionParam.setServiceAddress(
                thirdPartySystemConnectorDataSourceParamDTO.getServiceAddress());
        thirdPartySystemConnectorConnectionParam.setAuthConfig(
                thirdPartySystemConnectorDataSourceParamDTO.getAuthConfig());
        thirdPartySystemConnectorConnectionParam.setSelectInterface(
                thirdPartySystemConnectorDataSourceParamDTO.getSelectInterface());
        thirdPartySystemConnectorConnectionParam.setSubmitInterface(
                thirdPartySystemConnectorDataSourceParamDTO.getSubmitInterface());
        thirdPartySystemConnectorConnectionParam.setPollStatusInterface(
                thirdPartySystemConnectorDataSourceParamDTO.getPollStatusInterface());
        thirdPartySystemConnectorConnectionParam.setStopInterface(
                thirdPartySystemConnectorDataSourceParamDTO.getStopInterface());
        thirdPartySystemConnectorConnectionParam.setInterfaceTimeout(
                thirdPartySystemConnectorDataSourceParamDTO.getInterfaceTimeout());

        return thirdPartySystemConnectorConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, ThirdPartySystemConnectorConnectionParam.class);
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
        ThirdPartySystemConnectorConnectionParam baseConnectionParam =
                (ThirdPartySystemConnectorConnectionParam) connectionParam;
        try {
            String token = AuthenticationUtils.authenticateAndGetToken(baseConnectionParam);
            return token != null;
        } catch (Exception e) {
            log.error("connect error, e:{}", e.getMessage());
            return false;
        }
    }

    @Override
    public DbType getDbType() {
        return DbType.THIRDPARTY_SYSTEM_CONNECTOR;
    }

    @Override
    public DataSourceProcessor create() {
        return new ThirdPartySystemConnectorDataSourceProcessor();
    }

    private void checkExternalSystemParam(ThirdPartySystemConnectorDataSourceParamDTO paramDTO) {

        // Check system name
        if (paramDTO.getName() == null || paramDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("system name cannot be empty");
        }
        String systemName = paramDTO.getName().trim();
        if (systemName.length() > 64) {
            throw new IllegalArgumentException("system name length cannot exceed 64 characters");
        }
        paramDTO.setName(systemName);

        // Check service address
        if (paramDTO.getServiceAddress() == null || paramDTO.getServiceAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("service address cannot be empty");
        }
        paramDTO.setServiceAddress(paramDTO.getServiceAddress().trim());

        // Check authentication configuration
        AuthConfig authConfig = paramDTO.getAuthConfig();
        if (authConfig == null) {
            throw new IllegalArgumentException("auth config cannot be empty");
        }
        if (authConfig.getAuthType() == null) {
            throw new IllegalArgumentException("auth type cannot be empty");
        }

        // 根据认证类型进行具体校验
        switch (authConfig.getAuthType()) {
            case BASIC_AUTH:
                if (authConfig.getBasicUsername() == null || authConfig.getBasicUsername().trim().isEmpty()) {
                    throw new IllegalArgumentException("basic auth username cannot be empty");
                }
                authConfig.setBasicUsername(authConfig.getBasicUsername().trim());
                if (authConfig.getBasicPassword() == null || authConfig.getBasicPassword().trim().isEmpty()) {
                    throw new IllegalArgumentException("basic auth password cannot be empty");
                }
                authConfig.setBasicPassword(authConfig.getBasicPassword().trim());
                break;
            case JWT:
                if (authConfig.getJwtToken() == null || authConfig.getJwtToken().trim().isEmpty()) {
                    throw new IllegalArgumentException("JWT token cannot be empty");
                }
                authConfig.setJwtToken(authConfig.getJwtToken().trim());
                break;
            case OAUTH2:
                if (authConfig.getOauth2TokenUrl() == null || authConfig.getOauth2TokenUrl().trim().isEmpty()) {
                    throw new IllegalArgumentException("OAuth2 token URL cannot be empty");
                }
                authConfig.setOauth2TokenUrl(authConfig.getOauth2TokenUrl().trim());
                if (authConfig.getOauth2ClientId() == null || authConfig.getOauth2ClientId().trim().isEmpty()) {
                    throw new IllegalArgumentException("OAuth2 client ID cannot be empty");
                }
                authConfig.setOauth2ClientId(authConfig.getOauth2ClientId().trim());
                if (authConfig.getOauth2ClientSecret() == null || authConfig.getOauth2ClientSecret().trim().isEmpty()) {
                    throw new IllegalArgumentException("OAuth2 client secret cannot be empty");
                }
                authConfig.setOauth2ClientSecret(authConfig.getOauth2ClientSecret().trim());
                if (authConfig.getOauth2GrantType() == null || authConfig.getOauth2GrantType().trim().isEmpty()) {
                    throw new IllegalArgumentException("OAuth2 grant type cannot be empty");
                }
                authConfig.setOauth2GrantType(authConfig.getOauth2GrantType().trim());
                if (authConfig.getOauth2GrantType().equals("password")) {
                    if (authConfig.getOauth2Username() == null || authConfig.getOauth2Username().trim().isEmpty()) {
                        throw new IllegalArgumentException("OAuth2 username cannot be empty");
                    }
                    authConfig.setOauth2Username(authConfig.getOauth2Username().trim());
                    if (authConfig.getOauth2Password() == null || authConfig.getOauth2Password().trim().isEmpty()) {
                        throw new IllegalArgumentException("OAuth2 password cannot be empty");
                    }
                    authConfig.setOauth2Password(authConfig.getOauth2Password().trim());
                }
                break;
            default:
                throw new IllegalArgumentException("unsupported auth type");
        }

        // Check interface configuration
        if (paramDTO.getSelectInterface() == null) {
            throw new IllegalArgumentException("select interface config cannot be empty");
        }
        if (paramDTO.getSubmitInterface() == null) {
            throw new IllegalArgumentException("submit interface config cannot be empty");
        }
        if (paramDTO.getPollStatusInterface() == null) {
            throw new IllegalArgumentException("poll status interface config cannot be empty");
        }
        if (paramDTO.getStopInterface() == null) {
            throw new IllegalArgumentException("stop interface config cannot be empty");
        }

        // Check interface configuration URL and method
        checkInterfaceConfig(paramDTO.getSelectInterface());
        checkInterfaceConfig(paramDTO.getSubmitInterface());
        checkInterfaceConfig(paramDTO.getPollStatusInterface());
        checkInterfaceConfig(paramDTO.getStopInterface());
    }

    private void checkInterfaceConfig(InterfaceInfo interfaceInfo) {
        if (interfaceInfo.getUrl() == null || interfaceInfo.getUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("interface URL cannot be empty");
        }
        interfaceInfo.setUrl(interfaceInfo.getUrl().trim());
        if (interfaceInfo.getMethod() == null) {
            throw new IllegalArgumentException("interface method cannot be empty");
        }
    }

}
