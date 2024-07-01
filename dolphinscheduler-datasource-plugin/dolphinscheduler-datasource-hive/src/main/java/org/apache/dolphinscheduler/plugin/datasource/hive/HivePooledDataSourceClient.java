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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.JAVA_SECURITY_KRB5_CONF;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.JAVA_SECURITY_KRB5_CONF_PATH;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.client.BasePooledDataSourceClient;
import org.apache.dolphinscheduler.plugin.datasource.hive.security.UserGroupInformationFactory;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import sun.security.krb5.Config;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

import com.zaxxer.hikari.HikariDataSource;

@Slf4j
public class HivePooledDataSourceClient extends BasePooledDataSourceClient {

    public HivePooledDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        super(baseConnectionParam, dbType);
    }

    public HikariDataSource createDataSourcePool(BaseConnectionParam baseConnectionParam, DbType dbType) {
        checkKerberosEnv();
        UserGroupInformationFactory.login(baseConnectionParam.getUser());
        return super.createDataSourcePool(baseConnectionParam, dbType);
    }

    // used in constructor
    private void checkKerberosEnv() {
        String krb5File = PropertyUtils.getString(JAVA_SECURITY_KRB5_CONF_PATH);
        Boolean kerberosStartupState = PropertyUtils.getBoolean(HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false);
        if (kerberosStartupState && StringUtils.isNotBlank(krb5File)) {
            System.setProperty(JAVA_SECURITY_KRB5_CONF, krb5File);
            try {
                Config.refresh();
                Class<?> kerberosName = Class.forName("org.apache.hadoop.security.authentication.util.KerberosName");
                Field field = kerberosName.getDeclaredField("defaultRealm");
                field.setAccessible(true);
                field.set(null, Config.getInstance().getDefaultRealm());
            } catch (Exception e) {
                throw new RuntimeException("Update Kerberos environment failed.", e);
            }
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            synchronized (HikariDataSource.class) {
                UserGroupInformationFactory.logout(baseConnectionParam.getUser());
                UserGroupInformationFactory.login(baseConnectionParam.getUser());
                return dataSource.getConnection();
            }
        }
    }

    @Override
    public void close() {
        try {
            super.close();
        } finally {
            UserGroupInformationFactory.logout(baseConnectionParam.getUser());
        }
        log.info("Closed Hive datasource client.");

    }
}
