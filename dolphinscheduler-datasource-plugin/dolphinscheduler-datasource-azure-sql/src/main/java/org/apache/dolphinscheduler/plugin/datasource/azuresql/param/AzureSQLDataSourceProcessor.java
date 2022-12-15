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

package org.apache.dolphinscheduler.plugin.datasource.azuresql.param;

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
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.auto.service.AutoService;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;

@AutoService(DataSourceProcessor.class)
public class AzureSQLDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, AzureSQLDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        AzureSQLConnectionParam connectionParams = (AzureSQLConnectionParam) createConnectionParams(connectionJson);
        String[] hostSeperator = connectionParams.getAddress().split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);

        AzureSQLDataSourceParamDTO azureSQLDatasourceParamDTO = new AzureSQLDataSourceParamDTO();
        azureSQLDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        azureSQLDatasourceParamDTO.setUserName(connectionParams.getUser());
        azureSQLDatasourceParamDTO.setOther(parseOther(connectionParams.getOther()));
        azureSQLDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        azureSQLDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);
        azureSQLDatasourceParamDTO.setMode(connectionParams.getMode());
        switch (azureSQLDatasourceParamDTO.getMode()) {
            case AD_MSI:
                if (!StringUtils.isEmpty(connectionParams.getMSIClientId())) {
                    azureSQLDatasourceParamDTO.setMSIClientId(connectionParams.getMSIClientId());
                }
                break;
            case ACCESSTOKEN:
                azureSQLDatasourceParamDTO.setEndpoint(connectionParams.getEndpoint());
                break;
            default:
                break;
        }
        return azureSQLDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        AzureSQLDataSourceParamDTO azureSQLParam = (AzureSQLDataSourceParamDTO) datasourceParam;
        if (azureSQLParam.mode.equals(AzureSQLAuthMode.ACCESSTOKEN)) {
            return createTokenConnectionParams(azureSQLParam);
        }

        String address =
                String.format("%s%s:%s", DataSourceConstants.JDBC_SQLSERVER, azureSQLParam.getHost(),
                        azureSQLParam.getPort());
        String jdbcUrl = address + ";databaseName=" + azureSQLParam.getDatabase();
        AzureSQLConnectionParam azureSQLConnectionParam = new AzureSQLConnectionParam();
        azureSQLConnectionParam.setAddress(address);
        azureSQLConnectionParam.setDatabase(azureSQLParam.getDatabase());
        // deal with authentication mode
        azureSQLConnectionParam.setJdbcUrl(processAuthMode(jdbcUrl, azureSQLParam));
        azureSQLConnectionParam.setOther(transformOther(azureSQLParam.getOther()));
        azureSQLConnectionParam.setUser(azureSQLParam.getUserName());
        azureSQLConnectionParam.setPassword(PasswordUtils.encodePassword(azureSQLParam.getPassword()));
        azureSQLConnectionParam.setDriverClassName(getDatasourceDriver());
        azureSQLConnectionParam.setValidationQuery(getValidationQuery());
        azureSQLConnectionParam.setProps(azureSQLParam.getOther());

        return azureSQLConnectionParam;
    }

    @Override
    public BaseConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, AzureSQLConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_SQLSERVER_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.SQLSERVER_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        AzureSQLConnectionParam azureSQLConnectionParam = (AzureSQLConnectionParam) connectionParam;

        if (!StringUtils.isEmpty(azureSQLConnectionParam.getOther())) {
            return String.format("%s;%s", azureSQLConnectionParam.getJdbcUrl(), azureSQLConnectionParam.getOther());
        }
        return azureSQLConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        AzureSQLConnectionParam azureSQLConnectionParam = (AzureSQLConnectionParam) connectionParam;
        // token access way
        if (azureSQLConnectionParam.getMode().equals(AzureSQLAuthMode.ACCESSTOKEN)) {
            try {
                return tokenGetConnection(azureSQLConnectionParam);
            } catch (MalformedURLException | ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        // normal way
        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(connectionParam), azureSQLConnectionParam.getUser(),
                PasswordUtils.decodePassword(azureSQLConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.AZURESQL;
    }

    @Override
    public DataSourceProcessor create() {
        return new AzureSQLDataSourceProcessor();
    }

    private AzureSQLConnectionParam createTokenConnectionParams(AzureSQLDataSourceParamDTO azureSQLParam) {
        AzureSQLConnectionParam azureSQLConnectionParam = new AzureSQLConnectionParam();
        azureSQLConnectionParam.setAddress(azureSQLParam.getHost());
        azureSQLConnectionParam.setDatabase(azureSQLParam.getDatabase());
        azureSQLConnectionParam.setJdbcUrl(azureSQLParam.getHost());
        azureSQLConnectionParam.setOther(transformOther(azureSQLParam.getOther()));
        azureSQLConnectionParam.setUser(azureSQLParam.getUserName());
        azureSQLConnectionParam.setPassword(PasswordUtils.encodePassword(azureSQLParam.getPassword()));
        azureSQLConnectionParam.setDriverClassName(getDatasourceDriver());
        azureSQLConnectionParam.setValidationQuery(getValidationQuery());
        azureSQLConnectionParam.setProps(azureSQLParam.getOther());
        azureSQLConnectionParam.setEndpoint(azureSQLParam.getEndpoint());
        return azureSQLConnectionParam;
    }

    private Connection tokenGetConnection(AzureSQLConnectionParam param) throws MalformedURLException, ExecutionException, InterruptedException {
        String spn = DataSourceConstants.AZURE_SQL_DATABASE_SPN;
        String stsURL = param.getEndpoint(); // Replace with your STS URL.
        String clientId = param.getUser(); // Replace with your client ID.
        String clientSecret = PasswordUtils.decodePassword(param.getPassword()); // Replace with your client secret.

        String scope = spn + DataSourceConstants.AZURE_SQL_DATABASE_TOKEN_SCOPE;
        Set<String> scopes = new HashSet<>();
        scopes.add(scope);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        IClientCredential credential = ClientCredentialFactory.createFromSecret(clientSecret);
        ConfidentialClientApplication clientApplication = ConfidentialClientApplication
                .builder(clientId, credential).executorService(executorService).authority(stsURL).build();
        CompletableFuture<IAuthenticationResult> future = clientApplication
                .acquireToken(ClientCredentialParameters.builder(scopes).build());
        IAuthenticationResult authenticationResult = future.get();
        String accessToken = authenticationResult.accessToken();

        // Connect with the access token.
        SQLServerDataSource ds = new SQLServerDataSource();

        ds.setServerName(param.getAddress());
        ds.setDatabaseName(param.getDatabase());
        ds.setAccessToken(accessToken);

        try {
            return ds.getConnection();
        } catch (SQLServerException e) {
            throw new RuntimeException(e);
        }
    }

    private String processAuthMode(String jdbcUrl, AzureSQLDataSourceParamDTO param) {

        switch (param.getMode()) {
            case SQL_PASSWORD:
            case AD_PASSWORD:
            case AD_SERVICE_PRINCIPAL:
                return jdbcUrl + ";authentication=" + param.getMode().getDescp();
            case AD_MSI:
                if (StringUtils.isEmpty(param.getMSIClientId())) {
                    return jdbcUrl + ";authentication=" + param.getMode().getDescp();
                } else {
                    // write MSIClientId inside jdbc URL so no need MSIClientId in the AzureSQLConnectionParam
                    return jdbcUrl + ";authentication=" + param.getMode().getDescp()
                            + ";MSIClientId=" + param.getMSIClientId();
                }
            case ACCESSTOKEN:
            default:
                return jdbcUrl;
        }

    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        otherMap.forEach((key, value) -> stringBuilder.append(String.format("%s=%s;", key, value)));
        return stringBuilder.toString();
    }

    private Map<String, String> parseOther(String other) {
        if (StringUtils.isEmpty(other)) {
            return null;
        }
        Map<String, String> otherMap = new LinkedHashMap<>();
        for (String config : other.split(";")) {
            otherMap.put(config.split("=")[0], config.split("=")[1]);
        }
        return otherMap;
    }
}
