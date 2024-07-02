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

package org.apache.dolphinscheduler.plugin.datasource.aliyunadbspark.param;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.AbstractDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;

import lombok.extern.slf4j.Slf4j;

import com.google.auto.service.AutoService;

@AutoService(DataSourceProcessor.class)
@Slf4j
public class AliyunAdbSparkDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, AliyunAdbSparkDataSourceParamDTO.class);
    }

    @Override
    public void checkDatasourceParam(BaseDataSourceParamDTO baseDataSourceParamDTO) {
        AliyunAdbSparkDataSourceParamDTO aliyunAdbSparkDataSourceParamDTO =
                (AliyunAdbSparkDataSourceParamDTO) baseDataSourceParamDTO;

        if (StringUtils.isEmpty(aliyunAdbSparkDataSourceParamDTO.getAliyunAccessKeyId())) {
            throw new IllegalArgumentException("accessKeyId in param is not valid");
        }

        if (StringUtils.isEmpty(aliyunAdbSparkDataSourceParamDTO.getAliyunAccessKeySecret())) {
            throw new IllegalArgumentException("accessKeySecret in param is not valid");
        }

        if (StringUtils.isEmpty(aliyunAdbSparkDataSourceParamDTO.getAliyunRegionId())) {
            throw new IllegalArgumentException("regionId in param is not valid");
        }
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        AliyunAdbSparkConnectionParam connectionParams =
                (AliyunAdbSparkConnectionParam) createConnectionParams(connectionJson);
        AliyunAdbSparkDataSourceParamDTO aliyunAdbSparkDataSourceParamDTO =
                new AliyunAdbSparkDataSourceParamDTO();

        aliyunAdbSparkDataSourceParamDTO.setAliyunAccessKeyId(connectionParams.getAliyunAccessKeyId());
        aliyunAdbSparkDataSourceParamDTO.setAliyunAccessKeySecret(connectionParams.getAliyunAccessKeySecret());
        aliyunAdbSparkDataSourceParamDTO.setAliyunRegionId(connectionParams.getAliyunRegionId());
        return aliyunAdbSparkDataSourceParamDTO;
    }

    @Override
    public ConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        AliyunAdbSparkDataSourceParamDTO aliyunAdbSparkDataSourceParamDTO =
                (AliyunAdbSparkDataSourceParamDTO) datasourceParam;

        AliyunAdbSparkConnectionParam aliyunAdbSparkConnectionParam =
                new AliyunAdbSparkConnectionParam();

        aliyunAdbSparkConnectionParam.setAliyunAccessKeyId(aliyunAdbSparkDataSourceParamDTO.getAliyunAccessKeyId());
        aliyunAdbSparkConnectionParam
                .setAliyunAccessKeySecret(aliyunAdbSparkDataSourceParamDTO.getAliyunAccessKeySecret());
        aliyunAdbSparkConnectionParam.setAliyunRegionId(aliyunAdbSparkDataSourceParamDTO.getAliyunRegionId());

        return aliyunAdbSparkConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, AliyunAdbSparkConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return "";
    }

    @Override
    public java.lang.String getDatasourceUniqueId(ConnectionParam connectionParam, DbType dbType) {
        AliyunAdbSparkConnectionParam baseConnectionParam =
                (AliyunAdbSparkConnectionParam) connectionParam;

        return MessageFormat.format(
                "{0}@{1}@{2}@{3}",
                dbType.getName(),
                baseConnectionParam.getAliyunRegionId(),
                PasswordUtils.encodePassword(baseConnectionParam.getAliyunAccessKeyId()),
                PasswordUtils.encodePassword(baseConnectionParam.getAliyunAccessKeySecret()));
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
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException, IOException {
        return null;
    }

    @Override
    public DbType getDbType() {
        return DbType.ALIYUN_ADB_SPARK;
    }

    @Override
    public DataSourceProcessor create() {
        return new AliyunAdbSparkDataSourceProcessor();
    }
}
