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

package org.apache.dolphinscheduler.plugin.datasource.k8s.param;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.datasource.k8s.K8sClientWrapper;
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
public class K8sDataSourceProcessor implements DataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, K8sDataSourceParamDTO.class);

    }

    @Override
    public void checkDatasourceParam(BaseDataSourceParamDTO datasourceParam) {
        K8sDataSourceParamDTO k8sDataSourceParamDTO = (K8sDataSourceParamDTO) datasourceParam;
        if (StringUtils.isEmpty(k8sDataSourceParamDTO.getKubeConfig())) {
            throw new IllegalArgumentException("sagemaker datasource param is not valid");
        }
    }

    @Override
    public String getDatasourceUniqueId(ConnectionParam connectionParam, DbType dbType) {
        K8sConnectionParam baseConnectionParam = (K8sConnectionParam) connectionParam;
        return MessageFormat.format("{0}@{1}@{2}", dbType.getDescp(),
                PasswordUtils.encodePassword(baseConnectionParam.getKubeConfig()), baseConnectionParam.getNamespace());
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        K8sConnectionParam connectionParams = (K8sConnectionParam) createConnectionParams(connectionJson);
        K8sDataSourceParamDTO k8sDataSourceParamDTO = new K8sDataSourceParamDTO();
        k8sDataSourceParamDTO.setKubeConfig(connectionParams.getKubeConfig());
        k8sDataSourceParamDTO.setNamespace(connectionParams.getNamespace());
        return k8sDataSourceParamDTO;
    }

    @Override
    public K8sConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        K8sDataSourceParamDTO k8sDataSourceParam = (K8sDataSourceParamDTO) datasourceParam;
        K8sConnectionParam k8sConnectionParam = new K8sConnectionParam();
        k8sConnectionParam.setKubeConfig(k8sDataSourceParam.getKubeConfig());
        k8sConnectionParam.setNamespace(k8sDataSourceParam.getNamespace());
        return k8sConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, K8sConnectionParam.class);
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
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException, IOException {
        return null;
    }

    @Override
    public boolean checkDataSourceConnectivity(ConnectionParam connectionParam) {
        K8sConnectionParam baseConnectionParam = (K8sConnectionParam) connectionParam;
        try (
                K8sClientWrapper k8sClientWrapper = new K8sClientWrapper()) {
            return k8sClientWrapper.checkConnect(baseConnectionParam.kubeConfig, baseConnectionParam.namespace);
        } catch (Exception e) {
            log.error("failed to connect to the K8S cluster", e);
            return false;
        }
    }

    @Override
    public DbType getDbType() {
        return DbType.K8S;
    }

    @Override
    public DataSourceProcessor create() {
        return new K8sDataSourceProcessor();
    }
}
