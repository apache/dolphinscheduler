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

package org.apache.dolphinscheduler.dao.datasource;

import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * data source base class
 */
public abstract class BaseDataSource {

    private static final Logger logger = LoggerFactory.getLogger(BaseDataSource.class);

    /**
     * user name
     */
    protected String user;

    /**
     * user password
     */
    protected String password;

    /**
     * data source address
     */
    private String address;

    /**
     * database name
     */
    private String database;

    /**
     * other connection parameters for the data source
     */
    private String other;

    /**
     * principal
     */
    private String principal;

    /**
     * java.security.krb5.conf
     */
    private String javaSecurityKrb5Conf;

    /**
     * login.user.keytab.username
     */
    private String loginUserKeytabUsername;

    /**
     * login.user.keytab.path
     */
    private String loginUserKeytabPath;

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    /**
     * @return driver class
     */
    public abstract String driverClassSelector();

    /**
     * @return db type
     */
    public abstract DbType dbTypeSelector();

    /**
     * gets the JDBC url for the data source connection
     * @return getJdbcUrl
     */
    public String getJdbcUrl() {
        StringBuilder jdbcUrl = new StringBuilder(getAddress());

        appendDatabase(jdbcUrl);
        appendPrincipal(jdbcUrl);
        appendOther(jdbcUrl);

        return jdbcUrl.toString();
    }

    /**
     * append database
     * @param jdbcUrl jdbc url
     */
    protected void appendDatabase(StringBuilder jdbcUrl) {
        if (dbTypeSelector() == DbType.SQLSERVER) {
            jdbcUrl.append(";databaseName=").append(getDatabase());
        } else {
            if (getAddress().lastIndexOf('/') != (jdbcUrl.length() - 1)) {
                jdbcUrl.append("/");
            }
            jdbcUrl.append(getDatabase());
        }
    }

    /**
     * append principal
     * @param jdbcUrl jdbc url
     */
    private void appendPrincipal(StringBuilder jdbcUrl) {
        boolean tag = dbTypeSelector() == DbType.HIVE || dbTypeSelector() == DbType.SPARK;
        if (tag && StringUtils.isNotEmpty(getPrincipal())) {
            jdbcUrl.append(";principal=").append(getPrincipal());
        }
    }

    /**
     * append other
     * @param jdbcUrl jdbc url
     */
    private void appendOther(StringBuilder jdbcUrl) {
        String otherParams = filterOther(getOther());
        if (StringUtils.isNotEmpty(otherParams)) {
            String separator = "";
            switch (dbTypeSelector()) {
                case CLICKHOUSE:
                case MYSQL:
                case ORACLE:
                case POSTGRESQL:
                case PRESTO:
                    separator = "?";
                    break;
                case DB2:
                    separator = ":";
                    break;
                case HIVE:
                case SPARK:
                case SQLSERVER:
                    separator = ";";
                    break;
                default:
                    logger.error("Db type mismatch!");
            }
            jdbcUrl.append(separator).append(otherParams);
        }
    }

    /**
     * the data source test connection
     * @return Connection Connection
     * @throws Exception Exception
     */
    public Connection getConnection() throws Exception {
        Class.forName(driverClassSelector());
        return DriverManager.getConnection(getJdbcUrl(), getUser(), getPassword());
    }

    protected String filterOther(String otherParams) {
        return otherParams;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    /**
     * password need decode
     * @return
     */
    public String getPassword() {
        return CommonUtils.decodePassword(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String getJavaSecurityKrb5Conf() {
        return javaSecurityKrb5Conf;
    }

    public void setJavaSecurityKrb5Conf(String javaSecurityKrb5Conf) {
        this.javaSecurityKrb5Conf = javaSecurityKrb5Conf;
    }

    public String getLoginUserKeytabUsername() {
        return loginUserKeytabUsername;
    }

    public void setLoginUserKeytabUsername(String loginUserKeytabUsername) {
        this.loginUserKeytabUsername = loginUserKeytabUsername;
    }

    public String getLoginUserKeytabPath() {
        return loginUserKeytabPath;
    }

    public void setLoginUserKeytabPath(String loginUserKeytabPath) {
        this.loginUserKeytabPath = loginUserKeytabPath;
    }
}
