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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ExternalSystemService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaderContentType;
import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaders;
import org.apache.dolphinscheduler.common.model.OkHttpResponse;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.ExternalSystemTaskQuery;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.AuthenticationUtils;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.AuthConfig;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.InterfaceInfo;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.RequestParameter;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.ResponseParameter;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.ThirdPartySystemConnectorConnectionParam;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.externalSystem.ExternalTaskConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.JsonPath;

@Service
@Slf4j
public class ExternalSystemServiceImpl extends BaseServiceImpl implements ExternalSystemService {

    @Autowired
    private DataSourceMapper dataSourceMapper;

    private static final String EXTERNAL_TASK_ID = "id";
    private static final String EXTERNAL_TASK_NAME = "name";

    private OkHttpResponse callSelectInterface(ThirdPartySystemConnectorConnectionParam baseExternalSystemParam,
                                               boolean dbPassword) {
        if (baseExternalSystemParam == null || baseExternalSystemParam.getSelectInterface() == null) {
            throw new IllegalArgumentException(
                    "ThirdPartySystemConnectorConnectionParam or SelectInterface cannot be null");
        }

        InterfaceInfo selectConfig = baseExternalSystemParam.getSelectInterface();

        // Replace parameter placeholders
        String url = baseExternalSystemParam.getCompleteUrl(selectConfig.getUrl());

        Map<String, String> headeMap = new HashMap<>();
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> requestParams = new HashMap<>();
        String token;

        try {
            if (dbPassword) {
                // Saved information, retrieve from database and decrypt
                AuthConfig authConfig = baseExternalSystemParam.getAuthConfig();
                decodePassword(authConfig);
                baseExternalSystemParam.setAuthConfig(authConfig);
                token = AuthenticationUtils.authenticateAndGetToken(baseExternalSystemParam);
            } else {
                if (baseExternalSystemParam.getId() != null) {
                    DataSource existingSystem = dataSourceMapper.selectById(baseExternalSystemParam.getId());
                    if (existingSystem == null) {
                        // New information test connection
                        token = AuthenticationUtils.authenticateAndGetToken(baseExternalSystemParam);
                    } else {
                        // Update information test connection, if password is not modified, use password saved in
                        // database for test connection
                        ThirdPartySystemConnectorConnectionParam oldParams =
                                JSONUtils.parseObject(existingSystem.getConnectionParams(),
                                        ThirdPartySystemConnectorConnectionParam.class);
                        AuthConfig authConfig = baseExternalSystemParam.getAuthConfig();
                        if (authConfig.getBasicPassword() != null
                                && authConfig.getBasicPassword().equals(Constants.XXXXXX)) {
                            authConfig.setBasicPassword(oldParams.getAuthConfig().getBasicPassword());
                        }
                        if (authConfig.getOauth2ClientSecret() != null
                                && authConfig.getOauth2ClientSecret().equals(Constants.XXXXXX)) {
                            authConfig.setOauth2ClientSecret(oldParams.getAuthConfig().getOauth2ClientSecret());
                        }
                        if (authConfig.getOauth2Password() != null
                                && authConfig.getOauth2Password().equals(Constants.XXXXXX)) {
                            authConfig.setOauth2Password(oldParams.getAuthConfig().getOauth2Password());
                        }
                        if (authConfig.getJwtToken() != null && authConfig.getJwtToken().equals(Constants.XXXXXX)) {
                            authConfig.setJwtToken(oldParams.getAuthConfig().getJwtToken());
                        }
                        decodePassword(authConfig);
                        baseExternalSystemParam.setAuthConfig(authConfig);
                        token = AuthenticationUtils.authenticateAndGetToken(baseExternalSystemParam);
                    }
                } else {
                    token = AuthenticationUtils.authenticateAndGetToken(baseExternalSystemParam);
                }
            }
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            throw new ServiceException(Status.EXTERNAL_SYSTEM_CONNECT_AUTH_FAILED);
        }

        try {
            headeMap.put("Authorization",
                    baseExternalSystemParam.getTokenPrefix(baseExternalSystemParam.getAuthConfig().getHeaderPrefix())
                            + token);

            // Process parameters
            for (RequestParameter param : selectConfig.getParameters()) {
                // todo String value = replaceParameterPlaceholders(param.getParamValue());
                String value = param.getParamValue();

                switch (param.getLocation().name()) {
                    case "HEADER":
                        headeMap.put(param.getParamName(), value);
                        break;
                    case "PARAM":
                        requestParams.put(param.getParamName(), value);
                        break;
                }
            }
            if (selectConfig.getBody() != null) {
                requestBody = JSONUtils.parseObject((selectConfig.getBody()), Map.class);
            }
            OkHttpRequestHeaders headers = new OkHttpRequestHeaders();
            OkHttpRequestHeaderContentType contentType = getContentType(headeMap);

            headers.setOkHttpRequestHeaderContentType(contentType);
            if (!headeMap.isEmpty()) {
                headers.setHeaders(headeMap);
            }
            OkHttpResponse response;
            if (InterfaceInfo.HttpMethod.POST.equals(selectConfig.getMethod())) {
                if (contentType.equals(OkHttpRequestHeaderContentType.APPLICATION_JSON)) {
                    response = OkHttpUtils.post(url, headers, requestParams, requestBody, 120000, 120000, 120000);

                } else if (contentType.equals(OkHttpRequestHeaderContentType.APPLICATION_FORM_URLENCODED)) {
                    FormBody.Builder formBodyBuilder = new FormBody.Builder();
                    if (requestBody != null) {
                        for (Map.Entry<String, Object> entry : requestBody.entrySet()) {
                            formBodyBuilder.add(entry.getKey(), entry.getValue().toString());
                        }
                    }
                    response = OkHttpUtils.postFormBody(url, headers, requestParams, formBodyBuilder.build(), 120000,
                            120000, 120000);
                } else {
                    log.error("select task failed, OkHttpRequestHeaderContentType not support: {},", contentType);
                    throw new ServiceException(Status.EXTERNAL_SYSTEM_CONNECT_AUTH_FAILED);
                }
            } else if (InterfaceInfo.HttpMethod.PUT.equals(selectConfig.getMethod())) {
                response = OkHttpUtils.put(url, headers, requestBody, 120000, 120000, 120000);
            } else {
                response = OkHttpUtils.get(url, headers, requestParams, 120000, 120000, 120000);
            }
            return response;

        } catch (Exception e) {
            log.error("select task failed, id: {}, serviceAddress: {}, dbPassword: {}",
                    baseExternalSystemParam.getId(),
                    baseExternalSystemParam.getServiceAddress(),
                    dbPassword, e);
            throw new ServiceException(Status.EXTERNAL_SYSTEM_CONNECT_AUTH_FAILED);
        }
    }

    private OkHttpRequestHeaderContentType getContentType(Map<String, String> headers) {
        String contentType = headers.get("Content-Type");
        if (contentType != null) {
            if (contentType.contains("application/json")) {
                return OkHttpRequestHeaderContentType.APPLICATION_JSON;
            } else if (contentType.contains("application/x-www-form-urlencoded")) {
                return OkHttpRequestHeaderContentType.APPLICATION_FORM_URLENCODED;
            }
        }
        return OkHttpRequestHeaderContentType.APPLICATION_JSON; // 默认值
    }

    private void decodePassword(AuthConfig authConfig) {
        if (null != authConfig.getOauth2ClientSecret() && !authConfig.getOauth2ClientSecret().isEmpty()) {
            authConfig.setOauth2ClientSecret(PasswordUtils.decodePassword(authConfig.getOauth2ClientSecret()));
        }
        if (null != authConfig.getOauth2Password() && !authConfig.getOauth2Password().isEmpty()) {
            authConfig.setOauth2Password(PasswordUtils.decodePassword(authConfig.getOauth2Password()));
        }
        if (null != authConfig.getJwtToken() && !authConfig.getJwtToken().isEmpty()) {
            authConfig.setJwtToken(PasswordUtils.decodePassword(authConfig.getJwtToken()));
        }
        if (null != authConfig.getBasicPassword() && !authConfig.getBasicPassword().isEmpty()) {
            authConfig.setBasicPassword(PasswordUtils.decodePassword(authConfig.getBasicPassword()));
        }
    }

    /**
     * get hidden password (resolve the security hotspot)
     *
     * @return hidden password
     */
    private String getHiddenPassword() {
        return Constants.XXXXXX;
    }

    @Override
    public List<ExternalSystemTaskQuery> queryExternalSystemTasks(User loginUser, int externalSystemId) {

        DataSource dataSource = dataSourceMapper.selectById(externalSystemId);
        ThirdPartySystemConnectorConnectionParam baseExternalSystemParam =
                JSONUtils.parseObject(dataSource.getConnectionParams(), ThirdPartySystemConnectorConnectionParam.class);

        // Validate query parameters
        String taskIdExpression = "";
        String taskNameExpression = "";
        for (ResponseParameter param : baseExternalSystemParam.getSelectInterface()
                .getResponseParameters()) {
            if (EXTERNAL_TASK_ID.equals(param.getKey())) {
                taskIdExpression = param.getJsonPath();
            }
            if (EXTERNAL_TASK_NAME.equals(param.getKey())) {
                taskNameExpression = param.getJsonPath();
            }
        }
        if (taskIdExpression.isEmpty() || taskNameExpression.isEmpty()) {
            throw new IllegalStateException("External field mapping for 'id' and 'name' not found");
        }

        OkHttpResponse selectResponse = callSelectInterface(baseExternalSystemParam, true);
        if (selectResponse.getStatusCode() != ExternalTaskConstants.RESPONSE_CODE_SUCCESS) {
            throw new TaskException("Select task failed: " + selectResponse.getBody());
        }
        // 解析响应获取id name
        return parseSelectResponse(selectResponse.getBody(), taskIdExpression, taskNameExpression);

    }

    private List<ExternalSystemTaskQuery> parseSelectResponse(String responseBody, String taskIdExpression,
                                                              String taskNameExpression) throws TaskException {
        List<ExternalSystemTaskQuery> resultList = new ArrayList<>();

        try {

            List<String> idValues = JsonPath.read(responseBody, taskIdExpression);
            List<String> nameValues = JsonPath.read(responseBody, taskNameExpression);

            if (idValues.size() != nameValues.size()) {
                throw new TaskException("ID and name lists have different sizes");
            }

            // Create tasks
            for (int i = 0; i < idValues.size(); i++) {
                ExternalSystemTaskQuery task = new ExternalSystemTaskQuery();
                task.setId(idValues.get(i));
                task.setName(nameValues.get(i));
                resultList.add(task);
            }

        } catch (Exception e) {
            log.error("Parse select response failed", e);
            throw new TaskException("Parse select response failed", e);
        }
        return resultList;
    }

}
