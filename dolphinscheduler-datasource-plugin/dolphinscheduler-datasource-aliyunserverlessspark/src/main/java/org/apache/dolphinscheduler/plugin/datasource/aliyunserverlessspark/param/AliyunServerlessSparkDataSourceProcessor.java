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

package org.apache.dolphinscheduler.plugin.datasource.aliyunserverlessspark.param;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.aliyunserverlessspark.AliyunServerlessSparkClientWrapper;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.AbstractDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.text.MessageFormat;

import lombok.extern.slf4j.Slf4j;

import com.google.auto.service.AutoService;

@AutoService(DataSourceProcessor.class)
@Slf4j
public class AliyunServerlessSparkDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, AliyunServerlessSparkDataSourceParamDTO.class);
    }

    @Override
    public void checkDatasourceParam(BaseDataSourceParamDTO datasourceParamDTO) {
        AliyunServerlessSparkDataSourceParamDTO aliyunServerlessSparkDataSourceParamDTO =
                (AliyunServerlessSparkDataSourceParamDTO) datasourceParamDTO;
        if (StringUtils.isEmpty(aliyunServerlessSparkDataSourceParamDTO.getRegionId()) ||
                StringUtils.isEmpty(aliyunServerlessSparkDataSourceParamDTO.getAccessKeyId())) {
            throw new IllegalArgumentException("spark datasource param is not valid");
        }
    }

    @Override
    public String getDatasourceUniqueId(ConnectionParam connectionParam, DbType dbType) {
        AliyunServerlessSparkConnectionParam baseConnectionParam =
                (AliyunServerlessSparkConnectionParam) connectionParam;
        return MessageFormat.format(
                "{0}@{1}@{2}@{3}",
                dbType.getName(),
                baseConnectionParam.getRegionId(),
                PasswordUtils.encodePassword(baseConnectionParam.getAccessKeyId()),
                PasswordUtils.encodePassword(baseConnectionParam.getAccessKeySecret()));
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        AliyunServerlessSparkConnectionParam connectionParams =
                (AliyunServerlessSparkConnectionParam) createConnectionParams(connectionJson);
        AliyunServerlessSparkDataSourceParamDTO aliyunServerlessSparkDataSourceParamDTO =
                new AliyunServerlessSparkDataSourceParamDTO();

        aliyunServerlessSparkDataSourceParamDTO.setAccessKeyId(connectionParams.getAccessKeyId());
        aliyunServerlessSparkDataSourceParamDTO.setAccessKeySecret(connectionParams.getAccessKeySecret());
        aliyunServerlessSparkDataSourceParamDTO.setRegionId(connectionParams.getRegionId());
        aliyunServerlessSparkDataSourceParamDTO.setEndpoint(connectionParams.getEndpoint());
        return aliyunServerlessSparkDataSourceParamDTO;
    }

    @Override
    public AliyunServerlessSparkConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        AliyunServerlessSparkDataSourceParamDTO aliyunServerlessSparkDataSourceParamDTO =
                (AliyunServerlessSparkDataSourceParamDTO) datasourceParam;
        AliyunServerlessSparkConnectionParam aliyunServerlessSparkConnectionParam =
                new AliyunServerlessSparkConnectionParam();
        aliyunServerlessSparkConnectionParam.setAccessKeyId(aliyunServerlessSparkDataSourceParamDTO.getAccessKeyId());
        aliyunServerlessSparkConnectionParam
                .setAccessKeySecret(aliyunServerlessSparkDataSourceParamDTO.getAccessKeySecret());
        aliyunServerlessSparkConnectionParam.setRegionId(aliyunServerlessSparkDataSourceParamDTO.getRegionId());
        aliyunServerlessSparkConnectionParam.setEndpoint(aliyunServerlessSparkDataSourceParamDTO.getEndpoint());

        return aliyunServerlessSparkConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, AliyunServerlessSparkConnectionParam.class);
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
        AliyunServerlessSparkConnectionParam baseConnectionParam =
                (AliyunServerlessSparkConnectionParam) connectionParam;
        try (
                AliyunServerlessSparkClientWrapper aliyunServerlessSparkClientWrapper =
                        new AliyunServerlessSparkClientWrapper(
                                baseConnectionParam.getAccessKeyId(),
                                baseConnectionParam.getAccessKeySecret(),
                                baseConnectionParam.getRegionId(),
                                baseConnectionParam.getEndpoint())) {
            return aliyunServerlessSparkClientWrapper.checkConnect(
                    baseConnectionParam.getAccessKeyId(),
                    baseConnectionParam.getAccessKeySecret(),
                    baseConnectionParam.getRegionId());
        } catch (Exception e) {
            log.error("spark client failed to connect to the server", e);
            return false;
        }
    }

    @Override
    public DbType getDbType() {
        return DbType.ALIYUN_SERVERLESS_SPARK;
    }

    @Override
    public DataSourceProcessor create() {
        return new AliyunServerlessSparkDataSourceProcessor();
    }
}
