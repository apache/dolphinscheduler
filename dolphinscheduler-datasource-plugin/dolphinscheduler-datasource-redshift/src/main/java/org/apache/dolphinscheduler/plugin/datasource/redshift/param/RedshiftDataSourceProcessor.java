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

package org.apache.dolphinscheduler.plugin.datasource.redshift.param;

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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.auto.service.AutoService;

@AutoService(DataSourceProcessor.class)
public class RedshiftDataSourceProcessor extends AbstractDataSourceProcessor {

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, RedshiftDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        RedshiftConnectionParam connectionParams = (RedshiftConnectionParam) createConnectionParams(connectionJson);

        String[] hostSeperator = connectionParams.getAddress().split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);

        RedshiftDataSourceParamDTO redshiftDatasourceParamDTO = new RedshiftDataSourceParamDTO();
        redshiftDatasourceParamDTO.setMode(connectionParams.getMode());
        redshiftDatasourceParamDTO.setDbUser(connectionParams.getDbUser());
        if (connectionParams.getMode().equals(RedshiftAuthMode.PASSWORD)) {
            redshiftDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
            redshiftDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);
        } else {
            if (hostPortArray[0].contains(Constants.COLON)) {
                String portString = hostPortArray[0].split(Constants.COLON)[1];
                if (StringUtils.isNumeric(portString)) {
                    redshiftDatasourceParamDTO.setPort(Integer.parseInt(portString));
                    redshiftDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);
                } else {
                    redshiftDatasourceParamDTO.setHost(hostPortArray[0]);
                }
            } else {
                redshiftDatasourceParamDTO.setHost(hostPortArray[0]);
            }

        }
        redshiftDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        redshiftDatasourceParamDTO.setUserName(connectionParams.getUser());
        redshiftDatasourceParamDTO.setOther(connectionParams.getOther());

        return redshiftDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        RedshiftDataSourceParamDTO redshiftParam = (RedshiftDataSourceParamDTO) datasourceParam;
        String address = getAddress(redshiftParam);
        String jdbcUrl = address + Constants.SLASH + redshiftParam.getDatabase();

        RedshiftConnectionParam redshiftConnectionParam = new RedshiftConnectionParam();
        redshiftConnectionParam.setUser(redshiftParam.getUserName());
        redshiftConnectionParam.setPassword(PasswordUtils.encodePassword(redshiftParam.getPassword()));
        redshiftConnectionParam.setOther(redshiftParam.getOther());
        redshiftConnectionParam.setAddress(address);
        redshiftConnectionParam.setJdbcUrl(jdbcUrl);
        redshiftConnectionParam.setDatabase(redshiftParam.getDatabase());
        redshiftConnectionParam.setDriverClassName(getDatasourceDriver());
        redshiftConnectionParam.setValidationQuery(getValidationQuery());
        redshiftConnectionParam.setMode(redshiftParam.getMode());
        redshiftConnectionParam.setDbUser(redshiftParam.getDbUser());

        return redshiftConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, RedshiftConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_REDSHIFT_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.REDHIFT_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        RedshiftConnectionParam redshiftConnectionParam = (RedshiftConnectionParam) connectionParam;
        if (MapUtils.isNotEmpty(redshiftConnectionParam.getOther())) {
            return String.format("%s?%s", redshiftConnectionParam.getJdbcUrl(),
                    transformOther(redshiftConnectionParam.getOther()));
        }
        return redshiftConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        RedshiftConnectionParam redshiftConnectionParam = (RedshiftConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        if (redshiftConnectionParam.getMode().equals(RedshiftAuthMode.PASSWORD)) {
            return DriverManager.getConnection(getJdbcUrl(connectionParam),
                    redshiftConnectionParam.getUser(),
                    PasswordUtils.decodePassword(redshiftConnectionParam.getPassword()));
        } else if (redshiftConnectionParam.getMode().equals(RedshiftAuthMode.IAM_ACCESS_KEY)) {
            return getConnectionByIAM(redshiftConnectionParam);
        }
        return null;
    }

    @Override
    public DbType getDbType() {
        return DbType.REDSHIFT;
    }

    @Override
    public DataSourceProcessor create() {
        return new RedshiftDataSourceProcessor();
    }

    /**
     * 2 auth mode
     * PASSWORD: address example: jdbc:redshift://examplecluster.abc123xyz789.us-west-2.redshift.amazonaws.com:5439
     * IAM_ACCESS_KEY:
     * address example1: jdbc:redshift:iam://examplecluster:us-west-2
     * address example2: jdbc:redshift:iam://examplecluster.abc123xyz789.us-west-2.redshift.amazonaws.com:5439
     *
     * @param redshiftParam
     * @return
     */
    private String getAddress(RedshiftDataSourceParamDTO redshiftParam) {
        if (redshiftParam.getMode().equals(RedshiftAuthMode.PASSWORD)) {
            return String.format("%s%s:%s", DataSourceConstants.JDBC_REDSHIFT, redshiftParam.getHost(),
                    redshiftParam.getPort());
        } else if (redshiftParam.getMode().equals(RedshiftAuthMode.IAM_ACCESS_KEY)) {
            if (redshiftParam.getPort() == null) {
                // construct IAM_ACCESS_KEY example 1 format
                return String.format("%s%s", DataSourceConstants.JDBC_REDSHIFT_IAM, redshiftParam.getHost());
            } else {
                // construct IAM_ACCESS_KEY example 2 format
                return String.format("%s%s:%s", DataSourceConstants.JDBC_REDSHIFT_IAM, redshiftParam.getHost(),
                        redshiftParam.getPort());
            }
        }
        return null;
    }

    private static String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isNotEmpty(otherMap)) {
            List<String> list = new ArrayList<>(otherMap.size());
            otherMap.forEach((key, value) -> list.add(String.format("%s=%s", key, value)));
            return String.join(Constants.SEMICOLON, list);
        }
        return null;
    }

    /**
     * 2 auth mode
     * PASSWORD: address example: jdbc:redshift://examplecluster.abc123xyz789.us-west-2.redshift.amazonaws.com:5439/dev
     * IAM_ACCESS_KEY:
     * address example1: jdbc:redshift:iam://examplecluster:us-west-2/dev?AccessKeyID=xxx&SecretAccessKey=xxx&DbUser=kristen
     * address example2: jdbc:redshift:iam://examplecluster.abc123xyz789.us-west-2.redshift.amazonaws.com:5439/dev?AccessKeyID=xxx&SecretAccessKey=xxx&DbUser=kristen
     *
     * @param redshiftConnectionParam
     * @return
     */
    public static Connection getConnectionByIAM(RedshiftConnectionParam redshiftConnectionParam) {
        String basic;
        String authParams = String.format("AccessKeyID=%s&SecretAccessKey=%s&DbUser=%s",
                redshiftConnectionParam.getUser(), PasswordUtils.decodePassword(redshiftConnectionParam.getPassword()),
                redshiftConnectionParam.getDbUser());
        String connectionUrl;
        if (MapUtils.isNotEmpty(redshiftConnectionParam.getOther())) {
            basic = String.format("%s?%s", redshiftConnectionParam.getJdbcUrl(),
                    transformOther(redshiftConnectionParam.getOther()));
            // if have other params map, basic will be
            // 'jdbc:redshift:iam://examplecluster:us-west-2/dev?param1=xx&param2=xx'
            // append AccessKeyID &SecretAccessKey &DbUser
            connectionUrl = String.format("%s&%s", basic, authParams);
        } else {
            basic = redshiftConnectionParam.getJdbcUrl();
            // if none other params map, basic will be 'jdbc:redshift:iam://examplecluster:us-west-2/dev'
            // append AccessKeyID &SecretAccessKey &DbUser
            connectionUrl = String.format("%s?%s", basic, authParams);
        }
        try {
            Class.forName(DataSourceConstants.COM_REDSHIFT_JDBC_DRIVER);
            return DriverManager.getConnection(connectionUrl);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
