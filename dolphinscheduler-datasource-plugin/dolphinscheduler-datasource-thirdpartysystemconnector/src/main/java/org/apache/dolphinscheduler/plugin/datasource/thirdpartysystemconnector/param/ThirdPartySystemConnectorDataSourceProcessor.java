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

import static org.apache.dolphinscheduler.common.constants.Constants.HTTP_CONNECT_TIMEOUT;

import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaderContentType;
import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaders;
import org.apache.dolphinscheduler.common.model.OkHttpResponse;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.AbstractDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import com.fasterxml.jackson.databind.JsonNode;
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

        thirdPartySystemConnectorDataSourceParamDTO.setSystemName(connectionParams.getSystemName());
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

        thirdPartySystemConnectorConnectionParam.setSystemName(
                thirdPartySystemConnectorDataSourceParamDTO.getSystemName());
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
            OkHttpResponse response = callSelectInterface(baseConnectionParam);
            if (response.getStatusCode() == 200) {
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("connect error, e:{}", e.getMessage());
            return false;
        }
    }

    private OkHttpResponse callSelectInterface(ThirdPartySystemConnectorConnectionParam baseConnectionParam) {
        try {
            InterfaceInfo selectConfig = baseConnectionParam.getSelectInterface();

            // 替换参数占位符
            String url = selectConfig.getUrl();

            OkHttpRequestHeaders headers = new OkHttpRequestHeaders();
            headers.setOkHttpRequestHeaderContentType(OkHttpRequestHeaderContentType.APPLICATION_JSON);

            Map<String, String> headerMap = new HashMap<>();
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> requestParams = new HashMap<>();

            // 获取认证token
            String token = authenticateAndGetToken(baseConnectionParam);

            headerMap.put("Authorization", token);

            // 处理参数
            if (selectConfig.getParameters() != null) {
                for (RequestParameter param : selectConfig.getParameters()) {
                    String value = param.getParamValue();

                    switch (param.getLocation()) {
                        case "HEADER":
                            headerMap.put(param.getParamName(), value);
                            break;
                        case "BODY":
                            if ("body".equals(param.getParamName())) {
                                requestBody = JSONUtils.parseObject(value, Map.class);
                            }
                            break;
                        case "PARAM":
                            requestParams.put(param.getParamName(), value);
                            break;
                    }
                }
            }

            if (!headerMap.isEmpty()) {
                headers.setHeaders(headerMap);
            }

            OkHttpResponse response;
            if ("POST".equals(selectConfig.getMethod())) {
                response = OkHttpUtils.post(url, headers, requestParams, requestBody,
                        HTTP_CONNECT_TIMEOUT, HTTP_CONNECT_TIMEOUT, HTTP_CONNECT_TIMEOUT);
            } else if ("PUT".equals(selectConfig.getMethod())) {
                response = OkHttpUtils.put(url, headers, requestBody,
                        HTTP_CONNECT_TIMEOUT, HTTP_CONNECT_TIMEOUT, HTTP_CONNECT_TIMEOUT);
            } else {
                response = OkHttpUtils.get(url, headers, requestParams,
                        HTTP_CONNECT_TIMEOUT, HTTP_CONNECT_TIMEOUT, HTTP_CONNECT_TIMEOUT);
            }
            return response;

        } catch (Exception e) {
            log.error("select task failed", e);
            throw new RuntimeException("select task failed", e);
        }
    }

    private String authenticateAndGetToken(ThirdPartySystemConnectorConnectionParam baseConnectionParam) throws Exception {
        AuthConfig authConfig = baseConnectionParam.getAuthConfig();
        if (authConfig == null) {
            throw new RuntimeException("AuthConfig is not provided");
        }

        switch (authConfig.getAuthType()) {
            case "BASIC":
                // 基础认证
                String auth = authConfig.getBasicUsername() + ":" + authConfig.getBasicPassword();
                String encoding = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
                return encoding;
            case "JWT":
                // JWT认证
                return authConfig.getJwtToken();
            case "OAUTH2":
                // OAuth2认证
                return getOAuth2Token(baseConnectionParam);
            default:
                throw new RuntimeException("Unsupported auth type: " + authConfig.getAuthType());
        }
    }

    private String getOAuth2Token(ThirdPartySystemConnectorConnectionParam baseConnectionParam) throws Exception {
        AuthConfig authConfig = baseConnectionParam.getAuthConfig();

        OkHttpRequestHeaders headers = new OkHttpRequestHeaders();
        headers.setHeaders(new HashMap<>());
        headers.setOkHttpRequestHeaderContentType(OkHttpRequestHeaderContentType.APPLICATION_FORM_URLENCODED);

        FormBody.Builder formBodyBuilder = new FormBody.Builder()
                .add("client_id", authConfig.getOauth2ClientId())
                .add("client_secret", authConfig.getOauth2ClientSecret())
                .add("username", authConfig.getOauth2Username())
                .add("password", authConfig.getOauth2Password())
                .add("grant_type", authConfig.getOauth2GrantType());

        // 添加 authMappings 中的参数
        if (authConfig.getAuthMappings() != null) {
            for (AuthMapping authMapping : authConfig.getAuthMappings()) {
                formBodyBuilder.add(authMapping.getKey(), authMapping.getValue());
            }
        }

        RequestBody formBody = formBodyBuilder.build();

        OkHttpResponse response = OkHttpUtils.postFormBody(
                baseConnectionParam.getServiceAddress() + authConfig.getOauth2TokenUrl(),
                headers,
                null,
                formBody,
                HTTP_CONNECT_TIMEOUT, HTTP_CONNECT_TIMEOUT, HTTP_CONNECT_TIMEOUT);

        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Authentication failed: " + response.getBody());
        }

        JsonNode authResult = JSONUtils.parseObject(response.getBody(), JsonNode.class);
        if (authResult.has("access_token")) {
            log.info("Authentication successful, token obtained");
            return authResult.get("access_token").asText();
        } else {
            throw new RuntimeException("Failed to get access token from response");
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
}
