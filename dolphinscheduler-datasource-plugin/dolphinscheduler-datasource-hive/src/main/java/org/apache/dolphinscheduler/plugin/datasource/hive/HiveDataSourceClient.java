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

package org.apache.dolphinscheduler.plugin.datasource.hive;

import static org.apache.dolphinscheduler.spi.task.TaskConstants.JAVA_SECURITY_KRB5_CONF;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.JAVA_SECURITY_KRB5_CONF_PATH;

import org.apache.dolphinscheduler.plugin.datasource.api.client.CommonDataSourceClient;
import org.apache.dolphinscheduler.plugin.datasource.api.provider.JdbcDataSourceProvider;
import org.apache.dolphinscheduler.plugin.datasource.utils.CommonUtil;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariDataSource;

public class HiveDataSourceClient extends CommonDataSourceClient {

    private static final Logger logger = LoggerFactory.getLogger(HiveDataSourceClient.class);

    protected HikariDataSource oneSessionDataSource;
    private UserGroupInformation ugi;

    public HiveDataSourceClient(BaseConnectionParam baseConnectionParam) {
        super(baseConnectionParam);
    }

    @Override
    protected void initClient(BaseConnectionParam baseConnectionParam) {
        logger.info("Create UserGroupInformation.");
        this.ugi = createUserGroupInformation(baseConnectionParam.getUser());
        logger.info("Create ugi success.");

        super.initClient(baseConnectionParam);
        this.oneSessionDataSource = JdbcDataSourceProvider.createOneSessionJdbcDataSource(baseConnectionParam);
        logger.info("Init {} success.", getClass().getName());
    }

    @Override
    protected void checkEnv(BaseConnectionParam baseConnectionParam) {
        super.checkEnv(baseConnectionParam);
        checkKerberosEnv();
    }

    private void checkKerberosEnv() {
        String krb5File = PropertyUtils.getString(JAVA_SECURITY_KRB5_CONF_PATH);
        if (StringUtils.isNotBlank(krb5File)) {
            System.setProperty(JAVA_SECURITY_KRB5_CONF, krb5File);
        }
    }

    private UserGroupInformation createUserGroupInformation(String username) {
        String krb5File = PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH);
        String keytab = PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH);
        String principal = PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME);
        try {
            return CommonUtil.createUGI(getHadoopConf(), principal, keytab, krb5File, username);
        } catch (IOException e) {
            throw new RuntimeException("createUserGroupInformation fail. ", e);
        }
    }

    protected Configuration getHadoopConf() {
        return new Configuration();
    }

    @Override
    public Connection getConnection() {
        try {
            return oneSessionDataSource.getConnection();
        } catch (SQLException e) {
            logger.error("get oneSessionDataSource Connection fail SQLException: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void close() {
        super.close();
        logger.info("close HiveDataSourceClient.");

        this.oneSessionDataSource.close();
        this.oneSessionDataSource = null;
    }
}
