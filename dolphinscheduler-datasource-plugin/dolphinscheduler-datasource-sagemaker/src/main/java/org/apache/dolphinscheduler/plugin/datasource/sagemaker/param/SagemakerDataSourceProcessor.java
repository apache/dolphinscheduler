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

package org.apache.dolphinscheduler.plugin.datasource.sagemaker.param;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.datasource.sagemaker.SagemakerClientWrapper;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.text.MessageFormat;

import lombok.extern.slf4j.Slf4j;

import com.google.auto.service.AutoService;

@AutoService(DataSourceProcessor.class)
@Slf4j
public class SagemakerDataSourceProcessor implements DataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, SagemakerDataSourceParamDTO.class);
    }

    @Override
    public void checkDatasourceParam(BaseDataSourceParamDTO datasourceParamDTO) {
        SagemakerDataSourceParamDTO sageMakerDataSourceParamDTO = (SagemakerDataSourceParamDTO) datasourceParamDTO;
        if (StringUtils.isEmpty(sageMakerDataSourceParamDTO.getUserName())
                || StringUtils.isEmpty(sageMakerDataSourceParamDTO.getPassword())
                || StringUtils.isEmpty(sageMakerDataSourceParamDTO.getAwsRegion())) {
            throw new IllegalArgumentException("sagemaker datasource param is not valid");
        }
    }

    @Override
    public String getDatasourceUniqueId(ConnectionParam connectionParam, DbType dbType) {
        SagemakerConnectionParam baseConnectionParam = (SagemakerConnectionParam) connectionParam;
        return MessageFormat.format("{0}@{1}@{2}@{3}", dbType.getDescp(),
                PasswordUtils.encodePassword(baseConnectionParam.getUserName()),
                PasswordUtils.encodePassword(baseConnectionParam.getPassword()),
                PasswordUtils.encodePassword(baseConnectionParam.getAwsRegion()));
    }

    // SageMaker
    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        SagemakerConnectionParam connectionParams = (SagemakerConnectionParam) createConnectionParams(connectionJson);
        SagemakerDataSourceParamDTO sagemakerDataSourceParamDTO = new SagemakerDataSourceParamDTO();

        sagemakerDataSourceParamDTO.setUserName(connectionParams.getUserName());
        sagemakerDataSourceParamDTO.setPassword(connectionParams.getPassword());
        sagemakerDataSourceParamDTO.setAwsRegion(connectionParams.getAwsRegion());
        return sagemakerDataSourceParamDTO;
    }

    @Override
    public SagemakerConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        SagemakerDataSourceParamDTO sageMakerDataSourceParam = (SagemakerDataSourceParamDTO) datasourceParam;
        SagemakerConnectionParam sageMakerConnectionParam = new SagemakerConnectionParam();
        sageMakerConnectionParam.setUserName(sageMakerDataSourceParam.getUserName());
        sageMakerConnectionParam.setPassword(sageMakerDataSourceParam.getPassword());
        sageMakerConnectionParam.setAwsRegion(sageMakerDataSourceParam.getAwsRegion());

        return sageMakerConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, SagemakerConnectionParam.class);
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
        SagemakerConnectionParam baseConnectionParam = (SagemakerConnectionParam) connectionParam;
        try (
                SagemakerClientWrapper sagemakerClientWrapper =
                        new SagemakerClientWrapper(baseConnectionParam.userName,
                                baseConnectionParam.password, baseConnectionParam.awsRegion)) {
            return sagemakerClientWrapper.checkConnect();
        } catch (Exception e) {
            log.error("sagemaker client failed to connect to the server", e);
            return false;
        }
    }

    @Override
    public DbType getDbType() {
        return DbType.SAGEMAKER;
    }

    @Override
    public DataSourceProcessor create() {
        return new SagemakerDataSourceProcessor();
    }
}
