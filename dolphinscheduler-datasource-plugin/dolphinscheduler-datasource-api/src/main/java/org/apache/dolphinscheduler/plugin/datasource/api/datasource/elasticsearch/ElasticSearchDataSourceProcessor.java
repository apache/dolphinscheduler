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

package org.apache.dolphinscheduler.plugin.datasource.api.datasource.elasticsearch;

import org.apache.dolphinscheduler.plugin.datasource.api.datasource.AbstractDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;

public class ElasticSearchDataSourceProcessor implements DataSourceProcessor {

    private final Logger logger = LoggerFactory.getLogger(ElasticSearchDataSourceProcessor.class);

    @Override
    public void checkDatasourceParam(BaseDataSourceParamDTO datasourceParam) {

    }

    @Override
    public String getDatasourceUniqueId(ConnectionParam connectionParam, DbType dbType) {
        BaseConnectionParam baseConnectionParam = (BaseConnectionParam) connectionParam;
        return MessageFormat.format("{0}@{1}@{2}@{3}", dbType.getDescp(), baseConnectionParam.getUser(), PasswordUtils.encodePassword(baseConnectionParam.getPassword()), baseConnectionParam.getAddress());
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        ElasticSearchConnectionParam connectionParams = (ElasticSearchConnectionParam) createConnectionParams(connectionJson);
        ElasticSearchDataSourceParamDTO elasticSearchDataSourceParamDTO = new ElasticSearchDataSourceParamDTO();
        elasticSearchDataSourceParamDTO.setUserName(connectionParams.getUser());

        String address = connectionParams.getAddress();
        String[] hostSeperator = address.split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);
        elasticSearchDataSourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        elasticSearchDataSourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);

        return elasticSearchDataSourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO dataSourceParam) {
        ElasticSearchDataSourceParamDTO elasticSearchDataSourceParam = (ElasticSearchDataSourceParamDTO) dataSourceParam;
        String address = String.format("%s%s:%s", "http://", elasticSearchDataSourceParam.getHost(), elasticSearchDataSourceParam.getPort());

        ElasticSearchConnectionParam elasticSearchConnectionParam = new ElasticSearchConnectionParam();
        elasticSearchConnectionParam.setAddress(address);
        elasticSearchConnectionParam.setUser(elasticSearchDataSourceParam.getUserName());
        elasticSearchConnectionParam.setPassword(PasswordUtils.encodePassword(elasticSearchDataSourceParam.getPassword()));

        return elasticSearchConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, ElasticSearchConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return null;
    }

    @Override
    public String getValidationQuery() {
        return null;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        return null;
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        return null;
    }

    @Override
    public DbType getDbType() {
        return DbType.ELASTICSEARCH;
    }
}
