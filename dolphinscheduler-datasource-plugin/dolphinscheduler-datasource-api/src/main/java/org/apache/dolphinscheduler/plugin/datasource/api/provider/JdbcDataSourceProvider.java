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

package org.apache.dolphinscheduler.plugin.datasource.api.provider;

import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.DataSourceParam;
import org.apache.dolphinscheduler.spi.datasource.JdbcConnectionParam;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.Map;

import javax.sql.DataSource;

/**
 * provider create Jdbc Data Source
 */
public class JdbcDataSourceProvider {

    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String JDBC_URL = "jdbcUrl";
    public static final String DRIVER_CLASS_NAME = "driverClassName";
    public static final String PROPS = "props";
    public static final String KERBEROS_PRINCIPAL = "kerberosPrincipal";
    public static final String KERBEROS_KEYTAB = "kerberosKeytab";
    public static final String KERBEROS_KRB5CONF = "kerberosKrb5Conf";

    public static DataSourceFactory<? extends DataSource> getDataSourceFactory() {
        return new DataSourceFactoryBasicImpl();
    }

    /**
     * build JDBC connection parameters
     *
     * @param dataSourceParam datasourceParam
     */
    public static JdbcConnectionParam buildConnectionParams(DataSourceParam dataSourceParam) {
        JdbcConnectionParam jdbcConnectionParam = new JdbcConnectionParam();
        jdbcConnectionParam.setDbType(dataSourceParam.getDbType());
        jdbcConnectionParam.setJdbcUrl(dataSourceParam.getProps().get(JDBC_URL).toString());
        jdbcConnectionParam.setUser(dataSourceParam.getProps().get(USER).toString());
        jdbcConnectionParam.setPassword(PasswordUtils.encodePassword(dataSourceParam.getProps().get(PASSWORD).toString()));
        String driverClassName = dataSourceParam.getProps().getOrDefault(DRIVER_CLASS_NAME, "").toString();
        jdbcConnectionParam.setDriverClassName(StringUtils.isBlank(driverClassName) ? dataSourceParam.getDbType().getDefaultDriverClass() : driverClassName);

        if (CommonUtils.getKerberosStartupState()) {
            jdbcConnectionParam.setKerberosKeytab(dataSourceParam.getProps().get(KERBEROS_KEYTAB).toString());
            jdbcConnectionParam.setKerberosKrb5Conf(dataSourceParam.getProps().get(KERBEROS_KRB5CONF).toString());
            jdbcConnectionParam.setKerberosPrincipal(dataSourceParam.getProps().get(KERBEROS_PRINCIPAL).toString());
        }
        Map<String, String> props = (Map<String, String>) dataSourceParam.getProps().get(PROPS);
        if (props != null) {
            jdbcConnectionParam.setProps(props);
        }
        return jdbcConnectionParam;
    }

    public static JdbcConnectionParam buildConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, JdbcConnectionParam.class);
    }
}
